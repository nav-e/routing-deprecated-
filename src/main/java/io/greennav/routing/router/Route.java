package io.greennav.routing.router;

import io.greennav.osm.Node;

import java.util.List;

public class Route {
    private final List<Node> route;
    private final long runtimeInMs;

    Route(List<Node> route, long runtimeInMs) {
        this.route = route;
        this.runtimeInMs = runtimeInMs;
    }

    public List<Node> getRoute() {
        return route;
    }

    long getRuntimeInMs() {
        return runtimeInMs;
    }
}
