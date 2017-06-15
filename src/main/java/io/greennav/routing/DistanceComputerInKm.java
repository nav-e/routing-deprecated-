package io.greennav.routing;

import de.topobyte.osm4j.core.model.impl.Node;
import java.lang.Double;

public final class DistanceComputerInKm extends DistanceComputer {
    @Override
    public Double apply(Node lhs, Node rhs) {
        final double lhs_lon = lhs.getLongitude();
        final double rhs_lon = rhs.getLongitude();
        final double lhs_lat = lhs.getLatitude();
        final double rhs_lat = rhs.getLatitude();

        final double theta = lhs_lon - rhs_lon;
        double dist = Math.sin(deg2rad(lhs_lat)) * Math.sin(deg2rad(rhs_lat)) +
                Math.cos(deg2rad(lhs_lat)) * Math.cos(deg2rad(rhs_lat)) * Math.cos(deg2rad(theta));

        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        dist = dist * 1.609344;

        return dist;
    }
}
