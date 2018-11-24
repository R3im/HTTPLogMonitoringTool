package com.httplogmonitoringtool.tests;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.httplogmonitoringtool.MonitorLog;
import com.httplogmonitoringtool.model.HTTPStatsAlertType;
import com.httplogmonitoringtool.model.HTTPStatsType;

public class MonitorLogTest {
	@Before
	public void setUpStreams() throws IOException, InterruptedException {
		File file = new File(TrafficFakeLogThread.LOG_FILE_PATH);
		if (file.exists()) {
			file.delete();
		}
		file.createNewFile();
	}

	@After
	public void restoreStreams() throws IOException {
		Files.delete(Paths.get(TrafficFakeLogThread.LOG_FILE_PATH));
	}

	@Test
	public void testFalseLog() {

		TrafficFakeLogThread fakeLogThread = new TrafficFakeLogThread();
		// set log set with various sections
		fakeLogThread.setLogArray(TrafficFakeLogThread.FALS_LOGS);
		// set high speed log
		fakeLogThread.setSpeed(10);
		try {
			// start fake log generator thread
			fakeLogThread.start();

			// init log monitor
			MonitorLog monitorLogs = new MonitorLog();
			monitorLogs.setLogFilePath(TrafficFakeLogThread.LOG_FILE_PATH);

			// wait for log to be initialized
			TimeUnit.MILLISECONDS.sleep(fakeLogThread.getSpeed() * 10);

			// update stats to consume log rows
			monitorLogs.updateStats();

			// monitor has raised a low traffic alert ?
			Long reqCount = monitorLogs.getLogStats().getStatsValues().get(HTTPStatsType.TOTAL_REQUESTS);
			Assert.assertTrue("Should not have any request (" + reqCount + ")", reqCount == 0);
			Long totalBadFormat = monitorLogs.getLogStats().getStatsValues().get(HTTPStatsType.TOTAL_BAD_FORMAT_LOG);
			Assert.assertTrue("Should have bad format (" + totalBadFormat + ")", totalBadFormat > 0);
		} catch (InterruptedException e) {
			Assert.fail(e.getMessage());
			return;
		} finally {
			// stop thread and delete tmp test logs
			fakeLogThread.interrupt();
			try {
				fakeLogThread.join();
			} catch (InterruptedException e) {
				Assert.fail(e.getMessage());
				return;
			}
		}
	}

	@Test
	public void testMostSections() {

		TrafficFakeLogThread fakeLogThread = new TrafficFakeLogThread();
		// set log set with various sections
		fakeLogThread.setLogArray(TrafficFakeLogThread.DEFAULT_LOGS);
		// set high speed log
		fakeLogThread.setSpeed(50);
		try {
			// start fake log generator thread
			fakeLogThread.start();

			// init log monitor
			MonitorLog monitorLogs = new MonitorLog();
			monitorLogs.setLogFilePath(TrafficFakeLogThread.LOG_FILE_PATH);

			// wait for log to be initialized
			TimeUnit.MILLISECONDS.sleep(fakeLogThread.getSpeed() * 10);

			// update stats to consume log rows
			monitorLogs.updateStats();

			// monitor has raised a low traffic alert ?
			Assert.assertTrue("Most hit section not correct",
					"/sport".equals(monitorLogs.getLogStats().getMostHitSection().keySet().iterator().next()));
		} catch (InterruptedException e) {
			Assert.fail(e.getMessage());
			return;
		} finally {
			// stop thread and delete tmp test logs
			fakeLogThread.interrupt();
			try {
				fakeLogThread.join();
			} catch (InterruptedException e) {
				Assert.fail(e.getMessage());
				return;
			}
		}
	}

	@Test
	public void testAlert() {
		int alertMonitoringTime = 1000 * 3;

		TrafficFakeLogThread fakeLogThread = new TrafficFakeLogThread();
		// set log set with one log at a time
		fakeLogThread.setLogArray(TrafficFakeLogThread.UNIQUE_LOGS);
		// set log generator to high speed
		fakeLogThread.setSpeed(50);
		try {
			// start fake log generator thread
			fakeLogThread.start();

			// wait for first logs
			TimeUnit.MILLISECONDS.sleep(100);

			// init log monitor
			MonitorLog monitorLogs = new MonitorLog();
			monitorLogs.setAlertAverageTreshold(10);
			monitorLogs.setAlertTimeWindow(alertMonitoringTime);
			monitorLogs.setLogFilePath(TrafficFakeLogThread.LOG_FILE_PATH);

			Date lastTime = new Date();
			boolean highTrafficAsserted = false;
			while (true) {
				monitorLogs.updateStats();

				// refresh stats
				Date currentTime = new Date();
				if (currentTime.getTime() - lastTime.getTime() >= alertMonitoringTime) {
					if (monitorLogs.getRaisedAlerts().size() == 1 && !highTrafficAsserted) {
						// monitor has raised a hight traffic alert ?
						Assert.assertTrue("High traffic alert not raised", !monitorLogs.getRaisedAlerts().isEmpty());
						Assert.assertTrue("High traffic alert not raised",
								HTTPStatsAlertType.HIGH_TRAFFIC.equals(monitorLogs.getRaisedAlerts().get(0).getType()));
						highTrafficAsserted = true;
						// set log generator to low speed
						fakeLogThread.setSpeed(1000);
						// wait for new speed logs
						TimeUnit.MILLISECONDS.sleep(1000);
						monitorLogs.updateStats();
					} else if (monitorLogs.getRaisedAlerts().size() == 2) {
						// monitor has raised a hight traffic alert ?
						Assert.assertTrue("High traffic alert not raised", !monitorLogs.getRaisedAlerts().isEmpty());
						Assert.assertTrue("High traffic alert not raised",
								HTTPStatsAlertType.HIGH_TRAFFIC.equals(monitorLogs.getRaisedAlerts().get(0).getType()));
						break;
					} else {
						Assert.fail("No Alert raised");
						break;
					}
					lastTime = currentTime;
				}
				TimeUnit.MILLISECONDS.sleep(alertMonitoringTime / 2);
			}
		} catch (InterruptedException e) {
			Assert.fail(e.getMessage());
			return;
		} finally {
			// stop thread and delete tmp test logs
			fakeLogThread.interrupt();
			try {
				fakeLogThread.join();
			} catch (InterruptedException e) {
				Assert.fail(e.getMessage());
				return;
			}
		}
	}

}
