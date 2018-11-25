package com.httplogmonitoringtool;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;

import org.apache.logging.log4j.core.util.Integers;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.Ansi.Erase;
import org.fusesource.jansi.AnsiConsole;

import com.httplogmonitoringtool.models.HTTPStats;
import com.httplogmonitoringtool.models.HTTPStatsAlert;
import com.httplogmonitoringtool.models.HTTPStatsStatus;
import com.httplogmonitoringtool.models.HTTPStatsType;

/**
 * 
 * log MonitorLog results into console
 * 
 * @author Remi c
 *
 */
public class Main {

	/**
	 * refreshing console log statistics default: 10 seconds (milliseconds)
	 */
	private final static int STATS_REFRESHING_FREQUENCY = 1000 * 10;

	/**
	 * console width for console log design default: 100char (char count)
	 */
	private final static int CONSOLE_WIDTH = 100;

	/**
	 * MonitorLog instance: Managing the HTTP traffic log parsing and statistics
	 * processing
	 */
	private static MonitorLog monitorLogs = new MonitorLog();

	/**
	 * last statistic refreshing time since the last
	 * {@link #STATS_REFRESHING_FREQUENCY}
	 */
	private static Date lastStatsRefreshingTime = new Date();

	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		AnsiConsole.systemInstall();
		try {
			// argument processing
			for (int i = 0; i < args.length; i++) {
				String arg = args[i];

				if (arg.equals("--help") || arg.equals("-h") || arg.equals("-?")) {// help option
					showHelpLog();
				} else if (arg.equals("-log") || arg.equals("-l")) {// HTTP log path option
					if (args.length <= i + 1) {// bad parameter
						showBadParameterLog();
						return;
					} else {
						monitorLogs.setLogFilePath(args[i + 1]);
					}
				} else if (arg.equals("-alert_threshold") || arg.equals("-at")) {// Alert threshold option
					if (args.length <= i + 1) {// bad parameter
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
			try {
				startMonitoring();
			} catch (FileNotFoundException e) {
				appendLog("File ", monitorLogs.getLogFilePath(), " seems to have been deleted: ", e.getMessage(), ".");
			} catch (IOException e) {
				appendLog("An error was raised when reading log file ", e.getMessage(), ".");
			}
		} finally {
			AnsiConsole.out.println(Ansi.ansi().reset());
			AnsiConsole.systemUninstall();
		}
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
		AnsiConsole.out.println("  USAGE: HTTPLogMonitoringTool [option...] [--help] ");
		AnsiConsole.out.println("   -?, -h, --help \t\tShows this help message.");
		AnsiConsole.out.println("   -log, -l \t\t\tSet HTTP log file fullpath (default: \"/var/log/access.log\").");
		AnsiConsole.out.println("   -alert_threshold, -at \tSet alert threshold (>0) (default: 2 minutes).");
		appendLogFilled();
		appendLogFilled();
		System.exit(0);
	}

	/**
	 * show bad parameter console log also showing help
	 */
	private static void showBadParameterLog() {
		AnsiConsole.out.println(Ansi.ansi().fgRed().a(" !!! BAD PARAMETER !!!").reset());
		showHelpLog();
	}

	/**
	 * starts monitoring HTTP logs
	 * 
	 * @throws IOException:           while reading log file
	 * @throws FileNotFoundException: while reading log file
	 */
	private static void startMonitoring() throws FileNotFoundException, IOException {

		// initialize console statistics console log
		refreshStatsLog();

		// infinite loop continually updating statistics and refreshing console log
		// every #STATS_REFRESHING_FREQUENCY
		while (true) {

			// read logs and update statistics
			boolean fileLogHasChanged = monitorLogs.updateStats();

			// continue while log file has not changed
			if (!fileLogHasChanged) {
				continue;
			}

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

		// log title
		appendLogFilled();
		appendLogTitle(" HTTP TRAFFIC MONITORING LOG ");
		appendLogFilled();

		// log all raised alerts
		logAlerts();

		// log stats
		logStats();

		// end border
		appendLogFilled();

		// clear short statistics
		monitorLogs.getLogStats().clearSections();
		monitorLogs.getLogStats().clearUsers();
		monitorLogs.getLogStats().clearRemoteHosts();
		monitorLogs.getLogStats().clearTotalContent();
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
		}
	}

	/**
	 * log statistics
	 */
	private static void logStats() {

		// log overall requests
		appendLogTitle(" OVERALL REQUESTS ");
		appendLog(" ");

		// log stats value
		int count = 0;
		StringBuilder statLogRow = new StringBuilder();
		HashMap<HTTPStatsType, Long> statsValues = monitorLogs.getLogStats().getStatsValues();

		// append TOTAL_CONTENT part
		statLogRow.append(getValueStatsPart(HTTPStatsType.TOTAL_REQUESTS.toString(),
				statsValues.get(HTTPStatsType.TOTAL_REQUESTS).toString(), 2));

		// append TOTAL_CONTENT part
		statLogRow.append(getValueStatsPart(HTTPStatsType.TOTAL_BAD_FORMAT_LOG.toString(),
				statsValues.get(HTTPStatsType.TOTAL_BAD_FORMAT_LOG).toString(), 2));

		// append row values
		appendLog(statLogRow.toString());
		statLogRow.setLength(0);// clear string builder

		appendLog(" ");

		// log stats status
		count = 0;
		HashMap<HTTPStatsStatus, Long> statsStatus = monitorLogs.getLogStats().getStatsStatus();
		for (Entry<HTTPStatsStatus, Long> entryStatus : statsStatus.entrySet()) {
			count++;
			// append value part
			statLogRow.append(getValueStatsPart(entryStatus.getKey().toString(), entryStatus.getValue().toString(), 2));
			if (count % 2 == 0 || count == statsStatus.size()) {
				appendLog(statLogRow.toString());
				statLogRow.setLength(0);// clear string builder
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
		String averageValue = monitorLogs.getLogStats().getAlertAverage() == 0 ? "-"
				: monitorLogs.getLogStats().getAlertAverage() + "";
		appendLog("Last ", reducedAlertTimeWindow + "", alertTimeWindowUnit + "", " traffic average: ", averageValue,
				" requests/s");
		appendLog(" ");

		// log last #STATS_REFRESHING_FREQUENCY statistics
		appendLogTitle(" LAST " + STATS_REFRESHING_FREQUENCY / 1000 + " SECONDS ");
		appendLog(" ");

		// log TOTAL_CONTENT part
		Long totalContent = statsValues.get(HTTPStatsType.TOTAL_CONTENT);
		String unit = totalContent > 0 ? " bytes" : " byte";
		statLogRow.append(getValueStatsPart(HTTPStatsType.TOTAL_CONTENT.toString(), totalContent.toString() + unit, 2));

		// append row values
		appendLog(statLogRow.toString());
		statLogRow.setLength(0);// clear string builder

		// log most present user
		appendLog("Most present user: ", monitorLogs.getLogStats().getTopUser());

		// log most present remote host
		appendLog("Most present remote host: ", monitorLogs.getLogStats().getTopRemoteHost());

		appendLog(" ");
		// log most hit sections
		HashMap<String, Integer> hitSections = monitorLogs.getLogStats().getMostHitSection();
		appendLog("Most hit section (", "" + hitSections.size(), "/", "" + hitSections.size(), "):");

		count = 0;
		for (Entry<String, Integer> entry : hitSections.entrySet()) {
			count++;
			// append value part
			statLogRow
					.append(getValueStatsPart("\"" + entry.getKey().toString() + "\"", entry.getValue().toString(), 3));
			if (count % 3 == 0 || count == hitSections.size()) {
				appendLog(statLogRow.toString());
				statLogRow.setLength(0);
			}
		}
		// add empty log row when there is no value present
		for (int i = 0; i < (HTTPStats.MOST_HIT_SECTION_DISPLAYED - hitSections.size()) / 3; i++) {
			appendLog(" ");
		}

		appendLog(" ");
	}

	/**
	 * get string builder of key: value row log part
	 * 
	 * @param key
	 * @param value
	 * @param       consoleColumnCount: console column divider
	 * @return "key: value" StringBuilder
	 */
	private static StringBuilder getValueStatsPart(String key, String value, int consoleColumnCount) {
		StringBuilder valueStatsPart = new StringBuilder();
		valueStatsPart.append(key);
		valueStatsPart.append(": ");
		valueStatsPart.append(value);
		// fill last part
		for (int i = CONSOLE_WIDTH / consoleColumnCount - valueStatsPart.length() - 4; i >= 0; i--) {
			valueStatsPart.append(" ");
		}
		return valueStatsPart;
	}

	/**
	 * fill log row with '-' on {@link #CONSOLE_WIDTH}
	 */
	private static void appendLogFilled() {
		StringBuilder logSB = new StringBuilder();
		logSB.append(' ');
		for (int i = 0; i < CONSOLE_WIDTH - 1; i++) {
			logSB.append('-');
		}
		AnsiConsole.out.println(logSB.toString());
	}

	/**
	 * log centered title surrounded by '-'
	 * 
	 * @param title
	 */
	private static void appendLogTitle(String title) {
		StringBuilder logSB = new StringBuilder();
		// fill last part
		for (int i = CONSOLE_WIDTH / 2 - title.length() / 2 - 6; i >= 0; i--) {
			logSB.append('-');
		}
		logSB.append(title);
		appendLog('-', logSB.toString());
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
		logSB.append(' ');
		logSB.append('-');
		logSB.append(fillChar);
		logSB.append(fillChar);
		for (String message : messages) {
			logSB.append(message);
		}
		// complete row with -
		int sbLength = logSB.length();
		for (int i = 0; i < CONSOLE_WIDTH - sbLength - 1; i++) {
			logSB.append(fillChar);
		}
		logSB.append('-');
		AnsiConsole.out.println(logSB.toString());
	}

	/**
	 * clear console window
	 */
	private static void clearConsole() {
		AnsiConsole.out.println(Ansi.ansi().eraseScreen(Erase.ALL).fgBrightGreen());
		for (int i = 0; i < 20; ++i) {
			AnsiConsole.out.println();
		}
	}
}
