package com.hukoomi.task;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;

import com.hukoomi.bo.TSPollsBO;
import com.hukoomi.bo.TSSurveyBO;
import com.hukoomi.utils.PostgreTSConnection;
import com.interwoven.cssdk.common.CSClient;
import com.interwoven.cssdk.common.CSException;
import com.interwoven.cssdk.filesys.CSAreaRelativePath;
import com.interwoven.cssdk.filesys.CSBranch;
import com.interwoven.cssdk.filesys.CSEdition;
import com.interwoven.cssdk.filesys.CSFile;
import com.interwoven.cssdk.filesys.CSHole;
import com.interwoven.cssdk.filesys.CSSimpleFile;
import com.interwoven.cssdk.filesys.impl.CSHoleImpl;
import com.interwoven.cssdk.workflow.CSExternalTask;
import com.interwoven.cssdk.workflow.CSURLExternalTask;

/**
 * PollSurveyTask is the workflow task class for polls and survey master data
 * insert URL task. It contains methods to insert / update polls and survey
 * master date to its corresponding tables.
 *
 * @author Vijayaragavamoorthy
 */
public class PollSurveyTask implements CSURLExternalTask {
    /**
     * Logger object to check the flow of the code.
     */
    private final Logger logger = Logger.getLogger(PollSurveyTask.class);
    /**
     * DCR Type Meta data name
     */
    public static final String META_DATA_NAME_DCR_TYPE = "TeamSite/Templating/DCR/Type";
    /**
     * XPath to the poll /sruvey id
     */
    public static final String ID_PATH = "/root/information/id";
    /**
     * XPath to the language selection
     */
    public static final String LANG_PATH = "/root/information/language/value";
    /**
     * XPath to the poll question
     */
    public static final String QUESTION_PATH = "/root/detail/question";
    /**
     * XPath to the poll start date
     */
    public static final String POLL_START_DATE_PATH = "/root/detail/start-date";
    /**
     * XPath to the poll end date
     */
    public static final String POLL_END_DATE_PATH = "/root/detail/end-date";
    /**
     * XPath to the submit type
     */
    public static final String SUBMIT_TYPE = "/root/details/submitType";
    /**
     * XPath to the persona selection
     */
    public static final String PERSONA_PATH = "/root/settings/persona/value";
    /**
     * XPath to the organization selection
     */
    public static final String SERVICE_ENTITIES = "/root/settings/service-entities/value";
    /**
     * XPath to the topic selection
     */
    public static final String TOPICS = "/root/settings/topics/value";
    /**
     * XPath to the option
     */
    public static final String OPTION_PATH = "/root/detail/option";
    /**
     * XPath to the survey title
     */
    public static final String TITLE_PATH = "/root/details/survey-title";
    /**
     * XPath to the survey description
     */
    public static final String DESCRIPTION_PATH = "/root/details/description";
    /**
     * XPath to the survey start date
     */
    public static final String SURVEY_START_DATE_PATH = "/root/details/start-date";
    /**
     * XPath to the survey end date
     */
    public static final String SURVEY_END_DATE_PATH = "/root/details/end-date";
    /**
     * XPath to the survey form field
     */
    public static final String FIELD_PATH = "/root/form-field";
    /**
     * XPath to the option field
     */
    public static final String OPTION_FIELD_PATH = "/root/options/option-field/option";
    /**
     * XPath to the estimated time
     */
    public static final String ESTIMATED_TIME = "/root/details/estimatedTime";
    /**
     * Success transition message
     */
    public static final String SUCCESS_TRANSITION = "Insert Master Data Success";
    /**
     * Failure transition message
     */
    public static final String FAILURE_TRANSITION = "Insert Master Data Failure";
    /**
     * Database property file name
     */
    private static final String DB_PROPERTY_FILE = "dbconfig.properties";
    /**
     * XPath for the poll option label
     */
    private static final String OPTION_LABEL = "label";
    /**
     * XPath for the poll option value
     */
    private static final String OPTION_VALUE = "value";
    /**
     * XPath for the poll option is open respnse
     */
    private static final String IS_OPEN_RESPONSE = "isOpenResponse";
    /**
     * Transition hashmap key
     */
    private static final String TRANSITION = "TRANSITION";
    /**
     * Transition comment hashmap key
     */
    private static final String TRANSITION_COMMENT = "TRANSITION_COMMENT";
    /**
     * Poll update transition success message
     */
    private static final String POLL_UPDATE_SUCCESS = "Poll master data updated successfully";
    /**
     * Poll update transition failure message
     */
    private static final String POLL_UPDATE_FAILURE = "Failed to updated poll master data";
    /**
     * Poll insert transition success message
     */
    private static final String POLL_INSERT_SUCCESS = "Poll master data inserted successfully";
    /**
     * Poll insert transition failure message
     */
    private static final String POLL_INSERT_FAILURE = "Failed to insert poll master data";
    /**
     * Poll transition technical error message
     */
    private static final String POLL_TECHNICAL_ERROR = "Technical Error in poll master data insert";
    /**
     * Survey update transition success message
     */
    private static final String SURVEY_UPDATE_SUCCESS = "Survey master data updated successfully";
    /**
     * Survey update transition failure message
     */
    private static final String SURVEY_UPDATE_FAILURE = "Failed to updated survey master data";
    /**
     * Survey insert transition success message
     */
    private static final String SURVEY_INSERT_SUCCESS = "Survey master data inserted successfully";
    /**
     * Survey insert transition failure message
     */
    private static final String SURVEY_INSERT_FAILURE = "Failed to insert survey master data";
    /**
     * Survey transition technical error message
     */
    private static final String SURVEY_TECHNICAL_ERROR = "Technical Error in survey master data insert";
    /**
     * Polls Published Status
     */
    private static final String PUBLISHED_STATUS = "Published";
    /**
     * Polls Unpublished Status
     */
    private static final String UNPUBLISHED_STATUS = "Unpublished";
    /**
     * Polls Content type
     */
    private static final String POLLS_CONTENT_TYPE = "Content/Polls";
    /**
     * Survey Content type
     */
    private static final String SURVEY_CONTENT_TYPE = "Content/Survey";
    /**
     * Blog Content type
     */
    private static final String BLOG_CONTENT_TYPE = "Content/Blog";
    /**
     * Error Content type
     */
    private static final String ERROR_CONTENT_TYPE = "Content/Error-page-banner";
    /**
     * Dynamic Survey Content type
     */
    private static final String DYNAMIC_SURVEY_CONTENT_TYPE = "Content/Dynamic-Survey";
    /**
     * Postgre class instance variable
     */
    PostgreTSConnection postgre = null;
    /**
     * Options List array contains the list of field type which has options
     */
    ArrayList optionsList = new ArrayList<>();

    /**
     * Overridden method from CSSDK
     *
     * @param client CSClient object
     * @param task   CSExternalTask object
     * @param params Hashtable object
     */
    @Override
    public void execute(CSClient client, CSExternalTask task,
            Hashtable params) throws CSException {
        logger.info("PollSurveyTask - execute");
        HashMap<String, String> statusMap = null;
        CSAreaRelativePath[] taskFileList = task.getFiles();
        logger.debug("TaskFileList Length : " + taskFileList.length);

        postgre = new PostgreTSConnection(client, task, DB_PROPERTY_FILE);
        statusMap = new HashMap<>();
        statusMap.put(TRANSITION, SUCCESS_TRANSITION);
        statusMap.put(TRANSITION_COMMENT, "");
        
        for (CSAreaRelativePath taskFilePath : taskFileList) {
            try {
                
                CSFile file = task.getArea().getFile(taskFilePath);
                String fileName = file.getName();
                logger.debug("File Name : " + fileName);
                
                if (file.getKind() == CSHole.KIND) {
                    CSHoleImpl taskHoleFile = (CSHoleImpl) file;
                    
                    String comment = taskHoleFile.getRevisionComment();
                    logger.info("comment : "+comment);
                    
                    if(!comment.startsWith("[moved to")) {
                    
                        logger.info("PollSurveyTask - Deleted File");
                        CSSimpleFile csSimpleTaskFile = getDeletedFile(client, taskHoleFile);
                        String dcrType = csSimpleTaskFile
                                .getExtendedAttribute(META_DATA_NAME_DCR_TYPE)
                                .getValue();
                        
                        if (dcrType != null) {
                            if (POLLS_CONTENT_TYPE.equalsIgnoreCase(dcrType)) {
                                Document doc = getTaskDocument(csSimpleTaskFile);
                                logger.info("doc of hole file : "+doc.asXML());
                                String pollId = getDCRValue(doc, ID_PATH);
                                String lang = getDCRValue(doc, LANG_PATH);
                                statusMap = (HashMap<String, String>) updateDeletePollMasterData(pollId, lang, postgre.getConnection());
                            }else if (SURVEY_CONTENT_TYPE
                                    .equalsIgnoreCase(dcrType)) {
                                Document doc = getTaskDocument(csSimpleTaskFile);
                                logger.info("doc of hole file : "+doc.asXML());
                                String surveyId = getDCRValue(doc, ID_PATH);
                                String lang = getDCRValue(doc, LANG_PATH);
                                statusMap = (HashMap<String, String>) updateDeleteSurveyMasterData(
                                        surveyId, lang, postgre.getConnection());
                            } else if (DYNAMIC_SURVEY_CONTENT_TYPE
                                    .equalsIgnoreCase(dcrType)) {
                                Document doc = getTaskDocument(csSimpleTaskFile);
                                logger.debug("doc of hole file : "+doc.asXML());
                                String surveyId = getDCRValue(doc, ID_PATH);
                                String lang = getDCRValue(doc, LANG_PATH);
                                statusMap = (HashMap<String, String>) updateDeleteDynamicSurveyMasterData(
                                        surveyId, lang, postgre.getConnection());
                            }else {
                                logger.debug(
                                        "Deleted status update skipped - Not Polls or Survey DCR");
                            }
                        }else {
                            logger.info("PollSurveyTask - DCR Type metadata is null");
                        }
                    }else {
                        logger.info("PollSurveyTask - Renamed moved file");
                    }
                    
                }else {
                
                    CSSimpleFile taskSimpleFile = (CSSimpleFile) file;
    
                    String dcrType = taskSimpleFile
                            .getExtendedAttribute(META_DATA_NAME_DCR_TYPE)
                            .getValue();
    
                    if (dcrType != null) {
                        if (POLLS_CONTENT_TYPE.equalsIgnoreCase(dcrType)) {
                            statusMap = (HashMap<String, String>) processPollDCR(
                                    taskSimpleFile);
                        } else if (SURVEY_CONTENT_TYPE
                                .equalsIgnoreCase(dcrType)) {
                            setOptionFieldTypes();
                            statusMap = (HashMap<String, String>) processSurveyDCR(
                                    taskSimpleFile);
                        } else if (BLOG_CONTENT_TYPE.equalsIgnoreCase(dcrType)) {
                            BlogTask blog = new BlogTask();
                            statusMap = (HashMap<String, String>) blog.processBlogDCR(
                                    taskSimpleFile,postgre);
                        } else if (ERROR_CONTENT_TYPE.equalsIgnoreCase(dcrType)) {
                            ErrorTask error = new ErrorTask();
                            statusMap = (HashMap<String, String>) error.processBlogDCR(
                                    taskSimpleFile,postgre);
                        } 
                        else if (DYNAMIC_SURVEY_CONTENT_TYPE
                            .equalsIgnoreCase(dcrType)) {
                            setOptionFieldTypes();
                            statusMap = (HashMap<String, String>) processDynamicSurveyDCR(
                                taskSimpleFile);
                        } else {
                            logger.info(
                                    "Master data insert skipped - Not Polls or Survey DCR");
                        }
                    }else {
                        logger.info("PollSurveyTask - DCR Type metadata is null");
                    }
                }
                
            } catch (Exception e) {
                logger.error("Exception in execute: ", e);
            }
        }

        logger.info("transition : " + statusMap.get(TRANSITION));
        logger.info("transitionComment : "
                + statusMap.get(TRANSITION_COMMENT));
        task.chooseTransition(statusMap.get(TRANSITION),
                statusMap.get(TRANSITION_COMMENT));
    }
    
    
    private void setOptionFieldTypes() {
        logger.info("PollSurveyTask - setOptionFieldTypes");
        if(optionsList != null) {
            optionsList.add("radio");
            optionsList.add("checkbox");
            optionsList.add("singleselect");
            optionsList.add("multiselect");
        }
    }
    
    
    private CSSimpleFile getDeletedFile(CSClient client, CSHoleImpl taskHoleFile) {
        CSSimpleFile taskFile = null;
        logger.info("PollSurveyTask - getDeletedFile");
        try {
            logger.debug("Task file kind : "+taskHoleFile.getKind());
            if (taskHoleFile.getKind() == CSHole.KIND) {
                CSHole hole = (CSHole) taskHoleFile;
                logger.debug("Task file prvious kind : "+hole.getPreviousKind());
                if(hole.getPreviousKind() == CSSimpleFile.KIND) {
                    CSBranch branch = hole.getRevisionBranch();
                    //logger.error("branch : "+branch);
                    CSEdition[] editions =  branch.getEditions(true, false, 999999);
                    //logger.error("editions length : "+editions.length);
                    CSEdition edition = null;
                    logger.debug("File creation time : "+hole.getCreationDate().getTime());
                    for (CSEdition ed : editions) {
                        logger.debug("Edtion creation time : "+ed.getCreationDate().getTime());
                        if (hole.getCreationDate().getTime() > ed.getCreationDate().getTime()) {
                            edition = ed;
                            break;
                        }
                    }
                    String areaRelativePath = taskHoleFile.getAreaRelativePath();
                    logger.debug("areaRelativePath : "+areaRelativePath);
                    CSAreaRelativePath caAreaRelativePath = new CSAreaRelativePath(areaRelativePath);
                    CSFile file = edition.getFile(caAreaRelativePath);
                    if (file.getKind() == CSHole.KIND) {
                        logger.info("Deleted File");
                    }else if (file.getKind() == CSSimpleFile.KIND) {
                        logger.info("Simple File");
                        taskFile = (CSSimpleFile)file;                        
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Exception in getDeletedFile : ", e);
            e.printStackTrace();
        }
        return taskFile;
    }

    /**
     * Method process the poll dcr from the workflow task and insert poll master
     * data
     *
     * @param taskSimpleFile Task file of CSSimpleFile object
     * @return Returns map contains the transition status and transition comment.
     */
    public Map<String, String> processPollDCR(
            CSSimpleFile taskSimpleFile) {
        logger.info("PollSurveyTask : processPollDCR");
        boolean isDBOperationSuccess = false;
        HashMap<String, String> statusMap = new HashMap<>();
        try {
            Document document = getTaskDocument(taskSimpleFile);
            if (isPollMasterDataAvailable(document)) {
                isDBOperationSuccess = updatePollData(document, taskSimpleFile.getName());
                logger.info(
                        "isPollDataUpdated : " + isDBOperationSuccess);
                if (isDBOperationSuccess) {
                    statusMap.put(TRANSITION, SUCCESS_TRANSITION);
                    statusMap.put(TRANSITION_COMMENT, POLL_UPDATE_SUCCESS);
                } else {
                    statusMap.put(TRANSITION, FAILURE_TRANSITION);
                    statusMap.put(TRANSITION_COMMENT, POLL_UPDATE_FAILURE);
                }
            } else {
                isDBOperationSuccess = insertPollData(document, taskSimpleFile.getName());
                logger.info(
                        "isPollDataInserted : " + isDBOperationSuccess);
                if (isDBOperationSuccess) {
                    statusMap.put(TRANSITION, SUCCESS_TRANSITION);
                    statusMap.put(TRANSITION_COMMENT, POLL_INSERT_SUCCESS);
                } else {
                    statusMap.put(TRANSITION, FAILURE_TRANSITION);
                    statusMap.put(TRANSITION_COMMENT, POLL_INSERT_FAILURE);
                }
            }
        } catch (Exception e) {
            statusMap.put(TRANSITION, FAILURE_TRANSITION);
            statusMap.put(TRANSITION_COMMENT, POLL_TECHNICAL_ERROR);
            logger.error("Exception in processPollDCR : ", e);
        }
        return statusMap;
    }

    /**
     * Method process the survey dcr from the workflow task and insert survey master
     * data
     *
     * @param taskSimpleFile Task file of CSSimpleFile object
     * @return Returns map contains the transition status and transition comment.
     */
    public Map<String, String> processSurveyDCR(
            CSSimpleFile taskSimpleFile) {
        logger.info("PollSurveyTask : processSurveyDCR");
        boolean isDBOperationSuccess = false;
        HashMap<String, String> statusMap = new HashMap<>();
        try {
            Document document = getTaskDocument(taskSimpleFile);
            if (isSurveyMasterDataAvailable(document)) {
                isDBOperationSuccess = updateSurveyData(document, taskSimpleFile.getName());
                logger.info(
                        "isSurveyDataUpdated : " + isDBOperationSuccess);
                if (isDBOperationSuccess) {
                    statusMap.put(TRANSITION, SUCCESS_TRANSITION);
                    statusMap.put(TRANSITION_COMMENT,
                            SURVEY_UPDATE_SUCCESS);
                } else {
                    statusMap.put(TRANSITION, FAILURE_TRANSITION);
                    statusMap.put(TRANSITION_COMMENT,
                            SURVEY_UPDATE_FAILURE);
                }
            } else {
                isDBOperationSuccess = insertSurveyData(document, taskSimpleFile.getName());
                logger.info(
                        "isSurveyDataInserted : " + isDBOperationSuccess);
                if (isDBOperationSuccess) {
                    statusMap.put(TRANSITION, SUCCESS_TRANSITION);
                    statusMap.put(TRANSITION_COMMENT,
                            SURVEY_INSERT_SUCCESS);
                } else {
                    statusMap.put(TRANSITION, FAILURE_TRANSITION);
                    statusMap.put(TRANSITION_COMMENT,
                            SURVEY_INSERT_FAILURE);
                }
            }
        } catch (Exception e) {
            statusMap.put(TRANSITION, FAILURE_TRANSITION);
            statusMap.put(TRANSITION_COMMENT, SURVEY_TECHNICAL_ERROR);
            logger.error("Exception in processSurveyDCR : ", e);
        }
        return statusMap;
    }
    
    
    /**
     * Method process the dynamic survey dcr from the workflow task and insert dynamic survey master
     * data
     *
     * @param taskSimpleFile Task file of CSSimpleFile object
     * @return Returns map contains the transition status and transition comment.
     */
    public Map<String, String> processDynamicSurveyDCR(
            CSSimpleFile taskSimpleFile) {
        logger.info("PollSurveyTask : processDynamicSurveyDCR");
        boolean isDBOperationSuccess = false;
        HashMap<String, String> statusMap = new HashMap<>();
        try {
            Document document = getTaskDocument(taskSimpleFile);
            if (isDynamicSurveyMasterDataAvailable(document)) {
                isDBOperationSuccess = updateDynamicSurveyData(document, taskSimpleFile.getName());
                logger.info(
                        "isDynamicSurveyDataUpdated : " + isDBOperationSuccess);
                if (isDBOperationSuccess) {
                    statusMap.put(TRANSITION, SUCCESS_TRANSITION);
                    statusMap.put(TRANSITION_COMMENT,
                            SURVEY_UPDATE_SUCCESS);
                } else {
                    statusMap.put(TRANSITION, FAILURE_TRANSITION);
                    statusMap.put(TRANSITION_COMMENT,
                            SURVEY_UPDATE_FAILURE);
                }
            } else {
                isDBOperationSuccess = insertDynamicSurveyData(document, taskSimpleFile.getName());
                logger.info(
                        "isDynamicSurveyDataUpdated : " + isDBOperationSuccess);
                if (isDBOperationSuccess) {
                    statusMap.put(TRANSITION, SUCCESS_TRANSITION);
                    statusMap.put(TRANSITION_COMMENT,
                            SURVEY_INSERT_SUCCESS);
                } else {
                    statusMap.put(TRANSITION, FAILURE_TRANSITION);
                    statusMap.put(TRANSITION_COMMENT,
                            SURVEY_INSERT_FAILURE);
                }
            }
        } catch (Exception e) {
            statusMap.put(TRANSITION, FAILURE_TRANSITION);
            statusMap.put(TRANSITION_COMMENT, SURVEY_TECHNICAL_ERROR);
            logger.error("Exception in processDynamicSurveyDCR : ", e);
        }
        return statusMap;
    }

    /**
     * Method to get the task file as a xml document.
     *
     * @param taskSimpleFile Task file of CSSimpleFile object
     * @return Returns xml document of the task file.
     */
    public Document getTaskDocument(CSSimpleFile taskSimpleFile) {
        logger.info("PollSurveyTask : getTaskDocument");
        Document document = null;
        try {
            byte[] taskSimpleFileByteArray = taskSimpleFile.read(0, -1);
            String taskSimpleFileString = new String(
                    taskSimpleFileByteArray);
            logger.debug("taskSimpleFileString : " + taskSimpleFileString);
            document = DocumentHelper.parseText(taskSimpleFileString);
            logger.debug("document : " + document.asXML());
        } catch (Exception e) {
            logger.error("Exception in getTaskDocument: ", e);
        }
        return document;
    }
    
    /**
     * Checks for poll master data already exists
     *
     * @param document Document object of the polls DCR
     * @return Returns true if the poll data is already available in the database
     *         else returns false
     */
    public boolean isPollMasterDataAvailable(Document document) {
        logger.debug("PollSurveyTask : isPollMasterDataAvailable");
        PreparedStatement prepareStatement = null;
        Connection connection = null;
        ResultSet rs = null;
        boolean isPollDataAvailable = false;
        try {

            connection = postgre.getConnection();

            Long pollId = Long.parseLong(getDCRValue(document, ID_PATH));
            String lang = getDCRValue(document, LANG_PATH);

            String pollMasterQuery = "SELECT COUNT(*) FROM POLL_MASTER WHERE POLL_ID = ? AND LANG = ?";
            logger.debug("pollMasterQuery in isPollMasterDataAvailable: "
                    + pollMasterQuery);
            prepareStatement = connection
                    .prepareStatement(pollMasterQuery);
            prepareStatement.setLong(1, pollId);
            prepareStatement.setString(2, lang);
            rs = prepareStatement.executeQuery();
            while (rs.next()) {
                int count = rs.getInt(1);
                if (count > 0) {
                    isPollDataAvailable = true;
                }
            }
            logger.info("isPollDataAvailable : " + isPollDataAvailable);

        } catch (Exception e) {
            logger.error("Exception in isPollMasterDataAvailable : ", e);
        } finally {
            postgre.releaseConnection(connection, prepareStatement, rs);
            logger.info(
                    "Released connection in isPollMasterDataAvailable");
        }
        return isPollDataAvailable;
    }

    /**
     * Method creates the business object, calls insert master and option data
     *
     * @param document Document object of the polls DCR
     * @return Returns status of the insert poll data as boolean
     */
    public boolean insertPollData(Document document, String fileName) {
        logger.info("PollSurveyTask : insertPollData");
        Connection connection = null;
        boolean isPollDataInserted = false;
        try {

            connection = postgre.getConnection();
            TSPollsBO pollsBO = new TSPollsBO();
            pollsBO.setPollId(getDCRValue(document, ID_PATH));
            pollsBO.setLang(getDCRValue(document, LANG_PATH));
            pollsBO.setQuestion(getDCRValue(document, QUESTION_PATH));
            pollsBO.setStartDate(
                    getDCRValue(document, POLL_START_DATE_PATH));
            pollsBO.setEndDate(getDCRValue(document, POLL_END_DATE_PATH));
            pollsBO.setPersona(getDCRValue(document, PERSONA_PATH));
            pollsBO.setServiceEntities(getDCRValue(document, SERVICE_ENTITIES));
            pollsBO.setTopics(getDCRValue(document, TOPICS));
            pollsBO.setFileName(fileName);
            logger.debug("PollsBO : " + pollsBO);

            int result = insertPollMasterData(pollsBO, connection);
            logger.info("insertPollData result : " + result);
            if (result > 0) {
                logger.info("Poll Master Data Inserted");

                boolean isPollOptionInserted = insertPollOptionsData(
                        pollsBO, document, connection);

                if (isPollOptionInserted) {
                    connection.commit();
                    logger.info("Poll Option Inserted");
                    isPollDataInserted = true;
                } else {
                    connection.rollback();
                    logger.info(
                            "insertPollData Option batch insert failed");
                }
            } else {
                connection.rollback();
                logger.info("Poll master insert failed");
            }

        } catch (Exception e) {
            try {
                if (connection != null) {
                    connection.rollback();
                }
                logger.error("Exception in insertPollData: ", e);
            } catch (SQLException ex) {
                logger.error(
                        "Exception in insertPollData rollback catch block : ",
                        ex);
            }
        } finally {
            postgre.releaseConnection(connection, null, null);
            logger.info("Released insertPollData connection");
        }
        return isPollDataInserted;
    }

    /**
     * Method inserts the polls master data
     *
     * @param pollsBO    Polls business object
     * @param connection Database connection object
     * @return Returns number of rows affected by query execution
     * @throws SQLException
     */
    public int insertPollMasterData(TSPollsBO pollsBO, Connection connection)
            throws SQLException {
        PreparedStatement preparedStatement = null;
        int result = 0;
        try {
            logger.info("PollSurveyTask : insertPollMasterData");
            String pollMasterQuery = "INSERT INTO POLL_MASTER (POLL_ID, "
                    + "LANG, QUESTION, START_DATE, END_DATE, PERSONA, SERVICE_ENTITIES, TOPICS, STATUS, FILE_NAME) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            logger.debug("insertPollMasterData pollMasterQuery : "
                    + pollMasterQuery);
            connection.setAutoCommit(false);
            preparedStatement = connection
                    .prepareStatement(pollMasterQuery);
            preparedStatement.setLong(1,
                    Long.parseLong(pollsBO.getPollId()));
            preparedStatement.setString(2, pollsBO.getLang());
            preparedStatement.setString(3, pollsBO.getQuestion());
            preparedStatement.setDate(4, getDate(pollsBO.getStartDate()));
            preparedStatement.setDate(5, getDate(pollsBO.getEndDate()));
            preparedStatement.setString(6, pollsBO.getPersona());
            preparedStatement.setString(7, pollsBO.getServiceEntities());
            preparedStatement.setString(8, pollsBO.getTopics());  
            preparedStatement.setString(9, PUBLISHED_STATUS);
            preparedStatement.setString(10, pollsBO.getFileName());
            result = preparedStatement.executeUpdate();
            logger.info("insertPollMasterData result : " + result);
        } catch (NumberFormatException | SQLException e) {
            logger.error("Exception in insertPollMasterData: ", e);
            throw e;
        } finally {
            postgre.releaseConnection(null, preparedStatement, null);
            logger.info("Released insertPollMasterData connection");
        }
        return result;
    }

    /**
     * Method inserts the polls option data
     *
     * @param pollsBO    Polls business object
     * @param document   Document object of the polls DCR
     * @param connection Database connection object
     * @return Returns true for successful insert else false for failure.
     * @throws SQLException
     */
    public boolean insertPollOptionsData(TSPollsBO pollsBO,
            Document document, Connection connection) throws SQLException {
        PreparedStatement preparedStatement = null;
        boolean isPollOptionInserted = false;
        try {
            logger.info("PollSurveyTask : insertPollOptionsData");
            String pollOptionQuery = "INSERT INTO POLL_OPTION"
                    + " (OPTION_ID, LANG, POLL_ID, OPTION_LABEL, "
                    + "OPTION_VALUE) VALUES(?, ?, ?, ?, ?)";
            preparedStatement = connection
                    .prepareStatement(pollOptionQuery);
            logger.debug("insertPollOptionsData pollOptionQuery : "
                    + pollOptionQuery);

            List<Node> nodes = document.selectNodes(OPTION_PATH);
            long optionId = 1l;
            for (Node node : nodes) {
                logger.debug(
                        "insertPollOptionsData optionId : " + optionId);
                String label = node.selectSingleNode(OPTION_LABEL)
                        .getText();
                String value = node.selectSingleNode(OPTION_VALUE)
                        .getText();
                logger.debug("insertPollOptionsData label : " + label);
                logger.debug("insertPollOptionsData value : " + value);
                preparedStatement.setLong(1, optionId);
                preparedStatement.setString(2, pollsBO.getLang());
                preparedStatement.setLong(3,
                        Long.parseLong(pollsBO.getPollId()));
                preparedStatement.setString(4, label);
                preparedStatement.setString(5, value);
                preparedStatement.addBatch();
                optionId++;
            }
            int[] optionBatch = preparedStatement.executeBatch();
            logger.info("insertPollOptionsData optionBatch length : "
                    + optionBatch.length);
            postgre.releaseConnection(null, preparedStatement, null);

            if (optionBatch.length == optionId - 1) {
                isPollOptionInserted = true;
            }
        } catch (NumberFormatException | SQLException e) {
            logger.error("Exception in insertPollOptionsData: ", e);
            throw e;
        } finally {
            postgre.releaseConnection(null, preparedStatement, null);
            logger.info("Released insertPollOptionsData connection");
        }
        return isPollOptionInserted;
    }

    /**
     * Method creates the business object, calls update master and option data
     *
     * @param document Document object of the polls DCR
     * @return Returns status of the update poll data as boolean
     */
    public boolean updatePollData(Document document, String fileName) {
        logger.info("PollSurveyTask : updatePollData");
        Connection connection = null;
        boolean isPollDataInserted = false;
        try {

            connection = postgre.getConnection();

            TSPollsBO pollsBO = new TSPollsBO();
            pollsBO.setPollId(getDCRValue(document, ID_PATH));
            pollsBO.setLang(getDCRValue(document, LANG_PATH));
            pollsBO.setQuestion(getDCRValue(document, QUESTION_PATH));
            pollsBO.setEndDate(getDCRValue(document, POLL_END_DATE_PATH));
            pollsBO.setPersona(getDCRValue(document, PERSONA_PATH));
            pollsBO.setServiceEntities(getDCRValue(document, SERVICE_ENTITIES));
            pollsBO.setTopics(getDCRValue(document, TOPICS));
            pollsBO.setServiceEntities(getDCRValue(document, SERVICE_ENTITIES));
            pollsBO.setTopics(getDCRValue(document, TOPICS));
            pollsBO.setFileName(fileName);
            logger.debug("PollsBO : " + pollsBO);
            
            int result = updatePollMasterData(pollsBO, connection);

            if (result > 0) {
                logger.info("Poll Master Data Updated");

                boolean isPollOptionUpdated = updatePollOptionsData(
                        pollsBO, document, connection);
                if (isPollOptionUpdated) {
                    connection.commit();
                    logger.info("Poll Option Updated");
                    isPollDataInserted = true;
                } else {
                    connection.rollback();
                    logger.info(
                            "updatePollData Option batch update failed");
                }
            } else {
                connection.rollback();
                logger.info("Poll master update failed");
            }

        } catch (Exception e) {
            try {
                if (connection != null) {
                    connection.rollback();
                }
                logger.error("Exception in updatePollData: ", e);
            } catch (SQLException ex) {
                logger.error(
                        "Exception in updatePollData rollback catch block : ",
                        ex);
            }
        } finally {
            postgre.releaseConnection(connection, null, null);
            logger.info("Released updatePollData connection");
        }
        return isPollDataInserted;
    }

    /**
     * Method updates the polls master data
     *
     * @param pollsBO    Polls business object
     * @param connection Database connection object
     * @return Returns number of rows affected by query execution
     * @throws SQLException
     */
    public int updatePollMasterData(TSPollsBO pollsBO, Connection connection)
            throws SQLException {
        PreparedStatement preparedStatement = null;
        int result = 0;
        try {
            logger.info("PollSurveyTask : updatePollMasterData");
            String pollMasterQuery = "UPDATE POLL_MASTER SET QUESTION = ?, "
                    + "END_DATE = ?, PERSONA = ?, SERVICE_ENTITIES = ?, TOPICS = ?, STATUS = ?, FILE_NAME = ? "
                    + "WHERE POLL_ID = ? AND LANG = ?";
            logger.debug("updatePollMasterData pollMasterQuery : "
                    + pollMasterQuery);
            connection.setAutoCommit(false);
            preparedStatement = connection
                    .prepareStatement(pollMasterQuery);
            preparedStatement.setString(1, pollsBO.getQuestion());
            preparedStatement.setDate(2, getDate(pollsBO.getEndDate()));
            preparedStatement.setString(3, pollsBO.getPersona());
            preparedStatement.setString(4, pollsBO.getServiceEntities());
            preparedStatement.setString(5, pollsBO.getTopics()); 
            preparedStatement.setString(6, PUBLISHED_STATUS);
            preparedStatement.setString(7, pollsBO.getFileName());
            preparedStatement.setLong(8,
                    Long.parseLong(pollsBO.getPollId()));
            preparedStatement.setString(9, pollsBO.getLang());

            result = preparedStatement.executeUpdate();
            logger.info("updatePollMasterData result : " + result);
        } catch (NumberFormatException | SQLException e) {
            logger.error("Exception in updatePollMasterData: ", e);
            throw e;
        } finally {
            postgre.releaseConnection(null, preparedStatement, null);
            logger.info("Released updatePollMasterData connection");
        }
        return result;
    }
    
    /**
     * Method updates the delete status as unpublished in polls master data
     *
     * @param pollId    Poll id of the deleted poll
     * @param lang  Language of the deleted poll
     * @param connection Database connection object
     * @return Returns status of the update in the Map object
     */
    public Map<String, String> updateDeletePollMasterData(String pollId, String lang, Connection connection)
            throws SQLException {
        PreparedStatement preparedStatement = null;
        int result = 0;
        HashMap<String, String> statusMap = new HashMap<>();
        try {
            logger.info("PollSurveyTask : updateDeletePollMasterData");
            String pollMasterQuery = "UPDATE POLL_MASTER SET STATUS = ? "
                    + "WHERE POLL_ID = ? AND LANG = ?";
            logger.debug("updatePollMasterData pollMasterQuery : "
                    + pollMasterQuery);
            connection.setAutoCommit(false);
            preparedStatement = connection
                    .prepareStatement(pollMasterQuery);
            preparedStatement.setString(1, UNPUBLISHED_STATUS); 
            preparedStatement.setLong(2,
                    Long.parseLong(pollId));
            preparedStatement.setString(3, lang);

            result = preparedStatement.executeUpdate();
            logger.info("updateDeletePollMasterData result : " + result);
            if (result > 0) {
                connection.commit();
                logger.info("updateDeletePollMasterData transaction committed : ");
                statusMap.put(TRANSITION, SUCCESS_TRANSITION);
                statusMap.put(TRANSITION_COMMENT, POLL_UPDATE_SUCCESS);
            }else {
                statusMap.put(TRANSITION, FAILURE_TRANSITION);
                statusMap.put(TRANSITION_COMMENT, POLL_UPDATE_FAILURE);
            }
        } catch (NumberFormatException | SQLException e) {
            logger.error("Exception in updateDeletePollMasterData: ", e);
            statusMap.put(TRANSITION, FAILURE_TRANSITION);
            statusMap.put(TRANSITION_COMMENT, POLL_TECHNICAL_ERROR);
            throw e;
        } finally {
            postgre.releaseConnection(null, preparedStatement, null);
            logger.info("Released updateDeletePollMasterData connection");
        }
        return statusMap;
    }
    
    /**
     * Method updates the delete status as unpublished in survey master data
     *
     * @param surveyId    survey id of the deleted poll
     * @param lang  Language of the deleted poll
     * @param connection Database connection object
     * @return Returns status of the update in the Map object
     */
    public Map<String, String> updateDeleteSurveyMasterData(String surveyId, String lang, Connection connection)
            throws SQLException {
        PreparedStatement preparedStatement = null;
        int result = 0;
        HashMap<String, String> statusMap = new HashMap<>();
        try {
            logger.info("PollSurveyTask : updateDeleteSurveyMasterData");
            String surveyMasterQuery = "UPDATE SURVEY_MASTER SET STATUS = ? "
                    + "WHERE SURVEY_ID = ? AND LANG = ?";
            logger.debug("updateDeleteSurveyMasterData surveyMasterQuery : "
                    + surveyMasterQuery);
            connection.setAutoCommit(false);
            preparedStatement = connection
                    .prepareStatement(surveyMasterQuery);
            preparedStatement.setString(1, UNPUBLISHED_STATUS); 
            preparedStatement.setLong(2,
                    Long.parseLong(surveyId));
            preparedStatement.setString(3, lang);

            result = preparedStatement.executeUpdate();
            logger.info("updateDeleteSurveyMasterData result : " + result);
            if (result > 0) {
                connection.commit();
                logger.info("updateDeleteSurveyMasterData transaction committed : ");
                statusMap.put(TRANSITION, SUCCESS_TRANSITION);
                statusMap.put(TRANSITION_COMMENT, SURVEY_UPDATE_SUCCESS);
            }else {
                statusMap.put(TRANSITION, FAILURE_TRANSITION);
                statusMap.put(TRANSITION_COMMENT, SURVEY_UPDATE_FAILURE);
            }
        } catch (NumberFormatException | SQLException e) {
            logger.error("Exception in updateDeleteSurveyMasterData: ", e);
            statusMap.put(TRANSITION, FAILURE_TRANSITION);
            statusMap.put(TRANSITION_COMMENT, SURVEY_TECHNICAL_ERROR);
            throw e;
        } finally {
            postgre.releaseConnection(null, preparedStatement, null);
            logger.info("Released updateDeleteSurveyMasterData connection");
        }
        return statusMap;
    }
    
    /**
     * Method updates the delete status as unpublished in dynamic survey master data
     *
     * @param surveyId    survey id of the deleted poll
     * @param lang  Language of the deleted poll
     * @param connection Database connection object
     * @return Returns status of the update in the Map object
     */
    public Map<String, String> updateDeleteDynamicSurveyMasterData(String surveyId, String lang, Connection connection)
            throws SQLException {
        PreparedStatement preparedStatement = null;
        int result = 0;
        HashMap<String, String> statusMap = new HashMap<>();
        try {
            logger.info("PollSurveyTask : updateDeleteDynamicSurveyMasterData");
            String surveyMasterQuery = "UPDATE DYNAMIC_SURVEY_MASTER SET STATUS = ? "
                    + "WHERE SURVEY_ID = ? AND LANG = ?";
            logger.debug("updateDeleteDynamicSurveyMasterData dynamicSurveyMasterQuery : "
                    + surveyMasterQuery);
            connection.setAutoCommit(false);
            preparedStatement = connection
                    .prepareStatement(surveyMasterQuery);
            preparedStatement.setString(1, UNPUBLISHED_STATUS); 
            preparedStatement.setLong(2,
                    Long.parseLong(surveyId));
            preparedStatement.setString(3, lang);

            result = preparedStatement.executeUpdate();
            logger.info("updateDeleteDynamicSurveyMasterData result : " + result);
            if (result > 0) {
                connection.commit();
                logger.info("updateDeleteDynamicSurveyMasterData transaction committed : ");
                statusMap.put(TRANSITION, SUCCESS_TRANSITION);
                statusMap.put(TRANSITION_COMMENT, SURVEY_UPDATE_SUCCESS);
            }else {
                statusMap.put(TRANSITION, FAILURE_TRANSITION);
                statusMap.put(TRANSITION_COMMENT, SURVEY_UPDATE_FAILURE);
            }
        } catch (NumberFormatException | SQLException e) {
            logger.error("Exception in updateDeleteDynamicSurveyMasterData: ", e);
            statusMap.put(TRANSITION, FAILURE_TRANSITION);
            statusMap.put(TRANSITION_COMMENT, SURVEY_TECHNICAL_ERROR);
            throw e;
        } finally {
            postgre.releaseConnection(null, preparedStatement, null);
            logger.info("Released updateDeleteDynamicSurveyMasterData connection");
        }
        return statusMap;
    }
    
    /**
     * Method updates the polls option data
     *
     * @param pollsBO    Polls business object
     * @param document   Document object of the polls DCR
     * @param connection Database connection object
     * @return Returns true for successful update else false for failure.
     * @throws SQLException
     */
    public boolean updatePollOptionsData(TSPollsBO pollsBO,
            Document document, Connection connection) throws SQLException {
        PreparedStatement preparedStatement = null;
        boolean isPollOptionUpdated = false;
        try {
            logger.info("PollSurveyTask : updatePollOptionsData");
            String pollOptionQuery = "UPDATE POLL_OPTION SET "
                    + "OPTION_LABEL = ? WHERE OPTION_ID = ? AND "
                    + "POLL_ID = ? AND LANG = ?";
            preparedStatement = connection
                    .prepareStatement(pollOptionQuery);
            logger.debug("updatePollMasterData pollOptionQuery : "
                    + pollOptionQuery);

            List<Node> nodes = document.selectNodes(OPTION_PATH);
            long optionId = 1l;
            for (Node node : nodes) {
                logger.debug("updatePollMasterData optionId : " + optionId);
                String label = node.selectSingleNode(OPTION_LABEL)
                        .getText();
                logger.debug("updatePollMasterData label : " + label);
                preparedStatement.setString(1, label);
                preparedStatement.setLong(2, optionId);
                preparedStatement.setLong(3,
                        Long.parseLong(pollsBO.getPollId()));
                preparedStatement.setString(4, pollsBO.getLang());
                preparedStatement.addBatch();
                optionId++;
            }
            int[] optionBatch = preparedStatement.executeBatch();
            logger.info("updatePollMasterData optionBatch length : "
                    + optionBatch.length);

            if (optionBatch.length == optionId - 1) {
                isPollOptionUpdated = true;
            }
        } catch (NumberFormatException | SQLException e) {
            logger.error("Exception in insertPollOptionsData: ", e);
            throw e;
        } finally {
            postgre.releaseConnection(null, preparedStatement, null);
            logger.info("Released insertPollOptionsData connection");
        }
        return isPollOptionUpdated;
    }

    /**
     * Checks for survey master data already exists
     *
     * @param document Document object of the survey DCR
     * @return Returns true if the survey data is already available in the database
     *         else returns false
     */
    public boolean isSurveyMasterDataAvailable(Document document) {
        logger.info("PollSurveyTask : isSurveyMasterDataAvailable");
        PreparedStatement prepareStatement = null;
        Connection connection = null;
        ResultSet rs = null;
        boolean isSurveyDataAvailable = false;
        try {

            connection = postgre.getConnection();

            Long surveyId = Long.parseLong(getDCRValue(document, ID_PATH));
            String lang = getDCRValue(document, LANG_PATH);

            String surveyMasterQuery = "SELECT COUNT(*) FROM SURVEY_MASTER WHERE SURVEY_ID = ? AND LANG = ?";
            logger.debug("surveyMasterQuery isSurveyMasterDataAvailable : "
                    + surveyMasterQuery);
            prepareStatement = connection
                    .prepareStatement(surveyMasterQuery);
            prepareStatement.setLong(1, surveyId);
            prepareStatement.setString(2, lang);
            rs = prepareStatement.executeQuery();
            while (rs.next()) {
                int count = rs.getInt(1);
                if (count > 0) {
                    isSurveyDataAvailable = true;
                }
            }
            logger.info(
                    "isSurveyDataAvailable : " + isSurveyDataAvailable);

        } catch (Exception e) {
            logger.error("Exception in isSurveyMasterDataAvailable: ", e);
        } finally {
            postgre.releaseConnection(connection, prepareStatement, rs);
            logger.info("isSurveyMasterDataAvailable Released connection");
        }
        return isSurveyDataAvailable;
    }
    
    
    /**
     * Checks for dynamic survey master data already exists
     *
     * @param document Document object of the survey DCR
     * @return Returns true if the dynamic survey data is already available in the database
     *         else returns false
     */
    public boolean isDynamicSurveyMasterDataAvailable(Document document) {
        logger.info("PollSurveyTask : isDynamicSurveyMasterDataAvailable");
        PreparedStatement prepareStatement = null;
        Connection connection = null;
        ResultSet rs = null;
        boolean isSurveyDataAvailable = false;
        try {

            connection = postgre.getConnection();

            Long surveyId = Long.parseLong(getDCRValue(document, ID_PATH));
            String lang = getDCRValue(document, LANG_PATH);

            String surveyMasterQuery = "SELECT COUNT(*) FROM DYNAMIC_SURVEY_MASTER WHERE SURVEY_ID = ? AND LANG = ?";
            logger.debug("surveyMasterQuery isDynamicSurveyMasterDataAvailable : "
                    + surveyMasterQuery);
            prepareStatement = connection
                    .prepareStatement(surveyMasterQuery);
            prepareStatement.setLong(1, surveyId);
            prepareStatement.setString(2, lang);
            rs = prepareStatement.executeQuery();
            while (rs.next()) {
                int count = rs.getInt(1);
                if (count > 0) {
                    isSurveyDataAvailable = true;
                }
            }
            logger.info(
                    "isSurveyDataAvailable : " + isSurveyDataAvailable);

        } catch (Exception e) {
            logger.error("Exception in isDynamicSurveyMasterDataAvailable: ", e);
        } finally {
            postgre.releaseConnection(connection, prepareStatement, rs);
            logger.info("isDynamicSurveyMasterDataAvailable Released connection");
        }
        return isSurveyDataAvailable;
    }

    /**
     * Method creates the business object, calls insert master, question and option
     * data
     *
     * @param document Document object of the survey DCR
     * @return Returns status of the insert survey data as boolean
     */
    public boolean insertSurveyData(Document document, String fileName) {
        logger.info("PollSurveyTask : insertSurveyData");
        Connection connection = null;
        boolean isSurveyDataInserted = false;
        try {

            connection = postgre.getConnection();
            connection.setAutoCommit(false);
            TSSurveyBO surveyBO = new TSSurveyBO();
            surveyBO.setSurveyId(getDCRValue(document, ID_PATH));
            surveyBO.setLang(getDCRValue(document, LANG_PATH));
            surveyBO.setTitle(getDCRValue(document, TITLE_PATH));
            surveyBO.setDescription(
                    getDCRValue(document, DESCRIPTION_PATH));
            surveyBO.setStartDate(
                    getDCRValue(document, SURVEY_START_DATE_PATH));
            surveyBO.setEndDate(
                    getDCRValue(document, SURVEY_END_DATE_PATH));
            surveyBO.setPersona(getDCRValue(document, PERSONA_PATH));
            surveyBO.setServiceEntities(getDCRValue(document, SERVICE_ENTITIES));
            surveyBO.setTopics(getDCRValue(document, TOPICS));
            surveyBO.setSubmitType(getDCRValue(document, SUBMIT_TYPE));
            surveyBO.setEstimatedTime(getDCRValue(document, ESTIMATED_TIME));
            surveyBO.setFileName(fileName);
            logger.debug("SurveyBO : " + surveyBO);

            int result = insertSurveyMasterData(surveyBO, connection);
            logger.info("result : " + result);
            if (result > 0) {
                logger.info("Survey Master Data Inserted");
                isSurveyDataInserted = insertSurveyQuestionData(surveyBO,
                        document, connection);
            } else {
                connection.rollback();
                logger.info("Survey master insert failed");
            }

            if (isSurveyDataInserted) {
                connection.commit();
                logger.info("Survey insert transaction committed");
            }

        } catch (Exception e) {
            try {
                if (connection != null) {
                    connection.rollback();
                }
                logger.error("Exception in insertSurveyData : ", e);
            } catch (SQLException ex) {
                logger.error(
                        "Exception in insertSurveyData rollback catch block : ",
                        ex);
            }
        } finally {
            postgre.releaseConnection(connection, null, null);
            logger.info("Released insertSurveyData connection");
        }
        return isSurveyDataInserted;
    }
    
    /**
     * Method creates the business object, calls insert master, question and option
     * data
     *
     * @param document Document object of the survey DCR
     * @return Returns status of the insert survey data as boolean
     */
    public boolean insertDynamicSurveyData(Document document, String fileName) {
        logger.info("PollSurveyTask : insertDynamicSurveyData");
        Connection connection = null;
        boolean isSurveyDataInserted = false;
        try {

            connection = postgre.getConnection();
            connection.setAutoCommit(false);
            TSSurveyBO surveyBO = new TSSurveyBO();
            surveyBO.setSurveyId(getDCRValue(document, ID_PATH));
            surveyBO.setLang(getDCRValue(document, LANG_PATH));
            surveyBO.setTitle(getDCRValue(document, TITLE_PATH));
            surveyBO.setDescription(
                    getDCRValue(document, DESCRIPTION_PATH));
            surveyBO.setStartDate(
                    getDCRValue(document, SURVEY_START_DATE_PATH));
            surveyBO.setEndDate(
                    getDCRValue(document, SURVEY_END_DATE_PATH));
            surveyBO.setPersona(getDCRValue(document, PERSONA_PATH));
            surveyBO.setServiceEntities(getDCRValue(document, SERVICE_ENTITIES));
            surveyBO.setTopics(getDCRValue(document, TOPICS));
            surveyBO.setSubmitType(getDCRValue(document, SUBMIT_TYPE));
            surveyBO.setEstimatedTime(getDCRValue(document, ESTIMATED_TIME));
            surveyBO.setFileName(fileName);
            logger.debug("SurveyBO : " + surveyBO);
            
            Long surveyMasterId = getNextSequenceValue(
                    "dynamic_survey_master_survey_master_id_seq",
                    postgre.getConnection());
            surveyBO.setSurveyMasterId(surveyMasterId);

            int result = insertDynamicSurveyMasterData(surveyBO, connection);
            logger.info("result : " + result);
            if (result > 0) {
                logger.info("Dynamic Survey Master Data Inserted");
                isSurveyDataInserted = insertDynamicSurveyQuestionData(surveyBO,
                        document, connection);
            } else {
                connection.rollback();
                logger.info("Dynamic Survey master insert failed");
            }

            if (isSurveyDataInserted) {
                connection.commit();
                logger.info("Dynamic Survey insert transaction committed");
            }

        } catch (Exception e) {
            try {
                if (connection != null) {
                    connection.rollback();
                }
                logger.error("Exception in insertDynamicSurveyData : ", e);
            } catch (SQLException ex) {
                logger.error(
                        "Exception in insertDynamicSurveyData rollback catch block : ",
                        ex);
            }
        } finally {
            postgre.releaseConnection(connection, null, null);
            logger.info("Released insertDynamicSurveyData connection");
        }
        return isSurveyDataInserted;
    }

    /**
     * Method inserts the survey master data
     *
     * @param surveyBO   Survey business object
     * @param connection Database connection object
     * @return Returns number of rows affected by query execution
     * @throws SQLException
     */
    public int insertSurveyMasterData(TSSurveyBO surveyBO,
            Connection connection) throws SQLException {
        PreparedStatement preparedStatement = null;
        int result = 0;
        try {
            logger.info("PollSurveyTask : insertSurveyMasterData");
            String surveyMasterQuery = "INSERT INTO SURVEY_MASTER ("
                    + "SURVEY_ID, LANG, SURVEY_TITLE, SURVEY_DESCRIPTION, "
                    + "START_DATE, END_DATE, PERSONA, SERVICE_ENTITIES, TOPICS, SUBMIT_TYPE, STATUS, FILE_NAME, ESTIMATED_TIME) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            logger.debug("insertSurveyMasterData surveyMasterQuery : "
                    + surveyMasterQuery);
            preparedStatement = connection
                    .prepareStatement(surveyMasterQuery);
            preparedStatement.setLong(1,
                    Long.parseLong(surveyBO.getSurveyId()));
            preparedStatement.setString(2, surveyBO.getLang());
            preparedStatement.setString(3, surveyBO.getTitle());
            preparedStatement.setString(4, surveyBO.getDescription());
            preparedStatement.setDate(5, getDate(surveyBO.getStartDate()));
            preparedStatement.setDate(6, getDate(surveyBO.getEndDate()));
            preparedStatement.setString(7, surveyBO.getPersona());
            preparedStatement.setString(8, surveyBO.getServiceEntities());
            preparedStatement.setString(9, surveyBO.getTopics());
            preparedStatement.setString(10, surveyBO.getSubmitType());
            preparedStatement.setString(11, PUBLISHED_STATUS);
            preparedStatement.setString(12, surveyBO.getFileName());
            preparedStatement.setString(13, surveyBO.getEstimatedTime());
            result = preparedStatement.executeUpdate();
            logger.info("insertSurveyMasterData result : " + result);
        } catch (NumberFormatException | SQLException e) {
            logger.error("Exception in insertSurveyMasterData: ", e);
            throw e;
        } finally {
            postgre.releaseConnection(null, preparedStatement, null);
            logger.info("Released insertSurveyMasterData connection");
        }
        return result;
    }
    
    /**
     * Method inserts the survey master data
     *
     * @param surveyBO   Survey business object
     * @param connection Database connection object
     * @return Returns number of rows affected by query execution
     * @throws SQLException
     */
    public int insertDynamicSurveyMasterData(TSSurveyBO surveyBO,
            Connection connection) throws SQLException {
        PreparedStatement preparedStatement = null;
        int result = 0;
        try {
            logger.info("PollSurveyTask : insertDynamicSurveyMasterData");
            String surveyMasterQuery = "INSERT INTO DYNAMIC_SURVEY_MASTER ("
                    + "SURVEY_MASTER_ID, SURVEY_ID, LANG, SURVEY_TITLE, SURVEY_DESCRIPTION, "
                    + "START_DATE, END_DATE, PERSONA, SERVICE_ENTITIES, TOPICS, SUBMIT_TYPE, "
                    + "ESTIMATED_TIME, STATUS, FILE_NAME) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            logger.debug("insertDynamicSurveyMasterData surveyMasterQuery : "
                    + surveyMasterQuery);
            preparedStatement = connection
                    .prepareStatement(surveyMasterQuery);
            preparedStatement.setLong(1, surveyBO.getSurveyMasterId());
            preparedStatement.setLong(2,
                    Long.parseLong(surveyBO.getSurveyId()));
            preparedStatement.setString(3, surveyBO.getLang());
            preparedStatement.setString(4, surveyBO.getTitle());
            preparedStatement.setString(5, surveyBO.getDescription());
            preparedStatement.setDate(6, getDate(surveyBO.getStartDate()));
            preparedStatement.setDate(7, getDate(surveyBO.getEndDate()));
            preparedStatement.setString(8, surveyBO.getPersona());
            preparedStatement.setString(9, surveyBO.getServiceEntities());
            preparedStatement.setString(10, surveyBO.getTopics());
            preparedStatement.setString(11, surveyBO.getSubmitType());
            preparedStatement.setString(12, surveyBO.getEstimatedTime());
            preparedStatement.setString(13, PUBLISHED_STATUS);
            preparedStatement.setString(14, surveyBO.getFileName());
            result = preparedStatement.executeUpdate();
            logger.info("insertDynamicSurveyMasterData result : " + result);
        } catch (NumberFormatException | SQLException e) {
            logger.error("Exception in insertDynamicSurveyMasterData: ", e);
            throw e;
        } finally {
            postgre.releaseConnection(null, preparedStatement, null);
            logger.info("Released insertDynamicSurveyMasterData connection");
        }
        return result;
    }

    /**
     * Method inserts the survey question data
     *
     * @param surveyBO   Survey business object
     * @param document   Document object of the survey DCR
     * @param connection Database connection object
     * @return Returns true for successful insert else false for failure.
     * @throws SQLException
     */
    public boolean insertSurveyQuestionData(TSSurveyBO surveyBO,
            Document document, Connection connection) throws SQLException {
        PreparedStatement preparedStatement = null;
        boolean result = false;
        try {
            logger.info("PollSurveyTask : insertSurveyQuestionData");
            String surveyQuestionQuery = "INSERT INTO SURVEY_QUESTION "
                    + "(QUESTION_ID, SURVEY_ID, LANG, QUESTION_NO, "
                    + "QUESTION_TYPE, QUESTION) VALUES (?, ?, ?, ?, ?, ?)";
            preparedStatement = connection
                    .prepareStatement(surveyQuestionQuery);
            logger.debug("insertSurveyQuestionData surveyQuestionQuery : "
                    + surveyQuestionQuery);

            List<Node> nodes = document.selectNodes(FIELD_PATH);
            int questionNo = 1;
            for (Node node : nodes) {
                logger.debug("insertSurveyQuestionData questionNo : "
                        + questionNo);

                String questionType = node
                        .selectSingleNode(".//field-type").getText();
                logger.debug("insertSurveyQuestionData questionType : "
                        + questionType);

                if (questionType != null && !"".equals(questionType) && !"button".equalsIgnoreCase(questionType)) {

                    Long questionId = getNextSequenceValue(
                            "survey_question_question_id_seq",
                            postgre.getConnection());
                    logger.debug("insertSurveyQuestionData questionId : "
                            + questionId);

                    String question = node.selectSingleNode(".//question")
                            .getText();
                    logger.debug("question : " + question);
                    preparedStatement.setLong(1, questionId);
                    preparedStatement.setLong(2,
                            Long.parseLong(surveyBO.getSurveyId()));
                    preparedStatement.setString(3, surveyBO.getLang());
                    preparedStatement.setInt(4, questionNo);
                    preparedStatement.setString(5, questionType);
                    preparedStatement.setString(6, question);
                    int questionResult = preparedStatement.executeUpdate();

                    if (questionResult > 0) {
                        result = true;
                        logger.info("Survey Question Inserted");
                        if(optionsList.contains(questionType)) {
                            surveyBO.setQuestionId(questionId);
                            surveyBO.setQuestionNo(questionNo);
                            boolean isOptionsInserted = insertSurveyOptionData(
                                    surveyBO, node, connection);
    
                            if (isOptionsInserted) {
                                result = true;
                            } else {
                                result = false;
                                connection.rollback();
                                logger.info(
                                        "insertSurveyQuestionData Option batch insert failed");
                                break;
                            }
                            questionNo++;
                        }
                    } else {
                        result = false;
                        connection.rollback();
                        logger.info(
                                "insertSurveyQuestionData Question insert failed");
                        break;
                    }
                }
            }
        } catch (NumberFormatException | SQLException e) {
            logger.error("Exception in insertSurveyQuestionData: ", e);
            throw e;
        } finally {
            postgre.releaseConnection(null, preparedStatement, null);
            logger.info("Released insertSurveyQuestionData connection");
        }
        return result;
    }
    
    /**
     * Method inserts the dynamic survey question data
     *
     * @param surveyBO   Survey business object
     * @param document   Document object of the survey DCR
     * @param connection Database connection object
     * @return Returns true for successful insert else false for failure.
     * @throws SQLException
     */
    public boolean insertDynamicSurveyQuestionData(TSSurveyBO surveyBO,
            Document document, Connection connection) throws SQLException {
        PreparedStatement preparedStatement = null;
        boolean result = false;
        try {
            logger.info("PollSurveyTask : insertDynamicSurveyQuestionData");
            String surveyQuestionQuery = "INSERT INTO DYNAMIC_SURVEY_QUESTION "
                    + "(SURVEY_QUESTION_ID, SURVEY_MASTER_ID, QUESTION_ID, "
                    + "QUESTION_TYPE, QUESTION) VALUES (?, ?, ?, ?, ?)";
            preparedStatement = connection
                    .prepareStatement(surveyQuestionQuery);
            logger.debug("insertDynamicSurveyQuestionData surveyQuestionQuery : "
                    + surveyQuestionQuery);

            List<Node> nodes = document.selectNodes(OPTION_FIELD_PATH);
            for (Node node : nodes) {
                
                Long surveyQuestionId = getNextSequenceValue(
                        "dynamic_survey_question_survey_question_id_seq",
                        postgre.getConnection());
                surveyBO.setSurveyQuestionId(surveyQuestionId);
                
                String questionIdStr = node
                        .selectSingleNode(".//question-id").getText();
                if(questionIdStr != null && !"".equals(questionIdStr)) {
                    
                    long questionId = Long.parseLong(questionIdStr);
                    logger.debug("insertDynamicSurveyQuestionData questionId : "
                        + questionId);
                    
                    String questionType = node
                            .selectSingleNode(".//field-type").getText();
                    logger.debug("insertDynamicSurveyQuestionData questionType : "
                            + questionType);

                    if (questionType != null && !"".equals(questionType) && !"button".equalsIgnoreCase(questionType)) {

                        String question = node.selectSingleNode(".//question")
                                .getText();
                        logger.debug("question : " + question);
                        
                        preparedStatement.setLong(1, surveyQuestionId);
                        preparedStatement.setLong(2, surveyBO.getSurveyMasterId());
                        preparedStatement.setLong(3, questionId);
                        preparedStatement.setString(4, questionType);
                        preparedStatement.setString(5, question);
                        int questionResult = preparedStatement.executeUpdate();

                        if (questionResult > 0) {
                            result = true;
                            logger.info("Survey Question Inserted");
                            if(optionsList.contains(questionType)) {
                                boolean isOptionsInserted = insertDynamicSurveyOptionData(
                                        surveyBO, node, connection);
    
                                if (isOptionsInserted) {
                                    result = true;
                                } else {
                                    result = false;
                                    connection.rollback();
                                    logger.info(
                                            "insertDynamicSurveyQuestionData Option batch insert failed");
                                    break;
                                }
                            }
                        } else {
                            result = false;
                            connection.rollback();
                            logger.info(
                                    "insertDynamicSurveyQuestionData Question insert failed");
                            break;
                        }
                    }
                    
                }
                
            }
        } catch (NumberFormatException | SQLException e) {
            logger.error("Exception in insertDynamicSurveyQuestionData: ", e);
            throw e;
        } finally {
            postgre.releaseConnection(null, preparedStatement, null);
            logger.info("Released insertDynamicSurveyQuestionData connection");
        }
        return result;
    }

    /**
     * Method inserts the survey option data
     *
     * @param surveyBO   Survey business object
     * @param node       Node object of the survey option node
     * @param connection Database connection object
     * @return Returns true for successful insert else false for failure.
     * @throws SQLException
     */
    public boolean insertSurveyOptionData(TSSurveyBO surveyBO, Node node,
            Connection connection) throws SQLException {
        PreparedStatement preparedStatement = null;
        boolean result = false;
        try {
            logger.info("PollSurveyTask : insertSurveyOptionData");
            String surveyOptionQuery = "INSERT INTO "
                    + "SURVEY_OPTION (OPTION_ID, SURVEY_ID, "
                    + "LANG, QUESTION_ID, QUESTION_NO, "
                    + "OPTION_NO, OPTION_LABEL, IS_USER_INPUT, OPTION_VALUE) " + "VALUES "
                    + "(nextval('survey_option_option_id_seq')"
                    + ", ?, ?, ?, ?, ?, ?, ?, ?)";
            preparedStatement = connection
                    .prepareStatement(surveyOptionQuery);
            logger.debug("surveyOptionQuery : " + surveyOptionQuery);
            List<Node> optionNodes = node.selectNodes(".//option");
            int optionNo = 1;
            for (Node optnode : optionNodes) {
                logger.debug("optionNo : " + optionNo);
                String optionLabel = optnode.selectSingleNode(OPTION_LABEL)
                        .getText();
                String optionValue = optnode.selectSingleNode(OPTION_VALUE)
                        .getText();
                String isOpenResponse = optnode.selectSingleNode(IS_OPEN_RESPONSE)
                        .getText();
                logger.debug(optionLabel + " : " + optionValue);
                preparedStatement.setLong(1,
                        Long.parseLong(surveyBO.getSurveyId()));
                preparedStatement.setString(2, surveyBO.getLang());
                preparedStatement.setLong(3, surveyBO.getQuestionId());
                preparedStatement.setInt(4, surveyBO.getQuestionNo());
                preparedStatement.setInt(5, optionNo);
                preparedStatement.setString(6, optionLabel);
                preparedStatement.setString(7, isOpenResponse);
                preparedStatement.setString(8, optionValue);
                preparedStatement.addBatch();
                optionNo++;
            }
            int[] optionBatch = preparedStatement.executeBatch();
            logger.info("insertSurveyOptionData optionBatch length : "
                    + optionBatch.length);

            if (optionBatch.length == optionNo - 1) {
                logger.info("Survey Option Inserted");
                result = true;
            }

        } catch (NumberFormatException | SQLException e) {
            logger.error("Exception in insertSurveyOptionData: ", e);
            throw e;
        } finally {
            postgre.releaseConnection(null, preparedStatement, null);
            logger.info("Released insertSurveyOptionData connection");
        }
        return result;
    }
    
    /**
     * Method inserts the survey option data
     *
     * @param surveyBO   Survey business object
     * @param node       Node object of the survey option node
     * @param connection Database connection object
     * @return Returns true for successful insert else false for failure.
     * @throws SQLException
     */
    public boolean insertDynamicSurveyOptionData(TSSurveyBO surveyBO, Node node,
            Connection connection) throws SQLException {
        PreparedStatement preparedStatement = null;
        boolean result = false;
        try {
            logger.info("PollSurveyTask : insertDynamicSurveyOptionData");
            //OPTION_ID - should be generated in the db
            String surveyOptionQuery = "INSERT INTO DYNAMIC_SURVEY_OPTION "
                    + " (SURVEY_QUESTION_ID, OPTION_LABEL, IS_USER_INPUT, "
                    + "OPTION_VALUE) VALUES (?, ?, ?, ?) ";
            preparedStatement = connection
                    .prepareStatement(surveyOptionQuery);
            logger.debug("surveyOptionQuery : " + surveyOptionQuery);
            List<Node> optionNodes = node.selectNodes(".//option");
            int optionNo = 1;
            for (Node optnode : optionNodes) {
                logger.debug("optionNo : " + optionNo);
                String optionLabel = optnode.selectSingleNode(OPTION_LABEL)
                        .getText();
                String optionValue = optnode.selectSingleNode(OPTION_VALUE)
                        .getText();
                String isOpenResponse = optnode.selectSingleNode(IS_OPEN_RESPONSE)
                        .getText();
                logger.debug(optionLabel + " : " + optionValue);
                preparedStatement.setLong(1, surveyBO.getSurveyQuestionId());
                preparedStatement.setString(2, optionLabel);
                preparedStatement.setString(3, isOpenResponse);
                preparedStatement.setString(4, optionValue);
                preparedStatement.addBatch();
                optionNo++;
            }
            int[] optionBatch = preparedStatement.executeBatch();
            logger.info("insertDynamicSurveyOptionData optionBatch length : "
                    + optionBatch.length);

            if (optionBatch.length == optionNo - 1) {
                logger.info("Survey Option Inserted");
                result = true;
            }

        } catch (NumberFormatException | SQLException e) {
            logger.error("Exception in insertDynamicSurveyOptionData: ", e);
            throw e;
        } finally {
            postgre.releaseConnection(null, preparedStatement, null);
            logger.info("Released insertSurveyOptionData connection");
        }
        return result;
    }

    /**
     * Method creates the business object, calls update master and option data
     *
     * @param document Document object of the survey DCR
     * @return Returns status of the update survey data as boolean
     */
    public boolean updateSurveyData(Document document, String fileName) {
        logger.info("PollSurveyTask : updateSurveyData");
        PreparedStatement prepareStatement = null;
        PreparedStatement prepareStatementSurveyQuestion = null;
        PreparedStatement optionPrepareStatement = null;
        Connection connection = null;
        ResultSet rs = null;
        boolean isSurveyDataUpdated = false;
        try {

            connection = postgre.getConnection();
            connection.setAutoCommit(false);
            TSSurveyBO surveyBO = new TSSurveyBO();
            surveyBO.setSurveyId(getDCRValue(document, ID_PATH));
            surveyBO.setLang(getDCRValue(document, LANG_PATH));
            surveyBO.setTitle(getDCRValue(document, TITLE_PATH));
            surveyBO.setDescription(
                    getDCRValue(document, DESCRIPTION_PATH));
            surveyBO.setEndDate(
                    getDCRValue(document, SURVEY_END_DATE_PATH));
            surveyBO.setPersona(getDCRValue(document, PERSONA_PATH));
            surveyBO.setServiceEntities(getDCRValue(document, SERVICE_ENTITIES));
            surveyBO.setTopics(getDCRValue(document, TOPICS));
            surveyBO.setSubmitType(getDCRValue(document, SUBMIT_TYPE));
            surveyBO.setEstimatedTime(getDCRValue(document, ESTIMATED_TIME));
            surveyBO.setFileName(fileName);
            logger.debug("SurveyBO : " + surveyBO);

            int result = updateSurveyMasterData(surveyBO, connection);

            if (result > 0) {
                logger.info("Survey Master Data Updated");
                isSurveyDataUpdated = updateSurveyQuestionData(surveyBO,
                        document, connection);
            } else {
                if (connection != null) {
                    connection.rollback();
                }
                logger.info("Survey master update failed");
            }

            if (isSurveyDataUpdated) {
                if (connection != null) {
                    connection.commit();
                }
                logger.info("Survey update transaction committed");
            }

        } catch (Exception e) {
            try {
                if (connection != null) {
                    connection.rollback();
                }
                logger.error("Exception in updateSurveyMasterData : ", e);
            } catch (SQLException ex) {
                logger.error(
                        "Exception in updateSurveyMasterData rollback catch block : ",
                        ex);
            }
        } finally {
            postgre.releaseConnection(null, prepareStatementSurveyQuestion,
                    null);
            postgre.releaseConnection(null, optionPrepareStatement, null);
            postgre.releaseConnection(connection, prepareStatement, rs);
            logger.info("Released updateSurveyMasterData connection");
        }
        return isSurveyDataUpdated;
    }
    
    /**
     * Method creates the business object, calls update master and option data
     *
     * @param document Document object of the survey DCR
     * @return Returns status of the update survey data as boolean
     */
    public boolean updateDynamicSurveyData(Document document, String fileName) {
        logger.info("PollSurveyTask : updateDynamicSurveyData");
        PreparedStatement prepareStatement = null;
        PreparedStatement prepareStatementSurveyQuestion = null;
        PreparedStatement optionPrepareStatement = null;
        Connection connection = null;
        ResultSet rs = null;
        boolean isSurveyDataUpdated = false;
        try {

            connection = postgre.getConnection();
            connection.setAutoCommit(false);
            TSSurveyBO surveyBO = new TSSurveyBO();
            surveyBO.setSurveyId(getDCRValue(document, ID_PATH));
            surveyBO.setLang(getDCRValue(document, LANG_PATH));
            surveyBO.setTitle(getDCRValue(document, TITLE_PATH));
            surveyBO.setDescription(
                    getDCRValue(document, SURVEY_START_DATE_PATH));
            surveyBO.setEndDate(
                    getDCRValue(document, SURVEY_END_DATE_PATH));
            surveyBO.setPersona(getDCRValue(document, PERSONA_PATH));
            surveyBO.setServiceEntities(getDCRValue(document, SERVICE_ENTITIES));
            surveyBO.setTopics(getDCRValue(document, TOPICS));
            surveyBO.setSubmitType(getDCRValue(document, SUBMIT_TYPE));
            surveyBO.setEstimatedTime(getDCRValue(document, ESTIMATED_TIME));
            surveyBO.setFileName(fileName);
            
            setSurveyMasterId(surveyBO, connection);
            
            logger.debug("SurveyBO : " + surveyBO);

            int result = updateDynamicSurveyMasterData(surveyBO, connection);

            if (result > 0) {
                logger.info("Survey Master Data Updated");
                isSurveyDataUpdated = updateDynamicSurveyQuestionData(surveyBO,
                        document, connection);
            } else {
                if (connection != null) {
                    connection.rollback();
                }
                logger.info("Survey master update failed");
            }

            if (isSurveyDataUpdated) {
                if (connection != null) {
                    connection.commit();
                }
                logger.info("Survey update transaction committed");
            }

        } catch (Exception e) {
            try {
                if (connection != null) {
                    connection.rollback();
                }
                logger.error("Exception in updateDynamicSurveyData : ", e);
            } catch (SQLException ex) {
                logger.error(
                        "Exception in updateDynamicSurveyData rollback catch block : ",
                        ex);
            }
        } finally {
            postgre.releaseConnection(null, prepareStatementSurveyQuestion,
                    null);
            postgre.releaseConnection(null, optionPrepareStatement, null);
            postgre.releaseConnection(connection, prepareStatement, rs);
            logger.info("Released updateDynamicSurveyData connection");
        }
        return isSurveyDataUpdated;
    }
    
    /**
     * Method fetches the survey master id and set it to the survey BO
     *
     * @param surveyBO   Survey business object
     * @param connection Database connection object
     * @throws SQLException
     */
    public void setSurveyMasterId(TSSurveyBO surveyBO,
            Connection connection) throws SQLException {
        PreparedStatement preparedStatement = null;
        ResultSet rs = null;
        int result = 0;
        try {
            logger.info("PollSurveyTask : setSurveyMasterId");
            String dynamicSurveyMasterQuery = "SELECT SURVEY_MASTER_ID FROM DYNAMIC_SURVEY_MASTER "
                    + "WHERE SURVEY_ID = ? AND LANG = ? ";
            logger.debug("setSurveyMasterId dynamicSurveyMasterQuery : "
                    + dynamicSurveyMasterQuery);
            preparedStatement = connection
                    .prepareStatement(dynamicSurveyMasterQuery);
            preparedStatement.setLong(1,
                    Long.parseLong(surveyBO.getSurveyId()));
            preparedStatement.setString(2, surveyBO.getLang());

            rs = preparedStatement.executeQuery();
            while (rs.next()) {
                Long surveyMasterId = rs.getLong(1);
                surveyBO.setSurveyMasterId(surveyMasterId);
            }
            logger.debug("surveyMasterId : " + surveyBO.getSurveyMasterId());
        } catch (NumberFormatException | SQLException e) {
            logger.error("Exception in setSurveyMasterId: ", e);
            throw e;
        } finally {
            postgre.releaseConnection(null, preparedStatement, rs);
            logger.info("Released setSurveyMasterId connection");
        }
    }
    
    /**
     * Method fetches the survey question id and set it to the survey BO
     *
     * @param surveyBO   Survey business object
     * @param connection Database connection object
     * @throws SQLException
     */
    public void setSurveyQuestionId(TSSurveyBO surveyBO,
            Connection connection) throws SQLException {
        PreparedStatement preparedStatement = null;
        ResultSet rs = null;
        int result = 0;
        try {
            logger.info("PollSurveyTask : setSurveyQuestionId");
            String dynamicSurveyMasterQuery = "SELECT SURVEY_QUESTION_ID FROM DYNAMIC_SURVEY_QUESTION "
                    + "WHERE SURVEY_MASTER_ID = ? ";
            logger.debug("setSurveyQuestionId dynamicSurveyMasterQuery : "
                    + dynamicSurveyMasterQuery);
            preparedStatement = connection
                    .prepareStatement(dynamicSurveyMasterQuery);
            preparedStatement.setLong(1, surveyBO.getSurveyMasterId());

            rs = preparedStatement.executeQuery();
            while (rs.next()) {
                Long surveyQuestionId = rs.getLong(1);
                surveyBO.setSurveyQuestionId(surveyQuestionId);
            }
            logger.debug("surveyQuestionId : " + surveyBO.getSurveyQuestionId());
        } catch (NumberFormatException | SQLException e) {
            logger.error("Exception in setSurveyQuestionId: ", e);
            throw e;
        } finally {
            postgre.releaseConnection(null, preparedStatement, rs);
            logger.info("Released setSurveyQuestionId connection");
        }
    }

    /**
     * Method updates the survey master data
     *
     * @param surveyBO   Survey business object
     * @param connection Database connection object
     * @return Returns number of rows affected by query execution
     * @throws SQLException
     */
    public int updateSurveyMasterData(TSSurveyBO surveyBO,
            Connection connection) throws SQLException {
        PreparedStatement preparedStatement = null;
        int result = 0;
        try {
            logger.info("PollSurveyTask : updateSurveyMasterData");
            String surveyMasterQuery = "UPDATE SURVEY_MASTER SET "
                    + "SURVEY_TITLE = ?, SURVEY_DESCRIPTION = ?, "
                    + "END_DATE = ?, PERSONA = ?, SERVICE_ENTITIES = ?, "
                    + "TOPICS = ?, SUBMIT_TYPE = ?, STATUS = ? , FILE_NAME = ?, ESTIMATED_TIME = ? "
                    + "WHERE SURVEY_ID = ? AND LANG = ?";
            logger.debug("updateSurveyMasterData surveyMasterQuery : "
                    + surveyMasterQuery);
            preparedStatement = connection
                    .prepareStatement(surveyMasterQuery);
            preparedStatement.setString(1, surveyBO.getTitle());
            preparedStatement.setString(2, surveyBO.getDescription());
            preparedStatement.setDate(3, getDate(surveyBO.getEndDate()));
            preparedStatement.setString(4, surveyBO.getPersona());
            preparedStatement.setString(5, surveyBO.getServiceEntities());
            preparedStatement.setString(6, surveyBO.getTopics());
            preparedStatement.setString(7, surveyBO.getSubmitType());
            preparedStatement.setString(8, PUBLISHED_STATUS);
            preparedStatement.setString(9, surveyBO.getFileName());
            preparedStatement.setString(10, surveyBO.getEstimatedTime());
            preparedStatement.setLong(11,
                    Long.parseLong(surveyBO.getSurveyId()));
            preparedStatement.setString(12, surveyBO.getLang());

            result = preparedStatement.executeUpdate();
            logger.info("updateSurveyMasterData result : " + result);
        } catch (NumberFormatException | SQLException e) {
            logger.error("Exception in updateSurveyMasterData: ", e);
            throw e;
        } finally {
            postgre.releaseConnection(null, preparedStatement, null);
            logger.info("Released updateSurveyMasterData connection");
        }
        return result;
    }
    
    /**
     * Method updates the survey master data
     *
     * @param surveyBO   Survey business object
     * @param connection Database connection object
     * @return Returns number of rows affected by query execution
     * @throws SQLException
     */
    public int updateDynamicSurveyMasterData(TSSurveyBO surveyBO,
            Connection connection) throws SQLException {
        PreparedStatement preparedStatement = null;
        int result = 0;
        try {
            logger.info("PollSurveyTask : updateDynamicSurveyMasterData");
            String surveyMasterQuery = "UPDATE DYNAMIC_SURVEY_MASTER SET "
                    + "SURVEY_TITLE = ?, SURVEY_DESCRIPTION = ?, "
                    + "END_DATE = ?, PERSONA = ?, SERVICE_ENTITIES = ?, TOPICS = ?, SUBMIT_TYPE = ?, ESTIMATED_TIME = ?, STATUS = ?, FILE_NAME = ?  "
                    + "WHERE SURVEY_MASTER_ID = ?";
            logger.debug("updateSurveyMasterData surveyMasterQuery : "
                    + surveyMasterQuery);
            preparedStatement = connection
                    .prepareStatement(surveyMasterQuery);
            preparedStatement.setString(1, surveyBO.getTitle());
            preparedStatement.setString(2, surveyBO.getDescription());
            preparedStatement.setDate(3, getDate(surveyBO.getEndDate()));
            preparedStatement.setString(4, surveyBO.getPersona());
            preparedStatement.setString(5, surveyBO.getServiceEntities());
            preparedStatement.setString(6, surveyBO.getTopics());
            preparedStatement.setString(7, surveyBO.getSubmitType());
            preparedStatement.setString(8, surveyBO.getEstimatedTime());
            preparedStatement.setString(9, PUBLISHED_STATUS);
            preparedStatement.setString(10, surveyBO.getFileName());
            preparedStatement.setLong(11, surveyBO.getSurveyMasterId());

            result = preparedStatement.executeUpdate();
            logger.info("updateDynamicSurveyMasterData result : " + result);
        } catch (NumberFormatException | SQLException e) {
            logger.error("Exception in updateDynamicSurveyMasterData: ", e);
            throw e;
        } finally {
            postgre.releaseConnection(null, preparedStatement, null);
            logger.info("Released updateDynamicSurveyMasterData connection");
        }
        return result;
    }

    /**
     * Method updates the survey question data
     *
     * @param surveyBO   Survey business object
     * @param document   Document object of the survey DCR
     * @param connection Database connection object
     * @return Returns true for successful update else false for failure.
     * @throws SQLException
     */
    public boolean updateSurveyQuestionData(TSSurveyBO surveyBO,
            Document document, Connection connection) throws SQLException {
        PreparedStatement preparedStatement = null;
        boolean result = false;
        try {
            logger.info("PollSurveyTask : updateSurveyQuestionData");
            String surveyQuestionQuery = "UPDATE SURVEY_QUESTION SET "
                    + "QUESTION = ? WHERE SURVEY_ID = ? AND LANG = ? "
                    + "AND QUESTION_NO = ?";
            preparedStatement = connection
                    .prepareStatement(surveyQuestionQuery);
            logger.debug("surveyQuestionQuery : " + surveyQuestionQuery);

            List<Node> nodes = document.selectNodes(FIELD_PATH);
            int questionNo = 1;
            for (Node node : nodes) {
                logger.debug("updateSurveyQuestionData questionNo : "
                        + questionNo);

                String questionType = node
                        .selectSingleNode(".//field-type").getText();
                logger.debug("updateSurveyQuestionData questionType : "
                        + questionType);

                if (questionType != null && !"".equals(questionType) && !"button".equalsIgnoreCase(questionType)) {

                    String question = node.selectSingleNode(".//question")
                            .getText();
                    logger.debug("question : " + question);
                    preparedStatement.setString(1, question);
                    preparedStatement.setLong(2,
                            Long.parseLong(surveyBO.getSurveyId()));
                    preparedStatement.setString(3, surveyBO.getLang());
                    preparedStatement.setInt(4, questionNo);
                    int questionResult = preparedStatement.executeUpdate();

                    if (questionResult > 0) {
                        result = true;
                        logger.info("Survey Question Inserted");
                        if(optionsList.contains(questionType)) {
                            surveyBO.setQuestionNo(questionNo);
                            boolean isOptionsUpdated = updateSurveyOptionData(
                                    surveyBO, node, connection);
    
                            if (isOptionsUpdated) {
                                result = true;
                            } else {
                                result = false;
                                connection.rollback();
                                logger.info(
                                        "updateSurveyQuestionData Option batch update failed");
                                break;
                            }
                            questionNo++;
                        }
                    } else {
                        result = false;
                        connection.rollback();
                        logger.info(
                                "updateSurveyQuestionData Question update failed");
                        break;
                    }
                }
            }
        } catch (NumberFormatException | SQLException e) {
            logger.error("Exception in updateSurveyQuestionData: ", e);
            throw e;
        } finally {
            postgre.releaseConnection(null, preparedStatement, null);
            logger.info("Released updateSurveyQuestionData connection");
        }
        return result;
    }
    
    /**
     * Method updates the dynamic survey question data
     *
     * @param surveyBO   Survey business object
     * @param document   Document object of the survey DCR
     * @param connection Database connection object
     * @return Returns true for successful update else false for failure.
     * @throws SQLException
     */
    public boolean updateDynamicSurveyQuestionData(TSSurveyBO surveyBO,
            Document document, Connection connection) throws SQLException {
        PreparedStatement preparedStatement = null;
        boolean result = false;
        try {
            logger.info("PollSurveyTask : updateDynamicSurveyQuestionData");
            String surveyQuestionQuery = "UPDATE DYNAMIC_SURVEY_QUESTION SET "
                    + "QUESTION = ? WHERE SURVEY_QUESTION_ID = ? AND SURVEY_MASTER_ID = ? ";
            preparedStatement = connection
                    .prepareStatement(surveyQuestionQuery);
            logger.debug("surveyQuestionQuery : " + surveyQuestionQuery);
            
            setSurveyQuestionId(surveyBO, connection);

            List<Node> nodes = document.selectNodes(OPTION_FIELD_PATH);
            for (Node node : nodes) {

                String questionType = node
                        .selectSingleNode(".//field-type").getText();
                logger.debug("updateDynamicSurveyQuestionData questionType : "
                        + questionType);

                if (questionType != null && !"".equals(questionType) && !"button".equalsIgnoreCase(questionType)) {

                    String question = node.selectSingleNode(".//question")
                            .getText();
                    logger.debug("question : " + question);
                    preparedStatement.setString(1, question);
                    preparedStatement.setLong(2, surveyBO.getSurveyQuestionId());
                    preparedStatement.setLong(3, surveyBO.getSurveyMasterId());
                    int questionResult = preparedStatement.executeUpdate();

                    if (questionResult > 0) {
                        result = true;
                        logger.info("Survey Question Inserted");
                        if(optionsList.contains(questionType)) {
                            boolean isOptionsUpdated = updateDynamicSurveyOptionData(
                                    surveyBO, node, connection);
    
                            if (isOptionsUpdated) {
                                result = true;
                            } else {
                                result = false;
                                connection.rollback();
                                logger.info(
                                        "updateDynamicSurveyQuestionData Option batch update failed");
                                break;
                            }
                        }
                    } else {
                        result = false;
                        connection.rollback();
                        logger.info(
                                "updateDynamicSurveyQuestionData Question update failed");
                        break;
                    }
                }
            }
        } catch (NumberFormatException | SQLException e) {
            logger.error("Exception in updateDynamicSurveyQuestionData: ", e);
            throw e;
        } finally {
            postgre.releaseConnection(null, preparedStatement, null);
            logger.info("Released updateDynamicSurveyQuestionData connection");
        }
        return result;
    }

    /**
     * Method updates the survey option data
     *
     * @param surveyBO   Survey business object
     * @param node       Node object of the survey option node
     * @param connection Database connection object
     * @return Returns true for successful update else false for failure.
     * @throws SQLException
     */
    public boolean updateSurveyOptionData(TSSurveyBO surveyBO, Node node,
            Connection connection) throws SQLException {
        PreparedStatement preparedStatement = null;
        boolean result = false;
        try {
            logger.info("PollSurveyTask : updateSurveyOptionData");
            String surveyOptionQuery = "UPDATE "
                    + "SURVEY_OPTION SET OPTION_LABEL = ?,  IS_USER_INPUT = ?,"
                    //+ "OPTION_VALUE = ? WHERE SURVEY_ID = ? "
                    + "WHERE SURVEY_ID = ? "
                    + "AND LANG = ? AND QUESTION_NO = ? "
                    + "AND OPTION_NO = ?";
            preparedStatement = connection
                    .prepareStatement(surveyOptionQuery);
            logger.debug("updateSurveyOptionData surveyOptionQuery : "
                    + surveyOptionQuery);
            List<Node> optionNodes = node.selectNodes(".//option");
            int optionNo = 1;
            for (Node optnode : optionNodes) {
                logger.debug(
                        "updateSurveyOptionData optionNo : " + optionNo);
                String optionLabel = optnode.selectSingleNode(OPTION_LABEL)
                        .getText();
                String isOpenResponse = optnode.selectSingleNode(IS_OPEN_RESPONSE)
                        .getText();
                String optionValue = optnode.selectSingleNode(OPTION_VALUE)
                        .getText();
                logger.debug(optionLabel + " : " + optionValue);
                preparedStatement.setString(1, optionLabel);
                preparedStatement.setString(2, isOpenResponse);
                /*preparedStatement.setString(3, optionValue);
                preparedStatement.setLong(4,
                        Long.parseLong(surveyBO.getSurveyId()));
                preparedStatement.setString(5, surveyBO.getLang());
                preparedStatement.setInt(6, surveyBO.getQuestionNo());
                preparedStatement.setInt(7, optionNo);*/
                preparedStatement.setLong(3,
                        Long.parseLong(surveyBO.getSurveyId()));
                preparedStatement.setString(4, surveyBO.getLang());
                preparedStatement.setInt(5, surveyBO.getQuestionNo());
                preparedStatement.setInt(6, optionNo);
                preparedStatement.addBatch();
                optionNo++;
            }
            int[] optionBatch = preparedStatement.executeBatch();
            logger.info("updateSurveyOptionData optionBatch length : "
                    + optionBatch.length);

            if (optionBatch.length == optionNo - 1) {
                logger.info("Survey Option Inserted");
                result = true;
            }

        } catch (NumberFormatException | SQLException e) {
            logger.error("Exception in updateSurveyOptionData: ", e);
            throw e;
        } finally {
            postgre.releaseConnection(null, preparedStatement, null);
            logger.info("Released updateSurveyOptionData connection");
        }
        return result;
    }
    
    /**
     * Method updates the dynamic survey option data
     *
     * @param surveyBO   Survey business object
     * @param node       Node object of the survey option node
     * @param connection Database connection object
     * @return Returns true for successful update else false for failure.
     * @throws SQLException
     */
    public boolean updateDynamicSurveyOptionData(TSSurveyBO surveyBO, Node node,
            Connection connection) throws SQLException {
        PreparedStatement preparedStatement = null;
        boolean result = false;
        try {
            logger.info("PollSurveyTask : updateDynamicSurveyOptionData");
            String surveyOptionQuery = "UPDATE "
                    + "DYNAMIC_SURVEY_OPTION SET OPTION_LABEL = ?,  IS_USER_INPUT = ? "
                    + "WHERE SURVEY_QUESTION_ID = ? ";
            preparedStatement = connection
                    .prepareStatement(surveyOptionQuery);
            logger.debug("updateDynamicSurveyOptionData surveyOptionQuery : "
                    + surveyOptionQuery);
            List<Node> optionNodes = node.selectNodes(".//option");
            int optionNo = 1;
            for (Node optnode : optionNodes) {
                logger.debug(
                        "updateDynamicSurveyOptionData optionNo : " + optionNo);
                String optionLabel = optnode.selectSingleNode(OPTION_LABEL)
                        .getText();
                String isOpenResponse = optnode.selectSingleNode(IS_OPEN_RESPONSE)
                        .getText();
                logger.debug("optionLabel : "+optionLabel);
                preparedStatement.setString(1, optionLabel);
                preparedStatement.setString(2, isOpenResponse);
                preparedStatement.setLong(3, surveyBO.getSurveyQuestionId());
                preparedStatement.addBatch();
                optionNo++;
            }
            int[] optionBatch = preparedStatement.executeBatch();
            logger.info("updateDynamicSurveyOptionData optionBatch length : "
                    + optionBatch.length);

            if (optionBatch.length == optionNo - 1) {
                logger.info("Survey Option Inserted");
                result = true;
            }

        } catch (NumberFormatException | SQLException e) {
            logger.error("Exception in updateDynamicSurveyOptionData: ", e);
            throw e;
        } finally {
            postgre.releaseConnection(null, preparedStatement, null);
            logger.info("Released updateDynamicSurveyOptionData connection");
        }
        return result;
    }

    /**
     * Method to get the DCR value for the input node name
     *
     * @param document Document object of the DCR
     * @param nodeName Name of the value node
     * @return
     */
    public String getDCRValue(Document document, String nodeName) {
        logger.info("PollSurveyTask : getDCRValue");
        String dcrValue = document.selectSingleNode(nodeName).getText();
        logger.debug(nodeName + " : " + dcrValue);
        return dcrValue;
    }

    /**
     * Method to get only the date object without time for the input date time
     * string.
     *
     * @param inputDate Input date string.
     * @return Returns Date object created from the input date string.
     */
    public Date getDate(String inputDate) {
        logger.info("PollSurveyTask : getDate");
        String[] dateArr = inputDate.split(" ");
        Date outDate = java.sql.Date.valueOf(dateArr[0]);
        logger.debug(inputDate + " >>> " + outDate);
        return outDate;
    }

    /**
     * Method to get the next sequence value from the database from the input
     * sequence name
     *
     * @param sequenceName Name of the database sequence to get the next value
     * @param connection   Database connection object
     * @return Returns next sequence value
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
            logger.error("Exception in  getNextSequenceValue", e);
        } finally {
            postgre.releaseConnection(connection, queryStmt, rs);
        }
        return seqValue;
    }

}