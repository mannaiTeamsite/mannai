package com.hukoomi.livesite.external;

import com.interwoven.livesite.runtime.RequestContext;

import com.hukoomi.contact.model.ContactEmail;

import com.hukoomi.contact.service.HukoomiEmailSenderService;

import com.hukoomi.utils.VerifyRecaptcha;
/*import org.springframework.mail.MailException;*/

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
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

public class ContactUsExternal {
	
	private HukoomiEmailSenderService emailSenderService;

	private static final Logger logger = Logger.getLogger(ContactUsExternal.class);

	private static final String EMAIL_ATTRIBUTE = "email";
	private static final String EMAIL_SENT_SUCCESSFULLY = "emailSent";
	private static final String EMAIL_SUBJECT_LIST_ATTRIBUTE = "subjectList";
	private static final String RECAPTCHA_RESPONSE_PARAMETER ="g-recaptcha-response";
	
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
		 		 
		 		logger.debug("gRecaptchaResponse::::"+gRecaptchaResponse);
		 		verify = VerifyRecaptcha.verify(gRecaptchaResponse);
		 		verify = true;
				if (verify) {
					//emailSenderService.sendEmailToHukoomi(email, (getLocaleLanguage(language)));
					String from = "noreply@hukoomi.qa";
					String to = "thavamani.sakthivel@gfi.in";
					Properties props = new Properties();
					props.put("mail.smtp.host", "172.16.162.200");
					props.put("mail.smtp.starttls.enable", "true");
					props.put("mail.smtp.port", "25");
					Session session = Session.getDefaultInstance(props, null);
					session.setDebug(true);
					MimeMessage msg = new MimeMessage(session);
					msg.setFrom(new InternetAddress(from));
					msg.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
					msg.setSubject("test"); 
					msg.setText("Hi, This mail is to inform you...");  
					Transport.send(msg);
					/*
					 * status.setComplete(); model.addAttribute(EMAIL_ATTRIBUTE, new
					 * ContactHukoomiEmail()); model.addAttribute(EMAIL_SENT_SUCCESSFULLY,
					 * Boolean.TRUE);
					 */
					status = "Successfully Sent";					
				}else{
					/* result.reject("error.captcha.verification"); 
					fatalError = true;*/
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
				ResourceBundle bundle = ResourceBundle.getBundle("com.hukoomi.utils.contactUs", lang);
				String validationMessage = bundle.getString("text.mail_subject");
				Element resultElement = document.addElement("Result");
				String[] arrOfStr = validationMessage.split(",", 2);
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
		

}
