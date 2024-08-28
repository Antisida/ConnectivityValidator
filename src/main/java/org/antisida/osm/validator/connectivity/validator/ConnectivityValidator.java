package org.antisida.osm.validator.connectivity.validator;

import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.antisida.osm.validator.connectivity.model.OsmRegion;
import org.antisida.osm.validator.connectivity.model.ValidationResult;
import org.antisida.osm.validator.connectivity.repository.Repository;
import org.antisida.osm.validator.connectivity.utils.FileUtils;
import org.antisida.osm.validator.connectivity.utils.OM5Utils;

@Slf4j
public class ConnectivityValidator implements Validator {

  private final ConnectivityUtils connectivityUtils;
  private final FileUtils fileUtils;
  private final OM5Utils om5Utils;
  private final Repository repository;

  public ConnectivityValidator(ConnectivityUtils connectivityUtils, FileUtils fileUtils,
      OM5Utils om5Utils, Repository repository) {
    this.connectivityUtils = connectivityUtils;
    this.fileUtils = fileUtils;
    this.om5Utils = om5Utils;
    this.repository = repository;
  }

  @Override
  public String validate(String filePath) {
//    OsmRegion osmRegion = getOsmRegion(filePath); fixme
    ValidationResult result = innerValidate(filePath);
    repository.saveComponents(result.components());
    repository.saveNodes(result.adjacencyList().values());
//    outerValidate(filePath, ); fixme
    return null;
  }

  public ValidationResult innerValidate(String filePath) {
    return Optional.of(filePath)
        .map(fileUtils::readOM5File)
        .map(om5Utils::getRoutingWays)
        .map(connectivityUtils::createAdjacencyList)
        .map(connectivityUtils::markNodesAndComputeComponents)
        .map(validationResult -> {
          log.info("Регион: --. Количество ConnectedComponent: {}",
              validationResult.components().size());
          return validationResult;
        })
        .orElseThrow();
  }

}
