package org.antisida.osm.validator.connectivity.model;

import java.util.UUID;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public final class ConnectedComponent {
  private final UUID id;
  private int regionId;
  private final boolean isolated;
}
