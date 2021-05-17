package com.hukoomi.task;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;

import com.hukoomi.bo.ErrorBO;
import com.hukoomi.utils.PostgreTSConnection;
import com.interwoven.cssdk.filesys.CSSimpleFile;

public class ErrorTask {

	 /**
     * Logger object to check the flow of the code.
     */
    private final Logger logger = Logger.getLogger(ErrorTask.class);
    /**
     * DCR Type Meta data name
     */
    public static final String META_DATA_NAME_DCR_TYPE = "TeamSite/Templating/DCR/Type";
    /**
     * XPath to the error id
     */
    public static final String ID_PATH = "/root/information/id";
    /**
     * XPath to the language selection
     */
    public static final String LANG_PATH = "/root/information/language/value";
    /**
     * XPath to the Title path
     */
    public static final String STATUS_PATH = "/root/detail/status-code";
    /**
     * XPath to the error updated date
     */
    public static final String ERROR_NAME_PATH = "/root/detail/err-name-tech";
    /**
     * XPath to the error title
     */
	private static final String ERROR_TITLE_PATH = "/root/information/title";
	 /**
     * XPath to the error message
     */
	private static final String ERROR_MESSAGE_PATH = "/root/detail/err-message";

    
    
    
    /**
     * Success transition message
     */
    public static final String SUCCESS_TRANSITION = "Insert Master Data Success";
    /**
     * Failure transition message
     */
    public static final String FAILURE_TRANSITION = "Insert Master Data Failure";
    /**
     * Transition hashmap key
     */
    private static final String TRANSITION = "TRANSITION";
    /**
     * Transition comment hashmap key
     */
    private static final String TRANSITION_COMMENT = "TRANSITION_COMMENT";
    /**
     * Error update transition success message
     */
    private static final String ERROR_UPDATE_SUCCESS = "Error master data updated successfully";
    /**
     * Error update transition failure message
     */
    private static final String ERROR_UPDATE_FAILURE = "Failed to updated Error master data";
    /**
     * Error insert transition success message
     */
    private static final String ERROR_INSERT_SUCCESS = "Error master data inserted successfully";
    /**
     * Error insert transition failure message
     */
    private static final String ERROR_INSERT_FAILURE = "Failed to insert Error master data";
    /**
     * Error transition technical error message
     */
    private static final String ERROR_TECHNICAL_ERROR = "Technical Error in Error master data insert";
    
    




    /**
     * Method process the poll dcr from the workflow task and insert poll master
     * data
     *
     * @param taskSimpleFile Task file of CSSimpleFile object
     * @return Returns map contains the transition status and transition comment.
     */
    public Map<String, String> processErrorDCR(
            CSSimpleFile taskSimpleFile,PostgreTSConnection postgre) {
        boolean isDBOperationSuccess = false;
        HashMap<String, String> statusMap = new HashMap<>();
        try {
            Document document = getTaskDocument(taskSimpleFile);
            if (isErrorMasterDataAvailable(document,postgre)) {
               int result = updateErrorData(document,postgre);
                logger.info(
                        "isErrorDataUpdated : " + isDBOperationSuccess);
                if (result >0) {
                    statusMap.put(TRANSITION, SUCCESS_TRANSITION);
                    statusMap.put(TRANSITION_COMMENT, ERROR_UPDATE_SUCCESS);
                } else {
                    statusMap.put(TRANSITION, FAILURE_TRANSITION);
                    statusMap.put(TRANSITION_COMMENT, ERROR_UPDATE_FAILURE);
                }
            } else {
                isDBOperationSuccess = insertErrorData(document,postgre);
                logger.info(
                        "isErrorDataInserted : " + isDBOperationSuccess);
                if (isDBOperationSuccess) {
                    statusMap.put(TRANSITION, SUCCESS_TRANSITION);
                    statusMap.put(TRANSITION_COMMENT, ERROR_INSERT_SUCCESS);
                } else {
                    statusMap.put(TRANSITION, FAILURE_TRANSITION);
                    statusMap.put(TRANSITION_COMMENT, ERROR_INSERT_FAILURE);
                }
            }
        } catch (Exception e) {
            statusMap.put(TRANSITION, FAILURE_TRANSITION);
            statusMap.put(TRANSITION_COMMENT, ERROR_TECHNICAL_ERROR);
            logger.info("Exception in poll master: ", e);
        }
        return statusMap;
    }

    private boolean insertErrorData(Document document,PostgreTSConnection postgre) {
        logger.info("ErrorTask : insertErrorData");
        Connection connection = null;
        boolean isErrorDataInserted = false;
        try {
            connection = postgre.getConnection();
            logger.info("ErrorTask : after getConnection");
            ErrorBO errorBO = new ErrorBO();
            errorBO.setErrorId(getDCRValue(document, ID_PATH));
            errorBO.setLang(getDCRValue(document, LANG_PATH));
            errorBO.setStatusCode(getDCRValue(document, STATUS_PATH));            
            errorBO.setErrorNameTechnical(getDCRValue(document, ERROR_NAME_PATH));
            errorBO.setTitle(getDCRValue(document, ERROR_TITLE_PATH));
            errorBO.setMessage(getDCRValue(document, ERROR_MESSAGE_PATH));
            logger.info("getErrorId : "+ errorBO.getErrorId());
            logger.info("getLang : "+ errorBO.getLang());
            logger.info("getTitle : "+ errorBO.getStatusCode());
            logger.info("getUpdatedDate : "+ errorBO.getErrorNameTechnical());
            int result = insertBolgMasterData(errorBO, connection,postgre);
            logger.info("insertErrorData result : " + result);
            if (result > 0) {
                logger.info("Error Master Data Inserted");
                isErrorDataInserted = true;
            } else {
                logger.info("Error master insert failed");
            }

        } catch (Exception e) {
                logger.error(
                        "Exception in insertErrorData rollback catch block : ",
                        e);
        } finally {
            postgre.releaseConnection(connection, null, null);
            logger.info("Released insertErrorData connection");
        }
        return isErrorDataInserted;
    }

    private int insertBolgMasterData(ErrorBO errorBO,
            Connection connection,PostgreTSConnection postgre) {
        PreparedStatement preparedStatement = null;
        int result = 0;
        try {
            logger.info("ErrorTask : insertBolgMasterData");
           
            String errorMasterQuery = "INSERT INTO ERROR_MASTER ("
                    + " STATUS_CODE, LANGUAGE, ERROR_NAME_TECHNICAL,"
                    + "ERROR_TITLE, ERROR_MESSAGE ) "
                    + "VALUES ( ?, ?, ?, ?, ?)";
            logger.info("insertErrorMasterData errorMasterQuery : "
                    + errorMasterQuery);
            preparedStatement = connection
                    .prepareStatement(errorMasterQuery);
            
            preparedStatement.setString(1, errorBO.getStatusCode());
            preparedStatement.setString(2, errorBO.getLang());
            preparedStatement.setString(3, errorBO.getErrorNameTechnical());
            preparedStatement.setString(4, errorBO.getTitle());
            preparedStatement.setString(5, errorBO.getMessage());
            result = preparedStatement.executeUpdate();
            logger.info("insertErrorMasterData result : " + result);
        } catch (Exception e) {
            logger.info("Exception in insertErrorMasterData: ", e);
        }
        finally {

                postgre.releaseConnection(connection, preparedStatement, null);
                logger.info("Released insertErrorData connection");

        }
        return result;
    }

    private int updateErrorData(Document document,PostgreTSConnection postgre) throws Exception {
        PreparedStatement preparedStatement = null;
        int result = 0;
        Connection connection = null;
        try {
            connection = postgre.getConnection();
            ErrorBO errorBO = new ErrorBO();
            errorBO.setErrorId(getDCRValue(document, ID_PATH));
            logger.info("ID_PATH : " + errorBO.getErrorId());
            errorBO.setStatusCode(getDCRValue(document, STATUS_PATH));
            logger.info("TITLE_PATH : " + errorBO.getStatusCode());
            errorBO.setErrorNameTechnical(getDCRValue(document, ERROR_NAME_PATH));
            logger.info("TITLE_PATH : " + errorBO.getErrorNameTechnical());
            errorBO.setLang(getDCRValue(document, LANG_PATH));
            logger.info("LANG_PATH : " + errorBO.getLang());
            logger.info("ErrorTask : updateErrorMasterData");
            String errorMasterQuery = "UPDATE ERROR_MASTER SET  ERROR_MESSAGE = ?, ERROR_TITLE = ?,ERROR_NAME_TECHNICAL = ?, STATUS_CODE = ?,  LANGUAGE = ? "
                    + "WHERE STATUS_CODE = ? AND LANGUAGE = ?";
            logger.info("updateErrorMasterData pollMasterQuery : "
                    + errorMasterQuery);
            preparedStatement = connection
                    .prepareStatement(errorMasterQuery);
            preparedStatement.setString(1, errorBO.getMessage());
            preparedStatement.setString(2, errorBO.getTitle());
            preparedStatement.setString(3, errorBO.getErrorNameTechnical());
            preparedStatement.setString(4, errorBO.getStatusCode());
            preparedStatement.setString(5, errorBO.getLang());
            preparedStatement.setString(6, errorBO.getErrorId());
            preparedStatement.setString(7, errorBO.getLang());
            result = preparedStatement.executeUpdate();
            logger.info("updateErrorMasterData result : " + result);

        } catch (NumberFormatException | SQLException e) {
            logger.info("Exception in updateErrorMasterData: ", e);
            throw e;
        } finally {
            postgre.releaseConnection(null, preparedStatement, null);
            logger.info("Released updateErrorMasterData connection");
        }
        return result;
    }

    private boolean isErrorMasterDataAvailable(Document document,PostgreTSConnection postgre) {
        logger.info("ErrorTask : isErrorMasterDataAvailable");
        PreparedStatement prepareStatement = null;
        Connection connection = null;
        ResultSet rs = null;
        boolean isErrorDataAvailable = false;
        try {

            connection = postgre.getConnection();

            String status_code = getDCRValue(document, STATUS_PATH);
            String lang = getDCRValue(document, LANG_PATH);

            String errorMasterQuery = "SELECT COUNT(*) FROM ERROR_MASTER WHERE STATUS_CODE = ? AND LANGUAGE = ?";
            logger.info("pollMasterQuery in isErrorMasterDataAvailable: "
                    + errorMasterQuery);
            prepareStatement = connection
                    .prepareStatement(errorMasterQuery);
            prepareStatement.setString(1, status_code);
            prepareStatement.setString(2, lang);
            rs = prepareStatement.executeQuery();
            while (rs.next()) {
                int count = rs.getInt(1);
                if (count > 0) {
                    isErrorDataAvailable = true;
                }
            }
            logger.info("isErrorDataAvailable : " + isErrorDataAvailable);

        } catch (Exception e) {
            logger.info("Exception in isErrorDataAvailable : ", e);
        } finally {
            postgre.releaseConnection(connection, prepareStatement, rs);
            logger.info(
                    "Released connection in isErrorDataAvailable");
        }
        return isErrorDataAvailable;
    }
    /**
     * Method to get the task file as a xml document.
     *
     * @param taskSimpleFile Task file of CSSimpleFile object
     * @return Returns xml document of the task file.
     */
    public Document getTaskDocument(CSSimpleFile taskSimpleFile) {
        logger.info("ErrorTask : getTaskDocument");
        Document document = null;
        try {
            byte[] taskSimpleFileByteArray = taskSimpleFile.read(0, -1);
            String taskSimpleFileString = new String(
                    taskSimpleFileByteArray);
            logger.info("taskSimpleFileString : " + taskSimpleFileString);
            document = DocumentHelper.parseText(taskSimpleFileString);
            logger.info("document : " + document.asXML());
        } catch (Exception e) {
            logger.info("Exception in getTaskDocument: ", e);
        }
        return document;
    }
    /**
     * Method to get the DCR value for the input node name
     *
     * @param document Document object of the DCR
     * @param nodeName Name of the value node
     * @return
     */
    public String getDCRValue(Document document, String nodeName) {
        logger.info("ErrorTask : getDCRValue");
        String dcrValue = document.selectSingleNode(nodeName).getText();
        logger.info(nodeName + " : " + dcrValue);
        return dcrValue;
    }


}

