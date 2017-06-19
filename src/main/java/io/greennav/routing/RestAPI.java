package io.greennav.routing;

import de.topobyte.osm4j.core.model.impl.Node;
import io.greennav.persistence.Persistence;
import javafx.util.Pair;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static spark.Spark.get;

class RestAPI {
    void createServer(Persistence db) {
        /*
        Until Persistance is not integrated into Router, there will be a mockup of graph
         */
        final Node[] nodes_array = {
                new Node(0, 0, 0),
                new Node(1, 0, 1),
                new Node(2, 1, 0),
                new Node(3, 1, 1)
        };
        final List<Node> nodes = Arrays.asList(nodes_array);
        final List<Pair<Node, Node>> edges = Arrays.asList(new Pair[] {
                new Pair<>(nodes_array[0], nodes_array[1]),
                new Pair<>(nodes_array[1], nodes_array[2]),
                new Pair<>(nodes_array[1], nodes_array[3]),
                new Pair<>(nodes_array[0], nodes_array[3])
        });
        Router router = new DijkstraRouter(nodes, edges, new DistanceComputerInKm());
        /*
        End mockup
         */

        JSONTransformer toJson = new JSONTransformer();

        get("/query/:name", (request, response) -> {
            Collection<Node> query = db.queryNodes("name", request.params(":name"));
            Collection<JSONNode> res = new ArrayList<>();

            for (Node n : query)
                res.add(new JSONNode(n));

            return res;
        }, toJson);

        get("/from/:from/to/:to", (req, res) -> {
            long startId = Long.parseLong(req.params(":from"));
            long goalId = Long.parseLong(req.params(":to"));

            Node start = db.getNodeById(startId);
            Node goal = db.getNodeById(goalId);

            Collection<Node> path = router.getShortestPath(start, goal).getRoute();
            Collection<JSONNode> result = new ArrayList<>();

            for (Node n : path)
                result.add(new JSONNode(n));

            return result;
        }, toJson);
    }
}
