package com.krupagajera.enggservicesinspection.sshutils;

/**
 * Interface that must be implemented by any object wishing to Listen
 * to the Jsch connection status (either connected or disconnected).
 */
public interface ConnectionStatusListener {

    /**
     * Handles event of Session not connected
     */
    public void onDisconnected();

    /**
     * Handles event of Session connected
     */
    public void onConnected();
}
