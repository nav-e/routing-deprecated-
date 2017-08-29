package io.greennav.routing.roadgraph.impl;

import de.topobyte.osm4j.core.model.impl.Node;
import io.greennav.persistence.Persistence;
import io.greennav.routing.roadgraph.iface.NodeWeightFunction;
import io.greennav.routing.shortestpath.DistanceEstimate;
import io.greennav.routing.shortestpath.SearchManager;
import io.greennav.routing.utils.QueueEntry;
import org.jgrapht.Graphs;
import org.jgrapht.graph.specifics.ArrayUnenforcedSetEdgeSetFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RoadGraphCH extends RoadGraph<RoadEdgeCH> {
    public final SearchManager forwardSearchManager;
    public final SearchManager reverseSearchManager;
    private final List<Node> touchingEdgesSources;
    private final Map<Node, DirectedEdgeContainer<Node, RoadEdgeCH>> shortcutsContainer;
    public final Map<Node, RoadGraphCHNodeDescriptor> nodeDescriptors;

    public RoadGraphCH(Persistence persistence, NodeWeightFunction nodeWeightFunction) {
        super(persistence, nodeWeightFunction, RoadEdgeCH.class);
        fullInitialize();
        nodeDescriptors = new LinkedHashMap<>();
        vertexSet().forEach(node -> nodeDescriptors.put(node, new RoadGraphCHNodeDescriptor()));
        forwardSearchManager = new SearchManager(this::outgoingEdgesOf, vertexSet());
        reverseSearchManager = new SearchManager(this::incomingEdgesOf, vertexSet());
        touchingEdgesSources = new ArrayList<>();
        shortcutsContainer = new LinkedHashMap<>();
    }

    public Optional<Node> getIntermediateNode(final RoadEdgeCH edge) {
        return edge.getIntermediateNode();
    }

    private void setIntermediateNode(final RoadEdgeCH edge, final Node node) {
        edge.setIntermediateNode(node);
    }

    @Override
    protected void cacheNeighborsIfAbsent(final Node node, final boolean outgoing) {
        final Set<Node> container = outgoing ? cachedOutgoingNeighbors : cachedIncomingNeighbors;
        final Function<Node, Set<Node>> persistenceNeighborsFunction = outgoing ?
                persistence::outgoingNeighbors : persistence::incomingNeighbors;
        final Function<Node, Set<RoadEdgeCH>> shortcutsNeighborsFunction = v -> {
            DirectedEdgeContainer<Node, RoadEdgeCH> edgeContainer = shortcutsContainer.get(v);
            if (outgoing) {
                return edgeContainer.getUnmodifiableOutgoingEdges();
            } else {
                return edgeContainer.getUnmodifiableIncomingEdges();
            }
        };
        if (!container.contains(node)) {
            addNeighborsByNode(node, persistenceNeighborsFunction, outgoing);
            if (shortcutsContainer.containsKey(node)) {
                addNeighborsByEdge(node, shortcutsNeighborsFunction, outgoing);
            }
            container.add(node);
        }
    }

    private void AddTouchingEdge(final RoadEdgeCH edge) {
        final Node sourceNode = getEdgeSource(edge);
        final RoadGraphCHNodeDescriptor sourceDescriptor = nodeDescriptors.get(sourceNode);
        sourceDescriptor.touchingEdges.add(edge);
        touchingEdgesSources.add(sourceNode);
    }

    private void RemoveAllTouchingEdges() {
        touchingEdgesSources.forEach(node -> nodeDescriptors.get(node).touchingEdges.clear());
        touchingEdgesSources.clear();
    }

    private List<RoadEdgeCH> TouchingEdgesOf(final Node node) {
        return nodeDescriptors.get(node).touchingEdges;
    }

    private Stream<RoadEdgeCH> outgoingEdgesActiveFilter(final Node node) {
        return outgoingEdgesOf(node).stream().filter(edge -> isActiveNode(getEdgeTarget(edge)));
    }

    private Stream<RoadEdgeCH> incomingEdgesActiveFilter(final Node node) {
        return incomingEdgesOf(node).stream().filter(edge -> isActiveNode(getEdgeSource(edge)));
    }

    private boolean isActiveNode(final Node node) {
        final RoadGraphCHNodeDescriptor descriptor = nodeDescriptors.get(node);
        return !descriptor.toContract && !descriptor.isContracted;
    }

    private void UpdateContractionStatusOfNeighbor(final Node node, final Node neighbor) {
        RoadGraphCHNodeDescriptor nodeDescriptor = nodeDescriptors.get(node);
        RoadGraphCHNodeDescriptor neighborDescriptor = nodeDescriptors.get(neighbor);
        ++neighborDescriptor.numberOfContractedNeighbors;
        neighborDescriptor.level = Math.max(neighborDescriptor.level, nodeDescriptor.level + 1);
    }

    private void removeAllEdgesSafely(final Set<RoadEdgeCH> edges) {
        removeAllEdges(edges.toArray(new RoadEdgeCH[edges.size()]));
    }

    private void ContractNodes() {
        for (Map.Entry<Node, RoadGraphCHNodeDescriptor> entry : nodeDescriptors.entrySet()) {
            final Node node = entry.getKey();
            final RoadGraphCHNodeDescriptor descriptor = entry.getValue();
            if (descriptor.toContract) {
                removeAllEdgesSafely(outgoingEdgesOf(node));
                removeAllEdgesSafely(incomingEdgesOf(node));
                removeVertex(node);
                descriptor.toContract = false;
                descriptor.isContracted = true;
            }
        }
    }

    private void OneMoreEdgeUpdate(final Node node) {
        if (forwardSearchManager.isActualEstimate(node)) {
            for (RoadEdgeCH touchingEdge : TouchingEdgesOf(node)) {
                final Node neighbor = getEdgeTarget(touchingEdge);
                final double weight = getEdgeWeight(touchingEdge);
                forwardSearchManager.update(node, neighbor, weight, false);
            }
        }
    }

    private void WitnessPathSearch(final Node fromNode, final double weightRadius, final long edgeRadius) {
        forwardSearchManager.initialize(fromNode, weightRadius, edgeRadius - 1);
        while (!forwardSearchManager.empty()) {
            final DistanceEstimate minUnvisited = forwardSearchManager.pop();
            final Node node = minUnvisited.node;
            for (RoadEdgeCH edge : forwardSearchManager.neighborsOf(node)) {
                final Node neighbor = Graphs.getOppositeVertex(this, edge, node);
                if (isActiveNode(neighbor)) {
                    forwardSearchManager.update(node, neighbor, getEdgeWeight(edge));
                    OneMoreEdgeUpdate(neighbor);
                }
            }
        }
        while (!forwardSearchManager.emptyContainer()) {
            OneMoreEdgeUpdate(forwardSearchManager.pop().node);
        }
    }

    private void ContractNodeSimulation(final Node contractingNode, long witnessPathEdgeRadius,
                                        final BiConsumer<RoadEdgeCH, RoadEdgeCH> callback) {
        if (!incomingEdgesActiveFilter(contractingNode).findFirst().isPresent()) {
            return;
        }
        if (!outgoingEdgesActiveFilter(contractingNode).findFirst().isPresent()) {
            return;
        }
        final Optional<RoadEdgeCH> edgeWithMaxWeightToTarget = outgoingEdgesActiveFilter(contractingNode)
                .max((lhs, rhs) -> (int)Math.signum(getEdgeWeight(lhs) - getEdgeWeight(rhs)));
        final double maxWeightToTarget = edgeWithMaxWeightToTarget.map(this::getEdgeWeight)
                                                                  .orElse(Double.POSITIVE_INFINITY);
        final Supplier<Stream<RoadEdgeCH>> intermediateEdgesStreamGetter =
                () -> outgoingEdgesActiveFilter(contractingNode)
                        .map(this::getEdgeTarget)
                        .map(this::incomingEdgesOf)
                        .flatMap(Collection::stream);
        intermediateEdgesStreamGetter.get().forEach(this::AddTouchingEdge);
        final Optional<RoadEdgeCH> edgeWithMinWeightToIntermediate = intermediateEdgesStreamGetter
                .get().min((lhs, rhs) -> (int)(getEdgeWeight(lhs) - getEdgeWeight(rhs)));
        final double minWeightToIntermediate = edgeWithMinWeightToIntermediate.map(this::getEdgeWeight)
                                                                              .orElse(Double.POSITIVE_INFINITY);
        final Set<RoadEdgeCH> activeIncomingEdges =
                incomingEdgesActiveFilter(contractingNode).collect(Collectors.toSet());
        for (RoadEdgeCH incomingEdge : activeIncomingEdges) {
            final double radius = getEdgeWeight(incomingEdge) + maxWeightToTarget - minWeightToIntermediate;
            WitnessPathSearch(getEdgeSource(incomingEdge), radius, witnessPathEdgeRadius);
            final Set<RoadEdgeCH> activeOutgoingEdges = outgoingEdgesActiveFilter(contractingNode)
                    .filter(edge -> getEdgeSource(edge) != getEdgeTarget(edge))
                    .collect(Collectors.toSet());
            for (RoadEdgeCH outgoingEdge : activeOutgoingEdges) {
                final double bypassPathWeight =
                        forwardSearchManager.distanceEstimate(getEdgeTarget(outgoingEdge)).weight;
                final double directPathWeight = getEdgeWeight(incomingEdge) + getEdgeWeight(outgoingEdge);
                if (directPathWeight < bypassPathWeight) {
                    callback.accept(incomingEdge, outgoingEdge);
                }
            }
        }
        RemoveAllTouchingEdges();
    }

    private void addShortcutToContainer(RoadEdgeCH edge, Node node) {
        if (!shortcutsContainer.containsKey(node)) {
            final ArrayUnenforcedSetEdgeSetFactory<Node, RoadEdgeCH> edgeSetFactory =
                    new ArrayUnenforcedSetEdgeSetFactory<>();
            shortcutsContainer.put(node, new DirectedEdgeContainer<>(edgeSetFactory, node));
        }
        final DirectedEdgeContainer<Node, RoadEdgeCH> directedEdgeContainer = shortcutsContainer.get(node);
        if (getEdgeSource(edge) == node) {
            directedEdgeContainer.addOutgoingEdge(edge);
        } else if (getEdgeTarget(edge) == node) {
            directedEdgeContainer.addIncomingEdge(edge);
        }
    }

    private void AddShortcuts(final Node contractingNode, final long witnessPathEdgeRadius) {
        BiConsumer<RoadEdgeCH, RoadEdgeCH> addShortcutIfNecessary = (incomingEdge, outgoingEdge) -> {
            final double shortcutWeight = getEdgeWeight(incomingEdge) + getEdgeWeight(outgoingEdge);
            final Node source = getEdgeSource(incomingEdge);
            final Node target = getEdgeTarget(outgoingEdge);
            final RoadEdgeCH searchResult = getEdge(source, target);
            final RoadEdgeCH shortcut = searchResult == null ? addEdge(source, target) : searchResult;
            if (searchResult == null) {
                addShortcutToContainer(shortcut, source);
                addShortcutToContainer(shortcut, target);
            }
            if (searchResult == null || shortcutWeight < getEdgeWeight(searchResult)) {
                setEdgeWeight(shortcut, shortcutWeight);
                setIntermediateNode(shortcut, contractingNode);
            }
        };
        nodeDescriptors.get(contractingNode).toContract = true;
        outgoingEdgesActiveFilter(contractingNode)
                .map(this::getEdgeTarget)
                .forEach(neighbor -> UpdateContractionStatusOfNeighbor(contractingNode, neighbor));
        incomingEdgesActiveFilter(contractingNode)
                .map(this::getEdgeSource)
                .forEach(neighbor -> UpdateContractionStatusOfNeighbor(contractingNode, neighbor));
        ContractNodeSimulation(contractingNode, witnessPathEdgeRadius, addShortcutIfNecessary);
    }

    private long UpdateNodeImportance(final Node node, final long witnessPathEdgeRadius) {
        AtomicLong numberOfShortcuts = new AtomicLong();
        Set<Node> neighborsObtainingShortcut = new LinkedHashSet<>();
        BiConsumer<RoadEdgeCH, RoadEdgeCH> computeShortcutStatistics = (incomingEdge, outgoingEdge) -> {
            numberOfShortcuts.getAndIncrement();
            neighborsObtainingShortcut.add(getEdgeSource(incomingEdge));
            neighborsObtainingShortcut.add(getEdgeTarget(outgoingEdge));
        };
        RoadGraphCHNodeDescriptor nodeDescriptor = nodeDescriptors.get(node);
        nodeDescriptor.toContract = true;
        ContractNodeSimulation(node, witnessPathEdgeRadius, computeShortcutStatistics);
        nodeDescriptor.toContract = false;
        final long inDegree = incomingEdgesOf(node)
                .stream()
                .map(this::getEdgeSource)
                .filter(v -> !nodeDescriptors.get(v).toContract)
                .count();
        final long outDegree = outgoingEdgesOf(node)
                .stream()
                .map(this::getEdgeTarget)
                .filter(v -> !nodeDescriptors.get(v).toContract)
                .count();
        final long edgeDifference = numberOfShortcuts.get() - inDegree - outDegree;
        final long numberOfContractedNeighbors = nodeDescriptor.numberOfContractedNeighbors;
        final long shortcutCover = neighborsObtainingShortcut.size();
        final long nodeLevel = nodeDescriptor.level;
        return nodeDescriptor.importance = edgeDifference + numberOfContractedNeighbors + shortcutCover + nodeLevel;
    }

    public void PreprocessGraph() {
        final Set<Node> nodeSet = vertexSet();
        final int numberOfNodes = nodeSet.size();
        final PriorityQueue<QueueEntry<Long, Node>> queue = new PriorityQueue<>();
        for (Node node : nodeSet) {
            final long nodeImportance = UpdateNodeImportance(node, 1);
            queue.add(new QueueEntry<>(nodeImportance, node));
        }
        final int kNumIterationsToUpdate = 1 << 17;
        final int kMaxHopSize = 3;
        final int kPartSize = Math.max(numberOfNodes / kMaxHopSize, 1);
        AtomicLong numberOfContractedNodes = new AtomicLong();
        while (!queue.isEmpty()) {
            final long witnessPathEdgeRadius = numberOfContractedNodes.get() / kPartSize + 1;
            final Node minOrderEntryNode = queue.poll().value;
            final long updatedImportance = UpdateNodeImportance(minOrderEntryNode, witnessPathEdgeRadius);
            if (queue.isEmpty() || updatedImportance <= queue.peek().key) {
                AddShortcuts(minOrderEntryNode, witnessPathEdgeRadius);
                nodeDescriptors.get(minOrderEntryNode).rank = numberOfContractedNodes.getAndIncrement();
                if (numberOfContractedNodes.get() % kNumIterationsToUpdate == 0) {
                    ContractNodes();
                    queue.clear();
                    nodeDescriptors
                            .entrySet().stream()
                            .filter(entry -> !entry.getValue().isContracted)
                            .forEach(entry -> {
                                final long importance = entry.getValue().importance;
                                final Node node = entry.getKey();
                                queue.add(new QueueEntry<>(importance, node));
                            });
                }
            } else {
                queue.add(new QueueEntry<>(updatedImportance, minOrderEntryNode));
            }
        }
        ContractNodes();
        reset();
    }
}
