package com.ibm.nrpreprocessor;

/**
 * Created by John TK on 24/11/2015.
 */
public class PreProcessor {

    public static void main(String agrs[]) throws Throwable {
        Consumer remoteQInteractor = new Consumer();
        remoteQInteractor.Consume(remoteQInteractor);
        remoteQInteractor.finalize();
    }
}
