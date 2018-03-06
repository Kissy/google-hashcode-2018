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
import io.jenetics.util.ISeq;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.jenetics.engine.EvolutionResult.toBestPhenotype;

class Pair {
    private final Ride startRide;
    private final Ride endRide;
    private final int score;

    public Pair(Ride startRide, Ride endRide, int score) {
        this.startRide = startRide;
        this.endRide = endRide;
        this.score = score;
    }

    public Ride getStartRide() {
        return startRide;
    }

    public Ride getEndRide() {
        return endRide;
    }

    public int getScore() {
        return score;
    }
}

public class Simulation {

    public Maps maps = null;

    public int simulate() {
        calculateStats();

//        maps.getRides().forEach(r1 -> {
//            int sumOfDistanceToNextRides = maps.getRides().parallelStream()
//                    .filter(r2 -> r2 != r1)
//                    .mapToInt(r2 -> {
//                        int distanceTo = r1.getFinish().distanceTo(r2.getStart());
//                        int earliestArrival = r1.getEarliestFinish() + distanceTo;
//                        if (earliestArrival <= r2.getLatestStart()) {
//                            return r2.getLatestStart() - earliestArrival;
//                        }
//                        return 0;
//                    })
//                    .sum();
//            if (sumOfDistanceToNextRides == 0) {
//                sumOfDistanceToNextRides = Integer.MAX_VALUE;
//            }
//            r1.setTimeToClosestNextRide(sumOfDistanceToNextRides);
//        });

//        maps.getRides().parallelStream().forEach(r1 -> {
//            maps.getRides().stream()
//                    .filter(r2 -> r2 != r1)
//                    .map(r2 -> (BookedRide) r2)
//                    .forEach(((BookedRide) r1)::calculatePossibleDistanceToRide);
//            ((BookedRide) r1).sortClosestRides();
//        });

//        maps.getRides().forEach(r1 -> {
//            List<BookedRide> orderRidesByBestDistance = maps.getRides().parallelStream()
//                    .filter(r2 -> r2 != r1)
//                    .sorted(Comparator.comparingInt(((BookedRide) r1)::possibleDistanceToRide))
//                    .filter(r2 -> ((BookedRide) r1).possibleDistanceToRide(r2) != Integer.MAX_VALUE)
//                    .map(r2 -> (BookedRide) r2)
//                    .collect(Collectors.toList());
//            ((BookedRide) r1).setClosestRides(orderRidesByBestDistance);
//        });

        //tryToFindRidePairs();

        maps.getVehicleRides().clear();
        for (int i = 0; i < maps.getVehicles(); i++) {
            VehicleRides vr = new VehicleRides(this);
//            if (i < maps.getVehicles() / 2) {
//                double ratio = i / maps.getVehicles();
//                vr.add(new EmptyRide(this, Point.ORIGIN, new Point((int) Math.round(maps.getRows() * ratio), (int) Math.round(maps.getColumns() * ratio))));
//            }
            maps.getVehicleRides().add(vr);
        }

        int remainingRides = 0;
        while (maps.getRides().size() != remainingRides) {
            remainingRides = maps.getRides().size();
            maps.getVehicleRides().forEach(vr -> {
                maps.getRides().stream()
                        .filter(vr::canRide)
                        .min(Comparator.comparingDouble(vr::wasteTimeTo))
                        .ifPresent(vr::add);
                maps.getRides().removeAll(vr.getRides());
            });
        }

//        geneticEngine();

        //tryToOptimize();

        return maps.getVehicleRides().stream().mapToInt(VehicleRides::getScore).sum();
    }

    public int resumeSimulate() {

        geneticEngine();

        //tryToOptimize();

        return maps.getVehicleRides().stream().mapToInt(VehicleRides::getScore).sum();
    }

    private void calculateStats() {
        int maximumScore = maps.getRides().stream().mapToInt(r -> {
            BookedRide bookedRide = (BookedRide) r;
            return bookedRide.getDuration() + (bookedRide.isCanReachBonus() ? bookedRide.getBonus() : 0);
        }).sum();
        System.out.println("Maximum score is " + maximumScore);
    }

    private void tryToOptimize() {
        maps.getRides().sort(Comparator.comparingInt(Ride::getTimeToClosestNextRide));
        maps.getVehicleRides().sort(Comparator.comparingInt(Ride::getScore));

        VehicleRides vr = maps.getVehicleRides().get(0);
        maps.getRides().addAll(vr.getRides());
        vr.getRides().clear();
        maps.getRides().parallelStream()
                .filter(vr::canRide)
                .min(Comparator.comparingDouble(vr::wasteTimeTo))
                .ifPresent(vr::add);
        maps.getRides().removeAll(vr.getRides());

        System.out.println(maps.getVehicleRides());
    }

    private void tryToFindRidePairs() {
        Map<Ride, Pair> pairsFound = new HashMap<>();

        maps.getRides().forEach(r1 -> {
            maps.getRides().parallelStream()
                    .filter(r2 -> r2 != r1)
                    .forEach(r2 -> {
                        int distanceTo = r1.getFinish().distanceTo(r2.getStart());
                        int earliestArrival = r1.getEarliestFinish() + distanceTo;
                        if (earliestArrival == r2.getLatestStart() && distanceTo < 1000) {
                            if (pairsFound.containsKey(r1)) {
                                if (pairsFound.get(r1).getScore() < distanceTo) {
                                    return;
                                }
                            }
                            pairsFound.put(r1, new Pair(r1, r2, distanceTo));
                        }
                    });
        });

        // Put optimized rides
        pairsFound.values().forEach(pair -> {
            VehicleRides optimizedRide = new VehicleRides(this, pair.getStartRide(), pair.getEndRide());
            maps.getRides().add(optimizedRide);
            maps.getRides().remove(pair.getStartRide());
            maps.getRides().remove(pair.getEndRide());
        });
    }

    private void geneticEngine() {
        int numberOfOptimizedRides = 1;
        while (numberOfOptimizedRides != 0) {
            numberOfOptimizedRides = 0;
            maps.getVehicleRides().sort(Comparator.comparingInt(Ride::getScore));
            for (int i = 0; i < maps.getVehicleRides().size(); i++) {
                List<Ride> foundOrder = new ArrayList<>(maps.getRides());
                VehicleRides vr = maps.getVehicleRides().get(i);
                foundOrder.add(new StartingRide());
                foundOrder.addAll(vr.getRides());

                System.out.println("Optimizing ride " + vr.toString());

                int startingScore = vr.getScore();
                final TravelingSalesman tsm =
                        new TravelingSalesman(maps, ISeq.of(foundOrder));

                final Engine<EnumGene<Ride>, Double> engine = Engine.builder(tsm)
                        .optimize(Optimize.MAXIMUM)
                        .alterers(
                                new SwapMutator<>(0.15),
                                new PartiallyMatchedCrossover<>(0.15)
                        )
                        .build();

                final Phenotype<EnumGene<Ride>, Double> best = engine.stream()
//                        .limit(bySteadyFitness(1000))
                        .limit(10000)
//                        .peek(r -> System.out.println("Best fitness " + r.getBestFitness() + " at " + r.getGeneration()))
                        .collect(toBestPhenotype());

                final ISeq<Ride> path = best.getGenotype()
                        .getChromosome().toSeq()
                        .map(Gene::getAllele);

                double bestSum = tsm.fitness().apply(path);
                if (bestSum > startingScore) {
                    System.out.println("Found best score " + bestSum);
                    numberOfOptimizedRides++;
                    maps.getRides().clear();
                    vr.getRides().clear();
                    boolean foundStartingRide = false;
                    for (Ride ride : path) {
                        if (ride instanceof StartingRide) {
                            foundStartingRide = true;
                            continue;
                        }
                        if (!foundStartingRide) {
                            maps.getRides().add(ride);
                        } else {
                            vr.getRides().add(ride);
                        }
                    }
                }
            }
            System.out.println("optimized " + numberOfOptimizedRides);
        }
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
