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
import java.nio.charset.StandardCharsets;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import com.hukoomi.utils.Constants;
import com.hukoomi.utils.Postgre;
import com.hukoomi.utils.VerifyRecaptcha;
import com.ibm.faces.component.html.HtmlFileupload;
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
	private static Connection connection;
	static {
		CustomerServiceExternal.connection = null;
	}
	
	
	@SuppressWarnings("deprecation")
	public Document getServices(RequestContext context) throws DocumentException, IOException {
		String action = "";
		String eService = "";
		String language = "";
		String captcha = "";
		String ticketNumber = "";
		HtmlFileupload file=null;
		Document result = null;
		logger.info("ReportIncident");
		action = context.getParameterString("Action");		
		if (!action.equals("")) {
			switch (action) {
			case ACTION_ESERVICE:
				result = getEServices();
				break;
			case ACTION_SERVICE:
				eService = context.getParameterString("eService");
				result = getServices(eService);
				break;
			case ACTION_SUBMIT_TICKET:
				captcha = context.getParameterString("captcha");
				file = (HtmlFileupload) context.getParameters().get("file");
				result = submitTicket(captcha,file);
				break;
			case ACTION_QUERY_TICKET:
				ticketNumber = context.getParameterString("ticketNumber");
				result = queryTicket(ticketNumber);
				break;
			case ACTION_COMMENT:
				eService = context.getParameterString("eService");
				language = context.getParameterString("language");
				result = getComment(eService,language);
				break;
			case ACTION_ID_TYPE:
				language = context.getParameterString("language");
				result = getIdType(language);
				break;
			default:
				break;
			}
		}
		return result;
	}

	private Document getIdType(String language) throws UnsupportedEncodingException {
		logger.debug("IncidentReport:getIdType:Entering");
		Locale lang = getLocale(language);
		ResourceBundle bundle = ResourceBundle.getBundle("com.hukoomi.utils.ReportIncidentPortletResource", lang);
		String validationMessage = bundle.getString("idType");
		Document document = DocumentHelper.createDocument();
		Element resultElement = document.addElement("Result");
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
	
	public String decodeToArabicString(String str) {
	      
	      byte[] charset = str.getBytes(StandardCharsets.UTF_8);
	      return new String(charset, StandardCharsets.UTF_8);
	   }
	private Document getComment(String eService,String language) throws DocumentException {
		logger.debug("IncidentReport:getComment:Entering");
		Locale lang = getLocale(language);
		logger.debug("IncidentReport:getComment:" + lang);
		ResourceBundle bundle = ResourceBundle
				.getBundle("com.hukoomi.utils.ReportIncidentPortletResource", lang);
		String comments = bundle.getString(eService);
		if(!comments.equals("") && !comments.equals(null) ) {
		if(language.equals("ar")) {
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

	private Locale getLocale(String language) {
		switch (language) {
		case "en":
			return Locale.ENGLISH;
		case "ar":
			return new Locale("ar");
		default:
			return Locale.ENGLISH;
		}
	}

	private Document queryTicket(String ticketNumber) {
		Document document = DocumentHelper.createDocument();
		Element resultElement = document.addElement("Result");
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

	private Document submitTicket( String captcha,HtmlFileupload file) throws IOException, DocumentException {
		logger.debug("submitTicket:Entering");	
		logger.debug("submitTicket:file:"+file.getValue());	
		logger.debug("submitTicket:file:"+file.getSize());	
		logger.debug("submitTicket:file:"+file.getFilename());	
			
		return DocumentHelper.parseText( "<recaptcha>" + VerifyRecaptcha.verify(captcha) + "</recaptcha>");
	}

	private Document getServices(String eService) throws IOException {
		logger.debug("IncidentReportDAO:getServices:Entering");
		ResultSet resultSet = null;
		Statement statement = null;
		PreparedStatement preparedStatement = null;
		StringBuilder builder = new StringBuilder();
		connection = Postgre.getConnection();
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
			
			if (null != connection) {
				preparedStatement = connection.prepareStatement(builder.toString());
				
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
					 OutputFormat format = OutputFormat.createPrettyPrint();
			         XMLWriter writer;
			         writer = new XMLWriter( System.out, format );
			         writer.write( document );
			}
			logger.debug("IncidentReportDAO:getServices:result set");
			logger.debug("IncidentReportDAO:getServices:document" + document.toString());
		} catch (SQLException e) {
			logger.error((Object) ("getConfiguration()" + e.getMessage()));
			Postgre.releaseConnection(connection, statement, resultSet);
			e.printStackTrace();
			return document;
		} finally {
			Postgre.releaseConnection(connection, statement, resultSet);
		}
		Postgre.releaseConnection(connection, statement, resultSet);
		return document;
	}

	private Document getEServices() {
		logger.debug("IncidentReportDAO:geteServices:Begin");
		ResultSet resultSet = null;
		Statement statement = null;
		CustomerServiceExternal.connection = Postgre.getConnection();
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
			
			if (null != CustomerServiceExternal.connection) {
				preparedStatement = CustomerServiceExternal.connection.prepareStatement(builder.toString());
				
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
			logger.debug("IncidentReportDAO:geteServices:result set");
			logger.debug("IncidentReportDAO:geteServices:Entering" + resultList);
		} catch (SQLException e) {
			logger.error((Object) ("getConfiguration()" + e.getMessage()));
			e.printStackTrace();
			return document;
		} finally {
			Postgre.releaseConnection(CustomerServiceExternal.connection, statement, resultSet);
		}
		Postgre.releaseConnection(CustomerServiceExternal.connection, statement, resultSet);
		return document;
	}

}
