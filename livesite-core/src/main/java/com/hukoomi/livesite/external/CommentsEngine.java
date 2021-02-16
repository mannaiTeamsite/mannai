package com.hukoomi.livesite.external;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;

import com.hukoomi.utils.Postgre;
import com.hukoomi.utils.ValidationUtils;
import com.hukoomi.utils.XssUtils;
import com.interwoven.livesite.runtime.RequestContext;

/** Logger object to check the flow of the code. */

public class CommentsEngine {
    private static final Logger LOGGER =
            Logger.getLogger(CommentsEngine.class);

    public Document insertComment(final RequestContext context) {
        LOGGER.info("CommentsEngine");
        Document document = null;
        ValidationUtils util = new ValidationUtils();
        XssUtils xssUtils = new XssUtils();
        String ip = context.getParameterString("ip");
        String comments = context.getParameterString("comments");
        String dcrName = context.getParameterString("dcr_name");
        String blogUrl = context.getParameterString("blog_url");
        String userName = context.getParameterString("username");
        document = insertCommentsToTable(comments, dcrName, blogUrl,
                userName, ip, context);
        return document;
    }

    public Document insertCommentsToTable(String comments, String dcrName,
            String blogUrl, String userName, String ip,
            RequestContext context) {
        LOGGER.info("CommentEngine : insertSubscriber");
        Postgre objPostgre = new Postgre(context);
        Document document = DocumentHelper.createDocument();
        Connection connection = null;
        PreparedStatement prepareStatement = null;
        String insertQuery = "";
        LOGGER.info("CommentEngine : insert");
        insertQuery =
                "INSERT INTO COMMENTS_BLOG (COMMENT,USER_NAME,BLOG_NAME,BLOG_URL,STATUS,IP,COMMENTED_ON) VALUES(?,?,?,?,?,LOCALTIMESTAMP)";
        try {
            connection = objPostgre.getConnection();
            prepareStatement = connection.prepareStatement(insertQuery);
            prepareStatement.setString(1, comments);
            prepareStatement.setString(2, userName);
            prepareStatement.setString(3, dcrName);
            prepareStatement.setString(4, blogUrl);
            prepareStatement.setString(5, ip);
            final int result = prepareStatement.executeUpdate();
            if (result == 0) {
                LOGGER.info("failed to insert/update comments data!");
            } else {
                LOGGER.info(" comments data insert/update successfully!");

            }
        } catch (SQLException e) {
            LOGGER.error("SQLException :", e);
            return document = null;

        } finally {
            objPostgre.releaseConnection(connection, prepareStatement,
                    null);
        }

        return document;
    }

}
