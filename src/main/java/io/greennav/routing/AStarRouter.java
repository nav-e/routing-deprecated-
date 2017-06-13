package io.greennav.routing;

import io.greennav.map.MapEdge;
import io.greennav.map.MapNode;
import io.greennav.map.MapNodeWeightFunction;
import javafx.util.Pair;
import org.jgrapht.alg.interfaces.AStarAdmissibleHeuristic;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm;
import org.jgrapht.alg.shortestpath.AStarShortestPath;

public class AStarRouter extends Router {
    private final AStarAdmissibleHeuristic<MapNode> heuristic;
    public AStarRouter(Iterable<MapNode> nodes, Iterable<Pair<MapNode, MapNode>> edges,
                       MapNodeWeightFunction weightFunction) {
        super(nodes, edges, weightFunction);
        heuristic = weightFunction::apply;
    }

    @Override
    ShortestPathAlgorithm<MapNode, MapEdge> getShortestPath(MapNode source, MapNode target) {
        return new AStarShortestPath<>(this.graph, this.heuristic);
    }
}
