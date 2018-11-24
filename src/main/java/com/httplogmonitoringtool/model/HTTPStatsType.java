package com.httplogmonitoringtool.model;

public enum HTTPStatsType {

	TOTAL_REQUESTS("Total requests"), TOTAL_CONTENT("Total content size"), VALID_REQUESTS(200, "Valid requests"),
	FAILED_REQUESTS(400, "Bad Request requests"), NOTFOUND_REQUESTS(404, "Not Found requests"),
	MULTIPLECHOICES_REQUESTS(300, "Multiple Choices requests"),
	MOVEDPERMANENTLY_REQUESTS(301, "Moved Permanently requests"), FOUND_REQUESTS(302, "Found requests"),
	NOTMODIFIED_REQUESTS(304, "Not Modified requests"), TEMPORARYREDIRECT_REQUESTS(307, "Temporary Redirect requests"),
	UNAUTHORIZED_REQUESTS(401, "Unauthorized requests"), FORBIDDEN_REQUESTS(403, "Forbidden requests"),
	GONE_REQUESTS(410, "Gone requests"), INTERNALSERVERERROR_REQUESTS(500, "Internal Server Error requests"),
	NOTIMPLEMENTED_REQUESTS(501, "Not Implemented requests"),
	SERVICEUNAVAILABLE_REQUESTS(503, "Service Unavailable requests"),
	PERMISSIONDENIED_REQUESTS(550, "Permission Denied requests"), 
	TOTAL_BAD_FORMAT_LOG(-2,"Total bad format logs");

	private final int code;
	private final String value;

	/**
	 * @param value
	 */
	private HTTPStatsType(final String value) {
		this.value = value;
		this.code = -1;
	}

	/**
	 * 
	 * @param code
	 * @param value
	 */
	private HTTPStatsType(final int code, final String value) {
		this.code = code;
		this.value = value;
	}

	public int getCode() {
		return code;
	}

	public String getValue() {
		return value;
	}

	@Override
	public String toString() {
		return value;
	}
}