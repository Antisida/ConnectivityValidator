package org.antisida.osm.validator.connectivity.model;

public record Node(
    long osmId,
    long wayOsmId,
    int regionId,
    int orderInWay,
    float lat,
    float lon
) {

}
