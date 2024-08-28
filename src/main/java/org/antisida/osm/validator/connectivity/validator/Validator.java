package org.antisida.osm.validator.connectivity.validator;

@FunctionalInterface
public interface Validator {

  String validate(String filePath);

}
