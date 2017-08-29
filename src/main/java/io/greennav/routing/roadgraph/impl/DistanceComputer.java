package io.greennav.routing.roadgraph.impl;

import de.topobyte.osm4j.core.model.impl.Node;
import io.greennav.routing.roadgraph.iface.NodeWeightFunction;

public abstract class DistanceComputer implements NodeWeightFunction {
    public abstract Double apply(Node lhs, Node rhs);

    static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    static double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }

    static double haversine(double phi) { return (1 - Math.cos(phi)) / 2.0;}

    static final double kEarthRadiusInMetres = 6378137.0;
}
