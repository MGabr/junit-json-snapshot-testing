# JUnit JSON Snapshot Testing

> Minimal JUnit extension for JSON snapshot tests

Inspired by 
[Java Snapshot Testing from Origin Energy](https://github.com/origin-energy/java-snapshot-testing)
and the aim to not add more dependencies.

It supports
  * automatic creation of missing snapshots
  * automatic updates of wrong snapshots
  * multiple snapshots per test method
  * pretty-printed JSON
  * ignoring specific fields in JSON comparison

Nothing more. No flexible configuration or customization.

## Example

1. Write a snapshot test like the following.
    ```java
    package com.example;
    
    // imports
    
    @ExtendWith(JsonSnapshotExtension.class)
    public class SnapshotTest {
    
      private ExpectJsonSnapshot expectJsonSnapshot;
    
      @Test
      void testMatchesSnapshots() {
        LocalDateTime indeterministicValue = LocalDateTime.now();
        String json1 =
          "{\"createdAt\": \"" + indeterministicValue + "\", \"field\": \"value\"}";
        String json2 = "{\"otherField\": 1}";
        
        expectJsonSnapshot.withIgnoredFields("createdAt").toMatch(json1);
        
        expectJsonSnapshot.toMatch(json2);
      }
    }
    ```


2. To automatically create or update snapshots on test run, add a `json-snapshot.properties` file in the test resources dir with content
    ```
    update-snapshots=true
    ```


3. Run the test to then find the snapshots. For the example test this should be a snapshot file `testMatchesSnapshots.json` in dir `test/resources/snapshots/com/example/JsonSnapshotExtensionTest`
   ```json
   {
     "createdAt": "2024-10-19T16:35:46.708845800",
     "field": "value"
   }
   ```
   and a second snapshot file `testMatchesSnapshots2.json` in the same dir.
   ```json
   {
     "otherField": 1
   }
   ```

4. To disable automatic creation or update of snapshots, change the `json-snapshot.properties` file to
    ```
    update-snapshots=false
    ```

4. Run the test. It will now assert that the JSON produced in the test matches the JSON from the snapshot file. :tada:

<br/>
<br/>

> [!NOTE]
> Ignored fields are included in the snapshot, just ignored for comparison. \
> So if you update snapshots then the ignored fields will also be updated in the snapshots.
