package com.hukoomi.livesite.external;

import java.nio.charset.StandardCharsets;
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

import com.hukoomi.utils.CommonUtils;
import com.hukoomi.utils.Constants;
import com.hukoomi.utils.Postgre;
import com.interwoven.livesite.runtime.RequestContext;

public class CustomerServiceExternal {

	/** Logger object to check the flow of the code. */
	private static final Logger logger = Logger.getLogger(CustomerServiceExternal.class);
	private static final String ACTION_ESERVICE = "eservice";
	private static final String ACTION_SERVICE = "service";
	private static final String ACTION_SUBMIT_TICKET = "submitticket";
	private static final String ACTION_QUERY_TICKET = "queryticket";
	private static final String ACTION_COMMENT = "comment";
	private static final String ACTION_ID_TYPE = "idtype";
	private static final String ELEMENT_RESULT = "Result";
	private static final String ELEMENT_OPTION = "Option";
	String language ;
	private static Connection connection;
	static {
		CustomerServiceExternal.connection = null;
	}
	
	
	@SuppressWarnings("deprecation")
	public Document getServices(RequestContext context) throws DocumentException {
		String action = "";
		String eService = "";
		String captcha = "";
		String ticketNumber = "";
		Document result = null;
		CommonUtils util= new CommonUtils();
		logger.info("ReportIncident");
		action = context.getParameterString("action");		
		if (!action.equals("")) {
			
			if(action.equals(ACTION_ESERVICE)) {
				result = getEServices(context);
			} else if(action.equals(ACTION_SERVICE)) {
				eService = context.getParameterString("eService");
				result = getServices(eService,context);
			} else if(action.equals(ACTION_COMMENT)) {
			
				eService = context.getParameterString("eService");
				language = context.getParameterString("locale");
				Locale locale = util.getLocale(language);
				result = getComment(eService,locale);
			}
			else if(action.equals(ACTION_ID_TYPE)) {
				language = context.getParameterString("locale");
				Locale locale = util.getLocale(language);
				result = getIdType(locale);
			}
			
		}
		return result;
	}

	private Document getIdType(Locale lang) {
		logger.debug("IncidentReport:getIdType:Entering");
		ResourceBundle bundle = ResourceBundle.getBundle("com.hukoomi.resources.CustomerServiceResource", lang);
		String validationMessage = bundle.getString("idType");
		Document document = DocumentHelper.createDocument();
		Element resultElement = document.addElement(ELEMENT_RESULT);
		String[] arrOfStr = validationMessage.split(",", 2);
		for(int i=0;i<arrOfStr.length;i++) {
			String value = arrOfStr[i].split("!")[0];
			String text = arrOfStr[i].split("!")[1];
			Element optionElement = resultElement.addElement(ELEMENT_OPTION);
			Element valueele = optionElement.addElement("value");
			valueele.setText(value);
			Element textele = optionElement.addElement("text");
			if(lang.toString().equals("ar")) {
				textele.setText(decodeToArabicString(text));
			}
			else {
				textele.setText(text);
			}
			
		}
		return document;
	}
	
	public static String decodeToArabicString(String str) {
	      
	      byte[] charset = str.getBytes(StandardCharsets.UTF_8);
	      return new String(charset, StandardCharsets.UTF_8);
	   }
	private Document getComment(String eService,Locale lang) throws DocumentException {
		logger.debug("IncidentReport:getComment:Entering");
		logger.debug("IncidentReport:getComment:" + lang);
		ResourceBundle bundle = ResourceBundle
				.getBundle("com.hukoomi.resources.CustomerServiceResource", lang);
		String comments = null;
		comments = bundle.getString(eService);
		
		if(comments !=null ) {
		if(lang.toString().equals("ar")) {
			comments = decodeToArabicString(comments);
		}
		logger.debug("IncidentReport:getComment:" + comments);
		comments = "<comments>" + comments + "</comments>";
		return DocumentHelper.parseText(comments);
	}
		else {
			return null;
		}
		
		
	}

	

	/*private Document queryTicket(String ticketNumber) {
		Document document = DocumentHelper.createDocument();
		Element resultElement = document.addElement(ELEMENT_RESULT);
		Element eidElement = resultElement.addElement("EID");
		eidElement.setText("0");
		Element qidElement = resultElement.addElement("QID");
		qidElement.setText("12312312311");
		Element serviceElement = resultElement.addElement("Service");
		serviceElement.setText("Resident Permits");
		Element createdOnElement = resultElement.addElement("CreatedOn");
		createdOnElement.setText("14-Oct-2020 10:01:43");
		Element statusElement = resultElement.addElement("TicketStatus");
		statusElement.setText("Open");
		
		return document;
	}

	private Document submitTicket( String captcha) throws IOException, DocumentException {
		logger.debug("submitTicket:Entering");		
			
		return DocumentHelper.parseText( "<recaptcha>" + VerifyRecaptcha.verify(captcha) + "</recaptcha>");
	}*/

	private Document getServices(String eService,RequestContext context) {
		logger.debug("IncidentReportDAO:getServices:Entering");
		Postgre postgre =  new Postgre(context);
		ResultSet resultSet = null;
		Statement statement = null;
		PreparedStatement preparedStatement = null;
		StringBuilder builder = new StringBuilder();
		connection = postgre.getConnection();
		Document document = DocumentHelper.createDocument();
		Element resultElement = document.addElement(ELEMENT_RESULT);
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
			
			if (null != connection) {
				preparedStatement = connection.prepareStatement(builder.toString());
				
					resultSet = preparedStatement.executeQuery();
					while (resultSet.next()) {

						Element optionElement = resultElement.addElement(ELEMENT_OPTION);
						Element serviceId = optionElement.addElement("ServiceId");
						serviceId.setText(String.valueOf(resultSet.getInt("SERVICEID")));
						Element serviceEDecs = optionElement.addElement("ServiceEDecs");
						serviceEDecs.setText(resultSet.getString("SERVICEEDESC"));
						Element serviceADecs = optionElement.addElement("ServiceADecs");
						serviceADecs.setText(decodeToArabicString(resultSet.getString("SERVICEADESC")));
					}
					 
			}
			logger.debug("IncidentReportDAO:getServices:result set");
			logger.debug("IncidentReportDAO:getServices:document" + document.toString());
		} catch (SQLException e) {
			logger.error((Object) ("getConfiguration()" + e.getMessage()));
			
			e.printStackTrace();
			return document;
		}  finally { postgre.releaseConnection(connection, statement, resultSet); }
			 
		
		return document;
	}

	private  Document getEServices(RequestContext context) {
		Postgre postgre =  new Postgre(context);
		logger.debug("IncidentReportDAO:geteServices:Begin");
		ResultSet resultSet = null;
		Statement statement = null;
		CustomerServiceExternal.connection = postgre.getConnection();
		PreparedStatement preparedStatement = null;
		StringBuilder builder = new StringBuilder();
		List<Map<String, String>> resultList = null;
		Document document = DocumentHelper.createDocument();
		Element resultElement = document.addElement(ELEMENT_RESULT);
		
		builder.append("SELECT E.\"ESERVICEID\", E.\"ESERVICEADESC\", E.\"ESERVICEEDESC\", E.\"MINISTRYID\" ");
		builder.append("FROM public.\"ESERVICES\" E, public.\"INCIDENT_ESERVICE_MAPPING\" IEM ");
		builder.append(
				"WHERE E.\"ESERVICEID\"= IEM.\"ESERVICEID\" AND E.\"ESERVICEADESC\"!='' AND E.\"ESERVICEEDESC\"!='' AND E.\"ESERVICEADESC\" IS NOT NULL and E.\"ESERVICEEDESC\" IS NOT NULL ");
		builder.append("ORDER BY E.\"ESERVICEID\" ");

		try {
			logger.debug("IncidentReportDAO:geteServices:After builder");
			
			if (null != CustomerServiceExternal.connection) {
				preparedStatement = CustomerServiceExternal.connection.prepareStatement(builder.toString());
				
					resultSet = preparedStatement.executeQuery();
					while (resultSet.next()) {
						Element optionElement = resultElement.addElement(ELEMENT_OPTION);
						Element eserviceId = optionElement.addElement("EServiceId");
						eserviceId.setText(String.valueOf(resultSet.getInt("ESERVICEID")));
						Element eserviceEDecs = optionElement.addElement("EServiceEDecs");
						eserviceEDecs.setText(resultSet.getString("ESERVICEEDESC"));
						Element eserviceADecs = optionElement.addElement("EServiceADecs");
						eserviceADecs.setText(decodeToArabicString(resultSet.getString("ESERVICEADESC")));
					
				}
			}
			logger.debug("IncidentReportDAO:geteServices:result set");
			logger.debug("IncidentReportDAO:geteServices:Entering" + resultList);
		} catch (SQLException e) {
			logger.error((Object) ("getConfiguration()" + e.getMessage()));
			e.printStackTrace();
			return document;
		} finally {
			postgre.releaseConnection(CustomerServiceExternal.connection, statement, resultSet);
		}
		return document;
	}

}
