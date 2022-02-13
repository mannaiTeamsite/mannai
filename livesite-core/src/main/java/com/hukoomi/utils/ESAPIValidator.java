package com.hukoomi.utils;

public class ESAPIValidator {
	/** Logger object to check the flow of the code. */
	private ESAPIValidator() {

	}

	public static final String EMAIL_ID = "EmailId";
	public static final String ALPHANUMERIC = "Alphanumeric";
	public static final String ALPHANUMERIC_SPACE = "Alphanumeric_space";
	public static final String ALPHANUMERIC_HYPHEN = "Alphanumeric_hyphen";
	public static final String NUMERIC = "Numeric";
	public static final String ALPHABET = "Alphabet";
	public static final String ALPHABET_SPACE = "AlphabetSpace";
	public static final String ALPHABET_HYPEN = "AlphabetHypen";
	public static final String IP_ADDRESS = "IPAddress";
	public static final String USER_ID = "UserId";
	public static final String TEXT = "Text";
	public static final String URL = "URL";

	public static boolean checkNull(String inputValue) {
		return (inputValue == null || "null".equalsIgnoreCase(inputValue) || "".equalsIgnoreCase(inputValue));
	}

}
