package org.antisida.osm.validator.connectivity.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Way {

//  private final int id;
  private final long osmId;
  private final Node[] node;

  private boolean isolated;
}
