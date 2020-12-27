/**
 * External Java to fetch the dcr content passed as param.
 */
package com.hukoomi.livesite.external;

import com.hukoomi.utils.CommonUtils;
import com.interwoven.livesite.runtime.RequestContext;
import com.interwoven.livesite.runtime.model.page.RuntimePage;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import com.hukoomi.utils.SolrQueryUtil;
import com.hukoomi.utils.PropertiesFileReader;
import org.dom4j.Element;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Map;
import java.util.Properties;

public class DetailExternalRelatedContent {
    /** Default query to fetch all solr content. */
    public static final String DEFAULT_QUERY = "*:*";
    /** Default query to fetch all solr content. */
    public static final String UTF = "UTF-8";
/** Logger object to check the flow of the code. */
private final Logger logger = Logger.getLogger(DetailExternalRelatedContent.class);
/** This method will be called from Component
 * External for DCR Content fetching.
 * @param context The parameter context object passed from Component.
 *
 * @return detailDocument return the document generated from
 * the mapped DCR.
 */
@SuppressWarnings("deprecation")
public Document getContentDetail(final RequestContext context) {
    Document detailDocument = DocumentHelper.createDocument();
    CommonUtils commonUtils = new CommonUtils();
    String paramLocale = context.getParameterString("locale", "en");
    logger.info("paramLocale : " + paramLocale);
    try {
        detailDocument = commonUtils.getDCRContent(context);
        if(!context.getParameterString("detail-page","true").equals("true")){
            logger.info("The Component is not present on a detail page. Skipping the Dynamic metadata values.");
            return detailDocument;
        }
        String title = commonUtils.getValueFromXML("/content/root/information/title", detailDocument);
        if(title.equals("")){
            logger.debug("No DCR found to add the PageScope Data");
            return detailDocument;
        }
        context.getPageScopeData().put(RuntimePage.PAGESCOPE_TITLE, title);
        logger.info("Set PageScope Title : " + title);
        String locale = paramLocale.equals("ar") ? "ar_QA" :"en_US";
        context.getPageScopeData().put("locale",locale);
        logger.info("PageScope Locale : " + locale);
        String description = commonUtils.getValueFromXML("/content/root/page-details/description", detailDocument);
        logger.info("Set PageScope Meta Description to : " + description);
        String keywords = commonUtils.getValueFromXML("/content/root/page-details/keywords", detailDocument);
        logger.info("Set PageScope Meta Keywords to : " + keywords);
        String contentCategory = commonUtils.getValueFromXML("/content/root/category", detailDocument);
        String ogType = contentCategory.equals("Articles") ? "article" : "website";
        context.getPageScopeData().put("ogType", ogType);
        logger.info("Set PageScope ogType to : " + ogType);
        String articlePublishDate = commonUtils.getValueFromXML("/content/root/date", detailDocument);
        context.getPageScopeData().put("article-published-time", articlePublishDate);
        context.getPageScopeData().put("article-modified-time", commonUtils.getValueFromXML("/content/root/date", detailDocument));
        logger.info("Set PageScope article-published-time / article-modified-time to : " + articlePublishDate);
        context.getPageScopeData().put("article-tag", keywords);
        logger.info("Set PageScope article-tag to : " + keywords);
        String imageValue = commonUtils.getValueFromXML("/content/root/information/image", detailDocument);
        if(!imageValue.equals("")){
            context.getPageScopeData().put("image", imageValue);
            logger.info("Image added to the PageScope: " + imageValue);
            Map<String, Integer> imageDimensions = commonUtils.getImageDimensions(context.getFileDal().getRoot() + context.getFileDal().getSeparator() + imageValue);
            if(!imageDimensions.isEmpty()){
                int imageWidth = imageDimensions.containsKey("width") ? imageDimensions.get("width") : 0;
                int imageHeight = imageDimensions.containsKey("height") ? imageDimensions.get("height") : 0;
                context.getPageScopeData().put("image-width", imageWidth);
                context.getPageScopeData().put("image-height", imageHeight);
                logger.info("Set PageScope image dimensions as: " + imageWidth + " * " + imageHeight);
            }
        }
        String currentPageLink = context.getPageLink(".");
        String dcrName = context.getParameterString("record");
        logger.info("Current Page Link " + currentPageLink);
        String prettyURLforCurrentPage = commonUtils.getPrettyURLForPage(currentPageLink, paramLocale, dcrName);
        context.getPageScopeData().put("current-url", prettyURLforCurrentPage);
        context.getPageScopeData().put("href-lang-default", prettyURLforCurrentPage);
        logger.info("Set PageScope href-lang-default as: " + prettyURLforCurrentPage);
        context.getPageScopeData().put("href-lang-en", commonUtils.getPrettyURLForPage(currentPageLink, paramLocale, dcrName));
        context.getPageScopeData().put("href-lang-ar", commonUtils.getPrettyURLForPage(currentPageLink, paramLocale, dcrName));
        logger.info("Set PageScope href-lang attributes for Alternate Language");
    } catch (Exception e) {
        logger.error("Error fetching detail content for record "
                + context.getParameterString("record"), e);
    }
    return getRelatedContent(context, detailDocument);
}

    /** This method will be called from Component
     * External for related solr Content fetching.
     * @param context The parameter context object passed from Component.
     * @param detailDocument The final content document to be returned.
     *
     * @return no return type.
     */
    public Document getRelatedContent(
            final RequestContext context,
            final Document detailDocument) {
        SolrQueryUtil squ = new SolrQueryUtil();
        PropertiesFileReader propertyFileReader = new PropertiesFileReader(
                context, "solrconfig.properties");
        Properties properties = propertyFileReader.getPropertiesFile();
        String fieldQuery = "";
        String baseQuery = "";
        String solrHost = "";
        if (context.isRuntime()) {
            solrHost = context.getParameterString("solrHost",
                    properties.getProperty("solrRuntimeHost"));
        } else {
            solrHost = context.getParameterString("solrHost",
                    properties.getProperty("solrAuthHost"));
        }
        final String solrCore = context.getParameterString("solrCore");
        final String baseUrl = solrHost + "/" + solrCore ;
        String rows = context.getParameterString("rows", "9");
        final String requestHandler = context.getParameterString("requestHandler",
                properties.getProperty("requestHandler"));
        StringBuilder sb = new StringBuilder(baseUrl);
        sb.append("/" + requestHandler);
        String fq = context.getParameterString(
                "relatedQuery", "");
        try {
            fieldQuery = URLDecoder.decode(
                    fq, UTF);
            logger.debug("fieldQuery Query : " + fieldQuery);
        } catch (UnsupportedEncodingException e) {
            logger.warn("Unable to decode fieldQuery="
                    + fq, e);
        }
        try {
            baseQuery = URLDecoder.decode(context
                    .getParameterString("baseQuery", DEFAULT_QUERY), UTF);
            logger.debug("Solr Base Query: " + baseQuery);
        } catch (UnsupportedEncodingException e) {
            logger.error("Unable to decode baseQuery="
                    + context.getParameterString("baseQuery",
                    DEFAULT_QUERY), e);
        }
        sb.append("?q=" + (StringUtils.isNotBlank(baseQuery)
                ? baseQuery : DEFAULT_QUERY));
        if (StringUtils.isNotBlank(fieldQuery)) {
            sb.append("&fq=" + fieldQuery);
        }
        Document relatedDoc = squ.doJsonQuery(sb.toString(),
                "relatedContent");
        detailDocument.getRootElement().add(relatedDoc.getRootElement());
        String otherQuery = context
                .getParameterString("otherQuery", "");
        logger.debug("otherQuery : " + otherQuery);
        if (StringUtils.isNotBlank(otherQuery)) {
            Document otherContDoc = squ.doJsonQuery(sb.toString() +
                            "&" + otherQuery, "otherContent");
            detailDocument.getRootElement().add(otherContDoc.getRootElement());
        }
        return detailDocument;
    }
}
