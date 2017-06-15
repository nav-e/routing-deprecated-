package io.greennav.routing;

import de.topobyte.osm4j.core.model.impl.Node;
import javafx.util.Pair;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm;
import org.jgrapht.graph.SimpleWeightedGraph;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

public abstract class Router {
    protected final SimpleWeightedGraph<Node, MapEdge> graph = new SimpleWeightedGraph<>(MapEdge.class);

    Router(Iterable<Node> nodes, Iterable<Pair<Node, Node>> edges, MapNodeWeightFunction weightFunction) {
        nodes.forEach(graph::addVertex);
        edges.forEach(edge_pair -> {
            final Node source = edge_pair.getKey();
            final Node target = edge_pair.getValue();
            final MapEdge edge = graph.addEdge(source, target);
            final double weight = weightFunction.apply(source, target);
            graph.setEdgeWeight(edge, weight);
        });
    }

    public Route getShortestPath(Node start, Node finish) {
        final Instant begin = Instant.now();
        final List<Node> routeNodes = getShortestPathAlgorithm(start, finish).getPath(start, finish).getVertexList();
        final Instant end = Instant.now();
        final long durationInMillis = Duration.between(begin, end).toMillis();
        return new Route(routeNodes, durationInMillis);
    }

    abstract ShortestPathAlgorithm<Node, MapEdge> getShortestPathAlgorithm(Node source, Node target);
}
