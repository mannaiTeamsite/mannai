package com.hukoomi.task;

import com.hukoomi.utils.PostgreTSConnection;
import com.interwoven.cssdk.access.CSUser;
import com.interwoven.cssdk.common.CSClient;
import com.interwoven.cssdk.common.CSException;
import com.interwoven.cssdk.filesys.CSAreaRelativePath;
import com.interwoven.cssdk.filesys.CSExtendedAttribute;
import com.interwoven.cssdk.filesys.CSHole;
import com.interwoven.cssdk.filesys.CSSimpleFile;
import com.interwoven.cssdk.workflow.CSComment;
import com.interwoven.cssdk.workflow.CSExternalTask;
import com.interwoven.cssdk.workflow.CSURLExternalTask;
import com.interwoven.cssdk.workflow.CSWorkflow;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;

import java.sql.*;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

public class DataBaseStatusIngest implements CSURLExternalTask {
    /**
     * Logger object to check the flow of the code.
     */
    private final Logger logger = Logger.getLogger(DataBaseStatusIngest.class);
    /**
     * Database property file name
     */
    private static final String DB_PROPERTY_FILE = "DbInsertConfig.properties";
    /**
     * DCR Review date
     */
    public static final String META_REVIEW_DATE = "TeamSite/Metadata/ReviewTime";
    /**
     * DCR Approve date
     */
    public static final String META_APPROVE_DATE = "TeamSite/Metadata/ApproveTime";
    /**
     * DCR Type Meta data name
     */
    public static final String META_DATA_NAME_DCR_TYPE = "TeamSite/Templating/DCR/Type";
    /**
     * DCR review reject dates
     */
    public static final String META_REVIEW_REJECT_DATES = "TeamSite/Metadata/reviewRejectDates";
    /**
     * DCR Approve reject dates
     */
    public static final String META_APPROVE_REJECT_DATES = "TeamSite/Metadata/approveRejectDates";
    /**
     * DCR Service Entity
     */
    public static final String META_ENTITY = "TeamSite/Metadata/Entity";
    /**
     * Last modifier name for page
     */
    public static final String META_LAST_MODIFIER = "TeamSite/Metadata/LastModifier";
    /**
     * Transition hashmap key
     */
    private static final String TRANSITION = "TRANSITION";
    /**
     * Transition comment hashmap key
     */
    private static final String TRANSITION_COMMENT = "TRANSITION_COMMENT";
    /**
     * Success transition message
     */
    public static final String SUCCESS_TRANSITION = "Review Pending DB Success";
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
        logger.info("Data Status Ingest Task - execute");
        HashMap<String, String> statusMap = null;
        CSAreaRelativePath[] taskFileList = task.getFiles();
        logger.debug("TaskFileList Length : " + taskFileList.length);
        String taskName = task.getName();
        logger.debug("Task Name : "+taskName);
        postgre = new PostgreTSConnection(client, task, DB_PROPERTY_FILE);
        statusMap = new HashMap<>();
        statusMap.put(TRANSITION, taskName+" Success");
        statusMap.put(TRANSITION_COMMENT, "");

        for (CSAreaRelativePath taskFile : taskFileList) {
            try {
                CSSimpleFile taskSimpleFile = (CSSimpleFile) task.getArea().getFile(taskFile);
                logger.debug("File Name : " + taskSimpleFile.getName());

                statusMap = (HashMap<String, String>) process(taskSimpleFile, task, taskName);
            } catch (Exception e) {
                logger.error("Exception in execute: ", e);
            }
        }
        logger.debug("transition : " + statusMap.get(TRANSITION));
        logger.debug("transitionComment : "+ statusMap.get(TRANSITION_COMMENT));
        task.chooseTransition(statusMap.get(TRANSITION),statusMap.get(TRANSITION_COMMENT));
    }

        /**
         * Method process the dcr from the workflow task and insert db
         * data
         *
         * @param taskSimpleFile Task file of CSSimpleFile object
         * @return Returns map contains the transition status and transition comment.
         */
        public Map<String, String> process(CSSimpleFile taskSimpleFile, CSExternalTask task, String tName) {
            boolean isDBInsertionAlreadyDone = false;
            boolean isDBOperationWorkflowSuccess = false;
            boolean isDBUpdationSuccess = false;
            HashMap<String, String> statusMap = new HashMap<>();
            try {
                isDBInsertionAlreadyDone = workflowQuery(taskSimpleFile, task);
                if(isDBInsertionAlreadyDone){
                    isDBUpdationSuccess = updateWorkflowData(taskSimpleFile, task);
                }else {
                    isDBOperationWorkflowSuccess = insertWorkFlowData(taskSimpleFile, task);
                }
                logger.debug("DBOperationWorkflow : " + isDBOperationWorkflowSuccess);
                if (isDBOperationWorkflowSuccess || isDBUpdationSuccess){
                    statusMap.put(TRANSITION, tName+" Success");
                    statusMap.put(TRANSITION_COMMENT, DATA_INSERT_SUCCESS);
                } else {
                    statusMap.put(TRANSITION, tName+" Failure");
                    statusMap.put(TRANSITION_COMMENT, DATA_INSERT_FAILURE);
                }
            } catch (Exception e) {
                statusMap.put(TRANSITION, tName+" Failure");
                logger.error("Exception : ", e);
            }
            return statusMap;
        }

        /**
         * Method checks the content in DB
         *
         * @param taskSimpleFile    file path
         * @param taskObj content
         * @return Returns number of rows affected by query execution
         * @throws SQLException
         */
        public boolean workflowQuery(CSSimpleFile taskSimpleFile, CSExternalTask taskObj) throws SQLException, CSException{
            logger.debug("DataBaseStatusIngest : workflowQueryData");
            Connection connection = null;
            boolean isDataSelected = false;
            ResultSet result = null;
            PreparedStatement preparedStatement = null;
            try {
                connection = postgre.getConnection();
                String selectQuery = "SELECT COUNT(*) FROM WORKFLOW_TABLE WHERE \"WORKFLOW_ID\" = ? AND \"CONTENT_PATH\" = ?";
                logger.info("selectQuery : " + selectQuery);
                connection.setAutoCommit(true);
                preparedStatement = connection.prepareStatement(selectQuery);

                preparedStatement.setLong(1,taskObj.getWorkflowId());
                preparedStatement.setString(2,getTaskFilePath(taskSimpleFile));

                logger.info("insertData preparedStatement : " + preparedStatement);
                // result = preparedStatement.executeUpdate();
                result = preparedStatement.executeQuery();

                logger.info("selectQuery result : " + result.toString());
                while (result.next()) {
                    int count = result.getInt(1);
                    logger.info("insertedData result count: " + count);
                    if(count > 0) isDataSelected = true;
                }

            } catch (SQLException e) {
                logger.error("SQL Exception in workflowQuery: ", e);
            } catch (Exception e) {
                logger.error("Exception in workflowQuery: ", e);
            }finally {
                postgre.releaseConnection(connection, preparedStatement, null);
                logger.info("Released taxonomyQuery connection from workflowQuery");
            }
            return isDataSelected;
        }

    /**
     * Method updates the content
     *
     * @param taskSimpleFile    file path
     * @param taskObj content
     * @return Returns number of rows affected by query execution
     * @throws SQLException
     */
        public boolean updateWorkflowData(CSSimpleFile taskSimpleFile, CSExternalTask taskObj) throws SQLException, CSException{
            logger.debug("DataBaseStatusIngest : updateWorkflowData");
            Connection connection = null;
            boolean isDataUpdated = false;
            PreparedStatement preparedStatement = null;

            String taskName = null;
            String modifier = null;
            String lastModifiedBy = null;
            String author = null;
            String commentOnModifier = "";
            String reviewer = "" ;
            String approver = "" ;
            String reviewDate = "";
            String approveDate = "";
            String[] dateFormats =  {"EEE MMM dd yyyy HH:mm:ss","EEE MMM dd HH:mm:ss yyyy"};
            String jobStatus = taskObj.getName().split("DB")[0].trim().toUpperCase();
            String fileStatus  = "";
            String commentStr = "";
            String entityVal = taskSimpleFile.getExtendedAttribute(META_ENTITY).getValue();
            if(entityVal == null)
                entityVal = "";

            try {
                
                taskName = taskObj.getName();
                logger.info("Task Name : " + taskName);
                
                author = taskSimpleFile.getCreator().getName();
                logger.info("Content author : " + author);
                
                modifier = taskSimpleFile.getExtendedAttribute(META_LAST_MODIFIER).getValue();
                logger.info("Content modifier META: " + modifier);
                
                lastModifiedBy = taskSimpleFile.getLastModifier().getName();
                logger.info("Content lastModifiedBy File: " + modifier);
                
                reviewer = taskObj.getWorkflow().getVariable("WF_Reviewer");
                logger.info("Workflow Reviewer : " + reviewer );

                approver = taskObj.getWorkflow().getVariable("WF_Approver");
                logger.info("Workflow Approver : " + approver );

                reviewDate = taskSimpleFile.getExtendedAttribute(META_REVIEW_DATE).getValue();
                logger.info("Review Date : " + reviewDate );

                Timestamp reviewedDate = null;
                if(reviewDate != null)
                    reviewedDate = new Timestamp(DateUtils.parseDate(reviewDate,dateFormats).getTime());
                logger.info("reviewedDate : " + reviewedDate );


                approveDate = taskSimpleFile.getExtendedAttribute(META_APPROVE_DATE).getValue();
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
                
                if((getContentCategory(taskSimpleFile).equals("sites/portal-en") 
                        || getContentCategory(taskSimpleFile).equals("sites/portal-ar")) ) {
                        //&& getTaskFilePath(taskSimpleFile).endsWith(".page")) {
                    if(modifier != null && !"".equals(modifier) 
                            && taskName != null && "Approval Pending DB".equals(taskName) 
                            && modifier.equals(reviewer)) {
                        commentOnModifier = "Updated content on behalf of "+author + System.lineSeparator();
                    }
                }

                CSWorkflow workflow = taskObj.getWorkflow();
                CSComment[] comments = workflow.getComments();
                StringBuilder commentsToLogInDB = new StringBuilder();
                for(CSComment comment : comments) {
                    commentsToLogInDB.append("["+ comment.getCreationDate() +"] "+ comment.getCreator() + ": "+ comment.getComment() + System.lineSeparator());
                }
                
                if(!"".equals(commentOnModifier)) {
                    commentsToLogInDB.append("[").append(DateUtils.parseDate(reviewDate, dateFormats)).append("] ").append("tsadmin").append(": ").append(commentOnModifier);
                }
                
                commentStr = commentsToLogInDB.toString();

                connection = postgre.getConnection();
                int result = 0;
                String updateQuery = "UPDATE WORKFLOW_TABLE SET \"REVIEW_DATE\" = ?, \"APPROVAL_DATE\" = ?, \"COUNT_REJECT_REVIEW\" = ?, \"COUNT_REJECT_APPROVE\" = ?, \"WORKFLOW_STATUS\" = ?, \"FILE_STATUS\" = ?, \"WORKFLOW_COMMENTS\" = ?, \"ENTITY\" = ? WHERE \"WORKFLOW_ID\" = ? AND \"CONTENT_PATH\" = ?";
                logger.info("updateQuery : " + updateQuery);

                connection.setAutoCommit(true);
                preparedStatement = connection.prepareStatement(updateQuery);
                preparedStatement.setTimestamp(1, reviewedDate);
                preparedStatement.setTimestamp(2, approvalDate);
                preparedStatement.setLong(3,getRejectCount(taskSimpleFile.getExtendedAttribute(META_REVIEW_REJECT_DATES).getValue() ));
                preparedStatement.setLong(4,getRejectCount(taskSimpleFile.getExtendedAttribute(META_APPROVE_REJECT_DATES).getValue() ));
                preparedStatement.setString(5,jobStatus);
                preparedStatement.setString(6,fileStatus);
                preparedStatement.setString(7,commentStr);
                preparedStatement.setString(8,entityVal);
                preparedStatement.setLong(9,taskObj.getWorkflowId());
                preparedStatement.setString(10,getTaskFilePath(taskSimpleFile));

                logger.info("updateData preparedStatement : " + preparedStatement);
                result = preparedStatement.executeUpdate();
                logger.info("updateQuery result : " + result);
                if (result > 0) {
                    isDataUpdated = true;
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


    /**
     * Method inserts the content
     *
     * @param taskSimpleFile    file path
     * @param task content
     * @return Returns number of rows affected by query execution
     * @throws SQLException
     */
    public boolean insertWorkFlowData(CSSimpleFile taskSimpleFile, CSExternalTask task) throws SQLException, CSException {
        logger.debug("DataBaseStatusIngest : insertWorkFlowData");
        Connection connection = null;
        boolean isDataInserted = false;

        PreparedStatement preparedStatement = null;
        try {
            logger.info("Workflow Details : "+task.getWorkflow().toString());
            
            String taskName = "";
            String author = "";
            String reviewer = "" ;
            String approver = "" ;
            String reviewDate = "";
            String approveDate = "";
            String[] dateFormats =  {"EEE MMM dd yyyy HH:mm:ss","EEE MMM dd HH:mm:ss yyyy"};
            String jobStatus = task.getName().split("DB")[0].trim().toUpperCase();
            String fileStatus  = "";
            String commentStr = "";            
            String entityVal = taskSimpleFile.getExtendedAttribute(META_ENTITY).getValue();
            if(entityVal == null)
                entityVal = "";
            
            taskName = task.getName();
            logger.info("Task Name : " + taskName);
            
            author = taskSimpleFile.getCreator().getName();
            logger.info("Content author : " + author);
            
            if((getContentCategory(taskSimpleFile).equals("sites/portal-en") 
                    || getContentCategory(taskSimpleFile).equals("sites/portal-ar")) 
                    && getTaskFilePath(taskSimpleFile).endsWith(".page") 
                    && taskName != null && "Review Pending DB".equals(taskName)) {
                CSExtendedAttribute[] csEAArray = new CSExtendedAttribute[1];
                csEAArray[0] = new CSExtendedAttribute(META_LAST_MODIFIER, author);
                taskSimpleFile.setExtendedAttributes(csEAArray);
            }

            reviewDate = taskSimpleFile.getExtendedAttribute(META_REVIEW_DATE).getValue();
            logger.info("Review Date : " + reviewDate );
            Timestamp reviewedDate = null;
            if(reviewDate != null)
                reviewedDate = new Timestamp(DateUtils.parseDate(reviewDate,dateFormats).getTime());
            logger.info("reviewedDate : " + reviewedDate );

            approveDate = taskSimpleFile.getExtendedAttribute(META_APPROVE_DATE).getValue();
            logger.info("Approve Date : " + approveDate );
            Timestamp approvalDate = null;
            if(approveDate != null)
                    approvalDate = new Timestamp(DateUtils.parseDate(approveDate,dateFormats).getTime());
            logger.info("approvalDate : " + approvalDate );
            
            reviewer = task.getWorkflow().getVariable("WF_Reviewer");
            logger.info("Workflow Reviewer : " + reviewer );

            approver = task.getWorkflow().getVariable("WF_Approver");
            logger.info("Workflow Approver : " + approver );

            logger.info("task time  : "+ taskSimpleFile.getExtendedAttribute("timeStamp"));
            logger.info("task time local : "+ taskSimpleFile.getExtendedAttribute("localTimeStamp"));
            logger.info("task output stream : "+ taskSimpleFile.getOutputStream(true).toString());

            if(taskSimpleFile.getKind() == CSHole.KIND){
                fileStatus = "DELETE";
            }else if(taskSimpleFile.getRevisionNumber() == 0){
                fileStatus = "ADD";
            }else if(taskSimpleFile.getRevisionNumber() > 0){
                fileStatus = "MODIFY";
            }
            
            CSWorkflow workflow = task.getWorkflow();
            CSComment[] comments = workflow.getComments();
            StringBuilder commentsToLogInDB = new StringBuilder();
            for(CSComment comment : comments) {
                commentsToLogInDB.append("["+ comment.getCreationDate() +"] "+ comment.getCreator() + ": "+ comment.getComment() + System.lineSeparator());
            }
            commentStr = commentsToLogInDB.toString();

            connection = postgre.getConnection();
            logger.info("DB connection : "+connection.toString());
            /* long millis = System.currentTimeMillis();
            Timestamp PublishDate = new Timestamp(millis);
            logger.info("Publish Date : "+PublishDate.toString()); */
            int result = 0;
            logger.info("DataBaseStatusIngest : insertWorkFlowData");
            String query = "INSERT INTO WORKFLOW_TABLE(\"WORKFLOW_ID\", \"WORKFLOW_USER\",\"CATEGORY\", \"CONTENT_PATH\", \"LANG\", \"WORKFLOW_START_DATE\", \"WORKFLOW_REVIEWER\", \"REVIEW_DATE\", \"WORKFLOW_APPROVER\", \"APPROVAL_DATE\", \"COUNT_REJECT_REVIEW\", \"COUNT_REJECT_APPROVE\", \"WORKFLOW_STATUS\", \"FILE_STATUS\", \"WORKFLOW_COMMENTS\", \"ENTITY\") VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            logger.info("Query : " + query);
            connection.setAutoCommit(true);
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setLong(1, task.getWorkflowId());
            preparedStatement.setString(2, task.getWorkflow().getOwner().getNormalizedName());
            preparedStatement.setString(3, getContentCategory(taskSimpleFile));
            preparedStatement.setString(4, getTaskFilePath(taskSimpleFile));
            preparedStatement.setString(5, getLang(taskSimpleFile));
            preparedStatement.setTimestamp(6, new Timestamp(task.getWorkflow().getActivationDate().getTime()));
            //preparedStatement.setTimestamp(7, PublishDate);
            preparedStatement.setString(7, reviewer);
            preparedStatement.setTimestamp(8, reviewedDate);
            preparedStatement.setString(9, approver);
            preparedStatement.setTimestamp(10, approvalDate);
            preparedStatement.setLong(11,getRejectCount(taskSimpleFile.getExtendedAttribute(META_REVIEW_REJECT_DATES).getValue() ));
            preparedStatement.setLong(12,getRejectCount(taskSimpleFile.getExtendedAttribute(META_APPROVE_REJECT_DATES).getValue() ));
            //preparedStatement.setTimestamp(14, PublishDate);
            preparedStatement.setString(13,jobStatus);
            preparedStatement.setString(14,fileStatus);
            preparedStatement.setString(15,commentStr);
            preparedStatement.setString(16,entityVal);
            logger.info("insertData preparedStatement : " + preparedStatement);
            result = preparedStatement.executeUpdate();
            logger.info("insertData result : " + result);
            if (result > 0) {
                isDataInserted = true;
            }
        } catch (NumberFormatException | SQLException e) {
            logger.error(" NumberFormatException/SQLException Exception in insertData: ", e);
            throw e;
        } catch (Exception e) {
            logger.error("Exception in insertData: ", e);
        } finally {
            postgre.releaseConnection(connection, preparedStatement, null);
            logger.info("Released insertData connection");
        }
        return isDataInserted;
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
            logger.debug("fileLocation : " + fileLocation);

            if(null != taskSimpleFile.getExtendedAttribute(META_DATA_NAME_DCR_TYPE).getValue()) {
                category = taskSimpleFile.getExtendedAttribute(META_DATA_NAME_DCR_TYPE).getValue();
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
     * Method to get the task file path.
     *
     * @param taskSimpleFile Task file of CSSimpleFile object
     * @return Returns path of file.
     */
    public String getTaskFilePath(CSSimpleFile taskSimpleFile) {
        String taskSimpleFileString = "";
        try {
            taskSimpleFileString = taskSimpleFile.getVPath().toString();
            logger.debug("taskSimpleFileString : " + taskSimpleFileString);
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
            logger.debug("fileLocation : " + fileLocation);
            if(StringUtils.contains(fileLocation,"/data/") && StringUtils.contains(fileLocation,"Content")) {
                lang = fileLocation.substring(fileLocation.indexOf("/data/") + 6, fileLocation.lastIndexOf("/"));
            }
            logger.debug("lang : " + lang);
        } catch (Exception e) {
            logger.error("Exception in getLang: ", e);
        }
        return lang;
    }

    public int getRejectCount(String rejectDateString){
        int rejectCount = 0;
        rejectDateString = rejectDateString != null ? rejectDateString : "";
        logger.info(" Reject Dates : " + rejectDateString );

        rejectCount = rejectDateString.replaceAll("\\s+","").length() / 20;
        logger.info(" Reject Count : " + rejectCount );
        return rejectCount;
    }

}
