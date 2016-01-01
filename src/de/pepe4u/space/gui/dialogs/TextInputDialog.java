package de.pepe4u.space.gui.dialogs;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Simple helper class needed as long as TextInputDialog from java standard
 * is not available on most of the devices
 * 
 * @license    GPL 2 (http://www.gnu.org/licenses/gpl.html)
 * @author Philipp Neuser <info@pepe-4u.de>
 *
 */
public class TextInputDialog {

	@FXML
	private Label labelLabel;
	
	@FXML
	private Label labelText;
	
	@FXML
	private TextField textValue;
	
	private String text;
	private String label;
	private String titel;
	
	private String returnValue;
	
	private Stage rootStage;

	public TextInputDialog(String text, String label, String titel)
	{
		this.text = text;
		this.label = label;
		this.titel = titel;
		returnValue = "";
	}
	
	@FXML
	private void handleOKPressed()
	{
		returnValue = textValue.getText();
		rootStage.close();
	}
	
	@FXML
	private void handleCancelPressed()
	{
		rootStage.close();
	}

	public String showTextInputDialog()
	{
		Stage stage = new Stage();
		FXMLLoader loader = new FXMLLoader(getClass().getResource("fxml/textinputdialog.fxml"));
		loader.setController(this);
		Parent root = null;
		try {
			root = loader.load();

	        Scene scene = new Scene(root, 400, 80);
	        
	        labelLabel.setText(label);
	        labelText.setText(text);
	        stage.setTitle(titel);
	        stage.setScene(scene);
		    
			rootStage = stage;
	        
	        stage.showAndWait();
	        
		} catch (Exception e) {
			e.printStackTrace();
		}
        
        return returnValue;
	}
}
