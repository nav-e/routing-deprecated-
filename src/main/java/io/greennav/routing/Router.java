package io.greennav.routing;

import io.greennav.map.MapEdge;
import io.greennav.map.MapNode;
import io.greennav.map.MapNodeWeightFunction;
import javafx.util.Pair;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm;
import org.jgrapht.graph.SimpleWeightedGraph;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

public abstract class Router {
    protected final SimpleWeightedGraph<MapNode, MapEdge> graph = new SimpleWeightedGraph<>(MapEdge.class);

    Router(Iterable<MapNode> nodes, Iterable<Pair<MapNode, MapNode>> edges, MapNodeWeightFunction weightFunction) {
        nodes.forEach(graph::addVertex);
        edges.forEach(edge_pair -> {
            final MapNode source = edge_pair.getKey();
            final MapNode target = edge_pair.getValue();
            final MapEdge edge = graph.addEdge(source, target);
            final double weight = weightFunction.apply(source, target);
            graph.setEdgeWeight(edge, weight);
        });
    }

    public Route getRoute(MapNode start, MapNode finish) {
        final Instant begin = Instant.now();
        final List<MapNode> routeNodes = getShortestPath(start, finish).getPath(start, finish).getVertexList();
        final Instant end = Instant.now();
        final long durationInMillis = Duration.between(begin, end).toMillis();
        return new Route(routeNodes, durationInMillis);
    }

    abstract ShortestPathAlgorithm<MapNode, MapEdge> getShortestPath(MapNode source, MapNode target);
}