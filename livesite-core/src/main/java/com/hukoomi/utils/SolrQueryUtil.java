package com.hukoomi.utils;

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
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;

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
            if(query.contains("&hl=") || query.contains("&hl.fl=title&hl.simple.post=") || query.contains("&hl.simple.pre=")) {
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

                //Writing Temporary XML File
                FileWriter writer = new FileWriter("/usr/opentext/TeamSite/tmp/SearchText.xml");
                BufferedWriter bufferedWriter = new BufferedWriter(writer);
                bufferedWriter.write(tempReturnXML);
                bufferedWriter.close();
                writer.close();

                File xmlFile = new File("/usr/opentext/TeamSite/tmp/SearchText.xml");
                DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

                org.w3c.dom.Document doc = docBuilder.parse(xmlFile);
                NodeList titleL = doc.getElementsByTagName("title");
                logger.info("Title tags total Length: " + titleL.getLength());
                Node highlightTag = doc.getElementsByTagName("highlighting").item(0);
                if (highlightTag.hasChildNodes()) {
                    NodeList nodeL = highlightTag.getChildNodes();
                    for (int i = 0; i < nodeL.getLength(); i++) {
                        logger.info("highlight Tag Child" + i + " :" + nodeL.item(i));
                        Node titleNode = titleL.item(i);
                        for (int j = 0; j < nodeL.getLength(); j++) {
                            Node hightitleNode = nodeL.item(j);
                            logger.info("hightitle Node Name: "+hightitleNode.getNodeName());
                            Node subTitleNode = hightitleNode.getFirstChild();
                            logger.info("subTitle Node: "+subTitleNode);
                            if (hightitleNode.getNodeName().equals("hightitle"+(i+1))) {
                                if(!hightitleNode.getTextContent().isBlank()) {
                                    logger.info("title values" + i + " :" + titleNode.getTextContent() + "---" + subTitleNode.getTextContent());
                                    titleNode.setTextContent(subTitleNode.getTextContent());
                                    logger.info("Main Title Node Value after replacement: " + titleNode.getTextContent());
                                }
                                logger.info("Breaking For hightitle number: "+j);
                                break;
                            }
                        }
                    }
                    //Remove highlighting tags from XML
                    Node docFirstChild = doc.getFirstChild();
                    NodeList rootChildList = docFirstChild.getChildNodes();
                    for (int temp = 0; temp < rootChildList.getLength(); temp++) {
                        Node nodeRootChild = rootChildList.item(temp);
                        if (nodeRootChild.getNodeName().equals("highlighting")) {
                            docFirstChild.removeChild(nodeRootChild);
                            break;
                        }
                    }
                }
                //Converting Document object into String
                DOMSource domSource = new DOMSource(doc);
                StringWriter stringWriter = new StringWriter();
                StreamResult result = new StreamResult(stringWriter);
                TransformerFactory tf = TransformerFactory.newInstance();
                Transformer transformer = tf.newTransformer();
                transformer.transform(domSource, result);
                returnXML = stringWriter.toString();
                logger.info("stringWriter Result String: " + returnXML);

                //Empty Temporary XML File
                PrintWriter printWriter = new PrintWriter("/usr/opentext/TeamSite/tmp/SearchText.xml");
                printWriter.print("");
                printWriter.close();
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
