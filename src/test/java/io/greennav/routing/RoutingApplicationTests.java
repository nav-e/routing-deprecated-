package io.greennav.routing;

import io.greennav.routing.DistanceComputerInKm;
import io.greennav.routing.MapNodeWeightFunction;
import io.greennav.routing.AStarRouter;
import io.greennav.routing.DijkstraRouter;
import io.greennav.routing.Router;
import de.topobyte.osm4j.core.model.impl.Node;
import javafx.util.Pair;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RoutingApplicationTests {

	@Test
	public void dijkstraRouterTest() {
		final Node[] nodes_array = {
				new Node(0, 0, 0),
				new Node(1, 0, 1),
				new Node(2, 1, 0),
				new Node(3, 1, 1)
		};
		final List<Node> nodes = Arrays.asList(nodes_array);
		final List<Pair<Node, Node>> edges = Arrays.asList(new Pair[] {
				new Pair<>(nodes_array[0], nodes_array[1]),
				new Pair<>(nodes_array[1], nodes_array[2]),
				new Pair<>(nodes_array[1], nodes_array[3]),
				new Pair<>(nodes_array[0], nodes_array[3])
		});
		final Router router = new DijkstraRouter(nodes, edges, new DistanceComputerInKm());
		final Node begin = nodes_array[0];
		final Node end = nodes_array[nodes_array.length - 1];
		final List<Node> actual = Arrays.asList(begin, end);
		final List<Node> expected = router.getShortestPath(begin, end).getRoute();
		assertEquals(expected, actual);
	}

	@Test
	public void aStarRouterTest() {
		final Map<Pair<Double, Double>, Node> nodes = new HashMap<>();
		final List<Pair<Node, Node>> edges = new ArrayList<>();
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
		final Node begin = nodes.get(new Pair<>(0., 0.)), end = nodes.get(new Pair<>(5., 5.));
		final MapNodeWeightFunction weightFunction = (lhs, rhs) ->
				Math.abs(lhs.getLatitude() * (lhs.getLatitude() - lhs.getLongitude()));
		final Router dijkstraRouter = new DijkstraRouter(nodes.values(), edges, weightFunction);
		final Router aStarRouter = new AStarRouter(nodes.values(), edges, weightFunction);
		final List<Node> expected = dijkstraRouter.getShortestPath(begin, end).getRoute();
		final List<Node> actual = aStarRouter.getShortestPath(begin, end).getRoute();
		assertEquals(expected, actual);
	}
}
