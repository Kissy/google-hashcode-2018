package fun.google.hash_code_2018.model;

public interface Ride {

    String getRideId();

    Point getStart();

    Point getFinish();

    int getEarliestStart();

    int getLatestStart();

    int getEarliestFinish();

    int getLatestFinish();

    int getDuration();

    int getScore();

    int getBonus();

    int getSize();

}
