package com.hukoomi.livesite.external;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;
import java.util.StringJoiner;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import org.dom4j.Node;

import org.owasp.esapi.ESAPI;
import org.owasp.esapi.ValidationErrorList;


import com.hukoomi.bo.SurveyBO;
import com.hukoomi.utils.ESAPIValidator;
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
     * Success Status constant.
     */
    private static final String SUCCESS = "Success";

    /**
     * Failed Status constant.
     */
    private static final String FAILED = "Failed";
    
    /**
     * Constant for action polls and survey.
     */
    public static final String ACTION_POLLS_AND_SURVEY = "pollsandsurvey";
    /**
     * Constant for action survey listing.
     */
    public static final String ACTION_SURVEY_LISTING = "listing";
    /**
     * Constant for action survey detail.
     */
    public static final String ACTION_SURVEY_DETAIL = "detail";
    /**
     * Constant for action survey submit.
     */
    public static final String ACTION_SUBIT = "submit";

    /**
     * Submitted Status constant.
     */
    private static final String SUBMITTED = "Submitted";

    /**
     * Survey BO Object.
     */
    SurveyBO surveyBO = null;

    /**
     * Constant for BIGINT.
     */
    public static final String BIGINT = "BIGINT";

    /**
     * Constant for Survey Id.
     */
    public static final String SURVEYID = "SURVEY_ID";

    /**
     * This method will be called from Component External to insert Survey form
     * data.
     * 
     * @param context
     *                Request context object.
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
        surveyBO = new SurveyBO();
        boolean isInputValid = setBO(context, surveyBO);
        logger.info("SurveyBO : " + surveyBO);

        Document document = DocumentHelper.createDocument();
        Element surveyResponseElem = document.addElement("SurveyResponse");
        Element surveyStatusElem = surveyResponseElem.addElement("Status");
        if (isInputValid) {
            if (surveyBO.getAction() != null
                    && !"".equals(surveyBO.getAction())) {
                GoogleRecaptchaUtil captchUtil = new GoogleRecaptchaUtil();
                if ("submit".equalsIgnoreCase(surveyBO.getAction())) {
                    String status = getSubmissionDatabaseStatus(
                            surveyBO.getSurveyId(), postgre, surveyBO);

                    if (StringUtils.isEmpty(status)) {
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
                    } else {
                        surveyStatusElem.setText(SUBMITTED);
                    }
                } else if ("detail"
                        .equalsIgnoreCase(surveyBO.getAction())) {
                    logger.info("SurveyExternal : Loading Properties....");
                    PropertiesFileReader propertyFileReader = new PropertiesFileReader(
                            context, "captchaconfig.properties");
                    Properties properties = propertyFileReader
                            .getPropertiesFile();
                    logger.info("SurveyExternal : Properties Loaded");
                    String siteKey = properties.getProperty("siteKey");
                    logger.info("siteKey : " + siteKey);
                    document = detailExt.getContentDetail(context);

                    String surveyId = document
                            .selectSingleNode(
                                    "/content/root/information/id")
                            .getText();
                    logger.info("Detail External Survey Id : " + surveyId);
                    String submittedSurveyId = null;
                    if (StringUtils.isNotBlank(surveyId)) {
                        submittedSurveyId = getSubmissionDatabaseStatus(
                                surveyId, postgre, surveyBO);
                        logger.info("Submitted Survey Id : "
                                + submittedSurveyId);
                    }

                    if (StringUtils.isNotEmpty(submittedSurveyId)) {
                        document.getRootElement().addAttribute("Status",
                                SUBMITTED);
                    }
                    document.getRootElement().addAttribute("Sitekey",
                            siteKey);
                } else if ("listing"
                        .equalsIgnoreCase(surveyBO.getAction())) {
                    document = checkSubmissionStatus(context);
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
     * @param context
     * @return document
     */
    private Document checkSubmissionStatus(RequestContext context) {
        logger.info("checkSubmissionStatus()");
        Document document = null;

        HukoomiExternal hukoomiExternal = new HukoomiExternal();

        // Getting Survey Solr Document
        document = hukoomiExternal.getLandingContent(context);

        // Extracting Survey Ids from Survey Document
        String surveyIds = getSurveyIdsFromDoc(document);

        logger.info("SurveyIds from doc : " + surveyIds);

        // Checking for already submitted Surveys
        String submittedSurveyIds = getSubmissionDatabaseStatus(surveyIds,
                postgre, surveyBO);

        // Add Status code to document
        if (StringUtils.isNotBlank(submittedSurveyIds)) {
            document = addStatusToXml(document, submittedSurveyIds);
        }

        return document;
    }

    /**
     * @param document
     * @param submittedSurveyIds
     * @return
     */
    @SuppressWarnings("unchecked")
    public Document addStatusToXml(Document document,
            String submittedSurveyIds) {
        logger.info("addStatusToXml()");
        List<Node> nodes = document
                .selectNodes("/SolrResponse/response/docs");

        for (Node node : nodes) {
            String surveyId = node.selectSingleNode("id").getText();
            Element status = (Element) node;
            if (submittedSurveyIds.contains(surveyId)) {
                status.addAttribute("status", "submitted");
            }
        }

        return document;
    }

    /**
     * This method is used for checking Survey submission data in database.
     * 
     * @param surveyIds
     *                  Comma seprated Survey Ids
     * @param postgre
     *                  Postgre Object.
     * 
     * @return Returns comma seperated string containing already submitted Survey
     *         ids.
     */
    public String getSubmissionDatabaseStatus(String surveyIds,
            Postgre postgre, SurveyBO surveyBO) {
        logger.info("getSubmissionDatabaseStatus()");

        StringBuilder checkSubmittedSurveyQuery = new StringBuilder(
                "SELECT DISTINCT SR.SURVEY_ID FROM SURVEY_RESPONSE SR,SURVEY_MASTER SM WHERE SM.SURVEY_ID = SR.SURVEY_ID AND SR.SURVEY_ID = ANY (?) AND SM.SUBMIT_TYPE='Single'");
        StringJoiner submittedSurveyIds = new StringJoiner(",");

        String[] surveyIdsArr = surveyIds.split(",");

        if (surveyBO.getUserId() != null
                && !"".equals(surveyBO.getUserId())) {
            checkSubmittedSurveyQuery.append("AND USER_ID = ? ");
        } else if (surveyBO.getIpAddress() != null
                && !"".equals(surveyBO.getIpAddress())) {
            checkSubmittedSurveyQuery.append("AND IP_ADDRESS = ? ");
        }
        logger.info("checkSubmittedSurveyQuery ::"
                + checkSubmittedSurveyQuery.toString());
        Connection connection = null;
        PreparedStatement prepareStatement = null;
        ResultSet rs = null;

        try {
            connection = postgre.getConnection();
            prepareStatement = connection.prepareStatement(
                    checkSubmittedSurveyQuery.toString());
            prepareStatement.setArray(1,
                    connection.createArrayOf(BIGINT, surveyIdsArr));
            if (surveyBO.getUserId() != null
                    && !"".equals(surveyBO.getUserId())) {
                prepareStatement.setString(2, surveyBO.getUserId());
            } else if (surveyBO.getIpAddress() != null
                    && !"".equals(surveyBO.getIpAddress())) {
                prepareStatement.setString(2, surveyBO.getIpAddress());
            }
            rs = prepareStatement.executeQuery();
            while (rs.next()) {
                submittedSurveyIds.add(rs.getString(SURVEYID));
            }
            logger.info("Submitted Surveys : "
                    + submittedSurveyIds.toString());

        } catch (Exception e) {
            logger.error("Exception in getSubmissionDatabaseStatus", e);
        } finally {
            postgre.releaseConnection(connection, prepareStatement, rs);
        }
        return submittedSurveyIds.toString();
    }

    /**
     * This method is used to extract and create Survey Ids' comma seprated string.
     *
     * @param doc
     *            Document Object.
     *
     * @return Comma Seprated String containing Survey Ids.
     */
    @SuppressWarnings("unchecked")
    public String getSurveyIdsFromDoc(Document doc) {
        logger.info("getSurveyIdsFromDoc()");

        List<Node> nodes = doc.selectNodes("/SolrResponse/response/docs");
        StringJoiner joiner = new StringJoiner(",");

        for (Node node : nodes) {
            joiner.add(node.selectSingleNode("id").getText());
        }
        return joiner.toString();
    }

    /**
     * This method is used to insert Survey form data in database.
     * 
     * @param surveyBO
     *                 SurveyBO object.
     * @param context
     *                 Request Context object.
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
                if (isAdded) {
                    logger.info("responseId : " + responseId);

                    // Number of items added to the answers batch is set to the un-used qustionNo
                    // field.
                    int addedToBatch = surveyBO.getQuestionNo();
                    logger.info("addedToBatch : " + addedToBatch);

                    int[] answerBatch = answersprepareStatement
                            .executeBatch();
                    logger.info("Total answers inserted : "
                            + answerBatch.length);

                    if (answerBatch.length == addedToBatch) {
                        connection.commit();
                        logger.info("Survey Answer Inserted");
                        isSurveyInserted = true;
                    } else {
                        logger.info("Failed to insert Survey Answer");
                        connection.rollback();
                    }
                } else {
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
     * @param totalCount
     *                                Total number of answers.
     * @param context
     *                                Request Context object.
     * @param answersprepareStatement
     *                                Answer query prepared statement.
     * @param responseId
     *                                Response Id from the survey resposne table.
     * @param surveyBO
     *                                SurveyBO object.
     * 
     * @return Returns true if all the answers are added to the batch else returns
     *         false
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
            logger.info("value >>>" + value + "<<<");
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
                    answersprepareStatement.setString(6,
                            xssUtils.stripXSS(mutiOptValue));
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
                answersprepareStatement.setString(6,
                        xssUtils.stripXSS(value));
                answersprepareStatement.addBatch();
                numOfAnswersAdded++;
            }
            surveyBO.setQuestionNo(numOfAnswersAdded);
        }
        return isAdded;
    }



    /**
     * This method is used to get int value.
     * 
     * @param inputStringValue
     *                         Input String value.
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
     * @param sequenceName
     *                     Sequence name.
     * @param connection
     *                     Connection object.
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

        final String POLL_ACTION = "pollAction";
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
        ValidationErrorList errorList = new ValidationErrorList();
        String validData  = "";
        
        //TODO: Field length needs to be validated against content model and database. 
        
        String surveyAction = context.getParameterString(SURVEY_ACTION);
        logger.info(SURVEY_ACTION + " >>>"+surveyAction+"<<<");
        if (!ESAPIValidator.checkNull(surveyAction)) {
            validData  = ESAPI.validator().getValidInput(SURVEY_ACTION, surveyAction, ESAPIValidator.ALPHABET, 20, false, true, errorList);
            if(errorList.isEmpty()) {
                surveyBO.setAction(validData);
            }else {
                logger.info(errorList.getError(SURVEY_ACTION));
                return false;
            }
        }
        
        String locale = context.getParameterString(LOCALE, "en");
        logger.info(LOCALE + " >>>"+locale+"<<<");
        validData  = ESAPI.validator().getValidInput(LOCALE, locale, ESAPIValidator.ALPHABET, 2, false, true, errorList);
        if(errorList.isEmpty()) {
            surveyBO.setLang(validData);
        }else {
            logger.info(errorList.getError(LOCALE));
            return false;
        }
        
        String userId = context.getParameterString(USER_ID);
        logger.info(USER_ID + " >>>"+userId+"<<<");
        validData  = ESAPI.validator().getValidInput(USER_ID, userId, ESAPIValidator.USER_ID, 50, true, true, errorList);
        if(errorList.isEmpty()) {
            surveyBO.setUserId(validData);
        }else {
            logger.info(errorList.getError(USER_ID));
            return false;
        }
        
        String ipAddress = requestHeaderUtils.getClientIpAddress();
        logger.info("ipaddress >>>" +ipAddress+"<<<");
        validData  = ESAPI.validator().getValidInput("ipaddress", ipAddress, ESAPIValidator.IP_ADDRESS, 20, false, true, errorList);
        if(errorList.isEmpty()) {
            surveyBO.setIpAddress(validData);
        }else {
            logger.info(errorList.getError("ipaddress"));
            return false;
        }
        
        surveyBO.setUserAgent(context.getRequest().getHeader(USER_AGENT));
        
        if(ACTION_SUBIT.equalsIgnoreCase(surveyAction)) {
            
            String surveyTakenFrom = context.getParameterString(SURVEY_TAKEN_FROM);
            logger.info(SURVEY_TAKEN_FROM + " >>>"+surveyTakenFrom+"<<<");
            validData  = ESAPI.validator().getValidInput(SURVEY_TAKEN_FROM, surveyTakenFrom, ESAPIValidator.ALPHABET, 150, false, true, errorList);
            if(errorList.isEmpty()) {
                surveyBO.setTakenFrom(validData);
            }else {
                logger.info(errorList.getError(USER_AGENT));
                return false;
            }
            
            String surveyId = context.getParameterString(SURVEY_ID);
            logger.info(SURVEY_ID + " >>>"+surveyId+"<<<");
            validData  = ESAPI.validator().getValidInput(SURVEY_ID, surveyId, ESAPIValidator.NUMERIC, 200, false, true, errorList);
            if(errorList.isEmpty()) {
                surveyBO.setSurveyId(validData);
            }else {
                logger.info(errorList.getError(SURVEY_ID));
                return false;
            }
            
            String totalQuestions = context.getParameterString(TOTAL_QUESTIONS);
            logger.info(TOTAL_QUESTIONS + " >>>"+totalQuestions+"<<<");
            validData  = ESAPI.validator().getValidInput(TOTAL_QUESTIONS, totalQuestions, ESAPIValidator.NUMERIC, 50, false, true, errorList);
            if(errorList.isEmpty()) {
                surveyBO.setTotalQuestions(validData);
            }else {
                logger.info(errorList.getError(TOTAL_QUESTIONS));
                return false;
            }
            
            surveyBO.setCaptchaResponse(
                    context.getParameterString("g-recaptcha-response"));
            
        }
        
        String pollAction = context.getParameterString(POLL_ACTION);
        logger.info(POLL_ACTION + " >>>" +pollAction+ "<<<");   
        if(ACTION_POLLS_AND_SURVEY.equalsIgnoreCase(pollAction)) {
            
            String surveyGroup = context.getParameterString(SURVEY_GROUP);
            logger.info(SURVEY_GROUP + " >>>" +surveyGroup+ "<<<");
            if (!ESAPIValidator.checkNull(surveyGroup)) {
                surveyBO.setGroup(getContentName(surveyGroup));
            }
            
            String surveyGroupCategory = context.getParameterString(SURVEY_GROUP_CATEGORY);
            logger.info(SURVEY_GROUP_CATEGORY + " >>>"+surveyGroupCategory+"<<<");
            validData  = ESAPI.validator().getValidInput(SURVEY_GROUP_CATEGORY, surveyGroupCategory, ESAPIValidator.ALPHABET_HYPEN, 50, false, true, errorList);
            if(errorList.isEmpty()) {
                surveyBO.setGroupCategory(validData);
            }else {
                logger.info(errorList.getError(SURVEY_GROUP_CATEGORY));
                return false;
            }
            
            String surveyCategory = context.getParameterString(SURVEY_CATEGORY);
            logger.info(SURVEY_CATEGORY + " >>>"+surveyCategory+"<<<");
            validData  = ESAPI.validator().getValidInput(SURVEY_CATEGORY, surveyCategory, ESAPIValidator.ALPHABET, 50, false, true, errorList);
            if(errorList.isEmpty()) {
                surveyBO.setCategory(validData);
            }else {
                logger.info(errorList.getError(SURVEY_CATEGORY));
                return false;
            }
            
            String solrSurveyCategory = context.getParameterString(SOLR_SURVEY_CATEGORY);
            logger.info(SOLR_SURVEY_CATEGORY + " >>>"+solrSurveyCategory+"<<<");
            validData  = ESAPI.validator().getValidInput(SOLR_SURVEY_CATEGORY, solrSurveyCategory, ESAPIValidator.ALPHABET, 50, false, true, errorList);
            if(errorList.isEmpty()) {
                surveyBO.setSolrCategory(validData);
            }else {
                logger.info(errorList.getError(SOLR_SURVEY_CATEGORY));
                return false;
            }
            
        }
        
        return true;
    }

    /**
     * This method is used to get Content name.
     * 
     * @param context
     *                Request Context object.
     * 
     * @return Returns Content name.
     */
    public String getContentName(String contentPath) {
        String[] contentPathArr = contentPath.split("/");
        return contentPathArr[contentPathArr.length - 1];
    }
}
