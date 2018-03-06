package fun.google.hash_code_2018.file_parser;

import fun.google.hash_code_2018.Simulation;
import fun.google.hash_code_2018.model.Maps;
import fun.google.hash_code_2018.model.Point;
import fun.google.hash_code_2018.model.BookedRide;
import fun.google.hash_code_2018.model.Ride;
import fun.google.hash_code_2018.model.VehicleRides;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ReadFile {

    public static final Map<String, Integer> bestScore = new HashMap<>();
    static {
        bestScore.put("a_example.in", 10);
        bestScore.put("b_should_be_easy.in", 176877); // MAX 180498 = 3621
        bestScore.put("c_no_hurry.in", 15810342); // MAX 16740973 = 930631
        bestScore.put("d_metropolis.in", 12013853); // MAX 14272358 = 2258505
        bestScore.put("e_high_bonus.in", 21465945); // MAX 21478343 = 12398
    }

    public static final Map<String, Double> longRideRatio = new HashMap<>();
    static {
        longRideRatio.put("a_example.in", 1d);
        longRideRatio.put("b_should_be_easy.in", 1d);
        longRideRatio.put("c_no_hurry.in", 108d);
        longRideRatio.put("d_metropolis.in", 0.44);
//        longRideRatio.put("d_metropolis.in", 15d);
        longRideRatio.put("e_high_bonus.in", 2d);
    }

    public static final Map<String, Double> longRideMinStep = new HashMap<>();
    static {
        longRideMinStep.put("a_example.in", 0.98);
        longRideMinStep.put("b_should_be_easy.in", 0.98);
        longRideMinStep.put("c_no_hurry.in", 1d);
        longRideMinStep.put("d_metropolis.in", 0.802);
//        longRideMinStep.put("d_metropolis.in", 0.98);
        longRideMinStep.put("e_high_bonus.in", 0.98);
    }

    public static Map<String, Maps> getFileFromPath() throws IOException, URISyntaxException {
        Map<String, Maps> holder = new HashMap<>();

        List<Path> paths = Files.find(Paths.get(ClassLoader.getSystemResource("inputs").toURI()), 5, (path, attr) -> path.toString().toLowerCase().endsWith(".in"))
                .sorted((path2, path1) -> path1.getFileName().toString().compareTo(path2.getFileName().toString()))
                .collect(Collectors.toList());
        int total = 0;
        for (Path file : paths) {
            String filename = file.getFileName().toString();
            final Simulation simulation = new Simulation();

            readInputFileToSimulation(holder, file, filename, simulation);

            int simulateTotal = 0;
//            for (int i = 1; i < 5000; i++) {
                List<Ride> savesRides = new ArrayList<>(simulation.maps.getRides());
                simulation.maps.setLongRideRatio(longRideRatio.get(filename));
                simulation.maps.setLongRideMinStep(longRideMinStep.get(filename));
                int currentSimulationTotal = simulation.simulate();
                if (currentSimulationTotal > simulateTotal) {
                    simulateTotal = currentSimulationTotal;
//                    System.out.println("New best " + filename + " = " + simulateTotal + " (" + NumberFormat.getInstance().format(simulateTotal) + ") for " + i);
                    System.out.println("Remaining rides " + simulation.maps.getRides().size());
                } else {
//                    System.out.println("Not best " + filename + " = " + simulateTotal + " (" + NumberFormat.getInstance().format(simulateTotal) + ") for " + i);
                }
                simulation.maps.getRides().clear();
                simulation.maps.getRides().addAll(savesRides);
//            }

            if (simulateTotal > bestScore.get(filename)) {
                System.out.println("[BEST] Total for " + filename + " = " + simulateTotal + " (" + NumberFormat.getInstance().format(simulateTotal) + ")");
            } else {
                System.out.println("Total for " + filename + " = " + simulateTotal + " (" + NumberFormat.getInstance().format(simulateTotal) + ")");
            }

            total += simulateTotal;
        }
        if (total > bestScore.values().stream().mapToInt(i -> i).sum()) {
            System.out.println("[BEST] Total = " + NumberFormat.getInstance().format(total));
        } else {
            System.out.println("Total = " + NumberFormat.getInstance().format(total));
        }
        return holder;
    }

    public static Map<String, Maps> resumeFileFromPath() throws IOException, URISyntaxException {
        Map<String, Maps> holder = new HashMap<>();

        List<Path> paths = Files.find(Paths.get(ClassLoader.getSystemResource("previous_output").toURI()), 5, (path, attr) -> path.toString().toLowerCase().endsWith(".out"))
                .sorted((path2, path1) -> path1.getFileName().toString().compareTo(path2.getFileName().toString()))
                .collect(Collectors.toList());
        int total = 0;
        for (Path file : paths) {
            String filename = file.getFileName().toString().replace(".out", ".in");
            Path inputFile = Paths.get(ClassLoader.getSystemResource("all_inputs").toURI()).resolve(filename);

            final Simulation simulation = new Simulation();

            readInputFileToSimulation(holder, inputFile, filename, simulation);

            try (Stream<String> stream = Files.lines(Paths.get(file.toString()))) {
                stream.forEach((String line) -> {
                    String[] lineParsed = line.split(" ");
                    VehicleRides vehicleRides = new VehicleRides(simulation);
                    for (int i = 1; i < lineParsed.length; i++) {
                        String rideId = lineParsed[i];
                        simulation.maps.getRides().stream().filter(r -> r.getRideId().equals(rideId)).findFirst().ifPresent(vehicleRides::add);
                    }
                    simulation.maps.getVehicleRides().add(vehicleRides);
                });
                simulation.maps.getVehicleRides().stream().flatMap(VehicleRides::getRidesStream).forEach(simulation.maps.getRides()::remove);

                holder.put(filename, simulation.maps);
            } catch (IOException e) {
                e.printStackTrace();
            }

            int simulateTotal = 0;
            simulation.maps.setLongRideRatio(longRideRatio.get(filename));
            simulation.maps.setLongRideMinStep(longRideMinStep.get(filename));
            int currentSimulationTotal = simulation.resumeSimulate();
            if (currentSimulationTotal > simulateTotal) {
                simulateTotal = currentSimulationTotal;
                System.out.println("Remaining rides " + simulation.maps.getRides().size());
            }

            if (simulateTotal > bestScore.get(filename)) {
                System.out.println("[BEST] Total for " + filename + " = " + simulateTotal + " (" + NumberFormat.getInstance().format(simulateTotal) + ")");
            } else {
                System.out.println("Total for " + filename + " = " + simulateTotal + " (" + NumberFormat.getInstance().format(simulateTotal) + ")");
            }

            total += simulateTotal;
        }
        if (total > bestScore.values().stream().mapToInt(i -> i).sum()) {
            System.out.println("[BEST] Total = " + NumberFormat.getInstance().format(total));
        } else {
            System.out.println("Total = " + NumberFormat.getInstance().format(total));
        }
        return holder;
    }

    private static void readInputFileToSimulation(Map<String, Maps> holder, Path file, String filename, Simulation simulation) {
        try (Stream<String> stream = Files.lines(Paths.get(file.toString()))) {
            AtomicInteger nextRideId = new AtomicInteger();
            stream.forEach((String line) -> {
                String[] lineParsed = line.split(" ");
                if (!simulation.isInitialized()) {
                    simulation.setMaps(new Maps(lineParsed[0], lineParsed[1], lineParsed[2], lineParsed[4], lineParsed[5]));
                } else {
                    String rideId = String.valueOf(nextRideId.getAndIncrement());
                    simulation.addRide(new BookedRide(simulation, rideId,
                            new Point(Integer.parseInt(lineParsed[0]), Integer.parseInt(lineParsed[1])),
                            new Point(Integer.parseInt(lineParsed[2]), Integer.parseInt(lineParsed[3])),
                            Integer.parseInt(lineParsed[4]),
                            Integer.parseInt(lineParsed[5])));
                }
            });

            holder.put(filename, simulation.maps);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
