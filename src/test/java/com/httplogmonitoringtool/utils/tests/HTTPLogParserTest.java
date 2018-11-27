package com.httplogmonitoringtool.utils.tests;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.junit.Assert;
import org.junit.Test;

import com.httplogmonitoringtool.exceptions.HTTPLogRowFormatException;
import com.httplogmonitoringtool.models.HTTPLogRow;
import com.httplogmonitoringtool.utils.HTTPLogParser;

public class HTTPLogParserTest {

	private final static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("[dd/MMM/yyyy:HH:mm:ss ZZZ]");

	@Test
	public void testParseSuccess() {
		String logRow = "127.0.0.1 - lily [24/Nov/2222:16:05:35 +0100] \"DELETE /sport/volleyball HTTP/1.0\" 300 8";
		
		try {
			HTTPLogRow row = HTTPLogParser.parse(logRow);
			Assert.assertEquals("Bad RemoteHost","127.0.0.1", row.getRemoteHost());
			Assert.assertEquals("Bad AuthUser","lily", row.getAuthUser());
			Assert.assertEquals("Bad ReqDate",DATE_FORMAT.parse("[24/Nov/2222:16:05:35 +0100]"), row.getReqDate());
			Assert.assertEquals("Bad ReqType","DELETE", row.getReqType());
			Assert.assertEquals("Bad ReqResource","/sport/volleyball", row.getReqResource());
			Assert.assertEquals("Bad ReqSection","/sport", row.getReqSection());
			Assert.assertEquals("Bad ReqProtocol","HTTP/1.0", row.getReqProtocol());
			Assert.assertEquals("Bad ReqStatus",300, row.getReqSatus());
			Assert.assertEquals("Bad ContentLength",8, row.getContentLength());
		} catch (HTTPLogRowFormatException | ParseException e) {
			Assert.fail(e.getMessage());
		}

	}

	@Test
	public void testParseFail() {

		try {
			HTTPLogParser.parse(null);
			Assert.fail("Should throw Exception");
		} catch (HTTPLogRowFormatException e) {
			Assert.assertTrue("Bad exception: "+e.getMessage(),e.getMessage().equals("Empty Row"));
		}
		try {
			HTTPLogParser.parse("");
			Assert.fail("Should throw Exception");
		} catch (HTTPLogRowFormatException e) {
			Assert.assertTrue("Bad exception: "+e.getMessage(),e.getMessage().equals("Empty Row"));
		}
		try {
			HTTPLogParser.parse(" 127.0.0.1 - lily [24/Nov/2018:16:05:35 +0100] \"DELETE /sport/volleyball HTTP/1.0\" 300 8");
			Assert.fail("Should throw Exception");
		} catch (HTTPLogRowFormatException e) {
			Assert.assertTrue("Bad exception: "+e.getMessage(),e.getMessage().equals("First char whitespace"));
		}
		try {
			HTTPLogParser.parse("127.0.0.1  lily [24/Nov/2018:16:05:35 +0100] \"DELETE /sport/volleyball HTTP/1.0\" 300 8");
			Assert.fail("Should throw Exception");
		} catch (HTTPLogRowFormatException e) {
			Assert.assertTrue("Bad exception: "+e.getMessage(),e.getMessage().equals("RemoteLogName is blank"));
		}
		try {
			HTTPLogParser.parse("127.0.0.1 - lily [24/Nov/aaa:16:05:35 +0100] \"DELETE /sport/volleyball HTTP/1.0\" 300 8");
			Assert.fail("Should throw Exception");
		} catch (HTTPLogRowFormatException e) {
			Assert.assertTrue("Bad exception: "+e.getMessage(),e.getMessage().equals("ReqDate bad format"));
		}
		try {
			HTTPLogParser.parse("127.0.0.1 - li ly [24/Nov/2018:16:05:35 +0100] \"DELETE /sport/volleyball HTTP/1.0\" 300 8");
			Assert.fail("Should throw Exception");
		} catch (HTTPLogRowFormatException e) {
			Assert.assertTrue("Bad exception: "+e.getMessage(),e.getMessage().equals("ReqDate bad format"));
		}
		try {
			HTTPLogParser.parse("127.0.0.1 - lily [24/Nov/2018:16:05:35 +0100] DELETE /sport/volleyball HTTP/1.0\" 300 8");
			Assert.fail("Should throw Exception");
		} catch (HTTPLogRowFormatException e) {
			Assert.assertTrue("Bad exception: "+e.getMessage(),e.getMessage().equals("ReqType bad format"));
		}
		try {
			HTTPLogParser.parse("127.0.0.1 - lily [24/Nov/2018:16:05:35 +0100] \"DELETE /sport/ volleyball HTTP/1.0\" 300 8");
			Assert.fail("Should throw Exception");
		} catch (HTTPLogRowFormatException e) {
			Assert.assertTrue("Bad exception: "+e.getMessage(),e.getMessage().equals("ReqProtocol bad format"));
		}
		try {
			HTTPLogParser.parse("127.0.0.1 - lily [24/Nov/2018:16:05:35 +0100] \"DELETE /sport/volleyball HTTP/1.0 300 8");
			Assert.fail("Should throw Exception");
		} catch (HTTPLogRowFormatException e) {
			Assert.assertTrue("Bad exception: "+e.getMessage(),e.getMessage().equals("ReqProtocol bad format"));
		}
		try {
			HTTPLogParser.parse("127.0.0.1 - lily [24/Nov/2018:16:05:35 +0100] \"DELETE /sport/volleyball HTTP/1.0\" zzz 8");
			Assert.fail("Should throw Exception");
		} catch (HTTPLogRowFormatException e) {
			Assert.assertTrue("Bad exception: "+e.getMessage(),e.getMessage().equals("ReqStatus bad format"));
		}
		try {
			HTTPLogParser.parse("127.0.0.1 - lily [24/Nov/2018:16:05:35 +0100] \"DELETE /sport/volleyball HTTP/1.0\" 300 gg");
			Assert.fail("Should throw Exception");
		} catch (HTTPLogRowFormatException e) {
			Assert.assertTrue("Bad exception: "+e.getMessage(),e.getMessage().equals("Content length bad format"));
		}
		try {
			HTTPLogParser.parse("127.0.0.1 - lily [24/Nov/2018:16:05:35 +0100] ");
			Assert.fail("Should throw Exception");
		} catch (HTTPLogRowFormatException e) {
			Assert.assertTrue("Bad exception: "+e.getMessage(),e.getMessage().equals("Bad format"));
		}
		try {
			HTTPLogParser.parse("127.0.0.1 -  [24/Nov/2018:16:05:35 +0100] \"DELETE /sport/volleyball HTTP/1.0\" 66 8");
			Assert.fail("Should throw Exception");
		} catch (HTTPLogRowFormatException e) {
			Assert.assertTrue("Bad exception: "+e.getMessage(),e.getMessage().equals("AuthUser is blank"));
		}
		try {
			HTTPLogParser.parse("127.0.0.1 - lily  \"DELETE /sport/volleyball HTTP/1.0\" 66 8");
			Assert.fail("Should throw Exception");
		} catch (HTTPLogRowFormatException e) {
			Assert.assertTrue("Bad exception: "+e.getMessage(),e.getMessage().equals("ReqDate bad format"));
		}
		try {
			HTTPLogParser.parse("127.0.0.1 - lala [24/Nov/2018:16:05:35 +0100] \" /sport/volleyball HTTP/1.0\" 66 8");
			Assert.fail("Should throw Exception");
		} catch (HTTPLogRowFormatException e) {
			Assert.assertTrue("Bad exception: "+e.getMessage(),e.getMessage().equals("ReqType is blank"));
		}
		try {
			HTTPLogParser.parse("127.0.0.1 - lily [24/Nov/2018:16:05:35 +0100] \"DELETE  HTTP/1.0\" 66 8");
			Assert.fail("Should throw Exception");
		} catch (HTTPLogRowFormatException e) {
			Assert.assertTrue("Bad exception: "+e.getMessage(),e.getMessage().equals("ReqResource is blank"));
		}
		try {
			HTTPLogParser.parse("127.0.0.1 - lily [24/Nov/2018:16:05:35 +0100] \"DELETE /sport/volleyball \" 66 8");
			Assert.fail("Should throw Exception");
		} catch (HTTPLogRowFormatException e) {
			Assert.assertTrue("Bad exception: "+e.getMessage(),e.getMessage().equals("ReqProtocol is blank"));
		}
		try {
			HTTPLogParser.parse("127.0.0.1 - lily [24/Nov/2018:16:05:35 +0100] \"DELETE /sport/volleyball HTTP/1.0\"  8");
			Assert.fail("Should throw Exception");
		} catch (HTTPLogRowFormatException e) {
			Assert.assertTrue("Bad exception: "+e.getMessage(),e.getMessage().equals("ReqStatus bad format"));
		}
		try {
			HTTPLogParser.parse("127.0.0.1 - lily [24/Nov/2018:16:05:35 +0100] \"DELETE /sport/volleyball HTTP/1.0\" 66 ");
			Assert.fail("Should throw Exception");
		} catch (HTTPLogRowFormatException e) {
			Assert.assertTrue("Bad exception: "+e.getMessage(),e.getMessage().equals("Bad format"));
		}

	}
	
	
}
