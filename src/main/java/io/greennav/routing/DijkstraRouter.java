package io.greennav.routing;

import de.topobyte.osm4j.core.model.impl.Node;
import javafx.util.Pair;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;

public class DijkstraRouter extends Router {
    public DijkstraRouter(Iterable<Node> nodes, Iterable<Pair<Node, Node>> edges,
                          MapNodeWeightFunction weightFunction) {
        super(nodes, edges, weightFunction);
    }

    @Override
    ShortestPathAlgorithm<Node, MapEdge> getShortestPathAlgorithm(Node source, Node target) {
        return new DijkstraShortestPath<>(this.graph);
    }
}
