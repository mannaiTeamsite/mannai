package com.hukoomi.livesite.task;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;

import com.hukoomi.utils.Postgre;
import com.hukoomi.utils.TSPropertiesFileReader;
import com.interwoven.cssdk.access.CSAuthorizationException;
import com.interwoven.cssdk.access.CSExpiredSessionException;
import com.interwoven.cssdk.common.CSClient;
import com.interwoven.cssdk.common.CSException;
import com.interwoven.cssdk.common.CSObjectNotFoundException;
import com.interwoven.cssdk.common.CSRemoteException;
import com.interwoven.cssdk.filesys.CSAreaRelativePath;
import com.interwoven.cssdk.filesys.CSFile;
import com.interwoven.cssdk.filesys.CSSimpleFile;
import com.interwoven.cssdk.filesys.CSVPath;
import com.interwoven.cssdk.workflow.CSExternalTask;
import com.interwoven.cssdk.workflow.CSURLExternalTask;
import com.interwoven.livesite.runtime.RequestContext;

public class PollSurveyTask  implements CSURLExternalTask{
	/** Logger object to check the flow of the code.*/
	private final Logger logger = Logger.getLogger(PollSurveyTask.class);
	public static final String META_DATA_NAME_DCR_TYPE = "TeamSite/Templating/DCR/Type";
	public static final String ID_PATH = "/root/information/id"; 
	public static final String LANG_PATH = "/root/information/language/value"; 
	public static final String QUESTION_PATH = "/root/detail/question"; 
	public static final String POLL_START_DATE_PATH = "/root/detail/start-date"; 
	public static final String POLL_END_DATE_PATH = "/root/detail/end-date"; 
	public static final String PERSONA_PATH = "/root/settings/persona/value"; 
	public static final String OPTION_PATH = "/root/detail/option"; 
	public static final String TITLE_PATH = "/root/details/survey-title"; 
	public static final String DESCRIPTION_PATH = "/root/details/description"; 
	public static final String SURVEY_START_DATE_PATH = "/root/details/start-date"; 
	public static final String SURVEY_END_DATE_PATH = "/root/details/end-date"; 
	public static final String FIELD_PATH = "/root/form-field";
	public static final String SUCCESS_TRANSITION = "Insert Master Data Success";
	public static final String FAILURE_TRANSITION = "Insert Master Data Failure";
	private static final String DB_PROPERTY_FILE = "dbconfig.properties";
	Postgre postgre =  null;
	
	@Override
	 public void execute ( CSClient client, CSExternalTask task, Hashtable params) throws CSException{
		logger.info("PollSurveyTask - execute");
		boolean isDBOperationSuccess = false;
		String transition = "";
		String transitionComment = "";
		CSAreaRelativePath[] taskFileList = task.getFiles();
		logger.debug("TaskFileList Length : " + taskFileList.length);
		
		//propertiesFile = propertiesFileLoader(client, task);
		//String connectionStr = getConnectionString();
		
		postgre =  new Postgre(client, task, DB_PROPERTY_FILE);
		
		for (CSAreaRelativePath taskFile : taskFileList) {
			try {	
				CSSimpleFile taskSimpleFile = (CSSimpleFile) task.getArea().getFile(taskFile);
				logger.debug("File Name : "+taskSimpleFile.getName());
				
				String dcrType  = taskSimpleFile.getExtendedAttribute(META_DATA_NAME_DCR_TYPE).getValue();
				
				if(dcrType != null) {
					if("Content/Polls".equalsIgnoreCase(dcrType)) {
						Document document = getTaskDocument(taskSimpleFile);
						if(isPollMasterDataAvailable(document)) {
							isDBOperationSuccess = updatePollMasterData(document);
							logger.debug("isPollDataUpdated : "+isDBOperationSuccess);
							if(isDBOperationSuccess) {
								transitionComment = "Poll master data updated successfully";
								transition = SUCCESS_TRANSITION;
							}else {
								transitionComment = "Failed to updated poll master data";
								transition = FAILURE_TRANSITION;
							}
						}else {
							isDBOperationSuccess = insertPollMasterData(document);
							logger.debug("isPollDataInserted : "+isDBOperationSuccess);
							if(isDBOperationSuccess) {
								transitionComment = "Poll master data inserted successfully";
								transition = SUCCESS_TRANSITION;
							}else {
								transitionComment = "Failed to insert poll master data";
								transition = FAILURE_TRANSITION;
							}
						}
					}else if("Content/Survey".equalsIgnoreCase(dcrType)) {
						Document document = getTaskDocument(taskSimpleFile);
						if(isSurveyMasterDataAvailable(document)) {
							isDBOperationSuccess = updateSurveyMasterData(document);
							logger.debug("isSurveyDataUpdated : "+isDBOperationSuccess);
							if(isDBOperationSuccess) {
								transitionComment = "Survey master data updated successfully";
								transition = SUCCESS_TRANSITION;
							}else {
								transitionComment = "Failed to updated survey master data";
								transition = FAILURE_TRANSITION;
							}
						}else {
							isDBOperationSuccess = insertSurveyMasterData(document);
							logger.debug("isSurveyDataInserted : "+isDBOperationSuccess);
							if(isDBOperationSuccess) {
								transitionComment = "Survey master data inserted successfully";
								transition = SUCCESS_TRANSITION;
							}else {
								transitionComment = "Failed to insert survey master data";
								transition = FAILURE_TRANSITION;
							}
						}
					}else {
						logger.debug("Master data insert skipped - Not Polls or Survey DCR");
						transitionComment = "";
						transition = SUCCESS_TRANSITION;
					}
				}
				
			} catch (Exception e) {
				transitionComment = "Technical Error in master data insert";
				transition = FAILURE_TRANSITION;
				logger.error("execute : Exception : "+ e.getMessage());
				e.printStackTrace();
			}
		}
		
		//Testing Failure Transition
		//transitionComment = "Technical Error in master data insert";
		//transition = FAILURE_TRANSITION;
		
		logger.debug("transition : "+transition);
		logger.debug("transitionComment : "+transitionComment);
		task.chooseTransition(transition, transitionComment);
	}
	
	 /*public String getPropertiesValue(final String key) {
        String value = "";
        if (key != null && !key.equals("")) {
            value = propertiesFile.getProperty(key);
        }
        return value;
    }*/
	 
	 /*private String getConnectionString() {
		 logger.info("PollSurveyTask : getConnectionString()");
		 String connectionStr = null;
		 String host = getPropertiesValue("host");
		 String port = getPropertiesValue("port");
		 String database = getPropertiesValue("database");
		 String schema = getPropertiesValue("schema");
		 String username = getPropertiesValue("username");
		 String password = getPropertiesValue("password");

		 // jdbc:postgresql://172.16.167.164:5432/devapps,"tsdev","Motc@1234"
		 connectionStr = "jdbc:" + database + "://" + host + ":" + port+ "/" + schema;
		 logger.info("Connection String : " + connectionStr);

		 return connectionStr;
	 }*/
	 
	 /*public Properties loadPropertyFile() {
		 
		String vPath = task.getArea().getRootDir().getVPath().toString();
		logger.debug("Task Area Root Dir String : " + vPath);
		
		Properties propFile = new Properties();
		String propFilePath = vPath + PROPERTY_PATH + DB_PROPERTY_FILE;
		CSSimpleFile propSimpleFile = (CSSimpleFile) client.getFile(new CSVPath(propFilePath));
		InputStream inputStream = propSimpleFile.getInputStream(false);
		
		if (inputStream != null) {
		    try {
		        propFile.load(inputStream);
		        logger.debug("Properties File Loaded");
		    } catch (IOException ex) {
		    	logger.error("IO Exception while loading Properties file : ", ex);
		    }
		}
		
		//CSSimpleFile.getInputStream
		
		logger.debug("Task Area Root Dir : " + task.getArea().getRootDir().getVPath().toElement().getStringValue());
		logger.debug("Task Area Branch Vpath Name : " + task.getArea().getBranch().getVPath().getName());
		return propFile;
	 }*/
	 
	 /*private Properties propertiesFileLoader(CSClient client, CSExternalTask task) {
		 logger.debug("PollSurveyTask : propertiesFileLoader");
	     Properties propFile = new Properties();
	     
	     try {
	    	 String vPath = task.getArea().getRootDir().getVPath().toString();
			 logger.debug("vPath : " + vPath);
				
			 if (vPath != null && !vPath.equals("")) {
				 String propFilePath = vPath + PROPERTY_PATH + DB_PROPERTY_FILE;
				 logger.debug("propFilePath : " + propFilePath);
				 CSSimpleFile propSimpleFile = (CSSimpleFile) client.getFile(new CSVPath(propFilePath));
				 InputStream inputStream = propSimpleFile.getInputStream(false);
				
				 if (inputStream != null) {
					 try {
						 propFile.load(inputStream);
						 logger.debug("Properties File Loaded");
					 }catch (IOException ex) {
						 logger.error("IO Exception while loading Properties file : ", ex);
					 }
				 }
			       
			 }else{
				 logger.error("Error reading vPath");
			 }
			 logger.info("Finish Loading Properties File.");
		 }catch (Exception e) {
			 logger.error("Exception in loading property file : "+e.getMessage());
			 e.printStackTrace();
		 }
	     return propFile;
	  }*/
	 
	
	public Document getTaskDocument(CSSimpleFile taskSimpleFile) {
		logger.debug("PollSurveyTask : getTaskDocument");
		Document document = null;
		try {
			byte[] taskSimpleFileByteArray = taskSimpleFile.read(0,-1);
			String taskSimpleFileString = new String(taskSimpleFileByteArray);
			logger.debug(taskSimpleFileString+" = "+taskSimpleFileString);
			document = DocumentHelper.parseText(taskSimpleFileString);
			logger.debug(document+" = "+document.asXML());
		} catch (Exception e) {
			logger.error("Exception : "+ e.getMessage());
			e.printStackTrace();
		}
		return document;
	}
	
	public boolean isPollMasterDataAvailable(Document document) {
		logger.debug("PollSurveyTask : isPollMasterDataAvailable");
		PreparedStatement prepareStatement = null;
		Connection connection = null;
		ResultSet rs = null;
		boolean isPollDataAvailable = false;
		try {
			
			connection = postgre.getConnection();
			logger.info("Connection : "+connection);
			
			Long pollId = Long.parseLong(getDCRValue(document, ID_PATH));
			String lang = getDCRValue(document, LANG_PATH);
			
			String pollMasterQuery = "SELECT COUNT(*) FROM POLL_MASTER WHERE POLL_ID = ? AND LANG = ?";
			logger.info("pollMasterQuery : "+pollMasterQuery);
			prepareStatement = connection.prepareStatement(pollMasterQuery);
		    prepareStatement.setLong(1, pollId);
		    prepareStatement.setString(2, lang);
		    rs = prepareStatement.executeQuery();
		    while (rs.next()) {
		    	int count = rs.getInt(1);
		    	if(count > 0) {
		    		isPollDataAvailable = true;
		    	}
		    }
		    logger.info("isPollDataAvailable : "+isPollDataAvailable);
			
		} catch (Exception e) {
		    logger.error("Exception : "+e.getMessage());
		    e.printStackTrace();
		} finally {
		    Postgre.releaseConnection(connection, prepareStatement, null);
		    logger.info("Released connection");
		}
		return isPollDataAvailable;
	}
	
	public boolean insertPollMasterData(Document document) {
		logger.debug("PollSurveyTask : insertPollMasterData");
		PreparedStatement prepareStatement = null;
		Connection connection = null;
		boolean isPollDataInserted = false;
		try {
			
			connection = postgre.getConnection();
			logger.info("Connection : "+connection);
			
			Long pollId = Long.parseLong(getDCRValue(document, ID_PATH));
			String lang = getDCRValue(document, LANG_PATH);
			String question = getDCRValue(document, QUESTION_PATH);
			String startDateStr = getDCRValue(document, POLL_START_DATE_PATH);
			String endDateStr = getDCRValue(document, POLL_END_DATE_PATH);
			String persona = getDCRValue(document, PERSONA_PATH);
			
			Date startDate = getDate(startDateStr);
			Date endDate = getDate(endDateStr);
			
			String pollMasterQuery = "INSERT INTO POLL_MASTER (POLL_ID, LANG, QUESTION, START_DATE, END_DATE, PERSONA) VALUES (?, ?, ?, ?, ?, ?)";
			logger.info("pollMasterQuery : "+pollMasterQuery);
			connection.setAutoCommit(false);
			prepareStatement = connection.prepareStatement(pollMasterQuery);
		    prepareStatement.setLong(1, pollId);
		    prepareStatement.setString(2, lang);
		    prepareStatement.setString(3, question);
		    prepareStatement.setDate(4, startDate);
		    prepareStatement.setDate(5, endDate);
		    prepareStatement.setString(6, persona);
		    int result = prepareStatement.executeUpdate();
		    logger.info("result : "+result);
		    if (result > 0) {
		    	logger.info("Poll Master Data Inserted");
		    	
		    	String pollOptionQuery = "INSERT INTO POLL_OPTION (OPTION_ID, LANG, POLL_ID, OPTION_LABEL, OPTION_VALUE) VALUES(?, ?, ?, ?, ?)";
		    	prepareStatement = connection.prepareStatement(pollOptionQuery);
		    	logger.info("pollOptionQuery : "+pollOptionQuery);
		    	
		    	List<Node> nodes = document.selectNodes(OPTION_PATH);
		    	long optionId = 1l;
		    	for (Node node : nodes) {
		    		logger.info("optionId : "+optionId);
		    		String label = node.selectSingleNode("label").getText();
		    		String value = node.selectSingleNode("value").getText();
		    		logger.info("label : "+label);
		    		logger.info("value : "+value);
		    		prepareStatement.setLong(1, optionId);
		    		prepareStatement.setString(2, lang);
		    		prepareStatement.setLong(3, pollId);
		            prepareStatement.setString(4, label);
		            prepareStatement.setString(5, value);
		            prepareStatement.addBatch();
		            optionId++;
		    	}
		    	int[] optionBatch = prepareStatement.executeBatch();
		    	logger.info("optionBatch length : "+optionBatch.length);
		    	
		    	if(optionBatch.length == optionId-1) {
		    		connection.commit();
		    		logger.info("Poll Option Inserted");
		    		isPollDataInserted = true;
		    	}else {
		    		connection.rollback();
		    		logger.info("Option batch insert failed");
		    	}
		    } else {
		    	connection.rollback();
		    	logger.info("Poll master insert failed");
		    }
			
		} catch (Exception e) {
			try {
				connection.rollback();
				logger.error("Exception : "+e.getMessage());
			    e.printStackTrace();
			} catch (SQLException ex) {
				logger.error("Exception in rollback catch block : "+ex.getMessage());
				ex.printStackTrace();
			}
		} finally {
		    Postgre.releaseConnection(connection, prepareStatement, null);
		    logger.info("Released connection");
		}
		return isPollDataInserted;
	}
	
	public boolean updatePollMasterData(Document document) {
		logger.debug("PollSurveyTask : updatePollMasterData");
		PreparedStatement prepareStatement = null;
		Connection connection = null;
		boolean isPollDataInserted = false;
		try {
			
			connection = postgre.getConnection();
			logger.info("Connection : "+connection);
			
			Long pollId = Long.parseLong(getDCRValue(document, ID_PATH));
			String lang = getDCRValue(document, LANG_PATH);
			String question = getDCRValue(document, QUESTION_PATH);
			String endDateStr = getDCRValue(document, POLL_END_DATE_PATH);
			String persona = getDCRValue(document, PERSONA_PATH);
			
			Date endDate = getDate(endDateStr);
			
			String pollMasterQuery = "UPDATE POLL_MASTER SET QUESTION = ?, END_DATE = ?, PERSONA = ? WHERE POLL_ID = ? AND LANG = ?";
			logger.info("pollMasterQuery : "+pollMasterQuery);
			connection.setAutoCommit(false);
			prepareStatement = connection.prepareStatement(pollMasterQuery);
			prepareStatement.setString(1, question);
			prepareStatement.setDate(2, endDate);
			prepareStatement.setString(3, persona);
		    prepareStatement.setLong(4, pollId);
		    prepareStatement.setString(5, lang);
		    
		    int result = prepareStatement.executeUpdate();
		    logger.info("result : "+result);
		    if (result > 0) {
		    	logger.info("Poll Master Data Updated");
		    	
		    	String pollOptionQuery = "UPDATE POLL_OPTION SET OPTION_LABEL = ? WHERE OPTION_ID = ? AND POLL_ID = ? AND LANG = ?";
		    	prepareStatement = connection.prepareStatement(pollOptionQuery);
		    	logger.info("pollOptionQuery : "+pollOptionQuery);
		    	
		    	List<Node> nodes = document.selectNodes(OPTION_PATH);
		    	long optionId = 1l;
		    	for (Node node : nodes) {
		    		logger.info("optionId : "+optionId);
		    		String label = node.selectSingleNode("label").getText();
		    		logger.info("label : "+label);
		    		prepareStatement.setString(1, label);
		    		prepareStatement.setLong(2, optionId);
		    		prepareStatement.setLong(3, pollId);
		    		prepareStatement.setString(4, lang);
		            prepareStatement.addBatch();
		            optionId++;
		    	}
		    	int[] optionBatch = prepareStatement.executeBatch();
		    	logger.info("optionBatch length : "+optionBatch.length);
		    	
		    	if(optionBatch.length == optionId-1) {
		    		connection.commit();
		    		logger.info("Poll Option Updated");
		    		isPollDataInserted = true;
		    	}else {
		    		connection.rollback();
		    		logger.info("Option batch update failed");
		    	}
		    } else {
		    	connection.rollback();
		    	logger.info("Poll master update failed");
		    }
			
		} catch (Exception e) {
			try {
				connection.rollback();
				logger.error("Exception : "+e.getMessage());
			    e.printStackTrace();
			} catch (SQLException ex) {
				logger.error("Exception in rollback catch block : "+ex.getMessage());
				ex.printStackTrace();
			}
		} finally {
		    Postgre.releaseConnection(connection, prepareStatement, null);
		    logger.info("Released connection");
		}
		return isPollDataInserted;
	}
	
	public boolean isSurveyMasterDataAvailable(Document document) {
		logger.debug("PollSurveyTask : isSurveyMasterDataAvailable");
		PreparedStatement prepareStatement = null;
		Connection connection = null;
		ResultSet rs = null;
		boolean isSurveyDataAvailable = false;
		try {
			
			connection = postgre.getConnection();
			logger.info("Connection : "+connection);
			
			Long surveyId = Long.parseLong(getDCRValue(document, ID_PATH));
			String lang = getDCRValue(document, LANG_PATH);
			
			String surveyMasterQuery = "SELECT COUNT(*) FROM SURVEY_MASTER WHERE SURVEY_ID = ? AND LANG = ?";
			logger.info("surveyMasterQuery : "+surveyMasterQuery);
			prepareStatement = connection.prepareStatement(surveyMasterQuery);
		    prepareStatement.setLong(1, surveyId);
		    prepareStatement.setString(2, lang);
		    rs = prepareStatement.executeQuery();
		    while (rs.next()) {
		    	int count = rs.getInt(1);
		    	if(count > 0) {
		    		isSurveyDataAvailable = true;
		    	}
		    }
		    logger.info("isSurveyDataAvailable : "+isSurveyDataAvailable);
			
		} catch (Exception e) {
		    logger.error("Exception : "+e.getMessage());
		    e.printStackTrace();
		} finally {
		    Postgre.releaseConnection(connection, prepareStatement, null);
		    logger.info("Released connection");
		}
		return isSurveyDataAvailable;
	}
	
	public boolean insertSurveyMasterData(Document document) {
		logger.debug("PollSurveyTask : insertSurveyMasterData");
		PreparedStatement prepareStatement = null;
		PreparedStatement optionPrepareStatement = null;
		Statement questionIdQueryStmt = null;
		Connection connection = null;
		ResultSet rs = null;
		boolean isSurveyDataInserted = false;
		try {
			
			connection = postgre.getConnection();
			
			Long surveyId = Long.parseLong(getDCRValue(document, ID_PATH));
			String lang = getDCRValue(document, LANG_PATH);
			String title = getDCRValue(document, TITLE_PATH);
			String description = getDCRValue(document, DESCRIPTION_PATH);
			String startDateStr = getDCRValue(document, SURVEY_START_DATE_PATH);
			String endDateStr = getDCRValue(document, SURVEY_END_DATE_PATH);
			String persona = getDCRValue(document, PERSONA_PATH);
			
			Date startDate = getDate(startDateStr);
			Date endDate = getDate(endDateStr);
			
			String surveyMasterQuery = "INSERT INTO SURVEY_MASTER (SURVEY_ID, LANG, SURVEY_TITLE, SURVEY_DESCRIPTION, START_DATE, END_DATE, PERSONA) VALUES (?, ?, ?, ?, ?, ?, ?)";
			logger.info("surveyMasterQuery : "+surveyMasterQuery);
			connection.setAutoCommit(false);
			prepareStatement = connection.prepareStatement(surveyMasterQuery);
		    prepareStatement.setLong(1, surveyId);
		    prepareStatement.setString(2, lang);
		    prepareStatement.setString(3, title);
		    prepareStatement.setString(4, description);
		    prepareStatement.setDate(5, startDate);
		    prepareStatement.setDate(6, endDate);
		    prepareStatement.setString(7, persona);
		    int result = prepareStatement.executeUpdate();
		    logger.info("result : "+result);
		    if (result > 0) {
		    	logger.info("Survey Master Data Inserted");
		    	
		    	String surveyQuestionQuery = "INSERT INTO SURVEY_QUESTION (QUESTION_ID, SURVEY_ID, LANG, QUESTION_NO, QUESTION_TYPE, QUESTION) VALUES (?, ?, ?, ?, ?, ?)";
		    	prepareStatement = connection.prepareStatement(surveyQuestionQuery);
		    	logger.info("surveyQuestionQuery : "+surveyQuestionQuery);
		    	
		    	List<Node> nodes = document.selectNodes(FIELD_PATH);
		    	int questionNo = 1;
		    	for (Node node : nodes) {
		    		logger.info("questionNo : "+questionNo);
		    		
		    		String questionType = node.selectSingleNode(".//field-type").getText();
		    		logger.info("questionType : "+questionType);
		    		
		    		if(!"button".equalsIgnoreCase(questionType)) {
		    			
			    		String questionIdQuery = "SELECT nextval('survey_question_question_id_seq') as questionId";
		            	questionIdQueryStmt = connection.createStatement();
		            	rs = questionIdQueryStmt.executeQuery(questionIdQuery);
		            	Long questionId = 0L;
		            	while (rs.next()) {
		            		questionId = rs.getLong("questionId");
		            	}
		            	logger.info("questionId : "+questionId);
			    		
			    		
			    		String question = node.selectSingleNode(".//question").getText();
			    		logger.info("question : "+question);
			    		prepareStatement.setLong(1, questionId);
			    		prepareStatement.setLong(2, surveyId);
			    		prepareStatement.setString(3, lang);
			    		prepareStatement.setInt(4, questionNo);
			            prepareStatement.setString(5, questionType);
			            prepareStatement.setString(6, question);
			            int questionResult = prepareStatement.executeUpdate();
			            
			            if (questionResult > 0) {
			            	logger.info("Survey Question Inserted");
			            	
				            String surveyOptionQuery = "INSERT INTO SURVERY_OPTION (OPTION_ID, SURVEY_ID, LANG, QUESTION_ID, QUESTION_NO, OPTION_NO, OPTION) VALUES (nextval('survery_option_option_id_seq'), ?, ?, ?, ?, ?, ?)";
				            optionPrepareStatement = connection.prepareStatement(surveyOptionQuery);
				            logger.info("surveyOptionQuery : "+surveyOptionQuery);
				            List<Node> optionNodes = node.selectNodes(".//option");
				            int optionNo = 1;
				            for (Node optnode : optionNodes) {
				            	logger.info("optionNo : "+optionNo);
					    		String option = optnode.selectSingleNode("label").getText();
					    		logger.info("option : "+option);
					    		optionPrepareStatement.setLong(1, surveyId);
					    		optionPrepareStatement.setString(2, lang);
					    		optionPrepareStatement.setLong(3, questionId);
					    		optionPrepareStatement.setInt(4, questionNo);
					    		optionPrepareStatement.setInt(5, optionNo);
					    		optionPrepareStatement.setString(6, option);
					    		optionPrepareStatement.addBatch();
					    		optionNo++;
					    	}
				            int[] optionBatch = optionPrepareStatement.executeBatch();
				            logger.info("optionBatch length : "+optionBatch.length);
					    	
					    	if(optionBatch.length == optionNo-1) {
					    		logger.info("Survey Option Inserted");
					    	}else {
					    		connection.rollback();
					    		logger.info("Option batch insert failed");
					    		break;
					    	}
					    	questionNo++;
			            }else {
				    		connection.rollback();
				    		logger.info("Question insert failed");
				    		break;
				    	}
		    		}
		    	}
		    	isSurveyDataInserted = true;
		    } else {
		    	connection.rollback();
		    	logger.info("Survey master insert failed");
		    }
		    
		    if(isSurveyDataInserted) {
		    	connection.commit();
		    	logger.info("Survey insert transaction committed");
		    }
			
		} catch (Exception e) {
			try {
				connection.rollback();
				logger.error("Exception : "+e.getMessage());
			    e.printStackTrace();
			} catch (SQLException ex) {
				logger.error("Exception in rollback catch block : "+ex.getMessage());
				ex.printStackTrace();
			}
		} finally {
			Postgre.releaseConnection(null, questionIdQueryStmt, null);
		    Postgre.releaseConnection(null, optionPrepareStatement, null);
		    Postgre.releaseConnection(connection, prepareStatement, rs);
		    logger.info("Released connection");
		}
		return isSurveyDataInserted;
	}
	
	
	public boolean updateSurveyMasterData(Document document) {
		logger.debug("PollSurveyTask : updateSurveyMasterData");
		PreparedStatement prepareStatement = null;
		PreparedStatement optionPrepareStatement = null;
		Statement questionIdQueryStmt = null;
		Connection connection = null;
		ResultSet rs = null;
		boolean isSurveyDataUpdated = false;
		try {
			
			connection = postgre.getConnection();
			
			Long surveyId = Long.parseLong(getDCRValue(document, ID_PATH));
			String lang = getDCRValue(document, LANG_PATH);
			String title = getDCRValue(document, TITLE_PATH);
			String description = getDCRValue(document, DESCRIPTION_PATH);
			String endDateStr = getDCRValue(document, SURVEY_END_DATE_PATH);
			String persona = getDCRValue(document, PERSONA_PATH);
			Date endDate = getDate(endDateStr);
			
			String surveyMasterQuery = "UPDATE SURVEY_MASTER SET SURVEY_TITLE = ?, SURVEY_DESCRIPTION = ?, END_DATE = ?, PERSONA = ? WHERE SURVEY_ID = ? AND LANG = ?";
			logger.info("surveyMasterQuery : "+surveyMasterQuery);
			connection.setAutoCommit(false);
			prepareStatement = connection.prepareStatement(surveyMasterQuery);
			prepareStatement.setString(1, title);
		    prepareStatement.setString(2, description);
		    prepareStatement.setDate(3, endDate);
		    prepareStatement.setString(4, persona);
		    prepareStatement.setLong(5, surveyId);
		    prepareStatement.setString(6, lang);
		    
		    int result = prepareStatement.executeUpdate();
		    logger.info("result : "+result);
		    if (result > 0) {
		    	logger.info("Survey Master Data Updated");
		    	
		    	String surveyQuestionQuery = "UPDATE SURVEY_QUESTION SET QUESTION = ? WHERE SURVEY_ID = ? AND LANG = ? AND QUESTION_NO = ?";
		    	prepareStatement = connection.prepareStatement(surveyQuestionQuery);
		    	logger.info("surveyQuestionQuery : "+surveyQuestionQuery);
		    	
		    	List<Node> nodes = document.selectNodes(FIELD_PATH);
		    	int questionNo = 1;
		    	for (Node node : nodes) {
		    		logger.info("questionNo : "+questionNo);
		    		
		    		String questionType = node.selectSingleNode(".//field-type").getText();
		    		logger.info("questionType : "+questionType);
		    		
		    		if(!"button".equalsIgnoreCase(questionType)) {
		    			
			    		String question = node.selectSingleNode(".//question").getText();
			    		logger.info("question : "+question);
			    		prepareStatement.setString(1, question);
			    		prepareStatement.setLong(2, surveyId);
			    		prepareStatement.setString(3, lang);
			    		prepareStatement.setInt(4, questionNo);			            
			            int questionResult = prepareStatement.executeUpdate();
			            
			            if (questionResult > 0) {
			            	logger.info("Survey Question Inserted");
			            	
				            String surveyOptionQuery = "UPDATE SURVERY_OPTION SET OPTION = ? WHERE SURVEY_ID = ? AND LANG = ? AND QUESTION_NO = ? AND OPTION_NO = ?";
				            optionPrepareStatement = connection.prepareStatement(surveyOptionQuery);
				            logger.info("surveyOptionQuery : "+surveyOptionQuery);
				            List<Node> optionNodes = node.selectNodes(".//option");
				            int optionNo = 1;
				            for (Node optnode : optionNodes) {
				            	logger.info("optionNo : "+optionNo);
					    		String option = optnode.selectSingleNode("label").getText();
					    		logger.info("option : "+option);
					    		optionPrepareStatement.setString(1, option);
					    		optionPrepareStatement.setLong(2, surveyId);
					    		optionPrepareStatement.setString(3, lang);
					    		optionPrepareStatement.setInt(4, questionNo);
					    		optionPrepareStatement.setInt(5, optionNo);
					    		optionPrepareStatement.addBatch();
					    		optionNo++;
					    	}
				            int[] optionBatch = optionPrepareStatement.executeBatch();
				            logger.info("optionBatch length : "+optionBatch.length);
					    	
					    	if(optionBatch.length == optionNo-1) {
					    		logger.info("Survey Option Updated");
					    	}else {
					    		connection.rollback();
					    		logger.info("Option batch update failed");
					    		break;
					    	}
					    	questionNo++;
			            }else {
				    		connection.rollback();
				    		logger.info("Question update failed");
				    		break;
				    	}
		    		}
		    	}
		    	isSurveyDataUpdated = true;
		    } else {
		    	connection.rollback();
		    	logger.info("Survey master update failed");
		    }
		    
		    if(isSurveyDataUpdated) {
		    	connection.commit();
		    	logger.info("Survey update transaction committed");
		    }
			
		} catch (Exception e) {
			try {
				connection.rollback();
				logger.error("Exception : "+e.getMessage());
			    e.printStackTrace();
			} catch (SQLException ex) {
				logger.error("Exception in rollback catch block : "+ex.getMessage());
				ex.printStackTrace();
			}
		} finally {
			Postgre.releaseConnection(null, questionIdQueryStmt, null);
		    Postgre.releaseConnection(null, optionPrepareStatement, null);
		    Postgre.releaseConnection(connection, prepareStatement, rs);
		    logger.info("Released connection");
		}
		return isSurveyDataUpdated;
	}
	
	public String getDCRValue(Document document, String nodeName) {
		logger.debug("PollSurveyTask : getDCRValue");
		String dcrValue = document.selectSingleNode(nodeName).getText();	
		logger.debug(nodeName+" : "+dcrValue);
		return dcrValue;
	}
	
	public Date getDate(String inputDate) {
		logger.debug("PollSurveyTask : getDate");
		String[] DateArr = inputDate.split(" ");
		Date outDate = java.sql.Date.valueOf(DateArr[0]);
		logger.info(inputDate+" >>> "+outDate);
		return outDate;
	}
	
}
