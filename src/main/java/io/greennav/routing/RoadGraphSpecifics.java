package io.greennav.routing;

import de.topobyte.osm4j.core.model.impl.Node;
import org.jgrapht.graph.EdgeSetFactory;
import org.jgrapht.graph.specifics.DirectedEdgeContainer;
import org.jgrapht.graph.specifics.FastLookupDirectedSpecifics;
import java.util.Map;
import java.util.Set;

public class RoadGraphSpecifics extends FastLookupDirectedSpecifics<Node, RoadEdge> {
    RoadGraphSpecifics(RoadGraph roadGraph) {
        super(roadGraph);
    }

    RoadGraphSpecifics(RoadGraph roadGraph,
                       Map<Node, DirectedEdgeContainer<Node, RoadEdge>> vertexMap) {
        super(roadGraph, vertexMap);
    }

    RoadGraphSpecifics(RoadGraph roadGraph,
                       Map<Node, DirectedEdgeContainer<Node, RoadEdge>> vertexMap,
                       EdgeSetFactory<Node, RoadEdge> edgeEdgeSetFactory) {
        super(roadGraph, vertexMap, edgeEdgeSetFactory);
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
