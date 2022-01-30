package com.hukoomi.livesite.external;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;
import java.util.StringJoiner;
import java.util.UUID;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.ValidationErrorList;

import com.hukoomi.bo.PollsBO;
import com.hukoomi.utils.ESAPIValidator;
import com.hukoomi.utils.GoogleRecaptchaUtil;
import com.hukoomi.utils.Postgre;
import com.hukoomi.utils.PropertiesFileReader;
import com.hukoomi.utils.RequestHeaderUtils;
import com.hukoomi.utils.UserInfoSession;
import com.interwoven.livesite.runtime.RequestContext;

/**
 * PollsExternal is the components external class.
 * 
 * @author Vijayaragavamoorthy
 *
 */
@SuppressWarnings("deprecation")
public class PollsExternal {

    /** Logger object to check the flow of the code. */
    private static final Logger logger = Logger
            .getLogger(PollsExternal.class);
    /** Default query to fetch all solr content. */
    public static final String DEFAULT_QUERY = "*:*";
    /**
     * Maximum current polls to be fethced. public static final String
     * MAX_CURRENT_POLLS_FETCH = "1000";
     */
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
     * Constant for action polls and survey.
     */
    public static final String ACTION_POLLS_AND_SURVEY = "pollsandsurvey";
    /**
     * Constant for action dashboard polls and survey.
     */
    public static final String DASHBOARD = "Dashboard";
    /**
     * Constant for action current polls.
     */
    public static final String ACTION_CURRENT_POLLS = "current";
    /**
     * Constant for action past polls.
     */
    public static final String ACTION_PAST_POLLS = "past";
    /**
     * Constant for action vote.
     */
    public static final String ACTION_VOTE = "vote";
    /**
     * HashSet to collect Voted Poll Ids.
     */
    HashSet<String> votedPolls;
    /**
     * Http Servlet Request Object
     */
    HttpServletRequest request = null;
    /**
     * Http Servlet Response Object
     */
    HttpServletResponse response = null;
    /**
     * NLUID key
     */
    public static final String NLUID = "NLUID";
    /**
     * NLUID QUERY part
     */
    public static final String NLUID_QUERY ="AND NLUID = ? ";
    /**
     * USER_ID QUERY part
     */
    public static final String USERID_QUERY ="AND USER_ID = ? ";
    /**
     * NLUID Cookie Expiry
     */
    public static final String NLUSERCOOKIEEXPIRY = "nlUserCookieExpiry";
    /**
     * POLL_ACTION
     */
    public static final String POLL_ACTION = "pollAction";
    /**
     * Properties for non logged in user cookie
     */
    Properties nluseridProp =  null;
    /**
     * Properties for captcha config properties
     */
    Properties captchaconfigProp = null;

    /**
     * This method will be called from Component External for solr Content fetching.
     * 
     * @param context Request context object.
     *
     * @return doc Returns the solr response document generated from solr query.
     * 
     */
    public Document performPollAction(RequestContext context) {

        votedPolls = new LinkedHashSet<>();

        logger.info("PollsExternal : performPollAction()");
        
        Document doc = DocumentHelper.createDocument();

        PollsBO pollsBO = new PollsBO();
        postgre = new Postgre(context);
        boolean isValidInput = setBO(context, pollsBO, postgre);
        logger.info("isValidInput : " + isValidInput);
        
        logger.info("PollsExternal : Loading captchaconfig Properties....");
        PropertiesFileReader captchapropertyFileReader = new PropertiesFileReader(
                context, "captchaconfig.properties");
        captchaconfigProp = captchapropertyFileReader
                .getPropertiesFile();
        logger.info("PollsExternal : captchaconfig Properties Loaded");
        
        if (isValidInput) {

            logger.debug("PollsBO : " + pollsBO);
            HukoomiExternal he = new HukoomiExternal();
            
            if(StringUtils.equalsIgnoreCase("vote", pollsBO.getAction())) {
                doc = processVotePoll(pollsBO, postgre, votedPolls, context);
            } else if(StringUtils.equalsIgnoreCase(ACTION_CURRENT_POLLS, pollsBO.getAction())) {
                doc = processCurrentPolls(context, he, pollsBO);
            } else if(StringUtils.equalsIgnoreCase("past", pollsBO.getAction())) { 
                doc = processPastPolls(context, he, pollsBO);
            }
        } else {
            logger.info("Invalid input parameter");
            if (StringUtils.equalsIgnoreCase(ACTION_CURRENT_POLLS, pollsBO.getAction())
                    || StringUtils.equalsIgnoreCase("past", pollsBO.getAction())) {
                Element pollResponseElem = doc.addElement("SolrResponse")
                        .addElement("response").addElement("numFound");
                pollResponseElem.setText("0");
            } else if (StringUtils.equalsIgnoreCase("vote", pollsBO.getAction())) {
                doc.addElement("PollResult").addElement(RESULT);
            }
        }
        logger.debug("Final Result :" + doc.asXML());
        return doc;

    }

    /**
     * This method is used to process the vote for a poll.
     * 
     * @param pollsBO PollsBO object.
     * @param postgre Postgre object.
     * 
     * @return Returns document which contains the result of the voted poll.
     * 
     */
    public Document processVotePoll(PollsBO pollsBO, Postgre postgre,
            Set<String> votedPolls, RequestContext context) {
        logger.info("Polls External : processVotePoll");
        Map<Long, Long> votedOptions = null;
        boolean isCaptchaValid = true;
        
        if(!StringUtils.isNotBlank(pollsBO.getUserId()) && !StringUtils.isNotBlank(pollsBO.getNLUID())) {
            String nlUID = UUID.randomUUID().toString();
            logger.info(NLUID+" : " + nlUID);
            pollsBO.setNLUID(nlUID);
            Cookie nlUIDCookie = new Cookie(NLUID, nlUID);
            
            logger.info("nlUIDCookie : " + nlUIDCookie);
            String nlUserCookieExpiryStr = nluseridProp.getProperty(NLUSERCOOKIEEXPIRY);
            logger.info("nlUserCookieExpiryStr : " + nlUserCookieExpiryStr);
            int nlUserCookieExpiry = 0;
            if(StringUtils.isNotBlank(nlUserCookieExpiryStr)) {
                nlUserCookieExpiry = Integer.parseInt(nlUserCookieExpiryStr);               
            }
            nlUIDCookie.setMaxAge(nlUserCookieExpiry);
            nlUIDCookie.setPath("/");
            nlUIDCookie.setHttpOnly(true);
            response.addCookie(nlUIDCookie);
            logger.info("nlUIDCookie added to cookie");
        }
        logger.info("Polls External : processVotePoll : isPollVoted");
        Map<String, List<Map<String, String>>> responseMap = null;
        if (!isPollVoted(pollsBO, postgre)) {
            GoogleRecaptchaUtil captchUtil = new GoogleRecaptchaUtil();
            if (captchUtil.validateCaptcha(context,
                    pollsBO.getCaptchaResponse())) {
                isCaptchaValid = true;
                insertPollResponse(pollsBO, postgre);
                
                // Fetch Result from DB for above poll_ids which were voted already by user
                responseMap = getPollResponse(pollsBO, postgre, votedPolls);

                if (!votedPolls.isEmpty()) {
                    votedOptions = getVotedOption(postgre, votedPolls, pollsBO);
                    logger.info("Voted Polls :: " + votedOptions.toString());
                }
                
            } else {
                logger.info("Google Recaptcha is not valid");
                isCaptchaValid = false;
            }
        }
        return createPollResultDoc(pollsBO, responseMap, votedOptions, isCaptchaValid);
    }

    /**
     * This method is used to check if a poll is voted or not.
     * 
     * @param pollsBO PollsBO object.
     * @param postgre Postgre object.
     * 
     * @return Returns true if the poll is voted by the user or non logged-in user
     *         else returns false.
     * 
     */
    public boolean isPollVoted(PollsBO pollsBO, Postgre postgre) {
        boolean isPollVoted = false;

        logger.info("isPollVoted()");
        logger.debug("isPollVoted - PollId : " + pollsBO.getPollId()
                + "\nUserId : " + pollsBO.getUserId() + "\nNLUID : "
                + pollsBO.getNLUID());
        StringBuilder checkVotedQuery = new StringBuilder(
                "SELECT POLL_ID FROM POLL_RESPONSE WHERE POLL_ID = ? ");

        if (StringUtils.isNotBlank(pollsBO.getUserId())) {
            checkVotedQuery.append(USERID_QUERY);
        } else {
            checkVotedQuery.append(NLUID_QUERY);
        }
        logger.debug("checkVotedQuery ::" + checkVotedQuery.toString());
        Connection connection = null;
        PreparedStatement prepareStatement = null;
        ResultSet rs = null;

        try {
            connection = postgre.getConnection();
            prepareStatement = connection
                    .prepareStatement(checkVotedQuery.toString());
            prepareStatement.setBigDecimal(1,
                    new BigDecimal(pollsBO.getPollId()));
            if (StringUtils.isNotBlank(pollsBO.getUserId())) {
                prepareStatement.setString(2, pollsBO.getUserId());
            } else {
                prepareStatement.setString(2, pollsBO.getNLUID());
            }
            rs = prepareStatement.executeQuery();
            while (rs.next()) {
                isPollVoted = true;
            }
        } catch (Exception e) {
            logger.error("Exception in isPollVoted", e);
        } finally {
            postgre.releaseConnection(connection, prepareStatement, rs);
        }
        logger.info("isPollVoted - isPollVoted : " + isPollVoted);
        return isPollVoted;
    }

    /**
     * This method is used to process the past poll.
     * 
     * @param context Request context object.
     * @param he      HukoomiExternal object.
     * @param postgre Postgre object.
     * 
     * @return Returns document which contains the past polls.
     * 
     */
    private Document processPastPolls(final RequestContext context,
            HukoomiExternal he, PollsBO pollsBO) {
        Document doc;
        context.setParameterString("rows", pollsBO.getPastPollsPerPage());

        doc = he.getLandingContent(context);

        logger.debug("doc : " + doc.asXML());

        String pollIdSFromDoc = getPollIdSFromDoc(doc);
        logger.debug("past pollIdSFromDoc : " + pollIdSFromDoc);

        if (StringUtils.isNotBlank(pollIdSFromDoc)) {
            pollsBO.setPollId(pollIdSFromDoc);

            // Fetch Result from DB for above poll_ids which were voted already by user
            Map<String, List<Map<String, String>>> pollresponse = getPollResponse(
                    pollsBO, postgre, votedPolls);

            Map<Long, Long> votedOptions = null;

            if (!votedPolls.isEmpty()) {
                votedOptions = getVotedOption(postgre, votedPolls,
                        pollsBO);
                logger.info("Voted Polls ::: " + votedOptions.toString());
            }

            doc = addResultToXml(doc, pollresponse, votedOptions);
        }
        return doc;
    }

    /**
     * @param
     * @return
     */
    public Map<Long, Long> getVotedOption(Postgre postgre,
            Set<String> votedPolls, PollsBO pollsBO) {
        logger.info("getVotedOption()");

        logger.info("Voted Polls :::: " + votedPolls.toString());
        logger.info("Polls BO : " + pollsBO);

        StringJoiner pollIds = new StringJoiner(",");

        Map<Long, Long> votedOptions = new LinkedHashMap<>();

        Iterator<String> iterator = votedPolls.iterator();
        while (iterator.hasNext()) {
            pollIds.add(iterator.next());
        }

        StringBuilder getVotedOptions = new StringBuilder(
                "SELECT POLL_ID, OPTION_ID FROM POLL_RESPONSE WHERE POLL_ID = ANY (?) ");

        if (StringUtils.isNotBlank(pollsBO.getUserId())) {
            getVotedOptions.append(USERID_QUERY);
        } else {
            getVotedOptions.append(NLUID_QUERY);
        }
        logger.info("checkVotedQuery :::" + getVotedOptions.toString());
        Connection connection = null;
        PreparedStatement prepareStatement = null;
        ResultSet rs = null;

        try {
            connection = postgre.getConnection();
            prepareStatement = connection
                    .prepareStatement(getVotedOptions.toString());
            prepareStatement.setArray(1, connection.createArrayOf(BIGINT,
                    pollIds.toString().split(",")));
            if (StringUtils.isNotBlank(pollsBO.getUserId())) {
                prepareStatement.setString(2, pollsBO.getUserId());
            } else {
                prepareStatement.setString(2, pollsBO.getNLUID());
            }
            rs = prepareStatement.executeQuery();
            while (rs.next()) {
                votedOptions.put(rs.getLong(POLL_ID),
                        rs.getLong("OPTION_ID"));
            }
            logger.info("Voted Polls ::::: " + votedOptions.toString());

        } catch (Exception e) {
            logger.error("Exception in checkResponseData", e);
        } finally {
            postgre.releaseConnection(connection, prepareStatement, rs);
        }

        return votedOptions;
    }

    /**
     * This method is used to process the past poll.
     * 
     * @param context
     *                Request context object.
     * @param he
     *                HukoomiExternal object.
     * @param postgre
     *                Postgre object.
     * 
     * @return Returns document which contains the current polls.
     * 
     */
    private Document processCurrentPolls(RequestContext context,
            HukoomiExternal he, PollsBO pollsBO) {
        Document doc;
        context.setParameterString("rows",
                pollsBO.getCurrentPollsPerPage());

        doc = he.getLandingContent(context);
        String siteKey = captchaconfigProp.getProperty("siteKey");
        logger.debug("siteKey : " + siteKey);
        doc.getRootElement().addAttribute("Sitekey", siteKey);
        logger.debug("current doc : " + doc.asXML());

        String pollIdSFromDoc = getPollIdSFromDoc(doc);
        logger.debug("current pollIdSFromDoc : " + pollIdSFromDoc);

        if (StringUtils.isNotBlank(pollIdSFromDoc)) {

            // Extract poll_id from doc and check with database any of the poll has been
            // answered by user_id or ipAddress
            pollsBO.setPollId(pollIdSFromDoc);
            String votedPollIds = "";
            if(StringUtils.isNotBlank(pollsBO.getUserId()) || StringUtils.isNotBlank(pollsBO.getNLUID())) {
                votedPollIds = checkResponseData(pollsBO, postgre);
            }
            
            if(StringUtils.isNotBlank(votedPollIds)) {
                pollsBO.setPollId(votedPollIds);
                // Fetch Result from DB for above poll_ids which were voted already by user
                Map<String, List<Map<String, String>>> pollresponse = getPollResponse(
                        pollsBO, postgre, votedPolls);

                Map<Long, Long> votedOptions = null;

                if (!votedPolls.isEmpty()) {
                    votedOptions = getVotedOption(postgre, votedPolls,
                            pollsBO);
                    logger.info(
                            "Voted Polls : " + votedOptions.toString());
                }

                // Iterate the solr doc and match the poll_ids , matched: try to add reponse in
                // particular poll_id element
                doc = addResultToXml(doc, pollresponse, votedOptions);
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
                logger.debug((entry.getKey() + ":" + entry.getValue()));
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
            Map<String, List<Map<String, String>>> response,
            Map<Long, Long> votedOptions) {
        logger.info("addResultToXml()");
        logger.debug("Received Doc :" + doc.asXML());
        try {
            List<Node> nodes = doc
                    .selectNodes("/SolrResponse/response/docs");
            logger.info("Nodes::" + nodes);
            for (Node node : nodes) {
                String sPollId = node.selectSingleNode("id").getText();
                Long optionVoted = null;
                if (votedOptions != null) {
                    optionVoted = votedOptions
                        .get(Long.parseLong(sPollId));
                }
                logger.debug("sPollId" + sPollId);
                if (response != null && response.containsKey(sPollId)) {
                    List<Map<String, String>> responseMap = response
                            .get(sPollId);
                    Element element = (Element) node;
                    Element resultElement = element.addElement(RESULT);
                    Long i = 1L;
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
                        
                        if (i.equals(optionVoted)) {
                            pollPercentElem.addAttribute("Voted", "true");
                        }
                        pollPercentElem.setText(optMap.get(POLLPCT));
                        i++;
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
            Map<String, List<Map<String, String>>> response,
            Map<Long, Long> votedOptions, boolean isCaptchaValid) {
        Document document = null;
        try {

            document = DocumentHelper.createDocument();
            Element pollResultElem = document.addElement("PollResult");
                     
            Element captchaResponseElement = pollResultElem.addElement("isCaptchaValid");
            if(isCaptchaValid) {
                captchaResponseElement.setText("Valid");
                Element resultElement = pollResultElem.addElement(RESULT);
                
                List<Map<String, String>> responseList = response
                        .get(pollsBO.getPollId());

                Long optionVoted = votedOptions
                        .get(Long.parseLong(pollsBO.getPollId()));
                Long i = 1L;
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
                    
                    if (i.equals(optionVoted)) {
                        pollPercentElem.addAttribute("Voted", "true");
                    }
                    pollPercentElem.setText(optMap.get(POLLPCT));
                    i++;
                }
            }else {
                captchaResponseElement.setText("Invalid");
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
     * @param pollsBO    PollsBO Object.
     *
     * @param connection Connection Object.
     */
    public void insertPollResponse(PollsBO pollsBO, Postgre postgre) {
        logger.info("PollsExternal : insertPollResponse");
        String pollResponseQuery = "INSERT INTO POLL_RESPONSE (POLL_ID, "
                + "OPTION_ID, USER_ID, NLUID, LANG, VOTED_FROM, "
                + "USER_AGENT, VOTED_ON, PERSONA) VALUES(?,?,?,?,?,?,?,LOCALTIMESTAMP,?)";
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
            prepareStatement.setString(4, pollsBO.getNLUID());
            prepareStatement.setString(5, pollsBO.getLang());
            prepareStatement.setString(6, pollsBO.getVotedFrom());
            prepareStatement.setString(7, pollsBO.getUserAgent());
            prepareStatement.setString(8, pollsBO.getPersona());
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
     * @param pollsBO    PollsBO Object.
     * @param connection Connection Object.
     *
     * @return
     */
    public Map<String, List<Map<String, String>>> getPollResponse(
            PollsBO pollsBO, Postgre postgre, Set<String> votedPolls) {
        logger.info("getPollResponse()");

        String pollQuery = null;
        logger.debug("lang:: " + pollsBO.getLang());
        if (pollsBO.getLang().equalsIgnoreCase("en")) {
            pollQuery = "SELECT * FROM vw_poll_stats_en WHERE poll_id = ANY(?)";
        } else {
            pollQuery = "SELECT * FROM vw_poll_stats_ar WHERE poll_id = ANY(?)";
        }
        logger.debug("Poll Query ::" + pollQuery);
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

                logger.debug("Option Value::" + rs.getString(OPTION_VALUE));

                votedPolls.add(rs.getString(POLL_ID));

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

        logger.debug("Poll Result Map :: " + pollsResultMap.toString());
        return pollsResultMap;

    }

    /**
     * This method is used for checking Polls response data in database.
     * 
     * @param pollsBO    PollsBO Object.
     * @param connection Connection Object.
     * 
     * @return Returns comma seperated string containing voted poll ids.
     */
    public String checkResponseData(PollsBO pollsBO, Postgre postgre) {
        logger.info("checkResponseData()");
        StringBuilder checkVotedQuery = new StringBuilder(
                "SELECT POLL_ID FROM POLL_RESPONSE WHERE POLL_ID = ANY (?) ");
        StringJoiner votedPollIds = new StringJoiner(",");

        String[] pollIdsArr = pollsBO.getPollId().split(",");

        if (pollsBO.getUserId() != null
                && !"".equals(pollsBO.getUserId())) {
            checkVotedQuery.append(USERID_QUERY);
        } else if (pollsBO.getNLUID() != null
                && !"".equals(pollsBO.getNLUID())) {
            checkVotedQuery.append(NLUID_QUERY);
        }
        logger.debug("checkVotedQuery ::::" + checkVotedQuery.toString());
        Connection connection = null;
        PreparedStatement prepareStatement = null;
        ResultSet rs = null;

        try {
            connection = postgre.getConnection();
            prepareStatement = connection
                    .prepareStatement(checkVotedQuery.toString());
            prepareStatement.setArray(1,
                    connection.createArrayOf(BIGINT, pollIdsArr));
            if (StringUtils.isNotBlank(pollsBO.getUserId())) {
                prepareStatement.setString(2, pollsBO.getUserId());
            } else if (StringUtils.isNotBlank(pollsBO.getNLUID())) {
                prepareStatement.setString(2, pollsBO.getNLUID());
            }
            rs = prepareStatement.executeQuery();
            while (rs.next()) {
                votedPollIds.add(rs.getString(POLL_ID));
            }
            logger.debug("Voteed Polls : " + votedPollIds.toString());

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
     */
    public boolean setBO(final RequestContext context, PollsBO pollsBO, Postgre postgreObj) {

        boolean isValid = true;
        
        final String USER_AGENT = "User-Agent";
        
        final String BASEQUERY = "baseQuery";
        final String FIELDQUERY = "fieldQuery";
        final String START = "start";
        final String SORT = "sort";
        final String SOLRCORE = "solrCore";
        int errorCount = 0;
        
        logger.info("PollsExternal : Loading nluserid Properties....");
        PropertiesFileReader nluseridpropertyFileReader = new PropertiesFileReader(
                context, "NLUserCookie.properties");
        nluseridProp = nluseridpropertyFileReader
                .getPropertiesFile();
        logger.info("PollsExternal : nluserid Properties Loaded");
        
        request = context.getRequest();
        response = context.getResponse();
                
        logger.debug(BASEQUERY + " >>>"+context.getParameterString(BASEQUERY)+"<<<");
        logger.debug(FIELDQUERY + " >>>"+context.getParameterString(FIELDQUERY)+"<<<");
        logger.debug(START + " >>>"+context.getParameterString(START)+"<<<");
        logger.debug(SORT + " >>>"+context.getParameterString(SORT)+"<<<");
        logger.debug(SOLRCORE + " >>>"+context.getParameterString(SOLRCORE)+"<<<");
        
        HashMap<String, String> cookiesMap = (HashMap<String, String>) getCookiesMap(request);
        
        errorCount += validatePollAction(context, pollsBO);
        errorCount += validateLocale(context, pollsBO);
        errorCount += validateUserSession(context, pollsBO);
        errorCount += validateClientIpAddress(context, pollsBO);
        errorCount += validateNLUID(context, pollsBO, cookiesMap);
        pollsBO.setUserAgent(context.getRequest().getHeader(USER_AGENT));
        errorCount += validateCurrentPollRows(context, pollsBO);
        errorCount += validatePastPollRows(context, pollsBO);
                
        if(ACTION_VOTE.equalsIgnoreCase(pollsBO.getAction())) {
            errorCount += validatePollId(context, pollsBO);
        
            String captchaResponse = context.getParameterString("g-recaptcha-response");
            logger.debug("captchaResponse >>>" +captchaResponse+ "<<<");
            pollsBO.setCaptchaResponse(captchaResponse);
            
            errorCount += validateOption(context, pollsBO);
            errorCount += validateVotedFrom(context, pollsBO);
            errorCount += validatePersona(context, pollsBO, postgreObj, cookiesMap);
        }
        
        if(ACTION_POLLS_AND_SURVEY.equalsIgnoreCase(pollsBO.getAction()) || DASHBOARD.equalsIgnoreCase(pollsBO.getAction())) {
            
            errorCount += validatePollsGroup(context, pollsBO);             
            
            if(DASHBOARD.equalsIgnoreCase(pollsBO.getAction())) {                
                errorCount += validatePollsGroupConfig(context, pollsBO);               
                errorCount += validatePollGroupConfigCategory(context, pollsBO);
            }
            
            errorCount += validatePollGroupCategory(context, pollsBO);              
            errorCount += validatePollCategory(context, pollsBO);               
            errorCount += validateSolrPollCategory(context, pollsBO);               
            errorCount += validatePersona(context, pollsBO, postgreObj, cookiesMap);
        }
        
        if(errorCount > 0){
            isValid = false;
        }
        return isValid;
    }
    
    /**
     * This method is to validate pollAction and set BO.
     * 
     * @param context Request Context object.
     * @param polls business object.
     * 
     * @return Returns error count.
     */
    public int validatePollAction(final RequestContext context, PollsBO pollsBO){
        int errorCount = 0;
        String validData  = "";
        ValidationErrorList errorList = new ValidationErrorList();
        String pollAction = context.getParameterString(POLL_ACTION);
        logger.debug(POLL_ACTION + " >>>"+pollAction+"<<<");
        validData = ESAPI.validator().getValidInput(POLL_ACTION, pollAction,
                ESAPIValidator.ALPHABET, 20, false, true, errorList);
        if(errorList.isEmpty()) {
            pollsBO.setAction(validData);
        }else {
            logger.debug(errorList.getError(POLL_ACTION));
            errorCount = 1;
        }
        return errorCount;
    }
    
    /**
     * This method is to validate locale and set BO.
     * 
     * @param context Request Context object.
     * @param polls business object.
     * 
     * @return Returns error count.
     */
    public int validateLocale(final RequestContext context, PollsBO pollsBO){
        final String LOCALE = "locale";
        int errorCount = 0;
        String validData  = "";
        ValidationErrorList errorList = new ValidationErrorList();
        String locale = context.getParameterString(LOCALE, "en");
        logger.debug(LOCALE + " >>>" +locale+ "<<<");
        validData = ESAPI.validator().getValidInput(LOCALE, locale, ESAPIValidator.ALPHABET, 2,
                false, true, errorList);
        if(errorList.isEmpty()) {
            pollsBO.setLang(validData);
        }else {
            logger.debug(errorList.getError(LOCALE));
            errorCount = 1;
        }
        return errorCount;
    }
    
    /**
     * This method is to validate user session and set BO.
     * 
     * @param context Request Context object.
     * @param polls business object.
     * 
     * @return Returns error count.
     */
    public int validateUserSession(final RequestContext context, PollsBO pollsBO){
        final String USER_ID = "user_id";
        int errorCount = 0;
        UserInfoSession ui = new UserInfoSession();
        String valid = ui.getStatus(context);
        if(valid.equalsIgnoreCase("valid")) {
            if(request.getSession().getAttribute("userId") != null) {
                String userId = request.getSession().getAttribute("userId")
                        .toString();
                logger.debug(USER_ID + " >>>" + userId + "<<<");
                pollsBO.setUserId(userId);
            }else {
                logger.debug("UserId from session is null.");
            }
            
        }
        return errorCount;
    }
    
    /**
     * This method is to validate Client IP Address and set BO.
     * 
     * @param context Request Context object.
     * @param polls business object.
     * 
     * @return Returns error count.
     */
    public int validateClientIpAddress(final RequestContext context, PollsBO pollsBO){
        int errorCount = 0;
        String validData  = "";
        ValidationErrorList errorList = new ValidationErrorList();
        RequestHeaderUtils requestHeaderUtils = new RequestHeaderUtils(context);
        String ipAddress = requestHeaderUtils.getClientIpAddress();
        logger.debug("ipaddress >>>" +ipAddress+ "<<<");
        validData = ESAPI.validator().getValidInput("IPAddress", ipAddress,
                ESAPIValidator.IP_ADDRESS, 20, false, true, errorList);
        if(errorList.isEmpty()) {
            pollsBO.setIpAddress(validData);
        }else {
            logger.debug(errorList.getError("IPAddress"));
            errorCount = 1;
        }
        return errorCount;
    }
    
    /**
     * This method is to validate NLUID and set BO.
     * 
     * @param context Request Context object.
     * @param polls business object.
     * 
     * @return Returns error count.
     */
    public int validateNLUID(final RequestContext context, PollsBO pollsBO, Map<String, String> cookiesMap){
        int errorCount = 0;
        String validData  = "";
        ValidationErrorList errorList = new ValidationErrorList();
        String nlUID = cookiesMap.get(NLUID);
        logger.debug(NLUID + " >>>"+nlUID+"<<<");
        validData = ESAPI.validator().getValidInput(NLUID, nlUID,
                ESAPIValidator.ALPHANUMERIC_HYPHEN, 36, true, true, errorList);
        if(errorList.isEmpty()) {
            pollsBO.setNLUID(validData);
        }else {
            logger.debug(errorList.getError(NLUID));
            errorCount = 1;
        }
        return errorCount;
    }
    
    /**
     * This method is to validate Current Poll Rows and set BO.
     * 
     * @param context Request Context object.
     * @param polls business object.
     * 
     * @return Returns error count.
     */
    public int validateCurrentPollRows(final RequestContext context, PollsBO pollsBO){
        final String CURRENT_POLL_ROWS = "current_poll_rows";
        int errorCount = 0;
        String validData  = "";
        ValidationErrorList errorList = new ValidationErrorList();
        if(ACTION_CURRENT_POLLS.equalsIgnoreCase(pollsBO.getAction())) {
            String currentPollRows = context.getParameterString(CURRENT_POLL_ROWS);
            logger.debug(CURRENT_POLL_ROWS + " >>>" +currentPollRows+ "<<<");
            validData = ESAPI.validator().getValidInput(CURRENT_POLL_ROWS, currentPollRows,
                    ESAPIValidator.NUMERIC, 2, false, true, errorList);
            if(errorList.isEmpty()) {
                pollsBO.setCurrentPollsPerPage(validData);
            }else {
                logger.debug(errorList.getError(CURRENT_POLL_ROWS));
                errorCount = 1;
            }
        }
        return errorCount;
    }
    
    /**
     * This method is to validate Past Poll Rows and set BO.
     * 
     * @param context Request Context object.
     * @param polls business object.
     * 
     * @return Returns error count.
     */
    public int validatePastPollRows(final RequestContext context, PollsBO pollsBO){
        final String PAST_POLL_ROWS = "past_poll_rows";
        int errorCount = 0;
        String validData  = "";
        ValidationErrorList errorList = new ValidationErrorList();
        if(ACTION_PAST_POLLS.equalsIgnoreCase(pollsBO.getAction())) {
            String pastPollRows = context.getParameterString(PAST_POLL_ROWS);
            logger.debug(PAST_POLL_ROWS + " >>>" +pastPollRows+ "<<<");
            validData = ESAPI.validator().getValidInput(PAST_POLL_ROWS, pastPollRows,
                    ESAPIValidator.NUMERIC, 2, false, true, errorList);
            if(errorList.isEmpty()) {
                pollsBO.setPastPollsPerPage(validData);
            }else {
                logger.debug(errorList.getError(PAST_POLL_ROWS));
                errorCount = 1;
            }
        }
        return errorCount;
    }
    
    /**
     * This method is to validate Poll Id and set BO.
     * 
     * @param context Request Context object.
     * @param polls business object.
     * 
     * @return Returns error count.
     */
    public int validatePollId(final RequestContext context, PollsBO pollsBO){
        final String POLLID = "pollId";
        int errorCount = 0;
        String validData  = "";
        ValidationErrorList errorList = new ValidationErrorList();
        String pollId = context.getParameterString(POLLID);
        logger.debug(POLLID + " >>>" +pollId+ "<<<");
        validData = ESAPI.validator().getValidInput(POLLID, pollId, ESAPIValidator.NUMERIC, 200,
                false, true, errorList);
        if(errorList.isEmpty()) {
            pollsBO.setPollId(validData);
        }else {
            logger.debug(errorList.getError(POLLID));
            errorCount = 1;
        }
        return errorCount;
    }
    
    /**
     * This method is to validate Option and set BO.
     * 
     * @param context Request Context object.
     * @param polls business object.
     * 
     * @return Returns error count.
     */
    public int validateOption(final RequestContext context, PollsBO pollsBO){
        final String OPTION = "option";
        int errorCount = 0;
        String validData  = "";
        ValidationErrorList errorList = new ValidationErrorList();
        String option = context.getParameterString(OPTION);
        logger.debug(OPTION + " >>>" +option+ "<<<");
        validData = ESAPI.validator().getValidInput(OPTION, option, ESAPIValidator.NUMERIC, 2,
                false, true, errorList);
        if(errorList.isEmpty()) {
            pollsBO.setSelectedOption(validData);
        }else {
            logger.debug(errorList.getError(OPTION));
            errorCount = 1;
        }
        return errorCount;
    }
    
    
    /**
     * This method is to validate Voted From and set BO.
     * 
     * @param context Request Context object.
     * @param polls business object.
     * 
     * @return Returns error count.
     */
    public int validateVotedFrom(final RequestContext context, PollsBO pollsBO){
        final String VOTED_FROM = "votedFrom";
        int errorCount = 0;
        String validData  = "";
        ValidationErrorList errorList = new ValidationErrorList();
        String votedFrom = context.getParameterString(VOTED_FROM);
        logger.debug(VOTED_FROM + " >>>" +votedFrom+ "<<<");
        validData = ESAPI.validator().getValidInput(VOTED_FROM, votedFrom,
                ESAPIValidator.ALPHABET, 150, false, true, errorList);
        if(errorList.isEmpty()) {
            pollsBO.setVotedFrom(validData);
        }else {
            logger.debug(errorList.getError(VOTED_FROM));
            errorCount = 1;
        }
        return errorCount;
    }
    
    /**
     * This method is to validate Persona and set BO.
     * 
     * @param context Request Context object.
     * @param polls business object.
     * 
     * @return Returns error count.
     */
    public int validatePersona(final RequestContext context, PollsBO pollsBO, Postgre postgreObj, Map<String, String> cookiesMap){
        final String PERSONA = "persona";
        int errorCount = 0;
        String validData  = "";
        ValidationErrorList errorList = new ValidationErrorList();
        //Get Persona details from persona settings
        String persona = null;
        if(StringUtils.isNotBlank(pollsBO.getUserId())) {
            DashboardSettingsExternal dsExt = new DashboardSettingsExternal();
            persona = dsExt.getPersonaForUser(pollsBO.getUserId(), postgreObj);
            logger.debug("Persona from DB >>>" +persona+ "<<<");
            pollsBO.setPersona(persona);
        }
        
        if(!StringUtils.isNotBlank(persona)) {
            String personaValue = cookiesMap.get(PERSONA);
            logger.debug(PERSONA + " >>>" +personaValue+ "<<<");
            validData = ESAPI.validator().getValidInput(PERSONA, personaValue,
                    ESAPIValidator.ALPHABET_HYPEN, 200, true, true, errorList);
            if(errorList.isEmpty()) {
                pollsBO.setPersona(validData);
            }else {
                logger.debug(errorList.getError(PERSONA));
                errorCount = 1;
            }
        }
        return errorCount;
    }
    
    /**
     * This method is to validate Polls Group and set BO.
     * 
     * @param context Request Context object.
     * @param polls business object.
     * 
     * @return Returns error count.
     */
    public int validatePollsGroup(final RequestContext context, PollsBO pollsBO){
        final String POLLS_GROUP = "PollsGroup";
        int errorCount = 0;
        String pollsGroup = context.getParameterString(POLLS_GROUP);
        logger.debug(POLLS_GROUP + " >>>" +pollsGroup+ "<<<");
        if (!ESAPIValidator.checkNull(pollsGroup)) {
            pollsBO.setGroup(getContentName(pollsGroup));
        }
        return errorCount;
    }
    
    /**
     * This method is to validate Polls Group Config and set BO.
     * 
     * @param context Request Context object.
     * @param polls business object.
     * 
     * @return Returns error count.
     */
    public int validatePollsGroupConfig(final RequestContext context, PollsBO pollsBO){
        final String POLLS_GROUP_CONFIG = "PollsGroupConfig";
        int errorCount = 0;
        String pollsGroupConfig = context.getParameterString(POLLS_GROUP_CONFIG);
        logger.debug(POLLS_GROUP_CONFIG + " >>>" +pollsGroupConfig+ "<<<");
        if (!ESAPIValidator.checkNull(pollsGroupConfig)) {
            pollsBO.setPollGroupConfig(getContentName(pollsGroupConfig));
        }
        return errorCount;
    }
    
    /**
     * This method is to validate Poll Group Config Category and set BO.
     * 
     * @param context Request Context object.
     * @param polls business object.
     * 
     * @return Returns error count.
     */
    public int validatePollGroupConfigCategory(final RequestContext context, PollsBO pollsBO){
        final String POLL_GROUP_CONFIG_CATEGORY = "pollGroupConfigCategory";
        int errorCount = 0;
        String validData  = "";
        ValidationErrorList errorList = new ValidationErrorList();
        String pollGroupConfigCategory = context.getParameterString(POLL_GROUP_CONFIG_CATEGORY);
        logger.debug(POLL_GROUP_CONFIG_CATEGORY + " >>>" +pollGroupConfigCategory+ "<<<");
        validData = ESAPI.validator().getValidInput(POLL_GROUP_CONFIG_CATEGORY,
                pollGroupConfigCategory, ESAPIValidator.ALPHABET_HYPEN, 50, false, true,
                errorList);
        if(errorList.isEmpty()) {
            pollsBO.setPollGroupConfigCategory(validData);
        }else {
            logger.debug(errorList.getError(POLL_GROUP_CONFIG_CATEGORY));
            errorCount = 1;
        }
        return errorCount;
    }
    
    /**
     * This method is to validate Poll Group Category and set BO.
     * 
     * @param context Request Context object.
     * @param polls business object.
     * 
     * @return Returns error count.
     */
    public int validatePollGroupCategory(final RequestContext context, PollsBO pollsBO){
        final String POLL_GROUP_CATEGORY = "pollGroupCategory"; 
        int errorCount = 0;
        String validData  = "";
        ValidationErrorList errorList = new ValidationErrorList();
        String pollGroupCategory = context.getParameterString(POLL_GROUP_CATEGORY);
        logger.debug(POLL_GROUP_CATEGORY + " >>>" +pollGroupCategory+ "<<<");
        validData = ESAPI.validator().getValidInput(POLL_GROUP_CATEGORY, pollGroupCategory,
                ESAPIValidator.ALPHABET_HYPEN, 50, false, true, errorList);
        if(errorList.isEmpty()) {
            pollsBO.setGroupCategory(validData);
        }else {
            logger.debug(errorList.getError(POLL_GROUP_CATEGORY));
            errorCount = 1;
        }
        return errorCount;
    }
    
    /**
     * This method is to validate Poll Category and set BO.
     * 
     * @param context Request Context object.
     * @param polls business object.
     * 
     * @return Returns error count.
     */
    public int validatePollCategory(final RequestContext context, PollsBO pollsBO){
        final String POLL_CATEGORY = "pollCategory";
        int errorCount = 0;
        String validData  = "";
        ValidationErrorList errorList = new ValidationErrorList();
        String pollCategory = context.getParameterString(POLL_CATEGORY);
        logger.debug(POLL_CATEGORY + " >>>" +pollCategory+ "<<<");
        validData = ESAPI.validator().getValidInput(POLL_CATEGORY, pollCategory,
                ESAPIValidator.ALPHABET, 50, false, true, errorList);
        if(errorList.isEmpty()) {
            pollsBO.setCategory(validData);
        }else {
            logger.debug(errorList.getError(POLL_CATEGORY));
            errorCount = 1;
        }
        return errorCount;
    }
    
    
    /**
     * This method is to validate Solr Poll Category and set BO.
     * 
     * @param context Request Context object.
     * @param polls business object.
     * 
     * @return Returns error count.
     */
    public int validateSolrPollCategory(final RequestContext context, PollsBO pollsBO){
        final String SOLR_POLL_CATEGORY = "solrPollCategory";
        int errorCount = 0;
        String validData  = "";
        ValidationErrorList errorList = new ValidationErrorList();
        String solrPollCategory = context.getParameterString(SOLR_POLL_CATEGORY);
        logger.debug(SOLR_POLL_CATEGORY + " >>>" +solrPollCategory+ "<<<");
        validData = ESAPI.validator().getValidInput(SOLR_POLL_CATEGORY, solrPollCategory,
                ESAPIValidator.ALPHABET, 50, false, true, errorList);
        if(errorList.isEmpty()) {
            pollsBO.setSolrCategory(validData);
        }else {
            logger.debug(errorList.getError(SOLR_POLL_CATEGORY));
            errorCount = 1;
        }
        return errorCount;
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
     * This method is used to get all cookies as map.
     * 
     * @param request HttpServletRequest object.
     * 
     * @return Returns cookies as string key value pair map.
     */
    public Map<String, String> getCookiesMap(HttpServletRequest request) {
        Cookie[] cookies = null;
        HashMap<String, String> cookieMap = new HashMap<>();
        try {
            cookies = request.getCookies();
            if (cookies != null) {
                for (int i = 0; i < cookies.length; i++) {
                    Cookie cookie = cookies[i];
                    cookieMap.put(cookie.getName(), cookie.getValue());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cookieMap;
    }
}
