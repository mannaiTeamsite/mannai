/*
 * SEOController, to generate the Base SEO meta tags for the Portal pages.
 */
package com.hukoomi.livesite.controller;

import com.hukoomi.utils.CommonUtils;
import com.interwoven.livesite.common.web.ForwardAction;
import com.interwoven.livesite.runtime.RequestContext;
import org.apache.log4j.Logger;

public class SEOController {
    /*
     * Logger object to check the flow of the code.
     */
    private final Logger logger = Logger.getLogger(SEOController.class);
    /* Declare Constant Variable name for Locale. */
    private static final String PARAM_LOCALE = "locale";
    public static final String DEFAULT_SOCIAL_IMAGE_EN = "/assets/images/hukoomi-social-en.png";
    public static final String DEFAULT_SOCIAL_IMAGE_AR = "/assets/images/hukoomi-social-ar.png";

    /*
     * Set Base SEO meta tags.
     * For example, Locale, Canonical URL, Alternate links, etc.
     * This is used as Site wide pre-controller.
     *
     * @param RequestContext context
     *
     * @return ForwardAction
     */
    public ForwardAction setBaseMetadata(RequestContext context)
    {
        String currentPageLink = context.getPageLink(".");
        if(!currentPageLink.contains("-details.page")) {
            CommonUtils commonUtils = new CommonUtils();
            String urlPrefix = commonUtils.getURLPrefix(context);
            String paramLocale = context.getSite().getLocale();
            logger.info("paramLocale : " + paramLocale);
            String paramLocaleISO = paramLocale.equals("ar") ? "ar_QA" : "en_US";
            context.getPageScopeData().put(PARAM_LOCALE, paramLocaleISO);
            logger.info("Current Page Link " + currentPageLink);
            String prettyURLforCurrentPage = commonUtils.getPrettyURLForPage(currentPageLink, paramLocale, "");
            context.getPageScopeData().remove("current-url");
            context.getPageScopeData().put("current-url", urlPrefix + prettyURLforCurrentPage);
            context.getPageScopeData().remove("href-lang-default");
            context.getPageScopeData().put("href-lang-default", urlPrefix + prettyURLforCurrentPage);
            logger.info("Set PageScope href-lang-default as: " + urlPrefix + prettyURLforCurrentPage);
            context.getPageScopeData().remove("href-lang-en");
            context.getPageScopeData().put("href-lang-en", urlPrefix + commonUtils.getPrettyURLForPage(currentPageLink, "en", ""));
            context.getPageScopeData().remove("href-lang-ar");
            context.getPageScopeData().put("href-lang-ar", urlPrefix + commonUtils.getPrettyURLForPage(currentPageLink, "ar", ""));
            logger.info("Set PageScope href-lang attributes for Alternate Language");
            String ogType = "website";
            context.getPageScopeData().put("ogType", ogType);
            logger.info("Set PageScope ogType to : " + ogType);
            if(paramLocale.equals("ar")){
                commonUtils.generateImageMetadata(context, DEFAULT_SOCIAL_IMAGE_AR, urlPrefix);
            } else {
                commonUtils.generateImageMetadata(context, DEFAULT_SOCIAL_IMAGE_EN, urlPrefix);
            }
        } else {
            logger.info("Details Page found, the SEOMetadata will be set per the dynamic detail content.");
        }
        return null;
    }
}
