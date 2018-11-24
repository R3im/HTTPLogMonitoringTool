package com.httplogmonitoringtool.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;

public class HTTPLogRow {

	private String remoteHost;
	private String authUser;
	private Date reqDate;
	private String reqType;
	private String reqSection;
	private String reqResource;
	private String reqProtocol;
	private int reqSatus = -1;
	private int contentLength = 0;

	private final static int REMOTEHOST_POSITION = 0;
	private final static int AUTHUSER_POSITION = 2;
	private final static int REQDATE_POSITION = 3;
	private final static int REQTYPE_POSITION = 4;
	private final static int REQRESOURCE_POSITION = 5;
	private final static int REQPROTO_POSITION = 6;
	private final static int STATUS_POSITION = 7;
	private final static int CONTENTLENGTH_POSITION = 8;
	private final static SimpleDateFormat dateFormat = new SimpleDateFormat("[dd/MM/yyyy:HH:mm:ss ZZZ]");

	public HTTPLogRow(String line) throws HTTPLogRowFormatException {

		StringBuilder sbWork = new StringBuilder();

		char[] lineChars = line.toCharArray();

		// test bad format log
		if (lineChars.length == 0) {
			throw new HTTPLogRowFormatException("Empty Row");
		} else if (lineChars[0] == ' ') {
			throw new HTTPLogRowFormatException("First char whitespace");
		}

		int position = 0;
		int lastChar = -1;
		char charToListen = ' ';
		for (char currentChar : lineChars) {
			// ignore consecutive spaces
			if (lastChar == charToListen && lastChar == currentChar) {
				continue;
			}

			switch (position) {
			case REMOTEHOST_POSITION:
				if (currentChar == charToListen) {
					position++;
					remoteHost = sbWork.toString();
					if(Strings.isBlank(remoteHost))	{
						throw new HTTPLogRowFormatException("RemoteHost is blank");
					}
					sbWork.setLength(0);
				} else {
					sbWork.append(currentChar);
				}
				break;
			case AUTHUSER_POSITION:
				if (currentChar == charToListen) {
					lastChar = charToListen;
					charToListen = ']';
					position++;
					authUser = sbWork.toString();
					if(Strings.isBlank(authUser))	{
						throw new HTTPLogRowFormatException("AuthUser is blank");
					}
					sbWork.setLength(0);
				} else {
					sbWork.append(currentChar);
				}
				break;
			case REQDATE_POSITION:
				if (currentChar == ']') {
					lastChar = charToListen;
					charToListen = ' ';
				}
				if (currentChar == charToListen) {
					position++;
					try {
						reqDate = dateFormat.parse(sbWork.toString());
					} catch (ParseException ex) {
						throw new HTTPLogRowFormatException("Date bad format");
					}
					sbWork.setLength(0);
				} else {
					sbWork.append(currentChar);
				}
				if (currentChar == ']') {
					lastChar = charToListen;
					charToListen = ' ';
				}
				break;
			case REQTYPE_POSITION:
				if (currentChar == ' ') {
					position++;
					reqType = sbWork.toString();
					if(Strings.isBlank(reqType))	{
						throw new HTTPLogRowFormatException("ReqType is blank");
					}
					sbWork.setLength(0);
				} else if (currentChar != '"') {
					sbWork.append(currentChar);
				}
				break;
			case REQRESOURCE_POSITION:
				if (currentChar == ' ') {
					position++;
					reqResource = sbWork.toString();
					if(Strings.isBlank(reqResource))	{
						throw new HTTPLogRowFormatException("ReqResource is blank");
					}
					else if(!reqResource.startsWith("/"))	{
						throw new HTTPLogRowFormatException("ReqResource bad format");
					}
					// get section
					int secondSlashIndex = reqResource.indexOf('/', 1);
					reqSection = reqResource.substring(0,
							secondSlashIndex > 0 ? secondSlashIndex : reqResource.length());
					sbWork.setLength(0);
				} else if (currentChar != '"') {
					sbWork.append(currentChar);
				}
				break;
			case REQPROTO_POSITION:
				if (currentChar == ' ') {
					position++;
					reqProtocol = sbWork.toString();
					if(Strings.isBlank(reqResource))	{
						throw new HTTPLogRowFormatException("ReqProtocol is blank");
					}
					sbWork.setLength(0);
				} else if (currentChar != '"') {
					sbWork.append(currentChar);
				}
				break;
			case STATUS_POSITION:
				if (currentChar == ' ') {
					position++;
					try {
						reqSatus = Integer.parseInt(sbWork.toString());
					} catch (NumberFormatException ex) {
						throw new HTTPLogRowFormatException("Status bad format");
					}
					sbWork.setLength(0);
				} else {
					sbWork.append(currentChar);
				}
				break;
			case CONTENTLENGTH_POSITION:
				sbWork.append(currentChar);
				break;
			default:
				if (currentChar == ' ') {
					position++;
				}
				break;
			}
			lastChar = currentChar;
		}
		// get content length
		try {
			contentLength = Integer.parseInt(sbWork.toString());
		} catch (NumberFormatException ex) {
			throw new HTTPLogRowFormatException("Content length bad format");
		}
		sbWork.setLength(0);

	}

	public String getRemoteHost() {
		return remoteHost;
	}

	public String getAuthUser() {
		return authUser;
	}

	public Date getReqDate() {
		return reqDate;
	}

	public String getReqType() {
		return reqType;
	}

	public String getReqSection() {
		return reqSection;
	}

	public String getReqResource() {
		return reqResource;
	}

	public String getReqProtocol() {
		return reqProtocol;
	}

	public int getReqSatus() {
		return reqSatus;
	}

	public int getContentLength() {
		return contentLength;
	}

	public static SimpleDateFormat getDateformat() {
		return dateFormat;
	}
}
