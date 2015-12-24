package de.pepe4u.space.messenger;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class PublicMessageServerThread extends Thread {
	

	private DatagramSocket server;
	private MessageManager mm;
	private boolean keepRunning;
	
	public PublicMessageServerThread (MessageManager mm, int port) throws SocketException
	{
		server = new DatagramSocket(port);
		this.mm = mm;
		keepRunning = true;
	}
	
	@Override
	public void run() {
		try{
			while(keepRunning)
			{
				/**
				 * Get packet and process message
				 */
				byte buff[] = new byte[1024];
				DatagramPacket rcv_packet = new DatagramPacket(buff, 1024);
				server.receive(rcv_packet);
				String line = new String(rcv_packet.getData());
				mm.processDirectTelegramm(line, rcv_packet.getAddress().getHostAddress());
			}
			
		}catch(Exception e)
		{
			//nothing to do
		}
	}
	
	public void shutdownThread()
	{
		try {
			keepRunning = false;
			server.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
