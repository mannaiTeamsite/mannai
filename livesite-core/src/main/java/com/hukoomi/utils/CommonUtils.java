/*
 * Common utils java for local DCR operations.
 */
package com.hukoomi.utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;

import com.interwoven.livesite.file.FileDal;
import com.interwoven.livesite.runtime.LiveSiteDal;
import com.interwoven.livesite.runtime.RequestContext;

public class CommonUtils {
    /**
     * Logger object to check the flow of the code.
     */
    private final Logger logger = Logger.getLogger(CommonUtils.class);
    /** Set content error code value. */
    private static final int CONTEXT_ERROR_CODE = 490;
    /** Initialize context to null. */
    private RequestContext context = null;
    /** Initialize file dal to null. */
    private FileDal fileDal = null;
    /** Initialize livesite dal to null. */
    private LiveSiteDal liveSiteDal = null;
    /** Declare separator character. */
    private char separator;
    /** Initialize locale value to null. */
    private String locale = "";
    /** Declare file root variable. */
    private String fileRoot;
    /** Declare dcr category variable. */
    private String dcrCategory;
    /** Initialize hashmap for config parameter. */
    private static HashMap<String, String> configParamsMap = new HashMap();
    /** Null method.
     */
    public CommonUtils() {
    }
    /** This method will set the class local variables.
     * @param requestContext component context passed with params.
     */
    public CommonUtils(final RequestContext requestContext) {
        this.context = requestContext;
        this.liveSiteDal = requestContext.getLiveSiteDal();
        this.fileDal = requestContext.getFileDal();
        this.fileRoot = this.fileDal.getRoot();
        this.separator = this.fileDal.getSeparator();
        this.locale = requestContext.getParameterString("locale", "en");
        if (context.getParameterString("dcrcategory") != null) {
            this.dcrCategory = context.getParameterString("dcrcategory");
        } else {
            this.dcrCategory = "Content";
        }
        logger.info("CommonUtils(dcrCategory) : " + dcrCategory);
    }
    /**This method will get dcr path as param and check if it
     * exists, read it and return as a document.
     * @param path DCR path.
     *
     * @return doc dcr content as a document.
     */
    public Document readDCR(final String path) {
        logger.info("File Read Request Received: " + path);
        Document doc = null;
        if (isPathExists(path)) {
            logger.info("File Path exists: " + path);
            doc = this.liveSiteDal.readXmlFile(path);
            logger.info("DCR Retrieved: " + doc.asXML());
        } else {
            logger.error("DCR does not exist at path : " + path);
        }
        return doc;
    }
    /**This method will get dcr path as param and check if it
     * exists.
     * @param path DCR path.
     *
     * @return boolean return whether the file exists or not.
     */
    public boolean isPathExists(final String path) {
        String completePath = getCompletePath(path);
        return this.fileDal.exists(completePath);
    }
    /**This method will get dcr path as param, append it with file
     * root to return the complete file path.
     * @param path DCR path.
     *
     * @return CompletePath complete fiepath with root.
     */
    public String getCompletePath(final String path) {
        String root = this.fileRoot;
        String completePath = root + this.separator + path;
        logger.info("Complete Path of File: " + path);
        return completePath;
    }
    /** This method will get various component context param
     * values to fetch the dcr content and return as documument.
     * @param reqcontext component context passed with params.
     *
     * @return doc return dcr content as document.
     */
    public Document getDCRContent(final RequestContext reqcontext) {
        logger.info("Fetching DCR Content");
        Document doc = DocumentHelper.createDocument();
        Element root = doc.addElement("content");
        String dct = reqcontext.getParameterString("category");
        logger.info("Type of DCT: " + dct);
        locale = reqcontext.getParameterString("locale", "en");
        logger.info("Locale: " + locale);
        String dcr = reqcontext.getParameterString("record");
        logger.info("DCR: " + dcr);
        CommonUtils commonUtils = new CommonUtils(reqcontext);
        String dcrPath = commonUtils.getCategoryPath(dct, locale, dcr);
        logger.info("DCR Path: " + dcrPath);
        Document data = commonUtils.readDCR(dcrPath);
        if (data == null) {
            try {
                reqcontext.getResponse().sendError(CONTEXT_ERROR_CODE);
            } catch (IOException ex) {
                logger.error("Error while setting response", ex);
            }
            return null;
        }
        Element detailedElement = data.getRootElement();
        String titleItem = reqcontext.getParameterString("titleXPath");
        if (titleItem != null) {
            logger.info("Title Item: " + titleItem);
            Node titleNode = data.selectSingleNode(titleItem);
            if (titleNode != null) {
                String title = titleNode.getStringValue();
                logger.info("Title Set for Breadcrumb: " + title);
                reqcontext.getPageScopeData().put("record.title", title);
            }
        }
        logger.info("DCR: " + dcr);
        root.add(detailedElement);
        return doc;
    }
    /** This method will take dct value and concatenate them
     * with dcr category to return the dcr category path.
     * @param dct DCT value..
     *
     * @return categoryPath return the dct and locale concatenated
     * category path.
     */
    public String getCategoryPath(final String dct) {
        logger.info("Building Path of Category: " + dct);
        StringBuilder categoryPath = new StringBuilder("templatedata");
        categoryPath.append(this.separator).append(dcrCategory);
        categoryPath.append(this.separator).append(dct);
        categoryPath.append(this.separator).append("data");
        categoryPath.append(this.separator);
        logger.info("Path of Category: " + dct);
        return categoryPath.toString();
    }
    /** This method will take dct,dcr name and locale to concatenate them
     * and return the dcr category path.
     * @param dct DCT value..
     * @param localeValue Locale value.
     * @param dcr DCR name value.
     *
     * @return categoryPath return the dct and locale concatenated
     * category path.
     */
    public String getCategoryPath(final String dct,
                                  final String localeValue, final String dcr) {
        String categoryPath = getCategoryPath(dct, localeValue);
        categoryPath = categoryPath + dcr;
        return categoryPath;
    }
    /** This method will take dct name and locale, concatenate them
     * to return the dcr category path.
     * @param dct DCT value..
     * @param localeValue Locale value.
     *
     * @return categoryPath return the dct and locale concatenated
     * category path.
     */
    public String getCategoryPath(final String dct,
                                  final String localeValue) {
        String categoryPath = getCategoryPath(dct);
        categoryPath = categoryPath + localeValue + this.separator;
        return categoryPath;
    }

    /**
     * this method will get config param from table and set to hashmap.
     *
     * @param utilContext component context passed with param
     */
    public void loadConfigparams(final RequestContext utilContext) {
        logger.info("in getmailserverProperty:");
        Statement st = null;
        ResultSet rs = null;
        String query = null;
        Connection connection = null;
        Postgre objPostgre = new Postgre(utilContext);
        query = "SELECT * FROM CONFIG_PARAM";
        try {
            connection = objPostgre.getConnection();
            st = connection.createStatement();
            rs = st.executeQuery(query);
            while (rs.next()) {
                String configParamCode = rs.getString("config_param_code");
                String configParamValue = rs
                        .getString("config_param_value");
                logger.debug("configParamCode:" + configParamCode);
                logger.debug("configParamValue:" + configParamValue);
                configParamsMap.put(configParamCode, configParamValue);
            }

        } catch (SQLException e) {
            logger.error("getConfiguration()" + e.getMessage());
            e.printStackTrace();
        } finally {
            objPostgre.releaseConnection(connection, st, rs);
        }
        objPostgre.releaseConnection(connection, st, rs);
    }

    /**
     * this method will take config parameter code and return config
     * parameter value.
     *
     * @param property
     * @param utilContext
     * @return configParamValue return config parameter value.
     */
    public  String getConfiguration(final String property,
            final RequestContext utilContext) {
        CommonUtils util = new CommonUtils();
        String configParamValue = null;
        if (property != null && !"".equals(property)) {
            if (CommonUtils.configParamsMap == null
                    || CommonUtils.configParamsMap.isEmpty()) {
                util.loadConfigparams(utilContext);
            }
            configParamValue = CommonUtils.configParamsMap.get(property);
            logger.debug("configParamValue:" + configParamValue);

        }
        return configParamValue;

    }
    /**
     * This method will take language, and returns Locale.
     *
     * @param language page language
     * @return locale
     */
    public Locale getLocale(final String language) {
        if ("en".equals(language)) {
            return Locale.ENGLISH;
        } else if ("ar".equals(language)) {
            return new Locale("ar");
        } else {
            return Locale.ENGLISH;
        }
    }

    /**
     * This method will take encode arabic string and returns decode arabic
     * string.
     *
     * @param encodedArabicString
     * @return decodedArabicString
     */
    public String decodeToArabicString(final String encodedArabicString) {
        byte[] charset = encodedArabicString
                .getBytes(StandardCharsets.UTF_8);
        return new String(charset, StandardCharsets.UTF_8);
    }
}
