package org.antisida.osm.validator.connectivity.model;

import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

public record ValidationResult(
    Map<Long, MarkedNode> adjacencyList,
    List<ConnectedComponent> components,
    int regionId
) { }
