package com.hukoomi.livesite.external;

import com.hukoomi.livesite.solr.SolrQueryBuilder;
import com.hukoomi.utils.SolrQueryUtil;
import com.interwoven.livesite.runtime.RequestContext;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class HukoomiExternal {
/** Logger object to check the flow of the code.*/
private final Logger logger =
        Logger.getLogger(HukoomiExternal.class);
/** Default query to fetch all solr content. */
public static final String DEFAULT_QUERY = "*:*";

/** This method will be called from Component
 * External for solr Content fetching.
 * @param context The parameter context object passed from Component.
 *
 * @return doc return the solr response document
 * generated from solr query.
 */
public Document getLandingContent(final RequestContext context) {
    SolrQueryUtil squ = new SolrQueryUtil();
    SolrQueryBuilder sqb = new SolrQueryBuilder(context);
    String fieldQuery = "";
    String fq = context.getParameterString(
            "fieldQuery", "");
    try {
        fieldQuery = URLDecoder.decode(
                fq, "UTF-8");
        logger.debug("fieldQuery Query : " + fieldQuery);
    } catch (UnsupportedEncodingException e) {
        logger.warn("Unable to decode fieldQuery="
                + fq, e);
    }
    String category = context
            .getParameterString("solrcategory", "");
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

    String fields = context.getParameterString(
            "fields", "");
    logger.debug("fields : " + fields);
    if (StringUtils.isNotBlank(fields)) {
        logger.debug("fieldQuery : " + fieldQuery);
        sqb.addFields(fields);
    }
    String query = sqb.build();
    logger.debug("Landing Query : " + query);
    Document doc = squ.doJsonQuery(query, "SolrResponse");
    Element root = doc.getRootElement();
    root.addElement("category").addText(category);
    return doc;
}

}
