package io.greennav.routing.roadgraph.impl;

import io.greennav.osm.Node;

import java.util.Optional;

public class RoadEdgeCH extends RoadEdge {
    private Optional<Node> intermediateNode = Optional.empty();

    Optional<Node> getIntermediateNode() {
        return intermediateNode;
    }

    void setIntermediateNode(final Node node) {
        intermediateNode = Optional.of(node);
    }
}
