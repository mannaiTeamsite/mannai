package com.hukoomi.utils;
final class Constants {
    private Constants() {
        throw new IllegalStateException("Utility class");
    }
    /** error message. */
    public static final String ERROR_SYSTEM = "system_error";
    /** success message. */
    public static final String SUCCESS = "success";
    /** false message. */
    public static final String FALSE = "false";
    /** blocked services for customer services. */
    public static final String BLOCKED_SERVICES = "53001,53002,53003,53004,53005";
    /** production end point for customer service wsdl. */
    public static final String PROD_END_POINT = "https://motcsm.mirqab.gov.qa:13080/SM/7/ContactCenter.wsdl";
    /** staging end point for customer service wsdl. */
    public static final String STAGING_END_POINT = "https://motcsm.mirqab.gov.qa:13080/SM/7/ContactCenter.wsdl";
}
