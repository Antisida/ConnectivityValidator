package org.antisida.osm.validator.connectivity.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ArrayUtils {

  public long[] sumTwoArray(long[] one, long[] two) {
    long[] result = new long[one.length + two.length];
    System.arraycopy(one, 0, result, 0, one.length);
    System.arraycopy(two, 0, result, one.length, two.length);
    return result;
  }
}
