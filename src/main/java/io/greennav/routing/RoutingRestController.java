package io.greennav.routing;

import de.topobyte.osm4j.core.model.impl.Node;
import io.greennav.persistence.Persistence;
import io.greennav.routing.roadgraph.impl.DistanceComputerInMetres;
import io.greennav.routing.roadgraph.iface.NodeWeightFunction;
import io.greennav.routing.router.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
public class RoutingRestController {
    final private Persistence db;
    final private NodeWeightFunction weightFunction = new DistanceComputerInMetres();
    private Router router;

    @Autowired
    RoutingRestController(Persistence db) {
        this.db = db;
    }

    @RequestMapping(method = RequestMethod.GET, path = "/route")
    List<Node> getShortestPath(@RequestParam(value = "from") Long fromId,
                               @RequestParam(value = "to") Long toId,
                               @RequestParam(value = "algorithm") String algorithm) {
        final Node fromNode = db.getNodeById(fromId);
        final Node toNode = db.getNodeById(toId);
        switch (algorithm) {
            case "dijkstra":
                router = new DijkstraRouter(db, weightFunction);
                break;
            case "astar":
                router = new AStarRouter(db, weightFunction);
                break;
            case "bidirectional-dijkstra":
                router = new BidirectionalDijkstraRouter(db, weightFunction);
                break;
            case "contraction-hierarchies":
                router = new ContractionHierarchiesRouter(db, weightFunction);
                break;
            default:
                break;
        }
        return router.getShortestPath(fromNode, toNode).getRoute();
    }
}
