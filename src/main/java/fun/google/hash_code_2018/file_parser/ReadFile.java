package fun.google.hash_code_2018.file_parser;

import fun.google.hash_code_2018.Simulation;
import fun.google.hash_code_2018.model.Maps;
import fun.google.hash_code_2018.model.Point;
import fun.google.hash_code_2018.model.BookedRide;
import fun.google.hash_code_2018.model.Ride;

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
        bestScore.put("b_should_be_easy.in", 176877);
        bestScore.put("c_no_hurry.in", 15810342);
        //bestScore.put("d_metropolis.in", 11868571);
        bestScore.put("d_metropolis.in", 11966217);
        bestScore.put("e_high_bonus.in", 21465945);
    }

    public static final Map<String, Double> longRideRatio = new HashMap<>();
    static {
        longRideRatio.put("a_example.in", 1d);
        longRideRatio.put("b_should_be_easy.in", 1d);
        longRideRatio.put("c_no_hurry.in", 108d);
        longRideRatio.put("d_metropolis.in", 0.44);
        longRideRatio.put("e_high_bonus.in", 2d);
    }

    public static final Map<String, Double> longRideMinStep = new HashMap<>();
    static {
        longRideMinStep.put("a_example.in", 0.98);
        longRideMinStep.put("b_should_be_easy.in", 0.98);
        longRideMinStep.put("c_no_hurry.in", 1.0);
        longRideMinStep.put("d_metropolis.in", 0.802);
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


            int simulateTotal = 0;
//            for (int i = 1; i < 5000; i++) {
                List<Ride> savesRides = new ArrayList<>(simulation.maps.getRides());
                simulation.maps.setLongRideRatio(longRideRatio.get(file.getFileName().toString()));
                simulation.maps.setLongRideMinStep(longRideMinStep.get(file.getFileName().toString()));
                int currentSimulationTotal = simulation.simulate();
                if (currentSimulationTotal > simulateTotal) {
                    simulateTotal = currentSimulationTotal;
//                    System.out.println("New best " + file.getFileName().toString() + " = " + simulateTotal + " (" + NumberFormat.getInstance().format(simulateTotal) + ") for " + i);
                    System.out.println("Remaining rides " + simulation.maps.getRides().size());
                } else {
//                    System.out.println("Not best " + file.getFileName().toString() + " = " + simulateTotal + " (" + NumberFormat.getInstance().format(simulateTotal) + ") for " + i);
                }
                simulation.maps.getRides().clear();
                simulation.maps.getRides().addAll(savesRides);
//            }

            if (simulateTotal > bestScore.get(file.getFileName().toString())) {
                System.out.println("[BEST] Total for " + file.getFileName().toString() + " = " + simulateTotal + " (" + NumberFormat.getInstance().format(simulateTotal) + ")");
            } else {
                System.out.println("Total for " + file.getFileName().toString() + " = " + simulateTotal + " (" + NumberFormat.getInstance().format(simulateTotal) + ")");
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


}
