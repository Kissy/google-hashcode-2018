package fun.google.hash_code_2018;

import fun.google.hash_code_2018.model.Maps;
import fun.google.hash_code_2018.model.Point;
import fun.google.hash_code_2018.model.Ride;
import fun.google.hash_code_2018.model.StartingRide;
import io.jenetics.EnumGene;
import io.jenetics.engine.Codec;
import io.jenetics.engine.Codecs;
import io.jenetics.engine.Problem;
import io.jenetics.util.ISeq;

import java.util.Objects;
import java.util.function.Function;

public final class TravelingSalesman implements Problem<ISeq<Ride>, EnumGene<Ride>, Double> {

    private final ISeq<Ride> _points;
    private Maps currentMap;

    public TravelingSalesman(Maps currentMap, final ISeq<Ride> points) {
        this.currentMap = currentMap;
        _points = Objects.requireNonNull(points);
    }

    @Override
    public Codec<ISeq<Ride>, EnumGene<Ride>> codec() {
        return Codecs.ofPermutation(_points);
    }

    @Override
    public Function<ISeq<Ride>, Double> fitness() {
        return rides -> {
            double totalScore = 0;

            Point previousPoint = null;
            int duration = 0;
            for (Ride currentRide : rides) {
                if (currentRide instanceof StartingRide) {
                    previousPoint = Point.ORIGIN;
                    duration = 0;
                    continue;
                }
                if (previousPoint == null) {
                    continue;
                }

                // Go to ride starting point
                duration += previousPoint.distanceTo(currentRide.getStart());
                // wait for start of ride
                duration = Math.max(duration, currentRide.getEarliestStart());
                // Check for bonus
                if (duration == currentRide.getEarliestStart()) {
                    totalScore += currentRide.getBonus();
                }
                duration += currentRide.getDuration();
                if (duration <= currentRide.getLatestFinish()) {
                    totalScore += currentRide.getScore();
                }
                previousPoint = currentRide.getFinish();
            }

            return totalScore;
        };
    }

}