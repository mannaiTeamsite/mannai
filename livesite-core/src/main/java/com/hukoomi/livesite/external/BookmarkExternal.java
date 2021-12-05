package com.hukoomi.livesite.external;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.ValidationErrorList;

import com.hukoomi.utils.ESAPIValidator;
import com.hukoomi.utils.Postgre;
import com.hukoomi.utils.UserInfoSession;
import com.interwoven.livesite.runtime.RequestContext;

public class BookmarkExternal {
    private String locale = "";
    private String userID = "";
    private String pagetitle = "";
    private String pagedescription = "";
    private String pageurl = "";
    private String active = "";
    private String contenttype = "";
    private String table = "";
    private String isBookmarked = "";
    private String category = "";
    private String localLiteral="locale";
    private String activeLiteral="active";
    private String categoryLiteral="category";
    private String pagetitleLiteral="page_title";
    private String pagedescriptionLiteral="page_description";
    private String pageurlLiteral="page_url";
    private String userIDLiteral="userID";
    private String contenttypeLiteral="content_type";

    private static final Logger logger = Logger.getLogger(BookmarkExternal.class);
    Postgre postgre = null;
    @SuppressWarnings("deprecation")
    public Document bookmarkSearch(final RequestContext context) {
        logger.info("BookmarkExternal()====> Starts");
       
        Document bookmarkSearchDoc = DocumentHelper.createDocument();
        Element bookmarkResultEle = bookmarkSearchDoc.addElement("bookmark");
       
        postgre = new Postgre(context);
       
UserInfoSession ui = new UserInfoSession();
		
		HttpSession session = context.getRequest().getSession();
		String valid = ui.getStatus(context);
		if(valid.equalsIgnoreCase("valid")) {
        
            userID = (String) session.getAttribute(userIDLiteral);
            logger.info("userID:" + userID);
            locale = context.getParameterString(localLiteral).trim().toLowerCase();
            pagetitle = context.getParameterString(pagetitleLiteral);
            pagedescription = context.getParameterString(pagedescriptionLiteral);
            pageurl = context.getParameterString(pageurlLiteral);
            active = context.getParameterString(activeLiteral);
            contenttype = context.getParameterString("content_type");
            category = context.getParameterString(categoryLiteral);
            String queryType = context.getParameterString("queryType").trim();
            table = context.getParameterString("bookmark").trim();
            boolean isExist = false;
            logger.info("locale:" + locale);

            logger.info("table:" + table);
            if (!"".equals(table) && !"".equals(userID)) {
                if ("insert".equalsIgnoreCase(queryType)) {
                    isExist = isBookmarkPresent();
                    if (isExist) {
                        int updateStatus = updateBookmark();
                        if (updateStatus == 1) {
                            logger.info("Bookmark updated");
                        } else {
                            logger.info("Bookmark not updated");
                        }
                    } else {
                        int insertStatus = insertBookmark();
                        if (insertStatus == 1) {
                            logger.info("Bookmark inserted");
                        } else {
                            logger.info("Bookmark not inserted");
                        }
                    }
                } else {
                    getBookmark(bookmarkResultEle);
                }
            }
            logger.info("session valid");
        }
        else {

            logger.info("session invalid");
            context.getResponse().setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            bookmarkResultEle = bookmarkResultEle.addElement("session");
            bookmarkResultEle.setText("Session Invalid");
        }
        logger.info("bookmarkSearch====> ends");
        return bookmarkSearchDoc;
    }


    private int insertBookmark() {

    	logger.info("insertBookmark()====> Starts");

        logger.debug("Logging Broken link in Database");
        ValidationErrorList errorList = new ValidationErrorList();
        if (!ESAPIValidator.checkNull(pagetitle)) {
            pagetitle  = ESAPI.validator().getValidInput("pagetitle", pagetitle, ESAPIValidator.ALPHANUMERIC_SPACE, 255, false, true, errorList);
            if(!errorList.isEmpty()) {
                logger.info(errorList.getError("pagetitle"));
                logger.error("Not a valid parameter pagetitle. The incident will not be logged.");
                return 0;
            }
        }

        if (!ESAPIValidator.checkNull(pagedescription)) {
            pagedescription  = ESAPI.validator().getValidInput("pagedescription", pagedescription, ESAPIValidator.ALPHANUMERIC_SPACE, 255, false, true, errorList);
            if(!errorList.isEmpty()) {
                logger.info(errorList.getError("pagedescription"));
                logger.error("Not a valid parameter pagedescription. The incident will not be logged.");
                return 0;
            }
        }

        if (!ESAPIValidator.checkNull(pageurl)) {
            pageurl  = ESAPI.validator().getValidInput("pageurl", pageurl, ESAPIValidator.URL, 255, false, true, errorList);
            if(!errorList.isEmpty()) {
                logger.info(errorList.getError("pageurl"));
                logger.error("Not a valid parameter pageurl. The incident will not be logged.");
                return 0;
            }
        }

        if (!ESAPIValidator.checkNull(locale)) {
            locale  = ESAPI.validator().getValidInput(localLiteral, locale, ESAPIValidator.ALPHABET, 20, false, true, errorList);
            if(!errorList.isEmpty()) {
                logger.info(errorList.getError("locale"));
                logger.error("Not a valid parameter locale. The incident will not be logged.");
                return 0;
            }
        }

        if (!ESAPIValidator.checkNull(userID)) {
            userID  = ESAPI.validator().getValidInput(userIDLiteral, userID, ESAPIValidator.USER_ID, 255, false, true, errorList);
            if(!errorList.isEmpty()) {
                logger.info(errorList.getError(userIDLiteral));
                logger.error("Not a valid parameter userID. The incident will not be logged.");
                return 0;
            }
        }
        if (!ESAPIValidator.checkNull(active)) {
            active  = ESAPI.validator().getValidInput(activeLiteral, active, ESAPIValidator.ALPHANUMERIC_SPACE, 255, false, true, errorList);
            if(!errorList.isEmpty()) {
                logger.info(errorList.getError(activeLiteral));
                logger.error("Not a valid parameter active. The incident will not be logged.");
                return 0;
            }
        }
        if (!ESAPIValidator.checkNull(contenttype)) {
            contenttype  = ESAPI.validator().getValidInput("contenttype", contenttype, ESAPIValidator.ALPHANUMERIC_SPACE, 255, false, true, errorList);
            if(!errorList.isEmpty()) {
                logger.info(errorList.getError("contenttype"));
                logger.error("Not a valid parameter contenttype. The incident will not be logged.");
                return 0;
            }
        }
        if (!ESAPIValidator.checkNull(category)) {
            category  = ESAPI.validator().getValidInput(categoryLiteral, category, ESAPIValidator.ALPHANUMERIC_SPACE, 255, false, true, errorList);
            if(!errorList.isEmpty()) {
                logger.info(errorList.getError(categoryLiteral));
                logger.error("Not a valid parameter category. The incident will not be logged.");
                return 0;
            }
        }


       

        int result = 0;
        Connection connection = null;
        PreparedStatement prepareStatement = null;
        String bookmarkInsertQuery = "INSERT INTO" + " "
                + table +"(\"page_title\",\"page_description\", \"page_url\", \"locale\", \"creation_date\", \"user_id\",\"active\",\"content_type\",\"category\")"
                + " VALUES(?,?,?,?,LOCALTIMESTAMP,?,?,?,?)";
        logger.info("bookmarkInsertQuery:" +bookmarkInsertQuery);

        try{
            connection = getConnection();
            if(connection != null){

                prepareStatement =  connection.prepareStatement(bookmarkInsertQuery);
                prepareStatement.setString(1, pagetitle);
                prepareStatement.setString(2, pagedescription);
                prepareStatement.setString(3, pageurl);
                prepareStatement.setString(4, locale);
                prepareStatement.setString(5, userID);
                prepareStatement.setString(6, active);
                prepareStatement.setString(7, contenttype);
                prepareStatement.setString(8, category);
                result = prepareStatement.executeUpdate();

            }else{
                logger.info("Connection is null !");
            }
        }catch(SQLException ex){
            logger.error("Exception on insert Query:", ex);
        }finally {
            postgre.releaseConnection(connection, prepareStatement, null);
        }
        logger.info("insertBookmark()====> ends");
        return result;
    }

    private Connection getConnection() {
        return postgre.getConnection();
    }

    private void getBookmark(Element bookmarkResultEle) {
        String activeflag="Y";
        logger.info("getTopSearch()====> Starts");

        logger.debug("Logging Broken link in Database in getBookmark");
       



        Connection connection = getConnection();
        PreparedStatement prepareStatement = null;
        String searchQuery = "select page_title, page_url, page_description, active, content_type, category from" + " " +
                table + " " + "where" + " " + "locale='"+ locale +"' and " + "user_id='" + userID+"' and category='" + category+"' and active='" + activeflag+"'" ;
        logger.info("searchQuery:" + searchQuery);
        ResultSet resultSet = null;
        try {
            if(connection != null){
                prepareStatement = connection.prepareStatement(searchQuery);
                resultSet = prepareStatement.executeQuery();
                String pageTitle = "";
                String pageURL="";

                while(resultSet.next()){
                    Element ele = bookmarkResultEle.addElement("bookmarkDetails");
                    pageTitle = resultSet.getString(1);
                    pageURL = resultSet.getString(2);
                    String pagedesc = resultSet.getString(3);
                    String pageactive = resultSet.getString(4);
                    String ctype = resultSet.getString(5);
                    String categoryType = resultSet.getString(6);
                    if(!"".equals(pageTitle) && !"".equals(pageURL)){
                        Element ele1 = ele.addElement("pageTitle");
                        ele1.setText(pageTitle);
                        Element ele2 = ele.addElement("pageURL");
                        ele2.setText(pageURL);
                        Element ele3 = ele.addElement("pageDescription");
                        ele3.setText(pagedesc);
                        Element ele4 = ele.addElement(activeLiteral);
                        ele4.setText(pageactive);
                        Element ele5 = ele.addElement("contentType");
                        ele5.setText(ctype);
                        Element ele6 = ele.addElement(categoryLiteral);
                        ele6.setText(categoryType);
                        logger.info("Result:" + pageTitle+":"+pageURL);
                    }

                }
            }
        } catch (SQLException ex) {
            logger.error("Exception on Select Query:",ex);
        }finally {
            postgre.releaseConnection(connection, prepareStatement, resultSet);
        }
        logger.info("getBookmark()====> ends");

    }

    private boolean isBookmarkPresent() {
        boolean check=false;
        logger.info("isBookmarkPresent()====> Starts");

        logger.debug("Logging Broken link in Database in isBookmarkPresent");
       


        Connection connection = getConnection();
        PreparedStatement prepareStatement = null;
        String searchQuery = "select active from" + " " +
                table + " " + "where" + " " + "locale='"+ locale +"' and user_id='"
                + userID+"' and page_title='"+pagetitle+ "' and page_url='"+pageurl+"' and content_type='"+contenttype+"'";
        ResultSet resultSet = null;
        try {
            if(connection != null){
                prepareStatement = connection.prepareStatement(searchQuery);
                resultSet = prepareStatement.executeQuery();

                while(resultSet.next()){
                    isBookmarked = resultSet.getString(1);

                    if(!"".equals(isBookmarked))
                    {
                        check=true;
                        logger.info("check:" + check);
                    }
                }
            }
        } catch (SQLException ex) {
            logger.error("Exception on Select Query:",ex);
        }finally {
            postgre.releaseConnection(connection, prepareStatement, resultSet);
        }
        logger.info("getBookmark()====> ends");
        return check;
    }

    private int updateBookmark() {
        logger.info("updateBookmark()====> Starts");

        logger.debug("Logging Broken link in Database in updateBookmark");
        ValidationErrorList errorList = new ValidationErrorList();
        if (!ESAPIValidator.checkNull(pagetitle)) {
            pagetitle  = ESAPI.validator().getValidInput(pagetitleLiteral, pagetitle, ESAPIValidator.ALPHANUMERIC_SPACE, 255, false, true, errorList);
            if(!errorList.isEmpty()) {
                logger.info(errorList.getError(pagetitleLiteral));
                logger.error("Not a valid parameter pagetitle. The incident will not be logged.");
                return 0;
            }
        }

        if (!ESAPIValidator.checkNull(pagedescription)) {
            pagedescription  = ESAPI.validator().getValidInput(pagedescriptionLiteral, pagedescription, ESAPIValidator.ALPHANUMERIC_SPACE, 255, false, true, errorList);
            if(!errorList.isEmpty()) {
                logger.info(errorList.getError(pagedescriptionLiteral));
                logger.error("Not a valid parameter pagedescription. The incident will not be logged.");
                return 0;
            }
        }

        if (!ESAPIValidator.checkNull(pageurl)) {
            pageurl  = ESAPI.validator().getValidInput(pageurlLiteral, pageurl, ESAPIValidator.URL, 255, false, true, errorList);
            if(!errorList.isEmpty()) {
                logger.info(errorList.getError(pageurlLiteral));
                logger.error("Not a valid parameter pageurl. The incident will not be logged.");
                return 0;
            }
        }

        if (!ESAPIValidator.checkNull(locale)) {
            locale  = ESAPI.validator().getValidInput(localLiteral, locale, ESAPIValidator.ALPHABET, 20, false, true, errorList);
            if(!errorList.isEmpty()) {
                logger.info(errorList.getError(localLiteral));
                logger.error("Not a valid parameter locale. The incident will not be logged.");
                return 0;
            }
        }

        if (!ESAPIValidator.checkNull(userID)) {
            userID  = ESAPI.validator().getValidInput(userIDLiteral, userID, ESAPIValidator.USER_ID, 255, false, true, errorList);
            if(!errorList.isEmpty()) {
                logger.info(errorList.getError(userIDLiteral));
                logger.error("Not a valid parameter userID. The incident will not be logged.");
                return 0;
            }
        }
        if (!ESAPIValidator.checkNull(active)) {
            active  = ESAPI.validator().getValidInput(activeLiteral, active, ESAPIValidator.ALPHANUMERIC_SPACE, 255, false, true, errorList);
            if(!errorList.isEmpty()) {
                logger.info(errorList.getError(activeLiteral));
                logger.error("Not a valid parameter active. The incident will not be logged.");
                return 0;
            }
        }
        if (!ESAPIValidator.checkNull(contenttype)) {
            contenttype  = ESAPI.validator().getValidInput(contenttypeLiteral, contenttype, ESAPIValidator.ALPHANUMERIC_SPACE, 255, false, true, errorList);
            if(!errorList.isEmpty()) {
                logger.info(errorList.getError(contenttypeLiteral));
                logger.error("Not a valid parameter contenttype. The incident will not be logged.");
                return 0;
            }
        }
        if (!ESAPIValidator.checkNull(category)) {
            category  = ESAPI.validator().getValidInput(categoryLiteral, category, ESAPIValidator.ALPHANUMERIC_SPACE, 255, false, true, errorList);
            if(!errorList.isEmpty()) {
                logger.info(errorList.getError(categoryLiteral));
                logger.error("Not a valid parameter category. The incident will not be logged.");
                return 0;
            }
        }


        int result = 0;
        Connection connection = null;
        PreparedStatement prepareStatement = null;
        String bookmarkUpdateQuery = "UPDATE" + " "
                + table +" set active='"+active+"' where locale='"+ locale
                +"' and user_id='" + userID+"' and page_title='"+pagetitle+ "' and page_url='"
                +pageurl+"' and content_type='"+contenttype+"'";
        logger.info("bookmarkInsertQuery:" +bookmarkUpdateQuery);

        try{
            connection = getConnection();
            if(connection != null){

                prepareStatement =  connection.prepareStatement(bookmarkUpdateQuery);
                result = prepareStatement.executeUpdate();

            }else{
                logger.info("Connection is null !");
            }
        }catch(SQLException ex){
            logger.error("Exception on insert Query:", ex);
        }finally {
            postgre.releaseConnection(connection, prepareStatement, null);
        }
        logger.info("insertBookmark()====> ends");
        return result;
    }


}
