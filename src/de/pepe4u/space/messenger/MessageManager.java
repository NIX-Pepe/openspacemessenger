package de.pepe4u.space.messenger;

import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

import de.pepe4u.space.dto.CommunicationMessage;
import de.pepe4u.space.dto.CommunicationPartner;

/**
 * This class is the heart of messenger service. It processes all incomming messages
 * manages the neighborhood of currents instance and manages the process of sending messages
 * to communication partners.
 * 
 * @license    GPL 2 (http://www.gnu.org/licenses/gpl.html)
 * @author Philipp Neuser <info@pepe-4u.de>
 *
 */
public class MessageManager {
	public static final int MESSAGE_DEFAULT_TTL = 7;
	
	private List<CommunicationPartner> myContacts;
	private List<CommunicationPartner> myNeighborhood;
	private List<CommunicationMessage> receivedMessages;
	private List<CommunicationPartner> receivedStateChanges;
	private int tcp_port;
	private int udp_port;
	private CommunicationPartner meAndMyself;
	private List<Integer> receivedMessageIds;
	private Object lockReceiveMessageIds;
	private Object lockReceivedMessages;
	private Object lockReceivedStateChanges;
	private Object lockMyNeighborhood;
	private Object lockMyContacts;
	private long messageCounter;
	
	public MessageManager(int tcp_port, int udp_port) {
		myContacts = new ArrayList<CommunicationPartner>();
		myNeighborhood = new ArrayList<CommunicationPartner>();
		receivedMessages = new ArrayList<CommunicationMessage>();
		receivedMessageIds = new ArrayList<Integer>();
		messageCounter = 0L;
		this.tcp_port = tcp_port;
		this.udp_port = udp_port;
		
		lockMyNeighborhood = new Object();
		lockReceivedMessages = new Object();
		lockReceiveMessageIds = new Object();
		lockReceivedStateChanges =  new Object();
		lockMyContacts = new Object();
	}
	
	/**
	 * Adds an Message to the Buffer
	 * @param comM
	 */
	private void addReceivedMessage(CommunicationMessage comM)
	{
		synchronized (lockReceivedMessages)
		{
			receivedMessages.add(comM);
		}
	}
	
	/**
	 * Gets all received Messages and clears the buffer.
	 * @return
	 */
	public List<CommunicationMessage> getAllReceivedMessages()
	{
		List<CommunicationMessage> lRet = new ArrayList<CommunicationMessage>();
		synchronized (lockReceivedMessages)
		{
			lRet.addAll(receivedMessages);
			// Remove alle received:
			receivedMessages.clear();
		}
		return lRet;
	}
	
	/**
	 * Processes the received telegramm.
	 * Is the message for this host, its added to received messages. otherwise the ttl is reduced and 
	 * the message is forwarded to other hosts.
	 * Channels start with #, in this case the message is kept and forwarded.
	 * @param telegramm
	 */
	public void processDirectTelegramm(String telegramm, String src_ip)
	{
		try{
			if(telegramm != null && telegramm.length() > 1)
			{
				telegramm = telegramm.trim();
				//System.out.println(telegramm);
				String[] parts = telegramm.split("\\|");
				if(telegramm.startsWith("D"))
				{
					if(parts[4].toLowerCase().equals(meAndMyself.getName().toLowerCase()) || parts[4].toLowerCase().startsWith("#"))
					{
						/*
						 * Is source contact in our contactlist?
						 */
						CommunicationPartner partner = null;
						synchronized (lockMyContacts) {
							for(CommunicationPartner cp : myContacts)
							{
								if((cp.getName().toLowerCase().equals(parts[3].toLowerCase()) && !parts[4].startsWith("#") ) || 
										(parts[4].startsWith("#") && cp.getName().toLowerCase().equals(parts[4])))
									partner = cp;
							}	
						}
						
						// we can't use changing parts like ttl when calculating hashcode
						Integer telegrammId = new String(parts[1]+parts[2]+parts[3]+parts[6]).hashCode();
						

						// Check if partner did not received this message before:
						boolean allreadyReceived = false;
						synchronized (lockReceiveMessageIds) {
							allreadyReceived = receivedMessageIds.contains(telegrammId);
							if(!allreadyReceived)
								receivedMessageIds.add(telegrammId);
						}
						
						// We allready got this telegramm, so stop this.
						if(allreadyReceived)
							return;
						
						if(partner != null)
						{
							CommunicationMessage cm = new CommunicationMessage();
							cm.setDate(new Date());
							cm.setMessage(parts[6]);
							cm.setMessageId(telegrammId+"");
							// if destination is a channel, add source as channelmember
							if(partner.getName().startsWith("#"))
							{
								CommunicationPartner cpSrc = new CommunicationPartner();
								cpSrc.setName(parts[3]);
								cm.setChannelMember(cpSrc);
							}
							
							cm.setPartner(partner);
							addReceivedMessage(cm);
						}
					}
					
					// If we have a message from a channel, we forward the message
					if(!parts[4].toLowerCase().equals(meAndMyself.getName().toLowerCase()) && Integer.parseInt(parts[5])-1 > 0)
					{
						// Not at the end of time to life, let's forward the message
						CommunicationPartner cpSrc = new CommunicationPartner();
						CommunicationPartner cpTarget = new CommunicationPartner();

						cpSrc.setName(parts[3]);
						cpTarget.setName(parts[4]);

						sendDirectMessageToCommPartner(cpSrc, cpTarget, Integer.parseInt(parts[5])-1, Long.parseLong(parts[1]), parts[6]);
					}
				}
				
				// process keep alive
				if(telegramm.startsWith("K"))
				{
					CommunicationPartner contact = null;
					synchronized (lockMyContacts) {
						for(CommunicationPartner cp : myContacts)
						{
							if(cp.getName().toLowerCase().equals(parts[2].toLowerCase()))
								contact = cp;
						}
					}
					if(contact != null)
					{
						CommunicationPartner cpStateChange = new CommunicationPartner();
						cpStateChange.setName(contact.getName());
						cpStateChange.setOnline(contact.isOnline());
						cpStateChange.setLastSeen(contact.getLastSeen());
						cpStateChange.setIp(src_ip);
						addStateChange(cpStateChange);
					}
					
					synchronized (lockMyNeighborhood) {
						boolean found_in_neighborhood = false;
						CommunicationPartner src = new CommunicationPartner();
						src.setIp(src_ip);
						src.setName(parts[2]);
						/**
						 * Check if we knew this guy or send his message to our neighborhood
						 */
						for(CommunicationPartner cp : myNeighborhood)
						{
							if(cp.getName().equals(parts[2]))
							{
								found_in_neighborhood = true;
								cp.setIp(src_ip);
								cp.setOnline(true);
								cp.setLastSeen(new Date().getTime());
							}else{
								forwardKeepAliveMessage(src, cp, Integer.parseInt(parts[1]) - 1);
							}
						}
						/**
						 * We didn't knew this partner before, so add him
						 */
						if(!found_in_neighborhood)
						{
							if(!src.getName().equals(meAndMyself.getName()))
								myNeighborhood.add(src);
						}
					}
					
				}
			}
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Sends a Message to a CommPartner via target host
	 * @param cpMessageTarget
	 * @param cpHostTarget
	 * @param text
	 * @return
	 */
	public boolean sendDirectMessageToCommPartner(CommunicationPartner cpSrc, CommunicationPartner cpMessageTarget, CommunicationPartner cpHostTarget, int ttl, long messageCnt, String text)
	{
		String telegramm = "D|"+messageCnt+"|"+new Date()+"|"+cpSrc.getName()+"|"+cpMessageTarget.getName()+"|"+ttl+"|"+text.replaceAll("\n", "").replaceAll("\\|", "");
		try {
			Socket s = new Socket();
			s.connect(new InetSocketAddress(cpHostTarget.getIp(),tcp_port),5000);
			if(s.isConnected())
			{
				PrintWriter pw = new PrintWriter(s.getOutputStream());
				pw.println(telegramm);
				pw.flush();
			}
			s.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			
			setCommpartnerOffline(cpHostTarget);
		} 
		return false;
	}
	
	/**
	 * Sets the offline flag to a communication partner
	 * @param cpHost
	 */
	private void setCommpartnerOffline(CommunicationPartner cpHost)
	{
		/**
		 * Set neighbor offline
		 */
		synchronized (lockMyNeighborhood) {
			for(CommunicationPartner cp: myNeighborhood)
			{
				if(cp.getName().equals(cpHost.getName()))
					cp.setOnline(false);
			}
		}
		
		/**
		 * Create state chane
		 */
		CommunicationPartner cpStateChange = new CommunicationPartner();
		cpStateChange.setName(cpHost.getName());
		cpStateChange.setIp(cpHost.getIp());
		cpStateChange.setOnline(false);
		cpStateChange.setLastSeen(cpHost.getLastSeen());
		addStateChange(cpStateChange);
	}
	
	/**
	 * Adds a state change to the list of state changes
	 * @param cpStateChange
	 */
	private void addStateChange(CommunicationPartner cpStateChange)
	{
		synchronized (lockReceivedStateChanges) {
			receivedStateChanges.add(cpStateChange);
		}
	}
	/**
	 * Returns the current message number and increments the counter.
	 * @return
	 */
	private synchronized long getMessageCount()
	{
		messageCounter += 1;
		return messageCounter;
	}
	
	/**
	 * Trys to send a message directly to a communication partner. Is the partner not available directly, 
	 * messages are distributed to the network.
	 * @param cpTarget
	 * @param ttl
	 * @param text
	 */
	public void sendDirectMessageToCommPartner(CommunicationPartner cpSrc,CommunicationPartner cpTarget, int ttl, long messageCnt, String text)
	{
		if(cpTarget.getName().startsWith("#") || !sendDirectMessageToCommPartner(cpSrc,cpTarget, cpTarget, ttl, messageCnt, text))
		{
			List<CommunicationPartner> possibleTargets = new ArrayList<CommunicationPartner>();
			synchronized (lockMyNeighborhood) {
				possibleTargets.addAll(myNeighborhood);
			}
			for(CommunicationPartner target : possibleTargets)
			{
				sendDirectMessageToCommPartner(cpSrc,cpTarget, target, ttl, messageCnt, text);
			}
			removeOfflineNeighborhood();
		}
	}
	
	/**
	 * Sends a keep alive message to a target
	 * @param cpSrc
	 * @param cpTarget
	 * @param ttl
	 */
	private void forwardKeepAliveMessage(CommunicationPartner cpSrc, CommunicationPartner cpTarget, int ttl)
	{
		// Don't send with a ttl less than 1
		if(ttl < 1)
			return;
		String telegramm = "K|"+ttl+"|"+cpSrc.getName();
		try{
			DatagramPacket dp = new DatagramPacket(telegramm.getBytes(),telegramm.length());
			dp.setAddress(InetAddress.getByName(cpTarget.getIp()));
			dp.setPort(udp_port);
			DatagramSocket socket = new DatagramSocket();
			socket.send(dp);
			socket.close();
		}catch(Exception e)
		{
			e.printStackTrace();
			setCommpartnerOffline(cpTarget);
		}
		
	}
	
	/**
	 * Creates a keep alive message for ourself and sends it to a target.
	 * @param target
	 */
	public void sendKeepAlivePackage(CommunicationPartner target)
	{
		forwardKeepAliveMessage(meAndMyself, target, MESSAGE_DEFAULT_TTL);
	}
	
	/**
	 * Sends a keep alive package to all of our neighbors
	 */
	public void sendKeepAliveToNeighborhood()
	{
		List<CommunicationPartner> possibleTargets = new ArrayList<CommunicationPartner>();
		synchronized (lockMyNeighborhood) {
			possibleTargets.addAll(myNeighborhood);
		}
		for(CommunicationPartner target : possibleTargets)
		{
			sendKeepAlivePackage(target);
		}
	}
	
	/**
	 * send a keep alive package to broadcast adress
	 */
	public void sendKeepAliveToBroadcast()
	{
		try {
			/**
			 * First we need to iterate over all interfaces
			 */
			Enumeration<NetworkInterface> list = NetworkInterface.getNetworkInterfaces();
			while(list.hasMoreElements())
			{
				NetworkInterface iface = list.nextElement();
				if(iface != null && !iface.isLoopback() && iface.isUp())
				{
					/**
					 * and get all interface addresses
					 */
					for(InterfaceAddress addr : iface.getInterfaceAddresses())
					{
						if(addr != null)
						{
							/**
							 * having the address, we can get the broadcast address for given network
							 */
							if(addr.getBroadcast() != null)
							{
								CommunicationPartner cp = new CommunicationPartner();
								cp.setIp(addr.getBroadcast().getHostAddress());
								System.out.println("Send broadcast to: "+addr.getBroadcast().getHostAddress());
								sendKeepAlivePackage(cp);
							}
						}
					}
				}
			}
		} catch (SocketException e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Removes offline neighbors from neighborhood
	 */
	private void removeOfflineNeighborhood()
	{
		synchronized (lockMyNeighborhood) {
			List<CommunicationPartner> lToRemove = new ArrayList<CommunicationPartner>();
			for(CommunicationPartner cp : myNeighborhood)
			{
				if(!cp.isOnline())
				{
					lToRemove.add(cp);
				}
			}
			for(CommunicationPartner cp : lToRemove)
				myNeighborhood.remove(cp);
		}
	}
	
	/**
	 * Send as message to given communication partner
	 * @param cpTarget
	 * @param text
	 */
	public void sendDirectMessageToCommPartner(CommunicationPartner cpTarget, String text)
	{
		sendDirectMessageToCommPartner(meAndMyself,cpTarget, MESSAGE_DEFAULT_TTL, getMessageCount(), text);
	}

	/**
	 * Communication partner dataset of myself
	 * @return
	 */
	public CommunicationPartner getMeAndMyself() {
		return meAndMyself;
	}

	public void setMeAndMyself(CommunicationPartner meAndMyself) {
		this.meAndMyself = meAndMyself;
	}
	
	/**
	 * Add a partner to contacts
	 * @param cp
	 */
	public void addCommPartnerToContacts(CommunicationPartner cp)
	{
		synchronized (lockMyContacts) {
			myContacts.add(cp);
		}
	}
	
	/**
	 * Remove a contact from contacts
	 * @param cp
	 */
	public void removeCommPartnerFromContacts(CommunicationPartner cp)
	{
		synchronized (lockMyContacts) {
			CommunicationPartner del_tmp = null;
			for(CommunicationPartner cp_tmp : myContacts)
			{
				if(cp.getName().equals(cp_tmp.getName()))
					del_tmp = cp_tmp;
			}
			if(del_tmp != null)
				myContacts.remove(del_tmp);
		}
	}

	/**
	 * Returns all state changes and clears list...
	 * @return
	 */
	public List<CommunicationPartner> getReceivedStateChanges() {
		List<CommunicationPartner> lRet = new ArrayList<>();
		synchronized (lockReceivedStateChanges) {
			lRet.addAll(receivedStateChanges);
			receivedStateChanges.clear();
		}
		return receivedStateChanges;
	}
}
