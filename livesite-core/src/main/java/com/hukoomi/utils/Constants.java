package com.hukoomi.utils;

class Constants 
{
	private Constants() {
	    throw new IllegalStateException("Utility class");
	  }
	public static final String ERROR_SYSTEM = "system_error";	
	public static final String SUCCESS	= "success";
	public static final String FALSE	= "false";
	public static final String BLOCKED_SERVICES= "53001,53002,53003,53004,53005";
	public static final String PROD_END_POINT	 = "https://motcsm.mirqab.gov.qa:13080/SM/7/ContactCenter.wsdl";
	public static final String STAGING_END_POINT = "https://motcsm.mirqab.gov.qa:13080/SM/7/ContactCenter.wsdl";
}
