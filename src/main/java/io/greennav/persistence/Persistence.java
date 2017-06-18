package io.greennav.persistence;

import de.topobyte.osm4j.core.model.impl.Node;
import de.topobyte.osm4j.core.model.impl.Relation;
import de.topobyte.osm4j.core.model.impl.Way;
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

    Set<Node> getNeighbors(Node node);
}
