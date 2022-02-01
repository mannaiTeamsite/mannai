package com.hukoomi.livesite.external;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import com.hukoomi.utils.CommonUtils;
import com.hukoomi.utils.ESAPIValidator;
import com.hukoomi.utils.Postgre;
import com.hukoomi.utils.UserInfoSession;
import com.interwoven.livesite.runtime.RequestContext;

public class BookmarkExternal {
	private String locale = "";
	private String userID = "";
	private String pagetitle = "";
	private String pagedescription = "";
	private String pageurl = "";
	private String active = "";
	private String contenttype = "";
	private String table = "";
	private String category = "";
	private String localLiteral = "locale";
	private String activeLiteral = "active";
	private String categoryLiteral = "category";
	private String pagetitleLiteral = "page_title";
	private String pagedescriptionLiteral = "page_description";
	private String pageurlLiteral = "page_url";
	private String userIDLiteral = "userId";

	private static final Logger logger = Logger.getLogger(BookmarkExternal.class);
	Postgre postgre = null;

	@SuppressWarnings("deprecation")
	public Document bookmarkSearch(final RequestContext context) {
		logger.info("BookmarkExternal()====> Starts");

		Document bookmarkSearchDoc = DocumentHelper.createDocument();
		Element bookmarkResultEle = bookmarkSearchDoc.addElement("bookmark");

		postgre = new Postgre(context);

		UserInfoSession ui = new UserInfoSession();

		HttpSession session = context.getRequest().getSession();
		String valid = ui.getStatus(context);
		if (valid.equalsIgnoreCase("valid")) {

			userID = (String) session.getAttribute(userIDLiteral);
			logger.info("userID:" + userID);
			locale = context.getParameterString(localLiteral).trim().toLowerCase();
			pagetitle = context.getParameterString(pagetitleLiteral);
			pagedescription = context.getParameterString(pagedescriptionLiteral);
			pageurl = context.getParameterString(pageurlLiteral);
			active = context.getParameterString(activeLiteral);
			contenttype = context.getParameterString("content_type");
			category = context.getParameterString(categoryLiteral);
			String queryType = context.getParameterString("queryType").trim();
			table = context.getParameterString("bookmark").trim();
			int errorVal = 0;
			CommonUtils cu = new CommonUtils();

			errorVal += cu.esapiValidator("pagetitle", pagetitle, ESAPIValidator.ALPHANUMERIC_SPACE, 255, false, true);
			errorVal += cu.esapiValidator("pagedescription", pagedescription, ESAPIValidator.ALPHANUMERIC_SPACE, 255,
					true, true);
			errorVal += cu.esapiValidator("pageurl", pageurl, ESAPIValidator.URL, 255, false, true);
			errorVal += cu.esapiValidator(localLiteral, locale, ESAPIValidator.ALPHABET, 20, false, true);
			errorVal += cu.esapiValidator(userIDLiteral, userID, ESAPIValidator.USER_ID, 255, false, true);
			errorVal += cu.esapiValidator(activeLiteral, active, ESAPIValidator.ALPHANUMERIC_SPACE, 255, false, true);
			errorVal += cu.esapiValidator("contenttype", contenttype, ESAPIValidator.ALPHANUMERIC_SPACE, 255, false,
					true);
			errorVal += cu.esapiValidator(categoryLiteral, category, ESAPIValidator.ALPHANUMERIC_HYPHEN, 255, false,
					true);
			errorVal += cu.esapiValidator("queryType", queryType, ESAPIValidator.ALPHANUMERIC_HYPHEN, 255, false, true);
			errorVal += cu.esapiValidator("table", table, ESAPIValidator.ALPHANUMERIC_HYPHEN, 255, false, true);

			logger.info("Error Count :" + errorVal);
			if (errorVal > 0) {
				return bookmarkSearchDoc;
			}

			boolean isExist = false;
			logger.info("locale:" + locale);

			logger.info("table:" + table);
			if (!"".equals(table) && !"".equals(userID)) {
				if ("insert".equalsIgnoreCase(queryType)) {
					isExist = isBookmarkPresent();
					insertStatus(isExist);
				} else {
					getBookmark(bookmarkResultEle);
				}
			}
			logger.info("session valid");
		} else {

			logger.info("session invalid");
			context.getResponse().setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			bookmarkResultEle = bookmarkResultEle.addElement("session");
			bookmarkResultEle.setText("Session Invalid");
		}
		logger.info("bookmarkSearch====> ends");
		return bookmarkSearchDoc;
	}

	public void insertStatus(boolean isExist) {
		if (isExist) {
			int updateStatus = updateBookmark();
			if (updateStatus == 1) {
				logger.info("Bookmark updated");
			} else {
				logger.info("Bookmark not updated");
			}
		} else {
			int insertStatus = insertBookmark();
			if (insertStatus == 1) {
				logger.info("Bookmark inserted");
			} else {
				logger.info("Bookmark not inserted");
			}
		}

	}

	private int insertBookmark() {

		logger.info("insertBookmark()====> Starts");

		int result = 0;
		Connection connection = null;
		PreparedStatement prepareStatement = null;
		String bookmarkInsertQuery = "INSERT INTO" + " " + table
				+ "(\"page_title\",\"page_description\", \"page_url\", \"locale\", \"creation_date\", \"user_id\",\"active\",\"content_type\",\"category\")"
				+ " VALUES(?,?,?,?,LOCALTIMESTAMP,?,?,?,?)";
		logger.info("bookmarkInsertQuery:" + bookmarkInsertQuery);

		try {
			connection = getConnection();
			if (connection != null) {

				prepareStatement = connection.prepareStatement(bookmarkInsertQuery);
				prepareStatement.setString(1, pagetitle);
				prepareStatement.setString(2, pagedescription);
				prepareStatement.setString(3, pageurl);
				prepareStatement.setString(4, locale);
				prepareStatement.setString(5, userID);
				prepareStatement.setString(6, active);
				prepareStatement.setString(7, contenttype);
				prepareStatement.setString(8, category);
				result = prepareStatement.executeUpdate();

			} else {
				logger.info("Connection is null !");
			}
		} catch (SQLException ex) {
			logger.error("Exception on insert Query:", ex);
		} finally {
			postgre.releaseConnection(connection, prepareStatement, null);
		}
		logger.info("insertBookmark()====> ends");
		return result;
	}

	private Connection getConnection() {
		return postgre.getConnection();
	}

	private void getBookmark(Element bookmarkResultEle) {
		String activeflag = "Y";
		logger.info("getTopSearch()====> Starts");
		Connection connection = getConnection();
		PreparedStatement prepareStatement = null;
		String searchQuery = "select page_title, page_url, page_description, active, content_type, category from" + " "
				+ table + " " + "where locale = ? and user_id= ? and category= ? and active= ?";
		logger.info("searchQuery:" + searchQuery);
		ResultSet resultSet = null;
		try {
			if (connection != null) {
				prepareStatement = connection.prepareStatement(searchQuery);
				prepareStatement.setString(1, locale);
				prepareStatement.setString(2, userID);
				prepareStatement.setString(3, category);
				prepareStatement.setString(4, activeflag);
				resultSet = prepareStatement.executeQuery();
				String pageTitle = "";
				String pageURL = "";

				while (resultSet.next()) {
					Element ele = bookmarkResultEle.addElement("bookmarkDetails");
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
						Element ele4 = ele.addElement(activeLiteral);
						ele4.setText(pageactive);
						Element ele5 = ele.addElement("contentType");
						ele5.setText(ctype);
						Element ele6 = ele.addElement(categoryLiteral);
						ele6.setText(categoryType);
						logger.info("Result:" + pageTitle + ":" + pageURL);
					}

				}
			}
		} catch (SQLException ex) {
			logger.error("Exception on Select Query:", ex);
		} finally {
			postgre.releaseConnection(connection, prepareStatement, resultSet);
		}
		logger.info("getBookmark()====> ends");

	}

	private boolean isBookmarkPresent() {
		boolean check = false;
		String isBookmarked = "";
		logger.info("isBookmarkPresent()====> Starts");
		Connection connection = getConnection();
		PreparedStatement prepareStatement = null;
		String searchQuery = "select active from" + " " + table + " " + "where" + " " + "locale= ? and user_id= ? and page_title= ? and page_url= ? and content_type= ?";

		ResultSet resultSet = null;
		try {
			if (connection != null) {
				prepareStatement = connection.prepareStatement(searchQuery);
				prepareStatement.setString(1, locale);
				prepareStatement.setString(2, userID);
				prepareStatement.setString(3, pagetitle);
				prepareStatement.setString(4, pageurl);
				prepareStatement.setString(5, contenttype);
				resultSet = prepareStatement.executeQuery();

				while (resultSet.next()) {
					isBookmarked = resultSet.getString(1);

					if (!"".equals(isBookmarked)) {
						check = true;
						logger.info("check:" + check);
					}
				}
			}
		} catch (SQLException ex) {
			logger.error("Exception on Select Query:", ex);
		} finally {
			postgre.releaseConnection(connection, prepareStatement, resultSet);
		}
		logger.info("getBookmark()====> ends");
		logger.info("Is bookmark present :" + check);
		return check;
	}

	private int updateBookmark() {
		logger.info("updateBookmark()====> Starts");
		int result = 0;
		Connection connection = null;
		PreparedStatement prepareStatement = null;
		String bookmarkUpdateQuery = "UPDATE" + " " + table + " set active= ? where locale= ? and user_id= ? and page_title= ? and page_url= ? and content_type= ? ";
		logger.info("bookmarkInsertQuery:" + bookmarkUpdateQuery);

		try {
			connection = getConnection();
			if (connection != null) {

				prepareStatement = connection.prepareStatement(bookmarkUpdateQuery);
				prepareStatement.setString(1, active);
				prepareStatement.setString(2, locale);
				prepareStatement.setString(3, userID);
				prepareStatement.setString(4, pagetitle);
				prepareStatement.setString(5, pageurl);
				prepareStatement.setString(6, contenttype);
				result = prepareStatement.executeUpdate();

			} else {
				logger.info("Connection is null !");
			}
		} catch (SQLException ex) {
			logger.error("Exception on insert Query:", ex);
		} finally {
			postgre.releaseConnection(connection, prepareStatement, null);
		}
		logger.info("insertBookmark()====> ends");
		return result;
	}

}
