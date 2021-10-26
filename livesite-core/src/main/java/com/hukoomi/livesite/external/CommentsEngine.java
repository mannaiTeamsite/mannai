package com.hukoomi.livesite.external;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.ValidationErrorList;

import com.hukoomi.utils.ESAPIValidator;
import com.hukoomi.utils.GoogleRecaptchaUtil;
import com.hukoomi.utils.Postgre;
import com.hukoomi.utils.PropertiesFileReader;
import com.hukoomi.utils.RequestHeaderUtils;
import com.hukoomi.utils.XssUtils;
import com.interwoven.livesite.runtime.RequestContext;

public class CommentsEngine {
    /** Logger object to check the flow of the code. */
    private static final Logger LOGGER = Logger.getLogger(CommentsEngine.class);

    private static final String ELEMENT_RESULT = "Result";
    private static final String ELEMENT_STATUS = "Status";
    final String BLOG_ACTION = "blogAction";
    final String LOCALE = "locale";
    final String DCR_ID = "dcr_id";
    final String BLOG_URL = "BlogURL";
    final String OFFSET = "offset";
    final String NO_OF_ROWS = "noOfRows";
    final String IP = "ip";
    private static final String STATUS_FIELD_VALIDATION = "FieldValidationFailed";
    private static final String STATUS_ERROR_RECAPTHCHA = "errorInRecaptcha";
    String status = "";
    /** mail properties key. */
    private static final String CONTACT_FROM_MAIL = "sentFrom";
    /** mail properties key. */
    private static final String CONTACT_TO_MAIL = "sentTo";
    /** mail properties key. */
    private static final String CONTACT_MAIL_HOST = "host";
    /** mail properties key. */
    private static final String CONTACT_MAIL_PORT = "port";
    /** mail properties key. */
    private static final String STARTTLS_ENABLE = "false";
    /** character set Constant */
    private static final String CHAR_SET = "UTF-8";
    /** character set Constant */
    private static final String CONST_STATUS = "status";

    /**
     * This method internally makes call to get Comment/set comment/ get count
     * of approved comments
     *
     * @param context
     * @return Document
     * @throws SQLException
     */
    public Document commentEngine(final RequestContext context) throws SQLException {
        LOGGER.info("CommentsEngine");
        Document document = null;
        RequestHeaderUtils requestHeaderUtils = new RequestHeaderUtils(context);
        XssUtils xssUtils = new XssUtils();
        String action = xssUtils.stripXSS(context.getParameterString("action"));
        if (validateAction(action)) {
            int blogId = 0;
            String blogName = "";

            String dcrId = xssUtils.stripXSS(context.getParameterString("dcr_id"));
            String language = xssUtils.stripXSS(context.getParameterString("locale"));
            if (validateDCR(dcrId, language)) {
                switch (action) {
                case "getComments":
                    String noOfRows = xssUtils.stripXSS(context.getParameterString("noOfRows"));
                    String offset = xssUtils.stripXSS(context.getParameterString("offset"));
                    if (validateGetCommentCount(noOfRows, offset)) {
                        document = getComments(dcrId, Integer.parseInt(offset),
                                Integer.parseInt(noOfRows), language, context);
                    }
                    break;
                case "setComment":
                    String ip = requestHeaderUtils.getClientIpAddress();
                    String comments = xssUtils.stripXSS(context.getParameterString("comments"));
                    String blogUrl = xssUtils.stripXSS(context.getParameterString("blog_url"));
                    String userName = xssUtils.stripXSS(context.getParameterString("username"));
                    String gRecaptchaResponse =
                            xssUtils.stripXSS(context.getParameterString("recaptcha"));
                    if (validateCommentData(context, comments, userName, blogUrl, ip)) {
                        GoogleRecaptchaUtil captchUtil = new GoogleRecaptchaUtil();
                        boolean verify = captchUtil.validateCaptcha(context, gRecaptchaResponse);
                        if (verify) {
                            LOGGER.debug("Recapcha verification status:" + verify);

                            blogId = getBlogId(dcrId, language, context);
                            blogName = getBlogTitle(dcrId, language, context);

                            document = insertCommentsToDB(blogId, blogUrl, comments, userName, ip,
                                    context, blogName, language);

                        } else {
                            status = STATUS_ERROR_RECAPTHCHA;
                            return getDocument(status);
                        }
                    } else {
                        status = STATUS_FIELD_VALIDATION;
                        document = getDocument(status);
                    }

                    break;
                case "getCommentCount":
                    blogId = getBlogId(dcrId, language, context);
                    document = getCommentCount(blogId, context);
                    break;

                case "isUserLogged":
                    document = isUserLogged(context);
                    break;

                default:
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

    /**
     * This method validate the comments input data
     * 
     * @param comments
     * @param userName
     * @param blogUrl
     * @param ip
     * @return
     */
    private boolean validateCommentData(
            RequestContext context, String comments, String userName, String blogUrl, String ip
    ) {

        Properties propertiesFile = CommentsEngine.loadProperties(context);
        int userNameLength = Integer.parseInt(propertiesFile.getProperty("USERNAME_LENGTH"));
        int commentsLength = Integer.parseInt(propertiesFile.getProperty("COMMENTS_LENGTH"));

        if (userName.length() > userNameLength) {
            return false;
        } else if (comments.length() > commentsLength) {
            return false;
        } else {
            ValidationErrorList errorList = new ValidationErrorList();
            ESAPI.validator().getValidInput(BLOG_URL, blogUrl, ESAPIValidator.URL, 200, false, true,
                    errorList);
            if (errorList.isEmpty()) {
                ESAPI.validator().getValidInput(IP, ip, ESAPIValidator.IP_ADDRESS, 20, false, true,
                        errorList);
                return errorList.isEmpty();
            }
        }
        return false;
    }

    /**
     * This method is used to validate blog action
     * 
     * @param blogAction
     * @return
     */
    private boolean validateAction(String blogAction) {
        ValidationErrorList errorList = new ValidationErrorList();
        LOGGER.info(BLOG_ACTION + " >>>" + blogAction + "<<<");
        ESAPI.validator().getValidInput(BLOG_ACTION, blogAction, ESAPIValidator.ALPHABET, 20, false,
                true, errorList);
        return errorList.isEmpty();
    }

    /**
     * This method is used to validate DCR
     * 
     * @param dcrId
     * @param language
     * @return
     */
    private boolean validateDCR(String dcrId, String language) {
        ValidationErrorList errorList = new ValidationErrorList();

        LOGGER.info(DCR_ID + " >>>" + dcrId + "<<<");
        ESAPI.validator().getValidInput(DCR_ID, dcrId, ESAPIValidator.NUMERIC, 20, false, true,
                errorList);
        if (errorList.isEmpty()) {
            ESAPI.validator().getValidInput(LOCALE, language, ESAPIValidator.ALPHABET, 2, false,
                    true, errorList);
            if (errorList.isEmpty()) {
                return true;
            } else {
                LOGGER.info(errorList.getError(LOCALE));
                return false;
            }
        } else {
            LOGGER.info(errorList.getError(DCR_ID));
            return false;
        }
    }

    /**
     * @param noOfRows
     * @param offset
     * @return
     */
    private boolean validateGetCommentCount(String noOfRows, String offset) {
        ValidationErrorList errorList = new ValidationErrorList();

        LOGGER.info(DCR_ID + " >>>" + noOfRows + "<<<");
        ESAPI.validator().getValidInput(NO_OF_ROWS, noOfRows, ESAPIValidator.NUMERIC, 2, false,
                true, errorList);
        if (errorList.isEmpty()) {
            ESAPI.validator().getValidInput(OFFSET, offset, ESAPIValidator.NUMERIC, 2, false, true,
                    errorList);
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
                LOGGER.debug("Comment Count: " + rs.getInt("total"));
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
    private Document getComments(
            String dcrId, int offset, int noOfRows, String language, RequestContext context
    ) throws SQLException {
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
                        "SELECT COMMENT_ID, COMMENT, USER_NAME, COMMENTED_ON FROM BLOG_COMMENT "
                        + "WHERE BLOG_ID = ? AND STATUS = ? ORDER BY COMMENT_ID "
                                + "OFFSET ? ROWS " + "FETCH FIRST ? ROW ONLY;";
                connection = objPostgre.getConnection();
                prepareStatement = connection.prepareStatement(getComment);
                prepareStatement.setLong(1, blogId);
                prepareStatement.setString(2, "Approved");
                prepareStatement.setInt(3, offset);
                prepareStatement.setInt(4, noOfRows);
                LOGGER.debug("getComment :" + getComment);
                rs = prepareStatement.executeQuery();
                Element resultElement = document.addElement(ELEMENT_RESULT);
                while (rs.next()) {
                    LOGGER.debug("COMMENT_ID: " + rs.getInt("COMMENT_ID"));
                    Element comments = resultElement.addElement("Comments");
                    Element id = comments.addElement("CommentId");
                    commentId = rs.getInt("COMMENT_ID");
                    id.setText(String.valueOf(commentId));
                    Element comment = comments.addElement("Comment");
                    commentStr = rs.getString("COMMENT");
                    comment.setText(commentStr);
                    Element eleUsername = comments.addElement("UserName");
                    username = rs.getString("USER_NAME");
                    eleUsername.setText(String.valueOf(username));
                    Element eleCommentOn = comments.addElement("CommentOn");
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
    private int getBlogId(String dcrId, String language, RequestContext context) {
        Connection connection = null;

        PreparedStatement prepareStatement = null;
        ResultSet rs = null;
        Postgre objPostgre = new Postgre(context);
        int blogId = 0;
        final String query = "SELECT BLOG_ID FROM BLOG_MASTER WHERE DCR_ID = ? AND LANGUAGE = ?";
        try {
            connection = objPostgre.getConnection();

            prepareStatement = connection.prepareStatement(query);
            prepareStatement.setString(1, dcrId);
            prepareStatement.setString(2, language);
            rs = prepareStatement.executeQuery();
            while (rs.next()) {
                LOGGER.debug("BLOG ID : " + rs.getInt("BLOG_ID"));
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
     * return blog title for dcr id and language.
     *
     * @param dcrId
     * @param language
     * @param context
     * @return blogId
     */
    private String getBlogTitle(String dcrId, String language, RequestContext context) {
        Connection connection = null;

        PreparedStatement prepareStatement = null;
        ResultSet rs = null;
        Postgre objPostgre = new Postgre(context);
        String blogTitle = "";
        final String query = "SELECT BLOG_TITLE FROM BLOG_MASTER WHERE DCR_ID = ? AND LANGUAGE = ?";

        try {
            connection = objPostgre.getConnection();

            prepareStatement = connection.prepareStatement(query);
            prepareStatement.setString(1, dcrId);
            prepareStatement.setString(2, language);
            LOGGER.debug("query : " + query);
            rs = prepareStatement.executeQuery();
            while (rs.next()) {
                LOGGER.debug("BLOG TITLE " + rs.getString("BLOG_TITLE"));
                blogTitle = rs.getString("BLOG_TITLE");
            }
        } catch (SQLException e) {
            LOGGER.error("getBlogTitle", e);
        } finally {
            objPostgre.releaseConnection(connection, prepareStatement, rs);
        }
        return blogTitle;
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
    public Document insertCommentsToDB(
            int blogId, String blogUrl, String comments, String userName, String ip,
            RequestContext context, String blogtitle, String lang
    ) {
        LOGGER.info("CommentEngine : insertCommentsToDB");
        Postgre objPostgre = new Postgre(context);
        Document document = DocumentHelper.createDocument();
        Connection connection = null;
        PreparedStatement prepareStatement = null;
        String insertQuery = "";
        String emailid = "";
        HttpServletRequest request = context.getRequest();
        if (request.getSession().getAttribute(CONST_STATUS) != null
                && "valid".equals(request.getSession().getAttribute(CONST_STATUS))) {

            userName = request.getSession().getAttribute("fnEn").toString() + " "
                    + request.getSession().getAttribute("lnEn").toString();
            emailid = request.getSession().getAttribute("email").toString();
            LOGGER.info("username:" + userName);
        }
        insertQuery =
                "INSERT INTO BLOG_COMMENT (BLOG_ID,BLOG_URL,COMMENT,COMMENTED_ON,USER_NAME,"
                + "USER_IP_ADDRESS,STATUS,USER_EMAILID) VALUES(?,?,?,LOCALTIMESTAMP,?,?,?,?)";
        try {
            connection = objPostgre.getConnection();
            prepareStatement = connection.prepareStatement(insertQuery);
            prepareStatement.setInt(1, blogId);
            prepareStatement.setString(2, blogUrl);
            prepareStatement.setString(3, comments);
            prepareStatement.setString(4, userName);
            prepareStatement.setString(5, ip);
            prepareStatement.setString(6, "Pending");
            prepareStatement.setString(7, emailid);
            LOGGER.debug("query : " + insertQuery);
            final int result = prepareStatement.executeUpdate();
            if (result == 0) {
                LOGGER.info("failed to insert/update comments data!");
                document = getDocument("failed");
            } else {
                LOGGER.info(" comments data insert/update successfully!");
                document = getDocument("success");
                sentMailNotification(blogtitle, lang, userName, comments, context);
                LOGGER.info("Notification sent to comments approver!");
            }
        } catch (SQLException e) {
            LOGGER.error("SQLException :", e);
            document = getDocument("failed");

        } finally {
            objPostgre.releaseConnection(connection, prepareStatement, null);
        }

        return document;
    }

    /**
     * This method returns document with status updated.
     * 
     * @param status
     * @return
     */
    private Document getDocument(final String status) {
        LOGGER.info("getDocument:Enter");
        Document document = DocumentHelper.createDocument();
        Element resultElement = document.addElement(ELEMENT_RESULT);
        Element statusElement = resultElement.addElement(ELEMENT_STATUS);
        statusElement.setText(status);
        return document;
    }

    /**
     * This method is used to send the email notification
     * 
     * @param blogtitle
     * @param lang
     * @param context
     */
    private void sentMailNotification(
            String blogtitle, String lang, String username, String comments,
            final RequestContext context
    ) {

        MimeMessage mailMessage;
        LOGGER.debug(" lang::" + lang);
        try {
            mailMessage = createMailMessage(blogtitle, lang, username, comments, context);
            Transport.send(mailMessage);
        } catch (MessagingException e) {
            e.printStackTrace();
        }

    }

    /**
     * This Method creates the email message for the notification along with
     * all the message related set to MimeMessage
     * 
     * @param blogtitle
     * @param lang
     * @param context
     * @return
     * @throws MessagingException
     */
    private MimeMessage createMailMessage(
            String blogtitle, String lang, String username, String comments,
            final RequestContext context
    ) throws MessagingException {
        LOGGER.info("createMailMessage: Enter");

        String strBlogName = "<blogname>";
        String strUsername = "<username>";
        String strComments = "<comments>";
        Properties propertiesFile = CommentsEngine.loadProperties(context);
        String from = propertiesFile.getProperty(CONTACT_FROM_MAIL);
        String to = propertiesFile.getProperty(CONTACT_TO_MAIL);
        LOGGER.debug("sent To :" + to);
        String[] addressToList = to.split(",");
        InternetAddress[] dests = new InternetAddress[addressToList.length];
        int counter = 0;
        for (String addressTo : addressToList) {
            dests[counter] = new InternetAddress(addressTo.trim());
            counter++;
        }
        LOGGER.debug("Language :" + lang);
        String host = propertiesFile.getProperty(CONTACT_MAIL_HOST);
        LOGGER.debug("relay IP :" + host);
        String port = propertiesFile.getProperty(CONTACT_MAIL_PORT);
        Properties props = new Properties();
        String subject = "";
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.starttls.enable", STARTTLS_ENABLE);
        props.put("mail.smtp.port", port);
        Session session = Session.getDefaultInstance(props, null);
        session.setDebug(true);
        MimeMessage msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(from));
        msg.setRecipients(Message.RecipientType.TO, dests);
        subject = propertiesFile.getProperty("messageSubject_" + lang);
        StringBuilder sb = new StringBuilder();
        sb.append(propertiesFile.getProperty("successMessageBody_" + lang).replace(strBlogName,
                blogtitle));
        sb.append(propertiesFile.getProperty("successMessageBody1_" + lang).replace(strUsername,
                username));
        sb.append(propertiesFile.getProperty("successMessageBody2_" + lang).replace(strComments,
                comments));
        LOGGER.debug("SuccessMessageBody :" + sb.toString());
        if (lang.equals("ar")) {
            msg.setSubject(subject.replace(strBlogName, blogtitle), CHAR_SET);
            msg.setContent(sb.toString(), "text/html;Charset=UTF-8");
        } else {

            msg.setSubject(subject.replace(strBlogName, blogtitle));
            msg.setContent(sb.toString(), "text/html;Charset=UTF-8");
        }

        LOGGER.debug("msg:" + sb.toString());
        return msg;
    }

    /**
     * This method will be used to load the configuration properties.
     *
     * @param context The parameter context object passed from Component.
     * @return properties
     *
     */
    private static Properties loadProperties(final RequestContext context) {
        LOGGER.info("loadProperties:Begin");
        PropertiesFileReader propertyFileReader =
                new PropertiesFileReader(context, "blogcomment.properties");
        return propertyFileReader.getPropertiesFile();
    }

    /**
     * This method is used to check if user is logged in
     * 
     * @param context
     * @return
     */
    private Document isUserLogged(final RequestContext context) {
        LOGGER.info("isUserLogged:Enter");
        Boolean bool = false;
        String username = "";
        HttpServletRequest request = context.getRequest();
        if (request.getSession().getAttribute(CONST_STATUS) != null
                && "valid".equals(request.getSession().getAttribute(CONST_STATUS))) {
            bool = true;
            username = request.getSession().getAttribute("fnEn").toString();
        }
        Document document = DocumentHelper.createDocument();
        Element resultElement = document.addElement(ELEMENT_RESULT);
        Element statusElement = resultElement.addElement("Boolean");
        statusElement.setText(String.valueOf(bool));
        LOGGER.info("isUserLogged:Enter>>>>>>>" + username);
        Element usernameElement = resultElement.addElement("Username");
        usernameElement.setText(String.valueOf(username));
        LOGGER.info("isUserLogged:Enter<<<<<<<<<");
        return document;

    }

}
