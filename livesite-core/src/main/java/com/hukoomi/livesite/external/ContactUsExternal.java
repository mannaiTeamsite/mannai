package com.hukoomi.livesite.external;

import com.interwoven.livesite.runtime.RequestContext;

import com.hukoomi.contact.model.ContactEmail;
import com.hukoomi.utils.Postgre;
import com.hukoomi.utils.VerifyRecaptcha;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.mail.MailException;

public class ContactUsExternal {
	private static final Logger logger = Logger.getLogger(ContactUsExternal.class);

	private static final String EMAIL_ATTRIBUTE = "email";
	private static final String EMAIL_SENT_SUCCESSFULLY = "emailSent";
	private static final String EMAIL_SUBJECT_LIST_ATTRIBUTE = "subjectList";
	private static final String RECAPTCHA_RESPONSE_PARAMETER = "captcha";
	private static final String EMAIL_START_TEXT = "text.email_start";
	private static final String EMAIL_SENT_TEXT = "text.mail_sent";
	private static final String ERROR_MAIL_SENDING_TEXT = "error.sending.email ";
	private static final String CONFIG_CODE_HUKOOMI_CONTACT_TO_MAIL = "Hukoomi_Contact_To_Mail";
	private static final String CONFIG_CODE_HUKOOMI_CONTACT_FROM_MAIL = "Hukoomi_Contact_From_Mail";
	private static final String CONFIG_CODE_HUKOOMI_CONTACT_MAIL_HOST = "Hukoomi_Contact_Mail_Host";
	private static final String CONFIG_CODE_HUKOOMI_CONTACT_MAIL_PORT = "Hukoomi_Contact_Mail_Port";
	private static final String CONFIG_CODE_HUKOOMI_CONTACT_MAIL_AUTH = "Hukoomi_Contact_Mail_Auth";
	private static final String ERROR_RECAPTCHA_TEXT = "error.captcha.feedback";
	private static final String STATUS_ERROR = "error";
	private static final String STATUS_SUCCESS = "success";
	private static final String RESOURCE_BUNDLE_PATH = "com.hukoomi.resources.ContactUs";
	private static Connection connection;
	static {
		ContactUsExternal.connection = null;
	}
	private static HashMap<String, String> configParamsMap = new HashMap<String, String>();

	@SuppressWarnings("deprecation")
	public Document sendEmail(RequestContext context) {
		Document document = DocumentHelper.createDocument();
		boolean verify = false;
		String senderName = "";
		String senderEmail = "";
		String emailSubject = "";
		String emailText = "";
		String action = "";
		String gRecaptchaResponse = "";
		ContactEmail email = new ContactEmail();
		String status = "";
		String msg = "";
		String language = "";
		action = context.getParameterString("action");
		logger.debug("action:" + action);
		language = context.getParameterString("language");
		Locale lang = getLocaleLanguage(language);
		ResourceBundle bundle = ResourceBundle.getBundle(RESOURCE_BUNDLE_PATH, lang);
		if (action.equals("sendmail")) {
			logger.debug("Sendingemail.");
			senderName = context.getParameterString("senderName");
			senderEmail = context.getParameterString("senderEmail");
			emailSubject = context.getParameterString("emailSubject");
			emailText = context.getParameterString("emailText");
			gRecaptchaResponse = context.getParameterString(RECAPTCHA_RESPONSE_PARAMETER);
			logger.debug("senderName:" + senderName);
			logger.debug("senderEmail:" + senderEmail);
			logger.debug("emailText:" + emailText);
			logger.debug("emailSubject:" + emailSubject);
			logger.debug("language:" + language);
			try {
				
				if (!senderName.equals("")) {
					email.setSenderName(senderName);
				}
				if (!senderEmail.equals("")) {
					email.setSenderEmail(senderEmail);
				}
				if (!emailText.equals("")) {
					email.setEmailText(emailText);
				}
				if (!emailSubject.equals("")) {
					email.setEmailSubject(emailSubject);
				}
				if (!language.equals("")) {
					email.setLanguage(getLocaleLanguage(language));
				}
				verify = VerifyRecaptcha.verify(gRecaptchaResponse);

				if (verify) {
					document = sendEmailToHukoomi(email,context);
				} else {
					status = STATUS_ERROR;
					msg = bundle.getString(ERROR_RECAPTCHA_TEXT);
					return getDocument(status, msg);
				}

				return document;

			} catch (IOException e) {

				status = STATUS_ERROR;
				msg = bundle.getString(ERROR_RECAPTCHA_TEXT);
				return getDocument(status, msg);
			}
		} else if (action.equals("inquiryTypes")) {
			
			String validationMessage = bundle.getString("text.mail_subject");
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
					textele.setText(decodeToArabicString(text));
				} else {
					textele.setText(text);
				}

			}
			return document;
		}

		return document;
	}

	public String decodeToArabicString(String str) {

		byte[] charset = str.getBytes(StandardCharsets.UTF_8);
		return new String(charset, StandardCharsets.UTF_8);
	}

	private Document getDocument(String status, String msg) {
		Document document = DocumentHelper.createDocument();
		Element resultElement = document.addElement("Result");
		Element statusElement = resultElement.addElement("status");
		statusElement.setText(status);
		Element msgElement = resultElement.addElement("message");
		msgElement.setText(msg);
		return document;
	}

	public Locale getLocaleLanguage(String language) {
		switch (language) {
		case "en":
			return Locale.ENGLISH;
		case "ar":
			return new Locale("ar");
		default:
			return Locale.ENGLISH;
		}
	}

	public Document sendEmailToHukoomi(ContactEmail email,RequestContext context) {

		ResourceBundle bundle = ResourceBundle.getBundle(RESOURCE_BUNDLE_PATH, email.getLanguage());
		String status = "";
		String msg = "";
		MimeMessage mailMessage;
		try {
			mailMessage = createMailMessage(email,context);
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
		return getDocument(status, msg);
	}

	private MimeMessage createMailMessage(ContactEmail email,RequestContext context) throws MessagingException {

		String from = getmailserverProperties(CONFIG_CODE_HUKOOMI_CONTACT_FROM_MAIL,context);
		String to = getmailserverProperties(CONFIG_CODE_HUKOOMI_CONTACT_TO_MAIL,context);
		Properties props = new Properties();
		props.put("mail.smtp.host", getmailserverProperties(CONFIG_CODE_HUKOOMI_CONTACT_MAIL_HOST,context));
		props.put("mail.smtp.starttls.enable", "false");
		props.put("mail.smtp.port", getmailserverProperties(CONFIG_CODE_HUKOOMI_CONTACT_MAIL_PORT,context));
		Session session = Session.getDefaultInstance(props, null);
		session.setDebug(true);
		MimeMessage msg = new MimeMessage(session);
		msg.setFrom(new InternetAddress(from));
		msg.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
		ResourceBundle bundle = ResourceBundle.getBundle(RESOURCE_BUNDLE_PATH, email.getLanguage());
		String subject = "text.mail_subject." + email.getEmailSubject().toUpperCase().replace(" ", "_");
		msg.setSubject(bundle.getString(subject));
		StringBuilder sb = new StringBuilder();
		sb.append(bundle.getString(EMAIL_START_TEXT));
		sb.append(email.getSenderName()).append(" ").append(email.getSenderEmail()).append(" ")
				.append(email.getEmailText());
		msg.setText(sb.toString());
		return msg;

	}

	private static String getmailserverProperties(String property,RequestContext context) {

		String configParamValue = null;
		if (property != null && !"".equals(property)) {
			if (configParamsMap == null || configParamsMap.isEmpty()) {
				loadConfigparams(context);
			}
			configParamValue = configParamsMap.get(property);
			logger.debug("configParamValue:" + configParamValue);

		} 
		return configParamValue;

	}
	

	private static void loadConfigparams(RequestContext context) {
		logger.debug("in getmailserverProperty:");
		Statement st = null;
		ResultSet rs = null;
		Postgre objPostgre =  new Postgre(context);
		final String GET_OPTION_ID = "SELECT CONFIG_PARAM_CODE,CONFIG_PARAM_VALUE FROM CONFIG_PARAM";
		try {
			ContactUsExternal.connection = objPostgre.getConnection();
			st = ContactUsExternal.connection.createStatement();
			rs = st.executeQuery(GET_OPTION_ID);
			while (rs.next()) {
				String configParamCode = rs.getString("config_param_code");
				String configParamValue = rs.getString("config_param_value");
				logger.debug("configParamCode:" + configParamCode);
				logger.debug("configParamValue:" + configParamValue);
				configParamsMap.put(configParamCode, configParamValue);
			}

		} catch (SQLException e) {
			logger.error((Object) ("getConfiguration()" + e.getMessage()));
			e.printStackTrace();
		} finally {
			objPostgre.releaseConnection(ContactUsExternal.connection, st, rs);
		}
		objPostgre.releaseConnection(ContactUsExternal.connection, st, rs);

	}

}
