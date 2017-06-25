package io.greennav.routing;

import de.topobyte.osm4j.core.model.impl.Node;
import org.jgrapht.graph.EdgeSetFactory;
import org.jgrapht.graph.specifics.DirectedEdgeContainer;
import org.jgrapht.graph.specifics.FastLookupDirectedSpecifics;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RoadGraphSpecifics extends FastLookupDirectedSpecifics<Node, RoadEdge> {
    protected Set<Node> cachedNeighbors = new HashSet<>();

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
        if (!cachedNeighbors.contains(node)) {
            final RoadGraph roadGraph = getRoadGraph();
            final Set<Node> neighbors = roadGraph.getNeighbors(node);
            neighbors.forEach(neighbor -> {
                roadGraph.addVertex(neighbor);
                final RoadEdge edge = roadGraph.addEdge(node, neighbor);
                final double weight = roadGraph.getNodeWeightFunction().apply(node, neighbor);
                roadGraph.setEdgeWeight(edge, weight);
            });
            cachedNeighbors.add(node);
        }
        return getEdgeContainer(node).getUnmodifiableOutgoingEdges();
    }
}
