package io.greennav.routing.roadgraph.impl;

import java.util.ArrayList;
import java.util.List;

public class RoadGraphCHNodeDescriptor {
    public long importance;
    public long rank;
    public boolean isContracted;
    public boolean toContract;
    public int numberOfContractedNeighbors;
    public long level;
    public List<RoadEdgeCH> touchingEdges = new ArrayList<>();
}
