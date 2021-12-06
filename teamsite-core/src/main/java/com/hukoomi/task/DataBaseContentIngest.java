package com.hukoomi.task;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import com.hukoomi.utils.PostgreTSConnection;
import com.interwoven.cssdk.common.CSClient;
import com.interwoven.cssdk.common.CSException;
import com.interwoven.cssdk.filesys.CSAreaRelativePath;
import com.interwoven.cssdk.filesys.CSFile;
import com.interwoven.cssdk.filesys.CSHole;
import com.interwoven.cssdk.filesys.CSSimpleFile;
import com.interwoven.cssdk.filesys.CSVPath;
import com.interwoven.cssdk.workflow.CSComment;
import com.interwoven.cssdk.workflow.CSExternalTask;
import com.interwoven.cssdk.workflow.CSURLExternalTask;
import com.interwoven.cssdk.workflow.CSWorkflow;

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
     * XPath to the event location date
     */
    public static final String EVENT_LOCATION = "/root/location/map";
    /**
     * XPath to the event start date
     */
    public static final String EVENT_START_DATE_PATH = "/root/event-date/start-date";
    /**
     * XPath to the event end date
     */
    public static final String EVENT_END_DATE_PATH = "/root/event-date/end-date";
    /**
     * XPath to the persona selection
     */
    public static final String PERSONA_PATH = "/root/settings/audiences/value";
    /**
     * XPath to the organization selection
     */
    public static final String SERVICE_ENTITIES = "/root/settings/service-entities/value";
    /**
     * XPath to the mobile apps provider
     */
    public static final String MOBILE_APPS_PROVIDER = "/root/settings/providers/value";
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
     * XPath to the survey form field
     */
    public static final String CATEGORY = "/root/settings/categories/value";
    /**
     * Success transition message
     */
    public static final String SUCCESS_TRANSITION = "Data Base Log Completed";
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
    private static final String CONTENT_TYPE_DCR = "templatedata/Taxonomy/Content-Type/data/PortalContentTypes";
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
        HashMap<String, String> statusMap;
        CSAreaRelativePath[] taskFileList = task.getFiles();
        logger.debug("TaskFileList Length : " + taskFileList.length);
        postgre = new PostgreTSConnection(client, task, DB_PROPERTY_FILE);
        postgreLS = new PostgreTSConnection(client, task, LS_DB_PROPERTY_FILE);
        statusMap = new HashMap<>();
        statusMap.put(TRANSITION, SUCCESS_TRANSITION);
        statusMap.put(TRANSITION_COMMENT, "");

        for (CSAreaRelativePath taskFile : taskFileList) {
            try {
                CSFile taskSimpleFile = task.getArea()
                        .getFile(taskFile);
                logger.info("File Name : " + taskSimpleFile.getName());

                statusMap = (HashMap<String, String>) process(
                        taskSimpleFile , task, client);

            } catch (Exception e) {
                logger.error("Exception in execute: ", e);
            }
        }

        //remove metadata
        deleteMetaData(task);

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
     * @param taskFile Task file of CSSimpleFile object
     * @return Returns map contains the transition status and transition comment.
     */
    public Map<String, String> process(
            CSFile taskFile, CSExternalTask task, CSClient client) {
        boolean isDBOperationContentSuccess = false;
        boolean isDBOperationWorkflowSuccess;
        DataBaseStatusIngest dataBaseStatusIngest = new DataBaseStatusIngest();
        HashMap<String, String> statusMap = new HashMap<>();
        try {
            String dcrType = dataBaseStatusIngest.getContentCategory(taskFile);

            if (dcrType != null) {
                logger.debug("File DCR Type : " + dcrType);
                Document document = getTaskDocument(taskFile, client);
                if(dcrType.contains("Taxonomy/")){
                    isDBOperationContentSuccess = insertTaxonomyData(taskFile, document, postgre);
                    logger.info("Inserted Taxonomy data to Auth DB: " + isDBOperationContentSuccess);
                    isDBOperationContentSuccess = insertTaxonomyData(taskFile, document, postgreLS);
                    logger.info("Inserted Taxonomy data to Runtime DB: " + isDBOperationContentSuccess);
                }else {
                    isDBOperationContentSuccess = insertData(taskFile, document);
                }
            }
            isDBOperationWorkflowSuccess = updateWorkflowData(taskFile, task);
            logger.debug(
                    "DBOperationContent : " + isDBOperationContentSuccess);
            logger.debug(
                    "DBOperationWorkflow : " + isDBOperationWorkflowSuccess);
            statusMap.put(TRANSITION, SUCCESS_TRANSITION);
            if ((null != dcrType && isDBOperationContentSuccess && isDBOperationWorkflowSuccess) || (null == dcrType && isDBOperationWorkflowSuccess)){
                statusMap.put(TRANSITION_COMMENT, DATA_INSERT_SUCCESS);
            } else {
                statusMap.put(TRANSITION_COMMENT, DATA_INSERT_FAILURE);
            }
        } catch (Exception e) {
            statusMap.put(TRANSITION, SUCCESS_TRANSITION);
            statusMap.put(TRANSITION_COMMENT, DATA_INSERT_FAILURE);
            logger.error("Exception : ", e);
        }
        return statusMap;
    }

    /**
     * Method to get the task file path.
     *
     * @param taskFile Task file of CSSimpleFile object
     * @return Returns path of file.
     */
    public String getTaskFile(CSFile taskFile, CSClient client) {
        String taskSimpleFileString = "";
        try {
            if(taskFile.getKind()!=CSHole.KIND) {
                logger.info("Regular File");
                CSSimpleFile taskSimpleFile = (CSSimpleFile) taskFile;
                byte[] taskSimpleFileByteArray = taskSimpleFile.read(0, -1);

                taskSimpleFileString = new String(taskSimpleFileByteArray);
            } else {
                logger.info("Deleted File");
                CSSimpleFile taskSimpleFile = (CSSimpleFile) client.getFile(new CSVPath(taskFile.getVPath().toString().replaceAll("WORKAREA/default","STAGING/")));
                if(taskSimpleFile != null) {
                    logger.info(taskSimpleFile.getVPath());
                    byte[] taskSimpleFileByteArray = taskSimpleFile.read(0, -1);

                    taskSimpleFileString = new String(taskSimpleFileByteArray);
                } else {
                    logger.info("Could not find Corresponding Stage File");
                }
            }
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
    public String getTaskFilePath(CSFile taskSimpleFile) {
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
     * Method to get the task file as a xml document.
     *
     * @param taskSimpleFile Task file of CSSimpleFile object
     * @return Returns xml document of the task file.
     */
    public Document getTaskDocument(CSFile taskSimpleFile, CSClient client) {
        logger.debug("DB Insert Task : getTaskDocument");
        Document document = null;
        try {
            String taskSimpleFileString = getTaskFile(taskSimpleFile, client);
            document = DocumentHelper.parseText(taskSimpleFileString);
            logger.debug("document : " + document.asXML());
        } catch (Exception e) {
            logger.error("Exception in getTaskDocument: ", e);
        }
        return document;
    }
    public String getCategoryBasedUrl(CSFile taskSimpleFile,Document document) {
        String url="";
        String valueNode = "Value";
        DataBaseStatusIngest dataBaseStatusIngest = new DataBaseStatusIngest();
        try {
            String category = dataBaseStatusIngest.getContentCategory(taskSimpleFile);
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
                String keyLabelNode = "master/master-data/Key-Label";
                if (!doc.selectNodes(keyLabelNode).isEmpty()) {
                    logger.info("Taxonomy Master Key-Label ");
                    List<Node> labels = doc.selectNodes(keyLabelNode);
                    String catVal;
                    for (Node label : labels) {
                        catVal = label.selectSingleNode(valueNode).getText();
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
                url = "/" + dataBaseStatusIngest.getLang(taskSimpleFile) + "/"
                        + catDetail + "/"
                        + getDCRValue(document, DCR_NAME);
            } else {
                logger.info("Category Mapping File "+ contentMappingDCR +" does not exist");
                url = "/" + dataBaseStatusIngest.getLang(taskSimpleFile) + "/"
                        + category + "/"
                        + getDCRValue(document, DCR_NAME);
            }
        }catch(CSException | DocumentException e){
            logger.error("Exception occured in getCategoryBasedUrl : ", e);
        }
        logger.debug("Url :"+ url);
        return url;
    }
    /**
     * Method to get the document creation date.
     *
     * @param taskSimpleFile Task file of CSSimpleFile object
     * @return Returns path of file.
     */
    public Timestamp getCreationDate(CSFile taskSimpleFile) throws CSException {
        Timestamp createdDate;
        createdDate = new Timestamp(taskSimpleFile.getCreationDate().getTime());
        logger.debug("Created Date : "+createdDate.toString());
        return createdDate;
    }

    /**
     * Method to get the document modification date.
     *
     * @param taskSimpleFile Task file of CSSimpleFile object
     * @return Returns path of file.
     */
    public Timestamp getModificationDate(CSFile taskSimpleFile) throws CSException {
        Timestamp modifiedDate;
        Date contentModificationDate = taskSimpleFile.getContentModificationDate();
        modifiedDate = new Timestamp(contentModificationDate.getTime());
        logger.debug("modifiedDate : "+ modifiedDate);
        return modifiedDate;
    }
    /**
     * Method inserts the content
     *
     * @param taskFile    file path
     * @param document Document content
     * @return Returns number of rows affected by query execution
     */
    public boolean insertData(CSFile taskFile, Document document) {
        logger.info("DataBaseContentIngest : insertData");
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        boolean isDataInserted = false;
        String fileStatus  = "PUBLISHED";
        String dcrId = getDCRValue(document, ID_PATH);
        if(dcrId == null)
            dcrId = "0000000000000";
        try {
            DataBaseStatusIngest dataBaseStatusIngest = new DataBaseStatusIngest();
            if(taskFile.getKind() == CSHole.KIND){
                fileStatus = "DELETED";
            } else {
                CSSimpleFile taskSimpleFile = (CSSimpleFile) taskFile;
                String modifiedDateVal = taskSimpleFile.getExtendedAttribute(MODIFIED_DATE).getValue();
                logger.debug("modifiedDateVal : "+ modifiedDateVal);
            }
            logger.info("File Status :" + fileStatus + " for File: " + taskFile.getVPath());
            long version = 0;
            if(taskFile
                    .getRevisionNumber() > 0) {
                version = taskFile
                        .getRevisionNumber();
            }
            connection = postgre.getConnection();
            long millis = System.currentTimeMillis();
            Timestamp publishDate = new Timestamp(millis);
            logger.info("Publish Date : "+publishDate.toString());

            int result;
            logger.info("DataBaseContentIngest : insertContentTableData");
            String query = "INSERT INTO CONTENT_TABLE(\"DCR_ID\", \"CONTENT_TYPE\",\"CONTENT_TITLE\", \"LANG\", \"CONTENT_PATH\", \"CONTENT_TOPIC\", \"CONTENT_CATEGORY\", \"CONTENT_PERSONA\", \"CONTENT_ENTITY\", \"AUTHOR\", \"CREATED_DATE\", \"PUBLISH_DATE\", \"LAST_MODIFIED_DATE\", \"CONTENT_VERSION\", \"CONTENT_STATUS\", \"SERVICE_TYPE\", \"SERVICE_MODE\", \"SERVICE_CLASSIFICATION\", \"URL\", \"NEWS_SOURCE\", \"EVENT_ORGANIZER\", \"EVENT_TYPE\", \"EVENT_START_DATE\", \"EVENT_END_DATE\", \"EVENT_LOCATION\", \"MOBILE_APPS_PROVIDER\") VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            logger.info("Query : " + query);
            connection.setAutoCommit(true);
            preparedStatement = connection
                    .prepareStatement(query);
            preparedStatement.setLong(1,
                    Long.parseLong(dcrId));

            preparedStatement.setString(2, dataBaseStatusIngest.getContentCategory(taskFile));
            preparedStatement.setString(3, getDCRValue(document, TITLE_PATH));
            preparedStatement.setString(4, dataBaseStatusIngest.getLang(taskFile));
            preparedStatement.setString(5, dataBaseStatusIngest.getTaskFilePath(taskFile));
            preparedStatement.setString(6, getDCRValue(document, TOPICS));
            preparedStatement.setString(7, getDCRValue(document, CATEGORY));
            preparedStatement.setString(8, getDCRValue(document, PERSONA_PATH));
            preparedStatement.setString(9, getDCRValue(document, SERVICE_ENTITIES));
            preparedStatement.setString(10, taskFile.getLastModifier().getNormalizedName() );
            preparedStatement.setTimestamp(11, getCreationDate(taskFile));
            preparedStatement.setTimestamp(12, publishDate);
            preparedStatement.setTimestamp(13, getModificationDate(taskFile));
            preparedStatement.setLong(14, version);
            preparedStatement.setString(15,fileStatus);
            preparedStatement.setString(16, getDCRValue(document, SERVICE_TYPE));
            preparedStatement.setString(17, getDCRValue(document, SERVICE_MODE));
            preparedStatement.setString(18, getDCRValue(document, SERVICE_CLASSIFICATION));
            preparedStatement.setString(19, getCategoryBasedUrl(taskFile, document));
            preparedStatement.setString(20, getDCRValue(document, NEWS_SOURCE));
            preparedStatement.setString(21, getDCRValue(document, ORGANIZER));
            preparedStatement.setString(22, getDCRValue(document, TYPES));
            preparedStatement.setTimestamp(23, getSqlDate(getDCRValue(document, EVENT_START_DATE_PATH)));
            preparedStatement.setTimestamp(24, getSqlDate(getDCRValue(document, EVENT_END_DATE_PATH)));
            preparedStatement.setString(25, getDCRValue(document, EVENT_LOCATION));
            preparedStatement.setString(26, getDCRValue(document, MOBILE_APPS_PROVIDER));
            logger.info("insertData prepared Statement : " + preparedStatement);
            result = preparedStatement.executeUpdate();
            logger.info("insertData result : " + result);
            if (result > 0) {
                isDataInserted = true;
            }
        } catch (Exception e) {
            logger.error("Exception in insert Data: ", e);
        } finally {
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
     */
    public boolean insertTaxonomyData(CSFile taskSimpleFile, Document document, PostgreTSConnection postgreConnection) {
        boolean isDataInserted = false;
        String taxonomyType;
        String taxonomyKey;
        String labelEn;
        String labelAr;
        String taxonomyDcr;
        Date modifyDate;
        String modifiedDateVal;
        String valueNode = "Value";
        String keyLabelNode = "master/master-data/Key-Label";
        String labelENNode = "LabelEn";
        String labelARNode = "LabelAr";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE MMM dd yyyy HH:mm:ss");
        DataBaseStatusIngest dataBaseStatusIngest = new DataBaseStatusIngest();
        try{
            modifyDate = taskSimpleFile.getContentModificationDate();

            modifiedDateVal = simpleDateFormat.format(modifyDate);
            logger.debug("modifyDate : "+ modifyDate);
            logger.debug("modifiedDateVal : "+ modifiedDateVal);

            taxonomyDcr = taskSimpleFile.getName();
            logger.info("Taxonomy DCR Name : "+taxonomyDcr);

            taxonomyType = dataBaseStatusIngest.getContentCategory(taskSimpleFile);
            logger.info("Taxonomy Content Type : "+taxonomyType);

            taxonomyType = taxonomyType.replaceAll("Taxonomy/","");
            logger.info("Taxonomy Type : "+taxonomyType);

            if(!document.selectNodes(keyLabelNode).isEmpty()) {
                logger.info("Taxonomy Master Key-Label ");
                List<Node> labels = document.selectNodes(keyLabelNode);
                for (Node label : labels) {

                    taxonomyKey = label.selectSingleNode(valueNode).getText();
                    logger.info("Taxonomy Key : " + taxonomyKey);

                    labelEn = label.selectSingleNode(labelENNode).getText();
                    logger.info("Taxonomy English Label : " + labelEn);

                    labelAr = label.selectSingleNode(labelARNode).getText();
                    logger.info("Taxonomy Arabic Label : " + labelAr);

                    isDataInserted = taxonomyQuery("en", taxonomyType, taxonomyKey, labelEn, taxonomyDcr,postgreConnection);
                    logger.info("English Taxonomy Inserted / Updated : " + isDataInserted);

                    isDataInserted = taxonomyQuery("ar", taxonomyType, taxonomyKey, labelAr, taxonomyDcr,postgreConnection);
                    logger.info("Arabic Taxonomy Inserted / Updated : " + isDataInserted);

                }
            }else if (!document.selectNodes("master/master-data/Category").isEmpty()){
                logger.info("Taxonomy Master Category ");
                List<Node> categories = document.selectNodes("master/master-data/Category");
                for (Node category : categories) {

                    String catLabelEn;
                    String catLabelAr;
                    String catKey;

                    catLabelEn = category.selectSingleNode(labelENNode).getText();
                    logger.info("Taxonomy category English Label : " + catLabelEn);

                    catLabelAr = category.selectSingleNode(labelARNode).getText();
                    logger.info("Taxonomy category Arabic Label : " + catLabelAr);

                    catKey = category.selectSingleNode(valueNode).getText();
                    logger.info("Taxonomy category Key : " + catKey);

                    List<Node> subCategories = category.selectNodes("SubCategory");
                    for (Node subCategory : subCategories) {

                        String subCatLabelEn;
                        String subCatLabelAr;
                        String subCatKey;

                        subCatLabelEn = subCategory.selectSingleNode("LabelEn").getText();
                        logger.info("Taxonomy subCategory English Label : " + subCatLabelEn);

                        subCatLabelAr = subCategory.selectSingleNode("LabelAr").getText();
                        logger.info("Taxonomy subCategory Arabic Label : " + subCatLabelAr);

                        subCatKey = subCategory.selectSingleNode(valueNode).getText();
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

        } catch(Exception ex){
            logger.error("Error while logging taxonomy data",ex);
        }

        return isDataInserted;
    }


    public boolean taxonomyQuery(String lang, String type,String key,String label,String dcr,PostgreTSConnection postgreConnection ){
        logger.debug("DataBaseContentIngest : insertTaxonomyData");
        Connection connection = null;
        boolean isDataSelected = false;
        boolean isDataInserted;
        ResultSet result = null;
        PreparedStatement preparedStatement = null;
        try {
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
            result = preparedStatement.executeQuery();

            logger.info("selectQuery result : " + result.toString());
            while (result.next()) {
                int count = result.getInt(1);
                logger.info("insertData result count: " + count);
                if(count > 0) isDataSelected = true;
            }


        } catch (Exception e) {
            logger.error("Exception in insertData: ", e);
        } finally {
            postgre.releaseConnection(connection, preparedStatement, result);
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

            int result;
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
        } catch (Exception e) {
            logger.error("Exception in update Taxonomy Data: ", e);
        } finally {
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

            int result;
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
        } catch (Exception e) {
            logger.error("Exception in updateData: ", e);
        } finally {
            postgre.releaseConnection(connection, preparedStatement, null);
            logger.info("Released insertQuery connection");
        }
        return isDataInserted;
    }

    /**
     * Method updates the content
     *
     * @param taskSimpleFile    file path
     * @param taskObj content
     * @return Returns number of rows affected by query execution
     */
    public boolean updateWorkflowData(CSFile taskSimpleFile, CSExternalTask taskObj) {
        logger.debug("DataBaseContentIngest : updateWorkflowData");
        Connection connection = null;
        boolean isDataUpdated = false;
        PreparedStatement preparedStatement = null;

        String reviewDate;
        String approveDate;
        String[] dateFormats =  {"EEE MMM dd yyyy HH:mm:ss","EEE MMM dd HH:mm:ss yyyy","EEE MMM dd HH:mm:ss zzz yyyy"};
        String jobStatus = "PUBLISHED";
        String fileStatus  = "";
        String commentStr;
        String entityVal = "";

        try {
            if(taskSimpleFile.getKind()!=CSHole.KIND){
                CSSimpleFile taskFile = (CSSimpleFile) taskSimpleFile;
                entityVal = taskFile.getExtendedAttribute(META_ENTITY).getValue();
                if(entityVal == null)
                    entityVal = "";
            }
            reviewDate = taskObj.getWorkflow().getVariable(META_REVIEW_DATE);
            logger.info("Review Date : " + reviewDate );

            Timestamp reviewedDate = null;
            if(reviewDate != null)
                reviewedDate = new Timestamp(DateUtils.parseDate(reviewDate,dateFormats).getTime());
            logger.info("reviewedDate : " + reviewedDate );


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
                commentsToLogInDB.append("[").append(comment.getCreationDate()).append("] ").append(comment.getCreator()).append(": ").append(comment.getComment()).append(System.lineSeparator());
            }
            commentStr = commentsToLogInDB.toString();

            connection = postgre.getConnection();
            long millis = System.currentTimeMillis();
            Timestamp publishDate = new Timestamp(millis);
            logger.info("Publish Date : "+publishDate.toString());

            int result;
            String updateQuery = "UPDATE WORKFLOW_TABLE SET \"WORKFLOW_END_DATE\" = ?, \"REVIEW_DATE\" = ?, \"APPROVAL_DATE\" = ?, \"COUNT_REJECT_REVIEW\" = ?, \"COUNT_REJECT_APPROVE\" = ?, \"PUBLISH_DATE\" = ?, \"WORKFLOW_STATUS\" = ?, \"FILE_STATUS\" = ?, \"WORKFLOW_COMMENTS\" = ?, \"ENTITY\" = ? WHERE \"WORKFLOW_ID\" = ? AND \"CONTENT_PATH\" = ?";
            logger.info("updateQuery : " + updateQuery);

            connection.setAutoCommit(true);
            preparedStatement = connection.prepareStatement(updateQuery);
            preparedStatement.setTimestamp(1, publishDate);
            preparedStatement.setTimestamp(2, reviewedDate);
            preparedStatement.setTimestamp(3, approvalDate);
            preparedStatement.setLong(4,getRejectCount(taskObj.getWorkflow().getVariable(META_REVIEW_REJECT_DATES)));
            preparedStatement.setLong(5,getRejectCount(taskObj.getWorkflow().getVariable(META_APPROVE_REJECT_DATES)));
            preparedStatement.setTimestamp(6, publishDate);
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
            }
        } catch (Exception e) {
            logger.error("Exception in updateData: ", e);
        } finally {
            postgre.releaseConnection(connection, preparedStatement, null);
            logger.info("Released updateData connection");
        }
        return isDataUpdated;
    }

    public int getRejectCount(String rejectDateString){
        int rejectCount;
        rejectDateString = rejectDateString != null ? rejectDateString : "";
        logger.info(" Reject Dates : " + rejectDateString );

        rejectCount = rejectDateString.replaceAll("\\s+","").length() / 20;
        logger.info(" Reject Count : " + rejectCount );
        return rejectCount;
    }

    /**
     * Method to get the modifier comment for each task file.
     *
     * @param task Task file of CSSimpleFile object
     */
    public void deleteMetaData(CSExternalTask task) {
        try {
            task.getWorkflow().deleteVariable(META_REVIEW_DATE);
            task.getWorkflow().deleteVariable(META_APPROVE_DATE);
            task.getWorkflow().deleteVariable(META_REVIEW_REJECT_DATES);
            task.getWorkflow().deleteVariable(META_APPROVE_REJECT_DATES);
            logger.debug("Removed custom metadata from task");
        } catch (Exception e) {
            logger.error("Exception in deleteMetaData: ", e);
        }
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
          if (inputDate != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            Date date = sdf.parse(inputDate);
            sqlDate = new Timestamp(date.getTime());
            logger.info(inputDate + " >>> " + inputDate);
          } 
        } catch (Exception e) {
          logger.error("Exception in insertData: ", e);
        } 
        return sqlDate;
      }

}
