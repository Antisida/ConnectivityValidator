package org.antisida.osm.validator.connectivity.service;

import java.util.List;
import org.alex73.osmemory.OsmWay;
import org.antisida.osm.validator.connectivity.model.Node;
import org.antisida.osm.validator.connectivity.model.Way;
import org.antisida.osm.validator.connectivity.repository.WayRepository;

public class WayService {
  private static volatile WayService instance;
  private final WayRepository wayRepository;

  private WayService() {
    this.wayRepository = WayRepository.getInstance();
  }

  public static WayService getInstance() {
    WayService localInstance = instance;
    if (localInstance == null) {
      synchronized (WayService.class) {
        localInstance = instance;
        if (localInstance == null) {
          instance = localInstance = new WayService();
        }
      }
    }
    return localInstance;
  }

  public List<OsmWay> save(List<OsmWay> osmWays) {
    List<Way> ways = osmWays.stream()
        .map(this::toWay)
        .toList();
    wayRepository.saveAll(ways);
    return osmWays;
  }

  private Way toWay(OsmWay osmWay) {
    Node[] nodes = getNodes(osmWay);
    return new Way(osmWay.getId(), osmWay.getNodeIds(), false)
  }
}
