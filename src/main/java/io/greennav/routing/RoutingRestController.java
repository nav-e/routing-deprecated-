package io.greennav.routing;

import de.topobyte.osm4j.core.model.impl.Node;
import io.greennav.persistence.Persistence;
<<<<<<< HEAD
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import java.util.Collection;
=======
import io.greennav.routing.roadgraph.impl.DistanceComputerInMetres;
import io.greennav.routing.roadgraph.iface.NodeWeightFunction;
import io.greennav.routing.router.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
>>>>>>> 137679c0877850fb45b75cccdbfdf85d753eff15
import java.util.List;

@RestController
public class RoutingRestController {
    final private Persistence db;
<<<<<<< HEAD
    final private NodeWeightFunction weightFunction = new DistanceComputerInKm();
=======
    final private NodeWeightFunction weightFunction = new DistanceComputerInMetres();
>>>>>>> 137679c0877850fb45b75cccdbfdf85d753eff15
    private Router router;

    @Autowired
    RoutingRestController(Persistence db) {
        this.db = db;
<<<<<<< HEAD
        this.router = new AStarRouter(db, weightFunction);
    }

    @RequestMapping(method = RequestMethod.GET, path = "/query/{name}")
    Collection<Node> queryNodes(@PathVariable String name) {
        return db.queryNodes("name", name);
    }

    @RequestMapping(method = RequestMethod.GET, path = "/{fromId}/{toId}/{algorithm}")
    List<Node> getShortestPath(@PathVariable Long fromId, @PathVariable Long toId, @PathVariable String algorithm) {
=======
    }

    @RequestMapping(method = RequestMethod.GET, path = "/route")
    List<Node> getShortestPath(@RequestParam(value = "from") Long fromId,
                               @RequestParam(value = "to") Long toId,
                               @RequestParam(value = "algorithm") String algorithm) {
>>>>>>> 137679c0877850fb45b75cccdbfdf85d753eff15
        final Node fromNode = db.getNodeById(fromId);
        final Node toNode = db.getNodeById(toId);
        switch (algorithm) {
            case "dijkstra":
                router = new DijkstraRouter(db, weightFunction);
                break;
            case "astar":
                router = new AStarRouter(db, weightFunction);
                break;
<<<<<<< HEAD
=======
            case "bidirectional-dijkstra":
                router = new BidirectionalDijkstraRouter(db, weightFunction);
                break;
            case "contraction-hierarchies":
                router = new ContractionHierarchiesRouter(db, weightFunction);
                break;
>>>>>>> 137679c0877850fb45b75cccdbfdf85d753eff15
            default:
                break;
        }
        return router.getShortestPath(fromNode, toNode).getRoute();
    }
}
