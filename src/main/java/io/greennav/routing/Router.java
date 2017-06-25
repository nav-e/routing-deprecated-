package io.greennav.routing;

import de.topobyte.osm4j.core.model.impl.Node;
import io.greennav.persistence.InMemoryPersistence;
import io.greennav.persistence.Persistence;
import javafx.util.Pair;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.List;

abstract class Router {
    protected final SimpleDirectedWeightedGraph<Node, RoadEdge> graph;

    Router(Persistence persistence, NodeWeightFunction weightFunction) {
        this.graph = new RoadGraph(persistence, weightFunction);
    }

    Router(Collection<Node> nodes, Collection<Pair<Node, Node>> edges, NodeWeightFunction weightFunction) {
        this.graph = new SimpleDirectedWeightedGraph<>(RoadEdge.class);
        nodes.forEach(graph::addVertex);
        edges.forEach(edge_pair -> {
            final Node source = edge_pair.getKey();
            final Node target = edge_pair.getValue();
            final RoadEdge edge = graph.addEdge(source, target);
            final double weight = weightFunction.apply(source, target);
            graph.setEdgeWeight(edge, weight);
        });
    }

    Route getShortestPath(Node start, Node finish) {
        graph.addVertex(start);
        graph.addVertex(finish);
        final Instant begin = Instant.now();
        final List<Node> routeNodes = getShortestPathAlgorithm(start, finish).getPath(start, finish).getVertexList();
        final Instant end = Instant.now();
        final long durationInMillis = Duration.between(begin, end).toMillis();
        return new Route(routeNodes, durationInMillis);
    }

    abstract ShortestPathAlgorithm<Node, RoadEdge> getShortestPathAlgorithm(Node source, Node target);
}
