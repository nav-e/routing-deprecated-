package io.greennav.routing.router;

import de.topobyte.osm4j.core.model.impl.Node;
import de.topobyte.osm4j.core.model.impl.Way;
import gnu.trove.list.TLongList;
import gnu.trove.list.array.TLongArrayList;
import io.greennav.persistence.InMemoryPersistence;
import io.greennav.persistence.Persistence;
import io.greennav.routing.roadgraph.iface.NodeWeightFunction;
import io.greennav.routing.roadgraph.impl.RoadEdgeCH;
import io.greennav.routing.roadgraph.impl.RoadGraph;
import io.greennav.routing.roadgraph.impl.RoadEdge;
import io.greennav.routing.roadgraph.impl.RoadGraphCH;
import javafx.util.Pair;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public abstract class Router {
    final RoadGraph<? extends RoadEdge> graph;
    ShortestPathAlgorithm<Node, ? extends RoadEdge> shortestPathAlgorithm;

    <E extends RoadEdge> Router(Persistence persistence, NodeWeightFunction weightFunction, Class<E> edgeClass) {
        this.graph = edgeClass == RoadEdgeCH.class ?
                new RoadGraphCH(persistence, weightFunction) : new RoadGraph<>(persistence, weightFunction, edgeClass);
    }

    Router(Persistence persistence, NodeWeightFunction weightFunction) {
        this(persistence, weightFunction, RoadEdge.class);
    }

    <E extends RoadEdge> Router(Collection<Node> nodes, Collection<Pair<Node, Node>> edges,
                                NodeWeightFunction weightFunction, Class<E> edgeClass) {
        final Persistence persistence = new InMemoryPersistence();
        nodes.forEach(persistence::writeNode);
        final AtomicLong idCounter = new AtomicLong();
        edges.forEach(pair -> {
            final Node source = pair.getKey();
            final Node target = pair.getValue();
            final TLongList edgeIds = new TLongArrayList(new long[]{source.getId(), target.getId()});
            persistence.writeWay(new Way(idCounter.getAndAdd(1), edgeIds));
        });
        this.graph = edgeClass == RoadEdgeCH.class ?
                new RoadGraphCH(persistence, weightFunction) : new RoadGraph<>(persistence, weightFunction, edgeClass);
    }

    Router(Collection<Node> nodes, Collection<Pair<Node, Node>> edges, NodeWeightFunction weightFunction) {
        this(nodes, edges, weightFunction, RoadEdge.class);
    }

    public Route getShortestPath(Node start, Node finish) {
        if (shortestPathAlgorithm == null) {
            shortestPathAlgorithm = getShortestPathAlgorithm();
        }
        graph.addVertex(start);
        graph.addVertex(finish);
        final Instant begin = Instant.now();
        final List<Node> routeNodes = shortestPathAlgorithm.getPath(start, finish).getVertexList();
        final Instant end = Instant.now();
        final long durationInMillis = Duration.between(begin, end).toMillis();
        graph.reset();
        return new Route(routeNodes, durationInMillis);
    }

    public double getShortestPathWeight(Node start, Node finish) {
        if (shortestPathAlgorithm == null) {
            shortestPathAlgorithm = getShortestPathAlgorithm();
        }
        graph.addVertex(start);
        graph.addVertex(finish);
        final double weight = shortestPathAlgorithm.getPathWeight(start, finish);
        graph.reset();
        return weight;
    }

    abstract ShortestPathAlgorithm<Node, ? extends RoadEdge> getShortestPathAlgorithm();
}
