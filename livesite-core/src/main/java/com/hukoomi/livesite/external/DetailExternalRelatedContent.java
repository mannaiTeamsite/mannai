/**
 * External Java to fetch the dcr content passed as param.
 */
package com.hukoomi.livesite.external;

import com.hukoomi.utils.CommonUtils;
import com.interwoven.livesite.runtime.RequestContext;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Node;
import org.dom4j.DocumentHelper;
import com.hukoomi.utils.SolrQueryUtil;
import com.hukoomi.utils.PropertiesFileReader;
import org.dom4j.Element;

import java.util.List;
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
    CommonUtils commonUtils = new CommonUtils(context);
    PropertiesFileReader propertyFileReader = new PropertiesFileReader(
            context, "solrconfig.properties");
    Properties properties = propertyFileReader.getPropertiesFile();
    Document detailDocument = commonUtils.getDCRContent(context);
    if(detailDocument == null) {
        commonUtils.throwDCRNotFoundError(context, "No Content Record found");
    }
    detailDocument = commonUtils.getDCRContent(context);
    String relDCRContent = properties.getProperty("relDCRContent");
    String relDCRNode = properties.getProperty("relDCRNode");
    logger.info("relDCRContent: " + relDCRContent);
    logger.info("relDCRNode: " + relDCRNode);
    if (detailDocument.selectSingleNode(relDCRContent) != null) {
        Document detailDoc = DocumentHelper.createDocument();
        Element docRoot = detailDoc.addElement("relatedDCRContent");
        Element rootEle = null;
        Document docDcr = null;
        List<Node> nodes = detailDocument.selectNodes(relDCRContent);
        for(Node node:nodes) {
            String dcr = node.selectSingleNode(relDCRNode).getText();
            if(dcr.startsWith("/")){
                dcr = dcr.substring(1);
            }
            docDcr = commonUtils.readDCR(dcr);
			if (docDcr != null) {
				rootEle = docDcr.getRootElement();
				docRoot.add(rootEle);
			}
        }
        detailDocument.getRootElement().add(
                detailDoc.getRootElement());
    }
    if(!context.getParameterString("detail-page","true").equals("true")){
        logger.info("The Component is not present on a detail page. Skipping the Dynamic metadata values.");
        return detailDocument;
    }
    commonUtils.generateSEOMetaTagsForDynamicContent(detailDocument, context);
    return getRelatedContent(context, detailDocument, properties);
}

    /** This method will be called from Component
     * External for related solr Content fetching.
     * @param context The parameter context object passed from Component.
     * @param detailDocument The final content document to be returned.
     * @param properties solr properties file.
     *
     * @return Document return type.
     */
    public Document getRelatedContent(
            final RequestContext context,
            final Document detailDocument,
            final Properties properties) {
        CommonUtils commonUtils = new CommonUtils();
        String fieldQuery = "";
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
        final String requestHandler = context.getParameterString("requestHandler",
                properties.getProperty("requestHandler"));
        String ID = commonUtils.getValueFromXML("/content/root/information/id", detailDocument);
        String bq = "";
        if (StringUtils.isNotBlank(ID)) {
            bq = "-id:" + ID;
        } else {
            bq = DEFAULT_QUERY;
        }
        StringBuilder sb = new StringBuilder(baseUrl);
        sb.append("/" + requestHandler + "?q=" + bq);
        String category = commonUtils
                .getValueFromXML("/content/root/category",
                 detailDocument);
        if (StringUtils.isNotBlank(category)) {
            sb.append("&fq=category:" + category);
        }
        String relatedContent = context.getParameterString(
                "relatedContent", "");
        if (StringUtils.isNotBlank(relatedContent)) {
            addContent(context, sb.toString(), relatedContent, "relatedContent",
                    detailDocument, properties);
        }
        String trendContent = context.getParameterString(
                "trendContent", "");
        if (StringUtils.isNotBlank(trendContent)) {
            addContent(context, sb.toString(), trendContent, "trendingContent",
                    detailDocument, properties);
        }
        return detailDocument;
    }

    /** This method will be called from Component
     * External for related solr Content fetching.
     * @param nodeList The parameter context object passed from Component.
     * @param context The parameter context object passed from Component.
     * @param detailDocument The final content document to be returned.
     * @param fielQuery Solr field query.
     * @param root xml node name.
     * @param properties solr properties file.
     * @return no return type.
     */
    public void addContent(
            final RequestContext context,
            final String fielQuery,
            final String nodeList,
            final String root,
            final Document detailDocument,
            final Properties properties) {
        SolrQueryUtil squ = new SolrQueryUtil();
        CommonUtils commonUtils = new CommonUtils();
        String rows = context.getParameterString("rows", "9");
        String[] values = nodeList.split(",");
        String fq = fielQuery;
        String dcrValue = "";
        String sortVal = getSortContent(context, detailDocument, properties);
        for (int i=0;i<values.length;i++) {
            if (values[i].split(":")[2].equals("Node")) {
                dcrValue = commonUtils
                        .getValueFromXML(values[i].split(":")[1],
                                detailDocument);
            } else {
                dcrValue = values[i].split(":")[1];
            }
            if (values[i].split(":")[3].equals("Single")){
                dcrValue = "(" + dcrValue.replace(","," ")
                + ")";
            } else {
                dcrValue = "(*" + dcrValue.replace(",","* *")
                + "*)";
            }
            if (StringUtils.isNotBlank(dcrValue)) {
                fq = fq + " AND " + values[i].split(":")[0]
                        + ":" + dcrValue;
            }
        }
        Document relatedDoc = squ.doJsonQuery(fq + "&rows=" + rows
                        + "&sort=" + sortVal, root, false);
        detailDocument.getRootElement().add(relatedDoc.getRootElement());
    }

    /** This method will be called to set the
     * solr sort param content.
     * @param context The parameter context object passed from Component.
     * @param detailDocument The final content document to be returned.
     * @return String sort content.
     */
    public String getSortContent(
            final RequestContext context,
            final Document detailDocument,
            final Properties properties) {
        CommonUtils commonUtils = new CommonUtils();
        String locationPrefix = "distance-prefix";
        String locationSuffix = "distance-suffix";
        String sortVal = "";
        String sort = context.getParameterString(
                "relatedSort", "");
        logger.info("Content Sort: " + sort);
        if (StringUtils.isNotBlank(sort)) {
            if (sort.split(":")[1].equals("distance")) {
                logger.info("Sort Type: Distance");
                if (detailDocument.selectSingleNode(sort.split(":")[0]) != null) {
                    String location = commonUtils
                            .getValueFromXML(sort.split(":")[0],
                                    detailDocument);
                    if (StringUtils.isNotBlank(location)) {
                        sortVal = properties.getProperty(locationPrefix);
                        sortVal = sortVal + location
                                + properties.getProperty(locationSuffix);
                        logger.info("Sort Query: " + sortVal +
                                location + properties.getProperty(locationSuffix));
                    } else {
                        sortVal = properties.getProperty(locationPrefix);
                        sortVal = sortVal
                                + properties.getProperty("distance-default")
                                + properties.getProperty(locationSuffix);
                        logger.info("Sort Query: " + sortVal +
                                properties.getProperty("distance-default")
                                + properties.getProperty(locationSuffix));
                    }
                }
            } else {
                logger.info("Sort Type: Value");
                sortVal = sort.split(":")[0];
            }
        }
        return sortVal;
    }
}
