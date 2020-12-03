package com.hukoomi.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.interwoven.cssdk.common.CSClient;
import com.interwoven.cssdk.filesys.CSSimpleFile;
import com.interwoven.cssdk.filesys.CSVPath;
import com.interwoven.cssdk.workflow.CSExternalTask;

/**
 * TSPropertiesFileReader is the teamsite property file reader, used for reading
 * the property file configuration using the CSClient and CSExternalTask.
 * 
 * @author Vijayaragavamoorthy
 */
public class TSPropertiesFileReader {
    /**
     * Properties object that holds the property values
     */
    private Properties propertiesFile = null;
    /**
     * Logger object to log information.
     */
    private final Logger logger = Logger
            .getLogger(TSPropertiesFileReader.class);
    /**
     * Property file path
     */
    private static final String PROPERTY_PATH = "/iw/config/properties/";

    /**
     * TSPropertiesFileReader Constructor
     * 
     * @param propertiesFileName Name of the property file
     */
    public TSPropertiesFileReader(final String propertiesFileName) {
        propertiesFile = propertiesFileLoader(propertiesFileName);
    }

    /**
     * TSPropertiesFileReader Constructor
     * 
     * @param client           CSClient object
     * @param task             CSExternalTask object
     * @param propertyFileName Name of the property file
     */
    public TSPropertiesFileReader(final CSClient client,
            final CSExternalTask task, final String propertyFileName) {
        propertiesFile = propertiesFileLoader(client, task,
                propertyFileName);
    }

    /**
     * Method to load the property file values to the Properties object.
     * 
     * @param client           CSClient object
     * @param task             CSExternalTask object
     * @param propertyFileName Name of the property file
     * @return Properties object
     */
    private Properties propertiesFileLoader(CSClient client,
            CSExternalTask task, String propertyFileName) {
        logger.debug("PollSurveyTask : propertiesFileLoader");
        Properties propFile = new Properties();

        try {
            String vPath = task.getArea().getRootDir().getVPath()
                    .toString();
            logger.debug("vPath : " + vPath);

            if (vPath != null && !vPath.equals("")) {
                String propFilePath = vPath + PROPERTY_PATH
                        + propertyFileName;
                logger.debug("propFilePath : " + propFilePath);
                CSSimpleFile propSimpleFile = (CSSimpleFile) client
                        .getFile(new CSVPath(propFilePath));
                InputStream inputStream = propSimpleFile
                        .getInputStream(false);

                if (inputStream != null) {
                    propFile.load(inputStream);
                    logger.debug("Properties File Loaded");
                }

            } else {
                logger.error("Error reading vPath");
            }
            logger.info("Finish Loading Properties File.");
        } catch (Exception e) {
            logger.error("Exception in loading property file : ", e);
        }
        return propFile;
    }

    /**
     * Method to read a property value from the Properties object
     * 
     * @param key Property key for the value
     * @return Returns property value
     */
    public String getPropertiesValue(final String key) {
        String value = "";
        if (key != null && !key.equals("")) {
            value = propertiesFile.getProperty(key);
        }
        return value;
    }

    /**
     * Method to get the property value from the property file name passed and for
     * the property key.
     * 
     * @param propertiesFileName Name of the property file
     * @param key                Property key for the value
     * @return Returns property value
     */
    public String getPropertiesValue(final String propertiesFileName,
            final String key) {
        String value = "";
        if (key != null && !key.equals("")) {
            value = propertiesFileLoader(propertiesFileName)
                    .getProperty(key);
        }
        return value;
    }

    /**
     * Method to get the property value from the property file name passed and for
     * the property key using the CSClient and CSExternalTask.
     * 
     * @param client             CSClient object
     * @param task               CSExternalTask object
     * @param propertiesFileName Name of the property file
     * @param key                Property key for the value
     * @return Returns property value
     */
    public String getPropertiesValue(final CSClient client,
            final CSExternalTask task, final String propertiesFileName,
            final String key) {
        String value = "";
        if (key != null && !key.equals("")) {
            value = propertiesFileLoader(client, task, propertiesFileName)
                    .getProperty(key);
        }
        return value;
    }

    /**
     * Method loads the property file and returns properties object
     * 
     * @param propertiesFileName Name of the property file
     * @return Returns the properties object with properties loaded.
     */
    private Properties propertiesFileLoader(
            final String propertiesFileName) {
        logger.info("Loading Properties File.");
        Properties propFile = new Properties();
        if (propertiesFileName != null && !propertiesFileName.equals("")) {
            InputStream propertyFileAsStream = TSPropertiesFileReader.class
                    .getResourceAsStream(propertiesFileName);
            if (propertyFileAsStream != null) {
                try {
                    propFile.load(propertyFileAsStream);
                    logger.debug("Properties File Loaded");
                } catch (IOException ex) {
                    logger.error(
                            "IO Exception while loading Properties File : ",
                            ex);
                }
            }
        } else {
            logger.error("Invalid / Empty properties file name.");
        }
        logger.info("Finish Loading Properties File.");
        return propFile;
    }

    /**
     * Method to get the properties object
     * 
     * @return Returns the properties object
     */
    public Properties getPropertiesFile() {
        return propertiesFile;
    }

    /**
     * Method to set the properties object
     * 
     * @param propertiesFileObject Properties object
     */
    public void setPropertiesFile(final Properties propertiesFileObject) {
        this.propertiesFile = propertiesFileObject;
    }
}
