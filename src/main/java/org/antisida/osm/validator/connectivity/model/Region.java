package org.antisida.osm.validator.connectivity.model;

import java.util.List;

public record Region(
    int id,
    String name,

//    boolean isRussian,
    int[] neighborIds,
//    List<Region>neighbors,//todo убрать поле. Получать, когда надо, из базы по id
    String path
) {

}
