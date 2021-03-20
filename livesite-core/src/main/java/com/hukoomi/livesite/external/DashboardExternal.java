package com.hukoomi.livesite.external;

import java.io.IOException;
import java.util.Properties;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.json.JSONObject;

import com.hukoomi.utils.JWTTokenUtil;
import com.hukoomi.utils.PropertiesFileReader;
import com.interwoven.livesite.runtime.RequestContext;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;

public class DashboardExternal {
	private Properties properties = null;
	/** Logger object to check the flow of the code. */
	private static final Logger LOGGER = Logger.getLogger(DashboardExternal.class);

	public void dashboardServices(RequestContext context, String accessToken) {
		String jwtParsedToken = null;

		HttpServletRequest request = context.getRequest();
		JWTTokenUtil jwt = new JWTTokenUtil(context);
		if (accessToken != null) {
			try {
				jwtParsedToken = jwt.parseJwt(accessToken);
				setSessionAttributes(jwtParsedToken, request, "valid");
				Cookie[] cookies = request.getCookies();
				for (int i = 0; i < cookies.length; i++) {
					String name = cookies[i].getName();
					if (name.equals(accessToken)) {
						cookies[i].setValue("");
						cookies[i].setPath("/");
						cookies[i].setMaxAge(0);

					}
				}
			} catch (ExpiredJwtException e) {
				LOGGER.debug("Token Expired");
				setSessionAttributes(jwtParsedToken, request, "Token Expired");
			} catch (SignatureException e) {
				LOGGER.debug("Signature Exception");
				setSessionAttributes(jwtParsedToken, request, "Signature Exception");
			} catch (Exception e) {
				LOGGER.debug("Some other exception in JWT parsing" + e);
				setSessionAttributes(jwtParsedToken, request, "Some other exception in JWT parsing");
			}
		}

	}

	private void setSessionAttributes(String jwtParsedToken, HttpServletRequest request, String status) {

		LOGGER.info("--------------getDocument is called------------");
		HttpSession session = request.getSession(true);
		session.setAttribute("status", status);
		session.setAttribute("unm", getValue(jwtParsedToken, "unm"));
		session.setAttribute("uid", getValue(jwtParsedToken, "uid"));
		session.setAttribute("fnEn", getValue(jwtParsedToken, "fnEn"));
		session.setAttribute("fnAr", getValue(jwtParsedToken, "fnAr"));
		session.setAttribute("lnEn", getValue(jwtParsedToken, "lnEn"));
		session.setAttribute("lnAr", getValue(jwtParsedToken, "lnAr"));
		session.setAttribute("QID", getValue(jwtParsedToken, "QID"));
		session.setAttribute("EID", getValue(jwtParsedToken, "EID"));
		session.setAttribute("mobile", getValue(jwtParsedToken, "mobile"));
		session.setAttribute("email", getValue(jwtParsedToken, "email"));
		session.setAttribute("lstMdfy", getValue(jwtParsedToken, "lstMdfy"));
		session.setAttribute("role", getValue(jwtParsedToken, "role"));

		LOGGER.info("--------------getDocument is Ended------------");
	}

	private static String getValue(final String response, final String key) {
		String status = "";
		if (!response.equals("")) {
			JSONObject jsonObj = new JSONObject(response);
			if (!jsonObj.isNull(key) && !jsonObj.get(key).equals(null)) {
				status = (String) jsonObj.get(key);
			}
		}
		return status;
	}

	public DashboardExternal(RequestContext context) {
		LOGGER.info("JWTTokenUtil : Loading Properties....");
		properties = DashboardExternal.loadProperties(context);
		LOGGER.info("Postgre : Properties Loaded");
	}

	private static Properties loadProperties(final RequestContext context) {

		PropertiesFileReader prop = null;
		prop = new PropertiesFileReader(context, "dashboard.properties");
		return prop.getPropertiesFile();

	}

	public void doLogout(RequestContext context) throws IOException {

		String url = properties.getProperty("logout");
		LOGGER.info("Logout URL"+url);
		// invalidate the session if exists
		HttpServletRequest request = context.getRequest();
		HttpSession session = request.getSession(false);
		if (session != null) {
			session.removeAttribute("status");
			session.removeAttribute("unm");
			session.removeAttribute("uid");
			session.removeAttribute("fnEn");
			session.removeAttribute("fnAr");
			session.removeAttribute("lnEn");
			session.removeAttribute("lnAr");
			session.removeAttribute("QID");
			session.removeAttribute("EID");
			session.removeAttribute("mobile");
			session.removeAttribute("email");
			session.removeAttribute("lstMdfy");
			session.removeAttribute("role");
		}
		Cookie[] cookies = request.getCookies();
		for (int i = 0; i < cookies.length; i++) {
			String name = cookies[i].getName();
			if (name.equals("accessToken")) {
				cookies[i].setMaxAge(0);
				cookies[i].setPath(request.getRequestURI());
			}
		}

		url += "?relayURL=" + request.getRequestURL();
		LOGGER.info("Logout URL"+url);
		HttpServletResponse response = context.getResponse();
		response.sendRedirect(url);

	}
	public Document wecomedata(RequestContext conetxt) {
		Document doc = DocumentHelper.createDocument();
		GetUserData(conetxt, doc);		
		return doc;
		
	}
	public Document GetUserData(RequestContext context , Document doc) {
		
		 Element root = doc.getRootElement();
		 HttpServletRequest request = context.getRequest();
			HttpSession session = request.getSession(false);
			LOGGER.info("Session:" + session);
			LOGGER.info("Status : " + request.getSession().getAttribute("status"));
			String status = (String) request.getSession().getAttribute("status");
			String accessToken = null;
			Cookie cookie = null;
			Cookie[] cookies = null;
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
				if (!status.equalsIgnoreCase("valid")) {
					LOGGER.info("--------Dashboard External is called--------");
					DashboardExternal dash = new DashboardExternal(context);
					dash.dashboardServices(context, accessToken);

					if (root != null && root.isRootElement()) {

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
			} else if (status.equalsIgnoreCase("valid") && root != null && root.isRootElement()) {

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
		return doc;
	}
	
	
	
	

}
