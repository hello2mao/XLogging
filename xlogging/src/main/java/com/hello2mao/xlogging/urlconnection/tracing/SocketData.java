package com.hello2mao.xlogging.urlconnection.tracing;


public abstract class SocketData {

    //int a
    protected int eventtype;
    // String b
    protected String target;
    // int c
    protected int duration;
    // d
    protected int network_error_code;
    // f
    protected String desc;
    protected boolean isHttp;
    protected boolean isSend;
    
    public SocketData() {
        this.target = "";
        this.desc = "";
        this.isHttp = false;
        this.isSend = false;
    }
    
    public boolean isSend() {
        return this.isSend;
    }
    
    public boolean isHttp() {
        return this.isHttp;
    }
    
    public void setHttp(final boolean ishttp) {
        this.isHttp = ishttp;
    }
    
    public void setSend(final boolean isSend) {
        this.isSend = isSend;
    }
    
    public void setEventtype(final int eventtype) {
        this.eventtype = eventtype;
    }
    
    public void setTarget(final String target) {
        this.target = target;
    }
    
    public void setDuration(final int duration) {
        this.duration = duration;
    }
    
    public int getDuration() {
        return this.duration;
    }
    
    public void setNetworkErrorCode(final int network_error_code) {
        this.network_error_code= network_error_code;
    }
    
    public void setDesc(final String desc) {
        this.desc = desc;
    }

    public abstract void setDataFormat();

    @Override
    public String toString() {
        return "SocketData: eventtype:" + this.eventtype
                + ",target = " + this.target
                + ", duration = " + this.duration
                + ", network_error_code = " + this.network_error_code
                + ", desc = " + this.desc;
    }
}
