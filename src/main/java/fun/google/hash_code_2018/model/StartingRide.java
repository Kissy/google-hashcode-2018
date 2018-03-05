package fun.google.hash_code_2018.model;

public class StartingRide implements Ride {
    @Override
    public String getRideId() {
        return null;
    }

    @Override
    public Point getStart() {
        return Point.ORIGIN;
    }

    @Override
    public Point getFinish() {
        return Point.ORIGIN;
    }

    @Override
    public int getEarliestStart() {
        return 0;
    }

    @Override
    public int getLatestStart() {
        return 0;
    }

    @Override
    public int getEarliestFinish() {
        return 0;
    }

    @Override
    public int getLatestFinish() {
        return 0;
    }

    @Override
    public int getDuration() {
        return 0;
    }

    @Override
    public int getScore() {
        return 0;
    }

    @Override
    public int getBonus() {
        return 0;
    }

    @Override
    public int getSize() {
        return 0;
    }

    @Override
    public int getTimeToClosestNextRide() {
        return 0;
    }

    @Override
    public void setTimeToClosestNextRide(int timeToClosestNextRide) {

    }
}
