package com.hukoomi.livesite.external;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.json.JSONObject;

import com.hukoomi.utils.CommonUtils;
import com.interwoven.livesite.runtime.RequestContext;

public class NewsletterExternal {
    /** Logger object to check the flow of the code. */
    private static final Logger LOGGER =
            Logger.getLogger(NewsletterExternal.class);
    /** subscriber Lisid of mailchimp. */
    private String listId;
    /** authorizationHeader for authencicate mailchimp. */
    private String authorizationHeader;
    /** baseUrl of mailchimp. */
    private String baseUrl;
    /** hex value for Convert to hexa. */
    private static final int HEXA = 16;
    /** digest length. */
    private static final int DIGEST_LENGTH = 32;
    /** httpConnection for making call to mailchimp services. */
    private HttpURLConnection httpConnection = null;
    /** mailchimp response status. */
    private static final String STATUS_SUBSCRIBED = "subscribed";
    /** mailchimp response status. */
    private static final String STATUS_UNSUBSCRIBED = "unsubscribed";
    /** mailchimp response status. */
    private static final String STATUS_PENDING = "pending";
    /** mailchimp response status. */
    private static final String STATUS_NOTFOUND = "404";
    /** mailchimp response status. */
    private static final int STATUS_OK = 200;
    /** mailchimp response status. */
    private static final String STATUS_ALREADY_SUBSCRIBED =
            "alreadySubscribed";
    /** mailchimp response status. */
    private static final String STATUS_ALREADY_PENDING = "alreadyPending";
    /** status key in mailchimp response. */
    private static final String KEY_STATUS = "status";
    /** element for document. */
    private static final String ELEMENT_RESULT = "Result";
    /** element for document. */
    private static final String ELEMENT_STATUS = "status";
    /** element for document. */
    private static final String ELEMENT_EMAIL = "email";
    /** element for document. */
    private static final String ELEMENT_MESSAGE = "message";

    /**
     * @param context
     * @return memberdetail document
     * @throws IOException
     * @throws NoSuchAlgorithmException This method internally makes call
     *                                  to createSubscriberinMailChimp
     *                                  method for user subscription to
     *                                  Mailchimp.
     */
    public Document subscribeToNewsletter(final RequestContext context)
            throws IOException, NoSuchAlgorithmException {
        LOGGER.info("Newsletter Subscribtion");
        Document memberdetail = null;
        String email = context.getParameterString("email");
        LOGGER.debug("email:" + email);
        String language = context.getParameterString("locale", "en");
        String subscriptionLang =
                context.getParameterString("subscriptionLang");
        if (!email.equals("") && !subscriptionLang.equals("")) {
            memberdetail = createSubscriberinMailChimp(email,
                    subscriptionLang, context);
        }
        return memberdetail;
    }

    /**
     * @param email
     * @param subscriptionLang
     * @param context
     * @param lang
     * @return document
     * @throws IOException
     * @throws NoSuchAlgorithmException This method is used to make call to
     *                                  mailchimp and check if the users is
     *                                  already subscribed or not.
     */

    public Document createSubscriberinMailChimp(final String email,
            final String subscriptionLang, final RequestContext context)
            throws IOException, NoSuchAlgorithmException {
        LOGGER.info("createSubscriberinMailChimp:Enter");
        CommonUtils util = new CommonUtils();
        Document document = DocumentHelper.createDocument();
        listId = util.getConfiguration("Mailchimp_List_Id", context);
        authorizationHeader = "Basic " + util.getConfiguration(
                "Mailchimp_Authorization_Header", context);
        baseUrl = util.getConfiguration("Mailchimp_BaseURL", context);
        try {

            String status = null;
            status = isSubscriberexist(email);
            String response = null;
            LOGGER.debug("status: " + status);

            if (status != null) {
                if (STATUS_NOTFOUND.equals(status)) {
                    status = STATUS_SUBSCRIBED;
                    response = createsubscriber(email, status,
                            subscriptionLang);
                    if (response != null) {
                        document = getDocument(email, response);
                    }
                } else if (STATUS_SUBSCRIBED.equals(status)) {
                    status = STATUS_ALREADY_SUBSCRIBED;
                    document = getDocument(email, status);
                } else if (STATUS_PENDING.equals(status)) {
                    status = STATUS_ALREADY_PENDING;
                    document = getDocument(email, status);
                } else if (STATUS_UNSUBSCRIBED.equals(status)) {
                    status = STATUS_PENDING;
                    response = createsubscriber(email, status,
                            subscriptionLang);
                    if (response != null) {
                        document = getDocument(email, response);
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.error("exception:" , e);
        }

        return document;
    }

    /**
     * this method takes the email, status, messages and returns xml
     * document.
     *
     * @param email
     * @param status
     * @param validationMessage
     * @param lang
     * @return document
     */
    private Document getDocument(final String email, final String status) {
        LOGGER.info("getDocument:Enter");
        Document document = DocumentHelper.createDocument();
        Element resultElement = document.addElement(ELEMENT_RESULT);
        Element statusElement = resultElement.addElement(ELEMENT_STATUS);
        statusElement.setText(status);
        Element emailElement = resultElement.addElement(ELEMENT_EMAIL);
        emailElement.setText(email);
        return document;
    }

    /**
     * this method will take email, status, language and creates a
     * subscriber in mailchimp.
     *
     * @param email
     * @param status
     * @param lang
     * @return response
     * @throws NoSuchAlgorithmException
     */
    private String createsubscriber(final String email,
            final String status, final String lang)
            throws NoSuchAlgorithmException {
        LOGGER.info("createsubscriber:Enter");
        InputStream is = null;
        try {
            // Create connection
            httpConnection = getConnection(email);
            httpConnection.setRequestMethod("PUT");
            httpConnection.setRequestProperty("Content-Type",
                    "application/json");
            httpConnection.setRequestProperty("Authorization",
                    authorizationHeader);
            String requestJSON = "{ \"email_address\" : \"" + email
                    + "\", \"email_type\": \"html\",\"status\" : \""
                    + status + "\",\"language\":\"" + lang + "\" }";
            httpConnection.setRequestProperty("Content-Length",
                    Integer.toString(requestJSON.getBytes().length));
            httpConnection.setRequestProperty("Content-Language", "en-US");
            httpConnection.setUseCaches(false);
            httpConnection.setDoOutput(true);

            // Send request
            DataOutputStream wr =
                    new DataOutputStream(httpConnection.getOutputStream());
            wr.writeBytes(requestJSON);
            wr.close();

            // Get Response
            is = httpConnection.getInputStream();
            BufferedReader rd =
                    new BufferedReader(new InputStreamReader(is));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();
            return getStatus(response.toString());
        } catch (IOException ioe) {
            int statusCode;
            try {
                statusCode = httpConnection.getResponseCode();
                LOGGER.debug("statusCode: " +statusCode);
                return String.valueOf(statusCode);
            } catch (IOException e) {
                LOGGER.error("Exception in subscriber creation: " , e);
            }
            httpConnection.disconnect();
            LOGGER.error("Exception in subscriber creation: " , ioe);
            return null;
        }

    }

    /**
     * @param email
     * @return status
     * @throws IOException
     * @throws NoSuchAlgorithmException This method is used to check if the
     *                                  subscriber exists.
     */
    private String isSubscriberexist(final String email)
            throws IOException, NoSuchAlgorithmException {
        LOGGER.info("isSubscriberexist:Enter");
        InputStream content = null;
        try {
            httpConnection = getConnection(email);
            httpConnection.setRequestMethod("GET");
            httpConnection.setDoOutput(true);
            httpConnection.setRequestProperty("Authorization",
                    authorizationHeader);
            httpConnection.setRequestProperty("Accept",
                    "application/json");
            content = httpConnection.getInputStream();
            StringBuilder sb = new StringBuilder();
            BufferedReader rd =
                    new BufferedReader(new InputStreamReader(content));
            String line;
            while ((line = rd.readLine()) != null) {
                sb.append(line);
            }
            LOGGER.debug("response" + sb);
            return getStatus(sb.toString());
        } catch (IOException ioe) {
            int statusCode = httpConnection.getResponseCode();
            LOGGER.error("Exception:", ioe);
            if (statusCode != STATUS_OK) {
                return String.valueOf(statusCode);
            }
        }
        return null;
    }

    /**
     * @param email
     * @return httpConnection
     * @throws NoSuchAlgorithmException
     * @throws IOException              This method is used to get URL
     *                                  Connection to the Mailchimp service
     *                                  endpoint
     */
    private HttpURLConnection getConnection(final String email)
            throws NoSuchAlgorithmException, IOException {
        LOGGER.info("getConnection:Enter");
        String emailHash = md5Java(email);
        String endpoint =
                baseUrl + "/lists/" + listId + "/members/" + emailHash;
        URL url = new URL(endpoint);
        httpConnection = (HttpURLConnection) url.openConnection();
        return httpConnection;
    }

    /**
     * @param message
     * @return digest
     * @throws NoSuchAlgorithmException This method is used to apply
     *                                  Message Digest Algorithm to the
     *                                  email-id while making Mailchimp
     *                                  Service call.
     */
    private String md5Java(final String message)
            throws NoSuchAlgorithmException {
        LOGGER.info("md5Java:Enter");
        MessageDigest md = MessageDigest.getInstance("MD5");

        byte[] hash = md.digest(message.getBytes(StandardCharsets.UTF_8));
        // Convert byte array into signum representation
        BigInteger no = new BigInteger(1, hash);
        // Convert message digest into hex value
        String digest = no.toString(HEXA);
        while (digest.length() < DIGEST_LENGTH) {
            digest = "0" + digest;
        }
        return digest;
    }

    private String getStatus(final String response) {
        LOGGER.info("getStatus:Enter");
        String status = null;
        if (!response.equals("")) {
            JSONObject jsonObj = new JSONObject(response);
            status = (String) jsonObj.get(KEY_STATUS);
        }
        return status;
    }
}
