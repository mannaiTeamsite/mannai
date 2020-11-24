package com.hukoomi.livesite.external;

import com.hukoomi.livesite.solr.SolrQueryBuilder;
import com.hukoomi.utils.CommonUtils;
import com.hukoomi.utils.SolrQueryUtil;
import com.interwoven.livesite.runtime.RequestContext;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.DocumentHelper;
import org.dom4j.Document;
import org.dom4j.Element;
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
        String curatedContent = context
                .getParameterString("curated-content", "");
        logger.debug("curated content : " + curatedContent);
        return getCuratedContent(context,
                document, curatedContent, fieldQuery);
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
                .getText().equals("yes") && StringUtils.isNotBlank(city)) {
            fieldQuery += " AND " + city;
        }
        if (node.selectSingleNode("is-date-filter-required")
                .getText().equals("yes") && StringUtils.isNotBlank(date)) {
            fieldQuery += " AND " + date;
        }
        return fieldQuery;
    }

    /** This method will be called from Component
     * External for solr Content fetching.
     * @param context The parameter context object passed from Component.
     * @param documentPar The final content document to be returned.
     * @param curatedContent curated DCR path passed as param to java.
     * @param fq solr field query.
     *
     * @return document return the solr response document
     * generated from solr query.
     */
    public Document getCuratedContent(
            final RequestContext context,
            final Document documentPar, final String curatedContent,
            final String fq) {
        Document document = DocumentHelper.createDocument();
        Element docRoot = document.addElement("SolrResponse");
        CommonUtils commonUtils = new CommonUtils(context);
        Document curDoc = null;
        Element root = null;
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
                sqb.addFieldQuery(
                        getCuratedQuery(
                                fq, eleNode, city, date));

                String fields = context.getParameterString(
                        "fields", "");
                logger.debug("fields : " + fields);
                if (StringUtils.isNotBlank(fields)) {
                    logger.debug("fields : " + fields);
                    sqb.addFields(fields);
                }
                String query = sqb.build();
                logger.debug("Landing Query : " + query);
                curDoc = squ.doJsonQuery(
                        query, "CuratedContent");
                root = curDoc.getRootElement();
                if (null != curDoc && null != eleNode
                        && null != root) {
                    logger.debug("curDoc : " + curDoc);
                    logger.debug("Content Type : "
                            + eleNode.selectSingleNode("ContentType")
                            .getText());

                    root.addElement("content-type")
                            .addText(
                                    eleNode.selectSingleNode("ContentType")
                                            .getText());
                    root.addElement("title")
                            .addText(
                                    eleNode.selectSingleNode("title")
                                            .getText());
                    root.addElement("image")
                            .addText(
                                    eleNode.selectSingleNode("image")
                                            .getText());
                    docRoot.add(root);
                }
            }
        }
        return document;
    }
}
    