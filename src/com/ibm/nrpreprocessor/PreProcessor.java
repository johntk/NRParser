package com.ibm.nrpreprocessor;

import com.ibm.nrpreprocessor.db.DBConnection;
import com.ibm.nrpreprocessor.operations.Consumer;

import java.sql.Connection;

public class PreProcessor {

    public static void main(String args[]) throws Throwable {
//        DBConnection dbConnection = DBConnection.createApplication();

//        try(Connection connection = dbConnection.getConnection()){
            Consumer remoteQInteractor = new Consumer();
            remoteQInteractor.Consume(remoteQInteractor);
            remoteQInteractor.finalize();
        }
//    }
}
