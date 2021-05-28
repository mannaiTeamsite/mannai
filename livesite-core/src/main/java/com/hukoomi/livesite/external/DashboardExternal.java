package com.hukoomi.livesite.external;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
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

import com.hukoomi.utils.JWTTokenUtil;
import com.hukoomi.utils.Postgre;
import com.hukoomi.utils.PropertiesFileReader;
import com.hukoomi.utils.RequestHeaderUtils;
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
	private String isBookmarked = "";
	private String contenttype = "";

	private static final Logger LOGGER = Logger.getLogger(DashboardExternal.class);
	Postgre postgre = null;

	public void dashboardServices(RequestContext context, String accessToken) {
		String jwtParsedToken = null;
		HttpServletRequest request = context.getRequest();
		JWTTokenUtil jwt = new JWTTokenUtil(context);
		if (accessToken != null)
			try {
				jwtParsedToken = jwt.parseJwt(accessToken);

				setSessionAttributes(jwtParsedToken, request, "valid");
			} catch (ExpiredJwtException e) {
				LOGGER.info("Token Expired");
				setSessionAttributes(jwtParsedToken, request, "Token Expired");
			} catch (SignatureException e) {
				LOGGER.info("Signature Exception");
				setSessionAttributes(jwtParsedToken, request, "Signature Exception");
			} catch (Exception e) {
				LOGGER.info("Some other exception in JWT parsing" + e);
				setSessionAttributes(jwtParsedToken, request, "Some other exception in JWT parsing");
			}
	}

	private void setSessionAttributes(String jwtParsedToken, HttpServletRequest request, String status) {
		LOGGER.info("--------------setSessionAttributes is called------------");
		HttpSession session = request.getSession(true);

		session.setAttribute("status", status);
		if (status.equals("valid")) {
			session.setAttribute("unm", getValue(jwtParsedToken, "unm"));
			session.setAttribute("uid", getValue(jwtParsedToken, "uid"));
			session.setAttribute("fnEn", getValue(jwtParsedToken, "fnEn"));
			session.setAttribute("fnAr", getValue(jwtParsedToken, "fnAr"));
			session.setAttribute("lnEn", getValue(jwtParsedToken, "lnEn"));
			session.setAttribute("lnAr", getValue(jwtParsedToken, "lnAr"));
			session.setAttribute("QID", getValue(jwtParsedToken, "QID"));
			session.setAttribute("EID", getValue(jwtParsedToken, "EID"));
			session.setAttribute("mobile", getValue(jwtParsedToken, "mobile"));
			session.setAttribute("email", getValue(jwtParsedToken, "email"));
			session.setAttribute("lstMdfy", getValue(jwtParsedToken, "lstMdfy"));
			session.setAttribute("role", getValue(jwtParsedToken, "role"));
			session.setAttribute("exp", getValue(jwtParsedToken, "exp"));
			session.setAttribute("usertypeNo", getValue(jwtParsedToken, "type"));
			session.setAttribute("userId", getValue(jwtParsedToken, "uid"));

			LOGGER.info("UserId : " + getValue(jwtParsedToken, "uid"));

			String userTypeStr = getValue(jwtParsedToken, "type");
			LOGGER.info("userType JWT : " + userTypeStr);
			if (userTypeStr != null && !"".equals(userTypeStr)) {
				int userType = Integer.parseInt(userTypeStr);

				if (userType == 1 || userType == 4) {
					session.setAttribute("userType", "personal");
				} else if (userType == 2) {
					session.setAttribute("userType", "business");
				}
			}
			LOGGER.info("userType : " + session.getAttribute("userType"));

		}

		LOGGER.info("Expiry Date" + session.getAttribute("exp"));
		LOGGER.info("--------------setSessionAttributes is Ended------------");
	}

	private static String getValue(String response, String key) {
		String status = "";
		if (!response.equals("")) {
			JSONObject jsonObj = new JSONObject(response);
			if (!jsonObj.isNull(key) && !jsonObj.get(key).equals(null))
				status = (String) jsonObj.get(key);
		}
		return status;
	}

	public void removeSessionAttr(RequestContext context) {
		LOGGER.info("--------------removeSessionAttr is Ended------------");

		HttpServletRequest request = context.getRequest();
		HttpSession session = request.getSession(false);
		if (session != null)
			session.removeAttribute("status");
		session.removeAttribute("unm");
		session.removeAttribute("uid");
		session.removeAttribute("fnEn");
		session.removeAttribute("fnAr");
		session.removeAttribute("lnEn");
		session.removeAttribute("lnAr");
		session.removeAttribute("QID");
		session.removeAttribute("EID");
		session.removeAttribute("mobile");
		session.removeAttribute("email");
		session.removeAttribute("lstMdfy");
		session.removeAttribute("role");
		session.removeAttribute("exp");
		session.removeAttribute("userId");
		session.removeAttribute("usertypeNo");
		session.removeAttribute("userType");

		LOGGER.info("--------------removeSessionAttr is Ended------------" + session.getAttribute("status"));
	}

	public Document doLogout(RequestContext context) throws IOException {
		LOGGER.info("--------------doLogout Started------------");
		final String RELAY_URL = "relayURL";
		Document doc = DocumentHelper.createDocument();
		removeSessionAttr(context);

		PropertiesFileReader prop = null;
		prop = new PropertiesFileReader(context, "dashboard.properties");
		properties = prop.getPropertiesFile();
		RequestHeaderUtils rhu = new RequestHeaderUtils(context);
		String relayURL = rhu.getCookie(RELAY_URL);
		String url = properties.getProperty("logout") + "?relayURL=" + relayURL;
		LOGGER.info("---Logout url---" + url);
		HttpServletResponse response = context.getResponse();
		response.sendRedirect(url);
		LOGGER.info("--------------doLogout Ended------------");
		return doc;
	}

	public Document getDashboardContent(RequestContext context) {

		LOGGER.info("--------------getDashboardConetent Started------------");
		HttpSession session = context.getRequest().getSession();

		Document doc = DocumentHelper.createDocument();
		Element rootElement = doc.addElement("result");

		Document bookmarkDoc = getDashboardbookmark(context);

		PollSurveyExternal ps = new PollSurveyExternal();
		Document pollsSurveyDoc = ps.getContent(context);

		Element pollsrootElement = pollsSurveyDoc.getRootElement();
		Element bookmarkRoot = bookmarkDoc.getRootElement();

		Element pollsSurvey = rootElement.addElement("polls-survey");
		pollsSurvey.add(pollsrootElement);
		LOGGER.info("After adding Polls" + doc.asXML());
		Element bookmarkEle = rootElement.addElement("bookmarks");
		bookmarkEle.add(bookmarkRoot);
		LOGGER.info("After adding Bookmark" + doc.asXML());
		Element userdata = rootElement.addElement("user-data");
		String status = (String) session.getAttribute("status");
		LOGGER.info("status=" + session.getAttribute("status"));
		if (status != null && status.equals("valid")) {

			Element userTypeElement = userdata.addElement("userType");
			userTypeElement.setText((String) session.getAttribute("userType"));
			Element fnEnElement = userdata.addElement("fnEn");
			fnEnElement.setText((String) session.getAttribute("fnEn"));
			Element lnEnElement = userdata.addElement("lnEn");
			lnEnElement.setText((String) session.getAttribute("lnEn"));

			Element fnArElement = userdata.addElement("fnAr");
			fnArElement.setText((String) session.getAttribute("fnAr"));
			Element lnArElement = userdata.addElement("lnAr");
			lnArElement.setText((String) session.getAttribute("lnAr"));
			Element userTypeNoElement = userdata.addElement("userTypeNoElement");
			userTypeNoElement.setText((String) session.getAttribute("usertypeNo"));
		}

		LOGGER.info("Final doc" + doc.asXML());
		LOGGER.info("--------------getDashboardConetent Ended------------");
		return doc;

	}

	public Document getMyDataContent(RequestContext context) {

		LOGGER.info("--------------getDashboardConetent Started------------");
		HttpSession session = context.getRequest().getSession();

		Document doc = DocumentHelper.createDocument();
		Element resultTelement = doc.addElement("result");
		Element userData = resultTelement.addElement("userData");
		String status = (String) session.getAttribute("status");
		if (status != null && status.equals("valid")) {
			Element userTypeElement = userData.addElement("userType");
			userTypeElement.setText((String) session.getAttribute("userType"));
			Element fnEnElement = userData.addElement("fnEn");
			fnEnElement.setText((String) session.getAttribute("fnEn"));
			Element lnEnElement = userData.addElement("lnEn");
			lnEnElement.setText((String) session.getAttribute("lnEn"));

			Element fnArElement = userData.addElement("fnAr");
			fnArElement.setText((String) session.getAttribute("fnAr"));
			Element lnArElement = userData.addElement("lnAr");
			lnArElement.setText((String) session.getAttribute("lnAr"));
			Element userTypeNoElement = userData.addElement("userTypeNoElement");
			userTypeNoElement.setText((String) session.getAttribute("usertypeNo"));

		} else {
			try {
				redirectToLoginPage(context);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			LOGGER.info("session invalid");
		}
		LOGGER.info("Bookmark doc" + doc.asXML());
		LOGGER.info("--------------getDashboardConetent Ended------------");
		return doc;

	}

	public Document getDashboardbookmark(RequestContext context) {
		LOGGER.info("getDashboardbookmark()====> Starts");

		pagetitle = context.getParameterString("page_title");

		pageurl = context.getParameterString("page_url");

		contenttype = context.getParameterString("content_type");

		String queryType = context.getParameterString("queryType").trim();

		Document bookmarkSearchDoc = DocumentHelper.createDocument();
		Element bookmarkResultEle = bookmarkSearchDoc.addElement("bookmark");

		postgre = new Postgre(context);
		HttpSession session = context.getRequest().getSession();
		// String status = "valid";
		String status = (String) session.getAttribute("status");
		LOGGER.info("Dashboard status=" + session.getAttribute("status"));
		if (status != null && status.equals("valid")) {
			// userID = "69119";
			userID = (String) session.getAttribute("userId");
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
				// TODO Auto-generated catch block
				LOGGER.info("Error" + e);
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

		String activeflag = "N";
		LOGGER.info("removeBookmark()====> Starts");
		Connection connection = getConnection();
		PreparedStatement prepareStatement = null;
		String updateQuery = "UPDATE" + " " + table + " set active='" + activeflag + "' where locale='" + locale
				+ "' and user_id='" + userID + "' and page_title='" + pagetitle + "' and page_url='" + pageurl
				+ "' and content_type='" + contenttype + "'";
		LOGGER.info("updateQuery:" + updateQuery);

		int result = 0;
		try {
			if (connection != null) {
				prepareStatement = connection.prepareStatement(updateQuery);
				result = prepareStatement.executeUpdate();

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

	public Document redirectToLoginPage(RequestContext context) throws IOException {
		LOGGER.info("--------------nonLoggedIn Started------------");

//			final String RELAY_URL = "relayURL";
		PropertiesFileReader prop = null;
		prop = new PropertiesFileReader(context, "dashboard.properties");
		properties = prop.getPropertiesFile();
		RequestHeaderUtils rhu = new RequestHeaderUtils(context);
		String relayURL = rhu.getRequestURL();
		LOGGER.info("---relayURL url---" + relayURL);
		String url = properties.getProperty("login") + "?relayURL=" + relayURL;
		LOGGER.info("---Login url---" + url);
		String domain = "";
		URI uri;
		try {
			uri = new URI(relayURL);
			domain = uri.getHost();
			LOGGER.info("Domain:" + domain);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String livesiteDomain = properties.getProperty("domain");
		LOGGER.info("livesiteDomain:" + livesiteDomain);
		if(domain == livesiteDomain ) {
			HttpServletResponse response = context.getResponse();
			response.sendRedirect(url);
		}
		
		Document doc = DocumentHelper.createDocument();

		LOGGER.info("--------------nonLoggedIn Ended------------");
		return doc;

	}
}