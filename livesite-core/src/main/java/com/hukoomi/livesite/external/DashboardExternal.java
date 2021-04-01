package com.hukoomi.livesite.external;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
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
  
  private static final Logger LOGGER = Logger.getLogger(DashboardExternal.class);
  
  public void dashboardServices(RequestContext context, String accessToken) {
    String jwtParsedToken = null;
    HttpServletRequest request = context.getRequest();
    JWTTokenUtil jwt = new JWTTokenUtil(context);
    if (accessToken != null)
      try {
        jwtParsedToken = jwt.parseJwt(accessToken);
       
        setSessionAttributes(jwtParsedToken, request, "valid");
      } catch (ExpiredJwtException e) {
        LOGGER.info("Token Expired");
        setSessionAttributes(jwtParsedToken, request, "Token Expired");
      } catch (SignatureException e) {
        LOGGER.info("Signature Exception");
        setSessionAttributes(jwtParsedToken, request, "Signature Exception");
      } catch (Exception e) {
        LOGGER.info("Some other exception in JWT parsing" + e);
        setSessionAttributes(jwtParsedToken, request, "Some other exception in JWT parsing");
      }  
  }
  
  private void setSessionAttributes(String jwtParsedToken, HttpServletRequest request, String status) {
    LOGGER.info("--------------setSessionAttributes is called------------");
    HttpSession session = request.getSession(true); 
    
    session.setAttribute("status", status);
    if(status.equals("valid")) {
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
    session.setAttribute("exp", getValue(jwtParsedToken, "exp"));
    }
    LOGGER.info("Expiry Date"+session.getAttribute("exp"));
    LOGGER.info("--------------setSessionAttributes is Ended------------");
  }
  
  private static String getValue(String response, String key) {
	    String status = "";
	    if (!response.equals("")) {
	      JSONObject jsonObj = new JSONObject(response);
	      if (!jsonObj.isNull(key) && !jsonObj.get(key).equals(null))
	        status = (String)jsonObj.get(key); 
	    } 
	    return status;
	  }
  
protected void removeSessionAttr(RequestContext context) {
	LOGGER.info("--------------removeSessionAttr is Ended------------");
	
    HttpServletRequest request = context.getRequest();
    HttpSession session = request.getSession(false);
    if (session != null)
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
	    session.removeAttribute("exp");
    Cookie[] cookies = request.getCookies();
    for (int i = 0; i < cookies.length; i++) {
      String name = cookies[i].getName();
      if (name.equals("accessToken")) {
        cookies[i].setMaxAge(0);
        cookies[i].setPath(request.getRequestURI());
      } 
    } 
    LOGGER.info("--------------removeSessionAttr is Ended------------");
}
  
public DashboardExternal(RequestContext context) {
	LOGGER.info("DashboardExternal : Loading Properties....");
	properties = DashboardExternal.loadProperties(context);
	LOGGER.info("DashboardExternal : Properties Loaded");
}

/**
 * This method will be used to load the configuration properties.
 * 
 * @param context Request context object.
 * 
 */
private static Properties loadProperties(final RequestContext context) {
	LOGGER.info("loadProperties:Begin");
	PropertiesFileReader prop = null;
	prop = new PropertiesFileReader(context, "dashboard.properties");
	return prop.getPropertiesFile();

}

 public void doLogout(RequestContext context) throws IOException {
	 LOGGER.info("--------------doLogout is Ended------------");
	 removeSessionAttr(context);
	 String url = this.properties.getProperty("logout");
    url = url + "?relayURL=" + url;
    HttpServletResponse response = context.getResponse();
    response.sendRedirect(url);
    LOGGER.info("--------------doLogout is Ended------------");
  }
  
  public Document getprofileInfo(RequestContext context) {
	  Document doc = DocumentHelper.createDocument();
	  HttpServletRequest request = context.getRequest();
	  	Element root = doc.getRootElement();
	    Element result = root.addElement("result");
	    Element statusElement = result.addElement("status");
	    statusElement.setText(request.getSession().getAttribute("status").toString());
	    Element fnEnElement = result.addElement("fnEn");
		fnEnElement.setText(request.getSession().getAttribute("fnEn").toString());
		Element fnArElement = result.addElement("fnAr");
		fnArElement.setText(request.getSession().getAttribute("fnAr").toString());
		Element lnEnElement = result.addElement("lnEn");
		lnEnElement.setText(request.getSession().getAttribute("lnEn").toString());
		Element lnArElement = result.addElement("lnAr");
		lnArElement.setText(request.getSession().getAttribute("lnAr").toString());
		Element roleElement = result.addElement("role");
		roleElement.setText(request.getSession().getAttribute("role").toString());
	  
	  return doc;
  }
  
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
				
			String status = (String) request.getSession().getAttribute("status");
			LOGGER.info("Status:"+status);		
			if(status != "valid") {			
					LOGGER.info("--------dashboardServices is called--------");
					dashboardServices(context, accessToken);	
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
				if (expDt != null &&cureentDate.compareTo(expiryDate) < 0 ) {
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
		}else {

				removeSessionAttr(context);
			
		}				
		}
		return doc;
	}
  
}
