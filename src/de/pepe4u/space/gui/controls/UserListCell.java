package de.pepe4u.space.gui.controls;

import de.pepe4u.space.dto.CommunicationPartner;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;

/**
 * Custom list cell for contact list
 * 
 * @license    GPL 2 (http://www.gnu.org/licenses/gpl.html)
 * @author Philipp Neuser <info@pepe-4u.de>
 *
 */
public class UserListCell extends ListCell<CommunicationPartner> {
	
	private Parent n;
	private UserListCellController c;
	
	public UserListCell() {

		try {
		FXMLLoader loader = new FXMLLoader();
		n = loader.load(getClass().getResource("fxml/userlistcell.fxml").openStream());
		
		c = (UserListCellController)loader.getController();
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override
	protected void updateItem(CommunicationPartner item, boolean empty) {
		super.updateItem(item, empty);
		if(empty)
		{
			setText(null);
			setGraphic(null);
		}else{
			setCellContent(item);
		}
	}
	
	/**
	 * Fills the content of current cell
	 * @param cp
	 */
	protected void setCellContent(CommunicationPartner cp)
	{
		try {
			/**
			 * Highlight username when there is a new message
			 */
			if(cp.isNewMessageFlag())
				c.getLabelUser().setText("*"+cp.getName()+"*");
			else
				c.getLabelUser().setText(cp.getName());
			
			/**
			 * Set status text
			 */
			if(cp.isOnline())
				c.getLabelStatus().setText("online");				
			else
				c.getLabelStatus().setText("offline");
			
			/**
			 * No text for channels, we don't know if 
			 * somebody is online.
			 */
			if(cp.getName().startsWith("#"))
			{
				c.getLabelStatus().setText("");
			}
			
			/**
			 * Choose icon for buddy
			 */
			chooseImgForBuddy(cp, c);
			
			this.setGraphic(n);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Chooses an image for the buddy and sets it
	 * @param cp
	 * @param c
	 */
	private void chooseImgForBuddy(CommunicationPartner cp, UserListCellController c)
	{
		/**
		 * First check if comm-partner is a channel, then use channel images.
		 * For channels there are only online and new message available, hence we
		 * never know who is online in this channel.
		 */
		if(cp.getName().startsWith("#"))
		{
			if(cp.isNewMessageFlag())
				c.getImageIcon().setImage(new Image(getClass().getResourceAsStream("/res/imgs/channel_newmessage.png")));
			else
				c.getImageIcon().setImage(new Image(getClass().getResourceAsStream("/res/imgs/channel.png")));
			return;
		}
		
		/**
		 * Is there a new message, always show the image for new messages. 
		 * Does not matter if the comm-partner has gone offline since.
		 */
		if(cp.isNewMessageFlag()){
			c.getImageIcon().setImage(new Image(getClass().getResourceAsStream("/res/imgs/buddy_newmessage.png")));
			return;
		}
		
		/**
		 * No channel, no new messages, so lets simply decide between on- and offline.
		 */
		if(cp.isOnline())
			c.getImageIcon().setImage(new Image(getClass().getResourceAsStream("/res/imgs/buddy_online.png")));
		else
			c.getImageIcon().setImage(new Image(getClass().getResourceAsStream("/res/imgs/buddy_offline.png")));
	}
}
