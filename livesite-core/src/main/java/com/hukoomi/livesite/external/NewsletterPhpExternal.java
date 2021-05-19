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

import com.hukoomi.utils.MySql;
import com.hukoomi.utils.Postgre;
import com.hukoomi.utils.PropertiesFileReader;
import com.hukoomi.utils.ValidationUtils;
import com.hukoomi.utils.XssUtils;
import com.interwoven.livesite.file.FileDal;
import com.interwoven.livesite.runtime.RequestContext;

/**
 * @author Arbaj
 *
 */
public class NewsletterPhpExternal {

    /** Logger object to check the flow of the code. */
    private static final Logger logger = Logger
            .getLogger(NewsletterPhpExternal.class);

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

    /** preference updated response status. */
    private static final String PREFERENCE_UPDATED = "PreferenceUpdated";

    /** preference updated response status. */
    private static final String STATUS_ALREADY_SUBSCRIBED = "AlreadySubscribed";

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
    
    DashboardSettingsExternal settingExternal = new DashboardSettingsExternal();

    @SuppressWarnings("deprecation")
    public Document subscribeToNewsletter(final RequestContext context) {
        logger.info("NewsletterPhpExternal : subscribeToNewsletter()");

        HttpSession session = context.getRequest().getSession();

        postgre = new Postgre(context);

        Document memberdetail = null;
        ValidationUtils util = new ValidationUtils();
        XssUtils xssUtils = new XssUtils();

        String pageLang = context.getParameterString("lang");
        String uid = null;
        if (session.getAttribute("status") == "valid") {
            uid = (String) session.getAttribute("uid");
        }
        String email = context.getParameterString(ELEMENT_EMAIL);
        String subscriptionLang = context
                .getParameterString("subscriptionLang");
        String persona = context.getParameterString(PERSONA);
        if(uid !=null && !uid.equals("")) {
            
            persona = settingExternal.getPersonaForUser(uid,postgre);
            logger.debug("NewsletterPhpExternal : dashboard persona "+persona);
        }

        if (persona == null) {
            persona = "general";
        }

        logger.info("User Details From Front End :-> email : " + email
                + " subcription language : " + subscriptionLang
                + " persona : " + persona);

        if (!email.equals("") && !subscriptionLang.equals("")) {
            if ((subscriptionLang.equals("ar")
                    || subscriptionLang.equals("en"))
                    && (email.length() <= 50 && util
                            .validateEmailId(xssUtils.stripXSS(email)))) {
                if (!isEmailAlreadyExist(email)) {
                    double subscriberId = generateSubscriberId();
                    boolean subscriberMasterDataInsert = addSubscriberInMasterTable(
                            uid, subscriberId, email,
                            STATUS_PENDING);
                    boolean subscriberPreferenceDataInsert = addSubscriberPreferences(
                            subscriberId,
                            subscriptionLang, persona, STATUS_PENDING);
                    double preferenceId = getPreferenceId(subscriberId,
                            subscriptionLang, persona);
                    String confirmationToken = generateConfirmationToken(
                            subscriberId, preferenceId, email);
                    if (subscriberMasterDataInsert
                            && subscriberPreferenceDataInsert) {
                        sendConfirmationMail(email, pageLang,
                                confirmationToken, context);
                        memberdetail = getDocument(email,
                                CONFIRMATION_SENT);
                    }

                } else if (isEmailAlreadyExist(email)) {
                    // Check Confirmation Status
                    String confirmationStatus = checkConfirmationStatus(
                            email);
                    logger.info("Confirmation Status received from Db : "
                            + confirmationStatus);
                    if (STATUS_CONFIRMED.equals(confirmationStatus)) {
                        String subscriptionStatus = getSubscriptionStatus(
                                email);
                        if (STATUS_PENDING.equals(subscriptionStatus)) {
                            memberdetail = getDocument(email,
                                    STATUS_ALREADY_PENDING);
                        } else {
                            String preferenceStatus = checkPrefernceStatus(
                                    email, persona, subscriptionLang);
                            if (STATUS_ACTIVE.equals(preferenceStatus)) {
                                memberdetail = getDocument(email,
                                        STATUS_ALREADY_SUBSCRIBED);
                            } else if (STATUS_PENDING
                                    .equals(preferenceStatus)) {
                                memberdetail = getDocument(email,
                                        CONFIRMATION_PENDING);
                            } else {
                                double subscriberId = getSubcriberId(
                                        email);
                                boolean preferenceUpdateStatus = updateSubscriberPreference(
                                        subscriberId, subscriptionLang,
                                        persona, STATUS_PENDING);
                                double preferenceId = getPreferenceId(
                                        subscriberId, subscriptionLang,
                                        persona);
                                if (preferenceUpdateStatus) {
                                    String confirmationToken = generateConfirmationToken(
                                            subscriberId, preferenceId,
                                            email);
                                    sendConfirmationMail(email,
                                            pageLang,
                                            confirmationToken, context);
                                    logger.info(
                                            "Confirmation Mail Sent For Preference Update !");
                                    memberdetail = getDocument(email,
                                            CONFIRMATION_SENT);
                                } else {
                                    logger.info(
                                            "Newsletter Preference Not Updated !");
                                    memberdetail = getDocument(email,
                                            STATUS_ALREADY_SUBSCRIBED);
                                }
                            }
                        }
                    } else if (STATUS_PENDING.equals(confirmationStatus)) {
                        memberdetail = getDocument(email,
                                CONFIRMATION_PENDING);
                    }
                }
            } else {
                memberdetail = getDocument(email, STATUS_FIELD_VALIDATION);
            }

        }
        logger.info("Newsletter final document : " + memberdetail.asXML());
        return memberdetail;
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

        try {
            connection = postgre.getConnection();
            prepareStatement = connection
                    .prepareStatement(getSubcriberIdQuery);

            prepareStatement.setString(1, email);
            ResultSet resultSet = prepareStatement.executeQuery();

            if (resultSet.next()) {
                logger.info("Subscriber Id Available  !");
                subscriberId = resultSet.getDouble(1);
            } else {
                logger.info("Subscriber Id Not Available !");
            }
        } catch (Exception e) {
            logger.error("Exception in getSubcriberId", e);
        } finally {
            postgre.releaseConnection(connection, prepareStatement, null);
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
    private boolean updateSubscriberPreference(double subscriberId,
            String subscriptionLang, String persona, String status) {
        logger.info(
                "NewsletterPhpExternal : updateSubscriberPreference()");

        boolean subscriberPreferenceDataInsert = false;
        String addSubscriberPreferencesQuery = "INSERT INTO NEWSLETTER_PREFERENCE (SUBSCRIBER_ID, LANGUAGE, PERSONA, STATUS) VALUES(?,?,?,?)";
        Connection connection = null;
        PreparedStatement prepareStatement = null;

        try {
            connection = postgre.getConnection();
            prepareStatement = connection
                    .prepareStatement(addSubscriberPreferencesQuery);
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
    private String checkPrefernceStatus(String email, String persona,
            String subscriptionLang) {
        logger.info("NewsletterPhpExternal : checkPrefernceStatus()");
        String preferenceStatus = "";
        String tokenCheckQuery = "SELECT NP.STATUS FROM NEWSLETTER_MASTER NM INNER JOIN NEWSLETTER_PREFERENCE NP ON NM.SUBSCRIBER_ID = NP.SUBSCRIBER_ID WHERE NM.SUBSCRIBER_EMAIL = ? AND NP.PERSONA = ? AND NP.LANGUAGE = ?";
        Connection connection = null;
        PreparedStatement prepareStatement = null;

        try {
            connection = postgre.getConnection();
            prepareStatement = connection
                    .prepareStatement(tokenCheckQuery);
            prepareStatement.setString(1, email);
            prepareStatement.setString(2, persona);
            prepareStatement.setString(3, subscriptionLang);

            ResultSet resultSet = prepareStatement.executeQuery();
            while (resultSet.next()) {
                preferenceStatus = resultSet.getString(1);
            }

        } catch (Exception e) {
            logger.error("Exception in checkPrefernceStatus", e);
        } finally {
            postgre.releaseConnection(connection, prepareStatement, null);
        }
        return preferenceStatus;
    }

    /**
     * @param email
     */
    private String checkConfirmationStatus(String email) {
        String confirmationStatus = STATUS_PENDING;

        String checkConfirmationStatusQuery = "SELECT COUNT(*) FROM NEWSLETTER_CONFIRMATION_TOKEN WHERE SUBSCRIBER_EMAIL = ? AND CONFIRMATION_STATUS = 'Confirmed'";
        Connection connection = null;
        PreparedStatement prepareStatement = null;

        try {
            connection = postgre.getConnection();
            prepareStatement = connection
                    .prepareStatement(checkConfirmationStatusQuery);

            prepareStatement.setString(1, email);
            ResultSet resultSet = prepareStatement.executeQuery();
            resultSet.next();
            long count = resultSet.getLong(1);
            if (count > 0) {
                confirmationStatus = STATUS_CONFIRMED;
            }

        } catch (Exception e) {
            logger.error("Exception in checkConfirmationStatus", e);
        } finally {
            postgre.releaseConnection(connection, prepareStatement, null);
        }

        return confirmationStatus;

    }

    /**
     * @param subscriber_id
     * @param preferenceId
     * @return
     */
    private String generateConfirmationToken(double subscriberId,
            double preferenceId, String email) {
        logger.info("NewsletterPhpExternal : generateConfirmationToken()");
        String confirmationToken = RandomStringUtils
                .randomAlphanumeric(10);

        boolean tokenExist = isConfirmationTokenExist(confirmationToken);

        if (tokenExist) {
            generateConfirmationToken(subscriberId, preferenceId, email);
        } else {
            String addGeneratedTokenQuery = "INSERT INTO NEWSLETTER_CONFIRMATION_TOKEN (TOKEN, SUBSCRIBER_ID, PREFERENCE_ID, GENERATED_DATE, CONFIRMATION_STATUS, SUBSCRIBER_EMAIL) VALUES(?,?,?,LOCALTIMESTAMP,?,?)";
            Connection connection = null;
            PreparedStatement prepareStatement = null;

            try {
                connection = postgre.getConnection();
                prepareStatement = connection
                        .prepareStatement(addGeneratedTokenQuery);

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
                postgre.releaseConnection(connection, prepareStatement,
                        null);
            }
        }

        return confirmationToken;
    }

    /**
     * @param confirmationToken
     * @return
     */
    private boolean isConfirmationTokenExist(String confirmationToken) {
        logger.info("NewsletterPhpExternal : isConfirmationTokenExist()");
        boolean tokenExist = false;
        String tokenCheckQuery = "SELECT TOKEN FROM NEWSLETTER_CONFIRMATION_TOKEN WHERE TOKEN = ?";
        Connection connection = null;
        PreparedStatement prepareStatement = null;

        try {
            connection = postgre.getConnection();
            prepareStatement = connection
                    .prepareStatement(tokenCheckQuery);
            prepareStatement.setString(1, confirmationToken);

            ResultSet resultSet = prepareStatement.executeQuery();

            if (resultSet.next()) {
                logger.info("Token already Exist !");
                tokenExist = true;
            } else {
                logger.info("Token Doesn't Exist !");
            }
        } catch (Exception e) {
            logger.error("Exception in isConfirmationTokenExist", e);
        } finally {
            postgre.releaseConnection(connection, prepareStatement, null);
        }
        return tokenExist;
    }

    /**
     * @param email
     * @return
     */
    private String getSubscriptionStatus(String email) {
        logger.info("NewsletterPhpExternal : getSubscriptionStatus()");
        String subscriptionStatus = null;
        String getSubscriptionStatusQuery = "SELECT STATUS FROM NEWSLETTER_MASTER WHERE SUBSCRIBER_EMAIL = ?";
        Connection connection = null;
        PreparedStatement prepareStatement = null;

        try {
            connection = postgre.getConnection();
            prepareStatement = connection
                    .prepareStatement(getSubscriptionStatusQuery);
            prepareStatement.setString(1, email);

            ResultSet resultSet = prepareStatement.executeQuery();
            resultSet.next();
            subscriptionStatus = resultSet.getString(1);

        } catch (Exception e) {
            logger.error("Exception in getSubscriptionStatus", e);
        } finally {
            postgre.releaseConnection(connection, prepareStatement, null);
        }
        return subscriptionStatus;
    }

    /**
     * @param subscriber_id
     * @param subscriptionLang
     * @return
     */
    private double getPreferenceId(double subscriberId,
            String subscriptionLang, String persona) {
        logger.info("NewsletterPhpExternal : getPreferenceId()");
        double preferenceId = 0;
        String getPreferenceIdQuery = "SELECT PREFERENCE_ID FROM NEWSLETTER_PREFERENCE WHERE SUBSCRIBER_ID = ? AND LANGUAGE = ? AND PERSONA = ?";
        Connection connection = null;
        PreparedStatement prepareStatement = null;

        try {
            connection = postgre.getConnection();
            prepareStatement = connection
                    .prepareStatement(getPreferenceIdQuery);
            prepareStatement.setDouble(1, subscriberId);
            prepareStatement.setString(2, subscriptionLang);
            prepareStatement.setString(3, persona);

            ResultSet resultSet = prepareStatement.executeQuery();

            if (resultSet.next()) {
                logger.info("Preference Id Exist !");
                preferenceId = resultSet.getDouble(1);
            } else {
                logger.info("Preference Id Doesn't Exist !");
            }
        } catch (Exception e) {
            logger.error("Exception in getPreferenceId", e);
        } finally {
            postgre.releaseConnection(connection, prepareStatement, null);
        }
        return preferenceId;
    }

    /**
     * @param email
     */
    private void sendConfirmationMail(String email, String pageLanguage,
            String confirmationToken,
            RequestContext context) {
        logger.info("NewsletterPhpExternal : sendConfirmationMail()");

        try {
            Properties mailPropertiesFile = loadProperties(context,
                    "contactus.properties");
            String from = mailPropertiesFile
                    .getProperty(CONTACT_FROM_MAIL);
            String to = email;
            logger.info("sent To :" + to);
            String host = mailPropertiesFile
                    .getProperty(CONTACT_MAIL_HOST);
            logger.info("relay IP :" + host);
            String port = mailPropertiesFile
                    .getProperty(CONTACT_MAIL_PORT);
            Properties props = new Properties();
            String subject = "";
            props.put("mail.smtp.host", host);
            props.put("mail.smtp.starttls.enable", STARTTLS_ENABLE);
            props.put("mail.smtp.port", port);
            Session session = Session.getDefaultInstance(props, null);
            session.setDebug(true);
            MimeMessage msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(from));
            msg.setRecipient(Message.RecipientType.TO,
                    new InternetAddress(to));

            // Mail Subject
            Properties propertiesFile = loadProperties(context,
                    "phplist.properties");
            subject = propertiesFile
                    .getProperty("messageSubject_" + pageLanguage);

            String messageHtmlName = "newsletter-confirmation-mail-"
                    + pageLanguage + ".html";
            String message = getHtmlFile(messageHtmlName, context);
            message = message.replace("<token>", confirmationToken)
                    .replace("<lang>", pageLanguage);

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
    private boolean addSubscriberPreferences(double subscriberId,
            String subscriptionLanguage, String persona, String status) {
        logger.info("NewsletterPhpExternal : addSubscriberPreferences()");

        boolean subscriberPreferenceDataInsert = false;
        String addSubscriberPreferencesQuery = "INSERT INTO NEWSLETTER_PREFERENCE (SUBSCRIBER_ID, LANGUAGE, PERSONA, STATUS) VALUES(?,?,?,?)";
        Connection connection = null;
        PreparedStatement prepareStatement = null;

        try {
            connection = postgre.getConnection();
            prepareStatement = connection
                    .prepareStatement(addSubscriberPreferencesQuery);
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
                subscriberPreferenceDataInsert = false;
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
        double subscriberId = Double
                .parseDouble(RandomStringUtils.randomNumeric(7));

        String checkSubscriberIdQuery = "SELECT SUBSCRIBER_ID FROM NEWSLETTER_MASTER WHERE SUBSCRIBER_ID = ?";

        Connection connection = null;
        PreparedStatement prepareStatement = null;

        try {
            connection = postgre.getConnection();
            prepareStatement = connection
                    .prepareStatement(checkSubscriberIdQuery);
            prepareStatement.setDouble(1, subscriberId);

            ResultSet resultSet = prepareStatement.executeQuery();

            if (resultSet.next()) {
                logger.info("Subcriber Id Already Exist !");
                generateSubscriberId();
            } else {
                logger.info("Subcriber Id Doesn't Exist !");
            }
        } catch (Exception e) {
            logger.error("Exception in generateSubscriberId", e);
        } finally {
            postgre.releaseConnection(connection, prepareStatement, null);
        }

        return subscriberId;
    }

    /**
     * @param email
     */
    private boolean isEmailAlreadyExist(String email) {
        logger.info("NewsletterPhpExternal : isEmailAlreadyExist()");
        boolean emailsExistStatus = false;
        String emailCheckQuery = "SELECT SUBSCRIBER_EMAIL FROM NEWSLETTER_MASTER WHERE SUBSCRIBER_EMAIL = ?";
        Connection connection = null;
        PreparedStatement prepareStatement = null;

        try {
            connection = postgre.getConnection();
            prepareStatement = connection
                    .prepareStatement(emailCheckQuery);
            prepareStatement.setString(1, email);

            ResultSet resultSet = prepareStatement.executeQuery();

            if (resultSet.next()) {
                logger.info("Email Already Exist !");
                emailsExistStatus = true;
            } else {
                logger.info("Email Doesn't Exist !");
                emailsExistStatus = false;
            }
        } catch (Exception e) {
            logger.error("Exception in isEmailAlreadyExist", e);
        } finally {
            postgre.releaseConnection(connection, prepareStatement, null);
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
     * @throws IOException
     *                                  this method will verify the flag, based on
     *                                  flag it makes call to mailchimp
     */
    public boolean addSubscriberInMasterTable(String uid,
            double subscriberId,
            String email, String status) {
        logger.info("NewsletterPhp External : addSubscriberInMasterTable");
        boolean subscriberMasterDataInsert = false;
        String addMasterDataQuery = "INSERT INTO NEWSLETTER_MASTER (SUBSCRIBER_ID, SUBSCRIBER_EMAIL, STATUS, SUBSCRIBED_DATE,UID) VALUES(?,?,?,LOCALTIMESTAMP,?)";
        Connection connection = null;
        PreparedStatement prepareStatement = null;

        try {
            connection = postgre.getConnection();
            prepareStatement = connection
                    .prepareStatement(addMasterDataQuery);
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
                subscriberMasterDataInsert = false;
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
     * @param context
     *                The parameter context object passed from Component.
     * @return properties
     */
    private static Properties loadProperties(final RequestContext context,
            String propertyFile) {
        logger.info("loadProperties:Begin");
        PropertiesFileReader propertyFileReader = new PropertiesFileReader(
                context, propertyFile);
        return propertyFileReader.getPropertiesFile();

    }

    /**
     * This method will be used to load the configuration properties.
     *
     * @param context
     *                The parameter context object passed from Component.
     * @throws IOException
     * @throws MalformedURLException
     *
     */
    private static String getHtmlFile(String htmlFileName,
            RequestContext context) {
        logger.info("NewsletterPhp External : getHtmlFile");
        FileDal fileDal = context.getFileDal();
        String root = fileDal.getRoot();
        return fileDal
                .read(root + NEWSLETTER_TEMPLATE_PATH + htmlFileName);
    }

}
