package com.hukoomi.utils;

import com.interwoven.livesite.dom4j.Dom4jUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import java.util.List;

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

            String returnXML = "";
            if(query.contains("&hl=")) {
                logger.info("Json Response: " + response.getBody());
                String jsonResponse = response.getBody().replaceAll("\"\\d+\":", "\"higTitle\":");
                logger.info("Json After Id Tag replacement: " + jsonResponse);
                int inc = 0;
                while (jsonResponse.contains("higTitle")) {
                    jsonResponse = jsonResponse.replaceFirst("higTitle","hightitle"+ ++inc);
                }
                logger.info("Json Response After common tag replacement: " + jsonResponse);
                returnObject.put(xmlRootName, new JSONObject(jsonResponse));
                String tempReturnXML = XML.toString(returnObject);
                logger.info("XML data after conversion: " + tempReturnXML);
            }else{
                returnObject.put(xmlRootName, new JSONObject(response.getBody()));
                returnXML = XML.toString(returnObject);
            }
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
    public Document doXMLQuery(final String query, final String xmlRootName) {
        Document document = DocumentHelper.createDocument();
        logger.debug("SOLR Execute Query:" + query);
        if(!query.contains("wt=xml")){
            logger.info("Query is not intended for XML output. Use JSON output instead or add wt=xml in the query");
            return document;
        }
        try {
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.getForEntity(query, String.class);
            String solrResponse = response.getBody();
            logger.debug("XML Response from Solr: " + solrResponse);
            if(StringUtils.isNotBlank(solrResponse)){
                Document solrDocument = DocumentHelper.parseText(solrResponse);
                document.add(formatSolrDocument(solrDocument,xmlRootName).getRootElement().detach());
            }
        } catch (Exception e) {
            logger.error(SOLR_EXCEPTION, e);
        }
        return document;
    }

    public Document formatSolrDocument(Document solrDocument, String xmlRootName){
        logger.info("Formating Document");
        Document document = DocumentHelper.createDocument();
        Element root = document.addElement(xmlRootName);
        Element response = root.addElement("response");
        Element solrDocumentRootElement = solrDocument.getRootElement();
        Node solrResult = solrDocumentRootElement.selectSingleNode("result");
        Element solrResultElement = (Element) solrResult;
        List<Attribute> resultAttributes = solrResultElement.attributes();
        for(Attribute attribute : resultAttributes){
            response.addElement(attribute.getName()).addText(attribute.getText());
        }
        List<Node> docs = solrResult.selectNodes("doc");
        List<Node> listingNodes = solrDocumentRootElement.selectNodes("lst");

        Boolean isHighlightResponse = false;
        if(!listingNodes.isEmpty()){
            for(Node listing: listingNodes){
                Element listingElement = (Element) listing;
                if(listingElement.attributeValue("name").equals("highlighting")){
                    isHighlightResponse = true;
                }
                formatElement(root.addElement(listingElement.attributeValue("name")),listingElement);
            }
        }

        if(!docs.isEmpty()) {
           for (Node doc : docs) {
               Element currentDoc = response.addElement("docs");
               String documentId = doc.selectSingleNode("str[@name='id']").getText();
               logger.debug("Document ID: " + documentId);
               List<Node> arrays = doc.selectNodes("arr");
                if(!arrays.isEmpty()){
                    for ( Node array : arrays ){
                        Element arrayElement = (Element) array;
                        if(isHighlightResponse){
                            List<Node> correspondingHighlightedValueNodes = solrDocument.selectNodes("//lst[@name='highlighting']/lst[@name='" + documentId + "']/arr[@name='" + arrayElement.attributeValue("name") + "']");
                            if(!correspondingHighlightedValueNodes.isEmpty()) {
                                currentDoc.addElement(arrayElement.attributeValue("name")).addText(correspondingHighlightedValueNodes.get(0).selectSingleNode("str").getText());
                            } else {
                                currentDoc.addElement(arrayElement.attributeValue("name")).addText(arrayElement.element("str").getText());
                            }
                        } else {
                            currentDoc.addElement(arrayElement.attributeValue("name")).addText(arrayElement.element("str").getText());
                        }
                    }
                }
               List<Node> strings = doc.selectNodes("str");
               if(!strings.isEmpty()){
                   for ( Node string : strings ){
                       Element stringElement = (Element) string;
                       currentDoc.addElement(stringElement.attributeValue("name")).addText(stringElement.getText());
                   }
               }
               List<Node> dates = doc.selectNodes("date");
               if(!dates.isEmpty()){
                   for ( Node date : dates ){
                       Element dateElement = (Element) date;
                       currentDoc.addElement(dateElement.attributeValue("name")).addText(dateElement.getText());
                   }
               }
               List<Node> longs = doc.selectNodes("long");
               if(!dates.isEmpty()){
                   for ( Node longItems : longs ){
                       Element longElement = (Element) longItems;
                       currentDoc.addElement(longElement.attributeValue("name")).addText(longElement.getText());
                   }
               }
               List<Node> ints = doc.selectNodes("int");
               if(!dates.isEmpty()){
                   for ( Node integer : ints ){
                       Element intElement = (Element) integer;
                       currentDoc.addElement(intElement.attributeValue("name")).addText(intElement.getText());
                   }
               }
            }
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

     public Element formatElement(Element elementToAdd, Element solrElement){
         for(Element currentElement : solrElement.elements()){
             String currentElementName = currentElement.attributeValue("name");
             if(currentElementName.matches("[0-9]+")){
                 logger.debug("Element with Numeric only in name. Prepending with doc");
                 currentElementName = "doc-"+currentElementName;
             }
             if(currentElement.isTextOnly()) {
                 elementToAdd.addElement(currentElementName).addText(currentElement.getText());
             } else if (currentElement.getName().equals("arr")) {
                 elementToAdd.addElement(currentElementName).addText(currentElement.selectSingleNode("str").getText());
             } else {
                 formatElement(elementToAdd.addElement(currentElementName),currentElement);
             }
         }
         return elementToAdd;
     }
}
