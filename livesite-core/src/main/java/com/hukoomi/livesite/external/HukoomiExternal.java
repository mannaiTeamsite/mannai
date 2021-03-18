package com.hukoomi.livesite.external;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashMap;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;

import com.hukoomi.livesite.solr.SolrQueryBuilder;
import com.hukoomi.utils.CommonUtils;
import com.hukoomi.utils.SolrQueryUtil;
import com.interwoven.livesite.runtime.RequestContext;

public class HukoomiExternal {
	/** Logger object to check the flow of the code. */
	private final Logger logger = Logger.getLogger(HukoomiExternal.class);
	/** Default query to fetch all solr content. */
	public static final String DEFAULT_QUERY = "*:*";
	
	/**
	 * This method will be called from Component External for solr Content fetching.
	 * 
	 * @param context The parameter context object passed from Component.
	 *
	 * @return doc return the solr response document generated from solr query.
	 */
	public Document getLandingContent(RequestContext context) {
		SolrQueryUtil squ = new SolrQueryUtil();
		SolrQueryBuilder sqb = new SolrQueryBuilder(context);
		CommonUtils commonUtils = new CommonUtils();
		String fieldQuery = "";
		String fq = commonUtils.sanitizeSolrQuery(context.getParameterString("fieldQuery", ""));
		try {
			fieldQuery = URLDecoder.decode(fq, "UTF-8");
			logger.debug("fieldQuery Query : " + fieldQuery);
		} catch (UnsupportedEncodingException e) {
			logger.warn("Unable to decode fieldQuery=" + fq, e);
		}
		String category = commonUtils.sanitizeSolrQuery(context.getParameterString("solrcategory", ""));
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

		String fields = commonUtils.sanitizeSolrQuery(context.getParameterString("fields", ""));
		logger.debug("fields : " + fields);
		if (StringUtils.isNotBlank(fields)) {
			logger.debug("fieldQuery : " + fieldQuery);
			sqb.addFields(fields);
		}
		String query = sqb.build();
		logger.debug("Landing Query : " + query);
		Document doc = squ.doJsonQuery(query, "SolrResponse");
		Element root = doc.getRootElement();
		if (root != null && root.isRootElement()) {
			root.addElement("category").addText(category);
		}
		logger.debug("Before calling : " + doc);
		
	
		HttpServletRequest request = context.getRequest();		
		HttpSession session = request.getSession(false);	
		logger.info("Session:" + session);
		logger.info("Status : "+request.getSession().getAttribute("status"));
		String status = (String) request.getSession().getAttribute("status");
				if(status != "valid" || status == null || status =="") {
					 String accessToken = null;
						Cookie cookie = null;
						Cookie[] cookies = null;
						cookies = request.getCookies();
						if (cookies != null) {
							for (int i = 0; i < cookies.length; i++) {
								cookie = cookies[i];
								if (cookie.getName().equals("accessToken")) {
									accessToken = cookie.getValue();
								}
							}						
						if(accessToken != null) {
						logger.info("--------Dashboard External is called--------");	
						DashboardExternal dash = new DashboardExternal(context);
						dash.dashboardServices(context , accessToken);
						
						if(root != null && root.isRootElement()) {
						
						Element userData = root.addElement("userData");	
						Enumeration<String> attributes = request.getSession().getAttributeNames();
						while (attributes.hasMoreElements()) {						
						    String attribute =  attributes.nextElement();
						    logger.info(attribute+" = "+request.getSession().getAttribute(attribute));
							//HashMap<String,String> docData =  (HashMap<String, String>) request.getSession().getAttribute(attribute);
						    
						    Element docElement = userData.addElement(attribute);	
						    if(request.getSession().getAttribute(attribute) != null) {
								Object attrValue = request.getSession().getAttribute(attribute);
								logger.info(attribute+" : "+attrValue);																
								docElement.setText(attrValue.toString());
							}
							
						    
							}
						}
						}
						}
					}else if(request.getSession().getAttribute("status") == "valid") {
					if(root != null && root.isRootElement()) {
					Element userData = root.addElement("userData");	
					Enumeration<String> attributes = request.getSession().getAttributeNames();
					while (attributes.hasMoreElements()) {
					    String attribute = attributes.nextElement();
					   
					    logger.info(attribute+" : "+request.getSession().getAttribute(attribute));
						HashMap<String,String> docData = (HashMap<String, String>) request.getSession().getAttribute(attribute);
						logger.info(attribute+" : "+docData);
						 Element docElement = userData.addElement(attribute);
						 if (!docData.equals("")) {		
							    docElement.setText(docData.get("fnEn"));
							    }
						}
					}
				}		
		
				logger.info("Document"+doc.asXML());	
		
		return doc;
		
	}
	


}
