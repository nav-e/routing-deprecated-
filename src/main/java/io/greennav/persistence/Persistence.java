package io.greennav.persistence;

import de.topobyte.osm4j.core.model.impl.Node;
import de.topobyte.osm4j.core.model.impl.Relation;
import de.topobyte.osm4j.core.model.impl.Way;
import java.util.Collection;
import java.util.Set;

public interface Persistence {
    public void writeNode(Node node);

    public void writeWay(Way way);

    public void writeRelation(Relation relation);

    public Node getNodeById(long id) throws Exception;

    public Way getWayById(long id);

    public Relation getRelationById(long id);

    public Collection<Node> queryNodes(String key, String value);

    public Collection<Way> queryEdges(String key, String value);

    public Collection<Relation> queryRelations(String key, String value);

    public Set<Node> getNeighbors(Node node);
}
