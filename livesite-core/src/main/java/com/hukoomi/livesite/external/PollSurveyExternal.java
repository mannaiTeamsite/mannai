package com.hukoomi.livesite.external;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringJoiner;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;

import com.hukoomi.livesite.solr.SolrQueryBuilder;
import com.hukoomi.utils.Postgre;
import com.hukoomi.utils.SolrQueryUtil;
import com.interwoven.livesite.runtime.RequestContext;

public class PollSurveyExternal {
	/** Logger object to check the flow of the code.*/
	private final Logger logger = Logger.getLogger(PollSurveyExternal.class);
	private final String CONTENT = "Content";
	private final String POLL_GROUP_CATEGORY = "Polls-Groups";
	private final String SURVEY_GROUP_CATEGORY = "Survey-Group";
	private final String POLL_CATEGORY = "Polls";
	private final String SURVEY_CATEGORY = "Survey";
	private final String SOLR_POLL_CATEGORY = "polls";
	private final String SOLR_SURVEY_CATEGORY = "survey";
	private final String POLL_NODE = "/content/root/detail/polls";
	private final String SURVEY_NODE = "/content/root/details/survey";
	private final String ID_NODE = "/content/root/information/id";
	private final String START = "0";
	private final String ROW = "100";
	Postgre postgre =  null;

	public Document getContent(final RequestContext context) {
		logger.debug("PollSurveyExternal : getContent");
		Document doc = null;
		String lang = null;
		PollsExternal pollsExt = new PollsExternal();
		try {
			postgre =  new Postgre(context);
			String pollAction = context.getParameterString("pollAction");
			
			if ("vote".equalsIgnoreCase(pollAction)) {
				//TO DO: We can call the poll external method, but we need declare the variable lang outside a conditional block 
				//and initialize it in each conditional block where it is required.
				//doc = pollsExt.performPollAction(context);
				
				HttpServletRequest request = context.getRequest();
		        Locale locale = request.getLocale();
		        
				String pollId = context.getParameterString("pollId");
				String option = context.getParameterString("option");
				String ipAddress = context.getRequest().getRemoteAddr();
				String userId = context.getParameterString("user_id");
                //userId = "test_user";
                String userAgent = context.getRequest().getHeader("User-Agent");
                String votedFrom = context.getParameterString("votedFrom");
                lang = locale.getLanguage();

                logger.info("poll_id:" + pollId + "&option:" + option
                        + "&lang:" + lang + "&user_id:" + userId
                        + "&RemoteAddr:" + ipAddress + "&UserAgent:"
                        + userAgent + "&voted_from:" + votedFrom);
                // int optionId = getOptionId(pollId, lang, option);
                // logger.info(optionId);

                pollsExt.insertPollResponse(lang, pollId, option, userId, ipAddress,
                        userAgent, votedFrom, postgre.getConnection());

                // Fetch Result from DB for above poll_ids which were voted already by user
                Map<String, List<Map<String, String>>> response = pollsExt.getPollResponse(pollId, lang, postgre.getConnection());

                doc = pollsExt.createPollResultDoc(pollId, response);
			}else {
				doc = getGroupData(context, pollsExt);
			}
			logger.info("Final Document :" + doc.asXML());
		} catch (Exception e) {
			logger.error("Exception  : " + e.getMessage());
			e.printStackTrace();
		}
        
        return doc;
	}
	
	public Document getGroupData(RequestContext context, PollsExternal pollsExt) {
		Document doc = null;
		Document pollDoc = null;
		try {
			HttpServletRequest request = context.getRequest();
			Locale locale = request.getLocale();
			String lang = locale.getLanguage();
			String ipAddress = context.getRequest().getRemoteAddr();
			String  userId = context.getParameterString("user_id");
			//String userId = "test_user";
			//String userAgent = context.getRequest().getHeader("User-Agent");
			
			String pollsGroup = context.getParameterString("PollsGroup");
			String surveyGroup = context.getParameterString("SurveyGroup");
			
			String solarCore = "portal-en";
			if("ar".equalsIgnoreCase(lang)) {
				solarCore = "portal-ar";
			}
			
			logger.info("\nlang : " + lang+"\nipAddress : " + ipAddress+"\nuserId : " + userId+"\npollsGroup : " + pollsGroup+"\nsurveyGroup : " + surveyGroup);
			
			doc = DocumentHelper.createDocument();
			Element pollSurveyElem = doc.addElement("PollSurveyesponse");
			Element pollGroupElem = null;
			Element surveyGroupElem = null;
			
			//PollGroup Processing
			String pollGroupName = getContentName(pollsGroup);
			logger.info("pollGroupName : " + pollGroupName);
			Document document =  fetchGroupDoc(context, CONTENT, POLL_GROUP_CATEGORY, lang, pollGroupName);
			logger.info("Polls Group Doc :"+document.asXML());
			
			String pollIds = fetchIds(document, POLL_NODE, context, POLL_CATEGORY, lang);
			logger.info("pollIds : " + pollIds);
			if(pollIds != null && !"".equals(pollIds)) {
				
				Document pollsSolrDoc = fetchDocument(context, pollIds, SOLR_POLL_CATEGORY, START, ROW, solarCore);
				logger.info("pollsSolrDoc : " + pollsSolrDoc.asXML());
			
				String activePollIds = pollsExt.getPollIdSFromDoc(pollsSolrDoc);
				logger.info("activePollIds : " + activePollIds);
			
				if(activePollIds != null && !"".equals(activePollIds)) {
					pollGroupElem = pollSurveyElem.addElement("PollGroupResponse");
					
					String votedPollIds = pollsExt.checkResponseData(activePollIds, userId, ipAddress, postgre.getConnection());
					Map<String, List<Map<String, String>>> response = null;
					if(votedPollIds != null && !"".equals(votedPollIds)) {
						response = pollsExt.getPollResponse(votedPollIds, lang, postgre.getConnection());
					}

					//if(!response.isEmpty()) {
					pollDoc = pollsExt.addResultToXml(pollsSolrDoc, response);
					pollGroupElem.add(pollDoc.getRootElement());
					logger.info("Final Document - Poll Doc : " + doc.asXML());
					//}
				}
			}
			
			//SurveyGroup Processing
			String surveyGroupName =  getContentName(surveyGroup);
			logger.info("surveyGroupName : " + surveyGroupName);
			document =  fetchGroupDoc(context, CONTENT, SURVEY_GROUP_CATEGORY, lang, surveyGroupName);
			logger.info("Survey Group Doc :"+document.asXML());
  
			String surveyIds = fetchIds(document, SURVEY_NODE, context, SURVEY_CATEGORY, lang);
			logger.info("surveyIds : " + surveyIds);
			if(surveyIds != null && !"".equals(surveyIds)) {
				Document surveySolrDoc = fetchDocument(context, surveyIds, SOLR_SURVEY_CATEGORY, START, ROW, solarCore);
				logger.info("surveySolrDoc : " + surveySolrDoc.asXML());
				surveyGroupElem = pollSurveyElem.addElement("SurveyGroupResponse");
				surveyGroupElem.add(surveySolrDoc.getRootElement());
			}
			
			logger.info("Final Document  : " + doc.asXML());
		} catch (Exception e) {
			logger.error("Exception  : " + e.getMessage());
			e.printStackTrace();
		}
        
        return doc;
	}
	
	/*private Document fetchDocument(RequestContext context, String ids, String solrcategory, String start, String rows, String solrCore ) {
		 logger.info("PollSurveyExternal : fetchDocument");
		Document doc = null;
		logger.info("solrcategory : " + solrcategory+"\nstart : "+start+"\nrows : "+rows+"\nsolrCore : "+solrCore);
		HukoomiExternal hukoomiExt = new HukoomiExternal();
        context.setParameterString("solrcategory", solrcategory);
        context.setParameterString("fieldQuery", "end-date:[NOW TO *] AND id:("+ids+")");
        context.setParameterString("start", start);
        context.setParameterString("rows", rows);
        context.setParameterString("solrCore", solrCore);
        doc = hukoomiExt.getLandingContent(context);
		return doc;
	}*/
	
	public Document fetchDocument(RequestContext context, String ids, String solrcategory, String start, String rows, String solrCore ) {
		logger.info("PollSurveyExternal : fetchDocument");
		Document doc = null;
		SolrQueryUtil squ = new SolrQueryUtil();
		logger.info("\nids : " + ids+"\nsolrcategory : " + solrcategory+"\nstart : "+start+"\nrows : "+rows+"\nsolrCore : "+solrCore);
		
		context.setParameterString("fieldQuery", "");
		context.setParameterString("start", start);
		context.setParameterString("rows", rows);
		context.setParameterString("solrCore", solrCore);
		
		SolrQueryBuilder sqb = new SolrQueryBuilder(context);
		sqb.addFieldQuery("category:"+solrcategory+"&fq=end-date:[NOW TO *]&fq=id:("+ids+")");
		String query = sqb.build();
	    logger.debug("SQB Query : " + query);

	    doc = squ.doJsonQuery(query, "SolrResponse");
		return doc;
	}
	
	public Document fetchGroupDoc(RequestContext context, String dcrcategory, String category, String locale, String record) {
		logger.info("PollSurveyExternal : fetchGroupDoc");
		Document doc = null;
		logger.info("\ndcrcategory : " + dcrcategory+"\ncategory : "+category+"\nlocal : "+locale+"\nrecord : "+record);
		DetailExternal detailExt = new DetailExternal();
       context.setParameterString("dcrcategory", dcrcategory);
       context.setParameterString("category", category);
       context.setParameterString("locale", locale);
       context.setParameterString("record", record);
       doc = detailExt.getContentDetail(context);
		return doc;
	}
	
	
	public String getContentName(String contentPath) {
		String[] contentPathArr = contentPath.split("/");
        return contentPathArr[contentPathArr.length-1];
	}
	
	
	@SuppressWarnings("unchecked")
    public String fetchIds(Document doc, String nodeInput, RequestContext context, String category, String locale) {
        logger.info("PollSurveyExternal : fetchIds");
        StringJoiner contentIds = new StringJoiner(" ");
        logger.info("\nnodeInput : " + nodeInput+"\ncategory : "+category+"\nlocal : "+locale);
        try {
            List<Node> nodes = doc.selectNodes(nodeInput);
            logger.info("nodes size : " + nodes.size());
            for (Node node : nodes) {
            	
            	String contentPath = node.getText();
            	logger.info("contentPath : " + contentPath);
            	String contentName =  getContentName(contentPath);
            	logger.info("contentName : " + contentName);
            	
            	Document document =  fetchGroupDoc(context, CONTENT, category, locale, contentName);
                //logger.info("Content Doc :"+document.asXML());
            	
                String id = document.selectSingleNode(ID_NODE).getText();
            	contentIds.add(id);
            }
        } catch (Exception e) {
            logger.info("Exception : " + e.getMessage());
        }
        return contentIds.toString();

    }
	
	@SuppressWarnings("unchecked")
    public String getIdSFromDoc(Document doc, String nodePath) {
	 	logger.info("PollSurveyExternal : getIdSFromDoc");

        List<Node> nodes = doc.selectNodes(nodePath);
        StringJoiner joiner = new StringJoiner(",");

        for (Node node : nodes) {
            joiner.add(node.selectSingleNode("id").getText());
        }
        return joiner.toString();
    }
	
}
