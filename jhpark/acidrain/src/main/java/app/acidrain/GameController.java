package app.acidrain;


import com.github.javafaker.Faker;
import io.reactivex.Observable;
import io.reactivex.functions.Predicate;
import io.reactivex.rxjavafx.observables.JavaFxObservable;
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler;
import io.reactivex.subjects.PublishSubject;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;

public class GameController implements Initializable {
    @FXML AnchorPane pane;
    @FXML Label labelScore;
    @FXML Label labelLife;
    @FXML TextField textField;

    @Override
    public void initialize(URL location, ResourceBundle resourceBundle) {
        startGame();
    }

    private void startGame() {
        final GameData gameData = new GameData();
        gameData.wordRemoveObservable()
                .subscribe(label -> pane.getChildren().remove(label));
        gameData.lifeChangeObservable()
                .subscribe(life -> labelLife.setText(String.valueOf(Math.max(life, 0))));
        gameData.scoreChangeObservable()
                .subscribe(score -> labelScore.setText(String.valueOf(score)));

        JavaFxObservable.eventsOf(textField, ActionEvent.ACTION)
                .subscribe(ae -> {
                    gameData.increaseScore(textField.getText().length());
                    gameData.removeWord(textField.getText());
                    textField.clear();
                });

        PublishSubject.create().mergeWith(Observable.interval(0, 3, TimeUnit.SECONDS))
                .observeOn(JavaFxScheduler.platform())
                .takeUntil((Predicate<? super Object>) n -> gameData.isDied())
                .doOnNext(showTime -> {
                    final Label label = gameData.createWord();
                    label.setLayoutX(new Faker().random().nextInt((int) pane.getWidth()));
                    pane.getChildren().add(label);

                    final double height = pane.getHeight();
                    JavaFxObservable.interval(Duration.millis(100))
                            .takeUntil(n -> touchDown(label, height) || gameData.isDied() || gameData.alreadyCleared(label))
                            .doOnNext(downTime -> label.setLayoutY(Math.min(label.getLayoutY() + 10, height)))
                            .doOnComplete(() -> {
                                if (!gameData.alreadyCleared(label)) {
                                    gameData.reduceLife();
                                    pane.getChildren().remove(label);
                                }
                            })
                            .subscribe();
                })
                .doOnComplete(() -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("You Died.");
                    alert.setHeaderText("You Died.");
                    alert.setContentText("You Died.");

                    JavaFxObservable.fromDialog(alert)
                            .doOnSuccess(buttonType -> {
                                Platform.exit();
                                System.exit(0);
                            })
                            .filter(response -> response.equals(ButtonType.OK))
                            .subscribe();
                })
                .subscribe();
    }

    private boolean touchDown(Label label, double height) {
        return label.getLayoutY() >= height;
    }
}