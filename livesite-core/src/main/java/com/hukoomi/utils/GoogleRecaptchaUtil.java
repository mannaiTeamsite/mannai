package com.hukoomi.utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.log4j.Logger;
import org.json.JSONObject;


public class GoogleRecaptchaUtil {
	/** Logger object to check the flow of the code.*/
	private final Logger logger = Logger.getLogger(GoogleRecaptchaUtil.class);
	public static final String baseUrl = "https://www.google.com/recaptcha/api/siteverify";
	public static final String secretKey = "6LctK9YZAAAAALIuhAbmFZhK7a8TVmRiemRr2Bv2";
	
	/**
	 * Validates Google reCAPTCHA.
	 *
	 * @param secretKey 
	 * @param captchaResponse 
	 * @return true if validation successful, false otherwise.
	 */
	public boolean validateCaptch(String captchaResponse) {
		logger.debug("GoogleRecaptchaUtil : validateCaptch");
	    try {
	        String params = "secret=" + secretKey + "&response=" + captchaResponse;
	        //logger.debug("baseUrl : "+baseUrl);
	        //logger.debug("secretKey : "+secretKey);
	        //logger.debug("params : "+params);
	        
	        HttpURLConnection httpConn = (HttpURLConnection) new URL(baseUrl).openConnection();
	        httpConn.setDoOutput(true);
	        httpConn.setRequestMethod("POST");
	        httpConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
	        OutputStream outStream = httpConn.getOutputStream();
	        outStream.write(params.getBytes("UTF-8"));
	        outStream.flush();
	        outStream.close();

	        BufferedReader bufferReader = new BufferedReader(new InputStreamReader(httpConn.getInputStream(), "UTF-8"));
	        String line;
			StringBuffer response = new StringBuffer();
			while ((line = bufferReader.readLine()) != null) {
				response.append(line);
			}
			bufferReader.close();
			logger.debug("response : "+response.toString());
			
			JSONObject json = new JSONObject(response.toString());	 
			logger.debug("isValid : "+json.getBoolean("success"));
	        return json.getBoolean("success");
	    } catch (Exception e) {
	    	
	    }
	    return false;
	}
	
	/*public static void main(String[] args) {
		GoogleRecaptchaUtil captchUtil = new GoogleRecaptchaUtil();
		boolean resp  = captchUtil.validateCaptch("03AGdBq260ACxiJjjp7NLCwo4LMj9GsyDxY8g4ZUGN47cMFMkNdzkLZRRI_rt1qvLp5E-zrCVj-2GvGZm_Y_s7kR7Wkz7mHcgVHsyFc-eucnaCNZJh-z_7P3qPhptoOwFUmIXNi4JwN3CBXXMb1LKSo18XNXhe6hldPNWNYsEbrSpkGYzp7KM1K21i58TVIDDz7RFP3q5r37gcPtLmGer8WhuKy1WKSBFgU0TLwGuGCN41yh_4V1DPV1GHrAnrZ9pH4EKPn-n8gv7iYV2bJVv9k8a9jMH3TyfwuYGTUYuyIWbigGdkf6zmoi4rJzMwIMVNjqkwJggVt00pJEAaxVyFZa0rGeKZxtjYIVfO3vfaXm1t9HWoaTPRR2jEiQNzdOPLIMkoFwdfSfInPfk6ZGMv3GDo5NyPKaTcXlGfi_FNqGx73eAv9-ZwIBOUi3JDk7k_6gtGcdEvYOZwk9TY7HX7yqmxl515W1cUcg");
		System.out.println("Captcha Validation : "+resp);
	}*/
}
