package com.hukoomi.livesite.external;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.owasp.esapi.ESAPI;

import com.hukoomi.livesite.solr.SolrQueryBuilder;
import com.hukoomi.utils.CommonUtils;
import com.hukoomi.utils.SolrQueryUtil;
import com.hukoomi.utils.UserInfoSession;
import com.interwoven.livesite.common.web.CookieUtils;
import com.interwoven.livesite.runtime.RequestContext;

public class ContentAsAPIExternal {
    /** Logger object to check the flow of the code. */
    private final Logger logger = Logger.getLogger(ContentAsAPIExternal.class);
    /** Default query to fetch all solr content. */
    public static final String DEFAULT_QUERY = "*:*";
    /** Constant for cookie name. */
    private static final String DEFAULT_COOKIE = "persona";

    /**
     * This method will be called from Component External for solr Content fetching.
     * 
     * @param context The parameter context object passed from Component.
     *
     * @return json object as string return the solr response document generated from solr query.
     */
    public Document getSolrContent(RequestContext context) {
        SolrQueryUtil squ = new SolrQueryUtil();
        SolrQueryBuilder sqb = new SolrQueryBuilder(context);
        CommonUtils commonUtils = new CommonUtils();
        String fieldQuery = "";
        String fq = commonUtils.sanitizeSolrQuery(context.getParameterString("fieldQuery", ""));
        context.setParameterString("fieldQuery", fq);
        try {
            fieldQuery = URLDecoder.decode(fq, "UTF-8");
            logger.debug("fieldQuery Query : " + fieldQuery);
        } catch (UnsupportedEncodingException e) {
            logger.warn("Unable to decode fieldQuery=" + fq, e);
        }
        String category = commonUtils.sanitizeSolrQuery(context.getParameterString("solrcategory", ""));
        context.setParameterString("solrcategory", category);
        logger.debug("category : " + category);
        if (StringUtils.isNotBlank(category)) {
            if (StringUtils.isNotBlank(fieldQuery)) {
                fieldQuery += " AND category:" + category;
            } else {
                fieldQuery = "category:" + category;
            }
            logger.debug("fieldQuery : " + fieldQuery);
            sqb.addFieldQuery(fieldQuery);
        }

        String fields = commonUtils.sanitizeSolrQuery(context.getParameterString("fields", ""));
        context.setParameterString("fields", fields);
        logger.debug("fields : " + fields);
        if (StringUtils.isNotBlank(fields)) {
            logger.debug("fieldQuery : " + fieldQuery);
            sqb.addFields(fields);
        }

        String crawlFields = commonUtils.sanitizeSolrQuery(context.getParameterString("crawlFields", "title,url,description"));
        context.setParameterString("crawlFields", crawlFields);
        logger.debug("crawlFields : " + crawlFields);
        if (StringUtils.isNotBlank(crawlFields)) {
            logger.debug("fieldQuery : " + fieldQuery);
            sqb.addCrawlFields(crawlFields);
        }

        String highlighterVal = commonUtils.sanitizeSolrQuery(context.getParameterString("highlighter",""));
        logger.debug("highlighter: " +highlighterVal);
        if(StringUtils.isNotBlank(highlighterVal)){
            logger.debug("fieldQuery highlighter : " +fieldQuery);
            sqb.addHlTag(highlighterVal);
        }

        String highlightTagVal = commonUtils.sanitizeSolrQuery(context.getParameterString("highlightTag",""));
        logger.debug("highlightTag: "+highlightTagVal);
        if(StringUtils.isNotBlank(highlightTagVal)){
            logger.debug("fieldQuery highlightTag"+fieldQuery);
            sqb.addHlHtmlTag(highlightTagVal);
        }

        String highlightFieldVal = commonUtils.sanitizeSolrQuery(context.getParameterString("highlightField",""));
        logger.debug("highlightFieldVal: "+highlightFieldVal);
        if(StringUtils.isNotBlank(highlightFieldVal)){
            logger.debug("fieldQuery highlightFieldVal"+fieldQuery);
            sqb.addHlField(highlightFieldVal);
        }

        String cookieName = context.getParameterString("cookieName",DEFAULT_COOKIE);
        if(cookieName != null && !cookieName.equalsIgnoreCase("")){
            logger.debug("Cookie Name : " + cookieName);
            String personaCookieValue = CookieUtils.getValue(context.getRequest(), cookieName);
            if (personaCookieValue != null && !personaCookieValue.equalsIgnoreCase("")) {
                logger.debug("Persona Cookie Value: "+personaCookieValue);
                sqb.addDismaxBq(personaCookieValue);
            }
        }

        String query = sqb.build();
        logger.debug("Landing Query : " + query);
        Document doc = DocumentHelper.createDocument();
        Element contentElem = doc.addElement("Content");
        String jsonResp = squ.doJsonQuery(query);
        contentElem.setText(jsonResp);
        logger.info("Final Document" + doc.asXML());

        return doc;
    }

}
