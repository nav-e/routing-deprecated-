package io.greennav.routing;

import java.util.*;
import java.util.function.Consumer;
import de.topobyte.osm4j.core.model.impl.Node;
import org.jgrapht.*;
import org.jgrapht.util.*;

class DijkstraClosestFirstIteratorWithCallback implements Iterator<Node> {
    private final RoadGraph graph;
    private final double radius;
    private final FibonacciHeap<QueueEntry> heap;
    private final Map<Node, FibonacciHeapNode<QueueEntry>> seen;
    private final Consumer<Node> nodeConsumer;
    private final Consumer<Node> predecessorConsumer;
    private final Consumer<Node> successorConsumer;

    DijkstraClosestFirstIteratorWithCallback(RoadGraph graph, Node source, double radius, Consumer<Node> nodeConsumer,
                                             Consumer<Node> predecessorConsumer, Consumer<Node> successorConsumer) {
        this.graph = Objects.requireNonNull(graph, "Graph cannot be null");
        if (radius < 0.0) {
            throw new IllegalArgumentException("Radius must be non-negative");
        }
        this.radius = radius;
        this.heap = new FibonacciHeap<>();
        this.seen = new HashMap<>();
        this.nodeConsumer = nodeConsumer;
        this.predecessorConsumer = predecessorConsumer;
        this.successorConsumer = successorConsumer;
        updateDistance(source, null, 0d);
    }

    @Override
    public boolean hasNext() {
        if (heap.isEmpty()) {
            return false;
        }
        FibonacciHeapNode<QueueEntry> vNode = heap.min();
        double vDistance = vNode.getKey();
        if (radius < vDistance) {
            heap.clear();
            return false;
        }
        return true;
    }

    @Override
    public Node next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }

        FibonacciHeapNode<QueueEntry> vNode = heap.removeMin();
        Node v = vNode.getData().v;
        double vDistance = vNode.getKey();
        nodeConsumer.accept(v);

        for (RoadEdge e : graph.incomingEdgesOf(v)) {
            Node u = Graphs.getOppositeVertex(graph, e, v);
            predecessorConsumer.accept(u);
        }

        for (RoadEdge e : graph.outgoingEdgesOf(v)) {
            Node u = Graphs.getOppositeVertex(graph, e, v);
            double eWeight = graph.getEdgeWeight(e);
            if (eWeight < 0.0) {
                throw new IllegalArgumentException("Negative edge weight not allowed");
            }
            updateDistance(u, e, vDistance + eWeight);
            successorConsumer.accept(u);
        }

        return v;
    }

    private void updateDistance(Node v, RoadEdge e, double distance) {
        FibonacciHeapNode<QueueEntry> node = seen.get(v);
        if (node == null) {
            node = new FibonacciHeapNode<>(new QueueEntry(e, v));
            heap.insert(node, distance);
            seen.put(v, node);
        } else {
            if (distance < node.getKey()) {
                heap.decreaseKey(node, distance);
                node.getData().e = e;
            }
        }
    }

    class QueueEntry {
        RoadEdge e;
        Node v;

        QueueEntry(RoadEdge e, Node v) {
            this.e = e;
            this.v = v;
        }
    }
}
