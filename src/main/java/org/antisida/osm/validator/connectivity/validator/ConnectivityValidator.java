package org.antisida.osm.validator.connectivity.validator;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.antisida.osm.validator.Validator;
import org.antisida.osm.validator.connectivity.model.MarkedNode;
import org.antisida.osm.validator.connectivity.model.Region;
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

    Stream.concat(forValidateRegions.stream(), neighborRegions.stream())
        .distinct()
        .map(this::innerValidate)
        .flatMap(Optional::stream)
        .forEach(resultService::save);

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
