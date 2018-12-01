package app.acidrain;

import com.github.javafaker.Faker;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import javafx.scene.control.Label;

import java.util.LinkedHashMap;
import java.util.Map;

public class GameData {
    private PublishSubject<Integer> lifeChangeObservable = PublishSubject.create();
    private PublishSubject<Integer> scoreChangeObservable = PublishSubject.create();
    private PublishSubject<Label> wordRemoveObservable = PublishSubject.create();

    private Map<String, Label> currentWords = new LinkedHashMap<>();

    private int life = 5;
    private int score = 0;

    public Observable<Label> wordRemoveObservable() {
        return wordRemoveObservable;
    }

    public Observable<Integer> lifeChangeObservable() {
        return lifeChangeObservable;
    }

    public Observable<Integer> scoreChangeObservable() {
        return scoreChangeObservable;
    }

    public Label createWord() {
        String word = new Faker().dragonBall().character();
        Label ret = new Label(word);
        currentWords.put(word, ret);
        return ret;
    }

    public void removeWord(String word) {
        if (currentWords.containsKey(word)) {
            setScore(score + word.length());
            wordRemoveObservable.onNext(currentWords.get(word));

            currentWords.remove(word);
        }
    }

    public void increaseScore(int score) {
        setScore(this.score + score);
    }

    public boolean isDied() {
        return life <= 0;
    }

    public void reduceLife() {
        setLife(life - 1);
    }

    private void setLife(int life) {
        this.life = life;
        lifeChangeObservable.onNext(life);
    }

    private void setScore(int score) {
        this.score = score;
        scoreChangeObservable.onNext(score);
    }

    public boolean alreadyCleared(Label label) {
        return !currentWords.containsValue(label);
    }
}
