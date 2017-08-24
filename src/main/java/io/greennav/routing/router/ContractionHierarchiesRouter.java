package io.greennav.routing.router;

import de.topobyte.osm4j.core.model.impl.Node;
import io.greennav.persistence.Persistence;
import io.greennav.routing.roadgraph.iface.NodeWeightFunction;
import io.greennav.routing.roadgraph.impl.RoadEdge;
import io.greennav.routing.roadgraph.impl.RoadEdgeCH;
import io.greennav.routing.roadgraph.impl.RoadGraphCH;
import io.greennav.routing.shortestpath.ContractionHierarchiesShortestPath;
import javafx.util.Pair;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm;

import java.util.Collection;

public class ContractionHierarchiesRouter extends Router {
    public ContractionHierarchiesRouter(Persistence persistence, NodeWeightFunction weightFunction) {
        super(persistence, weightFunction, RoadEdgeCH.class);
        getRoadGraphCH().PreprocessGraph();
    }

    public ContractionHierarchiesRouter(Collection<Node> nodes, Collection<Pair<Node, Node>> edges,
                                        NodeWeightFunction weightFunction) {
        super(nodes, edges, weightFunction, RoadEdgeCH.class);
        getRoadGraphCH().PreprocessGraph();
    }

    @Override
    ShortestPathAlgorithm<Node, ? extends RoadEdge> getShortestPathAlgorithm() {
        return new ContractionHierarchiesShortestPath(getRoadGraphCH());
    }

    private RoadGraphCH getRoadGraphCH() {
        return (RoadGraphCH)graph;
    }
}
