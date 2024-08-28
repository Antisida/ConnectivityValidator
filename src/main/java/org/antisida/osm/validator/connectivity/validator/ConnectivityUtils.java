package org.antisida.osm.validator.connectivity.validator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Stack;
import java.util.UUID;
import org.alex73.osmemory.OsmWay;
import org.antisida.osm.validator.connectivity.model.ConnectedComponent;
import org.antisida.osm.validator.connectivity.model.MarkedNode;
import org.antisida.osm.validator.connectivity.model.ValidationResult;

public class ConnectivityUtils {


  public HashMap<Long, MarkedNode> createAdjacencyList(List<OsmWay> osmWays) {

    HashMap<Long, MarkedNode> commonAdjMap = new HashMap<>();

    for (OsmWay osmWay : osmWays) {
      HashMap<Long, MarkedNode> adjacencyMapForWay = createAdjacencyListForOneWay(osmWay);
      for (Long nodeId : adjacencyMapForWay.keySet()) {
        commonAdjMap.merge(
            nodeId,
            adjacencyMapForWay.get(nodeId),
            (oldValue, newValue) -> {
              //объединяем wayId в один массив
              oldValue.addWayIds(newValue.getOsmWayIds());
              //объединяем соседей в один массив
              oldValue.addNeighborNodeIds(newValue.getNeighborNodeIds());
              return oldValue;
            });
      }
    }

    //    System.out.println("Количество узлов в списке связности: " + commonAdjMap.size());
    return commonAdjMap;
  }

  /**
   * Формирует HashMap, где ключ - id точки, а значение MarkedNode.
   *
   * @param osmWay вей
   * @return Сгруппированные в HashMap{Long, MarkedNode} все точки вея, <br> где long = id точки,
   * MarkedNode = сама точка с заполненными соседними точками
   */

  private HashMap<Long, MarkedNode> createAdjacencyListForOneWay(OsmWay osmWay) {
    //long = id точки, MarkedNode = сама точка с заполненными соседними точками
    HashMap<Long, MarkedNode> adjMapForWay = new HashMap<>();
    if (osmWay.getNodeIds().length < 2) {
      throw new IllegalArgumentException("Вей " + osmWay.getId() + " имеет только 1 точку!");//todo
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
    if (i > 0 & i < nodeIds.length - 1) {
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
  public ValidationResult markNodesAndComputeComponents(HashMap<Long, MarkedNode> adjacencyList) {
    List<ConnectedComponent> connectedComponents = new ArrayList<>();


    // пока не останется ни одной не посещенной ноды
    while (adjacencyList.values().stream().anyMatch(n -> !n.isVisited())) {
      UUID componentId = UUID.randomUUID();

      // find first node !isVisited()
      Optional<MarkedNode> notVisitedMarkedNode = adjacencyList.values()
          .stream()
          .filter(n -> !n.isVisited())
          .findFirst();

      if (notVisitedMarkedNode.isPresent()) {
        MarkedNode markedNode = notVisitedMarkedNode.get();
        markedNode.setVisited(true);
        markedNode.setConnectedComponentId(componentId);

        int nodeCount = 1;

        // стек непосещенных нод формируется из соседних точек
        Stack<Long> notVisitedNodes = new Stack<>();
        for (long nodeId : markedNode.getNeighborNodeIds()) {
          if (!adjacencyList.get(nodeId).isVisited()) { //fixme долгая операция
            notVisitedNodes.push(nodeId);
          }
        }

        while (!notVisitedNodes.empty()) {
          MarkedNode nodeFromStack = adjacencyList.get(notVisitedNodes.pop());
          nodeFromStack.setVisited(true);
          nodeFromStack.setConnectedComponentId(componentId);
          nodeCount++;
          for (long neighbour : nodeFromStack.getNeighborNodeIds()) {
            if (!adjacencyList.get(neighbour).isVisited()) {
              notVisitedNodes.push(neighbour);
            }
          }
        }

        connectedComponents.add(
            new ConnectedComponent(componentId, 1000 < nodeCount));
      }
    }

    System.out.println("Количество ConnectedComponent: " + connectedComponents.size());
    return new ValidationResult(adjacencyList, connectedComponents);
  }

}
