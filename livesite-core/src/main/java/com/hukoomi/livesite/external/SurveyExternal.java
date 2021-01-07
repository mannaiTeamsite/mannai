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
import com.hukoomi.utils.Validator;
import com.hukoomi.utils.XssUtils;
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
     * ValidationUtils object.
     */
    Validator validate = new Validator();

    /**
     * Success Status variable.
     */
    private static final String SUCCESS = "Success";

    /**
     * Failed Status variable.
     */
    private static final String FAILED = "Failed";

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
        SurveyBO surveyBO =  new SurveyBO();
        boolean isInputValid = setBO(context, surveyBO);
        logger.info("SurveyBO : " + surveyBO);

        Document document = DocumentHelper.createDocument();
        Element surveyResponseElem = document
                .addElement("SurveyResponse");
        Element surveyStatusElem = surveyResponseElem
                .addElement("Status");
        if (isInputValid) {
            if (surveyBO.getAction() != null
                    && !"".equals(surveyBO.getAction())) {
                GoogleRecaptchaUtil captchUtil = new GoogleRecaptchaUtil();
                if ("submit".equalsIgnoreCase(surveyBO.getAction())) {
                    
                    if (captchUtil.validateCaptcha(context,
                            surveyBO.getCaptchaResponse())) {
                        logger.info("Google Recaptcha is valid");
                        boolean insertSurveyResponse = insertSurveyResponse(
                                surveyBO, context);
                        logger.info("insertSurveyResponse : "
                                + insertSurveyResponse);
                        if (insertSurveyResponse) {
                            surveyStatusElem.setText(SUCCESS);
                        } else {
                            surveyStatusElem.setText(FAILED);
                        }
                    } else {
                        logger.info("Google Recaptcha is not valid");
                        surveyStatusElem.setText(FAILED);
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
    } else {
        logger.info("Input is not valid");
        surveyStatusElem.setText(FAILED);
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

                boolean isAdded = addAnswerstoBatch(totalCount, context,
                        answersprepareStatement, responseId, surveyBO);
                logger.info("isAdded : " + isAdded);
                if(isAdded) {
                    logger.info("responseId : " + responseId);
                    
                    //Number of items added to the answers batch is set to the un-used qustionNo field.
                    int addedToBatch = surveyBO.getQuestionNo();
                    logger.info("addedToBatch : " + addedToBatch);
    
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
                }else {
                    logger.info("Survey answer is not created.");
                    connection.rollback();
                }
            } else {
                logger.info("Survey Response not Inserted.");
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
     * @return Returns true if all the answers are added to the batch else returns false
     * 
     * @deprecated
     */
    @Deprecated(since = "", forRemoval = false)
    private boolean addAnswerstoBatch(int totalCount,
            final RequestContext context,
            PreparedStatement answersprepareStatement, Long responseId,
            SurveyBO surveyBO) throws SQLException {
        logger.info("SurveyExternal : addAnswerstoBatch");
        XssUtils xssUtils = new XssUtils();
        int numOfAnswersAdded = 0;
        boolean isAdded = true;
        for (int i = 1; i <= totalCount; i++) {
            String value = context.getParameterString(String.valueOf(i));
            logger.info("value >>>"+value+"<<<" );
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
                    answersprepareStatement.setString(6, xssUtils.stripXSS(mutiOptValue));
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
                answersprepareStatement.setString(6, xssUtils.stripXSS(value));
                answersprepareStatement.addBatch();
                numOfAnswersAdded++;
            }
            surveyBO.setQuestionNo(numOfAnswersAdded);
        }
        return isAdded;
    }
    
    
    private boolean validateAnswer(String value) {
        boolean isValid = false;
        
        if (value != null && value.contains("#$#")) {
            String[] multipleOption = value.split("#\\$#");
            for (String mutiOptValue : multipleOption) {
                isValid = validate.isValidPattern(mutiOptValue, Validator.TEXT);
                if(!isValid) break;
            }
        }else {
            isValid = validate.isValidPattern(value, Validator.TEXT);
        }
        return isValid;
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
    public boolean setBO(final RequestContext context, SurveyBO surveyBO) {

        final String SURVEY_ACTION = "surveyAction";
        final String LOCALE = "locale";
        final String USER_ID = "user_id";
        final String USER_AGENT = "User-Agent";
        final String SURVEY_TAKEN_FROM = "surveyTakenfrom";
        final String SURVEY_ID = "surveyId";
        final String TOTAL_QUESTIONS = "totalQuestions";
        final String SURVEY_GROUP = "SurveyGroup";
        final String SURVEY_CATEGORY = "surveyCategory";
        final String SURVEY_GROUP_CATEGORY = "surveyGroupCategory";
        final String SOLR_SURVEY_CATEGORY = "solrSurveyCategory";

        RequestHeaderUtils requestHeaderUtils = new RequestHeaderUtils(context);
        logger.info("surveyAction >>>"
                + context.getParameterString(SURVEY_ACTION) + "<<<");
        if (!validate
                .checkNull(context.getParameterString(SURVEY_ACTION))) {
            if (validate.isValidPattern(
                    context.getParameterString(SURVEY_ACTION),
                    Validator.ALPHABET)) {
                surveyBO.setAction(
                        context.getParameterString(SURVEY_ACTION));
            }else {
                return false;
            }
        }
        
        logger.info(
                "locale >>>" + context.getParameterString(LOCALE) + "<<<");
        if (!validate.checkNull(context.getParameterString(LOCALE))) {
            if (validate.isValidPattern(context.getParameterString(LOCALE),
                    Validator.ALPHABET)) {
                surveyBO.setLang(context.getParameterString(LOCALE, "en"));
            }else {
                return false;
            }
        }
        
        logger.info("user_id >>>" + context.getParameterString(USER_ID)
                + "<<<");
        if (!validate.checkNull(context.getParameterString(USER_ID))) {
            if (validate.isValidPattern(
                    context.getParameterString(USER_ID),
                    Validator.USER_ID)) {
                surveyBO.setUserId(context.getParameterString(USER_ID));
            }else {
                return false;
            }
        }
        
        logger.info("ipaddress >>>" +requestHeaderUtils.getClientIpAddress()+"<<<" );
        if(!validate.checkNull(requestHeaderUtils.getClientIpAddress())) {
            if(validate.isValidPattern(requestHeaderUtils.getClientIpAddress(), Validator.IP_ADDRESS)) {
                surveyBO.setIpAddress(requestHeaderUtils.getClientIpAddress());
            }else {
                return false;
            }
        }
        
        surveyBO.setUserAgent(context.getRequest().getHeader(USER_AGENT));
        
        logger.info("surveyTakenfrom >>>"
                + context.getParameterString(SURVEY_TAKEN_FROM) + "<<<");
        if (!validate.checkNull(
                context.getParameterString(SURVEY_TAKEN_FROM))) {
            if (validate.isValidPattern(
                    context.getParameterString(SURVEY_TAKEN_FROM),
                    Validator.ALPHABET)) {
                surveyBO.setTakenFrom(
                        context.getParameterString(SURVEY_TAKEN_FROM));
            }else {
                return false;
            }
        }

        logger.info("surveyId >>>" + context.getParameterString(SURVEY_ID)
                + "<<<");
        if (!validate.checkNull(context.getParameterString(SURVEY_ID))) {
            if (validate.isValidPattern(
                    context.getParameterString(SURVEY_ID),
                    Validator.NUMERIC)) {
                surveyBO.setSurveyId(
                        context.getParameterString(SURVEY_ID));
            }else {
                return false;
            }
        }
        
        logger.info("totalQuestions >>>"
                + context.getParameterString(TOTAL_QUESTIONS) + "<<<");
        if (!validate
                .checkNull(context.getParameterString(TOTAL_QUESTIONS))) {
            if (validate.isValidPattern(
                    context.getParameterString(TOTAL_QUESTIONS),
                    Validator.NUMERIC)) {
                surveyBO.setTotalQuestions(
                        context.getParameterString(TOTAL_QUESTIONS));
            }else {
                return false;
            }
        }

        surveyBO.setCaptchaResponse(
                context.getParameterString("g-recaptcha-response"));
        
        if (!validate
                .checkNull(context.getParameterString(SURVEY_GROUP))) {
            surveyBO.setGroup(getContentName(
                    context.getParameterString(SURVEY_GROUP)));
        }

        logger.info("surveyGroupCategory >>>"
                + context.getParameterString(SURVEY_GROUP_CATEGORY)
                + "<<<");
        if (!validate.checkNull(
                context.getParameterString(SURVEY_GROUP_CATEGORY))) {
            if (validate.isValidPattern(
                    context.getParameterString(SURVEY_GROUP_CATEGORY),
                    Validator.ALPHABET_HYPEN)) {
                surveyBO.setGroupCategory(
                        context.getParameterString(SURVEY_GROUP_CATEGORY));
            }else {
                return false;
            }
        }
        
        logger.info("surveyCategory >>>"
                + context.getParameterString(SURVEY_CATEGORY) + "<<<");
        if (!validate
                .checkNull(context.getParameterString(SURVEY_CATEGORY))) {
            if (validate.isValidPattern(
                    context.getParameterString(SURVEY_CATEGORY),
                    Validator.ALPHABET)) {
                surveyBO.setCategory(
                        context.getParameterString(SURVEY_CATEGORY));
            }else {
                return false;
            }
        }
        
        logger.info("solrSurveyCategory >>>"
                + context.getParameterString(SOLR_SURVEY_CATEGORY)
                + "<<<");
        if (!validate.checkNull(
                context.getParameterString(SOLR_SURVEY_CATEGORY))) {
            if (validate.isValidPattern(
                    context.getParameterString(SOLR_SURVEY_CATEGORY),
                    Validator.ALPHABET)) {
                surveyBO.setSolrCategory(
                        context.getParameterString(SOLR_SURVEY_CATEGORY));
            }else {
                return false;
            }
        }
        return true;
    }
    
    /**
     * This method is used to get Content name.
     * 
     * @param context Request Context object.
     * 
     * @return Returns Content name.
     */
    public String getContentName(String contentPath) {
        String[] contentPathArr = contentPath.split("/");
        return contentPathArr[contentPathArr.length - 1];
    }
}
