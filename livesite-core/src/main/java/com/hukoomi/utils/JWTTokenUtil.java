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
	public String parseJwt(String jwtString) throws InvalidKeySpecException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException, java.security.InvalidKeyException {
//		  String rsaSignPublicKey = "-----BEGIN PUBLIC KEY-----"
//		            + "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAwLal8wA2ml3vIbc6yqE0"
//		            + "5kGRCoC7NzvWlQ1OyMDGY7v1VZh96Ua7JoDJE7fzQIUhahapBxD0+3H8VVsdAcdE"
//		            + "8N6KArEgz+3rSUIOBnf1pPbHirVD3h+S7vWCXr/2h9/SM0kg76DXQW6KJyEnHvEh"
//		            + "gQgNGzueCcdSbHH9Bxk+I/Deot669L15dsCmmPrlc+sy5yqxD3qodnYN7jKTBCYN"
//		            + "U+EeP9bnjojtjX/QN2Aj3pG/MLCknh3ix5ChTPy5tyi9wsJ+eM6s51l6ROvSz39l"
//		            + "wOrUP+xTwhAWS/VoZQpb4y4flfDhdY3JquGSoi78RDVenbyvUe/QQN/xwwKewVgL"
//		            + "YQIDAQAB"
//		            + "-----END PUBLIC KEY-----";
//		     String rsaPayloadPublicKey = "-----BEGIN PUBLIC KEY-----"
//		    + "MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEAyGUWVPdJIeSQnsqq8Y4X"
//		    + "ZnirIDNATwG6aEeF+aZf7M6PBJaNR77MVsWboZ92hnZ5svow+BW5Ze9em6VNArEf"
//		    + "Hu/qXb67uQBP39/wpR19MXhq8vWUfmvqaI6JvXXYbn3r4CmL25XYmnW8IqFnyVb+"
//		    + "CPW1oYyQoJ+RzgRRLIhEgIMdQdmxxSLJBWTUV6IdEMg3pLASDBtvtDNTR58eSfH8"
//		    + "oP31z3a0nq7CyzbRYmFsznl6MdBv7gps9m/lH5udVmES/1V0BDWl2Mn67sDHBVjK"
//		    + "RETRWudDOvZ2JJxjmwAu9VBJx3VAjzvcKsj4YngV4BLxHhIQ9HFYQ6C9g8Iiizx8"
//		    + "vhBY5YHEiThhC+Mhp3hBjDkUwfEgNj0pDQC32baXwwOXnV0Gj15o7b3E3EYYLrRN"
//		    + "BINJZYMF96FBDkYTGE10L4tLmgxFI7keAOLSbYeifHAnj7cwaOghQZ8XGBLF15AY"
//		    + "lk21uOnAMvbc9hHm8srm6RxRCjpF+hX1QpScOdrPRxZhi/1MZ+/HJaaPkeCJw0Q8"
//		    + "XOna7BfF6BAY2e6IDMcKre3rbND+bZ+gK4Mj1qmyScz4tSnjAKS3QATumCujNqWc"
//		    + "eGbf8bySU2LFAHtWY0J/uWIz+fFlIChmr+xSiASreXgQ9x6KvtygjBhS3kLyVrLY"
//		    + "POlSiGtrFu+w8YvvXfv11BkCAwEAAQ=="
//		    + "-----END PUBLIC KEY-----";
		String rsaSignPublicKey = properties.getProperty("RSASignaturePublicKey");
		 logger.info("rsaSignPublicKey :"+rsaSignPublicKey);
		String rsaPayloadPublicKey = properties.getProperty("RSAPayloadPublicKey");
	    PublicKey publicKey = getPublicKey(rsaSignPublicKey);
	    logger.info("rsaPayloadPublicKey :"+rsaPayloadPublicKey);	
	    logger.info("publicKey :"+publicKey);	
	    String data = null;	
	    
	    Jws<Claims> jwt;
	    logger.info("Jwts object :"+Jwts.parser().setSigningKey(publicKey));
		 jwt = Jwts.parser().setSigningKey(publicKey).parseClaimsJws(jwtString);
		 String encryptedData =   jwt.getBody().getSubject();
			logger.info("encryptedData:"+encryptedData);
			publicKey = getPublicKey(rsaPayloadPublicKey);
			logger.info("publicKey:"+publicKey);   
			data = decrypt(encryptedData,publicKey);
	    logger.info("data:"+data); 
	 System.out.println("Data :"+data);
	    return data;
	}

	public static String decrypt(String data, PublicKey publicKey) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException, NoSuchAlgorithmException, java.security.InvalidKeyException {
		logger.info("decrypt method called");    
		Cipher cipher = Cipher.getInstance("RSA");
	        cipher.init(Cipher.DECRYPT_MODE, publicKey);
	        return new String(cipher.doFinal(Base64.getDecoder().decode(data.getBytes())), StandardCharsets.UTF_8);
    }

    private static PublicKey getPublicKey(String rsaPublicKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
	    rsaPublicKey = rsaPublicKey.replace("-----BEGIN PUBLIC KEY-----", "");
	    rsaPublicKey = rsaPublicKey.replace("-----END PUBLIC KEY-----", "");
	    X509EncodedKeySpec keySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(rsaPublicKey));
	    KeyFactory kf = KeyFactory.getInstance("RSA");
	    PublicKey publicKey = kf.generatePublic(keySpec);
	    return publicKey;
	}
    public static String decodeToArabicString(String encodedArabicString) {
        byte[] charset = encodedArabicString
                .getBytes(StandardCharsets.UTF_8);
        return new String(charset, StandardCharsets.UTF_8);
    }
	public void main(String[] args) throws InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, java.security.InvalidKeyException {
		
		parseJwt("eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJqdGkiOiJlMzgzMmJkZi1kMWQzLTQwMzMtYWM3Yi05N2Y1ZjFhMDVhNWYiLCJpYXQiOjE2MTQyNDUyMzMsInN1YiI6IkMvMnh3YXNXcE5QcERrZm9zVU9wTnhqSVBRS1VWLzI3aE45N1BzN3RrTUVTckRDeGQvSVFMVFpiT1QreURGWkFhRmd5UzlIWWY4YkllQjBBT0RSTjJCNDZ1M0VzZHJxWUYreW9Dczh3cGVKVHoybnpHTTRhY0JURFA2bWtPb2d4T0pieWJ2TCtPUjh2endYNGpUSjN3K1hMaEM4elBPVXI0VjNLd0dZdDJBZGZ5MkFmR3daajRwdytteDE3Y3g2NmMyczVsZmljNldwZTV6RlVXaUJsNUV5cjRyYlFhd1hHcXRSZ3RydXlKMU1xNUV0eW1tL0dNRU1pZGw3Q2JybWZmc2ZzbmRxcUZ5MDdLbm51aTN6eC9oaW5pckYxcTN6RGh2MGF2eVVlZ0VpY3REeWxkQ21ockNRTGlIazFzK3ZjNUdkcDgvYVJ4U05mK2hJeXM2QUJhU0JOeVpuc1ZUUzZPdFgvcTVSZm53eW0xRlZoWW5PeFo2N0JGYWJzajVoc2o0Rmx6Tis5RXlMZi9rc1BYUGJqU1l6cDZoRG9ncTFKUmo1RUw4N0VVdjhtZ0pJOUdtTGJUMTQ0TXprVGRwMWs5MlNiVU5ydzZwQ0x3NnI3ZVpwN0dySzRXZnBQQU45MnVZdnF2VkpQVkd4OVdvOEFDampOTWJBdGVpWndlOUlnU2d2WjJrb29FdW9TQ1NmRHZqVVRiTk8vdmt0ZEJ4azE3Vm0yaHhaaEFvZU40clNGd00xMTFtdnFYSkZPN3hSVFl5NEZPYWZNdGdFL0RtU3ZaZ1czTzBtVkZjRXhKNVRyRDMybXY2VzRiLzJHbk4zbFh5V0xXbGhJQTVqcm9ERlpVcnlOWjZvdThhZGZla3N5d3VQRUViZHRWZWt5Q1NRanQySGpkS21MK213PSIsImlzcyI6Ik5BUy1TUCIsImF1ZCI6Ikh1a29vbWkiLCJleHAiOjE2MTQyNDcwMzN9.kCrlbPfHQV5oUoZggf4QFQ90E46B8xgLHeDRBQy7MXWJWXmKDyKb8MaRHImTpZfgJqV14rINGAQsgXdEyY0eFN71GWADHX4vb4BvPEIR4Wc9QUgcYLACwYpw4ZyMxyCYGP9wGYBkC-g9Pfphh_9teCtvmi220hlN5kkIZMiWIaW6GydMSwWxUz205IwA4AzHVvRc_fXtw26lEBwEmDGfxpHx0gWBoivKYZKN8hDzzFjYNnfG4D1Ver-gi_Htcpw9xYp9_ms4oeX7zuLYN3lKvxxBqr9r7AiA7Ysdk7lyGkNKi0IpOUCmfDKzQoidb4oLkjRwVrHYm425TfGuaAYq-Q");
		
	}
}
