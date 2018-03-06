package fun.google.hash_code_2018.model;

import fun.google.hash_code_2018.Simulation;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class VehicleRides implements Ride, Comparable<VehicleRides> {
    private final Simulation simulation;
    private List<Ride> rides = new ArrayList<>();
    private int duration = 0;
    private int score = 0;
    private int bonus = 0;
    private double ratio = 0;

    public VehicleRides(Simulation simulation, Ride ride) {
        this.simulation = simulation;
        add(ride);
    }

    public VehicleRides(Simulation simulation, Ride ride1, Ride ride2) {
        this.simulation = simulation;
        this.rides.add(ride1);
        add(ride2);
    }

    public VehicleRides(Simulation simulation) {
        this.simulation = simulation;
    }

    public void add(Ride ride) {
        this.rides.add(ride);
        calculateDurationAndScore();
        calculateRatio();
    }

    public Ride remove(int index) {
        Ride removedRide = this.rides.remove(index);
        calculateDurationAndScore();
        calculateRatio();
        return removedRide;
    }

    @Override
    public String getRideId() {
        return this.rides.stream().map(Ride::getRideId).filter(Objects::nonNull).collect(Collectors.joining(" "));
    }

    @Override
    public Point getStart() {
        return Point.ORIGIN;
    }

    @Override
    public Point getFinish() {
        if (this.rides.isEmpty()) {
            return Point.ORIGIN;
        }
        return this.rides.get(this.rides.size() - 1).getFinish();
    }

    @Override
    public int getEarliestStart() {
        return 0;
    }

    @Override
    public int getLatestStart() {
        return getLatestFinish() - this.duration;
    }

    @Override
    public int getEarliestFinish() {
        return getEarliestStart() + this.duration;
    }

    @Override
    public int getLatestFinish() {
        return simulation.maps.getSteps();
    }

    @Override
    public int getDuration() {
        return duration;
    }

    @Override
    public int getScore() {
        return score + bonus;
    }

    @Override
    public int getBonus() {
        return this.bonus;
    }

    @Override
    public int getSize() {
        return this.rides.stream().mapToInt(Ride::getSize).sum();
    }

    @Override
    public int getTimeToClosestNextRide() {
        if (this.rides.isEmpty()) {
            return simulation.maps.getSteps();
        }
        return this.rides.get(this.rides.size() - 1).getTimeToClosestNextRide();
    }

    @Override
    public void setTimeToClosestNextRide(int timeToClosestNextRide) {
        throw new IllegalStateException("cannot set timeToClosestNextRide in this class");
    }

    public double getRatio() {
        return ratio;
    }

    public double getInvertRatio() {
        return 1 / getRatio();
    }

    public List<Ride> getRides() {
        return rides;
    }

    public Stream<Ride> getRidesStream() {
        return rides.stream();
    }

    // Working method that calculate the score
    private void calculateDurationAndScore() {
        this.score = 0;
        this.bonus = 0;
        this.duration = 0;
        Point previousPoint = Point.ORIGIN;
        for (Ride currentRide : this.rides) {
            // Go to ride starting point
            duration += previousPoint.distanceTo(currentRide.getStart());
            // wait for start of ride
            duration = Math.max(duration, currentRide.getEarliestStart());
            // Check for bonus
            if (duration == currentRide.getEarliestStart()) {
                bonus += currentRide.getBonus();
            }
            duration += currentRide.getDuration();
            if (duration <= currentRide.getLatestFinish() && duration < simulation.maps.getSteps()) {
                score += currentRide.getScore();
            }
            previousPoint = currentRide.getFinish();
        }
    }

    private void calculateRatio() {
//        double waitRatio = this.duration / (double) (getLastRide().getEarliestStart() - getFirstRide().getDuration());
//        double waitRatio = 0;
//        if (simulation.maps.getBonus() > 500) {
//            double scoreRatio = (1 / (double) this.getDuration()) * this.bonus / 1000;
//            this.ratio = waitRatio > 0 ? scoreRatio * waitRatio : scoreRatio;
//        } else {
//            double scoreRatio = getScore() / (double) this.getDuration();
//            this.ratio = waitRatio > 0 ? scoreRatio * waitRatio : scoreRatio;
//        }
        // wasted time
        this.ratio = this.duration - this.score;
    }

    public boolean canRide(Ride ride) {
        int arrivalTime = this.duration + getFinish().distanceTo(ride.getStart());
        return arrivalTime + ride.getDuration() <= ride.getLatestFinish();
    }

    public double wasteTimeTo(Ride ride) {
        int durationToRide = this.getFinish().distanceTo(ride.getStart());
        int earliestArrival = Math.max(this.duration + durationToRide, ride.getEarliestStart());
        int finishFarMalus = (earliestArrival + ride.getDuration() <= simulation.maps.getLongRideMinStep()) ? (int) (ride.getTimeToClosestNextRide() / simulation.maps.getLongRideRatio()) : 0;
        return earliestArrival - this.duration + finishFarMalus;
    }

    public String getStringToFile() {
        return getSize() + " " + getRideId();
    }

    @Override
    public String toString() {
        return "VehicleRides{rides=" + rides.size() + ", duration=" + getDuration() + ", score=" + score + ", bonus=" + bonus + '}';
    }

    @Override
    public int compareTo(VehicleRides vehicleRides) {
        return Integer.compare(this.duration, vehicleRides.duration);
    }
}
