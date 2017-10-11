package io.greennav.routing.shortestpath;

import io.greennav.osm.Node;
import io.greennav.routing.roadgraph.impl.RoadEdgeCH;
import io.greennav.routing.roadgraph.impl.RoadGraphCH;
import org.jgrapht.GraphPath;
import org.jgrapht.Graphs;
import org.jgrapht.graph.GraphWalk;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class ContractionHierarchiesShortestPath extends BaseShortestPathAlgorithm<Node, RoadEdgeCH> {
    private final BestTouchingDescriptor bestTouchingDescriptor;

    public ContractionHierarchiesShortestPath(RoadGraphCH graph) {
        super(graph);
        bestTouchingDescriptor = new BestTouchingDescriptor();
    }

    private RoadGraphCH getRoadGraphCH() {
        return (RoadGraphCH) graph;
    }

    private void relaxNeighbors(SearchManager searchManager, DistanceEstimate nodeEstimate) {
        Node node = nodeEstimate.node;
        RoadGraphCH graphCH = getRoadGraphCH();
        for (RoadEdgeCH edge : searchManager.neighborsOf(node)) {
            Node neighbor = Graphs.getOppositeVertex(graph, edge, node);
            if (graphCH.nodeDescriptors.get(node).rank < graphCH.nodeDescriptors.get(neighbor).rank) {
                searchManager.update(node, neighbor, graphCH.getEdgeWeight(edge));
            }
        }
    }

    private void tryTouchingUpdate(SearchManager searchManager, SearchManager otherSearchManager, Node node) {
        if (searchManager.visited(node) && otherSearchManager.visited(node)) {
            final double estimate = searchManager.distanceEstimate(node).weight +
                    otherSearchManager.distanceEstimate(node).weight;
            if (estimate < bestTouchingDescriptor.estimateWeight) {
                bestTouchingDescriptor.estimateWeight = estimate;
                bestTouchingDescriptor.node = node;
            }
        }
    }

    private void processEdge(Node sourceNode, Node targetNode, LinkedList<RoadEdgeCH> roadEdges, boolean forward) {
        final RoadGraphCH graphCH = getRoadGraphCH();
        if (forward) {
            graphCH.outgoingEdgesOf(sourceNode);
        } else {
            graphCH.incomingEdgesOf(sourceNode);
        }
        final RoadEdgeCH edge =
                forward ? graphCH.getEdge(sourceNode, targetNode) : graphCH.getEdge(targetNode, sourceNode);
        final Optional<Node> intermediateNodeOptional = graphCH.getIntermediateNode(edge);
        if (intermediateNodeOptional.isPresent()) {
            final Node intermediateNode = intermediateNodeOptional.get();
            processEdge(intermediateNode, targetNode, roadEdges, forward);
            processEdge(sourceNode, intermediateNode, roadEdges, forward);
        } else if (forward) {
            roadEdges.addFirst(edge);
        } else {
            roadEdges.addLast(edge);
        }
    }

    private void restoreEdgeSequenceInOneDirection(Node startNode, SearchManager searchManager,
                                                   LinkedList<RoadEdgeCH> roadEdges, boolean forward) {
        Node target = startNode;
        while (searchManager.distanceEstimate(target).predecessor != target) {
            Node source = searchManager.distanceEstimate(target).predecessor;
            processEdge(source, target, roadEdges, forward);
            target = source;
        }
    }

    private List<RoadEdgeCH> restoreEdgeSequence(Node touchingNode, SearchManager forwardSearchManager,
                                                 SearchManager reverseSearchManager) {
        final LinkedList<RoadEdgeCH> roadEdges = new LinkedList<>();
        restoreEdgeSequenceInOneDirection(touchingNode, forwardSearchManager, roadEdges, true);
        restoreEdgeSequenceInOneDirection(touchingNode, reverseSearchManager, roadEdges, false);
        return roadEdges;
    }

    @Override
    public GraphPath<Node, RoadEdgeCH> getPath(Node source, Node target) {
        RoadGraphCH graphCH = getRoadGraphCH();
        SearchManager searchManager = graphCH.forwardSearchManager;
        SearchManager otherSearchManager = graphCH.reverseSearchManager;
        searchManager.initialize(source);
        otherSearchManager.initialize(target);
        bestTouchingDescriptor.reset();
        while (!searchManager.empty() || !otherSearchManager.empty()) {
            if (searchManager.empty()) {
                SearchManager tmp = searchManager;
                searchManager = otherSearchManager;
                otherSearchManager = tmp;
            }
            DistanceEstimate minUnvisited = searchManager.pop();
            Node node = minUnvisited.node;
            if (searchManager.distanceEstimate(node).weight <= bestTouchingDescriptor.estimateWeight) {
                relaxNeighbors(searchManager, minUnvisited);
                tryTouchingUpdate(searchManager, otherSearchManager, node);
            } else {
                searchManager.stop();
            }
            SearchManager tmp = searchManager;
            searchManager = otherSearchManager;
            otherSearchManager = tmp;
        }
        searchManager.reset();
        otherSearchManager.reset();
        if (Double.isFinite(bestTouchingDescriptor.estimateWeight)) {
            List<RoadEdgeCH> roadEdges = restoreEdgeSequence(bestTouchingDescriptor.node, graphCH.forwardSearchManager,
                    graphCH.reverseSearchManager);
            return new GraphWalk<>(graph, source, target, roadEdges, bestTouchingDescriptor.estimateWeight);
        } else {
            return createEmptyPath(source, target);
        }
    }

    private class BestTouchingDescriptor {
        double estimateWeight;
        Node node;

        void reset() {
            estimateWeight = Double.POSITIVE_INFINITY;
            node = null;
        }
    }
}
