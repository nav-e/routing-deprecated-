package io.greennav.persistence;

import io.greennav.osm.Node;
import io.greennav.osm.Relation;
import io.greennav.osm.Way;

import java.util.Collection;
import java.util.Set;

public interface Persistence {
    void writeNode(Node node);

    void removeNode(Node node);

    void writeWay(Way way);

    void removeWay(Way way);

    void writeRelation(Relation relation);

    void removeRelation(Relation relation);

    Node getNodeById(long id);

    Way getWayById(long id);

    Relation getRelationById(long id);

    Collection<Node> queryNodes(String key, String value);

    Collection<Way> queryEdges(String key, String value);

    Collection<Relation> queryRelations(String key, String value);

    Set<Node> incomingNeighbors(Node node);

    Set<Node> outgoingNeighbors(Node node);

    Collection<Node> getAllNodes();

    Collection<Way> getAllWays();
}
