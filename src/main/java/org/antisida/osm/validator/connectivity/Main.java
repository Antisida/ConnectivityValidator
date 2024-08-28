package org.antisida.osm.validator.connectivity;

import java.time.Duration;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.antisida.osm.validator.connectivity.repository.DbInitializer;

@Slf4j
public class Main {

  //mvn clean compile assembly:single
  public static void main(String[] args) {
    LocalDateTime start = LocalDateTime.now();
    DbInitializer.initDb();

    MainValidator mainValidator = new MainValidator();
    mainValidator.validate();
    log.info("Finish: {} mill", Duration.between(start, LocalDateTime.now()).toMillis());

  }

}
