package com.httplogmonitoringtool.model;

public enum HTTPStatsAlertType {
	
	HIGH_TRAFFIC("High traffic generated an alert - hits = %s, triggered at %s"),
	LOW_TRAFFIC("Back to normal traffic - hits = %s, triggered at %s");

    private final String value;

    /**
     * @param value
     */
    private HTTPStatsAlertType(final String value) {
        this.value = value;
    }

    @Override
    public String toString() {
    	return value;
    }
}