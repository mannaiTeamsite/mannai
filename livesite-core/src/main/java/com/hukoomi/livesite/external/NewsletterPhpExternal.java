package com.hukoomi.livesite.external;

import java.io.IOException;
import java.net.MalformedURLException;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.ValidationErrorList;

import com.hukoomi.utils.ESAPIValidator;
import com.hukoomi.utils.GoogleRecaptchaUtil;
import com.hukoomi.utils.MySql;
import com.hukoomi.utils.Postgre;
import com.hukoomi.utils.PropertiesFileReader;
import com.hukoomi.utils.XssUtils;
import com.interwoven.livesite.file.FileDal;
import com.interwoven.livesite.runtime.RequestContext;

/**
 * @author Arbaj
 *
 */
public class NewsletterPhpExternal {

	/** Logger object to check the flow of the code. */
	private static final Logger logger = Logger.getLogger(NewsletterPhpExternal.class);

	/** field validation status. */
	private static final String STATUS_FIELD_VALIDATION = "FieldValidationFailed";

	/** element for document. */
	private static final String ELEMENT_EMAIL = "email";

	/** external parameter. */
	private static final String PERSONA = "persona";

	/** element for document. */
	private static final String ELEMENT_RESULT = "Result";

	/** element for document. */
	private static final String ELEMENT_STATUS = "status";

	/** pending status constant. */
	private static final String STATUS_PENDING = "Pending";

	/** subscribed status constant. */
	private static final String STATUS_SUBSCRIBED = "Subscribed";

	/** confirmation sent status constant. */
	private static final String CONFIRMATION_SENT = "ConfirmationSent";

	/** confirmation sent status constant. */
	private static final String CONFIRMATION_PENDING = "ConfirmationPending";

	/** confirmed status constant. */
	private static final String STATUS_CONFIRMED = "Confirmed";

	/** active status constant. */
	private static final String STATUS_ACTIVE = "Active";

	/** already pending response status. */
	private static final String STATUS_ALREADY_PENDING = "AlreadyPending";
	/** already pending response status. */
	private static final String SUBSCRIPTION_LANG = "subscriptionLang";

	/** preference updated response status. */
	private static final String STATUS_ALREADY_SUBSCRIBED = "AlreadySubscribed";

	/** preference updated response status. */
	private static final String STATUS_ALREADY_UNSUBSCRIBED = "AlreadyUnsubscribed";

	/** initialization of error variable. */
	private static final String STATUS_ERROR_RECAPTHCHA = "errorInRecaptcha";

	/** confirmation mail element. */
	private static final String CONFIRMATION_EMAIL = "ConfirmationEmail";
	/**
	 * Unsubscribe confirmation mail element.
	 */
	private static final String UNSUBSCRIPTION_CONFIRMATION_EMAIL = "UnsubConfirmationEmail";

	/** Contact us properties key. */
	private static final String CONTACT_FROM_MAIL = "sentFrom";

	/** Contact us properties key. */
	private static final String CONTACT_MAIL_HOST = "host";

	/** Contact us properties key. */
	private static final String CONTACT_MAIL_PORT = "port";

	/** mail properties key. */
	private static final String STARTTLS_ENABLE = "false";

	/** character set Constant */
	private static final String CHAR_SET = "UTF-8";

	/** Initialising the filepath for Properties file inside WorkArea. */
	private static final String NEWSLETTER_TEMPLATE_PATH = "/iw/config/newsletter-templates/";

	/** MySql Object variable. */
	MySql mysql = null;

	/** Postgre Object variable. */
	Postgre postgre = null;

	/**
	 * Constant for action update unsubscribe settings.
	 */
	public static final String ACTION_UNSUBSCRIBE_NONLOGGED = "unsubscribeNonLogged";

	/**
	 * Parameter to get subscriberID.
	 */
	private static final String USING_EMAIL = "usingEmail";
	/**
	 * Constant for action update persona settings.
	 */
	public static final String ACTION_UNSUBSCRIBE = "unsubscribe";
	/**
	 * Constant for status not subscried.
	 */
	public static final String STATUS_NOT_SUBSCRIBED = "notSubscribed";

	/**
	 * Constant for status unsubscribed.
	 */
	public static final String STATUS_UNSUBSCRIBED = "Unsubscribed";

	/**
	 * Constant for status success.
	 */
	public static final String STATUS_SUCCESS = "success";

	/**
	 * Constant for settings-action.
	 */
	public static final String TOPICS = "topics";

	/**
	 * Parameter to get subscriberID..
	 */
	private static final String USING_UID = "usingUID";

	/**
	 * Constant for status.
	 */
	public static final String STATUS = "status";

	/**
	 * Constant for user-id.
	 */
	public static final String USER_ID = "user-id";

	/**
	 * Constant for error.
	 */
	public static final String ERROR = "error";

	/**
	 * UID variable.
	 */
	private String uid = null;

	@SuppressWarnings("deprecation")
	public Document subscribeToNewsletter(final RequestContext context) {
		logger.info("NewsletterPhpExternal : subscribeToNewsletter()");

		HttpSession session = context.getRequest().getSession();

		postgre = new Postgre(context);

		Document doc = DocumentHelper.createDocument();
		Element responseElem = doc.addElement("dashboard-settings");
		XssUtils xssUtils = new XssUtils();

		

		if (session.getAttribute(STATUS) == "valid") {
			uid = (String) session.getAttribute("uid");
		}
		String email = xssUtils.stripXSS(context.getParameterString(ELEMENT_EMAIL));
		String subscriptionLang = xssUtils.stripXSS(context.getParameterString(SUBSCRIPTION_LANG));
		String persona = xssUtils.stripXSS(context.getParameterString(PERSONA));
		if (uid != null && !uid.equals("")) {

			persona = getPersonaForUser(uid, postgre);
			logger.debug("NewsletterPhpExternal : dashboard persona " + persona);
		}

		if (persona == null) {
			persona = "general";
		}

		logger.info("User Details From Front End :-> email : " + email + " subcription language : " + subscriptionLang
				+ " persona : " + persona);
		/// Added code for unsubscription start
		final String SETTINGS_ACTION = "settingsAction";
		String settingsAction = context.getParameterString(SETTINGS_ACTION);
		
		String subStatus = "";

		if (ACTION_UNSUBSCRIBE_NONLOGGED.equalsIgnoreCase(settingsAction)) {
						
			boolean bool = isEmailAlreadyExist(email);
			if (bool) {

				subStatus = getSubscriptionStatus(email, ACTION_UNSUBSCRIBE);
				if (subStatus != null && !subStatus.equals("")) {			
					subscribeNewsLetter(context, subStatus, responseElem);	
				}

			} else {

				createTopicsResponseDoc(responseElem, null, null, "", STATUS_NOT_SUBSCRIBED, "");
			}
			return doc;
		}

		/// Added code for unsubscription end

		Document memberdetail = unsubscribeNewsLetter(context,  persona);

		if (memberdetail != null) {
			logger.info("Newsletter final document : " + memberdetail.asXML());
		}

		return memberdetail;
	}
	@SuppressWarnings("deprecation")
	public void subscribeNewsLetter(RequestContext context, String subStatus, Element responseElem) {
		XssUtils xssUtils = new XssUtils();
		double subscriberId = 0;
		double preferenceId = 0;
		String email = xssUtils.stripXSS(context.getParameterString(ELEMENT_EMAIL));
		subscriberId = getSubscriberID(email, USING_EMAIL);
		String pageLang = xssUtils.stripXSS(context.getParameterString("lang"));
		String unsubreason = context.getParameterString("unsubscribe_reason");
		if (subStatus.equals(STATUS_SUBSCRIBED)) {

			String confirmationToken = generateConfirmationToken(subscriberId, preferenceId, email);
			sendConfirmationMail(email, pageLang, confirmationToken, UNSUBSCRIPTION_CONFIRMATION_EMAIL,
					unsubreason, context);
			createTopicsResponseDoc(responseElem, null, null, "", STATUS_SUCCESS, "");

		} else if (subStatus.equals(STATUS_PENDING)) {

			createTopicsResponseDoc(responseElem, null, null, "", STATUS_PENDING, "");
		} else if (subStatus.equals(STATUS_UNSUBSCRIBED)) {
			createTopicsResponseDoc(responseElem, null, null, "", STATUS_UNSUBSCRIBED, "");

		}
	}
	@SuppressWarnings("deprecation")
	public Document unsubscribeNewsLetter(RequestContext context, String persona) {
		XssUtils xssUtils = new XssUtils();
		Document memberdetail = null;
		boolean verify = false;
		
		String email = xssUtils.stripXSS(context.getParameterString(ELEMENT_EMAIL));
		String subscriptionLang = xssUtils.stripXSS(context.getParameterString(SUBSCRIPTION_LANG));
		String gRecaptchaResponse = context.getParameterString("captcha");

		if (validateLanguage(subscriptionLang) && validateMailID(xssUtils.stripXSS(email))) {
			if (gRecaptchaResponse != null && !gRecaptchaResponse.equals("")) {
				GoogleRecaptchaUtil captchUtil = new GoogleRecaptchaUtil();
				verify = captchUtil.validateCaptcha(context, gRecaptchaResponse);
				logger.debug("Recapcha verification status:" + verify);
			} else {
				verify = true;
			}

			if (verify) {
				
				memberdetail = unsubscribe(context, memberdetail, persona);
				
			} else {
				memberdetail = getDocument(email, STATUS_ERROR_RECAPTHCHA);
			}
		} else {
			memberdetail = getDocument(email, STATUS_FIELD_VALIDATION);
		}
		return memberdetail;
	}
	@SuppressWarnings("deprecation")
	public Document unsubscribe(RequestContext context, Document memberdetail, String persona) {
		XssUtils xssUtils = new XssUtils();
		double subscriberId = 0;
		double preferenceId = 0;
		String email = xssUtils.stripXSS(context.getParameterString(ELEMENT_EMAIL));
		String subscriptionLang = xssUtils.stripXSS(context.getParameterString(SUBSCRIPTION_LANG));
		String pageLang = xssUtils.stripXSS(context.getParameterString("lang"));

		if (uid != null && !uid.equals("") && getSubscriptionStatusByUid(uid)) {
			memberdetail = getDocument(email, STATUS_ALREADY_SUBSCRIBED);
		} else if (!isEmailAlreadyExist(email)) {
			subscriberId = generateSubscriberId();
			boolean subscriberMasterDataInsert = addSubscriberInMasterTable(uid, subscriberId, email,
					STATUS_PENDING);
			boolean subscriberPreferenceDataInsert = addSubscriberPreferences(subscriberId, subscriptionLang,
					persona);
			preferenceId = getPreferenceId(subscriberId, subscriptionLang, persona);
			String confirmationToken = generateConfirmationToken(subscriberId, preferenceId, email);
			if (subscriberMasterDataInsert && subscriberPreferenceDataInsert) {
				sendConfirmationMail(email, pageLang, confirmationToken, CONFIRMATION_EMAIL, "", context);
				memberdetail = getDocument(email, CONFIRMATION_SENT);
			}

		} else if (isEmailAlreadyExist(email)) {
			
			// Check Confirmation Status					
			checkConfirmStatus(memberdetail, context, persona);					
		}
		
		return memberdetail;
	}
	
	@SuppressWarnings("deprecation")
	public Document checkConfirmStatus(Document memberdetail, RequestContext context, String persona) {

		// Check Confirmation Status
		double subscriberId = 0;
		double preferenceId = 0;
		XssUtils xssUtils = new XssUtils();
		String email = xssUtils.stripXSS(context.getParameterString(ELEMENT_EMAIL));
		String subscriptionLang = xssUtils.stripXSS(context.getParameterString(SUBSCRIPTION_LANG));
		String confirmationStatus = checkConfirmationStatus(email);
		logger.info("Confirmation Status received from Db : " + confirmationStatus);
		if (STATUS_CONFIRMED.equals(confirmationStatus)) {
			String subscriptionStatus = getSubscriptionStatus(email);
			if (STATUS_PENDING.equals(subscriptionStatus)) {
				memberdetail = getDocument(email, STATUS_ALREADY_PENDING);
			} else if (STATUS_UNSUBSCRIBED.equals(subscriptionStatus)) {
				memberdetail = getDocument(email, STATUS_ALREADY_UNSUBSCRIBED);
			} else {
				String preferenceStatus = checkPrefernceStatus(email, persona, subscriptionLang);
				if (STATUS_ACTIVE.equals(preferenceStatus)) {
					memberdetail = getDocument(email, STATUS_ALREADY_SUBSCRIBED);
				} else if (STATUS_PENDING.equals(preferenceStatus)) {
					memberdetail = getDocument(email, CONFIRMATION_PENDING);
				} else {
					subscriberId = getSubcriberId(email);
					boolean preferenceUpdateStatus = updateSubscriberPreference(subscriberId,
							subscriptionLang, persona, STATUS_PENDING);
					preferenceId = getPreferenceId(subscriberId, subscriptionLang, persona);
					
					memberdetail = updatePreference(context, preferenceUpdateStatus, subscriberId, preferenceId);					
				}
			}
		} else if (STATUS_PENDING.equals(confirmationStatus)) {
			memberdetail = getDocument(email, CONFIRMATION_PENDING);
		}
		return memberdetail;
	}
	@SuppressWarnings("deprecation")
	public Document updatePreference(RequestContext context, boolean preferenceUpdateStatus,double subscriberId, double preferenceId) {
		Document memberdetail= null;
		XssUtils xssUtils = new XssUtils();
		String email = xssUtils.stripXSS(context.getParameterString(ELEMENT_EMAIL));
		String pageLang = xssUtils.stripXSS(context.getParameterString("lang"));
		if (preferenceUpdateStatus) {
			String confirmationToken = generateConfirmationToken(subscriberId, preferenceId,
					email);
			sendConfirmationMail(email, pageLang, confirmationToken, CONFIRMATION_EMAIL, "",
					context);
			logger.info("Confirmation Mail Sent For Preference Update !");
			memberdetail = getDocument(email, CONFIRMATION_SENT);
		} else {
			logger.info("Newsletter Preference Not Updated !");
			memberdetail = getDocument(email, STATUS_ALREADY_SUBSCRIBED);
		}
		return memberdetail;
	}
	/**
	 * @param uid2
	 * @return
	 */
	private boolean getSubscriptionStatusByUid(String uid) {
		logger.info("NewsletterPhpExternal : getSubscriptionStatusByUid()");
		boolean emailsExistStatus = false;

		String emailCheckQuery = "SELECT SUBSCRIBER_EMAIL FROM NEWSLETTER_MASTER WHERE UID = ?";

		Connection connection = null;
		PreparedStatement prepareStatement = null;
		ResultSet resultSet = null;
		try {
			connection = postgre.getConnection();
			prepareStatement = connection.prepareStatement(emailCheckQuery);
			prepareStatement.setString(1, uid);

			resultSet = prepareStatement.executeQuery();

			if (resultSet.next()) {
				logger.info("Email Already Exist !");
				emailsExistStatus = true;
			} else {
				logger.info("Email Doesn't Exist !");
			}
		} catch (Exception e) {
			logger.error("Exception in getSubscriptionStatusByUid", e);
		} finally {
			postgre.releaseConnection(connection, prepareStatement, resultSet);
		}
		return emailsExistStatus;
	}

	/**
	 * @param email
	 * @return
	 */
	private double getSubcriberId(String email) {
		double subscriberId = 0;

		String getSubcriberIdQuery = "SELECT SUBSCRIBER_ID FROM NEWSLETTER_MASTER WHERE SUBSCRIBER_EMAIL = ?";
		Connection connection = null;
		PreparedStatement prepareStatement = null;
		ResultSet resultSet = null;
		try {
			connection = postgre.getConnection();
			prepareStatement = connection.prepareStatement(getSubcriberIdQuery);

			prepareStatement.setString(1, email);
			resultSet = prepareStatement.executeQuery();

			if (resultSet.next()) {
				logger.info("Subscriber Id Available  !");
				subscriberId = resultSet.getDouble(1);
			} else {
				logger.info("Subscriber Id Not Available !");
			}
		} catch (Exception e) {
			logger.error("Exception in getSubcriberId", e);
		} finally {
			postgre.releaseConnection(connection, prepareStatement, resultSet);
		}

		return subscriberId;
	}

	/**
	 * @param subscriber_id
	 * @param subscriptionLang
	 * @param persona
	 * @param statusActive
	 * @return
	 */
	private boolean updateSubscriberPreference(double subscriberId, String subscriptionLang, String persona,
			String status) {
		logger.info("NewsletterPhpExternal : updateSubscriberPreference()");

		boolean subscriberPreferenceDataInsert = false;
		String addSubscriberPreferencesQuery = "INSERT INTO NEWSLETTER_PREFERENCE (SUBSCRIBER_ID, LANGUAGE, PERSONA, STATUS) VALUES(?,?,?,?)";
		Connection connection = null;
		PreparedStatement prepareStatement = null;

		try {
			connection = postgre.getConnection();
			prepareStatement = connection.prepareStatement(addSubscriberPreferencesQuery);
			prepareStatement.setDouble(1, subscriberId);
			prepareStatement.setString(2, subscriptionLang);
			prepareStatement.setString(3, persona);
			prepareStatement.setString(4, status);

			int result = prepareStatement.executeUpdate();
			if (result != 0) {
				logger.info("Newsletter Preference Updated !");
				subscriberPreferenceDataInsert = true;
			} else {
				logger.info("Newsletter Preference Not Updated !");
			}
		} catch (Exception e) {
			logger.error("Exception in updateSubscriberPreference", e);
		} finally {
			postgre.releaseConnection(connection, prepareStatement, null);
		}

		return subscriberPreferenceDataInsert;

	}

	/**
	 * @param email
	 * @param persona
	 * @param subscriptionLang
	 * @return
	 */
	private String checkPrefernceStatus(String email, String persona, String subscriptionLang) {
		logger.info("NewsletterPhpExternal : checkPrefernceStatus()");
		String preferenceStatus = "";
		String tokenCheckQuery = "SELECT NP.STATUS FROM NEWSLETTER_MASTER NM INNER JOIN NEWSLETTER_PREFERENCE NP ON NM.SUBSCRIBER_ID = "
				+ "NP.SUBSCRIBER_ID WHERE NM.SUBSCRIBER_EMAIL = ? AND NP.PERSONA = ? AND NP.LANGUAGE = ?";
		Connection connection = null;
		PreparedStatement prepareStatement = null;
		ResultSet resultSet = null;
		try {
			connection = postgre.getConnection();
			prepareStatement = connection.prepareStatement(tokenCheckQuery);
			prepareStatement.setString(1, email);
			prepareStatement.setString(2, persona);
			prepareStatement.setString(3, subscriptionLang);

			resultSet = prepareStatement.executeQuery();
			while (resultSet.next()) {
				preferenceStatus = resultSet.getString(1);
			}

		} catch (Exception e) {
			logger.error("Exception in checkPrefernceStatus", e);
		} finally {
			postgre.releaseConnection(connection, prepareStatement, resultSet);
		}
		return preferenceStatus;
	}

	/**
	 * @param email
	 */
	private String checkConfirmationStatus(String email) {
		String confirmationStatus = STATUS_PENDING;

		String checkConfirmationStatusQuery = "SELECT COUNT(*) FROM NEWSLETTER_CONFIRMATION_TOKEN WHERE "
				+ "SUBSCRIBER_EMAIL = ? AND CONFIRMATION_STATUS = 'Confirmed'";
		Connection connection = null;
		PreparedStatement prepareStatement = null;
		ResultSet resultSet = null;
		try {
			connection = postgre.getConnection();
			prepareStatement = connection.prepareStatement(checkConfirmationStatusQuery);

			prepareStatement.setString(1, email);
			resultSet = prepareStatement.executeQuery();
			resultSet.next();
			long count = resultSet.getLong(1);
			if (count > 0) {
				confirmationStatus = STATUS_CONFIRMED;
			}

		} catch (Exception e) {
			logger.error("Exception in checkConfirmationStatus", e);
		} finally {
			postgre.releaseConnection(connection, prepareStatement, resultSet);
		}

		return confirmationStatus;

	}

	/**
	 * @param subscriber_id
	 * @param preferenceId
	 * @return
	 */
	public String generateConfirmationToken(double subscriberId, double preferenceId, String email) {
		logger.info("NewsletterPhpExternal : generateConfirmationToken()");
		String confirmationToken = RandomStringUtils.randomAlphanumeric(10);
		logger.debug("NewsletterPhpExternal :ConfirmationToken()" + confirmationToken);
		boolean tokenExist = isConfirmationTokenExist(confirmationToken);

		if (tokenExist) {
			generateConfirmationToken(subscriberId, preferenceId, email);
		} else {
			String addGeneratedTokenQuery = "INSERT INTO NEWSLETTER_CONFIRMATION_TOKEN (TOKEN, SUBSCRIBER_ID, PREFERENCE_ID, GENERATED_DATE, "
					+ "CONFIRMATION_STATUS, SUBSCRIBER_EMAIL) VALUES(?,?,?,LOCALTIMESTAMP,?,?)";
			Connection connection = null;
			PreparedStatement prepareStatement = null;

			try {
				connection = postgre.getConnection();
				prepareStatement = connection.prepareStatement(addGeneratedTokenQuery);

				prepareStatement.setString(1, confirmationToken);
				prepareStatement.setDouble(2, subscriberId);
				prepareStatement.setDouble(3, preferenceId);
				prepareStatement.setString(4, STATUS_PENDING);
				prepareStatement.setString(5, email);
				int result = prepareStatement.executeUpdate();

				if (result != 0) {
					logger.info("Token Generated and Added !");
				} else {
					logger.info("Token Not Generated and Not Added !");
				}
			} catch (Exception e) {
				logger.error("Exception in generateConfirmationToken", e);
			} finally {
				postgre.releaseConnection(connection, prepareStatement, null);
			}
		}

		return confirmationToken;
	}

	/**
	 * @param confirmationToken
	 * @return
	 */
	private boolean isConfirmationTokenExist(String confirmationToken) {
		logger.info("NewsletterPhpExternal : isConfirmationTokenExist()" + confirmationToken);
		boolean tokenExist = false;
		String tokenCheckQuery = "SELECT TOKEN FROM NEWSLETTER_CONFIRMATION_TOKEN WHERE TOKEN = ?";
		Connection connection = null;
		PreparedStatement prepareStatement = null;
		ResultSet resultSet = null;
		try {
			connection = postgre.getConnection();
			prepareStatement = connection.prepareStatement(tokenCheckQuery);
			prepareStatement.setString(1, confirmationToken);

			resultSet = prepareStatement.executeQuery();

			if (resultSet.next()) {
				logger.info("Token already Exist !");
				tokenExist = true;
			} else {
				logger.info("Token Doesn't Exist !");
			}
		} catch (Exception e) {
			logger.error("Exception in isConfirmationTokenExist", e);
		} finally {
			postgre.releaseConnection(connection, prepareStatement, resultSet);
		}
		return tokenExist;
	}

	/**
	 * @param email
	 * @return
	 */
	public String getSubscriptionStatus(String email) {
		logger.info("NewsletterPhpExternal : getSubscriptionStatus()");
		String subscriptionStatus = null;
		String getSubscriptionStatusQuery = "SELECT STATUS FROM NEWSLETTER_MASTER WHERE SUBSCRIBER_EMAIL = ?";
		Connection connection = null;
		PreparedStatement prepareStatement = null;
		ResultSet resultSet = null;
		try {
			connection = postgre.getConnection();
			prepareStatement = connection.prepareStatement(getSubscriptionStatusQuery);
			prepareStatement.setString(1, email);

			resultSet = prepareStatement.executeQuery();
			resultSet.next();
			subscriptionStatus = resultSet.getString(1);

		} catch (Exception e) {
			logger.error("Exception in getSubscriptionStatus", e);
		} finally {
			postgre.releaseConnection(connection, prepareStatement, resultSet);
		}
		return subscriptionStatus;
	}

	/**
	 * @param subscriber_id
	 * @param subscriptionLang
	 * @return
	 */
	private double getPreferenceId(double subscriberId, String subscriptionLang, String persona) {
		logger.info("NewsletterPhpExternal : getPreferenceId()");
		double preferenceId = 0;
		String getPreferenceIdQuery = "SELECT PREFERENCE_ID FROM NEWSLETTER_PREFERENCE WHERE "
				+ "SUBSCRIBER_ID = ? AND LANGUAGE = ? AND PERSONA = ?";
		Connection connection = null;
		PreparedStatement prepareStatement = null;
		ResultSet resultSet = null;
		try {
			connection = postgre.getConnection();
			prepareStatement = connection.prepareStatement(getPreferenceIdQuery);
			prepareStatement.setDouble(1, subscriberId);
			prepareStatement.setString(2, subscriptionLang);
			prepareStatement.setString(3, persona);

			resultSet = prepareStatement.executeQuery();

			if (resultSet.next()) {
				logger.info("Preference Id Exist !");
				preferenceId = resultSet.getDouble(1);
			} else {
				logger.info("Preference Id Doesn't Exist !");
			}
		} catch (Exception e) {
			logger.error("Exception in getPreferenceId", e);
		} finally {
			postgre.releaseConnection(connection, prepareStatement, resultSet);
		}
		return preferenceId;
	}

	/**
	 * @param email
	 */
	public void sendConfirmationMail(String email, String pageLanguage, String confirmationToken, String emailElement,
			String unsubReason, RequestContext context) {
		logger.info("NewsletterPhpExternal : sendConfirmationMail()");

		try {
			Properties mailPropertiesFile = loadProperties(context, "contactus.properties");
			String from = mailPropertiesFile.getProperty(CONTACT_FROM_MAIL);
			String to = email;
			String messageHtmlName = "";
			logger.info("sent To :" + to);
			String host = mailPropertiesFile.getProperty(CONTACT_MAIL_HOST);
			logger.info("relay IP :" + host);
			String port = mailPropertiesFile.getProperty(CONTACT_MAIL_PORT);
			Properties props = new Properties();
			String subject = "";
			props.put("mail.smtp.host", host);
			props.put("mail.smtp.starttls.enable", STARTTLS_ENABLE);
			props.put("mail.smtp.port", port);
			Session session = Session.getDefaultInstance(props, null);
			session.setDebug(true);
			MimeMessage msg = new MimeMessage(session);
			msg.setFrom(new InternetAddress(from));
			msg.setRecipient(Message.RecipientType.TO, new InternetAddress(to));

			// Mail Subject
			Properties propertiesFile = loadProperties(context, "phplist.properties");
			subject = propertiesFile.getProperty("messageSubject_" + pageLanguage);
			if (emailElement.equals(CONFIRMATION_EMAIL)) {
				messageHtmlName = "newsletter-confirmation-mail-" + pageLanguage + ".html";
			} else if (emailElement.equals(UNSUBSCRIPTION_CONFIRMATION_EMAIL)) {
				messageHtmlName = "newsletter-unsubscription-mail-" + pageLanguage + ".html";
			}
			logger.info("NewsletterPhpExternal : messageHtmlName " + messageHtmlName);
			String message = getHtmlFile(messageHtmlName, context);
			if (!unsubReason.equals("")) {
				message = message.replace("<token>", confirmationToken).replace("<lang>", pageLanguage)
						.replace("<reason>", unsubReason);

			} else {
				message = message.replace("<token>", confirmationToken).replace("<lang>", pageLanguage);
			}
			logger.info("Confirmation Mail HTML :" + message);

			if (pageLanguage.equals("ar")) {
				msg.setSubject(subject, CHAR_SET);
				msg.setContent(message, "text/html;Charset=UTF-8");
			} else {
				msg.setSubject(subject);
				msg.setContent(message, "text/html");
			}

			logger.info("msg:" + message);
			Transport.send(msg);

		} catch (MessagingException e) {
			logger.error("Exception in sendConfirmationMail : " + e);
		}

	}

	/**
	 * @param subscriber_id
	 * @param subscriptionLanguage
	 * @param persona
	 */
	private boolean addSubscriberPreferences(double subscriberId, String subscriptionLanguage, String persona) {
		logger.info("NewsletterPhpExternal : addSubscriberPreferences()");

		boolean subscriberPreferenceDataInsert = false;
		String addSubscriberPreferencesQuery = "INSERT INTO NEWSLETTER_PREFERENCE (SUBSCRIBER_ID, LANGUAGE, PERSONA, STATUS) VALUES(?,?,?,?)";
		Connection connection = null;
		PreparedStatement prepareStatement = null;

		try {
			connection = postgre.getConnection();
			prepareStatement = connection.prepareStatement(addSubscriberPreferencesQuery);
			prepareStatement.setDouble(1, subscriberId);
			prepareStatement.setString(2, subscriptionLanguage);
			prepareStatement.setString(3, persona);
			prepareStatement.setString(4, STATUS_PENDING);

			int result = prepareStatement.executeUpdate();
			if (result != 0) {
				logger.info("Newsletter Preference Added !");
				subscriberPreferenceDataInsert = true;
			} else {
				logger.info("Newsletter Preference Not Added !");
			}
		} catch (Exception e) {
			logger.error("Exception in addSubscriberPreferences", e);
		} finally {
			postgre.releaseConnection(connection, prepareStatement, null);
		}

		return subscriberPreferenceDataInsert;

	}

	/**
	 * @return
	 */
	private double generateSubscriberId() {
		logger.info("NewsletterPhpExternal : generateSubscriberId()");
		double subscriberId = Double.parseDouble(RandomStringUtils.randomNumeric(7));

		String checkSubscriberIdQuery = "SELECT SUBSCRIBER_ID FROM NEWSLETTER_MASTER WHERE SUBSCRIBER_ID = ?";

		Connection connection = null;
		PreparedStatement prepareStatement = null;
		ResultSet resultSet = null;
		try {
			connection = postgre.getConnection();
			prepareStatement = connection.prepareStatement(checkSubscriberIdQuery);
			prepareStatement.setDouble(1, subscriberId);

			resultSet = prepareStatement.executeQuery();

			if (resultSet.next()) {
				logger.info("Subcriber Id Already Exist !");
				generateSubscriberId();
			} else {
				logger.info("Subcriber Id Doesn't Exist !");
			}
		} catch (Exception e) {
			logger.error("Exception in generateSubscriberId", e);
		} finally {
			postgre.releaseConnection(connection, prepareStatement, resultSet);
		}

		return subscriberId;
	}

	/**
	 * @param email
	 */
	public boolean isEmailAlreadyExist(String email) {
		logger.info("NewsletterPhpExternal : isEmailAlreadyExist()");
		boolean emailsExistStatus = false;

		String emailCheckQuery = "SELECT SUBSCRIBER_EMAIL FROM NEWSLETTER_MASTER WHERE SUBSCRIBER_EMAIL = ?";

		Connection connection = null;
		PreparedStatement prepareStatement = null;
		ResultSet resultSet = null;
		try {
			connection = postgre.getConnection();
			prepareStatement = connection.prepareStatement(emailCheckQuery);
			prepareStatement.setString(1, email);

			resultSet = prepareStatement.executeQuery();

			if (resultSet.next()) {
				logger.info("Email Already Exist !");
				emailsExistStatus = true;
			} else {
				logger.info("Email Doesn't Exist !");
			}
		} catch (Exception e) {
			logger.error("Exception in isEmailAlreadyExist", e);
		} finally {
			postgre.releaseConnection(connection, prepareStatement, resultSet);
		}
		return emailsExistStatus;
	}

	/**
	 * @param subscriber_id
	 * @param email
	 * @param subscriptionLang
	 * @param flag
	 * @param context
	 * @return document
	 * @throws NoSuchAlgorithmException
	 * @throws IOException              this method will verify the flag, based on
	 *                                  flag it makes call to mailchimp
	 */
	public boolean addSubscriberInMasterTable(String uid, double subscriberId, String email, String status) {
		logger.info("NewsletterPhp External : addSubscriberInMasterTable");
		boolean subscriberMasterDataInsert = false;
		String addMasterDataQuery = "INSERT INTO NEWSLETTER_MASTER (SUBSCRIBER_ID, SUBSCRIBER_EMAIL, STATUS, SUBSCRIBED_DATE,UID) "
				+ "VALUES(?,?,?,LOCALTIMESTAMP,?)";
		Connection connection = null;
		PreparedStatement prepareStatement = null;

		try {
			connection = postgre.getConnection();
			prepareStatement = connection.prepareStatement(addMasterDataQuery);
			prepareStatement.setDouble(1, subscriberId);
			prepareStatement.setString(2, email);
			prepareStatement.setString(3, status);
			prepareStatement.setString(4, uid);
			int result = prepareStatement.executeUpdate();
			if (result != 0) {
				logger.info("Subscriber Added !");
				subscriberMasterDataInsert = true;
			} else {
				logger.info("Subscriber Not Added !");
			}
		} catch (Exception e) {
			logger.error("Exception in addSubscriberInMasterTable", e);
		} finally {
			postgre.releaseConnection(connection, prepareStatement, null);
		}

		return subscriberMasterDataInsert;
	}

	/**
	 * this method takes the email, status, messages and returns xml document.
	 *
	 * @param email
	 * @param status
	 * @param validationMessage
	 * @param lang
	 * @return document
	 */
	private Document getDocument(final String email, final String status) {
		logger.info("NewsletterPhpExternal : getDocument()");
		Document document = DocumentHelper.createDocument();
		Element resultElement = document.addElement(ELEMENT_RESULT);
		Element statusElement = resultElement.addElement(ELEMENT_STATUS);
		statusElement.setText(status);
		Element emailElement = resultElement.addElement(ELEMENT_EMAIL);
		emailElement.setText(email);
		return document;
	}

	/**
	 * This method will be used to load the configuration properties.
	 *
	 * @param context The parameter context object passed from Component.
	 * @return properties
	 */
	private static Properties loadProperties(final RequestContext context, String propertyFile) {
		logger.info("loadProperties:Begin");
		PropertiesFileReader propertyFileReader = new PropertiesFileReader(context, propertyFile);
		return propertyFileReader.getPropertiesFile();

	}

	/**
	 * This method is used to retrieve person setting for the user from database.
	 *
	 * @return returns the persona value
	 */
	public String getPersonaForUser(String userId, Postgre postgre) {
		logger.info("DashboardSettingsExternal : getPersonaForUser()");
		String personaValue = null;
		String personaQuery = "SELECT * FROM persona_settings WHERE user_id = ? AND active = True";
		logger.debug("personaQuery : " + personaQuery);
		Connection connection = null;
		PreparedStatement prepareStatement = null;
		ResultSet rs = null;

		try {
			connection = postgre.getConnection();
			prepareStatement = connection.prepareStatement(personaQuery);
			prepareStatement.setString(1, userId);
			rs = prepareStatement.executeQuery();
			while (rs.next()) {
				personaValue = rs.getString("persona_value");
				logger.debug("Persona Value :" + personaValue);
			}
		} catch (Exception e) {
			logger.error("Exception in getPersonaForUser", e);
		} finally {
			postgre.releaseConnection(connection, prepareStatement, rs);
		}

		return personaValue;
	}

	/**
	 * This method will be used to load the configuration properties.
	 *
	 * @param context The parameter context object passed from Component.
	 * @throws IOException
	 * @throws MalformedURLException
	 *
	 */
	private static String getHtmlFile(String htmlFileName, RequestContext context) {
		logger.info("NewsletterPhp External : getHtmlFile");
		FileDal fileDal = context.getFileDal();
		String root = fileDal.getRoot();
		return fileDal.read(root + NEWSLETTER_TEMPLATE_PATH + htmlFileName);
	}

	private boolean validateMailID(String emailId) {
		logger.info("Validate emailId : ");
		ValidationErrorList errorList = new ValidationErrorList();
		ESAPI.validator().getValidInput(ELEMENT_EMAIL, emailId, ESAPIValidator.EMAIL_ID, 50, false, true, errorList);
		return errorList.isEmpty();
	}

	private boolean validateLanguage(String language) {
		ValidationErrorList errorList = new ValidationErrorList();

		logger.info(" Validate language" + language + "<<<");
		ESAPI.validator().getValidInput("language", language, ESAPIValidator.ALPHABET, 2, false, true, errorList);
		if (errorList.isEmpty()) {
			return true;
		} else {
			logger.info(errorList.getError("language"));
			return false;
		}

	}

	/**
	 * @author Arbaj
	 * 
	 *         This method is used to get subscription status for Newsletter.
	 * 
	 * @param userId
	 * 
	 * @return
	 */
	private String getSubscriptionStatus(String userId, String action) {
		logger.info("DashboardSettingsExternal : getSubscriptionStatus()");
		String subscriptionStatus = null;
		String getSubscriptionStatusQuery = "";
		if (action.equals(TOPICS)) {
			getSubscriptionStatusQuery = "SELECT STATUS FROM NEWSLETTER_MASTER WHERE UID = ?";
		} else {
			getSubscriptionStatusQuery = "SELECT STATUS FROM NEWSLETTER_MASTER WHERE SUBSCRIBER_EMAIL = ?";
		}

		Connection connection = null;
		PreparedStatement prepareStatement = null;
		ResultSet resultSet = null;
		try {
			connection = postgre.getConnection();
			prepareStatement = connection.prepareStatement(getSubscriptionStatusQuery);
			prepareStatement.setString(1, userId);

			resultSet = prepareStatement.executeQuery();
			while (resultSet.next()) {
				subscriptionStatus = resultSet.getString(1);
			}

		} catch (Exception e) {
			logger.error("Exception in getSubscriptionStatus", e);
		} finally {
			postgre.releaseConnection(connection, prepareStatement, resultSet);
		}
		return subscriptionStatus;
	}

	/**
	 * @author Pramesh
	 * @param userId
	 * @return This method is used to fetch subscriberID using UID
	 */
	private int getSubscriberID(String userId, String condition) {
		logger.info("DashboardSettingsExternal : getSubscriberID()");

		String subscriberIDQuery = "";

		if (condition.equals(USING_UID)) {
			subscriberIDQuery = "SELECT SUBSCRIBER_ID FROM NEWSLETTER_MASTER WHERE UID = ?";

		} else if (condition.equals(USING_EMAIL)) {
			subscriberIDQuery = "SELECT SUBSCRIBER_ID FROM NEWSLETTER_MASTER WHERE SUBSCRIBER_EMAIL = ?";

		}

		int subscriberID = 0;
		Connection connection = null;
		PreparedStatement prepareStatement = null;
		ResultSet rs = null;

		try {
			connection = postgre.getConnection();
			prepareStatement = connection.prepareStatement(subscriberIDQuery);
			prepareStatement.setString(1, userId);
			rs = prepareStatement.executeQuery();

			while (rs.next()) {
				subscriberID = rs.getInt(1);
			}

			logger.debug("subscriberID : " + subscriberID);

		} catch (Exception e) {
			logger.error("Exception in subscriberID", e);
		} finally {
			postgre.releaseConnection(connection, prepareStatement, rs);
		}
		return subscriberID;
	}

	/**
	 * @author Arbaj
	 * 
	 *         This method is used to create the topics response document based on
	 *         the input.
	 * 
	 * @param responseElem
	 * @param action
	 * @param userId
	 * @param persona
	 * @param status
	 * @param error
	 */
	private void createTopicsResponseDoc(Element responseElem, String topics, String userId, String persona,
			String status, String error) {
		if (topics == null)
			topics = "";
		if (userId == null)
			userId = "";
		if (persona == null)
			persona = "";
		if (status == null)
			status = "";
		if (error == null)
			error = "";

		Element newsletterResponseElem = responseElem.addElement("newsletter-settings");

		newsletterResponseElem.addElement(STATUS).setText(status);
		newsletterResponseElem.addElement(TOPICS).setText(topics);
		newsletterResponseElem.addElement(USER_ID).setText(userId);
		newsletterResponseElem.addElement(PERSONA).setText(persona);
		newsletterResponseElem.addElement(ERROR).setText(error);

	}

}
