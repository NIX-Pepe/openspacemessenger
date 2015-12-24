package de.pepe4u.space.gui.controls;

import java.io.IOException;

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
			FXMLLoader loader = new FXMLLoader();
			Parent n = loader.load(getClass().getResource("fxml/messagelistcell.fxml").openStream());
			
			MessageListCellController c = (MessageListCellController)loader.getController();
			c.getLabelName().setText((comM.getChannelMember() != null ? comM.getChannelMember().getName() : comM.getPartner().getName())+" ("+comM.getDate()+")");
			c.getTextMessage().setText(comM.getMessage());
			
			this.setGraphic(n);
			//setText(comP.getName());
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
