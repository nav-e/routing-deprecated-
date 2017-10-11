package io.greennav.routing.router;

import io.greennav.osm.Node;
import io.greennav.osm.Way;
import io.greennav.persistence.InMemoryPersistence;
import io.greennav.persistence.Persistence;
import io.greennav.routing.roadgraph.iface.NodeWeightFunction;
import io.greennav.routing.roadgraph.impl.RoadEdge;
import io.greennav.routing.roadgraph.impl.RoadEdgeCH;
import io.greennav.routing.roadgraph.impl.RoadGraph;
import io.greennav.routing.roadgraph.impl.RoadGraphCH;
import io.greennav.routing.utils.Pair;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiFunction;

public abstract class Router {
    final RoadGraph<? extends RoadEdge> graph;
    private ShortestPathAlgorithm<Node, ? extends RoadEdge> shortestPathAlgorithm;

    <E extends RoadEdge> Router(Persistence persistence, NodeWeightFunction weightFunction, Class<E> edgeClass) {
        this.graph = graphInitialize(persistence, weightFunction, edgeClass);
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

            persistence.writeWay(new Way(
                    idCounter.getAndAdd(1),
                    Arrays.asList(source.getId(), target.getId())));
        });

        this.graph = graphInitialize(persistence, weightFunction, edgeClass);
    }

    Router(Collection<Node> nodes, Collection<Pair<Node, Node>> edges, NodeWeightFunction weightFunction) {
        this(nodes, edges, weightFunction, RoadEdge.class);
    }

    <E extends RoadEdge> Router(Collection<Node> nodes, Map<Pair<Node, Node>, ? extends Number> weightMap,
                                Class<E> edgeClass) {
        this(nodes, weightMap.keySet(), (lhs, rhs) -> weightMap.get(new Pair<>(lhs, rhs)).doubleValue(), edgeClass);
    }

    Router(Collection<Node> nodes, Map<Pair<Node, Node>, ? extends Number> weightMap) {
        this(nodes, weightMap, RoadEdge.class);
    }

    private <E extends RoadEdge> RoadGraph<? extends RoadEdge> graphInitialize(
            Persistence persistence, NodeWeightFunction weightFunction, Class<E> edgeClass) {
        return edgeClass == RoadEdgeCH.class ?
                new RoadGraphCH(persistence, weightFunction) : new RoadGraph<>(persistence, weightFunction, edgeClass);
    }

    private <T> T getShortestPathInfo(Node start, Node finish, BiFunction<Node, Node, T> function) {
        if (shortestPathAlgorithm == null) {
            shortestPathAlgorithm = getShortestPathAlgorithm();
        }
        graph.addVertex(start);
        graph.addVertex(finish);
        final T result = function.apply(start, finish);
        graph.reset();
        return result;
    }

    public Route getShortestPath(Node start, Node finish) {
        return getShortestPathInfo(start, finish, (lhs, rhs) -> {
            final Instant begin = Instant.now();
            final List<Node> routeNodes = shortestPathAlgorithm.getPath(lhs, rhs).getVertexList();
            final Instant end = Instant.now();
            final long durationInMillis = Duration.between(begin, end).toMillis();
            return new Route(routeNodes, durationInMillis);
        });
    }

    public double getShortestPathWeight(Node start, Node finish) {
        return getShortestPathInfo(start, finish, (lhs, rhs) -> shortestPathAlgorithm.getPathWeight(lhs, rhs));
    }

    abstract ShortestPathAlgorithm<Node, ? extends RoadEdge> getShortestPathAlgorithm();
}
