package io.greennav.routing;

import de.topobyte.osm4j.core.model.impl.Node;
import io.greennav.persistence.Persistence;
import java.util.ArrayList;
import java.util.Collection;

import static spark.Spark.get;

class RestAPI {
    void createServer(Persistence db) {
        final Router router = new DijkstraRouter(db, new DistanceComputerInKm());

        final JSONTransformer toJson = new JSONTransformer();

        get("/query/:name", (request, response) -> {
            final Collection<Node> query = db.queryNodes("name", request.params(":name"));
            final Collection<JSONNode> res = new ArrayList<>();

            for (Node n : query)
                res.add(new JSONNode(n));

            return res;
        }, toJson);

        get("/from/:from/to/:to", (req, res) -> {
            final long startId = Long.parseLong(req.params(":from"));
            final long goalId = Long.parseLong(req.params(":to"));

            final Node start = db.getNodeById(startId);
            final Node goal = db.getNodeById(goalId);

            final Collection<Node> path = router.getShortestPath(start, goal).getRoute();
            final Collection<JSONNode> result = new ArrayList<>();

            for (Node n : path)
                result.add(new JSONNode(n));

            return result;
        }, toJson);
    }
}
