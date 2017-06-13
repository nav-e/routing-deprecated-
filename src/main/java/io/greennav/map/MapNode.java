package io.greennav.map;

import jdk.nashorn.internal.objects.annotations.Getter;

public class MapNode {
    private final double latitude;
    private final double longitude;

    public MapNode(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @Getter
    public double getLatitude() {
        return latitude;
    }

    @Getter
    public double getLongitude() {
        return longitude;
    }
}
