package com.hukoomi.livesite.external;

import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;

import com.hukoomi.bo.PollsBO;
import com.hukoomi.bo.SurveyBO;
import com.hukoomi.livesite.solr.SolrQueryBuilder;
import com.hukoomi.utils.Postgre;
import com.hukoomi.utils.SolrQueryUtil;
import com.interwoven.livesite.runtime.RequestContext;
/**
 * PollsSurveyExternal is the components external class.
 * 
 * @author Vijayaragavamoorthy
 *
 */
public class PollSurveyExternal {
    /** Logger object to check the flow of the code. */
    private final Logger logger = Logger
            .getLogger(PollSurveyExternal.class);
    /**
     * Constant for Content. 
     */
    private static final String CONTENT = "Content";
    /**
     * Constant for Poll node. 
     */
    private static final String POLL_NODE = "/content/root/detail/polls";
    /**
     * Constant for Survey node. 
     */
    private static final String SURVEY_NODE = "/content/root/details/survey";
    /**
     * Constant for Id node. 
     */
    private static final String ID_NODE = "/content/root/information/id";
    /**
     * Postgre object variable.
     */
    Postgre postgre = null;

    
    /**
     * This method will be called from Component External for solr Content fetching.
     * 
     * @param context Request Context object.
     * 
     * @return doc Returns the solr response document generated from solr query. 
     * 
     * @deprecated
     */
    @Deprecated(since = "", forRemoval = false)
    public Document getContent(final RequestContext context) {
        logger.debug("PollSurveyExternal : getContent");
        Document doc = null;
        PollsExternal pollsExt = new PollsExternal();
        try {
            postgre = new Postgre(context);
            PollsBO pollsBO = pollsExt.setBO(context);
            logger.info("PollsBO : " + pollsBO);

            if ("vote".equalsIgnoreCase(pollsBO.getAction())) {

                pollsExt.insertPollResponse(pollsBO,
                        postgre.getConnection());

                // Fetch Result from DB for above poll_ids which were voted already by user
                Map<String, List<Map<String, String>>> response = pollsExt
                        .getPollResponse(pollsBO, postgre.getConnection());

                doc = pollsExt.createPollResultDoc(pollsBO, response);
            } else {
                doc = getGroupData(context);
            }
            logger.info("Final Document :" + doc.asXML());
        } catch (Exception e) {
            logger.error("Exception  in  getContent", e);
        }

        return doc;
    }

    
    /**
     * This method is used to create document containing Polls and Survey data.
     * 
     * @param context Request Context object.
     * 
     * @return doc Returns final document containing Polls and Survey data.
     * 
     * @deprecated
     */
    @Deprecated(since = "", forRemoval = false)
    public Document getGroupData(RequestContext context) {
        Document doc = null;
        Document pollDoc = null;
        try {
            PollsExternal pollsExt = new PollsExternal();
            SurveyExternal surveyExt = new SurveyExternal();
            PollsBO pollsBO = pollsExt.setBO(context);
            logger.info("PollsBO : " + pollsBO);
            SurveyBO surveyBO = surveyExt.setBO(context);
            logger.debug("SurveyBO : " + surveyBO);

            String solarCore = "portal-en";
            if ("ar".equalsIgnoreCase(pollsBO.getLang())) {
                solarCore = "portal-ar";
            }

            logger.info("\nlang : " + pollsBO.getLang() + "\nipAddress : "
                    + pollsBO.getIpAddress() + "\nuserId : "
                    + pollsBO.getUserId() + "\npollsGroup : "
                    + pollsBO.getGroup() + "\nsurveyGroup : "
                    + surveyBO.getGroup());

            doc = DocumentHelper.createDocument();
            Element pollSurveyElem = doc.addElement("PollSurveyesponse");
            Element pollGroupElem = null;
            Element surveyGroupElem = null;

            // PollGroup Processing
            String pollGroupName = getContentName(pollsBO.getGroup());
            logger.info("pollGroupName : " + pollGroupName);
            Document document = fetchGroupDoc(context, CONTENT,
                    pollsBO.getGroupCategory(), pollsBO.getLang(),
                    pollGroupName);
            logger.info("Polls Group Doc :" + document.asXML());

            String pollIds = fetchIds(document, POLL_NODE, context,
                    pollsBO.getCategory(), pollsBO.getLang());
            logger.info("pollIds : " + pollIds);
            if (pollIds != null && !"".equals(pollIds.trim())) {

                Document pollsSolrDoc = fetchDocument(context, pollIds,
                        pollsBO.getSolrCategory(), solarCore);
                logger.info("pollsSolrDoc : " + pollsSolrDoc.asXML());

                String activePollIds = pollsExt
                        .getPollIdSFromDoc(pollsSolrDoc);
                logger.info("activePollIds : " + activePollIds);

                if (activePollIds != null && !"".equals(activePollIds.trim())) {
                    pollGroupElem = pollSurveyElem
                            .addElement("PollGroupResponse");
                    pollsBO.setPollId(activePollIds);

                    String votedPollIds = pollsExt.checkResponseData(
                            pollsBO, postgre.getConnection());
                    Map<String, List<Map<String, String>>> response = null;
                    if (votedPollIds != null && !"".equals(votedPollIds.trim())) {
                        pollsBO.setPollId(votedPollIds);
                        response = pollsExt.getPollResponse(pollsBO,
                                postgre.getConnection());
                    }

                    pollDoc = pollsExt.addResultToXml(pollsSolrDoc,
                            response);
                    pollGroupElem.add(pollDoc.getRootElement());
                    logger.info(
                            "Final Document - Poll Doc : " + doc.asXML());
                }
            }

            // SurveyGroup Processing
            String surveyGroupName = getContentName(surveyBO.getGroup());
            logger.info("surveyGroupName : " + surveyGroupName);
            document = fetchGroupDoc(context, CONTENT,
                    surveyBO.getGroupCategory(), surveyBO.getLang(),
                    surveyGroupName);
            logger.info("Survey Group Doc :" + document.asXML());

            String surveyIds = fetchIds(document, SURVEY_NODE, context,
                    surveyBO.getCategory(), surveyBO.getLang());
            logger.info("surveyIds : " + surveyIds);
            if (surveyIds != null && !"".equals(surveyIds.trim())) {
                Document surveySolrDoc = fetchDocument(context, surveyIds,
                        surveyBO.getSolrCategory(), solarCore);
                logger.info("surveySolrDoc : " + surveySolrDoc.asXML());
                surveyGroupElem = pollSurveyElem
                        .addElement("SurveyGroupResponse");
                surveyGroupElem.add(surveySolrDoc.getRootElement());
            }

            logger.info("Final Document  : " + doc.asXML());
        } catch (Exception e) {
            logger.error("Exception in getGroupData",  e);
        }

        return doc;
    }

    /**
     * This method is used to fetch solr document.
     * 
     * @param context Request Context object.
     * 
     * @return doc Returns solr document.
     */
    public Document fetchDocument(RequestContext context, String ids,
            String solrcategory, String solrCore) {
        logger.info("PollSurveyExternal : fetchDocument");
        Document doc = null;
        SolrQueryUtil squ = new SolrQueryUtil();
        logger.info("\nids : " + ids + "\nsolrcategory : " + solrcategory
                + "\nsolrCore : " + solrCore);

        context.setParameterString("fieldQuery", "");
        context.setParameterString("solrCore", solrCore);

        SolrQueryBuilder sqb = new SolrQueryBuilder(context);
        sqb.addFieldQuery("category:" + solrcategory
                + "&fq=end-date:[NOW TO *]&fq=id:(" + ids + ")");
        String query = sqb.build();
        logger.debug("SQB Query : " + query);

        doc = squ.doJsonQuery(query, "SolrResponse");
        return doc;
    }
    
    /**
     * This method is used to fetch group document.
     * 
     * @param context Request Context object.
     * 
     * @return doc Returns group document.
     */
    public Document fetchGroupDoc(RequestContext context,
            String dcrcategory, String category, String locale,
            String record) {
        logger.info("PollSurveyExternal : fetchGroupDoc");
        Document doc = null;
        logger.info("\ndcrcategory : " + dcrcategory + "\ncategory : "
                + category + "\nlocal : " + locale + "\nrecord : "
                + record);
        DetailExternal detailExt = new DetailExternal();
        context.setParameterString("dcrcategory", dcrcategory);
        context.setParameterString("category", category);
        context.setParameterString("locale", locale);
        context.setParameterString("record", record);
        doc = detailExt.getContentDetail(context);
        return doc;
    }

    /**
     * This method is used to get Content name.
     * 
     * @param context Request Context object.
     * 
     * @return Returns Content name.
     */
    public String getContentName(String contentPath) {
        String[] contentPathArr = contentPath.split("/");
        return contentPathArr[contentPathArr.length - 1];
    }

    /**
     * This method is used to fetch ids from document.
     * 
     * @param doc Document object.
     * @param nodeInput Node Input.
     * @param context Request Context object.
     * @param category Catgory name.
     * @param locale Locale.
     * 
     * @return Returns ids.
     */
    @SuppressWarnings("unchecked")
    public String fetchIds(Document doc, String nodeInput,
            RequestContext context, String category, String locale) {
        logger.info("PollSurveyExternal : fetchIds");
        StringJoiner contentIds = new StringJoiner(" ");
        logger.info("\nnodeInput : " + nodeInput + "\ncategory : "
                + category + "\nlocal : " + locale);
        try {
            List<Node> nodes = doc.selectNodes(nodeInput);
            logger.info("nodes size : " + nodes.size());
            for (Node node : nodes) {

                String contentPath = node.getText();
                logger.info("contentPath : " + contentPath);
                String contentName = getContentName(contentPath);
                logger.info("contentName : " + contentName);

                Document document = fetchGroupDoc(context, CONTENT,
                        category, locale, contentName);

                String id = document.selectSingleNode(ID_NODE).getText();
                contentIds.add(id);
            }
        } catch (Exception e) {
            logger.error("Exception in fetchIds", e);
        }
        return contentIds.toString();

    }

    /**
     * This method is used to get ids from document.
     * 
     * @param doc Document object.
     * @param nodePath Node path.
     * 
     * @return Returns ids.
     */
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
