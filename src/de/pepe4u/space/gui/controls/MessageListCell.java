package de.pepe4u.space.gui.controls;

import de.pepe4u.space.dto.CommunicationMessage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ListCell;

/**
 * Custom list cell for messages
 *   
 * @license    GPL 2 (http://www.gnu.org/licenses/gpl.html)
 * @author Philipp Neuser <info@pepe-4u.de>
 *
 */
public class MessageListCell extends ListCell<CommunicationMessage> {
	
	private Parent n;
	private MessageListCellController c;
	
	public MessageListCell() {
		try {
			FXMLLoader loader = new FXMLLoader();
			n = loader.load(getClass().getResource("fxml/messagelistcell.fxml").openStream());		
			c = (MessageListCellController)loader.getController();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	protected void updateItem(CommunicationMessage item, boolean empty) {
		super.updateItem(item, empty);
		if(empty)
		{
			setText(null);
			setGraphic(null);
		}else{
			setCellContent(item);
		}
	}
	
	protected void setCellContent(CommunicationMessage comM)
	{
		try {
			c.getLabelName().setText((comM.getChannelMember() != null ? comM.getChannelMember().getName() : comM.getPartner().getName())+" ("+comM.getDate()+")");
			c.getTextMessage().setText(comM.getMessage());
			
			this.setGraphic(n);
			//setText(comP.getName());
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
