package com.httplogmonitoringtool.model;

public enum HTTPStatsType {
	
	TOTAL_REQUESTS("Total requests"),
	VALID_REQUESTS("Valid requests"),
	FAILED_REQUESTS("Failed requests"),
	NOTFOUND_REQUESTS("Not found requests"),
	TOTAL_CONTENT("Total content size");

    private final String value;

    /**
     * @param value
     */
    private HTTPStatsType(final String value) {
        this.value = value;
    }

    @Override
    public String toString() {
    	return value;
    }
}