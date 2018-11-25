package com.httplogmonitoringtool;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.httplogmonitoringtool.exceptions.HTTPLogRowFormatException;
import com.httplogmonitoringtool.models.HTTPLogRow;
import com.httplogmonitoringtool.models.HTTPStats;
import com.httplogmonitoringtool.models.HTTPStatsAlert;
import com.httplogmonitoringtool.models.HTTPStatsAlertType;
import com.httplogmonitoringtool.models.HTTPStatsStatus;
import com.httplogmonitoringtool.models.HTTPStatsType;
import com.httplogmonitoringtool.utils.HTTPLogParser;

/***
 * HTTP log monitor: Managing the HTTP traffic log parsing and statistics
 * processing
 * 
 * @author Remi c
 *
 */
public class MonitorLog {

	/**
	 * Time windows in which the average is monitored traffic to issue alerts.
	 * default: 2 minutes (milliseconds)
	 */
	private int alertTimeWindow = 1000 * 60 * 2;
	/**
	 * Log file path to monitor
	 */
	private String logFilePath = "/var/log/access.log";
	/**
	 * Alert average threshold: The monitor raise an alert when the average during
	 * the {@link #alertTimeWindow} exceed this value. default: 10 requests/seconds
	 * (request per seconds)
	 */
	private int alertAverageThreshold = 10;

	/**
	 * Alert monitoring time: Stores all the times during {@link #alertTimeWindow}
	 * It allows to calculate the average
	 */
	private List<Long> alertMonitoringTimes = new ArrayList<Long>();

	/**
	 * All raised alerts. It will always starts with a HIGH_TRAFFIC alert, followed
	 * by a LOW_TRAFFIC if traffic goes back under {@link #alertAverageThreshold}
	 */
	private final ArrayList<HTTPStatsAlert> raisedAlerts = new ArrayList<HTTPStatsAlert>();

	/***
	 * Last readed line count. It allows to go back to new log line when the log
	 * file is updated
	 */
	private long lastReadedLine = 0;

	/**
	 * HTTP statistics
	 */
	private final HTTPStats logStats = new HTTPStats();

	/**
	 * Update statistics. Parse log file and extract interesting data
	 * 
	 * @return file log has changed
	 * @throws IOException:           while reading log file
	 * @throws FileNotFoundException: while reading log file
	 */
	public boolean updateStats() throws FileNotFoundException, IOException {

		// read log file from the last row parsed
		boolean fileLogsHasChanged = false;

		try (FileInputStream fstream = new FileInputStream(logFilePath)) {// auto closable
			BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
			int lineCount = 0;
			String strLine;
			while ((strLine = br.readLine()) != null) {
				if (lastReadedLine < lineCount) {// read to last readed line
					// parses line and enriches statistics
					Date logDate = consumeLogLine(strLine);
					// ignore line if no date found
					if (logDate != null) {
						// collect date for alerting
						alertMonitoringTimes.add(logDate.getTime());
						// the stats have changed
						fileLogsHasChanged = true;
					}
				}
				lineCount++;
			}
			// store last readed line
			lastReadedLine = lineCount - 1;
		}

		// returns if no change appended
		if (!fileLogsHasChanged) {
			return false;
		}

		// managing the alerts
		if (alertMonitoringTimes.isEmpty()) {
			return true;
		}

		Date currentTime = new Date();
		long checkedTimePeriod = currentTime.getTime() - alertMonitoringTimes.get(0);
		if (checkedTimePeriod >= alertTimeWindow) {// alert time windows aimed
			int trafficAverage = (int) Math
					.ceil(((float) alertMonitoringTimes.size() / (float) (checkedTimePeriod / 1000)));// round up
																										// traffic
																										// average
			// store traffic average in stats data
			logStats.setAlertAverage(trafficAverage);
			boolean lastAlertIsLow = raisedAlerts.isEmpty()
					|| !HTTPStatsAlertType.HIGH_TRAFFIC.equals(raisedAlerts.get(raisedAlerts.size() - 1).getType());
			if (lastAlertIsLow) {
				if (trafficAverage > alertAverageThreshold) {
					raisedAlerts.add(new HTTPStatsAlert(HTTPStatsAlertType.HIGH_TRAFFIC, trafficAverage, currentTime));
				}
			} else {
				if (trafficAverage < alertAverageThreshold) {
					raisedAlerts.add(new HTTPStatsAlert(HTTPStatsAlertType.LOW_TRAFFIC, trafficAverage, currentTime));
				}
			}

			// remove all outdated alert monitoring times
			int lastCheckedTimeIndex = 0;
			for (long time : alertMonitoringTimes) {
				if (checkedTimePeriod < currentTime.getTime() - time) {
					break;
				}
				lastCheckedTimeIndex++;
			}
			alertMonitoringTimes = alertMonitoringTimes.subList(lastCheckedTimeIndex, alertMonitoringTimes.size());
		}

		return true;
	}

	/**
	 * Parses log line and enriches statistics
	 * 
	 * @param log line
	 * @return log date: null when bad line format
	 */
	private Date consumeLogLine(String line) {

		try {

			// parse line
			HTTPLogRow logRow = HTTPLogParser.parse(line);

			Date currentTime = new Date();
			// check time logic
			if (currentTime.getTime() < logRow.getReqDate().getTime() || (!alertMonitoringTimes.isEmpty()
					&& logRow.getReqDate().getTime() < alertMonitoringTimes.get(0))) {
				throw new HTTPLogRowFormatException("Incoherente date");
			}

			// increase stats values
			logStats.increase(HTTPStatsType.TOTAL_CONTENT, logRow.getContentLength());
			logStats.increase(HTTPStatsType.TOTAL_REQUESTS);
			for (HTTPStatsStatus status : HTTPStatsStatus.values()) {
				if (status.getCode() == logRow.getReqSatus()) {
					logStats.increase(status);
				}
			}
			// section add
			logStats.addSection(logRow.getReqSection());
			// user add
			logStats.addUser(logRow.getAuthUser());
			// remoteHost add
			logStats.addRemoteHost(logRow.getRemoteHost());

			return logRow.getReqDate();
		} catch (HTTPLogRowFormatException e) {
			//increase bad format counter
			logStats.increase(HTTPStatsType.TOTAL_BAD_FORMAT_LOG);
		}
		return null;
	}

	/**
	 * {@link #logFilePath}
	 * 
	 * @return logFilePath
	 */
	public String getLogFilePath() {
		return logFilePath;
	}

	/**
	 * {@link #logFilePath}
	 * 
	 * @param logFilePath
	 */
	public void setLogFilePath(String logFilePath) {
		this.logFilePath = logFilePath;
	}

	/**
	 * {@link #alertAverageThreshold}
	 * 
	 * @return alertAverageThreshold
	 */
	public int getAlertAverageThreshold() {
		return alertAverageThreshold;
	}

	/**
	 * {@link #alertAverageThreshold}
	 * 
	 * @param alertAverageThreshold
	 */
	public void setAlertAverageThreshold(int alertAverageThreshold) {
		this.alertAverageThreshold = alertAverageThreshold;
	}

	/**
	 * {@link #alertTimeWindow}
	 * 
	 * @return alertTimeWindow
	 */
	public int getAlertTimeWindow() {
		return alertTimeWindow;
	}

	/**
	 * {@link #alertTimeWindow}
	 * 
	 * @param alertTimeWindow
	 */
	public void setAlertTimeWindow(int alertTimeWindow) {
		this.alertTimeWindow = alertTimeWindow;
	}

	/**
	 * {@link #logStats}
	 * 
	 * @returnlogStats
	 */
	public HTTPStats getLogStats() {
		return logStats;
	}

	/**
	 * {@link #raisedAlerts}
	 * 
	 * @return raisedAlerts
	 */
	public ArrayList<HTTPStatsAlert> getRaisedAlerts() {
		return raisedAlerts;
	}

}
