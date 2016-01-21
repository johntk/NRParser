package com.ibm.nrpreprocessor.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;


public class DBConnection {

    private final String table;
    private static final String user = "CLOUD";
    private static final String password = "Dormant-2000";
    private static final String DB2url = "jdbc:db2://localhost:50000/NEWRELIC";


    /** Set the table name for applications */
    public static DBConnection createApplication() {
        return new DBConnection("APPLICATIONDATA");
    }

    public DBConnection(String table) {
        this.table =  String.format("NRDATA.%s", table);
    }

    public Connection getConnection() throws IllegalAccessException,
            InstantiationException, ClassNotFoundException, SQLException {

        try {
            Class.forName("COM.ibm.db2os390.sqlj.jdbc.DB2SQLJDriver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        System.out.println("Connecting to database...");
        Connection  connection = DriverManager.getConnection(DB2url, user, password);
        System.out.println( "From DAO, connection obtained " );
        return connection;
    }


    public boolean addHistory(ThroughputEntry entry) throws Exception {
        Connection connection = getConnection();
        Statement statement = connection.createStatement();

        return 0 < statement
                .executeUpdate(String
                        .format("INSERT INTO " + table
                                        + "(ID, RETRIEVED, PERIOD_END, ENVIRONMENT, APPNAME, THROUGHPUT)"
                                        + "VALUES ('%s', '%s', '%s', '%s', '%s', %s)",
                                entry.getUUID(), entry.getRetrieved(),
                                entry.getPeriodEnd(), entry.getEnvironment(),
                                entry.getName(), entry.getThroughput()));
    }

}
