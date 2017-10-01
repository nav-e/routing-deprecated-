package io.greennav.routing.router;

import de.topobyte.osm4j.core.model.impl.Node;
import io.greennav.persistence.Persistence;
import io.greennav.routing.roadgraph.iface.NodeWeightFunction;
import io.greennav.routing.roadgraph.impl.RoadEdge;
import io.greennav.routing.roadgraph.impl.RoadEdgeCH;
import io.greennav.routing.roadgraph.impl.RoadGraphCH;
import io.greennav.routing.shortestpath.ContractionHierarchiesShortestPath;
import io.greennav.routing.utils.Pair;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm;

import java.util.Collection;
import java.util.Map;

public class ContractionHierarchiesRouter extends Router {
    public ContractionHierarchiesRouter(Persistence persistence, NodeWeightFunction weightFunction) {
        super(persistence, weightFunction, RoadEdgeCH.class);
        Preprocess();
    }

    public ContractionHierarchiesRouter(Collection<Node> nodes, Collection<Pair<Node, Node>> edges,
                                        NodeWeightFunction weightFunction) {
        super(nodes, edges, weightFunction, RoadEdgeCH.class);
        Preprocess();
    }

    public ContractionHierarchiesRouter(Collection<Node> nodes, Map<Pair<Node, Node>, ? extends Number> weightMap) {
        super(nodes, weightMap, RoadEdgeCH.class);
        Preprocess();
    }

    private void Preprocess() {
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
