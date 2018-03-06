package fun.google.hash_code_2018;

import fun.google.hash_code_2018.file_parser.WriteFile;
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

import java.io.IOException;
import java.util.*;

import static io.jenetics.engine.EvolutionResult.toBestPhenotype;
import static io.jenetics.engine.Limits.bySteadyFitness;

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
//        calculateStats();

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

        //tryToFindRidePairs();

        maps.getVehicleRides().clear();
        for (int i = 0; i < maps.getVehicles(); i++) {
            maps.getVehicleRides().add(new VehicleRides(this));
        }

        Map<Ride, List<BookedRide>> closestRide = new HashMap<>();
        maps.getRides().forEach(r1 -> {
            maps.getRides().parallelStream()
                    .filter(r2 -> r2 != r1)
                    .filter(r2 -> !((BookedRide) r2).isTaken())
                    .min(Comparator.comparingInt(r2 -> r1.getFinish().distanceTo(r2.getStart())))
                    .ifPresent(r2 -> {
                        r1.setTimeToClosestNextRide(r1.getFinish().distanceTo(r2.getStart()));
                        closestRide.putIfAbsent(r2, new ArrayList<>());
                        closestRide.get(r2).add((BookedRide) r1);
                    });
        });

        int remainingRides = 0;
        while (maps.getRides().size() != remainingRides) {
            remainingRides = maps.getRides().size();
            maps.getVehicleRides().forEach(vr -> {
                maps.getRides().stream()
                        .filter(vr::canRide)
                        .min(Comparator.comparingDouble(vr::wasteTimeTo))
                        .ifPresent(r -> {
                            vr.add(r);
                            ((BookedRide) r).setTaken(true);
//
//                            closestRide.getOrDefault(r, Collections.emptyList()).forEach(r1 -> {
//                                maps.getRides().parallelStream()
//                                        .filter(r2 -> r2 != r1)
//                                        .filter(r2 -> !((BookedRide) r2).isTaken())
//                                        .min(Comparator.comparingInt(r2 -> r1.getFinish().distanceTo(r2.getStart())))
//                                        .ifPresent(r2 -> {
//                                            r1.setTimeToClosestNextRide(r1.getFinish().distanceTo(r2.getStart()));
//                                            closestRide.putIfAbsent(r2, new ArrayList<>());
//                                            closestRide.get(r2).add(r1);
//                                        });
//                            });
                        });
                maps.getRides().removeAll(vr.getRides());
            });
        }

        //geneticEngine();

        //tryToOptimize();

        return maps.getVehicleRides().stream().mapToInt(VehicleRides::getScore).sum();
    }

    public int resumeSimulate() {

        //geneticEngine();

        //tryToOptimize();

        maps.getRides().forEach(r -> {
            maps.getVehicleRides()
                    .stream()
                    .filter(vr -> vr.canRide(r))
                    .forEach(vr -> {
                        System.out.println("ok");
            });
        });

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
        int previousTotalScore = 0;
        while (numberOfOptimizedRides != 0) {
            numberOfOptimizedRides =  maps.getVehicleRides().stream().mapToInt(VehicleRides::getScore).sum();
            maps.getVehicleRides().sort(Comparator.comparingInt(Ride::getScore));
            for (int i = 0; i < maps.getVehicleRides().size(); i++) {
                maps.getRides().sort(Comparator.comparingInt(Ride::getScore));
                maps.getRides().

                List<Ride> foundOrder = new ArrayList<>(maps.getRides());
                VehicleRides vr = maps.getVehicleRides().get(i);
                foundOrder.add(new StartingRide());
                foundOrder.addAll(vr.getRides());

                System.out.println("Optimizing " + maps.getRides().size() + " rides with " + vr.toString());

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
                        .limit(bySteadyFitness(1000))
                        .limit(10000)
                //.peek(r -> System.out.println("Best fitness " + r.getBestFitness() + " at " + r.getGeneration()))
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
                        if (!foundStartingRide || !vr.canRide(ride)) {
                            maps.getRides().add(ride);
                        } else {
                            vr.getRides().add(ride);
                        }
                    }
                }
            }
            System.out.println("optimized " + numberOfOptimizedRides);
            int newTotalScore = maps.getVehicleRides().stream().mapToInt(VehicleRides::getScore).sum();
            if (newTotalScore > previousTotalScore) {
                System.out.println("NEW TOTAL SCORE " + newTotalScore);
                try {
                    WriteFile.writeFileToPath(Collections.singletonMap("d_metropolis.in", maps));
                    previousTotalScore = newTotalScore;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
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
