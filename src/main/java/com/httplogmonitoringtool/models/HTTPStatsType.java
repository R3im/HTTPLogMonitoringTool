package com.httplogmonitoringtool.models;

/**
 * HTTP statistics type
 * 
 * @author Remi c
 *
 */
public enum HTTPStatsType {

	TOTAL_REQUESTS("Total requests"), TOTAL_CONTENT("Total content size"),
	TOTAL_BAD_FORMAT_LOG("Total bad format logs");

	private final String value;

	/**
	 * @param value
	 */
	private HTTPStatsType(final String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	@Override
	public String toString() {
		return value;
	}
}