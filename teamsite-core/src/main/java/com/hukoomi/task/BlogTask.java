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

import com.hukoomi.bo.BlogBO;
import com.hukoomi.utils.PostgreTSConnection;
import com.interwoven.cssdk.filesys.CSSimpleFile;

public class BlogTask {
    /**
     * Logger object to check the flow of the code.
     */
    private final Logger logger = Logger.getLogger(BlogTask.class);
    /**
     * DCR Type Meta data name
     */
    public static final String META_DATA_NAME_DCR_TYPE = "TeamSite/Templating/DCR/Type";
    /**
     * XPath to the blog id
     */
    public static final String ID_PATH = "/root/information/id";
    /**
     * XPath to the language selection
     */
    public static final String LANG_PATH = "/root/information/language/value";
    /**
     * XPath to the Title path
     */
    public static final String TITLE_PATH = "/root/information/title";
    /**
     * XPath to the blog updated date
     */
    public static final String UPDATE_DATE_PATH = "/root/information/date";
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
     * Blog update transition success message
     */
    private static final String BLOG_UPDATE_SUCCESS = "Blog master data updated successfully";
    /**
     * Blog update transition failure message
     */
    private static final String BLOG_UPDATE_FAILURE = "Failed to updated blog master data";
    /**
     * Blog insert transition success message
     */
    private static final String BLOG_INSERT_SUCCESS = "Blog master data inserted successfully";
    /**
     * Blog insert transition failure message
     */
    private static final String BLOG_INSERT_FAILURE = "Failed to insert blog master data";
    /**
     * Blog transition technical error message
     */
    private static final String BLOG_TECHNICAL_ERROR = "Technical Error in Blog master data insert";



    /**
     * Method process the poll dcr from the workflow task and insert poll master
     * data
     *
     * @param taskSimpleFile Task file of CSSimpleFile object
     * @return Returns map contains the transition status and transition comment.
     */
    public Map<String, String> processBlogDCR(
            CSSimpleFile taskSimpleFile,PostgreTSConnection postgre) {
        boolean isDBOperationSuccess = false;
        HashMap<String, String> statusMap = new HashMap<>();
        try {
            Document document = getTaskDocument(taskSimpleFile);
            if (isBlogMasterDataAvailable(document,postgre)) {
               int result = updateBlogData(document,postgre);
                logger.debug(
                        "isBlogDataUpdated : " + isDBOperationSuccess);
                if (result >0) {
                    statusMap.put(TRANSITION, SUCCESS_TRANSITION);
                    statusMap.put(TRANSITION_COMMENT, BLOG_UPDATE_SUCCESS);
                } else {
                    statusMap.put(TRANSITION, FAILURE_TRANSITION);
                    statusMap.put(TRANSITION_COMMENT, BLOG_UPDATE_FAILURE);
                }
            } else {
                isDBOperationSuccess = insertBlogData(document,postgre);
                logger.debug(
                        "isBlogDataInserted : " + isDBOperationSuccess);
                if (isDBOperationSuccess) {
                    statusMap.put(TRANSITION, SUCCESS_TRANSITION);
                    statusMap.put(TRANSITION_COMMENT, BLOG_INSERT_SUCCESS);
                } else {
                    statusMap.put(TRANSITION, FAILURE_TRANSITION);
                    statusMap.put(TRANSITION_COMMENT, BLOG_INSERT_FAILURE);
                }
            }
        } catch (Exception e) {
            statusMap.put(TRANSITION, FAILURE_TRANSITION);
            statusMap.put(TRANSITION_COMMENT, BLOG_TECHNICAL_ERROR);
            logger.error("Exception in poll master: ", e);
        }
        return statusMap;
    }

    private boolean insertBlogData(Document document,PostgreTSConnection postgre) {
        logger.debug("BlogTask : insertBlogData");
        Connection connection = null;
        boolean isBlogDataInserted = false;
        try {
            connection = postgre.getConnection();
            logger.debug("BlogTask : after getConnection");
            BlogBO blogBO = new BlogBO();
            blogBO.setBlogId(getDCRValue(document, ID_PATH));
            blogBO.setLang(getDCRValue(document, LANG_PATH));
            blogBO.setUpdatedDate(getDCRValue(document, UPDATE_DATE_PATH));
            blogBO.setTitle(getDCRValue(document, TITLE_PATH));
            logger.debug("getBlogId : "+ blogBO.getBlogId());
            logger.debug("getLang : "+ blogBO.getLang());
            logger.debug("getTitle : "+ blogBO.getTitle());
            logger.debug("getUpdatedDate : "+ blogBO.getUpdatedDate());
            int result = insertBolgMasterData(blogBO, connection,postgre);
            logger.info("insertBlogData result : " + result);
            if (result > 0) {
                logger.info("Blog Master Data Inserted");
                isBlogDataInserted = true;
            } else {
                logger.info("Blog master insert failed");
            }

        } catch (Exception e) {
                logger.error(
                        "Exception in insertBlogData rollback catch block : ",
                        e);
        } finally {
            postgre.releaseConnection(connection, null, null);
            logger.info("Released insertBlogData connection");
        }
        return isBlogDataInserted;
    }

    private int insertBolgMasterData(BlogBO blogBO,
            Connection connection,PostgreTSConnection postgre) {
        PreparedStatement preparedStatement = null;
        int result = 0;
        try {
            logger.info("BlogTask : insertBolgMasterData");

            String blogMasterQuery = "INSERT INTO BLOG_MASTER ("
                    + " DCR_ID, BLOG_TITLE, LANGUAGE, CREATED_DATE,"
                    + " STATUS) "
                    + "VALUES ( ?, ?, ?, LOCALTIMESTAMP, 'active')";
            logger.info("insertBolgMasterData surveyMasterQuery : "
                    + blogMasterQuery);
            preparedStatement = connection
                    .prepareStatement(blogMasterQuery);
            preparedStatement.setLong(1,
                    Long.parseLong(blogBO.getBlogId()));
            preparedStatement.setString(2, blogBO.getTitle());
            preparedStatement.setString(3, blogBO.getLang());
            result = preparedStatement.executeUpdate();
            logger.info("insertSurveyMasterData result : " + result);
        } catch (Exception e) {
            logger.error("Exception in insertSurveyMasterData: ", e);
        }
        finally {

                postgre.releaseConnection(connection, preparedStatement, null);
                logger.info("Released insertBlogData connection");

        }
        return result;
    }

    private int updateBlogData(Document document,PostgreTSConnection postgre) throws Exception {
        PreparedStatement preparedStatement = null;
        int result = 0;
        Connection connection = null;
        try {
            connection = postgre.getConnection();
            BlogBO blogBO = new BlogBO();
            blogBO.setBlogId(getDCRValue(document, ID_PATH));
            blogBO.setBlogId(getDCRValue(document, TITLE_PATH));
            blogBO.setLang(getDCRValue(document, LANG_PATH));
            blogBO.setUpdatedDate(getDCRValue(document, UPDATE_DATE_PATH));
            logger.info("BlogTask : updateBlogMasterData");
            String blogMasterQuery = "UPDATE BLOG_MASTER SET BLOG_TITLE = ?, UPDATED_DATE = LOCALTIMESTAMP "
                    + "WHERE DCR_ID = ? AND LANGUAGE = ?";
            logger.info("updateBlogMasterData pollMasterQuery : "
                    + blogMasterQuery);
            preparedStatement = connection
                    .prepareStatement(blogMasterQuery);
            preparedStatement.setString(1, blogBO.getTitle());
            preparedStatement.setString(2,
                    blogBO.getBlogId());
            preparedStatement.setString(3, blogBO.getLang());
            result = preparedStatement.executeUpdate();
            logger.info("updateBlogMasterData result : " + result);

        } catch (NumberFormatException | SQLException e) {
            logger.error("Exception in updateBlogMasterData: ", e);
            throw e;
        } finally {
            postgre.releaseConnection(null, preparedStatement, null);
            logger.info("Released updateBlogMasterData connection");
        }
        return result;
    }

    private boolean isBlogMasterDataAvailable(Document document,PostgreTSConnection postgre) {
        logger.debug("BlogTask : isBlogMasterDataAvailable");
        PreparedStatement prepareStatement = null;
        Connection connection = null;
        ResultSet rs = null;
        boolean isBlogDataAvailable = false;
        try {

            connection = postgre.getConnection();

            String dcrId = getDCRValue(document, ID_PATH);
            String lang = getDCRValue(document, LANG_PATH);

            String blogMasterQuery = "SELECT COUNT(*) FROM BLOG_MASTER WHERE DCR_ID = ? AND LANGUAGE = ?";
            logger.info("pollMasterQuery in isBlogMasterDataAvailable: "
                    + blogMasterQuery);
            prepareStatement = connection
                    .prepareStatement(blogMasterQuery);
            prepareStatement.setString(1, dcrId);
            prepareStatement.setString(2, lang);
            rs = prepareStatement.executeQuery();
            while (rs.next()) {
                int count = rs.getInt(1);
                if (count > 0) {
                    isBlogDataAvailable = true;
                }
            }
            logger.info("isBlogDataAvailable : " + isBlogDataAvailable);

        } catch (Exception e) {
            logger.error("Exception in isBlogMasterDataAvailable : ", e);
        } finally {
            postgre.releaseConnection(connection, prepareStatement, rs);
            logger.info(
                    "Released connection in isBlogMasterDataAvailable");
        }
        return isBlogDataAvailable;
    }
    /**
     * Method to get the task file as a xml document.
     *
     * @param taskSimpleFile Task file of CSSimpleFile object
     * @return Returns xml document of the task file.
     */
    public Document getTaskDocument(CSSimpleFile taskSimpleFile) {
        logger.debug("BlogTask : getTaskDocument");
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
     * Method to get the DCR value for the input node name
     *
     * @param document Document object of the DCR
     * @param nodeName Name of the value node
     * @return
     */
    public String getDCRValue(Document document, String nodeName) {
        logger.debug("BlogTask : getDCRValue");
        String dcrValue = document.selectSingleNode(nodeName).getText();
        logger.debug(nodeName + " : " + dcrValue);
        return dcrValue;
    }


}
