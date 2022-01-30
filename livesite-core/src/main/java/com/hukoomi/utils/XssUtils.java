package com.hukoomi.utils;

import java.util.regex.Pattern;

import org.apache.log4j.Logger;

/**
 * @author arbaj.shaikh
 *
 */
public class XssUtils {

	/** Logger object to check the flow of the code. */
	private final Logger logger = Logger.getLogger(XssUtils.class);

	private static Pattern[] patterns = new Pattern[] {
			// Script fragments
			Pattern.compile("<script>(.*?)</script>", Pattern.CASE_INSENSITIVE),
			// src='...'
			Pattern.compile("src[\r\n]*=[\r\n]*\\\'(.*?)\\\'",
					Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
			Pattern.compile("src[\r\n]*=[\r\n]*\\\"(.*?)\\\"",
					Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
			// lonely script tags
			Pattern.compile("</script>", Pattern.CASE_INSENSITIVE),
			Pattern.compile("<script(.*?)>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
			// eval(...)
			Pattern.compile("eval\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
			// expression(...)
			Pattern.compile("expression\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
			// javascript:...
			Pattern.compile("javascript:", Pattern.CASE_INSENSITIVE),
			// vbscript:...
			Pattern.compile("vbscript:", Pattern.CASE_INSENSITIVE),
			// onload(...)=...
			Pattern.compile("onload(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
			// semicolon...
			Pattern.compile(";", Pattern.MULTILINE | Pattern.DOTALL) };

	public String stripXSS(String value) {
		logger.info("XssUtils : stripXSS()");
		if (value != null) {

			logger.info("Input Value : " + value);

			// Avoid null characters
			value = value.replace("\0", "");

			// Remove all sections that match a pattern
			for (Pattern scriptPattern : patterns) {
				value = scriptPattern.matcher(value).replaceAll("");
			}
			logger.info("Final Stripped Value : " + value);

		}
		return value;
	}
}
