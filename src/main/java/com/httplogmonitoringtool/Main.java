package com.httplogmonitoringtool;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;

import org.apache.logging.log4j.core.util.Integers;

import com.httplogmonitoringtool.model.HTTPStats;
import com.httplogmonitoringtool.model.HTTPStatsAlert;
import com.httplogmonitoringtool.model.HTTPStatsStatus;
import com.httplogmonitoringtool.model.HTTPStatsType;

/**
 * 
 * log MonitorLog results into console
 * 
 * @author Remi c
 *
 */
public class Main {

	/**
	 * refreshing console log statistics
	 */
	private final static int STATS_REFRESHING_FREQUENCY = 1000 * 10;

	/**
	 * console width for console log design
	 */
	private final static int CONSOLE_WIDTH = 100;

	/**
	 * MonitorLog instance: Managing the HTTP traffic log parsing and statistic
	 * processing
	 */
	private static MonitorLog monitorLogs = new MonitorLog();

	/**
	 * last statistic refreshing time since the last #STATS_REFRESHING_FREQUENCY
	 */
	private static Date lastStatsRefreshingTime = new Date();

	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		// argument processing
		for (int i = 0; i < args.length; i++) {
			String arg = args[i];

			if (arg.equals("--help") || arg.equals("-h") || arg.equals("-?")) {// help option
				showHelpLog();
			} else if (arg.equals("-log") || arg.equals("-l")) {// HTTP log path option
				if (arg.length() <= i + 1) {// bad parameter
					showBadParameterLog();
				} else {
					monitorLogs.setLogFilePath(args[i + 1]);
				}
			} else if (arg.equals("-alert_threshold") || arg.equals("-at")) {// Alert threshold option
				if (arg.length() <= i + 1) {// bad parameter
					showBadParameterLog();
				} else {
					try {
						monitorLogs.setAlertAverageThreshold(Integers.parseInt(args[i + 1]));
						if (monitorLogs.getAlertAverageThreshold() <= 0) {// bad parameter
							showBadParameterLog();
						}
					} catch (NumberFormatException e) {// bad parameter
						showBadParameterLog();
					}
				}
			}
		}

		// test if file does not exists
		if (!Files.exists(Paths.get(monitorLogs.getLogFilePath()))) {
			appendLog("File ", monitorLogs.getLogFilePath(), " does not exists.");
			System.exit(0);
		}

		// starts monitoring logs
		startMonitoring();
	}

	/**
	 * show help console log
	 */
	private static void showHelpLog() {
		appendLogFilled();
		appendLogTitle(" HTTP TRAFFIC MONITORING LOG ");
		appendLogFilled();
		appendLogTitle(" HELP ");
		appendLogFilled();
		System.out.println(" USAGE: HTTPLogMonitoringTool [option...] [--help] ");
		System.out.println(" -?, -h, --help \t\tShows this help message.");
		System.out.println(" -log, -l \t\tSet HTTP log file fullpath.");
		System.out.println(" -alert_threshold, -at \t\tSet alert threshold (>0).");
		appendLog('-', "-");
		appendLogFilled();
		System.exit(0);
	}

	/**
	 * show bad parameter console log also showing help
	 */
	private static void showBadParameterLog() {
		System.out.println(" BAD PARAMETER !!!");
		showHelpLog();
	}

	/**
	 * starts monitoring HTTP logs
	 */
	private static void startMonitoring() {

		// initialize console statistics console log
		refreshStatsLog();

		// infinite loop continually updating statistics and refreshing console log
		// every #STATS_REFRESHING_FREQUENCY
		while (true) {

			// read logs and update statistics
			monitorLogs.updateStats();

			// refresh statistics every #STATS_REFRESHING_FREQUENCY
			Date currentTime = new Date();
			if (currentTime.getTime() - lastStatsRefreshingTime.getTime() >= STATS_REFRESHING_FREQUENCY) {
				lastStatsRefreshingTime = currentTime;
				// refresh console statistics console log
				refreshStatsLog();
			}
		}
	}

	/**
	 * refresh console statistics console log
	 */
	private static void refreshStatsLog() {
		clearConsole();

		logStats();

		// clear short statistics
		monitorLogs.getLogStats().clearSections();
		monitorLogs.getLogStats().clearUsers();
		monitorLogs.getLogStats().clearRemoteHosts();

		// log all raised alerts
		logAlerts();
	}

	/**
	 * log all raised alerts
	 */
	private static void logAlerts() {
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

	/**
	 * log statistics
	 */
	private static void logStats() {
		// log title
		appendLogFilled();
		appendLogTitle(" HTTP TRAFFIC MONITORING LOG ");
		appendLogFilled();

		appendLogTitle(" OVERALL REQUESTS ");
		appendLog(" ");
		// log stats value
		int count = 0;
		StringBuilder statLogRow = new StringBuilder();
		HashMap<HTTPStatsType, Long> statsValues = monitorLogs.getLogStats().getStatsValues();
		for (Entry<HTTPStatsType, Long> entryValue : statsValues.entrySet()) {
			count++;
			statLogRow.append(entryValue.getKey());
			statLogRow.append(": ");
			statLogRow.append(entryValue.getValue());
			if (HTTPStatsType.TOTAL_CONTENT == entryValue.getKey()) {
				statLogRow.append(entryValue.getValue() > 0 ? " bytes" : " byte");
			}
			if (count % 2 == 0 || count == statsValues.size()) {
				appendLog(statLogRow.toString());
				statLogRow.setLength(0);
			} else {
				// fill last part
				for (int i = CONSOLE_WIDTH / 2 - statLogRow.length(); i >= 0; i--) {
					statLogRow.append(" ");
				}
			}
		}
		appendLog(" ");

		// log stats status
		count = 0;
		statLogRow.setLength(0);
		HashMap<HTTPStatsStatus, Long> statsStatus = monitorLogs.getLogStats().getStatsStatus();
		for (Entry<HTTPStatsStatus, Long> entryStatus : statsStatus.entrySet()) {
			count++;
			statLogRow.append(entryStatus.getKey());
			statLogRow.append(": ");
			statLogRow.append(entryStatus.getValue());
			if (count % 2 == 0 || count == statsStatus.size()) {
				appendLog(statLogRow.toString());
				statLogRow.setLength(0);
			} else {
				// fill last part
				for (int i = CONSOLE_WIDTH / 2 - statLogRow.length(); i >= 0; i--) {
					statLogRow.append(" ");
				}
			}
		}

		// log alert windows average
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
		
		//log las #STATS_REFRESHING_FREQUENCY ms statistics
		appendLogTitle(" LAST " + STATS_REFRESHING_FREQUENCY / 1000 + " SECONDS ");
		appendLog(" ");
		//log most present user
		appendLog("Most present user: ", monitorLogs.getLogStats().getTopUser());
		//log most present remote host
		appendLog("Most present remote host: ", monitorLogs.getLogStats().getTopRemoteHost());

		// log most hit sections
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
			}
		}
		// add empty log row when there is no value present
		for (int i = 0; i < (HTTPStats.MOST_HIT_SECTION_DISPLAYED - hitSections.size()) / 3; i++) {
			appendLog(" ");
		}

		appendLog(" ");
		appendLogFilled();

	}

	/**
	 * fill log row with '-' on #CONSOLE_WIDTH
	 */
	private static void appendLogFilled() {
		StringBuilder logSB = new StringBuilder();
		for (int i = 0; i < CONSOLE_WIDTH; i++) {
			logSB.append("-");
		}
		System.out.println(logSB.toString());
	}

	/**
	 * log centered title surrounded by '-'
	 * 
	 * @param title
	 */
	private static void appendLogTitle(String title) {
		StringBuilder statLogRow = new StringBuilder();
		// fill last part
		for (int i = CONSOLE_WIDTH / 2 - title.length() / 2 - 6; i >= 0; i--) {
			statLogRow.append("-");
		}
		statLogRow.append(title);
		appendLog('-', statLogRow.toString());
	}

	/**
	 * log messages bordered by '-'
	 * 
	 * @param messages
	 */
	private static void appendLog(String... messages) {
		appendLog(' ', messages);
	}

	/**
	 * log messages surrounded with #fillChar and bordered by '-'
	 * 
	 * @param fillChar
	 * @param messages
	 */
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
		System.out.println(logSB.toString());
	}

	/**
	 * clear console window TODO use windows and Linux bash command
	 */
	private static void clearConsole() {
		for (int i = 0; i < 40; ++i) {
			System.out.println();
		}
	}
}
