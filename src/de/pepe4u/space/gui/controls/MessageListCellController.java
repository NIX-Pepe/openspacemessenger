package de.pepe4u.space.gui.controls;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.text.Text;

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
