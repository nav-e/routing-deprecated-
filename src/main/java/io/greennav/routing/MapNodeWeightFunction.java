package io.greennav.routing;

import de.topobyte.osm4j.core.model.impl.Node;
import java.util.function.BiFunction;

interface MapNodeWeightFunction extends BiFunction<Node, Node, Double> {}
