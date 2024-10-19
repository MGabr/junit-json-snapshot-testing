package com.github.mgabr;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonSnapshotFile {

  private static final int JSON_INDENT_SPACES = 2;

  private static final Path SNAPSHOT_RESOURCES_PATH = Path.of(
    "src",
    "test",
    "resources",
    "snapshots"
  );

  private final Path path;

  public JsonSnapshotFile(Method testMethod, int snapshotNumber) {
    this.path = getSnapshotFilePath(testMethod, snapshotNumber);
  }

  public Path getPath() {
    return this.path;
  }

  public String load() throws IOException {
    return Files.readString(this.path);
  }

  public void save(String actualJson) throws IOException, JSONException {
    String prettyActualJson = new JSONObject(actualJson).toString(
      JSON_INDENT_SPACES
    );
    Files.createDirectories(this.path.getParent());
    Files.writeString(this.path, prettyActualJson, StandardOpenOption.CREATE);
  }

  private static Path getSnapshotFilePath(
    Method testMethod,
    int snapshotNumber
  ) {
    Path classDirPath = Path.of(
      testMethod
        .getDeclaringClass()
        .getPackageName()
        .replace(".", File.separator)
    );

    String className = testMethod.getDeclaringClass().getSimpleName();
    String methodName = testMethod.getName();
    String snapshotNumberPart = snapshotNumber > 1
      ? Integer.toString(snapshotNumber)
      : "";
    Path classAndMethodPath = Path.of(
      className,
      methodName + snapshotNumberPart + ".json"
    );

    return SNAPSHOT_RESOURCES_PATH.resolve(classDirPath, classAndMethodPath);
  }
}
