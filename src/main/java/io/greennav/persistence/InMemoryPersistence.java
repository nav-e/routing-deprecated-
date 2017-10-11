package io.greennav.persistence;

import io.greennav.osm.Node;
import io.greennav.osm.Relation;
import io.greennav.osm.Way;

import java.util.*;

public class InMemoryPersistence implements Persistence {
    private final Map<Long, Node> nodes = new HashMap<>();
    private final Map<Long, Way> ways = new HashMap<>();
    private final Map<Long, Relation> relations = new HashMap<>();
    private final Map<Long, Set<Long>> inNeighbors = new HashMap<>();
    private final Map<Long, Set<Long>> outNeighbors = new HashMap<>();

    public void writeNode(Node node) {
        nodes.put(node.getId(), node);
    }

    public void removeNode(Node node) {
        nodes.remove(node.getId());
    }

    public void writeWay(Way way) {
        ways.put(way.getId(), way);
        for (int i = 0; i < way.getNodes().size() - 1; ++i) {
            final Long fromId = way.getNodes().get(i);
            final Long toId = way.getNodes().get(i + 1);
            putOrCreateNeighbor(fromId, toId);
        }
    }

    private void putOrCreateNeighbor(Long fromId, Long toId) {
        outNeighbors.putIfAbsent(fromId, new HashSet<>());
        inNeighbors.putIfAbsent(toId, new HashSet<>());
        outNeighbors.get(fromId).add(toId);
        inNeighbors.get(toId).add(fromId);
    }

    public void removeWay(Way way) {
        ways.remove(way.getId());
        for (int i = 0; i < way.getNodes().size() - 1; ++i) {
            final Long fromId = way.getNodes().get(i);
            final Long toId = way.getNodes().get(i + 1);
            removeNeighbor(fromId, toId);
        }
    }

    private void removeNeighbor(Long fromId, Long toId) {
        outNeighbors.get(fromId).remove(toId);

        if (outNeighbors.get(fromId).isEmpty()) {
            outNeighbors.remove(fromId);
        }
    }

    public void writeRelation(Relation relation) {
        relations.put(relation.getId(), relation);
    }

    public void removeRelation(Relation relation) {
        relations.remove(relation.getId());
    }

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
        int maxNodes = 10;

        for (Node node : nodes.values()) {
            if (outgoingNeighbors(node).size() > 0
                    && node.getTags().containsKey(key)
                    && node.getTags().get(key).contains(value)) {
                results.add(node);
                maxNodes--;
            }

            if (maxNodes == 0)
                break;
        }
        return results;
    }

    @Override
    public Collection<Way> queryEdges(String key, String value) {
        final List<Way> results = new ArrayList<>();
        for (Way way : ways.values()) {
            if (way.getTags().containsKey(key)) {
                if (way.getTags().get(key).contains(value)) {
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
            if (relation.getTags().containsKey(key)) {
                if (relation.getTags().get(key).contains(value)) {
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

    public Set<Node> incomingNeighbors(Node node) {
        return getNeighbors(node, inNeighbors);
    }

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
