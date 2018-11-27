package controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import model.SentenceMaker;
import model.SentencesManager;
import model.vo.Sentence;
import rx.Subscription;

import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class BoardController implements Initializable {

    @FXML
    private AnchorPane pane;

    private SentenceMaker sentenceMaker;
    private SentencesManager sentencesManager;
    private Map<Sentence, Node> labelMap;
    private Random random;
    private Double paneHeight;
    private Double paneWidth;

    private Subscription makeSentenceSubscription;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        sentenceMaker = new SentenceMaker();

        random = new Random();
        labelMap = new HashMap<>();

        pane.heightProperty().addListener((observable, oldValue, newValue) -> paneHeight = (Double) newValue);
        pane.widthProperty().addListener((observable, oldValue, newValue) -> paneWidth = (Double) newValue);
    }

    public void startRaining(int maxIntervalMS, int minIntervalMS) {
        sentencesManager = new SentencesManager();
        sentencesManager.getSentencesHotObservable()
                .buffer(100, TimeUnit.MICROSECONDS)
                .subscribe(this::updateView,
                        this::logging,
                        this::gameOver);

        makeSentenceSubscription = sentenceMaker.getSentences(maxIntervalMS, minIntervalMS)
                .doOnNext(this::visibleSentence)
                .subscribe(sentencesManager::addSentences,
                        this::logging);
    }

    private void visibleSentence(Sentence sentence) {
        Platform.runLater(() -> {            //뷰에 글자 생성
            Label node = new Label(sentence.getSentence());
            double margin = sentence.getSentence().length() * 7;
            double xPos = margin + random.nextDouble() * (paneWidth - 2 * margin);
            node.setLayoutX(xPos);
            pane.getChildren().add(node);
            labelMap.put(sentence, node);
        });
    }

    private void updateView(List<Sentence> sentences) {
        Platform.runLater(() -> sentences
                .stream()
                .filter(labelMap::containsKey)
                .forEach(sentence -> labelMap.get(sentence).setLayoutY(paneHeight * 0.95 * sentence.getHeight() / 1000)));
    }

    private void gameOver() {
        makeSentenceSubscription.unsubscribe();
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("게임오바아");
            alert.setHeaderText("게임 오버되었습니다.");
            alert.showAndWait();
            restart();
        });
        System.out.println("gameOver");
    }

    private void logging(Throwable e) {
        e.printStackTrace();
    }

    public boolean removeSentence(String sentence) {
        Sentence removed = sentencesManager.popSentence(sentence);
        boolean removeSuccess = removed != null;
        if (removeSuccess) {
            pane.getChildren().remove(labelMap.get(removed));
            labelMap.remove(removed);
        }
        return removeSuccess;
    }

    private void restart() {
        labelMap.keySet()
                .stream()
                .map(Sentence::getSentence)
                .collect(Collectors.toList())
                .forEach(this::removeSentence);
        this.startRaining(1000, 500);
    }
}
