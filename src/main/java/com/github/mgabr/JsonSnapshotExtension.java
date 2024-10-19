package com.github.mgabr;

import java.lang.reflect.Method;
import java.util.stream.Stream;
import org.junit.jupiter.api.extension.*;

public class JsonSnapshotExtension
  implements ParameterResolver, BeforeEachCallback {

  @Override
  public boolean supportsParameter(
    ParameterContext parameterContext,
    ExtensionContext extensionContext
  ) throws ParameterResolutionException {
    return (
      parameterContext.getParameter().getType() == ExpectJsonSnapshot.class
    );
  }

  @Override
  public Object resolveParameter(
    ParameterContext parameterContext,
    ExtensionContext extensionContext
  ) throws ParameterResolutionException {
    return new ExpectJsonSnapshot(extensionContext.getRequiredTestMethod());
  }

  @Override
  public void beforeEach(ExtensionContext extensionContext) {
    Object testInstance = extensionContext.getRequiredTestInstance();
    Method testMethod = extensionContext.getRequiredTestMethod();
    Class<?> testClass = testMethod.getDeclaringClass();

    Stream.of(testClass.getDeclaredFields())
      .filter(f -> f.getType() == ExpectJsonSnapshot.class)
      .forEach(f -> {
        f.setAccessible(true);
        try {
          f.set(testInstance, new ExpectJsonSnapshot(testMethod));
        } catch (IllegalAccessException e) {
          throw new RuntimeException(e);
        }
      });
  }
}
