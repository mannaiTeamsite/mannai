package com.hukoomi.livesite.external;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.ResourceBundle;

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
    private static final String STATUS_ALREADY_SUBSCRIBED =
            "Already Subscribed";
    /** mailchimp response status. */
    private static final String STATUS_ALREADY_PENDING = "Already Pending";
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

        Document memberdetail = null;
        LOGGER.debug("Newsletter Subscribtion");
        String email = context.getParameterString("email");
        LOGGER.debug("email:" + email);
        String language = context.getParameterString("locale", "en");
        Locale locale = new CommonUtils().getLocale(language);
        LOGGER.debug("locale:" + locale);
        String subscriptionLang =
                context.getParameterString("subscriptionLang");
        if (!email.equals("") && !subscriptionLang.equals("")) {
            memberdetail = createSubscriberinMailChimp(email,
                    subscriptionLang, context, locale);
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
            final String subscriptionLang, final RequestContext context,
            final Locale lang)
            throws IOException, NoSuchAlgorithmException {
        // add subscriber
        LOGGER.debug("add subcriber:");

        ResourceBundle bundle = ResourceBundle
                .getBundle("com.hukoomi.resources.Newsletter", lang);
        String validationMessage = "";
        Document document = DocumentHelper.createDocument();
        listId = getConfiguration("Mailchimp_List_Id", context);
        LOGGER.debug("listId:" + listId);
        authorizationHeader = "Basic " + getConfiguration(
                "Mailchimp_Authorization_Header", context);
        baseUrl = getConfiguration("Mailchimp_BaseURL", context);
        LOGGER.debug("baseUrl:" + baseUrl);
        try {

            String status = null;
            status = isSubscriberexist(email);
            String response = null;
            LOGGER.debug("info: " + status);

            if (status != null) {
                if (STATUS_NOTFOUND.equals(status)) {
                    status = STATUS_SUBSCRIBED;
                    response = createsubscriber(email, status,
                            subscriptionLang);
                    validationMessage = bundle.getString("success.msg");
                    if (response != null) {
                        document = getDocument(email, response,
                                validationMessage, lang);
                    }
                } else if (STATUS_SUBSCRIBED.equals(status)) {
                    status = STATUS_ALREADY_SUBSCRIBED;
                    validationMessage = bundle.getString("subscribed.msg");
                    document = getDocument(email, status,
                            validationMessage, lang);
                } else if (STATUS_PENDING.equals(status)) {
                    status = STATUS_ALREADY_PENDING;
                    validationMessage = bundle.getString("pending.msg");
                    document = getDocument(email, status,
                            validationMessage, lang);
                } else if (STATUS_UNSUBSCRIBED.equals(status)) {
                    status = STATUS_PENDING;
                    response = createsubscriber(email, status,
                            subscriptionLang);
                    String unsubMessage =
                            bundle.getString("unsubscribed.msg");
                    String unsubMessage1 =
                            bundle.getString("unsubscribed.msg1");
                    validationMessage = unsubMessage + "," + unsubMessage1;
                    if (response != null) {
                        document = getDocument(email, response,
                                validationMessage, lang);
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.error("error:" + e);
            e.printStackTrace();
        }

        return document;
    }

    /**
     * this method will take config parameter code and return config
     * parameter value.
     *
     * @param property
     * @param context
     * @return configParamValue return config parameter value.
     */
    private static String getConfiguration(final String property,
            final RequestContext context) {
        CommonUtils util = new CommonUtils();
        String configParamValue = null;
        if (property != null && !"".equals(property)) {
            if (CommonUtils.configParamsMap == null
                    || CommonUtils.configParamsMap.isEmpty()) {
                util.loadConfigparams(context);
            }
            configParamValue = CommonUtils.configParamsMap.get(property);
            LOGGER.debug("configParamValue:" + configParamValue);

        }
        return configParamValue;

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
    private Document getDocument(final String email, final String status,
            final String validationMessage, final Locale lang) {
        CommonUtils util = new CommonUtils();
        Document document = DocumentHelper.createDocument();
        Element resultElement = document.addElement(ELEMENT_RESULT);
        Element statusElement = resultElement.addElement(ELEMENT_STATUS);
        Element msgElement = resultElement.addElement(ELEMENT_MESSAGE);
        Element emailElement = resultElement.addElement(ELEMENT_EMAIL);
        emailElement.setText(email);
        String pendingMessage = "";
        String pendingMessage1 = "";
        LOGGER.debug(status);
        if (status.equals(STATUS_SUBSCRIBED)
                || status.equals(STATUS_ALREADY_SUBSCRIBED)
                || status.equals(STATUS_ALREADY_PENDING)) {
            if ("ar".equals(lang.toString())) {
                msgElement.setText(
                        util.decodeToArabicString(validationMessage)
                                + " : " + email);
            } else {
                msgElement.setText(validationMessage + " : " + email);
                statusElement.setText(status);
            }
        } else if (status.equals(STATUS_PENDING)) {
            if ("ar".equals(lang.toString())) {

                pendingMessage = util.decodeToArabicString(
                        validationMessage.split(",")[0]);
                pendingMessage1 = util.decodeToArabicString(
                        validationMessage.split(",")[1]);
            } else {
                pendingMessage = validationMessage.split(",")[0];
                pendingMessage1 = validationMessage.split(",")[1];
            }
            msgElement.setText(pendingMessage + " : " + email + " "
                    + pendingMessage1);
            statusElement.setText(status);
        } else {
            statusElement.setText(status);
        }

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
            LOGGER.debug(requestJSON);
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
     * @return status
     * @throws IOException
     * @throws NoSuchAlgorithmException This method is used to check if the
     *                                  subscriber exists.
     */
    private String isSubscriberexist(final String email)
            throws IOException, NoSuchAlgorithmException {

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
            LOGGER.debug("get response" + sb);
            LOGGER.debug("test:" + httpConnection.getResponseMessage());
            return getStatus(sb.toString());
        } catch (IOException ioe) {
            int statusCode = httpConnection.getResponseCode();
            if (statusCode != 200) {
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

    private String getStatus(final String response) {
        String status = null;
        if (!response.equals("")) {
            JSONObject jsonObj = new JSONObject(response);
            status = (String) jsonObj.get(KEY_STATUS);
        }
        return status;
    }
}
