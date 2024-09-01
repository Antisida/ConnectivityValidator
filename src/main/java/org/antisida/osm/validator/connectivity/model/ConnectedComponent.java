package org.antisida.osm.validator.connectivity.model;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
public final class ConnectedComponent {

  private final UUID id;
  private final int regionId;
//  private final int size;
  private boolean isolated;

//  public boolean isIsolated() { return size < 100_000; }
}
