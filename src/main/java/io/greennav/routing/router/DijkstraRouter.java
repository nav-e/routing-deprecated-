package io.greennav.routing.router;

import de.topobyte.osm4j.core.model.impl.Node;
import io.greennav.persistence.Persistence;
import io.greennav.routing.roadgraph.iface.NodeWeightFunction;
import io.greennav.routing.roadgraph.impl.RoadEdge;
import javafx.util.Pair;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import java.util.Collection;

public class DijkstraRouter extends Router {
    public DijkstraRouter(Persistence persistence, NodeWeightFunction weightFunction) {
        super(persistence, weightFunction);
    }

    public DijkstraRouter(Collection<Node> nodes, Collection<Pair<Node, Node>> edges,
                          NodeWeightFunction weightFunction) {
        super(nodes, edges, weightFunction);
    }

    @Override
    ShortestPathAlgorithm<Node, ? extends RoadEdge> getShortestPathAlgorithm() {
        return new DijkstraShortestPath<>(this.graph);
    }
}
