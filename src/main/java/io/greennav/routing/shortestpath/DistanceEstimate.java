package io.greennav.routing.shortestpath;

import de.topobyte.osm4j.core.model.impl.Node;

public class DistanceEstimate {
    public Node node;
    public double weight;
    public long edgeNumber;
    public Node predecessor;

    DistanceEstimate(Node node, double weight, long edgeNumber) {
        this.node = node;
        this.weight = weight;
        this.edgeNumber = edgeNumber;
        this.predecessor = node;
    }

    DistanceEstimate(Node node, double weight) {
        this(node, weight, Integer.MAX_VALUE);
    }

    DistanceEstimate(Node node) {
        this(node, Double.POSITIVE_INFINITY, Integer.MAX_VALUE);
    }

    DistanceEstimate(DistanceEstimate other) {
        this.node = other.node;
        this.weight = other.weight;
        this.edgeNumber = other.edgeNumber;
        this.predecessor = other.predecessor;
    }
}
