package org.antisida.osm.validator.connectivity.repository;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.antisida.osm.validator.connectivity.utils.FileUtils;

public class DbInitializer {
  /*private static volatile DbInitializer instance;

  private DbInitializer() {
  }

  public static DbInitializer getInstance() {
    DbInitializer localInstance = instance;
    if (localInstance == null) {
      synchronized (DbInitializer.class) {
        localInstance = instance;
        if (localInstance == null) {
          instance = localInstance = new DbInitializer();
        }
      }
    }
    return localInstance;
  }*/

  private static Connection connection;

  private static final String INIT_DB_SQL_PATH = "META-INF/db/h2/init-tables.sql";
  private static final String INSERT_REGION_SQL_PATH = "META-INF/db/h2/insert-osm-region.sql";
  private static final String DROP_ALL_OBJECTS = "DROP ALL OBJECTS";

  public static void initDb() {
    DbInitializer.connection = H2Connector.getConnection();
    dropALL();
    createTables();
    insertRegions();
  }

//  private static void printDB() throws SQLException {
//    String sql = "SELECT * FROM PUBLIC.marked_nodes";
//
//    PreparedStatement ps = connection.prepareStatement(sql);
//    ResultSet rs = ps.executeQuery();
//
//    while (rs.next()) {
//      System.out.println(rs.getLong("osm_id"));
//      System.out.println(rs.getLong("osm_id1"));
//      System.out.println(rs.getLong("osm_id2"));
//    }
//  }


  private static void dropALL() {
    executeUpdate(DROP_ALL_OBJECTS);
  }

  private static void createTables() {
    String createQuery = FileUtils.readFileAsString(Path.of(INIT_DB_SQL_PATH));
    executeUpdate(createQuery);
  }

  private static void insertRegions() {
    String insertRegionQuery = FileUtils.readFileAsString(Path.of(INSERT_REGION_SQL_PATH));
    executeUpdate(insertRegionQuery);
  }

  private static void executeUpdate(String query) {
    try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
      preparedStatement.executeUpdate();
    } catch (SQLException ex) {
      ex.printStackTrace();
    }
  }

//  private static void fillClanTable(Connection connection) {
//    String insertQuery = "INSERT INTO marked_nodes (osm_id, osm_id1, osm_id2) VALUES " + "(?,?,?)";
//
//    insertIntoClanTable(insertQuery, 1, 0, 4);
//    insertIntoClanTable(insertQuery, 2, 0, 4);
//    insertIntoClanTable(insertQuery, 3, 0, 4);
//    insertIntoClanTable(insertQuery, 4, 0, 4);
//  }

//  private static void insertIntoClanTable(String query, int name, int gold, int i) {
//    try {
//      PreparedStatement insertPreparedStatement = connection.prepareStatement(query);
//      insertPreparedStatement.setInt(1, name);
//      insertPreparedStatement.setInt(2, gold);
//      insertPreparedStatement.setInt(3, i);
//      insertPreparedStatement.executeUpdate();
//      insertPreparedStatement.close();
//    } catch (SQLException ex) {
//      ex.printStackTrace();
//    }
//  }

//  private static void fillTaskTable() {
//    String insertQuery = "INSERT INTO task"
//        + " (description, gold_given) VALUES " + "(?,?)";
//
//    insertIntoClanTable(insertQuery, "You dont have to do anything, just take the money", 100);
//  }
//
//  private static void insertIntoTaskTable(String query, String description, int goldGiven) {
//    try {
//      PreparedStatement insertPreparedStatement = connection.prepareStatement(query);
//      insertPreparedStatement.setString(1, description);
//      insertPreparedStatement.setInt(2, goldGiven);
//      insertPreparedStatement.executeUpdate();
//      insertPreparedStatement.close();
//    } catch (SQLException ex) {
//      ex.printStackTrace();
//    }
//  }
//
//  private static void fillUserTable() {
//    String insertQuery = "INSERT INTO users "
//        + "(name, surname, clan_id) VALUES " + "(?,?,?)";
//
//    insertIntoUserTable(insertQuery, "John", "Doe", 1L);
//    insertIntoUserTable(insertQuery, "Jane", "Doe", 1L);
//    insertIntoUserTable(insertQuery, "Ivan", "Pirozhkov", 1L);
//    insertIntoUserTable(insertQuery, "Anton", "Sergeev", 2L);
//    insertIntoUserTable(insertQuery, "Bob", "Welch", 4L);
//    insertIntoUserTable(insertQuery, "Lindsey", "Buckingham", 4L);
//
//  }
//
//  private static void insertIntoUserTable(String query, String name, String surname, Long clanId) {
//    try {
//      PreparedStatement insertPreparedStatement = connection.prepareStatement(query);
//      insertPreparedStatement.setString(1, name);
//      insertPreparedStatement.setString(2, surname);
//      insertPreparedStatement.setLong(3, clanId);
//      insertPreparedStatement.executeUpdate();
//      insertPreparedStatement.close();
//    } catch (SQLException ex) {
//      ex.printStackTrace();
//    }
//  }

}