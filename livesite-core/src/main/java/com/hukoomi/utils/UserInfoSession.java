package com.hukoomi.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;

import com.hukoomi.livesite.external.DashboardExternal;
import com.hukoomi.livesite.external.DashboardSettingsExternal;
import com.interwoven.livesite.runtime.RequestContext;

public class UserInfoSession {
	private String strValid = "valid";
	private String strStatus = "status";
	private String strFnEn = "fnEn";
	private String strUid = "uid";
	private String strFnAr = "fnAr";
	private String strLnEn = "lnEn";
	private String strLnAr = "lnAr";
	private String strQID = "QID";
	private String strEID = "EID";
	private String strMobile = "mobile";
	private String strEmail = "email";
	private String strLstMdfy = "lstMdfy";
	private String strRole = "role";
	private String strExp = "exp";
	private String strUserId = "userId";

	private static final Logger LOGGER = Logger.getLogger(UserInfoSession.class);

	public Document getUserData(RequestContext context, Document doc) {
		try {
			HttpServletRequest request = context.getRequest();
			String valid = getStatus(context);
			if (valid.equalsIgnoreCase(strValid)) {
				Element root = doc.getRootElement();
				if (root != null && root.isRootElement()) {
					Element userData = root.addElement("userData");

					Element statusElement = userData.addElement(strStatus);
					statusElement.setText(request.getSession().getAttribute(strStatus).toString());
					Element uidElement = userData.addElement(strUid);
					uidElement.setText(request.getSession().getAttribute(strUid).toString());
					Element fnEnElement = userData.addElement(strFnEn);
					fnEnElement.setText(request.getSession().getAttribute(strFnEn).toString());
					Element fnArElement = userData.addElement(strFnAr);
					fnArElement.setText(request.getSession().getAttribute(strFnAr).toString());
					Element lnEnElement = userData.addElement(strLnEn);
					lnEnElement.setText(request.getSession().getAttribute(strLnEn).toString());
					Element lnArElement = userData.addElement(strLnAr);
					lnArElement.setText(request.getSession().getAttribute(strLnAr).toString());
					Element qIdElement = userData.addElement(strQID);
					qIdElement.setText(request.getSession().getAttribute(strQID).toString());
					Element eIdElement = userData.addElement(strEID);
					eIdElement.setText(request.getSession().getAttribute(strEID).toString());
					Element mobileElement = userData.addElement(strMobile);
					mobileElement.setText(request.getSession().getAttribute(strMobile).toString());
					Element emailElement = userData.addElement(strEmail);
					emailElement.setText(request.getSession().getAttribute(strEmail).toString());
					Element lstMdfyElement = userData.addElement(strLstMdfy);
					lstMdfyElement.setText(request.getSession().getAttribute(strLstMdfy).toString());
					Element roleElement = userData.addElement(strRole);
					roleElement.setText(request.getSession().getAttribute(strRole).toString());

				}
			}
		} catch (Exception e) {
			LOGGER.error("Exception", e);
		}
		return doc;
	}

	public String getCookie(RequestContext context, String value) {
		String cookieStr = "";
		HttpServletRequest request = context.getRequest();
		Cookie cookie = null;
		Cookie[] cookies = null;
		cookies = request.getCookies();
		if (cookies != null) {
			for (int i = 0; i < cookies.length; i++) {
				cookie = cookies[i];
				if (cookie.getName().equals(value)) {
					cookieStr = cookie.getValue();
				}
			}
		}
		return cookieStr;
	}

	public void checkStatus(RequestContext context, String token) {
		HttpServletRequest request = context.getRequest();
		DashboardExternal dashboard = new DashboardExternal();
		String status = (String) request.getSession().getAttribute(strStatus);
		if (!status.equalsIgnoreCase(strValid)) {
			LOGGER.info("--------dashboardServices is called--------");
			dashboard.dashboardServices(context, token);
			status = (String) request.getSession().getAttribute(strStatus);
			if (status != null && status.equalsIgnoreCase(strValid)) {
				setPersona(context, (String) request.getSession().getAttribute(strUserId));
			}
			LOGGER.info("Status:" + status);

		}
	}

	public String getStatus(RequestContext context) {
		String valid = "";
		HttpServletRequest request = context.getRequest();
		String accessToken = null;

		Date expiryDate = null;
		SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
		accessToken = getCookie(context, "accessToken");
		if (accessToken != null) {
			DashboardExternal dashboard = new DashboardExternal();
			String status = (String) request.getSession().getAttribute(strStatus);
			LOGGER.info("Status:" + status);
			checkStatus(context, accessToken);
			Date cureentDate = new Date(System.currentTimeMillis());
			LOGGER.info("Current Date:" + cureentDate);

			String expDt = (String) request.getSession().getAttribute(strExp);
			if (expDt != null) {
				try {
					expiryDate = formatter.parse(expDt);
				} catch (ParseException e) {
					LOGGER.error("exception in parsing string to date : ", e);
				}

				LOGGER.info("Expiry Date:" + expiryDate);
			}

			if (status != null && status.equalsIgnoreCase(strValid) && expDt != null
					&& cureentDate.compareTo(expiryDate) < 0) {

				valid = strValid;
				return valid;

			} else if (expDt != null && cureentDate.compareTo(expiryDate) < 0) {

				dashboard.removeSessionAttr(context);
			}
		}
		return valid;
	}

	private void setPersona(RequestContext context, String userId) {
		LOGGER.info("Set persona called");
		HttpServletResponse response = context.getResponse();
		HttpServletRequest request = context.getRequest();
		Postgre postgre = new Postgre(context);

		DashboardSettingsExternal dse = new DashboardSettingsExternal();
		LOGGER.info("userId value:" + userId);
		String persona = dse.getPersonaForUser(userId, postgre);
		LOGGER.info("Persona value:" + persona);
		if (persona != null) {

			Cookie cookie = getCookie(request, "persona");

			if (cookie != null) {
				cookie.setMaxAge(0);
				response.addCookie(cookie);
			}
		}
		Cookie personaCookie = new Cookie("persona", persona);
		personaCookie.setMaxAge(5 * 24 * 60 * 60 * 1000);
		personaCookie.setValue(persona);
		personaCookie.setPath("/");
		response.addCookie(personaCookie);

		LOGGER.info("Set persona ended");
	}

	public static Cookie getCookie(HttpServletRequest request, String name) {
		if (request.getCookies() != null) {
			for (Cookie cookie : request.getCookies()) {
				if (cookie.getName().equals(name)) {
					return cookie;
				}
			}
		}

		return null;
	}

}
