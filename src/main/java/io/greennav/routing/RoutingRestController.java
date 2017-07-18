package io.greennav.routing;

import de.topobyte.osm4j.core.model.impl.Node;
import io.greennav.persistence.Persistence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@RestController
public class RoutingRestController {
    final private Persistence db;
    final private NodeWeightFunction weightFunction = new DistanceComputerInKilometres();
    private Router router;

    @Autowired
    RoutingRestController(Persistence db) {
        this.db = db;
        this.router = new AStarRouter(db, weightFunction);
    }

    @RequestMapping(method = RequestMethod.GET, path = "/query/{name}")
    Collection<Node> queryNodes(@PathVariable String name) {
        return db.queryNodes("name", name);
    }

    @RequestMapping(method = RequestMethod.GET, path = "/{fromId}/{toId}/{algorithm}")
    List<Node> getShortestPath(@PathVariable Long fromId, @PathVariable Long toId, @PathVariable String algorithm) {
        final Node fromNode = db.getNodeById(fromId);
        final Node toNode = db.getNodeById(toId);
        switch (algorithm) {
            case "dijkstra":
                router = new DijkstraRouter(db, weightFunction);
                break;
            case "astar":
                router = new AStarRouter(db, weightFunction);
                break;
            default:
                break;
        }
        return router.getShortestPath(fromNode, toNode).getRoute();
    }

    @RequestMapping(method = RequestMethod.GET, path = "/range?startid={startid}&range={range}")
    Set<Node> getAvailableRangeBorderNodes(@PathVariable("startid") Long startId,
                                           @PathVariable Double range) {
        final Node sourceNode = db.getNodeById(startId);
        final Double rangeInKilometres = range / 1000;
        return router.getBorderNodes(sourceNode, rangeInKilometres);
    }
}
