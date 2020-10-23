package com.hukoomi.contact.service.impl;

import java.util.Locale;

import com.hukoomi.contact.model.ContactEmail;
import com.hukoomi.contact.service.impl.ContactEmailSenderService;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;

import org.apache.log4j.Logger;
import org.springframework.context.MessageSource;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.validation.Validator;

public class ContactEmailSenderService {
	
	private static final String EMAIL_START_TEXT = "text.email_start";

	private static final Logger logger = Logger.getLogger(ContactEmailSenderService.class);

	private String mailTo;
	private String mailFrom;

	private Validator contactEmailValidator;

	private MessageSource messageSource;
	private MailSender mailSender;

	public void sendEmailToHukoomi(ContactEmail email, Locale locale) {

		/* ValidationUtils.validate(email, "email", contactEmailValidator); */

		SimpleMailMessage mailMessage = createMailMessage(email, locale);
		try {
			mailSender.send(mailMessage);
			
		}
		catch (MailException e) {
			logger.error(e);
			throw e;
		}
	}

	private SimpleMailMessage createMailMessage(ContactEmail email, Locale locale) {
		SimpleMailMessage mailMessage = new SimpleMailMessage();
		mailMessage.setFrom(mailFrom);
		mailMessage.setTo(mailTo);
		mailMessage.setSubject(messageSource.getMessage("text.mail_subject."
				+ email.getEmailSubject().toString(), null, locale));
		StringBuilder sb = new StringBuilder();
		sb.append(messageSource.getMessage(EMAIL_START_TEXT, null, locale));
		sb.append(email.getSenderName()).append(" ").append(email.getSenderEmail()).append(" ").append(
				email.getEmailText());
		mailMessage.setText(sb.toString());
		return mailMessage;

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
