package com.hukoomi.contact.model;

import java.util.Locale;

public class ContactEmail {

    /** email senderName. */
    private String senderName;
    /** email sender emailid. */
	private String senderEmail;
	/** email text. */
	private String emailText;
	/** email locale. */
	private Locale locale;
	/** email subject. */
	private String emailSubject;

	/**
	 * @return
	 */
	public String getEmailSubject() {
		return emailSubject;
	}

	/**
	 * @param emailSubject
	 */
	public void setEmailSubject(String emailSubject) {
		this.emailSubject = emailSubject;
	}

	/**
	 * @return
	 */
	public String getSenderName() {
		return senderName;
	}

	/**
	 * @param senderName
	 */
	public void setSenderName(String senderName) {
		this.senderName = senderName;
	}

	/**
	 * @return
	 */
	public String getSenderEmail() {
		return senderEmail;
	}

	/**
	 * @param senderEmail
	 */
	public void setSenderEmail(String senderEmail) {
		this.senderEmail = senderEmail;
	}

	/**
	 * @return
	 */
	public String getEmailText() {
		return emailText;
	}

	/**
	 * @param emailText
	 */
	public void setEmailText(String emailText) {
		this.emailText = emailText;
	}
	/**
	 * @return
	 */
	public Locale getLanguage() {
		return locale;
	}
	/**
	 * @param locale
	 */
	public void setLanguage(Locale locale) {
		this.locale = locale;
	}


}
