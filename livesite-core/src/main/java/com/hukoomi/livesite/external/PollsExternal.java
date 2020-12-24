package com.hukoomi.livesite.external;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringJoiner;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;

import com.hukoomi.bo.PollsBO;
import com.hukoomi.utils.Postgre;
import com.hukoomi.utils.RequestHeaderUtils;
import com.interwoven.livesite.runtime.RequestContext;
/**
 * PollsExternal is the components external class.
 * 
 * @author Vijayaragavamoorthy
 *
 */
public class PollsExternal {

    /** Logger object to check the flow of the code. */
    private static final Logger logger = Logger
            .getLogger(PollsExternal.class);
    /** Default query to fetch all solr content. */
    public static final String DEFAULT_QUERY = "*:*";
    /**
     * Maximum current polls to be fethced.
     */
    public static final String MAX_CURRENT_POLLS_FETCH = "1000";
    /**
     * Constant for Result.
     */
    public static final String RESULT = "Result";
    /**
     * Constant for Poll Id.
     */
    public static final String POLL_ID = "POLL_ID";
    /**
     * Constant for Question.
     */
    public static final String QUESTION = "QUESTION";
    /**
     * Constant for Option Label.
     */
    public static final String OPTION_LABEL = "OPTION_LABEL";
    /**
     * Constant for Option Value.
     */
    public static final String OPTION_VALUE = "OPTION_VALUE";
    /**
     * Constant for Poll Count.
     */
    public static final String POLLCOUNT = "POLLCOUNT";
    /**
     * Constant for Total Response.
     */
    public static final String TOTALRESPONSE = "TOTALRESPONSE";
    /**
     * Constant for Poll Percentage.
     */
    public static final String POLLPCT = "POLLPCT";
    /**
     * Constant for BIGINT.
     */
    public static final String BIGINT = "BIGINT";
    /**
     * Postgre Object variable.
     */
    Postgre postgre = null;

    /**
     * This method will be called from Component External for solr Content fetching.
     * 
     * @param context Request context object.
     *
     * @return doc Returns the solr response document generated from solr query.
     * 
     * @deprecated
     */
    @Deprecated(since = "", forRemoval = false)
    public Document performPollAction(final RequestContext context) {

        logger.info("PollsExternal : performPollAction()");

        Document doc = null;
        HukoomiExternal he = new HukoomiExternal();

        PollsBO pollsBO = setBO(context);
        logger.info("PollsBO : " + pollsBO);
        postgre = new Postgre(context);

        if (pollsBO.getAction() != null) {
            if ("vote".equalsIgnoreCase(pollsBO.getAction())) {

                insertPollResponse(pollsBO, postgre);

                // Fetch Result from DB for above poll_ids which were voted already by user
                Map<String, List<Map<String, String>>> response = getPollResponse(
                        pollsBO, postgre);

                doc = createPollResultDoc(pollsBO, response);
                logger.info("Final Result - vote :" + doc.asXML());
            } else if ("current".equalsIgnoreCase(pollsBO.getAction())) {

                context.setParameterString("rows",
                        MAX_CURRENT_POLLS_FETCH);

                doc = he.getLandingContent(context);

                logger.info("current doc : " + doc.asXML());

                String pollIdSFromDoc = getPollIdSFromDoc(doc);
                logger.info("current pollIdSFromDoc : " + pollIdSFromDoc);
                pollsBO.setPollId(pollIdSFromDoc);

                // Extract poll_id from doc and check with database any of the poll has been
                // answered by user_id or ipAddress
                String votedPollIds = checkResponseData(pollsBO,
                        postgre);
                pollsBO.setPollId(votedPollIds);

                if (votedPollIds != null
                        && !"".equals(votedPollIds.trim())) {
                    // Fetch Result from DB for above poll_ids which were voted already by user
                    Map<String, List<Map<String, String>>> response = getPollResponse(
                            pollsBO, postgre);

                    // Iterate the solr doc and match the poll_ids , matched: try to add reponse in
                    // particular poll_id element
                    doc = addResultToXml(doc, response);
                    logger.info("Final Result - current :" + doc.asXML());
                }

            } else if ("past".equalsIgnoreCase(pollsBO.getAction())) {

                context.setParameterString("rows",
                        pollsBO.getPastPollsPerPage());

                doc = he.getLandingContent(context);

                logger.info("doc : " + doc.asXML());

                String pollIdSFromDoc = getPollIdSFromDoc(doc);
                logger.info("past pollIdSFromDoc : " + pollIdSFromDoc);
                
                if (pollIdSFromDoc != null && !"".equals(pollIdSFromDoc.trim())) {
                    pollsBO.setPollId(pollIdSFromDoc);

                    // Fetch Result from DB for above poll_ids which were voted already by user
                    Map<String, List<Map<String, String>>> response = getPollResponse(
                        pollsBO, postgre);

                    doc = addResultToXml(doc, response);
                }
                logger.info("Final Result - past :" + doc.asXML());
            } 
        }
        return doc;

    }

    /**
     * This method is used to create final document for PollGroup.
     *
     * @param expiredPollIds
     * @param response
     *
     * @return Returns final document containing result for PollGroup.
     *
     */
    @SuppressWarnings("deprecation")
    public Document createPollGroupDoc(String expiredPollIds,
            Map<String, List<Map<String, String>>> response) {
        logger.info("createPollGroupDoc");
        Document document = null;
        try {
            document = DocumentHelper.createDocument();
            Element resultElement = document.addElement(RESULT);
            Element votedpolls = resultElement.addElement("votedpolls");
            Iterator<Entry<String, List<Map<String, String>>>> iterator = response
                    .entrySet().iterator();

            while (iterator.hasNext()) {
                Entry<String, List<Map<String, String>>> entry = iterator
                        .next();
                List<Map<String, String>> optList = entry.getValue();
                logger.info((entry.getKey() + ":" + entry.getValue()));
                Element pollresult = votedpolls.addElement("pollresult");
                pollresult.setAttributeValue("pollId", entry.getKey());

                for (Map<String, String> optMap : optList) {
                    Element optionElement = pollresult
                            .addElement("option");
                    Element pollIDElem = optionElement.addElement(POLL_ID);
                    pollIDElem.setText(optMap.get(POLL_ID));
                    Element questionElem = optionElement
                            .addElement(QUESTION);
                    questionElem.setText(optMap.get(QUESTION));
                    Element optLabelElem = optionElement
                            .addElement(OPTION_LABEL);
                    optLabelElem.setText(optMap.get(OPTION_LABEL));
                    Element optValueElem = optionElement
                            .addElement(OPTION_VALUE);
                    optValueElem.setText(optMap.get(OPTION_VALUE));
                    Element pollCountElem = optionElement
                            .addElement(POLLCOUNT);
                    pollCountElem.setText(optMap.get(POLLCOUNT));
                    Element totRespElem = optionElement
                            .addElement(TOTALRESPONSE);
                    totRespElem.setText(optMap.get(TOTALRESPONSE));
                    Element pollPercentElem = optionElement
                            .addElement(POLLPCT);
                    pollPercentElem.setText(optMap.get(POLLPCT));
                }

            }

            Element expiredPolls = resultElement
                    .addElement("expiredPolls");
            expiredPolls.setText(expiredPollIds);

        } catch (Exception e) {
            logger.error("Exception in createPollGroupDoc", e);
        }

        return document;
    }

    /**
     * This method is used to add result from database to Solr doc.
     *
     * @param response Response containing data fetched from database.
     *
     * @return Returns final document containing result.
     *
     */
    @SuppressWarnings("unchecked")
    public Document addResultToXml(Document doc,
            Map<String, List<Map<String, String>>> response) {
        logger.info("addResultToXml()");
        logger.info("Received Doc :" + doc.asXML());
        try {
            List<Node> nodes = doc
                    .selectNodes("/SolrResponse/response/docs");
            logger.info("Nodes::" + nodes);
            for (Node node : nodes) {
                String sPollId = node.selectSingleNode("id").getText();
                logger.info("sPollId" + sPollId);
                    if (response != null && response.containsKey(sPollId)) {
                        List<Map<String, String>> responseMap = response
                                .get(sPollId);
                        Element element = (Element) node;
                        Element resultElement = element.addElement(RESULT);
    
                        for (Map<String, String> optMap : responseMap) {
                            Element optionElement = resultElement
                                    .addElement("Option");
                            Element pollIDElem = optionElement
                                    .addElement(POLL_ID);
                            pollIDElem.setText(optMap.get(POLL_ID));
                            Element questionElem = optionElement
                                    .addElement(QUESTION);
                            questionElem.setText(optMap.get(QUESTION));
                            Element optLabelElem = optionElement
                                    .addElement(OPTION_LABEL);
                            optLabelElem.setText(optMap.get(OPTION_LABEL));
                            Element optValueElem = optionElement
                                    .addElement(OPTION_VALUE);
                            optValueElem.setText(optMap.get(OPTION_VALUE));
                            Element pollCountElem = optionElement
                                    .addElement(POLLCOUNT);
                            pollCountElem.setText(optMap.get(POLLCOUNT));
                            Element totRespElem = optionElement
                                    .addElement(TOTALRESPONSE);
                            totRespElem.setText(optMap.get(TOTALRESPONSE));
                            Element pollPercentElem = optionElement
                                    .addElement(POLLPCT);
                            pollPercentElem.setText(optMap.get(POLLPCT));
                        }
                    }
            }
        } catch (Exception e) {
            logger.error("Exception in addResponseToXml", e);
        }
        return doc;

    }

    /**
     * This method is used to create final doc with database results for voted Poll.
     *
     * @param pollsBO  PollsBO Object.
     * @param response Response containing data fetched from database.
     *
     * @return Returns final document.
     *
     */
    public Document createPollResultDoc(PollsBO pollsBO,
            Map<String, List<Map<String, String>>> response) {
        Document document = null;
        try {

            List<Map<String, String>> responseList = response
                    .get(pollsBO.getPollId());
            document = DocumentHelper.createDocument();
            Element pollResultElem = document.addElement("PollResult");
            Element resultElement = pollResultElem.addElement(RESULT);

            for (Map<String, String> optMap : responseList) {
                Element optionElement = resultElement.addElement("Option");
                Element pollIDElem = optionElement.addElement(POLL_ID);
                pollIDElem.setText(optMap.get(POLL_ID));
                Element questionElem = optionElement.addElement(QUESTION);
                questionElem.setText(optMap.get(QUESTION));
                Element optLabelElem = optionElement
                        .addElement(OPTION_LABEL);
                optLabelElem.setText(optMap.get(OPTION_LABEL));
                Element optValueElem = optionElement
                        .addElement(OPTION_VALUE);
                optValueElem.setText(optMap.get(OPTION_VALUE));
                Element pollCountElem = optionElement
                        .addElement(POLLCOUNT);
                pollCountElem.setText(optMap.get(POLLCOUNT));
                Element totRespElem = optionElement
                        .addElement(TOTALRESPONSE);
                totRespElem.setText(optMap.get(TOTALRESPONSE));
                Element pollPercentElem = optionElement
                        .addElement(POLLPCT);
                pollPercentElem.setText(optMap.get(POLLPCT));
            }
        } catch (Exception e) {
            logger.error("Exception in createPollResultDoc", e);
        }
        return document;

    }

    /**
     * This method is used to extract and create Poll Ids' comma seprated string.
     *
     * @param doc Document Object.
     *
     * @return Comma Seprated String containing Poll Ids.
     */
    @SuppressWarnings("unchecked")
    public String getPollIdSFromDoc(Document doc) {
        logger.info("getPollIdSFromDoc()");

        List<Node> nodes = doc.selectNodes("/SolrResponse/response/docs");
        StringJoiner joiner = new StringJoiner(",");

        for (Node node : nodes) {
            joiner.add(node.selectSingleNode("id").getText());
        }
        return joiner.toString();
    }

    /**
     * This method is used to insert voted poll's data in database.
     *
     * @param pollsBO PollsBO Object.
     *
     * @param connection Connection Object.
     */
    public void insertPollResponse(PollsBO pollsBO, Postgre postgre
            ) {
        logger.debug("PollsExternal : insertPollResponse");
        String pollResponseQuery = "INSERT INTO POLL_RESPONSE (POLL_ID, "
                + "OPTION_ID, USER_ID, IP_ADDRESS, LANG, VOTED_FROM, "
                + "USER_AGENT, VOTED_ON) VALUES(?,?,?,?,?,?,?,LOCALTIMESTAMP)";
        Connection connection = null;
        PreparedStatement prepareStatement = null;

        try {
            connection = postgre.getConnection();
            prepareStatement = connection
                    .prepareStatement(pollResponseQuery);
            prepareStatement.setLong(1,
                    Long.parseLong(pollsBO.getPollId()));
            prepareStatement.setInt(2,
                    Integer.parseInt(pollsBO.getSelectedOption()));
            prepareStatement.setString(3, pollsBO.getUserId());
            prepareStatement.setString(4, pollsBO.getIpAddress());
            prepareStatement.setString(5, pollsBO.getLang());
            prepareStatement.setString(6, pollsBO.getVotedFrom());
            prepareStatement.setString(7, pollsBO.getUserAgent());
            int result = prepareStatement.executeUpdate();
            if (result == 0) {
                logger.info("Vote Not Recorded !");
            } else {
                logger.info("Vote Recorded !");
            }
        } catch (Exception e) {
            logger.error("Exception in insertPollResponse", e);
        } finally {
            postgre.releaseConnection(connection, prepareStatement, null);
        }
    }

    /**
     * This method is used to retrieve Polls' response data from database.
     *
     * @param pollsBO PollsBO Object.
     * @param connection Connection Object.
     *
     * @return
     */
    public Map<String, List<Map<String, String>>> getPollResponse(
            PollsBO pollsBO, Postgre postgre) {
        logger.info("getPollResponse()");

        String pollQuery = null;
        logger.info("lang:: " + pollsBO.getLang());
        if (pollsBO.getLang().equalsIgnoreCase("en")) {
            pollQuery = "SELECT * FROM vw_poll_stats_en WHERE poll_id = ANY(?)";
        } else {
            pollQuery = "SELECT * FROM vw_poll_stats_ar WHERE poll_id = ANY(?)";
        }
        logger.info("Poll Query ::" + pollQuery);
        Connection connection = null;
        PreparedStatement prepareStatement = null;
        ResultSet rs = null;

        String[] pollIdsArr = pollsBO.getPollId().split(",");

        Map<String, List<Map<String, String>>> pollsResultMap = new LinkedHashMap<>();

        try {
            connection = postgre.getConnection();
            prepareStatement = connection.prepareStatement(pollQuery);
            prepareStatement.setArray(1,
                    connection.createArrayOf(BIGINT, pollIdsArr));
            rs = prepareStatement.executeQuery();
            while (rs.next()) {

                logger.info("Option Value::" + rs.getString(OPTION_VALUE));

                List<Map<String, String>> pollOptList = null;
                Map<String, String> pollMap = new HashMap<>();
                pollMap.put(POLL_ID, rs.getString(POLL_ID));
                pollMap.put(QUESTION, rs.getString(QUESTION));
                pollMap.put(OPTION_LABEL, rs.getString(OPTION_LABEL));
                pollMap.put(OPTION_VALUE, rs.getString(OPTION_VALUE));
                pollMap.put(POLLCOUNT, rs.getString(POLLCOUNT));
                pollMap.put(TOTALRESPONSE, rs.getString(TOTALRESPONSE));
                pollMap.put(POLLPCT, rs.getString(POLLPCT));

                if (pollsResultMap.containsKey(rs.getString(POLL_ID))) {
                    pollOptList = pollsResultMap
                            .get(rs.getString(POLL_ID));
                } else {
                    pollOptList = new ArrayList<>();
                    pollsResultMap.put(rs.getString(POLL_ID), pollOptList);
                }
                pollOptList.add(pollMap);

            }

        } catch (Exception e) {
            logger.error("Exception in getPollResponse", e);
        } finally {
            postgre.releaseConnection(connection, prepareStatement, rs);
        }

        logger.info("Poll Result Map :: " + pollsResultMap.toString());
        return pollsResultMap;

    }

    /**
     * This method is used for checking Polls response data in database.
     * 
     * @param pollsBO PollsBO Object.
     * @param connection Connection Object.
     * 
     * @return Returns comma seperated string containing voted poll ids.
     */
    public String checkResponseData(PollsBO pollsBO,
            Postgre postgre ) {
        logger.info("checkResponseData()");
        StringBuilder checkVotedQuery = new StringBuilder(
                "SELECT POLL_ID FROM POLL_RESPONSE WHERE POLL_ID = ANY (?) ");
        StringJoiner votedPollIds = new StringJoiner(",");

        String[] pollIdsArr = pollsBO.getPollId().split(",");

        if (pollsBO.getUserId() != null
                && !"".equals(pollsBO.getUserId())) {
            checkVotedQuery.append("AND USER_ID = ? ");
        } else if (pollsBO.getIpAddress() != null
                && !"".equals(pollsBO.getIpAddress())) {
            checkVotedQuery.append("AND IP_ADDRESS = ? ");
        }
        logger.info("checkVotedQuery ::" + checkVotedQuery.toString());
        Connection connection = null;
        PreparedStatement prepareStatement = null;
        ResultSet rs = null;

        try {
            connection = postgre.getConnection();
            prepareStatement = connection
                    .prepareStatement(checkVotedQuery.toString());
            prepareStatement.setArray(1,
                    connection.createArrayOf(BIGINT, pollIdsArr));
            if (pollsBO.getUserId() != null
                    && !"".equals(pollsBO.getUserId())) {
                prepareStatement.setString(2, pollsBO.getUserId());
            } else if (pollsBO.getIpAddress() != null
                    && !"".equals(pollsBO.getIpAddress())) {
                prepareStatement.setString(2, pollsBO.getIpAddress());
            }
            rs = prepareStatement.executeQuery();
            while (rs.next()) {
                votedPollIds.add(rs.getString(POLL_ID));
            }
            logger.info("Voteed Polls : " + votedPollIds.toString());

        } catch (Exception e) {
            logger.error("Exception in checkResponseData", e);
        } finally {
            postgre.releaseConnection(connection, prepareStatement, rs);
        }
        return votedPollIds.toString();
    }

    /**
     * This method is used to set value to PollsBO object. 
     * 
     * @param context Request Context Object.
     * 
     * @return Returns PollsBO Object.
     * 
     * @deprecated
     */
    @Deprecated(since = "", forRemoval = false)
    public PollsBO setBO(final RequestContext context) {
        PollsBO pollsBO = new PollsBO();
        RequestHeaderUtils requestHeaderUtils = new RequestHeaderUtils(
                context);
        pollsBO.setAction(context.getParameterString("pollAction"));
        pollsBO.setLang(context.getParameterString("locale", "en"));
        pollsBO.setUserId(context.getParameterString("user_id"));
        pollsBO.setIpAddress(requestHeaderUtils.getClientIpAddress());
        pollsBO.setUserAgent(context.getRequest().getHeader("User-Agent"));
        pollsBO.setVotedFrom(context.getParameterString("votedFrom"));
        pollsBO.setPollId(context.getParameterString("pollId"));
        pollsBO.setCurrentPollsPerPage(
                context.getParameterString("current_poll_rows"));
        pollsBO.setPastPollsPerPage(
                context.getParameterString("past_poll_rows"));
        pollsBO.setGroup(context.getParameterString("PollsGroup"));
        pollsBO.setSelectedOption(context.getParameterString("option"));
        pollsBO.setGroupCategory(
                context.getParameterString("pollGroupCategory"));
        pollsBO.setCategory(context.getParameterString("pollCategory"));
        pollsBO.setSolrCategory(
                context.getParameterString("solrPollCategory"));
        return pollsBO;
    }

}
