package com.hukoomi.livesite.servlet;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.ValidationErrorList;

import com.hukoomi.utils.ESAPIValidator;
import com.hukoomi.utils.XssUtils;

@MultipartConfig
public class SubmitTicket extends HttpServlet {

    /** logger.debug object to check the flow of the code. */
    private static final Logger LOGGER =
            Logger.getLogger(SubmitTicket.class);
    /** attachment key. */
    private static final String ATTACHMENT_KEY = "attachment";
    /** Content type */
    private static final String CONTENTTYPE_JSON = "application/json";
    /** success of hpsm */
    private static final String SUCCESS_KEY = "success";
    /** failure of hpsm */
    private static final String SUCCESS_FALSE_VALUE = "false";
    /** errorMessage key */
    private static final String ERRORMESSAGE_KEY = "errorMessage";
    /** status for validationFailed */
    private static final String VALIDATION_FAILED_MSG = "validationFailed";
    /** passport key */
    private static final String PASSPORT_KEY = "passport";
    /** nationality key */
    private static final String NATIONALITY_KEY = "nationality";
    /** fullName key */
    private static final String FULLNAME_KEY = "fullName";
    /** companyName key */
    private static final String COMPANYNAME_KEY = "companyName";
    /** phoneNo key */
    private static final String PHONENUMBER_KEY = "phoneNo";
    /** emailId key */
    private static final String EMAIL_KEY = "emailId";
    /** eservice key */
    private static final String ESERVICE_KEY = "eservice";
    /** service key */
    private static final String SERVICE_KEY = "service";
    /** eServiceName key */
    private static final String ESERVICENAME_KEY = "eServiceName";
    /** serviceName key */
    private static final String SERVICENAME_KEY = "serviceName";
    /** subject key */
    private static final String SUBJECT_KEY = "subject";
    /** comments key */
    private static final String COMMENTS_KEY = "comments";
    /** fileSize key */
    private static final String FILESIZE_KEY = "fileSize";
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
   

    /**
     * @see HttpServlet#doPost(HttpServletRequest request,
     *      HttpServletResponse response)
     */
    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response) {
        LOGGER.info("Customer Service Submit Ticket: Start");
        boolean verify = true;
        JSONObject data = new JSONObject();
        try {
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(request.getInputStream()));
            String json = "";
            json = br.readLine();
            String httpServletAddress = request.getLocalAddr();
            data = new JSONObject(json);
            response.setContentType(CONTENTTYPE_JSON);
            response.setCharacterEncoding(CHAR_SET);
            JSONObject validateFields = validateFields(data);
            if (validateFields != null) {

                String gRecaptchaResponse =
                        validateFields.getString("recaptchaResponseField");
                verify = validateCaptcha(gRecaptchaResponse);
                LOGGER.debug(" verifycaptcha::" + verify);
                if (verify) {
                    String resp = createTicket(validateFields.toString(),
                            httpServletAddress).toString();
                    JSONObject respJson = new JSONObject(resp);
                    String ticketNumber =
                            respJson.getString("ticketNumber");
                    String email = respJson.getString(EMAIL_KEY);
                    String lang = validateFields.getString("lang");
                    sentMailNotification(ticketNumber, email, lang);
                    response.getWriter().write(resp);

                } else {
                    validateFields.put(SUCCESS_KEY, SUCCESS_FALSE_VALUE);
                    validateFields.put(ERRORMESSAGE_KEY,
                            "InvalidRecapcha");
                    response.getWriter().write(validateFields.toString());
                }
            } else {
                data.put(SUCCESS_KEY, SUCCESS_FALSE_VALUE);
                data.put(ERRORMESSAGE_KEY, VALIDATION_FAILED_MSG);
                response.getWriter().write(data.toString());
            }
            LOGGER.info("End of Submit Ticket");
        } catch (JSONException | IOException e) {
            response.setContentType(CONTENTTYPE_JSON);
            response.setCharacterEncoding(CHAR_SET);
            try {
                data.put(SUCCESS_KEY, SUCCESS_FALSE_VALUE);
                data.put(ERRORMESSAGE_KEY, e.getMessage());
                response.getWriter().write(data.toString());
            } catch (NullPointerException | JSONException
                | IOException ex) {
                LOGGER.error(" Exception Submit Service call ", ex);
            }
        }

    }

    private void sentMailNotification(String ticketNumber, String email,
            String lang) {
        if (ticketNumber != null && !ticketNumber.equals("")
                && !ticketNumber.equals("null")) {
            MimeMessage mailMessage;
            LOGGER.debug(" lang::" + lang);
            try {
                mailMessage = createMailMessage(ticketNumber, email, lang);
                Transport.send(mailMessage);
            } catch (MessagingException e) {
                e.printStackTrace();
            }
        }

    }

    private MimeMessage createMailMessage(String ticketNumber,
            String email, String lang) throws MessagingException {
        LOGGER.info("createMailMessage: Enter");

        String strTicketNumber = "<ticketnumber>";
        Properties propertiesFile =
                loadProperties("customerserviceconfig.properties");
        String from = propertiesFile.getProperty(CONTACT_FROM_MAIL);
        String to = email;
        LOGGER.debug("sent To :" + to);
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
        msg.setRecipient(Message.RecipientType.TO,
                new InternetAddress(to));
        subject = propertiesFile.getProperty("messageSubject_" + lang);
        StringBuilder sb = new StringBuilder();
        sb.append(
                propertiesFile.getProperty("successMessageBody_" + lang).replace(strTicketNumber, ticketNumber));
        if (lang.equals("ar")) {
            msg.setSubject(subject.replace(strTicketNumber, ticketNumber),
                    CHAR_SET);
            msg.setContent(sb.toString(), "text/html;Charset=UTF-8");
        } else {
            msg.setSubject(subject.replace(strTicketNumber, ticketNumber));
            msg.setContent(sb.toString(), "text/html;Charset=UTF-8");
        }

        LOGGER.debug("msg:" + sb.toString());
        return msg;
    }

    public StringBuilder createTicket(String json,
            String httpServletAddress) throws IOException {
        StringBuilder resp = new StringBuilder();
        String urlBase = "baseUrl";
        String baseUrl;
        String servletAddress = "<servletaddress>";
        Properties propertiesFile =
                loadProperties("customerserviceconfig.properties");
        baseUrl = propertiesFile.getProperty(urlBase);
        baseUrl = baseUrl.replace(servletAddress, httpServletAddress);
        String responseLine = null;
                     
        URL url = new URL(baseUrl);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        try {

            LOGGER.debug(" url::" + url);
            con.setRequestMethod("POST");
            con.setDoOutput(true);
            con.setRequestProperty("Content-Type", CONTENTTYPE_JSON);
            con.setRequestProperty("Accept", CONTENTTYPE_JSON);
            OutputStream os = con.getOutputStream();
            byte[] input = json.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);

            BufferedReader respbr =
                    new BufferedReader(new InputStreamReader(
                            con.getInputStream(), StandardCharsets.UTF_8));
            resp = new StringBuilder();

            while ((responseLine = respbr.readLine()) != null) {
                resp.append(responseLine.trim());
            }
        } catch (NullPointerException | IOException e) {
            BufferedReader resper =
                    new BufferedReader(new InputStreamReader(
                            con.getInputStream(), StandardCharsets.UTF_8));
            while ((responseLine = resper.readLine()) != null) {
                resp.append(responseLine.trim());
            }
        }
        return resp;

    }

    public JSONObject stripXSS(JSONObject data) {
        XssUtils xssUtils = new XssUtils();
        String qid = xssUtils.stripXSS(data.getString("qid"));
        String eid = xssUtils.stripXSS(data.getString("eid"));
        String passport = xssUtils.stripXSS(data.getString(PASSPORT_KEY));
        String nationality =
                xssUtils.stripXSS(data.getString(NATIONALITY_KEY));
        String fullName = xssUtils.stripXSS(data.getString(FULLNAME_KEY));
        String companyName =
                xssUtils.stripXSS(data.getString(COMPANYNAME_KEY));
        String phoneNo =
                xssUtils.stripXSS(data.getString(PHONENUMBER_KEY));
        String emailId = xssUtils.stripXSS(data.getString(EMAIL_KEY));
        String eservice = xssUtils.stripXSS(data.getString(ESERVICE_KEY));
        String service = xssUtils.stripXSS(data.getString(SERVICE_KEY));
        String eServiceName =
                xssUtils.stripXSS(data.getString(ESERVICENAME_KEY));
        String serviceName =
                xssUtils.stripXSS(data.getString(SERVICENAME_KEY));
        String subject = xssUtils.stripXSS(data.getString(SUBJECT_KEY));
        String comments = xssUtils.stripXSS(data.getString(COMMENTS_KEY));
        // update
        data.put("qid", qid);
        data.put("eid", eid);
        data.put(PASSPORT_KEY, passport);
        data.put(NATIONALITY_KEY, nationality);
        data.put(FULLNAME_KEY, fullName);
        data.put(COMPANYNAME_KEY, companyName);
        data.put(PHONENUMBER_KEY, phoneNo);
        data.put(EMAIL_KEY, emailId);
        data.put(ESERVICE_KEY, eservice);
        data.put(SERVICE_KEY, service);
        data.put(ESERVICENAME_KEY, eServiceName);
        data.put(SERVICENAME_KEY, serviceName);
        data.put(SUBJECT_KEY, subject);
        data.put(COMMENTS_KEY, comments);
        return data;
    }

    private boolean validateIdType(JSONObject data) {

        String idType = data.getString("idType");
        String qid = data.getString("qid");
        String eid = data.getString("eid");
        String passport = data.getString(PASSPORT_KEY);
        String nationality = data.getString(NATIONALITY_KEY);

        if (idType.equals("QID")) {
            return validateQIDType(qid, eid);
        } else if (idType.equals("Passport")) {
            return validatePassportType(passport, nationality);
        }
        return false;

    }

    private boolean validateQIDType(String qid, String eid) {
        ValidationErrorList errorList = new ValidationErrorList();
        LOGGER.info("Validate QID : ");
         ESAPI.validator().getValidInput("qId", qid, ESAPIValidator.NUMERIC, 11, false, true, errorList);
        if(errorList.isEmpty()) {
            LOGGER.info("Validate EID : ");
            ESAPI.validator().getValidInput("eId", eid, ESAPIValidator.NUMERIC, 8, true, true, errorList);
            if(errorList.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private boolean validatePassportType(String passport,
            String nationality) {
        LOGGER.info("Validate passport : ");
        if ((passport == null || "".equals(passport.trim())
                || passport.trim().length() > 8)||(nationality != null && !"".equals(nationality.trim())
                && nationality.trim().length() > 50)) {
            return false;
        }
        return true;
    }

    /**
     * this method validates form fields
     *
     * @param data
     * @return
     */
    private JSONObject validateFields(JSONObject data) {
        data = stripXSS(data);
        Iterator<String> keys = data.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            String value = data.getString(key);
            boolean isValid = false;
            switch (key) {
            case "idType":
                isValid = validateIdType(data);
                break;
            case FULLNAME_KEY:
                isValid = validateName(value);
                break;
            case COMPANYNAME_KEY:
                isValid = validateCompanyName(value);
                break;
            case PHONENUMBER_KEY:
                isValid = validatePhoneNumber(value);
                break;
            case EMAIL_KEY:
                isValid = validateMailID(value);
                break;
            case ESERVICE_KEY:
                isValid = validateEservice(value);
                break;
            case SERVICE_KEY:
                isValid = validateService(value);
                break;
            case ESERVICENAME_KEY:
                isValid = validateEserviceName(value);
                break;
            case SERVICENAME_KEY:
                isValid = validateServiceName(value);
                break;
            case SUBJECT_KEY:
                isValid = validateSubject(value);
                break;
            case COMMENTS_KEY:
                isValid = validateComments(value);
                break;
            case ATTACHMENT_KEY:
                JSONObject attachment = data.getJSONObject(ATTACHMENT_KEY);
                isValid = validateAttachment(data);
                double fileSize = attachment.getDouble(FILESIZE_KEY);
                double size = (1 * fileSize);
                double kbFileSize = size / 1024.0;
                attachment.put(FILESIZE_KEY, kbFileSize + " kb");
                data.put(ATTACHMENT_KEY, attachment);
                break;
            default:
                isValid = true;
            }
            if (!isValid) {
                return null;
            }
        }
        return data;

    }

    /**
     * validates comments
     *
     * @param comments
     * @return
     */
    private boolean validateComments(String comments) {
        LOGGER.info("Validate comments : ");
        return !(comments != null && !"".equals(comments.trim())
                && comments.trim().length() > 2500);
    }

    /**
     * validates subject of customer service
     *
     * @param subject
     * @return
     */
    private boolean validateSubject(String subject) {
        LOGGER.info("Validate subject : ");
        return !(subject == null || "".equals(subject.trim())
                || subject.trim().length() > 30);
    }

    /**
     * validates service name
     *
     * @param serviceName
     * @return
     */
    private boolean validateServiceName(String serviceName) {
        LOGGER.info("Validate serviceName : ");
        return !(serviceName == null || "".equals(serviceName.trim()));
    }

    /**
     * validates eservice name
     *
     * @param eServiceName
     * @return
     */
    private boolean validateEserviceName(String eServiceName) {
        LOGGER.info("Validate eServiceName : ");
        return !(eServiceName == null || "".equals(eServiceName.trim()));
    }

    /**
     * validates service
     *
     * @param service
     * @return
     */
    private boolean validateService(String service) {
        LOGGER.info("Validate service :");
        ValidationErrorList errorList = new ValidationErrorList();
        ESAPI.validator().getValidInput(SERVICE_KEY, service, ESAPIValidator.NUMERIC, 10, false, true, errorList);
        return errorList.isEmpty();
    }

    /**
     * validates eservice
     *
     * @param eservice
     * @return
     */
    private boolean validateEservice(String eservice) {
        ValidationErrorList errorList = new ValidationErrorList();
        ESAPI.validator().getValidInput(ESERVICE_KEY, eservice, ESAPIValidator.NUMERIC, 10, false, true, errorList);
        return errorList.isEmpty();
    }

    /**
     * validates email id
     *
     * @param emailId
     * @return
     */
    private boolean validateMailID(String emailId) {
        LOGGER.info("Validate emailId : ");
        ValidationErrorList errorList = new ValidationErrorList();
        ESAPI.validator().getValidInput("email", emailId, ESAPIValidator.EMAIL_ID, 50, false, true, errorList);
        return errorList.isEmpty();
    }

    /**
     * validates phone number
     *
     * @param phoneNo
     * @return
     */
    private boolean validatePhoneNumber(String phoneNo) {
        LOGGER.info("Validate phoneNo : ");
        ValidationErrorList errorList = new ValidationErrorList();
        ESAPI.validator().getValidInput("eId", phoneNo, ESAPIValidator.NUMERIC, 8, false, true, errorList);
        return errorList.isEmpty();
    }

    /**
     * this method validates company name
     *
     * @param companyName
     * @return
     */
    private boolean validateCompanyName(String companyName) {
        LOGGER.info("Validate companyName : ");
        return !(companyName != null && !"".equals(companyName.trim())
                && companyName.trim().length() > 30);
    }

    /**
     * validates the fullname
     *
     * @param fullName
     * @return
     */
    private boolean validateName(String fullName) {
        LOGGER.info("Validate fullName : ");
        return !(fullName == null || "".equals(fullName.trim())
                || fullName.length() > 30);
    }

    /**
     * this method validates the attachment
     *
     * @param data
     * @return
     */
    public boolean validateAttachment(JSONObject data) {
        boolean status = true;
        JSONObject attachment = data.getJSONObject(ATTACHMENT_KEY);
        String fileName = attachment.getString("fileName");
        LOGGER.info("fileName:" + fileName);
        String fileType = attachment.getString("fileType");
        LOGGER.info("fileType:" + fileType);
        double fileSize = attachment.getDouble(FILESIZE_KEY);
        LOGGER.info("fileSize:" + fileSize);
        // check file name contains only alphanumeric and space
        String[] fileNameArray = fileName.split("\\.");
        LOGGER.info("fileNameArray length:" + fileNameArray.length);
        LOGGER.info("fileNameArray:" + fileNameArray);
        if (fileNameArray.length != 2) {
            LOGGER.info(
                    "Customer Service Submit Ticket: Filename in valid");
            return false;
        }
        String fileExtension = fileNameArray[1].toLowerCase();
        LOGGER.info("fileExtension:" + fileExtension);
        if (fileExtension.equals("")) {
            LOGGER.info(
                    "Customer Service Submit Ticket:no file extension");

            return false;
        }
        if (!fileExtension.equals("jpg") && !fileExtension.equals("png")
                && !fileExtension.equals("gif")
                && !fileExtension.equals("jpeg")
                && !fileExtension.equals("pdf")
                && !fileExtension.equals("doc")
                && !fileExtension.equals("docx")) {
            LOGGER.info(
                    "Customer Service Submit Ticket file:invalid file extension");
            return false;
        }
        if (fileName.length() > 50) {
            LOGGER.info(
                    "Customer Service Submit Ticket regex:invalid file name length");
            return false;
        }

        Pattern p = Pattern.compile("^[ 0-9A-Za-z]+$");
        Matcher m = p.matcher(fileNameArray[0]);
        boolean b = m.matches();
        if (!b) {
            LOGGER.info(
                    "Customer Service Submit Ticket regex:invalid file Name");
            return false;
        }
        if (!validateFileSize(fileSize)) {
            LOGGER.info(
                    "Customer Service Submit Ticket regex:invalid file Size");
            return false;
        }

        return status;
    }

    public boolean validateFileSize(double kbFileSize) {
        return kbFileSize < 2000 * 1024;
    }

    /**
     * This method validates Google reCAPTCHA.
     *
     * @param captchaResponse Captcha response.
     *
     * @return Returns true if validation successful, false otherwise.
     */
    public boolean validateCaptcha(String captchaResponse) {
        LOGGER.debug("GoogleRecaptchaUtil : validateCaptch");
        boolean isCaptchaValid = false;
        try {
            Properties properties =
                    loadProperties("captchaconfig.properties");
            String params = "secret=" + properties.getProperty("secretKey")
                    + "&response=" + captchaResponse;

            HttpURLConnection httpConn = (HttpURLConnection) new URL(
                    properties.getProperty("baseUrl")).openConnection();
            httpConn.setDoOutput(true);
            httpConn.setRequestMethod("POST");
            httpConn.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded; charset=UTF-8");
            OutputStream outStream = httpConn.getOutputStream();
            outStream.write(params.getBytes(StandardCharsets.UTF_8));
            outStream.flush();
            outStream.close();

            BufferedReader bufferReader = new BufferedReader(
                    new InputStreamReader(httpConn.getInputStream(),
                            StandardCharsets.UTF_8));
            String line;
            StringBuilder response = new StringBuilder();
            while ((line = bufferReader.readLine()) != null) {
                response.append(line);
            }
            bufferReader.close();
            LOGGER.debug("response : " + response.toString());

            JSONObject json = new JSONObject(response.toString());
            LOGGER.debug("isValid : " + json.getBoolean(SUCCESS_KEY));
            isCaptchaValid = json.getBoolean(SUCCESS_KEY);
        } catch (Exception e) {
            LOGGER.error("Exception in GoogleRecaptchaUtil", e);
        }
        return isCaptchaValid;
    }

    /**
     * This method will be used to load the configuration properties.
     *
     * @param context The parameter context object passed from Component.
     * @throws IOException
     * @throws MalformedURLException
     *
     */
    private static Properties
            loadProperties(final String propertiesFileName) {
        LOGGER.info("Loading Properties File from Request Context.");
        Properties propFile = new Properties();
        if (propertiesFileName != null && !propertiesFileName.equals("")) {
            String root =
                    "/usr/opentext/LiveSiteDisplayServices/runtime/web/iw/config/properties";
            
            try(InputStream inputStream= new FileInputStream(
                    root + "/" + propertiesFileName)) {
                
                propFile.load(inputStream);
                LOGGER.info("Properties File Loaded");
            } catch (MalformedURLException e) {
                LOGGER.error(
                        "Malformed URL Exception while loading Properties file : ",
                        e);
            } catch (IOException e) {
                LOGGER.error(
                        "IO Exception while loading Properties file : ",
                        e);
            }

        } else {
            LOGGER.info("Invalid / Empty properties file name.");
        }
        LOGGER.info("Finish Loading Properties File.");
        return propFile;
    }

}
