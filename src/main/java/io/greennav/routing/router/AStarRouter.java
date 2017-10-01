package io.greennav.routing.router;

import de.topobyte.osm4j.core.model.impl.Node;
import io.greennav.persistence.Persistence;
import io.greennav.routing.roadgraph.iface.NodeWeightFunction;
import io.greennav.routing.roadgraph.impl.RoadEdge;
import io.greennav.routing.utils.Pair;
import org.jgrapht.alg.interfaces.AStarAdmissibleHeuristic;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm;
import org.jgrapht.alg.shortestpath.AStarShortestPath;
import java.util.Collection;

public class AStarRouter extends Router {
    private final AStarAdmissibleHeuristic<Node> heuristic;

    public AStarRouter(Persistence persistence, NodeWeightFunction weightFunction) {
        super(persistence, weightFunction);
        heuristic = weightFunction::apply;
    }

    public AStarRouter(Collection<Node> nodes, Collection<Pair<Node, Node>> edges,
                       NodeWeightFunction weightFunction) {
        super(nodes, edges, weightFunction);
        heuristic = weightFunction::apply;
    }

    @Override
    ShortestPathAlgorithm<Node, ? extends RoadEdge> getShortestPathAlgorithm() {
        return new AStarShortestPath<>(this.graph, this.heuristic);
    }
}
