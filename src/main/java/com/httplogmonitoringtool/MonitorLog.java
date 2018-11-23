package com.httplogmonitoringtool;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;

import com.httplogmonitoringtool.model.HTTPLogRow;
import com.httplogmonitoringtool.model.HTTPStats;
import com.httplogmonitoringtool.model.HTTPStatsAlert;
import com.httplogmonitoringtool.model.HTTPStatsAlertType;
import com.httplogmonitoringtool.model.HTTPStatsType;

public class MonitorLog {

	private String logFilePath = "/tmp/access.log";
	private final static Logger logger = LogManager.getLogger(MonitorLogTest.class.getName());

	private final static int ALERT_MONITORING_TIME = 1000 * 60 * 2;
	/**
	 * request per seconds
	 */
	private int alertAverageTreshold = 10;

	private final HTTPStats logStats = new HTTPStats();
//	private final HTTPStats shortLogStats = new HTTPStats();

	private List<Long> alertMonitoringTimes = new ArrayList<Long>();

	private final ArrayList<HTTPStatsAlert> risedAlerts = new ArrayList<HTTPStatsAlert>();

	private long lastReadedLine = 0;

	public boolean updateStats() {

		// consume log file
		Date currentTime = new Date();
		boolean fileLogsHasChanged = false;
		try (FileInputStream fstream = new FileInputStream(logFilePath)) {
			BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
			int lineCount = 0;
			String strLine;
			while ((strLine = br.readLine()) != null) {
				if (lastReadedLine < lineCount) {
					consumeLine(strLine);
					fileLogsHasChanged = true;
					alertMonitoringTimes.add(new Date().getTime());
				}
				lineCount++;
			}
			lastReadedLine = lineCount - 1;
		} catch (FileNotFoundException e) {
			logger.error(e.getMessage(), e);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}

		if (fileLogsHasChanged) {
			// alert
			if (!alertMonitoringTimes.isEmpty()
					&& currentTime.getTime() - alertMonitoringTimes.get(0) >= ALERT_MONITORING_TIME) {
				long checkedTimePeriod = currentTime.getTime() - alertMonitoringTimes.get(0);
				// TODO round up
				int trafficAverage = (int) (alertMonitoringTimes.size() / (checkedTimePeriod / 1000));
//				appendLog("trafficAverage:", trafficAverage + "", "\t", "logs:", alertMonitoringTimes.size() + "",
//						"\t", "time:", ((currentTime.getTime() - alertMonitoringTimes.get(0)) / 1000) + "", "\t");
				if (risedAlerts.isEmpty() || !HTTPStatsAlertType.HIGH_TRAFFIC
						.equals(risedAlerts.get(risedAlerts.size() - 1).getType())) {
					if (trafficAverage > alertAverageTreshold) {
						risedAlerts.add(
								new HTTPStatsAlert(HTTPStatsAlertType.HIGH_TRAFFIC, trafficAverage, currentTime));
					}
				} else if (!risedAlerts.isEmpty() && HTTPStatsAlertType.HIGH_TRAFFIC
						.equals(risedAlerts.get(risedAlerts.size() - 1).getType())) {
					if (trafficAverage < alertAverageTreshold) {
						risedAlerts.add(
								new HTTPStatsAlert(HTTPStatsAlertType.LOW_TRAFFIC, trafficAverage, currentTime));
					}
				}
				int lastCheckedTimeIndex = 0;
				for (long time : alertMonitoringTimes) {
					if (checkedTimePeriod < currentTime.getTime() - time) {
						break;
					}
					lastCheckedTimeIndex++;
				}
				alertMonitoringTimes = alertMonitoringTimes.subList(lastCheckedTimeIndex,
						alertMonitoringTimes.size());
			}
		}
		return fileLogsHasChanged;
	}
	
	private void consumeLine(String line) {

		HTTPLogRow logRow = new HTTPLogRow(line);

		if (Strings.isNotBlank(logRow.getAuthUser())) {
			logStats.increase(HTTPStatsType.TOTAL_REQUESTS);
			logStats.increase(HTTPStatsType.TOTAL_REQUESTS);
			switch (logRow.getReqSatus()) {
			case 200:
				logStats.increase(HTTPStatsType.VALID_REQUESTS);
				break;
			case 400:
				logStats.increase(HTTPStatsType.FAILED_REQUESTS);
			case 404:
				logStats.increase(HTTPStatsType.NOTFOUND_REQUESTS);
				break;
			default:
				break;
			}
			// section hitted
			logStats.sectionHitted(logRow.getReqSection());
		}
	}


//	private void logStats(HTTPStats stats) {
//
//		HashMap<String, Integer> hitSections = stats.getMostHitSection();
//
//		appendLog("Most hit section (", "" + hitSections.size(), "/", "" + hitSections.size(), "):");
//
//		StringBuilder statLogRow = new StringBuilder();
//		int count = 0;
//		for (Entry<String, Integer> entry : hitSections.entrySet()) {
//			count++;
//			statLogRow.append("\"");
//			statLogRow.append(entry.getKey());
//			statLogRow.append("\":");
//			statLogRow.append(entry.getValue().toString());
//
//			if (count % 3 == 0 || count == hitSections.size()) {
//				appendLog(statLogRow.toString());
//				statLogRow.setLength(0);
//			} else {
//				statLogRow.append("   ");
//			}
//		}
//
//		for (int i = 0; i < (HTTPStats.MOST_HIT_SECTION_DISPLAYED - hitSections.size()) / 3; i++) {
//			appendLog(".");
//		}
//
//		appendLog("Request summary:");
//
//		HashMap<HTTPStatsType, Integer> statsValues = stats.getStatsValues();
//
//		// stats value
//		count = 0;
//		for (Entry<HTTPStatsType, Integer> entry : statsValues.entrySet()) {
//			count++;
//			statLogRow.append(entry.getKey().toString());
//			statLogRow.append(": ");
//			statLogRow.append(entry.getValue().toString());
//			if (HTTPStatsType.TOTAL_CONTENT == entry.getKey()) {
//				statLogRow.append(entry.getValue() > 0 ? " bytes" : " byte");
//			}
//			if (count % 3 == 0 || count == statsValues.size()) {
//				appendLog(statLogRow.toString());
//				statLogRow.setLength(0);
//			} else {
//				statLogRow.append("......");
//			}
//		}
//	}


	public String getLogFilePath() {
		return logFilePath;
	}

	public void setLogFilePath(String logFilePath) {
		this.logFilePath = logFilePath;
	}

	public int getAlertAverageTreshold() {
		return alertAverageTreshold;
	}

	public void setAlertAverageTreshold(int alertAverageTreshold) {
		this.alertAverageTreshold = alertAverageTreshold;
	}

	public HTTPStats getLogStats() {
		return logStats;
	}

	public ArrayList<HTTPStatsAlert> getRisedAlerts() {
		return risedAlerts;
	}
 
	
}
