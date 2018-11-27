package controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;

import java.net.URL;
import java.util.ResourceBundle;

public class MainPresenter implements Initializable {
    @FXML private AnchorPane board;
    @FXML private Button tryRemoveButton;
    @FXML private TextField textField;
    @FXML private BoardController boardController;

    @Override
    public void initialize(URL arg0, ResourceBundle arg1){
        tryRemoveButton.setOnMouseClicked( event -> {
            tryRemoveAcid(textField.getText());
        });

        textField.setOnKeyPressed(event -> {
            if(event.getCode().equals(KeyCode.ENTER))
                tryRemoveAcid(textField.getText());
        });

        textField.setFocusTraversable(true);
        boardController.startRaining(2000,500);
    }

    private void tryRemoveAcid(String sentence){
        System.out.println(sentence);
        if(boardController.removeSentence(sentence)){
            textField.setText("");
        }
    }
}
