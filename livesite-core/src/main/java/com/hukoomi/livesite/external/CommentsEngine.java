package com.hukoomi.livesite.external;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.ValidationErrorList;

import com.hukoomi.utils.ESAPIValidator;
import com.hukoomi.utils.Postgre;
import com.hukoomi.utils.RequestHeaderUtils;
import com.hukoomi.utils.XssUtils;
import com.interwoven.livesite.runtime.RequestContext;

public class CommentsEngine {
    /** Logger object to check the flow of the code. */
    private static final Logger LOGGER =
            Logger.getLogger(CommentsEngine.class);

    private static final String ELEMENT_RESULT = "Result";
    private static final String ELEMENT_STATUS = "Status";
    final String BLOG_ACTION = "blogAction";
    final String LOCALE = "locale";
    final String DCR_ID = "dcr_id";
    final String USER_AGENT = "User-Agent";
    final String VOTED_FROM = "votedFrom";
    final String POLLID = "pollId";
    private static final String STATUS_FIELD_VALIDATION =
            "FieldValidationFailed";
    String status = "";

    /**
     * This method internally makes call to get Comment/set comment/ get
     * count of approved comments
     *
     * @param context
     * @return Document
     * @throws SQLException
     */
    public Document commentEngine(final RequestContext context)
            throws SQLException {
        LOGGER.info("CommentsEngine");
        Document document = null;
        RequestHeaderUtils requestHeaderUtils =
                new RequestHeaderUtils(context);
        XssUtils xssUtils = new XssUtils();
        String action =
                xssUtils.stripXSS(context.getParameterString("action"));
        if (validateAction(context)) {
            int blogId = 0;

            String dcrId = xssUtils
                    .stripXSS(context.getParameterString("dcr_id"));
            String language = xssUtils
                    .stripXSS(context.getParameterString("locale"));
            if (validateDCR(context, dcrId, language)) {
                switch (action) {
                case "getComments":
                    String noOfRows = xssUtils.stripXSS(
                            context.getParameterString("noOfRows"));
                    String offset = xssUtils.stripXSS(
                            context.getParameterString("offset"));
                    if(validateGetCommentCount(context, noOfRows, offset)) {
                        document = getComments(dcrId, Integer.parseInt(offset),
                                Integer.parseInt(noOfRows), language, context);
                    }
                    break;
                case "setComment":
                    String ip = requestHeaderUtils.getClientIpAddress();
                    String comments = xssUtils.stripXSS(
                            context.getParameterString("comments"));
                    String blogUrl = xssUtils.stripXSS(
                            context.getParameterString("blog_url"));
                    String userName = xssUtils.stripXSS(
                            context.getParameterString("username"));
                    if (validateCommentData(comments, userName)) {
                        blogId = getBlogId(dcrId, language, context);
                        document = insertCommentsToDB(blogId, blogUrl,
                                comments, userName, ip, context);
                    } else {
                        status = STATUS_FIELD_VALIDATION;
                        document = getDocument(status);
                    }

                    break;
                case "getCommentCount":
                    blogId = getBlogId(dcrId, language, context);
                    document = getCommentCount(blogId, context);
                    break;
                }
            } else {
                status = STATUS_FIELD_VALIDATION;
                document = getDocument(status);
            }

        } else {
            status = STATUS_FIELD_VALIDATION;
            document = getDocument(status);
        }

        return document;
    }

    private boolean validateCommentData(String comments, String userName) {
        if (userName.length() > 100) {
            return false;
        } else if (comments.length() > 150) {
            return false;
        }

        return true;
    }

    private boolean validateAction(RequestContext context) {
        ValidationErrorList errorList = new ValidationErrorList();

        String blogAction = context.getParameterString(BLOG_ACTION);
        LOGGER.info(BLOG_ACTION + " >>>" + blogAction + "<<<");
        ESAPI.validator().getValidInput(BLOG_ACTION, blogAction,
                ESAPIValidator.ALPHABET, 20, false, true, errorList);
        if (errorList.isEmpty()) {
            return true;
        } else {
            LOGGER.info(errorList.getError(BLOG_ACTION));
            return false;
        }
    }

    private boolean validateDCR(RequestContext context, String dcrId,
            String language) {
        ValidationErrorList errorList = new ValidationErrorList();

        LOGGER.info(DCR_ID + " >>>" + dcrId + "<<<");
        ESAPI.validator().getValidInput(DCR_ID, dcrId,
                ESAPIValidator.NUMERIC, 20, false, true, errorList);
        if (errorList.isEmpty()) {
            ESAPI.validator().getValidInput(LOCALE, language,
                    ESAPIValidator.ALPHABET, 2, false, true, errorList);
            if (errorList.isEmpty()) {
                return true;
            } else {
                LOGGER.info(errorList.getError(LOCALE));
                return false;
            }
        }

        else {
            LOGGER.info(errorList.getError(DCR_ID));
            return false;
        }
    }
    private boolean validateGetCommentCount(RequestContext context, String dcrId,
            String language) {
        ValidationErrorList errorList = new ValidationErrorList();

        LOGGER.info(DCR_ID + " >>>" + dcrId + "<<<");
        ESAPI.validator().getValidInput(DCR_ID, dcrId,
                ESAPIValidator.NUMERIC, 20, false, true, errorList);
        if (errorList.isEmpty()) {
            ESAPI.validator().getValidInput(LOCALE, language,
                    ESAPIValidator.ALPHABET, 2, false, true, errorList);
            if (errorList.isEmpty()) {
                return true;
            } else {
                LOGGER.info(errorList.getError(LOCALE));
                return false;
            }
        }

        else {
            LOGGER.info(errorList.getError(DCR_ID));
            return false;
        }
    }

    /**
     * method will return approved comments for a blog
     *
     * @param blogId
     * @param context
     * @return Document
     */
    private Document getCommentCount(int blogId, RequestContext context) {
        Connection connection = null;
        PreparedStatement prepareStatement = null;
        ResultSet rs = null;
        Postgre objPostgre = new Postgre(context);
        int count = 0;
        final String getcount =
                "SELECT COUNT(*) as total FROM BLOG_COMMENT WHERE BLOG_ID = ? AND STATUS = ?";
        try {
            connection = objPostgre.getConnection();
            prepareStatement = connection.prepareStatement(getcount);
            prepareStatement.setInt(1, blogId);
            prepareStatement.setString(2, "Approved");
            rs = prepareStatement.executeQuery();
            while (rs.next()) {
                LOGGER.debug("Count: " + rs.getInt("total"));
                count = rs.getInt("total");
            }
        } catch (SQLException e) {
            LOGGER.error("getCommentCount()", e);
        } finally {
            objPostgre.releaseConnection(connection, prepareStatement, rs);
        }
        Document document = DocumentHelper.createDocument();
        Element resultElement = document.addElement(ELEMENT_RESULT);
        Element statusElement = resultElement.addElement("Count");
        statusElement.setText(String.valueOf(count));
        return document;
    }

    /**
     * Method will return comments for a blog.
     *
     * @param dcrId
     * @param offset
     * @param noOfRows
     * @param language
     * @param context
     * @return
     * @throws SQLException
     */
    private Document getComments(String dcrId, int offset, int noOfRows,
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
                        "SELECT COMMENT_ID, COMMENT, USER_NAME, COMMENTED_ON FROM BLOG_COMMENT WHERE BLOG_ID = ? AND STATUS = ? ORDER BY COMMENT_ID "
                                + "OFFSET ? ROWS "
                                + "FETCH FIRST ? ROW ONLY;";
                connection = objPostgre.getConnection();
                prepareStatement = connection.prepareStatement(getComment);
                prepareStatement.setLong(1, blogId);
                prepareStatement.setString(2, "Approved");
                prepareStatement.setInt(3, offset);
                prepareStatement.setInt(4, noOfRows);
                LOGGER.debug("getComment :" + getComment);
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
        } finally {
            objPostgre.releaseConnection(connection, prepareStatement, rs);
        }
        return document;
    }

    /**
     * return blog id for dcr id and language.
     *
     * @param dcrId
     * @param language
     * @param context
     * @return blogId
     */
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
        } finally {
            objPostgre.releaseConnection(connection, prepareStatement, rs);
        }
        return blogId;
    }

    /**
     * method will insert comments and comments related data to db.
     *
     * @param blogId
     * @param blogUrl
     * @param comments
     * @param userName
     * @param ip
     * @param context
     * @return
     */
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
