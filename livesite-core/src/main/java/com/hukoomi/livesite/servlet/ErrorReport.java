package com.hukoomi.livesite.servlet;


import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;


import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.hukoomi.utils.XssUtils;

import com.interwoven.wcm.service.iwovregistry.utils.IREncryptionUtil;

public class ErrorReport extends HttpServlet {

    /** logger.debug object to check the flow of the code. */
    private static final Logger LOGGER =
            Logger.getLogger(ReviewComment.class);
    XssUtils xssUtils = new XssUtils();
    
   

    
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response) throws ServletException {
        LOGGER.info("Update status : Start");
        JSONObject data = null;
        Enumeration<String> attributes = request.getSession().getAttributeNames();
        while (attributes.hasMoreElements())
            LOGGER.info("Value is: " + attributes.nextElement());
        try {
            BufferedReader inbr = new BufferedReader(
                    new InputStreamReader(request.getInputStream()));
            String json = "";
            json = inbr.readLine();
            data = new JSONObject(json);
            boolean result = updateErrorData(data);
            if (result) {
                data.put("success", "success");
                response.getWriter().write(data.toString());
            } else {
                data.put("success", "false");
                data.put("errorMessage", "Failed to update");
                response.getWriter().write(data.toString());
            }

        } catch (IOException e) {
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            try {
                data.put("success", "false");
                data.put("errorMessage", e.getMessage());
                response.getWriter().write(data.toString());
            } catch (IOException e1) {
                LOGGER.error("REVIEW Failed : Exception ", e);
            }
        } finally {
            LOGGER.info("End of Review comment");
        }
    }
    
  
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException {
        LOGGER.info("ReviewComment : Start");
        JSONObject data = null;
        JSONArray dataArray = null;
        
        try {
            data = new JSONObject();            
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");           
            data.put("path", xssUtils.stripXSS(request.getParameter("path")));
            data.put("statusPath", xssUtils.stripXSS(request.getParameter("statusPath")));
            	dataArray = getErrorResponse(data);
            
                data.put("success", "success");
                data.put("comments", dataArray);
                response.getWriter().write(data.toString());
           

        } catch (IOException e) {
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            try {
                data.put("success", "false");
                data.put("errorMessage", e.getMessage());
                response.getWriter().write(data.toString());
            } catch (IOException e1) {
                LOGGER.error("REVIEW Failed : Exception ", e);
            }
        } finally {
            LOGGER.info("End of Review comment");
        }
    }

    
    private boolean updateErrorData(JSONObject data) {
        LOGGER.debug("BlogTask : insertBlogData");
        String path = data.getString("path");
        Properties dbProperties = loadProperties(path);
        Connection connection = null;
        boolean isDataUpdated = false;
        try {
            String userName = dbProperties.getProperty("username");            
            String password = dbProperties.getProperty("password");
            password = IREncryptionUtil.decrypt(password);
            connection = DriverManager.getConnection(
                    getConnectionString(dbProperties), userName, password);
            LOGGER.debug("UpdateErrorStatus : after getConnection");
            int result = updateErrorStatus(connection, data);
            
            if (result > 0) {
            	  LOGGER.info("Error status updated");
                isDataUpdated = true;
            } else {
                LOGGER.info("Error status update failed");
            }

        } catch (Exception e) {
            LOGGER.error("Exception in Update error data catch block : ",
                    e);
        } finally {
            releaseConnection(connection, null, null);
            LOGGER.info("Released Update error status connection");
        }
        return isDataUpdated;
    }
    
    /**
     * This method updates the comment status for approved/rejected
     * @param connection
     * @param data
     * @return
     */
    private int updateErrorStatus(Connection connection, JSONObject data) {
        PreparedStatement preparedStatement = null;
        int result = 0;
        try {
            
            long errorId = data.getLong("errorId");
            String status = xssUtils.stripXSS(data.getString("status"));
            String query =
            		"UPDATE ERROR_RESPONSE SET STATUS = ? WHERE RESPONSE_ID = ?";
            LOGGER.info("Query : " + query);
            preparedStatement = connection.prepareStatement(query);

            preparedStatement.setString(1, status);
            preparedStatement.setLong(2, errorId);
            result = preparedStatement.executeUpdate();
            LOGGER.info("update error status result : " + result);
                      
        } catch (NumberFormatException | SQLException e) {
            LOGGER.error("Exception in updateErrorStatus: ", e);
        } finally {
            releaseConnection(null, preparedStatement, null);
            LOGGER.info("Released updateErrorStatus connection");
        }
        return result;
    }

    /**
     * This method returns all the comments submitted for
     * specific blog based on blogID
     * @param data
     * @return
     */
    private JSONArray getErrorResponse(JSONObject data) {
        LOGGER.info("getErrorResponse");
        String propfilepath = data.getString("path");
       
        String errorStatuspath = data.getString("statusPath");
        
        LOGGER.info(errorStatuspath);
        PreparedStatement prepareStatement = null;
        ResultSet rs = null;
        Properties dbProperties = loadProperties(propfilepath);
        Properties statusProperties = loadProperties(errorStatuspath);
  
        String statusData = statusProperties.getProperty("status");
        LOGGER.info(statusData);
        
        Connection connection = null;
        JSONArray arrayComments = new JSONArray();
        String getComment ="";
        try {
            String userName = dbProperties.getProperty("username");            
            String password = dbProperties.getProperty("password");
  
            password = IREncryptionUtil.decrypt(password);
            connection = DriverManager.getConnection(
                    getConnectionString(dbProperties), userName, password);
           
                getComment = "SELECT * FROM ERROR_RESPONSE";
                prepareStatement = connection.prepareStatement(getComment);
                LOGGER.debug("getComment :" + getComment);
                rs = prepareStatement.executeQuery();

                while (rs.next()) {
                    LOGGER.debug("RESPONSE_ID: " + rs.getInt("RESPONSE_ID"));
                    int errorId = rs.getInt("RESPONSE_ID");
                    String broken_link = rs.getString("BROKEN_LINK");
                    String content_page = rs.getString("CONTENT_PAGE");
                    String last_reported = rs.getString("REPORTED_ON");
                    String language = rs.getString("LANGUAGE");
                    String status_code = rs.getString("STATUS_CODE");
                    String count = rs.getString("COUNT");
                    String status = rs.getString("STATUS");
                    JSONObject errorData = new JSONObject();
                    errorData.put("errorId", errorId);
                    errorData.put("broken_link", broken_link);
                    errorData.put("content_page", content_page);
                    errorData.put("last_reported", last_reported);
                    errorData.put("language", language);
                    errorData.put("status_code", status_code);
                    errorData.put("count", count);
                    errorData.put("status", status);
                    errorData.put("statusData", statusData);
                   
                   
                    
                    arrayComments.put(errorData);
                }
                rs.close();


        } catch (SQLException e) {
            LOGGER.error("getErrorResponse()", e);            

        } finally {
            releaseConnection(connection, null, null);
        }
        return arrayComments;
    }
    
    private String getConnectionString(Properties properties) {
        LOGGER.info("Postgre : getConnectionString()");
        String connectionStr = null;
        String host = properties.getProperty("host");
        String port = properties.getProperty("port");
        String database = properties.getProperty("database");
        String schema = properties.getProperty("schema");

        connectionStr = "jdbc:" + database + "://" + host + ":" + port
                + "/" + schema;

        LOGGER.debug("Connection String : " + connectionStr);
        return connectionStr;
    }

    /**
     * This method will be used for closing connection, statement and
     * resultset.
     *
     * @param con  Database connection to be closed
     * @param stmt Statement to be closed
     * @param rs   ResultSet to be closed
     *
     */
    public void releaseConnection(Connection con, Statement stmt,
            ResultSet rs) {
        LOGGER.info("Postgre : releaseConnection()");
        if (con != null) {
            try {
                con.close();
            } catch (Exception e) {
                LOGGER.error(
                        "Postgre : releaseConnection() : connection : ",
                        e);
            }
        }
        if (stmt != null) {
            try {
                stmt.close();
            } catch (Exception e) {
                LOGGER.error(
                        "Postgre : releaseConnection() : statement : ", e);
            }
        }
        if (rs != null) {
            try {
                rs.close();
            } catch (Exception e) {
                LOGGER.error(
                        "Postgre : releaseConnection() : resultset : ", e);
            }
        }
    }

      

  
   
    
    /**
     * This method will be used to load the configuration properties.
     *
     * @param context The parameter context object passed from Component.
     * @throws IOException
     * @throws MalformedURLException
     *
     */
    private Properties loadProperties(final String Propfilepath) {
        LOGGER.info("Loading Properties File from Request Context.");
        Properties propFile = new Properties();
        if (Propfilepath != null && !Propfilepath.equals("")) {
            String root = Propfilepath;
            InputStream inputStream;
            try {
                inputStream = new FileInputStream(root);
                if (inputStream != null) {
                    propFile.load(inputStream);
                    LOGGER.info("Properties File Loaded");
                }
            } catch (MalformedURLException e) {
                LOGGER.error(
                        "Malformed URL Exception while loading Properties file : ",
                        e);
            } catch (IOException e) {
                LOGGER.error(
                        "IO Exception while loading Properties file : ",
                        e);
            }

        } else {
            LOGGER.info("Invalid / Empty properties file name.");
        }
        LOGGER.info("Finish Loading Properties File.");
        return propFile;
    }

}
