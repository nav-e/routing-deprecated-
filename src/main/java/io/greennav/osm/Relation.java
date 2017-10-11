package io.greennav.osm;

import java.util.Map;

public class Relation {
    private long id;
    private Map<String, String> tags;

    public Relation(long id, Map<String, String> tags) {
        this.id = id;
        this.tags = tags;
    }

    public long getId() {
        return this.id;
    }

    public Map<String, String> getTags() {
        return this.tags;
    }
}
