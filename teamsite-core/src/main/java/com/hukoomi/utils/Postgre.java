package com.hukoomi.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.interwoven.cssdk.common.CSClient;
import com.interwoven.cssdk.workflow.CSExternalTask;

/**
 * Postgre is a database util class, which provides methods to load 
 * the properties, create connection string, get connection and release 
 * connection, statement and resultset.
 * 
 * @author Vijayaragavamoorthy
 */
public class Postgre {
    /** 
     * Logger object to log information 
     */
    private final Logger logger = Logger.getLogger(Postgre.class);
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
    private String userName= null;
    /**
     * Password for connecting to the database
     */
    private String password = null;
    /**
     * Properties object variable to load the 
     * properties from property file configuration. 
     */
    private static Properties properties = null;

    /**
     * This constructor will be called for creating database connection.
     * 
     * @param client CSClient object.
     * @param task CSExternalTask object.
     * @param propertyFileName Name of the property file.
     *
     */
    public Postgre(final CSClient client, final CSExternalTask task, final String propertyFileName) {
        logger.info("Postgre : Loading Properties....");
        Postgre.loadProperties(client, task, propertyFileName);
        logger.info("Postgre : Properties Loaded");
        connectionString = getConnectionString();
    }
    
    /**
     * This method will be used to load the configuration properties.
     * 
     * @param client CSClient object.
     * @param task CSExternalTask object.
     * @param propertyFileName Name of the property file.
     * 
     */
    private static void loadProperties(final CSClient client, 
            final CSExternalTask task, final String propertyFileName) {
        if(properties == null) {
            TSPropertiesFileReader propFileReader = new TSPropertiesFileReader(client, task, propertyFileName);
            Postgre.properties = propFileReader.getPropertiesFile();
        }
    }

    /**
     * This method will be used for creating connection string for database connection.
     * 
     * @return Returns the connection string for database connection.
     *
     */
    private String getConnectionString() {
		 logger.info("Postgre : getConnectionString()");
		 String connectionStr = null;
		 String host = properties.getProperty("host");
		 String port = properties.getProperty("port");
		 String database = properties.getProperty("database");
		 String schema = properties.getProperty("schema");
		 userName = properties.getProperty("username");
		 password = properties.getProperty("password");

		 connectionStr = "jdbc:" + database + "://" + host + ":" + port+ "/" + schema;
		 logger.debug("Connection String : " + connectionStr);

		 return connectionStr;
	 }

    /**
     * This method is to get database connection.
     * 
     * @return Returns the database connection.
     */
    public Connection getConnection() {
        logger.info("Postgre : getConnection()");
        // Creating Connection
        try {
            con = DriverManager.getConnection(connectionString,userName, password);
        } catch (Exception e) {
            logger.error("Postgre : getConnection()", e);
        }
        return con;
    }

    /**
     * This method will be used for closing connection, statement and resultset.
     * 
     * @param con Database connection to be closed
     * @param stmt Statement to be closed 
     * @param rs ResultSet to be closed
     * 
     */
    public void releaseConnection(Connection con, Statement stmt,
            ResultSet rs) {
        logger.info("Postgre : releaseConnection()");
        if (rs != null) {
            try {
                rs.close();
            } catch (Exception e) {
                logger.error("Postgre : releaseConnection() : connection : ", e);
            }
        }
        if (stmt != null) {
            try {
                stmt.close();
            } catch (Exception e) {
                logger.error("Postgre : releaseConnection() : statement : ", e);
            }
        }       
        if (con != null) {
            try {
                con.close();
            } catch (Exception e) {
                logger.error("Postgre : releaseConnection() : resultset : ", e);
            }
        }
    }

}
