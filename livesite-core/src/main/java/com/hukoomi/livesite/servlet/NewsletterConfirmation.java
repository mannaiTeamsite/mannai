package com.hukoomi.livesite.servlet;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.hukoomi.bo.PhpListUserBO;
import com.hukoomi.utils.MySqlForServlet;
import com.hukoomi.utils.PostgreForServlet;

/**
 * @author Arbaj
 *
 */
public class NewsletterConfirmation extends HttpServlet {
    
    /** Logger object to check the flow of the code. */
    private static final Logger logger = Logger
            .getLogger(NewsletterConfirmation.class);

    /** subscribed constant. */
    private static final String STATUS_SUBSCRIBED = "Subscribed";

    /** external parameter. */
    private static final String STATUS_ACTIVE = "Active";

    /** pending status constant. */
    private static final String STATUS_PENDING = "Pending";

    /** confirmed status constant. */
    private static final String STATUS_CONFIRMED = "Confirmed";
    
    /** external parameter. */
    private static final String STATUS_SUCCESS = "Success";
    
    /** external parameter. */
    private static final String STATUS_FAILED = "Failed";


    /** Postgre Object variable. */
    PostgreForServlet postgre = null;
    
    /** httpConnection for making call to phpList services. */
    private HttpURLConnection httpConnection = null;
    
    /** phpList properties key. */
    private static final String BASE_URL = "baseUrl";
    
    /** phpList properties key. */
    private static final String ADMIN_ID = "adminID";
    
    /** phpList properties key. */
    private static final String ADMIN_PWD = "adminPWD";
    
    /** phpList properties key. */
    private static final String PHP_USER_ID = "id";
    
    /** baseUrl of phpList. */
    private String baseUrl;
    
    /** phplist response status. */
    private static final String STATUS_NOTFOUND = "NOTFOUND";
    
    /** authorizationHeader for authencicate phpList. */
    private String authorizationHeader; 
    
    /** phpList subscriber email */
    private static final String SUBSCRIBER_EMAIL = "subscriber_email";
    
    /** phpList subscriber email */
    private static final String SUBSCRIBER_PERSONA = "persona";
    
    /** phpList subscriber email */
    private static final String SUBSCRIBER_LANGUAGE = "language";  
         
    
    /** phpList response status. */
    private static final String STATUS_ALREADY_SUBSCRIBED =
            "alreadySubscribed";
    
    /** phpList response status. */
    private static final String STATUS_PERSONA_NOTFOUND =
            "personaNOTFOUND";
    
    /** phpList response status. */
    private static final String SESSION_FAILED =
            "sessionfailed";
    
    /** phpList response status. */
    private static final String SUBSCRIBED_SUCCESSFULLY =
            "subscribedSuccessfully";
    
    /** phpList response status. */
    private static final String SUBSCRIBED_UNSUCCESSFULL =
            "subscribedUnsuccessfull";
    
    /** Postgre Object variable. */
    MySqlForServlet mysql = null;

    public void doGet(HttpServletRequest request,
            HttpServletResponse response)
            throws IOException, ServletException {
        logger.info("NewsletterConfirmation : doGet()");
        postgre = new PostgreForServlet();
        mysql = new MySqlForServlet();
        PhpListUserBO pluBO = null;
        PrintWriter writer = response.getWriter();
        String token = request.getParameter("token");
        String pageLang = request.getParameter("lang");
        String httpServletAddress = request.getLocalAddr();
        RequestDispatcher rd = request
                .getRequestDispatcher(
                        "/portal-" + pageLang + "/home.page");
        String confirmationStatus = getConfirmationTokenStatus(token);

        if (STATUS_PENDING.equals(confirmationStatus)) {
            Map<String, String> subscriberDetails = getSubcriberDetails(token);

            double subscriberId = Double
                    .parseDouble(subscriberDetails.get("subscriberId"));
            double preferenceId = Double
                    .parseDouble(subscriberDetails.get("preferenceId"));

            String subscriberemail = getSubscriberEmail(subscriberId);
            String persona = getSubscriberPersona(subscriberId,preferenceId);
            int listid = getSubscriberListID(persona);
            String status="";
            String subStatus="";
            String uptpostgreData = "";
            String syncStatus = "";
            int userId = 0;
            status = phpSubscriberExists(subscriberemail,listid);
            try {
                if (!status.equals("") && status.equals(STATUS_NOTFOUND)) {
                    
                    subStatus = createSubscriberPhplist(subscriberemail, httpServletAddress);
                    if(!subStatus.equals("") && subStatus.equals(SUBSCRIBED_SUCCESSFULLY)) { 
                        userId = getSubscriberID(subscriberemail);
                       updateSubscriberPersona(userId,listid);
                    }
                    uptpostgreData=STATUS_SUCCESS;
                }
                if(!status.equals("") && status.equals(STATUS_PERSONA_NOTFOUND))
                {
                    userId = getSubscriberID(subscriberemail);
                    updateSubscriberPersona(userId,listid);
                    uptpostgreData=STATUS_SUCCESS;
                } 
                if (uptpostgreData.equals(STATUS_SUCCESS) && status.equals(STATUS_NOTFOUND)) {
                    String getPhpUser = "SELECT ID, EMAIL, CONFIRMED, BLACKLISTED, OPTEDIN, BOUNCECOUNT, "
                            + "ENTERED, MODIFIED, UNIQID, UUID, HTMLEMAIL, SUBSCRIBEPAGE, RSSFREQUENCY, PASSWORD, "
                            + "PASSWORDCHANGED, DISABLED, EXTRADATA, FOREIGNKEY FROM PHPLIST_USER_USER WHERE ID = ? ";
                    
                    String updateSyncUserQuery = "INSERT INTO PHPLIST_USER_USER (ID, EMAIL, CONFIRMED, BLACKLISTED,"
                            + " OPTEDIN, BOUNCECOUNT, ENTERED, MODIFIED, UNIQID, UUID, HTMLEMAIL, SUBSCRIBEPAGE, RSSFREQUENCY, "
                            + "PASSWORD, PASSWORDCHANGED, DISABLED, EXTRADATA, FOREIGNKEY)"
                            + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
                    
                    String getUserList ="SELECT USERID, LISTID, ENTERED, MODIFIED FROM PHPLIST_LISTUSER WHERE ENTERED = "
                            + "(SELECT MAX(ENTERED) FROM PHPLIST_LISTUSER WHERE USERID = ? AND LISTID = ?)";
                    
                    String updateSyncListQuery="INSERT INTO PHPLIST_LISTUSER (USERID, LISTID, ENTERED, MODIFIED) VALUES (?,?,?,?)";
                    
                    pluBO = getsyncphpData(getPhpUser,userId,listid,"USERDATA");
                    syncStatus = updatesyncphpData(updateSyncUserQuery,pluBO,"USERDATA");
                    logger.debug("NewsletterConfirmation : syncstatus user table"+syncStatus);
                    /* int userid = getphpUserID(subscriberemail); */
                    logger.debug("NewsletterConfirmation : getphpUserID "+userId+" listid "+listid);
                    if(syncStatus.equals(STATUS_SUCCESS)) {                        
                        pluBO = getsyncphpData(getUserList,userId,listid,"LISTDATA");
                        syncStatus = updatesyncphpData(updateSyncListQuery,pluBO,"LISTDATA");
                    }
                    
                }
                logger.debug("NewsletterConfirmation : syncstatus >>>>>"+syncStatus);
                if (uptpostgreData.equals(STATUS_SUCCESS) && status.equals(STATUS_PERSONA_NOTFOUND)) {
                    
                    String getUserList ="SELECT USERID, LISTID, ENTERED, MODIFIED FROM PHPLIST_LISTUSER WHERE ENTERED = "
                            + "(SELECT MAX(ENTERED) FROM PHPLIST_LISTUSER WHERE USERID = ? AND LISTID = ?)";
                    
                    String updateSyncListQuery="INSERT INTO PHPLIST_LISTUSER (USERID, LISTID, ENTERED, MODIFIED) VALUES (?,?,?,?)";
                    
                    /* int userid = getphpUserID(subscriberemail); */
                    pluBO = getsyncphpData(getUserList,userId,listid, "LISTDATA");
                    syncStatus = updatesyncphpData(updateSyncListQuery,pluBO,"LISTDATA");
                    logger.debug("NewsletterConfirmation : syncstatus <<<<>>>>>"+syncStatus);
                }
                    
            
               
            } catch (NoSuchAlgorithmException e) {            
                logger.error("NewsletterConfirmation : doGet() >>>>"+e);
            } catch (IOException e) {            
                logger.error("NewsletterConfirmation : doGet() <<<<"+e);
            }

            if (uptpostgreData.equals(STATUS_SUCCESS)) {
            String tokenType = getTokenType(subscriberId);
            boolean tokenStatusUpdate = false;
            boolean masterDataUpdate = false;
            boolean preferenceDataUpdate = false;

            if ("NewSubscriber".equals(tokenType)) {
                tokenStatusUpdate = updateTokenStatus(token,
                        STATUS_CONFIRMED);
                masterDataUpdate = updateMasterTable(subscriberId);
                preferenceDataUpdate = updatePreferencesTable(subscriberId,
                        preferenceId);
            } else {
                tokenStatusUpdate = updateTokenStatus(token,
                        STATUS_CONFIRMED);
                masterDataUpdate = true;
                preferenceDataUpdate = updatePreferencesTable(subscriberId,
                        preferenceId);
            }
            
            if (tokenStatusUpdate && masterDataUpdate
                    && preferenceDataUpdate) {
                Cookie confirmationCookie = new Cookie("confirmationStatus",
                        "confirmed");
                response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1.
                response.setHeader("Pragma", "no-cache"); // HTTP 1.0.
                response.setDateHeader("Expires", 0);
                response.addCookie(confirmationCookie);
                rd.forward(request, response);
        }else {
            Cookie confirmationCookie = new Cookie("confirmationStatus",
                    "notConfirmed");
            response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1.
            response.setHeader("Pragma", "no-cache"); // HTTP 1.0.
            response.setDateHeader("Expires", 0);
            response.addCookie(confirmationCookie);
            rd.forward(request, response);
        }
    } else {
        Cookie confirmationCookie = new Cookie("confirmationStatus",
                "technicalIssue");
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1.
        response.setHeader("Pragma", "no-cache"); // HTTP 1.0.
        response.setDateHeader("Expires", 0);
        response.addCookie(confirmationCookie);
        rd.forward(request, response);
    }
       
    } else {
        Cookie confirmationCookie = new Cookie("confirmationStatus",
                "alreadyConfirmed");
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1.
        response.setHeader("Pragma", "no-cache"); // HTTP 1.0.
        response.setDateHeader("Expires", 0);
        response.addCookie(confirmationCookie);
        rd.forward(request, response);
    }

    }

    /**
     * @param token
     * @return
     */
    private String getTokenType(double subscriberId) {
        logger.info("NewsletterConfirmation : getTokenType");
        String confirmationTokentype = null;
        String getTokenTypeQuery = "SELECT COUNT(*) FROM NEWSLETTER_PREFERENCE WHERE SUBSCRIBER_ID = ?";
        Connection connection = null;
        PreparedStatement prepareStatement = null;
        ResultSet rs = null;
        try {
            connection = postgre.getConnection();
            prepareStatement = connection
                    .prepareStatement(getTokenTypeQuery);
            prepareStatement.setDouble(1, subscriberId);

            rs = prepareStatement.executeQuery();
            rs.next();
            if (rs.getInt(1) != 0) {
                confirmationTokentype = "NewSubscriber";
            } else {
                confirmationTokentype = "PreferenceUpdate";
            }
        } catch (Exception e) {
            logger.error("Exception in getTokenType", e);
        } finally {
            postgre.releaseConnection(connection, prepareStatement, null);
        }

        return confirmationTokentype;
    }

    /**
     * @param token
     * @return
     */
    private String getConfirmationTokenStatus(String token) {
        logger.info("NewsletterConfirmation : getConfirmationTokenStatus");
        String confirmationTokenStatus = null;
        String getConfirmationTokenStatusQuery = "SELECT CONFIRMATION_STATUS FROM NEWSLETTER_CONFIRMATION_TOKEN WHERE TOKEN = ?";
        Connection connection = null;
        PreparedStatement prepareStatement = null;
        ResultSet rs = null;
        try {
            connection = postgre.getConnection();
            prepareStatement = connection
                    .prepareStatement(getConfirmationTokenStatusQuery);
            prepareStatement.setString(1, token);

            rs = prepareStatement.executeQuery();
            while (rs.next()) {
                confirmationTokenStatus = rs.getString(1);
            }
        } catch (Exception e) {
            logger.error("Exception in getConfirmationTokenStatus", e);
        } finally {
            postgre.releaseConnection(connection, prepareStatement, null);
        }

        return confirmationTokenStatus;
    }

    /**
     * @param token
     * @param statusConfirmed
     * @return
     */
    private boolean updateTokenStatus(String token, String status) {
        logger.info("NewsletterConfirmation : updateTokenStatus()");
        boolean updateTokenStatus = false;
        String updateTokenStatusQuery = "UPDATE NEWSLETTER_CONFIRMATION_TOKEN SET CONFIRMATION_STATUS = ? WHERE TOKEN = ?";
        Connection connection = null;
        PreparedStatement prepareStatement = null;

        try {
            connection = postgre.getConnection();
            prepareStatement = connection
                    .prepareStatement(updateTokenStatusQuery);
            prepareStatement.setString(1, status);
            prepareStatement.setString(2, token);

            int result = prepareStatement.executeUpdate();
            if (result != 0) {
                logger.info("Token Status Updated !");
                updateTokenStatus = true;
            } else {
                logger.info("Token Status Not Updated !");
            }
        } catch (Exception e) {
            logger.error("Exception in updateTokenStatus", e);
        } finally {
            postgre.releaseConnection(connection, prepareStatement, null);
        }

        return updateTokenStatus;
    }

    /**
     * @param token
     * @return
     */
    private Map<String, String> getSubcriberDetails(String token) {
        logger.info("NewsletterConfirmation : getSubcriberDetails()");
        Map<String, String> subscriberDetails = new LinkedHashMap<String, String>();

        String getSubscriberDetailsQuery = "SELECT SUBSCRIBER_ID, PREFERENCE_ID FROM NEWSLETTER_CONFIRMATION_TOKEN WHERE TOKEN = ?";
        Connection connection = null;
        PreparedStatement prepareStatement = null;

        try {
            connection = postgre.getConnection();
            prepareStatement = connection
                    .prepareStatement(getSubscriberDetailsQuery);
            prepareStatement.setString(1, token);

            ResultSet resultSet = prepareStatement.executeQuery();

            if (resultSet.next()) {
                logger.info("Token Exist !");
                subscriberDetails.put("subscriberId",
                        resultSet.getString("SUBSCRIBER_ID"));
                subscriberDetails.put("preferenceId",
                        resultSet.getString("PREFERENCE_ID"));
            } else {
                logger.info("Token Doesn't Exist !");
            }
        } catch (Exception e) {
            logger.error("Exception in getSubcriberDetails", e);
        } finally {
            postgre.releaseConnection(connection, prepareStatement, null);
        }
        return subscriberDetails;
    }

    /**
     * @param subscriberId
     */
    private boolean updateMasterTable(double subscriberId) {
        logger.info("NewsletterConfirmation : updateMasterTable()");
        boolean subscriberPreferenceDataInsert = false;
        String addSubscriberPreferencesQuery = "UPDATE NEWSLETTER_MASTER SET STATUS = ? WHERE SUBSCRIBER_ID = ?";
        Connection connection = null;
        PreparedStatement prepareStatement = null;

        try {
            connection = postgre.getConnection();
            prepareStatement = connection
                    .prepareStatement(addSubscriberPreferencesQuery);
            prepareStatement.setString(1, STATUS_SUBSCRIBED);
            prepareStatement.setDouble(2, subscriberId);

            int result = prepareStatement.executeUpdate();
            if (result != 0) {
                logger.info("Subscription Status Updated !");
                subscriberPreferenceDataInsert = true;
            } else {
                logger.info("Subscription Status Not Updated !");
                subscriberPreferenceDataInsert = false;
            }
        } catch (Exception e) {
            logger.error("Exception in updateMasterTable", e);
        } finally {
            postgre.releaseConnection(connection, prepareStatement, null);
        }

        return subscriberPreferenceDataInsert;
    }

    /**
     * @param subscriberId
     */
    private boolean updatePreferencesTable(double subscriberId,
            double preferenceId) {
        logger.info("NewsletterConfirmation : updatePreferencesTable()");
        boolean subscriberPreferenceDataInsert = false;
        String addSubscriberPreferencesQuery = "UPDATE NEWSLETTER_PREFERENCE SET STATUS = ? WHERE SUBSCRIBER_ID = ? AND PREFERENCE_ID = ?";
        Connection connection = null;
        PreparedStatement prepareStatement = null;

        try {
            connection = postgre.getConnection();
            prepareStatement = connection
                    .prepareStatement(addSubscriberPreferencesQuery);
            prepareStatement.setString(1, STATUS_ACTIVE);
            prepareStatement.setDouble(2, subscriberId);
            prepareStatement.setDouble(3, preferenceId);

            int result = prepareStatement.executeUpdate();
            if (result != 0) {
                logger.info("Preference Status Updated !");
                subscriberPreferenceDataInsert = true;
            } else {
                logger.info("Preference Status Not Updated !");
                subscriberPreferenceDataInsert = false;
            }
        } catch (Exception e) {
            logger.error("Exception in updateMasterTable", e);
        } finally {
            postgre.releaseConnection(connection, prepareStatement, null);
        }

        return subscriberPreferenceDataInsert;
    }
    
    /**
     * @author pramesh
     * @param email
     * @param listid
     * @return
     * 
     * This method get the checks if Subsscriber status and updates
     */
    private String phpSubscriberExists(String email, int listid) {
        logger.info("NewsletterConfirmation : phpSubscriberExists");
        boolean subscriberPreferenceDataInsert = false;
        String checkSubscriberEmailQuery = "SELECT ID FROM PHPLIST_USER_USER WHERE EMAIL = ? ";
        Connection connection = null;
        PreparedStatement prepareStatement = null;
        int phpUserID ;
        ResultSet rs = null;
        String status="";
        try {
            connection = mysql.getConnection();
            prepareStatement = connection
                    .prepareStatement(checkSubscriberEmailQuery);
            prepareStatement.setString(1, email);

            rs = prepareStatement.executeQuery();
           
            if (rs.next()) {
                logger.info("Subscriber Already Exist !");
                phpUserID =  rs.getInt(PHP_USER_ID); 
                status = checkAlreadySubscribed(phpUserID,listid);
            } else {
                 logger.info("Subscriber Doesn't Exist !");
                 status = STATUS_NOTFOUND;
            }
            
            
        } catch (Exception e) {
            logger.error("Exception in phpSubscriberExists", e);
        } finally {
            mysql.releaseConnection(connection, prepareStatement, null);
        }

        return status;
    }
    
    /**
     * @author pramesh
     * @param email
     * @param listid
     * @return
     * 
     * This method check if the user is Already subscribed or not
     */
    private String checkAlreadySubscribed(int subscriberid, int listid) {
        logger.info("NewsletterConfirmation : checkAlreadySubscribed");
        boolean subscriberPreferenceDataInsert = false;
        String checkSubscriberEmailQuery = "SELECT USERID,LISTID FROM PHPLIST_LISTUSER WHERE USERID = ? AND LISTID = ?";
        Connection connection = null;
        PreparedStatement prepareStatement = null;        
        ResultSet rs = null;
        String status="";
        try {
            connection = mysql.getConnection();
            prepareStatement = connection
                    .prepareStatement(checkSubscriberEmailQuery);
            prepareStatement.setInt(1, subscriberid);
            prepareStatement.setInt(2, listid);
            rs = prepareStatement.executeQuery();  
            if (rs.next()) {
                logger.info("Subscriber and Listid Already Exist !");               
                status = STATUS_ALREADY_SUBSCRIBED;
            } else {
                 logger.info("Persona doesn't exist to be added  !");
                 status = STATUS_PERSONA_NOTFOUND;
            }
            
        } catch (Exception e) {
            logger.error("Exception in checkAlreadySubscribed", e);
        } finally {
            mysql.releaseConnection(connection, prepareStatement, null);
        }

        return status;
    }
    
    /**
     * @author pramesh
     * @param email
     * @param listid
     * @return
     * 
     * This method get the checks if Subsscriber status and updates
     */
    /*
     * private int getphpUserID(String email) {
     * logger.info("NewsletterConfirmation : getphpUserID"); boolean
     * subscriberPreferenceDataInsert = false; String
     * checkSubscriberEmailQuery =
     * "SELECT ID FROM PHPLIST_USER_USER WHERE EMAIL = ? "; Connection
     * connection = null; PreparedStatement prepareStatement = null; int
     * phpUserID =0; ResultSet rs = null; String status=""; try {
     * connection = mysql.getConnection(); prepareStatement = connection
     * .prepareStatement(checkSubscriberEmailQuery);
     * prepareStatement.setString(1, email);
     * 
     * rs = prepareStatement.executeQuery();
     * 
     * if (rs.next()) { logger.info("Subscriber Already Exist !");
     * phpUserID = rs.getInt(PHP_USER_ID);
     * 
     * } else { logger.info("Subscriber Doesn't Exist !");
     * 
     * }
     * 
     * 
     * } catch (Exception e) {
     * logger.error("Exception in phpSubscriberExists", e); } finally {
     * mysql.releaseConnection(connection, prepareStatement, null); }
     * 
     * return phpUserID; }
     */
    /**
     * @author pramesh
     * @param subscriberId
     * @return
     * 
     * This method get the subscriber email based on subscriber id
     */
    private String getSubscriberEmail(double subscriberId) {
        logger.info("NewsletterConfirmation : getSubscriberEmail");
        boolean subscriberPreferenceDataInsert = false;
        String addSubscriberPreferencesQuery = "SELECT SUBSCRIBER_EMAIL FROM NEWSLETTER_MASTER WHERE SUBSCRIBER_ID = ?";
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
            
                subscriberEmail =  rs.getString(SUBSCRIBER_EMAIL);
            }
        } catch (Exception e) {
            logger.error("Exception in getSubscriberEmail", e);
        } finally {
            postgre.releaseConnection(connection, prepareStatement, null);
        }

        return subscriberEmail;
    }
    
    /**
     * @author pramesh
     * @param subscriberId
     * @return
     * 
     * This method to fetch the subscriber persona based on subscriber id
     */
    private String getSubscriberPersona(double subscriberId, double preferenceId) {
        logger.info("NewsletterConfirmation : getSubscriberPersona");       
        String getSubscriberPersonaQuery = "SELECT PERSONA, LANGUAGE FROM NEWSLETTER_PREFERENCE WHERE SUBSCRIBER_ID = ? AND PREFERENCE_ID = ? ";
        Connection connection = null;
        PreparedStatement prepareStatement = null;
        String subscriberPersona = "";
        String subscriberLanguage = "";
        String persona="";
        ResultSet rs = null;
        try {
            connection = postgre.getConnection();
            prepareStatement = connection
                    .prepareStatement(getSubscriberPersonaQuery);
            prepareStatement.setDouble(1, subscriberId);
            prepareStatement.setDouble(2, preferenceId);
            rs = prepareStatement.executeQuery();   
            while (rs.next()) {
            
                subscriberPersona =  rs.getString(SUBSCRIBER_PERSONA);
                subscriberLanguage =  rs.getString(SUBSCRIBER_LANGUAGE);
            }
        } catch (Exception e) {
            logger.error("Exception in getSubscriberPersona", e);
        } finally {
            postgre.releaseConnection(connection, prepareStatement, null);
        }
        
        persona = subscriberPersona +"-"+ subscriberLanguage;
        logger.debug("Persona : "+ persona);
        return persona;
    }
    
    /**
     * @author pramesh
     * @param subscriberId
     * @return
     * 
     * This method to fetch the subscriber listId based on listName
     */
    private int getSubscriberListID(String listName) {
        logger.info("NewsletterConfirmation : getSubscriberListID");       
        String getSubscriberListQuery = "SELECT * FROM PHPLIST_LIST WHERE NAME = ?";
        Connection connection = null;
        PreparedStatement prepareStatement = null;        
        int listid=0;
        ResultSet rs = null;
        try {
            connection = mysql.getConnection();
            prepareStatement = connection
                    .prepareStatement(getSubscriberListQuery);
            prepareStatement.setString(1, listName);            
            rs = prepareStatement.executeQuery();   
            while (rs.next()) {            
                listid =  rs.getInt(PHP_USER_ID);                
            }
            logger.debug("NewsletterConfirmation : List ID "+listid); 
        } catch (Exception e) {
            logger.error("Exception in phpList query getSubscriberListID", e);
        } finally {
            mysql.releaseConnection(connection, prepareStatement, null);
        }        
        logger.debug("Subscriber persona phplist id " + listid);
        return listid;
    }
    
    /**
     * @author pramesh
     * @param email
     * @return
     * 
     * This method to fetch the subscriber id in phpTool 
     */
    private int getSubscriberID(String email) {
        logger.info("NewsletterConfirmation : getSubscriberID");       
        String getSubscriberListQuery = "SELECT ID FROM PHPLIST_USER_USER WHERE EMAIL = ?";
        Connection connection = null;
        PreparedStatement prepareStatement = null;        
        int userid=0;
        ResultSet rs = null;
        try {
            connection = mysql.getConnection();
            prepareStatement = connection
                    .prepareStatement(getSubscriberListQuery);
            prepareStatement.setString(1, email);            
            rs = prepareStatement.executeQuery();   
            while (rs.next()) {            
                userid =  rs.getInt(PHP_USER_ID);                
            }
            logger.debug("getSubscriberID :  ID "+userid); 
        } catch (Exception e) {
            logger.error("Exception in phpList query getSubscriberID", e);
        } finally {
            mysql.releaseConnection(connection, prepareStatement, null);
        }        
        logger.debug("Subscriber ID " + userid);
        return userid;
    }
    /**
     * @author pramesh
     * @param subscriberId
     * @return
     * 
     * This method to update the subscriber list based on userid & listid
     */
    private String updateSubscriberPersona(int subscriberId, int listId) {
        logger.info("NewsletterConfirmation : updateSubscriberPersona");      
        String updateSubscriberListQuery 
        = "INSERT INTO PHPLIST_LISTUSER (USERID, LISTID, ENTERED) VALUES (?,?,LOCALTIMESTAMP)";
        Connection connection = null;
        PreparedStatement prepareStatement = null;        
        String status="";        
        try {
            connection = mysql.getConnection();
            prepareStatement = connection
                    .prepareStatement(updateSubscriberListQuery);
            prepareStatement.setDouble(1, subscriberId);
            prepareStatement.setDouble(2, listId);            
            logger.debug("query : " + updateSubscriberListQuery);
            final int result = prepareStatement.executeUpdate();
            if (result == 0) {
                logger.debug("failed to insert/update comments data!");
                status="FAILURE";
            } else {
                logger.debug(" comments data insert/update successfully!");
                status="SUCCESS";
            }
        } catch (Exception e) {
            logger.error("Exception in updateSubscriberPersona", e);
        } finally {
            mysql.releaseConnection(connection, prepareStatement, null);
        }   
     
        return status;
    }
    
    /**
     * @author pramesh
     * @param email
     * @return
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * 
     * This method is to add subscriber to phpList 
     */
    private String createSubscriberPhplist(final String email, String httpServletAddress)
            throws IOException, NoSuchAlgorithmException {
        logger.info("createSubscriberPhplist:Enter"+httpServletAddress);       
        Properties properties =
                postgre.loadProperties("phplist.properties");
        String base64Token = getphpListToken();
        String status="";
        if(!base64Token.equals(SESSION_FAILED)) {
            
            authorizationHeader =
                    "Basic " + base64Token;
            baseUrl = properties.getProperty(BASE_URL);
            /* int unique_id = 0; */
            try {            
                
                status =
                                createSubscriber(email,authorizationHeader,httpServletAddress);
                        logger.debug("status: " + status);

            } catch (Exception e) {
                logger.error("exception: createSubscriberPhplist", e);
            }
            
        }else {
            
            status = SESSION_FAILED;
        }      

        return status;
    }
    
    /**
     * @author pramesh
     * @param email
     * @param authHeader
     * @return
     * @throws NoSuchAlgorithmException
     * 
     * This method makes service call to add subscriber to phpList
     */
    private String createSubscriber(final String email,String authHeader,String httpServletAddress)
            throws NoSuchAlgorithmException {
        logger.info("createsubscriber:Enter");
        InputStream is = null;
        String requestJSON = "";
        StringBuilder response = new StringBuilder();
        int statusCode=0;
        String status = "";
       
        try {
            // Create connection
            Properties properties =
                    postgre.loadProperties("phplist.properties"); 
          //baseUrl = properties.getProperty(BASE_URL);
            baseUrl = "http://"+httpServletAddress+"phplist";
            logger.info("Phplist baseUrl "+baseUrl);
          String endpoint = baseUrl +
            "/api/v2/subscribers";       
            URL url = new URL(endpoint);
            httpConnection = (HttpURLConnection) url.openConnection();
            httpConnection.setRequestMethod("POST");
            httpConnection.setRequestProperty("Content-Type",
                    "application/json");
            httpConnection.setRequestProperty("Authorization",
                    authHeader);                       
           
                
                requestJSON = "{ \"email\" : \"" + email
                        + "\", \"confirmed\": true,\"blacklisted\" : false, \"html_email\" : true, \"disabled\": false}";                
                
            logger.debug("requestJSON: " + requestJSON);
            httpConnection.setRequestProperty("Content-Length",
                    Integer.toString(requestJSON.getBytes().length));
            httpConnection.setRequestProperty("Content-Language", "en-US");
            httpConnection.setUseCaches(false);
            httpConnection.setDoOutput(true);

            // Send request
            DataOutputStream wr =
                    new DataOutputStream(httpConnection.getOutputStream());
            wr.writeBytes(requestJSON);
            wr.close();

            // Get Response
            is = httpConnection.getInputStream();
            BufferedReader rd =
                    new BufferedReader(new InputStreamReader(is));
            
            String line;
            while ((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();
            statusCode = httpConnection.getResponseCode();
        } catch (IOException ioe) {            
            try {
                statusCode = httpConnection.getResponseCode();
                logger.debug("statusCode: " + statusCode);
                status = SUBSCRIBED_UNSUCCESSFULL;
                
            } catch (IOException e) {
                logger.error("Exception in subscriber creation: ", e);
            }
            httpConnection.disconnect();
            logger.error("Exception in subscriber creation: ", ioe);
            
        }
        logger.debug("createsubscriber: statuscode" + statusCode);
        /* return getResponseValue(response.toString(),PHP_USER_ID); */
        if(statusCode==201) {
            status = SUBSCRIBED_SUCCESSFULLY;
        }else {
            status = SUBSCRIBED_UNSUCCESSFULL;
        }
        return status;
    }
    
    /**
     * @author pramesh
     * @return
     * @throws NoSuchAlgorithmException
     * @throws IOException
     * This method id used to get session token for phpList
     */
    private String getphpListToken()
            throws NoSuchAlgorithmException, IOException {
        logger.info("getConnection:Enter");  
        String token="";
        String requestJSON = "";
        InputStream is = null;
        String adminID= "";
        String adminPWD= ""; 
        int statusCode=0;
        Properties properties =
                  postgre.loadProperties("phplist.properties"); 
        baseUrl = properties.getProperty(BASE_URL);
        adminID = properties.getProperty(ADMIN_ID);
        adminPWD = properties.getProperty(ADMIN_PWD);
        String endpoint = baseUrl +
          "/api/v2/sessions";       
         
        StringBuilder response = new StringBuilder();
        
        try {
            URL url = new URL(endpoint);
            httpConnection = (HttpURLConnection) url.openConnection();
            httpConnection.setRequestMethod("POST");
            httpConnection.setRequestProperty("Content-Type",
                    "application/json");
            httpConnection.setDoOutput(true);       
            requestJSON = "{ \"login_name\" : \"" + adminID
                    + "\", \"password\" : \""
                    + adminPWD + "\" }";
            
            logger.debug("requestJSON: " + requestJSON);
            httpConnection.setRequestProperty("Content-Length",
                    Integer.toString(requestJSON.getBytes().length));
            httpConnection.setRequestProperty("Content-Language", "en-US");
            httpConnection.setUseCaches(false);
            httpConnection.setDoOutput(true);

            // Send request
            DataOutputStream wr =
                    new DataOutputStream(httpConnection.getOutputStream());
            wr.writeBytes(requestJSON);
            wr.close();

            // Get Response
            is = httpConnection.getInputStream();
            BufferedReader rd =
                    new BufferedReader(new InputStreamReader(is));
            /* response = new StringBuilder(); */
            String line;
            while ((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();
            logger.debug(
                    "Response in getphpListToken() " + response.toString());
            statusCode = httpConnection.getResponseCode();
        } catch (IOException e) {           
            
            try {
                statusCode = httpConnection.getResponseCode();
                logger.debug("statusCode: " + statusCode);
                
            } catch (IOException ioe) {
                logger.error("Exception in session creation: getphpListToken ", ioe);
            }
            httpConnection.disconnect();
            logger.error("Exception in session creation: getphpListToken ", e);
         
            return (SESSION_FAILED);
        }
        logger.debug("statusCode: " + statusCode);
        if(statusCode==201) {
            token = getToken(response.toString());
        }else {
            token = SESSION_FAILED;
        }
       return token;
    }
    
    /**
     * @author pramesh
     * @param response
     * @return
     * This method retuns the Base64 encoded phpList token
     */
    private String getToken(final String response) {
        logger.info("getToken:Enter");
        String token = null;
        if (!response.equals("")) {
            JSONObject jsonObj = new JSONObject(response);
            token = (String) jsonObj.get("key");
        }
        logger.info("getToken:End : "+token);
        String encodedToken = Base64.getEncoder().encodeToString((":"+token).getBytes(StandardCharsets.UTF_8));
        logger.info("EncodedToken: "+encodedToken);
        return encodedToken;
    }
    
    /**@author pramesh
     * @param response
     * @return
     */
    private int getResponseValue(final String response,String key) {
        logger.info("getResponseValue:Enter");
        int respValue=0;
        if (!response.equals("")) {
            JSONObject jsonObj = new JSONObject(response);
            respValue = (Integer) jsonObj.get(key);
        }
        logger.info("getResponseValue:End : "+respValue);
        
        return respValue;
    }

    public void doPost(HttpServletRequest request,
            HttpServletResponse response)
            throws IOException, ServletException {
        doGet(request, response);

    }
    
    public PhpListUserBO getsyncphpData(String query, int userid, int listid, String tabName) {
        
        logger.info("NewsletterConfirmation : getsyncphpData");      
        
        PreparedStatement prepareStatement = null;      
      
        Connection connection = null;
        ResultSet rs = null;       
        PhpListUserBO pluBO = null;
        try {
            connection = mysql.getConnection();
            prepareStatement = connection
                    .prepareStatement(query);
            if(tabName.equals("USERDATA")) {
                prepareStatement.setInt(1, userid);
            }else if(tabName.equals("LISTDATA")){
                prepareStatement.setInt(1, userid);
                prepareStatement.setInt(2, listid);
            }
            
            
            logger.debug("query : " + query);
            rs = prepareStatement.executeQuery();
            while(rs.next()) { 
                
                if(tabName.equals("USERDATA")) {
                    pluBO = new PhpListUserBO();
                    pluBO.setId(rs.getInt("ID"));
                    pluBO.setEmail(rs.getString("EMAIL"));
                    pluBO.setConfirmed(rs.getInt("CONFIRMED"));
                    pluBO.setBlacklisted(rs.getInt("BLACKLISTED"));
                    pluBO.setOptedin(rs.getInt("OPTEDIN"));
                    pluBO.setBouncecount(rs.getInt("BOUNCECOUNT"));
                    pluBO.setEntered(rs.getDate("ENTERED"));
                    pluBO.setModified(rs.getDate("MODIFIED"));
                    pluBO.setUniqid(rs.getString("UNIQID"));
                    pluBO.setUuid(rs.getString("UUID"));
                    pluBO.setHtmlemail(rs.getInt("HTMLEMAIL"));
                    pluBO.setSubscribepage(rs.getInt("SUBSCRIBEPAGE"));
                    pluBO.setRssfrequency(rs.getString("RSSFREQUENCY"));
                    pluBO.setPassword(rs.getString("PASSWORD"));
                    pluBO.setPasswordchanged(rs.getDate("PASSWORDCHANGED"));
                    pluBO.setDisabled(rs.getInt("DISABLED"));
                    pluBO.setExtradata(rs.getString("EXTRADATA"));
                    pluBO.setForeignkey(rs.getString("FOREIGNKEY"));
                    
                }else {
                    
                    pluBO = new PhpListUserBO();
                    pluBO.setUserid(rs.getInt("USERID"));
                    pluBO.setListid(rs.getInt("LISTID"));
                    pluBO.setEntered(rs.getDate("ENTERED"));
                    pluBO.setModified(rs.getDate("MODIFIED"));
                }
                
            }
           
        } catch (Exception e) {
            logger.error("Exception in getsyncphpData", e);
        } finally {
            mysql.releaseConnection(connection, prepareStatement, null);
        }   
     
        return pluBO;
        
    }
    
    private String updatesyncphpData(String query, PhpListUserBO pluBO, String tabName) {
        logger.info("NewsletterConfirmation : updatesyncphpData");  
        logger.info("PhpListUserBO "+pluBO.getId()+" "+pluBO.getEmail()+ " "+pluBO.getEntered()); 
       
        Connection connection = null;
        PreparedStatement prepareStatement = null;
        String syncStatus = "";
        ResultSet rs = null;
        try {
            connection = postgre.getConnection();
            prepareStatement = connection
                    .prepareStatement(query);
            if(tabName.equals("USERDATA")) {
                
                prepareStatement.setInt(1, pluBO.getId() );
                prepareStatement.setString(2, pluBO.getEmail());
                prepareStatement.setInt(3,pluBO.getConfirmed());
                prepareStatement.setInt(4, pluBO.getBlacklisted());
                prepareStatement.setInt(5, pluBO.getOptedin());
                prepareStatement.setInt(6, pluBO.getBouncecount());
                prepareStatement.setDate(7, pluBO.getEntered());
                prepareStatement.setDate(8, pluBO.getModified());
                prepareStatement.setString(9, pluBO.getUniqid());
                prepareStatement.setString(10, pluBO.getUuid());
                prepareStatement.setInt(11, pluBO.getHtmlemail());
                prepareStatement.setInt(12, pluBO.getSubscribepage());
                prepareStatement.setString(13, pluBO.getRssfrequency());
                prepareStatement.setString(14, pluBO.getPassword());
                prepareStatement.setDate(15, pluBO.getPasswordchanged());
                prepareStatement.setInt(16, pluBO.getDisabled());
                prepareStatement.setString(17, pluBO.getExtradata());
                prepareStatement.setString(18, pluBO.getForeignkey());
                
            }else if(tabName.equals("LISTDATA")){
                
                prepareStatement.setInt(1, pluBO.getUserid() );
                prepareStatement.setInt(2, pluBO.getListid() );
                prepareStatement.setDate(3, pluBO.getEntered());
                prepareStatement.setDate(4, pluBO.getModified());
            }
            
            int rowCount = prepareStatement.executeUpdate();   
            if(rowCount > 0) {
                syncStatus = STATUS_SUCCESS;
            }else {
                syncStatus = STATUS_FAILED;
            }
        } catch (Exception e) {
            logger.error("Exception in getSubscriberPersona", e);
        } finally {
            postgre.releaseConnection(connection, prepareStatement, null);
        }        
      
        
        return syncStatus;
    }
    
    

}
