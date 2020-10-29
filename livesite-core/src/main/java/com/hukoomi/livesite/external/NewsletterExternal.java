package com.hukoomi.livesite.external;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.json.JSONException;
import org.json.JSONObject;

import com.hukoomi.utils.Postgre;
import com.interwoven.livesite.runtime.RequestContext;

public class NewsletterExternal {
	/** Logger object to check the flow of the code. */
	private static final Logger logger = Logger.getLogger(NewsletterExternal.class);
	private String listId;
	private String authorizationHeader;
	private String baseUrl;
	private static Connection connection;
	private HttpURLConnection httpConnection = null;
	private static final String STATUS_SUBSCRIBED = "subscribed";
	private static final String STATUS_UNSUBSCRIBED = "unsubscribed";
	private static final String STATUS_PENDING = "pending";
	private static final String STATUS_NOTFOUND = "404";
	private static final String KEY_STATUS = "status";
	private static final String KEY_EMAIL = "email_address";
	static {
		NewsletterExternal.connection = null;
	}

	/**
	 * @param configCode
	 * @return
	 */
	public static String getConfiguration(String configCode) {
		logger.debug("in getConfiguration:" + configCode);
		Statement st = null;
		ResultSet rs = null;
		String configParamValue = "";
		final String GET_OPTION_ID = "SELECT CONFIG_PARAM_VALUE FROM CONFIG_PARAM WHERE CONFIG_PARAM_CODE = '"
				+ configCode + "'";
		try {
			NewsletterExternal.connection = Postgre.getConnection();
			st = NewsletterExternal.connection.createStatement();
			rs = st.executeQuery(GET_OPTION_ID);
			while (rs.next()) {
				configParamValue = rs.getString("config_param_value");
				logger.debug("configParamValue:" + configParamValue);
			}

		} catch (SQLException e) {
			logger.error((Object) ("getConfiguration()" + e.getMessage()));
			e.printStackTrace();
			return configParamValue;
		} finally {
			Postgre.releaseConnection(NewsletterExternal.connection, st, rs);
		}
		Postgre.releaseConnection(NewsletterExternal.connection, st, rs);
		return configParamValue;
	}

	/**
	 * @param email
	 * @param lang
	 * @param status
	 * This method is used to update the subscriber details in backend (DB)
	 */
	public static void insertSubscriber(String email, String lang, String status) {
		logger.debug("NewsletterExternal : insertSubscriber");
		PreparedStatement prepareStatement = null;
		String insertQuery = "";
		if (isscubscriberExist(email) > 0) {
			logger.debug("NewsletterExternal : update");
			insertQuery = "UPDATE NEWSLETTER_SUBSCRIBERS SET LANGUAGE = ?,STATUS = ? WHERE EMAIL = ?";

		} else {
			logger.debug("NewsletterExternal : insert");
			insertQuery = "INSERT INTO NEWSLETTER_SUBSCRIBERS (LANGUAGE,STATUS,EMAIL) VALUES(?,?,?)";

		}
		try {
			NewsletterExternal.connection = Postgre.getConnection();
			prepareStatement = NewsletterExternal.connection.prepareStatement(insertQuery);

			prepareStatement.setString(1, lang);
			prepareStatement.setString(2, status);
			prepareStatement.setString(3, email);
			final int result = prepareStatement.executeUpdate();
			if (result == 0) {
				logger.error(" subscription failed !");
			} else {
				logger.debug(" subscriber!");
			}
		} catch (SQLException e) {
			String errorMsg = "SQLException :";
			if (e.getMessage() != null) {
				errorMsg = String.valueOf(errorMsg) + e.getMessage();
			}
			logger.error((Object) errorMsg);

		} finally {
			Postgre.releaseConnection(NewsletterExternal.connection, prepareStatement, null);
		}

		Postgre.releaseConnection(NewsletterExternal.connection, prepareStatement, null);
	}

	/**
	 * @param email
	 * @return
	 * This method is used to check if the subscriber exits in the Mailchimp Tool
	 */
	private static int isscubscriberExist(String email) {
		Statement st = null;
		ResultSet rs = null;

		final String getcount = "SELECT COUNT(*) as total FROM NEWSLETTER_SUBSCRIBERS WHERE EMAIL = '" + email + "'";
		try {
			NewsletterExternal.connection = Postgre.getConnection();
			st = NewsletterExternal.connection.createStatement();
			rs = st.executeQuery(getcount);

			while (rs.next()) {
				logger.error("totalrow: " + rs.getInt("total"));
				return rs.getInt("total");
			}
		} catch (SQLException e) {
			logger.error("isscubscriberExist()" + e.getMessage());
			e.printStackTrace();

		} finally {
			Postgre.releaseConnection(NewsletterExternal.connection, st, rs);
		}
		Postgre.releaseConnection(NewsletterExternal.connection, st, rs);
		return 0;

	}

	/**
	 * @param context
	 * @return
	 * @throws IOException
	 * @throws DocumentException
	 * @throws NoSuchAlgorithmException
	 * @throws JSONException
	 * This method internally makes call to createSubscriberinMailChimp method for user subscription to Mailchimp
	 */
	public Document subscribeToNewsletter(RequestContext context)
			throws IOException, DocumentException, NoSuchAlgorithmException, JSONException {

		Document memberdetail = null;
		logger.debug("Newsletter Subscribtion");
		String email = context.getParameterString("email");
		logger.debug("email:" + email);
		String lang = context.getParameterString("lang");
		logger.debug("lang:" + lang);
		if (!email.equals("") && !lang.equals("")) {
			memberdetail = createSubscriberinMailChimp(email, lang);
		}
		return memberdetail;
	}

	/**
	 * @param email
	 * @param lang
	 * @return
	 * @throws IOException
	 * @throws DocumentException
	 * @throws NoSuchAlgorithmException
	 * @throws JSONException
	 * 
	 * This method is used to make call to mailchimp and check if the users is already subscribed or not
	 */
	private Document createSubscriberinMailChimp(String email, String lang)
			throws IOException, DocumentException, NoSuchAlgorithmException, JSONException {
		// add subcriber
		logger.debug("add subcriber:");
		
		Document document = DocumentHelper.createDocument() ;
		ResourceBundle bundle = ResourceBundle.getBundle("com.hukoomi.resources.Newsletter", getLocaleLanguage(lang));
		String validationMessage ="";
		if (email.equalsIgnoreCase("")) {
			logger.debug("email id not provided ");
		} else {
			listId = getConfiguration("Mailchimp_List_Id");
			logger.debug("listId:" + listId);
			authorizationHeader = "Basic "+getConfiguration("Mailchimp_Authorization_Header");			
			baseUrl = getConfiguration("Mailchimp_BaseURL");
			logger.debug("baseUrl:" + baseUrl);
			try {

				String status="";
				status = isSubscriberexist(email);
				String response ="";
				String responseStr="";				
				String str1 = "<Result><status>";
				String str2 = "</status><email>";
				String str3 = "</email><message>";
				String str4 = "</message></Result>";
				logger.debug("info: " + status);
				if (status != null && !status.equals("")) {
					switch(status) {
					case STATUS_NOTFOUND:
						status = STATUS_SUBSCRIBED;
						response = createsubscriber(email, status, lang);
						break;
					case STATUS_SUBSCRIBED:
						status = "Already Subscribed";
						validationMessage = bundle.getString("subscribed.msg") ;
						if("ar".equals(lang)) {
							
							validationMessage = decodeToArabicString(validationMessage);
						}
						responseStr = str1 + status + str2 + email + str3 +validationMessage+ " : "+ email+str4;
						break;
					case STATUS_PENDING:
						status = "In Pending";
						validationMessage = bundle.getString("pending.msg");	
						if("ar".equals(lang)) {
							
							validationMessage = decodeToArabicString(validationMessage);
						}
						responseStr = str1 + status + str2 + email + str3 +validationMessage+ " : "+ email+str4;
						break;
					case STATUS_UNSUBSCRIBED:
						status = STATUS_PENDING;
						response = createsubscriber(email, status, lang);
						break;
					default:
						logger.debug("Switch Case: " + "default");
					break;
					}
					if(response != null && !response.equals("")) {
					 responseStr = getDocument(email,status,response,lang);
					}
					logger.debug("after get document responseStr: " + responseStr);					
					document = DocumentHelper.parseText(responseStr);
				}
				
			} catch (IOException e) {
				logger.error("error:" + e);
				e.printStackTrace();
			}
		}

		return document;
	}

	/**
	 * @param email
	 * @param status
	 * @param response
	 * @return
	 * @throws JSONException
	 */
	private String  getDocument(String email,String status, String response,String lang) throws JSONException {
		String responseStr ="" ; 
		String str1 = "<Result><status>";
		String str2 = "</status><email>";
		String str3 = "</email><message>";
		String str5 = "</message></Result>";
		String str4 = "xmlstring ";
		
		ResourceBundle bundle = ResourceBundle.getBundle("com.hukoomi.resources.Newsletter", getLocaleLanguage(lang));
		String validationMessage ="";
		String pendingMessage ="";
		String pendingMessage1 ="";
		
		if (!response.equals("")) {
			JSONObject jsonObj = new JSONObject(response);
			status = (String) jsonObj.get(KEY_STATUS);
			email = (String) jsonObj.get(KEY_EMAIL);
		}
		logger.debug(status);
		if (status.equals(STATUS_SUBSCRIBED)  ) {	
			
			validationMessage = bundle.getString("success.msg");
			if("ar".equals(lang)) {
				
				validationMessage = decodeToArabicString(validationMessage);
			}
			responseStr = str1 + status + str2 + email + str3 +validationMessage+ " : " +email+str5;
		
		} else if (status.equals(STATUS_PENDING)){
			pendingMessage = bundle.getString("unsubscribed.msg");
			pendingMessage1 = bundle.getString("unsubscribed.msg");
			if("ar".equals(lang)) {
				
				pendingMessage = decodeToArabicString(pendingMessage);
				pendingMessage1 = decodeToArabicString(pendingMessage1);
			}
			validationMessage = pendingMessage +" : " +email+ " " +pendingMessage1;
			responseStr = str1 + status + str2 + email + str3 +validationMessage+str5;
		} else {
			responseStr = str1 + status + str2 + email + str3;			
			
		}
		return responseStr;
	}

	/**
	 * @param email
	 * @param status
	 * @param lang
	 * @return
	 * @throws NoSuchAlgorithmException
	 * 	 
	 */
	private String createsubscriber(String email, String status, String lang)
			throws NoSuchAlgorithmException {

		
		InputStream is = null;

		try {
			// Create connection
			httpConnection = getConnection(email);
			httpConnection.setRequestMethod("PUT");
			httpConnection.setRequestProperty("Content-Type", "application/json");
			httpConnection.setRequestProperty("Authorization", authorizationHeader);
			String requestJSON = "{ \"email_address\" : \"" + email + "\", \"email_type\": \"html\",\"status\" : \""
					+ status + "\",\"language\":\"" + lang + "\" }";
			httpConnection.setRequestProperty("Content-Length", Integer.toString(requestJSON.getBytes().length));
			httpConnection.setRequestProperty("Content-Language", "en-US");
			httpConnection.setUseCaches(false);
			httpConnection.setDoOutput(true);

			// Send request
			logger.debug(requestJSON);
			DataOutputStream wr = new DataOutputStream(httpConnection.getOutputStream());
			wr.writeBytes(requestJSON);
			wr.close();

			// Get Response

			is = httpConnection.getInputStream();

			BufferedReader rd = new BufferedReader(new InputStreamReader(is));

			StringBuilder response = new StringBuilder(); // or StringBuffer if Java version 5+
			String line;
			while ((line = rd.readLine()) != null) {
				response.append(line);
				response.append('\r');
			}
			rd.close();
			return response.toString();
		} catch (IOException ioe) {
				int statusCode;
				try {
					statusCode = httpConnection.getResponseCode();
					return String.valueOf(statusCode);
				} catch (IOException e) {
					e.printStackTrace();
				}
				httpConnection.disconnect();
				ioe.printStackTrace();
			return null;
		}

	}

	/**
	 * @param email
	 * @return
	 * @throws IOException
	 * @throws JSONException
	 * @throws NoSuchAlgorithmException
	 * 
	 * This method is used to check if the subscriber exists.
	 */
	private String isSubscriberexist(String email) throws IOException, JSONException, NoSuchAlgorithmException {

		InputStream content = null;
		try {
			String status = "";
			httpConnection = getConnection(email);
			httpConnection.setRequestMethod("GET");
			httpConnection.setDoOutput(true);
			httpConnection.setRequestProperty("Authorization",  authorizationHeader);
			httpConnection.setRequestProperty("Accept", "application/json");
			content = httpConnection.getInputStream();
			StringBuilder sb = new StringBuilder();
			BufferedReader rd = new BufferedReader(new InputStreamReader(content));
			String line;
			while ((line = rd.readLine()) != null) {
				sb.append(line);
			}
			logger.debug("get response" + sb);
			logger.debug("test:" + httpConnection.getResponseMessage());

			JSONObject jsonObj = new JSONObject(sb.toString());
			logger.debug("status:" + jsonObj.get(KEY_STATUS));

			return (String) jsonObj.get(KEY_STATUS);
		} catch (MalformedURLException | ProtocolException e) {
			e.printStackTrace();
		}
		catch (IOException ioe) {
				int statusCode = httpConnection.getResponseCode();
				if (statusCode != 200) {
					return String.valueOf(statusCode);
				}
		}
		return null;
	}

	/**
	 * @param email
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 * 
	 * This method is used to get URL Connection to the Mailchimp service endpoint
	 */
	/**
	 * @param email
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 */
	private HttpURLConnection getConnection(String email) throws NoSuchAlgorithmException, IOException {
		String emailHash = md5Java(email);
		String endpoint = baseUrl + "/lists/" + listId + "/members/" + emailHash;
		URL url = new URL(endpoint);		
		httpConnection = (HttpURLConnection) url.openConnection();
		return httpConnection;
	}

	/**
	 * @param message
	 * @return
	 * @throws NoSuchAlgorithmException
	 * 
	 * This method is used to apply Message Digest Algorithm to the email-id while making Mailchimp Service call.
	 */
	private String md5Java(String message) throws NoSuchAlgorithmException {
		String digest = null;

		MessageDigest md = MessageDigest.getInstance("MD5");
		byte[] hash = md.digest(message.getBytes(StandardCharsets.UTF_8));
		// converting byte array to Hexadecimal String
		StringBuilder sb = new StringBuilder(2 * hash.length);
		for (byte b : hash) {
			sb.append(String.format("%02x", b & 0xff));
		}
		digest = sb.toString();

		return digest;
	}

	public Locale getLocaleLanguage(String language){
		switch (language) {
		case "en":
			return Locale.ENGLISH;
		case "ar":
			return new Locale("ar");
		default:
			return Locale.ENGLISH;
		}
	}
	
	public String decodeToArabicString(String str) {
	      
	      byte[] charset = str.getBytes(StandardCharsets.UTF_8);
	      return new String(charset, StandardCharsets.UTF_8);
	   }
	
	
}
