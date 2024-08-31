package org.antisida.osm.validator.connectivity.repository;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import org.antisida.osm.validator.connectivity.utils.FileUtils;

public class H2Connector {

  private H2Connector() {
  }

  private static final HikariDataSource dataSource;

  private static final String H2_DRIVER = "org.h2.Driver";

  static {
    HikariConfig config = new HikariConfig();
    String workDir = FileUtils.getWorkPath().toString();
    config.setJdbcUrl("jdbc:h2:file:" + workDir + "/sample");
//    config.setJdbcUrl("jdbc:h2:file:" + workDir + "/sample;AUTO_SERVER=TRUE");
    config.setUsername("sa");
    config.setPassword("as");
    config.setConnectionTimeout(50000);
    config.setMaximumPoolSize(100);
//    config.setLeakDetectionThreshold(1000);
    config.setDriverClassName(H2_DRIVER);
    dataSource = new HikariDataSource(config);
  }

  public static Connection getConnection() {
    try {
      Connection connection = dataSource.getConnection();
      connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
      return connection;
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
}
