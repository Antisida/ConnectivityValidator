package org.antisida.osm.validator.connectivity.utils;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ArrayUtils {

  public long[] sumTwoArray(long[] one, long[] two) {
    long[] result = new long[one.length + two.length];
    System.arraycopy(one, 0, result, 0, one.length);
    System.arraycopy(two, 0, result, one.length, two.length);
    return result;
  }

  public static Long[] toLongArray(long[] array) {
    return Arrays.stream(array).boxed().toArray(Long[]::new);
  }

  public static Integer[] toIntArray(int[] array) {
    return Arrays.stream(array).boxed().toArray(Integer[]::new);
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
      return (int) Math.ceil((double) this.list.size() / (double) this.size);
    }
  }

}


