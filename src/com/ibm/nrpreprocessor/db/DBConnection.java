package com.ibm.nrpreprocessor.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;


public class DBConnection {

    private final String table;
    private static final String user = "johntk";
    private static final String password = "IBMdb2";
    private static final String DB2url = "jdbc:db2://localhost:50000/NEWRELIC";
    private static Connection conn = null;

    /*** Set the table name for applications*/
    public static DBConnection createApplication() {
        return new DBConnection("APPLICATIONDATA");
    }

    public DBConnection(String table) {
        this.table = String.format("NRDATA.%s", table);
    }

    public Connection getConnection() throws IllegalAccessException,
            InstantiationException, ClassNotFoundException, SQLException {

        try {
            Class.forName("COM.ibm.db2os390.sqlj.jdbc.DB2SQLJDriver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        System.out.println("Connecting to database...");
        conn = DriverManager.getConnection(DB2url, user, password);
        System.out.println("Database Connection Initialized.");
        return conn;
    }

    public void closeConnection() {
        if (conn == null) return;
        try {
            conn.close();
            conn = null;
        } catch(SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void addHistory(ThroughputEntry entry) throws Exception {

        try (Statement statement = conn.createStatement()) {
            statement
                    .executeUpdate(String
                            .format("INSERT INTO " + table
                                            + "(ID, RETRIEVED, PERIOD_END, ENVIRONMENT, APPNAME, THROUGHPUT)"
                                            + "VALUES ('%s', '%s', '%s', '%s', '%s', %s)",
                                    entry.getUUID(), entry.getRetrieved(),
                                    entry.getPeriodEnd(), entry.getEnvironment(),
                                    entry.getName(), entry.getThroughput()));
        }
    }
}
