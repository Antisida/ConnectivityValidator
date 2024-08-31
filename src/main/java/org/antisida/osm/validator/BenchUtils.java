package org.antisida.osm.validator;

import lombok.experimental.UtilityClass;

/**
 * 0 - это сам метод getStackTrace()
 * <p>
 * 1 - это вызываемый метод (calledMethod)
 * <p>
 * 2 - это вызывающий (родительский) метод (callerMethod)
 */
@UtilityClass
public class BenchUtils {

  public String calledMethod() {
    StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

    StackTraceElement stackTraceElement = stackTrace[2];
    return stackTraceElement.getMethodName();
  }
}
