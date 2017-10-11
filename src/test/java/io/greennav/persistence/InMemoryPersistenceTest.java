package io.greennav.persistence;

import io.greennav.osm.Node;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class InMemoryPersistenceTest {
    @Test
    public void getNodeById() throws Exception {
        Node n = new Node(42, 0, 0);
        InMemoryPersistence p = new InMemoryPersistence();

        assertEquals(p.getAllNodes().size(), 0);
        p.writeNode(n);
        assertEquals(p.getAllNodes().size(), 1);
        assertEquals(n, p.getNodeById(42));
    }

    @Test
    public void queryNodes() throws Exception {
        Map<String, String> t = new HashMap<String, String>();
        t.put("hello", "world");
        InMemoryPersistence p = new InMemoryPersistence();
        Node n = new Node(42, 0, 0, t);
        p.writeNode(n);

        assertEquals(Collections.singletonList(n), p.queryNodes("hello", "w"));
    }

}