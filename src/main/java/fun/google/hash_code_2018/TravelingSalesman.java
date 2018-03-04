package fun.google.hash_code_2018;

import fun.google.hash_code_2018.file_parser.ReadFile;
import fun.google.hash_code_2018.model.Point;
import fun.google.hash_code_2018.model.Ride;
import fun.google.hash_code_2018.model.Maps;
import fun.google.hash_code_2018.model.Ride;
import fun.google.hash_code_2018.model.StartingRide;
import io.jenetics.EnumGene;
import io.jenetics.Gene;
import io.jenetics.Optimize;
import io.jenetics.PartiallyMatchedCrossover;
import io.jenetics.Phenotype;
import io.jenetics.SwapMutator;
import io.jenetics.engine.Codec;
import io.jenetics.engine.Codecs;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionStatistics;
import io.jenetics.engine.Problem;
import io.jenetics.util.ISeq;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.sun.webkit.graphics.RenderTheme.METER;
import static io.jenetics.engine.EvolutionResult.toBestPhenotype;
import static io.jenetics.engine.Limits.bySteadyFitness;

public final class TravelingSalesman implements Problem<ISeq<Ride>, EnumGene<Ride>, Double> {

	private final ISeq<Ride> _points;
	private Maps currentMap;

	/**
	 * Create a new TSP instance with the way-points we want to visit.
	 *
	 * @param points the way-points we want to visit
	 * @throws NullPointerException if the given {@code points} seq is {@code null}
	 */
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
				if (duration <= currentRide.getLatestFinish() && duration < currentMap.getSteps()) {
					totalScore += currentRide.getScore();
				}
				previousPoint = currentRide.getFinish();
            }

            return totalScore;
        };
	}

	public static void main(String[] args) throws IOException, URISyntaxException {
		Map<String, Maps> maps = ReadFile.getFileFromPath();
		Maps currentMap = maps.get("b_should_be_easy.in");
		List<Ride> rides = currentMap.getRides();
		for (int i = 0; i < currentMap.getVehicles(); i++) {
			rides.add(new StartingRide());
		}
		final TravelingSalesman tsm =
			new TravelingSalesman(currentMap, ISeq.of(rides));

		final Engine<EnumGene<Ride>, Double> engine = Engine.builder(tsm)
			.optimize(Optimize.MAXIMUM)
			.alterers(
				new SwapMutator<>(0.55),
				new PartiallyMatchedCrossover<>(0.55)
			)
			.build();

		// Create evolution statistics consumer.
		final EvolutionStatistics<Double, ?>
			statistics = EvolutionStatistics.ofNumber();

		final Phenotype<EnumGene<Ride>, Double> best = engine.stream()
            .limit(bySteadyFitness(1000))
			.limit(100_000)
			.peek(statistics)
			.collect(toBestPhenotype());

		final ISeq<Ride> path = best.getGenotype()
			.getChromosome().toSeq()
			.map(Gene::getAllele);

		double bestSum = tsm.fitness().apply(path);

		System.out.println(statistics);
		System.out.println("Best score: " + bestSum);
	}
}