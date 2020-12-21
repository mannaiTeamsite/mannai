package com.hukoomi.livesite.external;

import com.hukoomi.utils.Postgre;
import com.interwoven.livesite.runtime.RequestContext;
import org.apache.commons.lang.WordUtils;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TopSearchExternal {
    private String ipAddress = "";
    private String baseQuery = "";
    private String userId = "";
    private String persona = "";
    private String locale = "";
    private String table = "";
    private int topSearchLimit;
    private String searchOrder = "";
    private static final Logger logger = Logger.getLogger(TopSearchExternal.class);
    Postgre postgre = null;

    public Document topSearch(final RequestContext context) {
        logger.info("topSearch()====> Starts");
        Document topSearchDoc = DocumentHelper.createDocument();
        Element topSearchResultEle = topSearchDoc.addElement("topSearchResult");
        postgre = new Postgre(context);
        ipAddress = context.getRequest().getRemoteAddr();
        String clientIpAddress = context.getRequest().getHeader("x-forwarded-for");
        logger.info("clientIpAddress:" + clientIpAddress);
        baseQuery = context.getParameterString("baseQuery").trim();
        try {
            baseQuery = URLDecoder.decode(baseQuery, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            logger.error(e);
        }
        locale = context.getParameterString("locale").trim().toLowerCase();
        String queryType = context.getParameterString("queryType").trim();
        table = context.getParameterString("topSearchTable").trim();
        topSearchLimit = Integer.parseInt(context.getParameterString("topSearchLimit").trim());
        searchOrder = context.getParameterString("searchOrder").trim();
        persona = context.getParameterString("persona").trim();
        logger.info("baseQuery:" + baseQuery);
        logger.info("locale:" + locale);
        logger.info("queryType:" + queryType);
        logger.info("table:" + table);
        logger.info("topSearchLimit:" + topSearchLimit);
        logger.info("searchOrder:" + searchOrder);

        if(!"".equals(table)){
            if(!"".equals(baseQuery) && "insert".equalsIgnoreCase(queryType)){
                int insertStatus = insertTopSearch();
                if(insertStatus == 1){
                    logger.info("Keyword inserted");
                }else{
                    logger.info("Keyword not inserted");
                }
            }else {
                getTopSearch(topSearchResultEle);
            }
        }

        logger.info("topSearch()====> ends");
        return topSearchDoc;
    }

    private void getTopSearch(Element topSearchResultEle) {
        logger.info("getTopSearch()====> Starts");
        Connection connection = getConnection();
        PreparedStatement prepareStatement = null;
        String searchQuery = "select lower(keyword), count(lower(keyword)) from" + " " +
                table + " " + "where" + " " + "locale='"+ locale +"' " + "group by lower(keyword) having count(lower(keyword)) > 1 " +
                "order by count(lower(keyword))" + " " + searchOrder + " " + "limit" + " " + topSearchLimit ;
        logger.info("searchQuery:" + searchQuery);
        ResultSet resultSet = null;
        try {
            if(connection != null){
                prepareStatement = connection.prepareStatement(searchQuery);
                resultSet = prepareStatement.executeQuery();
                String keyWord = "";
                while(resultSet.next()){
                    keyWord = resultSet.getString(1);
                    if(!"".equals(keyWord)){
                        Element topSearchEle = topSearchResultEle.addElement("keyword");
                        topSearchEle.setText(WordUtils.capitalize(keyWord.toLowerCase()));
                    }
                }
            }
        } catch (SQLException ex) {
            logger.error("Exception on Select Query:",ex);
        }finally {
            postgre.releaseConnection(connection, prepareStatement, resultSet);
        }
        logger.info("getTopSearch()====> ends");

    }

    private int insertTopSearch() {
        logger.info("insertTopSearch()====> Starts");
        int result = 0;
        Connection connection = null;
        PreparedStatement prepareStatement = null;
        String topSearchInsertQuery = "INSERT INTO" + " "
                + table + "(keyword,ip, user_id, " +
                "persona, locale, date)" +
                "VALUES(?,?,?,?,?, LOCALTIMESTAMP)";
        logger.info("topSearchInsertQuery:" +topSearchInsertQuery);

        try{
            connection = getConnection();
            if(connection != null){
                    if(!isKeywordExist(connection)){
                        prepareStatement =  connection.prepareStatement(topSearchInsertQuery);
                        prepareStatement.setString(1, baseQuery);
                        prepareStatement.setString(2, ipAddress);
                        prepareStatement.setString(3, userId);
                        prepareStatement.setString(4, persona);
                        prepareStatement.setString(5, locale);
                        result = prepareStatement.executeUpdate();
                    }
            }else{
                logger.debug("Connection is null !");
            }
        }catch(SQLException ex){
            logger.error("Exception on insert Query:", ex);
        }finally {
            postgre.releaseConnection(connection, prepareStatement, null);
        }
        logger.info("insertTopSearch()====> ends");
        return result;
    }

    private Connection getConnection() {
        return postgre.getConnection();
    }

    private boolean isKeywordExist(Connection connection){
        logger.info("isKeywordExist()====> Starts");
        String searchQuery = "select * from " + table + " where" +
                " keyword='" + baseQuery + "'" +
                " and ip='" + ipAddress + "'" +
                " and locale='" + locale + "'";
        logger.info("searchQuery:" + searchQuery);
        ResultSet resultSet = null;
        boolean isKeyword = false;
        PreparedStatement prepareStatement = null;
        try{
            if(connection != null){
                prepareStatement = connection.prepareStatement(searchQuery);
                resultSet = prepareStatement.executeQuery();
                while(resultSet.next()){
                    isKeyword = true;
                    break;
                }
            }

        }catch (SQLException ex){
            logger.error("Exception on Select Query:",ex);
        }finally {
            postgre.releaseConnection(null, prepareStatement, resultSet);
        }
        logger.info("isKeywordExist()====> ends");
        return isKeyword;
    }
}
