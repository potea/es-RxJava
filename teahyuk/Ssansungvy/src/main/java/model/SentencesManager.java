package model;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;
import model.vo.Sentence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 *
 */
public class SentencesManager {
    public static final int SENTENCE_DIVIDE_LENGTH = 1000;

    private int maxSpeed;
    private int minSpeed;

    private ArrayList<Sentence> sentences;

    private PublishSubject<Sentence> subject;
    private Observable<Sentence> observable;
    private Map<Sentence, Disposable> subscriptionMap;

    private Random random;

    public SentencesManager(int maxSpeed, int minSpeed) {
        this.maxSpeed = maxSpeed;
        this.minSpeed = minSpeed;

        sentences = new ArrayList<>();

        subject = PublishSubject.create();
        observable = subject.doOnNext(this::replaceSentenceIdx);
        subscriptionMap = new HashMap<>();
        random = new Random();
    }

    public void addSentences(Sentence sentence) {
        if (!sentences.contains(sentence)) {
            sentences.add(sentence);

            int interval = random.nextInt(SENTENCE_DIVIDE_LENGTH / minSpeed - SENTENCE_DIVIDE_LENGTH / maxSpeed) + SENTENCE_DIVIDE_LENGTH / maxSpeed;
            subscriptionMap.put(sentence,
                    Observable.interval(interval, TimeUnit.MILLISECONDS)
                            .take(SENTENCE_DIVIDE_LENGTH)
                            .map(i -> i + 1)
                            .doOnNext(sentence::setHeight)
                            .map(x -> sentence)
                            .subscribe(subject::onNext,
                                    this::logging,
                                    () -> removeSentence(sentence)));
        }
    }

    public Sentence removeSentence(String text) {
        Sentence removedSentence = sentences.stream()
                .filter(x -> x.getSentence().equals(text))
                .findFirst()
                .orElse(null);
        if (removedSentence != null) {
            removeSentence(removedSentence);
        }
        return removedSentence;
    }

    public Observable<Sentence> getSentencesObservable() {
        return observable;
    }

    public void close(){
        subject.onComplete();
        subscriptionMap.values().forEach(Disposable::dispose);
        subscriptionMap.clear();
        sentences.clear();
    }

    private void replaceSentenceIdx(Sentence sentence) {
        int idx = sentences.indexOf(sentence);
        if (idx > 0) {
            Sentence front = sentences.get(idx - 1);
            if (sentence.getHeight() > front.getHeight()) { //순서뒤집기
                sentences.set(idx - 1, sentence);

                sentences.set(idx, front);
            }
        }
    }

    private void removeSentence(Sentence removedSentence) {
        sentences.remove(removedSentence);

        subscriptionMap.get(removedSentence).dispose();
        subscriptionMap.remove(removedSentence);
    }

    private void logging(Throwable e) {
        e.printStackTrace();
    }
}
