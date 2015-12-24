package de.pepe4u.space.gui.controls;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.text.Text;

/**
 * FX controller for message list cells
 * 
 * @license    GPL 2 (http://www.gnu.org/licenses/gpl.html)
 * @author Philipp Neuser <info@pepe-4u.de>
 *
 */
public class MessageListCellController {
	@FXML
	private Label labelName;
	@FXML
	private Text textMessage;
	public Label getLabelName() {
		return labelName;
	}
	public Text getTextMessage() {
		return textMessage;
	}
}
