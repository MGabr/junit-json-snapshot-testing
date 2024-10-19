package com.github.mgabr;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.*;
import java.util.stream.Stream;
import org.json.JSONException;
import org.skyscreamer.jsonassert.Customization;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.ValueMatcher;
import org.skyscreamer.jsonassert.comparator.CustomComparator;
import org.skyscreamer.jsonassert.comparator.DefaultComparator;
import org.skyscreamer.jsonassert.comparator.JSONComparator;

public class ExpectJsonSnapshot {

  private static final JSONComparator DEFAULT_COMPARATOR =
    new DefaultComparator(JSONCompareMode.STRICT);
  private static final ValueMatcher<Object> ALWAYS_TRUE = (_, _) -> true;

  private final Method testMethod;

  private JSONComparator jsonComparator = DEFAULT_COMPARATOR;
  private int snapshotNumber = 1;

  public ExpectJsonSnapshot(Method testMethod) {
    this.testMethod = testMethod;
  }

  public ExpectJsonSnapshot withIgnoredFields(String... fieldsToIgnore) {
    this.jsonComparator = new CustomComparator(
      JSONCompareMode.STRICT,
      Stream.of(fieldsToIgnore)
        .map(field -> Customization.customization(field, ALWAYS_TRUE))
        .toArray(Customization[]::new)
    );
    return this;
  }

  public void toMatch(String actualJson) {
    try {
      toMatchWithExceptions(actualJson);
    } catch (IOException | JSONException e) {
      // tests won't be able to handle them properly anyway
      // and should not be forced to add them to their method signature
      throw new RuntimeException(e);
    }
  }

  private void toMatchWithExceptions(String actualJson)
    throws IOException, JSONException {
    JsonSnapshotFile jsonSnapshotFile = new JsonSnapshotFile(
      this.testMethod,
      this.snapshotNumber
    );
    this.snapshotNumber++;

    String expectedJson;
    try {
      expectedJson = jsonSnapshotFile.load();
    } catch (NoSuchFileException e) {
      if (JsonSnapshotProperties.shouldUpdateSnapshots()) {
        jsonSnapshotFile.save(actualJson);
        return;
      } else {
        throw new AssertionError(
          "Expected snapshot file \"" +
          jsonSnapshotFile.getPath() +
          "\" + does not exist"
        );
      }
    }

    try {
      JSONAssert.assertEquals(expectedJson, actualJson, this.jsonComparator);
    } catch (AssertionError e) {
      if (JsonSnapshotProperties.shouldUpdateSnapshots()) {
        jsonSnapshotFile.save(actualJson);
      } else {
        throw e;
      }
    }
  }
}
