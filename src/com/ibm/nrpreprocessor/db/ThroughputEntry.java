package com.ibm.nrpreprocessor.db;

import java.sql.ResultSet;
import java.sql.SQLException;

/** Stores the Throughput values received from New relic for an Applications*/
public class ThroughputEntry extends HistoryEntry {
    private double throughput;

    public ThroughputEntry() {
        super();

        this.setThroughput(-1.0);
    }

    public ThroughputEntry(ThroughputEntry entry) {
        super(entry);

        this.setThroughput(entry.getThroughput());
    }

    public ThroughputEntry(ResultSet rs) throws SQLException {
        super(rs);

        this.setName(rs.getString("APPNAME"));

        this.setThroughput(rs.getDouble("THROUGHPUT"));
    }

    public final double getThroughput() {
        return throughput;
    }

    public final void setThroughput(double throughput) {
        this.throughput = throughput;
    }

    @Override
    public String toString() {
        return String.format("{ '%s', '%s', '%s', '%s', '%s', %s }", getUUID(),
                getRetrieved(), getPeriodEnd(), getEnvironment(), getName(), getThroughput());
    }
}
