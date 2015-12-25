package de.pepe4u.space.gui.stages;

import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import com.sun.org.apache.xml.internal.security.encryption.CipherReference;

import de.pepe4u.space.dto.CommunicationMessage;
import de.pepe4u.space.dto.CommunicationPartner;
import de.pepe4u.space.gui.controls.MessageListCell;
import de.pepe4u.space.gui.controls.UserListCell;
import de.pepe4u.space.messenger.MessageManager;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Duration;

/**
 * FX controller of main view
 * 
 * @license    GPL 2 (http://www.gnu.org/licenses/gpl.html)
 * @author Philipp Neuser <info@pepe-4u.de>
 *
 */
public class MainStageControl extends Parent implements Initializable {

	private CommunicationPartner currentComPartner;
	
	private MessageManager mm;
	
	@FXML
	private TextField textMessage;
	
	@FXML
	private ListView<CommunicationPartner> listUser;
	
	@FXML
	private ListView<CommunicationMessage> listMessages;
	
	@FXML
	private ObservableList<CommunicationPartner> listOfCommpartners;
	
	@FXML
	private Label labelUser;
	
	private Timeline timelineMessages;
	
	private Timeline timelineStateChanges;
	
	@FXML
	private Parent rootPane;

	private CommunicationMessage createTestMessage(String text, CommunicationPartner cp)
	{
		CommunicationMessage cm = new CommunicationMessage();
		cm.setDate(new Date());
		cm.setMessage(text);
		cm.setMessageId("Test");
		cm.setPartner(cp);
		
		return cm;
	}
	
	public MainStageControl() {
		listOfCommpartners = FXCollections.observableArrayList();	
	}
	
	@FXML
	protected void handleUserClicked(){
		System.out.println("Test");
	}

	@FXML
	protected void handleSendMessageOnEnter()
	{
		handleSendMessage();
	}
	
	@FXML
	protected void handleSendMessage()
	{
		if(currentComPartner != null)
		{
			if(!currentComPartner.getName().startsWith("#"))
			{
				currentComPartner.getMessages().add(createTestMessage(textMessage.getText(), mm.getMeAndMyself()));
				listMessages.getItems().add(createTestMessage(textMessage.getText(), mm.getMeAndMyself()));
			}
			mm.sendDirectMessageToCommPartner(currentComPartner, textMessage.getText());
			textMessage.setText(null);
		}
	}

	public ObservableList<CommunicationPartner> getListOfCommpartners() {
		return listOfCommpartners;
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		listUser.setItems(listOfCommpartners);
		listUser.setCellFactory(new Callback<ListView<CommunicationPartner>, ListCell<CommunicationPartner>>() {

			@Override
			public ListCell<CommunicationPartner> call(ListView<CommunicationPartner> arg0) {
				return new UserListCell();
			}
		});
		
		listMessages.setCellFactory(new Callback<ListView<CommunicationMessage>, ListCell<CommunicationMessage>>() {
			
			@Override
			public ListCell<CommunicationMessage> call(ListView<CommunicationMessage> param) {
				return new MessageListCell();
			}
		});
		
		/**
		 * Initialize additional handlers
		 */
		listUser.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		listUser.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<CommunicationPartner>() {

			@Override
			public void changed(ObservableValue<? extends CommunicationPartner> observable, CommunicationPartner oldValue, CommunicationPartner newValue) {
					handleSelectionOfUserChanged(oldValue, newValue);
			}
		});
		
		/**
		 * Set timer for message retrieval
		 */
		timelineMessages = new Timeline(new KeyFrame(
		        Duration.millis(2500),
		        ae -> processNewMessages()));
		timelineMessages.setCycleCount(Animation.INDEFINITE);
		timelineMessages.play();
	}
	
	@FXML
	protected void handleAddCommPartner()
	{
		TextInputDialog dialog = new TextInputDialog("");
		dialog.setTitle("Add CommPartner");
		dialog.setHeaderText("Add a user from your domain");
		dialog.setContentText("Please enter username:");

		Optional<String> result = dialog.showAndWait();
		if (result.isPresent()){
		    CommunicationPartner cp = new CommunicationPartner();
		    cp.setOnline(false);
		    cp.setName(result.get());
		    if(cp.getName() != null && !cp.getName().trim().isEmpty())
		    {
		    	listOfCommpartners.add(cp);
		    	mm.addCommPartnerToContacts(cp);
		    }
		} 
	}
	
	@FXML
	protected void handleRemoveCommPartner()
	{
		CommunicationPartner tmp_cp = listUser.getSelectionModel().getSelectedItem();
		if(tmp_cp != null)
		{
			tmp_cp.setDeleteFlag(true);
			mm.removeCommPartnerFromContacts(tmp_cp);
			listOfCommpartners.remove(tmp_cp);
		}
	}
	
	private void handleSelectionOfUserChanged(CommunicationPartner oldValue, CommunicationPartner newValue)
	{
		listMessages.setItems(null);
		if(newValue != null)
			listMessages.setItems(FXCollections.observableArrayList(newValue.getMessages()));
		currentComPartner = newValue;
		/**
		 * Notify list about changes
		 */
		if(currentComPartner != null)
		{
			currentComPartner.setNewMessageFlag(false);
			if(oldValue != null && !oldValue.isDeleteFlag())
				listUser.fireEvent(new ListView.EditEvent<>(listUser, ListView.editCommitEvent(), currentComPartner, listUser.getItems().indexOf(currentComPartner)));
		}
		/**
		 * Scroll to the end
		 */
		if(listMessages.getItems() != null && listMessages.getItems().size() > 0)
			listMessages.scrollTo(listMessages.getItems().size()-1);
	}
	
	public void setMessageManager(MessageManager mm)
	{
		this.mm = mm;
		labelUser.setText("User: "+mm.getMeAndMyself().getName());
	}
	

	
	/**
	 * Gets new message from MM and distributes them to contacts and current view
	 */
	protected void processNewMessages()
	{
		try{
			/**
			 * This is for later user, when need to push window to front.
			 */
			Stage stage = (Stage)rootPane.getScene().getWindow();
			/**
			 * Get all messages from MessageManager...
			 */
			List<CommunicationMessage> lMessages = mm.getAllReceivedMessages();
			for(CommunicationMessage cm : lMessages)
			{
				/**
				 * First check, if the message is for commpartner currently shown.
				 */
				if(currentComPartner != null && currentComPartner.getName().equals(cm.getPartner().getName()))
				{
					currentComPartner.getMessages().add(cm);
					listMessages.getItems().add(cm);
					/**
					 * Notify user by push messenger to front
					 */
					stage.toFront();
				}else{
					/**
					 * ok, not for the shown one. we need to iterate...
					 */
					for(CommunicationPartner cp : listOfCommpartners)
					{
						try{
							if(cp.getName().equals(cm.getPartner().getName()))
							{
								cp.getMessages().add(cm);
								cp.setNewMessageFlag(true);
								
								/**
								 * Notify list about changes
								 */
								listUser.fireEvent(new ListView.EditEvent<>(listUser, ListView.editCommitEvent(), cp, listUser.getItems().indexOf(cp)));
								/**
								 * Notify user by push messenger to front
								 */
								stage.toFront();
							}
						}catch(Exception e)
						{
							e.printStackTrace();
						}
					}
				}
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Processes state changes received from network managements
	 */
	private void processStateChanges()
	{
		/**
		 * Get all state changes and iterate over changes
		 */
		List<CommunicationPartner> lReceivedStateChanges = mm.getReceivedStateChanges();
		for(CommunicationPartner cpContact : listOfCommpartners)
		{
			for(CommunicationPartner cpStateChange : lReceivedStateChanges)
			{
				if(cpStateChange.getName() != null && cpStateChange.getName().toLowerCase().equals(cpContact.getName().toLowerCase()))
				{
					/**
					 * Found a match, so start to copy states
					 */
					cpContact.setLastSeen(cpStateChange.getLastSeen());
					cpContact.setIp(cpStateChange.getIp());
					cpContact.setOnline(cpStateChange.isOnline());
					break; // stop iteration
				}
			}
			
			/**
			 * Check if last keep alive message is older than 30 seconds
			 */
			if(cpContact.isOnline() && (new Date().getTime() - 30L) > cpContact.getLastSeen())
				cpContact.setOnline(false);
			
			/**
			 * Update view
			 */
			listUser.fireEvent(new ListView.EditEvent<>(listUser, ListView.editCommitEvent(), cpContact, listUser.getItems().indexOf(cpContact)));
		}
	}
}
