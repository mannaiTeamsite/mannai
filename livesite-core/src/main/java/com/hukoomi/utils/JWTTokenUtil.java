package com.hukoomi.utils;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.Properties;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import com.interwoven.livesite.runtime.RequestContext;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;

public class JWTTokenUtil {
	private Properties properties = null;
	/**
	 * Logger object to log information
	 */
	private static final Logger logger = Logger.getLogger(JWTTokenUtil.class);

	/**
	 * This constructor will be called for creating database connection.
	 * 
	 * @param context Request context object.
	 *
	 */
	public JWTTokenUtil(RequestContext context) {
		logger.info("JWTTokenUtil : Loading Properties....");
		properties = JWTTokenUtil.loadProperties(context);
		logger.info("Postgre : Properties Loaded");
	}

	/**
	 * This method will be used to load the configuration properties.
	 * 
	 * @param context Request context object.
	 * 
	 */
	private static Properties loadProperties(final RequestContext context) {
		logger.info("loadProperties:Begin");
		PropertiesFileReader prop = null;
		prop = new PropertiesFileReader(context, "dashboard.properties");
		return prop.getPropertiesFile();

	}

	@SuppressWarnings("deprecation")
	public String parseJwt(String jwtString) throws InvalidKeySpecException, NoSuchAlgorithmException,
			IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException {
		String data = null;
		try {
			String strDate = null;
			String rsaSignPublicKey = properties.getProperty("RSASignaturePublicKey");

			String rsaPayloadPublicKey = properties.getProperty("RSAPayloadPublicKey");
			PublicKey publicKey = getPublicKey(rsaSignPublicKey);

			Jws<Claims> jwt;
			jwt = Jwts.parser().setSigningKey(publicKey).parseClaimsJws(jwtString);

			Date exp = jwt.getBody().getExpiration();
			if (exp != null) {
				SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
				strDate = formatter.format(exp);
			}

			String encryptedData = jwt.getBody().getSubject();

			publicKey = getPublicKey(rsaPayloadPublicKey);
			data = decrypt(encryptedData, publicKey);
			JSONObject jsonObj = new JSONObject(data);
			jsonObj.put("exp", strDate);
			data = jsonObj.toString();

		} catch (Exception e) {
			logger.error("Exception", e);
		}
		return data;
	}

	public static String decrypt(String data, PublicKey publicKey) throws IllegalBlockSizeException,
			BadPaddingException, NoSuchPaddingException, NoSuchAlgorithmException, java.security.InvalidKeyException {
		logger.info("decrypt method called");
		Cipher cipher = Cipher.getInstance("RSA"); // Compliant
		cipher.init(Cipher.DECRYPT_MODE, publicKey);
		return new String(cipher.doFinal(Base64.getDecoder().decode(data.getBytes())), StandardCharsets.UTF_8);
	}

	private static PublicKey getPublicKey(String rsaPublicKey)
			throws NoSuchAlgorithmException, InvalidKeySpecException {
		rsaPublicKey = rsaPublicKey.replace("-----BEGIN PUBLIC KEY-----", "");
		rsaPublicKey = rsaPublicKey.replace("-----END PUBLIC KEY-----", "");
		X509EncodedKeySpec keySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(rsaPublicKey));
		KeyFactory kf = KeyFactory.getInstance("RSA");
		return kf.generatePublic(keySpec);

	}

	public static String decodeToArabicString(String encodedArabicString) {
		byte[] charset = encodedArabicString.getBytes(StandardCharsets.UTF_8);
		return new String(charset, StandardCharsets.UTF_8);
	}

}
