package com.hukoomi.livesite.servlet;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Enumeration;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.hukoomi.utils.XssUtils;
import com.interwoven.wcm.service.iwovregistry.utils.IREncryptionUtil;

public class ReviewComment extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4858292897536522240L;

	/** logger.debug object to check the flow of the code. */
	private static final Logger LOGGER = Logger.getLogger(ReviewComment.class);

	/** mail properties key. */
	private static final String CONTACT_FROM_MAIL = "sentFrom";
	/** mail properties key. */
	private static final String CONTACT_MAIL_HOST = "host";
	/** mail properties key. */
	private static final String CONTACT_MAIL_PORT = "port";
	/** mail properties key. */
	private static final String STARTTLS_ENABLE = "false";
	/** character set Constant */
	private static final String CHAR_SET = "UTF-8";
	/** character set Constant */
	private static final String BLOG_ID = "blogId";
	/** character set Constant */
	private static final String ERROR_MESSAGE = "errorMessage";
	/** character set Constant */
	private static final String SUCCESS = "success";
	/** character set Constant */
	private static final String USERNAME = "username";
	/** character set Constant */
	private static final String CREDENTIALS = "credPd";
	/** character set Constant */
	private static final String APPLICATION_JSON = "application/json";
	/** character set Constant */
	private static final String GET_COMMENT_BLOGID = "getCommentbyBlogId()";

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		LOGGER.info("ReviewComment : Start");
		JSONObject data = null;
		Enumeration<String> attributes = request.getSession().getAttributeNames();
		while (attributes.hasMoreElements())
			LOGGER.info("Value is: " + attributes.nextElement());

		try (BufferedReader inbr = new BufferedReader(new InputStreamReader(request.getInputStream()))) {
			String json = "";
			json = inbr.readLine();
			data = new JSONObject(json);
			boolean result = updateReviewData(data);
			if (result) {
				data.put(SUCCESS, SUCCESS);
				response.getWriter().write(data.toString());
			} else {
				data.put(SUCCESS, STARTTLS_ENABLE);
				data.put(ERROR_MESSAGE, "Failed to update");
				response.getWriter().write(data.toString());
			}

		} catch (IOException e) {
			response.setContentType(APPLICATION_JSON);
			response.setCharacterEncoding(CHAR_SET);
			try {
				if (data != null) {
					data.put(SUCCESS, STARTTLS_ENABLE);
					data.put(ERROR_MESSAGE, e.getMessage());
					response.getWriter().write(data.toString());
				}
			} catch (IOException e1) {
				LOGGER.error("REVIEW Failed : Exception ", e);
			} catch (Exception e1) {
				LOGGER.info(e1);
			}
		} catch (Exception e) {
			LOGGER.info(e);
		} finally {
			LOGGER.info("End of Review comment");
		}

	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		LOGGER.info("ReviewComment : Start");
		JSONObject data = null;
		JSONArray dataArray = null;
		String action = "";
		XssUtils xssUtils = new XssUtils();
		try {
			data = new JSONObject();
			request.getParameter(BLOG_ID);
			response.setContentType(APPLICATION_JSON);
			response.setCharacterEncoding(CHAR_SET);
			action = xssUtils.stripXSS(request.getParameter("action"));
			data.put("path", xssUtils.stripXSS(request.getParameter("path")));
			data.put("action", action);
			if (action.equals("getBlogs")) {
				dataArray = getBlogs(data);
			} else if (action.equals("getComments")) {

				data.put(BLOG_ID, Integer.parseInt(xssUtils.stripXSS(request.getParameter(BLOG_ID))));
				dataArray = getCommentbyBlogId(data);

			}

			if (dataArray != null) {
				data.put(SUCCESS, SUCCESS);
				data.put("comments", dataArray);
				response.getWriter().write(data.toString());
			} else {
				data.put(SUCCESS, STARTTLS_ENABLE);
				data.put(ERROR_MESSAGE, "Failed to update");
				response.getWriter().write(data.toString());
			}

		} catch (IOException e) {
			response.setContentType(APPLICATION_JSON);
			response.setCharacterEncoding(CHAR_SET);
			try {
				data.put(SUCCESS, STARTTLS_ENABLE);
				data.put(ERROR_MESSAGE, e.getMessage());
				response.getWriter().write(data.toString());
			} catch (IOException e1) {
				LOGGER.error("REVIEW Failed : Exception ", e);
			} catch (Exception e2) {
				LOGGER.info(e);
			}
		} catch (Exception e) {
			LOGGER.info(e);
		} finally {
			LOGGER.info("End of Review comment");
		}
	}

	private JSONArray getBlogs(JSONObject data) throws IOException {
		LOGGER.info("getBlogs");
		String profilePath = data.getString("path");
		PreparedStatement prepareStatement = null;
		ResultSet rs = null;
		Properties dbProperties = loadProperties(profilePath);
		Connection connection = null;
		JSONArray arrayComments = new JSONArray();
		try {
			String userName = dbProperties.getProperty(USERNAME);
			String credPd = dbProperties.getProperty(CREDENTIALS);
			credPd = IREncryptionUtil.decrypt(credPd);
			connection = DriverManager.getConnection(getConnectionString(dbProperties), userName, credPd);

			String getBlog = "SELECT * FROM BLOG_MASTER";
			prepareStatement = connection.prepareStatement(getBlog);
			LOGGER.debug("getComment :" + getBlog);
			rs = prepareStatement.executeQuery();
			while (rs.next()) {
				LOGGER.debug("blog_id: " + rs.getInt("BLOG_ID"));
				int blogId = rs.getInt("BLOG_ID");
				String blogTitle = rs.getString("BLOG_TITLE");
				JSONObject blogs = new JSONObject();
				blogs.put(BLOG_ID, blogId);
				blogs.put("Title", (blogTitle));
				arrayComments.put(blogs);
			}
			rs.close();
		} catch (SQLException e) {
			LOGGER.error("getBlogs()", e);

		} finally {
			releaseConnection(connection, prepareStatement, rs);
		}
		return arrayComments;
	}

	private boolean updateReviewData(JSONObject data) throws IOException {
		LOGGER.debug("BlogTask : insertBlogData");
		String path = data.getString("path");
		Properties dbProperties = loadProperties(path);
		Connection connection = null;
		boolean isDataUpdated = false;
		try {
			String userName = dbProperties.getProperty(USERNAME);
			String credPd = dbProperties.getProperty(CREDENTIALS);
			credPd = IREncryptionUtil.decrypt(credPd);
			connection = DriverManager.getConnection(getConnectionString(dbProperties), userName, credPd);
			LOGGER.debug("BlogTask : after getConnection");
			int result = updateCommentData(connection, data);
			LOGGER.info("insertBlogData result : " + result);
			if (result > 0) {
				LOGGER.info("Blog Master Data Inserted");
				isDataUpdated = true;
			} else {
				LOGGER.info("Blog master insert failed");
			}

		} catch (Exception e) {
			LOGGER.error("Exception in Update comment data catch block : ", e);
		} finally {
			releaseConnection(connection, null, null);
			LOGGER.info("Released Update comment data connection");
		}
		return isDataUpdated;
	}

	/**
	 * This method updates the comment status for approved/rejected
	 * 
	 * @param connection
	 * @param data
	 * @return
	 * @throws IOException
	 */
	private int updateCommentData(Connection connection, JSONObject data) throws IOException {
		PreparedStatement preparedStatement = null;
		int result = 0;
		String userEmailID = "";
		XssUtils xssUtils = new XssUtils();
		try {
			String profilePath = data.getString("path");
			String blogPropPath = data.getString("blogpath");
			long commentId = data.getLong("commentId");
			String status = xssUtils.stripXSS(data.getString("status"));
			String query = "UPDATE BLOG_COMMENT SET STATUS = ?, STATUS_UPDATED_ON = LOCALTIMESTAMP "
					+ "WHERE COMMENT_ID = ? ";
			LOGGER.info("Query : " + query);
			preparedStatement = connection.prepareStatement(query);

			preparedStatement.setString(1, status);
			preparedStatement.setLong(2, commentId);
			result = preparedStatement.executeUpdate();
			LOGGER.info("update comment result : " + result);
			String blogTitle = getBlogTitle(commentId, profilePath);
			userEmailID = getUserEmail(commentId, profilePath);
			if (!userEmailID.equals("")) {
				sentMailNotification(status, blogTitle, userEmailID, blogPropPath);
			}

		} catch (NumberFormatException | SQLException e) {
			LOGGER.error("Exception in updateBlogMasterData: ", e);
		} finally {
			releaseConnection(connection, preparedStatement, null);
			LOGGER.info("Released updateBlogMasterData connection");
		}
		return result;
	}

	/**
	 * This method returns all the comments submitted for specific blog based on
	 * blogID
	 * 
	 * @param data
	 * @return
	 * @throws IOException
	 */
	private JSONArray getCommentbyBlogId(JSONObject data) throws IOException {
		LOGGER.info("getCommentbyBlogId");
		int blogId = data.getInt(BLOG_ID);
		String propFilePath = data.getString("path");
		PreparedStatement prepareStatement = null;
		ResultSet rs = null;
		Properties dbProperties = loadProperties(propFilePath);
		Connection connection = null;
		JSONArray arrayComments = new JSONArray();
		String getComment = "";
		try {
			String userName = dbProperties.getProperty(USERNAME);
			String credPd = dbProperties.getProperty(CREDENTIALS);
			credPd = IREncryptionUtil.decrypt(credPd);
			connection = DriverManager.getConnection(getConnectionString(dbProperties), userName, credPd);
			if (blogId > 0) {
				getComment = "SELECT COMMENT_ID, COMMENT, USER_NAME, COMMENTED_ON,BLOG_URL,USER_IP_ADDRESS  "
						+ "FROM BLOG_COMMENT WHERE BLOG_ID = ? AND STATUS = ? ORDER BY COMMENT_ID ";
				prepareStatement = connection.prepareStatement(getComment);
				prepareStatement.setLong(1, blogId);
				prepareStatement.setString(2, "Pending");
			} else {
				getComment = "SELECT COMMENT_ID, COMMENT, USER_NAME, COMMENTED_ON,BLOG_URL,USER_IP_ADDRESS  "
						+ "FROM BLOG_COMMENT WHERE STATUS = ? ORDER BY COMMENT_ID ";
				prepareStatement = connection.prepareStatement(getComment);
				prepareStatement.setString(1, "Pending");
			}

			LOGGER.debug("getComment :" + getComment);
			rs = prepareStatement.executeQuery();

			while (rs.next()) {
				LOGGER.debug("COMMENT_ID: " + rs.getInt("COMMENT_ID"));
				int commentId = rs.getInt("COMMENT_ID");
				String commentStr = rs.getString("COMMENT");
				String username = rs.getString("USER_NAME");
				String commentOn = rs.getString("COMMENTED_ON");
				String blogUrl = rs.getString("BLOG_URL");
				String ip = rs.getString("USER_IP_ADDRESS");
				JSONObject comments = new JSONObject();
				comments.put("CommentId", commentId);
				comments.put("Comment", commentStr);
				comments.put("UserName", username);
				comments.put("CommentOn", commentOn);
				comments.put("BlogURL", blogUrl);
				comments.put("IP", ip);
				arrayComments.put(comments);
			}
			rs.close();

		} catch (SQLException e) {
			LOGGER.error(GET_COMMENT_BLOGID, e);

		} finally {
			releaseConnection(connection, prepareStatement, rs);
		}
		return arrayComments;
	}

	/**
	 * This method returns the blog title for comment approved/rejected
	 * 
	 * @param commentid
	 * @param path
	 * @return
	 * @throws IOException
	 */
	private String getBlogTitle(long commentid, String path) throws IOException {
		LOGGER.info("getBlogId");

		PreparedStatement prepareStatement = null;
		ResultSet rs = null;
		Properties dbProperties = loadProperties(path);
		Connection connection = null;
		String titleQuery = "";
		String blogTitle = "";
		try {
			String userName = dbProperties.getProperty(USERNAME);
			String credPd = dbProperties.getProperty(CREDENTIALS);
			credPd = IREncryptionUtil.decrypt(credPd);
			connection = DriverManager.getConnection(getConnectionString(dbProperties), userName, credPd);

			titleQuery = "SELECT BLOG_TITLE FROM BLOG_MASTER WHERE BLOG_ID IN "
					+ "(SELECT BLOG_ID FROM BLOG_COMMENT WHERE COMMENT_ID = ? )";
			prepareStatement = connection.prepareStatement(titleQuery);
			prepareStatement.setLong(1, commentid);
			LOGGER.debug("titleQuery :" + titleQuery);
			rs = prepareStatement.executeQuery();

			while (rs.next()) {

				blogTitle = rs.getString("BLOG_TITLE");
			}
			LOGGER.info("Blog Title " + blogTitle);
			rs.close();

		} catch (SQLException e) {
			LOGGER.error(GET_COMMENT_BLOGID, e);

		} finally {
			releaseConnection(connection, prepareStatement, rs);
		}
		return blogTitle;
	}

	/**
	 * This method returns the user emailid for mail notification for the specific
	 * comment approved/rejected
	 * 
	 * @param commentid
	 * @param path
	 * @return
	 * @throws IOException
	 */
	private String getUserEmail(long commentid, String path) throws IOException {
		LOGGER.info("getUserEmail");

		PreparedStatement prepareStatement = null;
		ResultSet rs = null;
		Properties dbProperties = loadProperties(path);
		Connection connection = null;
		String emailQuery = "";
		String useremailID = "";
		try {
			String userName = dbProperties.getProperty(USERNAME);
			String credPd = dbProperties.getProperty(CREDENTIALS);
			credPd = IREncryptionUtil.decrypt(credPd);
			connection = DriverManager.getConnection(getConnectionString(dbProperties), userName, credPd);

			emailQuery = "SELECT USER_EMAILID FROM BLOG_COMMENT WHERE COMMENT_ID = ? ";
			prepareStatement = connection.prepareStatement(emailQuery);
			prepareStatement.setLong(1, commentid);
			LOGGER.debug("titleQuery :" + emailQuery);
			rs = prepareStatement.executeQuery();

			while (rs.next()) {

				useremailID = rs.getString("USER_EMAILID");
			}
			LOGGER.info("User EmailID " + useremailID);
			rs.close();

		} catch (SQLException e) {
			LOGGER.error(GET_COMMENT_BLOGID, e);

		} finally {
			releaseConnection(connection, prepareStatement, rs);
		}
		return useremailID;
	}

	private String getConnectionString(Properties properties) {
		LOGGER.info("Postgre : getConnectionString()");
		String connectionStr = null;
		String host = properties.getProperty("host");
		String port = properties.getProperty("port");
		String database = properties.getProperty("database");
		String schema = properties.getProperty("schema");

		connectionStr = "jdbc:" + database + "://" + host + ":" + port + "/" + schema;

		LOGGER.debug("Connection String : " + connectionStr);
		return connectionStr;
	}

	/**
	 * This method will be used for closing connection, statement and resultset.
	 *
	 * @param con  Database connection to be closed
	 * @param stmt Statement to be closed
	 * @param rs   ResultSet to be closed
	 *
	 */
	public void releaseConnection(Connection con, Statement stmt, ResultSet rs) {
		LOGGER.info("Postgre : releaseConnection()");
		if (con != null) {
			try {
				con.close();
			} catch (Exception e) {
				LOGGER.error("Postgre : releaseConnection() : connection : ", e);
			}
		}
		if (stmt != null) {
			try {
				stmt.close();
			} catch (Exception e) {
				LOGGER.error("Postgre : releaseConnection() : statement : ", e);
			}
		}
		if (rs != null) {
			try {
				rs.close();
			} catch (Exception e) {
				LOGGER.error("Postgre : releaseConnection() : resultset : ", e);
			}
		}
	}

	/**
	 * This method is used to send notification to user about status of his comment
	 * submitted for blog
	 * 
	 * @param status
	 * @param blogtitle
	 * @param userEmail
	 * @param path
	 * @throws IOException
	 */
	private void sentMailNotification(String status, String blogtitle, String userEmail, String path)
			throws IOException {

		MimeMessage mailMessage;
		LOGGER.debug(" status::" + status);
		LOGGER.debug(" blogtitle::" + blogtitle);
		try {

			mailMessage = createMailMessage(status, blogtitle, userEmail, path);
			Transport.send(mailMessage);
		} catch (MessagingException e) {
			e.printStackTrace();
		}

	}

	/**
	 * This method is used to create message to update user comment status
	 * 
	 * @param status
	 * @param blogtitle
	 * @param toEmail
	 * @param path
	 * @return
	 * @throws MessagingException
	 * @throws IOException
	 */
	private MimeMessage createMailMessage(String status, String blogtitle, String toEmail, String path)
			throws MessagingException, IOException {
		LOGGER.info("createMailMessage: Enter");

		String strBlogName = "<blogname>";
		String strStatus = "<status>";
		Properties propertiesFile = loadProperties(path);
		String from = propertiesFile.getProperty(CONTACT_FROM_MAIL);
		LOGGER.debug("sent To :" + toEmail);

		String host = propertiesFile.getProperty(CONTACT_MAIL_HOST);
		LOGGER.debug("relay IP :" + host);
		String port = propertiesFile.getProperty(CONTACT_MAIL_PORT);
		Properties props = new Properties();
		String subject = "";
		props.put("mail.smtp.host", host);
		props.put("mail.smtp.starttls.enable", STARTTLS_ENABLE);
		props.put("mail.smtp.port", port);
		Session session = Session.getDefaultInstance(props, null);
		session.setDebug(true);
		MimeMessage msg = new MimeMessage(session);
		msg.setFrom(new InternetAddress(from));
		msg.setRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));

		subject = propertiesFile.getProperty("statusmessageSubject");
		LOGGER.debug("subject :" + subject);
		StringBuilder sb = new StringBuilder();
		sb.append(propertiesFile.getProperty("statusMessageBody").replace(strStatus, status));

		LOGGER.debug("SuccessMessageBody :" + sb.toString());

		LOGGER.debug("before subject :");
		msg.setSubject(subject.replace(strBlogName, blogtitle));
		LOGGER.debug("after subject :");
		msg.setContent(sb.toString(), "text/html;Charset=UTF-8");

		LOGGER.debug("msg:" + sb.toString());
		return msg;
	}

	/**
	 * This method will be used to load the configuration properties.
	 *
	 * @param context The parameter context object passed from Component.
	 * @throws IOException
	 * @throws MalformedURLException
	 *
	 */
	private Properties loadProperties(final String Propfilepath) throws IOException {
		LOGGER.info("Loading Properties File from Request Context.");
		Properties propFile = new Properties();
		if (Propfilepath != null && !Propfilepath.equals("")) {
			String root = Propfilepath;
			try (InputStream inputStream = new FileInputStream(root)) {

				propFile.load(inputStream);
				LOGGER.info("Properties File Loaded");

			} catch (MalformedURLException e) {
				LOGGER.error("Malformed URL Exception while loading Properties file : ", e);
			}

		} else {
			LOGGER.info("Invalid / Empty properties file name.");
		}
		LOGGER.info("Finish Loading Properties File.");
		return propFile;
	}

}
