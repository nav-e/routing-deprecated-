package io.greennav.routing;

import de.topobyte.osm4j.core.model.impl.Node;
import io.greennav.persistence.Persistence;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.jgrapht.graph.specifics.Specifics;
import java.util.Set;

public class RoadGraph extends SimpleDirectedWeightedGraph<Node, RoadEdge> {
    private Persistence persistence;
    private NodeWeightFunction nodeWeightFunction;

    RoadGraph(Persistence persistence, NodeWeightFunction nodeWeightFunction) {
        super(RoadEdge.class);
        this.persistence = persistence;
        this.nodeWeightFunction = nodeWeightFunction;
    }

    Set<Node> getNeighbors(Node node) {
        return persistence.getNeighbors(node);
    }

    NodeWeightFunction getNodeWeightFunction() {
        return nodeWeightFunction;
    }

    @Override
    protected Specifics<Node, RoadEdge> createSpecifics() {
        return new RoadGraphSpecifics(this);
    }
}
