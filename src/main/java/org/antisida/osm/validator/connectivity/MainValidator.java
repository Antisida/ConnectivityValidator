package org.antisida.osm.validator.connectivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.antisida.osm.validator.Validator;
import org.antisida.osm.validator.connectivity.repository.RegionRepository;
import org.antisida.osm.validator.connectivity.service.ConnectivityResultService;
import org.antisida.osm.validator.connectivity.service.RegionService;
import org.antisida.osm.validator.connectivity.utils.FileUtils;
import org.antisida.osm.validator.connectivity.utils.OM5Utils;
import org.antisida.osm.validator.connectivity.validator.ConnectivityUtils;
import org.antisida.osm.validator.connectivity.validator.ConnectivityValidator;
import org.antisida.osm.validator.connectivity.repository.Repository;

public class MainValidator {

  private final FileUtils fileUtils = new FileUtils();
  private final OM5Utils om5Utils = new OM5Utils();
  private final Repository repository = new Repository();
  private final RegionService regionService = RegionService.getInstance();

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
                                     repository,
                                     regionService,
                                     ConnectivityResultService.getInstance());
  }
}
