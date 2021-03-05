package com.hukoomi.task;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
import com.interwoven.cssdk.filesys.CSSimpleFile;
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
     * Postgre class instance variable
     */
    PostgreTSConnection postgre = null;

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

        for (CSAreaRelativePath taskFile : taskFileList) {
            try {
                CSSimpleFile taskSimpleFile = (CSSimpleFile) task.getArea()
                        .getFile(taskFile);
                logger.debug("File Name : " + taskSimpleFile.getName());

                String dcrType = taskSimpleFile
                        .getExtendedAttribute(META_DATA_NAME_DCR_TYPE)
                        .getValue();

                if (dcrType != null) {
                    if ("Content/Polls".equalsIgnoreCase(dcrType)) {
                        statusMap = (HashMap<String, String>) processPollDCR(
                                taskSimpleFile);
                    } else if ("Content/Survey"
                            .equalsIgnoreCase(dcrType)) {
                        statusMap = (HashMap<String, String>) processSurveyDCR(
                                taskSimpleFile);
                    } else {
                        logger.debug(
                                "Master data insert skipped - Not Polls or Survey DCR");
                    }
                }

            } catch (Exception e) {
                logger.error("Exception in execute: ", e);
            }
        }

        logger.debug("transition : " + statusMap.get(TRANSITION));
        logger.debug("transitionComment : "
                + statusMap.get(TRANSITION_COMMENT));
        task.chooseTransition(statusMap.get(TRANSITION),
                statusMap.get(TRANSITION_COMMENT));
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
        boolean isDBOperationSuccess = false;
        HashMap<String, String> statusMap = new HashMap<>();
        try {
            Document document = getTaskDocument(taskSimpleFile);
            if (isPollMasterDataAvailable(document)) {
                isDBOperationSuccess = updatePollData(document);
                logger.debug(
                        "isPollDataUpdated : " + isDBOperationSuccess);
                if (isDBOperationSuccess) {
                    statusMap.put(TRANSITION, SUCCESS_TRANSITION);
                    statusMap.put(TRANSITION_COMMENT, POLL_UPDATE_SUCCESS);
                } else {
                    statusMap.put(TRANSITION, FAILURE_TRANSITION);
                    statusMap.put(TRANSITION_COMMENT, POLL_UPDATE_FAILURE);
                }
            } else {
                isDBOperationSuccess = insertPollData(document);
                logger.debug(
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
            logger.error("Exception in poll master: ", e);
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
        boolean isDBOperationSuccess = false;
        HashMap<String, String> statusMap = new HashMap<>();
        try {
            Document document = getTaskDocument(taskSimpleFile);
            if (isSurveyMasterDataAvailable(document)) {
                isDBOperationSuccess = updateSurveyData(document);
                logger.debug(
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
                isDBOperationSuccess = insertSurveyData(document);
                logger.debug(
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
            logger.error("Exception in survey master: ", e);
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
        logger.debug("PollSurveyTask : getTaskDocument");
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
            logger.info("pollMasterQuery in isPollMasterDataAvailable: "
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
    public boolean insertPollData(Document document) {
        logger.debug("PollSurveyTask : insertPollData");
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
                    + "LANG, QUESTION, START_DATE, END_DATE, PERSONA, SERVICE_ENTITIES, TOPICS) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            logger.info("insertPollMasterData pollMasterQuery : "
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
            logger.info("insertPollOptionsData pollOptionQuery : "
                    + pollOptionQuery);

            List<Node> nodes = document.selectNodes(OPTION_PATH);
            long optionId = 1l;
            for (Node node : nodes) {
                logger.info(
                        "insertPollOptionsData optionId : " + optionId);
                String label = node.selectSingleNode(OPTION_LABEL)
                        .getText();
                String value = node.selectSingleNode(OPTION_VALUE)
                        .getText();
                logger.info("insertPollOptionsData label : " + label);
                logger.info("insertPollOptionsData value : " + value);
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
    public boolean updatePollData(Document document) {
        logger.debug("PollSurveyTask : updatePollData");
        Connection connection = null;
        boolean isPollDataInserted = false;
        try {

            connection = postgre.getConnection();
            logger.info("updatePollData Connection : " + connection);

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
                    + "END_DATE = ?, PERSONA = ?, SERVICE_ENTITIES = ?, TOPICS = ? "
                    + "WHERE POLL_ID = ? AND LANG = ?";
            logger.info("updatePollMasterData pollMasterQuery : "
                    + pollMasterQuery);
            connection.setAutoCommit(false);
            preparedStatement = connection
                    .prepareStatement(pollMasterQuery);
            preparedStatement.setString(1, pollsBO.getQuestion());
            preparedStatement.setDate(2, getDate(pollsBO.getEndDate()));
            preparedStatement.setString(3, pollsBO.getPersona());
            preparedStatement.setString(4, pollsBO.getServiceEntities());
            preparedStatement.setString(5, pollsBO.getTopics());            
            preparedStatement.setLong(6,
                    Long.parseLong(pollsBO.getPollId()));
            preparedStatement.setString(7, pollsBO.getLang());

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
            logger.info("updatePollMasterData pollOptionQuery : "
                    + pollOptionQuery);

            List<Node> nodes = document.selectNodes(OPTION_PATH);
            long optionId = 1l;
            for (Node node : nodes) {
                logger.info("updatePollMasterData optionId : " + optionId);
                String label = node.selectSingleNode(OPTION_LABEL)
                        .getText();
                logger.info("updatePollMasterData label : " + label);
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
        logger.debug("PollSurveyTask : isSurveyMasterDataAvailable");
        PreparedStatement prepareStatement = null;
        Connection connection = null;
        ResultSet rs = null;
        boolean isSurveyDataAvailable = false;
        try {

            connection = postgre.getConnection();

            Long surveyId = Long.parseLong(getDCRValue(document, ID_PATH));
            String lang = getDCRValue(document, LANG_PATH);

            String surveyMasterQuery = "SELECT COUNT(*) FROM SURVEY_MASTER WHERE SURVEY_ID = ? AND LANG = ?";
            logger.info("surveyMasterQuery isSurveyMasterDataAvailable : "
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
     * Method creates the business object, calls insert master, question and option
     * data
     * 
     * @param document Document object of the survey DCR
     * @return Returns status of the insert survey data as boolean
     */
    public boolean insertSurveyData(Document document) {
        logger.debug("PollSurveyTask : insertSurveyData");
        Connection connection = null;
        boolean isSurveyDataInserted = false;
        try {

            connection = postgre.getConnection();

            TSSurveyBO surveyBO = new TSSurveyBO();
            surveyBO.setSurveyId(getDCRValue(document, ID_PATH));
            surveyBO.setLang(getDCRValue(document, LANG_PATH));
            surveyBO.setTitle(getDCRValue(document, TITLE_PATH));
            surveyBO.setDescription(
                    getDCRValue(document, SURVEY_START_DATE_PATH));
            surveyBO.setStartDate(
                    getDCRValue(document, SURVEY_START_DATE_PATH));
            surveyBO.setEndDate(
                    getDCRValue(document, SURVEY_END_DATE_PATH));
            surveyBO.setPersona(getDCRValue(document, PERSONA_PATH));
            surveyBO.setServiceEntities(getDCRValue(document, SERVICE_ENTITIES));
            surveyBO.setTopics(getDCRValue(document, TOPICS));
            surveyBO.setSubmitType(getDCRValue(document, SUBMIT_TYPE));
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
                    + "START_DATE, END_DATE, PERSONA, SERVICE_ENTITIES, TOPICS, SUBMIT_TYPE) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            logger.info("insertSurveyMasterData surveyMasterQuery : "
                    + surveyMasterQuery);
            connection.setAutoCommit(false);
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
            logger.info("insertSurveyQuestionData surveyQuestionQuery : "
                    + surveyQuestionQuery);

            List<Node> nodes = document.selectNodes(FIELD_PATH);
            int questionNo = 1;
            for (Node node : nodes) {
                logger.info("insertSurveyQuestionData questionNo : "
                        + questionNo);

                String questionType = node
                        .selectSingleNode(".//field-type").getText();
                logger.info("insertSurveyQuestionData questionType : "
                        + questionType);

                if (!"button".equalsIgnoreCase(questionType)) {

                    Long questionId = getNextSequenceValue(
                            "survey_question_question_id_seq",
                            postgre.getConnection());
                    logger.info("insertSurveyQuestionData questionId : "
                            + questionId);

                    String question = node.selectSingleNode(".//question")
                            .getText();
                    logger.info("question : " + question);
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
                    + "OPTION_NO, OPTION_LABEL, OPTION_VALUE) " + "VALUES "
                    + "(nextval('survey_option_option_id_seq')"
                    + ", ?, ?, ?, ?, ?, ?, ?)";
            preparedStatement = connection
                    .prepareStatement(surveyOptionQuery);
            logger.info("surveyOptionQuery : " + surveyOptionQuery);
            List<Node> optionNodes = node.selectNodes(".//option");
            int optionNo = 1;
            for (Node optnode : optionNodes) {
                logger.info("optionNo : " + optionNo);
                String optionLabel = optnode.selectSingleNode(OPTION_LABEL)
                        .getText();
                String optionValue = optnode.selectSingleNode(OPTION_VALUE)
                        .getText();
//                String isOpenResponse = optnode.selectSingleNode(IS_OPEN_RESPONSE)
//                        .getText();
                logger.info(optionLabel + " : " + optionValue);
                preparedStatement.setLong(1,
                        Long.parseLong(surveyBO.getSurveyId()));
                preparedStatement.setString(2, surveyBO.getLang());
                preparedStatement.setLong(3, surveyBO.getQuestionId());
                preparedStatement.setInt(4, surveyBO.getQuestionNo());
                preparedStatement.setInt(5, optionNo);
                preparedStatement.setString(6, optionLabel);
//                preparedStatement.setString(7, isOpenResponse);
                preparedStatement.setString(7, optionValue);
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
     * Method creates the business object, calls update master and option data
     * 
     * @param document Document object of the survey DCR
     * @return Returns status of the update survey data as boolean
     */
    public boolean updateSurveyData(Document document) {
        logger.debug("PollSurveyTask : updateSurveyData");
        PreparedStatement prepareStatement = null;
        PreparedStatement prepareStatementSurveyQuestion = null;
        PreparedStatement optionPrepareStatement = null;
        Connection connection = null;
        ResultSet rs = null;
        boolean isSurveyDataUpdated = false;
        try {

            connection = postgre.getConnection();

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
                    + "END_DATE = ?, PERSONA = ?, SERVICE_ENTITIES = ?, TOPICS = ?, SUBMIT_TYPE = ?  "
                    + "WHERE SURVEY_ID = ? AND LANG = ?";
            logger.info("updateSurveyMasterData surveyMasterQuery : "
                    + surveyMasterQuery);
            connection.setAutoCommit(false);
            preparedStatement = connection
                    .prepareStatement(surveyMasterQuery);
            preparedStatement.setString(1, surveyBO.getTitle());
            preparedStatement.setString(2, surveyBO.getDescription());
            preparedStatement.setDate(3, getDate(surveyBO.getEndDate()));
            preparedStatement.setString(4, surveyBO.getPersona());
            preparedStatement.setString(5, surveyBO.getServiceEntities());
            preparedStatement.setString(6, surveyBO.getTopics());
            preparedStatement.setString(7, surveyBO.getSubmitType());
            preparedStatement.setLong(8,
                    Long.parseLong(surveyBO.getSurveyId()));
            preparedStatement.setString(9, surveyBO.getLang());

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
            logger.info("surveyQuestionQuery : " + surveyQuestionQuery);

            List<Node> nodes = document.selectNodes(FIELD_PATH);
            int questionNo = 1;
            for (Node node : nodes) {
                logger.info("updateSurveyQuestionData questionNo : "
                        + questionNo);

                String questionType = node
                        .selectSingleNode(".//field-type").getText();
                logger.info("updateSurveyQuestionData questionType : "
                        + questionType);

                if (!"button".equalsIgnoreCase(questionType)) {

                    String question = node.selectSingleNode(".//question")
                            .getText();
                    logger.info("question : " + question);
                    preparedStatement.setString(1, question);
                    preparedStatement.setLong(2,
                            Long.parseLong(surveyBO.getSurveyId()));
                    preparedStatement.setString(3, surveyBO.getLang());
                    preparedStatement.setInt(4, questionNo);
                    int questionResult = preparedStatement.executeUpdate();

                    if (questionResult > 0) {
                        result = true;
                        logger.info("Survey Question Inserted");
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
                    + "SURVEY_OPTION SET OPTION_LABEL = ?, "
                    + "OPTION_VALUE = ? WHERE SURVEY_ID = ? "
                    + "AND LANG = ? AND QUESTION_NO = ? "
                    + "AND OPTION_NO = ?";
            preparedStatement = connection
                    .prepareStatement(surveyOptionQuery);
            logger.info("updateSurveyOptionData surveyOptionQuery : "
                    + surveyOptionQuery);
            List<Node> optionNodes = node.selectNodes(".//option");
            int optionNo = 1;
            for (Node optnode : optionNodes) {
                logger.info(
                        "updateSurveyOptionData optionNo : " + optionNo);
                String optionLabel = optnode.selectSingleNode(OPTION_LABEL)
                        .getText();
//                String isOpenResponse = optnode.selectSingleNode(IS_OPEN_RESPONSE)
//                        .getText();
                String optionValue = optnode.selectSingleNode(OPTION_VALUE)
                        .getText();
                logger.info(optionLabel + " : " + optionValue);
                preparedStatement.setString(1, optionLabel);
//                preparedStatement.setString(2, isOpenResponse);
                preparedStatement.setString(2, optionValue);
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
     * Method to get the DCR value for the input node name
     * 
     * @param document Document object of the DCR
     * @param nodeName Name of the value node
     * @return
     */
    public String getDCRValue(Document document, String nodeName) {
        logger.debug("PollSurveyTask : getDCRValue");
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
        logger.debug("PollSurveyTask : getDate");
        String[] dateArr = inputDate.split(" ");
        Date outDate = java.sql.Date.valueOf(dateArr[0]);
        logger.info(inputDate + " >>> " + outDate);
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
