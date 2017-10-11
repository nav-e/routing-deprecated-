package io.greennav.routing.roadgraph.impl;

import io.greennav.osm.Node;
import io.greennav.routing.roadgraph.iface.NodeWeightFunction;

public abstract class DistanceComputer implements NodeWeightFunction {
    static final double kEarthRadiusInMetres = 6378137.0;

    static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    static double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }

    static double haversine(double phi) {
        return (1 - Math.cos(phi)) / 2.0;
    }

    public abstract Double apply(Node lhs, Node rhs);
}
