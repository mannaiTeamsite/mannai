package com.hukoomi.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.interwoven.livesite.runtime.RequestContext;

import com.interwoven.wcm.service.iwovregistry.utils.IREncryptionUtil;

/**
 * MySql is a database util class, which provides methods to load the
 * properties, create connection string, get connection and release connection,
 * statement and resultset.
 * 
 * @author Arbaj
 */
public class MySql {
    /**
     * Logger object to log debugrmation
     */
    private final Logger logger = Logger.getLogger(MySql.class);
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
     * @param context
     *                Request context object.
     *
     */
    public MySql(RequestContext context) {
        logger.debug("MySql : Loading Properties....");
        loadProperties(context);
        logger.debug("MySql : Properties Loaded");
        connectionString = getConnectionString();
    }

    /**
     * This method will be used to load the configuration properties.
     * 
     * @param context
     *                Request context object.
     * 
     */
    private void loadProperties(final RequestContext context) {
        PropertiesFileReader propertyFileReader = new PropertiesFileReader(
                context, "dbconfig.properties");
        properties = propertyFileReader.getPropertiesFile();
    }

    /**
     * This method will be used for creating connection string for database
     * connection.
     * 
     * @return Returns the connection string for database connection.
     *
     */
    private String getConnectionString() {
        logger.debug("MySql : getConnectionString()");
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
        logger.debug("MySql : getConnection()");
        // Creating Connection
        try {
            Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection(connectionString, userName,
                    password);
        } catch (Exception e) {
            logger.error("MySql : getConnection()", e);
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
        logger.debug("MySql : releaseConnection()");
        if (con != null) {
            try {
                con.close();
            } catch (Exception e) {
                logger.error(
                        "MySql : releaseConnection() : connection : ",
                        e);
            }
        }
        if (stmt != null) {
            try {
                stmt.close();
            } catch (Exception e) {
                logger.error(
                        "MySql : releaseConnection() : statement : ", e);
            }
        }
        if (rs != null) {
            try {
                rs.close();
            } catch (Exception e) {
                logger.error(
                        "MySql : releaseConnection() : resultset : ", e);
            }
        }
    }

}
