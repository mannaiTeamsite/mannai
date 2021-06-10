package com.hukoomi.livesite.external;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;


import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.ValidationErrorList;

import com.hukoomi.bo.DashboardSettingsBO;
import com.hukoomi.utils.ESAPIValidator;
import com.hukoomi.utils.MySql;
import com.hukoomi.utils.Postgre;
import com.interwoven.livesite.runtime.RequestContext;

/**
 * DashboardSettingsExternal is the components external class for dashboard settings.
 * 
 * @author Vijayaragavamoorthy
 *
 */
public class DashboardSettingsExternal {

    /** Logger object to check the flow of the code. */
    private static final Logger logger = Logger
            .getLogger(DashboardSettingsExternal.class);
    /**
     * Postgre Object variable.
     */
    Postgre postgre = null;
    
    /**
     * MySql Object variable.
     */
    MySql mysql = null;
    /**
     * Constant for action get persona settings.
     */
    public static final String ACTION_GET_ALL_SETTINGS = "getAllSettings";
    /**
     * Constant for action get persona settings.
     */
    public static final String ACTION_GET_PERSONA = "getPersonaForUser";
    /**
     * Constant for action update persona settings.
     */
    public static final String ACTION_UPDATE_PERSONA = "updatePersonaForUser";
    /**
     * Constant for action update persona settings.
     */
    public static final String ACTION_UPDATE_TOPICS = "updateTopicsForUser";
    /**
     * Constant for action update persona settings.
     */
    public static final String ACTION_UNSUBSCRIBE = "unsubscribe";
    /**
     * Constant for action update persona settings.
     */
    public static final String ACTION_UNSUBSCRIBE_NONLOGGED =
            "unsubscribeNonLogged";
    /**
     * Constant for action update Language switch.
     */
    public static final String ACTION_LANG_ONOFF = "langOnOff";    
    
    /**
     * Constant for language arabic.
     */
    public static final String LANGUAGE_ARABIC = "arabic";
    /**
     * Constant for language english.
     */
    public static final String LANGUAGE_ENGLISH = "english";
    /**
     * Constant for language switch.
     */
    public static final String SWITCH_LANGUAGE = "switchlang";
    /**
     * Constant for language switch.
     */
    public static final String LANGUAGE_ON = "On";
    /**
     * Constant for language switch.
     */
    public static final String LANGUAGE_OFF = "Off";
    /** 
     * phpList subscriber email 
     */
    private static final String SUBSCRIBER_EMAIL = "subscriber_email";
    /** 
     * phpList unsubscription reason 
     */
    private static final String UNSUBSCRIBE_REASON = "unsubscribe_reason";
    /**
     * Email email for unsubscription
     */
    private static final String ELEMENT_EMAIL = "email";
    
    /**
     * Email email for unsubscription
     */
    private static final String ELEMENT_UNSUB_LANG = "lang";
    /**
     * phpList language Switch value
     */
    private static final String LANG_SWITCH = "lang_switch";
    /**
     * Unsubscribe confirmation mail element.
     */
    private static final String UNSUBSCRIPTION_CONFIRMATION_EMAIL =
            "UnsubConfirmationEmail";
    /**
     * Parameter to get subscriberID.
     */
    private static final String USING_EMAIL =
            "usingEmail";
    /**
     * Parameter to get subscriberID..
     */
    private static final String USING_UID =
            "usingUID";
    /** 
     * pending status constant. 
     */
    private static final String STATUS_PENDING = "Pending";
    /**
     * Constant for status success.
     */
    public static final String STATUS_SUCCESS = "success";
    /**
     * Constant for status unsubscribed.
     */
    public static final String STATUS_UNSUBSCRIBED = "Unsubscribed";
    /**
     * Constant for status failed.
     */
    public static final String STATUS_FAILED = "failed";
    /** active status constant. */
    private static final String STATUS_ACTIVE = "Active";
    /** inActive status constant. */
    private static final String STATUS_INACTIVE = "InActive";
    /**
     * Constant for status.
     */
    public static final String STATUS = "status";
    /**
     * Constant for persona.
     */
    public static final String PERSONA = "persona";
    /**
     * Constant for user-id.
     */
    public static final String USER_ID = "user-id";
    /**
     * Constant for settings-action.
     */
    public static final String TOPICS = "topics";
    /**
     * Constant for error.
     */
    public static final String ERROR = "error";
    /**
     * Constant for status subscried.
     */
    public static final String STATUS_SUBSCRIBED = "subscribed";
    /** confirmation pending status constant. */
    private static final String CONFIRMATION_PENDING = "ConfirmationPending";
    /**
     * Constant for status not subscried.
     */
    public static final String STATUS_NOT_SUBSCRIBED = "notSubscribed";
    
    /**
     * Constant for table name.
     */
    public static final String NEWSLETTER_MASTER = "newsletter_master";
    
    /**
     * Constant for table name.
     */
    public static final String NEWSLETTER_PREFERENCE =
            "newsletter_preference";
   

     NewsletterPhpExternal phpExternal = null; 

    /**
     * This method will be called from Component External for fetching and updating the persona settings.
     * 
     * @param context Request context object.
     *
     * @return doc Returns the solr response document generated from solr query.
     * 
     * @deprecated
     */
    @Deprecated(since = "", forRemoval = false)
    public Document performDashboardSettingsAction(final RequestContext context) {
        logger.info("DashboardSettingsExternal : performPersonaSettingsAction()");
        DashboardSettingsBO settingsBO = new DashboardSettingsBO();
        Document doc = DocumentHelper.createDocument();
        Element responseElem = doc.addElement("dashboard-settings");
        double subscriberId = 0;
        double preferenceId = 0;
        final String SETTINGS_ACTION = "settingsAction";
        String langSwitch = context.getParameterString(LANG_SWITCH);
        String language = context.getParameterString(SWITCH_LANGUAGE);
        String email = context.getParameterString(ELEMENT_EMAIL);        
        String pageLang = context.getParameterString("lang");
        String unsubreason = context.getParameterString("unsubscribe_reason");
        String subStatus = "";
        phpExternal = new NewsletterPhpExternal();

        HttpServletRequest request = context.getRequest();
        logger.debug("Session Status : "
                + request.getSession().getAttribute("status"));
        String settingsAction =
                context.getParameterString(SETTINGS_ACTION);
        postgre = new Postgre(context);
        mysql = new MySql(context);
        if (ACTION_UNSUBSCRIBE_NONLOGGED
                .equalsIgnoreCase(settingsAction)) {
            logger.info("Unsubscribe Non Logged"+pageLang);
            
            subscriberId = getSubscriberID(email,USING_EMAIL);
            boolean bool = isEmailAlreadyExist(email);
            if(bool) {
                
                subStatus = getSubscriptionStatus(email,ACTION_UNSUBSCRIBE);
                if(subStatus.equals("Subscribed")) {
                    
                    String confirmationToken =
                            generateConfirmationToken(
                                    subscriberId, preferenceId, email);
                    phpExternal.sendConfirmationMail(email, "en",
                            confirmationToken,
                            UNSUBSCRIPTION_CONFIRMATION_EMAIL, unsubreason, context);
                    createTopicsResponseDoc(responseElem, null, null,
                            "",
                            STATUS_SUCCESS, "");
                    
                }else if (subStatus.equals("Pending")) {
                    
                    createTopicsResponseDoc(responseElem, null, null,
                            "",
                            STATUS_PENDING, "");
                }else if(subStatus.equals("Unsubscribed")) {
                    createTopicsResponseDoc(responseElem, null, null,
                            "",
                            STATUS_UNSUBSCRIBED, "");
                    
                }
                
            }else {
                
                createTopicsResponseDoc(responseElem, null, null,
                        "",
                        STATUS_NOT_SUBSCRIBED, "");
            }
            return doc;
        }
        if (request.getSession().getAttribute("status") != null && "valid"
                .equals(request.getSession().getAttribute("status"))) {            
            
            if (validateInput(context, settingsBO)) {
                logger.info("Input data validation is successfull");
                logger.debug("settingsBO : "+settingsBO);
                
                if(settingsBO.getUserType() != null && !"".equals(settingsBO.getUserType())) {
                    Element userTypeElem = responseElem.addElement("user-type");
                    userTypeElem.addText(settingsBO.getUserType());
                }
                
                if (ACTION_GET_ALL_SETTINGS.equalsIgnoreCase(settingsBO.getAction())) {
                    getAllSettings(responseElem, settingsBO);
                    getTopicSettings(responseElem, settingsBO);
                } else if (ACTION_UPDATE_PERSONA.equalsIgnoreCase(settingsBO.getAction())) {
                    updatePersonaForUser(responseElem, settingsBO);
                } else if (ACTION_UPDATE_TOPICS
                        .equalsIgnoreCase(settingsBO.getAction())) {
                    logger.info("Topic Update Action");
                    String topics = context.getParameterString(TOPICS);
                    processTopicsOperations(responseElem, settingsBO, topics);
                } else if (ACTION_UNSUBSCRIBE
                        .equalsIgnoreCase(settingsBO.getAction())) {
                    logger.info("Unsubscribe Action");  
                    String unsubReason = context.getParameterString("UNSUBSCRIBE_REASON");
                    unsubscribeDashboardUser(settingsBO.getUserId(),unsubReason);
                } else if (ACTION_LANG_ONOFF
                        .equalsIgnoreCase(settingsBO.getAction())) {
                        logger.info("Switch Language Action");                      
                        switchDashboardLanguage(settingsBO.getUserId(),langSwitch,language);
                }
                
            }else {
                logger.info("Invalid input parameter");
                createResponseDoc(responseElem, settingsBO.getAction(), settingsBO.getAction(), settingsBO.getPersona(), STATUS_FAILED, "Invalid input parameter");
            }
        }else {
            logger.info("Invalid session - Redirecting to Login Page");
            try {
                DashboardExternal de = new DashboardExternal();
                de.redirectToLoginPage(context);
            } catch (Exception e) {
                logger.error("Exception in performDashboardSettingsAction - Redirecting to Login Page", e);
            }
        }
        logger.debug("Final Result :" + doc.asXML());
        return doc;
    }
    
    /**
     * @author Arbaj
     * 
     * @param responseElem
     * @param topics
     */
    public void processTopicsOperations(Element responseElem,
            DashboardSettingsBO settingsBO, String topics) {

        if (isTopicsExist(settingsBO.getUserId())) {
            updateTopicsData(responseElem, settingsBO, topics);
        } else {
            insertTopicsData(responseElem, settingsBO, topics);
        }
    }
    
    /**
     * @author Pramesh
     * @param userId
     * @param langSwitch
     * @param language
     * @return
     */
    private String switchDashboardLanguage(
            String userId, String langSwitch, String language
    ) {
        logger.info(
                "DashboardSettingsExternal : switchDashboardLanguage()");

        String status = "";
        double subscriberID = getSubscriberID(userId,USING_UID);
        String updatePreferenceQuery = "";

        Connection connection = null;
        PreparedStatement prepareStatement = null;
        ResultSet rs = null;
        String prefStatus = "";

        if (langSwitch.equals(LANGUAGE_ON)) {
            prefStatus = "Active";
            updatePreferenceQuery =
                    "UPDATE NEWSLETTER_PREFERENCE SET STATUS = ? WHERE SUBSCRIBER_ID = ? AND LANGUAGE = ?";
        } else if (langSwitch.equals(LANGUAGE_OFF)) {
            prefStatus = "InActive";
            updatePreferenceQuery =
                    "UPDATE NEWSLETTER_PREFERENCE SET STATUS = ? WHERE SUBSCRIBER_ID = ? AND LANGUAGE = ?";
        }

        try {
            connection = postgre.getConnection();
            prepareStatement =
                    connection.prepareStatement(updatePreferenceQuery);

            prepareStatement.setString(1, prefStatus);
            prepareStatement.setDouble(2, subscriberID);
            prepareStatement.setString(3, language);
            rs = prepareStatement.executeQuery();

            while (rs.next()) {
                status = STATUS_SUCCESS;
                updatePhpLanguageList(getList(userId, language),
                        subscriberID, langSwitch);
            }

        } catch (Exception e) {
            logger.error("Exception in unsubscribePostgreUser", e);
        } finally {
            postgre.releaseConnection(connection, prepareStatement, rs);
        }

        logger.debug(
                "switchDashboardLanguage() : update preference status "
                        + status);

        return status;
    }

    private String updatePhpLanguageList(
            List<String> listName, double subscriberID, String langSwitch
    ) {
        logger.info("DashboardSettingsExternal : updatePhpLanguageList()");

        String status = "";
        Connection connection = null;
        PreparedStatement prepareStatement = null;
        ResultSet rs = null;
        String phpQuery = "";
        String subscriberemail = getSubscriberEmail(subscriberID);
        if (langSwitch.equals(LANGUAGE_ON)) {
            phpQuery =
                    "INSERT INTO PHPLIST_LISTUSER (USERID, LISTID, ENTERED) VALUES ((SELECT ID FROM PHPLIST_USER_USER WHERE EMAIL = ?),(SELECT ID FROM PHPLIST_LIST WHERE NAME = ?),LOCALTIMESTAMP)";

        } else if (langSwitch.equals(LANGUAGE_OFF)) {

            phpQuery =
                    "DELETE FROM PHPLIST_LISTUSER WHERE USERID = ? and LISTID = (SELECT ID FROM PHPLIST_LIST WHERE NAME = ?)";
        }

        try {
            connection = mysql.getConnection();
            prepareStatement = connection.prepareStatement(phpQuery);

            Iterator<String> iteratorName = listName.iterator();
            while (iteratorName.hasNext()) {
                prepareStatement.setString(1, subscriberemail);
                prepareStatement.setString(2, iteratorName.next());
                rs = prepareStatement.executeQuery();
            }

        } catch (Exception e) {
            logger.error("Exception in unsubscribePhpUser", e);
        } finally {
            postgre.releaseConnection(connection, prepareStatement, rs);
        }

        return status;
    }

    private List<String> getList(String userId, String language) {
        logger.info("DashboardSettingsExternal : getList()");

        String listName = "";
        double subscriberID = getSubscriberID(userId,USING_UID);
        String getListQuery = "";

        Connection connection = null;
        PreparedStatement prepareStatement = null;
        ResultSet rs = null;

        List<String> list = new ArrayList<String>();
        getListQuery =
                "SELECT PERSONA FROM NEWSLETTER_PREFERENCE WHERE SUBSCRIBER_ID = ? and LANGUAGE = ?";

        try {
            connection = postgre.getConnection();
            prepareStatement = connection.prepareStatement(getListQuery);

            prepareStatement.setDouble(1, subscriberID);
            prepareStatement.setString(2, language);
            rs = prepareStatement.executeQuery();

            while (rs.next()) {
                listName = rs.getString("PERSONA") + "_" + language;
                list.add(listName);
            }

        } catch (Exception e) {
            logger.error("Exception in unsubscribePostgreUser", e);
        } finally {
            postgre.releaseConnection(connection, prepareStatement, rs);
        }

        logger.debug("getList() : size " + list.size());

        return list;
    }

    /**
     * @author Pramesh
     * @param userId
     * @return
     * 
     *         This method unssubscribes dashboard user and update status
     */
    private String
            unsubscribeDashboardUser(String userId, String unsubReason) {
        logger.info(
                "DashboardSettingsExternal : unsubscribeDashboardUser()");

        String unsubStatus = "";
        String status = "";
        int blacklistID = 1;
        int subscriberID = getSubscriberID(userId,USING_UID);
        String subscriberemail = getSubscriberEmail(subscriberID);

        String updateMasterQuery =
                "UPDATE NEWSLETTER_MASTER SET STATUS = \"Unsubscribed\" and UNSUBSCRIBED_REASON = ? WHERE UID = ?";
        String updatePreferenceQuery =
                "UPDATE NEWSLETTER_PREFERENCE SET STATUS = \"InActive\" WHERE SUBSCRIBER_ID = ?";
        String updatePhpQuery =
                "UPDATE PHPLIST_USER_USER SET BLACKLISTED = ? WHERE ID = (SELECT ID FROM PHPLIST_USER_USER WHERE EMAIL = ?)";
        status = unsubscribePostgreUser(updateMasterQuery, userId,
                NEWSLETTER_MASTER, unsubReason);
        logger.debug("unsubscribeDashboardUser() : update master status "
                + status);
        if (status.equals(STATUS_SUCCESS)) {

            status = unsubscribePostgreUser(updatePreferenceQuery,
                    Integer.toString(subscriberID), NEWSLETTER_PREFERENCE,
                    unsubReason);
        }
        logger.debug(
                "unsubscribeDashboardUser() : update preference status "
                        + status);

        if (status.equals(STATUS_SUCCESS)) {

            unsubStatus =
                    unsubscribePhpUser(updatePhpQuery, subscriberemail);
        }

        return unsubStatus;
    }
    
    /**
     * @author Pramesh
     * @param Query
     * @param userId
     * @param tabName
     * @return
     * 
     *         This method is used to update unsubscribed user
     */
    private String unsubscribePostgreUser(
            String Query, String userId, String tabName, String unsubReason
    ) {
        logger.info(
                "DashboardSettingsExternal : unsubscribePostgreUser()");

        String status = "";

        Connection connection = null;
        PreparedStatement prepareStatement = null;
        ResultSet rs = null;

        try {
            connection = postgre.getConnection();
            prepareStatement = connection.prepareStatement(Query);
            if (tabName.equals(NEWSLETTER_PREFERENCE)) {

                prepareStatement.setInt(1, Integer.parseInt(userId));
            } else {

                prepareStatement.setString(1, unsubReason);
                prepareStatement.setString(2, userId);
            }

            rs = prepareStatement.executeQuery();

            while (rs.next()) {
                status = STATUS_SUCCESS;
            }

        } catch (Exception e) {
            logger.error("Exception in unsubscribePostgreUser", e);
        } finally {
            postgre.releaseConnection(connection, prepareStatement, rs);
        }

        return status;
    }

    /**
     * @author Pramesh
     * @param Query
     * @param userId
     * @return
     */
    private String unsubscribePhpUser(String Query, String email) {
        logger.info("DashboardSettingsExternal : unsubscribePhpUser()");

        String status = "";
        int blacklistID = 1;
        Connection connection = null;
        PreparedStatement prepareStatement = null;
        ResultSet rs = null;

        try {
            connection = mysql.getConnection();
            prepareStatement = connection.prepareStatement(Query);

            prepareStatement.setInt(1, blacklistID);
            prepareStatement.setString(2, email);

            rs = prepareStatement.executeQuery();

            while (rs.next()) {
                status = STATUS_SUCCESS;
            }

        } catch (Exception e) {
            logger.error("Exception in unsubscribePhpUser", e);
        } finally {
            postgre.releaseConnection(connection, prepareStatement, rs);
        }

        return status;
    }

    /**
     * @author pramesh
     * @param subscriberId
     * @return
     * 
     *         This method get the subscriber email based on subscriber id
     */
    private String getSubscriberEmail(double subscriberId) {
        logger.info("NewsletterConfirmation : getSubscriberEmail");
        boolean subscriberPreferenceDataInsert = false;
        String addSubscriberPreferencesQuery =
                "SELECT SUBSCRIBER_EMAIL FROM NEWSLETTER_MASTER WHERE SUBSCRIBER_ID = ?";
        Connection connection = null;
        PreparedStatement prepareStatement = null;
        String subscriberEmail = "";
        ResultSet rs = null;
        try {
            connection = postgre.getConnection();
            prepareStatement = connection
                    .prepareStatement(addSubscriberPreferencesQuery);
            prepareStatement.setDouble(1, subscriberId);

            rs = prepareStatement.executeQuery();
            while (rs.next()) {

                subscriberEmail = rs.getString(SUBSCRIBER_EMAIL);
            }
        } catch (Exception e) {
            logger.error("Exception in updateMasterTable", e);
        } finally {
            postgre.releaseConnection(connection, prepareStatement, null);
        }

        return subscriberEmail;
    }

    /**
     * @author Pramesh
     * @param userId
     * @return This method is used to fetch subscriberID using UID
     */
    private int getSubscriberID(String userId, String condition) {
        logger.info("DashboardSettingsExternal : getSubscriberID()");
        
        String subscriberIDQuery = "";
        
        if(condition.equals(USING_UID)) {
            subscriberIDQuery =
                    "SELECT SUBSCRIBER_ID FROM NEWSLETTER_MASTER WHERE UID = ?";
            
        }else if(condition.equals(USING_EMAIL)) {
             subscriberIDQuery =
                    "SELECT SUBSCRIBER_ID FROM NEWSLETTER_MASTER WHERE SUBSCRIBER_EMAIL = ?";
            
        }
       
        int subscriberID = 0;
        Connection connection = null;
        PreparedStatement prepareStatement = null;
        ResultSet rs = null;

        try {
            connection = postgre.getConnection();
            prepareStatement =
                    connection.prepareStatement(subscriberIDQuery);
            prepareStatement.setString(1, userId);
            rs = prepareStatement.executeQuery();

            while (rs.next()) {
                subscriberID = rs.getInt(1);
            }

            logger.debug("subscriberID : " + subscriberID);

        } catch (Exception e) {
            logger.error("Exception in subscriberID", e);
        } finally {
            postgre.releaseConnection(connection, prepareStatement, rs);
        }
        return subscriberID;
    }

    /**
     * @author Arbaj
     * @param topics
     */
    public void insertTopicsData(Element responseElem,
            DashboardSettingsBO settingsBO,
            String topics) {
        logger.info("DashboardSettingsExternal : insertTopicsData()");
        Connection connection = postgre.getConnection();
        PreparedStatement prepareStatement = null;
        String personaSettingsQuery = null;
        String topicsArray[] = topics.split(",");
        
        Map<String, String> subscriptionDetails = getSubscriptionDetails(
                settingsBO.getUserId());
        logger.debug("subscriptionDetailsMap : "
                + subscriptionDetails.toString());

        try {
            personaSettingsQuery = "INSERT INTO NEWSLETTER_INTEREST (SUBSCRIBER_ID, UID, TOPIC_INTEREST_NAME, STATUS) VALUES (?, ?, ?, ?)";
            prepareStatement = connection
                    .prepareStatement(personaSettingsQuery);
            
            for (int index = 0; index < topicsArray.length; index++) {
                prepareStatement.setDouble(1, Double.parseDouble(
                        subscriptionDetails.get("subscriberId")));
                prepareStatement.setString(2,
                        subscriptionDetails.get("userId"));
                prepareStatement.setString(3, topicsArray[index]);
                prepareStatement.setString(4, STATUS_ACTIVE);
                prepareStatement.addBatch();
            }

            int result = prepareStatement.executeUpdate();
            String personaValue = getPersonaForUser(settingsBO.getUserId(),
                    postgre);
            if (result == 0) {
                logger.info(
                        "Newsletter interest topics insertion failed");
                createTopicsResponseDoc(responseElem, null, null,
                        personaValue,
                        STATUS_FAILED,
                        "Newsletter interest topics insertion failed");
            } else {
                logger.info(
                        "Newsletter interest topics inserted successfully!");
                createTopicsResponseDoc(responseElem, null, null,
                        personaValue,
                        STATUS_SUCCESS, "");
            }
        } catch (Exception e) {
            logger.error("Exception in insertTopicsData", e);
            createTopicsResponseDoc(responseElem, null, null, null,
                    STATUS_FAILED,
                    "Exception in insertTopicsData");
        } finally {
            postgre.releaseConnection(connection, prepareStatement, null);
        }

    }


    /**
     * @author Arbaj
     * @param topics
     */
    public void updateTopicsData(Element responseElem,
            DashboardSettingsBO settingsBO,
            String topics) {
        logger.info("DashboardSettingsExternal : updateTopicsData()");

        Connection connection = postgre.getConnection();
        PreparedStatement prepareStatement = null;
        String personaSettingsQuery = null;
        String inputTopicsArray[] = topics.split(",");
        List<String> databaseTopicsList = Arrays.asList(
                getTopicsInterest(settingsBO.getUserId()).split(","));

        logger.debug("Input Topics : " + inputTopicsArray.toString());
        logger.debug("Database Topics : " + databaseTopicsList.toString());

        Map<String, String> subscriptionDetails = getSubscriptionDetails(
                settingsBO.getUserId());

        try {
            personaSettingsQuery = "UPDATE NEWSLETTER_INTEREST SET SUBSCRIBER_ID = ? , TOPIC_INTEREST_NAME= ?, STATUS = ? WHERE UID = ?";
            prepareStatement = connection
                    .prepareStatement(personaSettingsQuery);

            for (int index = 0; index < inputTopicsArray.length; index++) {
                // If topic is not available in Database, inserting it
                if (!databaseTopicsList
                        .contains(inputTopicsArray[index])) {
                    insertTopicsData(responseElem, settingsBO,
                            inputTopicsArray[index]);
                }
                // If topic is already available in database
                else if (databaseTopicsList
                        .contains(inputTopicsArray[index])) {
                    prepareStatement.setDouble(1, Double.parseDouble(
                            subscriptionDetails.get("subscriberId")));
                    prepareStatement.setString(2, inputTopicsArray[index]);
                    prepareStatement.setString(3, STATUS_ACTIVE);
                    prepareStatement.setString(4,
                            subscriptionDetails.get("userId"));
                    prepareStatement.addBatch();
                }
                // If topic is available in database but removed from frontend request,
                // updating status
                else {
                    prepareStatement.setDouble(1, Double.parseDouble(
                            subscriptionDetails.get("subscriberId")));
                    prepareStatement.setString(2, inputTopicsArray[index]);
                    prepareStatement.setString(3, STATUS_INACTIVE);
                    prepareStatement.setString(4,
                            subscriptionDetails.get("userId"));
                    prepareStatement.addBatch();
                }

            }

            int result = prepareStatement.executeUpdate();
            String personaValue = getPersonaForUser(settingsBO.getUserId(),
                    postgre);
            if (result == 0) {
                logger.info("Newsletter interest topics insertion failed");
                createTopicsResponseDoc(responseElem, null, null,
                        personaValue, STATUS_FAILED,
                        "Newsletter interest topics insertion failed");
            } else {
                logger.info(
                        "Newsletter interest topics inserted successfully!");
                createTopicsResponseDoc(responseElem, null, null,
                        personaValue, STATUS_SUCCESS, "");
            }
        } catch (Exception e) {
            logger.error("Exception in updateTopicsData", e);
            createTopicsResponseDoc(responseElem, null, null, null,
                    STATUS_FAILED, "Exception in updateTopicsData");
        } finally {
            postgre.releaseConnection(connection, prepareStatement, null);
        }
    }

    /**
     * @author Arbaj
     * @param userId
     * @return
     */
    private boolean isTopicsExist(String userId) {
        logger.info("DashboardSettingsExternal : isTopicsExist()");

        boolean isTopicsExist = false;

        String personaSettingsQuery = "SELECT COUNT(*) FROM newsletter_interest WHERE UID = ?";

        Connection connection = null;
        PreparedStatement prepareStatement = null;
        ResultSet rs = null;

        try {
            connection = postgre.getConnection();
            prepareStatement = connection
                    .prepareStatement(personaSettingsQuery);
            prepareStatement.setString(1, userId);
            rs = prepareStatement.executeQuery();
            int count = 0;
            while (rs.next()) {
                count = rs.getInt(1);
            }

            if (count > 0) {
                isTopicsExist = true;
            }
            logger.debug(
                    "isTopicsExist : " + isTopicsExist);

        } catch (Exception e) {
            logger.error("Exception in isTopicsExist", e);
        } finally {
            postgre.releaseConnection(connection, prepareStatement, rs);
        }
        return isTopicsExist;
    }

    /**
     * @author Arbaj
     * 
     *         This method is used to get topic settings for Newsletter.
     * 
     * @param responseElem
     * @param settingsBO
     */
    public void getTopicSettings(Element responseElem,
            DashboardSettingsBO settingsBO) {
        logger.info("DashboardSettingsExternal : getTopicSettings()");
        getSubscriptionDetails(settingsBO.getUserId());
        // String subscriptionStatus = getSubscriptionStatus(
        // settingsBO.getUserId());
        String subscriptionStatus = getSubscriptionStatus(
                settingsBO.getUserId(), TOPICS);
        String topics = "";

        if ("Subscribed".equals(subscriptionStatus)) {
            // String topics = getTopicsInterest(settingsBO.getUserId());
            topics = getTopicsInterest(settingsBO.getUserId());


            createTopicsResponseDoc(responseElem, topics,
                    settingsBO.getUserId(), settingsBO.getPersona(),
                    STATUS_SUBSCRIBED, "");

        } else if ("Pending".equals(subscriptionStatus)) {
            createTopicsResponseDoc(responseElem, topics,
                    settingsBO.getUserId(), settingsBO.getPersona(),
                    CONFIRMATION_PENDING, "");
        } else {
            createTopicsResponseDoc(responseElem, topics,
                    settingsBO.getUserId(), settingsBO.getPersona(),
                    STATUS_NOT_SUBSCRIBED, "");
        }

    }

    /**
     * @param userId
     * @return
     */
    private Map<String, String> getSubscriptionDetails(String userId) {
        logger.info("DashboardSettingsExternal : getSubscriptionDetails()");
        
        Map<String, String> subscriptionDetails = new HashMap<String, String>();
        
        String subscriptionDetailsQuery = 
                "SELECT SUBSCRIBER_ID, SUBSCRIBER_EMAIL, STATUS, SUBSCRIBED_DATE, UID FROM NEWSLETTER_MASTER WHERE UID = ?"; 
        logger.info(
                "subscriptionDetailsQuery : " + subscriptionDetailsQuery);
        
        Connection connection = null;
        PreparedStatement prepareStatement = null;
        ResultSet rs = null;

        
        try {
            connection = postgre.getConnection();
            prepareStatement = connection
                    .prepareStatement(subscriptionDetailsQuery);
            prepareStatement.setString(1, userId);
            rs = prepareStatement.executeQuery();

            while (rs.next()) {
                subscriptionDetails.put("subscriberId", rs.getString(1));
                subscriptionDetails.put("subscriberEmail",
                        rs.getString(2));
                subscriptionDetails.put(STATUS, rs.getString(3));
                subscriptionDetails.put("subscribedDate", rs.getString(4));
                subscriptionDetails.put("userId", rs.getString(5));
            }

        } catch (Exception e) {
            logger.error("Exception in getSubscriptionDetails", e);
        } finally {
            postgre.releaseConnection(connection, prepareStatement, rs);
        }
        
        return subscriptionDetails;
    }

    /**
     * @author Arbaj
     * 
     * @param userId
     */
    public String getTopicsInterest(String userId) {
        logger.info("DashboardSettingsExternal : getTopicsInterest()");
        String getTopicsQuery = "SELECT TOPIC_INTEREST_NAME FROM NEWSLETTER_INTEREST WHERE UID = ?";
        StringJoiner topics = new StringJoiner(",");

        logger.debug("getTopicsQuery ::" + getTopicsQuery.toString());
        Connection connection = null;
        PreparedStatement prepareStatement = null;
        ResultSet rs = null;

        try {
            connection = postgre.getConnection();
            prepareStatement = connection.prepareStatement(getTopicsQuery);
            prepareStatement.setString(1, userId);

            rs = prepareStatement.executeQuery();
            while (rs.next()) {
                topics.add(rs.getString(1));
            }
            logger.debug("Topic From DB : " + topics.toString());

        } catch (Exception e) {
            logger.error("Exception in getTopicsInterest", e);
        } finally {
            postgre.releaseConnection(connection, prepareStatement, rs);
        }
        return topics.toString();
    }

    /**
     * @author Arbaj
     * 
     *         This method is used to create the topics response document based on
     *         the input.
     * 
     * @param responseElem
     * @param action
     * @param userId
     * @param persona
     * @param status
     * @param error
     */
    private void createTopicsResponseDoc(Element responseElem,
            String topics, String userId, String persona, String status,
            String error) {
        if (topics == null)
            topics = "";
        if (userId == null)
            userId = "";
        if (persona == null)
            persona = "";
        if (status == null)
            status = "";
        if (error == null)
            error = "";

        Element newsletterResponseElem = responseElem
                .addElement("newsletter-settings");

        newsletterResponseElem.addElement(STATUS).setText(status);
        newsletterResponseElem.addElement(TOPICS).setText(topics);
        newsletterResponseElem.addElement(USER_ID).setText(userId);
        newsletterResponseElem.addElement(PERSONA).setText(persona);
        newsletterResponseElem.addElement(ERROR).setText(error);

    }

    /**
     * @author Arbaj
     * 
     *         This method is used to get subscription status for Newsletter.
     * 
     * @param userId
     * 
     * @return
     */
    private String getSubscriptionStatus(String userId, String action) {
        logger.info("DashboardSettingsExternal : getSubscriptionStatus()");
        String subscriptionStatus = null;
        String getSubscriptionStatusQuery = "";
        if(action.equals(TOPICS)) {
            getSubscriptionStatusQuery = "SELECT STATUS FROM NEWSLETTER_MASTER WHERE UID = ?";
        }else {
            getSubscriptionStatusQuery = "SELECT STATUS FROM NEWSLETTER_MASTER WHERE SUBSCRIBER_EMAIL = ?";
        }
        
        Connection connection = null;
        PreparedStatement prepareStatement = null;

        try {
            connection = postgre.getConnection();
            prepareStatement = connection
                    .prepareStatement(getSubscriptionStatusQuery);
            prepareStatement.setString(1, userId);

            ResultSet resultSet = prepareStatement.executeQuery();
            while(resultSet.next())
            {
                subscriptionStatus = resultSet.getString(1);
            }            

        } catch (Exception e) {
            logger.error("Exception in getSubscriptionStatus", e);
        } finally {
            postgre.releaseConnection(connection, prepareStatement, null);
        }
        return subscriptionStatus;
    }

    /**
     * This method is used to validate the input.
     * 
     * @param context Request context object.
     * 
     * @return Returns true if the input is valid
     *         else returns false.
     * 
     */
    public boolean validateInput(final RequestContext context, DashboardSettingsBO settingsBO) {
        logger.info("DashboardSettingsExternal : validateInput()");
        
        ValidationErrorList errorList = new ValidationErrorList();
        final String SETTINGS_ACTION = "settingsAction";
        final String USER_ID = "userId";
        final String PERSONA = "persona";
        final String USER_ROLE = "userRole";
        final String USER_TYPE = "userType";
        final String EMAIL = "email";
        String validData  = "";
        
        String settingsAction = context.getParameterString(SETTINGS_ACTION);
        logger.debug(SETTINGS_ACTION + " >>>"+settingsAction+"<<<");
        validData  = ESAPI.validator().getValidInput(SETTINGS_ACTION, settingsAction, ESAPIValidator.ALPHABET, 20, false, true, errorList);
        if(errorList.isEmpty()) {
            settingsBO.setAction(validData);
        }else {
            logger.info(errorList.getError(SETTINGS_ACTION));
            return false;
        }
        
        HttpServletRequest request = context.getRequest();
       // logger.debug("Session Status : "+request.getSession().getAttribute("status"));
       // if(request.getSession().getAttribute("status") != null && "valid".equals(request.getSession().getAttribute("status"))) {
            
        if(request.getSession().getAttribute("userId") != null && !"".equals(request.getSession().getAttribute("userId"))) {
                String userId = request.getSession().getAttribute("userId").toString();
                logger.debug(USER_ID + " >>>"+userId+"<<<");
                settingsBO.setUserId(userId);
            }else {
                logger.debug("UserId from session is null.");
            }
            
            if(request.getSession().getAttribute("userType") != null) {
                String userType = request.getSession().getAttribute("userType").toString();
                logger.debug(USER_TYPE + " >>>"+userType+"<<<");
                validData  = ESAPI.validator().getValidInput(USER_TYPE, userType, ESAPIValidator.ALPHABET, 10, true, true, errorList);
                if(errorList.isEmpty()) {
                    settingsBO.setUserType(validData);
                }else {
                    logger.debug(errorList.getError(USER_ROLE));
                    return false;
                }
            }else {
                logger.debug("UserType from session is null.");
            }
        //}
        //settingsBO.setUserId("0");
        
        if (ACTION_UPDATE_PERSONA.equalsIgnoreCase(settingsAction)) {
            String persona = context.getParameterString(PERSONA);
            logger.debug(PERSONA + " >>>"+persona+"<<<");
            validData  = ESAPI.validator().getValidInput(PERSONA, persona, ESAPIValidator.ALPHABET_HYPEN, 50, false, true, errorList);
            if(errorList.isEmpty()) {
                settingsBO.setPersona(validData);
            }else {
                logger.debug(errorList.getError(PERSONA));
                return false;
            }
        }
        return true;
    }
    
    
    /**
     * This method is used to retrieve all setting for the user from database.
     *
     * @return returns Document with the all dashboard settings information  
     */
    public void getAllSettings(Element responseElem, DashboardSettingsBO settingsBO) {
        logger.info("DashboardSettingsExternal : getAllSettings()");
        try {
            String personaValue = getPersonaForUser(settingsBO.getUserId(), postgre);
            logger.debug("Persona Value :" + personaValue);
            if (personaValue != null && !"".equals(personaValue)) {
                createResponseDoc(responseElem, settingsBO.getAction(), settingsBO.getUserId(), personaValue, STATUS_SUCCESS, "");
            } else {
                createResponseDoc(responseElem, settingsBO.getAction(), settingsBO.getUserId(), personaValue, STATUS_FAILED, "Persona value not available");
            }
        } catch (Exception e) {
            logger.error("Exception in getAllSettings", e);
        } 
    }
    
    
    /**
     * This method is used to retrieve person setting for the user from database.
     *
     * @return returns the persona value  
     */
    public String getPersonaForUser(String userId, Postgre postgre) {
        logger.info("DashboardSettingsExternal : getPersonaForUser()");
        String personaValue = null;
        String personaQuery = "SELECT * FROM persona_settings WHERE user_id = ? AND active = True";
        logger.debug("personaQuery : " + personaQuery);
        Connection connection = null;
        PreparedStatement prepareStatement = null;
        ResultSet rs = null;

        try {
            connection = postgre.getConnection();
            prepareStatement = connection.prepareStatement(personaQuery);
            prepareStatement.setString(1, userId);
            rs = prepareStatement.executeQuery();
            while (rs.next()) {
                personaValue = rs.getString("persona_value");
                logger.debug("Persona Value :" + personaValue);
            }
        } catch (Exception e) {
            logger.error("Exception in getPersonaForUser", e);
        } finally {
            postgre.releaseConnection(connection, prepareStatement, rs);
        }

        return personaValue;
    }
    
    
    /**
     * This method is used to retrieve person setting for the user from database.
     *
     * @return returns Document with the persona value  
     */
    public void updatePersonaForUser(Element responseElem, DashboardSettingsBO settingsBO) {
        logger.info("DashboardSettingsExternal : updatePersonaForUser()");
        Connection connection = postgre.getConnection();
        PreparedStatement prepareStatement = null;
        String personaSettingsQuery = null;
        
        if(isPersonaSettingExists(settingsBO.getUserId())) {
            try {
                personaSettingsQuery = "UPDATE persona_settings SET persona_value = ?, active = ?, modified_on = current_timestamp WHERE user_id = ?";
                prepareStatement = connection.prepareStatement(personaSettingsQuery);
                prepareStatement.setString(1, settingsBO.getPersona());
                prepareStatement.setBoolean(2, true);
                prepareStatement.setString(3, settingsBO.getUserId());
                int result = prepareStatement.executeUpdate();
                String personaValue = getPersonaForUser(settingsBO.getUserId(), postgre);
                if (result == 0) {
                    logger.info("Persona settings updation failed!");
                    createResponseDoc(responseElem, settingsBO.getAction(), settingsBO.getUserId(), personaValue, STATUS_FAILED, "Persona settings updation failed");
                } else {
                    logger.info("Persona settings updated successfully!");
                    createResponseDoc(responseElem, settingsBO.getAction(), settingsBO.getUserId(), personaValue, STATUS_SUCCESS, "");
                }
            } catch (Exception e) {
                logger.error("Exception in updating persona settings", e);
                createResponseDoc(responseElem, settingsBO.getAction(), settingsBO.getUserId(), null, STATUS_FAILED, "Exception in updating persona settings");
            } finally {
                postgre.releaseConnection(connection, prepareStatement, null);
            }
        }else {
            try {
                personaSettingsQuery = "INSERT INTO persona_settings(user_id, persona_value, active, created_on, modified_on) VALUES (?, ?, ?, current_timestamp, current_timestamp)";
                prepareStatement = connection.prepareStatement(personaSettingsQuery);
                prepareStatement.setString(1, settingsBO.getUserId());
                prepareStatement.setString(2, settingsBO.getPersona());
                prepareStatement.setBoolean(3, true);
                int result = prepareStatement.executeUpdate();
                String personaValue = getPersonaForUser(settingsBO.getUserId(), postgre);
                if (result == 0) {
                    logger.info("Persona settings insertion failed!");
                    createResponseDoc(responseElem, settingsBO.getAction(), settingsBO.getUserId(), personaValue, STATUS_FAILED, "Persona settings insertion failed");
                } else {
                    logger.info("Persona settings inserted successfully!");
                    createResponseDoc(responseElem, settingsBO.getAction(), settingsBO.getUserId(), personaValue, STATUS_SUCCESS, "");
                }
            } catch (Exception e) {
                logger.error("Exception in insert persona settings", e);
                createResponseDoc(responseElem, settingsBO.getAction(), settingsBO.getUserId(), null, STATUS_FAILED, "Exception in insert persona settings");
            } finally {
                postgre.releaseConnection(connection, prepareStatement, null);
            }
        }
    }
    
    /**
     * This method is used to create the response document based on the input.
     * 
     * @param action Persona action from the request.
     * @param userId UserId of the logged-in user.
     * @param persona Persona for the logged-in user.
     * @param status Status of the request.
     * @param error Error messsage in the request processing is failed.
     * 
     * @return Returns document object with the response
     * 
     */
    public void createResponseDoc(Element responseElem, String action, String userId, String persona, String status, String error) {
        if(action == null) action = "";
        if(userId == null) userId = "";
        if(persona == null) persona = "";
        if(status == null) status = "";
        if(error == null) error = "";
        
        Document doc = DocumentHelper.createDocument();
        Element personaresponseElem = responseElem.addElement("persona-settings");
        personaresponseElem.addElement("status").setText(status);
        personaresponseElem.addElement("settings-action").setText(action);
        personaresponseElem.addElement("user-id").setText(userId);
        personaresponseElem.addElement("persona").setText(persona);
        personaresponseElem.addElement("error").setText(error);
    }
    
    /**
     * This method is used to check if a persona settins is available for the user in database.
     * 
     * 
     * @return Returns true if the persona settings exists for the user 
     *         else returns false.
     * 
     */
    public boolean isPersonaSettingExists(String userId) {
        boolean isPersonaSettingExists = false;

        logger.info("DashboardSettingsExternal : isPersonaSettingExists()");
        
        String personaSettingsQuery = "SELECT COUNT(*) FROM persona_settings WHERE user_id = ?";

        Connection connection = null;
        PreparedStatement prepareStatement = null;
        ResultSet rs = null;

        try {
            connection = postgre.getConnection();
            prepareStatement = connection.prepareStatement(personaSettingsQuery);
            prepareStatement.setString(1, userId);
            rs = prepareStatement.executeQuery();
            int count = 0;
            while (rs.next()) {
                count = rs.getInt(1);
            }
            
            if(count > 0 ) {
                isPersonaSettingExists = true;
            }
            logger.debug("isPersonaSettingExists : "+isPersonaSettingExists);
            
        } catch (Exception e) {
            logger.error("Exception in isPersonaSettingExists", e);
        } finally {
            postgre.releaseConnection(connection, prepareStatement, rs);
        }
        return isPersonaSettingExists;
    }
    
    /**
     * @param subscriber_id
     * @param preferenceId
     * @return
     */
    public String generateConfirmationToken(double subscriberId,
            double preferenceId, String email) {
        logger.info("DashboardSettingsExternal : generateConfirmationToken()");
        String confirmationToken = RandomStringUtils
                .randomAlphanumeric(10);
        logger.debug("DashboardSettingsExternal :ConfirmationToken()" +confirmationToken);
        boolean tokenExist = isConfirmationTokenExist(confirmationToken);

        if (tokenExist) {
            generateConfirmationToken(subscriberId, preferenceId, email);
        } else {
            String addGeneratedTokenQuery = "INSERT INTO NEWSLETTER_CONFIRMATION_TOKEN (TOKEN, SUBSCRIBER_ID, PREFERENCE_ID, GENERATED_DATE, CONFIRMATION_STATUS, SUBSCRIBER_EMAIL) VALUES(?,?,?,LOCALTIMESTAMP,?,?)";
            Connection connection = null;
            PreparedStatement prepareStatement = null;

            try {
                connection = postgre.getConnection();
                prepareStatement = connection
                        .prepareStatement(addGeneratedTokenQuery);

                prepareStatement.setString(1, confirmationToken);
                prepareStatement.setDouble(2, subscriberId);
                prepareStatement.setDouble(3, preferenceId);
                prepareStatement.setString(4, STATUS_PENDING);
                prepareStatement.setString(5, email);
                int result = prepareStatement.executeUpdate();

                if (result != 0) {
                    logger.info("Token Generated and Added !");
                } else {
                    logger.info("Token Not Generated and Not Added !");
            }
            } catch (Exception e) {
                logger.error("Exception in generateConfirmationToken", e);
            } finally {
                postgre.releaseConnection(connection, prepareStatement,
                        null);
            }
        }

        return confirmationToken;
    }
    /**
     * @param confirmationToken
     * @return
     */
    private boolean isConfirmationTokenExist(String confirmationToken) {
        logger.info("DashboardSettingsExternal : isConfirmationTokenExist()"+confirmationToken);
        boolean tokenExist = false;
        String tokenCheckQuery = "SELECT TOKEN FROM NEWSLETTER_CONFIRMATION_TOKEN WHERE TOKEN = ?";
        Connection connection = null;
        PreparedStatement prepareStatement = null;

        try {
            connection = postgre.getConnection();
            prepareStatement = connection
                    .prepareStatement(tokenCheckQuery);
            prepareStatement.setString(1, confirmationToken);

            ResultSet resultSet = prepareStatement.executeQuery();

            if (resultSet.next()) {
                logger.info("Token already Exist !");
                tokenExist = true;
            } else {
                logger.info("Token Doesn't Exist !");
            }
        } catch (Exception e) {
            logger.error("Exception in isConfirmationTokenExist", e);
        } finally {
            postgre.releaseConnection(connection, prepareStatement, null);
        }
        return tokenExist;
    }
    
    /**
     * @param email
     */
    public boolean isEmailAlreadyExist(String email) {
        logger.info("NewsletterPhpExternal : isEmailAlreadyExist()");
        boolean emailsExistStatus = false;
        String emailCheckQuery = "SELECT SUBSCRIBER_EMAIL FROM NEWSLETTER_MASTER WHERE SUBSCRIBER_EMAIL = ?";
        Connection connection = null;
        PreparedStatement prepareStatement = null;

        try {
            connection = postgre.getConnection();
            prepareStatement = connection
                    .prepareStatement(emailCheckQuery);
            prepareStatement.setString(1, email);

            ResultSet resultSet = prepareStatement.executeQuery();

            if (resultSet.next()) {
                logger.info("Email Already Exist !");
                emailsExistStatus = true;
            } else {
                logger.info("Email Doesn't Exist !");
                emailsExistStatus = false;
            }
        } catch (Exception e) {
            logger.error("Exception in isEmailAlreadyExist", e);
        } finally {
            postgre.releaseConnection(connection, prepareStatement, null);
        }
        return emailsExistStatus;
    }

}
