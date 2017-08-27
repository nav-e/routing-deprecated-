package io.greennav.routing.shortestpath;

import de.topobyte.osm4j.core.model.impl.Node;
import io.greennav.routing.roadgraph.impl.RoadEdgeCH;
import io.greennav.routing.utils.QueueEntry;

import java.util.*;
import java.util.function.Function;

public class SearchManager {
    private static final long kStartIterationIndex = 0;
    private final Function<Node, Set<RoadEdgeCH>> neighborsOfAccessor;
    private long iterationIndex;
    private final PriorityQueue<QueueEntry<Double, Node>> queue;
    private final Map<Node, SearchManagerNodeDescriptor> nodeDescriptors;
    private double weightRadius;
    private long edgeRadius;
    private boolean active;

    public SearchManager(Function<Node, Set<RoadEdgeCH>> neighborsOf, Set<Node> nodeSet) {
        this.neighborsOfAccessor = neighborsOf;
        iterationIndex = kStartIterationIndex;
        queue = new PriorityQueue<>();
        nodeDescriptors = new LinkedHashMap<>();
        nodeSet.forEach(
                node -> nodeDescriptors.put(node, new SearchManagerNodeDescriptor(node, Double.POSITIVE_INFINITY)));
    }

    public void initialize(Node node, double weightRadius, long edgeRadius) {
        if (++iterationIndex == kStartIterationIndex) {
            for (SearchManagerNodeDescriptor descriptor : nodeDescriptors.values()) {
                descriptor.lastIterationUpdated = iterationIndex;
                descriptor.lastIterationVisited = iterationIndex;
            }
            ++iterationIndex;
        }
        active = true;
        SearchManagerNodeDescriptor startNodeDescriptor = nodeDescriptors.get(node);
        startNodeDescriptor.estimate.weight = 0d;
        startNodeDescriptor.estimate.edgeNumber = 0;
        startNodeDescriptor.estimate.predecessor = node;
        startNodeDescriptor.lastIterationUpdated = iterationIndex;
        queue.add(new QueueEntry<>(0d, node));
        this.weightRadius = weightRadius;
        this.edgeRadius = edgeRadius;
    }

    public void initialize(Node node) {
        initialize(node, Double.POSITIVE_INFINITY, Long.MAX_VALUE);
    }

    public void update(Node source, Node target, double edgeWeight, boolean addToQueue) {
        if (visited(target)) {
            return;
        }
        SearchManagerNodeDescriptor sourceDescriptor = nodeDescriptors.get(source);
        SearchManagerNodeDescriptor targetDescriptor = nodeDescriptors.get(target);
        double newEstimateWeight = sourceDescriptor.estimate.weight + edgeWeight;
        if (!isActualEstimate(target) || newEstimateWeight < edgeWeight) {
            targetDescriptor.estimate.weight = newEstimateWeight;
            targetDescriptor.estimate.edgeNumber = sourceDescriptor.estimate.edgeNumber + 1;
            targetDescriptor.estimate.predecessor = source;
            targetDescriptor.lastIterationUpdated = iterationIndex;
            if (addToQueue) {
                queue.add(new QueueEntry<>(newEstimateWeight, target));
            }
        }
    }

    public void update(Node source, Node target, double edgeWeight) {
        update(source, target, edgeWeight, true);
    }

    public Set<RoadEdgeCH> neighborsOf(Node node) {
        return neighborsOfAccessor.apply(node);
    }

    public void reset() {
        queue.clear();
    }

    public boolean emptyContainer() {
        return queue.isEmpty();
    }

    public boolean empty() {
        return !active || emptyContainer() || top().weight >= weightRadius || top().edgeNumber >= edgeRadius;
    }

    public DistanceEstimate pop() {
        lazyQueueUpdate();
        nodeDescriptors.get(queue.peek().value).lastIterationVisited = iterationIndex;
        return nodeDescriptors.get(queue.poll().value).estimate;
    }

    public DistanceEstimate top() {
        lazyQueueUpdate();
        return nodeDescriptors.get(queue.peek().value).estimate;
    }

    public boolean isActualEstimate(Node node) {
        return nodeDescriptors.get(node).lastIterationUpdated == iterationIndex;
    }

    public boolean visited(Node node) {
        return nodeDescriptors.get(node).lastIterationVisited == iterationIndex;
    }

    public DistanceEstimate distanceEstimate(Node node) {
        DistanceEstimate estimate = new DistanceEstimate(nodeDescriptors.get(node).estimate);
        if (!isActualEstimate(node)) {
            estimate.weight = Double.POSITIVE_INFINITY;
            estimate.edgeNumber = Long.MAX_VALUE;
        }
        return estimate;
    }

    void stop() {
        active = false;
    }

    private void lazyQueueUpdate() {
        while(!emptyContainer() && nodeDescriptors.get(queue.peek().value).estimate.weight != queue.peek().key) {
            queue.poll();
        }
    }

    private class SearchManagerNodeDescriptor {
        DistanceEstimate estimate;
        long lastIterationUpdated;
        long lastIterationVisited;

        SearchManagerNodeDescriptor(Node node, double estimateWeight) {
            this.lastIterationVisited = SearchManager.kStartIterationIndex;
            this.lastIterationUpdated = SearchManager.kStartIterationIndex;
            this.estimate = new DistanceEstimate(node, estimateWeight);
        }
    }
}
