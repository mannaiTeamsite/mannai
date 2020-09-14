package com.utils;

import com.interwoven.livesite.file.FileDal;
import com.interwoven.livesite.runtime.RequestContext;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertyFileReader {
    /** Declare and Initialise Properties file to null.*/
    private Properties propertyFile = null;
    /** Logger object to check the flow of the code.*/
    private static final Logger LOGGER = Logger.getLogger(
            PropertyFileReader.class.getSimpleName());
    /** Setting the relative filepath of Properties file.*/
    private static final String PROPERTY_PATH = "iw/config/properties/";
    /**
     * Set the property file.
     * @param propertyFileName property file name.
     */
    public PropertyFileReader(final String propertyFileName) {
        propertyFile = propertiesFileLoader(propertyFileName);
    }
    /**
     * Set the property file.
     * @param propertyFileName property file name.
     * @param context Context passed from component.
     */
    public PropertyFileReader(
            final RequestContext context, final String propertyFileName) {
        propertyFile = propertiesFileLoader(context, propertyFileName);
    }
    /**
     * Get value for a given key in the property file.
     * @param key key in the property file.
     *
     * @return value set the param value as property file.
     */
    public String getPropertyValue(final String key) {
        String value = "";
        if (key != null && !key.equals("")) {
            value = propertyFile.getProperty(key);
        }
        return value;
    }
    /**
     * Get value for a given key in the property file.
     * @param propertyFileName property file name.
     * @param key key in the property file.
     *
     * @return value set the param value as property file.
     */
    public static String getPropertyValue(final String propertyFileName,
                                          final String key) {
        String value = "";
        if (key != null && !key.equals("")) {
            value = propertiesFileLoader(propertyFileName).getProperty(key);
        }
        return value;
    }
    /**
     * Get value for a given key in the property file.
     * @param context Context passed from component.
     * @param propertyFileName property file name.
     * @param key key in the property file.
     *
     * @return value set the param value as property file.
     */
    public static String getPropertyValue(
            final RequestContext context, final String propertyFileName,
            final String key) {
        String value = "";
        if (key != null && !key.equals("")) {
            value = propertiesFileLoader(context,
                    propertyFileName).getProperty(key);
        }
        return value;
    }

    private static Properties propertiesFileLoader(
            final String propertyFileName) {
        LOGGER.info("Entered.");
        Properties propFile = new Properties();
        if (propertyFileName != null && !propertyFileName.equals("")) {
            InputStream propertyFileAsStream = PropertyFileReader.class
                    .getResourceAsStream(propertyFileName);
            if (propertyFileAsStream != null) {
                try {
                    propFile.load(propertyFileAsStream);
                } catch (IOException ex) {
                    LOGGER.error("IO Exception : " + ex);
                }
            }
        } else {
            LOGGER.error("Property File Name is not proper "
                    + ": Either Null or Empty ");
        }
        LOGGER.info("Exit.");
        return propFile;
    }

    private static Properties propertiesFileLoader(
            final RequestContext context, final String propertyFileName) {
        LOGGER.info("Entered.");
        Properties propFile = new Properties();
        if (propertyFileName != null && !propertyFileName.equals("")) {
            FileDal fileDal = context.getFileDal();
            String root = fileDal.getRoot();
            char separator = fileDal.getSeparator();
            InputStream inputStream = fileDal.getStream(
                    root + separator + PROPERTY_PATH + propertyFileName);
            if (inputStream != null) {
                try {
                    propFile.load(inputStream);
                } catch (IOException ex) {
                    LOGGER.error("IO Exception : " + ex);
                }
            }
        } else {
            LOGGER.error("Property File Name is not proper "
                    + ": Either Null or Empty ");
        }
        LOGGER.info("Exit.");
        return propFile;
    }
    /**
     * Return property file.
     * @return propertyFile return property file.
     */
    public Properties getPropertyFile() {
        return propertyFile;
    }
    /**
     * Set property file with property filepath as param.
     * @param propertyFileValue set the param value as property file.
     */
    public void setPropertyFile(
            final Properties propertyFileValue) {
        this.propertyFile = propertyFileValue;
    }
}
