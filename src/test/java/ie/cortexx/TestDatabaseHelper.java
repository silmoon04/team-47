package ie.cortexx;

// utility for integration tests that need a real database
// connects to ipos_ca_test (not production) via db.properties
//
// setup: CREATE DATABASE ipos_ca_test; then run 01_schema.sql against it
public class TestDatabaseHelper {

    // TODO: implement getConnection() using db.test.url from properties
    // TODO: add seedTestData() and cleanTestData() methods
}