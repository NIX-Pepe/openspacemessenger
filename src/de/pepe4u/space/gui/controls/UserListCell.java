package de.pepe4u.space.gui.controls;

import java.io.IOException;

import de.pepe4u.space.dto.CommunicationPartner;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ListCell;

/**
 * Custom list cell for contact list
 * 
 * @license    GPL 2 (http://www.gnu.org/licenses/gpl.html)
 * @author Philipp Neuser <info@pepe-4u.de>
 *
 */
public class UserListCell extends ListCell<CommunicationPartner> {
	
	public UserListCell() {
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
	
	protected void setCellContent(CommunicationPartner comP)
	{
		try {
			FXMLLoader loader = new FXMLLoader();
			Parent n = loader.load(getClass().getResource("fxml/userlistcell.fxml").openStream());
			
			UserListCellController c = (UserListCellController)loader.getController();
			if(comP.isNewMessageFlag())
				c.getLabelUser().setText("*"+comP.getName()+"*");
			else
				c.getLabelUser().setText(comP.getName());
			
			if(comP.isOnline())
				c.getLabelStatus().setText("online");
			else
				c.getLabelStatus().setText("offline");
			if(comP.getName().startsWith("#"))
				c.getLabelStatus().setText("");
			
			this.setGraphic(n);
			//setText(comP.getName());
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
