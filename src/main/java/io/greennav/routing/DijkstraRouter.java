package io.greennav.routing;

import io.greennav.map.MapEdge;
import io.greennav.map.MapNode;
import io.greennav.map.MapNodeWeightFunction;
import javafx.util.Pair;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;

public class DijkstraRouter extends Router {
    public DijkstraRouter(Iterable<MapNode> nodes, Iterable<Pair<MapNode, MapNode>> edges,
                          MapNodeWeightFunction weightFunction) {
        super(nodes, edges, weightFunction);
    }

    @Override
    ShortestPathAlgorithm<MapNode, MapEdge> getShortestPath(MapNode source, MapNode target) {
        return new DijkstraShortestPath<>(this.graph);
    }
}
