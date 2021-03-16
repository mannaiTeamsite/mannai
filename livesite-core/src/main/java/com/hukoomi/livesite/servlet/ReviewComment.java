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
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.hukoomi.utils.CommonUtils;

public class ReviewComment extends HttpServlet {

    /** logger.debug object to check the flow of the code. */
    private static final Logger LOGGER =
            Logger.getLogger(ReviewComment.class);

    protected void doPost(HttpServletRequest request,
            HttpServletResponse response) throws ServletException {
        LOGGER.info("ReviewComment : Start");
        JSONObject data = null;
        try {
            BufferedReader inbr = new BufferedReader(
                    new InputStreamReader(request.getInputStream()));
            String json = "";
            json = inbr.readLine();
            data = new JSONObject(json);
            boolean result = updateReviewData(data);
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
            data.put("blogId", Integer.parseInt(request.getParameter("blogId")));
            data.put("path", request.getParameter("path"));
            dataArray = getCommentbyBlogId(data);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            if (dataArray != null) {
                data.put("success", "success");
                data.put("comments", dataArray);
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
    private boolean updateReviewData(JSONObject data) {
        LOGGER.debug("BlogTask : insertBlogData");
        String path = data.getString("path");
        Properties dbProperties = loadProperties(path);
        Connection connection = null;
        boolean isDataUpdated = false;
        try {
            String userName = dbProperties.getProperty("username");
            String password = dbProperties.getProperty("password");
            connection = DriverManager.getConnection(
                    getConnectionString(dbProperties), userName, password);
            LOGGER.debug("BlogTask : after getConnection");
            int result = updateCommentData(connection, data);
            LOGGER.info("insertBlogData result : " + result);
            if (result > 0) {
                LOGGER.info("Blog Master Data Inserted");
                isDataUpdated = true;
            } else {
                LOGGER.info("Blog master insert failed");
            }

        } catch (Exception e) {
            LOGGER.error(
                    "Exception in insertBlogData rollback catch block : ",
                    e);
        } finally {
            releaseConnection(connection, null, null);
            LOGGER.info("Released insertBlogData connection");
        }
        return isDataUpdated;
    }

    private int updateCommentData(Connection connection, JSONObject data) {
        PreparedStatement preparedStatement = null;
        int result = 0;
        try {

            long commentId = data.getLong("commentId");
            String status = data.getString("status");
            String query =
                    "UPDATE BLOG_COMMENT SET STATUS = ?, STATUS_UPDATED_ON = LOCALTIMESTAMP "
                            + "WHERE COMMENT_ID = ? ";
            LOGGER.info("Query : " + query);
            preparedStatement = connection.prepareStatement(query);

            preparedStatement.setString(1, status);
            preparedStatement.setLong(2, commentId);
            result = preparedStatement.executeUpdate();
            LOGGER.info("update comment result : " + result);

        } catch (NumberFormatException | SQLException e) {
            LOGGER.error("Exception in updateBlogMasterData: ", e);
        } finally {
            releaseConnection(null, preparedStatement, null);
            LOGGER.info("Released updateBlogMasterData connection");
        }
        return result;
    }
    private JSONArray getCommentbyBlogId(JSONObject data) {
        LOGGER.info("getCommentbyBlogId");
        int blogId = data.getInt("blogId");
        String Propfilepath = data.getString("path");
        PreparedStatement prepareStatement = null;
        ResultSet rs = null;
        Properties dbProperties = loadProperties(Propfilepath);
        Connection connection = null;
        JSONArray arrayComments = new JSONArray();
        CommonUtils util = new CommonUtils();
        try {
            String userName = dbProperties.getProperty("username");
            String password = dbProperties.getProperty("password");
            connection = DriverManager.getConnection(
                    getConnectionString(dbProperties), userName, password);
            if (blogId > 0) {
                String getComment =
                        "SELECT COMMENT_ID, COMMENT, USER_NAME, COMMENTED_ON,BLOG_URL,USER_IP_ADDRESS  FROM BLOG_COMMENT WHERE BLOG_ID = ? AND STATUS = ? ORDER BY COMMENT_ID ";
                prepareStatement = connection.prepareStatement(getComment);
                prepareStatement.setLong(1, blogId);
                prepareStatement.setString(2, "Pending");
                LOGGER.debug("getComment :" + getComment);
                rs = prepareStatement.executeQuery();

                while (rs.next()) {LOGGER.debug("COMMENT_ID: " + rs.getInt("COMMENT_ID"));
                    int commentId = rs.getInt("COMMENT_ID");
                    String commentStr = rs.getString("COMMENT");
                    String username = rs.getString("USER_NAME");
                    String commentOn = rs.getString("COMMENTED_ON");
                    String blogUrl = rs.getString("BLOG_URL");
                    String ip = rs.getString("USER_IP_ADDRESS");
                    JSONObject Comments = new JSONObject();
                    Comments.put("CommentId", commentId);
                    LOGGER.debug(" before decode commentStr :" + commentStr);
                    LOGGER.debug(" after decode commentStr :" +  util.decodeToArabicString(commentStr));
                    Comments.put("Comment", util.decodeToArabicString(commentStr));
                    Comments.put("UserName", util.decodeToArabicString(username));
                    Comments.put("CommentOn", commentOn);
                    Comments.put("BlogURL", blogUrl);
                    Comments.put("IP", ip);
                    arrayComments.put(Comments);
                }
                rs.close();
            }

        } catch (SQLException e) {
            LOGGER.error("getBlogId()", e);
            e.printStackTrace();

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
            String root =
                    Propfilepath;
            InputStream inputStream;
            try {
                inputStream = new FileInputStream(
                        root );
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
