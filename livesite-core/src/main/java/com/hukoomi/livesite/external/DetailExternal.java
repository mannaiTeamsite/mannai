/*
 * External Java to fetch the dcr content passed as param.
 */
package com.hukoomi.livesite.external;

import com.hukoomi.exception.DCRNotFoundException;
import com.hukoomi.livesite.controller.SEOController;
import com.hukoomi.utils.CommonUtils;
import com.interwoven.livesite.runtime.RequestContext;
import com.interwoven.livesite.runtime.model.page.RuntimePage;
import org.apache.log4j.Logger;
import org.dom4j.Document;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class DetailExternal {
    /** Logger object to check the flow of the code. */
    private final Logger logger = Logger.getLogger(DetailExternal.class);
    /** This method will be called from Component
     * External for DCR Content fetching.
     * Throws DCRNotFound exception if the DCR is not found.
     * @param context The parameter context object passed from Component.
     *
     * @return detailDocument return the document generated from
     * the mapped DCR.
     */
    @SuppressWarnings("deprecation")
    public Document getContentDetail(final RequestContext context) {
        CommonUtils commonUtils = new CommonUtils();
        Document detailDocument = commonUtils.getDCRContent(context);
        if(detailDocument == null) {
            logger.info("DCR Not Found Responding with 404");
            try {
                context.getResponse().setStatus(HttpServletResponse.SC_NOT_FOUND);
                context.getResponse().sendError(HttpServletResponse.SC_NOT_FOUND);
            } catch (IOException ex) {
                logger.error("Error while setting response", ex);
            }
            throw new DCRNotFoundException("Content DCR not found");
        }
        if(!context.getParameterString("detail-page","true").equals("true")){
            logger.info("The Component is not present on a detail page. Skipping the Dynamic metadata values.");
            return detailDocument;
        }
        String urlPrefix = commonUtils.getURLPrefix(context);
        String paramLocale = context.getParameterString("locale", "en");
        logger.info("paramLocale : " + paramLocale);
        String title = commonUtils.sanitizeMetadataField(commonUtils.getValueFromXML("/content/root/information/title", detailDocument));
        if(title.equals("")){
            logger.debug("No DCR found to add the PageScope Data");
            return detailDocument;
        }
        context.getPageScopeData().put(RuntimePage.PAGESCOPE_TITLE, title);
        logger.info("Set PageScope Title : " + title);
        String locale = paramLocale.equals("ar") ? "ar_QA" :"en_US";
        context.getPageScopeData().put("locale",locale);
        logger.info("PageScope Locale : " + locale);
        logger.info("Current PageScopeData: "+context.getPageScopeData().toString());
        String description = commonUtils.sanitizeMetadataField(commonUtils.getValueFromXML("/content/root/page-details/description", detailDocument));
        context.getPageScopeData().putIfAbsent(RuntimePage.PAGESCOPE_DESCRIPTION, description);
        logger.info("Set PageScope Meta Description to : " + description);
        String keywords = commonUtils.sanitizeMetadataField(commonUtils.getValueFromXML("/content/root/page-details/keywords", detailDocument));
        context.getPageScopeData().putIfAbsent(RuntimePage.PAGESCOPE_KEYWORDS, keywords);
        logger.info("Set PageScope Meta Keywords to : " + keywords);
        String contentCategory = commonUtils.getValueFromXML("/content/root/category", detailDocument);
        String ogType = contentCategory.equals("Articles") ? "article" : "website";
        context.getPageScopeData().putIfAbsent("ogType", ogType);
        logger.info("Set PageScope ogType to : " + ogType);
        String articlePublishDate = commonUtils.getValueFromXML("/content/root/date", detailDocument);
        context.getPageScopeData().putIfAbsent("article-published-time", articlePublishDate);
        context.getPageScopeData().putIfAbsent("article-modified-time", commonUtils.getValueFromXML("/content/root/date", detailDocument));
        logger.info("Set PageScope article-published-time / article-modified-time to : " + articlePublishDate);
        context.getPageScopeData().put("article-tag", keywords);
        logger.info("Set PageScope article-tag to : " + keywords);
        String imageValue = commonUtils.getValueFromXML("/content/root/information/image", detailDocument);
        if(imageValue.equals("")){
            imageValue = paramLocale.equals("ar") ? SEOController.DEFAULT_SOCIAL_IMAGE_AR : SEOController.DEFAULT_SOCIAL_IMAGE_EN;
        }
        commonUtils.generateImageMetadata(context, imageValue, urlPrefix);
        String currentPageLink = context.getPageLink(".");
        String dcrName = context.getParameterString("record");
        logger.info("Current Page Link " + currentPageLink);
        String prettyURLforCurrentPage = commonUtils.getPrettyURLForPage(currentPageLink, paramLocale, dcrName);
        context.getPageScopeData().remove("current-url");
        context.getPageScopeData().put("current-url", urlPrefix + prettyURLforCurrentPage);
        context.getPageScopeData().remove("href-lang-default");
        context.getPageScopeData().put("href-lang-default", urlPrefix + prettyURLforCurrentPage);
        logger.info("Set PageScope href-lang-default as: " + urlPrefix + prettyURLforCurrentPage);
        context.getPageScopeData().remove("href-lang-en");
        context.getPageScopeData().put("href-lang-en", urlPrefix + commonUtils.getPrettyURLForPage(currentPageLink, "en", dcrName));
        context.getPageScopeData().remove("href-lang-ar");
        context.getPageScopeData().put("href-lang-ar", urlPrefix + commonUtils.getPrettyURLForPage(currentPageLink, "ar", dcrName));
        logger.info("Set PageScope href-lang attributes for Alternate Language");
        return detailDocument;
    }
}
