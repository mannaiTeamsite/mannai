package com.hukoomi.livesite.external;

import java.io.IOException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.json.JSONObject;

import com.hukoomi.utils.CommonUtils;
import com.hukoomi.utils.JWTTokenUtil;
import com.hukoomi.utils.Postgre;
import com.hukoomi.utils.PropertiesFileReader;
import com.hukoomi.utils.RequestHeaderUtils;
import com.hukoomi.utils.UserInfoSession;
import com.interwoven.livesite.runtime.RequestContext;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;

public class DashboardExternal {
	private Properties properties = null;
	private String locale = "";
	private String userID = "";
	private String table = "";
	private String pagetitle = "";
	private String pageurl = "";
	private String contenttype = "";
	
	private String strValid = "valid";
	private String strStatus = "status";
	private String strUnm = "unm";
	private String strFnEn = "fnEn";
	private String strUid = "uid";
	private String strFnAr = "fnAr";
	private String strLnEn = "lnEn";
	private String strLnAr = "lnAr";
	private String strQID = "QID";
	private String strEID = "EID";
	private String strMobile = "mobile";
	private String strEmail = "email";
	private String strLstMdfy = "lstMdfy";
	private String strRole = "role";
	private String strExp = "exp";
	private String strType = "type";
	private String strUserId = "userId";
	private String strUsertypeNo = "usertypeNo";
	private String strUserType = "userType";

	private static final Logger LOGGER = Logger.getLogger(DashboardExternal.class);
	Postgre postgre = null;

	/** Decrypting the token and setting session. */
	public void dashboardServices(RequestContext context, String accessToken) {
		String jwtParsedToken = null;
		HttpServletRequest request = context.getRequest();
		JWTTokenUtil jwt = new JWTTokenUtil(context);
		
			try {
				jwtParsedToken = jwt.parseJwt(accessToken);

				setSessionAttributes(jwtParsedToken, request, strValid);
				
				
			} catch (ExpiredJwtException e) {
				LOGGER.error("Token Expired");
				setSessionAttributes(jwtParsedToken, request, "Token Expired");
			} catch (SignatureException e) {
				LOGGER.error("Signature Exception");
				setSessionAttributes(jwtParsedToken, request, "Signature Exception");
			} catch (Exception e) {
				LOGGER.error("Some other exception in JWT parsing" , e);
				setSessionAttributes(jwtParsedToken, request, "Exception in JWT parsing");
			}

		}
	

	/** Setting the user info in session. */
	private void setSessionAttributes(String jwtParsedToken, HttpServletRequest request, String status) {
		LOGGER.info("--------------setSessionAttributes is called------------");
	
		HttpSession session = request.getSession(true);

		session.setAttribute(strStatus, status);
		if (status.equals(strValid)) {
			session.setAttribute(strUnm, getValue(jwtParsedToken, strUnm));
			session.setAttribute(strUid, getValue(jwtParsedToken, strUid));
			session.setAttribute(strFnEn, getValue(jwtParsedToken, strFnEn));
			session.setAttribute(strFnAr, getValue(jwtParsedToken, strFnAr));
			session.setAttribute(strLnEn, getValue(jwtParsedToken, strLnEn));
			session.setAttribute(strLnAr, getValue(jwtParsedToken, strLnAr));
			session.setAttribute(strQID, getValue(jwtParsedToken, strQID));
			session.setAttribute(strEID, getValue(jwtParsedToken, strEID));
			session.setAttribute(strMobile, getValue(jwtParsedToken, strMobile));
			session.setAttribute(strEmail, getValue(jwtParsedToken, strEmail));
			session.setAttribute(strLstMdfy, getValue(jwtParsedToken, strLstMdfy));
			session.setAttribute(strRole, getValue(jwtParsedToken, strRole));
			session.setAttribute(strExp, getValue(jwtParsedToken, strExp));
			session.setAttribute(strUsertypeNo, getValue(jwtParsedToken, strType));
			session.setAttribute(strUserId, getValue(jwtParsedToken, strUid));
			String userTypeStr = getValue(jwtParsedToken, strType);

			if (userTypeStr != null && !"".equals(userTypeStr)) {
				int userType = Integer.parseInt(userTypeStr);

				if (userType == 1 || userType == 4) {
					session.setAttribute(strUserType, "personal");
				} else if (userType == 2) {
					session.setAttribute(strUserType, "business");
				}
			}
		}

		LOGGER.info("--------------setSessionAttributes is Ended------------");
	}


	private static String getValue(String response, String key) {
		String status = "";
		if (!response.equals("")) {
			JSONObject jsonObj = new JSONObject(response);
			if (!jsonObj.isNull(key))
				status = (String) jsonObj.get(key);
		}
		return status;
	}

	/** Removing the session values on logout. */
	public void removeSessionAttr(RequestContext context)  {
		LOGGER.info("--------------removeSessionAttr is Ended------------");
		HttpServletRequest request = context.getRequest();
		HttpSession session = request.getSession(false);	
		try {				
			removeSession(session, strStatus);	
			removeSession(session, strUnm);
			removeSession(session, strUid);
			removeSession(session, strFnEn);		
			removeSession(session, strFnAr);		
			removeSession(session, strLnEn);		
			removeSession(session, strLnAr);		
			removeSession(session, strQID);		
			removeSession(session, strEID);		
			removeSession(session, strMobile);		
			removeSession(session, strEmail);		
			removeSession(session, strLstMdfy);		
			removeSession(session, strRole);		
			removeSession(session, strExp);		
			removeSession(session, strUserId);	
			removeSession(session, strUsertypeNo);		
			removeSession(session, strUserType);				
		}catch(NullPointerException e) {
			LOGGER.error("Some other exception in JWT parsing" , e);
		}
		LOGGER.info("--------------removeSessionAttr is Ended------------" );
	}
	
	public void removeSession(HttpSession session, String rmAttr) {		
			if(session != null)
				session.removeAttribute(rmAttr);	
	}
	
	public Document doLogout(RequestContext context) throws IOException {
		/**
		 * This method will be called from Component External for logout.
		 * 
		 * @param context The parameter context object passed from Component.
		 *
		 * @return doc return the solr response document generated from solr query.
		 */
		LOGGER.info("--------------doLogout Started------------");
		CommonUtils cu = new CommonUtils();
		Document doc = DocumentHelper.createDocument();
		removeSessionAttr(context);

		PropertiesFileReader prop = null;
		prop = new PropertiesFileReader(context, "dashboard.properties");
		properties = prop.getPropertiesFile();
		

		RequestHeaderUtils rhu = new RequestHeaderUtils(context);
		final String RELAY_URL = "relayURL";
		String relayURL = rhu.getCookie(RELAY_URL);
		
		 relayURL = cu.getUrlPath(relayURL);

		 String urlPrefix = cu.getURLPrefix(context);
		 relayURL = urlPrefix + relayURL;
		
		
		String logoutrl = properties.getProperty("logout", "https://hukoomi.gov.qa/nas/auth/slo");
		
		logoutrl = logoutrl+ "?relayURL=" + relayURL;
		LOGGER.info("logout url:"+logoutrl);
		HttpServletResponse response = context.getResponse();
		response.sendRedirect(logoutrl);
		LOGGER.info("--------------doLogout Ended------------");
		return doc;
	}

	public Document getDashboardContent(RequestContext context) {
		/**
		 * This method will be called from Component External for Fetching user data after login
		 * 
		 * @param context The parameter context object passed from Component.
		 *
		 * @return doc return the solr response document generated from solr query.
		 */
		LOGGER.info("--------------getDashboardConetent Started------------");
		

		Document doc = DocumentHelper.createDocument();
		Element rootElement = doc.addElement("result");

		Document bookmarkDoc = getDashboardbookmark(context);

		PollSurveyExternal ps = new PollSurveyExternal();
		Document pollsSurveyDoc = ps.getContent(context);

		Element pollsrootElement = pollsSurveyDoc.getRootElement();
		Element bookmarkRoot = bookmarkDoc.getRootElement();

		Element pollsSurvey = rootElement.addElement("polls-survey");
		pollsSurvey.add(pollsrootElement);
		
		Element bookmarkEle = rootElement.addElement("bookmarks");
		bookmarkEle.add(bookmarkRoot);
		
		Element userdata = rootElement.addElement("user-data");
UserInfoSession ui = new UserInfoSession();
		String valid = ui.getStatus(context);
		if(valid.equalsIgnoreCase(valid)) {
			HttpSession session = context.getRequest().getSession();
			Element userTypeElement = userdata.addElement(strUserType);
			userTypeElement.setText((String) session.getAttribute(strUserType));
			Element fnEnElement = userdata.addElement(strFnEn);
			fnEnElement.setText((String) session.getAttribute(strFnEn));
			Element lnEnElement = userdata.addElement(strLnEn);
			lnEnElement.setText((String) session.getAttribute(strLnEn));

			Element fnArElement = userdata.addElement(strFnAr);
			fnArElement.setText((String) session.getAttribute(strFnAr));
			Element lnArElement = userdata.addElement(strLnAr);
			lnArElement.setText((String) session.getAttribute(strLnAr));
			Element userTypeNoElement = userdata.addElement("userTypeNoElement");
			userTypeNoElement.setText((String) session.getAttribute(strUsertypeNo));
			Element emailElement = userdata.addElement(strEmail);
			emailElement.setText((String) session.getAttribute(strEmail));
		}

		
		LOGGER.info("--------------getDashboardConetent Ended------------");
		return doc;

	}

	public Document getMyDataContent(RequestContext context) {
		/**
		 * This method will be called from Component External for Fetching user data after login
		 * 
		 * @param context The parameter context object passed from Component.
		 *
		 * @return doc return the solr response document generated from solr query.
		 */
		LOGGER.info("--------------getDashboardConetent Started------------");
		

		Document doc = DocumentHelper.createDocument();
		Element resultTelement = doc.addElement("result");
		Element userData = resultTelement.addElement("userData");
		
UserInfoSession ui = new UserInfoSession();

		String valid = ui.getStatus(context);
		if(valid.equalsIgnoreCase(strValid)) {
			HttpSession session = context.getRequest().getSession();
			Element userTypeElement = userData.addElement(strUserType);
			userTypeElement.setText((String) session.getAttribute(strUserType));
			Element fnEnElement = userData.addElement(strFnEn);
			fnEnElement.setText((String) session.getAttribute(strFnEn));
			Element lnEnElement = userData.addElement(strLnEn);
			lnEnElement.setText((String) session.getAttribute(strLnEn));

			Element fnArElement = userData.addElement(strFnAr);
			fnArElement.setText((String) session.getAttribute(strFnAr));
			Element lnArElement = userData.addElement(strLnAr);
			lnArElement.setText((String) session.getAttribute(strLnAr));
			Element userTypeNoElement = userData.addElement("userTypeNoElement");
			userTypeNoElement.setText((String) session.getAttribute("usertypeNo"));

		} else {
			try {
				redirectToLoginPage(context);
			} catch (IOException e) {
				LOGGER.error(e);

			}

			LOGGER.info("session invalid");
		}
		
		LOGGER.info("--------------getDashboardConetent Ended------------");
		return doc;

	}
	@SuppressWarnings("deprecation")
	public Document getDashboardbookmark(RequestContext context) {
		/**
		 * This method will be called from Component External for Fetching bookmark
		 * 
		 * @param context The parameter context object passed from Component.
		 *
		 * @return doc return the solr response document generated from solr query.
		 */
		LOGGER.info("getDashboardbookmark()====> Starts");

		pagetitle = context.getParameterString("page_title");

		pageurl = context.getParameterString("page_url");

		contenttype = context.getParameterString("content_type");

		String queryType = context.getParameterString("queryType").trim();

		Document bookmarkSearchDoc = DocumentHelper.createDocument();
		Element bookmarkResultEle = bookmarkSearchDoc.addElement("bookmark");

		postgre = new Postgre(context);
		
		
		UserInfoSession ui = new UserInfoSession();
		
		HttpSession session = context.getRequest().getSession();
		String valid = ui.getStatus(context);
		LOGGER.info("Dashboard status=" + session.getAttribute(strStatus));
		if ( valid.equals(strValid)) {
			userID = (String) session.getAttribute(strUserId);
			LOGGER.info("userID:" + userID);
			locale = context.getParameterString("locale").trim().toLowerCase();

			table = context.getParameterString("bookmark").trim();
			boolean isExist = false;
			LOGGER.info("locale:" + locale);

			isExist = isBookmarkPresent();

			LOGGER.info("table:" + table);

			if (!"".equals(table) && !"".equals(userID)) {
				if ("select".equals(queryType)) {
					Element bmm = bookmarkResultEle.addElement("bmm");
					getBookmark(bmm, "bmm");
					Element bmd = bookmarkResultEle.addElement("bmd");
					getBookmark(bmd, "bmd");
					Element bms = bookmarkResultEle.addElement("bms");
					getBookmark(bms, "bms");

				} else if (isExist && "remove".equals(queryType)) {
					removeBookmark(bookmarkResultEle);

				}

			}
			LOGGER.info("session valid");
		} else {

			try {
				redirectToLoginPage(context);
			} catch (IOException e) {
				LOGGER.error("Error" + e);
			}
			LOGGER.info("session invalid");
		}
		LOGGER.info("getDashboardbookmark()====> ends");
		return bookmarkSearchDoc;
	}

	private Connection getConnection() {
		return postgre.getConnection();
	}

	private void getBookmark(Element element, String category) {
		String activeflag = "Y";
		LOGGER.info("getBookmark()====> Starts");
		Connection connection = getConnection();
		PreparedStatement prepareStatement = null;
		String searchQuery = "select page_title, page_url, page_description, active, content_type, category from" + " "
				+ table + " " + "where" + " " + "locale='" + locale + "' and " + "user_id='" + userID + "' and active='"
				+ activeflag + "' and category='" + category + "'";
		LOGGER.info("searchQuery:" + searchQuery);
		ResultSet resultSet = null;
		try {

			if (connection != null) {
				prepareStatement = connection.prepareStatement(searchQuery);
				resultSet = prepareStatement.executeQuery();
				String pageTitle = "";
				String pageURL = "";
				int i = 0;
				while (resultSet.next()) {
					Element ele = element.addElement("bookmarks");
					pageTitle = resultSet.getString(1);
					pageURL = resultSet.getString(2);
					String pagedesc = resultSet.getString(3);
					String pageactive = resultSet.getString(4);
					String ctype = resultSet.getString(5);
					String categoryType = resultSet.getString(6);
					if (!"".equals(pageTitle) && !"".equals(pageURL)) {
						Element ele1 = ele.addElement("pageTitle");
						ele1.setText(pageTitle);
						Element ele2 = ele.addElement("pageURL");
						ele2.setText(pageURL);
						Element ele3 = ele.addElement("pageDescription");
						ele3.setText(pagedesc);
						Element ele4 = ele.addElement("active");
						ele4.setText(pageactive);
						Element ele5 = ele.addElement("contentType");
						ele5.setText(ctype);
						Element ele6 = ele.addElement("category");
						ele6.setText(categoryType);

						LOGGER.info("Result:" + pageTitle + ":" + pageURL);
					}
					i++;
				}
				Element ele7 = element.addElement("count");
				ele7.setText("" + i + "");
			}
		} catch (SQLException ex) {
			LOGGER.error("Exception on Select Query:", ex);
		} finally {
			postgre.releaseConnection(connection, prepareStatement, resultSet);
		}
		LOGGER.info("getBookmark()====> ends");

	}

	private void removeBookmark(Element bookmarkResultEle) {
		/**
		 * This method will be called from Component External for removing the bookmark
		 * 
		 * @param context The parameter context object passed from Component.
		 *
		 * @return doc return the solr response document.
		 */

		String activeflag = "N";
		LOGGER.info("removeBookmark()====> Starts");
		Connection connection = getConnection();
		PreparedStatement prepareStatement = null;
		String updateQuery = "UPDATE" + " " + table + " set active='" + activeflag + "' where locale='" + locale
				+ "' and user_id='" + userID + "' and page_title='" + pagetitle + "' and page_url='" + pageurl
				+ "' and content_type='" + contenttype + "'";
		LOGGER.info("updateQuery:" + updateQuery);

		
		try {
			if (connection != null) {
				prepareStatement = connection.prepareStatement(updateQuery);
				prepareStatement.executeUpdate();

				LOGGER.info("caling get bookmark");
				Element bmm = bookmarkResultEle.addElement("bmm");
				getBookmark(bmm, "bmm");
				LOGGER.info("bmm" + bmm.asXML());
				Element bmd = bookmarkResultEle.addElement("bmd");
				getBookmark(bmd, "bmd");
				LOGGER.info("bmd" + bmd.asXML());
				Element bms = bookmarkResultEle.addElement("bms");
				getBookmark(bms, "bms");
				LOGGER.info("bms" + bms.asXML());
			}
		} catch (SQLException ex) {
			LOGGER.error("Exception on update Query:", ex);
		} finally {
			postgre.releaseConnection(connection, prepareStatement, null);
		}

		LOGGER.info("removeBookmark()====> ends");

	}

	public boolean isBookmarkPresent() {
		boolean check = false;
		String isBookmarked = "";
		LOGGER.info("isBookmarkPresent()====> Starts");
		Connection connection = getConnection();
		PreparedStatement prepareStatement = null;
		String searchQuery = "select active from" + " " + table + " " + "where" + " " + "locale='" + locale
				+ "' and user_id='" + userID + "' and page_title='" + pagetitle + "' and page_url='" + pageurl
				+ "' and content_type='" + contenttype + "'";
		ResultSet resultSet = null;
		try {
			if (connection != null) {
				prepareStatement = connection.prepareStatement(searchQuery);
				resultSet = prepareStatement.executeQuery();

				while (resultSet.next()) {
					isBookmarked = resultSet.getString(1);

					if (!"".equals(isBookmarked)) {
						check = true;
						LOGGER.info("check:" + check);
					}
				}
			}
		} catch (SQLException ex) {
			LOGGER.error("Exception on Select Query:", ex);
		} finally {
			postgre.releaseConnection(connection, prepareStatement, resultSet);
		}
		LOGGER.info("getBookmark()====> ends");
		return check;
	}

	public void redirectToLoginPage(RequestContext context) throws IOException {
		LOGGER.info("--------------nonLoggedIn Started------------");
		PropertiesFileReader prop = null;
		prop = new PropertiesFileReader(context, "dashboard.properties");
		properties = prop.getPropertiesFile();
		RequestHeaderUtils rhu = new RequestHeaderUtils(context);
		final String RELAY_URL = "relayURL";
		String relayURL = rhu.getCookie(RELAY_URL);

		 CommonUtils cu = new CommonUtils();
		 relayURL = cu.getUrlPath(relayURL);	
		 String urlPrefix = cu.getURLPrefix(context);
		 relayURL = urlPrefix + relayURL;
		 LOGGER.info("---relayURL url---" + relayURL);
		 String url = properties.getProperty("login") + "?relayURL=" + relayURL;
		 
			HttpServletResponse response = context.getResponse();
			response.sendRedirect(url);
		

		LOGGER.info("--------------nonLoggedIn Ended------------");

	}
}