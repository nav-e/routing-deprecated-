package io.greennav.persistence;

import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.core.model.iface.OsmRelationMember;
import de.topobyte.osm4j.core.model.impl.Node;
import de.topobyte.osm4j.core.model.impl.Relation;
import de.topobyte.osm4j.core.model.impl.RelationMember;
import de.topobyte.osm4j.core.model.impl.Way;
import gnu.trove.list.TLongList;
import gnu.trove.list.linked.TLongLinkedList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest
public class PersistenceTests {
    private Persistence db = new InMemoryPersistence();

    @Test
    public void testInMemoryPersistenceNodeRWOperations() {
        final Map<Long, Node> nodes = new HashMap<>();
        nodes.put(0L, new Node(0L, 0., 0.));
        db.writeNode(nodes.get(0L));
        nodes.values().forEach(db::writeNode);

        assertEquals(nodes.get(0L), db.getNodeById(0L));

        db.removeNode(nodes.get(0L));

        assertEquals(null, db.getNodeById(0L));
    }

    @Test
    public void testInMemoryPersistenceWayRWOperations() {
        final Map<Long, Node> nodes = new HashMap<>();
        nodes.put(0L, new Node(0L, 0., 0.));
        nodes.put(1L, new Node(1L, 0., 0.5));
        nodes.put(2L, new Node(2L, 0.5, 0.));
        nodes.values().forEach(db::writeNode);

        final Map<Long, Way> ways = new HashMap<>();
        final TLongList way = new TLongLinkedList();
        way.add(new long[]{0, 1, 2});
        ways.put(0L, new Way(0L, way));
        db.writeWay(ways.get(0L));

        assertEquals(ways.get(0L), db.getWayById(0L));

        db.removeWay(ways.get(0L));

        assertEquals(null, db.getWayById(0L));
    }

    @Test
    public void testInMemoryPersistenceRelationRWOperations() {
        final Map<Long, OsmRelationMember> members = new HashMap<>();
        members.put(0L, new RelationMember(0L, EntityType.Node, ""));
        members.put(1L, new RelationMember(1L, EntityType.Node, ""));
        final Relation relation = new Relation(0L, new LinkedList<>(members.values()));
        db.writeRelation(relation);

        assertEquals(relation, db.getRelationById(0L));

        db.removeRelation(relation);

        assertEquals(null, db.getRelationById(0L));
    }

    @Test
    public void testInMemoryPersistenceNeighboursGetter() {
        final Map<Long, Node> nodes = new HashMap<>();
        nodes.put(0L, new Node(0L, 0., 0.));
        nodes.put(1L, new Node(1L, 0., 0.5));
        nodes.put(2L, new Node(2L, 0.5, 0.));
        nodes.put(3L, new Node(3L, 0.5, 0.5));
        nodes.values().forEach(db::writeNode);

        final Map<Long, Way> ways = new HashMap<>();
        final TLongList wayOne = new TLongLinkedList();
        wayOne.add(new long[]{0, 1, 2});
        ways.put(0L, new Way(0L, wayOne));
        final TLongList wayTwo = new TLongLinkedList();
        wayTwo.add(new long[]{1, 3});
        ways.put(1L, new Way(1L, wayTwo));
        ways.values().forEach(db::writeWay);

        assertEquals(
                Arrays.stream(new Node[]{nodes.get(0L), nodes.get(2L), nodes.get(3L)}).collect(Collectors.toSet()),
                db.getNeighbors(nodes.get(1L))
        );
        assertEquals(
                Arrays.stream(new Node[]{nodes.get(1L)}).collect(Collectors.toSet()),
                db.getNeighbors(nodes.get(2L))
        );
    }
}
