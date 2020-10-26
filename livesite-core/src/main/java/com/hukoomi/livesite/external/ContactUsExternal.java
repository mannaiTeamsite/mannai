package com.hukoomi.livesite.external;

import com.interwoven.livesite.runtime.RequestContext;

import com.hukoomi.contact.model.ContactEmail;
import com.hukoomi.utils.Postgre;
import com.hukoomi.utils.VerifyRecaptcha;
/*import org.springframework.mail.MailException;*/

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

/*import javax.xml.bind.ValidationException;*/

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.mail.MailException;

public class ContactUsExternal {
	
	//private ContactEmailSenderService emailSenderService;

	private static final Logger logger = Logger.getLogger(ContactUsExternal.class);

	private static final String EMAIL_ATTRIBUTE = "email";
	private static final String EMAIL_SENT_SUCCESSFULLY = "emailSent";
	private static final String EMAIL_SUBJECT_LIST_ATTRIBUTE = "subjectList";
	private static final String RECAPTCHA_RESPONSE_PARAMETER ="captcha";
	private static final String EMAIL_START_TEXT = "text.email_start";
	private static final String CONFIG_CODE_HUKOOMI_CONTACT_TO_MAIL = "Hukoomi_Contact_To_Mail";
	private static final String CONFIG_CODE_HUKOOMI_CONTACT_FROM_MAIL = "Hukoomi_Contact_From_Mail";
	private static final String CONFIG_CODE_HUKOOMI_CONTACT_MAIL_HOST = "Hukoomi_Contact_Mail_Host";
	private static final String CONFIG_CODE_HUKOOMI_CONTACT_MAIL_PORT = "Hukoomi_Contact_Mail_Port";
	private static final String CONFIG_CODE_HUKOOMI_CONTACT_MAIL_AUTH = "Hukoomi_Contact_Mail_Auth";
	private static Connection connection;
	static {
		ContactUsExternal.connection = null;
			}
	private static HashMap<String, String> configParamsMap = new HashMap<String,String>();
		public Document sendEmail(RequestContext context)
		{
			Document document = DocumentHelper.createDocument() ;
			boolean verify = false;
		 	boolean fatalError = false;
		 	String senderName = "";
		 	String senderEmail = "";
		 	String emailSubject = "";
		 	String emailText = "";
		 	String action = "";
		 	String gRecaptchaResponse="";
		 	ContactEmail email = new ContactEmail();
		 	String status="";
		 	String responseStr="";
		 	String language ="";
		 	action = context.getParameterString("action");
		 	logger.debug("action:"+ action);
		 	if(action.equals("sendmail")) {
		 		logger.debug("Sendingemail.");
		 	try{
		 		if (logger.isDebugEnabled()) {
					logger.debug("Sending email.");
				}
		 		 senderName = context.getParameterString("senderName");
			 	 senderEmail = context.getParameterString("senderEmail");
			 	 emailSubject = context.getParameterString("emailSubject");
			 	 emailText = context.getParameterString("emailText");
			 	language = context.getParameterString("language");
			 	logger.debug("senderName:"+senderName);
			 	logger.debug("senderEmail:"+senderEmail);
			 	logger.debug("emailText:"+emailText);
			 	logger.debug("emailSubject:"+emailSubject);
			 	logger.debug("language:"+language);
		 		 gRecaptchaResponse = context.getParameterString(RECAPTCHA_RESPONSE_PARAMETER);
		 		 if(!senderName.equals("")) {
		 			 email.setSenderName(senderName);
		 		 }
		 		if(!senderEmail.equals("")) {
		 			 email.setSenderEmail(senderEmail);
		 		 }
		 		if(!emailText.equals("")) {
		 			 email.setEmailText(emailText);
		 		 }
		 		if(!emailSubject.equals("")) {
		 			 email.setEmailSubject(emailSubject);
		 		 }
		 		if(!language.equals("")) {
		 			 email.setLanguage(getLocaleLanguage(language) );
		 		 }
		 		 
		 		logger.debug("gRecaptchaResponse::::"+gRecaptchaResponse);
		 		verify = VerifyRecaptcha.verify(gRecaptchaResponse);
				if (verify) {
					status = sendEmailToHukoomi(email, (getLocaleLanguage(language)));
					//status = "Successfully Sent";					
				}else{
					status = "Error Captcha Verification";					
					
				}

				if (logger.isDebugEnabled()) {
					logger.debug("Email sent successfully.");
				}
				
				responseStr = getDocument(status);
				document = DocumentHelper.parseText(responseStr);
			}catch (IOException re) {
				logger.error(
						"There was an error during recaptcha verification", re);
				
			}catch (Exception e) {
				if (logger.isDebugEnabled()) {
					logger.error("There was an error during Contact.", e);
				}
				
			}}
		 	else if(action.equals("inquiryTypes")) {
		 		Locale lang = getLocaleLanguage(language);
				ResourceBundle bundle = ResourceBundle.getBundle("com.hukoomi.resources.ContactUs", lang);
				String validationMessage = bundle.getString("text.mail_subject");
				Element resultElement = document.addElement("Result");
				String[] arrOfStr = validationMessage.split(",", 5);
				for(int i=0;i<arrOfStr.length;i++) {
					String value = arrOfStr[i].split("!")[0];
					String text = arrOfStr[i].split("!")[1];
					Element optionElement = resultElement.addElement("Option");
					Element valueele = optionElement.addElement("value");
					valueele.setText(value);
					Element textele = optionElement.addElement("text");
					if(language.equals("ar")) {
						textele.setText(decodeToArabicString(text));
					}
					else {
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
		private String  getDocument(String status) throws JSONException {
			String responseStr ="" ; 
			String str1="<Result><status>";
			String str2 = "</status></Result>";			
			responseStr = str1 + status + str2 ;
			return responseStr;
		}
		
		public Locale getLocaleLanguage(String language){
			switch (language) {
			case "en":
				return Locale.ENGLISH;
			case "ar":
				return new Locale("ar");
			default:
				return Locale.ENGLISH;
			}
		}
		public String sendEmailToHukoomi(ContactEmail email, Locale locale) {

			ResourceBundle bundle = ResourceBundle.getBundle("com.hukoomi.resources.ContactUs", email.getLanguage());
			
			MimeMessage mailMessage;
			try {
				mailMessage = createMailMessage(email, locale);
				Transport.send(mailMessage);
			} catch (AddressException e1) {
				
				e1.printStackTrace();
			} catch (MessagingException e1) {
				return bundle.getString("");
				e1.printStackTrace();
			}
			catch (MailException e) {
				msg.setSubject(bundle.getString(subject));
				logger.error(e);
				throw e;
			}
		}

		private MimeMessage createMailMessage(ContactEmail email, Locale locale) throws  MessagingException {

			String from = getmailserverProperties(CONFIG_CODE_HUKOOMI_CONTACT_FROM_MAIL);
			String to = getmailserverProperties(CONFIG_CODE_HUKOOMI_CONTACT_TO_MAIL);
			Properties props = new Properties();
			props.put("mail.smtp.host", getmailserverProperties(CONFIG_CODE_HUKOOMI_CONTACT_MAIL_HOST));
			props.put("mail.smtp.starttls.enable", "false");
			props.put("mail.smtp.port", getmailserverProperties(CONFIG_CODE_HUKOOMI_CONTACT_MAIL_PORT));
			Session session = Session.getDefaultInstance(props, null);
			session.setDebug(true);
			MimeMessage msg = new MimeMessage(session);
			msg.setFrom(new InternetAddress(from));
			msg.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
			ResourceBundle bundle = ResourceBundle.getBundle("com.hukoomi.resources.ContactUs", email.getLanguage());
			String subject= "text.mail_subject."+ email.getEmailSubject().toUpperCase().replace(" ", "_");
			msg.setSubject(bundle.getString(subject));
			StringBuilder sb = new StringBuilder();
			sb.append(bundle.getString(EMAIL_START_TEXT));
			sb.append(email.getSenderName()).append(" ").append(email.getSenderEmail()).append(" ").append(
					email.getEmailText());
			msg.setText(sb.toString());  
			return msg;

		}
		
		private static String getmailserverProperties(String property) {
			
			String configParamValue = null;
			if (property != null && !"".equals(property)) {
				if (configParamsMap == null || configParamsMap.isEmpty()) {
					loadConfigparams();
				}
				configParamValue = configParamsMap.get(property);
				logger.debug("configParamValue:"+configParamValue);

			} else {
				throw new RuntimeException("Error while reading config parameter");
			}
			return configParamValue;
			
		}
		private static void loadConfigparams() {
			logger.debug("in getmailserverProperty:");
			Statement st = null;
			ResultSet rs = null;
			
			final String GET_OPTION_ID = "SELECT CONFIG_PARAM_CODE,CONFIG_PARAM_VALUE FROM CONFIG_PARAM";
			try {
				ContactUsExternal.connection = Postgre.getConnection();
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
				Postgre.releaseConnection(ContactUsExternal.connection, st, rs);
			}
			Postgre.releaseConnection(ContactUsExternal.connection, st, rs);
			
		}

}
