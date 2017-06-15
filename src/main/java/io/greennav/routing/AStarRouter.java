package io.greennav.routing;

import de.topobyte.osm4j.core.model.impl.Node;
import javafx.util.Pair;
import org.jgrapht.alg.interfaces.AStarAdmissibleHeuristic;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm;
import org.jgrapht.alg.shortestpath.AStarShortestPath;

public class AStarRouter extends Router {
    private final AStarAdmissibleHeuristic<Node> heuristic;
    public AStarRouter(Iterable<Node> nodes, Iterable<Pair<Node, Node>> edges,
                       MapNodeWeightFunction weightFunction) {
        super(nodes, edges, weightFunction);
        heuristic = weightFunction::apply;
    }

    @Override
    ShortestPathAlgorithm<Node, MapEdge> getShortestPathAlgorithm(Node source, Node target) {
        return new AStarShortestPath<>(this.graph, this.heuristic);
    }
}
