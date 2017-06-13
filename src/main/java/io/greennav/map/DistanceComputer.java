package io.greennav.map;

public abstract class DistanceComputer implements MapNodeWeightFunction {
    @Override
    public abstract Double apply(MapNode lhs, MapNode rhs);

    protected static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    protected static double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }
}
