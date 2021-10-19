package com.hukoomi.livesite.external;


import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import com.hukoomi.utils.CommonUtils;
import com.hukoomi.utils.PropertiesFileReader;
import com.hukoomi.utils.RequestHeaderUtils;
import com.interwoven.livesite.runtime.RequestContext;


public class ErrorExternal {
	private Properties properties = null;
	 private final static Logger logger = Logger.getLogger(ErrorExternal.class);
	
	public Document errorData(final RequestContext context) {
		logger.info("ErrorExternal : errorData ---- Started");
		try {
		final String COMPONENT_TYPE = "componentType";
		final String LOCALE = "locale";
		final String STATUS = "error_code";
		RequestHeaderUtils req = new RequestHeaderUtils(context);
		
		 String compType = context.getParameterString(COMPONENT_TYPE); 
		 
		 String contentPage = req.getReferer(); 
		 PropertiesFileReader prop = null;
			prop = new PropertiesFileReader(context, "error.properties");
			properties = prop.getPropertiesFile();
			String errorPageEn=properties.getProperty("errorPageEn");
	        String errorPageAr=properties.getProperty("errorPageAr");
	        
	        String brokenLink = req.getRequestURL();
	        try {
				 brokenLink = (new URL(brokenLink)).getPath();
		        } catch (MalformedURLException e) {
		          logger.error("Exception occured while getting url path",e);
		        } 
 
		if(compType.equalsIgnoreCase("Banner") && context.isRuntime() && !contentPage.isBlank() && !brokenLink.equalsIgnoreCase(errorPageEn) && !brokenLink.equalsIgnoreCase(errorPageAr))
		{			
		 String language = context.getParameterString(LOCALE);
			 String statusCode = context.getParameterString(STATUS);
	        String errorURLStr =properties.getProperty("urls");
	        logger.info("urls  "+errorURLStr);
	        	try {
					 contentPage = (new URL(contentPage)).getPath();
			        } catch (MalformedURLException e) {
			         logger.error("Exception occured while getting url path",e);
			        } 
				
				 int count = 0;
				if(!errorURLStr.isBlank()) {
				   String[] urlsArray = errorURLStr.split(",");
					
					 for(var i = 0; i < urlsArray.length; i++) {
					    
					   if(urlsArray[i].equalsIgnoreCase(brokenLink)) {
						   count++;
					   }					    
					 }
				}
				 				 
				
				if(count == 0) { 
				 CommonUtils cu = new CommonUtils(context);
				 String urlPrefix = cu.getURLPrefix(context);
				 contentPage = urlPrefix + contentPage;
				 brokenLink = urlPrefix + brokenLink;
				 
			
					cu.logBrokenLink(brokenLink, contentPage, language, statusCode); 
				}			 
		}}catch(Exception e) {
			logger.error("Exception occured did not log in db" , e);
		}
		
		Document doc = getErrorDCRContent(context);  
	
		
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
}
