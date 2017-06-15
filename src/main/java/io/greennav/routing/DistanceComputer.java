package io.greennav.routing;

import de.topobyte.osm4j.core.model.impl.Node;

public abstract class DistanceComputer implements MapNodeWeightFunction {
    @Override
    public abstract Double apply(Node lhs, Node rhs);

    protected static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    protected static double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }
}
