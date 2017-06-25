package io.greennav.routing;

import de.topobyte.osm4j.core.model.impl.Node;
import de.topobyte.osm4j.core.model.util.OsmModelUtil;
import java.util.Map;

public class JSONNode {
    public long id;
    public double longitude;
    public double latitude;
    public Map<String, String> tags;

    public JSONNode(Node node) {
        this.id = node.getId();
        this.longitude = node.getLongitude();
        this.latitude = node.getLatitude();
        this.tags = OsmModelUtil.getTagsAsMap(node);
    }
}
