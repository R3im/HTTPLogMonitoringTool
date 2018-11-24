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
	private final static boolean isWindows = System.getProperty("os.name").toLowerCase().contains("windows");
	private static Date lastStatsRefreshingTime = new Date();

	public static void main(String[] args) {

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
		clearConsole();

		logStats();

		// clear short stats
		monitorLogs.getLogStats().clearSections();
		monitorLogs.getLogStats().clearUsers();
		monitorLogs.getLogStats().clearRemoteHosts();

		// display alerts
		if (!monitorLogs.getRaisedAlerts().isEmpty()) {
			appendLogTitle(" !!! ALERTS !!! ");
			appendLog(" ");
			for (HTTPStatsAlert alert : monitorLogs.getRaisedAlerts()) {
				appendLog(alert.toString());
			}
			appendLog(" ");
			appendLogFilled();
		}
	}

	private static void logStats() {
		appendLogFilled();

		appendLogTitle(" HTTP TRAFFIC MONITORING LOG ");
		appendLogFilled();
		appendLogTitle(" OVERALL REQUESTS ");
		appendLog(" ");

		HashMap<HTTPStatsType, Long> statsValues = monitorLogs.getLogStats().getStatsValues();

		// stats value
		int count = 0;
//		int countVisible = 0;
		StringBuilder statLogRow = new StringBuilder();
		for (HTTPStatsType httpStatsType : HTTPStatsType.values()) {
			Long value = statsValues.get(httpStatsType);
			count++;
//			if (entry.getValue() > 0 || entry.getKey().getCode() == -1) {
//				countVisible++;
			statLogRow.append(httpStatsType);
			statLogRow.append(": ");
			statLogRow.append(value);
			if (HTTPStatsType.TOTAL_CONTENT == httpStatsType) {
				statLogRow.append(value > 0 ? " bytes" : " byte");
			}
//			}
			if (count % 2 == 0 || count == statsValues.size()) {
				appendLog(statLogRow.toString());
				statLogRow.setLength(0);
			} else {
				// fill last part
				for (int i = CONSOLE_WIDTH / 2 - statLogRow.length(); i >= 0; i--) {
					statLogRow.append(" ");
				}
//				statLogRow.append("- ");
			}
		}
		String alertTimeWindowUnit = "s";
		int reducedAlertTimeWindow = monitorLogs.getAlertTimeWindow() / 1000;
		if (reducedAlertTimeWindow >= 60) {
			alertTimeWindowUnit = "min";
			reducedAlertTimeWindow = reducedAlertTimeWindow / 60;
		}
		appendLog(" ");
		appendLog("Last ", reducedAlertTimeWindow + "", alertTimeWindowUnit + "", " traffic average: ",
				monitorLogs.getLogStats().getAlertAverage() + "", " requests/s");
		appendLog(" ");
		appendLogTitle(" LAST " + STATS_REFRESHING_FREQUENCY / 1000 + " SECONDS ");
		appendLog(" ");
		appendLog("Most present user: ", monitorLogs.getLogStats().getTopUser());
		appendLog("Most present remote host: ", monitorLogs.getLogStats().getTopRemoteHost());

		HashMap<String, Integer> hitSections = monitorLogs.getLogStats().getMostHitSection();

		appendLog("Most hit section (", "" + hitSections.size(), "/", "" + hitSections.size(), "):");

		count = 0;
		int partCount = 0;
		for (Entry<String, Integer> entry : hitSections.entrySet()) {
			count++;
			statLogRow.append("\"");
			statLogRow.append(entry.getKey());
			statLogRow.append("\": ");
			statLogRow.append(entry.getValue().toString());

			if (count % 3 == 0 || count == hitSections.size()) {
				appendLog(statLogRow.toString());
				statLogRow.setLength(0);
			} else {
				partCount++;
				// fill last part
				for (int i = partCount * CONSOLE_WIDTH / 3 - statLogRow.length(); i >= 0; i--) {
					statLogRow.append(" ");
				}
//				statLogRow.append("   ");
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

	private static void appendLogTitle(String title) {
		StringBuilder statLogRow = new StringBuilder();
		// fill last part
		for (int i = CONSOLE_WIDTH / 2 - title.length() / 2 - 6; i >= 0; i--) {
			statLogRow.append("-");
		}
		statLogRow.append(title);
		appendLog('-', statLogRow.toString());
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

	private static void clearConsole() {
//		try {
//			if (isWindows) {
//				Runtime.getRuntime().exec("cls").waitFor();
//			} else {
//				Runtime.getRuntime().exec("clear");
//			}
//		} catch (final IOException | InterruptedException e) {
		for (int i = 0; i < 50; ++i) {
			logger.debug("");
		}
//		}
	}
}
