package com.hukoomi.livesite.external;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import com.hukoomi.bo.SurveyBO;
import com.hukoomi.utils.GoogleRecaptchaUtil;
import com.hukoomi.utils.Postgre;
import com.hukoomi.utils.PropertiesFileReader;
import com.hukoomi.utils.RequestHeaderUtils;
import com.interwoven.livesite.runtime.RequestContext;

/**
 * SurveyExternal is the components external class.
 * 
 * @author Vijayaragavamoorthy
 *
 */
public class SurveyExternal {
    /** Logger object to check the flow of the code. */
    private final Logger logger = Logger.getLogger(SurveyExternal.class);
    /**
     * Postgre Object variable.
     */
    Postgre postgre = null;

    /**
     * This method will be called from Component External to insert Survey form
     * data.
     * 
     * @param context Request context object.
     *
     * @return doc Returns the document by adding status about insert operation in
     *         database.
     * 
     * @deprecated
     */
    @Deprecated(since = "", forRemoval = false)
    public Document submitSurvey(final RequestContext context) {
        logger.info("SurveyExternal : submitSurvey");

        postgre = new Postgre(context);
        DetailExternal detailExt = new DetailExternal();
        SurveyBO surveyBO = setBO(context);
        logger.info("SurveyBO : " + surveyBO);

        Document document = DocumentHelper.createDocument();
        if (surveyBO.getAction() != null
                && !"".equals(surveyBO.getAction())) {
            GoogleRecaptchaUtil captchUtil = new GoogleRecaptchaUtil();
            if ("submit".equalsIgnoreCase(surveyBO.getAction())) {
                Element surveyResponseElem = document
                        .addElement("SurveyResponse");
                Element surveyStatusElem = surveyResponseElem
                        .addElement("Status");
                if (captchUtil.validateCaptcha(context,
                        surveyBO.getCaptchaResponse())) {
                    logger.info("Google Recaptcha is valid");
                    boolean insertSurveyResponse = insertSurveyResponse(
                            surveyBO, context);
                    logger.info("insertSurveyResponse : "
                            + insertSurveyResponse);
                    if (insertSurveyResponse) {
                        surveyStatusElem.setText("Success");
                    } else {
                        surveyStatusElem.setText("Failed");
                    }
                } else {
                    logger.info("Google Recaptcha is not valid");
                }
            } else if ("detail".equalsIgnoreCase(surveyBO.getAction())) {
                logger.info("SurveyExternal : Loading Properties....");
                PropertiesFileReader propertyFileReader = new PropertiesFileReader(
                        context, "captchaconfig.properties");
                Properties properties = propertyFileReader.getPropertiesFile();
                logger.info("SurveyExternal : Properties Loaded");
                String siteKey = properties.getProperty("siteKey");
                logger.info("siteKey : " + siteKey);
                document = detailExt.getContentDetail(context);
                document.getRootElement().addAttribute("Sitekey", siteKey);
            }
            logger.info("Final Result :" + document.asXML());
        } else {
            logger.info("Error : Survey Action not available");
        }
        return document;
    }

    /**
     * This method is used to insert Survey form data in database.
     * 
     * @param surveyBO SurveyBO object.
     * @param context  Request Context object.
     * 
     * @return Returns boolean status about data insert operation.
     * 
     * @deprecated
     */
    @Deprecated(since = "", forRemoval = false)
    private boolean insertSurveyResponse(SurveyBO surveyBO,
            RequestContext context) {
        logger.info("SurveyExternal : insertSurveyResponse");

        PreparedStatement surveyprepareStatement = null;
        PreparedStatement answersprepareStatement = null;
        Connection connection = null;
        ResultSet rs = null;
        int totalCount = 0;
        boolean isSurveyInserted = false;

        try {

            totalCount = getIntValue(surveyBO.getTotalQuestions());
            logger.info("totalCount : " + totalCount);

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
                logger.info("Survey Response Inserted : " + result);

                String surveyAnswerQuery = "INSERT INTO SURVEY_ANSWERS "
                        + "(ANSWER_ID, RESPONSE_ID, SURVEY_ID, LANG, "
                        + "QUESTION_NO, ANSWER) VALUES(?, ?, ?, ?, ?, ?)";
                answersprepareStatement = connection
                        .prepareStatement(surveyAnswerQuery);
                int addedToBatch = addAnswerstoBatch(totalCount, context,
                        answersprepareStatement, responseId, surveyBO);
                logger.info("responseId : " + responseId);

                int[] answerBatch = answersprepareStatement.executeBatch();
                logger.info(
                        "Total answers inserted : " + answerBatch.length);

                if (answerBatch.length == addedToBatch) {
                    connection.commit();
                    logger.info("Survey Answer Inserted");
                    isSurveyInserted = true;
                } else {
                    logger.info("Failed to insert Survey Answer");
                    connection.rollback();
                }
            } else {
                logger.info("Survey Response not Inserted : ");
                connection.rollback();
            }
        } catch (Exception e) {
            logger.error("Exception in insertSurveyResponse", e);
        } finally {
            postgre.releaseConnection(null, answersprepareStatement, null);
            postgre.releaseConnection(connection, surveyprepareStatement,
                    rs);
        }

        return isSurveyInserted;
    }

    /**
     * This method is used to add the answers to the batch for database batch
     * execution for inserting the survey answers.
     * 
     * @param totalCount              Total number of answers.
     * @param context                 Request Context object.
     * @param answersprepareStatement Answer query prepared statement.
     * @param responseId              Response Id from the survey resposne table.
     * @param surveyBO                SurveyBO object.
     * 
     * @return Returns number of answers added the batch.
     * 
     * @deprecated
     */
    @Deprecated(since = "", forRemoval = false)
    private int addAnswerstoBatch(int totalCount,
            final RequestContext context,
            PreparedStatement answersprepareStatement, Long responseId,
            SurveyBO surveyBO) throws SQLException {
        logger.info("SurveyExternal : addAnswerstoBatch");
        int numOfAnswersAdded = 0;
        for (int i = 1; i <= totalCount; i++) {
            String value = context.getParameterString(String.valueOf(i));
            logger.info("Answer for question " + i + " : " + value);

            if (value != null && value.contains("#$#")) {
                String[] multipleOption = value.split("#\\$#");
                logger.info(
                        "Multiple Option Answer" + multipleOption.length);
                for (String mutiOptValue : multipleOption) {
                    Long answerId = getNextSequenceValue(
                            "survey_answers_answer_id_seq",
                            postgre.getConnection());
                    logger.info("answerId : " + answerId);
                    logger.info("mutiOptValue : " + mutiOptValue);

                    answersprepareStatement.setLong(1, answerId);
                    answersprepareStatement.setLong(2, responseId);
                    answersprepareStatement.setLong(3,
                            Long.parseLong(surveyBO.getSurveyId()));
                    answersprepareStatement.setString(4,
                            surveyBO.getLang());
                    answersprepareStatement.setInt(5, i);
                    answersprepareStatement.setString(6, mutiOptValue);
                    answersprepareStatement.addBatch();
                    numOfAnswersAdded++;
                }

            } else {
                Long answerId = getNextSequenceValue(
                        "survey_answers_answer_id_seq",
                        postgre.getConnection());
                logger.info("answerId : " + answerId);

                answersprepareStatement.setLong(1, answerId);
                answersprepareStatement.setLong(2, responseId);
                answersprepareStatement.setLong(3,
                        Long.parseLong(surveyBO.getSurveyId()));
                answersprepareStatement.setString(4, surveyBO.getLang());
                answersprepareStatement.setInt(5, i);
                answersprepareStatement.setString(6, value);
                answersprepareStatement.addBatch();
                numOfAnswersAdded++;
            }
        }
        return numOfAnswersAdded;
    }

    /**
     * This method is used to get int value.
     * 
     * @param inputStringValue Input String value.
     * 
     * @return Returns int.
     */
    private int getIntValue(String inputStringValue) {
        int intValue = 0;
        if (inputStringValue != null && !"".equals(inputStringValue)) {
            intValue = Integer.parseInt(inputStringValue);
        }
        return intValue;
    }

    /**
     * This methods is used to get sequence value.
     * 
     * @param sequenceName Sequence name.
     * @param connection   Connection object.
     * 
     * @return Returns sequence value.
     */
    private Long getNextSequenceValue(String sequenceName,
            Connection connection) {
        Long seqValue = 0L;
        PreparedStatement queryStmt = null;
        ResultSet rs = null;
        try {
            String query = "SELECT nextval('" + sequenceName
                    + "') as seqValue";
            queryStmt = connection.prepareStatement(query);
            rs = queryStmt.executeQuery();
            while (rs.next()) {
                seqValue = rs.getLong("seqValue");
            }
        } catch (Exception e) {
            logger.error("Exception in getNextSequenceValue", e);
        } finally {
            postgre.releaseConnection(connection, queryStmt, rs);
        }
        return seqValue;
    }

    /**
     * This method is used to set value to SurveyBO object.
     * 
     * @param context Request Context Object.
     * 
     * @return Returns SurveyBO Object.
     * 
     * @deprecated
     */
    @Deprecated(since = "", forRemoval = false)
    public SurveyBO setBO(final RequestContext context) {
        SurveyBO surveyBO = new SurveyBO();
        RequestHeaderUtils requestHeaderUtils = new RequestHeaderUtils(context);
        surveyBO.setAction(context.getParameterString("surveyAction"));
        surveyBO.setLang(context.getParameterString("locale", "en"));
        surveyBO.setUserId(context.getParameterString("user_id"));
        surveyBO.setIpAddress(requestHeaderUtils.getClientIpAddress());
        surveyBO.setUserAgent(
                context.getRequest().getHeader("User-Agent"));
        surveyBO.setTakenFrom(
                context.getParameterString("surveyTakenfrom"));
        surveyBO.setSurveyId(context.getParameterString("surveyId"));
        surveyBO.setTotalQuestions(
                context.getParameterString("totalQuestions"));
        surveyBO.setCaptchaResponse(
                context.getParameterString("g-recaptcha-response"));
        surveyBO.setGroup(context.getParameterString("SurveyGroup"));
        surveyBO.setGroupCategory(
                context.getParameterString("surveyGroupCategory"));
        surveyBO.setCategory(context.getParameterString("surveyCategory"));
        surveyBO.setSolrCategory(
                context.getParameterString("solrSurveyCategory"));
        return surveyBO;
    }

}
