package org.antisida.osm.validator.connectivity;

import org.antisida.osm.validator.connectivity.repository.DbInitializer;

public class Main {

  //mvn clean compile assembly:single
  public static void main(String[] args) {
    DbInitializer.initDb();

    MainValidator mainValidator = new MainValidator();
    mainValidator.validate();

  }

}
