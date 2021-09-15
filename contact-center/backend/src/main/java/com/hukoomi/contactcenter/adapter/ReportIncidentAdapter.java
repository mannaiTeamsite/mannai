package com.hukoomi.contactcenter.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hp.schemas.sm._7.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.xml.ws.BindingProvider;
import java.net.MalformedURLException;
import java.net.URL;

@Component
public class ReportIncidentAdapter {

	private static final Logger logger = LoggerFactory.getLogger(ReportIncidentAdapter.class);

	@Value("${soap.contact.center.service.url}")
	private String serviceUrl;

	@Value("${soap.contact.center.service.endpoint.url}")
	private String serviceEndpointUrl;

	@Value("${soap.contact.center.service.username}")
	private String serviceUserName;

	@Value("${soap.contact.center.service.password}")
	private String servicePassword;

	private ContactCenter getContactCenterService() throws MalformedURLException {
		logger.debug("Prepare contact center service instance -- " + serviceUrl);
		URL url = new URL(serviceUrl);
		ContactCenter_Service contactCenterService=new ContactCenter_Service(url);
		ContactCenter contactCenter=contactCenterService.getContactCenter();
		logger.debug("ContactCenter instance created -- ");

		((BindingProvider) contactCenter).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, serviceEndpointUrl);
		((BindingProvider) contactCenter).getRequestContext().put(BindingProvider.USERNAME_PROPERTY, serviceUserName);
		((BindingProvider) contactCenter).getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, servicePassword);
		logger.debug("ContactCenter context endpoint url & credentials updated --");
		return contactCenter;
	}

	public RetrieveContactCenterResponse retrieveContactCenter(RetrieveContactCenterRequest retrieveContactCenterRequest) throws Exception {
		ContactCenter contactCenter = getContactCenterService();
		logger.debug("Prepare retrieve contact center request --");

		logger.debug("Calling retrieve contact center service --");
		RetrieveContactCenterResponse retrieveContactCenterResponse = contactCenter.retrieveContactCenter(retrieveContactCenterRequest);

		try {
			logger.debug("Retrieve contact center response --");
			logger.debug(new ObjectMapper().writeValueAsString(retrieveContactCenterResponse));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		return retrieveContactCenterResponse;
	}

	public CreateContactCenterResponse createReportIncident(CreateContactCenterRequest createContactCenterRequest) throws Exception {
		try
		{
			ContactCenter contactCenter = getContactCenterService();
			logger.debug("Calling create contact center service --");
			return contactCenter.createContactCenter(createContactCenterRequest);
		} catch (Exception e){
			logger.error("Create contact center adapter exception -- "+e);
			throw e;
		}
	}
}
