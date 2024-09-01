package org.antisida.osm.validator.connectivity.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.antisida.osm.validator.BenchUtils;
import org.antisida.osm.validator.connectivity.model.Region;
import org.antisida.osm.validator.connectivity.repository.RegionRepository;
import org.antisida.osm.validator.connectivity.utils.ArrayUtils;

@Slf4j
public class RegionService {

  private static volatile RegionService instance;

  private RegionService() { }

  public static RegionService getInstance() {
    RegionService localInstance = instance;
    if (localInstance == null) {
      synchronized (RegionService.class) {
        localInstance = instance;
        if (localInstance == null) {
          instance = localInstance = new RegionService();
        }
      }
    }
    return localInstance;
  }

  private final RegionRepository regionRepository = RegionRepository.getInstance();

  public List<Region> getAllByFileNameIn(List<String> fileNames) {
    LocalDateTime start = LocalDateTime.now();
    List<Region> allByFileNameIn = regionRepository.getAllByFileNameIn(fileNames);
    log.info("{}: {} mill", BenchUtils.calledMethod(), Duration.between(start, LocalDateTime.now()).toMillis());
//    log.info(allByFileNameIn.toString());
//    allByFileNameIn.stream()
//        .map(Region::neighborIds)
//        .forEach(n -> log.info(Arrays.toString(n)));

    return allByFileNameIn;

  }

  public List<Region> getNeighbors(List<Region> forValidateRegions) {
    List<Integer> neighborIds = forValidateRegions.stream()
        .map(Region::neighborIds)
        .map(ArrayUtils::toIntArray)
        .flatMap(Arrays::stream)
        .distinct()
        .toList();
    List<Region> neighbors = regionRepository.getRegionsIn(neighborIds);
    log.info(neighbors.toString());
//    neighbors.stream()
//        .map(Region::neighborIds)
//        .forEach(n -> log.info(Arrays.toString(n)));
    return neighbors;
  }
}
