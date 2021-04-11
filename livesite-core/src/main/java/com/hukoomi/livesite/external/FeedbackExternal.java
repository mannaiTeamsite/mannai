package com.hukoomi.livesite.external;

import com.hukoomi.utils.CommonUtils;
import com.hukoomi.utils.Postgre;
import com.hukoomi.utils.RequestHeaderUtils;
import com.interwoven.livesite.runtime.RequestContext;
import org.apache.commons.lang.WordUtils;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.hukoomi.utils.GoogleRecaptchaUtil;

import javax.servlet.http.HttpSession;


public class FeedbackExternal {
    private static final String STATUS_ERROR_RECAPTHCHA =
            "errorInRecaptcha";
    private static final String STATUS_SUCCESS = "success";
    private boolean verify = false;

    private String locale = "";
    private String userID = "";
    private String pagePath = "";
    private String moduleName = "";
    private String persona = "";
    private String optSelected = "";
    private String feedback = "";
    private String feedbackDate = "";
    private String topic = "";
    private String entity = "";
    private String gRecaptchaResponse = null;
    private String table = "";

    private int topSearchLimit;

    private static final Logger logger = Logger.getLogger(FeedbackExternal.class);
    Postgre postgre = null;

    public Document insertFeedback(final RequestContext context) {
        logger.debug("insertFeedback()====> Starts");
        RequestHeaderUtils requestHeaderUtils = new RequestHeaderUtils(context);
        CommonUtils commonUtils = new CommonUtils();
        Document feedbackDoc = DocumentHelper.createDocument();
        String status="";
        postgre = new Postgre(context);
        gRecaptchaResponse = context.getParameterString("captcha");

        HttpSession session = context.getRequest().getSession();
        session.getAttribute("status");
        if(status.equals("valid")) {
            userID = session.getAttribute("uid").toString();
        }
        if(userID=="")
        {
            userID=null;
        }
        logger.debug("userID:" + userID);
        locale = context.getParameterString("locale").trim().toLowerCase();
        if(locale=="")
        {
            locale=null;
        }
        pagePath = context.getParameterString("pagePath");
        if(pagePath=="")
        {
            pagePath=null;
        }
        moduleName = context.getParameterString("moduleName");
        if(moduleName=="")
        {
            moduleName=null;
        }
        persona = context.getParameterString("persona");
        if(persona=="")
        {
            persona=null;
        }
        optSelected = context.getParameterString("optSelected");
        if(optSelected=="")
        {
            optSelected=null;
        }
        feedback = context.getParameterString("feedback");
        if(feedback=="")
        {
            feedback=null;
        }
        feedbackDate = context.getParameterString("feedbackDate");

        topic = context.getParameterString("topic");
        if(topic=="")
        {
            topic=null;
        }
        entity = context.getParameterString("entity");
        if(entity=="")
        {
            entity=null;
        }
        table = context.getParameterString("feedback_content").trim();

        logger.debug("locale:" + locale);

        logger.debug("table:" + table);
        GoogleRecaptchaUtil captchUtil = new GoogleRecaptchaUtil();
        verify = captchUtil.validateCaptcha(context,
                gRecaptchaResponse);
        logger.debug("Recapcha verification status:" + verify);
        if (verify) {
            if(!"".equals(table)){

                int insertStatus = insertFeedback();
                if(insertStatus == 1){
                    logger.debug("Feedback inserted");
                    status="Successfully Inserted";
                }else{
                    logger.debug("Feedback not inserted");
                    status="Not Inserted";
                }

            }

        } else {
            status = STATUS_ERROR_RECAPTHCHA;
        }

        Element feedbackDocEle = feedbackDoc.addElement("insertResult");
        feedbackDocEle.setText(status);
        logger.debug("insertFeedback()====> ends");
        return feedbackDoc;
    }


    private int insertFeedback() {
        logger.debug("insertFeedback()====> Starts");
        int result = 0;
        Connection connection = null;
        PreparedStatement prepareStatement = null;
        String feedbackInsertQuery = "INSERT INTO" + " "
                + table +"(\"USER_ID\",\"LANG\", \"PAGE_PATH\", \"MODULE_NAME\", \"PERSONA\", \"OPTION_SELECTED\",\"FEEDBACK\",\"FEEDBACK_DATE\",\"TOPIC\",\"ENTITY\")"
                + " VALUES(?,?,?,?,?,?,?,LOCALTIMESTAMP,?,?)";
        logger.debug("feedbackInsertQuery:" +feedbackInsertQuery);

        try{
            connection = getConnection();
            if(connection != null){

                prepareStatement =  connection.prepareStatement(feedbackInsertQuery);
                prepareStatement.setString(1, userID);
                prepareStatement.setString(2, locale);
                prepareStatement.setString(3, pagePath);
                prepareStatement.setString(4, moduleName);
                prepareStatement.setString(5, persona);
                prepareStatement.setString(6, optSelected);
                prepareStatement.setString(7, feedback);
                prepareStatement.setString(8, topic);
                prepareStatement.setString(9, entity);
                result = prepareStatement.executeUpdate();

            }else{
                logger.debug("Connection is null !");
            }
        }catch(SQLException ex){
            logger.error("Exception on insert Query:", ex);
        }finally {
            postgre.releaseConnection(connection, prepareStatement, null);
        }
        logger.debug("insertFeedback()====> ends");
        return result;
    }

    private Connection getConnection() {
        return postgre.getConnection();
    }


}
