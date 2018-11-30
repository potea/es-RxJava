package model;

import io.reactivex.Observable;
import model.vo.Sentence;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class SentenceMaker {

    private List<String> sentences;
    private Random random;

    private int maxMakeInterval;
    private int minMakeInterval;
    private int maxLength;
    private int minLength;

    public SentenceMaker(int maxMakeInterval, int minMakeInterval, int maxLength, int minLength) {
        try (BufferedReader br = Files.newBufferedReader(Paths.get("C:/Users/user/IdeaProjects/es-RxJava/teahyuk/Ssansungvy/target/classes/texts.csv"))){
            sentences = Arrays.asList(br.readLine().split(","));
            br.close();
            random = new Random();
            this.maxMakeInterval = maxMakeInterval;
            this.minMakeInterval = minMakeInterval;
            this.maxLength = maxLength;
            this.minLength = minLength;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //timeInterval ms
    public Observable<Sentence> getSentences() {
        if(sentences.size() == 0)
            return Observable.error(new Exception("NoTextsException"));
        AtomicInteger interval = new AtomicInteger(maxMakeInterval);
        return Observable.defer(() -> Observable.just(new Sentence(sentences.get(random.nextInt(sentences.size())))))
                .repeat()
                .filter(this::isSentenceInLength)
                .take(1)
                .delay(minMakeInterval < interval.get() ? interval.getAndDecrement() : minMakeInterval,
                                TimeUnit.MILLISECONDS)
                .repeat()
                .doOnDispose(()->System.out.println("dispose"));
    }

    private boolean isSentenceInLength(Sentence s){
        return minLength <= s.getSentence().length() && s.getSentence().length() <= maxLength;
    }

}
