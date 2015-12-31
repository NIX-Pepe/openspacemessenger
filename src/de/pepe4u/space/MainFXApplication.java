package de.pepe4u.space;

import java.io.File;
import java.util.ArrayList;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import de.pepe4u.space.config.CommunicationPartnerList;
import de.pepe4u.space.dto.CommunicationPartner;
import de.pepe4u.space.gui.stages.MainStageControl;
import de.pepe4u.space.messenger.DirectMessageServerThread;
import de.pepe4u.space.messenger.KeepAliveTrigger;
import de.pepe4u.space.messenger.MessageManager;
import de.pepe4u.space.messenger.PublicMessageServerThread;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


/**
 * Main Class for application bootstrapping
 * 
 * @license    GPL 2 (http://www.gnu.org/licenses/gpl.html)
 * @author Philipp Neuser <info@pepe-4u.de>
 *
 */
public class MainFXApplication extends Application {
	
	private static final int UDP_PORT = 15668;
	private static final int TCP_PORT = 15669;
	private static final String CONTACT_LIST_DIR = ".OpenSpaceMessenger";
	private static final String CONTACT_LIST_FILE = "ContactList.xml";
	
	private MessageManager mm;
	private PublicMessageServerThread pmst;
	private DirectMessageServerThread dmst;
	private KeepAliveTrigger kat;
	private MainStageControl msc;
	
	private CommunicationPartner createMeAndMyself()
	{
		CommunicationPartner cp = new CommunicationPartner();
		cp.setName(System.getProperty("user.name"));
		return cp;
				
	}
	
	/**
	 * Returns the path to the contactlist file
	 * @return
	 */
	private String getContactListFilePath()
	{
		/**
		 * Get home dir for user
		 */
		String home_dir = System.getProperty("user.home");
		if(home_dir == null || home_dir.isEmpty())
			home_dir = "";
		/**
		 * Check if directory exists or create it
		 */
		File d = new File(home_dir+"/"+CONTACT_LIST_DIR);
		if(!d.exists() || !d.isDirectory())
			d.mkdir();
		/**
		 * Return filename
		 */
		return home_dir+"/"+CONTACT_LIST_DIR+"/"+CONTACT_LIST_FILE;
	}
	
	/**
	 * Bootstraps all network services
	 */
	private void bootstrapMessageServices()
	{
		// create message service
		mm = new MessageManager(TCP_PORT, UDP_PORT);
		try {
			// initialize instances
			pmst = new PublicMessageServerThread(mm, UDP_PORT);
			dmst = new DirectMessageServerThread(mm, TCP_PORT);
			kat = new KeepAliveTrigger(mm);
			// start threads
			pmst.start();
			dmst.start();
			kat.start();
			
			mm.setMeAndMyself(this.createMeAndMyself());
			mm.sendKeepAliveToBroadcast();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Loads contactlist from xml-file
	 */
	private void loadContactList()
	{
		Serializer serializer = new Persister();
		
		/**
		 * Load contact list from home dir
		 */
		File source = new File(getContactListFilePath());

		try {
			CommunicationPartnerList cpl = serializer.read(CommunicationPartnerList.class, source);
			for(CommunicationPartner cp : cpl.getCommunicationPartnerList())
			{
				mm.addCommPartnerToContacts(cp);
				msc.getListOfCommpartners().add(cp);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Saves contactlist to xml-file
	 */
	private void saveContactList()
	{		
		Serializer serializer = new Persister();
		CommunicationPartnerList cpl = new CommunicationPartnerList();
		cpl.setCommunicationPartnerList(new ArrayList<CommunicationPartner>());
		cpl.getCommunicationPartnerList().addAll(msc.getListOfCommpartners());
		/**
		 * Save contact list to home dir
		 */
		File result = new File(getContactListFilePath());

		try {
			serializer.write(cpl, result);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

	@Override
	public void start(Stage arg0) throws Exception {
		
		this.bootstrapMessageServices();
		
		Stage stage = new Stage();
		FXMLLoader loader = new FXMLLoader();
		Parent root = loader.load(getClass().getResource("gui/stages/fxml/mainstage.fxml").openStream());
	    
        Scene scene = new Scene(root, 600, 500);
        
        stage.setTitle("openSpaceMessenger 2.0rc");
        stage.setScene(scene);
        
        // Shutdown threads when closing application
        stage.setOnCloseRequest( ae -> {pmst.shutdownThread(); dmst.shutdownThread(); kat.shutdownThread(); saveContactList();});
        
        // Set message service:
        msc = (MainStageControl)loader.getController();
        msc.setMessageManager(mm);
        
        // load contact list
        loadContactList();
        
        stage.show();
	}

	public static void main (String[] args)
	{
		launch(args);
	}
	
}
