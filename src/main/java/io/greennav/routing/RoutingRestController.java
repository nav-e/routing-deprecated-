package io.greennav.routing;

import de.topobyte.osm4j.core.model.impl.Node;
import io.greennav.persistence.Persistence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import java.util.Collection;

@RestController
public class RoutingRestController {
    final private Persistence db;
    final private Router router;

    @Autowired
    RoutingRestController(Persistence db) {
        this.db = db;
        this.router = new AStarRouter(db, new DistanceComputerInKm());
    }

    @RequestMapping(method = RequestMethod.GET, path = "/query/{name}")
    Collection<Node> queryNodes(@PathVariable String name) {
        return db.queryNodes("name", name);
    }

    @RequestMapping(method = RequestMethod.GET, path = "/from/{fromId}/to/{toId}")
    Collection<Node> getShortestPath(@PathVariable Long fromId, @PathVariable Long toId) {
        final Node fromNode = db.getNodeById(fromId);
        final Node toNode = db.getNodeById(toId);
        return router.getShortestPath(fromNode, toNode).getRoute();
    }
}
