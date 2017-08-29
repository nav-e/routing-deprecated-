package io.greennav.persistence;

import de.topobyte.osm4j.core.model.impl.Node;
import de.topobyte.osm4j.core.model.impl.Relation;
import de.topobyte.osm4j.core.model.impl.Way;
import de.topobyte.osm4j.core.model.util.OsmModelUtil;
import java.util.*;

public class InMemoryPersistence implements Persistence {
    private final Map<Long, Node> nodes = new HashMap<>();
    private final Map<Long, Way> ways = new HashMap<>();
    private final Map<Long, Relation> relations = new HashMap<>();
    private final Map<Long, Set<Long>> inNeighbors = new HashMap<>();
    private final Map<Long, Set<Long>> outNeighbors = new HashMap<>();

    @Override
    public void writeNode(Node node) {
        nodes.put(node.getId(), node);
    }

    @Override
    public void removeNode(Node node) {
        nodes.remove(node.getId());
    }

    @Override
    public void writeWay(Way way) {
        ways.put(way.getId(), way);
        for (int i = 0; i < way.getNumberOfNodes() - 1; ++i) {
            final Long fromId = way.getNodeId(i);
            final Long toId = way.getNodeId(i + 1);
            putOrCreateNeighbor(fromId, toId);
        }
    }

    private void putOrCreateNeighbor(Long fromId, Long toId) {
        outNeighbors.putIfAbsent(fromId, new HashSet<>());
        inNeighbors.putIfAbsent(toId, new HashSet<>());
        outNeighbors.get(fromId).add(toId);
        inNeighbors.get(toId).add(fromId);
    }

    @Override
    public void removeWay(Way way) {
        ways.remove(way.getId());
        for (int i = 0; i < way.getNumberOfNodes() - 1; ++i) {
            final Long fromId = way.getNodeId(i);
            final Long toId = way.getNodeId(i + 1);
            removeNeighbor(fromId, toId);
        }
    }

    private void removeNeighbor(Long fromId, Long toId) {
        outNeighbors.get(fromId).remove(toId);

        if (outNeighbors.get(fromId).isEmpty()) {
            outNeighbors.remove(fromId);
        }
    }

    @Override
    public void writeRelation(Relation relation) {
        relations.put(relation.getId(), relation);
    }

    @Override
    public void removeRelation(Relation relation) {
        relations.remove(relation.getId());
    }

    @Override
    public Node getNodeById(long id) {
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
        final List<Node> results = new ArrayList<>();
        for (Node node : nodes.values()) {
            final Map<String, String> tags = OsmModelUtil.getTagsAsMap(node);
            if (outgoingNeighbors(node).size() > 0
                    && tags.containsKey(key)
                    && tags.get(key).contains(value)) {
                results.add(node);
            }
        }
        return results;
    }

    @Override
    public Collection<Way> queryEdges(String key, String value) {
        final List<Way> results = new ArrayList<>();
        for (Way way : ways.values()) {
            final Map<String, String> tags = OsmModelUtil.getTagsAsMap(way);
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
        final List<Relation> results = new ArrayList<>();
        for (Relation relation : relations.values()) {
            final Map<String, String> tags = OsmModelUtil.getTagsAsMap(relation);
            if (tags.containsKey(key)) {
                if (tags.get(key).contains(value)) {
                    results.add(relation);
                }
            }
        }
        return results;
    }

    private Set<Node> getNeighbors(Node node, Map<Long, Set<Long>> container) {
        final Set<Node> result = new HashSet<>();
        if (container.containsKey(node.getId())) {
            final Set<Long> nodeNeighbors = container.get(node.getId());
            nodeNeighbors.forEach(id -> result.add(nodes.get(id)));
        }
        return result;
    }

    @Override
    public Set<Node> incomingNeighbors(Node node) {
        return getNeighbors(node, inNeighbors);
    }

    @Override
    public Set<Node> outgoingNeighbors(Node node) {
        return getNeighbors(node, outNeighbors);
    }

    @Override
    public Collection<Node> getAllNodes() {
        return nodes.values();
    }

    @Override
    public Collection<Way> getAllWays() {
        return ways.values();
    }
}
