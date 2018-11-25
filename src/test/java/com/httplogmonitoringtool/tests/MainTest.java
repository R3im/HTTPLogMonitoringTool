package com.httplogmonitoringtool.tests;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Scanner;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;

import com.httplogmonitoringtool.Main;

public class MainTest {
	
    @Rule
    public final ExpectedSystemExit exit = ExpectedSystemExit.none();
	
	private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
	private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
	private final PrintStream originalOut = System.out;
	private final PrintStream originalErr = System.err;

	@Before
	public void setUpStreams() {
		System.setOut(new PrintStream(outContent));
		System.setErr(new PrintStream(errContent));
	}

	@After
	public void restoreStreams() {
		System.setOut(originalOut);
		System.setErr(originalErr);
	}

	@Test
	public void testMain() {
		try {
	        exit.expectSystemExit();
			Main.main(new String[] { "-l", "/var/log/file_not_existing.log" });
			Assert.fail("Should be exited");
		} finally {
			Assert.assertTrue("Message file does not exists not found",
					outContent.toString().contains("does not exists."));
		}
		try {
	        exit.expectSystemExit();
			Main.main(new String[] { "-h" });
			Assert.fail("Should be exited");
		} finally {
			Assert.assertTrue("Message does not show help",
					outContent.toString().contains("HELP"));
		}


	}
	
}
