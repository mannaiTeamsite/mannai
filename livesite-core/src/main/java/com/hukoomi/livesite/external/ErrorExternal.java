package com.hukoomi.livesite.external;


import com.hukoomi.utils.CommonUtils;
import com.hukoomi.utils.PropertiesFileReader;
import com.hukoomi.utils.RequestHeaderUtils;
import com.interwoven.livesite.runtime.RequestContext;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

public class ErrorExternal {
	
	 private final static Logger logger = Logger.getLogger(ErrorExternal.class);
	 
	 
	 
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
			 
			 String errorpagePathEn = "/en/error.page";			
			 String errorpagePathAr = "/ar/error.page";
			 String contentPagePath = "";
				try {
					 contentPagePath = new URL(contentPage).getPath();
					 
				} catch (MalformedURLException e) {
					logger.debug(e);
				}
				String brokenLinkPath = "";
				try {
					brokenLinkPath = new URL(brokenLink).getPath();
					 
				} catch (MalformedURLException e) {
					logger.debug(e);
				}			
			 if((!errorpagePathEn.equals(contentPagePath) && !errorpagePathAr.equals(contentPagePath)) && !errorpagePathEn.equals(brokenLinkPath) && !errorpagePathAr.equals(brokenLinkPath)) {
				 CommonUtils cu = new CommonUtils(context);
				 String domain = cu.getURLPrefix(context);
				 
				 brokenLink = domain+brokenLinkPath;
				 logger.info("brokenLink  "+brokenLink);
				 contentPage = domain+contentPagePath;
				 logger.info("contentPage  "+contentPage);
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
	
	
	
         
                                    
}
