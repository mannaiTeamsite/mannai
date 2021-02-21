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

    public Document insertComment(final RequestContext context) {
        LOGGER.info("CommentsEngine");
        Document document = null;
        ValidationUtils util = new ValidationUtils();
        XssUtils xssUtils = new XssUtils();
        RequestHeaderUtils requestHeaderUtils = new RequestHeaderUtils(context);
        String ip = requestHeaderUtils.getClientIpAddress();
        String comments = context.getParameterString("comments");
        String dcrId = context.getParameterString("dcr_id");
        String blogUrl = context.getParameterString("blog_url");
        String userName = context.getParameterString("username");
        String language = context.getParameterString("language");

        int blogId = getBlogId(dcrId, language, context);
        document = insertCommentsToDB(blogId, blogUrl, comments,
                userName, ip, context);
        return document;
    }

    private int getBlogId(String dcrId,String language, RequestContext context) {
        Connection connection = null;
        PreparedStatement prepareStatement = null;
        ResultSet rs = null;
        Postgre objPostgre = new Postgre(context);
        int blogId = 0;
        final String getcount =
                "SELECT BLOG_ID as total FROM BLOG_MASTER WHERE DCR_ID = ? && LANGUAGE = ?";
        try {
            connection = objPostgre.getConnection();
            prepareStatement = connection.prepareStatement(getcount);
            prepareStatement.setString(1, dcrId);
            prepareStatement.setString(2, language);
            rs = prepareStatement.executeQuery();
            while (rs.next()) {
                LOGGER.debug("Count: " + rs.getInt("total"));
                blogId = rs.getInt("total");
            }
        } catch (SQLException e) {
            LOGGER.error("isscubscriberExist()", e);
            e.printStackTrace();

        } finally {
            objPostgre.releaseConnection(connection, prepareStatement, rs);
        }
        objPostgre.releaseConnection(connection, prepareStatement, rs);
        return blogId;
    }

    public Document insertCommentsToDB(int blogId, String blogUrl, String comments,
             String userName, String ip,
            RequestContext context) {
        LOGGER.info("CommentEngine : insertSubscriber");
        Postgre objPostgre = new Postgre(context);
        Document document = DocumentHelper.createDocument();
        Connection connection = null;
        PreparedStatement prepareStatement = null;
        String insertQuery = "";
        LOGGER.info("CommentEngine : insert");
        insertQuery =
                "INSERT INTO BLOG_COMMENT (BLOG_ID,BLOG_URL,COMMENT,COMMENTED_ON,USER_NAME,) VALUES(?,?,?,LOCALTIMESTAMP,?)";
        try {
            connection = objPostgre.getConnection();
            prepareStatement = connection.prepareStatement(insertQuery);
            prepareStatement.setInt(1, blogId);
            prepareStatement.setString(2, blogUrl);
            prepareStatement.setString(3, comments);
            prepareStatement.setString(4, userName);
            prepareStatement.setString(5, ip);

            final int result = prepareStatement.executeUpdate();
            if (result == 0) {
                LOGGER.info("failed to insert/update comments data!");
                document= getDocument("failed");
            } else {
                LOGGER.info(" comments data insert/update successfully!");
                document= getDocument("success");
            }
        } catch (SQLException e) {
            LOGGER.error("SQLException :", e);
            document= getDocument("failed");


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
