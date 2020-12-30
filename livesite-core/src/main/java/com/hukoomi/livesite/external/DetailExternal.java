/**
 * External Java to fetch the dcr content passed as param.
 */
package com.hukoomi.livesite.external;

import com.hukoomi.utils.CommonUtils;
import com.hukoomi.utils.RequestHeaderUtils;
import com.interwoven.livesite.runtime.RequestContext;
import com.interwoven.livesite.runtime.model.page.RuntimePage;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;

import java.util.Map;

public class DetailExternal {
/** Logger object to check the flow of the code. */
private final Logger logger = Logger.getLogger(DetailExternal.class);
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
    RequestHeaderUtils requestHeaderUtils = new RequestHeaderUtils(context);
    String hostname = requestHeaderUtils.getForwardedHost();
    String urlScheme = "https";
    if(context.getRequest().getScheme()!=null){
        urlScheme = context.getRequest().getScheme();
    }
    String urlPrefix = urlScheme + "://" + hostname;
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
        String imageValue = urlPrefix + commonUtils.getValueFromXML("/content/root/information/image", detailDocument);
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
        context.getPageScopeData().put("current-url", urlPrefix + prettyURLforCurrentPage);
        context.getPageScopeData().put("href-lang-default", urlPrefix + prettyURLforCurrentPage);
        logger.info("Set PageScope href-lang-default as: " + urlPrefix + prettyURLforCurrentPage);
        context.getPageScopeData().put("href-lang-en", urlPrefix + commonUtils.getPrettyURLForPage(currentPageLink, paramLocale, dcrName));
        context.getPageScopeData().put("href-lang-ar", urlPrefix + commonUtils.getPrettyURLForPage(currentPageLink, paramLocale, dcrName));
        logger.info("Set PageScope href-lang attributes for Alternate Language");
    } catch (Exception e) {
        logger.error("Error fetching detail content for record "
                + context.getParameterString("record"), e);
    }
    return detailDocument;
}
}
