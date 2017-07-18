package io.greennav.routing.utils;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.jgrapht.*;
import org.jgrapht.util.*;

public class DijkstraClosestFirstIteratorWithRadiusBreakCallback<V, E> implements Iterator<V> {
    private final Graph<V, E> graph;
    private final double radius;
    private final FibonacciHeap<QueueEntry> heap;
    private final Map<V, FibonacciHeapNode<QueueEntry>> seen;
    private final Consumer<V> callback;

    public DijkstraClosestFirstIteratorWithRadiusBreakCallback(Graph<V, E> graph, V source, Consumer<V> callback,
                                                        double radius) {
        this.graph = Objects.requireNonNull(graph, "Graph cannot be null");
        if (radius < 0.0) {
            throw new IllegalArgumentException("Radius must be non-negative");
        }
        this.radius = radius;
        this.heap = new FibonacciHeap<>();
        this.seen = new HashMap<>();
        this.callback = callback;

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
            processHeapNodesNeighbors();
            heap.clear();
            return false;
        }
        return true;
    }

    @Override
    public V next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }

        FibonacciHeapNode<QueueEntry> vNode = heap.removeMin();
        V v = vNode.getData().v;
        double vDistance = vNode.getKey();

        Set<E> outgoingEdges = graph.edgesOf(v).stream()
                                    .filter(edge -> graph.getEdgeSource(edge).equals(v))
                                    .collect(Collectors.toSet());
        for (E e : outgoingEdges) {
            V u = Graphs.getOppositeVertex(graph, e, v);
            double eWeight = graph.getEdgeWeight(e);
            if (eWeight < 0.0) {
                throw new IllegalArgumentException("Negative edge weight not allowed");
            }
            updateDistance(u, e, vDistance + eWeight);
        }

        return v;
    }

    private void processHeapNodesNeighbors() {
        while (!heap.isEmpty()) {
            QueueEntry entry = heap.removeMin().getData();
            V source = graph.getEdgeSource(entry.e);
            callback.accept(source);
        }
    }

    private void updateDistance(V v, E e, double distance) {
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
        E e;
        V v;

        QueueEntry(E e, V v) {
            this.e = e;
            this.v = v;
        }
    }
}
