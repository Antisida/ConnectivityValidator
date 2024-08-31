package org.antisida.osm.validator;

import java.util.List;

@FunctionalInterface
public interface Validator {

  String validate(List<String> filePath);

}
