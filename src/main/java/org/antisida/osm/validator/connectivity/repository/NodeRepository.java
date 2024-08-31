package org.antisida.osm.validator.connectivity.repository;

import static org.antisida.osm.validator.connectivity.utils.ArrayUtils.toLongArray;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.AbstractList;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.antisida.osm.validator.connectivity.model.MarkedNode;

@Slf4j
public class NodeRepository {

  private static volatile NodeRepository instance;

  private NodeRepository() { }

  public static NodeRepository getInstance() {
    NodeRepository localInstance = instance;
    if (localInstance == null) {
      synchronized (NodeRepository.class) {
        localInstance = instance;
        if (localInstance == null) {
          instance = localInstance = new NodeRepository();
        }
      }
    }
    return localInstance;
  }

  public void save(List<MarkedNode> nodes) {
    List<List<MarkedNode>> partitions = partition(nodes, 100000);
    for (List<MarkedNode> partition : partitions) {

      PreparedStatement ps;

      try (Connection connection = H2Connector.getConnection()) {
        connection.setAutoCommit(true);

        String compiledQuery = """
           INSERT INTO nodes(osm_id, osm_way_ids, con_component_id, neighbor_node_ids)
           VALUES (?, ?, ?, ?);
          """;
        ps = connection.prepareStatement(compiledQuery);

        for (MarkedNode node : partition) {
          ps.setLong(1, node.getOsmId());
          ps.setArray(2, connection.createArrayOf("BIGINT ARRAY", toLongArray(node.getOsmWayIds())));
          ps.setString(3, node.getConnectedComponentId().toString());
          ps.setArray(4, connection.createArrayOf("BIGINT ARRAY", toLongArray(node.getNeighborNodeIds())));
          ps.addBatch();
        }

        int[] inserted = ps.executeBatch();

//        log.info("Inserted count: " + inserted.length);
        ps.close();

      } catch (SQLException ex) {
        throw new RuntimeException("Error");
      }
    }
  }

  public static <T> List<List<T>> partition(List<T> list, int size) {
    Objects.requireNonNull(list, "list");
    if (size <= 0) {
      throw new IllegalArgumentException("Size must be greater than 0");
    } else {
      return new Partition(list, size);
    }
  }

  private static final class Partition<T> extends AbstractList<List<T>> {
    private final List<T> list;
    private final int size;

    private Partition(List<T> list, int size) {
      this.list = list;
      this.size = size;
    }

    public List<T> get(int index) {
      int listSize = this.size();
      if (index < 0) {
        throw new IndexOutOfBoundsException("Index " + index + " must not be negative");
      } else if (index >= listSize) {
        throw new IndexOutOfBoundsException("Index " + index + " must be less than size " + listSize);
      } else {
        int start = index * this.size;
        int end = Math.min(start + this.size, this.list.size());
        return this.list.subList(start, end);
      }
    }
    public boolean isEmpty() {
      return this.list.isEmpty();
    }

    public int size() {
      return (int)Math.ceil((double)this.list.size() / (double)this.size);
    }
  }
}
