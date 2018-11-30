package model.vo;

public class GameLevel {
    private int level = 1;

    private int maxInterval = 2000;
    private int minInterval = 500;
    private int maxDropSpeed = 50;
    private int minDropSpeed = 10;
    private int maxCharLength = 5;
    private int minCharLength = 3;


    public int getLevel() {
        return level;
    }

    public int getMaxInterval() {
        return maxInterval;
    }

    public int getMinInterval() {
        return minInterval;
    }

    public int getMaxDropSpeed() {
        return maxDropSpeed;
    }

    public int getMinDropSpeed() {
        return minDropSpeed;
    }

    public int getMaxCharLength() {
        return maxCharLength;
    }

    public int getMinCharLength() {
        return minCharLength;
    }

    public void setMaxInterval(int maxInterval) {
        this.maxInterval = maxInterval;
    }

    public void setMinInterval(int minInterval) {
        this.minInterval = minInterval;
    }

    public void setMaxDropSpeed(int maxDropSpeed) {
        this.maxDropSpeed = maxDropSpeed;
    }

    public void setMinDropSpeed(int minDropSpeed) {
        this.minDropSpeed = minDropSpeed;
    }

    public void setMaxCharLength(int maxCharLength) {
        this.maxCharLength = maxCharLength;
    }

    public void setMinCharLength(int minCharLength) {
        this.minCharLength = minCharLength;
    }

    public GameLevel levelUp(){
        maxInterval = Math.max(500,maxInterval-100);
        minInterval = Math.max(200,minInterval-50);
        maxDropSpeed = Math.min(100,maxDropSpeed+10);
        minDropSpeed = Math.min(50,minDropSpeed+5);
        maxCharLength += 2;
        minCharLength = Math.min(7,minCharLength+1);

        level++;

        return this;
    }
}
