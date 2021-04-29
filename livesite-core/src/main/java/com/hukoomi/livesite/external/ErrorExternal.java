package com.hukoomi.livesite.external;


import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import com.hukoomi.utils.CommonUtils;


import com.hukoomi.utils.RequestHeaderUtils;
import com.interwoven.livesite.runtime.RequestContext;

public class ErrorExternal {
	
	 private final Logger logger = Logger.getLogger(ErrorExternal.class);
	 
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
			
			 String brokenLink = req.getReferer();
			 String language = context.getParameterString(LOCALE);
			 String statusCode = context.getParameterString(STATUS);
			   String contentPage = req.getRequestURL();
			
			CommonUtils cu = new CommonUtils();
			cu.logBrokenLink(brokenLink, contentPage, language, statusCode);
			 
		}
        Document doc = getErrorDCRContent(context);  
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
       String dcrPath = reqcontext.getParameterString("dcrPath")+"/error-"+status;
      
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
