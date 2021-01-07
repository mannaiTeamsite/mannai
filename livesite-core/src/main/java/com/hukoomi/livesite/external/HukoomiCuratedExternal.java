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
    /** category solr field name. */
    private String category = "";
    /** cateory value dcr field name. */
    private String xnode = "";

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
        xnode = context.getParameterString(
                "curatedNode", "ContentType");
        category = context.getParameterString(
                "solrField", "category");
        try {
            fieldQuery = URLDecoder.decode(
                    fq, "UTF-8");
            logger.debug("fieldQuery Query : " + fieldQuery);
        } catch (UnsupportedEncodingException e) {
            logger.warn("Unable to decode fieldQuery="
                    + fq, e);
        }
        String curatedContentPath = context
                .getParameterString("curated-content-path",
                "templatedata/Home-Page/Curated-Content/data");
        String curatedContentDCR = context
                .getParameterString("curated-content-dcr",
                "curated-content");
        String curatedContent = curatedContentPath
                + "/" + curatedContentDCR;
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
        String and = " AND ";
        String curatedCategory = node
                .selectSingleNode(xnode).getText();
        if (StringUtils.isNotBlank(fieldQuery)) {
            fieldQuery += and + category + ":" + curatedCategory;
        } else {
            fieldQuery = category + ":" + curatedCategory;
        }
        if (node.selectSingleNode("is-location-filter-required") != null
        && node.selectSingleNode("is-location-filter-required")
        .getText().equals("yes") && StringUtils.isNotBlank(city)) {
                fieldQuery += and + city;
        }
        if (node.selectSingleNode("is-date-filter-required") != null
        && node.selectSingleNode("is-date-filter-required")
        .getText().equals("yes") && StringUtils.isNotBlank(date)) {
                fieldQuery += and + date;
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
                logger.debug("curDoc : " + curDoc);
                logger.debug("Content Type : "
            + eleNode.selectSingleNode(xnode).getText());
                addElementDetails(root, eleNode);
                docRoot.add(root);

            }
        }
        return document;
    }
    /** This method will set the curated
     * element details to the result doc.
     * @param root curated doc root element.
     * @param eleNode curated node in the curated
     * DCR.
     */
    public void addElementDetails(
        final Element root, final Node eleNode) {
        String image = "image";
        String title = "title";
        String detail = "detailSearch";
        root.addElement("content-type")
                .addText(
                        eleNode.selectSingleNode(xnode)
                                .getText());
        if (eleNode.selectSingleNode(title) != null) {
            root.addElement(title)
    .addText(eleNode.selectSingleNode(title).getText());
        }
        if (eleNode.selectSingleNode(image)
                != null) {
            root.addElement(image)
    .addText(eleNode.selectSingleNode(image).getText());
        }
        if (eleNode.selectSingleNode(detail)
                != null) {
            root.addElement(detail)
                    .addText(eleNode.selectSingleNode(detail).getText());
        }

    }
}
