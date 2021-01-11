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
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.hukoomi.utils.ValidationUtils;
import com.hukoomi.utils.XssUtils;

@MultipartConfig
public class SubmitTicket extends HttpServlet {

    /** logger.debug object to check the flow of the code. */
    private static final Logger LOGGER =
            Logger.getLogger(SubmitTicket.class);
    /**
     * Properties object that holds the property values
     */
    private static Properties properties = null;

    /**
     * @see HttpServlet#doPost(HttpServletRequest request,
     *      HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response) throws ServletException {
        LOGGER.info("Customer Service Submit Ticket: Start");
        StringBuilder resp = null;
        boolean verify = true;
        JSONObject data = null;
        try {
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(request.getInputStream()));
            String json = "";
            json = br.readLine();
            String httpServletAddress = request.getLocalAddr();
            data = new JSONObject(json);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            JSONObject validateFields = validateFields(data);
            if (validateFields != null) {
                String gRecaptchaResponse =
                        data.getString("recaptchaResponseField");
                verify = validateCaptcha(gRecaptchaResponse);
                LOGGER.debug(" verifycaptcha::" + verify);
                if (verify) {
                    URL url = null;
                    url = new URL("http://" + httpServletAddress + ":"
                            + "8082/api/contact/center/ticket");
                    LOGGER.debug(" url::" + url);
                    HttpURLConnection con =
                            (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("POST");
                    con.setDoOutput(true);
                    con.setRequestProperty("Content-Type",
                            "application/json");
                    con.setRequestProperty("Accept", "application/json");
                    OutputStream os = con.getOutputStream();
                    byte[] input = validateFields.toString()
                            .getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);

                    BufferedReader respbr = new BufferedReader(
                            new InputStreamReader(con.getInputStream(),
                                    StandardCharsets.UTF_8));
                    resp = new StringBuilder();
                    String responseLine = null;
                    while ((responseLine = respbr.readLine()) != null) {
                        resp.append(responseLine.trim());
                    }
                } else {
                    validateFields.put("success", "false");
                    validateFields.put("errorMessage", "InvalidRecapcha");
                    response.getWriter().write(validateFields.toString());
                }
            } else {
                data.put("success", "false");
                data.put("errorMessage", "validationFailed");
                response.getWriter().write(data.toString());
            }

        } catch (IOException e) {
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            try {
                data.put("success", "false");
                data.put("errorMessage", e.getMessage());
                response.getWriter().write(data.toString());
            } catch (IOException e1) {
                LOGGER.error(" Exception Submit Service call ", e);
            }
        } finally {
            LOGGER.info("End of Submit Ticket");
        }
        try {
            response.getWriter().write(resp.toString());
        } catch (IOException e) {
            LOGGER.error("Customer Service Query Ticket: Exception ", e);
        }
    }

    /**
     * this method validates form fields
     *
     * @param data
     * @return
     */
    private JSONObject validateFields(JSONObject data) {
        ValidationUtils util = new ValidationUtils();
        String idType = data.getString("idType");
        XssUtils xssUtils = new XssUtils();
        if (idType.equals("QID") || idType.equals("Passport")) {
            String qid = xssUtils.stripXSS(data.getString("qid"));
            String eid = xssUtils.stripXSS(data.getString("eid"));
            String passport =
                    xssUtils.stripXSS(data.getString("passport"));
            String nationality =
                    xssUtils.stripXSS(data.getString("nationality"));
            String fullName =
                    xssUtils.stripXSS(data.getString("fullName"));
            String companyName =
                    xssUtils.stripXSS(data.getString("companyName"));
            String phoneNo = xssUtils.stripXSS(data.getString("phoneNo"));
            String emailId = xssUtils.stripXSS(data.getString("emailId"));
            String eservice =
                    xssUtils.stripXSS(data.getString("eservice"));
            String service = xssUtils.stripXSS(data.getString("service"));
            String eServiceName =
                    xssUtils.stripXSS(data.getString("eServiceName"));
            String serviceName =
                    xssUtils.stripXSS(data.getString("serviceName"));
            String subject = xssUtils.stripXSS(data.getString("subject"));
            String comments =
                    xssUtils.stripXSS(data.getString("comments"));
            if (idType.equals("QID")) {
                LOGGER.info("Validate QID : ");
                if (qid == null || "".equals(qid.trim())) {
                    return null;
                } else if (!util.validateNumeric(qid)
                        || qid.trim().length() != 11) {
                    return null;
                }
                LOGGER.info("Validate eid : ");
                if (eid != null && !"".equals(eid.trim())) {
                    if (!util.validateNumeric(eid)
                            || eid.trim().length() != 8) {
                        return null;
                    }
                }
            } else if (idType.equals("Passport")) {
                LOGGER.info("Validate passport : ");
                if (passport == null || "".equals(passport.trim())) {
                    return null;
                } else if (passport.trim().length() > 8) {
                    return null;
                }
                LOGGER.info("Validate nationality : ");
                if (nationality != null || !"".equals(passport.trim())) {
                    if (nationality.length() > 50) {
                        return null;
                    }
                }
            }
            LOGGER.info("Validate fullName : ");
            if (fullName == null || "".equals(fullName.trim())) {
                return null;
            } else if (fullName.length() > 30) {
                return null;
            }
            LOGGER.info("Validate phoneNo : ");
            if (phoneNo == null || "".equals(phoneNo.trim())) {
                return null;
            } else if (!util.validateNumeric(phoneNo)
                    || (phoneNo.trim().length() != 8)) {
                return null;
            }
            LOGGER.info("Validate emailId : ");
            if (emailId == null || "".equals(emailId.trim())) {
                return null;
            } else if (!util.validateEmailId(emailId)
                    || emailId.trim().length() > 50) {
                return null;
            }
            LOGGER.info("Validate eservice : ");
            if (eservice == null || "".equals(eservice.trim())) {
                return null;
            } else if (!util.validateNumeric(eservice)) {
                return null;
            }
            LOGGER.info("Validate service :");
            if (service == null || "".equals(service.trim())) {
                return null;
            } else if (!util.validateNumeric(service)) {
                return null;
            }
            LOGGER.info("Validate eServiceName : ");
            if (eServiceName == null || "".equals(eServiceName.trim())) {
                return null;
            }
            LOGGER.info("Validate serviceName : ");
            if (serviceName == null || "".equals(serviceName.trim())) {
                return null;
            }
            LOGGER.info("Validate companyName : ");
            if (companyName != null && !"".equals(companyName.trim())) {
                if (companyName.trim().length() > 30) {
                    return null;
                }
            }
            LOGGER.info("Validate subject : ");
            if (subject == null || "".equals(subject.trim())) {
                return null;
            } else if (subject.trim().length() > 30) {
                return null;
            }
            LOGGER.info("Validate comments : ");
            if (comments != null || !"".equals(comments.trim())) {
                if (comments.trim().length() > 2500) {
                    return null;
                }
            }
            // update
            data.put("qid", qid);
            data.put("eid", eid);
            data.put("passport", passport);
            data.put("nationality", nationality);
            data.put("fullName", fullName);
            data.put("companyName", companyName);
            data.put("phoneNo", phoneNo);
            data.put("emailId", emailId);
            data.put("eservice", eservice);
            data.put("service", service);
            data.put("eServiceName", eServiceName);
            data.put("serviceName", serviceName);
            data.put("subject", subject);
            data.put("comments", comments);
            return data;
        } else {
            return null;
        }

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
            properties = loadProperties("captchaconfig.properties");
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
            LOGGER.debug("isValid : " + json.getBoolean("success"));
            isCaptchaValid = json.getBoolean("success");
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
            InputStream inputStream;
            try {
                inputStream = new FileInputStream(
                        root + "/" + propertiesFileName);
                if (inputStream != null) {
                    propFile.load(inputStream);
                    LOGGER.info("Properties File Loaded");
                }
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
