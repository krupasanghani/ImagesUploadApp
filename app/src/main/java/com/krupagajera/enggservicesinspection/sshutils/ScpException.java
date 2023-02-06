package com.krupagajera.enggservicesinspection.sshutils;

public class ScpException extends Exception{
    //private static final long serialVersionUID=-5616888495583253811L;
    public int id;
    private Throwable cause=null;
    public ScpException(int id, String message) {
        super(message);
        this.id=id;
    }
    public ScpException(int id, String message, Throwable e) {
        super(message);
        this.id=id;
        this.cause=e;
    }
    public String toString(){
        return id+": "+getMessage();
    }
    public Throwable getCause(){
        return this.cause;
    }
}

