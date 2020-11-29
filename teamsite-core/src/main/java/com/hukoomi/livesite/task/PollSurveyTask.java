package com.hukoomi.livesite.task;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;

import com.hukoomi.bo.PollsBO;
import com.hukoomi.bo.SurveyBO;
import com.hukoomi.utils.Postgre;
import com.interwoven.cssdk.common.CSClient;
import com.interwoven.cssdk.common.CSException;
import com.interwoven.cssdk.filesys.CSAreaRelativePath;
import com.interwoven.cssdk.filesys.CSSimpleFile;
import com.interwoven.cssdk.workflow.CSExternalTask;
import com.interwoven.cssdk.workflow.CSURLExternalTask;

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
	private static final String OPTION_LABEL = "label";
	private static final String OPTION_VALUE = "value";
	private static final String TRANSITION = "TRANSITION";
	private static final String TRANSITION_COMMENT = "TRANSITION_COMMENT";
	private static final String POLL_UPDATE_SUCCESS = "Poll master data updated successfully";
	private static final String POLL_UPDATE_FAILURE = "Failed to updated poll master data";
	private static final String POLL_INSERT_SUCCESS = "Poll master data inserted successfully";
	private static final String POLL_INSERT_FAILURE = "Failed to insert poll master data";
	private static final String POLL_TECHNICAL_ERROR = "Technical Error in poll master data insert";
	private static final String SURVEY_UPDATE_SUCCESS = "Survey master data updated successfully";
	private static final String SURVEY_UPDATE_FAILURE = "Failed to updated survey master data";
	private static final String SURVEY_INSERT_SUCCESS = "Survey master data inserted successfully";
	private static final String SURVEY_INSERT_FAILURE = "Failed to insert survey master data";
	private static final String SURVEY_TECHNICAL_ERROR = "Technical Error in survey master data insert";
	
	Postgre postgre =  null;
	
	@Override
	 public void execute ( CSClient client, CSExternalTask task, Hashtable params) throws CSException{
		logger.info("PollSurveyTask - execute");
		HashMap<String,String> statusMap = null;
		CSAreaRelativePath[] taskFileList = task.getFiles();
		logger.debug("TaskFileList Length : " + taskFileList.length);
		
		postgre =  new Postgre(client, task, DB_PROPERTY_FILE);
		statusMap = new HashMap<>();
		statusMap.put(TRANSITION, SUCCESS_TRANSITION);
		statusMap.put(TRANSITION_COMMENT, "");
		
		for (CSAreaRelativePath taskFile : taskFileList) {
			try {	
				CSSimpleFile taskSimpleFile = (CSSimpleFile) task.getArea().getFile(taskFile);
				logger.debug("File Name : "+taskSimpleFile.getName());
				
				String dcrType  = taskSimpleFile.getExtendedAttribute(META_DATA_NAME_DCR_TYPE).getValue();
				
				if(dcrType != null) {
					if("Content/Polls".equalsIgnoreCase(dcrType)) {
						statusMap = (HashMap<String,String>) processPollDCR(taskSimpleFile);
					}else if("Content/Survey".equalsIgnoreCase(dcrType)) {
						statusMap = (HashMap<String,String>) processSurveyDCR(taskSimpleFile);
					}else {
						logger.debug("Master data insert skipped - Not Polls or Survey DCR");
					}
				}
				
			} catch (Exception e) {
				logger.error("Exception in execute: "+ e.getMessage());
				e.printStackTrace();
			}
		}
		
		logger.debug("transition : "+statusMap.get(TRANSITION));
		logger.debug("transitionComment : "+statusMap.get(TRANSITION_COMMENT));
		task.chooseTransition(statusMap.get(TRANSITION), statusMap.get(TRANSITION_COMMENT));
	}
	
	public Map<String,String> processPollDCR(CSSimpleFile taskSimpleFile) {
		boolean isDBOperationSuccess = false;
		HashMap<String,String> statusMap = new HashMap<>();
		try {
			Document document = getTaskDocument(taskSimpleFile);
			if(isPollMasterDataAvailable(document)) {
				isDBOperationSuccess = updatePollMasterData(document);
				logger.debug("isPollDataUpdated : "+isDBOperationSuccess);
				if(isDBOperationSuccess) {
					statusMap.put(TRANSITION, SUCCESS_TRANSITION);
					statusMap.put(TRANSITION_COMMENT, POLL_UPDATE_SUCCESS);
				}else {
					statusMap.put(TRANSITION, FAILURE_TRANSITION);
					statusMap.put(TRANSITION_COMMENT, POLL_UPDATE_FAILURE);
				}
			}else {
				isDBOperationSuccess = insertPollData(document);
				logger.debug("isPollDataInserted : "+isDBOperationSuccess);
				if(isDBOperationSuccess) {
					statusMap.put(TRANSITION, SUCCESS_TRANSITION);
					statusMap.put(TRANSITION_COMMENT, POLL_INSERT_SUCCESS);
				}else {
					statusMap.put(TRANSITION, FAILURE_TRANSITION);
					statusMap.put(TRANSITION_COMMENT, POLL_INSERT_FAILURE);
				}
			}
		} catch (Exception e) {
			statusMap.put(TRANSITION, FAILURE_TRANSITION);
			statusMap.put(TRANSITION_COMMENT, POLL_TECHNICAL_ERROR);
			logger.error("Exception in poll master: "+ e.getMessage());
			e.printStackTrace();
		}
		return statusMap;
	}
	
	public Map<String,String> processSurveyDCR(CSSimpleFile taskSimpleFile) {
		boolean isDBOperationSuccess = false;
		HashMap<String,String> statusMap = new HashMap<>();
		try {
			Document document = getTaskDocument(taskSimpleFile);
			if(isSurveyMasterDataAvailable(document)) {
				isDBOperationSuccess = updateSurveyMasterData(document);
				logger.debug("isSurveyDataUpdated : "+isDBOperationSuccess);
				if(isDBOperationSuccess) {
					statusMap.put(TRANSITION, SUCCESS_TRANSITION);
					statusMap.put(TRANSITION_COMMENT, SURVEY_UPDATE_SUCCESS);
				}else {
					statusMap.put(TRANSITION, FAILURE_TRANSITION);
					statusMap.put(TRANSITION_COMMENT, SURVEY_UPDATE_FAILURE);
				}
			}else {
				isDBOperationSuccess = insertSurveyData(document);
				logger.debug("isSurveyDataInserted : "+isDBOperationSuccess);
				if(isDBOperationSuccess) {
					statusMap.put(TRANSITION, SUCCESS_TRANSITION);
					statusMap.put(TRANSITION_COMMENT, SURVEY_INSERT_SUCCESS);
				}else {
					statusMap.put(TRANSITION, FAILURE_TRANSITION);
					statusMap.put(TRANSITION_COMMENT, SURVEY_INSERT_FAILURE);
				}
			}
		} catch (Exception e) {
			statusMap.put(TRANSITION, FAILURE_TRANSITION);
			statusMap.put(TRANSITION_COMMENT, SURVEY_TECHNICAL_ERROR);
			logger.error("Exception in survey master: "+ e.getMessage());
			e.printStackTrace();
		}
		return statusMap;
	}
			
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
			logger.error("Exception in getTaskDocument: "+ e.getMessage());
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
			
			Long pollId = Long.parseLong(getDCRValue(document, ID_PATH));
			String lang = getDCRValue(document, LANG_PATH);
			
			String pollMasterQuery = "SELECT COUNT(*) FROM POLL_MASTER WHERE POLL_ID = ? AND LANG = ?";
			logger.info("pollMasterQuery in isPollMasterDataAvailable: "+pollMasterQuery);
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
		    logger.error("Exception in isPollMasterDataAvailable : "+e.getMessage());
		    e.printStackTrace();
		} finally {
		    Postgre.releaseConnection(connection, prepareStatement, rs);
		    logger.info("Released connection in isPollMasterDataAvailable");
		}
		return isPollDataAvailable;
	}
	
	public boolean insertPollData(Document document) {
		logger.debug("PollSurveyTask : insertPollData");
		PreparedStatement prepareStatement = null;
		PreparedStatement prepareStatementPollOption = null;
		Connection connection = null;
		boolean isPollDataInserted = false;
		try {
			
			connection = postgre.getConnection();
			PollsBO pollsBO = new PollsBO();
			pollsBO.setPollId(getDCRValue(document, ID_PATH));
			pollsBO.setLang(getDCRValue(document, LANG_PATH));
			pollsBO.setQuestion(getDCRValue(document, QUESTION_PATH));
			pollsBO.setStartDate(getDCRValue(document, POLL_START_DATE_PATH));
			pollsBO.setEndDate(getDCRValue(document, POLL_END_DATE_PATH));
			
			int result = insertPollMasterData(pollsBO, connection);
		    logger.info("insertPollMasterData result : "+result);
		    if (result > 0) {
		    	logger.info("Poll Master Data Inserted");
		    	
		    	boolean isPollOptionInserted = insertPollOptionsData(pollsBO, document, connection);
		    	
		    	if(isPollOptionInserted) {
	    			connection.commit();
		    		logger.info("Poll Option Inserted");
		    		isPollDataInserted = true;
		    	}else {
	    			connection.rollback();
		    		logger.info("insertPollData Option batch insert failed");
		    	}
		    } else {
	    		connection.rollback();
		    	logger.info("Poll master insert failed");
		    }
			
		} catch (Exception e) {
			try {
				if(connection != null) {
					connection.rollback();
				}
				logger.error("Exception in insertPollData: "+e.getMessage());
			    e.printStackTrace();
			} catch (SQLException ex) {
				logger.error("Exception in insertPollData rollback catch block : "+ex.getMessage());
				ex.printStackTrace();
			}
		} finally {
		    Postgre.releaseConnection(null, prepareStatementPollOption, null);
		    Postgre.releaseConnection(connection, prepareStatement, null);
		    logger.info("Released insertPollData connection");
		}
		return isPollDataInserted;
	}
	
	public int insertPollMasterData(PollsBO pollsBO, Connection connection) throws SQLException{
	    PreparedStatement preparedStatement = null;
	    int result = 0;
        try {
            logger.info("PollSurveyTask : insertPollMasterData");
            String pollMasterQuery = "INSERT INTO POLL_MASTER (POLL_ID, LANG, QUESTION, START_DATE, END_DATE, PERSONA) VALUES (?, ?, ?, ?, ?, ?)";
            logger.info("insertPollMasterData pollMasterQuery : "+pollMasterQuery);
            connection.setAutoCommit(false);
            preparedStatement = connection.prepareStatement(pollMasterQuery);
            preparedStatement.setLong(1, Long.parseLong(pollsBO.getPollId()));
            preparedStatement.setString(2, pollsBO.getLang());
            preparedStatement.setString(3, pollsBO.getQuestion());
            preparedStatement.setDate(4, getDate(pollsBO.getStartDate()));
            preparedStatement.setDate(5, getDate(pollsBO.getEndDate()));
            preparedStatement.setString(6, pollsBO.getPersona());
            result = preparedStatement.executeUpdate();
            logger.info("insertPollMasterData result : "+result);
        } catch (NumberFormatException | SQLException e) {
            logger.error("Exception in insertPollMasterData: "+e.getMessage());
            e.printStackTrace();
            throw e;
        }finally {
            Postgre.releaseConnection(null, preparedStatement, null);
            logger.info("Released insertPollMasterData connection");
        }
	    return result;
	}
	
	public boolean insertPollOptionsData(PollsBO pollsBO, Document document, Connection connection) throws SQLException {
        PreparedStatement preparedStatement = null;
        boolean isPollOptionInserted = false;
        try {
            logger.info("PollSurveyTask : insertPollOptionsData");
            String pollOptionQuery = "INSERT INTO POLL_OPTION (OPTION_ID, LANG, POLL_ID, OPTION_LABEL, OPTION_VALUE) VALUES(?, ?, ?, ?, ?)";
            preparedStatement = connection.prepareStatement(pollOptionQuery);
            logger.info("insertPollOptionsData pollOptionQuery : "+pollOptionQuery);
            
            List<Node> nodes = document.selectNodes(OPTION_PATH);
            long optionId = 1l;
            for (Node node : nodes) {
                logger.info("insertPollOptionsData optionId : "+optionId);
                String label = node.selectSingleNode(OPTION_LABEL).getText();
                String value = node.selectSingleNode(OPTION_VALUE).getText();
                logger.info("insertPollOptionsData label : "+label);
                logger.info("insertPollOptionsData value : "+value);
                preparedStatement.setLong(1, optionId);
                preparedStatement.setString(2, pollsBO.getLang());
                preparedStatement.setLong(3, Long.parseLong(pollsBO.getPollId()));
                preparedStatement.setString(4, label);
                preparedStatement.setString(5, value);
                preparedStatement.addBatch();
                optionId++;
            }
            int[] optionBatch = preparedStatement.executeBatch();
            logger.info("insertPollOptionsData optionBatch length : "+optionBatch.length);
            Postgre.releaseConnection(null, preparedStatement, null);
            
            if(optionBatch.length == optionId-1) {
                isPollOptionInserted = true;
            }
        } catch (NumberFormatException | SQLException e) {
            logger.error("Exception in insertPollOptionsData: "+e.getMessage());
            e.printStackTrace();
            throw e;
        }finally {
            Postgre.releaseConnection(null, preparedStatement, null);
            logger.info("Released insertPollOptionsData connection");
        }
        return isPollOptionInserted;
    }
	
	public boolean updatePollMasterData(Document document) {
		logger.debug("PollSurveyTask : updatePollMasterData");
		PreparedStatement prepareStatement = null;
		PreparedStatement prepareStatementPollOption = null;
		Connection connection = null;
		boolean isPollDataInserted = false;
		try {
			
			connection = postgre.getConnection();
			logger.info("updatePollMasterData Connection : "+connection);
			
			Long pollId = Long.parseLong(getDCRValue(document, ID_PATH));
			String lang = getDCRValue(document, LANG_PATH);
			String question = getDCRValue(document, QUESTION_PATH);
			String endDateStr = getDCRValue(document, POLL_END_DATE_PATH);
			String persona = getDCRValue(document, PERSONA_PATH);
			
			Date endDate = getDate(endDateStr);
			
			String pollMasterQuery = "UPDATE POLL_MASTER SET QUESTION = ?, END_DATE = ?, PERSONA = ? WHERE POLL_ID = ? AND LANG = ?";
			logger.info("updatePollMasterData pollMasterQuery : "+pollMasterQuery);
			connection.setAutoCommit(false);
			prepareStatement = connection.prepareStatement(pollMasterQuery);
			prepareStatement.setString(1, question);
			prepareStatement.setDate(2, endDate);
			prepareStatement.setString(3, persona);
		    prepareStatement.setLong(4, pollId);
		    prepareStatement.setString(5, lang);
		    
		    int result = prepareStatement.executeUpdate();
		    logger.info("updatePollMasterData result : "+result);
		    if (result > 0) {
		    	logger.info("Poll Master Data Updated");
		    	
		    	String pollOptionQuery = "UPDATE POLL_OPTION SET OPTION_LABEL = ? WHERE OPTION_ID = ? AND POLL_ID = ? AND LANG = ?";
		    	prepareStatementPollOption = connection.prepareStatement(pollOptionQuery);
		    	logger.info("updatePollMasterData pollOptionQuery : "+pollOptionQuery);
		    	
		    	List<Node> nodes = document.selectNodes(OPTION_PATH);
		    	long optionId = 1l;
		    	for (Node node : nodes) {
		    		logger.info("updatePollMasterData optionId : "+optionId);
		    		String label = node.selectSingleNode(OPTION_LABEL).getText();
		    		logger.info("updatePollMasterData label : "+label);
		    		prepareStatementPollOption.setString(1, label);
		    		prepareStatementPollOption.setLong(2, optionId);
		    		prepareStatementPollOption.setLong(3, pollId);
		    		prepareStatementPollOption.setString(4, lang);
		    		prepareStatementPollOption.addBatch();
		            optionId++;
		    	}
		    	int[] optionBatch = prepareStatementPollOption.executeBatch();
		    	logger.info("updatePollMasterData optionBatch length : "+optionBatch.length);
		    	
		    	if(optionBatch.length == optionId-1) {
	    			connection.commit();
		    		logger.info("Poll Option Updated");
		    		isPollDataInserted = true;
		    	}else {
	    			connection.rollback();
		    		logger.info("updatePollMasterData Option batch update failed");
		    	}
		    } else {
	    		connection.rollback();
		    	logger.info("Poll master update failed");
		    }
			
		} catch (Exception e) {
			try {
				if(connection != null) {
					connection.rollback();
				}
				logger.error("Exception in updatePollMasterData: "+e.getMessage());
			    e.printStackTrace();
			} catch (SQLException ex) {
				logger.error("Exception in updatePollMasterData rollback catch block : "+ex.getMessage());
				ex.printStackTrace();
			}
		} finally {
		    Postgre.releaseConnection(null, prepareStatementPollOption, null);
		    Postgre.releaseConnection(connection, prepareStatement, null);
		    logger.info("Released updatePollMasterData connection");
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
			
			Long surveyId = Long.parseLong(getDCRValue(document, ID_PATH));
			String lang = getDCRValue(document, LANG_PATH);
			
			String surveyMasterQuery = "SELECT COUNT(*) FROM SURVEY_MASTER WHERE SURVEY_ID = ? AND LANG = ?";
			logger.info("surveyMasterQuery isSurveyMasterDataAvailable : "+surveyMasterQuery);
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
		    logger.error("Exception in isSurveyMasterDataAvailable: "+e.getMessage());
		    e.printStackTrace();
		} finally {
		    Postgre.releaseConnection(connection, prepareStatement, rs);
		    logger.info("isSurveyMasterDataAvailable Released connection");
		}
		return isSurveyDataAvailable;
	}
	
	public boolean insertSurveyData(Document document) {
		logger.debug("PollSurveyTask : insertSurveyData");
		PreparedStatement prepareStatement = null;
		PreparedStatement prepareStatementSurveyQuestion = null;
		PreparedStatement optionPrepareStatement = null;
		Statement questionIdQueryStmt = null;
		Connection connection = null;
		ResultSet rs = null;
		boolean isSurveyDataInserted = false;
		try {
			
			connection = postgre.getConnection();
			
			/*Long surveyId = Long.parseLong(getDCRValue(document, ID_PATH));
			String lang = getDCRValue(document, LANG_PATH);
			String title = getDCRValue(document, TITLE_PATH);
			String description = getDCRValue(document, DESCRIPTION_PATH);
			String startDateStr = getDCRValue(document, SURVEY_START_DATE_PATH);
			String endDateStr = getDCRValue(document, SURVEY_END_DATE_PATH);
			String persona = getDCRValue(document, PERSONA_PATH);
			
			Date startDate = getDate(startDateStr);
			Date endDate = getDate(endDateStr);*/
			
			SurveyBO surveyBO = new SurveyBO();
			surveyBO.setSurveyId(getDCRValue(document, ID_PATH));
			surveyBO.setLang(getDCRValue(document, LANG_PATH));
			surveyBO.setTitle(getDCRValue(document, TITLE_PATH));
			surveyBO.setDescription(getDCRValue(document, SURVEY_START_DATE_PATH));
			surveyBO.setStartDate(getDCRValue(document, SURVEY_START_DATE_PATH));
			surveyBO.setEndDate(getDCRValue(document, SURVEY_END_DATE_PATH));
			surveyBO.setPersona(getDCRValue(document, PERSONA_PATH));
			
			String surveyMasterQuery = "INSERT INTO SURVEY_MASTER (SURVEY_ID, LANG, SURVEY_TITLE, SURVEY_DESCRIPTION, START_DATE, END_DATE, PERSONA) VALUES (?, ?, ?, ?, ?, ?, ?)";
			logger.info("insertSurveyData surveyMasterQuery : "+surveyMasterQuery);
			connection.setAutoCommit(false);
			prepareStatement = connection.prepareStatement(surveyMasterQuery);
		    prepareStatement.setLong(1, Long.parseLong(surveyBO.getSurveyId()));
		    prepareStatement.setString(2, surveyBO.getLang());
		    prepareStatement.setString(3, surveyBO.getTitle());
		    prepareStatement.setString(4, surveyBO.getDescription());
		    prepareStatement.setDate(5, getDate(surveyBO.getStartDate()));
		    prepareStatement.setDate(6, getDate(surveyBO.getEndDate()));
		    prepareStatement.setString(7, surveyBO.getPersona());
		    int result = prepareStatement.executeUpdate();
		    logger.info("result : "+result);
		    if (result > 0) {
		    	logger.info("Survey Master Data Inserted");
		    	
		    	String surveyQuestionQuery = "INSERT INTO SURVEY_QUESTION (QUESTION_ID, SURVEY_ID, LANG, QUESTION_NO, QUESTION_TYPE, QUESTION) VALUES (?, ?, ?, ?, ?, ?)";
		    	prepareStatementSurveyQuestion = connection.prepareStatement(surveyQuestionQuery);
		    	logger.info("insertSurveyData surveyQuestionQuery : "+surveyQuestionQuery);
		    	
		    	List<Node> nodes = document.selectNodes(FIELD_PATH);
		    	int questionNo = 1;
		    	for (Node node : nodes) {
		    		logger.info("insertSurveyData questionNo : "+questionNo);
		    		
		    		String questionType = node.selectSingleNode(".//field-type").getText();
		    		logger.info("insertSurveyData questionType : "+questionType);
		    		
		    		if(!"button".equalsIgnoreCase(questionType)) {
		    			
		    		    Long questionId = getNextSequenceValue("survey_question_question_id_seq", postgre.getConnection());
		            	logger.info("insertSurveyData questionId : "+questionId);
			    		
			    		
			    		String question = node.selectSingleNode(".//question").getText();
			    		logger.info("question : "+question);
			    		prepareStatementSurveyQuestion.setLong(1, questionId);
			    		prepareStatementSurveyQuestion.setLong(2, Long.parseLong(surveyBO.getSurveyId()));
			    		prepareStatementSurveyQuestion.setString(3, surveyBO.getLang());
			    		prepareStatementSurveyQuestion.setInt(4, questionNo);
			    		prepareStatementSurveyQuestion.setString(5, questionType);
			            prepareStatementSurveyQuestion.setString(6, question);
			            int questionResult = prepareStatementSurveyQuestion.executeUpdate();
			            
			            if (questionResult > 0) {
			            	logger.info("Survey Question Inserted");
			            	
				            String surveyOptionQuery = "INSERT INTO SURVERY_OPTION (OPTION_ID, SURVEY_ID, LANG, QUESTION_ID, QUESTION_NO, OPTION_NO, OPTION_LABEL, OPTION_VALUE) VALUES (nextval('survery_option_option_id_seq'), ?, ?, ?, ?, ?, ?, ?)";
				            optionPrepareStatement = connection.prepareStatement(surveyOptionQuery);
				            logger.info("surveyOptionQuery : "+surveyOptionQuery);
				            List<Node> optionNodes = node.selectNodes(".//option");
				            int optionNo = 1;
				            for (Node optnode : optionNodes) {
				            	logger.info("optionNo : "+optionNo);
					    		String optionLabel = optnode.selectSingleNode(OPTION_LABEL).getText();
					    		String optionValue = optnode.selectSingleNode(OPTION_VALUE).getText();
					    		logger.info(optionLabel+" : "+optionValue);
					    		optionPrepareStatement.setLong(1, Long.parseLong(surveyBO.getSurveyId()));
					    		optionPrepareStatement.setString(2, surveyBO.getLang());
					    		optionPrepareStatement.setLong(3, questionId);
					    		optionPrepareStatement.setInt(4, questionNo);
					    		optionPrepareStatement.setInt(5, optionNo);
					    		optionPrepareStatement.setString(6, optionLabel);
					    		optionPrepareStatement.setString(7, optionValue);
					    		optionPrepareStatement.addBatch();
					    		optionNo++;
					    	}
				            int[] optionBatch = optionPrepareStatement.executeBatch();
				            logger.info("insertSurveyData optionBatch length : "+optionBatch.length);
					    	
					    	if(optionBatch.length == optionNo-1) {
					    		logger.info("Survey Option Inserted");
					    	}else {
				    			connection.rollback();
					    		logger.info("insertSurveyData Option batch insert failed");
					    		break;
					    	}
					    	questionNo++;
			            }else {
		            		connection.rollback();
				    		logger.info("insertSurveyData Question insert failed");
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
				if(connection != null) {
					connection.rollback();
				}
				logger.error("Exception in insertSurveyData : "+e.getMessage());
			    e.printStackTrace();
			} catch (SQLException ex) {
				logger.error("Exception in insertSurveyData rollback catch block : "+ex.getMessage());
				ex.printStackTrace();
			}
		} finally {
		    Postgre.releaseConnection(null, prepareStatementSurveyQuestion, null);
		    Postgre.releaseConnection(null, questionIdQueryStmt, rs);
		    Postgre.releaseConnection(null, optionPrepareStatement, null);
		    Postgre.releaseConnection(connection, prepareStatement, null);
		    logger.info("Released insertSurveyData connection");
		}
		return isSurveyDataInserted;
	}
	
	public int insertSurveyMasterData(PollsBO pollsBO, Connection connection) throws SQLException{
        PreparedStatement preparedStatement = null;
        int result = 0;
        try {
            logger.info("PollSurveyTask : insertPollMasterData");
            String pollMasterQuery = "INSERT INTO POLL_MASTER (POLL_ID, LANG, QUESTION, START_DATE, END_DATE, PERSONA) VALUES (?, ?, ?, ?, ?, ?)";
            logger.info("insertPollMasterData pollMasterQuery : "+pollMasterQuery);
            connection.setAutoCommit(false);
            preparedStatement = connection.prepareStatement(pollMasterQuery);
            preparedStatement.setLong(1, Long.parseLong(pollsBO.getPollId()));
            preparedStatement.setString(2, pollsBO.getLang());
            preparedStatement.setString(3, pollsBO.getQuestion());
            preparedStatement.setDate(4, getDate(pollsBO.getStartDate()));
            preparedStatement.setDate(5, getDate(pollsBO.getEndDate()));
            preparedStatement.setString(6, pollsBO.getPersona());
            result = preparedStatement.executeUpdate();
            logger.info("insertPollMasterData result : "+result);
        } catch (NumberFormatException | SQLException e) {
            logger.error("Exception in insertPollMasterData: "+e.getMessage());
            e.printStackTrace();
            throw e;
        }finally {
            Postgre.releaseConnection(null, preparedStatement, null);
            logger.info("Released insertPollMasterData connection");
        }
        return result;
    }
	
	
	public boolean updateSurveyMasterData(Document document) {
		logger.debug("PollSurveyTask : updateSurveyMasterData");
		PreparedStatement prepareStatement = null;
		PreparedStatement prepareStatementSurveyQuestion = null;
		PreparedStatement optionPrepareStatement = null;
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
			logger.info("updateSurveyMasterData surveyMasterQuery : "+surveyMasterQuery);
			connection.setAutoCommit(false);
			prepareStatement = connection.prepareStatement(surveyMasterQuery);
			prepareStatement.setString(1, title);
		    prepareStatement.setString(2, description);
		    prepareStatement.setDate(3, endDate);
		    prepareStatement.setString(4, persona);
		    prepareStatement.setLong(5, surveyId);
		    prepareStatement.setString(6, lang);
		    
		    int result = prepareStatement.executeUpdate();
		    logger.info("updateSurveyMasterData result : "+result);
		    if (result > 0) {
		    	logger.info("Survey Master Data Updated");
		    	
		    	String surveyQuestionQuery = "UPDATE SURVEY_QUESTION SET QUESTION = ? WHERE SURVEY_ID = ? AND LANG = ? AND QUESTION_NO = ?";
		    	prepareStatementSurveyQuestion = connection.prepareStatement(surveyQuestionQuery);
		    	logger.info("surveyQuestionQuery : "+surveyQuestionQuery);
		    	
		    	List<Node> nodes = document.selectNodes(FIELD_PATH);
		    	int questionNo = 1;
		    	for (Node node : nodes) {
		    		logger.info("updateSurveyMasterData questionNo : "+questionNo);
		    		
		    		String questionType = node.selectSingleNode(".//field-type").getText();
		    		logger.info("updateSurveyMasterData questionType : "+questionType);
		    		
		    		if(!"button".equalsIgnoreCase(questionType)) {
		    			
			    		String question = node.selectSingleNode(".//question").getText();
			    		logger.info("question : "+question);
			    		prepareStatementSurveyQuestion.setString(1, question);
			    		prepareStatementSurveyQuestion.setLong(2, surveyId);
			    		prepareStatementSurveyQuestion.setString(3, lang);
			    		prepareStatementSurveyQuestion.setInt(4, questionNo);			            
			            int questionResult = prepareStatementSurveyQuestion.executeUpdate();
			            
			            if (questionResult > 0) {
			            	logger.info("Survey Question Inserted");
			            	
				            String surveyOptionQuery = "UPDATE SURVERY_OPTION SET OPTION_LABEL = ?, OPTION_VALUE = ? WHERE SURVEY_ID = ? AND LANG = ? AND QUESTION_NO = ? AND OPTION_NO = ?";
				            optionPrepareStatement = connection.prepareStatement(surveyOptionQuery);
				            logger.info("updateSurveyMasterData surveyOptionQuery : "+surveyOptionQuery);
				            List<Node> optionNodes = node.selectNodes(".//option");
				            int optionNo = 1;
				            for (Node optnode : optionNodes) {
				            	logger.info("updateSurveyMasterData optionNo : "+optionNo);
					    		String optionLabel = optnode.selectSingleNode(OPTION_LABEL).getText();
					    		String optionValue = optnode.selectSingleNode(OPTION_VALUE).getText();
					    		logger.info(optionLabel+" : "+optionValue);
					    		optionPrepareStatement.setString(1, optionLabel);
					    		optionPrepareStatement.setString(2, optionValue);
					    		optionPrepareStatement.setLong(3, surveyId);
					    		optionPrepareStatement.setString(4, lang);
					    		optionPrepareStatement.setInt(5, questionNo);
					    		optionPrepareStatement.setInt(6, optionNo);
					    		optionPrepareStatement.addBatch();
					    		optionNo++;
					    	}
				            int[] optionBatch = optionPrepareStatement.executeBatch();
				            logger.info("updateSurveyMasterData optionBatch length : "+optionBatch.length);
					    	
					    	if(optionBatch.length == optionNo-1) {
					    		logger.info("Survey Option Updated");
					    	}else {
					    		if(connection != null) {
					    			connection.rollback();
					    		}
					    		logger.info("updateSurveyMasterData Option batch update failed");
					    		break;
					    	}
					    	questionNo++;
			            }else {
			            	if(connection != null) {
			            		connection.rollback();
			            	}
				    		logger.info("updateSurveyMasterData Question update failed");
				    		break;
				    	}
		    		}
		    	}
		    	isSurveyDataUpdated = true;
		    } else {
		    	if(connection != null) {
		    		connection.rollback();
		    	}
		    	logger.info("Survey master update failed");
		    }
		    
		    if(isSurveyDataUpdated) {
		    	if(connection != null) {
		    		connection.commit();
		    	}
		    	logger.info("Survey update transaction committed");
		    }
			
		} catch (Exception e) {
			try {
				if(connection != null) {
					connection.rollback();
				}
				logger.error("Exception in updateSurveyMasterData : "+e.getMessage());
			    e.printStackTrace();
			} catch (SQLException ex) {
				logger.error("Exception in updateSurveyMasterData rollback catch block : "+ex.getMessage());
				ex.printStackTrace();
			}
		} finally {
			Postgre.releaseConnection(null, prepareStatementSurveyQuestion, null);
		    Postgre.releaseConnection(null, optionPrepareStatement, null);
		    Postgre.releaseConnection(connection, prepareStatement, rs);
		    logger.info("Released updateSurveyMasterData connection");
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
		String[] dateArr = inputDate.split(" ");
		Date outDate = java.sql.Date.valueOf(dateArr[0]);
		logger.info(inputDate+" >>> "+outDate);
		return outDate;
	}
	
    private Long getNextSequenceValue(String sequenceName,
            Connection connection) {
        Long seqValue = 0L;
        Statement queryStmt = null;
        ResultSet rs = null;
        try {
            String query = "SELECT nextval('" + sequenceName
                    + "') as seqValue";
            queryStmt = connection.createStatement();
            rs = queryStmt.executeQuery(query);
            while (rs.next()) {
                seqValue = rs.getLong("seqValue");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            Postgre.releaseConnection(connection, queryStmt, rs);
        }
        return seqValue;
    }
	
}
