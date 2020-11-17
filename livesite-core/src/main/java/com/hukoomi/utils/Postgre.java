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
    private static Connection con = null;

    static String connectionString = null;
    
    static String userName= null;
    static String password = null;

    public Postgre(RequestContext context) {
        connectionString = getConnectionString(context);
    }
    
    @Deprecated
    private String getConnectionString(final RequestContext context) {
        logger.info("Postgre : getConnectionString()");

        String connectionStr = null;
        PropertiesFileReader propertyFileReader = new PropertiesFileReader(
                context, "dbconfig.properties");
        Properties properties = propertyFileReader.getPropertiesFile();

        String host = context.getParameterString("host",
                properties.getProperty("host"));
        String port = context.getParameterString("port",
                properties.getProperty("port"));
        String database = context.getParameterString("database",
                properties.getProperty("database"));
        String schema = context.getParameterString("schema",
                properties.getProperty("schema"));
        userName = context.getParameterString("username",
                properties.getProperty("username"));
        password = context.getParameterString("password",
                properties.getProperty("password"));

        // jdbc:postgresql://172.16.167.164:5432/devapps,"tsdev","Motc@1234"
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
        	//Class.forName("org.postgresql.Driver");
            con = DriverManager.getConnection(connectionString,userName, password);
        } catch (Exception e) {
            logger.error("Postgre : getConnection()" + e.getMessage());
            e.printStackTrace();
        }
        return con;
    }

    public static void releaseConnection(Connection con, Statement stmt,
            ResultSet rs) {
        //logger.info("Postgre : releaseConnection()");
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
