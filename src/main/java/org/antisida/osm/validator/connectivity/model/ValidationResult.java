package org.antisida.osm.validator.connectivity.model;

import java.util.List;
import java.util.Map;

public record ValidationResult(
    Map<Long, MarkedNode> adjacencyList,
    List<ConnectedComponent> components
    // FIXME: 28.08.2024 Добавить регион
) {

}
