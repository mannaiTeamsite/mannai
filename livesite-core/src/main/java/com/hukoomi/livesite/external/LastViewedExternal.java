package com.hukoomi.livesite.external;

import com.hukoomi.utils.CommonUtils;
import com.hukoomi.utils.Postgre;
import com.hukoomi.utils.RequestHeaderUtils;
import com.interwoven.livesite.runtime.RequestContext;
import org.apache.commons.lang.WordUtils;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class LastViewedExternal {
    private String locale = "";
    private String userID = "";
    private String pagetitle = "";
    private String pagedescription = "";
    private String pageurl = "";
    private String contenttype = "";
    private String table = "";
    private String category = "";

    private static final Logger logger = Logger.getLogger(LastViewedExternal.class);
    Postgre postgre = null;

    public Document lastViewedsearch(final RequestContext context) {
        logger.info("lastViewedsearch()====> Starts");
        RequestHeaderUtils requestHeaderUtils = new RequestHeaderUtils(context);
        CommonUtils commonUtils = new CommonUtils();
        Document lastviewedDoc = DocumentHelper.createDocument();
        Element lastviewedResultEle = lastviewedDoc.addElement("bookmark");
        String status="valid";
        postgre = new Postgre(context);
        HttpSession session = context.getRequest().getSession(true);
         status=(String) session.getAttribute("status");
        logger.info("status="+status);
        if(status.equals("valid")) {
            userID = (String) session.getAttribute("uid");;
            logger.info("userID:" + userID);

            locale = context.getParameterString("locale").trim().toLowerCase();
            pagetitle = context.getParameterString("page_title");
            pagedescription = context.getParameterString("page_description");
            pageurl = context.getParameterString("page_url");
            contenttype = context.getParameterString("content_type");
            category = context.getParameterString("category");
            String queryType = context.getParameterString("queryType").trim();
            table = context.getParameterString("last_viewed").trim();
            boolean isExist = false;
            logger.info("locale:" + locale);

            logger.info("table:" + table);

            if (!"".equals(table) && !"".equals(userID)) {
                if ("insert".equalsIgnoreCase(queryType)) {
                    isExist = isLastViewed();
                    if (isExist) {
                        int updateStatus = updateLastViewed();
                        if (updateStatus == 1) {
                            logger.info("LastViewed updared");
                        } else {
                            logger.info("LastViewed not updated");
                        }
                    } else {
                        int insertStatus = insertLastViewed();
                        if (insertStatus == 1) {
                            logger.info("LastViewed inserted");
                        } else {
                            logger.info("LastViewed not inserted");
                        }
                    }
                } else {
                    getLastviewed(lastviewedResultEle);
                }
            }
            logger.info("session valid");
        }
        else {
            logger.info("session invalid");
        }
        logger.info("LastViewedSearch====> ends");
        return lastviewedDoc;
    }


    private int insertLastViewed() {
        logger.info("insertLastViewed()====> Starts");
        int result = 0;
        Connection connection = null;
        PreparedStatement prepareStatement = null;
        String lastviewedInsertQuery = "INSERT INTO" + " "
                + table +"(\"page_title\",\"page_description\", \"page_url\", \"locale\", \"view_date\", \"user_id\",\"content_type\",\"category\")"
                + " VALUES(?,?,?,?,LOCALTIMESTAMP,?,?,?)";
        logger.info("lastviewedInsertQuery:" +lastviewedInsertQuery);

        try{
            connection = getConnection();
            if(connection != null){

                prepareStatement =  connection.prepareStatement(lastviewedInsertQuery);
                prepareStatement.setString(1, pagetitle);
                prepareStatement.setString(2, pagedescription);
                prepareStatement.setString(3, pageurl);
                prepareStatement.setString(4, locale);
                prepareStatement.setString(5, userID);
                prepareStatement.setString(6, contenttype);
                prepareStatement.setString(7, category);
                result = prepareStatement.executeUpdate();

            }else{
                logger.info("Connection is null !");
            }
        }catch(SQLException ex){
            logger.error("Exception on insert Query:", ex);
        }finally {
            postgre.releaseConnection(connection, prepareStatement, null);
        }
        logger.info("insertLastViewed()====> ends");
        return result;
    }

    private Connection getConnection() {
        return postgre.getConnection();
    }

    private void getLastviewed(Element lastviewedResultEle) {

        logger.info("getLastviewed()====> Starts");
        Connection connection = getConnection();
        PreparedStatement prepareStatement = null;
        String searchQuery = "select page_title, page_url, page_description, content_type, category from" + " " +
                table + " " + "where" + " " + "locale='"+ locale +"' and " + "user_id='" + userID+"' and category='" + category+"'" ;
        logger.info("searchQuery:" + searchQuery);
        ResultSet resultSet = null;
        try {
            if(connection != null){
                prepareStatement = connection.prepareStatement(searchQuery);
                resultSet = prepareStatement.executeQuery();
                String pageTitle = "";
                String pageURL="";
                int i=1;
                while(resultSet.next()){
                    Element ele = lastviewedResultEle.addElement("lastviewed");
                    pageTitle = resultSet.getString(1);
                    pageURL = resultSet.getString(2);
                    String pagedesc = resultSet.getString(3);
                    String ctype = resultSet.getString(4);
                    String categoryType = resultSet.getString(5);
                    if(!"".equals(pageTitle) && !"".equals(pageURL)){
                        Element ele1 = ele.addElement("pageTitle");
                        ele1.setText(pageTitle);
                        Element ele2 = ele.addElement("pageURL");
                        ele2.setText(pageURL);
                        Element ele3 = ele.addElement("pageDescription");
                        ele3.setText(pagedesc);
                        Element ele5 = ele.addElement("contentType");
                        ele5.setText(ctype);
                        Element ele6 = ele.addElement("category");
                        ele6.setText(categoryType);
                        logger.info("Result:" + pageTitle+":"+pageURL);
                    }
                    i++;
                }
            }
        } catch (SQLException ex) {
            logger.error("Exception on Select Query:",ex);
        }finally {
            postgre.releaseConnection(connection, prepareStatement, resultSet);
        }
        logger.info("getLastviewed()====> ends");

    }

    private boolean isLastViewed() {
        boolean check=false;
        int count=0;
        logger.info("isLastViewed()====> Starts");
        Connection connection = getConnection();
        PreparedStatement prepareStatement = null;
        String searchQuery = "select count(*) from" + " " +
                table + " " + "where" + " " + "locale='"+ locale +"' and user_id='"
                + userID+"' and page_title='"+pagetitle+ "' and page_url='"+pageurl+"' and content_type='"+contenttype+"'";
        ResultSet resultSet = null;
        try {
            if(connection != null){
                prepareStatement = connection.prepareStatement(searchQuery);
                resultSet = prepareStatement.executeQuery();

                while(resultSet.next()){
                    count = Integer.parseInt(resultSet.getString(1));

                    if(count > 0)
                    {
                        check=true;
                        logger.debug("check:" + check);
                    }
                }
            }
        } catch (SQLException ex) {
            logger.error("Exception on Select Query:",ex);
        }finally {
            postgre.releaseConnection(connection, prepareStatement, resultSet);
        }
        logger.info("isLastViewed()====> ends");
        return check;
    }

    private int updateLastViewed() {
        logger.info("updateLastViewed()====> Starts");
        int result = 0;
        Connection connection = null;
        PreparedStatement prepareStatement = null;
        String lastviewedUpdateQuery = "UPDATE" + " "
                + table +" set view_date=LOCALTIMESTAMP where locale='"+ locale
                +"' and user_id='" + userID+"' and page_title='"+pagetitle+ "' and page_url='"
                +pageurl+"' and content_type='"+contenttype+"'";
        logger.debug("lastviewedUpdateQuery:" +lastviewedUpdateQuery);

        try{
            connection = getConnection();
            if(connection != null){

                prepareStatement =  connection.prepareStatement(lastviewedUpdateQuery);
                result = prepareStatement.executeUpdate();

            }else{
                logger.info("Connection is null !");
            }
        }catch(SQLException ex){
            logger.error("Exception on insert Query:", ex);
        }finally {
            postgre.releaseConnection(connection, prepareStatement, null);
        }
        logger.info("updateLastViewed()====> ends");
        return result;
    }


}
