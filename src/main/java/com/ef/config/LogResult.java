package com.ef.config;

public class LogResult {

    private String ipAddr; 
    private int ipCount = 0;
    private String theReason;

    public LogResult( String ipAddr, Integer ipCount, String theReason ) {
        this.ipAddr = ipAddr;
        this.ipCount = ipCount;
        this.theReason = theReason;
    }

    public String getIpAddr() {
        return ipAddr;
    }

    public void setIpAddr( String ipAddr ) {
        this.ipAddr = ipAddr;
    }

    public int getIpCount() {
        return ipCount;
    }

    public void setIpCount( int ipCount ) {
        this.ipCount = ipCount;
    }

    public String getTheReason() {
        return theReason;
    }

    public void setTheReason( String theReason ) {
        this.theReason = theReason;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append( ipAddr );
        sb.append( ',' );
        sb.append( ipCount );
        sb.append( ',' );
        sb.append( theReason );

        return sb.toString();
    }
}
