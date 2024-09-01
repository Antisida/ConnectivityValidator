package org.antisida.osm.validator.connectivity.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.antisida.osm.validator.connectivity.model.Region;
import org.antisida.osm.validator.connectivity.utils.ArrayUtils;

@Slf4j
public class RegionRepository {

  private static volatile RegionRepository instance;

  private RegionRepository() { }

  public static RegionRepository getInstance() {
    RegionRepository localInstance = instance;
    if (localInstance == null) {
      synchronized (RegionRepository.class) {
        localInstance = instance;
        if (localInstance == null) {
          instance = localInstance = new RegionRepository();
        }
      }
    }
    return localInstance;
  }

  private static final String REGION_WHERE_PATH_IN = "SELECT * FROM regions WHERE path IN (%s)";

  public List<Region> getAllByFileNameIn(List<String> fileNames) {
    List<Region> regions = new ArrayList<>();
    String sql = REGION_WHERE_PATH_IN.formatted(preparePlaceHolders(fileNames.size()));

    try (Connection connection = H2Connector.getConnection();
         PreparedStatement ps = connection.prepareStatement(sql)) {
      setValues(ps, fileNames);

      try (ResultSet resultSet = ps.executeQuery()) {
        while (resultSet.next()) {
          regions.add(mapRegion(resultSet));
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
    return regions;
  }

  private Region mapRegion(ResultSet resultSet) throws SQLException {
    Object[] rsArray = (Object[]) resultSet.getArray("neighbors").getArray();
    int[] neighborIds = Arrays.stream(rsArray).mapToInt(o -> (int) o).toArray();
    return new Region(
        resultSet.getInt("id"),
        resultSet.getString("name"),
        neighborIds,
        resultSet.getString("path")
    );
  }

  private static final String REGION_WHERE_ID_IN = "SELECT * FROM regions WHERE id IN (%s)";

  public List<Region> getRegionsIn(List<Integer> regionIds) {
    List<Region> regions = new ArrayList<>();
    String sql = REGION_WHERE_ID_IN.formatted(preparePlaceHolders(regionIds.size()));

    try (Connection connection = H2Connector.getConnection();
         PreparedStatement ps = connection.prepareStatement(sql)) {
      setValues(ps, regionIds);

      try (ResultSet resultSet = ps.executeQuery()) {
        while (resultSet.next()) {
          regions.add(mapRegion(resultSet));
        }
      }
    } catch (SQLException e) {
      log.error(e.getStackTrace().toString());
      throw new RuntimeException(e);
    }
    return regions;

  }

  private static String preparePlaceHolders(int length) {
    return String.join(",", Collections.nCopies(length, "?"));
  }

//  private static void setStringValues(PreparedStatement preparedStatement, List<String> strings) throws SQLException {
//    for (int i = 0; i < strings.size(); i++) {
//      preparedStatement.setString(i + 1, strings.get(i));
//    }
//  }
//
//  public static void setIntValues(PreparedStatement preparedStatement, List<Integer> integers) throws SQLException {
//    for (int i = 0; i < integers.size(); i++) {
//      preparedStatement.setInt(i + 1, integers.get(i));
//    }
//  }

  public static void setValues(PreparedStatement preparedStatement, List<?> values) throws SQLException {
    for (int i = 0; i < values.size(); i++) {
      preparedStatement.setObject(i + 1, values.get(i));
    }
  }
}
