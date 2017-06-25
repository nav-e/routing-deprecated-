package io.greennav.routing;

import de.topobyte.osm4j.core.model.impl.Node;
import io.greennav.persistence.Persistence;
import javafx.util.Pair;
import org.jgrapht.alg.interfaces.AStarAdmissibleHeuristic;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm;
import org.jgrapht.alg.shortestpath.AStarShortestPath;
import java.util.Collection;

class AStarRouter extends Router {
    private final AStarAdmissibleHeuristic<Node> heuristic;

    AStarRouter(Persistence persistence, NodeWeightFunction weightFunction) {
        super(persistence, weightFunction);
        heuristic = weightFunction::apply;
    }

    AStarRouter(Collection<Node> nodes, Collection<Pair<Node, Node>> edges,
                NodeWeightFunction weightFunction) {
        super(nodes, edges, weightFunction);
        heuristic = weightFunction::apply;
    }

    @Override
    ShortestPathAlgorithm<Node, RoadEdge> getShortestPathAlgorithm(Node source, Node target) {
        return new AStarShortestPath<>(this.graph, this.heuristic);
    }
}
