package org.antisida.osm.validator.connectivity.utils;


import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.alex73.osmemory.MemoryStorage;
import org.alex73.osmemory.O5MReader;
import org.antisida.osm.validator.BenchUtils;
import org.antisida.osm.validator.connectivity.Main;
import org.antisida.osm.validator.connectivity.model.Region;

@Slf4j
public class FileUtils {

  @SneakyThrows
  public static boolean isExistO5mFile(Region region) {
    try (Stream<Path> stream = Files.walk(getWorkPath())) {
      boolean isExistFile = stream.filter(Files::isRegularFile)
          .anyMatch(f -> f.toString().equals(region.path()));
      if (!isExistFile)
        log.info("File {} not found. Validation near the border of this region will be incomplete.",
                 getWorkPath() + "\\" + region.path());
      return isExistFile;
    }
  }

  public MemoryStorage readOM5File(String path) {
    LocalDateTime start = LocalDateTime.now();
    String pathString = getWorkPath().toString() + "\\" + path;
    try {
      MemoryStorage read = new O5MReader().read(new File(pathString));
      log.info("{}: {} mill", BenchUtils.calledMethod(), Duration.between(start, LocalDateTime.now()).toMillis());
      return read;
    } catch (Exception e) {
      log.error("File *.om5 not found: {}", pathString);
      throw new RuntimeException("File *.om5 not found: " + pathString);
    }
  }

  @SneakyThrows
  public static Path getWorkPath() {
    URL url = Main.class.getProtectionDomain().getCodeSource().getLocation();
    Path parent = Paths.get(url.toURI()).getParent();
    log.info("Work dir: {}", parent); //fixme del
    return parent;
  }

  @SneakyThrows
  public List<Path> findFiles() {
    try (Stream<Path> stream = Files.walk(getWorkPath())) {
      return stream.filter(Files::isRegularFile)
          .filter(f -> f.toString().endsWith(".txt"))
          .toList();
    }
  }

  public List<String> getRegionFromPropFile() {
    LocalDateTime start = LocalDateTime.now();
    List<String> strings = readPropertyFile(Path.of(getWorkPath().toString() + "\\" + "prop.txt"));
    log.info("{}: {} mill", BenchUtils.calledMethod(), Duration.between(start, LocalDateTime.now()).toMillis());
    return strings;
  }

  @SneakyThrows
  public List<String> readPropertyFile(Path path) {
    List<String> strings = Files.readAllLines(path);
    StringBuilder sb = new StringBuilder();
    strings.forEach(sb::append);
    String[] split = sb.toString().replace(" ", "").split(",");
    return Stream.of(split).distinct().toList();
  }

  @SneakyThrows
  public static String readFileAsString(Path path) {
//    URL url = Main.class.getResource("META-INF/" + path.toString().replace("\\", "/"));
//    URL url = Main.class.getResource("/META-INF/src/main/resource/db/h2/init-tables.sql");
//    System.out.println("classpath is: " + System.getProperty("java.class.path"));
//    System.out.println(Files.readString(Path.of(url.toURI())));
//    log.info("url: {}", url);
//    log.info("META-INF\\" + path.toString());
//    URL resource = ClassLoader.getSystemClassLoader().getResource(path.toString());

    InputStream resourceAsStream =
        ClassLoader.getSystemClassLoader().getResourceAsStream(path.toString().replace("\\", "/"));
//        .getResourceAsStream("META-INF/db/h2/init-tables.sql");
//        .getResourceAsStream(("src/" + path).replace("\\", "/"));
//        .getResourceAsStream("META-INF/db/h2/init-tables.sql".replace("/", "\\"));

//        .getResourceAsStream("META-INF/src/main/resource/db/h2/init-tables.sql");
    if (resourceAsStream == null) {
      return Files.readString(Path.of("src/" + path));
//      resourceAsStream = ClassLoader.getSystemClassLoader()
//          .getResourceAsStream(path.toString().replace("\\", "/"));

    } else {
      return new BufferedReader(new InputStreamReader(resourceAsStream))
          .lines()
          .collect(Collectors.joining("\n"));
//    System.out.println(result);
//      return result;
    }
  }
}
