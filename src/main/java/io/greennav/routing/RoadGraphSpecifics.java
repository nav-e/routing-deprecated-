package io.greennav.routing;

import de.topobyte.osm4j.core.model.impl.Node;
import org.jgrapht.graph.specifics.FastLookupDirectedSpecifics;
import java.util.Set;

public class RoadGraphSpecifics extends FastLookupDirectedSpecifics<Node, RoadEdge> {
    RoadGraphSpecifics(RoadGraph roadGraph) {
        super(roadGraph);
    }

    private RoadGraph getRoadGraph() {
        return (RoadGraph)abstractBaseGraph;
    }

    @Override
    public Set<RoadEdge> outgoingEdgesOf(Node node) {
        getRoadGraph().cacheNeighborsIfAbsent(node);
        return getEdgeContainer(node).getUnmodifiableOutgoingEdges();
    }
}
