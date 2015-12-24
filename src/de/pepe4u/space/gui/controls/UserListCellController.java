package de.pepe4u.space.gui.controls;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;

/**
 * FX controller for contact list cell items
 *  * 
 * @license    GPL 2 (http://www.gnu.org/licenses/gpl.html)
 * @author Philipp Neuser <info@pepe-4u.de>
 *
 */
public class UserListCellController {
	@FXML
	private ImageView imageIcon;
	@FXML
	private Label labelUser;
	@FXML
	private Label labelStatus;
	
	public ImageView getImageIcon() {
		return imageIcon;
	}
	public Label getLabelUser() {
		return labelUser;
	}
	public Label getLabelStatus() {
		return labelStatus;
	}
}
