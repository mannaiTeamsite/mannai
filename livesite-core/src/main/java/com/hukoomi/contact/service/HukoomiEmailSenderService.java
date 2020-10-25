package com.hukoomi.contact.service;

import java.util.Locale;

import com.hukoomi.contact.model.ContactEmail;

public interface HukoomiEmailSenderService {
	
	void sendEmailToHukoomi(ContactEmail email, Locale locale);

}
