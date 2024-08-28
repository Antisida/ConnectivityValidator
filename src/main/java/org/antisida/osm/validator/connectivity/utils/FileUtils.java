package org.antisida.osm.validator.connectivity.utils;


import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.alex73.osmemory.MemoryStorage;
import org.alex73.osmemory.O5MReader;
import org.antisida.osm.validator.connectivity.Main;

@Slf4j
public class FileUtils {

  public MemoryStorage readOM5File(String path) {
    String pathString = getWorkPath().toString() + "\\" + path;
    try {
      return new O5MReader().read(new File(pathString));
    } catch (Exception e) {
      log.error("FФайл .om5 не найден: {}", pathString);
      throw new RuntimeException("Файл .om5 не найден: " + pathString);
    }
  }

  @SneakyThrows
  public static Path getWorkPath() {
    URL url = Main.class.getProtectionDomain().getCodeSource().getLocation();
    Path parent = Paths.get(url.toURI()).getParent();
    log.info("Work dir: {}", parent);
    return parent;
  }

  @SneakyThrows
  public List<Path> findFiles() {
    try (Stream<Path> stream = Files.walk(getWorkPath())) {
      return stream
          .filter(Files::isRegularFile)
          .filter(f -> f.toString().endsWith(".txt"))
          .toList();
    }
  }

  public List<String> getRegionFromPropFile() {
    return readPropertyFile(Path.of(getWorkPath().toString() + "\\" + "prop.txt"));
  }

  @SneakyThrows
  public List<String> readPropertyFile(Path path) {
    List<String> strings = Files.readAllLines(path);
    StringBuilder sb = new StringBuilder();
    strings.forEach(sb::append);
    String[] split = sb.toString()
        .replace(" ", "")
        .split(",");
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

    InputStream resourceAsStream = ClassLoader.getSystemClassLoader()
        .getResourceAsStream(path.toString().replace("\\", "/"));
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
          .lines().collect(Collectors.joining("\n"));
//    System.out.println(result);
//      return result;
    }
  }
}
