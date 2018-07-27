Quick Setup (Java 1.8.0_171 & Maven 3.5.3)
-----------

1.  Clone this repository.
2.  Build with maven `mvn clean install`.
2.a This proposal uses Spring Batch, please create a folder named `batches` in your user home directory e.g.:
    `home/javier/batches/` and copy there your `access.log` file.
3.  Connect to MySQL instance then run next files(located in project's resources folder, respect order): 
    `reset_log_registration.sql, create_schema.sql` 
4.  Open a terminal window where your project is located and run the app: 
`java -cp target/parser-0.0.1-SNAPSHOT-jar-with-dependencies.jar com.ef.Parser --startDate=2017-01-01.00:00:00 --duration=Daily --threshold=200`.
4.  Connect to MySQL instance and use sql test queries: `testing.sql`.



