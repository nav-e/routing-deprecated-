package io.greennav.persistence;

import de.topobyte.osm4j.core.access.OsmHandler;
import de.topobyte.osm4j.core.model.iface.OsmBounds;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.core.model.impl.Node;
import de.topobyte.osm4j.core.model.impl.Relation;
import de.topobyte.osm4j.core.model.impl.Way;
import java.io.IOException;

public class PersistingOsmHandler implements OsmHandler {
    private Persistence persistence;

    public PersistingOsmHandler(Persistence persistence) {
        this.persistence = persistence;
    }

    @Override
    public void handle(OsmBounds bounds) throws IOException {
    }

    @Override
    public void handle(OsmNode node) throws IOException {
        persistence.writeNode((Node) node);
    }

    @Override
    public void handle(OsmWay way) throws IOException {
        persistence.writeWay((Way) way);
    }

    @Override
    public void handle(OsmRelation relation) throws IOException {
        persistence.writeRelation((Relation) relation);
    }

    @Override
    public void complete() throws IOException {
    }
}
