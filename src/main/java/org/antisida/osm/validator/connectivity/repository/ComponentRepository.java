package org.antisida.osm.validator.connectivity.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.antisida.osm.validator.connectivity.model.ConnectedComponent;
import org.antisida.osm.validator.connectivity.model.Region;

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
           INSERT INTO components(id, region_id, size)
           VALUES (?, ?, ?);
          """;

    try (Connection connection = H2Connector.getConnection();
         PreparedStatement ps = connection.prepareStatement(compiledQuery);) {
      connection.setAutoCommit(true);

      for (ConnectedComponent component : components) {
        ps.setString(1, component.getId().toString());
        ps.setInt(2, component.getRegionId());
        ps.setInt(3, component.getSize());
        ps.addBatch();
      }

      int[] inserted = ps.executeBatch();

//        log.info("Inserted count: " + inserted.length);
      ps.close();

    } catch (SQLException ex) {
      throw new RuntimeException("Error");
    }
  }

  public boolean isReadyInnerValidation(Region region) {
    String sql = "select id from components where region_id = ?";
    int result = 0;
    try (Connection connection = H2Connector.getConnection();
         PreparedStatement ps = connection.prepareStatement(sql)) {
      ps.setInt(1, region.id());

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

}