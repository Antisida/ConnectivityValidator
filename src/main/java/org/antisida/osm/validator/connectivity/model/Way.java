package org.antisida.osm.validator.connectivity.model;

public record Way(
    int id,
    long osmId,
    Node[] nodes
) {

}
