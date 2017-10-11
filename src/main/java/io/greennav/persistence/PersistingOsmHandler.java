package io.greennav.persistence;

import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.*;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;

import java.util.*;

public class PersistingOsmHandler implements Sink {
    private Persistence persistence;

    public PersistingOsmHandler(Persistence persistence) {
        this.persistence = persistence;
    }

    private Map<String, String> convertTags(Collection<Tag> tags) {
        Map<String, String> newTags = new HashMap<>();

        for (Tag tag : tags) {
            newTags.put(tag.getKey(), tag.getValue());
        }

        return newTags;
    }

    private List<Long> convertNodes(Collection<WayNode> nodes) {
        List<Long> newNodes = new ArrayList<>();

        for (WayNode w : nodes) {
            newNodes.add(w.getNodeId());
        }

        return newNodes;
    }

    private void handle(Node node) {
        io.greennav.osm.Node n = new io.greennav.osm.Node(
                node.getId(),
                node.getLatitude(),
                node.getLongitude(),
                convertTags(node.getTags())
        );

        persistence.writeNode(n);
    }

    private void handle(Way way) {
        io.greennav.osm.Way w = new io.greennav.osm.Way(
                way.getId(),
                convertNodes(way.getWayNodes()),
                convertTags(way.getTags())
        );

        persistence.writeWay(w);
    }

    private void handle(Relation relation) {
        io.greennav.osm.Relation r = new io.greennav.osm.Relation(
                relation.getId(),
                convertTags(relation.getTags())
        );

        persistence.writeRelation(r);
    }

    @Override
    public void process(EntityContainer entityContainer) {
        if (entityContainer.getEntity() instanceof Node) {
            handle((Node) entityContainer.getEntity());
        } else if (entityContainer.getEntity() instanceof Way) {
            handle((Way) entityContainer.getEntity());
        } else if (entityContainer.getEntity() instanceof Relation) {
            handle((Relation) entityContainer.getEntity());
        }
    }

    @Override
    public void initialize(Map<String, Object> metaData) {
    }

    @Override
    public void close() {
    }

    @Override
    public void complete() {
    }
}
