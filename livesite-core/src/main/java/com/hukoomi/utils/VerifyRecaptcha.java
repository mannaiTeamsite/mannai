package com.hukoomi.utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.net.ssl.HttpsURLConnection;

import org.apache.log4j.Logger;

public class VerifyRecaptcha {
	private static final Logger log = Logger.getLogger(VerifyRecaptcha.class);
	public static final String url = "https://www.google.com/recaptcha/api/siteverify";
	
//	Production secret key
	public static final String secret = "6LdOH9cZAAAAAM6Wb4AAXH22FsVyayS9XtfpAK75";
	
	private final static String USER_AGENT = "Mozilla/5.0";

	public static boolean verify(String gRecaptchaResponse) throws IOException {
		log.debug("Entering->VerifyRecaptcha->verify"); 

		if (gRecaptchaResponse == null || "".equals(gRecaptchaResponse)) {
			log.debug("gRecaptchaResponse is null... ");
			return false;
		}

		try{
			URL obj = new URL(url);
			HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

			// add reuqest header
			con.setRequestMethod("POST");
			con.setRequestProperty("User-Agent", USER_AGENT);
			con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

			String postParams = "secret=" + secret + "&response="+ gRecaptchaResponse;

			
			// Send post request
			con.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes(postParams);
			wr.flush();
			wr.close();

			int responseCode = con.getResponseCode();
			log.debug("\nSending 'POST' request to URL : " + url);
			log.debug("Post parameters : " + postParams);
			log.debug("Response Code : " + responseCode);

			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();

			// print result
			log.debug(response.toString());

			//parse JSON response and return 'success' value
			JsonReader jsonReader = Json.createReader(new StringReader(response.toString()));
			JsonObject jsonObject = jsonReader.readObject();
			jsonReader.close();

			log.debug("Exiting->VerifyRecaptcha->verify");
			return jsonObject.getBoolean("success");
		}catch(Exception e){
			log.error("ERROR IN RECAPTCHA::"+e);
			e.printStackTrace();
			return false;
		}
	}
}
