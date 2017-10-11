package io.greennav.routing.router;

import io.greennav.osm.Node;
import io.greennav.persistence.Persistence;
import io.greennav.routing.roadgraph.iface.NodeWeightFunction;
import io.greennav.routing.roadgraph.impl.RoadEdge;
import io.greennav.routing.utils.Pair;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;

import java.util.Collection;
import java.util.Map;

public class DijkstraRouter extends Router {
    public DijkstraRouter(Persistence persistence, NodeWeightFunction weightFunction) {
        super(persistence, weightFunction);
    }

    public DijkstraRouter(Collection<Node> nodes, Collection<Pair<Node, Node>> edges,
                          NodeWeightFunction weightFunction) {
        super(nodes, edges, weightFunction);
    }

    public DijkstraRouter(Collection<Node> nodes, Map<Pair<Node, Node>, ? extends Number> weightMap) {
        super(nodes, weightMap);
    }

    @Override
    ShortestPathAlgorithm<Node, ? extends RoadEdge> getShortestPathAlgorithm() {
        return new DijkstraShortestPath<>(this.graph);
    }
}
