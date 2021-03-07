package com.hukoomi.utils;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Properties;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.json.JSONObject;
import org.json.XML;

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

	public static String parseJwt(String jwtString)
			throws InvalidKeySpecException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException,
			BadPaddingException, NoSuchPaddingException, java.security.InvalidKeyException {

		  String rsaSignPublicKey = "-----BEGIN PUBLIC KEY-----"
		            + "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAwLal8wA2ml3vIbc6yqE0"
		            + "5kGRCoC7NzvWlQ1OyMDGY7v1VZh96Ua7JoDJE7fzQIUhahapBxD0+3H8VVsdAcdE"
		            + "8N6KArEgz+3rSUIOBnf1pPbHirVD3h+S7vWCXr/2h9/SM0kg76DXQW6KJyEnHvEh"
		            + "gQgNGzueCcdSbHH9Bxk+I/Deot669L15dsCmmPrlc+sy5yqxD3qodnYN7jKTBCYN"
		            + "U+EeP9bnjojtjX/QN2Aj3pG/MLCknh3ix5ChTPy5tyi9wsJ+eM6s51l6ROvSz39l"
		            + "wOrUP+xTwhAWS/VoZQpb4y4flfDhdY3JquGSoi78RDVenbyvUe/QQN/xwwKewVgL"
		            + "YQIDAQAB"
		            + "-----END PUBLIC KEY-----";
		     String rsaPayloadPublicKey = "-----BEGIN PUBLIC KEY-----"
		    + "MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEAyGUWVPdJIeSQnsqq8Y4X"
		    + "ZnirIDNATwG6aEeF+aZf7M6PBJaNR77MVsWboZ92hnZ5svow+BW5Ze9em6VNArEf"
		    + "Hu/qXb67uQBP39/wpR19MXhq8vWUfmvqaI6JvXXYbn3r4CmL25XYmnW8IqFnyVb+"
		    + "CPW1oYyQoJ+RzgRRLIhEgIMdQdmxxSLJBWTUV6IdEMg3pLASDBtvtDNTR58eSfH8"
		    + "oP31z3a0nq7CyzbRYmFsznl6MdBv7gps9m/lH5udVmES/1V0BDWl2Mn67sDHBVjK"
		    + "RETRWudDOvZ2JJxjmwAu9VBJx3VAjzvcKsj4YngV4BLxHhIQ9HFYQ6C9g8Iiizx8"
		    + "vhBY5YHEiThhC+Mhp3hBjDkUwfEgNj0pDQC32baXwwOXnV0Gj15o7b3E3EYYLrRN"
		    + "BINJZYMF96FBDkYTGE10L4tLmgxFI7keAOLSbYeifHAnj7cwaOghQZ8XGBLF15AY"
		    + "lk21uOnAMvbc9hHm8srm6RxRCjpF+hX1QpScOdrPRxZhi/1MZ+/HJaaPkeCJw0Q8"
		    + "XOna7BfF6BAY2e6IDMcKre3rbND+bZ+gK4Mj1qmyScz4tSnjAKS3QATumCujNqWc"
		    + "eGbf8bySU2LFAHtWY0J/uWIz+fFlIChmr+xSiASreXgQ9x6KvtygjBhS3kLyVrLY"
		    + "POlSiGtrFu+w8YvvXfv11BkCAwEAAQ=="
		    + "-----END PUBLIC KEY-----";
		
		//String rsaSignPublicKey = properties.getProperty("RSASignaturePublicKey");
		logger.info("rsaSignPublicKey :" + rsaSignPublicKey);
		//String rsaPayloadPublicKey = properties.getProperty("RSAPayloadPublicKey");
		PublicKey publicKey = getPublicKey(rsaSignPublicKey);
		logger.info("rsaPayloadPublicKey :" + rsaPayloadPublicKey);
		logger.info("publicKey :" + publicKey);
		String data = null;

		Jws<Claims> jwt;
		logger.info("Jwts object :" + Jwts.parser().setSigningKey(publicKey));
		jwt = Jwts.parser().setSigningKey(publicKey).parseClaimsJws(jwtString);
		String encryptedData = jwt.getBody().getSubject();
		logger.info("encryptedData:" + encryptedData);
		publicKey = getPublicKey(rsaPayloadPublicKey);
		logger.info("publicKey:" + publicKey);
		data = decrypt(encryptedData, publicKey);
		logger.info("data:" + data);
		System.out.println("Data :" + data);
		return data;
	}

	public static String decrypt(String data, PublicKey publicKey)
			throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException,
			NoSuchAlgorithmException, java.security.InvalidKeyException {
		logger.info("decrypt method called");
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.DECRYPT_MODE, publicKey);
		return new String(cipher.doFinal(Base64.getDecoder().decode(data.getBytes())), StandardCharsets.UTF_8);
	}

	private static PublicKey getPublicKey(String rsaPublicKey)
			throws NoSuchAlgorithmException, InvalidKeySpecException {
		rsaPublicKey = rsaPublicKey.replace("-----BEGIN PUBLIC KEY-----", "");
		rsaPublicKey = rsaPublicKey.replace("-----END PUBLIC KEY-----", "");
		X509EncodedKeySpec keySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(rsaPublicKey));
		KeyFactory kf = KeyFactory.getInstance("RSA");
		PublicKey publicKey = kf.generatePublic(keySpec);
		return publicKey;
	}

	public static String decodeToArabicString(String encodedArabicString) {
		byte[] charset = encodedArabicString.getBytes(StandardCharsets.UTF_8);
		return new String(charset, StandardCharsets.UTF_8);
	}

	public static void main(String[] args) throws InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException,
			InvalidKeyException, IllegalBlockSizeException, BadPaddingException, java.security.InvalidKeyException {
		
		parseJwt(
				"eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJqdGkiOiJiNGM1ZmM1Ni1lZGZlLTQxODgtOWViNi1lMGIwY2U5Nzc5MmQiLCJpYXQiOjE2MTUxMzY5NDQsInN1YiI6IkxnalBOT0paRDVPdGZMeDBZbnVudk13THNMMnhKajBEcWdsazFZd2x6SS9zcjJUYWlQUG53bHFBSE1YcXJLdWdTUVBWdWd5Tjlna2xMVFkyOE1Kd2NLc2MwMFU1V1FpZ09tS2dJNmhmR01yN3dKM1VycDZUUjJ3azZrUEdybW41blErQzBYMXh6aFM0K015ZVBGc2xVQVpyRnAyQmxDeXJVSHh4M3JTTFhEZ2JQWnBDQWgzUzhMTW5WSVFacUtxT2IrVE1OOUJpSC9wTmxWTGFKdnBNQzhWMUhkbzVTb3RvZmF2eExrSTFEZzloZGIyOWZOTHZ3dzhQRVRxdXlLZTVjWG91MHR2QUdaOXJMNGkvSHMxR29hQ0QrMVg1bG1XV1BpV1dpcVpzMmVXMlJLUlY5OXJTR0N2UWRYR3pueHNxbWtXVWRJYjZWOWVzWVg4ZERxUkZyZU1yMTdOd0s3dGRuYUErNUxqVk5JVHRnY2FEYXNYZEdnek9vVTJLSHEzRWJWR2d1TUZJMytIWkdMdkxaUGZ4OWxCSlBHUXN1bnpyWVQyODIvR00wdk1qSHIvSGNwYVV3Kzk1WGFGYXZBczlTU3BHWUVzOXBVT0VKWnZidnJOQmp1RzFEd1ZxVHE2Sjd6WHlPMUpnRUQvNU5CMUdNaGZORGd5LzhYMk5WeXh6VXJKbmVWREdUeEI4anFiZUdDb2lmdVFmMnBrS3hYY2dmU1lIc3p6cUFuZ2JBdFVPMjFtaHhFSFBlam5NU2RDNDNYK3dTelBNSW9nZmlYY2NrR2ZCdFBpU0h3UE9iRW9Vd1QrWkpNMHBkaXhZQndZc2F6NnlwQzZ1Mlk1b0FQY0N4eit2UFZsMVN3SFNUcWZ4d3VlZVNHTVhZWmZPM092SlpZRmJjcXZ1NWtnPSIsImlzcyI6Ik5BUy1TUCIsImF1ZCI6Ikh1a29vbWkiLCJleHAiOjE2MTUxMzg3NDR9.EtCPqUSsrWFM6FIEste2IPRLuIuscpRt8Ous3t0kXOxNrb-SIDLcVmTZh6M18pjRZaMQyfmsIrrRbwOV_KVcoiSf4oePzbz3C47crDfQ_TfOILoT1FHh2hooKq9V1ncJIYf8C4FA_thpGSHbeZXWF5A4Yb2ePDwk1X8UauURCl37xkYSvNhBWmqTI7GxEvxuM9M_T7aGxUve3dqjwZmFWPvQLV7OSwYk_wWp5RF-jMJnQOCDEG2q-qv3ozIbXT5Efa9EsnlCh0eZgNpMUdq6sCuhfCEmfOah6ZkWADthJUrb_x4OOZjBbD6drtQXE3RbdUyoVPxxwYT6zh7CSAgX3Q");
	}
}