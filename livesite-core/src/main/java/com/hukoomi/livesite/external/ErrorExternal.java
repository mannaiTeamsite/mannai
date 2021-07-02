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
	
	 private final Logger logger = Logger.getLogger(ErrorExternal.class);
	 private Properties properties = null;
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
			 if(!contentPage.isEmpty() && contentPage != "" && contentPage != null) {
				 
			
			 PropertiesFileReader prop = null;
				prop = new PropertiesFileReader(context, "error.properties");
				properties = prop.getPropertiesFile();
			 String errorpagePathEn = properties.getProperty("errorPageEn");
			 logger.info("errorpagePathEn  "+errorpagePathEn);
			 String errorpagePathAr = properties.getProperty("errorPageAr");
			 logger.info("errorpagePathAr  "+errorpagePathAr);
			 String domain = properties.getProperty("domain");
			 logger.info("Domain  "+domain);
			 logger.info("Actual broken link  "+req.getForwardedHost());
			 String contentPage_path = "";
				try {
					 contentPage_path = new URL(contentPage).getPath();
					 logger.info("contentPage_path  "+contentPage_path);
				} catch (MalformedURLException e) {
					logger.debug(e);
				}
				String broken_link_path = "";
				try {
					broken_link_path = new URL(brokenLink).getPath();
					 logger.info(" broken_link_path  "+broken_link_path);
				} catch (MalformedURLException e) {
					logger.debug(e);
				}			
			 if((!errorpagePathEn.equals(contentPage_path) && !errorpagePathAr.equals(contentPage_path)) && !errorpagePathEn.equals(broken_link_path) && !errorpagePathAr.equals(broken_link_path)) {
				 CommonUtils cu = new CommonUtils(context);
				 brokenLink = domain+broken_link_path;
				 logger.info("brokenLink  "+brokenLink);
				 contentPage = domain+contentPage_path;
				 logger.info("contentPage  "+contentPage);
					cu.logBrokenLink(brokenLink, contentPage, language, statusCode); 
			 }	
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
		statusElement.setText(context.getParameterString("error_code"));
        logger.info("ErrorExternal : getStatus ---- Ended");
		return doc;		
	}

	
	public Document getErrorDCRContent(RequestContext reqcontext) {
        logger.info("Fetching Error DCR Content");
        final String STATUS = "error_code";
        final String GENERAL_ERROR = "general_error";
        Document doc = DocumentHelper.createDocument();
        Element root = doc.addElement("content");
        String status = reqcontext.getParameterString(STATUS);
        String generalError = reqcontext.getParameterString(GENERAL_ERROR);
        Element statusElement = root.addElement("status");
      		statusElement.setText(status);
       String dcrPath = reqcontext.getParameterString("dcrPath")+"/error-"+status.replace("\"", "");

      
       logger.info("Status"+status);
     
       if(dcrPath.equals("")){
           return doc;
       }
        CommonUtils commonUtils = new CommonUtils(reqcontext);
        Document data = null;
        if (commonUtils.isPathExists(dcrPath)) {
        	 logger.info("DCR Path: " + dcrPath);
              data = commonUtils.readDCR(dcrPath);
        }else {
        	logger.info("generalError Path: " + generalError);
             data = commonUtils.readDCR(generalError);
        }
        
        if (data == null) {
            return null;
        }
        Element detailedElement = data.getRootElement();
        root.add(detailedElement);
        return doc;
    }
	
	
	
         
                                    
}
