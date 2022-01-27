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
import java.util.Enumeration;
import java.util.Properties;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.hukoomi.utils.XssUtils;

import com.interwoven.wcm.service.iwovregistry.utils.IREncryptionUtil;

public class ErrorReport extends HttpServlet {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/** logger.debug object to check the flow of the code. */
    private static final Logger LOGGER =
            Logger.getLogger(ErrorReport.class);
    private static XssUtils xssUtils = new XssUtils();
    
	private static String statusPath = "statusPath";
	private static String language = "language";
	private static String strStatus = "status";
	private static String strStatusCodeVal = "statusCodeVal";
	private static String contentType = "application/json";
	private static String strSuccess = "success";
	private static String strFalse = "false";
	private static String encodingType = "UTF-8";
	private static String strErrorMessage = "errorMessage";
    @Override 
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response) throws ServletException {
        LOGGER.info("Update status : Start");
        JSONObject data = null;
        Enumeration<String> attributes = request.getSession().getAttributeNames();
        while (attributes.hasMoreElements())
            LOGGER.info("Value is: " + attributes.nextElement());
        try ( BufferedReader inbr = new BufferedReader(
                new InputStreamReader(request.getInputStream()));){         
            String json = "";
            json = inbr.readLine();
            data = new JSONObject(json);
            boolean result = updateErrorData(data);
            if (result) {
                data.put(strSuccess, strSuccess);
                response.getWriter().write(data.toString());
            } else {
                data.put(strSuccess, strFalse);
                data.put(strErrorMessage, "Failed to update");
                response.getWriter().write(data.toString());
            }

        } catch (IOException |JSONException e) {
        	data = new JSONObject();
            response.setContentType(contentType);
            response.setCharacterEncoding(encodingType);
            try {
                data.put(strSuccess, strFalse);
                data.put(strErrorMessage, e.getMessage());
                response.getWriter().write(data.toString());
            } catch (IOException | NullPointerException e1) {
            	 LOGGER.error("Status update Failed : Exception ", e);
            }
        } finally {
            LOGGER.info("Error Report End");
        }
    }
    
    @Override 
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException {
        LOGGER.info("Error Report : Start");
        JSONObject data = new JSONObject(); 
        JSONArray dataArray = null;
        
        try {              
            response.setContentType(contentType);
            response.setCharacterEncoding(encodingType);           
            data.put("path", xssUtils.stripXSS(request.getParameter("path")));
            data.put(statusPath, xssUtils.stripXSS(request.getParameter(statusPath)));
            data.put(language, xssUtils.stripXSS(request.getParameter(language)));
            data.put(strStatus, xssUtils.stripXSS(request.getParameter(strStatus)));
            data.put(strStatusCodeVal, xssUtils.stripXSS(request.getParameter(strStatusCodeVal)));                    
            	dataArray = getFilterResponse(data);
                data.put(strSuccess, strSuccess);
                data.put("comments", dataArray);
                response.getWriter().write(data.toString());

        } catch (IOException|JSONException e ) {
            response.setContentType(contentType);
            response.setCharacterEncoding(encodingType);
            try {
                data.put(strSuccess, strFalse);
                data.put(strErrorMessage, e.getMessage());
                response.getWriter().write(data.toString());
            } catch (IOException|NullPointerException e1) {
                LOGGER.error("Status update Failed : Exception ", e);
            }
        } finally {
            LOGGER.info("Error Report End");
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
            String status = xssUtils.stripXSS(data.getString(strStatus));
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
   
    
    
    private JSONArray getFilterResponse(JSONObject data) {
        LOGGER.info("Filter getErrorResponse");
        String propfilepath = data.getString("path");
       
        String errorStatuspath = data.getString(statusPath);
        
        String lang = data.getString(language);
        String statusCodeVal = data.getString(strStatusCodeVal);

        String statusVal = data.getString(strStatus);
        
        LOGGER.info(errorStatuspath);
        PreparedStatement prepareStatement = null;
        ResultSet rs = null;
        Properties dbProperties = loadProperties(propfilepath);
        Properties statusProperties = loadProperties(errorStatuspath);
  
        String statusData = statusProperties.getProperty(strStatus);
        String statusCode = statusProperties.getProperty("statusCode");
        
        LOGGER.info(statusCode);
        LOGGER.info(statusData);
        
        Connection connection = null;
        JSONArray arrayComments = new JSONArray();
        
        try {
            String userName = dbProperties.getProperty("username");            
            String password = dbProperties.getProperty("password");
  
            password = IREncryptionUtil.decrypt(password);
            connection = DriverManager.getConnection(
                    getConnectionString(dbProperties), userName, password);
            String getError = "SELECT * FROM ERROR_RESPONSE ";
            
            if(!lang.equalsIgnoreCase("ALL") || !statusVal.equalsIgnoreCase("ALL") || !statusCodeVal.equalsIgnoreCase("ALL")) {
            	getError += "WHERE "; 
             getError = geterroorStatement( getError, lang, statusVal, statusCodeVal);
            }
                prepareStatement = connection.prepareStatement(getError);
                
                rs = prepareStatement.executeQuery();

                while (rs.next()) {
                    LOGGER.debug("RESPONSE_ID: " + rs.getInt("RESPONSE_ID"));
                    int errorId = rs.getInt("RESPONSE_ID");
                    String brokenLink = rs.getString("BROKEN_LINK");
                    String contentPage = rs.getString("CONTENT_PAGE");
                    String lastReported = rs.getString("REPORTED_ON");
                    String errStatus = rs.getString("STATUS_CODE");
                    String count = rs.getString("COUNT");
                    String status = rs.getString("STATUS");
                    JSONObject errorData = new JSONObject();
                    errorData.put("errorId", errorId);
                    errorData.put("broken_link", brokenLink);
                    errorData.put("content_page", contentPage);
                    errorData.put("last_reported", lastReported);
                    errorData.put(language, lang);
                    errorData.put("status_code", errStatus);
                    errorData.put("count", count);
                    errorData.put(strStatus, status);
                    errorData.put("statusData", statusData);
                    errorData.put("statusCode", statusCode);
                    
                    
                    arrayComments.put(errorData);
                }
                rs.close();


        } catch (SQLException e) {
            LOGGER.error("getErrorResponse()", e);            

        } finally {
            releaseConnection(connection, prepareStatement, rs);
        }
        return arrayComments;
    }
    public String geterroorStatement(String getError, String lang, String statusVal, String statusCodeVal ) {
    	
         	if(!lang.equalsIgnoreCase("ALL") ) {
             	getError += "LANGUAGE = '"+lang.toLowerCase()+"' ";
             }
         	if(!statusVal.equalsIgnoreCase("ALL") ) {
         		if(!lang.equalsIgnoreCase("ALL") ) {
         			getError += "AND STATUS = '"+statusVal.toLowerCase()+"' ";
         		}else {
         			getError += "STATUS = '"+statusVal.toLowerCase()+"' ";
         		}
             	
             }
         	if(!statusCodeVal.equalsIgnoreCase("ALL") ) {
         		if(!statusVal.equalsIgnoreCase("ALL") || !lang.equalsIgnoreCase("ALL")) {
         			getError += "AND STATUS_CODE = '"+statusCodeVal.toLowerCase()+"' ";
         		}else {
         			getError += "STATUS_CODE = '"+statusCodeVal.toLowerCase()+"' ";
         		}
             	
             }

         
         getError += " ORDER BY REPORTED_ON DESC ";
         return getError;
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
           
            try( InputStream inputStream = new FileInputStream(root);) {
            		propFile.load(inputStream);
                    LOGGER.info("Properties File Loaded");
                
            } catch (MalformedURLException e) {
                LOGGER.error(
                        "Malformed URL Exception while loading Properties file : ",
                        e);
            } catch (IOException e) {
                LOGGER.error(
                        "IO Exception while loading Properties file : ",
                        e);
            }catch(Exception e) {
            	LOGGER.error(
                        "Exception while loading Properties file : ",
                        e);
            }

        } else {
            LOGGER.info("Invalid / Empty properties file name.");
        }
        LOGGER.info("Finish Loading Properties File.");
        return propFile;
    }

}
