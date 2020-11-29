package com.hukoomi.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.interwoven.livesite.runtime.RequestContext;

public class Postgre {
    /** Logger object to check the flow of the code. */
    private final Logger logger = Logger.getLogger(Postgre.class);
    /** Connection variable. */
    private Connection con = null;
    private String connectionString = null;    
    private String userName= null;
    private String password = null;
    private static Properties properties = null;

    /**
     * This constructor will be called for creating Postgres connection.
     * 
     * @param context The parameter context object passed from Component.
     *
     */
    public Postgre(RequestContext context) {
        logger.info("Postgre : Loading Properties....");
        Postgre.loadProperties(context);
        logger.info("Postgre : Properties Loaded");
        connectionString = getConnectionString();
    }
    
    /**
     * This method will be used to load the configuration properties.
     * 
     * @param context The parameter context object passed from Component.
     * 
     */
    private static void loadProperties(final RequestContext context) {
        if(properties == null) {
            PropertiesFileReader propertyFileReader = new PropertiesFileReader(
                    context, "dbconfig.properties");
            Postgre.properties = propertyFileReader.getPropertiesFile();
        }
    }
    
    /**
     * This method will be used for creating connection string for Postgres connection.
     * 
     * @param context The parameter context object passed from Component.
     * 
     * @return connectionStr return the connection string for Postgres connection.
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

        connectionStr = "jdbc:" + database + "://" + host + ":" + port
                + "/" + schema;

        logger.info("Connection String : " + connectionStr);
        return connectionStr;
    }
    
    /**
     * method to getConnection.
     */
    public Connection getConnection() {
        logger.info("Postgre : getConnection()");
        // Creating Connection
        try {
            con = DriverManager.getConnection(connectionString, userName, password);
        } catch (Exception e) {
            logger.error("Postgre : getConnection()" + e.getMessage());
            e.printStackTrace();
        }
        return con;
    }

    public static void releaseConnection(Connection con, Statement stmt,
            ResultSet rs) {
        // Releasing Connection
        if (con != null) {
            try {
                con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

}
