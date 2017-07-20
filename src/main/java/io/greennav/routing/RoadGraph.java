package io.greennav.routing;

import de.topobyte.osm4j.core.model.impl.Node;
import io.greennav.persistence.Persistence;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.jgrapht.graph.specifics.Specifics;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

public class RoadGraph extends SimpleDirectedWeightedGraph<Node, RoadEdge> {
    private final Persistence persistence;
    private final NodeWeightFunction nodeWeightFunction;
    private final Set<Node> cachedIncomingNeighbors = new HashSet<>();
    private final Set<Node> cachedOutgoingNeighbors = new HashSet<>();

    RoadGraph(Persistence persistence, NodeWeightFunction nodeWeightFunction) {
        super(RoadEdge.class);
        this.persistence = persistence;
        this.nodeWeightFunction = nodeWeightFunction;
    }

    void reset() {
        cachedIncomingNeighbors.clear();
        cachedOutgoingNeighbors.clear();
        new HashSet<>(edgeSet()).forEach(this::removeEdge);
        new HashSet<>(vertexSet()).forEach(this::removeVertex);
    }

    private void cacheNeighborsIfAbsent(Node node, boolean outgoing) {
        final Set<Node> container = outgoing ? cachedOutgoingNeighbors : cachedIncomingNeighbors;
        final Function<Node, Set<Node>> neighborsFunction = outgoing ?
                persistence::outgoingNeighbors : persistence::incomingNeighbors;
        if (!container.contains(node)) {
            final Set<Node> neighbors = neighborsFunction.apply(node);
            neighbors.forEach(neighbor -> {
                addVertex(neighbor);
                final RoadEdge edge = outgoing ? getOrAddEdge(node, neighbor) : getOrAddEdge(neighbor, node);
                final double weight = nodeWeightFunction.apply(node, neighbor);
                setEdgeWeight(edge, weight);
            });
            container.add(node);
        }
    }

    void cacheIncomingNeighborsIfAbsent(Node node) {
        cacheNeighborsIfAbsent(node, false);
    }

    void cacheOutgoingNeighborsIfAbsent(Node node) {
        cacheNeighborsIfAbsent(node, true);
    }

    private RoadEdge getOrAddEdge(Node sourceVertex, Node targetVertex) {
        RoadEdge edge = getEdge(sourceVertex, targetVertex);
        if (edge == null) {
            edge = addEdge(sourceVertex, targetVertex);
        }
        return edge;
    }

    @Override
    protected Specifics<Node, RoadEdge> createSpecifics() {
        return new RoadGraphSpecifics(this);
    }
}
