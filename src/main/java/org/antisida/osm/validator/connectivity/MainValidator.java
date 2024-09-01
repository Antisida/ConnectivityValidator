package org.antisida.osm.validator.connectivity;

import java.util.ArrayList;
import java.util.List;
import org.antisida.osm.validator.Validator;
import org.antisida.osm.validator.connectivity.service.ConnectivityResultService;
import org.antisida.osm.validator.connectivity.service.RegionService;
import org.antisida.osm.validator.connectivity.service.WayService;
import org.antisida.osm.validator.connectivity.utils.FileUtils;
import org.antisida.osm.validator.connectivity.utils.OM5Utils;
import org.antisida.osm.validator.connectivity.validator.ConnectivityUtils;
import org.antisida.osm.validator.connectivity.validator.ConnectivityValidator;

public class MainValidator {

  private final FileUtils fileUtils = new FileUtils();
  private final OM5Utils om5Utils = new OM5Utils();

  private final List<Validator> validators = new ArrayList<>();

  public MainValidator() {
    ConnectivityValidator connectivityValidator = createConnectivityValidator();
    validators.add(connectivityValidator);
  }

  public void validate() {
    List<String> forValidateFileNames = fileUtils.getRegionFromPropFile();
    validators.forEach(validator -> validator.validate(forValidateFileNames));
  }

  private ConnectivityValidator createConnectivityValidator() {
    return new ConnectivityValidator(new ConnectivityUtils(),
                                     fileUtils,
                                     om5Utils,
                                     RegionService.getInstance(),
                                     WayService.getInstance(),
                                     ConnectivityResultService.getInstance());
  }
}
