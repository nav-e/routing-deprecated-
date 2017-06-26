package io.greennav.routing;

import de.topobyte.osm4j.core.model.impl.Node;
import de.topobyte.osm4j.core.model.impl.Way;
import gnu.trove.list.TLongList;
import gnu.trove.list.array.TLongArrayList;
import io.greennav.persistence.InMemoryPersistence;
import io.greennav.persistence.Persistence;
import javafx.util.Pair;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

abstract class Router {
    protected final RoadGraph graph;

    Router(Persistence persistence, NodeWeightFunction weightFunction) {
        this.graph = new RoadGraph(persistence, weightFunction);
    }

    Router(Collection<Node> nodes, Collection<Pair<Node, Node>> edges, NodeWeightFunction weightFunction) {
        final Persistence persistence = new InMemoryPersistence();
        nodes.forEach(persistence::writeNode);
        final AtomicLong idCounter = new AtomicLong();
        edges.forEach(pair -> {
            final Node source = pair.getKey();
            final Node target = pair.getValue();
            final TLongList edgeIds = new TLongArrayList(new long[]{source.getId(), target.getId()});
            persistence.writeWay(new Way(idCounter.getAndAdd(1), edgeIds));
        });
        this.graph = new RoadGraph(persistence, weightFunction);
    }

    Route getShortestPath(Node start, Node finish) {
        graph.addVertex(start);
        graph.addVertex(finish);
        final Instant begin = Instant.now();
        final List<Node> routeNodes = getShortestPathAlgorithm(start, finish).getPath(start, finish).getVertexList();
        final Instant end = Instant.now();
        final long durationInMillis = Duration.between(begin, end).toMillis();
        new HashSet<>(graph.edgeSet()).forEach(graph::removeEdge);
        new HashSet<>(graph.vertexSet()).forEach(graph::removeVertex);
        graph.resetCache();
        return new Route(routeNodes, durationInMillis);
    }

    abstract ShortestPathAlgorithm<Node, RoadEdge> getShortestPathAlgorithm(Node source, Node target);
}
