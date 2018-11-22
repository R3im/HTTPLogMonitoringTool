package main.java.com.httplogmonitoringtool;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;
import java.util.stream.Stream;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter.DEFAULT;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;

import main.java.com.httplogmonitoringtool.model.HTTPLogRow;
import main.java.com.httplogmonitoringtool.model.HTTPStats;
import main.java.com.httplogmonitoringtool.model.HTTPStatsType;

public class MonitorLog {

	private final static String LOG_FILE_PATH = "/tmp/access.log";
	private final static Logger logger = LogManager.getLogger(MonitorLog.class.getName());

	private final static int STATS_REFRESHING_FREQUENCY = 1000 * 10;
	private final static int ALERT_MONITORING_TIME = 1000 * 60 * 2;
	/**
	 * request per seconds
	 */
	private final static int ALERT_AVERAGE_TRESHOLD = 10;

	private final HTTPStats totalLogStats = new HTTPStats();
	private final HTTPStats shortLogStats = new HTTPStats();

	public static void main(String[] args) throws IOException {
		new MonitorLog().startMonitoring();
	}

	public void startMonitoring() {
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
			case 300:
				break;
			case 301:
				break;
			case 302:
				break;
			case 307:
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
			//section hitted
			totalLogStats.sectionHitted(logRow.getReqSection());
			shortLogStats.sectionHitted(logRow.getReqSection());
		}
	}

	private void refreshStats() {
		logger.debug(shortLogStats.toString());
		shortLogStats.clear();
	}
}
