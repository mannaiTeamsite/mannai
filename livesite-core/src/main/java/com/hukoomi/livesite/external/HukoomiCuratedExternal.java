package com.hukoomi.livesite.external;

import com.hukoomi.livesite.solr.SolrQueryBuilder;
import com.hukoomi.utils.CommonUtils;
import com.hukoomi.utils.SolrQueryUtil;
import com.interwoven.livesite.runtime.RequestContext;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.DocumentHelper;
import org.dom4j.Document;
import org.dom4j.Node;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;

public class HukoomiCuratedExternal {
/** Logger object to check the flow of the code.*/
private final Logger logger =
        Logger.getLogger(HukoomiCuratedExternal.class);
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
    Document document = DocumentHelper.createDocument();
    String field = "";
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
    field = fieldQuery;
    String curatedContent = context
            .getParameterString("curated-content", "");
    logger.debug("curated content : " + curatedContent);
    return getCuratedContent(context,
            document, curatedContent, fieldQuery, field);
}
    /** This method will be called from Component
     * External for solr Content fetching.
     * @param node curated DCR replicant node instance.
     * @param city city param value.
     * @param date date city param value.
     * @param fq solr field query.
     *
     * @return fieldQuery return final solr
     * field query.
     */
    public String getCuratedQuery(final String fq,
              final Node node, final String city,
                final String date) {
        String fieldQuery = fq;
        String curatedCategory = node
                .selectSingleNode("ContentType").getText();
        if (StringUtils.isNotBlank(fieldQuery)) {
            fieldQuery += " AND category:" + curatedCategory;
        } else {
            fieldQuery = "category:" + curatedCategory;
        }
        if (node.selectSingleNode("is-location-filter-required")
                .getText().equals("yes")) {
            fieldQuery += " AND " + city;
        }
        if (node.selectSingleNode("is-date-filter-required")
                .getText().equals("yes")) {
            fieldQuery += " AND " + date;
        }
        return fieldQuery;
    }

    /** This method will be called from Component
     * External for solr Content fetching.
     * @param context The parameter context object passed from Component.
     * @param document The final content document to be returned.
     * @param curatedContent curated DCR path passed as param to java.
     * @param fq solr field query.
     * @param field field variable with query content.
     *
     * @return document return the solr response document
     * generated from solr query.
     */
    public Document getCuratedContent(
            final RequestContext context,
            final Document document, final String curatedContent,
            final String fq, final String field) {
        String fieldQuery = fq;
        CommonUtils commonUtils = new CommonUtils(context);
        SolrQueryUtil squ = new SolrQueryUtil();
        SolrQueryBuilder sqb = new SolrQueryBuilder(context);
        String city = context.getParameterString(
                "city", "");
        String date = context.getParameterString(
                "date", "");
        if (StringUtils.isNotBlank(curatedContent)) {
            Document curatedDoc = commonUtils
                    .readDCR(curatedContent);
            String curatedXpath = context
                    .getParameterString(
                    "curatedXpath",
                    "/Root/CuratedContent/ContentType");
            List<Node> categoryNodes =
                    curatedDoc.selectNodes(
                            curatedXpath);
            for (Node eleNode : categoryNodes) {
                fieldQuery = field;
                sqb.addFieldQuery(
                        getCuratedQuery(
                    fieldQuery, eleNode, city, date));

                String fields = context.getParameterString(
                        "fields", "");
                logger.debug("fields : " + fields);
            if (StringUtils.isNotBlank(fields)) {
                logger.debug("fieldQuery : " + fieldQuery);
                sqb.addFields(fields);
            }
                String query = sqb.build();
                logger.debug("Landing Query : " + query);
                Document curDoc = squ.doJsonQuery(
                        query, "SolrResponse");
                curDoc.getRootElement().add(eleNode);
                document.getRootElement().add(
                        curDoc.getRootElement());
            }
        }
        return document;
    }

}
