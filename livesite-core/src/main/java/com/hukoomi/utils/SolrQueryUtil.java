package com.hukoomi.utils;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.interwoven.livesite.dom4j.Dom4jUtils;

public class SolrQueryUtil {
	/**
	 * Logger object to check the flow of the code.
	 */
	private final Logger logger = Logger.getLogger(SolrQueryUtil.class);
	/**
	 * Set contant variable with default solr error message.
	 */
	public static final String SOLR_EXCEPTION = "SOLR Query Exception";

	/**
	 * Query Solr based on solution result target jsonObjectName in specified
	 * xmlRootName.
	 *
	 * @param query          Solr Query.
	 * @param jsonObjectName Fetched JSON Object to be returned.
	 * @param xmlRootName    XML Root Element ofr returned document.
	 * @return document solr query result document.
	 */
	public Document doJsonQuery(final String query, final String jsonObjectName, final String xmlRootName) {
		Document document = DocumentHelper.createDocument();
		logger.debug("SOLR Execute Query:" + query);
		try {
			RestTemplate restTemplate = new RestTemplate();
			ResponseEntity<String> response = restTemplate.getForEntity(query, String.class);
			JSONObject jsonResponse = new JSONObject(response.getBody());
			JSONArray jsonArray = new JSONArray();
			jsonArray.put(jsonResponse.get(jsonObjectName));
			JSONObject returnObject = new JSONObject();
			returnObject.put(xmlRootName, jsonArray);
			String returnXML = XML.toString(returnObject);
			logger.debug("SOLR Query Result XML:" + returnXML);
			document = Dom4jUtils.newDocument(returnXML);
		} catch (Exception e) {
			logger.error(SOLR_EXCEPTION + " For: " + query, e);

		}
		return document;
	}

	@SuppressWarnings("unchecked")
	public List<Node> getHiglightedDoc(List<Node> resultDocs, Node highlightNode) {
		for (Node resultDoc : resultDocs) {
			Node documentIDNode = resultDoc.selectSingleNode("id");
			if (documentIDNode != null) {
				String documentID = documentIDNode.getText();
				logger.trace("Changing Highlighted Values for: " + documentID);
				Node highlightedDocumentNode = highlightNode.selectSingleNode("doc-" + documentID);
				if (highlightedDocumentNode != null) {
					Element highlightedDocumentElement = (Element) highlightedDocumentNode;
					List<Element> highlightedDocuments = highlightedDocumentElement.elements();
					for (Element highlightedDocument : highlightedDocuments) {
						Element correspondingProperty = (Element) resultDoc
								.selectSingleNode(highlightedDocument.getName());
						logger.trace("From: " + correspondingProperty.getText());
						if (StringUtils.isNotBlank(highlightedDocument.getText())) {
							correspondingProperty.setText(highlightedDocument.getText());
							logger.trace("To: " + highlightedDocument.getText());
						}
					}
				}
				logger.trace("Changed Highlighted Values for: " + documentID);
			}
		}
		return resultDocs;
	}

	public Document getSolrDoc(Document document, String correctWordStr, List<Node> suggestionNodeList,
			String originalWordStr, String query, String xmlRootName) {
		if (!suggestionNodeList.isEmpty()) {
			if (suggestionNodeList.size() > 1) {
				int maxFreq = 0;
				int freqVal;
				for (Node node : suggestionNodeList) {
					freqVal = Integer.parseInt(node.selectSingleNode("freq").getText());
					if (freqVal > maxFreq) {
						maxFreq = freqVal;
						correctWordStr = node.selectSingleNode("word").getText();
					}
				}
			} else {
				correctWordStr = suggestionNodeList.get(0).selectSingleNode("word").getText();
			}
			logger.info("Corrected wordStr: " + correctWordStr);
			String newQuery = query.replaceAll(originalWordStr, correctWordStr);
			logger.info("New Query: " + newQuery);
			document = doJsonQuery(newQuery, xmlRootName, false);
			document.getRootElement().addElement("OriginalWord").addText(originalWordStr);
			document.getRootElement().addElement("CorrectedWord").addText(correctWordStr);
		}
		return document;
	}

	/**
	 * Query Solr based on solution result target in specified xmlRootName.
	 *
	 * @param query       Solr Query.
	 * @param xmlRootName root node name for Solr Query results document.
	 * @return document solr query result document.
	 */
	@SuppressWarnings("unchecked")
	public Document doJsonQuery(String query, final String xmlRootName, boolean checkSpell) {
		Document document = DocumentHelper.createDocument();
		logger.debug("SOLR Execute Query:" + query);
		if (query.contains("category:null") || !checkSpell) {
			query = query.replaceFirst("/spell", "/select");
		}
		logger.debug("SOLR Final Execute Query:" + query);
		try {
			RestTemplate restTemplate = new RestTemplate();
			ResponseEntity<String> response = restTemplate.getForEntity(query, String.class);
			JSONObject returnObject = new JSONObject();
			String returnXML = "";

			String jsonResponse = response.getBody().replaceAll("\"(\\d+)\":", "\"doc-$1\":");

			returnObject.put(xmlRootName, new JSONObject(jsonResponse));
			returnXML = XML.toString(returnObject);
			document = Dom4jUtils.newDocument(returnXML);
			List<Node> resultDocs = document.selectNodes("//docs");
			Node highlightNode = document.getRootElement().selectSingleNode("highlighting");
			if (highlightNode != null) {
				logger.info("Highlighting Text Query available in the Response");
				resultDocs = getHiglightedDoc(resultDocs, highlightNode);
			}
			if (resultDocs.isEmpty() && checkSpell) {
				logger.info("Result Docs is empty");
				logger.info("Query: " + query);
				List<Node> suggestionNodeList = document.selectNodes("//suggestion");
				String queryStr = "";
				String originalWordStr = "";
				if (query.contains("?q=title:")) {
					queryStr = query.split("\\?q=title:")[1];
				} else if (query.contains("?q=")) {
					queryStr = query.split("\\?q=")[1];
				}

				if (queryStr.contains("*&fl")) {
					originalWordStr = queryStr.substring(1, queryStr.indexOf("*&fl"));
				} else if (queryStr.contains("&fl")) {
					originalWordStr = queryStr.substring(0, queryStr.indexOf("&fl"));
				} else if (queryStr.contains("&")) {
					originalWordStr = queryStr.substring(0, queryStr.indexOf("&"));
				}
				logger.info("Original wordStr: " + originalWordStr);

				String correctWordStr = originalWordStr;

				document = getSolrDoc(document, correctWordStr, suggestionNodeList, originalWordStr, query,
						xmlRootName);

			}
		} catch (Exception e) {
			logger.error(SOLR_EXCEPTION + " For- " + query, e);
		}
		return document;
	}

	/**
	 * Query Solr based on solution result target jsonObjectName in specified
	 * xmlRootName.
	 *
	 * @param query Solr Query.
	 * @return responseBody return solr response as String.
	 */
	public String doJsonQuery(final String query) {
		String responseBody = StringUtils.EMPTY;
		try {
			RestTemplate restTemplate = new RestTemplate();
			ResponseEntity<String> response = restTemplate.getForEntity(query, String.class);
			responseBody = response.getBody();
		} catch (Exception e) {
			logger.error(SOLR_EXCEPTION + " For= " + query, e);
		}
		return responseBody;
	}
}