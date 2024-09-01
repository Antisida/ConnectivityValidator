package org.antisida.osm.validator.connectivity.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.antisida.osm.validator.connectivity.model.ConnectedComponent;

@Slf4j
public class ComponentRepository {

  private static volatile ComponentRepository instance;

  private ComponentRepository() { }

  public static ComponentRepository getInstance() {
    ComponentRepository localInstance = instance;
    if (localInstance == null) {
      synchronized (ComponentRepository.class) {
        localInstance = instance;
        if (localInstance == null) {
          instance = localInstance = new ComponentRepository();
        }
      }
    }
    return localInstance;
  }

  public void save(List<ConnectedComponent> components) {
    String compiledQuery = """
         INSERT INTO components(id, region_id, isolated)
         VALUES (?, ?, ?);
        """;

    try (Connection connection = H2Connector.getConnection();
         PreparedStatement ps = connection.prepareStatement(compiledQuery);) {
      connection.setAutoCommit(true);

      for (ConnectedComponent component : components) {
        ps.setString(1, component.getId().toString());
        ps.setInt(2, component.getRegionId());
        ps.setBoolean(3, component.isIsolated());
        ps.addBatch();
      }

      int[] inserted = ps.executeBatch();

//        log.info("Inserted count: " + inserted.length);
      ps.close();

    } catch (SQLException ex) {
      throw new RuntimeException("Error");
    }
  }

  public boolean isReadyInnerValidation(int regionId) {
    String sql = "select id from components where region_id = ?";
    int result = 0;
    try (Connection connection = H2Connector.getConnection();
         PreparedStatement ps = connection.prepareStatement(sql)) {
      ps.setInt(1, regionId);

      try (ResultSet resultSet = ps.executeQuery()) {
        if (resultSet.next()) {
          result = resultSet.getInt("id");
        }
      }

    } catch (SQLException e) {
      log.error(e.getStackTrace().toString());
      throw new RuntimeException(e);
    }
    return result == 0;
  }

  public List<ConnectedComponent> getByRegionId(Integer regionId) {
    String sql = "select * from components where region_id = ?";

    List<ConnectedComponent> components = new ArrayList<>();
    try (Connection connection = H2Connector.getConnection();
         PreparedStatement ps = connection.prepareStatement(sql)) {
      ps.setInt(1, regionId);

      try (ResultSet resultSet = ps.executeQuery()) {
        while (resultSet.next()) {
          components.add(mapComponent(resultSet));
        }
      }

    } catch (SQLException e) {
      log.error(e.getStackTrace().toString());
      throw new RuntimeException(e);
    }
    return components;
  }

  private ConnectedComponent mapComponent(ResultSet resultSet) throws SQLException {
    return new ConnectedComponent(
        UUID.fromString(resultSet.getString("id")),
        resultSet.getInt("region_id"),
        resultSet.getBoolean("isolated")
    );
  }

  public List<ConnectedComponent> getIsolatedByRegionId(Integer regionId) {
    String sql = "select * from components where region_id = ? AND isolated = TRUE";

    List<ConnectedComponent> components = new ArrayList<>();
    try (Connection connection = H2Connector.getConnection();
         PreparedStatement ps = connection.prepareStatement(sql)) {
      ps.setInt(1, regionId);

      try (ResultSet resultSet = ps.executeQuery()) {
        while (resultSet.next()) {
          components.add(mapComponent(resultSet));
        }
      }

    } catch (SQLException e) {
      log.error(e.getStackTrace().toString());
      throw new RuntimeException(e);
    }
    return components;
  }

  public List<ConnectedComponent> getIsolatedComponents(Integer regionId) {
    String sql = "select * from components where region_id = ? AND isolated = TRUE";

    List<ConnectedComponent> components = new ArrayList<>();
    try (Connection connection = H2Connector.getConnection();
         PreparedStatement ps = connection.prepareStatement(sql)) {
      ps.setInt(1, regionId);

      try (ResultSet resultSet = ps.executeQuery()) {
        while (resultSet.next()) {
          components.add(mapComponent(resultSet));
        }
      }

    } catch (SQLException e) {
      log.error(e.getStackTrace().toString());
      throw new RuntimeException(e);
    }
    return components;
  }

  public List<ConnectedComponent> getNotIsolatedComponents(Integer regionId) {
    String sql = "select * from components where region_id = ? AND isolated = FALSE";

    List<ConnectedComponent> components = new ArrayList<>();
    try (Connection connection = H2Connector.getConnection();
         PreparedStatement ps = connection.prepareStatement(sql)) {
      ps.setInt(1, regionId);

      try (ResultSet resultSet = ps.executeQuery()) {
        while (resultSet.next()) {
          components.add(mapComponent(resultSet));
        }
      }

    } catch (SQLException e) {
      log.error(e.getStackTrace().toString());
      throw new RuntimeException(e);
    }
    return components;
  }

  public void setIsolated(List<UUID> uuids, boolean isolated) {
    String sql = "UPDATE components SET isolated = ? WHERE id IN (%s);".formatted(preparePlaceHolders(uuids.size()));

    try (Connection connection = H2Connector.getConnection();
         PreparedStatement ps = connection.prepareStatement(sql)) {
      ps.setBoolean(1, isolated);
      setValues(ps, uuids);
      int update = ps.executeUpdate();
      log.info("update: " + update);
    } catch (SQLException e) {
      log.error(e.getStackTrace().toString());
      throw new RuntimeException(e);
    }
  }

  private static String preparePlaceHolders(int length) {
    return String.join(",", Collections.nCopies(length, "?"));
  }

  public static void setValues(PreparedStatement preparedStatement, List<?> values) throws SQLException {
    for (int i = 0; i < values.size(); i++) {
      preparedStatement.setObject(i + 2, values.get(i).toString());
    }
  }
}