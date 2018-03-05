package fun.google.hash_code_2018.model;

import fun.google.hash_code_2018.Simulation;

public class EmptyRide implements Ride {
    private final String rideId;
    protected final Point start;
    protected final Point finish;
    private final int duration;
    private final int latestFinish;

    public EmptyRide(Simulation simulation, Point start, Point finish) {
        this.rideId = null;
        this.start = start;
        this.finish = finish;
        this.duration = this.start.distanceTo(this.finish);
        this.latestFinish = simulation.maps.getSteps();
    }

    @Override
    public String getRideId() {
        return rideId;
    }

    @Override
    public Point getStart() {
        return start;
    }

    @Override
    public Point getFinish() {
        return finish;
    }

    @Override
    public int getEarliestStart() {
        return -1;
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
        return latestFinish;
    }

    @Override
    public int getDuration() {
        return duration;
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

    @Override
    public int getScore() {
        return 0;
    }

    @Override
    public int getBonus() {
        return 0;
    }
}
