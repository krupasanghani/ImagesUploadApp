package com.krupagajera.enggservicesinspection.sshutils;

import com.jcraft.jsch.ChannelSftp;

import java.util.Vector;

public interface TaskCallbackHandler {

    /**
     * Called when remote process begins.
     */
    public void OnBegin();

    /**
     * Called when remote process fails.
     */
    public void onFail();

    /**
     * Called when remote process is completed.
     * @param lsEntries Vector of JSch LsEntry items, the contents of the remote current directory.
     */
    public void onTaskFinished(Vector<ChannelSftp.LsEntry> lsEntries);
}
