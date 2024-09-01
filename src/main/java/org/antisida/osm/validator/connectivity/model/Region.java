package org.antisida.osm.validator.connectivity.model;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import lombok.EqualsAndHashCode;


public record Region(
    int id,
    String name,

//    boolean isRussian,
    int[] neighborIds,
//    List<Region>neighbors,//todo убрать поле. Получать, когда надо, из базы по id
    String path
) {

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Region region)) return false;
    return id == region.id && Objects.equals(name, region.name) && Objects.equals(path, region.path)
        && Objects.deepEquals(neighborIds, region.neighborIds);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, Arrays.hashCode(neighborIds), path);
  }
}
