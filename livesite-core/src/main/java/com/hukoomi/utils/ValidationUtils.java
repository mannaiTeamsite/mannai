package com.hukoomi.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ValidationUtils {

	/** regex for emailId. */
	private static final String EMAIL_PATTERN = "(\\\\.[a-zA-Z0-9][a-zA-Z0-9\\\\-]{0,25})(\\\\.[a-zA-Z0-9][a-zA-Z0-9\\\\-]{0,25})?(\\\\.[a-zA-Z0-9][a-zA-Z0-9\\\\-]{0,25})?(\\\\.[a-zA-Z0-9][a-zA-Z0-9\\\\-]{0,25})?";
	/** regex for alphanumeric with space. */
	private static final String ALPHANUMERIC_SPACE_PATTERN = "^[ 0-9A-Za-z]+$";
	/** regex for numeric space. */
	private static final String NUMERIC_PATTERN = "^[[0-9]]+$";
	/** regex for alphanumeric. */
	private static final String ALPHANUMERIC_PATTERN = "^[0-9A-Za-z]+$";
	/** regex for alphabet. */
	private static final String ALPHABET_SPACE_PATTERN = "^[ A-Za-z]+$";
	/** regex for comments. */
	private static final String COMMENTS_PATTERN = "^[a-zA-Z0-9-_().,:\"@&\n\t ]+$";
	/** regex for alphanumeric with some special character. */
	private static final String ALPHANUMERIC_ADDITIONAL_PATTERN = "^[a-zA-Z0-9-_().,:@& ]+$";

	/**
	 * this method validates alphanumeric with space
	 * 
	 * @param str
	 * @return b
	 */
	public boolean validateField(String str) {
		Pattern p = Pattern.compile(ALPHANUMERIC_SPACE_PATTERN);
		Matcher m = p.matcher(str);
		return m.matches();
	}

	/**
	 * this method validates emailId
	 * 
	 * @param str
	 * @return b
	 */
	public boolean validateEmailId(String str) {
		Pattern p = Pattern.compile(EMAIL_PATTERN);
		Matcher m = p.matcher(str);
		return m.matches();
	}

	/**
	 * this method validates numeric values
	 * 
	 * @param str
	 * @return b
	 */
	public boolean validateNumeric(String str) {
		Pattern p = Pattern.compile(NUMERIC_PATTERN);
		Matcher m = p.matcher(str);
		return m.matches();
	}

	/**
	 * this method validates olny alphanumeric
	 * 
	 * @param str
	 * @return b
	 */
	public boolean validateAlphaNumeric(String str) {
		Pattern p = Pattern.compile(ALPHANUMERIC_PATTERN);
		Matcher m = p.matcher(str);
		return m.matches();
	}

	/**
	 * this method validates alphabet with space
	 * 
	 * @param str
	 * @return b
	 */
	public boolean validateAlphabet(String str) {
		Pattern p = Pattern.compile(ALPHABET_SPACE_PATTERN);
		Matcher m = p.matcher(str);
		return m.matches();
	}

	/**
	 * this method validates alphanumeric with space and some special character
	 * 
	 * @param str
	 * @return b
	 */
	public boolean validateAdditionalPattern(String str) {
		Pattern p = Pattern.compile(ALPHANUMERIC_ADDITIONAL_PATTERN);
		Matcher m = p.matcher(str);
		return m.matches();
	}

	/**
	 * this method validates comments
	 * 
	 * @param str
	 * @return b
	 */
	public boolean validateComments(String str) {
		Pattern p = Pattern.compile(COMMENTS_PATTERN);
		Matcher m = p.matcher(str);
		return m.matches();
	}

}