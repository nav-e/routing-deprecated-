package io.greennav.routing;

import io.greennav.map.MapNode;
import java.util.List;

public class Route {
    private final List<MapNode> route;
    private final long runtimeInMs;

    Route(List<MapNode> route, long runtimeInMs) {
        this.route = route;
        this.runtimeInMs = runtimeInMs;
    }

    public List<MapNode> getRoute() {
        return route;
    }

    public long getRuntimeInMs() {
        return runtimeInMs;
    }
}
