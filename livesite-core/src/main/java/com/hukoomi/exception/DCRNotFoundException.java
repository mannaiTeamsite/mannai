/*
 * Custom Exception if the DCR is not available to be served on the page.
 */
package com.hukoomi.exception;

public class DCRNotFoundException extends RuntimeException {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public DCRNotFoundException(String message) {
        super(message);
    }
}
