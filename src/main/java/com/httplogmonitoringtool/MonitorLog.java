package com.httplogmonitoringtool;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;

import com.httplogmonitoringtool.model.HTTPLogRow;
import com.httplogmonitoringtool.model.HTTPStats;
import com.httplogmonitoringtool.model.HTTPStatsAlert;
import com.httplogmonitoringtool.model.HTTPStatsAlertType;
import com.httplogmonitoringtool.model.HTTPStatsType;

public class MonitorLog {

	private final static String LOG_FILE_PATH = "/tmp/access.log";
	private final static Logger logger = LogManager.getLogger(MonitorLog.class.getName());

	private final static int STATS_REFRESHING_FREQUENCY = 1000 * 10;
	private final static int ALERT_MONITORING_TIME = 1000 * 10;// 60 * 2;
	/**
	 * request per seconds
	 */
	private final static int ALERT_AVERAGE_TRESHOLD = 10;

	private final HTTPStats totalLogStats = new HTTPStats();
	private final HTTPStats shortLogStats = new HTTPStats();

	private final ArrayList<Long> alertMonitoringTimes = new ArrayList<Long>();

	private final ArrayList<HTTPStatsAlert> risedAlerts = new ArrayList<HTTPStatsAlert>();

	public static void main(String[] args) throws IOException {
		new MonitorLog().startMonitoring();
	}

	public void startMonitoring() {
		refreshStats();
		long lastReadedLine = 0;
		Date lastStatsRefringTime = new Date();
		while (true) {
			// consume log file
			try (FileInputStream fstream = new FileInputStream(LOG_FILE_PATH)) {
				BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
				int lineCount = 0;
				String strLine;
				while ((strLine = br.readLine()) != null) {
					if (lastReadedLine < lineCount) {
						consumeLine(strLine);
					}
					lineCount++;
				}
				lastReadedLine = lineCount - 1;
			} catch (FileNotFoundException e) {
				logger.error(e.getMessage(), e);
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}

			// refresh stats
			Date currentTime = new Date();
			if (currentTime.getTime() - lastStatsRefringTime.getTime() >= STATS_REFRESHING_FREQUENCY) {
				lastStatsRefringTime = currentTime;
				refreshStats();
			}
			// alert
			if (!alertMonitoringTimes.isEmpty()
					&& currentTime.getTime() - alertMonitoringTimes.get(0) >= ALERT_MONITORING_TIME) {
				alertMonitoringTimes.remove(0);
				// TODO round up
				int trafficAverage = (int) (alertMonitoringTimes.size()
						/ ((currentTime.getTime() - alertMonitoringTimes.get(0)) / 1000));
				if (risedAlerts.isEmpty()
						|| !HTTPStatsAlertType.HIGH_TRAFFIC.equals(risedAlerts.get(risedAlerts.size() - 1).getType())) {
					if (trafficAverage > ALERT_AVERAGE_TRESHOLD) {
						risedAlerts
								.add(new HTTPStatsAlert(HTTPStatsAlertType.HIGH_TRAFFIC, trafficAverage, currentTime));
					}
				} else if (!risedAlerts.isEmpty()
						&& HTTPStatsAlertType.HIGH_TRAFFIC.equals(risedAlerts.get(risedAlerts.size() - 1).getType())) {
					if (trafficAverage < ALERT_AVERAGE_TRESHOLD) {
						risedAlerts
								.add(new HTTPStatsAlert(HTTPStatsAlertType.LOW_TRAFFIC, trafficAverage, currentTime));
					}
				}
			}
			alertMonitoringTimes.add(currentTime.getTime());

		}
	}

	private void consumeLine(String line) {

		HTTPLogRow logRow = new HTTPLogRow(line);

		if (Strings.isNotBlank(logRow.getAuthUser())) {
			totalLogStats.increase(HTTPStatsType.TOTAL_REQUESTS);
			shortLogStats.increase(HTTPStatsType.TOTAL_REQUESTS);
			totalLogStats.increase(HTTPStatsType.TOTAL_CONTENT, logRow.getContentLength());
			shortLogStats.increase(HTTPStatsType.TOTAL_CONTENT, logRow.getContentLength());
			switch (logRow.getReqSatus()) {
			case 200:
				totalLogStats.increase(HTTPStatsType.VALID_REQUESTS);
				shortLogStats.increase(HTTPStatsType.VALID_REQUESTS);
				break;
			case 400:
				totalLogStats.increase(HTTPStatsType.FAILED_REQUESTS);
				shortLogStats.increase(HTTPStatsType.FAILED_REQUESTS);
			case 404:
				totalLogStats.increase(HTTPStatsType.NOTFOUND_REQUESTS);
				shortLogStats.increase(HTTPStatsType.NOTFOUND_REQUESTS);
				break;
			default:
				break;
			}
			// section hitted
			totalLogStats.sectionHitted(logRow.getReqSection());
			shortLogStats.sectionHitted(logRow.getReqSection());
		}
	}

	private final static String os = System.getProperty("os.name");

	private void refreshStats() {
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
		appendLog("-----------------------------------------");
		appendLog("-------HTTP traffic monitoring log-------");
		appendLog("-----------------------------------------");
		appendLog("--Total stats--");
		logStats(totalLogStats);
		appendLog("-----------------------------------------");
		appendLog("--Last ", "" + STATS_REFRESHING_FREQUENCY / 1000, " seconds stats--");
		logStats(shortLogStats);
		appendLog("-----------------------------------------");

		// clear short stast
		shortLogStats.clear();

		// display alerts
		if (!risedAlerts.isEmpty()) {
			appendLog("-------ALERTS-!!!-------");
			for (HTTPStatsAlert alert : risedAlerts) {
				appendLog(alert.toString());
			}
		}
		appendLog("-----------------------------------------");
	}

	private void logStats(HTTPStats stats) {

		HashMap<String, Integer> hitSections = stats.getMostHitSection();

		appendLog("Most hit section (", "" + hitSections.size(), "/", "" + hitSections.size(), "):");

		StringBuilder statLogRow = new StringBuilder();
		int count = 0;
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
				statLogRow.append("\t\t");
			}
		}

		for (int i = 0; i < (HTTPStats.MOST_HIT_SECTION_DISPLAYED - hitSections.size()) / 3; i++) {
			appendLog(" ");
		}

		appendLog("Request summary:");

		HashMap<HTTPStatsType, Integer> statsValues = stats.getStatsValues();

		// stats value
		count = 0;
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
				statLogRow.append("\t\t");
			}
		}
	}

	private void appendLog(String... messages) {
		StringBuilder logSB = new StringBuilder();
		logSB.append("--");
		for (String message : messages) {
			logSB.append(message);
		}
		logger.debug(logSB.toString());
	}
}
