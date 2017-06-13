package io.greennav.map;

import java.util.function.BiFunction;

public interface MapNodeWeightFunction extends BiFunction<MapNode, MapNode, Double> {}
