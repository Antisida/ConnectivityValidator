package org.antisida.osm.validator.connectivity.validator;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.alex73.osmemory.OsmWay;
import org.antisida.osm.validator.connectivity.model.ConnectedComponent;
import org.antisida.osm.validator.connectivity.model.MarkedNode;
import org.antisida.osm.validator.connectivity.model.ValidationResult;

@Slf4j
public class ConnectivityUtils {


  public Map<Long, MarkedNode> createAdjacencyList(List<OsmWay> osmWays) {

    Map<Long, MarkedNode> commonAdjMap = new HashMap<>();

    for (OsmWay osmWay : osmWays) {
      Map<Long, MarkedNode> adjacencyMapForWay = createAdjacencyListForOneWay(osmWay);
      for (MarkedNode markedNode : adjacencyMapForWay.values()) {
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

    log.info("MarkedNode count: {}", commonAdjMap.size());
    return commonAdjMap;
  }

  /**
   * Формирует HashMap, где ключ - id точки, а значение MarkedNode.
   *
   * @param osmWay вей
   * @return Сгруппированные в HashMap{Long, MarkedNode} все точки вея, <br> где long = id точки,
   * MarkedNode = сама точка с заполненными соседними точками
   */

  private Map<Long, MarkedNode> createAdjacencyListForOneWay(OsmWay osmWay) {
    //long = id точки, MarkedNode = сама точка с заполненными соседними точками
    Map<Long, MarkedNode> adjMapForWay = new HashMap<>();
    if (osmWay.getNodeIds().length < 2) {
      throw new IllegalArgumentException("Way " + osmWay.getId() + " has only 1 node!");//todo
    }

    long[] nodeIds = osmWay.getNodeIds();
    int i = 0;

    while (i < nodeIds.length) {
      long[] neighborNodeIds = this.getNeighborNodeIds(nodeIds, i);
      // merge потому что веи могут пересекать сами себя, и одна точка может входить несколько раз в один вей
      adjMapForWay.merge(
          nodeIds[i],
          new MarkedNode(nodeIds[i], osmWay.getId(), neighborNodeIds),
          (oldValue, newValue) -> {
            oldValue.addNeighborNodeIds(newValue.getNeighborNodeIds());
            return oldValue;
          });
      i++;
    }
    return adjMapForWay;
  }

  private long[] getNeighborNodeIds(long[] nodeIds, int i) {
    // если точка не первая и не последняя в вее
    if (i > 0 && i != nodeIds.length - 1) {
      return new long[]{nodeIds[i - 1], nodeIds[i + 1]};
    }
    // если первая точка в вее
    if (i == 0) {
      return new long[]{nodeIds[1]};
    }
    // если последняя точка в вее
    if (i == nodeIds.length - 1) {
      return new long[]{nodeIds[nodeIds.length - 2]};
    }
    throw new IllegalArgumentException(
        "Ошибка определения соседних точек: индекс массива меньше 0");
  }

  //mark nodes by component via dfs
  public ValidationResult markNodesAndComputeComponents(Map<Long, MarkedNode> adjacencyList) {
    List<ConnectedComponent> connectedComponents = new ArrayList<>();

    Collection<MarkedNode> adjacencyListValues = adjacencyList.values();

    // пока не останется ни одной не посещенной ноды
    for (MarkedNode markedNode : adjacencyListValues) {
      if (markedNode.isVisited()) {
        continue;
      }
      UUID componentId = UUID.randomUUID();

      markedNode.setVisited(true);
      markedNode.setConnectedComponentId(componentId);

      int nodeCount = 1;

      // стек непосещенных нод формируется из соседних точек
      Deque<Long> notVisitedStack = new ArrayDeque<>();
      for (long nodeId : markedNode.getNeighborNodeIds()) {
        if (!adjacencyList.get(nodeId).isVisited()) {
          notVisitedStack.push(nodeId);
        }
      }

      while (!notVisitedStack.isEmpty()) {
        MarkedNode nodeFromStack = adjacencyList.get(notVisitedStack.pop());
        nodeFromStack.setVisited(true);
        nodeFromStack.setConnectedComponentId(componentId);
        nodeCount++;
        for (long neighbour : nodeFromStack.getNeighborNodeIds()) {
          if (!adjacencyList.get(neighbour).isVisited()) {
            notVisitedStack.push(neighbour);
          }
        }
      }

      connectedComponents.add(new ConnectedComponent(componentId, 1000 < nodeCount));
    }

    if (adjacencyListValues.stream().noneMatch(MarkedNode::isVisited)) {
      log.error("Exist not visited MarkedNode!");
    }

    return new ValidationResult(adjacencyList, connectedComponents);
  }

}
