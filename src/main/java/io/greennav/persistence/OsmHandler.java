package io.greennav.persistence;

import io.greennav.osm.Node;
import io.greennav.osm.Relation;
import io.greennav.osm.Way;

public interface OsmHandler {
    public void handle(Node node);

    public void handle(Way way);

    public void handle(Relation relation);

    public void complete();
}
