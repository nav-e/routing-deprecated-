package io.greennav.routing;

import de.topobyte.osm4j.core.model.impl.Node;
import de.topobyte.osm4j.core.model.impl.Way;
import gnu.trove.list.TLongList;
import gnu.trove.list.array.TLongArrayList;
import io.greennav.persistence.InMemoryPersistence;
import io.greennav.persistence.Persistence;
import javafx.util.Pair;
import org.junit.Test;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.assertEquals;

public class RoutingTests {

	/*
	Creates grid 5x5 with nodes in integer points. As soon as there are no unique shortest path in this graph for most
	of pairs, then it is necessary to use asymmetric distance function, e.g. |y * (y - x)|, prioritizing nodes
	with lower value of y
	 */
	private void createGridGraph(Map<Pair<Double, Double>, Node> nodes, List<Pair<Node, Node>> edges) {
		AtomicLong idCounter = new AtomicLong();
		for (double lat = 0.; lat <= 5.; lat += 1.) {
			for (double lon = 0.; lon <= 5.; lon += 1.) {
				for (double lat_delta : new double[]{-1., 0., 1.}) {
					for (double lon_delta : new double[]{-1., 0., 1.}) {
						final double lat_new = lat + lat_delta, lon_new = lon + lon_delta;
						if (lat_new < 0. || lat_new > 5. || lon_new < 0. || lon_new > 5.) continue;
						final long id = idCounter.getAndAdd(1);
						nodes.putIfAbsent(
								new Pair<>(lat_new, lon_new),
								new Node(id, lat_new, lon_new)
						);
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
		final Router router = new DijkstraRouter(nodes, edges, new DistanceComputerInKm());
		final Node begin = nodesArray[0];
		final Node end = nodesArray[nodesArray.length - 1];
		final List<Node> expected = Arrays.asList(begin, end);
		final List<Node> actual = router.getShortestPath(begin, end).getRoute();
		assertEquals(expected, actual);
	}

	@Test
	public void aStarRouterTest() {
		final Map<Pair<Double, Double>, Node> nodes = new HashMap<>();
		final List<Pair<Node, Node>> edges = new ArrayList<>();
		createGridGraph(nodes, edges);
		final Node begin = nodes.get(new Pair<>(0., 0.)), end = nodes.get(new Pair<>(5., 5.));
		final NodeWeightFunction weightFunction = (lhs, rhs) ->
				Math.abs(lhs.getLatitude() * (lhs.getLatitude() - lhs.getLongitude()));
		final Router dijkstraRouter = new DijkstraRouter(nodes.values(), edges, weightFunction);
		final Router aStarRouter = new AStarRouter(nodes.values(), edges, weightFunction);
		final List<Node> expected = dijkstraRouter.getShortestPath(begin, end).getRoute();
		final List<Node> actual = aStarRouter.getShortestPath(begin, end).getRoute();
		assertEquals(expected, actual);
	}

	@Test
	public void dijkstraPersistenceRouterTest() {
		final Persistence persistence = new InMemoryPersistence();
		final Node[] nodesArray = {
				new Node(0, 0, 0),
				new Node(1, 0, 1),
				new Node(2, 1, 0),
				new Node(3, 1, 1)
		};
		final List<Node> nodes = Arrays.asList(nodesArray);
		nodes.forEach(persistence::writeNode);
		final TLongList firstWayIds = new TLongArrayList(new long[]{0, 1, 2});
		final TLongList secondWayIds = new TLongArrayList(new long[]{1, 3});
		final TLongList thirdWayIds = new TLongArrayList(new long[]{0, 3});
		persistence.writeWay(new Way(0, firstWayIds));
		persistence.writeWay(new Way(1, secondWayIds));
		persistence.writeWay(new Way(2, thirdWayIds));
		final Router router = new DijkstraRouter(persistence, new DistanceComputerInKm());
		final Node begin = nodesArray[0];
		final Node end = nodesArray[nodesArray.length - 1];
		final List<Node> actual = Arrays.asList(begin, end);
		final List<Node> expected = router.getShortestPath(begin, end).getRoute();
		assertEquals(expected, actual);
	}

	@Test
	public void aStarPersistenceRouterTest() {
		final Persistence persistence = new InMemoryPersistence();
		final Map<Pair<Double, Double>, Node> nodes = new HashMap<>();
		final List<Pair<Node, Node>> edges = new ArrayList<>();
		createGridGraph(nodes, edges);
		nodes.values().forEach(persistence::writeNode);
		AtomicLong idCounter = new AtomicLong();
		edges.forEach(pair -> {
			Node source = pair.getKey();
			Node target = pair.getValue();
			final TLongList edgeIds = new TLongArrayList(new long[]{source.getId(), target.getId()});
			persistence.writeWay(new Way(idCounter.getAndAdd(1), edgeIds));
		});
		final Node begin = nodes.get(new Pair<>(0., 0.)), end = nodes.get(new Pair<>(5., 5.));
		final NodeWeightFunction weightFunction = (lhs, rhs) ->
				Math.abs(lhs.getLatitude() * (lhs.getLatitude() - lhs.getLongitude()));
		final Router dijkstraRouter = new DijkstraRouter(persistence, weightFunction);
		final Router aStarRouter = new AStarRouter(persistence, weightFunction);
		final List<Node> expected = dijkstraRouter.getShortestPath(begin, end).getRoute();
		final List<Node> actual = aStarRouter.getShortestPath(begin, end).getRoute();
		assertEquals(expected, actual);
	}
}
