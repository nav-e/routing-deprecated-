package io.greennav.osm;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Way {
    private long id;
    private List<Long> nodes;
    private Map<String, String> tags;

    public Way(long id, List<Long> nodes, Map<String, String> tags) {
        this.id = id;
        this.nodes = nodes;
        this.tags = tags;
    }

    public Way(long id, List<Long> nodes) {
        new Way(id, nodes, new HashMap<>());
    }

    public long getId() {
        return this.id;
    }

    public List<Long> getNodes() {
        return this.nodes;
    }

    public Map<String, String> getTags() {
        return this.tags;
    }
}
