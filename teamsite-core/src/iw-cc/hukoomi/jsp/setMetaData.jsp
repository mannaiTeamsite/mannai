<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@ page import="com.interwoven.cssdk.common.CSClient"%>
<%@ page import="com.interwoven.cssdk.filesys.CSFile"%>
<%@ page import="com.interwoven.cssdk.filesys.CSSimpleFile"%>
<%@ page import="com.interwoven.cssdk.filesys.CSVPath"%>
<%@ page import="com.interwoven.cssdk.filesys.CSExtendedAttribute"%>
<%@ page import="java.util.Map"%>
<%@ page import="java.util.List"%>
<%@ page import="java.util.Arrays"%>
<%@page import="org.apache.log4j.Level"%>
<%@page import="org.apache.log4j.Logger"%>
<%@ taglib uri="/WEB-INF/tlds/context.tld" prefix="context"%>
<context:use var="user_ctx" createIfNeeded="true" contextClass="com.interwoven.ui.teamsite.auth.CSClientContext" />
<%!Logger logger = Logger.getLogger(getClass());%>
<%
	try {
		logger.error("----Inside setMetadata.jsp----");
		CSClient client = user_ctx.getCSClient();
		
		String dcrpath = request.getParameter("path");		
		String master = dcrpath; String nonmaster = ""; 
		String locale = ""; String sublocale = "";
		
		if(dcrpath.contains("data/en")){
            nonmaster = dcrpath.replaceAll("data/en","data/ar");
			locale = "en";
			sublocale = "ar";
        }else if(dcrpath.contains("data/ar")){
            nonmaster = dcrpath.replaceAll("data/ar","data/en");
			locale = "ar";
			sublocale = "en";
        }
		
		logger.error("DCR PATH [" + dcrpath + "] LOCAL [" + locale + "]");
		CSVPath path1 = new CSVPath(master);
		CSSimpleFile csFile1 = (CSSimpleFile) client.getFile(path1);
		CSVPath path2 = new CSVPath(nonmaster);
		CSSimpleFile csFile2 = (CSSimpleFile) client.getFile(path2);
		
		CSExtendedAttribute[] sourceEA3 = { new CSExtendedAttribute("TeamSite/Metadata/lang", locale) };
		csFile1.setExtendedAttributes(sourceEA3);
		CSExtendedAttribute[] sourceEA4 = { new CSExtendedAttribute("TeamSite/Metadata/lang", sublocale) };
		csFile2.setExtendedAttributes(sourceEA4);
		Map<String, String[]> params = request.getParameterMap();
		for (String key : params.keySet()) {		
			String value[] = params.get(key);
			String metadata_name = "TeamSite/Metadata/" + key;
			logger.error("KEY [" + key + "], value length" + value.length);
			if (logger.isInfoEnabled()) {
				logger.error(metadata_name + " ==> " + Arrays.toString(value));
			}			
			for (int i = 0; i < value.length; i++) {
				if (csFile1 != null && csFile2 != null) {
					CSExtendedAttribute[] sourceEA1 = { new CSExtendedAttribute(metadata_name, value[i]) };
					csFile1.setExtendedAttributes(sourceEA1);
					if ("isMaster".equals(key)) {
						if("True".equals(value[i])) {
							value[i] = "False";
						} else {
							value[i] = "True";
						}
					}
					if ("contentEntryStatus".equals(key)) {
						value[i] = "Not Started";
					}
					CSExtendedAttribute[] sourceEA2 = { new CSExtendedAttribute(metadata_name, value[i]) };
					csFile2.setExtendedAttributes(sourceEA2);					
					logger.error("[NOT NULL]");
				} else {
					logger.error("[NULL]");
				}
				logger.error("KEY [" + key + "], value[" + i + "] :" + value[i]);
			}			
		}
		logger.error("----End of Metadata---- ");
	} catch (Exception e) {
		logger.error("Exception occured while settin the Extended Attributes", e);
	}
%>