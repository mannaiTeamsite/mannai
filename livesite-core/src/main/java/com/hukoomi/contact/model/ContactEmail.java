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
     * @param subject
     */
    public void setEmailSubject(final String subject) {
        this.emailSubject = subject;
    }
    /**
     * @return senderName
     */
    public String getSenderName() {
        return senderName;
    }
    /**
     * @param name
     */
    public void setSenderName(final String name) {
        this.senderName = name;
    }
    /**
     * @return senderEmail
     */
    public String getSenderEmail() {
        return senderEmail;
    }
    /**
     * @param email
     */
    public void setSenderEmail(final String email) {
        this.senderEmail = email;
    }
    /**
     * @return emailText
     */
    public String getEmailText() {
        return emailText;
    }

    /**
     * @param text
     */
    public void setEmailText(final String text) {
        this.emailText = text;
    }
    /**
     * @return locale
     */
    public Locale getLanguage() {
        return locale;
    }
    /**
     * @param siteLocale
     */
    public void setLanguage(final Locale siteLocale) {
        this.locale = siteLocale;
    }


}
