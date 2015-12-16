package com.ibm.nrpreprocessor;


import com.ibm.nrpreprocessor.operations.Consumer;

public class PreProcessor {

    public static void main(String args[]) throws Throwable {
        Consumer remoteQInteractor = new Consumer();
        remoteQInteractor.Consume(remoteQInteractor);
        remoteQInteractor.finalize();
    }
}
