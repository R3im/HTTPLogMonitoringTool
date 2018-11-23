package com.httplogmonitoringtool;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.util.Integers;

import com.httplogmonitoringtool.model.HTTPStats;
import com.httplogmonitoringtool.model.HTTPStatsAlert;
import com.httplogmonitoringtool.model.HTTPStatsType;

public class Main {
	private final static int STATS_REFRESHING_FREQUENCY = 1000 * 10;
	
	private static MonitorLog monitorLogs;
	private final static int CONSOLE_WIDTH = 100;
	private final static Logger logger = LogManager.getLogger(Main.class.getName());
	private final static String os = System.getProperty("os.name");
	private static Date lastStatsRefreshingTime = new Date();
	
	public static void main(String[] args) throws IOException {

		monitorLogs = new MonitorLog();
		
		boolean showHelp = false;
		for (int i = 0; i < args.length; i++) {
			String arg = args[i];
			if (arg.equals("--help") || arg.equals("-h") || arg.equals("-?")) {
				showHelp = true;
			} else if (arg.equals("-log") || arg.equals("-l")) {
				if (arg.length() <= i + 1) {
					showHelp = true;
				} else {
					monitorLogs.setLogFilePath(args[i + 1]);
				}
			} else if (arg.equals("-alert_treshold") || arg.equals("-at")) {
				if (arg.length() <= i + 1) {
					showHelp = true;
				} else {
					try {
						monitorLogs.setAlertAverageTreshold(Integers.parseInt(args[i + 1]));
						if (monitorLogs.getAlertAverageTreshold() <= 0) {
							showHelp = true;
						}
					} catch (NumberFormatException e) {  
						showHelp = true;
					} 
				}
			}
			if (showHelp) {
				appendLogFilled();
				appendLog('-', "--- HTTP TRAFFIC MONITORING LOG ");
				appendLogFilled();
				appendLog('-', "--- HELP ");
				logger.debug(" USAGE: HTTPLogMonitoringTool [option...] [--help] ");
				logger.debug(" -?, -h, --help \t\tShows this help message.");
				logger.debug(" -log, -l \t\tSet log file fullpath.");
				logger.debug(" -alert_treshold, -at \t\tSet alert treshhold (>0).");
				appendLog(" ");
				appendLogFilled();
				System.exit(0);
			}
		}

		// exit if file does not exists
		if (!Files.exists(Paths.get(monitorLogs.getLogFilePath()))) {
			appendLog("File ", monitorLogs.getLogFilePath(), " does not exists.");
			System.exit(0);
		}
		
		startMonitoring();
	}
	


	public static void startMonitoring() {

		// init consol stats
		refreshStats();
		while (true) {
			monitorLogs.updateStats();

			// refresh stats
			Date currentTime = new Date();
			if (currentTime.getTime() - lastStatsRefreshingTime.getTime() >= STATS_REFRESHING_FREQUENCY) {
				lastStatsRefreshingTime = currentTime;
				refreshStats();
			}
		}
	}
	

	private static void refreshStats() {
		try {
			if (os.contains("Windows")) {
				Runtime.getRuntime().exec("cls");
			} else {
				Runtime.getRuntime().exec("clear");
			}
		} catch (final IOException e) {
			for (int i = 0; i < 20; ++i)
				logger.debug("");
		}
//		appendLog(".");
//		appendLog("...............HTTP traffic monitoring log");
//		appendLog(".");
//		appendLog("....Total stats");
//		logStats(totalLogStats);
//		appendLog(".");
//		appendLog("....Last ", "" + STATS_REFRESHING_FREQUENCY / 1000, " seconds stats");
//		logStats(shortLogStats);
//		appendLog(".");
		logStats();

		// clear short stast
		monitorLogs.getLogStats().clearSections();

		// display alerts
		if (!monitorLogs.getRisedAlerts().isEmpty()) {
			appendLog("     !!! ALERTS !!!");
			appendLog(" ");
			for (HTTPStatsAlert alert : monitorLogs.getRisedAlerts()) {
				appendLog(alert.toString());
			}
			appendLogFilled();
		}
	}

	private static void logStats() {
		appendLogFilled();
		appendLog('-', "--- HTTP TRAFFIC MONITORING LOG ");
		appendLogFilled();
		appendLog('-', "--- OVERALL REQUESTS ");
		appendLog(" ");

		HashMap<HTTPStatsType, Integer> statsValues = monitorLogs.getLogStats().getStatsValues();

		// stats value
		int count = 0;
		StringBuilder statLogRow = new StringBuilder();
		for (Entry<HTTPStatsType, Integer> entry : statsValues.entrySet()) {
			count++;
			statLogRow.append(entry.getKey().toString());
			statLogRow.append(": ");
			statLogRow.append(entry.getValue().toString());
			if (HTTPStatsType.TOTAL_CONTENT == entry.getKey()) {
				statLogRow.append(entry.getValue() > 0 ? " bytes" : " byte");
			}
			if (count % 3 == 0 || count == statsValues.size()) {
				appendLog(statLogRow.toString());
				statLogRow.setLength(0);
			} else {
				statLogRow.append("       ");
			}
		}
		appendLog(" ");
		appendLog('-', "--- LAST ", "" + STATS_REFRESHING_FREQUENCY / 1000, " SECONDS ");
		appendLog(" ");

		HashMap<String, Integer> hitSections = monitorLogs.getLogStats().getMostHitSection();

		appendLog("Most hit section (", "" + hitSections.size(), "/", "" + hitSections.size(), "):");

		count = 0;
		for (Entry<String, Integer> entry : hitSections.entrySet()) {
			count++;
			statLogRow.append("\"");
			statLogRow.append(entry.getKey());
			statLogRow.append("\":");
			statLogRow.append(entry.getValue().toString());

			if (count % 3 == 0 || count == hitSections.size()) {
				appendLog(statLogRow.toString());
				statLogRow.setLength(0);
			} else {
				statLogRow.append("   ");
			}
		}

		for (int i = 0; i < (HTTPStats.MOST_HIT_SECTION_DISPLAYED - hitSections.size()) / 3; i++) {
			appendLog(" ");
		}
		appendLog(" ");
		appendLogFilled();

	}
	
	private static void appendLogFilled() {
		StringBuilder logSB = new StringBuilder();
		for (int i = 0; i < CONSOLE_WIDTH; i++) {
			logSB.append("-");
		}
		logger.debug(logSB.toString());
	}

	private static void appendLog(String... messages) {
		appendLog(' ', messages);
	}

	private static void appendLog(char fillChar, String... messages) {
		StringBuilder logSB = new StringBuilder();
		logSB.append("-" + fillChar + fillChar);
		for (String message : messages) {
			logSB.append(message);
		}
		// complete row with -
		int sbLength = logSB.length();
		for (int i = 0; i < CONSOLE_WIDTH - sbLength - 1; i++) {
			logSB.append(fillChar);
		}
		logSB.append("-");
		logger.debug(logSB.toString());
	}
}
