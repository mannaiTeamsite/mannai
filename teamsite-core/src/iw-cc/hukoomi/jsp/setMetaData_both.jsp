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
		
		String priDCRPath	= request.getParameter("priDCRName");
		String secDCRPath		= request.getParameter("secDCRName");
		String priDCRExistStatus	= request.getParameter("priDCRExistStatus");
		String secDCRExistStatus	= request.getParameter("secDCRExistStatus");
		logger.error("priDCRPath[" + priDCRPath + "] secDCRPath[" + secDCRPath + "] priDCRExistStatus[" + priDCRExistStatus + "] secDCRExistStatus[" + secDCRExistStatus + "]");
		
		CSVPath priDCRVPath		= new CSVPath(priDCRPath);
		CSSimpleFile priDCR	= (CSSimpleFile) client.getFile(priDCRVPath);	
		
		CSVPath secDCRVPath		= new CSVPath(secDCRPath);
		CSSimpleFile secDCR	= (CSSimpleFile) client.getFile(secDCRVPath);		
		
		CSExtendedAttribute[] masterDCRExt = { new CSExtendedAttribute("TeamSite/Metadata/masterDCR", request.getParameter("masterDCRName")) };
		CSExtendedAttribute[] localDCRExt = { new CSExtendedAttribute("TeamSite/Metadata/localDCR", request.getParameter("localDCRName")) };
		CSExtendedAttribute[] isLocalised = { new CSExtendedAttribute("TeamSite/Metadata/isLocalised", request.getParameter("isLocalised")) };
		CSExtendedAttribute[] priDCRMasterStatus = { new CSExtendedAttribute("TeamSite/Metadata/isMaster", request.getParameter("priDCRMasterStatus")) };
		CSExtendedAttribute[] secDCRMasterStatus = { new CSExtendedAttribute("TeamSite/Metadata/isMaster", request.getParameter("secDCRMasterStatus")) };
		CSExtendedAttribute[] priDCRContentEntryStatus = { new CSExtendedAttribute("TeamSite/Metadata/contentEntryStatus", request.getParameter("priDCRContentEntryStatus")) };
		CSExtendedAttribute[] secDCRContentEntryStatus = { new CSExtendedAttribute("TeamSite/Metadata/contentEntryStatus", request.getParameter("secDCRContentEntryStatus")) };
		CSExtendedAttribute[] priCreatedOn = { new CSExtendedAttribute("TeamSite/Metadata/createdOn", request.getParameter("priCreatedOn")) };
		CSExtendedAttribute[] priModifiedOn = { new CSExtendedAttribute("TeamSite/Metadata/modifiedOn", request.getParameter("priModifiedOn"))};
		CSExtendedAttribute[] secCreatedOn = { new CSExtendedAttribute("TeamSite/Metadata/createdOn", request.getParameter("secCreatedOn")) };
		CSExtendedAttribute[] secModifiedOn = { new CSExtendedAttribute("TeamSite/Metadata/modifiedOn", request.getParameter("secModifiedOn"))};
		
		logger.error("EXT ATT - 1");
		
		priDCR.setExtendedAttributes(masterDCRExt);
		logger.error("EXT ATT - 1.1");
		priDCR.setExtendedAttributes(localDCRExt);
		logger.error("EXT ATT - 1.2");
		priDCR.setExtendedAttributes(isLocalised);
		logger.error("EXT ATT - 1.3");
		priDCR.setExtendedAttributes(priDCRMasterStatus);
		logger.error("EXT ATT - 1.4");
		priDCR.setExtendedAttributes(priDCRContentEntryStatus);
		logger.error("EXT ATT - 1.5");
		
		logger.error("EXT ATT - 2");
		
		if ("false".equals(priDCRExistStatus)) {
			priDCR.setExtendedAttributes(priCreatedOn);
		} else {			
			priDCR.setExtendedAttributes(priModifiedOn);
		}
		
		logger.error("EXT ATT - 3");
		
		secDCR.setExtendedAttributes(masterDCRExt);
		secDCR.setExtendedAttributes(localDCRExt);
		secDCR.setExtendedAttributes(isLocalised);
		secDCR.setExtendedAttributes(secDCRMasterStatus);
		secDCR.setExtendedAttributes(secDCRContentEntryStatus);
		
		logger.error("EXT ATT - 4");
		
		if ("false".equals(secDCRExistStatus)) {
			secDCR.setExtendedAttributes(secCreatedOn);
		} else {
			secDCR.setExtendedAttributes(secModifiedOn);
		}
		
		logger.error("EXT ATT - 5");
		
		logger.error("----End of Metadata---- ");
	} catch (Exception e) {
		logger.error("Exception occured while settin the Extended Attributes", e);
	}
%>