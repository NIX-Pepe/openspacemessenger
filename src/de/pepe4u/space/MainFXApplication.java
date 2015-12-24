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

public class MainFXApplication extends Application {
	
	private static final int UDP_PORT = 15668;
	private static final int TCP_PORT = 15669;
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
	
	private void loadContactList()
	{
		Serializer serializer = new Persister();
		File source = new File(CONTACT_LIST_FILE);

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
	
	private void saveContactList()
	{
		Serializer serializer = new Persister();
		CommunicationPartnerList cpl = new CommunicationPartnerList();
		cpl.setCommunicationPartnerList(new ArrayList<CommunicationPartner>());
		cpl.getCommunicationPartnerList().addAll(msc.getListOfCommpartners());
		File result = new File(CONTACT_LIST_FILE);

		try {
			serializer.write(cpl, result);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

	@Override
	public void start(Stage arg0) throws Exception {
		/*MainStageControl mainstage = new MainStageControl();
		Stage stage = new Stage();
		stage.setScene(new Scene(mainstage));
		stage.setTitle("Custom Control");
		stage.setWidth(300);
		stage.setHeight(200);
		stage.show();*/
		
		this.bootstrapMessageServices();
		
		Stage stage = new Stage();
		FXMLLoader loader = new FXMLLoader();
		Parent root = loader.load(getClass().getResource("gui/stages/fxml/mainstage.fxml").openStream());
	    
        Scene scene = new Scene(root, 600, 500);
    
        stage.setTitle("openSpaceMessenger 1.0");
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
