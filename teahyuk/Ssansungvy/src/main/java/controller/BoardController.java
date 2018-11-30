package controller;

import io.reactivex.disposables.Disposable;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import model.SentenceMaker;
import model.SentencesManager;
import model.vo.GameLevel;
import model.vo.Sentence;

import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class BoardController implements Initializable {

    private int LEVEL_UP_COUNT = 10;
    private int removedCount = 0;

    @FXML
    private Label levelLabel;

    @FXML
    private Label lifeLabel;

    @FXML
    private Label scoreLabel;

    @FXML
    private AnchorPane pane;

    private SentencesManager sentencesManager;
    private Map<Sentence, Node> labelMap;
    private Random random;
    private Double paneHeight;
    private Double paneWidth;

    private Disposable makeSentenceDisposable;

    private GameLevel currentGameLevel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        random = new Random();
        labelMap = new HashMap<>();

        pane.heightProperty().addListener((observable, oldValue, newValue) -> paneHeight = (Double) newValue);
        pane.widthProperty().addListener((observable, oldValue, newValue) -> paneWidth = (Double) newValue);
    }

    public void startRaining() {
        currentGameLevel = new GameLevel();
        scoreLabel.setText("0");
        lifeLabel.setText("5");
        this.startRaining(currentGameLevel);
    }

    public void startRaining(GameLevel level) {
        currentGameLevel = level;
        levelLabel.setText(String.valueOf(currentGameLevel.getLevel()));
        SentenceMaker sentenceMaker = new SentenceMaker(level.getMaxInterval(), level.getMinInterval(), level.getMaxCharLength(), level.getMinCharLength());
        sentencesManager = new SentencesManager(level.getMaxDropSpeed(), level.getMinDropSpeed());
        Disposable disposable = sentencesManager.getSentencesObservable()
                .buffer(100, TimeUnit.MICROSECONDS)
                .subscribe(this::updateView,
                        this::logging);

        makeSentenceDisposable = sentenceMaker.getSentences()
                .doOnNext(this::visibleSentence)
                .subscribe(sentencesManager::addSentences,
                        this::logging,
                        disposable::dispose);
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
                .forEach(this::updateSentence));
    }

    private void updateSentence(Sentence sentence) {
        if (sentence.getHeight() == SentencesManager.SENTENCE_DIVIDE_LENGTH) {
            removeSentence(sentence);
            loseLife();
        } else {
            labelMap.get(sentence).setLayoutY(paneHeight * 0.95 * sentence.getHeight() / SentencesManager.SENTENCE_DIVIDE_LENGTH);
        }
    }

    private void loseLife() {
        int life = Integer.parseInt(lifeLabel.getText());
        life--;
        if (life <= 0)
            gameOver();
        else
            lifeLabel.setText(String.valueOf(life));
    }

    private void gameOver() {
        makeSentenceDisposable.dispose();
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("게임오바아");
            alert.setHeaderText("게임 오버되었습니다. \n 처음부터 ?");
            sentencesManager.close();
            alert.showAndWait();
            restart();
        });
        System.out.println("gameOver");
    }

    private void logging(Throwable e) {
        e.printStackTrace();
    }

    boolean removeSentence(String sentence) {
        Sentence removed = sentencesManager.removeSentence(sentence);
        boolean removeSuccess = removed != null;
        if (removeSuccess) {
            removeSentence(removed);

            int score = Integer.parseInt(scoreLabel.getText());
            score += removed.getSentence().length();
            scoreLabel.setText(String.valueOf(score));
            removedCount++;
            if(removedCount>=LEVEL_UP_COUNT){
                moveToNextLevel();
                removedCount=0;
            }
        }
        return removeSuccess;
    }

    private void removeSentence(Sentence removed) {
        pane.getChildren().remove(labelMap.get(removed));
        labelMap.remove(removed);
    }

    private void restart() {
        labelMap.keySet()
                .stream()
                .map(Sentence::getSentence)
                .collect(Collectors.toList())
                .forEach(this::removeSentence);
        this.startRaining();
    }

    private void moveToNextLevel() {
        labelMap.keySet()
                .stream()
                .collect(Collectors.toList())
                .forEach(this::removeSentence);
        makeSentenceDisposable.dispose();
        sentencesManager.close();
        startRaining(currentGameLevel.levelUp());
    }
}
