package com.ef;

import java.io.Serializable;
import java.util.Date;

public class LogRegistration implements Serializable {

    private static final long serialVersionUID = 1L;

    private Date reqTm; 
    private String ipAddress;
    private String reqType;
    private String respCode;
    private String userAgent;

    public LogRegistration() {
    }

    public LogRegistration(Date reqTm, String ipAddress, String reqType, String respCode, String userAgent) {
        super();
        this.reqTm = reqTm;
        this.ipAddress = ipAddress;
        this.reqType = reqType;
        this.respCode = respCode;
        this.userAgent = userAgent;
    }

	public Date getReqTm() {
		return reqTm;
	}

	public void setReqTm(Date reqTm) {
		this.reqTm = reqTm;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public String getReqType() {
		return reqType;
	}

	public void setReqType(String reqType) {
		this.reqType = reqType;
	}

	public String getRespCode() {
		return respCode;
	}

	public void setRespCode(String respCode) {
		this.respCode = respCode;
	}

	public String getUserAgent() {
		return userAgent;
	}

	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}


}
