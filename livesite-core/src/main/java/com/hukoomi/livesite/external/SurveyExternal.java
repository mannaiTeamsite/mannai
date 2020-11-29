/**
 * 
 */
package com.hukoomi.livesite.external;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import com.hukoomi.bo.SurveyBO;
import com.hukoomi.utils.GoogleRecaptchaUtil;
import com.hukoomi.utils.Postgre;
import com.interwoven.livesite.runtime.RequestContext;

/**
 * @author vmohandass
 *
 */
public class SurveyExternal {
    /** Logger object to check the flow of the code. */
    private final Logger logger = Logger.getLogger(SurveyExternal.class);
    Postgre postgre = null;

    public Document submitSurvey(final RequestContext context) {
        logger.debug("SurveyExternal : submitSurvey");

        postgre = new Postgre(context);
        DetailExternal detailExt = new DetailExternal();
        SurveyBO surveyBO = setBO(context);

        Document document = DocumentHelper.createDocument();
        if (surveyBO.getAction() != null
                && !"".equals(surveyBO.getAction())) {
            if ("submit".equalsIgnoreCase(surveyBO.getAction())) {
                Element surveyResponseElem = document
                        .addElement("SurveyResponse");
                Element surveyStatusElem = surveyResponseElem
                        .addElement("Status");
                GoogleRecaptchaUtil captchUtil = new GoogleRecaptchaUtil();
                if (captchUtil
                        .validateCaptcha(context, surveyBO.getCaptchaResponse())) {
                    boolean insertSurveyResponse = insertSurveyResponse(
                            surveyBO, context);
                    if (insertSurveyResponse) {
                        surveyStatusElem.setText("Success");
                    } else {
                        surveyStatusElem.setText("Failed");
                    }
                } else {
                    surveyStatusElem.setText("Failed");
                }
            } else if ("detail".equalsIgnoreCase(surveyBO.getAction())) {
                document = detailExt.getContentDetail(context);
            }

        } else {
            logger.debug("Error : Survey Action not available");
        }
        return document;
    }

    private boolean insertSurveyResponse(SurveyBO surveyBO,
            RequestContext context) {
        logger.debug("SurveyExternal : insertSurveyResponse");

        PreparedStatement surveyprepareStatement = null;
        PreparedStatement answersprepareStatement = null;
        Connection connection = null;
        ResultSet rs = null;
        int totalCount = 0;
        boolean isSurveyInserted = false;

        try {

            totalCount = getIntValue(surveyBO.getTotalQuestions());

            Long responseId = getNextSequenceValue(
                    "survey_response_response_id_seq",
                    postgre.getConnection());
            logger.info("responseId : " + responseId);

            connection = postgre.getConnection();

            String surveyResponseQuery = "INSERT INTO SURVEY_RESPONSE ("
                    + "RESPONSE_ID, SURVEY_ID, LANG, USER_ID, "
                    + "IP_ADDRESS, USER_AGENT, SURVEY_TAKEN_ON, "
                    + "SURVEY_TAKEN_FROM) VALUES(?, ?, ?, ?, ?, ?, "
                    + "LOCALTIMESTAMP, ?)";
            connection.setAutoCommit(false);
            surveyprepareStatement = connection
                    .prepareStatement(surveyResponseQuery);
            surveyprepareStatement.setLong(1, responseId);
            surveyprepareStatement.setLong(2,
                    Long.parseLong(surveyBO.getSurveyId()));
            surveyprepareStatement.setString(3, surveyBO.getLang());
            surveyprepareStatement.setString(4, surveyBO.getUserId());
            surveyprepareStatement.setString(5, surveyBO.getIpAddress());
            surveyprepareStatement.setString(6, surveyBO.getUserAgent());
            surveyprepareStatement.setString(7, surveyBO.getTakenFrom());
            int result = surveyprepareStatement.executeUpdate();
            if (result > 0) {
                logger.info("Survey Response Inserted");

                String surveyAnswerQuery = "INSERT INTO SURVEY_ANSWERS "
                        + "(ANSWER_ID, RESPONSE_ID, SURVEY_ID, LANG, "
                        + "QUESTION_NO, ANSWER) VALUES(?, ?, ?, ?, ?, ?)";
                answersprepareStatement = connection
                        .prepareStatement(surveyAnswerQuery);
                for (int i = 1; i <= totalCount; i++) {

                    Long answerId = getNextSequenceValue(
                            "survey_answers_answer_id_seq",
                            postgre.getConnection());
                    logger.info("answerId : " + answerId);

                    answersprepareStatement.setLong(1, answerId);
                    answersprepareStatement.setLong(2, responseId);
                    answersprepareStatement.setLong(3,
                            Long.parseLong(surveyBO.getSurveyId()));
                    answersprepareStatement.setString(4,
                            surveyBO.getLang());
                    answersprepareStatement.setInt(5, i);
                    answersprepareStatement.setString(6,
                            context.getParameterString(String.valueOf(i)));
                    answersprepareStatement.addBatch();
                }
                int[] answerBatch = answersprepareStatement.executeBatch();

                if (answerBatch.length == totalCount) {
                    connection.commit();
                    logger.info("Survey Answer Inserted");
                    isSurveyInserted = true;
                } else {
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
            Postgre.releaseConnection(null, answersprepareStatement, null);
            Postgre.releaseConnection(connection, surveyprepareStatement,
                    rs);
        }

        return isSurveyInserted;
    }

    private int getIntValue(String inputStringValue) {
        int intValue = 0;
        if (inputStringValue != null && !"".equals(inputStringValue)) {
            intValue = Integer.parseInt(inputStringValue);
        }
        return intValue;
    }

    private Long getNextSequenceValue(String sequenceName,
            Connection connection) {
        Long seqValue = 0L;
        Statement queryStmt = null;
        ResultSet rs = null;
        try {
            String query = "SELECT nextval('" + sequenceName
                    + "') as seqValue";
            queryStmt = connection.createStatement();
            rs = queryStmt.executeQuery(query);
            while (rs.next()) {
                seqValue = rs.getLong("seqValue");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            Postgre.releaseConnection(connection, queryStmt, rs);
        }
        return seqValue;
    }
    
    public SurveyBO setBO(final RequestContext context) {
        SurveyBO surveyBO = new SurveyBO();
        surveyBO.setAction(context.getParameterString("surveyAction"));
        logger.info("surveyAction : " + surveyBO.getAction());
        surveyBO.setLang(context.getParameterString("locale", "en"));
        logger.info("lang : " + surveyBO.getLang());
        surveyBO.setUserId(context.getParameterString("user_id"));
        logger.info("userId : " + surveyBO.getUserId());
        surveyBO.setIpAddress(context.getRequest().getRemoteAddr());
        logger.info("ipAddress : " + surveyBO.getIpAddress());
        surveyBO.setUserAgent(
                context.getRequest().getHeader("User-Agent"));
        logger.info("userAgent : " + surveyBO.getUserAgent());
        surveyBO.setTakenFrom(
                context.getParameterString("surveyTakenfrom"));
        logger.info("surveyTakenfrom : " + surveyBO.getTakenFrom());
        surveyBO.setSurveyId(context.getParameterString("surveyId"));
        logger.info("surveyId : " + surveyBO.getSurveyId());
        surveyBO.setTotalQuestions(
                context.getParameterString("totalQuestions"));
        logger.info("totalQuestions : " + surveyBO.getTotalQuestions());
        surveyBO.setCaptchaResponse(
                context.getParameterString("g-recaptcha-response"));
        logger.info("captchaResponse : " + surveyBO.getCaptchaResponse());
        surveyBO.setGroup(context.getParameterString("SurveyGroup"));
        logger.info("surveyGroup : " + surveyBO.getGroup());
        return surveyBO;
    }

}
