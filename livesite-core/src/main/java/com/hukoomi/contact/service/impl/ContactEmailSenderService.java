package com.hukoomi.contact.service.impl;

import java.util.Date;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.activation.DataHandler;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import com.hukoomi.contact.model.ContactEmail;
import com.hukoomi.contact.service.HukoomiEmailSenderService;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;

import org.apache.log4j.Logger;
import org.springframework.context.MessageSource;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.validation.Validator;

public class ContactEmailSenderService implements HukoomiEmailSenderService {
	
	private static final String EMAIL_START_TEXT = "text.email_start";

	private static final Logger logger = Logger.getLogger(ContactEmailSenderService.class);

	private String mailTo;
	private String mailFrom;

	private Validator contactEmailValidator;

	private MessageSource messageSource;
	private MailSender mailSender;

	public void sendEmailToHukoomi(ContactEmail email, Locale locale) {



		MimeMessage mailMessage;
		try {
			mailMessage = createMailMessage(email, locale);
			Transport.send(mailMessage);
		} catch (AddressException e1) {

			e1.printStackTrace();
		} catch (MessagingException e1) {

			e1.printStackTrace();
		}
		catch (MailException e) {
			logger.error(e);
			throw e;
		}
	}

	private MimeMessage createMailMessage(ContactEmail email, Locale locale) throws  MessagingException {

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
		ResourceBundle bundle = ResourceBundle.getBundle("com.hukoomi.utils.ContactUs", email.getLanguage());
		
		msg.setSubject(bundle.getString("text.mail_subject."+ email.getEmailSubject()));
		StringBuilder sb = new StringBuilder();
		sb.append(bundle.getString(EMAIL_START_TEXT));
		sb.append(email.getSenderName()).append(" ").append(email.getSenderEmail()).append(" ").append(
				email.getEmailText());
		msg.setText(sb.toString());  
		
//		email.getEmailSubject();
//		mailMessage.setFrom(mailFrom);
//		mailMessage.setTo(mailTo);
//		mailMessage.setSubject(messageSource.getMessage("text.mail_subject."				+ email.getEmailSubject().toString(), null, locale));
//		StringBuilder sb = new StringBuilder();
//		sb.append(messageSource.getMessage(EMAIL_START_TEXT, null, locale));
//		sb.append(email.getSenderName()).append(" ").append(email.getSenderEmail()).append(" ").append(
//				email.getEmailText());
//		mailMessage.setText(sb.toString());
		return msg;

	}

	public void setMailTo(String mailTo) {
		this.mailTo = mailTo;
	}

	public void setMailFrom(String mailFrom) {
		this.mailFrom = mailFrom;
	}

	public void setMessageSource(MessageSource messageSource) {
		this.messageSource = messageSource;
	}

	public void setMailSender(MailSender mailSender) {
		this.mailSender = mailSender;
	}

	public void setContactEmailValidator(Validator contactEmailValidator) {
		this.contactEmailValidator = contactEmailValidator;
	}

}
