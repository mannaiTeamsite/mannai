package com.hukoomi.livesite.external;

import com.hukoomi.livesite.solr.SolrQueryBuilder;
import com.hukoomi.utils.CommonUtils;
import com.hukoomi.utils.SolrQueryUtil;
import com.hukoomi.utils.UserInfoSession;
import com.interwoven.livesite.common.web.CookieUtils;
import com.interwoven.livesite.runtime.RequestContext;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.owasp.esapi.ESAPI;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class HukoomiExternal {
	/** Logger object to check the flow of the code. */
	private final Logger logger = Logger.getLogger(HukoomiExternal.class);
	/** Default query to fetch all solr content. */
	public static final String DEFAULT_QUERY = "*:*";
	/** Constant for cookie name. */
	private static final String DEFAULT_COOKIE = "persona";
	private static final String FIELDQUERY_CONSTANT = "fieldQuery";
	private static final String BASEQUERY_CONSTANT = "baseQuery";
	private static final String CORRECTWORD_CONSTANT = "CorrectedWord";

	/**
	 * This method will be called from Component External for solr Content fetching.
	 * 
	 * @param context The parameter context object passed from Component.
	 *
	 * @return doc return the solr response document generated from solr query.
	 */
	 @SuppressWarnings("deprecation")
	public Document getLandingContent(RequestContext context) {
		SolrQueryUtil squ = new SolrQueryUtil();
		SolrQueryBuilder sqb = new SolrQueryBuilder(context);
		CommonUtils commonUtils = new CommonUtils();
		String fieldQuery = "";
		String fq = commonUtils.sanitizeSolrQuery(context.getParameterString(FIELDQUERY_CONSTANT, ""));
		context.setParameterString(FIELDQUERY_CONSTANT, fq);
		try {
			fieldQuery = URLDecoder.decode(fq, "UTF-8");
			logger.debug("fieldQuery Query : " + fieldQuery);
		} catch (UnsupportedEncodingException e) {
			logger.warn("Unable to decode fieldQuery=" + fq, e);
		}
		String category = commonUtils.sanitizeSolrQuery(context.getParameterString("solrcategory", ""));
		context.setParameterString("solrcategory", category);
		logger.debug("category : " + category);
				
		sqb = landingContentQuery(context, sqb, fieldQuery, category);
		
		String query = sqb.build();
		logger.debug("Landing Query : " + query);
		Document doc;
		doc = squ.doJsonQuery(query, "SolrResponse", true);
		Element root = doc.getRootElement();
		logger.info("Context FieldQuery: " + context.getParameterString(FIELDQUERY_CONSTANT));
		logger.info("Context BaseQuery: " + context.getParameterString(BASEQUERY_CONSTANT));
		logger.info("Current FieldQuery: " + fq);
		logger.info("Current Category: " + category);
		String correctedWord = context.getParameterString(BASEQUERY_CONSTANT);
		if(root != null && root.element(CORRECTWORD_CONSTANT) != null && StringUtils.isNotBlank(root.element(CORRECTWORD_CONSTANT).getText())){
			logger.info("Word has been corrected in Portal Core");
			correctedWord = root.element(CORRECTWORD_CONSTANT).getText();
			logger.debug("Corrected word: " + correctedWord);
			context.setParameterString(BASEQUERY_CONSTANT, correctedWord);
		}
		Document nutchDoc = null;

		String lang = commonUtils.sanitizeSolrQuery(context
				.getParameterString("lang", ""));
		logger.debug("lang : " + lang);
		if((category.equalsIgnoreCase("All") || category.equalsIgnoreCase("*") || category.equalsIgnoreCase("")) && StringUtils.isBlank(lang)){
			String nutchQuery = sqb.crawlBuild(correctedWord);
			logger.debug("Crawl Query : " + nutchQuery);
			nutchDoc = squ.doJsonQuery(nutchQuery, "NutchResponse", false);
		}

		if (root != null && root.isRootElement()) {
			root.addElement("category").addText(category);
			String baseQuery = commonUtils.sanitizeSolrQuery(context.getParameterString(BASEQUERY_CONSTANT));
			String arabicRegEx = "^[a-zA-Z-_0-9.@\\/\\u0621-\\u064A\\u0660-\\u0669 ]+$";
			if(!baseQuery.matches(arabicRegEx)){
				baseQuery = ESAPI.encoder().encodeForHTML(baseQuery);
			}
			String sanitizedFieldQuery = commonUtils.sanitizeSolrQuery(fq);
			if(!sanitizedFieldQuery.matches(arabicRegEx)){
				sanitizedFieldQuery = ESAPI.encoder().encodeForHTML(sanitizedFieldQuery);
			}
			logger.info("Sanitized BaseQuery: " + baseQuery);
			logger.info("Sanitized FieldQuery: " + sanitizedFieldQuery);
			root.addElement(BASEQUERY_CONSTANT).addText(baseQuery);
			root.addElement(FIELDQUERY_CONSTANT).addText(sanitizedFieldQuery);
			if (nutchDoc != null && nutchDoc.getRootElement() != null) {
					root.add(nutchDoc.getRootElement());
			}

		}
		
		
		logger.debug("Document : " + doc.asXML());
		
		UserInfoSession inf = new UserInfoSession();		
		doc = inf.getUserData(context, doc);
		return doc;

	}
	 @SuppressWarnings("deprecation")
	 private SolrQueryBuilder landingContentQuery(RequestContext context, SolrQueryBuilder sqb, String fieldQuery, String category) {
		 
		 CommonUtils commonUtils = new CommonUtils();
		 if (StringUtils.isNotBlank(category)) {
				if (StringUtils.isNotBlank(fieldQuery)) {
					fieldQuery += " AND category:" + category;
				} else {
					fieldQuery = "category:" + category;
				}
				logger.debug(FIELDQUERY_CONSTANT+": " + fieldQuery);
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
		 return sqb;
	 }

}
