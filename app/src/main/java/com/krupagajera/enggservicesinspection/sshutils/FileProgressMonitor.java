package com.krupagajera.enggservicesinspection.sshutils;

import com.jcraft.jsch.SftpProgressMonitor;

public interface FileProgressMonitor extends SftpProgressMonitor {
    void onFail();
}
