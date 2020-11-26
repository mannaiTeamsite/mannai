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
     * @return emailSubject
     */
    public String getEmailSubject() {
        return emailSubject;
    }
    /**
     * @param emailSubject
     */
    public void setEmailSubject(final String emailSubject) {
        this.emailSubject = emailSubject;
    }
    /**
     * @return senderName
     */
    public String getSenderName() {
        return senderName;
    }
    /**
     * @param senderName
     */
    public void setSenderName(final String senderName) {
        this.senderName = senderName;
    }
    /**
     * @return senderEmail
     */
    public String getSenderEmail() {
        return senderEmail;
    }
    /**
     * @param senderEmail
     */
    public void setSenderEmail(final String senderEmail) {
        this.senderEmail = senderEmail;
    }
    /**
     * @return emailText
     */
    public String getEmailText() {
        return emailText;
    }

    /**
     * @param emailText
     */
    public void setEmailText(final String emailText) {
        this.emailText = emailText;
    }
    /**
     * @return locale
     */
    public Locale getLanguage() {
        return locale;
    }
    /**
     * @param locale
     */
    public void setLanguage(final Locale locale) {
        this.locale = locale;
    }


}
