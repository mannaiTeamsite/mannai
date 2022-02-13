package com.hukoomi.livesite.servlet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.hukoomi.utils.MySqlForServlet;
import com.hukoomi.utils.PostgreForServlet;

public class NewsletterUnsubscription extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8104292216889008294L;

	/** Logger object to check the flow of the code. */
	private static final Logger logger = Logger.getLogger(NewsletterUnsubscription.class);

	/**
	 * Constant for status success.
	 */
	public static final String STATUS_SUCCESS = "success";

	/**
	 * Constant for table name.
	 */
	public static final String NEWSLETTER_MASTER = "newsletter_master";

	/**
	 * Constant for table name.
	 */
	public static final String NEWSLETTER_PREFERENCE = "newsletter_preference";

	/**
	 * phpList subscriber email
	 */
	private static final String SUBSCRIBER_EMAIL = "subscriber_email";

	/**
	 * phpList subscriber id
	 */
	private static final String SUBSCRIBER_ID = "subscriber_id";

	/**
	 * phpList unsubscription reason
	 */
	private static final String UNSUBSCRIBE_REASON = "unsubreason";

	/** inActive status constant. */
	private static final String STATUS_INACTIVE = "InActive";
	/** Unsubscribed status constant. */
	private static final String STATUS_UNSUBSCRIBED = "Unsubscribed";

	/** confirmed status constant. */
	private static final String STATUS_CONFIRMED = "Confirmed";

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		logger.info("NewsletterUnsubscription : doGet()");
		PostgreForServlet postgre = new PostgreForServlet();
		MySqlForServlet mysql = new MySqlForServlet();

		double subscriberID;
		String subscriberemail = "";

		String token = request.getParameter("token");
		String pageLang = request.getParameter("lang");
		String unsubReason = request.getParameter(UNSUBSCRIBE_REASON);
		String status = "";
		logger.debug("token " + token + " lang " + pageLang + " unsubReason " + unsubReason);
		RequestDispatcher rd = request.getRequestDispatcher("/portal-" + pageLang + "/home.page");

		subscriberID = getSubscriberID(token, postgre);
		subscriberemail = getSubscriberEmail(subscriberID, postgre);

		logger.debug("NewsletterUnsubscription subscriberID " + subscriberID + " subscriberemail " + subscriberemail);
		if (token != null) {
			logger.debug("token not null");
			status = unsubscribeDashboardUser(token, unsubReason, subscriberID, subscriberemail, postgre, mysql);
			logger.debug("NewsletterUnsubscription : unsubscription in DB completed" + status);
			if (status.equals(STATUS_SUCCESS)) {
				logger.debug("NewsletterUnsubscription : unsubscription in DB completed");

				Cookie confirmationCookie = new Cookie("unsubscriptionStatus",
						"unSubscriptionSuccess:" + subscriberemail);
				confirmationCookie.setHttpOnly(true);
				response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1.
				response.setHeader("Pragma", "no-cache"); // HTTP 1.0.
				response.setDateHeader("Expires", 0);
				response.addCookie(confirmationCookie);
			}

		} else {
			logger.debug("token null");
			Cookie confirmationCookie = new Cookie("unsubscriptionStatus", "unsubscribe");
			confirmationCookie.setHttpOnly(true);
			response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1.
			response.setHeader("Pragma", "no-cache"); // HTTP 1.0.
			response.setDateHeader("Expires", 0);
			response.addCookie(confirmationCookie);

		}
		try {
			rd.forward(request, response);
		} catch (Exception e) {
			logger.info(e);
		}
	}

	/**
	 * @author Pramesh
	 * @param userId
	 * @return
	 * 
	 *         This method unssubscribes dashboard user and update status
	 */
	private String unsubscribeDashboardUser(String token, String unsubReason, double subscriberID, String email,
			PostgreForServlet postgre, MySqlForServlet mysql) {
		logger.info("NewsletterUnsubscription : unsubscribeDashboardUser()");

		String unsubStatus = "";
		String syncStatus = "";
		String status = "";

		String updateMasterQuery = "UPDATE NEWSLETTER_MASTER SET STATUS = ? , UNSUBSCRIBED_REASON = ? WHERE SUBSCRIBER_ID = ?";
		String updatePreferenceQuery = "UPDATE NEWSLETTER_PREFERENCE SET STATUS = ? WHERE SUBSCRIBER_ID = ?";
		String updatePhpQuery = "UPDATE PHPLIST_USER_USER SET BLACKLISTED = ? WHERE EMAIL = ?";
		status = unsubscribePostgreUser(updateMasterQuery, subscriberID, unsubReason, STATUS_UNSUBSCRIBED,
				NEWSLETTER_MASTER, postgre);
		logger.debug("unsubscribeDashboardUser() : update master status " + status);
		if (status.equals(STATUS_SUCCESS)) {
			status = unsubscribePostgreUser(updatePreferenceQuery, subscriberID, unsubReason, STATUS_INACTIVE,
					NEWSLETTER_PREFERENCE, postgre);
		}
		logger.debug("unsubscribeDashboardUser() : update preference status " + status);

		if (status.equals(STATUS_SUCCESS)) {

			unsubStatus = unsubscribePhpUser(updatePhpQuery, email, unsubReason, mysql);
		}

		if (unsubStatus.equals(STATUS_SUCCESS)) {

			String syncPhpQuery = "UPDATE PHPLIST_USER_USER SET BLACKLISTED = ? WHERE EMAIL = ?";
			String syncPhpBlacklist = "INSERT INTO PHPLIST_USER_BLACKLIST (EMAIL, ADDED) VALUES (?,LOCALTIMESTAMP)";
			String syncPhpBlacklistData = "INSERT INTO PHPLIST_USER_BLACKLIST_DATA (EMAIL, NAME, DATA) VALUES (?,?,?)";

			syncStatus = syncUnsubscribeData(syncPhpQuery, email, unsubReason, "USERDATA", postgre);

			if (syncStatus.equals(STATUS_SUCCESS)) {

				syncStatus = syncUnsubscribeData(syncPhpBlacklist, email, unsubReason, "USERBLACKLIST", postgre);
			}
			if (syncStatus.equals(STATUS_SUCCESS)) {

				syncStatus = syncUnsubscribeData(syncPhpBlacklistData, email, unsubReason, "BLACKLISTDATA", postgre);
			}

		}
		if (syncStatus.equals(STATUS_SUCCESS)) {
			updateTokenStatus(token, STATUS_CONFIRMED, postgre);
		}

		return unsubStatus;
	}

	/**
	 * @author Pramesh
	 * @param queryVal
	 * @param userId
	 * @param tabName
	 * @return
	 * 
	 *         This method is used to update unsubscribed user
	 */
	private String unsubscribePostgreUser(String queryVal, double subscriberId, String unsubReason, String status,
			String tabName, PostgreForServlet postgre) {
		logger.info("NewsletterUnsubscription : unsubscribeMasterUser()");

		String rsstatus = "";

		Connection connection = null;
		PreparedStatement prepareStatement = null;
		ResultSet rs = null;

		try {
			connection = postgre.getConnection();
			prepareStatement = connection.prepareStatement(queryVal);

			if (tabName.equals(NEWSLETTER_PREFERENCE)) {

				prepareStatement.setString(1, status);
				prepareStatement.setDouble(2, subscriberId);
			} else {

				prepareStatement.setString(1, status);
				prepareStatement.setString(2, unsubReason);
				prepareStatement.setDouble(3, subscriberId);

			}

			int count = prepareStatement.executeUpdate();

			if (count > 0) {
				rsstatus = STATUS_SUCCESS;
			}

		} catch (Exception e) {
			logger.error("Exception in unsubscribePostgreUser", e);
		} finally {
			postgre.releaseConnection(connection, prepareStatement, rs);
		}

		return rsstatus;
	}

	/**
	 * @author Pramesh
	 * @param queryVal
	 * @param userId
	 * @return
	 */
	private String unsubscribePhpUser(String queryVal, String email, String unsubReason, MySqlForServlet mysql) {
		logger.info("NewsletterUnsubscription : unsubscribePhpUser() " + unsubReason);

		String status = "";
		int blacklistID = 1;
		Connection connection = null;
		PreparedStatement prepareStatement = null;
		ResultSet rs = null;
		String updatePhpBlacklist = "INSERT INTO PHPLIST_USER_BLACKLIST (EMAIL, ADDED) VALUES (?,LOCALTIMESTAMP)";
		String updatePhpBlacklistData = "INSERT INTO PHPLIST_USER_BLACKLIST_DATA (EMAIL, NAME, DATA) VALUES (?,?,?)";
		try {
			connection = mysql.getConnection();
			prepareStatement = connection.prepareStatement(queryVal);

			prepareStatement.setInt(1, blacklistID);
			prepareStatement.setString(2, email);

			int count = prepareStatement.executeUpdate();

			if (count > 0) {
				status = updatePhpBlacklist(updatePhpBlacklist, email, "", mysql);
				if (status.equals(STATUS_SUCCESS)) {

					status = updatePhpBlacklist(updatePhpBlacklistData, email, unsubReason, mysql);
				}
			}

		} catch (Exception e) {
			logger.error("Exception in unsubscribePhpUser", e);
		} finally {
			mysql.releaseConnection(connection, prepareStatement, rs);
		}

		return status;
	}

	/**
	 * @author Pramesh
	 * @param queryVal
	 * @param userId
	 * @return
	 */
	private String updatePhpBlacklist(String queryVal, String email, String unsubReason, MySqlForServlet mysql) {
		logger.info("NewsletterUnsubscription : updatePhpBlacklist() ");

		String status = "";
		Connection connection = null;
		PreparedStatement prepareStatement = null;
		ResultSet rs = null;

		try {
			connection = mysql.getConnection();
			prepareStatement = connection.prepareStatement(queryVal);
			if (!"".equals(unsubReason)) {
				prepareStatement.setString(1, email);
				prepareStatement.setString(2, "reason");
				prepareStatement.setString(3, unsubReason);

			} else {
				prepareStatement.setString(1, email);
			}

			int count = prepareStatement.executeUpdate();

			if (count > 0) {
				status = STATUS_SUCCESS;
			}

		} catch (Exception e) {
			logger.error("Exception in updatePhpBlacklist", e);
		} finally {
			mysql.releaseConnection(connection, prepareStatement, rs);
		}

		return status;
	}

	private String syncUnsubscribeData(String queryVal, String email, String unsubReason, String param,
			PostgreForServlet postgre) {
		logger.info("NewsletterUnsubscription : syncUnsubscribeData() ");

		String status = "";
		int blacklistID = 1;
		Connection connection = null;
		PreparedStatement prepareStatement = null;
		ResultSet rs = null;

		try {
			connection = postgre.getConnection();
			prepareStatement = connection.prepareStatement(queryVal);

			if (param.equals("USERDATA")) {

				prepareStatement.setInt(1, blacklistID);
				prepareStatement.setString(2, email);
			} else if (param.equals("USERBLACKLIST")) {

				prepareStatement.setString(1, email);

			} else if (param.equals("BLACKLISTDATA")) {

				prepareStatement.setString(1, email);
				prepareStatement.setString(2, "reason");
				prepareStatement.setString(3, unsubReason);

			}

			int count = prepareStatement.executeUpdate();

			if (count > 0) {

				status = STATUS_SUCCESS;
			}

		} catch (Exception e) {
			logger.error("Exception in unsubscribePhpUser", e);
		} finally {
			postgre.releaseConnection(connection, prepareStatement, rs);
		}

		return status;
	}

	/**
	 * @author pramesh
	 * @param subscriberId
	 * @return
	 * 
	 *         This method get the subscriber email based on subscriber id
	 */
	private String getSubscriberEmail(double subscriberId, PostgreForServlet postgre) {
		logger.info("NewsletterUnsubscription : getSubscriberEmail");

		String addSubscriberPreferencesQuery = "SELECT SUBSCRIBER_EMAIL FROM NEWSLETTER_MASTER WHERE SUBSCRIBER_ID = ?";
		Connection connection = null;
		PreparedStatement prepareStatement = null;
		String subscriberEmail = "";
		ResultSet rs = null;
		try {
			connection = postgre.getConnection();
			prepareStatement = connection.prepareStatement(addSubscriberPreferencesQuery);
			prepareStatement.setDouble(1, subscriberId);

			rs = prepareStatement.executeQuery();
			while (rs.next()) {

				subscriberEmail = rs.getString(SUBSCRIBER_EMAIL);
			}
		} catch (Exception e) {
			logger.error("Exception in updateMasterTable", e);
		} finally {
			postgre.releaseConnection(connection, prepareStatement, rs);
		}

		return subscriberEmail;
	}

	/**
	 * @author Pramesh
	 * @param userId
	 * @return This method is used to fetch subscriberID using UID
	 */
	private double getSubscriberID(String token, PostgreForServlet postgre) {
		logger.info("NewsletterUnsubscription : getSubscriberID()");

		String subscriberIDQuery = "SELECT SUBSCRIBER_ID FROM NEWSLETTER_CONFIRMATION_TOKEN WHERE TOKEN = ?";

		double subscriberID = 0;
		Connection connection = null;
		PreparedStatement prepareStatement = null;
		ResultSet rs = null;

		try {
			connection = postgre.getConnection();
			prepareStatement = connection.prepareStatement(subscriberIDQuery);
			prepareStatement.setString(1, token);
			rs = prepareStatement.executeQuery();

			while (rs.next()) {
				subscriberID = rs.getDouble(SUBSCRIBER_ID);
			}

			logger.debug("NewsletterUnsubscription subscriberID : " + subscriberID);

		} catch (Exception e) {
			logger.error("Exception in subscriberID", e);
		} finally {
			postgre.releaseConnection(connection, prepareStatement, rs);
		}
		return subscriberID;
	}

	/**
	 * @author pramesh
	 * @param token
	 * @param statusConfirmed
	 * @return
	 */
	private boolean updateTokenStatus(String token, String status, PostgreForServlet postgre) {
		logger.info("NewsletterConfirmation : updateTokenStatus()");
		boolean updateTokenStatus = false;
		String updateTokenStatusQuery = "UPDATE NEWSLETTER_CONFIRMATION_TOKEN SET "
				+ "CONFIRMATION_STATUS = ? WHERE TOKEN = ?";
		Connection connection = null;
		PreparedStatement prepareStatement = null;

		try {
			connection = postgre.getConnection();
			prepareStatement = connection.prepareStatement(updateTokenStatusQuery);
			prepareStatement.setString(1, status);
			prepareStatement.setString(2, token);

			int result = prepareStatement.executeUpdate();
			if (result != 0) {
				logger.info("Token Status Updated !");
				updateTokenStatus = true;
			} else {
				logger.info("Token Status Not Updated !");
			}
		} catch (Exception e) {
			logger.error("Exception in updateTokenStatus", e);
		} finally {
			postgre.releaseConnection(connection, prepareStatement, null);
		}

		return updateTokenStatus;
	}

}