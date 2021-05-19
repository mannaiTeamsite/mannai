package com.hukoomi.livesite.external;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringJoiner;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

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
    public static final String ACTION_SURVEY_SUBIT = "submit";
    
    /**
     * Constant for action survey submit.
     */
    public static final String ACTION_DYNAMIC_SURVEY_SUBIT = "dynamicsurveysubmit";


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
     * Constant for Survey Master Id.
     */
    public static final String SURVEY_MASTER_ID = "SURVEY_MASTER_ID";
    /**
     * Constant for Survey Question Id.
     */
    public static final String SURVEY_QUESTION_ID = "SURVEY_QUESTION_ID";
    /**
     * Solr Categroy of survey.
     */
    public static final String CATEGORY_SURVEY = "survey";
    
    /**
     * Solr Categroy of dynamic survey.
     */
    public static final String CATEGORY_DYNAMIC_SURVEY = "dynamic-survey";
    

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
        logger.debug("SurveyBO : " + surveyBO);

        Document document = DocumentHelper.createDocument();
        Element surveyResponseElem = document.addElement("SurveyResponse");
        Element surveyStatusElem = surveyResponseElem.addElement("Status");
        if (isInputValid) {
            if (surveyBO.getAction() != null
                    && !"".equals(surveyBO.getAction())) {
                GoogleRecaptchaUtil captchUtil = new GoogleRecaptchaUtil();
                if (ACTION_SURVEY_SUBIT.equalsIgnoreCase(surveyBO.getAction())) {
                    ArrayList surveyArr = new ArrayList();
                    surveyArr.add(surveyBO.getSurveyId());
                    ArrayList submittedSurveyIdArr = getSubmittedSurveyIds(surveyArr, null, postgre, surveyBO);

                    if (submittedSurveyIdArr != null  && submittedSurveyIdArr.isEmpty()) {
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
                } else if (ACTION_SURVEY_DETAIL.equalsIgnoreCase(surveyBO.getAction())) {
                    logger.info("SurveyExternal : Loading Properties....");
                    PropertiesFileReader propertyFileReader = new PropertiesFileReader(
                            context, "captchaconfig.properties");
                    Properties properties = propertyFileReader
                            .getPropertiesFile();
                    logger.info("SurveyExternal : Properties Loaded");
                    String siteKey = properties.getProperty("siteKey");
                    logger.debug("siteKey : " + siteKey);
                    document = detailExt.getContentDetail(context);

                    String surveyId = document
                            .selectSingleNode(
                                    "/content/root/information/id")
                            .getText();
                    logger.debug("Detail External Survey Id : " + surveyId);
                    ArrayList submittedSurveyIdArr = null;
                    if (StringUtils.isNotBlank(surveyId)) {
                        ArrayList surveyArr = new ArrayList();
                        surveyArr.add(surveyId);
                        submittedSurveyIdArr = getSubmittedSurveyIds(surveyArr, 
                                null, postgre, surveyBO);
                        logger.debug("Submitted Survey Id : "
                                + submittedSurveyIdArr);
                    }

                    if(submittedSurveyIdArr != null && !submittedSurveyIdArr.isEmpty()) {
                        document.getRootElement().addAttribute("Status",
                                SUBMITTED);
                    }
                    document.getRootElement().addAttribute("Sitekey", siteKey);
                } else if (ACTION_SURVEY_LISTING.equalsIgnoreCase(surveyBO.getAction())) {
                    document = getSurveyList(context);
                } else if (ACTION_DYNAMIC_SURVEY_SUBIT.equalsIgnoreCase(surveyBO.getAction())) {
                    ArrayList dynamicSurveyArr = new ArrayList();
                    dynamicSurveyArr.add(surveyBO.getSurveyId());
                    ArrayList submittedSurveyIdArr = getSubmittedSurveyIds(null, dynamicSurveyArr, postgre, surveyBO);

                    if (submittedSurveyIdArr != null  && submittedSurveyIdArr.isEmpty()) {
                        if (captchUtil.validateCaptcha(context,
                                surveyBO.getCaptchaResponse())) {
                            logger.info("Google Recaptcha is valid");
                            boolean insertDynamicSurveyResponse = insertDynamicSurveyResponse(
                                    surveyBO, context);
                            logger.info("insertDynamicSurveyResponse : "
                                    + insertDynamicSurveyResponse);
                            if (insertDynamicSurveyResponse) {
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
                } 

                logger.debug("Final Result :" + document.asXML());
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
    private Document getSurveyList(RequestContext context) {
        logger.info("SurveyExternal : getSurveyList");
        Document document = null;

        HukoomiExternal hukoomiExternal = new HukoomiExternal();

        // Getting Survey Solr Document
        document = hukoomiExternal.getLandingContent(context);
        logger.debug("Survey Listing Doc :" + document.asXML());

        // Extracting Survey Ids from Survey Document
        ArrayList surveyArr = new ArrayList();
        ArrayList dynamicSurveyArr = new ArrayList();
        getSurveyIdsFromDoc(document, surveyArr, dynamicSurveyArr);
        logger.debug("Survey SurveyIds from doc : " + surveyArr);
        logger.debug("Dynamic Survey SurveyIds from doc : " + dynamicSurveyArr);
        
        if(!surveyArr.isEmpty() || !dynamicSurveyArr.isEmpty()) {
        //if (StringUtils.isNotBlank(surveyIds)) {
            // Checking for already submitted Surveys
            
            ArrayList submittedSurveyIds = getSubmittedSurveyIds(surveyArr, dynamicSurveyArr,
                postgre, surveyBO);
            logger.debug("No. of Submitted Survey Ids : " + submittedSurveyIds.size());
            
            // Add Status code to document
            if (submittedSurveyIds != null && !submittedSurveyIds.isEmpty()) {
                document = addStatusToXml(document, submittedSurveyIds);
            }
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
            ArrayList submittedSurveyIds) {
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
    public ArrayList getSubmittedSurveyIds(ArrayList surveyArr, ArrayList dynamicSurveyArr,
            Postgre postgre, SurveyBO surveyBO) {
        logger.info("SurveyExternal : getSubmittedSurveyIds");
        ArrayList submittedSurveyIds = new ArrayList();
        Connection connection = null;
        PreparedStatement prepareStatement = null;
        ResultSet rs = null;
        
        try {
            connection = postgre.getConnection();
            String userId = surveyBO.getUserId();
            String ipAddress = surveyBO.getIpAddress();
            
            if(surveyArr != null && !surveyArr.isEmpty()) {
                
                logger.info("Fetching submitted id's for Survey");
                StringBuilder surveyQuery = new StringBuilder(
                        "SELECT DISTINCT SR.SURVEY_ID FROM SURVEY_RESPONSE SR, SURVEY_MASTER SM "
                        + "WHERE SM.SURVEY_ID = SR.SURVEY_ID AND SR.SURVEY_ID = ANY (?) "
                        + "AND SM.SUBMIT_TYPE = 'Single' ");
    
                if(userId != null && !"".equals(userId)) {
                    surveyQuery.append("AND USER_ID = ? ");
                }else if(ipAddress != null && !"".equals(ipAddress)) {
                    surveyQuery.append("AND IP_ADDRESS = ? ");
                }
                logger.debug("Survey Query : "+ surveyQuery.toString());
                    
                prepareStatement = connection.prepareStatement(
                        surveyQuery.toString());
                prepareStatement.setArray(1,
                        connection.createArrayOf(BIGINT, surveyArr.toArray()));
                if(userId != null && !"".equals(userId)) {
                    prepareStatement.setString(2, surveyBO.getUserId());
                }else if(ipAddress != null && !"".equals(ipAddress)) {
                    prepareStatement.setString(2, surveyBO.getIpAddress());
                }
                rs = prepareStatement.executeQuery();
                while (rs.next()) {
                    submittedSurveyIds.add(rs.getString(SURVEYID));
                }
                
            }
            
            if(dynamicSurveyArr != null && !dynamicSurveyArr.isEmpty()) {
                logger.info("Fetching submitted id's for Dynamic Survey");
                StringBuilder dynamicSurveyQuery = new StringBuilder(
                        "SELECT DISTINCT DSM.SURVEY_ID FROM DYNAMIC_SURVEY_RESPONSE DSR, DYNAMIC_SURVEY_MASTER DSM "
                        + "WHERE DSM.SURVEY_MASTER_ID = DSR.SURVEY_MASTER_ID AND DSM.SURVEY_ID = ANY (?) "
                        + "AND DSM.SUBMIT_TYPE = 'Single' ");
    
                if(userId != null && !"".equals(userId)) {
                    dynamicSurveyQuery.append("AND USER_ID = ? ");
                }else if(ipAddress != null && !"".equals(ipAddress)) {
                    dynamicSurveyQuery.append("AND IP_ADDRESS = ? ");
                }
                logger.debug("Dynamic Survey Query : "+ dynamicSurveyQuery.toString());
                    
                prepareStatement = connection.prepareStatement(
                        dynamicSurveyQuery.toString());
                prepareStatement.setArray(1,
                        connection.createArrayOf(BIGINT, dynamicSurveyArr.toArray()));
                if(userId != null && !"".equals(userId)) {
                    prepareStatement.setString(2, surveyBO.getUserId());
                }else if(ipAddress != null && !"".equals(ipAddress)) {
                    prepareStatement.setString(2, surveyBO.getIpAddress());
                }
                rs = prepareStatement.executeQuery();
                while (rs.next()) {
                    submittedSurveyIds.add(rs.getString(SURVEYID));
                }
            }
            logger.debug("Survey submitted id's : "+ submittedSurveyIds);
            
        } catch (Exception e) {
            logger.error("Exception in getSubmittedSurveyIds", e);
        } finally {
            postgre.releaseConnection(connection, prepareStatement, rs);
        }
        
        return submittedSurveyIds;
    }

    /**
     * This method is used to extract the survey id's from the input document and
     * sets the id to the respective list
     *
     * @param doc Document Object.
     *
     */
    @SuppressWarnings("unchecked")
    public void getSurveyIdsFromDoc(Document doc, ArrayList surveyArray, ArrayList dynamicSurveyArray) {
        logger.info("getSurveyIdsFromDoc()");

        if(surveyArray != null && dynamicSurveyArray != null) {
            List<Node> nodes = doc.selectNodes("/SolrResponse/response/docs");
    
            for (Node node : nodes) {
                String category = node.selectSingleNode("category").getText();
                
                if(category != null && CATEGORY_SURVEY.equals(category)) {
                    surveyArray.add(node.selectSingleNode("id").getText());
                }else if(category != null && CATEGORY_DYNAMIC_SURVEY.equals(category)) {
                    dynamicSurveyArray.add(node.selectSingleNode("id").getText());
                }
            }
        }
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
            logger.debug("responseId : " + responseId);

            connection = postgre.getConnection();

            String surveyResponseQuery = "INSERT INTO SURVEY_RESPONSE ("
                    + "RESPONSE_ID, SURVEY_ID, LANG, USER_ID, "
                    + "IP_ADDRESS, USER_AGENT, SURVEY_TAKEN_ON, "
                    + "SURVEY_TAKEN_FROM, PERSONA) VALUES(?, ?, ?, ?, ?, ?, "
                    + "LOCALTIMESTAMP, ?, ?)";
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
            surveyprepareStatement.setString(8, surveyBO.getPersona());
            int result = surveyprepareStatement.executeUpdate();
            if (result > 0) {
                logger.info("Survey Response Inserted : " + result);
                
                String surveyAnswerQuery = "INSERT INTO SURVEY_ANSWERS "
                        + "(ANSWER_ID, RESPONSE_ID, SURVEY_ID, LANG, "
                        + "QUESTION_NO, ANSWER) VALUES(?, ?, ?, ?, ?, ?)";
                answersprepareStatement = connection
                        .prepareStatement(surveyAnswerQuery);

                boolean isAdded = addSurveyAnswerstoBatch(totalCount, context,
                        answersprepareStatement, responseId, surveyBO);
                logger.info("isAdded : " + isAdded);
                if (isAdded) {
                    logger.debug("responseId : " + responseId);

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
     * This method gets the Survey master id from the dynamic survey master table.
     * 
     * @param postgre
     *                  Postgre Object.
     * @param surveyBO
     *                  dynamic survey business object.
     * 
     * @return Returns Survey master id for the dynamic survey 
     */
    public Long getDynamicSurveyMasterId(Postgre postgre, SurveyBO surveyBO) {
        logger.info("SurveyExternal : getDynamicSurveyMasterId");
        Long surveyMasterId = null;
        Connection connection = null;
        PreparedStatement prepareStatement = null;
        ResultSet rs = null;
        
        try {
            
            connection = postgre.getConnection();
            String surveyId = surveyBO.getSurveyId();
            String lang = surveyBO.getLang();
            
            if((surveyId != null && !"".equals(surveyId)) && 
                    (lang != null && !"".equals(lang)) ) {
                String dynamicSurveyQuery = "SELECT DISTINCT SURVEY_MASTER_ID FROM DYNAMIC_SURVEY_MASTER WHERE SURVEY_ID = ? AND LANG = ? ";
    
                logger.debug("dynamicSurveyQuery : "+ dynamicSurveyQuery);
                    
                prepareStatement = connection.prepareStatement(dynamicSurveyQuery);
                prepareStatement.setLong(1, Long.parseLong(surveyId));
                prepareStatement.setString(2, lang);
                rs = prepareStatement.executeQuery();
                while (rs.next()) {
                    surveyMasterId = rs.getLong(SURVEY_MASTER_ID);
                    /*String surveyMasterIdStr = rs.getString(SURVEY_MASTER_ID);
                    if(surveyMasterIdStr != null && !"".endsWith(surveyMasterIdStr)) {
                        surveyMasterId = Long.parseLong(surveyMasterIdStr);
                    }*/
                }
            }
            logger.debug("Dynamic Survey Master Id : "+ surveyMasterId);
            
        } catch (Exception e) {
            logger.error("Exception in getDynamicSurveyMasterId", e);
        } finally {
            postgre.releaseConnection(connection, prepareStatement, rs);
        }
        
        return surveyMasterId;
    }
    
    /**
     * This method gets the Survey question id from the dynamic survey master table.
     * 
     * @param postgre
     *                  Postgre Object.
     * @param surveyBO
     *                  dynamic survey business object.
     * 
     * @return Returns Survey question id for the dynamic survey 
     */
    public Long getDynamicSurveyQuestionId(Postgre postgre, SurveyBO surveyBO) {
        logger.info("SurveyExternal : getDynamicSurveyQuestionId");
        Long surveyQuestionId = null;
        Connection connection = null;
        PreparedStatement prepareStatement = null;
        ResultSet rs = null;
        
        try {
            connection = postgre.getConnection();
            Long questionId = surveyBO.getQuestionId();
            Long surveyMasterId = surveyBO.getSurveyMasterId();
            
            if(questionId != null && surveyMasterId != null  ) {
                String dynamicSurveyQuery = "SELECT SURVEY_QUESTION_ID FROM dynamic_survey_question WHERE SURVEY_MASTER_ID = ? AND QUESTION_ID = ? ";
    
                logger.debug("dynamicSurveyQuery : "+ dynamicSurveyQuery);
                    
                prepareStatement = connection.prepareStatement(dynamicSurveyQuery);
                prepareStatement.setLong(1, surveyMasterId);
                prepareStatement.setLong(2, questionId);
                rs = prepareStatement.executeQuery();
                while (rs.next()) {
                    surveyQuestionId = rs.getLong(SURVEY_QUESTION_ID);
                    /*String surveyQuestionIdStr = rs.getString(SURVEY_QUESTION_ID);
                    if(surveyQuestionIdStr != null && !"".endsWith(surveyQuestionIdStr)) {
                        surveyQuestionId = Long.parseLong(surveyQuestionIdStr);
                    }*/
                }
            }
            logger.debug("Dynamic Survey Question Id : "+ surveyQuestionId);
            
        } catch (Exception e) {
            logger.error("Exception in getDynamicSurveyQuestionId", e);
        } finally {
            postgre.releaseConnection(connection, prepareStatement, rs);
        }
        
        return surveyQuestionId;
    }
            
    
    /**
     * This method is used to insert dynamic survey data into database.
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
    private boolean insertDynamicSurveyResponse(SurveyBO surveyBO,
            RequestContext context) {
        logger.info("SurveyExternal : insertDynamicSurveyResponse");

        PreparedStatement surveyprepareStatement = null;
        PreparedStatement answersprepareStatement = null;
        Connection connection = null;
        ResultSet rs = null;
        int totalCount = 0;
        boolean isSurveyInserted = false;

        try {
            
            totalCount = getIntValue(surveyBO.getTotalQuestions());
            logger.info("totalCount : " + totalCount);
            
            Long dynamicSurveyMasterId = getDynamicSurveyMasterId(postgre, surveyBO);
            surveyBO.setSurveyMasterId(dynamicSurveyMasterId);

            //totalCount = getIntValue(surveyBO.getTotalQuestions());
            //logger.info("totalCount : " + totalCount);

            Long responseId = getNextSequenceValue(
                    "dynamic_survey_response_response_id_seq",
                    postgre.getConnection());
            logger.debug("responseId : " + responseId);
            surveyBO.setResponseId(responseId);
            
            connection = postgre.getConnection();

            String surveyResponseQuery = "INSERT INTO DYNAMIC_SURVEY_RESPONSE ("
                    + "RESPONSE_ID, SURVEY_MASTER_ID, USER_ID, "
                    + "IP_ADDRESS, USER_AGENT, SURVEY_TAKEN_ON, "
                    + "SURVEY_TAKEN_FROM, PERSONA) VALUES(?, ?, ?, ?, ?, "
                    + "LOCALTIMESTAMP, ?, ?)";
            connection.setAutoCommit(false);
            surveyprepareStatement = connection
                    .prepareStatement(surveyResponseQuery);
            surveyprepareStatement.setLong(1, surveyBO.getResponseId());
            surveyprepareStatement.setLong(2, surveyBO.getSurveyMasterId());
            surveyprepareStatement.setString(3, surveyBO.getUserId());
            surveyprepareStatement.setString(4, surveyBO.getIpAddress());
            surveyprepareStatement.setString(5, surveyBO.getUserAgent());
            surveyprepareStatement.setString(6, surveyBO.getTakenFrom());
            surveyprepareStatement.setString(7, surveyBO.getPersona());
            int result = surveyprepareStatement.executeUpdate();
            if (result > 0) {
                logger.info("Dynamic Survey Response Inserted : " + result);
                
                String surveyAnswerQuery = "INSERT INTO DYNAMIC_SURVEY_ANSWERS "
                        + "( RESPONSE_ID, SURVEY_QUESTION_ID, ANSWER ) "
                        + "VALUES(?, ?, ?)";
                answersprepareStatement = connection
                        .prepareStatement(surveyAnswerQuery);

                boolean isAdded = addDynamicSurveyAnswerstoBatch(totalCount, context,
                        answersprepareStatement, responseId, surveyBO);
                logger.info("isAdded : " + isAdded);
                if (isAdded) {
                    logger.debug("responseId : " + responseId);

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
                        logger.info("Dynamic Survey Answer Inserted");
                        isSurveyInserted = true;
                    } else {
                        logger.info("Failed to insert Dynamic Survey Answer");
                        connection.rollback();
                    }
                } else {
                    logger.info("Dynamic Survey answer is not created.");
                    connection.rollback();
                }
            } else {
                logger.info("Dynamic Survey Response not Inserted.");
                connection.rollback();
            }
        } catch (Exception e) {
            logger.error("Exception in insertDynamicSurveyResponse", e);
        } finally {
            postgre.releaseConnection(null, answersprepareStatement, null);
            postgre.releaseConnection(connection, surveyprepareStatement,
                    rs);
        }

        return isSurveyInserted;
    }

    /**
     * This method is used to add the survey answers to the batch for database batch
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
    private boolean addSurveyAnswerstoBatch(int totalCount,
            final RequestContext context,
            PreparedStatement answersprepareStatement, Long responseId,
            SurveyBO surveyBO) throws SQLException {
        logger.info("SurveyExternal : addSurveyAnswerstoBatch");
        XssUtils xssUtils = new XssUtils();
        int numOfAnswersAdded = 0;
        boolean isAdded = false;
        for (int i = 1; i <= totalCount; i++) {
            String value = context.getParameterString(String.valueOf(i));
            logger.debug("value >>>" + value + "<<<");
            logger.debug("Answer for question " + i + " : " + value);

            if (value != null && value.contains("#$#")) {
                String[] multipleOption = value.split("#\\$#");
                logger.debug(
                        "Multiple Option Answer" + multipleOption.length);
                for (String mutiOptValue : multipleOption) {
                    Long answerId = getNextSequenceValue(
                            "survey_answers_answer_id_seq",
                            postgre.getConnection());
                    logger.debug("answerId : " + answerId);
                    logger.debug("mutiOptValue : " + mutiOptValue);

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
                    isAdded = true;
                }

            } else {
                Long answerId = getNextSequenceValue(
                        "survey_answers_answer_id_seq",
                        postgre.getConnection());
                logger.debug("answerId : " + answerId);

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
                isAdded = true;
            }
            surveyBO.setQuestionNo(numOfAnswersAdded);
        }
        return isAdded;
    }
    
    /**
     * This method is used to add the dynamic survey answers to the batch for database batch
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
    private boolean addDynamicSurveyAnswerstoBatch(int totalCount,
            final RequestContext context,
            PreparedStatement answersprepareStatement, Long responseId,
            SurveyBO surveyBO) throws SQLException {
        logger.info("SurveyExternal : addDynamicSurveyAnswerstoBatch");
        XssUtils xssUtils = new XssUtils();
        int numOfAnswersAdded = 0;
        boolean isAdded = false;
        logger.debug("totalCount : " + totalCount);
        for (int i = 1; i <= totalCount; i++) {
            
            String value = context.getParameterString(String.valueOf(i));
            logger.debug("value >>>" + value + "<<<");
            
            String[] questionAnswerArr = value.split("#\\?#");
            
            String questionId = questionAnswerArr[0];
            String answer = questionAnswerArr[1];
            
            if((questionId != null && !"".equals(questionId)) &&
                    (answer != null && !"".equals(answer)) ) {
                
                surveyBO.setQuestionId(Long.parseLong(questionId));
            
                Long dynamicSurveyQuestionId = getDynamicSurveyQuestionId(postgre, surveyBO);
                //surveyBO.setQuestionId(dynamicSurveyQuestionId);
                
                logger.debug("Answer for questionId " + questionId + " : " + answer);
    
                if (answer.contains("#$#")) {
                    String[] multipleOption = answer.split("#\\$#");
                    logger.debug(
                            "Multiple Option Answer" + multipleOption.length);
                    for (String mutiOptValue : multipleOption) {
                        logger.debug("mutiOptValue : " + mutiOptValue);
    
                        answersprepareStatement.setLong(1, surveyBO.getResponseId());
                        answersprepareStatement.setLong(2, dynamicSurveyQuestionId);
                        answersprepareStatement.setString(3,
                                xssUtils.stripXSS(mutiOptValue));
                        answersprepareStatement.addBatch();
                        numOfAnswersAdded++;
                        isAdded = true;
                    }
    
                } else {
    
                    answersprepareStatement.setLong(1, surveyBO.getResponseId());
                    answersprepareStatement.setLong(2, dynamicSurveyQuestionId);
                    answersprepareStatement.setString(3,
                            xssUtils.stripXSS(answer));
                    answersprepareStatement.addBatch();
                    numOfAnswersAdded++;
                    isAdded = true;
                }
                surveyBO.setQuestionNo(numOfAnswersAdded);
            }
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
        final String SURVEY_GROUP_CONFIG = "SurveyGroupConfig";
        final String SURVEY_CATEGORY = "surveyCategory";
        final String SURVEY_GROUP_CATEGORY = "surveyGroupCategory";
        final String SURVEY_GROUP_CONFIG_CATEGORY = "surveyGroupConfigCategory";
        final String SOLR_SURVEY_CATEGORY = "solrSurveyCategory";
        final String PERSONA = "persona";

        RequestHeaderUtils requestHeaderUtils = new RequestHeaderUtils(context);
        ValidationErrorList errorList = new ValidationErrorList();
        HttpServletRequest request = context.getRequest();
        String validData  = "";
        String userId = null;
        
        String surveyAction = context.getParameterString(SURVEY_ACTION);
        logger.debug(SURVEY_ACTION + " >>>"+surveyAction+"<<<");
        if (!ESAPIValidator.checkNull(surveyAction)) {
            validData  = ESAPI.validator().getValidInput(SURVEY_ACTION, surveyAction, ESAPIValidator.ALPHABET, 25, false, true, errorList);
            if(errorList.isEmpty()) {
                surveyBO.setAction(validData);
            }else {
                logger.debug(errorList.getError(SURVEY_ACTION));
                return false;
            }
        }
        
        String locale = context.getParameterString(LOCALE, "en");
        logger.debug(LOCALE + " >>>"+locale+"<<<");
        validData  = ESAPI.validator().getValidInput(LOCALE, locale, ESAPIValidator.ALPHABET, 2, false, true, errorList);
        if(errorList.isEmpty()) {
            surveyBO.setLang(validData);
        }else {
            logger.debug(errorList.getError(LOCALE));
            return false;
        }
        
        logger.debug("Session Status : "+request.getSession().getAttribute("status"));
        if(request.getSession().getAttribute("status") != null && "valid".equals(request.getSession().getAttribute("status"))) {
            if(request.getSession().getAttribute("userId") != null) {
                userId = request.getSession().getAttribute("userId").toString();
                
                logger.debug(USER_ID + " >>>"+userId+"<<<");
                surveyBO.setUserId(userId);
            }else {
                logger.debug("UserId from session is null.");
            }
            /*validData  = ESAPI.validator().getValidInput(USER_ID, userId, ESAPIValidator.USER_ID, 50, true, true, errorList);
            if(errorList.isEmpty()) {
                surveyBO.setUserId(validData);
            }else {
                logger.debug(errorList.getError(USER_ID));
                return false;
            }*/
        }
        
        /*String userId = context.getParameterString(USER_ID);
        logger.debug(USER_ID + " >>>"+userId+"<<<");
        validData  = ESAPI.validator().getValidInput(USER_ID, userId, ESAPIValidator.USER_ID, 50, true, true, errorList);
        if(errorList.isEmpty()) {
            surveyBO.setUserId(validData);
        }else {
            logger.debug(errorList.getError(USER_ID));
            return false;
        }*/
        
        String ipAddress = requestHeaderUtils.getClientIpAddress();
        logger.debug("ipaddress >>>" +ipAddress+"<<<");
        validData  = ESAPI.validator().getValidInput("ipaddress", ipAddress, ESAPIValidator.IP_ADDRESS, 20, false, true, errorList);
        if(errorList.isEmpty()) {
            surveyBO.setIpAddress(validData);
        }else {
            logger.debug(errorList.getError("ipaddress"));
            return false;
        }
        
        surveyBO.setUserAgent(context.getRequest().getHeader(USER_AGENT));
        
        if(ACTION_SURVEY_SUBIT.equalsIgnoreCase(surveyAction) || 
                ACTION_DYNAMIC_SURVEY_SUBIT.equalsIgnoreCase(surveyAction)) {
            
            String surveyTakenFrom = context.getParameterString(SURVEY_TAKEN_FROM);
            logger.debug(SURVEY_TAKEN_FROM + " >>>"+surveyTakenFrom+"<<<");
            validData  = ESAPI.validator().getValidInput(SURVEY_TAKEN_FROM, surveyTakenFrom, ESAPIValidator.ALPHABET, 150, false, true, errorList);
            if(errorList.isEmpty()) {
                surveyBO.setTakenFrom(validData);
            }else {
                logger.debug(errorList.getError(USER_AGENT));
                return false;
            }
            
            String surveyId = context.getParameterString(SURVEY_ID);
            logger.debug(SURVEY_ID + " >>>"+surveyId+"<<<");
            validData  = ESAPI.validator().getValidInput(SURVEY_ID, surveyId, ESAPIValidator.NUMERIC, 200, false, true, errorList);
            if(errorList.isEmpty()) {
                surveyBO.setSurveyId(validData);
            }else {
                logger.debug(errorList.getError(SURVEY_ID));
                return false;
            }
            
            String totalQuestions = context.getParameterString(TOTAL_QUESTIONS);
            logger.debug(TOTAL_QUESTIONS + " >>>"+totalQuestions+"<<<");
            validData  = ESAPI.validator().getValidInput(TOTAL_QUESTIONS, totalQuestions, ESAPIValidator.NUMERIC, 50, false, true, errorList);
            if(errorList.isEmpty()) {
                surveyBO.setTotalQuestions(validData);
            }else {
                logger.debug(errorList.getError(TOTAL_QUESTIONS));
                return false;
            }
            
            surveyBO.setCaptchaResponse(
                    context.getParameterString("g-recaptcha-response"));
            
          //Get Persona details from persona settings
            String persona = null;
            if(userId != null && !"".equals(userId)) {
                DashboardSettingsExternal dsExt = new DashboardSettingsExternal();
                persona = dsExt.getPersonaForUser(userId, postgre);
                logger.debug("Persona from DB >>>" +persona+ "<<<");
                surveyBO.setPersona(persona);
            }
            
            if(persona == null || "".equals(persona)) {
                Cookie[] cookies = request.getCookies();
                for(int i = 0 ; i < cookies.length;  i++) {
                    Cookie cookie = cookies[i];
                    String name = cookie.getName();
                    String personaValue = null;
                    if(name != null && "persona".equalsIgnoreCase(name)) {
                        personaValue = cookie.getValue();
                        logger.debug(PERSONA + " >>>" +personaValue+ "<<<");
                        validData  = ESAPI.validator().getValidInput(PERSONA, personaValue, ESAPIValidator.ALPHABET_HYPEN, 200, true, true, errorList);
                        if(errorList.isEmpty()) {
                            surveyBO.setPersona(validData);
                        }else {
                            logger.debug(errorList.getError(PERSONA));
                            return false;
                        }
                    }
                }
            }
        }
        
        String pollAction = context.getParameterString(POLL_ACTION);
        logger.debug(POLL_ACTION + " >>>" +pollAction+ "<<<");   
        if(ACTION_POLLS_AND_SURVEY.equalsIgnoreCase(pollAction)) {
            
            String surveyGroup = context.getParameterString(SURVEY_GROUP);
            logger.debug(SURVEY_GROUP + " >>>" +surveyGroup+ "<<<");
            if (!ESAPIValidator.checkNull(surveyGroup)) {
                surveyBO.setGroup(getContentName(surveyGroup));
            }
            
            String surveyGroupConfig = context.getParameterString(SURVEY_GROUP_CONFIG);
            logger.debug(SURVEY_GROUP_CONFIG + " >>>" +surveyGroupConfig+ "<<<");
            if (!ESAPIValidator.checkNull(surveyGroupConfig)) {
                surveyBO.setGroup(getContentName(surveyGroupConfig));
            }
            
            String surveyGroupCategory = context.getParameterString(SURVEY_GROUP_CATEGORY);
            logger.debug(SURVEY_GROUP_CATEGORY + " >>>"+surveyGroupCategory+"<<<");
            validData  = ESAPI.validator().getValidInput(SURVEY_GROUP_CATEGORY, surveyGroupCategory, ESAPIValidator.ALPHABET_HYPEN, 50, false, true, errorList);
            if(errorList.isEmpty()) {
                surveyBO.setGroupCategory(validData);
            }else {
                logger.debug(errorList.getError(SURVEY_GROUP_CATEGORY));
                return false;
            }
            
            String surveyGroupConfigCategory = context.getParameterString(SURVEY_GROUP_CONFIG_CATEGORY);
            logger.debug(SURVEY_GROUP_CONFIG_CATEGORY + " >>>"+surveyGroupConfigCategory+"<<<");
            validData  = ESAPI.validator().getValidInput(SURVEY_GROUP_CONFIG_CATEGORY, surveyGroupConfigCategory, ESAPIValidator.ALPHABET_HYPEN, 50, false, true, errorList);
            if(errorList.isEmpty()) {
                surveyBO.setGroupCategory(validData);
            }else {
                logger.debug(errorList.getError(SURVEY_GROUP_CONFIG_CATEGORY));
                return false;
            }
            
            String surveyCategory = context.getParameterString(SURVEY_CATEGORY);
            logger.debug(SURVEY_CATEGORY + " >>>"+surveyCategory+"<<<");
            validData  = ESAPI.validator().getValidInput(SURVEY_CATEGORY, surveyCategory, ESAPIValidator.ALPHABET, 50, false, true, errorList);
            if(errorList.isEmpty()) {
                surveyBO.setCategory(validData);
            }else {
                logger.debug(errorList.getError(SURVEY_CATEGORY));
                return false;
            }
            
            String solrSurveyCategory = context.getParameterString(SOLR_SURVEY_CATEGORY);
            logger.debug(SOLR_SURVEY_CATEGORY + " >>>"+solrSurveyCategory+"<<<");
            validData  = ESAPI.validator().getValidInput(SOLR_SURVEY_CATEGORY, solrSurveyCategory, ESAPIValidator.ALPHABET, 50, false, true, errorList);
            if(errorList.isEmpty()) {
                surveyBO.setSolrCategory(validData);
            }else {
                logger.debug(errorList.getError(SOLR_SURVEY_CATEGORY));
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
