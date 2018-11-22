package com.httplogmonitoringtool.model;

import java.util.Date;

public class HTTPStatsAlert {
	private HTTPStatsAlertType type;
	private int totalTraffic;
	private Date date;
	
	public HTTPStatsAlert(HTTPStatsAlertType type, int totalTraffic, Date date) {
		super();
		this.type = type;
		this.totalTraffic = totalTraffic;
		this.date = date;
	}
	
}
