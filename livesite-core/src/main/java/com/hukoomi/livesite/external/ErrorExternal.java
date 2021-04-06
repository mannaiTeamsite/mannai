package com.hukoomi.livesite.external;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
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
          	 Element errData = doc.addElement("errData");
        	 Element statusElement = errData.addElement("status");
	         statusElement.setText(status.toString());
        
        logger.info("ErrorExternal : errorData ---- Ended");
		return doc;		
	}
	 private boolean insertErrorResponse(RequestContext context) {
         logger.info("ErrorExternal : insertErrorResponse");
         RequestHeaderUtils req = new RequestHeaderUtils(context);
         Integer status = req.getStatus();
         String referer = req.getReferer();
         String reqURL = req.getRequestURL();
         String compType = context.getParameterString("componentType");
         logger.info("Componrnt Type:"+compType);
         
         boolean iserrorDataInserted = false;   
         
         if(compType != null && compType.equalsIgnoreCase("FAQ") && !referer.isBlank() && !reqURL.isBlank() && status != 200) {
        	  PreparedStatement errorprepareStatement = null;
              Connection connection = null;
              postgre = new Postgre(context);
         try {
        	 logger.info("ErrorExternal : insertTable--Start");   	    
             connection = postgre.getConnection();  
             String errorResponseQuery = "";
             errorResponseQuery = "INSERT INTO ERROR_RESPONSE ("
                     + "BROKEN_LINK, CONTENT_PAGE, REPORTED_ON, "
                     + ") VALUES( ?, ?,?,LOCALTIMESTAMP)";  
             logger.info("ErrorExternal : errorResponseQuery"+errorResponseQuery);
             connection.setAutoCommit(false);
             errorprepareStatement = connection
                     .prepareStatement(errorResponseQuery);
             errorprepareStatement.setLong(1, status);
             errorprepareStatement.setString(2, referer);
             errorprepareStatement.setString(3, reqURL);
             
             
             iserrorDataInserted = true;
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
