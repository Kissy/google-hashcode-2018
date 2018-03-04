package fun.google.hash_code_2018.model;

import fun.google.hash_code_2018.Simulation;

public class BookedRide implements Ride {
    private Simulation simulation;
    private final String rideId;
    private final Point start;
    private final Point finish;
    private final int earliestStart;
    private final int latestStart;
    private final int earliestFinish;
    private final int latestFinish;
    private final int duration;
    private int timeToClosestNextRide;

    public BookedRide(Simulation simulation, String rideId, Point start, Point finish, int earliestStart, int latestFinish) {
        this.simulation = simulation;
        this.rideId = rideId;
        this.start = start;
        this.finish = finish;
        this.earliestStart = earliestStart;
        this.latestFinish = latestFinish;
        this.duration = this.start.distanceTo(this.finish);
        this.latestStart = latestFinish - this.duration;
        this.earliestFinish = earliestStart + this.duration;
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
        return earliestStart;
    }

    @Override
    public int getLatestStart() {
        return latestStart;
    }

    @Override
    public int getEarliestFinish() {
        return earliestFinish;
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
        return 1;
    }

    @Override
    public int getScore() {
        return duration;
    }

    @Override
    public int getBonus() {
        return simulation.maps.getBonus();
    }

    public void setTimeToClosestNextRide(int timeToClosestNextRide) {
        this.timeToClosestNextRide = timeToClosestNextRide;
    }

    public int getTimeToClosestNextRide() {
        return timeToClosestNextRide;
    }

    @Override
    public String toString() {
        return "Ride # " + rideId + " {" +
                "from " + start +
                " to " + finish +
                " (" + duration + ")" +
                ", earliestStart=" + earliestStart +
                ", latestFinish=" + latestFinish +
                ", timeToClosestNextRide=" + timeToClosestNextRide +
                '}';
    }
}
