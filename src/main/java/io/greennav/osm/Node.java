package io.greennav.osm;

import java.util.HashMap;
import java.util.Map;

public class Node {
    private long id;
    private double lon, lat;
    private Map<String, String> tags;

    public Node(long id, double lon, double lat, Map<String, String> tags) {
        this.id = id;
        this.lon = lon;
        this.lat = lat;
        this.tags = tags;
    }

    public Node(long id, double lon, double lat) {
        this.id = id;
        this.lon = lon;
        this.lat = lat;
        this.tags = new HashMap<>();
    }

    public long getId() {
        return this.id;
    }

    public double getLongitude() {
        return this.lon;
    }

    public double getLatitude() {
        return this.lat;
    }

    public Map<String, String> getTags() {
        return this.tags;
    }
}
