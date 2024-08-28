package org.antisida.osm.validator.connectivity;

import java.util.List;
import org.antisida.osm.validator.connectivity.repository.H2Connector;
import org.antisida.osm.validator.connectivity.utils.FileUtils;
import org.antisida.osm.validator.connectivity.utils.OM5Utils;
import org.antisida.osm.validator.connectivity.validator.ConnectivityUtils;
import org.antisida.osm.validator.connectivity.validator.ConnectivityValidator;
import org.antisida.osm.validator.connectivity.repository.Repository;

public class MainValidator {

  private final FileUtils fileUtils = new FileUtils();
  private final ConnectivityUtils connectivityUtils = new ConnectivityUtils();
  private final OM5Utils om5Utils = new OM5Utils();
  private final Repository repository = new Repository();
  private final ConnectivityValidator connectivityValidator = new ConnectivityValidator(connectivityUtils, fileUtils, om5Utils, repository);


  public void validate() {
    List<String> executedRegions = fileUtils.getRegionFromPropFile();
    executedRegions.forEach(connectivityValidator::validate);

  }
}
