package com.hukoomi.task;

import com.hukoomi.utils.PostgreTSConnection;
import com.interwoven.cssdk.common.CSClient;
import com.interwoven.cssdk.common.CSException;
import com.interwoven.cssdk.filesys.CSAreaRelativePath;
import com.interwoven.cssdk.filesys.CSHole;
import com.interwoven.cssdk.filesys.CSSimpleFile;
import com.interwoven.cssdk.workflow.CSComment;
import com.interwoven.cssdk.workflow.CSExternalTask;
import com.interwoven.cssdk.workflow.CSURLExternalTask;
import com.interwoven.cssdk.workflow.CSWorkflow;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.*;

/**
 * DataBaseContentIngest is the workflow task class for dcr data
 * insert URL task. It contains methods to insert / update dcr
 * data to its corresponding tables.
 *
 */
public class DataBaseContentIngest implements CSURLExternalTask {
    /**
     * Logger object to check the flow of the code.
     */
    private final Logger logger = Logger.getLogger(DataBaseContentIngest.class);
    /**
     * DCR Type Meta data name
     */
    public static final String META_DATA_NAME_DCR_TYPE = "TeamSite/Templating/DCR/Type";
    /**
     * DCR modified date
     */
    public static final String MODIFIED_DATE = "TeamSite/Metadata/modifiedOn";
    /**
     * DCR review reject dates
     */
    public static final String META_REVIEW_REJECT_DATES = "TeamSite/Metadata/reviewRejectDates";
    /**
     * DCR Approve reject dates
     */
    public static final String META_APPROVE_REJECT_DATES = "TeamSite/Metadata/approveRejectDates";
    /**
     * DCR Review date
     */
    public static final String META_REVIEW_DATE = "TeamSite/Metadata/ReviewTime";
    /**
     * DCR Approve date
     */
    public static final String META_APPROVE_DATE = "TeamSite/Metadata/ApproveTime";
    /**
     * DCR Service Entity
     */
    public static final String META_ENTITY = "TeamSite/Metadata/Entity";
    /**
     * XPath to the poll /sruvey id
     */
    public static final String ID_PATH = "/root/information/id";
    /**
     * XPath to the DCR Name
     */
    public static final String DCR_NAME = "/root/information/original-dcr-name";
    /**
     * XPath to the language selection
     */
    public static final String LANG_PATH = "/root/information/language/value";
    /**
     * XPath to the event location date
     */
    public static final String EVENT_LOCATION = "/root/location/map";
    /**
     * XPath to the start date
     */
    public static final String START_DATE_PATH = "/root/information/date";
    /**
     * XPath to the event start date
     */
    public static final String EVENT_START_DATE_PATH = "/root/event-date/start-date";
    /**
     * XPath to the event end date
     */
    public static final String EVENT_END_DATE_PATH = "/root/event-date/end-date";
    /**
     * XPath to the last modified date
     */
    public static final String MODIFIED_DATE_PATH = "/root/information/last-modified";
    /**
     * XPath to the persona selection
     */
    public static final String PERSONA_PATH = "/root/settings/audiences/value";
    /**
     * XPath to the organization selection
     */
    public static final String SERVICE_ENTITIES = "/root/settings/service-entities/value";
    /**
     * XPath to the service-classification selection
     */
    public static final String SERVICE_CLASSIFICATION = "/root/settings/service-classification/value";
    /**
     * XPath to the organizer selection
     */
    public static final String ORGANIZER = "/root/settings/organizers/value";
    /**
     * XPath to the organizer selection
     */
    public static final String NEWS_SOURCE = "/root/settings/channels/value";
    /**
     * XPath to the topic selection
     */
    public static final String TOPICS = "/root/settings/topics/value";
    /**
     * XPath to the types selection
     */
    public static final String TYPES = "/root/settings/types/value";
    /**
     * XPath to the service-type selection
     */
    public static final String SERVICE_TYPE = "/root/settings/service-type/value";
    /**
     * XPath to the service-mode selection
     */
    public static final String SERVICE_MODE = "/root/settings/service-mode/value";
    /**
     * XPath to the survey title
     */
    public static final String TITLE_PATH = "/root/information/title";
    /**
     * DCR Version Meta data Date
     */
    public static final String META_DATA_VERSION = "Version";
    /**
     * XPath to the survey form field
     */
    public static final String CATEGORY = "/root/settings/categories/value";
    /**
     * Success transition message
     */
    public static final String SUCCESS_TRANSITION = "Data Base Log Completed";
    /**
     * Failure transition message
     */
    public static final String FAILURE_TRANSITION = "Insert Master Data Failure";
    /**
     * Poll insert transition success message
     */
    private static final String DATA_INSERT_SUCCESS = "Data inserted successfully";
    /**
     * Poll insert transition failure message
     */
    private static final String DATA_INSERT_FAILURE = "Failed to insert data";
    /**
     * Database property file name
     */
    private static final String DB_PROPERTY_FILE = "DbInsertConfig.properties";
    /**
     * Database property file name
     */
    private static final String LS_DB_PROPERTY_FILE = "dbconfig.properties";
    /*
     * Content Type DCR
     */
    private static String CONTENT_TYPE_DCR = "templatedata/Taxonomy/Content-Type/data/PortalContentTypes";
    /**
     * Transition hashmap key
     */
    private static final String TRANSITION = "TRANSITION";
    /**
     * Transition comment hashmap key
     */
    private static final String TRANSITION_COMMENT = "TRANSITION_COMMENT";
    /**
     * Postgre class instance variable
     */
    PostgreTSConnection postgre = null;
    PostgreTSConnection postgreLS = null;

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
        logger.info("Data Ingest Task - execute");
        HashMap<String, String> statusMap = null;
        CSAreaRelativePath[] taskFileList = task.getFiles();
        logger.debug("TaskFileList Length : " + taskFileList.length);
        postgre = new PostgreTSConnection(client, task, DB_PROPERTY_FILE);
        postgreLS = new PostgreTSConnection(client, task, LS_DB_PROPERTY_FILE);
        statusMap = new HashMap<>();
        statusMap.put(TRANSITION, SUCCESS_TRANSITION);
        statusMap.put(TRANSITION_COMMENT, "");

        for (CSAreaRelativePath taskFile : taskFileList) {
            try {
                CSSimpleFile taskSimpleFile = (CSSimpleFile) task.getArea()
                        .getFile(taskFile);
                logger.debug("File Name : " + taskSimpleFile.getName());

                statusMap = (HashMap<String, String>) process(
                        taskSimpleFile , task);

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
     * Method process the dcr from the workflow task and insert db
     * data
     *
     * @param taskSimpleFile Task file of CSSimpleFile object
     * @return Returns map contains the transition status and transition comment.
     */
    public Map<String, String> process(
            CSSimpleFile taskSimpleFile, CSExternalTask task) {
        boolean isDBOperationContentSuccess = false;
        boolean isDBOperationWorkflowSuccess = false;
        HashMap<String, String> statusMap = new HashMap<>();
        try {
            String dcrType = taskSimpleFile
                    .getExtendedAttribute(META_DATA_NAME_DCR_TYPE)
                    .getValue();

            if (dcrType != null) {
                logger.debug("File DCR Type : " + dcrType);
                Document document = getTaskDocument(taskSimpleFile);
                if(dcrType.contains("Taxonomy/")){
                    isDBOperationContentSuccess = insertTaxonomyData(taskSimpleFile, document, postgre);
                    logger.info("Inserted Taxonomy data to Auth DB: " + isDBOperationContentSuccess);
                    isDBOperationContentSuccess = insertTaxonomyData(taskSimpleFile, document, postgreLS);
                    logger.info("Inserted Taxonomy data to Runtime DB: " + isDBOperationContentSuccess);
                }else {
                    isDBOperationContentSuccess = insertData(taskSimpleFile, document);
                }
            }
            isDBOperationWorkflowSuccess = updateWorkflowData(taskSimpleFile, task);
            logger.debug(
                    "DBOperationContent : " + isDBOperationContentSuccess);
            logger.debug(
                    "DBOperationWorkflow : " + isDBOperationWorkflowSuccess);
            if ((null != dcrType && false != isDBOperationContentSuccess && false != isDBOperationWorkflowSuccess) || (null == dcrType && false != isDBOperationWorkflowSuccess)){
                statusMap.put(TRANSITION, SUCCESS_TRANSITION);
                statusMap.put(TRANSITION_COMMENT, DATA_INSERT_SUCCESS);
            } else {
                /*statusMap.put(TRANSITION, FAILURE_TRANSITION);
                statusMap.put(TRANSITION_COMMENT, DATA_INSERT_FAILURE);*/
                statusMap.put(TRANSITION, SUCCESS_TRANSITION);
                statusMap.put(TRANSITION_COMMENT, DATA_INSERT_FAILURE);
            }
        } catch (Exception e) {
            //statusMap.put(TRANSITION, FAILURE_TRANSITION);
            statusMap.put(TRANSITION, SUCCESS_TRANSITION);
            statusMap.put(TRANSITION_COMMENT, DATA_INSERT_FAILURE);
            logger.error("Exception : ", e);
        }
        return statusMap;
    }

    /**
     * Method to get the task file path.
     *
     * @param taskSimpleFile Task file of CSSimpleFile object
     * @return Returns path of file.
     */
    public String getTaskFile(CSSimpleFile taskSimpleFile) {
        String taskSimpleFileString = "";
        try {
            //  taskSimpleFileString = taskSimpleFile.getExtendedAttribute("Location").getValue();
            //   logger.debug(
            //      "taskSimpleFileString : " + taskSimpleFileString);
            //  logger.debug(
            //        "taskSimpleFile Vpath : " + taskSimpleFile.getVPath());

            byte[] taskSimpleFileByteArray = taskSimpleFile.read(0, -1);

            taskSimpleFileString = new String(taskSimpleFileByteArray);
        } catch (Exception e) {
            logger.error("Exception in getTaskFile: ", e);
        }
        return taskSimpleFileString;
    }
    /**
     * Method to get the task file path.
     *
     * @param taskSimpleFile Task file of CSSimpleFile object
     * @return Returns path of file.
     */
    public String getTaskFilePath(CSSimpleFile taskSimpleFile) {
        String taskSimpleFileString = "";
        try {
            taskSimpleFileString = taskSimpleFile.getVPath().toString();
            logger.debug(
                    "taskSimpleFileString : " + taskSimpleFileString);
        } catch (Exception e) {
            logger.error("Exception in getTaskFilePath: ", e);
        }
        return taskSimpleFileString;
    }
    /**
     * Method to get the task file path.
     *
     * @param taskSimpleFile Task file of CSSimpleFile object
     * @return Returns path of file.
     */
    public String getLang(CSSimpleFile taskSimpleFile) {
        String fileLocation = "";
        String lang = "";
        try {
            fileLocation = taskSimpleFile.getVPath().toString();
            logger.debug(
                    "fileLocation : " + fileLocation);
            if(StringUtils.contains(fileLocation,"/data/") && StringUtils.contains(fileLocation
                    ,"Content")) {
                lang = fileLocation.substring(fileLocation.indexOf("/data/") + 6, fileLocation.lastIndexOf("/"));
            }
            logger.debug(
                    "lang : " + lang);
        } catch (Exception e) {
            logger.error("Exception in getLang: ", e);
        }
        return lang;
    }
    /**
     * Method to get the task file as a xml document.
     *
     * @param taskSimpleFile Task file of CSSimpleFile object
     * @return Returns xml document of the task file.
     */
    public Document getTaskDocument(CSSimpleFile taskSimpleFile) {
        logger.debug("DB Insert Task : getTaskDocument");
        Document document = null;
        try {
            String taskSimpleFileString = getTaskFile(taskSimpleFile);
            document = DocumentHelper.parseText(taskSimpleFileString);
            logger.debug("document : " + document.asXML());
        } catch (Exception e) {
            logger.error("Exception in getTaskDocument: ", e);
        }
        return document;
    }
    public String getCategoryBasedUrl(CSSimpleFile taskSimpleFile,Document document) {
        String Url="";
        try {
            String category = taskSimpleFile.getExtendedAttribute(META_DATA_NAME_DCR_TYPE).getValue();
            logger.debug(" orig category : " + category);
            category = category.replaceAll("\\s+", "");
            logger.debug(" small letter category : " + category);
            String vPathWA = taskSimpleFile.getArea().getRootDir().getVPath().toString();
            vPathWA = vPathWA.replaceFirst("/(.*)default/","/default/");
            logger.info("VPath for the WorkArea: " + vPathWA);
            String contentMappingDCR = vPathWA + "/" + CONTENT_TYPE_DCR;
            logger.info("Content Mapping DCR: " + contentMappingDCR);
            File contentMappingFile = new File(contentMappingDCR);
            if(contentMappingFile.exists()) {
                SAXReader reader = new SAXReader();
                Document doc = reader.read(contentMappingFile);
                String catDetail = "";

                if (!doc.selectNodes("master/master-data/Key-Label").isEmpty()) {
                    logger.info("Taxonomy Master Key-Label ");
                    List<Node> labels = doc.selectNodes("master/master-data/Key-Label");
                    String catVal = "";
                    for (Node label : labels) {
                        catVal = label.selectSingleNode("Value").getText();
                        logger.info("Taxonomy Value : " + catVal);
                        if (catVal.equals(category)) {
                            catDetail = label.selectSingleNode("Detail").getText();
                            logger.info("Taxonomy Detail : " + catDetail);
                            break;
                        }
                    }
                }
                if (catDetail.isEmpty())
                    catDetail = category;
                logger.debug(" category for url : " + catDetail);
                Url = "/" + getLang(taskSimpleFile) + "/"
                        + catDetail + "/"
                        + getDCRValue(document, DCR_NAME);
            } else {
                logger.info("Category Mapping File "+ contentMappingDCR +" does not exist");
                Url = "/" + getLang(taskSimpleFile) + "/"
                        + category + "/"
                        + getDCRValue(document, DCR_NAME);
            }
        }catch(CSException | DocumentException e){
            logger.error("Exception occured in getCategoryBasedUrl : ", e);
        }
        logger.debug("Url :"+ Url);
        return Url;
    }
    /**
     * Method to get the document creation date.
     *
     * @param taskSimpleFile Task file of CSSimpleFile object
     * @return Returns path of file.
     */
    public Timestamp getCreationDate(CSSimpleFile taskSimpleFile) throws CSException {
        Timestamp createdDate = null;
        createdDate = new Timestamp(taskSimpleFile.getCreationDate().getTime());
        logger.debug("Created Date : "+createdDate.toString());
        return createdDate;
    }
    /**
     * Method to get the content type of the DCR.
     *
     * @param taskSimpleFile Task file of CSSimpleFile object
     * @return Returns content type.
     */
    public String getContentCategory(CSSimpleFile taskSimpleFile) {

        String fileLocation = "";
        String category="";
        try {
            fileLocation = taskSimpleFile.getVPath().toString();
            logger.debug(
                    "fileLocation : " + fileLocation);

            if(null != taskSimpleFile.getExtendedAttribute(META_DATA_NAME_DCR_TYPE).getValue()) {
                category = taskSimpleFile
                        .getExtendedAttribute(META_DATA_NAME_DCR_TYPE)
                        .getValue();
            }else{
                logger.info("File is not a Content DCR");
                if(fileLocation.contains("/WORKAREA/default/")) {
                    String [] path = fileLocation.substring(fileLocation.indexOf("/WORKAREA/default/")+18).split("/");
                    category = path[0]+"/"+path[1];
                }
            }

            logger.info("getContentCategory: category : "+category);


        } catch (Exception e) {
            logger.error("Exception in getContentCategory: ", e);
        }

        return category;
    }
    /**
     * Method to get the document modification date.
     *
     * @param taskSimpleFile Task file of CSSimpleFile object
     * @return Returns path of file.
     */
    public Timestamp getModificationDate(CSSimpleFile taskSimpleFile) throws CSException {
        Timestamp modifiedDate = null;
        Date contentModificationDate = taskSimpleFile.getContentModificationDate();
        modifiedDate = new Timestamp(contentModificationDate.getTime());
        logger.debug("modifiedDate : "+ modifiedDate);
        String modifyDate = taskSimpleFile.getExtendedAttribute("Modified").getValue();
        String modifiedDateVal = taskSimpleFile.getExtendedAttribute(MODIFIED_DATE).getValue();
        logger.debug("modifyDate : "+ modifyDate);
        logger.debug("modifiedDateVal : "+ modifiedDateVal);
        return modifiedDate;
    }
    /**
     * Method inserts the content
     *
     * @param taskSimpleFile    file path
     * @param document Document content
     * @return Returns number of rows affected by query execution
     * @throws SQLException
     */
    public boolean insertData(CSSimpleFile taskSimpleFile, Document document)
            throws SQLException, CSException {
        logger.debug("DataBaseContentIngest : insertData");
        String DcrId = getDCRValue(document, ID_PATH);
        if(DcrId == null)
            return true;
        Connection connection = null;
        boolean isDataInserted = false;
        //String Url = "/" + getDCRValue(document, getLang(taskSimpleFile)) + "/"
        //					+ getDCRValue(document, DCR_NAME);

        String modifyDate = taskSimpleFile.getExtendedAttribute("Modified").getValue();
        String modifiedDateVal = taskSimpleFile.getExtendedAttribute(MODIFIED_DATE).getValue();
        logger.debug("modifyDate : "+ modifyDate);
        logger.debug("modifiedDateVal : "+ modifiedDateVal);

        String fileStatus  = "PUBLISHED";
        if(taskSimpleFile.getKind() == CSHole.KIND)
            fileStatus = "DELETED";

        PreparedStatement preparedStatement = null;
        try {
            long version = 0;
            // if(taskSimpleFile
            // .getExtendedAttribute(META_DATA_VERSION)
            // .getValue() != null) {
            // version = Long.parseLong(taskSimpleFile
            // .getExtendedAttribute(META_DATA_VERSION)
            // .getValue());
            // }
            if(taskSimpleFile
                    .getRevisionNumber() > 0) {
                version = taskSimpleFile
                        .getRevisionNumber();
            }
            connection = postgre.getConnection();
            long millis = System.currentTimeMillis();
            Timestamp PublishDate = new Timestamp(millis);
            logger.info("Publish Date : "+PublishDate.toString());


            int result = 0;
            logger.info("DataBaseContentIngest : insertContentTableData");
            String query = "INSERT INTO CONTENT_TABLE(\"DCR_ID\", \"CONTENT_TYPE\",\"CONTENT_TITLE\", \"LANG\", \"CONTENT_PATH\", \"CONTENT_TOPIC\", \"CONTENT_CATEGORY\", \"CONTENT_PERSONA\", \"CONTENT_ENTITY\", \"AUTHOR\", \"CREATED_DATE\", \"PUBLISH_DATE\", \"LAST_MODIFIED_DATE\", \"CONTENT_VERSION\", \"CONTENT_STATUS\", \"SERVICE_TYPE\", \"SERVICE_MODE\", \"SERVICE_CLASSIFICATION\", \"URL\", \"NEWS_SOURCE\", \"EVENT_ORGANIZER\", \"EVENT_TYPE\", \"EVENT_START_DATE\", \"EVENT_END_DATE\", \"EVENT_LOCATION\") VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            logger.info("Query : " + query);
            connection.setAutoCommit(true);
            preparedStatement = connection
                    .prepareStatement(query);
            preparedStatement.setLong(1,
                    Long.parseLong(DcrId));
            preparedStatement.setString(2, taskSimpleFile
                    .getExtendedAttribute(META_DATA_NAME_DCR_TYPE)
                    .getValue());
            preparedStatement.setString(3, getDCRValue(document, TITLE_PATH));
            preparedStatement.setString(4, getLang(taskSimpleFile));
            preparedStatement.setString(5, getTaskFilePath(taskSimpleFile));
            preparedStatement.setString(6, getDCRValue(document, TOPICS));
            preparedStatement.setString(7, getDCRValue(document, CATEGORY));
            preparedStatement.setString(8, getDCRValue(document, PERSONA_PATH));
            preparedStatement.setString(9, getDCRValue(document, SERVICE_ENTITIES));
            // preparedStatement.setString(10, getDCRValue(document, ORGANIZER));
            preparedStatement.setString(10, taskSimpleFile.getLastModifier().getNormalizedName() );
            //preparedStatement.setTimestamp(11, getSqlDate(getDCRValue(document, START_DATE_PATH)));
            preparedStatement.setTimestamp(11, getCreationDate(taskSimpleFile));
            preparedStatement.setTimestamp(12, PublishDate);
            //  preparedStatement.setTimestamp(13, getSqlDate(getDCRValue(document, MODIFIED_DATE_PATH)));
            preparedStatement.setTimestamp(13, getModificationDate(taskSimpleFile));
            preparedStatement.setLong(14, version);
            preparedStatement.setString(15,fileStatus);
            preparedStatement.setString(16, getDCRValue(document, SERVICE_TYPE));
            preparedStatement.setString(17, getDCRValue(document, SERVICE_MODE));
            preparedStatement.setString(18, getDCRValue(document, SERVICE_CLASSIFICATION));
            preparedStatement.setString(19, getCategoryBasedUrl(taskSimpleFile, document));
            preparedStatement.setString(20, getDCRValue(document, NEWS_SOURCE));
            preparedStatement.setString(21, getDCRValue(document, ORGANIZER));
            preparedStatement.setString(22, getDCRValue(document, TYPES));
            preparedStatement.setTimestamp(23, getSqlDate(getDCRValue(document, EVENT_START_DATE_PATH)));
            preparedStatement.setTimestamp(24, getSqlDate(getDCRValue(document, EVENT_END_DATE_PATH)));
            preparedStatement.setString(25, getDCRValue(document, EVENT_LOCATION));
            logger.info("insertData preparedStatement : " + preparedStatement);
            result = preparedStatement.executeUpdate();
            logger.info("insertData result : " + result);
            if (result > 0) {
                isDataInserted = true;
            }
        } catch (NumberFormatException | SQLException e) {
            logger.error("Exception in insertData: ", e);
            throw e;
        } catch (Exception e) {
            logger.error("Exception in insertData: ", e);
        } finally {
            // connection.commit();
            // postgre.releaseConnection(null, preparedStatement, null);
            postgre.releaseConnection(connection, preparedStatement, null);
            logger.info("Released insertData connection");
        }
        return isDataInserted;
    }



    /**
     * Method inserts the taxonomy content
     *
     * @param taskSimpleFile    file path
     * @param document Document content
     * @return Returns number of rows affected by query execution
     * @throws SQLException
     */
    public boolean insertTaxonomyData(CSSimpleFile taskSimpleFile, Document document, PostgreTSConnection postgreConnection)
            throws SQLException, CSException {
        boolean isDataInserted = false;
        //String Url = "/" + getDCRValue(document, getLang(taskSimpleFile)) + "/"
        //					+ getDCRValue(document, DCR_NAME);

        String modifyDate = taskSimpleFile.getExtendedAttribute("Modified").getValue();
        String modifiedDateVal = taskSimpleFile.getExtendedAttribute(MODIFIED_DATE).getValue();
        logger.debug("modifyDate : "+ modifyDate);
        logger.debug("modifiedDateVal : "+ modifiedDateVal);
        String taxonomyType = "";
        String taxonomyKey = "";
        String labelEn = "";
        String labelAr = "";
        String taxonomyDcr = "";

        taxonomyDcr = taskSimpleFile.getName();
        logger.info("Taxonomy DCR Name : "+taxonomyDcr);

        taxonomyType = taskSimpleFile.getExtendedAttribute(META_DATA_NAME_DCR_TYPE).getValue();
        logger.info("Taxonomy Content Type : "+taxonomyType);
        taxonomyType = taxonomyType.replaceAll("Taxonomy/","");
        logger.info("Taxonomy Type : "+taxonomyType);
        if(document.selectNodes("master/master-data/Key-Label").size() > 0) {
            logger.info("Taxonomy Master Key-Label ");
            List<Node> labels = document.selectNodes("master/master-data/Key-Label");
            for (Node label : labels) {

                taxonomyKey = label.selectSingleNode("Value").getText();
                logger.info("Taxonomy Key : " + taxonomyKey);

                labelEn = label.selectSingleNode("LabelEn").getText();
                logger.info("Taxonomy English Label : " + labelEn);

                labelAr = label.selectSingleNode("LabelAr").getText();
                logger.info("Taxonomy Arabic Label : " + labelAr);

                isDataInserted = taxonomyQuery("en", taxonomyType, taxonomyKey, labelEn, taxonomyDcr,postgreConnection);
                logger.info("English Taxonomy Inserted / Updated : " + isDataInserted);

                isDataInserted = taxonomyQuery("ar", taxonomyType, taxonomyKey, labelAr, taxonomyDcr,postgreConnection);
                logger.info("Arabic Taxonomy Inserted / Updated : " + isDataInserted);

            }
        }else if (document.selectNodes("master/master-data/Category").size() > 0){
            logger.info("Taxonomy Master Category ");
            List<Node> categories = document.selectNodes("master/master-data/Category");
            for (Node category : categories) {

                String catLabelEn = "";
                String catLabelAr = "";
                String catKey = "";

                catLabelEn = category.selectSingleNode("LabelEn").getText();
                logger.info("Taxonomy category English Label : " + catLabelEn);

                catLabelAr = category.selectSingleNode("LabelAr").getText();
                logger.info("Taxonomy category Arabic Label : " + catLabelAr);

                catKey = category.selectSingleNode("Value").getText();
                logger.info("Taxonomy category Key : " + catKey);

                List<Node> subCategories = category.selectNodes("SubCategory");
                for (Node subCategory : subCategories) {

                    String subCatLabelEn = "";
                    String subCatLabelAr = "";
                    String subCatKey = "";

                    subCatLabelEn = subCategory.selectSingleNode("LabelEn").getText();
                    logger.info("Taxonomy subCategory English Label : " + subCatLabelEn);

                    subCatLabelAr = subCategory.selectSingleNode("LabelAr").getText();
                    logger.info("Taxonomy subCategory Arabic Label : " + subCatLabelAr);

                    subCatKey = subCategory.selectSingleNode("Value").getText();
                    logger.info("Taxonomy subCategory Key : " + subCatKey);

                    taxonomyKey = catKey+"-"+subCatKey;
                    logger.info("Taxonomy Key : " + taxonomyKey);

                    labelEn = subCatLabelEn+" ( "+catLabelEn+" )";
                    logger.info("Taxonomy English Label : " + labelEn);

                    labelAr = subCatLabelAr+" ( "+catLabelAr+" )";
                    logger.info("Taxonomy Arabic Label : " + labelAr);

                    isDataInserted = taxonomyQuery("en", taxonomyType, taxonomyKey, labelEn, taxonomyDcr,postgreConnection);
                    logger.info("English Taxonomy Inserted / Updated : " + isDataInserted);

                    isDataInserted = taxonomyQuery("ar", taxonomyType, taxonomyKey, labelAr, taxonomyDcr,postgreConnection);
                    logger.info("Arabic Taxonomy Inserted / Updated : " + isDataInserted);
                }

            }
        }

        return isDataInserted;
    }


    public boolean taxonomyQuery(String lang, String type,String key,String label,String dcr,PostgreTSConnection postgreConnection ){
        logger.debug("DataBaseContentIngest : insertTaxonomyData");
        Connection connection = null;
        boolean isDataSelected = false;
        boolean isDataInserted = false;
        ResultSet result = null;
        PreparedStatement preparedStatement = null;
        try {
            long version = 0;
            // if(taskSimpleFile
            // .getExtendedAttribute(META_DATA_VERSION)
            // .getValue() != null) {
            // version = Long.parseLong(taskSimpleFile
            // .getExtendedAttribute(META_DATA_VERSION)
            // .getValue());
            // }

            connection = postgreConnection.getConnection();


            logger.info("DataBaseContentIngest : taxonomyQuery");
            String selectQuery = "SELECT COUNT(*) FROM TAXONOMY_TABLE WHERE \"LANG\" = ? AND \"TAXONOMY_TYPE\" = ? AND \"KEY\" = ? AND \"TAXONOMY_DCR\" = ?";
            logger.info("selectQuery : " + selectQuery);

            connection.setAutoCommit(true);
            preparedStatement = connection
                    .prepareStatement(selectQuery);
            preparedStatement.setString(1,lang);
            preparedStatement.setString(2,type);
            preparedStatement.setString(3, key);
            preparedStatement.setString(4, dcr);

            logger.info("insertData preparedStatement : " + preparedStatement);
            // result = preparedStatement.executeUpdate();
            result = preparedStatement.executeQuery();

            logger.info("selectQuery result : " + result.toString());
            while (result.next()) {
                int count = result.getInt(1);
                logger.info("insertData result count: " + count);
                if(count > 0) isDataSelected = true;
            }


        } catch (NumberFormatException | SQLException e) {
            logger.error("Exception in insertData: ", e);

        } catch (Exception e) {
            logger.error("Exception in insertData: ", e);
        } finally {
            // connection.commit();
            // postgre.releaseConnection(null, preparedStatement, null);
            postgre.releaseConnection(connection, preparedStatement, null);
            logger.info("Released taxonomyQuery connection");
            if(isDataSelected){
                isDataInserted = taxonomyUpdateQuery(lang, type, key, label, dcr,postgreConnection );
            }else{
                isDataInserted = taxonomyInsertQuery(lang, type, key, label, dcr,postgreConnection );
            }
        }
        return isDataInserted;
    }

    public boolean taxonomyUpdateQuery(String lang, String type,String key,String label,String dcr,PostgreTSConnection postgreConnection ){
        logger.debug("DataBaseContentIngest : updateTaxonomyData");
        Connection connection = null;
        boolean isDataInserted = false;
        PreparedStatement preparedStatement = null;
        try {

            connection = postgreConnection.getConnection();

            int result = 0;
            logger.info("DataBaseContentIngest : taxonomyUpdateQuery");
            String updateQuery = "UPDATE TAXONOMY_TABLE SET \"LABEL\" = ? WHERE \"LANG\" = ? AND \"TAXONOMY_TYPE\" = ? AND \"KEY\" = ? AND \"TAXONOMY_DCR\" = ?";
            logger.info("updateQuery : " + updateQuery);

            connection.setAutoCommit(true);
            preparedStatement = connection
                    .prepareStatement(updateQuery);
            preparedStatement.setString(1, label);
            preparedStatement.setString(2, lang);
            preparedStatement.setString(3, type);
            preparedStatement.setString(4, key);
            preparedStatement.setString(5, dcr);

            logger.info("updateData preparedStatement : " + preparedStatement);
            result = preparedStatement.executeUpdate();
            logger.info("updateQuery result : " + result);
            if (result > 0) {
                isDataInserted = true;
            }
        } catch (NumberFormatException | SQLException e) {
            logger.error("Exception in updateData: ", e);

        } catch (Exception e) {
            logger.error("Exception in updateData: ", e);
        } finally {
            // connection.commit();
            // postgre.releaseConnection(null, preparedStatement, null);
            postgre.releaseConnection(connection, preparedStatement, null);
            logger.info("Released updateData connection");
        }
        return isDataInserted;
    }
    public boolean taxonomyInsertQuery(String lang, String type,String key,String label,String dcr,PostgreTSConnection postgreConnection ){
        logger.debug("DataBaseContentIngest : insertTaxonomyData");
        Connection connection = null;
        boolean isDataInserted = false;
        PreparedStatement preparedStatement = null;
        try {

            connection = postgreConnection.getConnection();

            int result = 0;
            logger.info("DataBaseContentIngest : taxonomyInsertQuery");
            String insertQuery = "INSERT INTO TAXONOMY_TABLE(\"LANG\", \"TAXONOMY_TYPE\", \"KEY\", \"LABEL\", \"TAXONOMY_DCR\") VALUES ( ?, ?, ?, ?, ?)";
            logger.info("insertQuery : " + insertQuery);

            connection.setAutoCommit(true);
            preparedStatement = connection
                    .prepareStatement(insertQuery);
            preparedStatement.setString(1, lang);
            preparedStatement.setString(2, type);
            preparedStatement.setString(3, key);
            preparedStatement.setString(4, label);
            preparedStatement.setString(5, dcr);

            logger.info("insertData preparedStatement : " + preparedStatement);
            result = preparedStatement.executeUpdate();
            logger.info("insertData result : " + result);
            if (result > 0) {
                isDataInserted = true;
            }
        } catch (NumberFormatException | SQLException e) {
            logger.error("Exception in updateData: ", e);

        } catch (Exception e) {
            logger.error("Exception in updateData: ", e);
        } finally {
            // connection.commit();
            // postgre.releaseConnection(null, preparedStatement, null);
            postgre.releaseConnection(connection, preparedStatement, null);
            logger.info("Released insertQuery connection");
        }
        return isDataInserted;
    }
    /**
     * Method inserts the content
     *
     * @param taskSimpleFile    file path
     * @param task content
     * @return Returns number of rows affected by query execution
     * @throws SQLException
     */
    /*public boolean insertWorkFlowData(CSSimpleFile taskSimpleFile, CSExternalTask task)
            throws SQLException, CSException {
        logger.debug("DataBaseContentIngest : insertWorkFlowData");
        Connection connection = null;
        boolean isDataInserted = false;

        PreparedStatement preparedStatement = null;
        try {

            // if(taskSimpleFile
            // .getExtendedAttribute(META_DATA_VERSION)
            // .getValue() != null) {
            // version = Long.parseLong(taskSimpleFile
            // .getExtendedAttribute(META_DATA_VERSION)
            // .getValue());
            // }
            logger.info("Workflow Details : "+task.getWorkflow().toString());

            //logger.info("Parent Owner : "+task.getWorkflow().getParentTask().getOwner());
            // logger.info("Event Details : "+task.getWorkflow().getEvents().toString());
            String reviewer = "" ;
            String approver = "" ;
            String reviewRejectDates = "";
            String approveRejectDates = "";
            String reviewDate = "";
            String approveDate = "";
            String[] dateFormats =  {"EEE MMM dd yyyy HH:mm:ss","EEE MMM dd HH:mm:ss yyyy"};

            reviewDate = taskSimpleFile.getExtendedAttribute(META_REVIEW_DATE)
                    .getValue();
            logger.info("Review Date : " + reviewDate );
            Timestamp reviewedDate = new Timestamp(DateUtils.parseDate(reviewDate,dateFormats).getTime());
            logger.info("reviewedDate : " + reviewedDate );


            approveDate = taskSimpleFile.getExtendedAttribute(META_APPROVE_DATE)
                    .getValue();
            logger.info("Approve Date : " + approveDate );
            Timestamp approvalDate = new Timestamp(DateUtils.parseDate(approveDate,dateFormats).getTime());
            logger.info("approvalDate : " + approvalDate );

            reviewer = task.getWorkflow().getVariable("WF_Reviewer");
            logger.info("Workflow Reviewer : " + reviewer );

            approver = task.getWorkflow().getVariable("WF_Approver");
            logger.info("Workflow Approver : " + approver );


            //    logger.info("util reviewer : "+ WorkflowUtils.findVariable(task,"HK_Reviewer"));
            //    logger.info("util approve_fail_time : "+ WorkflowUtils.findVariable(task,"approve_fail_time"));
            logger.info("task time  : "+ taskSimpleFile.getExtendedAttribute("timeStamp"));
            logger.info("task time local : "+ taskSimpleFile.getExtendedAttribute("localTimeStamp"));
            logger.info("task output stream : "+ taskSimpleFile.getOutputStream(true).toString());


            connection = postgre.getConnection();
            logger.info("DB connection : "+connection.toString());
            long millis = System.currentTimeMillis();
            Timestamp PublishDate = new Timestamp(millis);
            logger.info("Publish Date : "+PublishDate.toString());
            int result = 0;
            logger.info("DataBaseContentIngest : insertWorkFlowData");
            String query = "INSERT INTO WORKFLOW_TABLE(\"WORKFLOW_ID\", \"WORKFLOW_USER\",\"CATEGORY\", \"CONTENT_PATH\", \"LANG\", \"WORKFLOW_START_DATE\", \"WORKFLOW_END_DATE\", \"WORKFLOW_REVIEWER\", \"REVIEW_DATE\", \"WORKFLOW_APPROVER\", \"APPROVAL_DATE\", \"COUNT_REJECT_REVIEW\", \"COUNT_REJECT_APPROVE\", \"PUBLISH_DATE\") VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            logger.info("Query : " + query);
            connection.setAutoCommit(true);
            preparedStatement = connection
                    .prepareStatement(query);
            preparedStatement.setLong(1,
                    (task.getWorkflowId()));
            preparedStatement.setString(2, task.getWorkflow().getOwner().getNormalizedName());
            preparedStatement.setString(3, getContentCategory(taskSimpleFile));
            preparedStatement.setString(4, getTaskFilePath(taskSimpleFile));
            preparedStatement.setString(5, getLang(taskSimpleFile));
            preparedStatement.setTimestamp(6, new Timestamp(task.getWorkflow().getActivationDate().getTime()));
            preparedStatement.setTimestamp(7, PublishDate);
            preparedStatement.setString(8, reviewer);
            preparedStatement.setTimestamp(9, reviewedDate);
            preparedStatement.setString(10, approver);
            preparedStatement.setTimestamp(11, approvalDate);
            preparedStatement.setLong(12,getRejectCount(taskSimpleFile.getExtendedAttribute(META_REVIEW_REJECT_DATES).getValue() ));
            preparedStatement.setLong(13,getRejectCount(taskSimpleFile.getExtendedAttribute(META_APPROVE_REJECT_DATES).getValue() ));
            preparedStatement.setTimestamp(14, PublishDate);
            logger.info("insertData preparedStatement : " + preparedStatement);
            result = preparedStatement.executeUpdate();
            logger.info("insertData result : " + result);
            if (result > 0) {
                isDataInserted = true;
            }
        } catch (NumberFormatException | SQLException e) {
            logger.error("Exception in insertData: ", e);
            throw e;
        } catch (Exception e) {
            logger.error("Exception in insertData: ", e);
        } finally {
            // connection.commit();
            // postgre.releaseConnection(null, preparedStatement, null);
            postgre.releaseConnection(connection, preparedStatement, null);
            logger.info("Released insertData connection");
        }
        return isDataInserted;
    } */

    /**
     * Method updates the content
     *
     * @param taskSimpleFile    file path
     * @param taskObj content
     * @return Returns number of rows affected by query execution
     * @throws SQLException
     */
    public boolean updateWorkflowData(CSSimpleFile taskSimpleFile, CSExternalTask taskObj) throws SQLException, CSException{
        logger.debug("DataBaseContentIngest : updateWorkflowData");
        Connection connection = null;
        boolean isDataUpdated = false;
        PreparedStatement preparedStatement = null;

        String reviewDate = "";
        String approveDate = "";
        String[] dateFormats =  {"EEE MMM dd yyyy HH:mm:ss","EEE MMM dd HH:mm:ss yyyy","EEE MMM dd HH:mm:ss zzz yyyy"};
        String jobStatus = "PUBLISHED";
        String fileStatus  = "";
        String commentStr = "";
        String entityVal = taskSimpleFile.getExtendedAttribute(META_ENTITY).getValue();
        if(entityVal == null)
            entityVal = "";

        try {

            //reviewDate = taskSimpleFile.getExtendedAttribute(META_REVIEW_DATE).getValue();
            reviewDate = taskObj.getWorkflow().getVariable(META_REVIEW_DATE);
            logger.info("Review Date : " + reviewDate );

            Timestamp reviewedDate = null;
            if(reviewDate != null)
                reviewedDate = new Timestamp(DateUtils.parseDate(reviewDate,dateFormats).getTime());
            logger.info("reviewedDate : " + reviewedDate );


            //approveDate = taskSimpleFile.getExtendedAttribute(META_APPROVE_DATE).getValue();
            approveDate = taskObj.getWorkflow().getVariable(META_APPROVE_DATE);
            logger.info("Approve Date : " + approveDate );

            Timestamp approvalDate = null;
            if(approveDate != null)
                approvalDate = new Timestamp(DateUtils.parseDate(approveDate,dateFormats).getTime());
            logger.info("approvalDate : " + approvalDate );

            if(taskSimpleFile.getKind() == CSHole.KIND){
                fileStatus = "DELETE";
            }else if(taskSimpleFile.getRevisionNumber() == 0){
                fileStatus = "ADD";
            }else if(taskSimpleFile.getRevisionNumber() > 0){
                fileStatus = "MODIFY";
            }

            CSWorkflow workflow = taskObj.getWorkflow();
            CSComment[] comments = workflow.getComments();
            StringBuilder commentsToLogInDB = new StringBuilder();
            for(CSComment comment : comments) {
                commentsToLogInDB.append("["+ comment.getCreationDate() +"] "+ comment.getCreator() + ": "+ comment.getComment() + System.lineSeparator());
            }
            commentStr = commentsToLogInDB.toString();

            connection = postgre.getConnection();
            long millis = System.currentTimeMillis();
            Timestamp PublishDate = new Timestamp(millis);
            logger.info("Publish Date : "+PublishDate.toString());

            int result = 0;
            String updateQuery = "UPDATE WORKFLOW_TABLE SET \"WORKFLOW_END_DATE\" = ?, \"REVIEW_DATE\" = ?, \"APPROVAL_DATE\" = ?, \"COUNT_REJECT_REVIEW\" = ?, \"COUNT_REJECT_APPROVE\" = ?, \"PUBLISH_DATE\" = ?, \"WORKFLOW_STATUS\" = ?, \"FILE_STATUS\" = ?, \"WORKFLOW_COMMENTS\" = ?, \"ENTITY\" = ? WHERE \"WORKFLOW_ID\" = ? AND \"CONTENT_PATH\" = ?";
            logger.info("updateQuery : " + updateQuery);

            connection.setAutoCommit(true);
            preparedStatement = connection.prepareStatement(updateQuery);
            preparedStatement.setTimestamp(1, PublishDate);
            preparedStatement.setTimestamp(2, reviewedDate);
            preparedStatement.setTimestamp(3, approvalDate);
            //preparedStatement.setLong(4,getRejectCount(taskSimpleFile.getExtendedAttribute(META_REVIEW_REJECT_DATES).getValue() ));
            preparedStatement.setLong(4,getRejectCount(taskObj.getWorkflow().getVariable(META_REVIEW_REJECT_DATES)));
            //preparedStatement.setLong(5,getRejectCount(taskSimpleFile.getExtendedAttribute(META_APPROVE_REJECT_DATES).getValue() ));
            preparedStatement.setLong(5,getRejectCount(taskObj.getWorkflow().getVariable(META_APPROVE_REJECT_DATES)));
            preparedStatement.setTimestamp(6, PublishDate);
            preparedStatement.setString(7,jobStatus);
            preparedStatement.setString(8,fileStatus);
            preparedStatement.setString(9,commentStr);
            preparedStatement.setString(10,entityVal);
            preparedStatement.setLong(11,taskObj.getWorkflowId());
            preparedStatement.setString(12,getTaskFilePath(taskSimpleFile));

            logger.info("updateData preparedStatement : " + preparedStatement);
            result = preparedStatement.executeUpdate();
            logger.info("updateQuery result : " + result);
            if (result > 0) {
                isDataUpdated = true;
                taskObj.getWorkflow().deleteVariable(META_REVIEW_DATE);
                taskObj.getWorkflow().deleteVariable(META_APPROVE_DATE);
                taskObj.getWorkflow().deleteVariable(META_REVIEW_REJECT_DATES);
                taskObj.getWorkflow().deleteVariable(META_APPROVE_REJECT_DATES);
            }
        }catch (NumberFormatException | SQLException e) {
            logger.error("NumberFormatException/SQL Exception in updateData: ", e);
            throw e;
        } catch (Exception e) {
            logger.error("Exception in updateData: ", e);
        } finally {
            postgre.releaseConnection(connection, preparedStatement, null);
            logger.info("Released updateData connection");
        }
        return isDataUpdated;
    }

    public int getRejectCount(String rejectDateString){
        int rejectCount = 0;
        rejectDateString = rejectDateString != null ? rejectDateString : "";
        logger.info(" Reject Dates : " + rejectDateString );

        rejectCount = rejectDateString.replaceAll("\\s+","").length() / 20;
        logger.info(" Reject Count : " + rejectCount );
        return rejectCount;
    }



    /**
     * Method to get the DCR value for the input node name
     *
     * @param document Document object of the DCR
     * @param nodeName Name of the value node
     * @return string node value.
     */
    public String getDCRValue(Document document, String nodeName) {
        logger.info("DB Content Ingest Task : getDCRValue");
        String dcrValue = null;
        if(document.selectSingleNode(nodeName) != null) {
            dcrValue = document.selectSingleNode(nodeName).getText();
            logger.info(nodeName + " Exists");
        }
        logger.info(nodeName + " : " + dcrValue);
        return dcrValue;
    }

    /**
     * Method to get only the date object without time for the input date time
     * string.
     *
     * @param inputDate Input date string.
     * @return Returns Date object created from the input date string.
     */
    public Timestamp getSqlDate(String inputDate) {
        logger.debug("Data Insert Task : getDate");
        Timestamp sqlDate = null;
        try {
            if(inputDate != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
                java.util.Date date = sdf.parse(inputDate);
                sqlDate = new Timestamp(date.getTime());
                logger.info(inputDate + " >>> " + sqlDate);
            } else {
                logger.info(inputDate + " >>> null");
            }
        } catch(Exception e) {
            logger.error("Exception in insertData: ", e);
        }
        return sqlDate;
    }

}
