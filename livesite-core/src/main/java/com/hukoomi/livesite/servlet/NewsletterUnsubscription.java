package com.hukoomi.livesite.servlet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;


import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.hukoomi.utils.MySqlForServlet;
import com.hukoomi.utils.PostgreForServlet;

public class NewsletterUnsubscription extends HttpServlet{
    
    /** Logger object to check the flow of the code. */
    private static final Logger logger = Logger
            .getLogger(NewsletterUnsubscription.class);
    
    /** Postgre Object variable. */
    PostgreForServlet postgre = null;
    
    /** Mysql Object variable. */
    MySqlForServlet mysql = null;
    
    /**
     * Constant for status success.
     */
    public static final String STATUS_SUCCESS = "success";
    
    /**
     * Constant for table name.
     */
    public static final String NEWSLETTER_MASTER = "newsletter_master";

    /**
     * Constant for table name.
     */
    public static final String NEWSLETTER_PREFERENCE =
            "newsletter_preference";
    
    /**
     * phpList subscriber email
     */
    private static final String SUBSCRIBER_EMAIL = "subscriber_email";
    
    /**
     * phpList subscriber id
     */
    private static final String SUBSCRIBER_ID = "subscriber_id";
    
    /**
     * phpList unsubscription reason
     */
    private static final String UNSUBSCRIBE_REASON = "unsubreason";
    
    /** active status constant. */
    private static final String STATUS_ACTIVE = "Active";
    /** inActive status constant. */
    private static final String STATUS_INACTIVE = "InActive";
    /** Unsubscribed status constant. */
    private static final String STATUS_UNSUBSCRIBED = "Unsubscribed";

    public void doGet(HttpServletRequest request,
            HttpServletResponse response)
            throws IOException, ServletException {
        logger.info("NewsletterUnsubscription : doGet()");  
        postgre = new PostgreForServlet();
        mysql = new MySqlForServlet();
        
        double subscriberID ;
        String subscriberemail = "";
        
        String token = request.getParameter("token");
        String pageLang = request.getParameter("lang");
        String unsubReason = request
                .getParameter("unsubreason");
        String status="";
        logger.debug("token "+token+" lang "+pageLang+" unsubReason "+unsubReason);  
        RequestDispatcher rd = request
                .getRequestDispatcher(
                        "/portal-" + pageLang + "/home.page");          
        
        subscriberID= getSubscriberID(token);
        subscriberemail = getSubscriberEmail(subscriberID);
        
        logger.debug("NewsletterUnsubscription subscriberID "+subscriberID+" subscriberemail "+subscriberemail);  
        if(token != null && !token.equals("")) {
            
            status = unsubscribeDashboardUser(token,
                    unsubReason,subscriberID,subscriberemail);
            logger.debug("NewsletterUnsubscription : unsubscription in DB completed"+status);  
            if(status.equals(STATUS_SUCCESS)) {
             logger.debug("NewsletterUnsubscription : unsubscription in DB completed");
            
            Cookie confirmationCookie = new Cookie("unsubscriptionStatus",
                    "unSubscriptionSuccess:"+subscriberemail);
            response.addCookie(confirmationCookie);            
            }
            
        }else{            
           
            Cookie confirmationCookie = new Cookie("unsubscriptionStatus",
                    "unsubscribe");
            response.addCookie(confirmationCookie);            
            
        }
        
        rd.forward(request, response); 
    }
    
    /**
     * @author Pramesh
     * @param userId
     * @return
     * 
     *         This method unssubscribes dashboard user and update status
     */
    private String unsubscribeDashboardUser
    (String token, String unsubReason,double subscriberID, String email) {
        logger.info(
                "NewsletterUnsubscription : unsubscribeDashboardUser()");

        String unsubStatus = "";
        String status = "";
        int blacklistID = 1;
        
        /* int phpID = getPhpID(email); */
        
        String updateMasterQuery =
                "UPDATE NEWSLETTER_MASTER SET STATUS = ? , UNSUBSCRIBED_REASON = ? WHERE SUBSCRIBER_ID = ?";
        String updatePreferenceQuery =
                "UPDATE NEWSLETTER_PREFERENCE SET STATUS = ? WHERE SUBSCRIBER_ID = ?";
        String updatePhpQuery =
                "UPDATE PHPLIST_USER_USER SET BLACKLISTED = ? WHERE EMAIL = ?";
        status = unsubscribePostgreUser(updateMasterQuery, subscriberID,
                 unsubReason, STATUS_UNSUBSCRIBED, NEWSLETTER_MASTER);
        logger.debug("unsubscribeDashboardUser() : update master status "+ status);
        if (status.equals(STATUS_SUCCESS)) {            
            status = unsubscribePostgreUser(updatePreferenceQuery,
                    subscriberID,unsubReason,STATUS_INACTIVE, NEWSLETTER_PREFERENCE);
        }
        logger.debug(
                "unsubscribeDashboardUser() : update preference status "+ status);

        if (status.equals(STATUS_SUCCESS)) {

            unsubStatus =
                    unsubscribePhpUser(updatePhpQuery, email , unsubReason );
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
            String Query, double subscriberId, String unsubReason, String status , String tabName) {
        logger.info(
                "NewsletterUnsubscription : unsubscribeMasterUser()");

        String rsstatus = "";

        Connection connection = null;
        PreparedStatement prepareStatement = null;
        ResultSet rs = null;

        try {
            connection = postgre.getConnection();
            prepareStatement = connection.prepareStatement(Query);
            
            if (tabName.equals(NEWSLETTER_PREFERENCE)) {

                prepareStatement.setString(1, status);
                prepareStatement.setDouble(2, subscriberId);   
            } else {

                prepareStatement.setString(1, status);
                prepareStatement.setString(2, unsubReason);
                prepareStatement.setDouble(3, subscriberId);
           
            }     

            int count = prepareStatement.executeUpdate();

            if(count > 0) {
                rsstatus = STATUS_SUCCESS;
            }

        } catch (Exception e) {
            logger.error("Exception in unsubscribePostgreUser", e);
        } finally {
            postgre.releaseConnection(connection, prepareStatement, rs);
        }

        return rsstatus;
    }
    
     /**
     * @author Pramesh
     * @param Query
     * @param userId
     * @return
     */
    private String unsubscribePhpUser(String Query, String email, String unsubReason) {
        logger.info("NewsletterUnsubscription : unsubscribePhpUser() ");

        String status = "";
        int blacklistID = 1;
        Connection connection = null;
        PreparedStatement prepareStatement = null;
        ResultSet rs = null;
        String updatePhpBlacklist =
                "INSERT INTO PHPLIST_USER_BLACKLIST (EMAIL, ADDED) VALUES (?,LOCALTIMESTAMP)";
        String updatePhpBlacklistData =
                "INSERT INTO PHPLIST_USER_BLACKLIST_DATA (EMAIL, NAME, DATA) VALUES (?,?,?)";
        try {
            connection = mysql.getConnection();
            prepareStatement = connection.prepareStatement(Query);

            prepareStatement.setInt(1, blacklistID);
            prepareStatement.setString(2, email);

            int count = prepareStatement.executeUpdate();

            if(count > 0) {                
                status = updatePhpBlacklist(updatePhpBlacklist,email,"");
                if(status.equals(STATUS_SUCCESS)) {
                    
                    status = updatePhpBlacklist(updatePhpBlacklistData,email,unsubReason);
                }
            }

        } catch (Exception e) {
            logger.error("Exception in unsubscribePhpUser", e);
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
    private String updatePhpBlacklist(String Query, String email, String unsubReason) {
        logger.info("NewsletterUnsubscription : updatePhpBlacklist() ");

        String status = "";        
        Connection connection = null;
        PreparedStatement prepareStatement = null;
        ResultSet rs = null;

        try {
            connection = mysql.getConnection();
            prepareStatement = connection.prepareStatement(Query);
            if(!"".equals(unsubReason)) {
                prepareStatement.setString(1, email);
                prepareStatement.setString(2, "reason");
                prepareStatement.setString(3, unsubReason);
                
            }else {                
                prepareStatement.setString(1, email);
            }
            

            int count = prepareStatement.executeUpdate();

            if(count > 0) {
                status = STATUS_SUCCESS;
            }

        } catch (Exception e) {
            logger.error("Exception in updatePhpBlacklist", e);
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
    /*
     * private int getPhpID(String email) {
     * logger.info("NewsletterUnsubscription : getPhpID()");
     * 
     * String status = ""; int phpID = 0; Connection connection = null;
     * PreparedStatement prepareStatement = null; ResultSet rs = null;
     * String query = "SELECT ID FROM PHPLIST_USER_USER WHERE EMAIL = ?";
     * 
     * try { connection = mysql.getConnection(); prepareStatement =
     * connection.prepareStatement(query);
     * 
     * prepareStatement.setString(1, email);
     * 
     * rs = prepareStatement.executeQuery();
     * 
     * while(rs.next()) { phpID = rs.getInt(1);; }
     * 
     * } catch (Exception e) {
     * logger.error("Exception in unsubscribePhpUser", e); } finally {
     * postgre.releaseConnection(connection, prepareStatement, rs); }
     * 
     * return phpID; }
     */
    
    /**
     * @author pramesh
     * @param subscriberId
     * @return
     * 
     *         This method get the subscriber email based on subscriber id
     */
    private String getSubscriberEmail(double subscriberId) {
        logger.info("NewsletterUnsubscription : getSubscriberEmail");
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
    private double getSubscriberID(String token) {
        logger.info("NewsletterUnsubscription : getSubscriberID()");
        
        String subscriberIDQuery =
                    "SELECT SUBSCRIBER_ID FROM NEWSLETTER_CONFIRMATION_TOKEN WHERE TOKEN = ?";
            
        
       
        double subscriberID = 0;
        Connection connection = null;
        PreparedStatement prepareStatement = null;
        ResultSet rs = null;

        try {
            connection = postgre.getConnection();
            prepareStatement =
                    connection.prepareStatement(subscriberIDQuery);
            prepareStatement.setString(1, token);
            rs = prepareStatement.executeQuery();

            while (rs.next()) {
                subscriberID = rs.getDouble(SUBSCRIBER_ID);
            }

            logger.debug("NewsletterUnsubscription subscriberID : " + subscriberID);

        } catch (Exception e) {
            logger.error("Exception in subscriberID", e);
        } finally {
            postgre.releaseConnection(connection, prepareStatement, rs);
        }
        return subscriberID;
    }

}
