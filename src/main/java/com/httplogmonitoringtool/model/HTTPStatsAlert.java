package com.httplogmonitoringtool.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Formatter;
import java.util.Locale;

public class HTTPStatsAlert {
	private HTTPStatsAlertType type;
	private int trafficAverage;
	private Date date;

	private final static SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

	public HTTPStatsAlert(HTTPStatsAlertType type, int trafficAverage, Date date) {
		super();
		this.type = type;
		this.trafficAverage = trafficAverage;
		this.date = date;
	}

	public HTTPStatsAlertType getType() {
		return type;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		try (Formatter formatter = new Formatter(sb, Locale.US)) {
			formatter.format(type.toString(), trafficAverage, dateFormat.format(date));
		}

		return sb.toString();
	}

}
