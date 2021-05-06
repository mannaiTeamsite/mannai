package com.hukoomi.livesite.external;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.ValidationErrorList;

import com.hukoomi.bo.DashboardSettingsBO;
import com.hukoomi.utils.ESAPIValidator;
import com.hukoomi.utils.Postgre;
import com.interwoven.livesite.runtime.RequestContext;

/**
 * DashboardSettingsExternal is the components external class for dashboard settings.
 * 
 * @author Vijayaragavamoorthy
 *
 */
public class DashboardSettingsExternal {

    /** Logger object to check the flow of the code. */
    private static final Logger logger = Logger
            .getLogger(DashboardSettingsExternal.class);
    /**
     * Postgre Object variable.
     */
    Postgre postgre = null;
    /**
     * Persona Action variable.
     */
    //String settingsAction = null;
    /**
     * UserId variable.
     */
    //String userId = null;
    /**
     * Persona variable.
     */
    //String persona = null;
    /**
     * UserRole variable.
     */
    //String userRole = null;
    /**
     * Constant for action get persona settings.
     */
    public static final String ACTION_GET_ALL_SETTINGS = "getAllSettings";
    /**
     * Constant for action get persona settings.
     */
    public static final String ACTION_GET_PERSONA = "getPersonaForUser";
    /**
     * Constant for action update persona settings.
     */
    public static final String ACTION_UPDATE_PERSONA = "updatePersonaForUser";
    /**
     * Constant for status success.
     */
    public static final String STATUS_SUCCESS = "success";
    /**
     * Constant for status failed.
     */
    public static final String STATUS_FAILED = "failed";
    
    /**
     * This method will be called from Component External for fetching and updating the persona settings.
     * 
     * @param context Request context object.
     *
     * @return doc Returns the solr response document generated from solr query.
     * 
     * @deprecated
     */
    @Deprecated(since = "", forRemoval = false)
    public Document performDashboardSettingsAction(final RequestContext context) {
        logger.info("DashboardSettingsExternal : performPersonaSettingsAction()");
        DashboardSettingsBO settingsBO = new DashboardSettingsBO();
        Document doc = DocumentHelper.createDocument();
        Element responseElem = doc.addElement("dashboard-settings");
        postgre = new Postgre(context);
        if (validateInput(context, settingsBO)) {
            logger.info("Input data validation is successfull");
            logger.debug("settingsBO : "+settingsBO);
            Element userTypeElem = responseElem.addElement("user-type");
            userTypeElem.addText(settingsBO.getUserRole());
            if (ACTION_GET_ALL_SETTINGS.equalsIgnoreCase(settingsBO.getAction())) {
                getAllSettings(responseElem, settingsBO);
            //}else if (ACTION_GET_PERSONA.equalsIgnoreCase(personaAction)) {
                //doc = getPersonaForUser();
            } else if (ACTION_UPDATE_PERSONA.equalsIgnoreCase(settingsBO.getAction())) {
                updatePersonaForUser(responseElem, settingsBO);
            }
        }else {
            logger.info("Invalid input parameter");
            createResponseDoc(responseElem, settingsBO.getAction(), settingsBO.getAction(), settingsBO.getPersona(), STATUS_FAILED, "Invalid input parameter");
        }
        logger.debug("Final Result :" + doc.asXML());
        return doc;
    }
    
    /**
     * This method is used to validate the input.
     * 
     * @param context Request context object.
     * 
     * @return Returns true if the input is valid
     *         else returns false.
     * 
     */
    public boolean validateInput(final RequestContext context, DashboardSettingsBO settingsBO) {
        logger.info("DashboardSettingsExternal : validateInput()");
        
        ValidationErrorList errorList = new ValidationErrorList();
        final String SETTINGS_ACTION = "settingsAction";
        final String USER_ID = "userId";
        final String PERSONA = "persona";
        final String USER_ROLE = "userRole";
        String validData  = "";
        
        String settingsAction = context.getParameterString(SETTINGS_ACTION);
        logger.debug(SETTINGS_ACTION + " >>>"+settingsAction+"<<<");
        validData  = ESAPI.validator().getValidInput(SETTINGS_ACTION, settingsAction, ESAPIValidator.ALPHABET, 20, false, true, errorList);
        if(errorList.isEmpty()) {
            settingsBO.setAction(validData);
        }else {
            logger.info(errorList.getError(SETTINGS_ACTION));
            return false;
        }
        
        HttpServletRequest request = context.getRequest();
        if(request.getSession().getAttribute("status") != null && "valid".equals(request.getSession().getAttribute("status"))) {
            String userId = request.getSession().getAttribute("uid").toString();
            logger.debug(USER_ID + " >>>"+userId+"<<<");
            validData  = ESAPI.validator().getValidInput(USER_ID, userId, ESAPIValidator.USER_ID, 50, true, true, errorList);
            if(errorList.isEmpty()) {
                settingsBO.setUserId(validData);
            }else {
                logger.debug(errorList.getError(USER_ID));
                return false;
            }
            
            
            String userRole = request.getSession().getAttribute("role").toString();
            logger.debug(USER_ROLE + " >>>"+userRole+"<<<");
            validData  = ESAPI.validator().getValidInput(USER_ROLE, userRole, ESAPIValidator.ALPHABET, 50, true, true, errorList);
            if(errorList.isEmpty()) {
                settingsBO.setUserRole(validData);
            }else {
                logger.debug(errorList.getError(USER_ROLE));
                return false;
            }
        }
        //settingsBO.setUserId("Test_User");
        //settingsBO.setUserRole("Personal");
        
        
        /*userId = context.getParameterString(USER_ID);
        logger.info(USER_ID + " >>>"+userId+"<<<");
        validData  = ESAPI.validator().getValidInput(USER_ID, userId, ESAPIValidator.USER_ID, 50, false, true, errorList);
        if(errorList.isEmpty()) {
            userId = validData;
        }else {
            logger.info(errorList.getError(USER_ID));
            return false;
        }*/        
        
        
        if (ACTION_UPDATE_PERSONA.equalsIgnoreCase(settingsAction)) {
            String persona = context.getParameterString(PERSONA);
            logger.debug(PERSONA + " >>>"+persona+"<<<");
            validData  = ESAPI.validator().getValidInput(PERSONA, persona, ESAPIValidator.ALPHABET_HYPEN, 50, false, true, errorList);
            if(errorList.isEmpty()) {
                settingsBO.setPersona(validData);
            }else {
                logger.debug(errorList.getError(PERSONA));
                return false;
            }
        }
        return true;
    }
    
    
    /**
     * This method is used to retrieve all setting for the user from database.
     *
     * @return returns Document with the all dashboard settings information  
     */
    public void getAllSettings(Element responseElem, DashboardSettingsBO settingsBO) {
        logger.info("DashboardSettingsExternal : getAllSettings()");
        //Document doc = DocumentHelper.createDocument();
        try {
            String personaValue = getPersonaForUser(settingsBO.getUserId());
            logger.debug("Persona Value :" + personaValue);
            if (personaValue != null && !"".equals(personaValue)) {
                createResponseDoc(responseElem, settingsBO.getAction(), settingsBO.getUserId(), personaValue, STATUS_SUCCESS, "");
            } else {
                createResponseDoc(responseElem, settingsBO.getAction(), settingsBO.getUserId(), personaValue, STATUS_FAILED, "Persona value not available");
            }
        } catch (Exception e) {
            logger.error("Exception in getAllSettings", e);
        } 
        //return doc;
    }
    
    
    /**
     * This method is used to retrieve person setting for the user from database.
     *
     * @return returns the persona value  
     */
    public String getPersonaForUser(String userId) {
        logger.info("DashboardSettingsExternal : getPersonaForUser()");
        String personaValue = null;
        String personaQuery = "SELECT * FROM persona_settings WHERE user_id = ? AND active = True";
        logger.debug("personaQuery : " + personaQuery);
        Connection connection = null;
        PreparedStatement prepareStatement = null;
        ResultSet rs = null;

        try {
            connection = postgre.getConnection();
            prepareStatement = connection.prepareStatement(personaQuery);
            prepareStatement.setString(1, userId);
            rs = prepareStatement.executeQuery();
            while (rs.next()) {
                personaValue = rs.getString("persona_value");
                logger.debug("Persona Value :" + personaValue);
            }
        } catch (Exception e) {
            logger.error("Exception in getPersonaForUser", e);
        } finally {
            postgre.releaseConnection(connection, prepareStatement, rs);
        }

        return personaValue;
    }
    
    
    /**
     * This method is used to retrieve person setting for the user from database.
     *
     * @return returns Document with the persona value  
     */
    public void updatePersonaForUser(Element responseElem, DashboardSettingsBO settingsBO) {
        logger.info("DashboardSettingsExternal : updatePersonaForUser()");
        //Document doc = DocumentHelper.createDocument(); 
        Connection connection = postgre.getConnection();
        PreparedStatement prepareStatement = null;
        String personaSettingsQuery = null;
        
        if(isPersonaSettingExists(settingsBO.getUserId())) {
            try {
                personaSettingsQuery = "UPDATE persona_settings SET persona_value = ?, active = ?, modified_on = current_timestamp WHERE user_id = ?";
                prepareStatement = connection.prepareStatement(personaSettingsQuery);
                prepareStatement.setString(1, settingsBO.getPersona());
                prepareStatement.setBoolean(2, true);
                prepareStatement.setString(3, settingsBO.getUserId());
                int result = prepareStatement.executeUpdate();
                String personaValue = getPersonaForUser(settingsBO.getUserId());
                if (result == 0) {
                    logger.info("Persona settings updation failed!");
                    createResponseDoc(responseElem, settingsBO.getAction(), settingsBO.getUserId(), personaValue, STATUS_FAILED, "Persona settings updation failed");
                } else {
                    logger.info("Persona settings updated successfully!");
                    createResponseDoc(responseElem, settingsBO.getAction(), settingsBO.getUserId(), personaValue, STATUS_SUCCESS, "");
                }
            } catch (Exception e) {
                logger.error("Exception in updating persona settings", e);
                createResponseDoc(responseElem, settingsBO.getAction(), settingsBO.getUserId(), null, STATUS_FAILED, "Exception in updating persona settings");
            } finally {
                postgre.releaseConnection(connection, prepareStatement, null);
            }
        }else {
            try {
                personaSettingsQuery = "INSERT INTO persona_settings(user_id, persona_value, active, created_on, modified_on) VALUES (?, ?, ?, current_timestamp, current_timestamp)";
                prepareStatement = connection.prepareStatement(personaSettingsQuery);
                prepareStatement.setString(1, settingsBO.getUserId());
                prepareStatement.setString(2, settingsBO.getPersona());
                prepareStatement.setBoolean(3, true);
                int result = prepareStatement.executeUpdate();
                String personaValue = getPersonaForUser(settingsBO.getUserId());
                if (result == 0) {
                    logger.info("Persona settings insertion failed!");
                    createResponseDoc(responseElem, settingsBO.getAction(), settingsBO.getUserId(), personaValue, STATUS_FAILED, "Persona settings insertion failed");
                } else {
                    logger.info("Persona settings inserted successfully!");
                    createResponseDoc(responseElem, settingsBO.getAction(), settingsBO.getUserId(), personaValue, STATUS_SUCCESS, "");
                }
            } catch (Exception e) {
                logger.error("Exception in insert persona settings", e);
                createResponseDoc(responseElem, settingsBO.getAction(), settingsBO.getUserId(), null, STATUS_FAILED, "Exception in insert persona settings");
            } finally {
                postgre.releaseConnection(connection, prepareStatement, null);
            }
        }
        //return doc;
    }
    
    /**
     * This method is used to create the response document based on the input.
     * 
     * @param action Persona action from the request.
     * @param userId UserId of the logged-in user.
     * @param persona Persona for the logged-in user.
     * @param status Status of the request.
     * @param error Error messsage in the request processing is failed.
     * 
     * @return Returns document object with the response
     * 
     */
    public void createResponseDoc(Element responseElem, String action, String userId, String persona, String status, String error) {
        if(action == null) action = "";
        if(userId == null) userId = "";
        if(persona == null) persona = "";
        if(status == null) status = "";
        if(error == null) error = "";
        
        Document doc = DocumentHelper.createDocument();
        //Element responseElem = doc.addElement("persona-settings").addElement("response");
        Element personaresponseElem = responseElem.addElement("persona-settings");
        personaresponseElem.addElement("status").setText(status);
        personaresponseElem.addElement("settings-action").setText(action);
        personaresponseElem.addElement("user-id").setText(userId);
        personaresponseElem.addElement("persona").setText(persona);
        personaresponseElem.addElement("error").setText(error);
        //return doc;
    }
    
    /**
     * This method is used to check if a persona settins is available for the user in database.
     * 
     * 
     * @return Returns true if the persona settings exists for the user 
     *         else returns false.
     * 
     */
    public boolean isPersonaSettingExists(String userId) {
        boolean isPersonaSettingExists = false;

        logger.info("DashboardSettingsExternal : isPersonaSettingExists()");
        
        String personaSettingsQuery = "SELECT COUNT(*) FROM persona_settings WHERE user_id = ?";

        Connection connection = null;
        PreparedStatement prepareStatement = null;
        ResultSet rs = null;

        try {
            connection = postgre.getConnection();
            prepareStatement = connection.prepareStatement(personaSettingsQuery);
            prepareStatement.setString(1, userId);
            rs = prepareStatement.executeQuery();
            int count = 0;
            while (rs.next()) {
                count = rs.getInt(1);
            }
            
            if(count > 0 ) {
                isPersonaSettingExists = true;
            }
            logger.debug("isPersonaSettingExists : "+isPersonaSettingExists);
            
        } catch (Exception e) {
            logger.error("Exception in isPersonaSettingExists", e);
        } finally {
            postgre.releaseConnection(connection, prepareStatement, rs);
        }
        return isPersonaSettingExists;
    }
    
}
