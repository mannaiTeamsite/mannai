/**
 * 
 */
package com.hukoomi.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * @author Arbaj
 *
 */
public class PostgreForServlet {
    /**
     * Logger object to log information
     */
    private final Logger logger = Logger
            .getLogger(PostgreForServlet.class);
    /**
     * Connection object variable.
     */
    private Connection con = null;
    /**
     * Connection string to connect to the database.
     */
    private String connectionString = null;
    /**
     * Username for connecting to the database
     */
    private String userName = null;
    /**
     * Password for connecting to the database
     */
    private String password = null;
    /**
     * Properties object variable to load the properties from property file
     * configuration.
     */
    private Properties properties = null;

    /**
     * This constructor will be called for creating database connection.
     * 
     * @param context Request context object.
     *
     */
    public PostgreForServlet() {
        logger.info("PostgreForServlet : Loading Properties....");
        properties = loadProperties("dbconfig.properties");
        logger.info("PostgreForServlet : Properties Loaded");
        connectionString = getConnectionString();
    }

    /**
     * This method will be used to load the configuration properties.
     *
     * @param context
     *                The parameter context object passed from Component.
     * @throws IOException
     * @throws MalformedURLException
     *
     */
    public Properties loadProperties(
            final String propertiesFileName) {
        logger.info("Loading Properties File from Request Context.");
        Properties propFile = new Properties();
        if (propertiesFileName != null && !propertiesFileName.equals("")) {
            String root = "/usr/opentext/LiveSiteDisplayServices/runtime/web/iw/config/properties";
            InputStream inputStream;
            try {
                inputStream = new FileInputStream(
                        root + "/" + propertiesFileName);
                propFile.load(inputStream);
                logger.info("Properties File Loaded");
            } catch (MalformedURLException e) {
                logger.error(
                        "Malformed URL Exception while loading Properties file : ",
                        e);
            } catch (IOException e) {
                logger.error(
                        "IO Exception while loading Properties file : ",
                        e);
            }

        } else {
            logger.info("Invalid / Empty properties file name.");
        }
        logger.info("Finish Loading Properties File.");
        return propFile;
    }

    /**
     * This method will be used for creating connection string for database
     * connection.
     * 
     * @return Returns the connection string for database connection.
     *
     */
    private String getConnectionString() {
        logger.info("PostgreForServlet : getConnectionString()");
        String connectionStr = null;
        String host = properties.getProperty("host");
        String port = properties.getProperty("port");
        String database = properties.getProperty("database");
        String schema = properties.getProperty("schema");
        userName = properties.getProperty("username");
        password = properties.getProperty("password");

        connectionStr = "jdbc:" + database + "://" + host + ":" + port
                + "/" + schema;

        logger.debug("Connection String : " + connectionStr);
        return connectionStr;
    }

    /**
     * This method is to get database connection.
     * 
     * @return Returns the database connection.
     */
    public Connection getConnection() {
        logger.info("PostgreForServlet : getConnection()");
        // Creating Connection
        try {
            con = DriverManager.getConnection(connectionString, userName,
                    password);
        } catch (Exception e) {
            logger.error("Postgre : getConnection()", e);
        }
        return con;
    }

    /**
     * This method will be used for closing connection, statement and resultset.
     * 
     * @param con
     *             Database connection to be closed
     * @param stmt
     *             Statement to be closed
     * @param rs
     *             ResultSet to be closed
     * 
     */
    public void releaseConnection(Connection con, Statement stmt,
            ResultSet rs) {
        logger.info("PostgreForServlet : releaseConnection()");
        if (con != null) {
            try {
                con.close();
            } catch (Exception e) {
                logger.error(
                        "PostgreForServlet : releaseConnection() : connection : ",
                        e);
            }
        }
        if (stmt != null) {
            try {
                stmt.close();
            } catch (Exception e) {
                logger.error(
                        "PostgreForServlet : releaseConnection() : statement : ",
                        e);
            }
        }
        if (rs != null) {
            try {
                rs.close();
            } catch (Exception e) {
                logger.error(
                        "PostgreForServlet : releaseConnection() : resultset : ",
                        e);
            }
        }
    }
}
