package org.antisida.osm.validator.connectivity.utils;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;

import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;


class FileUtilsTest {

  FileUtils fileUtils = new FileUtils();

  @Test
  void getWorkPath() {
    Path workPath = FileUtils.getWorkPath();
    assertThat(workPath.toString()).hasToString("");
  }

  @Test
  void readPropertyFile() {
    Path path = Path.of("src/test/resource/prop.txt");

    List<String> regions = fileUtils.readPropertyFile(path);

    assertThat(regions).containsExactlyElementsOf(List.of("GE.OM5", "FD.sd")).hasSize(2);
  }

  @Test
  void getRegionFromPropFile() {
    List<String> regionFromPropFile = fileUtils.getRegionFromPropFile();

    assertThat(regionFromPropFile).containsExactlyElementsOf(List.of("GE.OM5", "FD.sd")).hasSize(2);
  }
}