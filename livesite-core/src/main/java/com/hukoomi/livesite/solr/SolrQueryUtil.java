package com.hukoomi.livesite.solr;

import com.interwoven.livesite.dom4j.Dom4jUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class SolrQueryUtil {
    /** Logger object to check the flow of the code.*/
    private final Logger logger = Logger.getLogger(SolrQueryUtil.class);
    /** Set contant variable with default solr error message.*/
    public static final String SOLR_EXCEPTION = "SOLR Query Exception";
    /**
     * Query Solr based on solution result target jsonObjectName
     * in specified xmlRootName.
     * @param query Solr Query.
     * @param jsonObjectName Fetched JSON Object to be returned.
     * @param xmlRootName XML Root Element ofr returned document.
     *
     * @return document solr query result document.
     */
    public Document doJsonQuery(
            final String query, final String jsonObjectName,
            final String xmlRootName) {
        Document document = DocumentHelper.createDocument();
        logger.debug("SOLR Execute Query:" + query);
        try {
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate
                    .getForEntity(query, String.class);
            JSONObject jsonResponse = new JSONObject(response.getBody());
            JSONArray jsonArray = new JSONArray();
            jsonArray.put(jsonResponse.get(jsonObjectName));
            JSONObject returnObject = new JSONObject();
            returnObject.put(xmlRootName, jsonArray);
            String returnXML = XML.toString(returnObject);
            logger.debug("SOLR Query Result XML:" + returnXML);
            document = Dom4jUtils.newDocument(returnXML);
        } catch (Exception e) {
            logger.error(SOLR_EXCEPTION, e);
        }
        return document;
    }
    /**
     * Query Solr based on solution result target in specified xmlRootName.
     * @param query Solr Query.
     * @param xmlRootName root node name for Solr Query results document.
     *
     * @return document solr query result document.
     */
    public Document doJsonQuery(final String query, final String xmlRootName) {
        Document document = DocumentHelper.createDocument();
        logger.debug("SOLR Execute Query:" + query);
        try {
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate
                    .getForEntity(query, String.class);
            JSONObject returnObject = new JSONObject();
            returnObject.put(xmlRootName, new JSONObject(response.getBody()));
            String returnXML = XML.toString(returnObject);
            document = Dom4jUtils.newDocument(returnXML);
        } catch (Exception e) {
            logger.error(SOLR_EXCEPTION, e);
        }
        return document;
    }
    /**
     * Query Solr based on solution result target jsonObjectName
     * in specified xmlRootName.
     * @param query Solr Query.
     *
     * @return responseBody return solr response as String.
     */
     public String doJsonQuery(final String query) {
         String responseBody = StringUtils.EMPTY;
         try {
             RestTemplate restTemplate = new RestTemplate();
             ResponseEntity<String> response = restTemplate
                     .getForEntity(query, String.class);
             responseBody = response.getBody();
         } catch (Exception e) {
             logger.error(SOLR_EXCEPTION, e);
         }
         return responseBody;
     }
}
