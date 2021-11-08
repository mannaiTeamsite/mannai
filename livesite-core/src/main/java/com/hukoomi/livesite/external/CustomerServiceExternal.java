package com.hukoomi.livesite.external;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.ValidationErrorList;

import com.hukoomi.utils.CommonUtils;
import com.hukoomi.utils.ESAPIValidator;
import com.hukoomi.utils.Postgre;
import com.hukoomi.utils.PropertiesFileReader;
import com.interwoven.livesite.runtime.RequestContext;

public class CustomerServiceExternal {

    /** Logger object to check the flow of the code. */
    private static final Logger LOGGER =
            Logger.getLogger(CustomerServiceExternal.class);
    /** external service action. */
    private static final String ACTION_ESERVICE = "eservice";
    /** external service action. */
    private static final String ACTION_SERVICE = "service";
    /** element for xml document. */
    private static final String ELEMENT_RESULT = "Result";
    /** element for xml document. */
    private static final String ELEMENT_OPTION = "Option";
    /** field validation status. */
    private static final String STATUS_FIELD_VALIDATION =
            "FieldValidationFailed";
    /** element for xml document. */
    private static final String ELEMENT_STATUS = "status";

    /**
     * This method internally makes call to get eservices/ services.
     *
     * @param context
     * @return result
     */
    public Document getServices(RequestContext context) {
        String action = "";
        String eService = "";
        Document result = null;
        LOGGER.info("CustomerService");
        action = context.getParameterString("action");
        if (validateAction(action)) {
            if (action.equals(ACTION_ESERVICE)) {
                result = getEServices(context);
            } else if (action.equals(ACTION_SERVICE)) {
                eService = context.getParameterString("eService");
                if (validateEService(eService)) {
                    result = getServices(eService, context);
                } else {
                     result = getFieldValidationFailedDocument(result);
                }
            }
            else {
                result = getFieldValidationFailedDocument(result);
           }
        }
        else {
            result = getFieldValidationFailedDocument(result);
        }
        return result;
    }

    private Document getFieldValidationFailedDocument( Document result) {
        result = DocumentHelper.createDocument();
        Element resultElement =
                result.addElement(ELEMENT_RESULT);
        Element element =
                resultElement.addElement(ELEMENT_STATUS);
        element.setText(STATUS_FIELD_VALIDATION);
        return result;
    }

    private boolean validateAction(String action) {
        ValidationErrorList errorList = new ValidationErrorList();
        LOGGER.info("CUSTOMER_SERVICE_ACTION" + " >>>" + action + "<<<");
        ESAPI.validator().getValidInput("CUSTOMER_SERVICE_ACTION", action,
                ESAPIValidator.ALPHABET, 20, false, true, errorList);
        return errorList.isEmpty();
    }

    private boolean validateEService(String eServiceID) {
        ValidationErrorList errorList = new ValidationErrorList();
        LOGGER.info("E_SERVICE" + " >>>" + eServiceID + "<<<");
        ESAPI.validator().getValidInput("E_SERVICE", eServiceID,
                ESAPIValidator.NUMERIC, 8, false, true, errorList);
        return errorList.isEmpty();
    }
    /**
     * This method will be used to get the services from db.
     *
     * @param context
     * @return document
     */
    private Document getServices(String eService, RequestContext context) {
        LOGGER.info("getServices:Entering");
        Postgre postgre = new Postgre(context);
        CommonUtils util = new CommonUtils();
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        StringBuilder builder = new StringBuilder();
        Connection connection = postgre.getConnection();
        Document document = DocumentHelper.createDocument();
        Element resultElement = document.addElement(ELEMENT_RESULT);
        Properties properties =
                CustomerServiceExternal.loadProperties(context);
        String blockService = properties.getProperty("blockService");
        builder.append(
                "SELECT S.ESERVICEID as ESERVICEID,S.SERVICEID as SERVICEID,S.SERVICEEDESC as SERVICEEDESC,"
                + "S.SERVICEADESC as SERVICEADESC ");
        builder.append("FROM SERVICES S ");
        builder.append("WHERE S.ESERVICEID=");
        builder.append(eService);
        builder.append(" AND S.SERVICEID NOT IN ( ");
        builder.append(blockService);
        builder.append(" ) ORDER BY S.SERVICEID ");
        try {
            LOGGER.info("getServices:After builder");
            if (null != connection) {
                preparedStatement =
                        connection.prepareStatement(builder.toString());
                resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    Element optionElement =
                            resultElement.addElement(ELEMENT_OPTION);
                    Element serviceId =
                            optionElement.addElement("ServiceId");
                    serviceId.setText(
                            String.valueOf(resultSet.getInt("SERVICEID")));
                    Element serviceEDecs =
                            optionElement.addElement("ServiceEDecs");
                    serviceEDecs
                            .setText(resultSet.getString("SERVICEEDESC"));
                    Element serviceADecs =
                            optionElement.addElement("ServiceADecs");
                    serviceADecs.setText(util.decodeToArabicString(
                            resultSet.getString("SERVICEADESC")));
                }
            }
            LOGGER.debug("getServices:Result" + document.toString());
        } catch (SQLException e) {
            LOGGER.error("Exception in getServices: ", e);
        } finally {
            postgre.releaseConnection(connection, preparedStatement,
                    resultSet);
        }

        return document;
    }

    /**
     * This method will be used to get the eservices from db.
     *
     * @param context
     * @return document
     */
    private Document getEServices(RequestContext context) {
        LOGGER.debug("getEServices:Begin");
        Postgre postgre = new Postgre(context);
        CommonUtils util = new CommonUtils();
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        Connection connection = postgre.getConnection();
        StringBuilder builder = new StringBuilder();
        List<Map<String, String>> resultList = null;
        Document document = DocumentHelper.createDocument();
        Element resultElement = document.addElement(ELEMENT_RESULT);
        builder.append(
                "SELECT E.ESERVICEID, E.ESERVICEADESC, E.ESERVICEEDESC, E.MINISTRYID ");
        builder.append("FROM ESERVICES E, INCIDENT_ESERVICE_MAPPING IEM ");
        builder.append(
                "WHERE E.ESERVICEID=IEM.ESERVICEID AND E.ESERVICEADESC!='' AND E.ESERVICEEDESC!='' AND ");
        builder.append("E.ESERVICEADESC IS NOT NULL and E.ESERVICEEDESC IS NOT NULL ORDER BY E.ESERVICEID ");
        try {
            LOGGER.info("getServices:After builder");
            if (null != connection) {
                preparedStatement =
                        connection.prepareStatement(builder.toString());
                resultSet = preparedStatement.executeQuery();

                while (resultSet.next()) {
                    Element optionElement =
                            resultElement.addElement(ELEMENT_OPTION);
                    Element eserviceId =
                            optionElement.addElement("EServiceId");
                    eserviceId.setText(String
                            .valueOf(resultSet.getInt("eserviceid")));
                    Element eserviceEDecs =
                            optionElement.addElement("EServiceEDecs");
                    eserviceEDecs
                            .setText(resultSet.getString("eserviceedesc"));
                    Element eserviceADecs =
                            optionElement.addElement("EServiceADecs");
                    eserviceADecs.setText(util.decodeToArabicString(
                            resultSet.getString("eserviceadesc")));

                }
            }
            LOGGER.debug("result set" + resultList);
        } catch (SQLException e) {
            LOGGER.error("Exception in getEServices: ", e);
        } finally {
            postgre.releaseConnection(connection, preparedStatement,
                    resultSet);
        }
        return document;
    }

    /**
     * This method will be used to load the configuration properties.
     *
     * @param context The parameter context object passed from Component.
     * @return properties
     */
    private static Properties
            loadProperties(final RequestContext context) {
        LOGGER.info("loadProperties:Begin");
        PropertiesFileReader propertyFileReader = new PropertiesFileReader(
                context, "customerserviceconfig.properties");
        return propertyFileReader.getPropertiesFile();
    }

}
