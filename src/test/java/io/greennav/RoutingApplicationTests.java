package io.greennav;

import io.greennav.map.DistanceComputerInKm;
import io.greennav.map.MapNodeWeightFunction;
import io.greennav.routing.AStarRouter;
import io.greennav.routing.DijkstraRouter;
import io.greennav.routing.Router;
import io.greennav.map.MapNode;
import javafx.util.Pair;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import java.util.*;
import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RoutingApplicationTests {

	@Test
	public void dijkstraRouterTest() {
		final MapNode[] nodes_array = {
				new MapNode(0, 0),
				new MapNode(0, 1),
				new MapNode(1, 0),
				new MapNode(1, 1)
		};
		final List<MapNode> nodes = Arrays.asList(nodes_array);
		final List<Pair<MapNode, MapNode>> edges = Arrays.asList(new Pair[] {
				new Pair<>(nodes_array[0], nodes_array[1]),
				new Pair<>(nodes_array[1], nodes_array[2]),
				new Pair<>(nodes_array[1], nodes_array[3]),
				new Pair<>(nodes_array[0], nodes_array[3])
		});
		final Router router = new DijkstraRouter(nodes, edges, new DistanceComputerInKm());
		final MapNode begin = nodes_array[0];
		final MapNode end = nodes_array[nodes_array.length - 1];
		final List<MapNode> actual = Arrays.asList(begin, end);
		final List<MapNode> expected = router.getRoute(begin, end).getRoute();
		assertEquals(expected, actual);
	}

	@Test
	public void aStarRouterTest() {
		final Map<Pair<Double, Double>, MapNode> nodes = new HashMap<>();
		final List<Pair<MapNode, MapNode>> edges = new ArrayList<>();
		for (double lat = 0.; lat <= 5.; lat += 1.) {
			for (double lon = 0.; lon <= 5.; lon += 1.) {
				for (double lat_delta : new double[]{-1., 0., 1.}) {
					for (double lon_delta : new double[]{-1., 0., 1.}) {
						double lat_new = lat + lat_delta, lon_new = lon + lon_delta;
						if (lat_new < 0. || lat_new > 5. || lon_new < 0. || lon_new > 5.) continue;
						nodes.putIfAbsent(
								new Pair<>(lat_new, lon_new),
								new MapNode(lat_new, lon_new)
						);
					}
				}
				final MapNode source = nodes.get(new Pair<>(lat, lon));
				for (double[] delta : new double[][]{{0., 1.}, {1., 0.}}) {
					double lat_delta = delta[0], lon_delta = delta[1];
					double lat_new = lat + lat_delta, lon_new = lon + lon_delta;
					if (lat_new > 5. || lon_new > 5.) continue;
					final MapNode target = nodes.get(new Pair<>(lat + lat_delta, lon + lon_delta));
					edges.add(new Pair<>(source, target));
				}
			}
		}
		final MapNode begin = nodes.get(new Pair<>(0., 0.)), end = nodes.get(new Pair<>(5., 5.));
		final MapNodeWeightFunction weightFunction = (lhs, rhs) ->
				Math.abs(lhs.getLatitude() * (lhs.getLatitude() - lhs.getLongitude()));
		final Router dijkstraRouter = new DijkstraRouter(nodes.values(), edges, weightFunction);
		final Router aStarRouter = new AStarRouter(nodes.values(), edges, weightFunction);
		final List<MapNode> expected = dijkstraRouter.getRoute(begin, end).getRoute();
		final List<MapNode> actual = aStarRouter.getRoute(begin, end).getRoute();
		assertEquals(expected, actual);
	}
}
