package com.github.mgabr;

import java.io.IOException;
import java.util.Properties;

public class JsonSnapshotProperties {

  private static final Properties PROPERTIES = new Properties();

  static {
    try {
      PROPERTIES.load(
        JsonSnapshotProperties.class.getClassLoader()
          .getResourceAsStream("json-snapshot.properties")
      );
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static boolean shouldUpdateSnapshots() {
    return Boolean.parseBoolean(PROPERTIES.getProperty("update-snapshots"));
  }

  static void setUpdateSnapshots(boolean value) {
    PROPERTIES.setProperty("update-snapshots", Boolean.toString(value));
  }
}
