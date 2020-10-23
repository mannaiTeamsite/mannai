package com.hukoomi.utils;

public class Constants 
{
	public static final String APP_MESSAGE_BUNDLE = "qa.gov.ict.incidentReporting.nl.ReportIncidentPortletResource";
//	public static final String RESOURCE_BUNDLE_KEYS = "qa.gov.ict.incidentReporting.nl.ReportIncidentPortletResource";
	public static final String RESOURCE_BUNDLE_KEYS = "qa.gov.ict.incidentReporting.nl.ResourceBundle";
	public static final String DELIM_COMMA = "[,]";
	public static final String DELIM_PIPE = "[!]";
	public static final String ESERVICE_LIST= "eserviceList";
	public static final String SERVICE_LIST= "serviceList";

	public static final String RECAPTCHA_CHALLENGE_PARAMETER = "recaptcha_challenge_field";
	public static final String RECAPTCHA_RESPONSE_PARAMETER ="recaptcha_response_field";
	public static final String RECAPTCHA_PRIVATE_KEY = "6Lf_PcgSAAAAAAeJpUpbQ3CI24adFRIM0A9geVlG";
	
	public static final String ERROR_SYSTEM = "system_error";
	
	public static final String ORIGIN="WebPortal";
	public static final boolean EXTERNAL=true;
	public static final String CATEGORY="incident";
	public static final String SERVICE_CATEGORY="HUKOOMI";
	public static final String ADD="add";
	public static final String ESERVICE="eService=";
	public static final String SERVICE="Service=";
	public static final String NATIONALITY="Nationality=";
	public static final String DATETIME_FORMATTER="dd-MMM-yyyy hh:mm:ss";
	
	public static final String ALLOWED_FILE_TYPES = "allowedFileTypes";
	public static final String ATTACHMENT_NOT_ALLOWED_TYPE_ERROR = "file_type_validation";
	public static final int BYTE_FIELD_SIZE = 1;
	public static final int ATTACHMENT_FILE_SIZE_MAX = 2;
	public static final String ERROR_INVALID_SIZE = "file_size_validation";
	public static final String SUCCESS	= "success";
	public static final String FALSE	= "false";
	
	public static final String PNG	= "image/png";
	public static final String PDF	= "application/pdf";
	public static final String DOCX = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
	public static final String DOC	= "application/msword";
	public static final String GIF  = "image/gif";
	public static final String JPEG	= "image/jpeg";
	public static final String JPG	= "image/jpg";
	
	public static final String APPKEY= "appKey";
	
	public static final String BLOCKED_SERVICES= "53001,53002,53003,53004,53005";
	public static final String PROD_END_POINT	 = "https://motcsm.mirqab.gov.qa:13080/SM/7/ContactCenter.wsdl";
	public static final String STAGING_END_POINT = "https://motcsm.mirqab.gov.qa:13080/SM/7/ContactCenter.wsdl";
	
	public static final String INVALID_INPUT = "javax.faces.component.UIInput.CONVERSION";
	public static final String REQUIRED_INPUT = "javax.faces.component.UIInput.REQUIRED";
	public static final String EMAILVALIDATION = "EmailValidation";
}
