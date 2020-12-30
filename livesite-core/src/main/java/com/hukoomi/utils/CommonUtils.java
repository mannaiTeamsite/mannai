/*
 * Common utils java for local DCR operations.
 */
package com.hukoomi.utils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.imageio.ImageIO;

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
        logger.info("Complete Path of File: " + completePath);
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
        if(dcr.equals("")){
            return doc;
        }
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
     * This method will take language, and returns Locale.
     *
     * @param language page language
     * @return locale
     */
    public Locale getLocale(final String language) {
        logger.info("getLocale: Enter");
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
        logger.info("decodeToArabicString: Enter");
        byte[] charset =
                encodedArabicString.getBytes(StandardCharsets.UTF_8);
        return new String(charset, StandardCharsets.UTF_8);
    }

    /** Get String Value for node from an XML Document Object
     *
     * @param xPath xPath for the Node.
     * @param document Document from which node needs to be retrieved.
     *
     * @return String value of the XML Node if it is available in Document. Blank String otherwise.
     */
    public String getValueFromXML(String xPath, Document document){
        String value = "";
        if(xPath.equals("")){
            return value;
        }
        Node node = document.selectSingleNode(xPath);
        logger.info("Retrieved Node Value for : " + xPath);
        if(node != null){
            value = node.getStringValue().trim();
        }
        return value;
    }

    /*
     * Get Dimensions of the Image in form of width and height.
     *
     * @param String imageSourcePath Path of the Image file.
     *
     * @return Map<String, Integer> Map with values as "width" and "height".
     */
    public Map<String, Integer> getImageDimensions(String imageSourcePath){
        Map<String, Integer> dimensions = new HashMap<>();
        if(imageSourcePath.equals("")){
            return dimensions;
        }
        File imageFile = new File(imageSourcePath);
        if(imageFile.exists()){
            BufferedImage image = null;
            try {
                image = ImageIO.read(new File(imageSourcePath));
            } catch (IOException ex) {
                logger.error("Error while Retrieving Image File.",ex);
            }
            if(image != null) {
                dimensions.put("width", image.getWidth());
                dimensions.put("height", image.getHeight());
            }
        }
        return dimensions;
    }

    /*
     * Get pretty URL from the Page Link items.
     *
     * @param url URL to convert as a pretty print.
     * @param locale String representing the locale for which URL to be generated while pretty print.
     * @param dcrName String DCR Name if the URL is required for any Detail pages.
     *
     * @return String prettyURL Converted Pretty URL.
     */
    public String getPrettyURLForPage(String url, String locale, String dcrName) {
        String prettyURL = "";
        if (url.equals("")){
            return prettyURL;
        }
        prettyURL = url.replaceAll("/sites|/portal-|-details.page","/");
        prettyURL = prettyURL.replaceFirst("/en/|/ar/","/"+locale+"/");
        if(!dcrName.equals("")){
            prettyURL = prettyURL + dcrName;
        }
        prettyURL = prettyURL.endsWith("/") ? prettyURL.substring(0,prettyURL.length()-1) : prettyURL;
        return prettyURL;
    }

}
