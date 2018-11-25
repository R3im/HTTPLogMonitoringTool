package com.httplogmonitoringtool.models;

import java.util.Date;

/**
 * HTTP log row parsed line
 * 
 * @author remi c
 *
 */
public class HTTPLogRow {

	private String remoteHost;
	private String remoteLogName;
	private String authUser;
	private Date reqDate;
	private String reqType;
	private String reqSection;
	private String reqResource;
	private String reqProtocol;
	private int reqSatus = -1;
	private int contentLength = 0;

	public String getRemoteHost() {
		return remoteHost;
	}

	public void setRemoteHost(String remoteHost) {
		this.remoteHost = remoteHost;
	}

	public String getRemoteLogName() {
		return remoteLogName;
	}

	public void setRemoteLogName(String remoteLogName) {
		this.remoteLogName = remoteLogName;
	}

	public String getAuthUser() {
		return authUser;
	}

	public void setAuthUser(String authUser) {
		this.authUser = authUser;
	}

	public Date getReqDate() {
		return reqDate;
	}

	public void setReqDate(Date reqDate) {
		this.reqDate = reqDate;
	}

	public String getReqType() {
		return reqType;
	}

	public void setReqType(String reqType) {
		this.reqType = reqType;
	}

	public String getReqSection() {
		return reqSection;
	}

	public void setReqSection(String reqSection) {
		this.reqSection = reqSection;
	}

	public String getReqResource() {
		return reqResource;
	}

	public void setReqResource(String reqResource) {
		this.reqResource = reqResource;
	}

	public String getReqProtocol() {
		return reqProtocol;
	}

	public void setReqProtocol(String reqProtocol) {
		this.reqProtocol = reqProtocol;
	}

	public int getReqSatus() {
		return reqSatus;
	}

	public void setReqSatus(int reqSatus) {
		this.reqSatus = reqSatus;
	}

	public int getContentLength() {
		return contentLength;
	}

	public void setContentLength(int contentLength) {
		this.contentLength = contentLength;
	}

}
