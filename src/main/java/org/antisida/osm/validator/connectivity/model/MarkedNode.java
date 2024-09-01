package org.antisida.osm.validator.connectivity.model;

import java.util.UUID;
import lombok.Data;

@Data
public final class MarkedNode {

  private long osmId;
  private long[] osmWayIds; //ids osm-веев, в которые входит точка //todo не понятно пока зачем они нужны
  private boolean visited;
  private UUID connectedComponentId;  //компонент связности в который входит точка
  private long[] neighborNodeIds; // ids соседних по веям точек, обычно двух (если нет петель), иначе больше
  private final int orderInWay;
  private final float lat;
  private final float lon;

  public MarkedNode(long osmId, long[] osmWayIds, long[] neighborNodeIds, UUID connectedComponentId,
                    int orderInWay,
                    float lat,
                    float lon) {
    this.osmId = osmId;
    this.osmWayIds = osmWayIds;
    this.neighborNodeIds = neighborNodeIds;
    this.connectedComponentId = connectedComponentId;
    this.orderInWay = orderInWay;
    this.lat = lat;
    this.lon = lon;
  }

  public void addWayIds(long[] osmWayIds) {
    this.osmWayIds = sumTwoArray(this.osmWayIds, osmWayIds);
  }

  private long[] sumTwoArray(long[] one, long[] two) {
    long[] result = new long[one.length + two.length];
    System.arraycopy(one, 0, result, 0, one.length);
    System.arraycopy(two, 0, result, one.length, two.length);
    return result;
  }

  public void addNeighborNodeIds(long[] neighborNodeIds) {
    this.neighborNodeIds = sumTwoArray(this.neighborNodeIds, neighborNodeIds);
  }

  public boolean notVisited() {return !visited;}
}
