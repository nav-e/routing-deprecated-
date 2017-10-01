package io.greennav.routing;

import java.io.Serializable;

public class DisplayNode implements Serializable {
    private String display_name;
    private double osm_id;
    private double lon, lat;

    public DisplayNode(String name, Long id, double lon, double lat) {
        this.display_name = name;
        this.osm_id = id;
        this.lon = lon;
        this.lat = lat;
    }

    public double getOsm_id() {
        return osm_id;
    }

    public double getLon() {
        return lon;
    }

    public double getLat() {
        return lat;
    }

    public String getDisplay_name() {
        return display_name;
    }
}
