package org.antisida.osm.validator.connectivity.validator;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.alex73.osmemory.OsmWay;
import org.antisida.osm.validator.connectivity.model.ConnectedComponent;
import org.antisida.osm.validator.connectivity.model.MarkedNode;
import org.antisida.osm.validator.connectivity.model.ValidationResult;

@Slf4j
public class ConnectivityUtils {

  public Map<Long, MarkedNode> createAdjacencyList(List<OsmWay> osmWays) {
    LocalDateTime start = LocalDateTime.now();
    Map<Long, MarkedNode> commonAdjMap = new HashMap<>();

    for (OsmWay osmWay : osmWays) {
      Map<Long, MarkedNode> adjacencyMapForOneWay = createAdjacencyListForOneWay(osmWay);
      for (MarkedNode markedNode : adjacencyMapForOneWay.values()) {
        commonAdjMap.merge(
            markedNode.getOsmId(),
            markedNode,
            (oldValue, newValue) -> {
              //объединяем wayId в один массив
              oldValue.addWayIds(newValue.getOsmWayIds());
              //объединяем соседей в один массив
              oldValue.addNeighborNodeIds(newValue.getNeighborNodeIds());
              return oldValue;
            });
      }

// todo почему-то  это работает быстрее
//      for (Long nodeId : adjacencyMapForWay.keySet()) {
//        commonAdjMap.merge(
//            nodeId,
//            adjacencyMapForWay.get(nodeId),
//            (oldValue, newValue) -> {
//              //объединяем wayId в один массив
//              oldValue.addWayIds(newValue.getOsmWayIds());
//              //объединяем соседей в один массив
//              oldValue.addNeighborNodeIds(newValue.getNeighborNodeIds());
//              return oldValue;
//            });
//      }
    }
    log.info("createAdjacencyList() duration: {}", Duration.between(start, LocalDateTime.now()).toMillis());
    log.info("MarkedNode count: {}", commonAdjMap.size());
    return commonAdjMap;
  }

  /**
   * Формирует HashMap, где ключ - id точки, а значение MarkedNode.
   *
   * @param osmWay вей
   * @return Сгруппированные в HashMap{Long, MarkedNode} все точки вея, <br> где long = id точки, MarkedNode = сама
   * точка с заполненными соседними точками
   */

  private Map<Long, MarkedNode> createAdjacencyListForOneWay(OsmWay osmWay) {
    //long = id точки, MarkedNode = сама точка с заполненными соседними точками
    Map<Long, MarkedNode> adjMapForOneWay = new HashMap<>();
    if (osmWay.getNodeIds().length < 2) {
      throw new IllegalArgumentException("Way " + osmWay.getId() + " has only 1 node!");//todo
    }

    long[] nodeIds = osmWay.getNodeIds();
    int i = 0;

    while (i < nodeIds.length) {
      long[] neighborNodeIds = this.getNeighborNodeIds(nodeIds, i);
      // merge потому что веи могут пересекать сами себя, и одна точка может входить несколько раз в один вей
      adjMapForOneWay.merge(nodeIds[i],
                            new MarkedNode(nodeIds[i], osmWay.getId(), neighborNodeIds),
                            (oldValue, newValue) -> {
                              oldValue.addNeighborNodeIds(newValue.getNeighborNodeIds());
                              return oldValue;
                            });
      i++;
    }
    return adjMapForOneWay;
  }

  private long[] getNeighborNodeIds(long[] nodeIds, int i) {
    // если точка не первая и не последняя в вее
    if (i > 0 && i != nodeIds.length - 1) return new long[]{nodeIds[i - 1], nodeIds[i + 1]};
    // если первая точка в вее
    if (i == 0) return new long[]{nodeIds[1]};
    // если последняя точка в вее
    if (i == nodeIds.length - 1) return new long[]{nodeIds[nodeIds.length - 2]};
    throw new IllegalArgumentException("Error in getNeighborNodeIds(): array index < 0");
  }

//  /**
//   * Set component`s UUID to markedNode.
//   *
//   * @param adjacencyList
//   * @return
//   */
  //mark nodes by component via dfs
  //fixme работает быстрее
  public ValidationResult markComponents(Map<Long, MarkedNode> adjacencyList, int regionId) {
    LocalDateTime start = LocalDateTime.now();
    List<ConnectedComponent> components = new ArrayList<>();

    Collection<MarkedNode> adjacencyListValues = adjacencyList.values();

    // пока не останется ни одной не посещенной ноды
    for (MarkedNode markedNode : adjacencyListValues) {
      if (markedNode.notVisited()) {
        UUID componentId = UUID.randomUUID();

        markedNode.setVisited(true);
        markedNode.setConnectedComponentId(componentId);

        int componentSize = 1;

        // стек непосещенных нод формируется из соседних точек
        Deque<Long> notVisitedStack = new ArrayDeque<>();
        for (long nodeId : markedNode.getNeighborNodeIds()) {
          if (adjacencyList.get(nodeId).notVisited()) {
            notVisitedStack.push(nodeId);
          }
        }

        while (!notVisitedStack.isEmpty()) {
          MarkedNode nodeFromStack = adjacencyList.get(notVisitedStack.pop());
          nodeFromStack.setVisited(true);
          nodeFromStack.setConnectedComponentId(componentId);
          componentSize++;
          for (long neighbourId : nodeFromStack.getNeighborNodeIds()) {
            if (adjacencyList.get(neighbourId).notVisited()) {
              notVisitedStack.push(neighbourId);
            }
          }
        }
        components.add(new ConnectedComponent(componentId, regionId, componentSize));
      }
    }
    log.info("markComponents() duration: {}", Duration.between(start, LocalDateTime.now()).toMillis());
//    if (adjacencyListValues.stream().noneMatch(MarkedNode::isVisited)) {
//      log.error("Exist not visited MarkedNode!");
//    }

    return new ValidationResult(adjacencyList, components, components.getFirst().getRegionId());
  }



  /*public void validateTwoAdjList(Map<Long, MarkedNode> oneAdjacencyList,
                                 Map<Long, MarkedNode> thoAdjacencyList) {

    //1. сет id всех изолированных компонентов региона//todo
    Set<UUID> oneIsolatedComponentIds = oneAdjacencyList.getIsolatedComponentIds();
    Set<UUID> thoIsolatedComponentIds = thoAdjacencyList.getIsolatedComponentIds();

    Collection<MarkedNode> markedNods = oneAdjacencyList.values();

    //2.сет id изолированных компонент региона с учетом соседа
    Set<UUID> resultIsolatedComponentIds = markedNods.stream()
        //все изолированные ноды
        .filter(markedNode -> oneIsolatedComponentIds.contains(markedNode.getConnectedComponentId()))
        //если нода есть у соседа, и если у соседа она не входит в сет изолированных компонент
        .filter(markedNode ->
                    thoAdjacencyList.containsKey(markedNode.getOsmId())
                        && !thoIsolatedComponentIds.contains(thoAdjacencyList.get(markedNode.getOsmId())
                                                                 .getConnectedComponentId()))
        //получаем id всех изолированных компонент
        .collect(Collectors.groupingBy(MarkedNode::getConnectedComponentId))
        .keySet();

//    Set<Integer> filteredRegionIsolatedComponentIds = regionAdjList.getAllMarkedNodes()
//        .stream()
//        //если нода входит в состав изолированных компонент графа региона
//        .filter(markedNode -> regionIsolatedComponentIds.contains(markedNode.getConnectedComponentId()))
//        //если нода есть у соседа, и если у соседа она не входит в сет изолированных компонент у соседа
//        .filter(markedNode -> neighborAdjList.containsMarkedNode(markedNode.getId())
//            && !neighborAdjList.getIsolatedComponentIds().contains(
//            neighborAdjList.getMarkedNode(markedNode.getId()).getConnectedComponentId()))
//        //получаем айдишники всех изолированных компонент
//        .collect(Collectors.groupingBy(MarkedNode::getConnectedComponentId))
//        .keySet();

    //изменение компонентов графа: если компонент приконнекчен к соседу, то помечаем его как неизолированный
    List<ConnectedComponent> regionConnectedComponents = regionAdjList.getConnectedComponents();
    for (ConnectedComponent connectedComponent : regionConnectedComponents) {
      if (resultIsolatedComponentIds.contains(connectedComponent.getId())) {
        System.out.println("Граф " + connectedComponent.getId() + " setIsolated(false)");
        connectedComponent.setIsolated(false);
      }
    }

  }*/


  /*public ValidationResult markComponents(Map<Long, MarkedNode> adjacencyList) {
  LocalDateTime start = LocalDateTime.now();
    List<ConnectedComponent> components = new ArrayList<>();

    Collection<MarkedNode> adjacencyListValues = adjacencyList.values();

    Set<Long> visitedNodeIds = new HashSet<>();
    // пока не останется ни одной не посещенной ноды
    for (MarkedNode markedNode : adjacencyListValues) {
      if (!visitedNodeIds.contains(markedNode.getOsmId())) {
        visitedNodeIds.add(markedNode.getOsmId());

        UUID componentId = UUID.randomUUID();

        markedNode.setConnectedComponentId(componentId);

        int componentSize = 1;

        // стек непосещенных нод формируется из соседних точек
        Deque<Long> notVisitedStack = new ArrayDeque<>();
        for (long nodeId : markedNode.getNeighborNodeIds()) {
          if (!visitedNodeIds.contains(nodeId)) {
            notVisitedStack.push(nodeId);
          }
        }

        while (!notVisitedStack.isEmpty()) {
          MarkedNode nodeFromStack = adjacencyList.get(notVisitedStack.pop());
          visitedNodeIds.add(nodeFromStack.getOsmId());
          nodeFromStack.setConnectedComponentId(componentId);
          componentSize++;
          for (long neighbourId : nodeFromStack.getNeighborNodeIds()) {
            if (!visitedNodeIds.contains(neighbourId)) {
              notVisitedStack.push(neighbourId);
            }
          }
        }
        components.add(new ConnectedComponent(componentId, componentSize));
      }
    }
    log.warn("markComponents2() duration: {}", Duration.between(start, LocalDateTime.now()).toMillis());
    if (adjacencyListValues.size() != visitedNodeIds.size()) {
      log.error("Exist not visited MarkedNode!");
    }

    return new ValidationResult(adjacencyList, components);
  }*/
}
