package com.hukoomi.livesite.external;

import com.hukoomi.utils.ESAPIValidator;
import com.hukoomi.utils.Postgre;
import com.interwoven.livesite.runtime.RequestContext;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.hukoomi.utils.GoogleRecaptchaUtil;
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.ValidationErrorList;

import javax.servlet.http.HttpSession;


public class FeedbackExternal {
    private static final String STATUS_ERROR_RECAPTHCHA =
            "errorInRecaptcha";
    private String locale = "";
    private String userID = "";
    private String pagePath = "";
    private String moduleName = "";
    private String persona = "";
    private String optSelected = "";
    private String feedback = "";
    private String topic = "";
    private String entity = "";
    private String table = "";
    private static final String LOCALE_CONSTANT = "locale";
    private static final String PAGEPATH_CONSTANT = "pagePath";
    private static final String MODULE_CONSTANT = "moduleName";
    private static final String PERSONA_CONSTANT = "persona";
    private static final String OPT_CONSTANT = "optSelected";
    private static final String TOPIC_CONSTANT = "topic";
    private static final String ENTITY_CONSTANT = "entity";
    private static final Logger logger = Logger.getLogger(FeedbackExternal.class);

    Postgre postgre = null;

    public Document insertFeedback(final RequestContext context) {
        boolean verify = false;
        String gRecaptchaResponse = null;
        logger.info("insertFeedback()====> Starts");
        Document feedbackDoc = DocumentHelper.createDocument();
        postgre = new Postgre(context);
        gRecaptchaResponse = context.getParameterString("captcha");
        HttpSession session = context.getRequest().getSession(true);
        String status=(String) session.getAttribute("status");
        logger.info("status="+status);
        if(status!=null && status.equals("valid")) {
            userID = (String) session.getAttribute("uid");
        }
        logger.info("userID:" + userID);
        locale = context.getParameterString(LOCALE_CONSTANT).trim().toLowerCase();
        pagePath = context.getParameterString(PAGEPATH_CONSTANT);
        moduleName = context.getParameterString(MODULE_CONSTANT);
        persona = context.getParameterString(PERSONA_CONSTANT);
        optSelected = context.getParameterString(OPT_CONSTANT);
        feedback = context.getParameterString("feedback");
        topic = context.getParameterString(TOPIC_CONSTANT);
        entity = context.getParameterString(ENTITY_CONSTANT);
        table = context.getParameterString("feedback_content").trim();
        logger.info("locale:" + locale);
        logger.info("table:" + table);
        GoogleRecaptchaUtil captchUtil = new GoogleRecaptchaUtil();
        verify = captchUtil.validateCaptcha(context,
                gRecaptchaResponse);
        logger.info("Recapcha verification status:" + verify);
        if (verify) {
            if(!"".equals(table)){

                int insertStatus = insertFeedback();
                if(insertStatus == 1){
                    logger.info("Feedback inserted");
                    status="Successfully Inserted";
                }else{
                    logger.info("Feedback not inserted");
                    status="Not Inserted";
                }

            }

        } else {
            status = STATUS_ERROR_RECAPTHCHA;
        }

        Element feedbackDocEle = feedbackDoc.addElement("insertResult");
        feedbackDocEle.setText(status);
        logger.info("insertFeedback()====> ends");
        return feedbackDoc;
    }


    private int insertFeedback() {
        logger.info("insertFeedback()====> Starts");
        ValidationErrorList errorList = new ValidationErrorList();
        if (!ESAPIValidator.checkNull(locale)) {
            locale  = ESAPI.validator().getValidInput(LOCALE_CONSTANT, locale, ESAPIValidator.ALPHABET, 20, false, true, errorList);
            if(!errorList.isEmpty()) {
                logger.info(errorList.getError(LOCALE_CONSTANT));
                logger.error("Not a valid parameter locale. The incident will not be logged.");
                return 0;
            }
        }
        if (!ESAPIValidator.checkNull(pagePath)) {
            pagePath  = ESAPI.validator().getValidInput(PAGEPATH_CONSTANT, pagePath, ESAPIValidator.URL, 255, false, true, errorList);
            if(!errorList.isEmpty()) {
                logger.info(errorList.getError(PAGEPATH_CONSTANT));
                logger.error("Not a valid parameter page-path. The incident will not be logged.");
                return 0;
            }
        }
        if (!ESAPIValidator.checkNull(moduleName)) {
            moduleName  = ESAPI.validator().getValidInput(MODULE_CONSTANT, moduleName, ESAPIValidator.ALPHANUMERIC_SPACE, 255, false, true, errorList);
            if(!errorList.isEmpty()) {
                logger.info(errorList.getError(MODULE_CONSTANT));
                logger.error("Not a valid parameter Module-name. The incident will not be logged.");
                return 0;
            }
        }
        if (!ESAPIValidator.checkNull(persona)) {
            persona  = ESAPI.validator().getValidInput(PERSONA_CONSTANT, persona, ESAPIValidator.ALPHANUMERIC_SPACE, 255, false, true, errorList);
            if(!errorList.isEmpty()) {
                logger.info(errorList.getError(PERSONA_CONSTANT));
                logger.error("Not a valid parameter persona. The incident will not be logged.");
                return 0;
            }
        }
        if (!ESAPIValidator.checkNull(optSelected)) {
            optSelected  = ESAPI.validator().getValidInput(OPT_CONSTANT, optSelected, ESAPIValidator.ALPHANUMERIC_SPACE, 255, false, true, errorList);
            if(!errorList.isEmpty()) {
                logger.info(errorList.getError(OPT_CONSTANT));
                logger.error("Not a valid parameter option selected. The incident will not be logged.");
                return 0;
            }
        }
        if (!ESAPIValidator.checkNull(topic)) {
            topic  = ESAPI.validator().getValidInput(TOPIC_CONSTANT, topic, ESAPIValidator.ALPHANUMERIC_SPACE, 255, false, true, errorList);
            if(!errorList.isEmpty()) {
                logger.info(errorList.getError(TOPIC_CONSTANT));
                logger.error("Not a valid parameter topic. The incident will not be logged.");
                return 0;
            }
        }
        if (!ESAPIValidator.checkNull(entity)) {
            entity  = ESAPI.validator().getValidInput(ENTITY_CONSTANT, entity, ESAPIValidator.ALPHANUMERIC_SPACE, 255, false, true, errorList);
            if(!errorList.isEmpty()) {
                logger.info(errorList.getError(ENTITY_CONSTANT));
                logger.error("Not a valid parameter entity. The incident will not be logged.");
                return 0;
            }
        }
        if (!ESAPIValidator.checkNull(userID)) {
            userID  = ESAPI.validator().getValidInput("userID", userID, ESAPIValidator.USER_ID, 255, false, true, errorList);
            if(!errorList.isEmpty()) {
                logger.info(errorList.getError("userID"));
                logger.error("Not a valid parameter userID. The incident will not be logged.");
                return 0;
            }
        }
        int result = 0;
        Connection connection = null;
        PreparedStatement prepareStatement = null;
        String feedbackInsertQuery = "INSERT INTO" + " "
                + table +"(\"USER_ID\",\"LANG\", \"PAGE_PATH\", \"MODULE_NAME\", \"PERSONA\", \"OPTION_SELECTED\",\"FEEDBACK\",\"FEEDBACK_DATE\",\"TOPIC\",\"ENTITY\")"
                + " VALUES(?,?,?,?,?,?,?,LOCALTIMESTAMP,?,?)";
        logger.info("feedbackInsertQuery:" +feedbackInsertQuery);

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
                logger.info("Connection is null !");
            }
        }catch(SQLException ex){
            logger.error("Exception on insert Query:", ex);
        }finally {
            postgre.releaseConnection(connection, prepareStatement, null);
        }
        logger.info("insertFeedback()====> ends");
        return result;
    }

    private Connection getConnection() {
        return postgre.getConnection();
    }


}
