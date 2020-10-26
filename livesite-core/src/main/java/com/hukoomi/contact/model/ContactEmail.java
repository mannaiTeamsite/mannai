package com.hukoomi.contact.model;

public class ContactEmail {
	
	private String senderName;

	private String senderEmail;

	private String emailText;

	/* private ContactHukoomiEmailSubject emailSubject; */
	
	private String emailSubject;

	public String getEmailSubject() {
		return emailSubject;
	}

	public void setEmailSubject(String emailSubject) {
		this.emailSubject = emailSubject;
	}

	public String getSenderName() {
		return senderName;
	}

	public void setSenderName(String senderName) {
		this.senderName = senderName;
	}

	public String getSenderEmail() {
		return senderEmail;
	}

	public void setSenderEmail(String senderEmail) {
		this.senderEmail = senderEmail;
	}

	public String getEmailText() {
		return emailText;
	}

	public void setEmailText(String emailText) {
		this.emailText = emailText;
	}

	/*
	 * public ContactHukoomiEmailSubject getEmailSubject() { return emailSubject; }
	 * 
	 * public void setEmailSubject(ContactHukoomiEmailSubject emailSubject) {
	 * this.emailSubject = emailSubject; }
	 */


}
