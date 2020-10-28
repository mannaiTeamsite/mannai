package com.hukoomi.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;

public class Postgre {
    /** Logger object to check the flow of the code.*/
    private final static Logger logger =
            Logger.getLogger(Postgre.class);
    /** Connection variable. */
    private static Connection con = null;
    
    /**
     * method to getConnection.
     * */
    public static Connection getConnection() {
        logger.info("Postgre : getConnection()");
     // Creating Connection
        try {
            con = DriverManager.getConnection(
                    "jdbc:postgresql://172.16.167.164:5432/devapps", "tsdev",
                    "Motc@admin_123");
        } catch (Exception e) {
            logger.error("Postgre : getConnection()"+e.getMessage());
            e.printStackTrace();
        }
        return con;
    }
    
    public static void releaseConnection(Connection con, Statement stmt, ResultSet rs) {
        logger.info("Postgre : releaseConnection()");
        // Releasing Connection
           if(con!=null) {
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
