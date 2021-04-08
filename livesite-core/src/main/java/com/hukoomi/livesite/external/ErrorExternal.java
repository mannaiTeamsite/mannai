package com.hukoomi.livesite.external;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.ValidationErrorList;

import com.hukoomi.utils.CommonUtils;
import com.hukoomi.utils.ESAPIValidator;
import com.hukoomi.utils.Postgre;
import com.hukoomi.utils.RequestHeaderUtils;
import com.interwoven.livesite.runtime.RequestContext;

public class ErrorExternal {
	
	 private final Logger logger = Logger.getLogger(ErrorExternal.class);
	 
	 /**
	     * Postgre Object variable.
	     */
	    Postgre postgre = null;
	public Document errorData(final RequestContext context) {
		logger.info("ErrorExternal : errorData ---- Started");
		final String COMPONENT_TYPE = "componentType";
		 String compType = context.getParameterString(COMPONENT_TYPE); 
		 logger.info("Component Type"+compType);
		if(compType != null && compType.equalsIgnoreCase("Banner"))
			insertErrorResponse(context);               
        Document doc = getErrorDCRContent(context);  
        logger.info("ErrorExternal : errorData ---- Ended");
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
	
	
	 private boolean insertErrorResponse(RequestContext context) {
         logger.info("ErrorExternal : insertErrorResponse");
         final String STATUS = "status";
         RequestHeaderUtils req = new RequestHeaderUtils(context);
         
         final String LOCALE = "locale";
         ValidationErrorList errorList = new ValidationErrorList();
         
         
         String status = context.getParameterString(STATUS);
         
         if (!ESAPIValidator.checkNull(status.toString())) {
        	 status  = ESAPI.validator().getValidInput("status", status, ESAPIValidator.ALPHANUMERIC, 20, false, true, errorList);
             if(!errorList.isEmpty()) {            
                 logger.info(errorList.getError("status"));
                 status = null;
             }
         }
         
         String referer = req.getReferer();
         if (!ESAPIValidator.checkNull(referer)) {
        	 referer  = ESAPI.validator().getValidInput("referer", referer, ESAPIValidator.URL, 255, false, true, errorList);
             if(!errorList.isEmpty()) {
                 logger.info(errorList.getError("referer"));
                 referer = null;
                
             }
         }
         
         String reqURL = req.getRequestURL();
         if (!ESAPIValidator.checkNull(reqURL)) {
        	 reqURL  = ESAPI.validator().getValidInput("reqURL", reqURL, ESAPIValidator.URL, 255, false, true, errorList);
             if(!errorList.isEmpty()) {
                 logger.info(errorList.getError("reqURL"));
                 reqURL = null;
                
             }
         }
         
         String lang = context.getParameterString(LOCALE);
         if (!ESAPIValidator.checkNull(lang)) {
        	 lang  = ESAPI.validator().getValidInput(LOCALE, lang, ESAPIValidator.ALPHABET, 20, false, true, errorList);
             if(!errorList.isEmpty()) {
                 logger.info(errorList.getError("lang"));
                 lang = null;
                
             }
         }      
         boolean iserrorDataInserted = false;  
         
         
         if( referer != null && !(referer.trim()).equals("") && reqURL != null && !(reqURL.trim()).equals("") &&  status != null) {
        	  PreparedStatement errorprepareStatement = null;
              Connection connection = null;
              postgre = new Postgre(context);
         try {
        	 logger.info("ErrorExternal : insertTable--Start");   	    
             connection = postgre.getConnection();  
             String errorResponseQuery = "";
             errorResponseQuery = "INSERT INTO ERROR_RESPONSE ("
                     + "BROKEN_LINK, CONTENT_PAGE, LANGUAGE, STATUS_CODE, REPORTED_ON"
                     + ") VALUES(?,?,?,?,LOCALTIMESTAMP)";  
             logger.info("ErrorExternal : errorResponseQuery"+errorResponseQuery);
             connection.setAutoCommit(false);
             errorprepareStatement = connection
                     .prepareStatement(errorResponseQuery);
             errorprepareStatement.setString(1, reqURL);
             errorprepareStatement.setString(2, referer);
             errorprepareStatement.setString(3, lang);
             errorprepareStatement.setString(4, status);             
             int insertResponse = errorprepareStatement.executeUpdate();
             
             if( insertResponse == 1) {
            	 iserrorDataInserted = true;
             }
             logger.info("ErrorExternal : insertTable--End");   	
             
		} catch (SQLException e) {
			logger.error("Exception in insertErrorResponse", e);
		}finally {
           
            postgre.releaseConnection(connection, errorprepareStatement,
                    null);
        }
         } 
         logger.info("iserrorDataInserted : " +iserrorDataInserted);  
        return iserrorDataInserted;
    }
         
                                    
}
