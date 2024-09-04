package org.antisida.osm.validator.connectivity.model;

import org.alex73.osmemory.OsmWay;

public record RoutingWay(
    Region region,
    OsmWay way
) {
}
