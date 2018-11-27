package model;

import model.vo.Sentence;
import rx.Observable;
import rx.Subscription;
import rx.subjects.PublishSubject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class SentencesManager {
    private int maxSpeed;
    private int minSpeed;

    private ArrayList<Sentence> sentences;

    private PublishSubject<Sentence> subject;
    private Observable<Sentence> observable;
    private Map<Sentence, Subscription> subscriptionMap;

    private Random random;

    public SentencesManager(int maxSpeed, int minSpeed) {
        this.maxSpeed = maxSpeed;
        this.minSpeed = minSpeed;

        sentences = new ArrayList<>();

        subject = PublishSubject.create();
        observable = subject.asObservable().doOnNext(this::replaceSentenceIdx);
        subscriptionMap = new HashMap<>();
        random = new Random();
    }

    public void addSentences(Sentence sentence) {
        if (!sentences.contains(sentence)) {
            sentences.add(sentence);

            int interval = random.nextInt(1000/minSpeed-1000/maxSpeed) + 1000/maxSpeed;
            subscriptionMap.put(sentence,
                    Observable.interval(interval, TimeUnit.MILLISECONDS)
                            .take(1000)
                            .map(i -> i + 1)
                            .doOnNext(sentence::setHeight)
                            .map(x -> sentence)
                            .subscribe(subject::onNext,
                                    this::logging,
                                    subject::onCompleted));
        }
    }

    public Sentence popSentence(String text) {
        Sentence removedSentence = sentences.stream()
                .filter(x -> x.getSentence().equals(text))
                .findFirst()
                .orElse(null);
        if (removedSentence != null) {
            sentences.remove(removedSentence);

            subscriptionMap.get(removedSentence).unsubscribe();
            subscriptionMap.remove(removedSentence);
        }
        return removedSentence;
    }

    public Observable<Sentence> getSentencesHotObservable() {
        return observable;
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
        System.out.println(sentence.getSentence() + ":" + sentence.getHeight());
    }

    private void logging(Throwable e) {
        e.printStackTrace();
    }
}
