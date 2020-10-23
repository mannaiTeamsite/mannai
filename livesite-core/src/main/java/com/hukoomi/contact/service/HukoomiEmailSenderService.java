package com.hukoomi.contact.service;

import java.util.Locale;

import com.hukoomi.contact.model.ContactHukoomiEmail;

public interface HukoomiEmailSenderService {
	
	void sendEmailToHukoomi(ContactHukoomiEmail email, Locale locale);

}
