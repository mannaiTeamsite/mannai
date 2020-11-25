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
	private static final Logger logger = Logger.getLogger(ContactUsExternal.class);

	private static final String RECAPTCHA_RESPONSE_PARAMETER = "captcha";
	private static final String EMAIL_START_TEXT = "text.email_start";
	private static final String EMAIL_SENT_TEXT = "text.mail_sent";
	private static final String ERROR_MAIL_SENDING_TEXT = "error.sending.email ";
	private static final String CONFIG_CODE_HUKOOMI_CONTACT_TO_MAIL = "Hukoomi_Contact_To_Mail";
	private static final String CONFIG_CODE_HUKOOMI_CONTACT_FROM_MAIL = "Hukoomi_Contact_From_Mail";
	private static final String CONFIG_CODE_HUKOOMI_CONTACT_MAIL_HOST = "Hukoomi_Contact_Mail_Host";
	private static final String CONFIG_CODE_HUKOOMI_CONTACT_MAIL_PORT = "Hukoomi_Contact_Mail_Port";
	private static final String ERROR_RECAPTCHA_TEXT = "error.captcha.feedback";
	private static final String STATUS_ERROR = "error";
	private static final String STATUS_SUCCESS = "success";
	private static final String RESOURCE_BUNDLE_PATH = "com.hukoomi.resources.ContactUs";

	/**
	 * this method will verify recaptcha and send mail to hukoomi
	 * 
	 * @param context component context passed with params
	 * @return document
	 */
	@SuppressWarnings("deprecation")
	public Document sendEmail(RequestContext context) {
		Document document = DocumentHelper.createDocument();
		ContactEmail email = new ContactEmail();
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
		logger.debug("action:" + action);
		language = context.getParameterString("locale");
		Locale locale = new CommonUtils().getLocale(language);
		logger.debug("lang:" + locale);
		ResourceBundle bundle = ResourceBundle.getBundle(RESOURCE_BUNDLE_PATH, locale);
		if (action.equals("sendmail")) {
			logger.debug("Sendingemail.");
			senderName = context.getParameterString("senderName");
			senderEmail = context.getParameterString("senderEmail");
			emailSubject = context.getParameterString("emailSubject");
			emailText = context.getParameterString("emailText");
			context.getParameterString("page");
			gRecaptchaResponse = context.getParameterString(RECAPTCHA_RESPONSE_PARAMETER);
			logger.debug("senderName:" + senderName);
			logger.debug("senderEmail:" + senderEmail);
			logger.debug("emailText:" + emailText);
			logger.debug("emailSubject:" + emailSubject);
			logger.debug("language:" + language);
			setValueToContactModel(email, senderName, senderEmail, emailText, emailSubject, locale);
			GoogleRecaptchaUtil captchUtil = new GoogleRecaptchaUtil();
			verify = captchUtil.validateCaptch(gRecaptchaResponse);

			if (verify) {
				document = sendEmailToHukoomi(email, context);
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
	 * this method will assign value to ContactEmail properties
	 * 
	 * @param email
	 * @param senderName
	 * @param senderEmail
	 * @param emailText
	 * @param emailSubject
	 * @param lang
	 */
	private void setValueToContactModel(ContactEmail email, String senderName, String senderEmail, String emailText,
			String emailSubject, Locale lang) {
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
	 * this method will get the bundle and language returns the inquiry type
	 * 
	 * @param bundle
	 * @param language
	 * @return document document with inquiry type
	 */
	private Document getInquiryTypes(ResourceBundle bundle, String language) {

		String validationMessage = bundle.getString("text.mail_subject");
		Document document = DocumentHelper.createDocument();
		Element resultElement = document.addElement("Result");
		String[] arrOfStr = validationMessage.split(",", 5);
		for (int i = 0; i < arrOfStr.length; i++) {
			String value = arrOfStr[i].split("!")[0];
			String text = arrOfStr[i].split("!")[1];
			Element optionElement = resultElement.addElement("Option");
			Element valueele = optionElement.addElement("value");
			valueele.setText(value);
			Element textele = optionElement.addElement("text");
			if (language.equals("ar")) {
				textele.setText(new CommonUtils().decodeToArabicString(text));
			} else {
				textele.setText(text);
			}

		}
		return document;
	}

	/**
	 * this method will create and send mail. returns document with status
	 * 
	 * @param email.   ContactEmail object
	 * @param context. context component context passed with param
	 * @return returns document with status
	 */
	public Document sendEmailToHukoomi(ContactEmail email, RequestContext context) {
		logger.debug("sendEmailToHukoomi: Enter");
		ResourceBundle bundle = ResourceBundle.getBundle(RESOURCE_BUNDLE_PATH, email.getLanguage());
		String status = "";
		String msg = "";
		MimeMessage mailMessage;
		try {
			mailMessage = createMailMessage(email, context);
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
		logger.debug("sendEmailToHukoomi: msg:" + msg);
		return getDocument(status, msg);
	}

	/**
	 * this method will create mail. returns MimeMessage
	 * 
	 * @param email
	 * @param context
	 * @return msg returns MimeMessage
	 * @throws MessagingException
	 */
	private MimeMessage createMailMessage(ContactEmail email, RequestContext context) throws MessagingException {

		String from = getmailserverProperties(CONFIG_CODE_HUKOOMI_CONTACT_FROM_MAIL, context);
		String to = getmailserverProperties(CONFIG_CODE_HUKOOMI_CONTACT_TO_MAIL, context);
		Properties props = new Properties();
		String subject = "";
		props.put("mail.smtp.host", getmailserverProperties(CONFIG_CODE_HUKOOMI_CONTACT_MAIL_HOST, context));
		props.put("mail.smtp.starttls.enable", "false");
		props.put("mail.smtp.port", getmailserverProperties(CONFIG_CODE_HUKOOMI_CONTACT_MAIL_PORT, context));
		Session session = Session.getDefaultInstance(props, null);
		session.setDebug(true);
		MimeMessage msg = new MimeMessage(session);
		msg.setFrom(new InternetAddress(from));
		msg.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
		logger.debug("page:" + context.getParameterString("page"));
		ResourceBundle bundle = ResourceBundle.getBundle(RESOURCE_BUNDLE_PATH, email.getLanguage());
		logger.debug("before setting subject:");
		if (context.getParameterString("page").equals("errorPage")) {
			subject = email.getEmailSubject();
			msg.setSubject(subject);
		} else {
			subject = "text.mail_subject." + email.getEmailSubject().toUpperCase().replace(" ", "_");
			msg.setSubject(bundle.getString(subject));
		}
		logger.debug("after setting subject:");

		StringBuilder sb = new StringBuilder();
		sb.append(bundle.getString(EMAIL_START_TEXT));
		sb.append(email.getSenderName()).append(" ").append(email.getSenderEmail()).append(" ")
				.append(email.getEmailText());
		msg.setText(sb.toString());
		logger.debug("msg:" + msg);
		return msg;

	}

	/**
	 * this method will take config parameter code and return config parameter value
	 * 
	 * @param paramCode
	 * @param context
	 * @return configParamValue return config parameter value
	 */
	private static String getmailserverProperties(String paramCode, RequestContext context) {
		CommonUtils utils = new CommonUtils();
		String configParamValue = null;
		if (paramCode != null && !"".equals(paramCode)) {
			if (utils.configParamsMap == null || utils.configParamsMap.isEmpty()) {
				utils.loadConfigparams(context);
			}
			configParamValue = utils.configParamsMap.get(paramCode);
			logger.debug("configParamValue:" + configParamValue);

		}
		return configParamValue;

	}

	/**
	 * this method get the strings and generate document
	 * 
	 * @param status
	 * @param msg
	 * @return document document with elements
	 */
	private Document getDocument(String status, String msg) {
		Document document = DocumentHelper.createDocument();
		Element resultElement = document.addElement("Result");
		Element statusElement = resultElement.addElement("status");
		statusElement.setText(status);
		Element msgElement = resultElement.addElement("message");
		msgElement.setText(msg);
		return document;
	}

}
