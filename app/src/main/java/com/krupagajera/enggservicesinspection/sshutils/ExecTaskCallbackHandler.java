package com.krupagajera.enggservicesinspection.sshutils;

public interface ExecTaskCallbackHandler {

    void onFail();

    void onComplete(String completeString);
}
