package org.antisida.osm.validator.connectivity.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.antisida.osm.validator.BenchUtils;
import org.antisida.osm.validator.connectivity.model.ConnectedComponent;
import org.antisida.osm.validator.connectivity.model.MarkedNode;
import org.antisida.osm.validator.connectivity.model.Region;
import org.antisida.osm.validator.connectivity.model.ValidationResult;
import org.antisida.osm.validator.connectivity.repository.ComponentRepository;
import org.antisida.osm.validator.connectivity.repository.NodeRepository;

@Slf4j
public class ConnectivityResultService {

  private static volatile ConnectivityResultService instance;
  private final ComponentRepository componentRepository;
  private final NodeRepository nodeRepository;

  private ConnectivityResultService() {
    this.componentRepository = ComponentRepository.getInstance();
    this.nodeRepository = NodeRepository.getInstance();
  }

  public static ConnectivityResultService getInstance() {
    ConnectivityResultService localInstance = instance;
    if (localInstance == null) {
      synchronized (ConnectivityResultService.class) {
        localInstance = instance;
        if (localInstance == null) {
          instance = localInstance = new ConnectivityResultService();
        }
      }
    }
    return localInstance;
  }


  public void save(ValidationResult validationResult) {
    saveComponents(validationResult.components());
    saveNodes(new ArrayList<>(validationResult.adjacencyList().values()));
  }

  private void saveNodes(List<MarkedNode> values) {
    LocalDateTime start = LocalDateTime.now();
    nodeRepository.save(values);
    log.info("{}: {} mill", BenchUtils.calledMethod(),
             Duration.between(start, LocalDateTime.now()).toMillis());
    log.info("{}: {} mill per 10 000 nodes", BenchUtils.calledMethod(),
             Duration.between(start, LocalDateTime.now()).toMillis() / (values.size() / 10_000));
  }

  private void saveComponents(List<ConnectedComponent> components) {
    componentRepository.save(components);
  }


  public void update(ValidationResult validationResult) {
    //todo
  }

  public Map<Long, MarkedNode> getAdjacencyList(Integer regionId) {
    //todo
    return null;//todo
  }

  public boolean isReadyInnerValidation(Region region) {
    return componentRepository.isReadyInnerValidation(region);
  }
}
