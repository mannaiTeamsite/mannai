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
		Document doc = DocumentHelper.createDocument();
		
         
         
        	 insertErrorResponse(context);       
        	 RequestHeaderUtils req = new RequestHeaderUtils(context);
             Integer status = req.getStatus();
             if(status == 200) {
            	 status = 404;
             }
             boolean insert = insertErrorResponse(context);
          	 Element errData = doc.addElement("errData");
        	 Element statusElement = errData.addElement("status");
        	 statusElement.setText(status.toString());
        	 Element insertElement = errData.addElement("Inserted");
        	 if(insert == true)
        		 insertElement.setText("true");       		 
        	 else
        		 insertElement.setText("false");
        		 
	        
        
        logger.info("ErrorExternal : errorData ---- Ended");
		return doc;		
	}
	 private boolean insertErrorResponse(RequestContext context) {
         logger.info("ErrorExternal : insertErrorResponse");
         RequestHeaderUtils req = new RequestHeaderUtils(context);
         
         final String LOCALE = "locale";
         final String COMPONENT_TYPE = "componentType";
         ValidationErrorList errorList = new ValidationErrorList();
         
         
         String status = req.getStatus().toString();
         
         if (!ESAPIValidator.checkNull(status.toString())) {
        	 status  = ESAPI.validator().getValidInput("status", status, ESAPIValidator.ALPHABET, 20, false, true, errorList);
             if(!errorList.isEmpty()) {            
                 logger.info(errorList.getError("status"));
                 status = null;
             }
         }
         
         String referer = req.getReferer();
         if (!ESAPIValidator.checkNull(referer)) {
        	 referer  = ESAPI.validator().getValidInput("referer", referer, ESAPIValidator.ALPHABET, 20, false, true, errorList);
             if(!errorList.isEmpty()) {
                 logger.info(errorList.getError("referer"));
                 referer = null;
                
             }
         }
         
         String reqURL = req.getRequestURL();
         if (!ESAPIValidator.checkNull(reqURL)) {
        	 reqURL  = ESAPI.validator().getValidInput("reqURL", reqURL, ESAPIValidator.ALPHABET, 20, false, true, errorList);
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
        

         
         String compType = context.getParameterString(COMPONENT_TYPE);      
         logger.info("Componrnt Type:"+compType);
         
         boolean iserrorDataInserted = false;   
         
         if(compType != null && compType.equalsIgnoreCase("Banner") && referer != null && reqURL != null && req.getStatus() != 200 && status != null) {
        	  PreparedStatement errorprepareStatement = null;
              Connection connection = null;
              postgre = new Postgre(context);
         try {
        	 logger.info("ErrorExternal : insertTable--Start");   	    
             connection = postgre.getConnection();  
             String errorResponseQuery = "";
             errorResponseQuery = "INSERT INTO ERROR_RESPONSE ("
                     + "BROKEN_LINK, CONTENT_PAGE, LANGUAGE, STATUS_CODE, REPORTED_ON"
                     + ") VALUES( ?, ?,?,?,LOCALTIMESTAMP)";  
             logger.info("ErrorExternal : errorResponseQuery"+errorResponseQuery);
             connection.setAutoCommit(false);
             errorprepareStatement = connection
                     .prepareStatement(errorResponseQuery);
             errorprepareStatement.setString(1, reqURL);
             errorprepareStatement.setString(2, referer);
             errorprepareStatement.setString(3, lang);
             errorprepareStatement.setString(4, status);             
          
             
             if( errorprepareStatement.executeUpdate() == 1) {
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

        return iserrorDataInserted;
    }
         
                                    
}
