package com.hukoomi.livesite.external;


import com.hukoomi.utils.CommonUtils;
import com.hukoomi.utils.Postgre;
import com.hukoomi.utils.PropertiesFileReader;
import com.hukoomi.utils.RequestHeaderUtils;
import com.interwoven.livesite.runtime.RequestContext;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;


public class ErrorExternal {
	private Properties properties = null;
	 private final static Logger logger = Logger.getLogger(ErrorExternal.class);
	 Postgre postgre = null;
	public Document errorData(final RequestContext context) {
		logger.info("ErrorExternal : errorData ---- Started");
		final String COMPONENT_TYPE = "componentType";
		final String LOCALE = "locale";
		 final String STATUS = "error_code";
		RequestHeaderUtils req = new RequestHeaderUtils(context);
		 String compType = context.getParameterString(COMPONENT_TYPE); 
		 logger.info("Component Type"+compType);		 
		 
		if(compType != null && compType.equalsIgnoreCase("Banner") && context.isRuntime())
		{
			
			String brokenLink = req.getRequestURL();
			 	 
			 
			 String language = context.getParameterString(LOCALE);
			 String statusCode = context.getParameterString(STATUS);
			 
			 logger.info("Error Status "+statusCode);
			 String contentPage = req.getReferer(); 
			 String brokenLinkPath = null;
			 try {
		        	brokenLinkPath = (new URL(brokenLink)).getPath();
		        } catch (MalformedURLException e) {
		          logger.debug(e);
		        } 
			 			 
			 PropertiesFileReader prop = null;
				prop = new PropertiesFileReader(context, "error.properties");
				properties = prop.getPropertiesFile();
			 String errorPageEn=properties.getProperty("errorPageEn");
		        String errorPageAr=properties.getProperty("errorPageAr");
		        String errorUrls=properties.getProperty("urls");
		        
				 if(brokenLinkPath.equalsIgnoreCase(errorPageEn) || brokenLinkPath.equalsIgnoreCase(errorPageAr)) {
				 HttpServletRequest request = context.getRequest();
				 brokenLink = contentPage;
					Cookie cookie = null;
					Cookie[] cookies = null;
					
					cookies = request.getCookies();
					if (cookies != null) {
						for (int i = 0; i < cookies.length; i++) {
							cookie = cookies[i];
							
							if (cookie.getName().equals("relayURL")) {
								contentPage = cookie.getValue();
							}
						}
					}
				 
				 }
				 try {
					 contentPage = (new URL(contentPage)).getPath();
			        } catch (MalformedURLException e) {
			          logger.debug(e);
			        } 
				 int count = 0;
				 CommonUtils cu = new CommonUtils();
				 String urlPrefix = cu.getURLPrefix(context);
				 contentPage = urlPrefix + contentPage;
				 brokenLink = urlPrefix + brokenLink;
				 String[] urls = errorUrls.split("\\s*,\\s*");
				for(int i = 0; i<urls.length;i++ ) {
					if(urls[i].equalsIgnoreCase(brokenLink)) {
						count++;
					}
				}
		
				 
				 logger.info("contentPage  "+contentPage);
				 logger.info("brokenLink  "+brokenLink);
				 
				
				if(count == 0) {
					cu.logBrokenLink(brokenLink, contentPage, language, statusCode); 
				}
					
			 
			 
		}
		Document doc = getErrorDCRContent(context);  
		 logger.info("ErrorBannerDoc"+doc.asXML());
        logger.info("ErrorExternal : errorData ---- Ended");
		return doc;		
	}

	public Document getStatus(final RequestContext context) {
		logger.info("ErrorExternal : getStatus ---- Started");              
        Document doc = DocumentHelper.createDocument();    
        Element statusElement = doc.addElement("status");
		CommonUtils cu = new CommonUtils(context);
		statusElement.setText(cu.sanitizeSolrQuery(context.getParameterString("error_code")));
        logger.info("ErrorExternal : getStatus ---- Ended");
		return doc;		
	}
	
	
	public Document getErrorDCRContent(RequestContext reqcontext) {
        logger.info("Fetching Error DCR Content");
        final String STATUS = "error_code";
        final String GENERAL_ERROR = "general_error";
        Document doc = DocumentHelper.createDocument();
        Element root = doc.addElement("content");
		CommonUtils cu = new CommonUtils(reqcontext);
        String status = cu.sanitizeSolrQuery(reqcontext.getParameterString(STATUS));
        String generalError = cu.sanitizeSolrQuery(reqcontext.getParameterString(GENERAL_ERROR));
        Element statusElement = root.addElement("status");
      		statusElement.setText(status);
       String dcrPath = reqcontext.getParameterString("dcrPath")+"/error-"+status.replace("\"", "");

       logger.info("Status"+status);
     
       if(dcrPath.equals("")){
           return doc;
       }
        Document data = null;
        if (cu.isPathExists(dcrPath)) {
        	 logger.info("DCR Path: " + dcrPath);
              data = cu.readDCR(dcrPath);
        }else {
        	logger.info("generalError Path: " + generalError);
             data = cu.readDCR(generalError);
        }
        
        if (data == null) {
            return null;
        }
        Element detailedElement = data.getRootElement();
        root.add(detailedElement);
        return doc;
    }
	
	
	public Document errorResponse(RequestContext reqcontext) {
		 Document doc = DocumentHelper.createDocument();
		 String language = reqcontext.getParameterString("lang");
		 
		 postgre = new Postgre(reqcontext);
	        Element errorResultEle = doc.addElement("result");
	        getErrorTable(errorResultEle, language);
		
		return doc;
	}
	
	private Connection getConnection() {
        return postgre.getConnection();
    }
	
	
	
	public void getErrorTable(Element errorResultEle, String language) {
        logger.info("Fetching getErrorTable Content");
       
        	
        
        PreparedStatement statement = null;
        Connection connection = null;
       
        ResultSet resultSet = null;
        try {
          logger.info("Get count of error");
          connection = getConnection();
          
          String query = "SELECT * FROM ERROR_RESPONSE WHERE LANGUAGE = '"+language+"'";
         logger.info("Query to run : " + query);
          
          statement = connection.prepareStatement(query);
          resultSet = statement.executeQuery();
          Element root = errorResultEle.addElement("content");
          while (resultSet.next()) {
        	  logger.info("result set  "+resultSet.getString("broken_link"));
        	  Element errorData = root.addElement("errorData");
          		 
        		  
        		  Element bokenLinkElement = errorData.addElement("broken_link");
        		  bokenLinkElement.setText(resultSet.getString("broken_link"));
        		  
        		  Element contentPageElement = errorData.addElement("content_page");
        		  contentPageElement.setText(resultSet.getString("content_page"));
        		  
        		  Element reportedElement = errorData.addElement("last_reported");
        		  reportedElement.setText(resultSet.getDate("reported_on").toString());
        		  
        		  Element languageElement = errorData.addElement("language");
        		  languageElement.setText(resultSet.getString("language"));
        		  
        		  Element statusCodeElement = errorData.addElement("status_code");
        		  statusCodeElement.setText(resultSet.getString("status_code"));
        		  
        		  Element countElement = errorData.addElement("count");
        		  int count = resultSet.getInt("count");
        		  countElement.setText(String.valueOf(count));
        		  
        		  Element statusElement = errorData.addElement("status");
        		  statusElement.setText(resultSet.getString("status"));       	
     	  
          }
                  
          logger.info("Fetching getErrorTable Content completed");
        } catch (SQLException ex) {
          logger.error("Error while fetching Error data from database.", ex);
        } finally {
         logger.info("Releasing Database Connection");
          postgre.releaseConnection(connection, statement, resultSet);
          logger.info("Released Database Connection");
          
        } 
            
        
           
        
    }
	
	
	
                                
}
