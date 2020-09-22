package com.hukoomi.utils;

import com.interwoven.livesite.file.FileDal;
import com.interwoven.livesite.runtime.RequestContext;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesFileReader {
    /** Declare and Initialise Properties file to null.*/
    private Properties propertiesFile = null;
    /** Logger object to check the flow of the code.*/
    private static final Logger LOGGER = Logger.getLogger(
            PropertiesFileReader.class.getSimpleName());
    /** Initialising the filepath for Properties file inside WorkArea.*/
    private static final String PROPERTY_PATH = "/iw/config/properties/";
    /**
     * Set the property file.
     * @param propertiesFileName properties file name.
     */
    public PropertiesFileReader(final String propertiesFileName) {
        propertiesFile = propertiesFileLoader(propertiesFileName);
    }
    /**
     * Set the properties file.
     * @param propertiesFileName properties file name.
     * @param context Context passed from component.
     */
    public PropertiesFileReader(
            final RequestContext context, final String propertiesFileName) {
        propertiesFile = propertiesFileLoader(context, propertiesFileName);
    }
    /**
     * Get value for a given key in the properties file.
     * @param key key in the properties file.
     *
     * @return value set the param value as properties file.
     */
    public String getPropertiesValue(final String key) {
        String value = "";
        if (key != null && !key.equals("")) {
            value = propertiesFile.getProperty(key);
        }
        return value;
    }
    /**
     * Get value for a given key in the properties file.
     * @param propertiesFileName properties file name.
     * @param key key in the properties file.
     *
     * @return value set the param value as properties file.
     */
    public static String getPropertiesValue(final String propertiesFileName,
                                            final String key) {
        String value = "";
        if (key != null && !key.equals("")) {
            value = propertiesFileLoader(propertiesFileName).getProperty(key);
        }
        return value;
    }
    /**
     * Get value for a given key in the properties file.
     * @param context Context passed from component.
     * @param propertiesFileName properties file name.
     * @param key key in the properties file.
     *
     * @return value of the property from Properties file.
     */
    public static String getPropertiesValue(
            final RequestContext context, final String propertiesFileName,
            final String key) {
        String value = "";
        if (key != null && !key.equals("")) {
            value = propertiesFileLoader(context,
                    propertiesFileName).getProperty(key);
        }
        return value;
    }

    private static Properties propertiesFileLoader(
            final String propertiesFileName) {
        LOGGER.info("Loading Properties File.");
        Properties propFile = new Properties();
        if (propertiesFileName != null && !propertiesFileName.equals("")) {
            InputStream propertyFileAsStream = PropertiesFileReader.class
                    .getResourceAsStream(propertiesFileName);
            if (propertyFileAsStream != null) {
                try {
                    propFile.load(propertyFileAsStream);
                    LOGGER.debug("Properties File Loaded");
                } catch (IOException ex) {
                    LOGGER.error(
                            "IO Exception while loading Properties File : ",
                            ex
                    );
                }
            }
        } else {
            LOGGER.error("Invalid / Empty properties file name.");
        }
        LOGGER.info("Finish Loading Properties File.");
        return propFile;
    }

    private static Properties propertiesFileLoader(
            final RequestContext context, final String propertiesFileName) {
        LOGGER.info("Loading Properties File from Request Context.");
        Properties propFile = new Properties();
        if (propertiesFileName != null && !propertiesFileName.equals("")) {
            FileDal fileDal = context.getFileDal();
            String root = fileDal.getRoot();
            LOGGER.info("File Dal Root: " + root);
            InputStream inputStream = fileDal.getStream(
                    root + PROPERTY_PATH + propertiesFileName);
            if (inputStream != null) {
                try {
                    propFile.load(inputStream);
                    LOGGER.debug("Properties File Loaded");
                } catch (IOException ex) {
                    LOGGER.error(
                            "IO Exception while loading Properties file : ",
                            ex
                    );
                }
            }
        } else {
            LOGGER.error("Invalid / Empty properties file name.");
        }
        LOGGER.info("Finish Loading Properties File.");
        return propFile;
    }
    /**
     * Get Current properties file from the context.
     *
     * @return Properties propertiesFile Current Properties file in the context.
     */
    public Properties getPropertiesFile() {
        return propertiesFile;
    }
    /**
     * Set properties file.
     * @param propertiesFileObject Properties file.
     */
    public void setPropertiesFile(final Properties propertiesFileObject) {
        this.propertiesFile = propertiesFileObject;
    }
}
