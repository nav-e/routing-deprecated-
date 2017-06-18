package io.greennav.routing;

import de.topobyte.osm4j.core.model.impl.Node;
import javafx.util.Pair;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import java.util.Collection;

class DijkstraRouter extends Router {
    DijkstraRouter(Collection<Node> nodes, Collection<Pair<Node, Node>> edges,
                          MapNodeWeightFunction weightFunction) {
        super(nodes, edges, weightFunction);
    }

    @Override
    ShortestPathAlgorithm<Node, RoadEdge> getShortestPathAlgorithm(Node source, Node target) {
        return new DijkstraShortestPath<>(this.graph);
    }
}
