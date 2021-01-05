package com.hukoomi.utils;

import com.interwoven.livesite.runtime.RequestContext;
import org.apache.log4j.Logger;

import javax.servlet.http.Cookie;

public class RequestHeaderUtils {
    private final Logger logger = Logger.getLogger(RequestHeaderUtils.class);
    private RequestContext context;
    private static final String X_FORWARDED_FOR = "x-forwarded-for";
    private static final String X_FORWARDED_HOST = "x-forwarded-host";

    public RequestHeaderUtils(RequestContext context) {
         this.context = context;
    }

    public String getCookie(String cookieName){
        logger.debug("getCookie()====> Starts");
        Cookie[] cookies = context.getRequest().getCookies();
        String personCookie = "";
        if(cookies != null){
            for(Cookie cookie: cookies){
                if(cookieName.equals(cookie.getName())){
                    personCookie = cookie.getValue();
                    break;
                }
            }
        }
        logger.debug("getCookie()====> ends");
        return personCookie;
    }

    public String getClientIpAddress(){
        logger.debug("getClientIpAddress()====> Starts");
        String clientIpAddress = context.getRequest().getHeader(X_FORWARDED_FOR);
        if(clientIpAddress != null && !clientIpAddress.isBlank()){
            clientIpAddress =  clientIpAddress.split(",")[0];
        }
        logger.debug("getClientIpAddress()====> ends");
        return clientIpAddress;
    }

    public String getForwardedHost(){
        logger.debug("getForwardedHost()====> Start");
        String hostname = "hukoomi.gov.qa";
        if(context.getRequest().getHeader(X_FORWARDED_HOST) != null && !context.getRequest().getHeader(X_FORWARDED_HOST).isBlank()) {
            hostname = context.getRequest().getHeader(X_FORWARDED_HOST);
        }
        logger.debug("getForwardedHost()====> End");
        return hostname;
    }

}
