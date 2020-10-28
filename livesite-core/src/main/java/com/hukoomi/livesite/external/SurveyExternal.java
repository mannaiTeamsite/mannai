/**
 * 
 */
package com.hukoomi.livesite.external;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import com.hukoomi.utils.GoogleRecaptchaUtil;
import com.hukoomi.utils.Postgre;
import com.interwoven.livesite.runtime.RequestContext;

/**
 * @author vmohandass
 *
 */
public class SurveyExternal {
	/** Logger object to check the flow of the code.*/
	private final Logger logger = Logger.getLogger(SurveyExternal.class);
	
	public Document submitSurvey(final RequestContext context) {
		logger.debug("SurveyExternal : submitSurvey");
		
		DetailExternal detailExt = new DetailExternal();
		String surveyAction = context.getParameterString("surveyAction");
	    logger.info("surveyAction : " + surveyAction);
		HttpServletRequest request = context.getRequest();
        Locale locale = request.getLocale();
        String lang = locale.getLanguage();
        logger.info("lang : " + lang);
        String userId = context.getParameterString("user_id");
        userId = "testUser";
        logger.info("userId : " + userId);
        String ipAddress = context.getRequest().getRemoteAddr();
        logger.info("ipAddress : " + ipAddress);
        String userAgent = context.getRequest().getHeader("User-Agent");
        logger.info("userAgent : " + userAgent);
        String surveyTakenfrom = context.getParameterString("surveyTakenfrom");
        logger.info("surveyTakenfrom : " + surveyTakenfrom);
        String surveyId = context.getParameterString("surveyId");
        logger.info("surveyId : " + surveyId);
        String totalQuestions = context.getParameterString("totalQuestions");
        logger.info("totalQuestions : " + totalQuestions);
        String captchaResponse = context.getParameterString("g-recaptcha-response");
        logger.info("captchaResponse : " + captchaResponse);
        Document document = DocumentHelper.createDocument();
        if(surveyAction != null && !"".equals(surveyAction)) {
        	if("submit".equalsIgnoreCase(surveyAction)) {
				Element surveyResponseElem = document.addElement("SurveyResponse");
				Element surveyStatusElem = surveyResponseElem.addElement("Status");
				GoogleRecaptchaUtil captchUtil = new GoogleRecaptchaUtil();
				if(captchUtil.validateCaptch(captchaResponse)) {
			        boolean insertSurveyResponse = insertSurveyResponse(surveyId, lang, userId, ipAddress, userAgent, surveyTakenfrom, totalQuestions, context);
			        if(insertSurveyResponse) {
			        	surveyStatusElem.setText("Success");
			        }else {
			        	surveyStatusElem.setText("Failed");
			        }
				}else {
					surveyStatusElem.setText("Failed");
				}
        	}else if("detail".equalsIgnoreCase(surveyAction)) {
        		document = detailExt.getContentDetail(context);
        	}
        	
        }else {
        	logger.debug("Error : Survey Action not available");
        }
	    return document;
	}
	
	private boolean insertSurveyResponse(String surveyId, String lang, String userId, String ipAddress, String userAgent, String surveyTakenfrom, String totalQuestions, RequestContext context) {
        logger.debug("SurveyExternal : insertSurveyResponse");
        
        PreparedStatement prepareStatement = null;
        Statement responseIdStmt = null;
        Statement answerIdQueryStmt = null;
        Connection connection = null;
        ResultSet rs = null;
        int totalCount = 0;
        boolean isSurveyInserted = false;
        
        try {
        	
        	if(totalQuestions != null && !"".equals(totalQuestions)) {
        		totalCount = Integer.parseInt(totalQuestions);
        	}
        	connection = Postgre.getConnection();
        	String responseIdQuery = "SELECT nextval('survey_response_response_id_seq') as responseId";
        	responseIdStmt = connection.createStatement();
        	rs = responseIdStmt.executeQuery(responseIdQuery);
        	Long responseId = 0L;
        	while (rs.next()) {
        		responseId = rs.getLong("responseId");
        	}
        	logger.info("responseId : "+responseId);
        	
        	String surveyResponseQuery = "INSERT INTO SURVEY_RESPONSE (RESPONSE_ID, SURVEY_ID, LANG, USER_ID, IP_ADDRESS, USER_AGENT, SURVEY_TAKEN_ON, SURVEY_TAKEN_FROM) VALUES(?, ?, ?, ?, ?, ?, LOCALTIMESTAMP, ?)";
        	connection.setAutoCommit(false);
            prepareStatement = connection.prepareStatement(surveyResponseQuery);
            prepareStatement.setLong(1, responseId);
            prepareStatement.setLong(2, Long.parseLong(surveyId));
            prepareStatement.setString(3, lang);
            prepareStatement.setString(4, userId);
            prepareStatement.setString(5, ipAddress);
            prepareStatement.setString(6, userAgent);
            prepareStatement.setString(7, surveyTakenfrom);
            int result = prepareStatement.executeUpdate();
            if (result > 0) {
            	logger.info("Survey Response Inserted");
            	
            	String surveyAnswerQuery = "INSERT INTO SURVEY_ANSWERS (ANSWER_ID, RESPONSE_ID, SURVEY_ID, LANG, QUESTION_ID, ANSWER) VALUES(?, ?, ?, ?, ?, ?)";
            	prepareStatement = connection.prepareStatement(surveyAnswerQuery);
            	for (int i = 1; i <= totalCount; i++) {
            		
            		String answerIdQuery = "SELECT nextval('survey_answers_answer_id_seq') as answerId";
                	answerIdQueryStmt = connection.createStatement();
                	rs = answerIdQueryStmt.executeQuery(answerIdQuery);
                	Long answerId = 0L;
                	while (rs.next()) {
                		answerId = rs.getLong("answerId");
                	}
                	logger.info("answerId : "+answerId);
            		
            		prepareStatement.setLong(1, answerId);
            		prepareStatement.setLong(2, responseId);
            		prepareStatement.setLong(3, Long.parseLong(surveyId));
                    prepareStatement.setString(4, lang);
                    prepareStatement.setInt(5, i);
                    prepareStatement.setString(6, context.getParameterString(String.valueOf(i)));    
                    prepareStatement.addBatch();
            	}
            	int[] answerBatch = prepareStatement.executeBatch();
            	
            	if(answerBatch.length == totalCount) {
            		connection.commit();
            		logger.info("Survey Answer Inserted");
            		isSurveyInserted = true;
            	}else {
            		connection.rollback();
            	}
            } else {
            	connection.rollback();
            }
        } catch (SQLException e) {
            String errorMsg = "SQLException :";
            if (null != e.getMessage()) {
                errorMsg += e.getMessage();
            }
            logger.error(errorMsg);
        } finally {
        	Postgre.releaseConnection(null, responseIdStmt, null);
        	Postgre.releaseConnection(null, answerIdQueryStmt, null);
            Postgre.releaseConnection(connection, prepareStatement, rs);
        }
        
        return isSurveyInserted;
    }

}
