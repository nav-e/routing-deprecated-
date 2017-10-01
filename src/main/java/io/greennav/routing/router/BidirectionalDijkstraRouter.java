package io.greennav.routing.router;

import de.topobyte.osm4j.core.model.impl.Node;
import io.greennav.persistence.Persistence;
import io.greennav.routing.roadgraph.iface.NodeWeightFunction;
import io.greennav.routing.roadgraph.impl.RoadEdge;
import io.greennav.routing.utils.Pair;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm;
import org.jgrapht.alg.shortestpath.BidirectionalDijkstraShortestPath;

import java.util.Collection;
import java.util.Map;

public class BidirectionalDijkstraRouter extends Router {
    public BidirectionalDijkstraRouter(Persistence persistence, NodeWeightFunction weightFunction) {
        super(persistence, weightFunction);
    }

    public BidirectionalDijkstraRouter(Collection<Node> nodes, Collection<Pair<Node, Node>> edges,
                                       NodeWeightFunction weightFunction) {
        super(nodes, edges, weightFunction);
    }

    public BidirectionalDijkstraRouter(Collection<Node> nodes, Map<Pair<Node, Node>, ? extends Number> weightMap) {
        super(nodes, weightMap);
    }

    @Override
    ShortestPathAlgorithm<Node, ? extends RoadEdge> getShortestPathAlgorithm() {
        return new BidirectionalDijkstraShortestPath<>(this.graph);
    }
}
