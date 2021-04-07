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
import com.hukoomi.utils.UserInfoSession;
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
  
public void removeSessionAttr(RequestContext context) {
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
    LOGGER.info("--------------removeSessionAttr is Ended------------"+session.getAttribute("status"));
}
  

 public Document doLogout(RequestContext context) throws IOException {
	 LOGGER.info("--------------doLogout  Started------------");
	 Document doc = DocumentHelper.createDocument();
	 removeSessionAttr(context);
	 
	 PropertiesFileReader prop = null;
	 prop = new PropertiesFileReader(context, "dashboard.properties");
	 properties = prop.getPropertiesFile();
	 String url = properties.getProperty("logout");	 
    LOGGER.info("---Logout url---"+url);
    HttpServletResponse response = context.getResponse();
    response.sendRedirect(url);
    LOGGER.info("--------------doLogout Ended------------");
	return doc;
  }
  
  public Document getprofileInfo(RequestContext context) {
	  Document doc = DocumentHelper.createDocument();
	  	Element root = doc.getRootElement();
	  	root = root.addElement("result");
	  	UserInfoSession inf = new UserInfoSession();
	   doc =inf.getUserData(context, doc);
	  
	  return doc;
  }
  
  
  
}
