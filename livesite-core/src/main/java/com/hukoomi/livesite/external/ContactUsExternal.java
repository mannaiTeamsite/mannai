package com.hukoomi.livesite.external;

import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

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
import com.hukoomi.utils.CommonUtils;
import com.hukoomi.utils.GoogleRecaptchaUtil;
import com.interwoven.livesite.runtime.RequestContext;

public class ContactUsExternal {
    /** Logger object to check the flow of the code. */
    private static final Logger LOGGER =
            Logger.getLogger(ContactUsExternal.class);
    /** initialization of context parameter with captcha. */
    private static final String RECAPTCHA_RESPONSE = "captcha";
    /** initialization of email body text. */
    private static final String EMAIL_START_TEXT = "text.email_start";
    /** initialization of email send text. */
    private static final String EMAIL_SENT_TEXT = "text.mail_sent";
    /** initialization of error in email send. */
    private static final String ERROR_MAIL_SENDING_TEXT =
            "error.sending.email ";
    /** initialization of config code variable. */
    private static final String CONFIG_CODE_HUKOOMI_CONTACT_TO_MAIL =
            "Hukoomi_Contact_To_Mail";
    /** initialization of config code variable. */
    private static final String CONFIG_CODE_HUKOOMI_CONTACT_FROM_MAIL =
            "Hukoomi_Contact_From_Mail";
    /** initialization of config code variable. */
    private static final String CONFIG_CODE_HUKOOMI_CONTACT_MAIL_HOST =
            "Hukoomi_Contact_Mail_Host";
    /** initialization of config code variable. */
    private static final String CONFIG_CODE_HUKOOMI_CONTACT_MAIL_PORT =
            "Hukoomi_Contact_Mail_Port";
    /** initialization of error in recaptcha for validation. */
    private static final String ERROR_RECAPTCHA_TEXT =
            "error.captcha.feedback";
    /** initialization of error variable. */
    private static final String STATUS_ERROR = "error";
    /** initialization of success variable. */
    private static final String STATUS_SUCCESS = "success";
    /** initialization of resource bundle path. */
    private static final String RESOURCE_BUNDLE_PATH =
            "com.hukoomi.resources.ContactUs";
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
        String msg = "";
        String language = "";
        action = context.getParameterString("action");
        LOGGER.debug("action:" + action);
        language = context.getParameterString("locale");
        Locale locale = new CommonUtils().getLocale(language);
        LOGGER.debug("lang:" + locale);
        ResourceBundle bundle =
                ResourceBundle.getBundle(RESOURCE_BUNDLE_PATH, locale);
        if (action.equals("sendmail")) {
            LOGGER.debug("Sendingemail.");
            senderName = context.getParameterString("senderName");
            senderEmail = context.getParameterString("senderEmail");
            emailSubject = context.getParameterString("emailSubject");
            emailText = context.getParameterString("emailText");
            gRecaptchaResponse =
                    context.getParameterString(RECAPTCHA_RESPONSE);
            LOGGER.debug("senderName:" + senderName);
            LOGGER.debug("senderEmail:" + senderEmail);
            LOGGER.debug("emailText:" + emailText);
            LOGGER.debug("emailSubject:" + emailSubject);
            LOGGER.debug("language:" + language);
            setValueToContactModel(senderName, senderEmail, emailText,
                    emailSubject, locale);
            GoogleRecaptchaUtil captchUtil = new GoogleRecaptchaUtil();
            verify = captchUtil.validateCaptch(gRecaptchaResponse);

            if (verify) {
                document = sendEmailToHukoomi(context);
            } else {
                status = STATUS_ERROR;
                msg = bundle.getString(ERROR_RECAPTCHA_TEXT);
                return getDocument(status, msg);
            }

            return document;
        } else if (action.equals("inquiryTypes")) {
            document = getInquiryTypes(bundle, language);

            return document;
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
    private void setValueToContactModel(final String senderName,
            final String senderEmail, final String emailText,
            final String emailSubject, final Locale lang) {
        if (senderName != null) {
            email.setSenderName(senderName);
        }
        if (senderEmail != null) {
            email.setSenderEmail(senderEmail);
        }
        if (emailText != null) {
            email.setEmailText(emailText);
        }
        if (emailSubject != null) {

            email.setEmailSubject(emailSubject);
        }
        if (lang != null) {
            email.setLanguage(lang);
        }

    }

    /**
     * this method will get the bundle and language returns the inquiry
     * type.
     *
     * @param bundle
     * @param language
     * @return document document with inquiry type
     */
    private Document getInquiryTypes(final ResourceBundle bundle,
            final String language) {

        String inquiryTypes = bundle.getString("text.mail_subject");
        Document document = DocumentHelper.createDocument();
        Element resultElement = document.addElement("Result");
        String[] arrOfStr = inquiryTypes.split(",");
        for (int i = 0; i < arrOfStr.length; i++) {
            String value = arrOfStr[i].split("!")[0];
            String text = arrOfStr[i].split("!")[1];
            Element optionElement = resultElement.addElement("Option");
            Element valueele = optionElement.addElement("value");
            valueele.setText(value);
            Element textele = optionElement.addElement("text");
            if (language.equals("ar")) {
                textele.setText(
                        new CommonUtils().decodeToArabicString(text));
            } else {
                textele.setText(text);
            }

        }
        return document;
    }

    /**
     * this method will create and send mail. returns document with status.
     *
     * @param context context component context passed with param
     * @return returns document with status
     */
    public Document sendEmailToHukoomi(final RequestContext context) {
        LOGGER.debug("sendEmailToHukoomi: Enter");
        ResourceBundle bundle = ResourceBundle
                .getBundle(RESOURCE_BUNDLE_PATH, email.getLanguage());
        String status = "";
        String msg = "";
        MimeMessage mailMessage;
        try {
            mailMessage = createMailMessage(context);
            Transport.send(mailMessage);
        } catch (MessagingException e) {
            e.printStackTrace();
            status = STATUS_ERROR;
            msg = bundle.getString(ERROR_MAIL_SENDING_TEXT);
            return getDocument(status, msg);

        } catch (MailException e) {
            status = STATUS_ERROR;
            msg = bundle.getString(ERROR_MAIL_SENDING_TEXT);
            return getDocument(status, msg);
        }
        status = STATUS_SUCCESS;
        msg = bundle.getString(EMAIL_SENT_TEXT);
        LOGGER.debug("sendEmailToHukoomi: msg:" + msg);
        return getDocument(status, msg);
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
        CommonUtils util = new CommonUtils();
        String from = util.getConfiguration(
                CONFIG_CODE_HUKOOMI_CONTACT_FROM_MAIL, context);
        String to = util.getConfiguration(
                CONFIG_CODE_HUKOOMI_CONTACT_TO_MAIL, context);
        String host = util.getConfiguration(
                CONFIG_CODE_HUKOOMI_CONTACT_MAIL_HOST, context);
        String port = util.getConfiguration(
                CONFIG_CODE_HUKOOMI_CONTACT_MAIL_PORT, context);
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
        LOGGER.debug("page:" + context.getParameterString("page"));
        ResourceBundle bundle = ResourceBundle
                .getBundle(RESOURCE_BUNDLE_PATH, email.getLanguage());
        LOGGER.debug("before setting subject:");
        if (context.getParameterString("page").equals("errorPage")) {
            subject = email.getEmailSubject();
            msg.setSubject(subject);
        } else {
            String sub = email.getEmailSubject().toUpperCase();
            subject = "text.mail_subject." + sub.replace(" ", "_");
            msg.setSubject(bundle.getString(subject));
        }
        LOGGER.debug("after setting subject:");

        StringBuilder sb = new StringBuilder();
        sb.append(bundle.getString(EMAIL_START_TEXT));
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
    private Document getDocument(final String status, final String msg) {
        Document document = DocumentHelper.createDocument();
        Element resultElement = document.addElement("Result");
        Element statusElement = resultElement.addElement("status");
        statusElement.setText(status);
        Element msgElement = resultElement.addElement("message");
        msgElement.setText(msg);
        return document;
    }

}
