package com.hukoomi.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;

import com.hukoomi.livesite.external.DashboardExternal;
import com.interwoven.livesite.runtime.RequestContext;

public class UserInfoSession {
	
	 private static final Logger LOGGER = Logger.getLogger(UserInfoSession.class);
	public Document getUserData(RequestContext context, Document doc) {
	  	HttpServletRequest request = context.getRequest();
	  	String accessToken = null;
		Cookie cookie = null;
		Cookie[] cookies = null;
		Date expiryDate = null ;
		SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");  
		cookies = request.getCookies();
		if (cookies != null) {
			for (int i = 0; i < cookies.length; i++) {
				cookie = cookies[i];
				if (cookie.getName().equals("accessToken")) {
					accessToken = cookie.getValue();
				}
			}
		}
		if (accessToken != null) {	
			DashboardExternal dashboard = new DashboardExternal();
			String status = (String) request.getSession().getAttribute("status");
			LOGGER.info("Status:"+status);		
			if(status != "valid") {			
					LOGGER.info("--------dashboardServices is called--------");					
					dashboard.dashboardServices(context, accessToken);	
					status = (String) request.getSession().getAttribute("status");
					LOGGER.info("Status:"+status);			
			}
			Date cureentDate = new Date(System.currentTimeMillis());
			LOGGER.info("Current Date:"+cureentDate);
			
			String expDt = (String) request.getSession().getAttribute("exp");
			if(expDt != null) {
			try {
				expiryDate=formatter.parse(expDt);
			} catch (ParseException e) {
				LOGGER.debug("exception in parsing string to date : " + e);
			}  
			
			LOGGER.info("Expiry Date:"+expiryDate);
			}
			
			if(status != null && status.equalsIgnoreCase("valid"))	{	
				if (expDt != null && cureentDate.compareTo(expiryDate) < 0 ) {
					Element root = doc.getRootElement();
					if(root != null && root.isRootElement()) {
					Element userData = root.addElement("userData");

					Element statusElement = userData.addElement("status");
					statusElement.setText(request.getSession().getAttribute("status").toString());
					Element uidElement = userData.addElement("uid");
					uidElement.setText(request.getSession().getAttribute("uid").toString());
					Element fnEnElement = userData.addElement("fnEn");
					fnEnElement.setText(request.getSession().getAttribute("fnEn").toString());
					Element fnArElement = userData.addElement("fnAr");
					fnArElement.setText(request.getSession().getAttribute("fnAr").toString());
					Element lnEnElement = userData.addElement("lnEn");
					lnEnElement.setText(request.getSession().getAttribute("lnEn").toString());
					Element lnArElement = userData.addElement("lnAr");
					lnArElement.setText(request.getSession().getAttribute("lnAr").toString());
					Element qIdElement = userData.addElement("QID");
					qIdElement.setText(request.getSession().getAttribute("QID").toString());
					Element eIdElement = userData.addElement("EID");
					eIdElement.setText(request.getSession().getAttribute("EID").toString());
					Element mobileElement = userData.addElement("mobile");
					mobileElement.setText(request.getSession().getAttribute("mobile").toString());
					Element emailElement = userData.addElement("email");
					emailElement.setText(request.getSession().getAttribute("email").toString());
					Element lstMdfyElement = userData.addElement("lstMdfy");
					lstMdfyElement.setText(request.getSession().getAttribute("lstMdfy").toString());
					Element roleElement = userData.addElement("role");
					roleElement.setText(request.getSession().getAttribute("role").toString());
					
				}
				}
				else {

					dashboard.removeSessionAttr(context);
		}
			
		}				
		}
		return doc;
	}

}
