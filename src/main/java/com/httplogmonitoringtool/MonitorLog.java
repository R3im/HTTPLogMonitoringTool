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
import com.httplogmonitoringtool.model.HTTPLogRowFormatException;
import com.httplogmonitoringtool.model.HTTPStats;
import com.httplogmonitoringtool.model.HTTPStatsAlert;
import com.httplogmonitoringtool.model.HTTPStatsAlertType;
import com.httplogmonitoringtool.model.HTTPStatsStatus;
import com.httplogmonitoringtool.model.HTTPStatsType;

public class MonitorLog {

	private String logFilePath = "/var/log/access.log";
	private final static Logger logger = LogManager.getLogger(MonitorLog.class.getName());

	private int alertTimeWindow = 1000 * 60 * 2;
	/**
	 * request per seconds
	 */
	private int alertAverageThreshold = 10;

	private final HTTPStats logStats = new HTTPStats();

	private List<Long> alertMonitoringTimes = new ArrayList<Long>();

	private final ArrayList<HTTPStatsAlert> raisedAlerts = new ArrayList<HTTPStatsAlert>();

	private long lastReadedLine = 0;

	public boolean updateStats() {

		// consume log file
		boolean fileLogsHasChanged = false;
		try (FileInputStream fstream = new FileInputStream(logFilePath)) {
			BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
			int lineCount = 0;
			String strLine;
			while ((strLine = br.readLine()) != null) {
				if (lastReadedLine < lineCount) {
					Date logDate = consumeLine(strLine);
					if (logDate != null) {
						alertMonitoringTimes.add(logDate.getTime());
						fileLogsHasChanged = true;
					}
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
			Date currentTime = new Date();
			// alert
			if (!alertMonitoringTimes.isEmpty()
					&& currentTime.getTime() - alertMonitoringTimes.get(0) >= alertTimeWindow) {
				long checkedTimePeriod = currentTime.getTime() - alertMonitoringTimes.get(0);
				// round up
				int trafficAverage = (int) Math
						.ceil(((float) alertMonitoringTimes.size() / (float) (checkedTimePeriod / 1000)));
				logStats.setAlertAverage(trafficAverage);
				if (raisedAlerts.isEmpty() || !HTTPStatsAlertType.HIGH_TRAFFIC
						.equals(raisedAlerts.get(raisedAlerts.size() - 1).getType())) {
					if (trafficAverage > alertAverageThreshold) {
						raisedAlerts
								.add(new HTTPStatsAlert(HTTPStatsAlertType.HIGH_TRAFFIC, trafficAverage, currentTime));
					}
				} else if (!raisedAlerts.isEmpty() && HTTPStatsAlertType.HIGH_TRAFFIC
						.equals(raisedAlerts.get(raisedAlerts.size() - 1).getType())) {
					if (trafficAverage < alertAverageThreshold) {
						raisedAlerts
								.add(new HTTPStatsAlert(HTTPStatsAlertType.LOW_TRAFFIC, trafficAverage, currentTime));
					}
				}
				int lastCheckedTimeIndex = 0;
				for (long time : alertMonitoringTimes) {
					if (checkedTimePeriod < currentTime.getTime() - time) {
						break;
					}
					lastCheckedTimeIndex++;
				}
				alertMonitoringTimes = alertMonitoringTimes.subList(lastCheckedTimeIndex, alertMonitoringTimes.size());
			}
		}
		return fileLogsHasChanged;
	}

	private Date consumeLine(String line) {

		try {

			HTTPLogRow logRow = new HTTPLogRow(line);

			Date currentTime = new Date();
			// check time logic
			if (currentTime.getTime() > logRow.getReqDate().getTime()
					&& (alertMonitoringTimes.isEmpty() || logRow.getReqDate().getTime() > alertMonitoringTimes.get(0))) {
				logStats.increase(HTTPStatsType.TOTAL_CONTENT, logRow.getContentLength());
				logStats.increase(HTTPStatsType.TOTAL_REQUESTS);
				for (HTTPStatsStatus status : HTTPStatsStatus.values()) {
					if (status.getCode() == logRow.getReqSatus()) {
						logStats.increase(status);
					}
				}
				// section add
				logStats.addSection(logRow.getReqSection());
				// user count
				logStats.addUser(logRow.getAuthUser());
				// remoteHost count
				logStats.addRemoteHost(logRow.getRemoteHost());
			}
			return logRow.getReqDate();
		} catch (HTTPLogRowFormatException e) {
			logStats.increase(HTTPStatsType.TOTAL_BAD_FORMAT_LOG);
		}
		return null;
	}

	public String getLogFilePath() {
		return logFilePath;
	}

	public void setLogFilePath(String logFilePath) {
		this.logFilePath = logFilePath;
	}

	public int getAlertAverageThreshold() {
		return alertAverageThreshold;
	}

	public void setAlertAverageThreshold(int alertAverageThreshold) {
		this.alertAverageThreshold = alertAverageThreshold;
	}

	public int getAlertTimeWindow() {
		return alertTimeWindow;
	}

	public void setAlertTimeWindow(int alertTimeWindow) {
		this.alertTimeWindow = alertTimeWindow;
	}

	public HTTPStats getLogStats() {
		return logStats;
	}

	public ArrayList<HTTPStatsAlert> getRaisedAlerts() {
		return raisedAlerts;
	}

}
