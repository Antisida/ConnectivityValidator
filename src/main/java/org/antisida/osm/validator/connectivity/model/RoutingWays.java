package org.antisida.osm.validator.connectivity.model;

import java.util.List;
import org.alex73.osmemory.OsmWay;

public record RoutingWays(
    Region region,
    List<OsmWay> ways
) {
}
