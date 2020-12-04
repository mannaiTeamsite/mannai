package com.hukoomi.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.interwoven.livesite.runtime.RequestContext;
/**
 * GoogleRecaptchaUtil is the Google Recaptcha util class.
 * 
 * @author Vijayaragavamoorthy
 *
 */
public class GoogleRecaptchaUtil {
    /** Logger object to check the flow of the code. */
    private final Logger logger = Logger
            .getLogger(GoogleRecaptchaUtil.class);
    
    /**
     * Properties object that holds the property values
     */
    private static Properties properties = null;

    /**
     * This method validates Google reCAPTCHA.
     *
     * @param context Request Context object. 
     * @param captchaResponse Captcha response.
     * 
     * @return Returns true if validation successful, false otherwise.
     */
    public boolean validateCaptcha(final RequestContext context, String captchaResponse) {
        logger.debug("GoogleRecaptchaUtil : validateCaptch");
        boolean isCaptchaValid = false;
        try {
            GoogleRecaptchaUtil.loadProperties(context);
            String params = "secret=" + properties.getProperty("secretKey") + "&response="
                    + captchaResponse;

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
            logger.debug("response : " + response.toString());

            JSONObject json = new JSONObject(response.toString());
            logger.debug("isValid : " + json.getBoolean("success"));
            isCaptchaValid = json.getBoolean("success");
        } catch (Exception e) {
            logger.error(
                    "Exception in GoogleRecaptchaUtil", e);
        }
        return isCaptchaValid;
    }
    
    /**
     * This method will be used to load the configuration properties.
     * 
     * @param context The parameter context object passed from Component.
     * 
     */
    private static void loadProperties(final RequestContext context) {
        if(properties == null) {
            PropertiesFileReader propertyFileReader = new PropertiesFileReader(
                    context, "captchaconfig.properties");
            GoogleRecaptchaUtil.properties = propertyFileReader.getPropertiesFile();
        }
    }
}
