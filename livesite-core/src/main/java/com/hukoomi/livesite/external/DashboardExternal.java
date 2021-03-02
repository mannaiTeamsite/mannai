package com.hukoomi.livesite.external;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.json.JSONObject;

import com.hukoomi.utils.JWTTokenUtil;
import com.interwoven.livesite.runtime.RequestContext;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;

public class DashboardExternal extends HttpServlet {

    /** Logger object to check the flow of the code. */
    private static final Logger LOGGER =
            Logger.getLogger(DashboardExternal.class);

    public Document dashboardServices(final RequestContext context){
        Document document = DocumentHelper.createDocument();
        String jwtParsedToken = null;
        
       HttpServletRequest request = context.getRequest();
        JWTTokenUtil jwt = new JWTTokenUtil(context);
        
        
        //String token = null;
        String action = null;
        action = context.getParameterString("action");
        LOGGER.info("action:" + action);
        if (action.equals("tokenverify")) { 
        	
	   String accessToken = null;
		   Cookie cookie = null;
		   Cookie[] cookies = null;
		   cookies = request.getCookies();
		   if( cookies != null ) {
			   for (int i = 0; i < cookies.length; i++) {
		            cookie = cookies[i];
		            LOGGER.info("Name:" + cookie.getName());
		            LOGGER.info("Value:" + cookie.getValue());
		            if (cookie.getName().equals("accessToken")) {
		            	accessToken = cookie.getValue();
		            }
		            }
		         }
        
		   LOGGER.info("accessToken:" + accessToken);
       
            LOGGER.debug("tokenverify.");
          // accessToken = context.getParameterString("accessToken");
            //token = context.getRequest().getHeaders().get("accessToken");
            if (accessToken != null) {
                try {
                    jwtParsedToken = jwt.parseJwt(accessToken);
                    System.out.println("data:\n" + jwtParsedToken);
                    JSONObject jsonObj = new JSONObject(jwtParsedToken);
                    jsonObj.get("fnAr");
                    System.out.println("name:\n" + jsonObj.get("fnAr"));

                    document = getDocument(jwtParsedToken);
                } catch (ExpiredJwtException e) {
                    LOGGER.debug("Token Expired");
                    document = getDocument(jwtParsedToken);
                    Element resultElement = document.addElement("Result");
                    Element statusElement = resultElement.addElement("status");
                    statusElement.setText("Token Expired");
                    return document;
                } catch (SignatureException e) {
                    LOGGER.debug("Signature Exception");
                    Element resultElement = document.addElement("Result");
                    Element statusElement = resultElement.addElement("status");
                    statusElement.setText("Signature Exception");
                    return document;
                } catch (Exception e) {
                    LOGGER.debug("Some other exception in JWT parsing" +e );
                    document = getDocument(jwtParsedToken);
                    Element resultElement = document.addElement("Result");
                    Element statusElement = resultElement.addElement("status");
                    statusElement.setText("Exception");
                    return document;
                }
            }
            return document;
        }
        Element resultElement = document.addElement("Result");
        Element statusElement = resultElement.addElement("status");
        statusElement.setText("null");
        return document;
    }

    private Document getDocument(String jwtParsedToken) {
    	
    	LOGGER.info("--------------getDocument is called------------");
        Document document = DocumentHelper.createDocument();
        Element resultElement = document.addElement("Result");
        Element statusElement = resultElement.addElement("status");
        statusElement.setText("Valid");
        Element unmElement = resultElement.addElement("unm");
        unmElement.setText(getValue(jwtParsedToken, "unm"));
        Element userIdElement = resultElement.addElement("uid");
        userIdElement.setText(getValue(jwtParsedToken, "uid"));
        Element nameElementEn = resultElement.addElement("fnEn");
        nameElementEn.setText(getValue(jwtParsedToken, "fnEn"));
        Element nameElementAr = resultElement.addElement("fnAr");
        nameElementAr.setText(getValue(jwtParsedToken, "fnAr"));        
        Element lastNameElementEn = resultElement.addElement("lnEn");
        lastNameElementEn.setText(getValue(jwtParsedToken, "lnEn"));
        Element nlastNmeElementAr = resultElement.addElement("lnAr");
        nlastNmeElementAr.setText(getValue(jwtParsedToken, "lnAr"));        
        Element Qid = resultElement.addElement("QID");
        Qid.setText(getValue(jwtParsedToken, "QID"));
        Element Eid = resultElement.addElement("EID");
        Eid.setText(getValue(jwtParsedToken, "EID"));
        Element phoneElement = resultElement.addElement("mobile");
        phoneElement.setText(getValue(jwtParsedToken, "mobile"));
        Element emailElement = resultElement.addElement("email");
        emailElement.setText(getValue(jwtParsedToken, "email"));        
        Element lstMdfyElement = resultElement.addElement("lstMdfy");
        lstMdfyElement.setText(getValue(jwtParsedToken, "lstMdfy"));
        Element roleElement = resultElement.addElement("role");
        roleElement.setText(getValue(jwtParsedToken, "role"));
        return document;
    }

    private static String getValue(final String response, final String key) {
        String status = null;
        if (!response.equals("")) {
            JSONObject jsonObj = new JSONObject(response);
            status = (String) jsonObj.get(key);
        }
        return status;
    }
}
