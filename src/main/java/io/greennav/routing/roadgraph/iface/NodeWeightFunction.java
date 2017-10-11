package io.greennav.routing.roadgraph.iface;

import io.greennav.osm.Node;

import java.util.function.BiFunction;

public interface NodeWeightFunction extends BiFunction<Node, Node, Double> {
}
