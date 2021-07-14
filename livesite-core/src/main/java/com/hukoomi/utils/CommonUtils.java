/*
 * Common utils java for local DCR operations.
 */
package com.hukoomi.utils;

import com.hukoomi.exception.DCRNotFoundException;
import com.hukoomi.livesite.controller.SEOController;
import com.interwoven.livesite.common.text.StringUtil;
import com.interwoven.livesite.file.FileDal;
import com.interwoven.livesite.runtime.LiveSiteDal;
import com.interwoven.livesite.runtime.RequestContext;
import com.interwoven.livesite.runtime.model.page.RuntimePage;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.ValidationErrorList;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/*
 * Since the Context.getParameterString method is deprecated by Product,
 * Suppress Sonarlint warnings about deprecation,
 */
@SuppressWarnings("deprecation")
public class CommonUtils {
    /**
     * Logger object to check the flow of the code.
     */
    private final Logger logger = Logger.getLogger(CommonUtils.class);
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
    /** Declare Constant Variable name for Locale. */
    private static final String PARAM_LOCALE = "locale";
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
        this.locale = requestContext.getParameterString(PARAM_LOCALE, "en");
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
        locale = reqcontext.getParameterString(PARAM_LOCALE, "en");
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
        logger.info("Retrieving Dimensions for: " + imageSourcePath);
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
            } catch (IllegalArgumentException ex) {
                logger.error("Illegal Argument Exception while getting dimensions for Image", ex);
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
        prettyURL = url.replaceAll("/sites|/portal-|-details.page|home.page","/");
        prettyURL = prettyURL.replaceFirst("/en/|/ar/","/"+locale+"/");
        if(!dcrName.equals("")){
            prettyURL = prettyURL + dcrName;
        }
        prettyURL = prettyURL.endsWith("/") ? prettyURL.substring(0,prettyURL.length()-1) : prettyURL;
        prettyURL = prettyURL.endsWith(".page") ? prettyURL.substring(0,prettyURL.indexOf(".page")) : prettyURL;
        return prettyURL;
    }

    /*
     * Get Sanitized Solr Query String.
     *
     * @param parameter String
     *
     * @return String sanitized solr query.
     */
    public String sanitizeSolrQuery(String parameter) {
        if(parameter == null || parameter.isBlank()){
            return "";
        }
        parameter = parameter.replaceAll("[^a-zA-Z0-9- \\\"*+%:~!_,.\\[\\]\\{\\}\\(\\)\\p{IsArabic}]","");
        String scriptRemovalRegex = "<script>(.*)</script>";
        try {
            logger.info("Decoding Parameter prior to script tag removal");
            parameter = URLDecoder.decode(parameter,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            logger.error("Error while decoding parameter",e);
        }
        return parameter.replaceAll(scriptRemovalRegex,"");
    }

    /*
     * Get URL Prefix based on the Request Header forwarded host.
     * This helps create the URL with FQDN values.
     *
     * @param RequestContext context
     * @return String urlprefix value. For example: "https://hukoomi.gov.qa"
     */
    public String getURLPrefix(RequestContext context){
        RequestHeaderUtils requestHeaderUtils = new RequestHeaderUtils(context);
        String hostname = requestHeaderUtils.getForwardedHost();
        if(hostname.isBlank() || hostname.equals("") || !hostname.matches("(dev\\.|stg\\.)?hukoomi\\.gov\\.qa")){
            hostname = "hukoomi.gov.qa";
        }
        String urlScheme = "https";
        if(context.getRequest().getScheme()!=null && !context.getRequest().getScheme().isBlank()){
            urlScheme = context.getRequest().getScheme();
        }
        return urlScheme + "://" + hostname;
    }

    /*
     * Generate Image SEO Metadata.
     * For Example, og:image, twitter:image, etc.
     *
     * @param RequestContext context
     * @param String imageValue Image path.
     * @param String urlPrefix Prefix of URL.
     */
    public void generateImageMetadata(RequestContext context, String imageValue, String urlPrefix){
        if(imageValue.isBlank()){
            return;
        }
        context.getPageScopeData().put("image", urlPrefix + imageValue);
        logger.info("Image added to the PageScope: " + urlPrefix + imageValue);
        Map<String, Integer> imageDimensions = getImageDimensions(context.getFileDal().getRoot() + context.getFileDal().getSeparator() + imageValue);
        if(!imageDimensions.isEmpty()){
            int imageWidth = imageDimensions.getOrDefault("width", 0);
            int imageHeight = imageDimensions.getOrDefault("height", 0);
            context.getPageScopeData().put("image-width", imageWidth);
            context.getPageScopeData().put("image-height", imageHeight);
            logger.info("Set PageScope image dimensions as: " + imageWidth + " * " + imageHeight);
        }
    }

    /*
     * Sanitize Values to be used for HTML metadata.
     * This prevents breaking HTML meta tags for special characters.
     * Uses LiveSite StringUtil methods.
     *
     * @param String metadata
     *
     * @return String sanitized metadata
     */
    public String sanitizeMetadataField(String metadata) {
        if(!metadata.isBlank()){
            metadata = StringUtil.toHTMLEntity(metadata);
        }
        return metadata;
    }

    /*
     * Generate SEO Metatags in PageScope for Dynamic Content.
     *
     * @param Document DCR XML Dom4j Document
     * @param RequestContext context
     */
    public void generateSEOMetaTagsForDynamicContent(Document dcr, RequestContext context) {
        String urlPrefix = getURLPrefix(context);
        String paramLocale = context.getParameterString(PARAM_LOCALE, "en");
        logger.info("paramLocale : " + paramLocale);
        String title = sanitizeMetadataField(getValueFromXML("/content/root/information/title", dcr));
        if(title.equals("")){
            logger.debug("No DCR found to add the PageScope Data");
            return;
        }
        context.getPageScopeData().put(RuntimePage.PAGESCOPE_TITLE, title);
        logger.info("Set PageScope Title : " + title);
        String docLocale = paramLocale.equals("ar") ? "ar_QA" :"en_US";
        context.getPageScopeData().put(PARAM_LOCALE,docLocale);
        logger.info("PageScope Locale : " + docLocale);
        logger.info("Current PageScopeData: "+context.getPageScopeData().toString());
        String description = sanitizeMetadataField(getValueFromXML("/content/root/page-details/description", dcr));
        context.getPageScopeData().put(RuntimePage.PAGESCOPE_DESCRIPTION, description);
        logger.info("Set PageScope Meta Description to : " + description);
        String keywords = sanitizeMetadataField(getValueFromXML("/content/root/page-details/keywords", dcr));
        context.getPageScopeData().put(RuntimePage.PAGESCOPE_KEYWORDS, keywords);
        logger.info("Set PageScope Meta Keywords to : " + keywords);
        String contentCategory = getValueFromXML("/content/root/category", dcr);
        String ogType = contentCategory.equals("Articles") ? "article" : "website";
        context.getPageScopeData().put("ogType", ogType);
        logger.info("Set PageScope ogType to : " + ogType);
        String articlePublishDate = getValueFromXML("/content/root/date", dcr);
        context.getPageScopeData().put("article-published-time", articlePublishDate);
        context.getPageScopeData().put("article-modified-time", getValueFromXML("/content/root/date", dcr));
        logger.info("Set PageScope article-published-time / article-modified-time to : " + articlePublishDate);
        context.getPageScopeData().put("article-tag", keywords);
        logger.info("Set PageScope article-tag to : " + keywords);
        String imageValue = getValueFromXML("/content/root/information/image", dcr);
        if(imageValue.equals("")){
            imageValue = paramLocale.equals("ar") ? SEOController.DEFAULT_SOCIAL_IMAGE_AR : SEOController.DEFAULT_SOCIAL_IMAGE_EN;
        }
        generateImageMetadata(context, imageValue, urlPrefix);
        String currentPageLink = context.getPageLink(".");
        String dcrName = context.getParameterString("record");
        logger.info("Current Page Link " + currentPageLink);
        String prettyURLforCurrentPage = getPrettyURLForPage(currentPageLink, paramLocale, dcrName);
        context.getPageScopeData().put("current-url", urlPrefix + prettyURLforCurrentPage);
        context.getPageScopeData().put("href-lang-default", urlPrefix + prettyURLforCurrentPage);
        logger.info("Set PageScope href-lang-default as: " + urlPrefix + prettyURLforCurrentPage);
        context.getPageScopeData().put("href-lang-en", urlPrefix + getPrettyURLForPage(currentPageLink, "en", dcrName));
        context.getPageScopeData().put("href-lang-ar", urlPrefix + getPrettyURLForPage(currentPageLink, "ar", dcrName));
        logger.info("Set PageScope href-lang attributes for Alternate Language");
    }

    /*
     * Throw DCR Not Found Exception.
     * Redirect user to the custom error page with 404 status code.
     *
     * @param RequestContext context
     * @param String message to throw as part of HTTP status/exception.
     */
    public void throwDCRNotFoundError(RequestContext context, String message){
        logger.debug("DCR Not Found Responding with 404");
        RequestHeaderUtils requestHeader = new RequestHeaderUtils(context);
        String referer = requestHeader.getReferer();
        if(StringUtils.isNotBlank(referer) && context.isRuntime()){
            logger.info("The user was redirected to the page via a broken link source. Logging the incident in Database.");
            logBrokenLink(requestHeader.getRequestURL(),referer,locale,"404");
        }
        try {
            context.getResponse().setStatus(HttpServletResponse.SC_NOT_FOUND);
            context.getResponse().sendError(HttpServletResponse.SC_NOT_FOUND);
        } catch (IOException ex) {
            logger.error(message, ex);
        }
        throw new DCRNotFoundException(message);
    }

    /*
     * Log Broken Link to the Databse.
     *
     * @param String brokenLink The BrokenLink which has been reported
     * @param String contentPage The Content Page/Referrer from where the broken link is present.
     * @param String language The content Language of the portal where the BrokenLink has been reported.
     * @param String statusCode The Error Status code to report the BrokenLink.
     */
    public void logBrokenLink(String brokenLink, String contentPage, String language, String statusCode){
        logger.debug("Logging Broken link in Database");
        ValidationErrorList errorList = new ValidationErrorList();
        
        try {
        	brokenLink = new URL(brokenLink).getPath();
			 
		} catch (MalformedURLException e) {
			logger.debug(e);
		}
        if (!ESAPIValidator.checkNull(brokenLink)) {
            brokenLink  = ESAPI.validator().getValidInput("brokenLink", brokenLink, ESAPIValidator.URL, 255, false, true, errorList);
            if(!errorList.isEmpty()) {
                logger.info(errorList.getError("brokenLink"));
                logger.error("Not a valid parameter brokenLink. The incident will not be logged.");
                return;
            }
        }
        try {
        	contentPage = new URL(contentPage).getPath();
			 
		} catch (MalformedURLException e) {
			logger.debug(e);
		}
        if (!ESAPIValidator.checkNull(contentPage)) {
            contentPage  = ESAPI.validator().getValidInput("contentPage", contentPage, ESAPIValidator.URL, 255, false, true, errorList);
            if(!errorList.isEmpty()) {
                logger.info(errorList.getError("contentPage"));
                logger.error("Not a valid parameter contentPage. The incident will not be logged.");
                return;
            }
        }
        if (!ESAPIValidator.checkNull(language)) {
            language  = ESAPI.validator().getValidInput("language", language, ESAPIValidator.ALPHABET, 20, false, true, errorList);
            if(!errorList.isEmpty()) {
                logger.info(errorList.getError("language"));
                logger.error("Not a valid parameter language. The incident will not be logged.");
                return;
            }
        }
        if (!ESAPIValidator.checkNull(statusCode)) {
            statusCode  = ESAPI.validator().getValidInput("statusCode", statusCode, ESAPIValidator.ALPHANUMERIC, 20, false, true, errorList);
            if(!errorList.isEmpty()) {
                logger.info(errorList.getError("statusCode"));
                logger.error("Not a valid parameter statusCode. The incident will not be logged.");
                return;
            }
        }
        if(contentPage != null && brokenLink != null) {
        logger.info("Logging the Broken link with status: " + statusCode + ", for URL: " + brokenLink + ", present on: " + contentPage + " for language: " + language);
        PreparedStatement statement = null;
        Connection connection = null;
        Postgre database = new Postgre(context);
        try {
            logger.info("Logging of Broken Link in DB Started");
            connection = database.getConnection();
            String query = "INSERT INTO ERROR_RESPONSE ("
                    + "BROKEN_LINK, CONTENT_PAGE, LANGUAGE, STATUS_CODE, REPORTED_ON"
                    + ") VALUES(?,?,?,?,LOCALTIMESTAMP)";

            logger.info("Query to run: " + query);

            statement = connection.prepareStatement(query);
            statement.setString(1, brokenLink);
            statement.setString(2, contentPage);
            statement.setString(3, language);
            statement.setString(4, statusCode);
            int insertResponse = statement.executeUpdate();

            if( insertResponse == 1) {
                logger.info("Broken Link has been logged successfully.");
            }
            logger.info("Logging Broken link in Database completed");
        } catch (SQLException ex) {
            logger.error("Error while logging broken link to the database.", ex);
        } finally {
            logger.info("Releasing Database Connection.");
            database.releaseConnection(connection, statement, null);
            logger.info("Released Database Connection.");
        }
        }
    }
}
