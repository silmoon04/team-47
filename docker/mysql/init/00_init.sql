-- Fresh-clone Docker bootstrap. Runs once when the MySQL volume is empty.
SOURCE /docker-entrypoint-initdb.d/db/01_schema.sql;
SOURCE /docker-entrypoint-initdb.d/db/02_reference_data.sql;
SOURCE /docker-entrypoint-initdb.d/db/03_demo_data.sql;
SOURCE /docker-entrypoint-initdb.d/db/05_sa_demo_data.sql;
