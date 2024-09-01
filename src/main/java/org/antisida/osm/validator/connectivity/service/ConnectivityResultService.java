package org.antisida.osm.validator.connectivity.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
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

  public void setNotIsolated(List<UUID> uuids) {
    log.info("Set ISOLATED=FALSE for: {}", uuids);
    componentRepository.setIsolated(uuids, false);
  }

  public List<ConnectedComponent> getIsolatedComponentsRegionId(Integer regionId) {
    return componentRepository.getIsolatedComponents(regionId);
  }

  public Map<Long, MarkedNode> getIsolatedAdjacencyList(Integer regionId) {
    LocalDateTime start = LocalDateTime.now();
    Map<Long, MarkedNode> collect = Optional.of(regionId)
        .map(componentRepository::getIsolatedByRegionId)
        .stream()
        .flatMap(Collection::stream)
        .map(ConnectedComponent::getId)
        .map(nodeRepository::getByComponentId)
        .flatMap(Collection::stream)
        .collect(Collectors.toMap(MarkedNode::getOsmId, markedNode -> markedNode));
    log.info("{}: {} mill", BenchUtils.calledMethod(), Duration.between(start, LocalDateTime.now()).toMillis());
    log.info("{}: {} mill per 10 000 nodes", BenchUtils.calledMethod(),
             Duration.between(start, LocalDateTime.now()).toMillis() / (collect.size() / 10_000));
    return collect;
  }

  public Map<Long, MarkedNode> getAdjacencyList(Integer regionId) {
    LocalDateTime start = LocalDateTime.now();
    Map<Long, MarkedNode> collect = Optional.of(regionId)
        .map(componentRepository::getByRegionId)
        .stream()
        .flatMap(Collection::stream)
        .map(ConnectedComponent::getId)
        .map(nodeRepository::getByComponentId)
        .flatMap(Collection::stream)
        .collect(Collectors.toMap(MarkedNode::getOsmId, markedNode -> markedNode));
    log.info("{}: {} mill", BenchUtils.calledMethod(), Duration.between(start, LocalDateTime.now()).toMillis());
    log.info("{}: {} mill per 10 000 nodes", BenchUtils.calledMethod(),
             Duration.between(start, LocalDateTime.now()).toMillis() / (collect.size() / 10_000));
    return collect;
  }

  public boolean isReadyInnerValidation(Region region) {
    return componentRepository.isReadyInnerValidation(region.id());
  }

  public List<MarkedNode> getIsolatedNodes(int regionId) {
    LocalDateTime start = LocalDateTime.now();
    List<MarkedNode> collect = Optional.of(regionId)
        .map(componentRepository::getIsolatedByRegionId)
        .stream()
        .flatMap(Collection::stream)
        .map(ConnectedComponent::getId)
        .map(nodeRepository::getByComponentId)
        .flatMap(Collection::stream)
        .toList();
    log.info("{}: {} mill", BenchUtils.calledMethod(), Duration.between(start, LocalDateTime.now()).toMillis());
    log.info("{}: {} mill per 10 000 nodes", BenchUtils.calledMethod(),
             Duration.between(start, LocalDateTime.now()).toMillis() / (collect.size() / 10_000));
    return collect;
  }

  public List<MarkedNode> getNotIsolatedNodes(Integer regionId) {
    LocalDateTime start = LocalDateTime.now();
    List<MarkedNode> collect = Optional.of(regionId)
        .map(componentRepository::getNotIsolatedComponents)
        .stream()
        .flatMap(Collection::stream)
        .map(ConnectedComponent::getId)
        .map(nodeRepository::getByComponentId)
        .flatMap(Collection::stream)
        .toList();
    log.info("{}: {} mill", BenchUtils.calledMethod(), Duration.between(start, LocalDateTime.now()).toMillis());
    log.info("{}: {} mill per 10 000 nodes", BenchUtils.calledMethod(),
             Duration.between(start, LocalDateTime.now()).toMillis() / (collect.size() / 10_000));
    return collect;
  }

  public Set<Long> getIsolatedNodeIds(int regionId) {
    LocalDateTime start = LocalDateTime.now();
    Set<Long> collect = Optional.of(regionId)
        .map(componentRepository::getIsolatedComponents)
        .stream()
        .flatMap(Collection::stream)
        .map(ConnectedComponent::getId)
        .map(nodeRepository::getNodeIdsByComponentId)
        .flatMap(Collection::stream)
        .collect(Collectors.toSet());
    log.info("{}: {} mill", BenchUtils.calledMethod(), Duration.between(start, LocalDateTime.now()).toMillis());
    log.info("{}: {} mill per 10 000 nodes", BenchUtils.calledMethod(),
             Duration.between(start, LocalDateTime.now()).toMillis() / (collect.size() / 10_000));
    return collect;
  }

  public Set<Long> getNotIsolatedNodeIds(int regionId) {
    LocalDateTime start = LocalDateTime.now();
    Set<Long> collect = Optional.of(regionId)
        .map(componentRepository::getNotIsolatedComponents)
        .stream()
        .flatMap(Collection::stream)
        .map(ConnectedComponent::getId)
        .map(nodeRepository::getNodeIdsByComponentId)
        .flatMap(Collection::stream)
        .collect(Collectors.toSet());
    log.info("{}: {} mill", BenchUtils.calledMethod(), Duration.between(start, LocalDateTime.now()).toMillis());
    log.info("{}: {} mill per 10 000 nodes", BenchUtils.calledMethod(),
             Duration.between(start, LocalDateTime.now()).toMillis() / (collect.size() / 10_000));
    return collect;
  }
}
