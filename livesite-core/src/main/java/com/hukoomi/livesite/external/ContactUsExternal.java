package com.hukoomi.livesite.external;

import com.interwoven.livesite.runtime.RequestContext;

import com.hukoomi.contact.model.ContactEmail;

import com.hukoomi.contact.service.HukoomiEmailSenderService;

import com.hukoomi.utils.VerifyRecaptcha;
/*import org.springframework.mail.MailException;*/

import java.io.IOException;
import java.util.Locale;

/*import javax.xml.bind.ValidationException;*/

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
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
		 	String gRecaptchaResponse="";
		 	ContactEmail email = new ContactEmail();
		 	String status="";
		 	String responseStr="";
		 	try{
		 		if (logger.isDebugEnabled()) {
					logger.debug("Sending email.");
				}
		 		 senderName = context.getParameterString("senderName");
			 	 senderEmail = context.getParameterString("senderEmail");
			 	 emailSubject = context.getParameterString("emailSubject");
			 	 emailText = context.getParameterString("emailText");
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

				if (verify) {
					emailSenderService.sendEmailToHukoomi(email, new Locale(getLocaleLanguage()));
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
				document = DocumentHelper.parseText(status);
			}catch (IOException re) {
				logger.error(
						"There was an error during recaptcha verification", re);
				
			}catch (Exception e) {
				if (logger.isDebugEnabled()) {
					logger.error("There was an error during Contact.", e);
				}
				
			}
		 	
		 	return document;
			
		}	
		
		private String  getDocument(String status) throws JSONException {
			String responseStr ="" ; 
			String str1="<Result><status>";
			String str2 = "</status</Result>>";			
			responseStr = str1 + status + str2 ;
			return responseStr;
		}
		public String getLocaleLanguage(){
			String lang = "en";
			if(LocaleContextHolder.getLocale().toString().equals("ar")){
				lang = "ar";
			}
			return lang;
		}

}
