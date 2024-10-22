package com.github.mgabr;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(JsonSnapshotExtension.class)
public class JsonSnapshotExtensionTest {

  private static final String TEST_SNAPSHOTS_DIRECTORY = String.join(
    File.separator,
    "src",
    "test",
    "resources",
    "snapshots",
    "com",
    "github",
    "mgabr",
    "JsonSnapshotExtensionTest"
  );

  private static final String JSON =
    "{\"strField\":\"v\",\"objField\":{\"intField\":1},\"arrField\":[{\"field1\": 1}, {\"field2\": 2}]}";

  private static final String JSON_2 = "{\"strField\":\"v\"}";

  private static final String PRETTY_JSON =
    """
    {
      "objField": {
        "intField": 1
      },
      "arrField": [
        {
          "field1": 1
        },
        {
          "field2": 2
        }
      ],
      "strField": "v"
    }""";

  private static final String PRETTY_JSON_2 =
    """
    {
      "strField": "v"
    }""";

  private static final String JSON_WITH_DIFFERENT_STR_FIELD =
    "{\"strField\":\"w\",\"objField\":{\"intField\":1},\"arrField\":[{\"field1\": 1}, {\"field2\": 2}]}";

  private static final String JSON_WITH_DIFFERENT_NESTED_ARR_FIELD =
    "{\"strField\":\"v\",\"objField\":{\"intField\":1},\"arrField\":[{\"field1\": 1}, {\"field2\": 3}]}";

  private ExpectJsonSnapshot expectJsonSnapshot;

  @AfterEach
  void cleanupAfterEach() throws IOException {
    deleteSnapshotFiles();
    resetSnapshotProperties();
  }

  void deleteSnapshotFiles() throws IOException {
    Path testSnapshotsPath = Path.of(TEST_SNAPSHOTS_DIRECTORY);
    try (Stream<Path> dirStream = Files.walk(testSnapshotsPath)) {
      dirStream
        .map(Path::toFile)
        .sorted(Comparator.reverseOrder())
        .forEach(File::delete);
    } catch (NoSuchFileException _) {}
  }

  void resetSnapshotProperties() {
    JsonSnapshotProperties.setUpdateSnapshots(false);
  }

  @Test
  void testSavesSnapshotFileWhenNoSnapshotFileAndFixProfile()
    throws IOException {
    JsonSnapshotProperties.setUpdateSnapshots(true);

    expectJsonSnapshot.toMatch(JSON);

    Path expectedSnapshotFilePath = Path.of(
      TEST_SNAPSHOTS_DIRECTORY,
      "testSavesSnapshotFileWhenNoSnapshotFileAndFixProfile.json"
    );
    assertTrue(Files.exists(expectedSnapshotFilePath));

    String actualSavedJson = Files.readString(expectedSnapshotFilePath);
    assertEquals(PRETTY_JSON, actualSavedJson);
  }

  @Test
  void testSavesMultipleSnapshotFilesWhenNoSnapshotFilesAndFixProfile()
    throws IOException {
    JsonSnapshotProperties.setUpdateSnapshots(true);

    expectJsonSnapshot.toMatch(JSON);

    expectJsonSnapshot.toMatch(JSON_2);

    Path expectedSnapshotFilePath1 = Path.of(
      TEST_SNAPSHOTS_DIRECTORY,
      "testSavesMultipleSnapshotFilesWhenNoSnapshotFilesAndFixProfile.json"
    );
    assertTrue(Files.exists(expectedSnapshotFilePath1));

    String actualSavedJson1 = Files.readString(expectedSnapshotFilePath1);
    assertEquals(PRETTY_JSON, actualSavedJson1);

    Path expectedSnapshotFilePath2 = Path.of(
      TEST_SNAPSHOTS_DIRECTORY,
      "testSavesMultipleSnapshotFilesWhenNoSnapshotFilesAndFixProfile2.json"
    );
    assertTrue(Files.exists(expectedSnapshotFilePath2));

    String actualSavedJson2 = Files.readString(expectedSnapshotFilePath2);
    assertEquals(PRETTY_JSON_2, actualSavedJson2);
  }

  @Test
  void testUpdatesSnapshotFileWhenNotMatchingSnapshotFileAndFixProfile()
    throws IOException {
    JsonSnapshotProperties.setUpdateSnapshots(true);

    Path snapshotFilePath = Path.of(
      TEST_SNAPSHOTS_DIRECTORY,
      "testUpdatesSnapshotFileWhenNotMatchingSnapshotFileAndFixProfile.json"
    );
    Files.createDirectories(snapshotFilePath.getParent());
    Files.writeString(
      snapshotFilePath,
      JSON_WITH_DIFFERENT_STR_FIELD,
      StandardOpenOption.CREATE_NEW
    );

    expectJsonSnapshot.toMatch(JSON);

    String actualSavedJson = Files.readString(snapshotFilePath);
    assertEquals(PRETTY_JSON, actualSavedJson);
  }

  @Test
  void testDoesNotUpdateSnapshotFileWhenMatchingSnapshotFileAndFixProfile()
    throws IOException {
    JsonSnapshotProperties.setUpdateSnapshots(true);

    Path snapshotFilePath = Path.of(
      TEST_SNAPSHOTS_DIRECTORY,
      "testDoesNotUpdateSnapshotFileWhenMatchingSnapshotFileAndFixProfile.json"
    );
    Files.createDirectories(snapshotFilePath.getParent());
    Files.writeString(snapshotFilePath, JSON, StandardOpenOption.CREATE_NEW);

    BasicFileAttributes attr = Files.readAttributes(
      snapshotFilePath,
      BasicFileAttributes.class
    );
    FileTime snapshotFileLastModifiedBeforeMatch = attr.lastModifiedTime();

    expectJsonSnapshot.toMatch(JSON);

    attr = Files.readAttributes(snapshotFilePath, BasicFileAttributes.class);
    FileTime snapshotFileLastModifiedAfterMatch = attr.lastModifiedTime();

    assertEquals(
      snapshotFileLastModifiedBeforeMatch,
      snapshotFileLastModifiedAfterMatch
    );
  }

  @Test
  void testFailsWhenNoSnapshotFileAndNoFixProfile() {
    assertThrows(AssertionError.class, () -> expectJsonSnapshot.toMatch(JSON));
  }

  @Test
  void testFailsWhenSnapshotJsonAndActualJsonDontMatch() throws IOException {
    Path snapshotFilePath = Path.of(
      TEST_SNAPSHOTS_DIRECTORY,
      "testFailsWhenSnapshotJsonAndActualJsonDontMatch.json"
    );
    Files.createDirectories(snapshotFilePath.getParent());
    Files.writeString(
      snapshotFilePath,
      JSON_WITH_DIFFERENT_STR_FIELD,
      StandardOpenOption.CREATE_NEW
    );

    assertThrows(AssertionError.class, () -> expectJsonSnapshot.toMatch(JSON));
  }

  @Test
  void testFailsWhenOneOfMultipleSnapshotJsonAndActualJsonDontMatch()
    throws IOException {
    Path snapshotFilePath1 = Path.of(
      TEST_SNAPSHOTS_DIRECTORY,
      "testFailsWhenOneOfMultipleSnapshotJsonAndActualJsonDontMatch.json"
    );
    Path snapshotFilePath2 = Path.of(
      TEST_SNAPSHOTS_DIRECTORY,
      "testFailsWhenOneOfMultipleSnapshotJsonAndActualJsonDontMatch2.json"
    );
    Files.createDirectories(snapshotFilePath1.getParent());
    Files.writeString(snapshotFilePath1, JSON, StandardOpenOption.CREATE_NEW);
    Files.writeString(snapshotFilePath2, JSON_2, StandardOpenOption.CREATE_NEW);

    expectJsonSnapshot.toMatch(JSON);

    assertThrows(AssertionError.class, () -> expectJsonSnapshot.toMatch(JSON));
  }

  @Test
  void testSucceedsWhenSnapshotJsonAndActualJsonMatch() throws IOException {
    Path snapshotFilePath = Path.of(
      TEST_SNAPSHOTS_DIRECTORY,
      "testSucceedsWhenSnapshotJsonAndActualJsonMatch.json"
    );
    Files.createDirectories(snapshotFilePath.getParent());
    Files.writeString(snapshotFilePath, JSON, StandardOpenOption.CREATE_NEW);

    expectJsonSnapshot.toMatch(JSON);
  }

  @Test
  void testSucceedsWhenMultipleSnapshotJsonAndActualJsonMatch()
    throws IOException {
    Path snapshotFilePath1 = Path.of(
      TEST_SNAPSHOTS_DIRECTORY,
      "testSucceedsWhenMultipleSnapshotJsonAndActualJsonMatch.json"
    );
    Path snapshotFilePath2 = Path.of(
      TEST_SNAPSHOTS_DIRECTORY,
      "testSucceedsWhenMultipleSnapshotJsonAndActualJsonMatch2.json"
    );
    Files.createDirectories(snapshotFilePath1.getParent());
    Files.writeString(snapshotFilePath1, JSON, StandardOpenOption.CREATE_NEW);
    Files.writeString(snapshotFilePath2, JSON_2, StandardOpenOption.CREATE_NEW);

    expectJsonSnapshot.toMatch(JSON);

    expectJsonSnapshot.toMatch(JSON_2);
  }

  @Test
  void testSucceedsWhenDifferentStrFieldIgnoredAndRemainingJsonAndActualJsonMatch()
    throws IOException {
    Path snapshotFilePath = Path.of(
      TEST_SNAPSHOTS_DIRECTORY,
      "testSucceedsWhenDifferentStrFieldIgnoredAndRemainingJsonAndActualJsonMatch.json"
    );
    Files.createDirectories(snapshotFilePath.getParent());
    Files.writeString(
      snapshotFilePath,
      JSON_WITH_DIFFERENT_STR_FIELD,
      StandardOpenOption.CREATE_NEW
    );

    expectJsonSnapshot.withIgnoredFields("strField").toMatch(JSON);
  }

  @Test
  void testSucceedsWhenDifferentNestedArrFieldIgnoredAndRemainingJsonAndActualJsonMatch()
    throws IOException {
    Path snapshotFilePath = Path.of(
      TEST_SNAPSHOTS_DIRECTORY,
      "testSucceedsWhenDifferentNestedArrFieldIgnoredAndRemainingJsonAndActualJsonMatch.json"
    );
    Files.createDirectories(snapshotFilePath.getParent());
    Files.writeString(
      snapshotFilePath,
      JSON_WITH_DIFFERENT_NESTED_ARR_FIELD,
      StandardOpenOption.CREATE_NEW
    );

    expectJsonSnapshot.withIgnoredFields("arrField[1].field2").toMatch(JSON);
  }

  @Test
  void testInjectedWithExpectThroughMethodAlso(
    ExpectJsonSnapshot methodInjectedExpectJsonSnapshot
  ) {
    assertNotNull(methodInjectedExpectJsonSnapshot);
  }

  @Test
  void testReadmeExampleSnapshotSaving() throws IOException {
    JsonSnapshotProperties.setUpdateSnapshots(true);

    LocalDateTime indeterministicValue = LocalDateTime.now();
    String json1 =
      "{\"createdAt\": \"" + indeterministicValue + "\", \"field\": \"value\"}";
    String json2 = "{\"otherField\": 1}";

    expectJsonSnapshot.withIgnoredFields("createdAt").toMatch(json1);

    expectJsonSnapshot.toMatch(json2);

    Path expectedSnapshotFilePath1 = Path.of(
      TEST_SNAPSHOTS_DIRECTORY,
      "testReadmeExampleSnapshotSaving.json"
    );
    assertTrue(Files.exists(expectedSnapshotFilePath1));

    Path expectedSnapshotFilePath2 = Path.of(
      TEST_SNAPSHOTS_DIRECTORY,
      "testReadmeExampleSnapshotSaving2.json"
    );
    assertTrue(Files.exists(expectedSnapshotFilePath2));

    String actualSavedJson2 = Files.readString(expectedSnapshotFilePath2);
    String expectedSavedJson2 =
      """
      {
        "otherField": 1
      }""";
    assertEquals(expectedSavedJson2, actualSavedJson2);
  }

  @Test
  void testReadmeExampleSnapshotAssertion() throws IOException {
    String json1 =
      "{\"createdAt\": \"2024-10-19T16:35:46.708845800\", \"field\": \"value\"}";
    String json2 = "{\"otherField\": 1}";

    Path snapshotFilePath1 = Path.of(
      TEST_SNAPSHOTS_DIRECTORY,
      "testReadmeExampleSnapshotAssertion.json"
    );
    Path snapshotFilePath2 = Path.of(
      TEST_SNAPSHOTS_DIRECTORY,
      "testReadmeExampleSnapshotAssertion2.json"
    );
    Files.createDirectories(snapshotFilePath1.getParent());
    Files.writeString(snapshotFilePath1, json1, StandardOpenOption.CREATE_NEW);
    Files.writeString(snapshotFilePath2, json2, StandardOpenOption.CREATE_NEW);

    LocalDateTime indeterministicValue = LocalDateTime.now();
    String newJson1 =
      "{\"createdAt\": \"" + indeterministicValue + "\", \"field\": \"value\"}";

    expectJsonSnapshot.withIgnoredFields("createdAt").toMatch(newJson1);

    expectJsonSnapshot.toMatch(json2);
  }
}
