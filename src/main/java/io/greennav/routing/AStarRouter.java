package io.greennav.routing;

import de.topobyte.osm4j.core.model.impl.Node;
import javafx.util.Pair;
import org.jgrapht.alg.interfaces.AStarAdmissibleHeuristic;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm;
import org.jgrapht.alg.shortestpath.AStarShortestPath;
import java.util.Collection;

class AStarRouter extends Router {
    private final AStarAdmissibleHeuristic<Node> heuristic;
    AStarRouter(Collection<Node> nodes, Collection<Pair<Node, Node>> edges,
                       MapNodeWeightFunction weightFunction) {
        super(nodes, edges, weightFunction);
        heuristic = weightFunction::apply;
    }

    @Override
    ShortestPathAlgorithm<Node, RoadEdge> getShortestPathAlgorithm(Node source, Node target) {
        return new AStarShortestPath<>(this.graph, this.heuristic);
    }
}
