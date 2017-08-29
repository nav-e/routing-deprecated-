package io.greennav.routing.roadgraph.impl;

import de.topobyte.osm4j.core.model.impl.Node;
import org.jgrapht.graph.specifics.FastLookupDirectedSpecifics;
import java.util.Set;

public class RoadGraphSpecifics<E extends RoadEdge> extends FastLookupDirectedSpecifics<Node, E> {
    RoadGraphSpecifics(RoadGraph<E> roadGraph) {
        super(roadGraph);
    }

    private RoadGraph getRoadGraph() {
        return (RoadGraph)abstractBaseGraph;
    }

    @Override
    public Set<E> incomingEdgesOf(Node node) {
        getRoadGraph().cacheIncomingNeighborsIfAbsent(node);
        return getEdgeContainer(node).getUnmodifiableIncomingEdges();
    }

    @Override
    public Set<E> outgoingEdgesOf(Node node) {
        getRoadGraph().cacheOutgoingNeighborsIfAbsent(node);
        return getEdgeContainer(node).getUnmodifiableOutgoingEdges();
    }
}
