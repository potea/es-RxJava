package controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;

import java.net.URL;
import java.util.ResourceBundle;

public class MainPresenter implements Initializable {
    @FXML private GridPane board;
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
        boardController.startRaining();
    }

    private void tryRemoveAcid(String sentence){
        System.out.println(sentence);
        if(boardController.removeSentence(sentence)){
            textField.setText("");
        }
    }
}
