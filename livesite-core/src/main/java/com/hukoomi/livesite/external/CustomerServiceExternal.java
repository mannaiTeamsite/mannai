package com.hukoomi.livesite.external;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import com.hukoomi.utils.Constants;
import com.hukoomi.utils.Postgre;
import com.hukoomi.utils.VerifyRecaptcha;
import com.interwoven.livesite.runtime.RequestContext;
import com.hukoomi.utils.VerifyRecaptcha;

public class CustomerServiceExternal {

	/** Logger object to check the flow of the code. */
	private static final Logger logger = Logger.getLogger(CustomerServiceExternal.class);
	private static final String ACTION_ESERVICE = "eservice";
	private static final String ACTION_SERVICE = "service";
	private static final String ACTION_SUBMIT_TICKET = "submitticket";
	private static final String ACTION_QUERY_TICKET = "queryticket";
	private static final String ACTION_COMMENT = "comment";
	private static final String ACTION_ID_TYPE = "idtype";
	
	String action = "";
	String eService = "";
	String language = "";
	String captcha = "";
	public Document getServices(RequestContext context) throws DocumentException, IOException {

		Document Result = null;
		logger.info("ReportIncident");
		action = context.getParameterString("Action");
		eService = context.getParameterString("eService");
		language = context.getParameterString("language");
		if (!action.equals("")) {
			switch (action) {
			case ACTION_ESERVICE:
				Result = getEServices();
				break;
			case ACTION_SERVICE:
				Result = getServices();
				break;
			case ACTION_SUBMIT_TICKET:
				captcha = context.getParameterString("captcha");
				Result = submitTicket();
				break;
			case ACTION_QUERY_TICKET:
				Result = queryTicket();
				break;
			case ACTION_COMMENT:
				Result = getComment();
				break;
			case ACTION_ID_TYPE:
				Result = getIdType();
				break;
			default:
				break;
			}
		}
		return Result;
	}

	private Document getIdType() throws UnsupportedEncodingException {
		logger.debug("IncidentReport:getIdType:Entering");
		Locale lang = getLocale();
		logger.debug("IncidentReport:getComment:" + lang);
		ResourceBundle bundle = ResourceBundle.getBundle("com.hukoomi.livesite.controller.ReportIncidentPortletResource", lang);
		String validationMessage = bundle.getString("idType");
		Document document = DocumentHelper.createDocument();
		Element resultElement = document.addElement("Result");
		
		logger.debug("IncidentReport:getComment:" + validationMessage);
		String[] arrOfStr = validationMessage.split(",", 2);
		for(int i=0;i<arrOfStr.length;i++) {
			String value = arrOfStr[i].split("!")[0];
			String text = arrOfStr[i].split("!")[1];
			Element optionElement = resultElement.addElement("Option");
			Element valueele = optionElement.addElement("value");
			valueele.setText(value);
			Element textele = optionElement.addElement("text");
			if(language.equals("ar")) {
				textele.setText(decodeToArabicString(text));
			}
			else {
				textele.setText(text);
			}
			
		}
		return document;
	}
	
	public String decodeToArabicString(String str) throws UnsupportedEncodingException{
	      
	      byte[] charset = str.getBytes("UTF-8");
	      String result = new String(charset, "UTF-8");
	      return result;
	   }
	private Document getComment() throws DocumentException, UnsupportedEncodingException {
		logger.debug("IncidentReport:getComment:Entering");
		Locale lang = getLocale();
		logger.debug("IncidentReport:getComment:" + lang);
		ResourceBundle bundle = ResourceBundle
				.getBundle("com.hukoomi.livesite.controller.ReportIncidentPortletResource", lang);
		String validationMessage = bundle.getString(eService);
		if(language.equals("ar")) {
			validationMessage = decodeToArabicString(validationMessage);
		}
		logger.debug("IncidentReport:getComment:" + validationMessage);
		validationMessage = "<comments>" + validationMessage + "</comments>";
		return DocumentHelper.parseText(validationMessage);
	}

	private Locale getLocale() {
		switch (language) {
		case "en":
			return Locale.ENGLISH;
		case "ar":
			return new Locale("ar");
		default:
			return Locale.ENGLISH;
		}
	}

	private Document queryTicket() {
		return null;
	}

	private Document submitTicket() throws IOException, DocumentException {
		logger.debug("submitTicket:Entering");
		VerifyRecaptcha verify = new VerifyRecaptcha();
	
		return DocumentHelper.parseText( "<recaptcha>" + verify.verify(captcha) + "</recaptcha>");
	}

	private Document getServices() throws UnsupportedEncodingException {
		logger.debug("IncidentReportDAO:getServices:Entering");
		Connection connection = null;
		ResultSet resultSet = null;
		Statement statement = null;
		PreparedStatement preparedStatement = null;
		StringBuilder builder = new StringBuilder();

		Document document = DocumentHelper.createDocument();
		Element resultElement = document.addElement("Result");

		builder.append(
				"SELECT S.\"ESERVICEID\" as ESERVICEID,S.\"SERVICEID\" as SERVICEID,S.\"SERVICEEDESC\" as SERVICEEDESC,S.\"SERVICEADESC\" as SERVICEADESC ");
		builder.append("FROM public.\"SERVICES\" S ");
		builder.append("WHERE S.\"ESERVICEID\"=");
		builder.append(eService);
		builder.append(" AND S.\"SERVICEID\" NOT IN ( ");
		builder.append(Constants.BLOCKED_SERVICES);
		builder.append(") ORDER BY ESERVICEID ");
		logger.debug("IncidentReportDAO:getServices:" + builder.toString());
		try {
			connection = Postgre.getConnection();
			if (null != connection) {
				preparedStatement = connection.prepareStatement(builder.toString());
				if (preparedStatement != null) {
					resultSet = preparedStatement.executeQuery();
					while (resultSet.next()) {

						Element optionElement = resultElement.addElement("Option");
						Element serviceId = optionElement.addElement("ServiceId");
						serviceId.setText(String.valueOf(resultSet.getInt("SERVICEID")));
						Element serviceEDecs = optionElement.addElement("ServiceEDecs");
						serviceEDecs.setText(resultSet.getString("SERVICEEDESC"));
						Element serviceADecs = optionElement.addElement("ServiceADecs");
						serviceADecs.setText(decodeToArabicString(resultSet.getString("SERVICEADESC")));
					}
				}
			}
			logger.debug("IncidentReportDAO:getServices:result set");
			logger.debug("IncidentReportDAO:getServices:Entering" + document);
		} catch (SQLException e) {
			logger.error((Object) ("getConfiguration()" + e.getMessage()));
			e.printStackTrace();
			return document;
		} finally {
			Postgre.releaseConnection(connection, statement, resultSet);
		}
		Postgre.releaseConnection(connection, statement, resultSet);
		return document;
	}

	private Document getEServices() throws UnsupportedEncodingException {
		logger.debug("IncidentReportDAO:geteServices:Begin");
		Connection connection = null;
		ResultSet resultSet = null;
		Statement statement = null;
		PreparedStatement preparedStatement = null;
		StringBuilder builder = new StringBuilder();
		List<Map<String, String>> resultList = null;
		Document document = DocumentHelper.createDocument();
		Element resultElement = document.addElement("Result");

		builder.append("SELECT E.\"ESERVICEID\", E.\"ESERVICEADESC\", E.\"ESERVICEEDESC\", E.\"MINISTRYID\" ");
		builder.append("FROM public.\"ESERVICES\" E, public.\"INCIDENT_ESERVICE_MAPPING\" IEM ");
		builder.append(
				"WHERE E.\"ESERVICEID\"= IEM.\"ESERVICEID\" AND E.\"ESERVICEADESC\"!='' AND E.\"ESERVICEEDESC\"!='' AND E.\"ESERVICEADESC\" IS NOT NULL and E.\"ESERVICEEDESC\" IS NOT NULL ");
		builder.append("ORDER BY E.\"ESERVICEID\" ");

		try {
			logger.debug("IncidentReportDAO:geteServices:After builder");
			connection = Postgre.getConnection();
			if (null != connection) {
				preparedStatement = connection.prepareStatement(builder.toString());
				if (preparedStatement != null) {
					resultSet = preparedStatement.executeQuery();
					while (resultSet.next()) {
						Element optionElement = resultElement.addElement("Option");
						Element eserviceId = optionElement.addElement("EServiceId");
						eserviceId.setText(String.valueOf(resultSet.getInt("ESERVICEID")));
						Element eserviceEDecs = optionElement.addElement("EServiceEDecs");
						eserviceEDecs.setText(resultSet.getString("ESERVICEEDESC"));
						Element eserviceADecs = optionElement.addElement("EServiceADecs");
						eserviceADecs.setText(decodeToArabicString(resultSet.getString("ESERVICEADESC")));
					}
				}
			}
			logger.debug("IncidentReportDAO:geteServices:result set");
			logger.debug("IncidentReportDAO:geteServices:Entering" + resultList);
		} catch (SQLException e) {
			logger.error((Object) ("getConfiguration()" + e.getMessage()));
			e.printStackTrace();
			return document;
		} finally {
			Postgre.releaseConnection(connection, statement, resultSet);
		}
		Postgre.releaseConnection(connection, statement, resultSet);
		return document;
	}

}
