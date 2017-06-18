package io.greennav.routing;

import de.topobyte.osm4j.core.model.impl.Node;

public abstract class DistanceComputer implements MapNodeWeightFunction {
    public abstract Double apply(Node lhs, Node rhs);

    static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    static double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }
}
