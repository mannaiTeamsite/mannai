package com.hukoomi.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.interwoven.cssdk.common.CSClient;
import com.interwoven.cssdk.filesys.CSSimpleFile;
import com.interwoven.cssdk.filesys.CSVPath;
import com.interwoven.cssdk.workflow.CSExternalTask;


public class TSPropertiesFileReader {
    private Properties propertiesFile = null;
    private final Logger logger = Logger.getLogger(TSPropertiesFileReader.class);
    private static final String PROPERTY_PATH = "/iw/config/properties/";

    public TSPropertiesFileReader(final String propertiesFileName) {
        propertiesFile = propertiesFileLoader(propertiesFileName);
    }
   
    public TSPropertiesFileReader(final CSClient client, final CSExternalTask task, final String propertyFileName) {
        propertiesFile = propertiesFileLoader(client, task, propertyFileName);
    }
    
    private Properties propertiesFileLoader(CSClient client, CSExternalTask task, String propertyFileName) {
		 logger.debug("PollSurveyTask : propertiesFileLoader");
	     Properties propFile = new Properties();
	     
	     try {
	    	 String vPath = task.getArea().getRootDir().getVPath().toString();
			 logger.debug("vPath : " + vPath);
				
			 if (vPath != null && !vPath.equals("")) {
				 String propFilePath = vPath + PROPERTY_PATH + propertyFileName;
				 logger.debug("propFilePath : " + propFilePath);
				 CSSimpleFile propSimpleFile = (CSSimpleFile) client.getFile(new CSVPath(propFilePath));
				 InputStream inputStream = propSimpleFile.getInputStream(false);
				
				 if (inputStream != null) {
					 try {
						 propFile.load(inputStream);
						 logger.debug("Properties File Loaded");
					 }catch (IOException ex) {
						 logger.error("IO Exception while loading Properties file : ", ex);
					 }
				 }
			       
			 }else{
				 logger.error("Error reading vPath");
			 }
			 logger.info("Finish Loading Properties File.");
		 }catch (Exception e) {
			 logger.error("Exception in loading property file : "+e.getMessage());
			 e.printStackTrace();
		 }
	     return propFile;
	  }
   
    public String getPropertiesValue(final String key) {
        String value = "";
        if (key != null && !key.equals("")) {
            value = propertiesFile.getProperty(key);
        }
        return value;
    }
   
    public String getPropertiesValue(final String propertiesFileName, final String key) {
        String value = "";
        if (key != null && !key.equals("")) {
            value = propertiesFileLoader(propertiesFileName).getProperty(key);
        }
        return value;
    }
   
    public String getPropertiesValue(final CSClient client, final CSExternalTask task, final String propertiesFileName, final String key) {
        String value = "";
        if (key != null && !key.equals("")) {
            value = propertiesFileLoader(client, task, propertiesFileName).getProperty(key);
        }
        return value;
    }

    private Properties propertiesFileLoader(final String propertiesFileName) {
        logger.info("Loading Properties File.");
        Properties propFile = new Properties();
        if (propertiesFileName != null && !propertiesFileName.equals("")) {
            InputStream propertyFileAsStream = TSPropertiesFileReader.class.getResourceAsStream(propertiesFileName);
            if (propertyFileAsStream != null) {
                try {
                    propFile.load(propertyFileAsStream);
                    logger.debug("Properties File Loaded");
                } catch (IOException ex) {
                    logger.error("IO Exception while loading Properties File : ", ex);
                }
            }
        } else {
            logger.error("Invalid / Empty properties file name.");
        }
        logger.info("Finish Loading Properties File.");
        return propFile;
    }
    
    public Properties getPropertiesFile() {
        return propertiesFile;
    }
  
    public void setPropertiesFile(final Properties propertiesFileObject) {
        this.propertiesFile = propertiesFileObject;
    }
}
