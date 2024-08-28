package org.antisida.osm.validator.connectivity.model;

import java.util.UUID;
import lombok.Data;

@Data
public final class MarkedNode {
  private long osmId;
  private long[] osmWayIds; //ids osm-веев, в которые входит точка //todo не понятно пока зачем они нужны
  private boolean visited;
  private UUID connectedComponentId; //компонент связности в который входит точка
  private long[] neighborNodeIds; // ids соседних по веям точек, обычно двух (если нет петель), иначе больше

  public MarkedNode(long osmId, long osmWayId, long[] neighborNodeIds) {
    this.osmId = osmId;
    this.osmWayIds = new long[]{osmWayId};
    this.neighborNodeIds = neighborNodeIds;
  }

  public long[] addWayIds(long[] osmWayIds) {
    this.osmWayIds = sumTwoArray(this.osmWayIds, osmWayIds);
    return this.osmWayIds;
  }

  private long[] sumTwoArray(long[] one, long[] two) {
    long[] result = new long[one.length + two.length];
    System.arraycopy(one, 0, result, 0, one.length);
    System.arraycopy(two, 0, result, one.length, two.length);
    return result;
  }

  public long[] addNeighborNodeIds(long[] neighborNodeIds) {
    this.neighborNodeIds = sumTwoArray(this.neighborNodeIds, neighborNodeIds);
    return this.neighborNodeIds;
  }
}
