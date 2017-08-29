package io.greennav.routing.roadgraph.impl;

import de.topobyte.osm4j.core.model.impl.Node;
import java.lang.Double;

public final class DistanceComputerInMetres extends DistanceComputer {
    @Override
    public Double apply(Node lhs, Node rhs) {
        final double lhsLon = deg2rad(lhs.getLongitude());
        final double rhsLon = deg2rad(rhs.getLongitude());
        final double lhsLat = deg2rad(lhs.getLatitude());
        final double rhsLat = deg2rad(rhs.getLatitude());
        final double deltaLon = rhsLon - lhsLon;
        final double deltaLat = rhsLat - lhsLat;
        final double haversineOfRatio = haversine(deltaLat) + Math.cos(lhsLat) * Math.cos(rhsLat) * haversine(deltaLon);
        return 2 * kEarthRadiusInMetres * Math.asin(Math.sqrt(haversineOfRatio));
    }
}
