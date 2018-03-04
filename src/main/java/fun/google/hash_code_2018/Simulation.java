package fun.google.hash_code_2018;

import fun.google.hash_code_2018.model.BookedRide;
import fun.google.hash_code_2018.model.Maps;
import fun.google.hash_code_2018.model.Ride;
import fun.google.hash_code_2018.model.StartingRide;
import fun.google.hash_code_2018.model.VehicleRides;
import io.jenetics.EnumGene;
import io.jenetics.Gene;
import io.jenetics.Optimize;
import io.jenetics.PartiallyMatchedCrossover;
import io.jenetics.Phenotype;
import io.jenetics.SwapMutator;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionStatistics;
import io.jenetics.util.ISeq;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static io.jenetics.engine.EvolutionResult.toBestPhenotype;
import static io.jenetics.engine.Limits.bySteadyFitness;

public class Simulation {

    public Maps maps = null;

    public int simulate() {
        maps.getRides().forEach(r1 -> {
            maps.getRides().parallelStream()
                    .filter(r2 -> r2 != r1)
                    .mapToInt(r2 -> {
                        int distanceTo = r1.getFinish().distanceTo(r2.getStart());
                        int earliestArrival = r1.getEarliestFinish() + distanceTo;
                        if (earliestArrival <= r2.getLatestStart()) {
                            return distanceTo;
                        }
                        return Integer.MAX_VALUE;
                    })
                    .min()
                    .ifPresent(((BookedRide) r1)::setTimeToClosestNextRide);
        });

        for (int i = 0; i < maps.getVehicles(); i++) {
            maps.getVehicleRides().add(new VehicleRides(this));
        }

        int remainingRides = 0;
        while (maps.getRides().size() != remainingRides) {
            remainingRides = maps.getRides().size();
            maps.getVehicleRides().forEach(vr -> {
                maps.getRides().parallelStream()
                        .filter(vr::canRide)
                        .min(Comparator.comparingInt(vr::wasteTimeTo))
                        .ifPresent(vr::add);
                maps.getRides().removeAll(vr.getRides());
            });
        }

        System.out.println("Remaining rides " + remainingRides);

        List<Ride> foundOrder = new ArrayList<>(maps.getRides());
        maps.getVehicleRides().forEach(vr -> {
            foundOrder.add(new StartingRide());
            foundOrder.addAll(vr.getRides());
        });

        final TravelingSalesman tsm =
                new TravelingSalesman(maps, ISeq.of(foundOrder));

        final Engine<EnumGene<Ride>, Double> engine = Engine.builder(tsm)
                .optimize(Optimize.MAXIMUM)
                .alterers(
                        new SwapMutator<>(0.15),
                        new PartiallyMatchedCrossover<>(0.15)
                )
                .build();

        // Create evolution statistics consumer.
        final EvolutionStatistics<Double, ?>
                statistics = EvolutionStatistics.ofNumber();

        final Phenotype<EnumGene<Ride>, Double> best = engine.stream()
                .limit(bySteadyFitness(10))
                .limit(10)
                .peek(statistics)
                .peek(r -> System.out.println("Best fitness " + r.getBestFitness() + " at " + r.getGeneration()))
                .collect(toBestPhenotype());

        final ISeq<Ride> path = best.getGenotype()
                .getChromosome().toSeq()
                .map(Gene::getAllele);

        double bestSum = tsm.fitness().apply(path);

        System.out.println(statistics);
        System.out.println("Best score: " + bestSum);

        maps.getVehicleRides().clear();
        VehicleRides nextRide = null;
        for (Ride ride : path) {
            if (ride instanceof StartingRide) {
                if (nextRide != null) {
                    maps.getVehicleRides().add(nextRide);
                }
                nextRide = new VehicleRides(this);
                continue;
            }
            if (nextRide == null) {
                continue;
            }

            nextRide.add(ride);
        }
        maps.getVehicleRides().add(nextRide);

        return maps.getVehicleRides().stream().mapToInt(VehicleRides::getScore).sum();
    }

    public boolean isInitialized() {
        return maps != null;
    }

    public void setMaps(Maps maps) {
        this.maps = maps;
    }

    public void addRide(BookedRide ride) {
        maps.addRide(ride);
    }
}
