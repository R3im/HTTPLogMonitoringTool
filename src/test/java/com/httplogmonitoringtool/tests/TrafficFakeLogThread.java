package com.httplogmonitoringtool.tests;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Formatter;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TrafficFakeLogThread extends Thread {

	public final static String LOG_FILE_PATH = "/var/log/access_test.log";
//	private final static Logger logger = LogManager.getLogger(TrafficFakeLogThread.class.getName());
//	public final static int HIGH_SPEED = 50;
//	public final static int LOW_SPEED = 2000;
//	public int speedSwitchTime = 1000 * 60 * 2;
	private final static SimpleDateFormat dateFormat = new SimpleDateFormat("[dd/MM/yyyy:HH:mm:ss ZZZ]");

	public final static String[] DEFAULT_LOGS = new String[] {
			"127.0.0.1 - robert %s \"DELETE /api HTTP/1.0\" 503 189\n",
			"127.0.0.1 - sophie %s \"PUT /sport HTTP/1.0\" 400 10\n",
			"127.0.0.1 - sophie %s \"PUT /sport HTTP/1.0\" 200 10\n",
			"127.0.0.1 - sophie %s \"PUT /sport HTTP/1.0\" 404 10\n" };

	public final static String[] UNIQUE_LOGS = new String[] {
			"127.0.0.1 - robert %s \"DELETE /api HTTP/1.0\" 503 189\n" };

	public final static String[] FALS_LOGS = new String[] { "",
			"ffffffffffffffffffffffffffffffffff\n",
			"f zef ef fez \n", "                             \n",
			"127.0.0.1 sophie %s \"PUT /sport HTTP/1.0\" 404 10\n",
			"127.0.0.1 - sophie %s \"PUT /sport HTTP/1.0\" 404 aa\n",
			"127.0.0.1 - sophie %s \"PUT /sport HTTP/1.0\" bbb, aa\n",
			"127.0.0.1 - sophie [oooo] \"PUT /sport HTTP/1.0\" bbb, aa\n" ,
			"127.0.0.1 - lily [24/11/2222:16:05:35 +0100] \"DELETE /sport/volleyball HTTP/1.0\" 300 8\n",
			"127.0.0.1 - lily [24/11/2222:16:05:35 +0100] \"DELETE /sport/vol\"leyball HTTP/1.0\" 300 8\n" };

	private String[] logArray = DEFAULT_LOGS;

	public TrafficFakeLogThread() {
	}

//	public TrafficFakeLogThread(int speedSwitchTime) {
//		this.speedSwitchTime = speedSwitchTime;
//	}

	private int speed = 500;

	@Override
	public void run() {

//		Date lastSwitchDate = new Date();
//		int speed = HIGH_SPEED;
		try {
			while (!isInterrupted()) {
				try (BufferedWriter writer = new BufferedWriter(new FileWriter(LOG_FILE_PATH, true))) {
					for (String log : logArray) {
						StringBuilder sb = new StringBuilder();
						try (Formatter formatter = new Formatter(sb, Locale.US)) {
							formatter.format(log, dateFormat.format(new Date()));
						}
						writer.write(sb.toString());
					}

//					Date currentDate = new Date();
//					if (currentDate.getTime() - lastSwitchDate.getTime() > speedSwitchTime) {
//						speed = speed == HIGH_SPEED ? LOW_SPEED : HIGH_SPEED;
//						lastSwitchDate = currentDate;
//					}
				} catch (IOException e) {
//			logger.error(e.getMessage(), e);
				}
				TimeUnit.MILLISECONDS.sleep(speed);
			}
		} catch (InterruptedException e) {
//				logger.error(e.getMessage(), e);
		}
	}

	public int getSpeed() {
		return speed;
	}

	public synchronized void setSpeed(int speed) {
		this.speed = speed;
	}

	public void setLogArray(String[] logArray) {
		this.logArray = logArray;
	}

}
