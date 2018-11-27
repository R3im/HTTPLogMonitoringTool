package com.httplogmonitoringtool.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.apache.logging.log4j.util.Strings;

import com.httplogmonitoringtool.exceptions.HTTPLogRowFormatException;
import com.httplogmonitoringtool.models.HTTPLogRow;

/**
 * HTTP log parser
 * 
 * @author Remi c
 *
 */
public class HTTPLogParser {

	// fields position in log line
	private final static int REMOTEHOST_POSITION = 0;
	private final static int REMOTELOGNAME_POSITION = 1;
	private final static int AUTHUSER_POSITION = 2;
	private final static int REQDATE_POSITION = 3;
	private final static int REQTYPE_POSITION = 4;
	private final static int REQRESOURCE_POSITION = 5;
	private final static int REQPROTO_POSITION = 6;
	private final static int STATUS_POSITION = 7;
	private final static int CONTENTLENGTH_POSITION = 8;

	// date format
	private final static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("[dd/MMM/yyyy:HH:mm:ss ZZZ]");

	/**
	 * Parse log line based on ' ', '[', ']', '"'
	 * 
	 * @see <a href="https://www.w3.org/Daemon/User/Config/Logging.html#LogFormat">W3C httpd format</a>
	 * 
	 * @param line
	 * @return HTTPLogRow
	 * @throws HTTPLogRowFormatException
	 */
	public static HTTPLogRow parse(String line) throws HTTPLogRowFormatException {

		if (line == null) {
			throw new HTTPLogRowFormatException("Empty Row");
		}

		HTTPLogRow row = new HTTPLogRow();

		StringBuilder sbWork = new StringBuilder();

		char[] lineChars = line.toCharArray();

		// test bad format log
		if (lineChars.length == 0) {
			throw new HTTPLogRowFormatException("Empty Row");
		} else if (lineChars[0] == ' ') {
			throw new HTTPLogRowFormatException("First char whitespace");
		}

		int position = 0;// start position
		char charToListen = ' ';// when aimed it passes to the next field
		for (char currentChar : lineChars) {
			switch (position) {
			case REMOTEHOST_POSITION:
				if (currentChar == charToListen) {
					position++;
					row.setRemoteHost(sbWork.toString());
					if (Strings.isBlank(row.getRemoteHost())) {// prevent blank value
						throw new HTTPLogRowFormatException("RemoteHost is blank");
					}
					sbWork.setLength(0);
					continue;
				}
				break;
			case REMOTELOGNAME_POSITION:
				if (currentChar == charToListen) {
					position++;
					row.setRemoteLogName(sbWork.toString());
					if (Strings.isBlank(row.getRemoteLogName())) {// prevent blank value
						throw new HTTPLogRowFormatException("RemoteLogName is blank");
					}
					sbWork.setLength(0);
					continue;
				}
				break;
			case AUTHUSER_POSITION:
				if (currentChar == charToListen) {
					charToListen = '[';
					position++;
					row.setAuthUser(sbWork.toString());
					if (Strings.isBlank(row.getAuthUser())) {// prevent blank value
						throw new HTTPLogRowFormatException("AuthUser is blank");
					}
					sbWork.setLength(0);
					continue;
				}
				break;
			case REQDATE_POSITION:
				if (currentChar == '[') {// to ignore ' ' until end of date
					charToListen = ']';
				} else if (currentChar == ']') {// to ignore ' ' until end of date
					charToListen = ' ';
				} else if (charToListen == '[' && sbWork.length() > 0) {// first char must be '['
					throw new HTTPLogRowFormatException("ReqDate bad format");
				}

				if (currentChar == charToListen) {
					charToListen = '"';
					position++;
					try {
						row.setReqDate(DATE_FORMAT.parse(sbWork.toString()));
					} catch (ParseException ex) {
						throw new HTTPLogRowFormatException("ReqDate bad format");
					}
					sbWork.setLength(0);
					continue;
				}
				break;
			case REQTYPE_POSITION:
				if (currentChar == '"' && charToListen == '"') {// to ignore first '"'
					charToListen = ' ';
					continue;
				} else if (charToListen == '"' && sbWork.length() > 0) {// first char must be '"'
					throw new HTTPLogRowFormatException("ReqType bad format");
				}
				if (currentChar == ' ') {
					position++;
					row.setReqType(sbWork.toString());
					if (Strings.isBlank(row.getReqType())) {// prevent blank value
						throw new HTTPLogRowFormatException("ReqType is blank");
					}
					sbWork.setLength(0);
					continue;
				}
				break;
			case REQRESOURCE_POSITION:
				if (currentChar == ' ') {
					charToListen = '"';
					position++;
					row.setReqResource(sbWork.toString());
					if (Strings.isBlank(row.getReqResource())) {// prevent blank value
						throw new HTTPLogRowFormatException("ReqResource is blank");
					} else if (!row.getReqResource().startsWith("/")) {
						throw new HTTPLogRowFormatException("ReqResource bad format");
					}
					// get section
					int secondSlashIndex = row.getReqResource().indexOf('/', 1);
					row.setReqSection(row.getReqResource().substring(0,
							secondSlashIndex > 0 ? secondSlashIndex : row.getReqResource().length()));
					sbWork.setLength(0);
					continue;
				}
				break;
			case REQPROTO_POSITION:
				if (currentChar == '"' && charToListen == '"') {// to ignore last '"'
					charToListen = ' ';
					continue;
				} else if (charToListen == '"' && currentChar == ' ') {// last char must be '"'
					throw new HTTPLogRowFormatException("ReqProtocol bad format");
				}
				if (currentChar == ' ') {
					position++;
					row.setReqProtocol(sbWork.toString());
					if (Strings.isBlank(row.getReqProtocol())) {// prevent blank value
						throw new HTTPLogRowFormatException("ReqProtocol is blank");
					}
					sbWork.setLength(0);
					continue;
				}
				break;
			case STATUS_POSITION:
				if (currentChar == ' ') {
					position++;
					try {
						row.setReqSatus(Integer.parseInt(sbWork.toString()));
					} catch (NumberFormatException ex) {
						throw new HTTPLogRowFormatException("ReqStatus bad format");
					}
					sbWork.setLength(0);
					continue;
				}
				break;
			case CONTENTLENGTH_POSITION:
				break;
			default:
				throw new HTTPLogRowFormatException("Bad format");
			}
			sbWork.append(currentChar);
		}
		// check empty end string builder
		if (sbWork.length() == 0) {
			throw new HTTPLogRowFormatException("Bad format");
		}
		// get content length
		try {
			row.setContentLength(Integer.parseInt(sbWork.toString()));
		} catch (NumberFormatException ex) {
			throw new HTTPLogRowFormatException("Content length bad format");
		}
		sbWork.setLength(0);

		return row;
	}

}
