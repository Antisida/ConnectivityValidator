package org.antisida.osm.validator.connectivity.model;

import java.util.UUID;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public final class ConnectedComponent {

  private final UUID id;
  private final int regionId;
  private final int size;

  public boolean isIsolated() { return size < 100_000; }
}
