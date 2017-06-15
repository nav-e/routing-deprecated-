package io.greennav.persistence;

import de.topobyte.osm4j.core.model.impl.Node;
import de.topobyte.osm4j.core.model.impl.Relation;
import de.topobyte.osm4j.core.model.impl.Way;
import de.topobyte.osm4j.core.model.util.OsmModelUtil;
import java.util.*;

public class InMemoryPersistence implements Persistence {
    public Map<Long, Node> nodes;
    public Map<Long, Way> ways;
    public Map<Long, Relation> relations;
    public Map<Long, Set<Long>> neighbors;

    public InMemoryPersistence() {
        nodes = new HashMap<>();
        ways = new HashMap<>();
        relations = new HashMap<>();
        neighbors = new HashMap<>();
    }

    @Override
    public void writeNode(Node node) {
        nodes.put(node.getId(), node);
    }

    @Override
    public void writeWay(Way way) {
        ways.put(way.getId(), way);

        for (int i = 0; i < way.getNumberOfNodes() - 1; i++) {
            Long fromId = way.getNodeId(i);
            Long toId = way.getNodeId(i + 1);

            putOrCreateNeighbor(fromId, toId);
            putOrCreateNeighbor(toId, fromId);
        }
    }

    private void putOrCreateNeighbor(Long fromId, Long toId) {
        neighbors.putIfAbsent(fromId, new HashSet<>());
        neighbors.get(fromId).add(toId);
    }

    @Override
    public void writeRelation(Relation relation) {
        relations.put(relation.getId(), relation);
    }

    @Override
    public Node getNodeById(long id) throws Exception {
        if (!nodes.containsKey(id)) {
            throw new Exception("Node with id " + id + " was not found");
        }

        return nodes.get(id);
    }

    @Override
    public Way getWayById(long id) {
        return ways.get(id);
    }

    @Override
    public Relation getRelationById(long id) {
        return relations.get(id);
    }

    @Override
    public Collection<Node> queryNodes(String key, String value) {
        List<Node> results = new ArrayList<>();

        for (Node node : nodes.values()) {
            Map<String, String> tags = OsmModelUtil.getTagsAsMap(node);

            if (getNeighbors(node).size() > 0
                    && tags.containsKey(key)
                    && tags.get(key).contains(value)) {
                results.add(node);
            }
        }

        return results;
    }

    @Override
    public Collection<Way> queryEdges(String key, String value) {
        List<Way> results = new ArrayList<>();

        for (Way way : ways.values()) {
            Map<String, String> tags = OsmModelUtil.getTagsAsMap(way);

            if (tags.containsKey(key)) {
                if (tags.get(key).contains(value)) {
                    results.add(way);
                }
            }
        }

        return results;
    }

    @Override
    public Collection<Relation> queryRelations(String key, String value) {
        List<Relation> results = new ArrayList<>();

        for (Relation relation : relations.values()) {
            Map<String, String> tags = OsmModelUtil.getTagsAsMap(relation);

            if (tags.containsKey(key)) {
                if (tags.get(key).contains(value)) {
                    results.add(relation);
                }
            }
        }

        return results;
    }

    @Override
    public Set<Node> getNeighbors(Node node) {
        Set<Node> result = new HashSet<>();

        if (!neighbors.containsKey(node.getId())) {
            System.out.println("NOT IN MEMORY: " + node.getId());
            return result;
        }

        for (Long id : neighbors.get(node.getId())) {
            result.add(nodes.get(id));
        }

        return result;
    }
}
