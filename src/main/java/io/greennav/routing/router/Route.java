package io.greennav.routing.router;

import de.topobyte.osm4j.core.model.impl.Node;
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
