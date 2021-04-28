package com.hukoomi.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.ValidationErrorList;

import com.hukoomi.livesite.external.ErrorExternal;
import com.interwoven.livesite.runtime.RequestContext;

public class ErrorDBInsert {
	
 private final Logger logger = Logger.getLogger(ErrorExternal.class);
	 
	 /**
	     * Postgre Object variable.
	     */
	    Postgre postgre = null;
	
	 public boolean insertErrorResponse(RequestContext context) {
         logger.info("ErrorExternal : insertErrorResponse");
         final String STATUS = "error_code";
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
         logger.info("Referer URL"+referer);
         logger.info("Request URL"+reqURL);
         logger.info("Final Status"+status);
         logger.info("Language"+lang);
         
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
