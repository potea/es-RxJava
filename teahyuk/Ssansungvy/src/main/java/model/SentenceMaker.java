package model;

import com.github.javafaker.Faker;
import model.vo.Sentence;
import rx.Observable;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class SentenceMaker {

    public SentenceMaker() {

    }

    //timeInterval ms
    public Observable<Sentence> getSentences(int maxInterval, int minInterval) {
        AtomicInteger interval = new AtomicInteger(maxInterval);
        return Observable.defer(() ->
                Observable.just(new Sentence(Faker.instance().dragonBall().character()))
                        .delay(minInterval < interval.get() ? interval.getAndDecrement() : minInterval,
                                TimeUnit.MILLISECONDS))
                .repeat();
    }

}
