package com.httplogmonitoringtool.exceptions;

/**
 * Throwed when parsing http traffic log
 * 
 * @author Remi c
 *
 */
public class HTTPLogRowFormatException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public HTTPLogRowFormatException(String message) {
		super(message);
	}
}
