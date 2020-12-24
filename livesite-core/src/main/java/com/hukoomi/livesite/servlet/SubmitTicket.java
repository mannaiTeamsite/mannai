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
        try {
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(request.getInputStream()));
            String json = "";
            json = br.readLine();
            String httpServletAddress = request.getLocalAddr();
            JSONObject data = new JSONObject(json);
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
                con.setRequestProperty("Content-Type", "application/json");
                con.setRequestProperty("Accept", "application/json");
                OutputStream os = con.getOutputStream();
                byte[] input = json.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);

                BufferedReader respbr = new BufferedReader(
                        new InputStreamReader(con.getInputStream(),
                                StandardCharsets.UTF_8));
                resp = new StringBuilder();
                String responseLine = null;
                while ((responseLine = respbr.readLine()) != null) {
                    resp.append(responseLine.trim());
                }
            }

        } catch (IOException e) {
            response.setContentType("text/html;charset=UTF-8");
            try {
                response.getWriter().write("No data to create ticket");
            } catch (IOException e1) {
                LOGGER.error(" Exception Submit Service call ", e);
            }

        } catch (Exception e) {
            LOGGER.error(" Exception Submit Service call ", e);
        } finally {
            LOGGER.info("End of Submit Ticket");
        }
        response.setContentType("text/html;charset=UTF-8");
        try {
            if (verify) {
                if (resp != null) {
                    response.getWriter().write(resp.toString());
                }
            } else {
                response.getWriter().write("Recapcha Error! ");
            }
        } catch (IOException e) {
            LOGGER.error("Exception  Submit Service call", e);
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
    private static Properties loadProperties(
            final String propertiesFileName)  {
        LOGGER.info("Loading Properties File from Request Context.");
        Properties propFile = new Properties();
        if (propertiesFileName != null && !propertiesFileName.equals("")) {
            String root = "/usr/opentext/LiveSiteDisplayServices/runtime/web/iw/config/properties";
            InputStream inputStream;
            try {
                inputStream = new FileInputStream(root + "/" + propertiesFileName);
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
