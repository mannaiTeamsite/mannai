package com.hukoomi.livesite.external;

import com.hukoomi.utils.CommonUtils;
import com.hukoomi.utils.ESAPIValidator;
import com.hukoomi.utils.Postgre;
import com.hukoomi.utils.RequestHeaderUtils;
import com.interwoven.livesite.runtime.RequestContext;
import org.apache.commons.lang.WordUtils;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletResponse;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.owasp.esapi.ESAPI;
import org.owasp.esapi.ValidationErrorList;

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

    private static final Logger logger = Logger.getLogger(BookmarkExternal.class);
    Postgre postgre = null;

    public Document bookmarkSearch(final RequestContext context) {
        logger.info("BookmarkExternal()====> Starts");
        RequestHeaderUtils requestHeaderUtils = new RequestHeaderUtils(context);
        CommonUtils commonUtils = new CommonUtils();
        Document bookmarkSearchDoc = DocumentHelper.createDocument();
        Element bookmarkResultEle = bookmarkSearchDoc.addElement("bookmark");
        String status="valid";
        postgre = new Postgre(context);
        HttpSession session = context.getRequest().getSession();
        status=(String) session.getAttribute("status");
        logger.info("status="+session.getAttribute("status"));
        if(status!=null && status.equals("valid")) {
            userID = (String) session.getAttribute("userId");
            logger.info("userID:" + userID);
            locale = context.getParameterString("locale").trim().toLowerCase();
            pagetitle = context.getParameterString("page_title");
            pagedescription = context.getParameterString("page_description");
            pageurl = context.getParameterString("page_url");
            active = context.getParameterString("active");
            contenttype = context.getParameterString("content_type");
            category = context.getParameterString("category");
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
                            logger.info("Bookmark updared");
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
            locale  = ESAPI.validator().getValidInput("locale", locale, ESAPIValidator.ALPHABET, 20, false, true, errorList);
            if(!errorList.isEmpty()) {
                logger.info(errorList.getError("locale"));
                logger.error("Not a valid parameter locale. The incident will not be logged.");
                return 0;
            }
        }

        if (!ESAPIValidator.checkNull(userID)) {
            userID  = ESAPI.validator().getValidInput("userID", userID, ESAPIValidator.USER_ID, 255, false, true, errorList);
            if(!errorList.isEmpty()) {
                logger.info(errorList.getError("userID"));
                logger.error("Not a valid parameter userID. The incident will not be logged.");
                return 0;
            }
        }
        if (!ESAPIValidator.checkNull(active)) {
            active  = ESAPI.validator().getValidInput("active", active, ESAPIValidator.ALPHANUMERIC_SPACE, 255, false, true, errorList);
            if(!errorList.isEmpty()) {
                logger.info(errorList.getError("active"));
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
            category  = ESAPI.validator().getValidInput("category", category, ESAPIValidator.ALPHANUMERIC_SPACE, 255, false, true, errorList);
            if(!errorList.isEmpty()) {
                logger.info(errorList.getError("category"));
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

        logger.debug("Logging Broken link in Database");
        ValidationErrorList errorList = new ValidationErrorList();
        if (!ESAPIValidator.checkNull(pagetitle)) {
            pagetitle  = ESAPI.validator().getValidInput("pagetitle", pagetitle, ESAPIValidator.ALPHANUMERIC_SPACE, 255, false, true, errorList);
            if(!errorList.isEmpty()) {
                logger.info(errorList.getError("pagetitle"));
                logger.error("Not a valid parameter pagetitle. The incident will not be logged.");
                return;
            }
        }

        if (!ESAPIValidator.checkNull(pagedescription)) {
            pagedescription  = ESAPI.validator().getValidInput("pagedescription", pagedescription, ESAPIValidator.ALPHANUMERIC_SPACE, 255, false, true, errorList);
            if(!errorList.isEmpty()) {
                logger.info(errorList.getError("pagedescription"));
                logger.error("Not a valid parameter pagedescription. The incident will not be logged.");
                return;
            }
        }

        if (!ESAPIValidator.checkNull(pageurl)) {
            pageurl  = ESAPI.validator().getValidInput("pageurl", pageurl, ESAPIValidator.URL, 255, false, true, errorList);
            if(!errorList.isEmpty()) {
                logger.info(errorList.getError("pageurl"));
                logger.error("Not a valid parameter pageurl. The incident will not be logged.");
                return;
            }
        }

        if (!ESAPIValidator.checkNull(locale)) {
            locale  = ESAPI.validator().getValidInput("locale", locale, ESAPIValidator.ALPHABET, 20, false, true, errorList);
            if(!errorList.isEmpty()) {
                logger.info(errorList.getError("locale"));
                logger.error("Not a valid parameter locale. The incident will not be logged.");
                return;
            }
        }

        if (!ESAPIValidator.checkNull(userID)) {
            userID  = ESAPI.validator().getValidInput("userID", userID, ESAPIValidator.USER_ID, 255, false, true, errorList);
            if(!errorList.isEmpty()) {
                logger.info(errorList.getError("userID"));
                logger.error("Not a valid parameter userID. The incident will not be logged.");
                return;
            }
        }
        if (!ESAPIValidator.checkNull(active)) {
            active  = ESAPI.validator().getValidInput("active", active, ESAPIValidator.ALPHANUMERIC_SPACE, 255, false, true, errorList);
            if(!errorList.isEmpty()) {
                logger.info(errorList.getError("active"));
                logger.error("Not a valid parameter active. The incident will not be logged.");
                return;
            }
        }
        if (!ESAPIValidator.checkNull(contenttype)) {
            contenttype  = ESAPI.validator().getValidInput("contenttype", contenttype, ESAPIValidator.ALPHANUMERIC_SPACE, 255, false, true, errorList);
            if(!errorList.isEmpty()) {
                logger.info(errorList.getError("contenttype"));
                logger.error("Not a valid parameter contenttype. The incident will not be logged.");
                return;
            }
        }
        if (!ESAPIValidator.checkNull(category)) {
            category  = ESAPI.validator().getValidInput("category", category, ESAPIValidator.ALPHANUMERIC_SPACE, 255, false, true, errorList);
            if(!errorList.isEmpty()) {
                logger.info(errorList.getError("category"));
                logger.error("Not a valid parameter category. The incident will not be logged.");
                return;
            }
        }




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
                int i=1;
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
                        Element ele4 = ele.addElement("active");
                        ele4.setText(pageactive);
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
        logger.info("getBookmark()====> ends");

    }

    private boolean isBookmarkPresent() {
        boolean check=false;
        logger.info("isBookmarkPresent()====> Starts");

        logger.debug("Logging Broken link in Database");
        ValidationErrorList errorList = new ValidationErrorList();
        if (!ESAPIValidator.checkNull(pagetitle)) {
            pagetitle  = ESAPI.validator().getValidInput("pagetitle", pagetitle, ESAPIValidator.ALPHANUMERIC_SPACE, 255, false, true, errorList);
            if(!errorList.isEmpty()) {
                logger.info(errorList.getError("pagetitle"));
                logger.error("Not a valid parameter pagetitle. The incident will not be logged.");
                return false;
            }
        }

        if (!ESAPIValidator.checkNull(pagedescription)) {
            pagedescription  = ESAPI.validator().getValidInput("pagedescription", pagedescription, ESAPIValidator.ALPHANUMERIC_SPACE, 255, false, true, errorList);
            if(!errorList.isEmpty()) {
                logger.info(errorList.getError("pagedescription"));
                logger.error("Not a valid parameter pagedescription. The incident will not be logged.");
                return false;
            }
        }

        if (!ESAPIValidator.checkNull(pageurl)) {
            pageurl  = ESAPI.validator().getValidInput("pageurl", pageurl, ESAPIValidator.URL, 255, false, true, errorList);
            if(!errorList.isEmpty()) {
                logger.info(errorList.getError("pageurl"));
                logger.error("Not a valid parameter pageurl. The incident will not be logged.");
                return false;
            }
        }

        if (!ESAPIValidator.checkNull(locale)) {
            locale  = ESAPI.validator().getValidInput("locale", locale, ESAPIValidator.ALPHABET, 20, false, true, errorList);
            if(!errorList.isEmpty()) {
                logger.info(errorList.getError("locale"));
                logger.error("Not a valid parameter locale. The incident will not be logged.");
                return false;
            }
        }

        if (!ESAPIValidator.checkNull(userID)) {
            userID  = ESAPI.validator().getValidInput("userID", userID, ESAPIValidator.USER_ID, 255, false, true, errorList);
            if(!errorList.isEmpty()) {
                logger.info(errorList.getError("userID"));
                logger.error("Not a valid parameter userID. The incident will not be logged.");
                return false;
            }
        }
        if (!ESAPIValidator.checkNull(active)) {
            active  = ESAPI.validator().getValidInput("active", active, ESAPIValidator.ALPHANUMERIC_SPACE, 255, false, true, errorList);
            if(!errorList.isEmpty()) {
                logger.info(errorList.getError("active"));
                logger.error("Not a valid parameter active. The incident will not be logged.");
                return false;
            }
        }
        if (!ESAPIValidator.checkNull(contenttype)) {
            contenttype  = ESAPI.validator().getValidInput("contenttype", contenttype, ESAPIValidator.ALPHANUMERIC_SPACE, 255, false, true, errorList);
            if(!errorList.isEmpty()) {
                logger.info(errorList.getError("contenttype"));
                logger.error("Not a valid parameter contenttype. The incident will not be logged.");
                return false;
            }
        }
        if (!ESAPIValidator.checkNull(category)) {
            category  = ESAPI.validator().getValidInput("category", category, ESAPIValidator.ALPHANUMERIC_SPACE, 255, false, true, errorList);
            if(!errorList.isEmpty()) {
                logger.info(errorList.getError("category"));
                logger.error("Not a valid parameter category. The incident will not be logged.");
                return false;
            }
        }


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
            locale  = ESAPI.validator().getValidInput("locale", locale, ESAPIValidator.ALPHABET, 20, false, true, errorList);
            if(!errorList.isEmpty()) {
                logger.info(errorList.getError("locale"));
                logger.error("Not a valid parameter locale. The incident will not be logged.");
                return 0;
            }
        }

        if (!ESAPIValidator.checkNull(userID)) {
            userID  = ESAPI.validator().getValidInput("userID", userID, ESAPIValidator.USER_ID, 255, false, true, errorList);
            if(!errorList.isEmpty()) {
                logger.info(errorList.getError("userID"));
                logger.error("Not a valid parameter userID. The incident will not be logged.");
                return 0;
            }
        }
        if (!ESAPIValidator.checkNull(active)) {
            active  = ESAPI.validator().getValidInput("active", active, ESAPIValidator.ALPHANUMERIC_SPACE, 255, false, true, errorList);
            if(!errorList.isEmpty()) {
                logger.info(errorList.getError("active"));
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
            category  = ESAPI.validator().getValidInput("category", category, ESAPIValidator.ALPHANUMERIC_SPACE, 255, false, true, errorList);
            if(!errorList.isEmpty()) {
                logger.info(errorList.getError("category"));
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
