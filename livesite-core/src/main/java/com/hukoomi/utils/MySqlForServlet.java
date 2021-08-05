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
import com.interwoven.wcm.service.iwovregistry.utils.IREncryptionUtil;

import org.apache.log4j.Logger;

/**
 * @author Arbaj
 *
 */
public class MySqlForServlet {
    /**
     * Logger object to log information
     */
    private final Logger logger = Logger
            .getLogger(MySqlForServlet.class);
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
    public MySqlForServlet() {
        logger.info("MySqlForServlet : Loading Properties....");
        properties = loadProperties("dbconfig.properties");
        logger.info("MySqlForServlet : Properties Loaded");
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
        logger.info("MySqlForServlet : getConnectionString()");
        String connectionStr = null;
        String host = properties.getProperty("mysql_host");
        String port = properties.getProperty("mysql_port");
        String database = properties.getProperty("mysql_database");
        String schema = properties.getProperty("mysql_schema");
        userName = properties.getProperty("mysql_username");
        //password = properties.getProperty("mysql_password");
        password = properties.getProperty("mysql_password");
        password = IREncryptionUtil.decrypt(password);


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
        logger.info("MySqlForServlet : getConnection()");
        // Creating Connection
        try {
            Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection(connectionString, userName,
                    password);
        } catch (Exception e) {
            logger.error("MySqlForServlet : getConnection()", e);
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
        logger.info("MySqlForServlet : releaseConnection()");
        if (con != null) {
            try {
                con.close();
            } catch (Exception e) {
                logger.error(
                        "MySqlForServlet : releaseConnection() : connection : ",
                        e);
            }
        }
        if (stmt != null) {
            try {
                stmt.close();
            } catch (Exception e) {
                logger.error(
                        "MySqlForServlet : releaseConnection() : statement : ",
                        e);
            }
        }
        if (rs != null) {
            try {
                rs.close();
            } catch (Exception e) {
                logger.error(
                        "MySqlForServlet : releaseConnection() : resultset : ",
                        e);
            }
        }
    }
}
