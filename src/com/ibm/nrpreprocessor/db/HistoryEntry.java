package com.ibm.nrpreprocessor.db;

import org.joda.time.Instant;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.UUID;


/** Stores the values for Applications and environment */
public class HistoryEntry {
    public static  Timestamp createNullTimestamp() {
        return new Timestamp(System.currentTimeMillis());
    }

    private UUID uuid;
    private Timestamp retrieved;
    private Timestamp periodEnd;
    private String environment;
    private String name;

    public HistoryEntry() {
        this.setUUID((UUID) null);
        this.setEnvironment(null);
        this.setName(null);
    }

    public HistoryEntry(HistoryEntry entry) {
        this.setUUID(entry.getUUID());
        this.setRetrieved(new Instant(entry.getRetrieved().getTime()));
        this.setPeriodEnd(new Instant(entry.getPeriodEnd().getTime()));
        this.setEnvironment(entry.getEnvironment());
        this.setName(entry.getName());
    }

    public HistoryEntry(ResultSet rs) throws SQLException {
        this.setUUID(rs.getString("ID"));
        this.setRetrieved(new Instant(rs.getTimestamp("RETRIEVED")));
        this.setPeriodEnd(new Instant(rs.getTimestamp("PERIOD_END")));
        this.setEnvironment(rs.getString("ENVIRONMENT"));
        this.setName(null);
    }

    public final UUID getUUID() {
        return uuid;
    }

    public final Timestamp getRetrieved() {
        return retrieved;
    }

    public final Timestamp getPeriodEnd() {
        return periodEnd;
    }

    public final String getEnvironment() {
        return environment;
    }

    public final String getName() {
        return name;
    }

    private  void setUUID(String uuid) {
        this.setUUID(UUID.fromString(uuid));
    }

    private  void setUUID(UUID uuid) {
        this.uuid = uuid;

        if (this.uuid == null) {
            this.uuid = UUID.randomUUID();
        }
    }


    public final void setRetrieved(Instant retrieved) {
        this.retrieved = new Timestamp(retrieved.getMillis());

        if (this.retrieved == null) {
            this.retrieved = createNullTimestamp();
        }
    }

    public final void setPeriodEnd(Instant periodEnd) {
        this.periodEnd = new Timestamp(periodEnd.getMillis());

        if (this.periodEnd == null) {
            this.periodEnd = createNullTimestamp();
        }
    }

    public final void setEnvironment(String environment) {
        this.environment = environment;

        if (this.environment == null) {
            this.environment = "";
        }
    }

    public final void setName(String name) {
        this.name = name;

        if (this.name == null) {
            this.name = "";
        }
    }

    @Override
    public String toString() {
        return String.format("{ '%s', '%s', '%s', '%s', '%s' }", uuid,
                retrieved, periodEnd, environment, name);
    }
}
