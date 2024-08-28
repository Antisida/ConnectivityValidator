package org.antisida.osm.validator.connectivity.model;

import java.util.List;

public record OsmRegion(
    String name,
    int id,
    boolean isRussian,
    Integer[] neighborIds,
    List<OsmRegion>neighbors,//todo убрать поле. Получать, когда надо, из базы по id
    String path
) {

}
