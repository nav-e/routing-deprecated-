package io.greennav.routing.roadgraph.impl;

import de.topobyte.osm4j.core.model.impl.Node;
import io.greennav.persistence.Persistence;
import io.greennav.routing.roadgraph.iface.NodeWeightFunction;
import org.jgrapht.Graphs;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.jgrapht.graph.specifics.Specifics;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Function;

public class RoadGraph<E extends RoadEdge> extends SimpleDirectedWeightedGraph<Node, E> {
    protected final Persistence persistence;
    private final NodeWeightFunction nodeWeightFunction;
    private final Set<Node> cachedIncomingNeighbors = new LinkedHashSet<>();
    private final Set<Node> cachedOutgoingNeighbors = new LinkedHashSet<>();

    public RoadGraph(Persistence persistence, NodeWeightFunction nodeWeightFunction, Class<E> edgeClass) {
        super(edgeClass);
        this.persistence = persistence;
        this.nodeWeightFunction = nodeWeightFunction;
    }

    private void computeAndSetEdgeWeight(E edge) {
        Node source = getEdgeSource(edge);
        Node target = getEdgeTarget(edge);
        final double weight = nodeWeightFunction.apply(source, target);
        setEdgeWeight(edge, weight);
    }

    void fullInitialize() {
        Graphs.addAllVertices(this, persistence.getAllNodes());
        persistence.getAllWays().forEach(way -> {
            for (int i = 0; i < way.getNumberOfNodes() - 1; ++i) {
                final Long fromId = way.getNodeId(i);
                final Long toId = way.getNodeId(i + 1);
                final Node from = persistence.getNodeById(fromId);
                final Node to = persistence.getNodeById(toId);
                E edge = addEdge(from, to);
                computeAndSetEdgeWeight(edge);
            }
        });
    }

    public void reset() {
        cachedIncomingNeighbors.clear();
        cachedOutgoingNeighbors.clear();
        new LinkedHashSet<>(edgeSet()).forEach(this::removeEdge);
        new LinkedHashSet<>(vertexSet()).forEach(this::removeVertex);
    }

    private void cacheNeighborsIfAbsent(Node node, boolean outgoing) {
        final Set<Node> container = outgoing ? cachedOutgoingNeighbors : cachedIncomingNeighbors;
        final Function<Node, Set<Node>> neighborsFunction = outgoing ?
                persistence::outgoingNeighbors : persistence::incomingNeighbors;
        if (!container.contains(node)) {
            final Set<Node> neighbors = neighborsFunction.apply(node);
            neighbors.forEach(neighbor -> {
                addVertex(neighbor);
                final E edge = outgoing ? getOrAddEdge(node, neighbor) : getOrAddEdge(neighbor, node);
                computeAndSetEdgeWeight(edge);
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

    private E getOrAddEdge(Node sourceVertex, Node targetVertex) {
        E edge = getEdge(sourceVertex, targetVertex);
        if (edge == null) {
            edge = addEdge(sourceVertex, targetVertex);
        }
        return edge;
    }

    @Override
    protected Specifics<Node, E> createSpecifics() {
        return new RoadGraphSpecifics<>(this);
    }
}
