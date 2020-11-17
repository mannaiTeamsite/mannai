package com.hukoomi.livesite.external;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringJoiner;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;

import com.hukoomi.utils.DBConstants;
import com.hukoomi.utils.Postgre;
import com.interwoven.livesite.p13n.model.UserProfile;
import com.interwoven.livesite.runtime.RequestContext;

public class PollsExternal {

    /** Logger object to check the flow of the code. */
    private static final Logger logger = Logger
            .getLogger(PollsExternal.class);
    /** Default query to fetch all solr content. */
    public static final String DEFAULT_QUERY = "*:*";
    /** Connection variable. */
    private static Connection connection = null;
    Postgre postgre =  null;

    /**
     * This method will be called from Component External for solr Content fetching.
     * 
     * @param context The parameter context object passed from Component.
     *
     * @return doc return the solr response document generated from solr query.
     * 
     * @deprecated
     */

    @Deprecated
    public Document performPollAction(final RequestContext context) {
        
        postgre =  new Postgre(context);
        

        logger.info("PollsExternal : performPollAction()");

        Document doc = null;
        HukoomiExternal he = new HukoomiExternal();
        HttpServletRequest request = context.getRequest();
        Locale locale = request.getLocale();

        /* Variables for Reading Parameter Values */
        String pollAction = context.getParameterString("pollAction");
        String pollId = null;
        String option = null;
        String ipAddress = null;
        String userId = null;
        String userAgent = null;
        String votedFrom = null;
        String lang = locale.getLanguage();
       
        if (pollAction != null) {
            if ("vote".equalsIgnoreCase(pollAction)) {
                logger.info("PollAction = Vote");

                pollId = context.getParameterString("pollId");
                option = context.getParameterString("option");
                ipAddress = context.getRequest().getRemoteAddr();
                userId = context.getParameterString("user_id");
                //userId = "test_user";
                userAgent = context.getRequest().getHeader("User-Agent");
                votedFrom = context.getParameterString("votedFrom");
              

                logger.info("poll_id:" + pollId + "&option:" + option
                        + "&lang:" + lang + "&user_id:" + userId
                        + "&RemoteAddr:" + ipAddress + "&UserAgent:"
                        + userAgent + "&voted_from:" + votedFrom);
                // int optionId = getOptionId(pollId, lang, option);
                // logger.info(optionId);

                insertPollResponse(lang, pollId, option, userId, ipAddress,
                        userAgent, votedFrom, postgre.getConnection());

                // Fetch Result from DB for above poll_ids which were voted already by user
                Map<String, List<Map<String, String>>> response = getPollResponse(
                        pollId,lang, postgre.getConnection());

                doc = createPollResultDoc(pollId, response);
            } else if ("current".equalsIgnoreCase(pollAction)) {
                logger.info("PollAction = Current Polls");

                ipAddress = context.getRequest().getRemoteAddr();
                userId = context.getParameterString("user_id");
                logger.info("user_id:" + userId + "&RemoteAddr:" + ipAddress);
                String current_poll_row = context
                        .getParameterString("current_poll_rows");
                context.setParameterString("rows", current_poll_row);

                doc = he.getLandingContent(context);

                logger.info("doc : " + doc.asXML());

                String pollIdSFromDoc = getPollIdSFromDoc(doc);
                logger.info("pollIdSFromDoc : " + pollIdSFromDoc);

                // Extract poll_id from doc and check with database any of the poll has been
                // answered by user_id or ipAddress
                String votedPollIds = checkResponseData(pollIdSFromDoc,
                        userId, ipAddress, postgre.getConnection());

                if(votedPollIds != null && !"".equals(votedPollIds.trim())) {
	                // Fetch Result from DB for above poll_ids which were voted already by user
	                Map<String, List<Map<String, String>>> response = getPollResponse(
	                        votedPollIds,lang, postgre.getConnection());
	
	                // Iterate the solr doc and match the poll_ids , matched: try to add reponse in
	                // particular poll_id element
	                doc = addResultToXml(doc, response);
                }
                logger.info("Final Result::" + doc.asXML());
            } else if ("past".equalsIgnoreCase(pollAction)) {
                logger.info("PollAction = Past Polls");

                String past_poll_row = context
                        .getParameterString("past_poll_rows");
                context.setParameterString("rows", past_poll_row);

                doc = he.getLandingContent(context);

                logger.info("doc : " + doc.asXML());

                String pollIdSFromDoc = getPollIdSFromDoc(doc);
                logger.info("pollIdSFromDoc : " + pollIdSFromDoc);

                // Fetch Result from DB for above poll_ids which were voted already by user
                Map<String, List<Map<String, String>>> response = getPollResponse(
                        pollIdSFromDoc,lang, postgre.getConnection());

                doc = addResultToXml(doc, response);
                logger.info("Final Doc for Past Polls::" + doc.asXML());
            } else if ("pollGroup".equalsIgnoreCase(pollAction)) {
                logger.info("PollAction = pollexpirycheck");
                logger.info("Page Name::"+context.getPageName());

                String pollIds = context.getParameterString("pollIds");
                logger.info("Poll ids::" + pollIds);

                userId = "test_user";
                
                // Checking poll is expired or not
                String activePollIds = getPollExpiryStatus(pollIds);
                logger.info("Expired Pollds:: " + activePollIds);
                
                ArrayList<String> frontEndIds = new ArrayList<>(Arrays.asList(pollIds.split(",")));

                ArrayList<String> activeIds = new ArrayList<>(Arrays.asList(activePollIds.split(",")));
                
                frontEndIds.removeAll(activeIds);
                
                StringJoiner expiryId = new StringJoiner(",");
                
                ListIterator<String> id = frontEndIds.listIterator();
                while (id.hasNext()) {
                    expiryId.add(id.next());
                }
                        
                String expiredPollIds = expiryId.toString();
                
                

                String votedPollIds = checkResponseData(activePollIds,
                        userId, ipAddress, postgre.getConnection());

                // Fetch Result from DB for above poll_ids which were voted already by user
                Map<String, List<Map<String, String>>> response = getPollResponse(
                        votedPollIds,lang, postgre.getConnection());

                doc = createPollGroupDoc(expiredPollIds, response);

                logger.info("Expired Poll Doc:: " + doc.asXML());

            } else if ("search".equalsIgnoreCase(pollAction)) {
                logger.info("PollAction = search");

                ipAddress = context.getRequest().getRemoteAddr();
                userId = context.getParameterString("userId");

                doc = he.getLandingContent(context);

                String pollIdSFromDoc = getPollIdSFromDoc(doc);
                logger.info("pollIdSFromDoc : " + pollIdSFromDoc);

                // Extract poll_id from doc and check with database any of the poll has been
                // answered by user_id or ipAddress
                String votedPollIds = checkResponseData(pollIdSFromDoc,
                        userId, ipAddress, postgre.getConnection());

                if(votedPollIds != null && !"".equals(votedPollIds)) {
	                // Fetch Result from DB for above poll_ids which were voted already by user
	                Map<String, List<Map<String, String>>> response = getPollResponse(
	                        votedPollIds,lang, postgre.getConnection());
	
	                // Iterate the solr doc and match the poll_ids , matched: try to add reponse in
	                // particular poll_id element
	                doc = addResultToXml(doc, response);
                }
                logger.info("Final Result::" + doc.asXML());

            }
        }
        return doc;

    }
    
    @SuppressWarnings("deprecation")
    public Document createPollGroupDoc(String expiredPollIds,
            Map<String, List<Map<String, String>>> response) {
        logger.info("createPollGroupDoc");
        Document document = null;
        try {
            document = DocumentHelper.createDocument();
            Element resultElement = document.addElement("Result");
            Element votedpolls = resultElement.addElement("votedpolls");
            Iterator<Entry<String, List<Map<String, String>>>> iterator = response
                    .entrySet().iterator();
            
            while (iterator.hasNext()) {
                Entry<String, List<Map<String, String>>> entry = iterator
                        .next();
                List<Map<String, String>> optList = entry.getValue();
                logger.info((entry.getKey() + ":" + entry.getValue()));
                Element pollresult = votedpolls
                        .addElement("pollresult");
                pollresult.setAttributeValue("pollId", entry.getKey());
                
                for (Map<String, String> optMap : optList) {
                    Element optionElement = pollresult.addElement("option");
                    Element pollIDElem = optionElement
                            .addElement(DBConstants.POLL_ID);
                    pollIDElem.setText(optMap.get(DBConstants.POLL_ID));
                    Element questionElem = optionElement
                            .addElement(DBConstants.QUESTION);
                    questionElem.setText(optMap.get(DBConstants.QUESTION));
                    Element optLabelElem = optionElement
                            .addElement(DBConstants.OPTION_LABEL);
                    optLabelElem.setText(optMap.get(DBConstants.OPTION_LABEL));
                    Element optValueElem = optionElement
                            .addElement(DBConstants.OPTION_VALUE);
                    optValueElem.setText(optMap.get(DBConstants.OPTION_VALUE));
                    Element pollCountElem = optionElement
                            .addElement(DBConstants.POLLCOUNT);
                    pollCountElem.setText(optMap.get(DBConstants.POLLCOUNT));
                    Element totRespElem = optionElement
                            .addElement(DBConstants.TOTALRESPONSE);
                    totRespElem.setText(optMap.get(DBConstants.TOTALRESPONSE));
                    Element pollPercentElem = optionElement
                            .addElement(DBConstants.POLLPCT);
                    pollPercentElem.setText(optMap.get(DBConstants.POLLPCT));
                }
                
            }

            Element expiredPolls = resultElement.addElement("expiredPolls");
            expiredPolls.setText(expiredPollIds);
            
            

        } catch (Exception e) {
            e.printStackTrace();
        }

        return document;
    }

    public String getPollExpiryStatus(String pollIds) {
        logger.info("getExpiryStatus()");
        StringJoiner expiredPollIds = new StringJoiner(",");

        String[] pollIdsArr = pollIds.split(",");

        String expiryCheckQuery = "SELECT P.POLL_ID FROM POLL_MASTER P WHERE P.END_DATE > CURRENT_DATE AND P.POLL_ID = ANY (?)";

        PreparedStatement prepareStatement = null;
        ResultSet rs = null;

        try {
            connection = postgre.getConnection();
            prepareStatement = connection
                    .prepareStatement(expiryCheckQuery);
            prepareStatement.setArray(1, connection
                    .createArrayOf(DBConstants.BIGINT, pollIdsArr));

            rs = prepareStatement.executeQuery();
            if (rs != null) {
                while (rs.next()) {
                    expiredPollIds.add(rs.getString(1));
                }
            }

        } catch (SQLException e) {
            String errorMsg = "SQLException :";
            if (null != e.getMessage()) {
                errorMsg += e.getMessage();
            }
            logger.error(errorMsg);
        } finally {
            Postgre.releaseConnection(connection, prepareStatement, rs);
        }

        return expiredPollIds.toString();
    }

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
                if (response.containsKey(sPollId)) {
                    List<Map<String, String>> responseMap = response
                            .get(sPollId);
                    Element element = (Element) node;
                    Element resultElement = element.addElement("Result");

                    for (Map<String, String> optMap : responseMap) {
                        Element optionElement = resultElement
                                .addElement("Option");
                        Element pollIDElem = optionElement
                                .addElement(DBConstants.POLL_ID);
                        pollIDElem
                                .setText(optMap.get(DBConstants.POLL_ID));
                        Element questionElem = optionElement
                                .addElement(DBConstants.QUESTION);
                        questionElem
                                .setText(optMap.get(DBConstants.QUESTION));
                        Element optLabelElem = optionElement
                                .addElement(DBConstants.OPTION_LABEL);
                        optLabelElem.setText(
                                optMap.get(DBConstants.OPTION_LABEL));
                        Element optValueElem = optionElement
                                .addElement(DBConstants.OPTION_VALUE);
                        optValueElem.setText(
                                optMap.get(DBConstants.OPTION_VALUE));
                        Element pollCountElem = optionElement
                                .addElement(DBConstants.POLLCOUNT);
                        pollCountElem.setText(
                                optMap.get(DBConstants.POLLCOUNT));
                        Element totRespElem = optionElement
                                .addElement(DBConstants.TOTALRESPONSE);
                        totRespElem.setText(
                                optMap.get(DBConstants.TOTALRESPONSE));
                        Element pollPercentElem = optionElement
                                .addElement(DBConstants.POLLPCT);
                        pollPercentElem
                                .setText(optMap.get(DBConstants.POLLPCT));
                    }
                }
            }
        } catch (Exception e) {
            logger.info("addResponseToXml:::::" + e.getMessage());
            e.printStackTrace();
        }
        return doc;

    }

    public Document createPollResultDoc(String pollId,
            Map<String, List<Map<String, String>>> response) {
        Document document = null;
        try {

            List<Map<String, String>> responseList = response.get(pollId);
            document = DocumentHelper.createDocument();
            Element pollResultElem = document.addElement("PollResult");
            Element resultElement = pollResultElem.addElement("Result");

            for (Map<String, String> optMap : responseList) {
                Element optionElement = resultElement.addElement("Option");
                Element pollIDElem = optionElement
                        .addElement(DBConstants.POLL_ID);
                pollIDElem.setText(optMap.get(DBConstants.POLL_ID));
                Element questionElem = optionElement
                        .addElement(DBConstants.QUESTION);
                questionElem.setText(optMap.get(DBConstants.QUESTION));
                Element optLabelElem = optionElement
                        .addElement(DBConstants.OPTION_LABEL);
                optLabelElem.setText(optMap.get(DBConstants.OPTION_LABEL));
                Element optValueElem = optionElement
                        .addElement(DBConstants.OPTION_VALUE);
                optValueElem.setText(optMap.get(DBConstants.OPTION_VALUE));
                Element pollCountElem = optionElement
                        .addElement(DBConstants.POLLCOUNT);
                pollCountElem.setText(optMap.get(DBConstants.POLLCOUNT));
                Element totRespElem = optionElement
                        .addElement(DBConstants.TOTALRESPONSE);
                totRespElem.setText(optMap.get(DBConstants.TOTALRESPONSE));
                Element pollPercentElem = optionElement
                        .addElement(DBConstants.POLLPCT);
                pollPercentElem.setText(optMap.get(DBConstants.POLLPCT));
            }
        } catch (Exception e) {
            logger.info("createPollResultDoc:::::" + e.getMessage());
            e.printStackTrace();
        }
        return document;

    }

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

    public void insertPollResponse(String lang, String pollId,
            String optionId, String userId, String ipAddress,
            String userAgent, String votedFrom, Connection connection) {
        logger.debug("PollsExternal : insertPollResponse");
        String POLL_RESPONSE_INSERT_QUERY = "INSERT INTO POLL_RESPONSE (POLL_ID, OPTION_ID, USER_ID, IP_ADDRESS, LANG, VOTED_FROM, USER_AGENT, VOTED_ON) VALUES(?,?,?,?,?,?,?,LOCALTIMESTAMP)";
        PreparedStatement prepareStatement = null;

        try {
            //connection = postgre.getConnection();
            prepareStatement = connection
                    .prepareStatement(POLL_RESPONSE_INSERT_QUERY);
            prepareStatement.setLong(1, Long.parseLong(pollId));
            prepareStatement.setInt(2, Integer.parseInt(optionId));
            prepareStatement.setString(3, userId);
            prepareStatement.setString(4, ipAddress);
            prepareStatement.setString(5, lang);
            prepareStatement.setString(6, votedFrom);
            prepareStatement.setString(7, userAgent);
            int result = prepareStatement.executeUpdate();
            if (result == 0) {
                logger.error("Vote Not Recorded !");
            } else {
                logger.info("Vote Recorded !");
            }
        } catch (SQLException e) {
            String errorMsg = "SQLException :";
            if (null != e.getMessage()) {
                errorMsg += e.getMessage();
            }
            logger.error(errorMsg);
        } finally {
            Postgre.releaseConnection(connection, prepareStatement, null);
        }
    }

    public Map<String, List<Map<String, String>>> getPollResponse(
            String pollIds, String lang, Connection connection) {
        logger.info("getPollResponse()");
        /*String pollQuery = "SELECT pm.poll_id, pm.question, ps.option_id,
        ps.option_label, ps.option_value, ps.PollCount, totalresponse,
        cast(cast(ps.PollCount as DECIMAL(3,0))/totalresponse * 100 AS INT) pollPct
        FROM poll_master pm INNER JOIN ( SELECT pr.poll_id, apom.option_id,
        apom.option_label, apom.option_value, COUNT(*) PollCount FROM poll_response
        pr INNER JOIN poll_option pom ON pr.poll_id = pom.poll_id AND pr.lang =
        pom.lang AND pr.option_id = pom.option_id LEFT OUTER JOIN poll_option apom ON
        pr.poll_id = apom.poll_id AND apom.lang = 'en' AND
        pr.option_id=apom.option_id GROUP BY pr.poll_id, apom.option_label,
        apom.option_value, apom.option_id ) PS ON PM.poll_id=ps.poll_id and
        pm.lang='en' AND pm.poll_id = ANY (?) INNER JOIN ( SELECT COUNT(*)
        totalresponse, ps.poll_id FROM poll_response ps GROUP BY poll_id) prc ON
        pm.poll_id = prc.poll_id ORDER BY poll_id, option_id";*/
        
        String pollQuery = null;
        logger.info("lang:: "+lang);
        if(lang.equalsIgnoreCase("en")) {
            pollQuery = "SELECT * FROM vw_poll_stats_en WHERE poll_id = ANY(?)";
        }else {
            pollQuery = "SELECT * FROM vw_poll_stats_ar WHERE poll_id = ANY(?)";
        }
        logger.info("Poll Query ::"+pollQuery);
        PreparedStatement prepareStatement = null;
        ResultSet rs = null;

        String[] pollIdsArr = pollIds.split(",");

        Map<String, List<Map<String, String>>> pollsResultMap = new LinkedHashMap<String, List<Map<String, String>>>();

        try {
            //connection = postgre.getConnection();
            prepareStatement = connection
                    .prepareStatement(pollQuery.toString());
            prepareStatement.setArray(1, connection
                    .createArrayOf(DBConstants.BIGINT, pollIdsArr));
            rs = prepareStatement.executeQuery();
            if (rs != null) {
                while (rs.next()) {

                    logger.info("Option Value::"
                            + rs.getString(DBConstants.OPTION_VALUE));

                    List<Map<String, String>> pollOptList = null;
                    Map<String, String> pollMap = new HashMap<String, String>();
                    pollMap.put(DBConstants.POLL_ID,
                            rs.getString(DBConstants.POLL_ID));
                    pollMap.put(DBConstants.QUESTION,
                            rs.getString(DBConstants.QUESTION));
                    pollMap.put(DBConstants.OPTION_LABEL,
                            rs.getString(DBConstants.OPTION_LABEL));
                    pollMap.put(DBConstants.OPTION_VALUE,
                            rs.getString(DBConstants.OPTION_VALUE));
                    pollMap.put(DBConstants.POLLCOUNT,
                            rs.getString(DBConstants.POLLCOUNT));
                    pollMap.put(DBConstants.TOTALRESPONSE,
                            rs.getString(DBConstants.TOTALRESPONSE));
                    pollMap.put(DBConstants.POLLPCT,
                            rs.getString(DBConstants.POLLPCT));

                    if (pollsResultMap.containsKey(
                            rs.getString(DBConstants.POLL_ID))) {
                        pollOptList = pollsResultMap
                                .get(rs.getString(DBConstants.POLL_ID));
                    } else {
                        pollOptList = new ArrayList<Map<String, String>>();
                        pollsResultMap.put(
                                rs.getString(DBConstants.POLL_ID),
                                pollOptList);
                    }
                    pollOptList.add(pollMap);

                }
            }

        } catch (SQLException e) {
            String errorMsg = "SQLException :";
            if (null != e.getMessage()) {
                errorMsg += e.getMessage();
            }
            logger.error(errorMsg);
        } finally {
            Postgre.releaseConnection(connection, prepareStatement, rs);
        }

        logger.info("Poll Result Map :: " + pollsResultMap.toString());
        return pollsResultMap;

    }

    public String checkResponseData(String pollIds, String user_id,
            String ipAddress, Connection connection) {
        logger.info("checkResponseData()");
        StringBuffer checkVotedQuery = new StringBuffer(
                "SELECT POLL_ID FROM POLL_RESPONSE WHERE POLL_ID = ANY (?) ");
        StringJoiner votedPollIds = new StringJoiner(",");

        String[] pollIdsArr = pollIds.split(",");

        if (user_id != null && !"".equals(user_id)) {
            checkVotedQuery.append("AND USER_ID = ? ");
        } else if (ipAddress != null && !"".equals(ipAddress)) {
            checkVotedQuery.append("AND IP_ADDRESS = ? ");
        }
        logger.info("checkVotedQuery ::"+checkVotedQuery.toString());
        PreparedStatement prepareStatement = null;
        ResultSet rs = null;

        try {
            //connection = postgre.getConnection();
            prepareStatement = connection
                    .prepareStatement(checkVotedQuery.toString());
            prepareStatement.setArray(1, connection
                    .createArrayOf(DBConstants.BIGINT, pollIdsArr));
            if (user_id != null && !"".equals(user_id)) {
                prepareStatement.setString(2, user_id);
            } else if (ipAddress != null && !"".equals(ipAddress)) {
                prepareStatement.setString(2, ipAddress);
            }
            rs = prepareStatement.executeQuery();
            if (rs != null) {
                while (rs.next()) {
                    votedPollIds.add(rs.getString("POLL_ID"));
                }
            }
            logger.info("Voteed Polls : " + votedPollIds.toString());

        } catch (SQLException e) {
            String errorMsg = "SQLException :";
            if (null != e.getMessage()) {
                errorMsg += e.getMessage();
            }
            logger.error(errorMsg);
        } finally {
            Postgre.releaseConnection(connection, prepareStatement, rs);
        }
        return votedPollIds.toString();
    }

    public int getOptionId(String pollId, String lang,
            String option) {
        logger.debug("PollsExternal : getOptionId()");
        Statement st = null;
        ResultSet rs = null;
        int optionId = 0;
        String GET_OPTION_ID = "SELECT OPTION_ID FROM POLL_OPTION WHERE POLL_ID = "
                + pollId + " AND LANG = '" + lang + "' AND OPTION = '"
                + option + "'";
        try {
            connection = postgre.getConnection();
            st = connection.createStatement();
            rs = st.executeQuery(GET_OPTION_ID);
            if (rs != null) {
                while (rs.next()) {
                    optionId = rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            logger.error("getOptionId()" + e.getMessage());
            e.printStackTrace();
        } finally {
            Postgre.releaseConnection(connection, st, rs);
        }

        return optionId;
    }

}
