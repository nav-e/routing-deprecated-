package io.greennav.routing;

import de.topobyte.osm4j.core.model.impl.Node;
import io.greennav.routing.roadgraph.impl.DistanceComputerInMetres;
import io.greennav.routing.roadgraph.iface.NodeWeightFunction;
import io.greennav.routing.router.*;
import javafx.util.Pair;
import org.junit.Test;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import static org.junit.Assert.*;

public class RoutingTests {
	private final String dataPath = "data/test";

	/*
	Creates grid 5x5 with nodes in integer points.
	 */
	private void createGridGraph(Map<Pair<Double, Double>, Node> nodes, List<Pair<Node, Node>> edges) {
		AtomicLong idCounter = new AtomicLong();
		for (double lat = 0.; lat <= 5.; lat += 1.) {
			for (double lon = 0.; lon <= 5.; lon += 1.) {
				for (double deltaLat : new double[]{-1., 0., 1.}) {
					for (double deltaLon : new double[]{-1., 0., 1.}) {
						final double newLat = lat + deltaLat, newLon = lon + deltaLon;
						if (newLat < 0. || newLat > 5. || newLon < 0. || newLon > 5.) continue;
						final long id = idCounter.getAndIncrement();
						nodes.putIfAbsent(new Pair<>(newLat, newLon), new Node(id, newLat, newLon));
					}
				}
				final Node source = nodes.get(new Pair<>(lat, lon));
				for (double[] delta : new double[][]{{0., 1.}, {1., 0.}}) {
					final double lat_delta = delta[0], lon_delta = delta[1];
					final double lat_new = lat + lat_delta, lon_new = lon + lon_delta;
					if (lat_new > 5. || lon_new > 5.) continue;
					final Node target = nodes.get(new Pair<>(lat + lat_delta, lon + lon_delta));
					edges.add(new Pair<>(source, target));
				}
			}
		}
	}

	private Pair<Map<Long, Node>, Map<Pair<Node, Node>, Double>> loadGraphComponents(final String testName) {
		final Path path = Paths.get(dataPath, "graphs", testName + ".gr");
		final Map<Long, Node> nodes = new LinkedHashMap<>();
		final Map<Pair<Node, Node>, Double> weightMap = new LinkedHashMap<>();
		try {
			final String[] firstLineParts = Files.lines(path).map(s -> s.split(" ")).findFirst().get();
			final long numberOfNodes = Long.parseLong(firstLineParts[0]);
			LongStream.range(1, numberOfNodes + 1).forEach(id -> nodes.put(id, new Node(id, 0d, 0d)));
			Files.lines(path).skip(1)
				 .map(s -> s.split(" "))
				 .forEach(lineParts -> {
					 final long fromId = Long.parseLong(lineParts[0]);
					 final long toId = Long.parseLong(lineParts[1]);
					 final double weight = Double.parseDouble(lineParts[2]);
					 weightMap.put(new Pair<>(nodes.get(fromId), nodes.get(toId)), weight);
				 });
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new Pair<>(nodes, weightMap);
	}

	private List<Pair<Node, Node>> loadQueries(final String testName, final Map<Long, Node> nodesMap) {
		List<Pair<Node, Node>> queries = new LinkedList<>();
		final Path path = Paths.get(dataPath, "queries", testName + ".txt");
		try (Stream<String> stream = Files.lines(path)) {
			stream.map(s -> s.split(" ")).forEach(lineParts -> {
				final long fromId = Long.parseLong(lineParts[0]);
				final long toId = Long.parseLong(lineParts[1]);
				queries.add(new Pair<>(nodesMap.get(fromId), nodesMap.get(toId)));
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
		return queries;
	}

	@Test
	public void dijkstraRouterTest() {
		final Node[] nodesArray = {
				new Node(0, 0, 0),
				new Node(1, 0, 1),
				new Node(2, 1, 0),
				new Node(3, 1, 1)
		};
		final List<Node> nodes = Arrays.asList(nodesArray);
		final List<Pair<Node, Node>> edges = Arrays.asList(
				new Pair<>(nodesArray[0], nodesArray[1]),
				new Pair<>(nodesArray[1], nodesArray[2]),
				new Pair<>(nodesArray[1], nodesArray[3]),
				new Pair<>(nodesArray[0], nodesArray[3])
		);
		final Router router = new DijkstraRouter(nodes, edges, new DistanceComputerInMetres());
		final Node begin = nodesArray[0];
		final Node end = nodesArray[nodesArray.length - 1];
		final List<Node> expected = Arrays.asList(begin, end);
		final List<Node> actual = router.getShortestPath(begin, end).getRoute();
		assertEquals(expected, actual);
	}

	private NodeWeightFunction euclideanDistanceFunction() {
		return (lhs, rhs) ->
				Math.abs(lhs.getLatitude() - rhs.getLatitude()) + Math.abs(lhs.getLongitude() - rhs.getLongitude());
	}

	@Test
	public void aStarRouterTest() {
		final Map<Pair<Double, Double>, Node> nodes = new HashMap<>();
		final List<Pair<Node, Node>> edges = new ArrayList<>();
		createGridGraph(nodes, edges);
		final Node begin = nodes.get(new Pair<>(0., 0.));
		final Node end = nodes.get(new Pair<>(5., 5.));
		final NodeWeightFunction weightFunction = euclideanDistanceFunction();
		final Router dijkstraRouter = new DijkstraRouter(nodes.values(), edges, weightFunction);
		final Router aStarRouter = new AStarRouter(nodes.values(), edges, weightFunction);
		final double expected = dijkstraRouter.getShortestPathWeight(begin, end);
		final double actual = aStarRouter.getShortestPathWeight(begin, end);
		assertTrue(expected > 0d && actual > 0d);
		assertEquals(expected, actual, 1e-6);
	}

	@Test
	public void bidirectionalDijkstraRouterTest() {
		final Map<Pair<Double, Double>, Node> nodes = new HashMap<>();
		final List<Pair<Node, Node>> edges = new ArrayList<>();
		createGridGraph(nodes, edges);
		final Node begin = nodes.get(new Pair<>(0., 0.));
		final Node end = nodes.get(new Pair<>(5., 5.));
		final NodeWeightFunction weightFunction = euclideanDistanceFunction();
		final Router dijkstraRouter = new DijkstraRouter(nodes.values(), edges, weightFunction);
		final Router bidirectionalDijkstraRouter =
				new BidirectionalDijkstraRouter(nodes.values(), edges, weightFunction);
		final double expected = dijkstraRouter.getShortestPathWeight(begin, end);
		final double actual = bidirectionalDijkstraRouter.getShortestPathWeight(begin, end);
		assertTrue(expected > 0d && actual > 0d);
		assertEquals(expected, actual, 1e-6);
	}

	@Test
	public void contractionHierarchiesTest() {
		final Map<Pair<Double, Double>, Node> nodes = new HashMap<>();
		final List<Pair<Node, Node>> edges = new ArrayList<>();
		createGridGraph(nodes, edges);
		final Node begin = nodes.get(new Pair<>(0., 0.));
		final Node end = nodes.get(new Pair<>(5., 5.));
		final NodeWeightFunction weightFunction = euclideanDistanceFunction();
		final Router dijkstraRouter = new DijkstraRouter(nodes.values(), edges, weightFunction);
		final Router contractionHierarchiesRouter = new ContractionHierarchiesRouter(
				nodes.values(), edges, weightFunction);
		final double expected = dijkstraRouter.getShortestPathWeight(begin, end);
		final double actual = contractionHierarchiesRouter.getShortestPathWeight(begin, end);
		assertTrue(expected > 0d && actual > 0d);
		assertEquals(expected, actual, 1e-6);
	}

	private void testFromFile(final String testName) {
		final Pair<Map<Long, Node>, Map<Pair<Node, Node>, Double>> graphComponents = loadGraphComponents(testName);
		final Map<Long, Node> nodesMap = graphComponents.getKey();
		final Collection<Node> nodes = nodesMap.values();
		final Map<Pair<Node, Node>, Double> weightMap = graphComponents.getValue();
		final List<Pair<Node, Node>> queries = loadQueries(testName, nodesMap);
		final Router bidirectionalDijkstraRouter = new BidirectionalDijkstraRouter(nodes, weightMap);
		final Router contractionHierarchiesRouter = new ContractionHierarchiesRouter(nodes, weightMap);
		queries.forEach(query -> {
			final Node from = query.getKey();
			final Node to = query.getValue();
			final double expected = bidirectionalDijkstraRouter.getShortestPathWeight(from, to);
			final double actual = contractionHierarchiesRouter.getShortestPathWeight(from, to);
			assertTrue(expected > 0d && actual > 0d);
			assertEquals(expected, actual, 1e-6);
		});
	}

	@Test
	public void test1() {
		testFromFile("test1");
	}

	@Test
	public void test2() {
		testFromFile("test2");
	}
}
