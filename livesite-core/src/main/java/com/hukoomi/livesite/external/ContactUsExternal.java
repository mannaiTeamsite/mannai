package com.hukoomi.livesite.external;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.springframework.mail.MailException;

import com.hukoomi.contact.model.ContactEmail;
import com.hukoomi.utils.GoogleRecaptchaUtil;
import com.hukoomi.utils.PropertiesFileReader;
import com.hukoomi.utils.ValidationUtils;
import com.hukoomi.utils.XssUtils;
import com.interwoven.livesite.runtime.RequestContext;

public class ContactUsExternal {
    /** Logger object to check the flow of the code. */
    private static final Logger LOGGER =
            Logger.getLogger(ContactUsExternal.class);
    /** Contact us properties key. */
    private static final String CONTACT_TO_MAIL = "sentTo";
    /** Contact us properties key. */
    private static final String CONTACT_FROM_MAIL = "sentFrom";
    /** Contact us properties key. */
    private static final String CONTACT_MAIL_HOST = "host";
    /** Contact us properties key. */
    private static final String CONTACT_MAIL_PORT = "port";
    /** initialization of error variable. */
    private static final String STATUS_ERROR_RECAPTHCHA =
            "errorInRecaptcha";
    /** field validation status. */
    private static final String STATUS_FIELD_VALIDATION =
            "FieldValidationFailed";
    /** initialization of error variable. */
    private static final String STATUS_FAIL_MAIL_SENT = "mailSentFailed";
    /** initialization of success variable. */
    private static final String STATUS_SUCCESS = "success";
    /**
     * Properties object that holds the property values
     */
    private static Properties properties = null;
    /** object creation of ContactEmail. */
    private ContactEmail email = new ContactEmail();

    /**
     * this method will verify recaptcha and send mail to hukoomi.
     *
     * @param context component context passed with params
     * @return document
     */
    @SuppressWarnings("deprecation")
    public Document sendEmail(final RequestContext context) {
        Document document = DocumentHelper.createDocument();
        boolean verify = false;
        String senderName = null;
        String senderEmail = null;
        String emailSubject = null;
        String emailText = null;
        String action = null;
        String gRecaptchaResponse = null;
        String status = "";
        action = context.getParameterString("action");
        LOGGER.debug("action:" + action);
        if (action.equals("sendmail")) {
            LOGGER.info("Sendingemail.");
            LOGGER.debug("page:" + context.getParameterString("page"));

            senderName = context.getParameterString("senderName");

            senderEmail = context.getParameterString("senderEmail");
            emailSubject = context.getParameterString("emailSubject");
            emailText = context.getParameterString("emailText");
            gRecaptchaResponse = context.getParameterString("captcha");
            boolean validation = setValueToContactModel(senderName,
                    senderEmail, emailText, emailSubject);
            if (validation) {
                GoogleRecaptchaUtil captchUtil = new GoogleRecaptchaUtil();
                verify = captchUtil.validateCaptcha(context,
                        gRecaptchaResponse);
                LOGGER.debug("Recapcha verification status:" + verify);
                if (verify) {
                    document = sendEmailToHukoomi(context);
                } else {
                    status = STATUS_ERROR_RECAPTHCHA;
                    return getDocument(status);
                }
                return document;
            } else {
                status = STATUS_FIELD_VALIDATION;
                return getDocument(status);
            }
        }
        return document;
    }

    /**
     * this method will assign value to ContactEmail properties.
     *
     * @param senderName
     * @param senderEmail
     * @param emailText
     * @param emailSubject
     * @param lang
     */
    private boolean setValueToContactModel(final String senderName,
            final String senderEmail, final String emailText,
            final String emailSubject) {
        ValidationUtils util = new ValidationUtils();
        XssUtils xssUtils = new XssUtils();
        LOGGER.info("setValueToContactModel: Enter");
        if (senderName != null && senderEmail != null && emailText != null
                && emailSubject != null) {
            if (senderName.length() <= 100) {
                email.setSenderName(xssUtils.stripXSS(senderName));
            } else {
                return false;
            }
            if (senderEmail.length() <= 50 && util
                    .validateEmailId(xssUtils.stripXSS(senderEmail))) {
                email.setSenderEmail(senderEmail);
            } else {
                return false;
            }
            if (emailText.length() <= 2500) {
                email.setEmailText(xssUtils.stripXSS(emailText));
            } else {
                return false;
            }
            email.setEmailSubject(xssUtils.stripXSS(emailSubject));

            return true;
        }

        return false;
    }

    /**
     * this method will create and send mail. returns document with status.
     *
     * @param context context component context passed with param
     * @return returns document with status
     */
    public Document sendEmailToHukoomi(final RequestContext context) {
        LOGGER.info("sendEmailToHukoomi: Enter");
        String status = "";
        MimeMessage mailMessage;
        try {
            mailMessage = createMailMessage(context);
            Transport.send(mailMessage);
        } catch (MessagingException | MailException e) {
            status = STATUS_FAIL_MAIL_SENT;
            LOGGER.error("Exception in sendEmailToHukoomi: ", e);
            return getDocument(status);
        }
        status = STATUS_SUCCESS;
        return getDocument(status);
    }

    /**
     * this method will create mail. returns MimeMessage.
     *
     * @param context
     * @return msg returns MimeMessage
     * @throws MessagingException
     */
    private MimeMessage createMailMessage(final RequestContext context)
            throws MessagingException {
        LOGGER.info("createMailMessage: Enter");
        ContactUsExternal.loadProperties(context);
        String from = properties.getProperty(CONTACT_FROM_MAIL);
        String to = properties.getProperty(CONTACT_TO_MAIL);
        String host = properties.getProperty(CONTACT_MAIL_HOST);
        String port = properties.getProperty(CONTACT_MAIL_PORT);
        Properties props = new Properties();
        String subject = "";
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.starttls.enable", "false");
        props.put("mail.smtp.port", port);
        Session session = Session.getDefaultInstance(props, null);
        session.setDebug(true);
        MimeMessage msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(from));
        msg.setRecipient(Message.RecipientType.TO,
                new InternetAddress(to));
        subject = email.getEmailSubject();
        msg.setSubject(subject);
        StringBuilder sb = new StringBuilder();
        sb.append(context.getParameterString("emailStartText"));
        sb.append(email.getSenderName());
        sb.append(" ");
        sb.append(email.getSenderEmail());
        sb.append(" ");
        sb.append(email.getEmailText());
        msg.setText(sb.toString());
        LOGGER.debug("msg:" + msg);
        return msg;

    }

    /**
     * this method get the strings and generate document.
     *
     * @param status
     * @param msg
     * @return document document with elements
     */
    private Document getDocument(final String status) {
        LOGGER.info("getDocument: Enter");
        Document document = DocumentHelper.createDocument();
        Element resultElement = document.addElement("Result");
        Element statusElement = resultElement.addElement("status");
        statusElement.setText(status);
        return document;
    }

    /**
     * This method will be used to load the configuration properties.
     *
     * @param context The parameter context object passed from Component.
     *
     */
    private static void loadProperties(final RequestContext context) {
        if (properties == null) {
            PropertiesFileReader propertyFileReader =
                    new PropertiesFileReader(context,
                            "contactus.properties");
            ContactUsExternal.properties =
                    propertyFileReader.getPropertiesFile();
        }
    }

}
