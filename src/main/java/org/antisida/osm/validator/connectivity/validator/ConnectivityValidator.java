package org.antisida.osm.validator.connectivity.validator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.text.Document;
import lombok.extern.slf4j.Slf4j;
import org.alex73.osmemory.MemoryStorage;
import org.alex73.osmemory.OsmWay;
import org.antisida.osm.validator.Validator;
import org.antisida.osm.validator.connectivity.model.MarkedNode;
import org.antisida.osm.validator.connectivity.model.Region;
import org.antisida.osm.validator.connectivity.model.RoutingWay;
import org.antisida.osm.validator.connectivity.model.RoutingWays;
import org.antisida.osm.validator.connectivity.model.ValidationResult;
import org.antisida.osm.validator.connectivity.repository.Repository;
import org.antisida.osm.validator.connectivity.service.ConnectivityResultService;
import org.antisida.osm.validator.connectivity.service.RegionService;
import org.antisida.osm.validator.connectivity.utils.FileUtils;
import org.antisida.osm.validator.connectivity.utils.OM5Utils;

@Slf4j
public class ConnectivityValidator implements Validator {

  private final ConnectivityUtils connectivityUtils;
  private final FileUtils fileUtils;
  private final OM5Utils om5Utils;
  private final Repository repository;
  private final RegionService regionService;
  private final ConnectivityResultService resultService;

  public ConnectivityValidator(ConnectivityUtils connectivityUtils,
                               FileUtils fileUtils,
                               OM5Utils om5Utils,
                               Repository repository,
                               RegionService regionService,
                               ConnectivityResultService resultService) {
    this.connectivityUtils = connectivityUtils;
    this.fileUtils = fileUtils;
    this.om5Utils = om5Utils;
    this.repository = repository;
    this.regionService = regionService;
    this.resultService = resultService;
  }

  @Override
  public String validate(List<String> forValidateFileNames) {
    List<Region> forValidateRegions = regionService.getAllByFileNameIn(forValidateFileNames);
    List<Region> neighborRegions = Optional.ofNullable(regionService.getNeighbors(forValidateRegions))
        .stream()
        .flatMap(Collection::stream)
        .filter(FileUtils::isExistO5mFile)
        .toList();

    Map<Region, List<RoutingWay>> waysByRegion = new HashMap<>();

    Stream.concat(forValidateRegions.stream(), neighborRegions.stream())
        .filter(resultService::isReadyInnerValidation)
//        .map(Region::path)
        .distinct()
        .map(region -> {
          MemoryStorage memoryStorage = fileUtils.readOM5File(region.path());
          ArrayList<OsmWay> routingWays = om5Utils.getRoutingWays(memoryStorage);
          List<RoutingWay> list = routingWays.stream()
              .map(osmWay -> new RoutingWay(region, osmWay))
              .toList();
          waysByRegion.put(region, list);
          return list;
        })
        .map(this::computeGraph)
        .flatMap(Optional::stream)
        .forEach(resultService::save);

    Map<Long, List<OsmWay>> allWays = Optional.of(region)
        .map(Region::path)
        .map(fileUtils::readOM5File)
        .map(om5Utils::getRoutingWays)
        .stream()
        .collect(Collectors.toMap(OsmWay::getId, osmWay -> osmWay));
    List<Long> isolatedWayIds = resultService.getIsolatedWayIds(regionId);
    isolatedWayIds.forEach(allWays.keySet()::remove);
    for (Region neighborRegion : neighborRegions) {
      List<Long> notIsolatedWayIds = resultService.getNotIsolatedWayIds(neighborRegion);
      for (Long notIsolatedWayId : notIsolatedWayIds) {
        if (allWays.containsKey(notIsolatedWayId)) {
          UUID componentId =
          allWays.remove(notIsolatedWayId);
        }
      }
    }
    List<Long>
//    forValidateRegions.stream()
    Optional.of(forValidateRegions.getFirst())
        .map(waysByRegion::get)
        .stream()
        .flatMap(Collection::stream)
        .filter(routingWay -> conteinsInMainGrepth(rget))
        .flatMap(Collection::stream)
        .filter(way -> conteins())
    forValidateRegions.parallelStream()
        .map(this::outerValidate)
        .forEach(resultService::setNotIsolated);
////    OsmRegion osmRegion = getOsmRegion(filePath); fixme
//    ValidationResult result = innerValidate(filePath);
//    repository.saveComponents(result.components());
//    repository.saveNodes(result.adjacencyList().values());
////    outerValidate(filePath, ); fixme
    return null;
  }

  public Optional<ValidationResult> computeGraph(List<RoutingWay> rw) {
    List<OsmWay> osmWays = Optional.of(rw).stream()
        .flatMap(Collection::stream)
        .map(RoutingWay::way)
        .toList();
    return Optional.of(osmWays)
        .map(connectivityUtils::createAdjacencyList)
        .map(adjacencyList -> connectivityUtils.markComponents(adjacencyList, rw.getFirst().region().id()))
        .map(validationResult -> {
          log.info("Region: {}. Component count: {}",
                   validationResult.components().getFirst().getRegionId(),
                   validationResult.components().size());
          return validationResult;
        });
  }

  public Optional<ValidationResult> innerValidate(Region region) {
    return Optional.of(region)
        .filter(resultService::isReadyInnerValidation)
        .map(Region::path)
        .map(fileUtils::readOM5File)
        .map(om5Utils::getRoutingWays)
        .map(connectivityUtils::createAdjacencyList)
        .map(adjacencyList -> connectivityUtils.markComponents(adjacencyList, region.id()))
        .map(validationResult -> {
          log.info("Region: {}. Component count: {}",
                   validationResult.components().getFirst().getRegionId(),
                   validationResult.components().size());
          return validationResult;
        });
  }

  private List<UUID> outerValidate(Region region) {

//    List<ConnectedComponent> isolatedComponents = resultService.getIsolatedComponentsRegionId(region.id());
    List<MarkedNode> isolatedNodes = resultService.getIsolatedNodes(region.id());

    List<Region> neighbors = regionService.getNeighbors(List.of(region));

    List<UUID> forChangeIsolateStatus = neighbors.stream()
        .filter(FileUtils::isExistO5mFile)//todo
        .map(Region::id)
        .map(resultService::getNotIsolatedNodeIds)
        .map(neighborNotIsolatedNodes ->
                 connectivityUtils.validateTwoAdjList(/*isolatedComponents,*/ isolatedNodes, neighborNotIsolatedNodes))
        .flatMap(Collection::stream)
        .distinct()
        .toList();
    log.info("Компоненты для смены статуса: {}", forChangeIsolateStatus);
    return forChangeIsolateStatus;

//    for (OsmRegion osmRegion: region.getNeighbors()) {
//      if (osmRegion == null) continue;
//      System.out.println(osmRegion.getName());
//    }
//
//    for (OsmRegion neighbor: region.getNeighbors()){
//      if (neighbor == null) continue;
//      AdjacencyList adjacencyNeighborList = SerializeUtils.deSerializeAdjList(neighbor.getPath());
//      validateTwoAdjList(adjacencyRegionList, adjacencyNeighborList);
//    }
////todo посмотреть что это
//    return new ValidationResult(region.getId(), region.getPath(),O5mStorageUtils
//    .readIsolatedWays
//    (adjacencyRegionList));
//    return null;
  }
}
