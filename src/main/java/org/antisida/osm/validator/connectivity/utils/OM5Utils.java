package org.antisida.osm.validator.connectivity.utils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import lombok.extern.slf4j.Slf4j;
import org.alex73.osmemory.MemoryStorage;
import org.alex73.osmemory.OsmWay;
import org.antisida.osm.validator.BenchUtils;

@Slf4j
public class OM5Utils {

  public ArrayList<OsmWay> getRoutingWays(MemoryStorage memoryStorage) {
    LocalDateTime start = LocalDateTime.now();

    ArrayList<OsmWay> osmWays = new ArrayList<>();
    memoryStorage.byTag("highway", o -> {
      if (
//                    o.getTag("highway", memoryStorage).equals("service") ||
          o.getTag("highway", memoryStorage).equals("unclassified") ||
              o.getTag("highway", memoryStorage).equals("residential") ||
              o.getTag("highway", memoryStorage).equals("tertiary") ||
              o.getTag("highway", memoryStorage).equals("secondary") ||
              o.getTag("highway", memoryStorage).equals("primary") ||
              o.getTag("highway", memoryStorage).equals("motorway") ||
              o.getTag("highway", memoryStorage).equals("trunk") ||
              o.getTag("highway", memoryStorage).equals("living_street") ||
              o.getTag("highway", memoryStorage).equals("motorway_link") ||
              o.getTag("highway", memoryStorage).equals("trunk_link") ||
              o.getTag("highway", memoryStorage).equals("primary_link") ||
              o.getTag("highway", memoryStorage).equals("tertiary_link") ||
              o.getTag("highway", memoryStorage).equals("secondary_link")
      ) {
        if (o instanceof OsmWay) {
          osmWays.add((OsmWay) o);
        }
      }
    });
    memoryStorage.byTag("ferry", o -> {
      if (
          o.getTag("ferry", memoryStorage).equals("unclassified") ||
              o.getTag("ferry", memoryStorage).equals("residential") ||
              o.getTag("ferry", memoryStorage).equals("tertiary") ||
              o.getTag("ferry", memoryStorage).equals("secondary") ||
              o.getTag("ferry", memoryStorage).equals("primary") ||
              o.getTag("ferry", memoryStorage).equals("trunk")
      ) {
        if (o instanceof OsmWay) {
          osmWays.add((OsmWay) o);
        }
      }
    });
    log.info("{}: {} mill", BenchUtils.calledMethod(), Duration.between(start, LocalDateTime.now()).toMillis());
    return osmWays;
  }

}
