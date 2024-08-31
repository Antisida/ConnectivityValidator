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
    LocalDateTime start1 = LocalDateTime.now();
    MainValidator mainValidator = new MainValidator();
    mainValidator.validate();
    log.info("mainValidator.validate(): {} mill", Duration.between(start1, LocalDateTime.now()).toMillis());
    log.info("Global: {} s", Duration.between(start, LocalDateTime.now()).toSeconds());

  }

}
