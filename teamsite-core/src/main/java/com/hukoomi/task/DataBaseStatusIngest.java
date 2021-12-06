package com.hukoomi.task;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.StringJoiner;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;

import com.hukoomi.utils.PostgreTSConnection;
import com.interwoven.cssdk.common.CSClient;
import com.interwoven.cssdk.common.CSException;
import com.interwoven.cssdk.filesys.CSAreaRelativePath;
import com.interwoven.cssdk.filesys.CSFile;
import com.interwoven.cssdk.filesys.CSHole;
import com.interwoven.cssdk.filesys.CSSimpleFile;
import com.interwoven.cssdk.workflow.CSComment;
import com.interwoven.cssdk.workflow.CSExternalTask;
import com.interwoven.cssdk.workflow.CSURLExternalTask;
import com.interwoven.cssdk.workflow.CSWorkflow;

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
     * Poll insert transition success message
     */
    private static final String DATA_INSERT_SUCCESS = "Data inserted successfully";
    /**
     * Poll insert transition failure message
     */
    private static final String DATA_INSERT_FAILURE = "Failed to insert data";
    /**
     * Review Pending DB task name
     */
    private static final String TASK_REVIEW_PENDING_DB = "Review Pending DB";
    /**
     * Approve Pending DB task name
     */
    private static final String TASK_APPROVE_PENDING_DB = "Approval Pending DB";
    /**
     * Date Time format
     */
    private static final String DATE_TIME_FORMAT = "EEE MMM dd yyyy HH:mm:ss";
    /**
     * Review Reject DB task name
     */
    private static final String TASK_REVIEW_REJECT_DB = "Review Reject DB";
    /**
     * Approval Reject DB task name
     */
    private static final String TASK_APPROVAL_REJECT_DB = "Approval Reject DB";
    /*
     * Success identifier
     */
    private static final String SUCCESS = " Success";
    /**
     * Comment date format
     */
    private static final String COMMENT_DATE_FORMAT = "EEE MMM dd HH:mm:ss z yyyy";
    /**
     * Comment Map holds comments for each file
     */
    HashMap<String, String>  commentsMap  = null;
    /**
     * Last modifier Map holds last modifier information for each file
     */
    HashMap<String, String> lastModifierMap = null;
    /**
     * PostgreTSConnection object reference
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
        logger.info("Data Status Ingest Task - execute");
        HashMap<String, String> statusMap;
        CSAreaRelativePath[] taskFileList = task.getFiles();
        logger.debug("TaskFileList Length : " + taskFileList.length);
        String taskName = task.getName();
        logger.debug("Task Name : "+taskName);
        postgre = new PostgreTSConnection(client, task, DB_PROPERTY_FILE);
        statusMap = new HashMap<>();
        statusMap.put(TRANSITION, taskName+SUCCESS);
        statusMap.put(TRANSITION_COMMENT, "");
        
        commentsMap  = new HashMap<>();
        lastModifierMap = new HashMap<>();

        processWorkFlowInputs(task, taskFileList);
        for (CSAreaRelativePath taskFilePath : taskFileList) {
            try {
                CSFile taskFile = task.getArea().getFile(taskFilePath);
                logger.debug("File Name : " + taskFile.getName());

                statusMap = (HashMap<String, String>) process(taskFile, task, taskName);
            } catch (Exception e) {
                logger.error("Exception in execute: ", e);
            }
        }
        task.getWorkflow().setVariable(META_LAST_MODIFIER, getLastModifierString());
        
        logger.debug("transition : " + statusMap.get(TRANSITION));
        logger.debug("transitionComment : "+ statusMap.get(TRANSITION_COMMENT));
        task.chooseTransition(statusMap.get(TRANSITION),statusMap.get(TRANSITION_COMMENT));
    }

        /**
         * Method process the dcr from the workflow task and insert db
         * data
         *
         * @param taskFile Task file of CSSimpleFile object
         * @return Returns map contains the transition status and transition comment.
         */
        public Map<String, String> process(CSFile taskFile, CSExternalTask task, String tName) {
            boolean isDBInsertionAlreadyDone;
            boolean isDBOperationWorkflowSuccess = false;
            boolean isDBUpdationSuccess = false;
            HashMap<String, String> statusMap = new HashMap<>();
            try {
                isDBInsertionAlreadyDone = workflowQuery(taskFile, task);
                if(isDBInsertionAlreadyDone){
                    isDBUpdationSuccess = updateWorkflowData(taskFile, task);
                }else {
                    isDBOperationWorkflowSuccess = insertWorkFlowData(taskFile, task);
                }
                logger.debug("DBOperationWorkflow : " + isDBOperationWorkflowSuccess);
                if (isDBUpdationSuccess){
                    statusMap.put(TRANSITION, tName+SUCCESS);
                    String commentOnModifier = commentsMap.get(getMapKey(taskFile));
                    if(StringUtils.isNotBlank(commentOnModifier)) {
                        statusMap.put(TRANSITION_COMMENT, commentOnModifier+" "+DATA_INSERT_SUCCESS);
                    }else {
                        statusMap.put(TRANSITION_COMMENT, DATA_INSERT_SUCCESS);
                    }
                }else if (isDBOperationWorkflowSuccess){
                    statusMap.put(TRANSITION, tName+SUCCESS);
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
         * @param taskFile    file path
         * @param taskObj content
         * @return Returns number of rows affected by query execution
         */
        public boolean workflowQuery(CSFile taskFile, CSExternalTask taskObj) {
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
                preparedStatement.setString(2,getTaskFilePath(taskFile));

                logger.info("insertData preparedStatement : " + preparedStatement);
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
                postgre.releaseConnection(connection, preparedStatement, result);
                logger.info("Released taxonomyQuery connection from workflowQuery");
            }
            return isDataSelected;
        }

    /**
     * Method updates the content
     *
     * @param taskFile    file path
     * @param taskObj content
     * @return Returns number of rows affected by query execution
     */
        public boolean updateWorkflowData(CSFile taskFile, CSExternalTask taskObj){
            logger.debug("DataBaseStatusIngest : updateWorkflowData");
            Connection connection = null;
            boolean isDataUpdated = false;
            PreparedStatement preparedStatement = null;

            String taskName;
            String modifierMetaData;
            String modifier;
            String reviewer;
            String approver;
            String reviewDate;
            String approveDate;
            String[] dateFormats =  {DATE_TIME_FORMAT,"EEE MMM dd HH:mm:ss yyyy"};
            String fileStatus  = "";
            String commentStr;
            String entityVal = "";
            try {
                if(taskFile.getKind() == CSHole.KIND){
                    fileStatus = "DELETE";
                }else if(taskFile.getRevisionNumber() == 0){
                    fileStatus = "ADD";
                }else if(taskFile.getRevisionNumber() > 0){
                    fileStatus = "MODIFY";
                }
                String jobStatus = taskObj.getName().split("DB")[0].trim().toUpperCase();
                if(taskFile.getKind()!=CSHole.KIND){
                    CSSimpleFile taskSimpleFile = (CSSimpleFile) taskFile;
                    entityVal = taskSimpleFile.getExtendedAttribute(META_ENTITY).getValue();
                }

                taskName = taskObj.getName();
                logger.info("Task Name: " + taskName);
                
                modifier = taskFile.getLastModifier().getName();
                logger.info("Last Modified By : " + modifier);
                
                modifierMetaData = lastModifierMap.get(getMapKey(taskFile));
                logger.info("EA Modifier : " + modifierMetaData);
                
                reviewer = taskObj.getWorkflow().getVariable("WF_Reviewer");
                logger.info("Workflow Reviewer : " + reviewer );

                approver = taskObj.getWorkflow().getVariable("WF_Approver");
                logger.info("Workflow Approver : " + approver );
                
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);
                if(StringUtils.equals(TASK_APPROVE_PENDING_DB, taskName)) {
                    taskObj.getWorkflow().setVariable(META_REVIEW_DATE, LocalDateTime.now().format(formatter));
                    logger.info("Set Review Date as : " + taskObj.getWorkflow().getVariable(META_REVIEW_DATE));
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


                
                String commentOnModifier = commentsMap.get(getMapKey(taskFile));
                logger.info("commentOnModifier : " + commentOnModifier );
                        
                if(StringUtils.isNotBlank(modifier)  
                        && StringUtils.equals(TASK_APPROVE_PENDING_DB, taskName)) {
                    lastModifierMap.put(getMapKey(taskFile), modifier);
                }
                
                CSWorkflow workflow = taskObj.getWorkflow();
                CSComment[] comments = workflow.getComments();
                StringBuilder commentsToLogInDB = new StringBuilder();
                for(CSComment comment : comments) {
                    commentsToLogInDB.append("[").append(comment.getCreationDate()).append("] ").append(comment.getCreator()).append(": ").append(comment.getComment()).append(System.lineSeparator());
                }
                
                if(StringUtils.isNotBlank(commentOnModifier)) {
                    logger.info("reviewDate : " + reviewDate );
                    String strRevDate = formatDateForComment(reviewDate, dateFormats, COMMENT_DATE_FORMAT);
                    commentsToLogInDB.append("[").append(strRevDate).append("] ").append(commentOnModifier).append(System.lineSeparator());
                }
                
                commentStr = commentsToLogInDB.toString();

                connection = postgre.getConnection();
                int result;
                String updateQuery = "UPDATE WORKFLOW_TABLE SET \"REVIEW_DATE\" = ?, \"APPROVAL_DATE\" = ?, \"COUNT_REJECT_REVIEW\" = ?, \"COUNT_REJECT_APPROVE\" = ?, \"WORKFLOW_STATUS\" = ?, \"FILE_STATUS\" = ?, \"WORKFLOW_COMMENTS\" = ?, \"ENTITY\" = ? WHERE \"WORKFLOW_ID\" = ? AND \"CONTENT_PATH\" = ?";
                logger.info("updateQuery : " + updateQuery);

                connection.setAutoCommit(true);
                preparedStatement = connection.prepareStatement(updateQuery);
                preparedStatement.setTimestamp(1, reviewedDate);
                preparedStatement.setTimestamp(2, approvalDate);
                preparedStatement.setLong(3,getRejectCount(taskObj.getWorkflow().getVariable(META_REVIEW_REJECT_DATES)));
                preparedStatement.setLong(4,getRejectCount(taskObj.getWorkflow().getVariable(META_APPROVE_REJECT_DATES)));
                preparedStatement.setString(5,jobStatus);
                preparedStatement.setString(6,fileStatus);
                preparedStatement.setString(7,commentStr);
                preparedStatement.setString(8,entityVal);
                preparedStatement.setLong(9,taskObj.getWorkflowId());
                preparedStatement.setString(10,getTaskFilePath(taskFile));

                logger.info("updateData preparedStatement : " + preparedStatement);
                result = preparedStatement.executeUpdate();
                logger.info("updateQuery result : " + result);
                if (result > 0) {
                    isDataUpdated = true;
                }
            }catch (NumberFormatException | SQLException e) {
                logger.error("NumberFormatException/SQL Exception in updateData: ", e);
            } catch (Exception e) {
                logger.error("Exception in updateData: ", e);
            } finally {
                postgre.releaseConnection(connection, preparedStatement, null);
                logger.info("Released updateData connection");
            }
            return isDataUpdated;
        }

        /**
         * Method formatDateForComment formats the inputDateStr to the output dateformat
         *
         * @param inputDateStr    input date string
         * @param dateFormats input date formats string array
         * @param outputDateformat output date format
         * @return Returns formatted date
         */
        public String formatDateForComment(String inputDateStr, String[] dateFormats, String outputDateformat) {
            String outputDateStr = "";
            try {
                Date inputDate = DateUtils.parseDate(inputDateStr, dateFormats);
                SimpleDateFormat sdformat = new SimpleDateFormat(outputDateformat);
                outputDateStr = sdformat.format(inputDate); 
                logger.info("outputDateStr : " + outputDateStr );
            } catch (ParseException e) {
                logger.error("Parse Exception in formatDateForComment: ", e);
            }finally {
                if(!StringUtils.isNotBlank(outputDateStr)) {
                    logger.info("Returning the input date not able to format input date." );
                    outputDateStr = inputDateStr;
                }
            }
            logger.info("Final outputDateStr : " + outputDateStr );
            return outputDateStr;
        }

    /**
     * Method inserts the content
     *
     * @param taskFile    file path
     * @param task content
     * @return Returns number of rows affected by query execution
     */
    public boolean insertWorkFlowData(CSFile taskFile, CSExternalTask task){
        logger.debug("DataBaseStatusIngest : insertWorkFlowData");
        Connection connection = null;
        boolean isDataInserted = false;

        PreparedStatement preparedStatement = null;
        try {
            logger.info("Workflow Details : "+task.getWorkflow().toString());
            
            String taskName;
            String modifier;
            String reviewer;
            String approver;
            String reviewDate;
            String approveDate;
            String[] dateFormats =  {DATE_TIME_FORMAT,"EEE MMM dd HH:mm:ss yyyy"};
            String jobStatus = task.getName().split("DB")[0].trim().toUpperCase();
            String fileStatus  = "";
            String commentStr;
            String entityVal = "";
            if(taskFile.getRevisionNumber() == 0){
                fileStatus = "ADD";
            }else if(taskFile.getRevisionNumber() > 0){
                fileStatus = "MODIFY";
            }else if(taskFile.getKind() == CSHole.KIND){
                logger.info("Deleted file in the Workflow");
                fileStatus = "DELETE";
            }
            if(taskFile.getKind() != CSHole.KIND){
                CSSimpleFile taskSimpleFile = (CSSimpleFile) taskFile;
                entityVal = taskSimpleFile.getExtendedAttribute(META_ENTITY).getValue();
            }
            if(entityVal == null)
                entityVal = "";

            taskName = task.getName();
            logger.info("Task Name: " + taskName);
            
            modifier = taskFile.getLastModifier().getName();
            logger.info("Last Modified By: " + modifier);
            
            if(StringUtils.isNotBlank(modifier)  
                    && StringUtils.equals(TASK_REVIEW_PENDING_DB, taskName)) {
                lastModifierMap.put(getMapKey(taskFile), modifier);
            }

            reviewDate = task.getWorkflow().getVariable(META_REVIEW_DATE);
            logger.info("Review Date : " + reviewDate );
            Timestamp reviewedDate = null;
            if(reviewDate != null)
                reviewedDate = new Timestamp(DateUtils.parseDate(reviewDate,dateFormats).getTime());
            logger.info("reviewedDate : " + reviewedDate );

            approveDate = task.getWorkflow().getVariable(META_APPROVE_DATE);
            logger.info("Approve Date : " + approveDate );
            Timestamp approvalDate = null;
            if(approveDate != null)
                    approvalDate = new Timestamp(DateUtils.parseDate(approveDate,dateFormats).getTime());
            logger.info("approvalDate : " + approvalDate );
            
            reviewer = task.getWorkflow().getVariable("WF_Reviewer");
            logger.info("Workflow Reviewer : " + reviewer );

            approver = task.getWorkflow().getVariable("WF_Approver");
            logger.info("Workflow Approver : " + approver );

            CSWorkflow workflow = task.getWorkflow();
            CSComment[] comments = workflow.getComments();
            StringBuilder commentsToLogInDB = new StringBuilder();
            for(CSComment comment : comments) {
                commentsToLogInDB.append("[").append(comment.getCreationDate()).append("] ").append(comment.getCreator()).append(": ").append(comment.getComment()).append(System.lineSeparator());
            }
            commentStr = commentsToLogInDB.toString();

            connection = postgre.getConnection();
            logger.info("DB connection : "+connection.toString());
            int result;
            logger.info("DataBaseStatusIngest : insertWorkFlowData");
            String query = "INSERT INTO WORKFLOW_TABLE(\"WORKFLOW_ID\", \"WORKFLOW_USER\",\"CATEGORY\", \"CONTENT_PATH\", \"LANG\", \"WORKFLOW_START_DATE\", \"WORKFLOW_REVIEWER\", \"REVIEW_DATE\", \"WORKFLOW_APPROVER\", \"APPROVAL_DATE\", \"COUNT_REJECT_REVIEW\", \"COUNT_REJECT_APPROVE\", \"WORKFLOW_STATUS\", \"FILE_STATUS\", \"WORKFLOW_COMMENTS\", \"ENTITY\") VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            logger.info("Query : " + query);
            connection.setAutoCommit(true);
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setLong(1, task.getWorkflowId());
            preparedStatement.setString(2, task.getWorkflow().getOwner().getNormalizedName());
            preparedStatement.setString(3, getContentCategory(taskFile));
            preparedStatement.setString(4, getTaskFilePath(taskFile));
            preparedStatement.setString(5, getLang(taskFile));
            preparedStatement.setTimestamp(6, new Timestamp(task.getWorkflow().getActivationDate().getTime()));
            preparedStatement.setString(7, reviewer);
            preparedStatement.setTimestamp(8, reviewedDate);
            preparedStatement.setString(9, approver);
            preparedStatement.setTimestamp(10, approvalDate);
            preparedStatement.setLong(11,getRejectCount(task.getWorkflow().getVariable(META_REVIEW_REJECT_DATES)));
            preparedStatement.setLong(12,getRejectCount(task.getWorkflow().getVariable(META_APPROVE_REJECT_DATES)));
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
     * @param taskFile Task file of CSSimpleFile object
     * @return Returns content type.
     */
    public String getContentCategory(CSFile taskFile) {

        String fileLocation;
        String category="";
        String workAreaLocation = "/WORKAREA/default/";
        String templateDataLocation = "templatedata/";
        String stageLocation = "/STAGING/";
        try {
            fileLocation = taskFile.getVPath().toString();
            logger.debug("fileLocation : " + fileLocation);
            if(taskFile.getKind()!=CSHole.KIND){
                CSSimpleFile taskSimpleFile = (CSSimpleFile) taskFile;
                if(null != taskSimpleFile.getExtendedAttribute(META_DATA_NAME_DCR_TYPE).getValue()) {
                    category = taskSimpleFile.getExtendedAttribute(META_DATA_NAME_DCR_TYPE).getValue();
                }else{
                    logger.info("File is not a Content DCR");
                    if(fileLocation.contains(workAreaLocation)) {
                        String [] path = fileLocation.substring(fileLocation.indexOf(workAreaLocation)+18).split("/");
                        category = path[0]+"/"+path[1];
                    }
                }
            } else{
                logger.info("File is Deleted Item");
                if(fileLocation.contains(workAreaLocation + templateDataLocation)) {
                    String [] path = fileLocation.substring(fileLocation.indexOf(workAreaLocation+templateDataLocation)+31).split("/");
                    category = path[0]+"/"+path[1];
                } else if(fileLocation.contains(workAreaLocation)) {
                    String [] path = fileLocation.substring(fileLocation.indexOf(workAreaLocation)+18).split("/");
                    category = path[0]+"/"+path[1];
                }  else if(fileLocation.contains(stageLocation + "templatedata/")) {
                    String [] path = fileLocation.substring(fileLocation.indexOf(stageLocation + templateDataLocation)+22).split("/");
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
     * @param taskFile Task file of CSSimpleFile object
     * @return Returns path of file.
     */
    public String getTaskFilePath(CSFile taskFile) {
        String taskFileString = "";
        try {
            taskFileString = taskFile.getVPath().toString();
            logger.debug("taskSimpleFileString : " + taskFileString);
        } catch (Exception e) {
            logger.error("Exception in getTaskFilePath: ", e);
        }
        return taskFileString;
    }

    /**
     * Method to get the lang of the input file.
     *
     * @param taskFile Task file of CSSimpleFile object
     * @return Returns lang of the input file.
     */
    public String getLang(CSFile taskFile) {
        String fileLocation;
        String lang = "";
        try {
            fileLocation = taskFile.getVPath().toString();
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

    /**
     * Method to get the reject count using the reject dates.
     *
     * @param rejectDateString concatenated reject dates
     * @return Returns reject count.
     */
    public int getRejectCount(String rejectDateString){
        int rejectCount;
        rejectDateString = rejectDateString != null ? rejectDateString : "";
        logger.info(" Reject Dates : " + rejectDateString );

        rejectCount = rejectDateString.replaceAll("\\s+","").length() / 20;
        logger.info(" Reject Count : " + rejectCount );
        return rejectCount;
    }
    
    /**
     * Method to process the workflow inputs.
     *
     * @param task CSExternalTask object
     * @param taskFileList Task file list of CSAreaRelativePath object
     */
    public void processWorkFlowInputs(CSExternalTask task, CSAreaRelativePath[] taskFileList) {
        logger.info("In processWorkFlowInputs");
        try {
            
            String lastModifierStr = task.getWorkflow().getVariable(META_LAST_MODIFIER);
            updateLastModifierMap(lastModifierStr);
            
            updateModifierComments(task, taskFileList);
            logger.info("commentsMap : " + commentsMap );
            
            updateRejectDates(task);
            
        } catch (Exception e) {
            logger.error("Exception in HashMap<String, String> lastModifierMap = new HashMap<String, String>();: ", e);
        }
    }
    
    /**
     * Method to updates the modifier comment for each task file in Map.
     *
     * @param task CSExternalTask object
     * @param taskFileList Task file list of CSAreaRelativePath object
     */
    public void updateModifierComments(CSExternalTask task, CSAreaRelativePath[] taskFileList) {
        try {
            logger.info("updateModifierComments file count : "+taskFileList.length);
            for (CSAreaRelativePath taskFile : taskFileList) {

                    CSFile taskSimpleFile = task.getArea().getFile(taskFile);
                    
                    String taskName = task.getName();
                    logger.info("Task Name : " + taskName);
                    
                    String fileName = taskSimpleFile.getName();
                    logger.info("File Name : " + fileName);
                    String lang = getLang(taskSimpleFile);
                    logger.info("Lang : " + lang);
                    
                    String modifier = taskSimpleFile.getLastModifier().getName();
                    logger.info("Last Modified By: " + modifier);
                    
                    String modifierMetaData = lastModifierMap.get(getMapKey(taskSimpleFile));
                    logger.info("EA Modifier : " + modifierMetaData);
                    
                    if(StringUtils.equals(TASK_APPROVE_PENDING_DB, taskName) 
                            && !StringUtils.equals(modifier, modifierMetaData)) {
                        String commentOnModifier = modifier+" : Updated content on behalf of "+modifierMetaData+".";
                        logger.info("commentOnModifier : " + commentOnModifier +" for file "+fileName+" lang "+lang);
                        commentsMap.put(getMapKey(taskSimpleFile), commentOnModifier);
                    }
            }
        } catch (Exception e) {
            logger.error("Exception in updateModifierComments: ", e);
        }
    }
    
    /**
     * Method to updates the reject dates for the task.
     *
     * @param task CSExternalTask object
     */
    public void updateRejectDates(CSExternalTask task) {
        try {
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);
            
            if(StringUtils.equals(TASK_REVIEW_REJECT_DB, task.getName())) {
                String reviewRejectDates = task.getWorkflow().getVariable(META_REVIEW_REJECT_DATES);
                if(reviewRejectDates == null) {
                    reviewRejectDates = LocalDateTime.now().format(formatter);
                }else {
                    reviewRejectDates = reviewRejectDates+" "+LocalDateTime.now().format(formatter);
                }
                task.getWorkflow().setVariable(META_REVIEW_REJECT_DATES, reviewRejectDates);
                logger.info("Updated Reveiew Reject Dates : " + task.getWorkflow().getVariable(META_REVIEW_REJECT_DATES) );
            }
            
            if(StringUtils.equals(TASK_APPROVAL_REJECT_DB, task.getName())) {
                String approveRejectDates = task.getWorkflow().getVariable(META_APPROVE_REJECT_DATES);
                if(approveRejectDates == null) {
                    approveRejectDates = LocalDateTime.now().format(formatter);
                }else {
                    approveRejectDates = approveRejectDates+" "+LocalDateTime.now().format(formatter);
                }
                task.getWorkflow().setVariable(META_APPROVE_REJECT_DATES, approveRejectDates);
                logger.info("Updated Approve Reject Dates : " + task.getWorkflow().getVariable(META_APPROVE_REJECT_DATES) );
            }
        } catch (Exception e) {
            logger.error("Exception in getModifierComments: ", e);
        }
    }
    
    /**
     * Method to get the comment message key.
     *
     * @param taskFile Task file of CSSimpleFile object
     * @return Returns comments key for the file.
     */
    public String getMapKey(CSFile taskFile) {
        String mapKey = "";
        String fileName;
        String lang;
        try {
            fileName = taskFile.getName();
            lang = getLang(taskFile);
            if(StringUtils.isNotBlank(lang)) {
                mapKey = fileName +"_"+ lang;
            }else {
                mapKey = fileName;
            }
            logger.debug("MapKey : " + mapKey);
        } catch (Exception e) {
            logger.error("Exception in getMapKey: ", e);
        }
        return mapKey;
    }
    
    /**
     * Method to update the map of last modifier for each task files.
     *
     * @param lastModifierStr Concatenated string of last modifiers
     * @return Returns map of last modifier for each task file.
     */
    public HashMap<String, String> updateLastModifierMap(String lastModifierStr) {
        logger.info("In updateLastModifierMap");
        try {
            if(StringUtils.isNotBlank(lastModifierStr)){
                String[] lastModifierArray = StringUtils.split(lastModifierStr, ",");
                logger.info("Total number of last modifer info (input): " + lastModifierArray.length);
                if(lastModifierArray.length > 0) {
                    for(String lastModifier : lastModifierArray) {
                        String[] lmArr = StringUtils.split(lastModifier, ":");
                        lastModifierMap.put(lmArr[0], lmArr[1]);
                    }
                }
            }
            logger.info("Created lastModifierMap : " + lastModifierMap);
        } catch (Exception e) {
            logger.error("Exception in updateLastModifierMap: ", e);
        }
        return lastModifierMap;
    }
    
    
    /**
     * Method to get the map of last modifier for each task files.
     *
     * @return Returns concatenated string of ylast modifiers.
     */
    public String getLastModifierString() {
        logger.info("In getLastModifierString");
        StringJoiner lastModifierSJ = new StringJoiner(",");
        try {
            if(lastModifierMap != null && !lastModifierMap.isEmpty()){
                logger.info("Total number of last modifer info (output): " + lastModifierMap.size());
                for (Map.Entry<String, String> lmset : lastModifierMap.entrySet()) {
                    lastModifierSJ.add(lmset.getKey()+":"+lmset.getValue());
               }
            }
            logger.info("Created lastModifierStr : " + lastModifierSJ.toString());
        } catch (Exception e) {
            logger.error("Exception in getLastModifierAsString: ", e);
        }
        return lastModifierSJ.toString();
    }
}
