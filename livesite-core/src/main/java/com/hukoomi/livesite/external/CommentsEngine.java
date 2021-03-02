package com.hukoomi.livesite.external;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import com.hukoomi.utils.Postgre;
import com.hukoomi.utils.RequestHeaderUtils;
import com.hukoomi.utils.ValidationUtils;
import com.hukoomi.utils.XssUtils;
import com.interwoven.livesite.runtime.RequestContext;

/** Logger object to check the flow of the code. */

public class CommentsEngine {
    private static final Logger LOGGER =
            Logger.getLogger(CommentsEngine.class);

    private static final String ELEMENT_RESULT = "Result";
    private static final String ELEMENT_STATUS = "Status";

    public Document insertComment(final RequestContext context) throws NumberFormatException, SQLException {
        LOGGER.info("CommentsEngine");
        Document document = null;

        ValidationUtils util = new ValidationUtils();
        XssUtils xssUtils = new XssUtils();
        RequestHeaderUtils requestHeaderUtils =
                new RequestHeaderUtils(context);
        String action = context.getParameterString("action");
        String dcrId = "";
        String language = context.getParameterString("locale");
        if (action.equals("getComments")) {
            String cursorSize = context.getParameterString("cursorSize");
            document = getComments(dcrId, Integer.parseInt(cursorSize),
                    language, context);
        } else if (action.equals("setComment")) {
            String ip = requestHeaderUtils.getClientIpAddress();
            String comments = context.getParameterString("comments");
            String blogUrl = context.getParameterString("blog_url");
            String userName = context.getParameterString("username");

            int blogId = getBlogId(dcrId, language, context);
            document = insertCommentsToDB(blogId, blogUrl, comments,
                    userName, ip, context);
        } else if (action.equals("getCommentCount")) {

            dcrId = context.getParameterString("dcr_id");
            int blogId = getBlogId(dcrId, language, context);
            document = getCommentCount(dcrId, language, context);

        }
        return document;
    }

    private Document getCommentCount(String dcrId, String language,
            RequestContext context) {
        Connection connection = null;
        PreparedStatement prepareStatement = null;
        ResultSet rs = null;
        Postgre objPostgre = new Postgre(context);
        int count = 0;
        final String getcount =
                "SELECT COUNT(*) as total FROM BLOG_MASTER WHERE DCR_ID = ? AND LANGUAGE = ? AND STATUS = ?";
        try {
            connection = objPostgre.getConnection();
            prepareStatement = connection.prepareStatement(getcount);
            prepareStatement.setString(1, dcrId);
            prepareStatement.setString(2, language);
            prepareStatement.setString(3, "Approved");
            rs = prepareStatement.executeQuery();
            while (rs.next()) {
                LOGGER.debug("Count: " + rs.getInt("total"));
                count = rs.getInt("total");
            }
        } catch (SQLException e) {
            LOGGER.error("getCommentCount()", e);
            e.printStackTrace();

        } finally {
            objPostgre.releaseConnection(connection, prepareStatement, rs);
        }
        objPostgre.releaseConnection(connection, prepareStatement, rs);
        Document document = DocumentHelper.createDocument();
        Element resultElement = document.addElement(ELEMENT_RESULT);
        Element statusElement = resultElement.addElement("Count");
        statusElement.setText(String.valueOf(count));
        return document;
    }

    private Document getComments(String dcrId, int cursorSize,
            String language, RequestContext context) throws SQLException {
        Connection connection = null;
        PreparedStatement prepareStatement = null;
        ResultSet rs = null;
        Postgre objPostgre = new Postgre(context);
        int commentId = 0;
        int blogId = 0;
        String commentStr = "";
        String username = "";
        String commentOn = "";
        Document document = DocumentHelper.createDocument();
        try {
                blogId = getBlogId(dcrId, language, context);
                LOGGER.debug("BLOG_ID: " + blogId);

            if (blogId > 0) {
                String getComment =
                        "SELECT COMMENT_ID, COMMENT, USER_NAME, COMMENTED_ON FROM BLOG_COMMENT WHERE BLOG_ID = ? AND STATUS = ? ";
                connection = objPostgre.getConnection();
                connection.setAutoCommit(false);
                prepareStatement = connection.prepareStatement(getComment);
                prepareStatement.setLong(1, blogId);
                prepareStatement.setString(2, "Approved");
                LOGGER.debug("getComment :" + getComment);
                prepareStatement.setFetchSize(cursorSize);

                rs = prepareStatement.executeQuery();
                Element resultElement =
                        document.addElement(ELEMENT_RESULT);
                while (rs.next()) {
                    LOGGER.debug("COMMENT_ID: " + rs.getInt("COMMENT_ID"));
                    Element comments =
                            resultElement.addElement("Comments");
                    Element id = comments.addElement("CommentId");
                    commentId = rs.getInt("COMMENT_ID");
                    id.setText(String.valueOf(commentId));
                    Element comment = comments.addElement("Comment");
                    commentStr = rs.getString("COMMENT");
                    comment.setText(commentStr);
                    Element eleUsername = comments.addElement("UserName");
                    username = rs.getString("USER_NAME");
                    eleUsername.setText(String.valueOf(username));
                    Element eleCommentOn =
                            comments.addElement("CommentOn");
                    commentOn = rs.getString("COMMENTED_ON");
                    eleCommentOn.setText(commentOn);
                }
                rs.close();
            }

        } catch (SQLException e) {
            LOGGER.error("getBlogId()", e);
            e.printStackTrace();

        } finally {
            objPostgre.releaseConnection(connection, prepareStatement, rs);
        }
        objPostgre.releaseConnection(connection, prepareStatement, rs);
        return document;
    }

    private int getBlogId(String dcrId, String language,
            RequestContext context) {
        Connection connection = null;

        PreparedStatement prepareStatement = null;
        ResultSet rs = null;
        Postgre objPostgre = new Postgre(context);
        int blogId = 0;
        final String query =
                "SELECT BLOG_ID FROM BLOG_MASTER WHERE DCR_ID = ? AND LANGUAGE = ?";
        try {
            connection = objPostgre.getConnection();

            prepareStatement = connection.prepareStatement(query);
            prepareStatement.setString(1, dcrId);
            prepareStatement.setString(2, language);
            LOGGER.debug("query : " + query);
            rs = prepareStatement.executeQuery();
            while (rs.next()) {
                LOGGER.debug("Count: " + rs.getInt("BLOG_ID"));
                blogId = rs.getInt("BLOG_ID");
            }
        } catch (SQLException e) {
            LOGGER.error("getBlogId()", e);
            e.printStackTrace();

        } finally {
            objPostgre.releaseConnection(connection, prepareStatement, rs);
        }
        objPostgre.releaseConnection(connection, prepareStatement, rs);
        return blogId;
    }

    public Document insertCommentsToDB(int blogId, String blogUrl,
            String comments, String userName, String ip,
            RequestContext context) {
        LOGGER.info("CommentEngine : insertCommentsToDB");
        Postgre objPostgre = new Postgre(context);
        Document document = DocumentHelper.createDocument();
        Connection connection = null;
        PreparedStatement prepareStatement = null;
        String insertQuery = "";

        insertQuery =
                "INSERT INTO BLOG_COMMENT (BLOG_ID,BLOG_URL,COMMENT,COMMENTED_ON,USER_NAME,USER_IP_ADDRESS,STATUS) VALUES(?,?,?,LOCALTIMESTAMP,?,?,?)";
        try {
            connection = objPostgre.getConnection();
            prepareStatement = connection.prepareStatement(insertQuery);
            prepareStatement.setInt(1, blogId);
            prepareStatement.setString(2, blogUrl);
            prepareStatement.setString(3, comments);
            prepareStatement.setString(4, userName);
            prepareStatement.setString(5, ip);
            prepareStatement.setString(6, "Pending");
            LOGGER.debug("query : " + insertQuery);
            final int result = prepareStatement.executeUpdate();
            if (result == 0) {
                LOGGER.info("failed to insert/update comments data!");
                document = getDocument("failed");
            } else {
                LOGGER.info(" comments data insert/update successfully!");
                document = getDocument("success");
            }
        } catch (SQLException e) {
            LOGGER.error("SQLException :", e);
            document = getDocument("failed");

        } finally {
            objPostgre.releaseConnection(connection, prepareStatement,
                    null);
        }

        return document;
    }

    private Document getDocument(final String status) {
        LOGGER.info("getDocument:Enter");
        Document document = DocumentHelper.createDocument();
        Element resultElement = document.addElement(ELEMENT_RESULT);
        Element statusElement = resultElement.addElement(ELEMENT_STATUS);
        statusElement.setText(status);
        return document;
    }

}
