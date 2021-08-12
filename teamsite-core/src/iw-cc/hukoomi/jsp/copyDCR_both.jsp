<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@page import="com.interwoven.cssdk.filesys.CSHole"%>
<%@page import="java.util.Date" %>
<%@page import="java.text.SimpleDateFormat" %>
<%@page import="java.util.Date" %>
<%@page import="com.interwoven.cssdk.common.CSClient" %>
<%@page import="com.interwoven.cssdk.filesys.CSFile" %>
<%@page import="com.interwoven.cssdk.filesys.CSSimpleFile" %>
<%@page import="com.interwoven.cssdk.filesys.CSVPath" %>
<%@page import="com.interwoven.cssdk.filesys.CSExtendedAttribute" %>
<%@page import="java.text.SimpleDateFormat" %>
<%@page import="java.util.Map"%>
<%@page import="org.apache.commons.io.FilenameUtils"%>
<%@page import="java.util.Properties"%>
<%@page import="java.util.Enumeration"%>
<%@page import="com.interwoven.cssdk.common.CSClient"%>
<%@page import="com.interwoven.cssdk.common.CSVersion"%>
<%@page import="com.interwoven.cssdk.filesys.CSDir"%>
<%@page import="com.interwoven.cssdk.factory.CSFactory"%>
<%@page import=" java.util.Locale"%>
<%@page import="org.apache.log4j.Level"%>
<%@page import="org.apache.log4j.Logger"%>

<%@ taglib uri="/WEB-INF/tlds/context.tld" prefix="context"%>
<context:use var="user_ctx" createIfNeeded="true" contextClass="com.interwoven.ui.teamsite.auth.CSClientContext"/>
<%! Logger logger = Logger.getLogger(getClass()); %>
<html>
	<head></head>
	<%
		String priDCRName = "";
		Boolean priDCRExistFlag = false;
		boolean priDCRModFlag = false;
		String priDCRIsMasterValue = "";
		String priDCRContentEntryStatus = "";

		String secDCRName = "";
		Boolean secDCRExistFlag = false;
		boolean secDCRModFlag = false;
		String secDCRIsMasterValue = "";
		String secDCRContentEntryStatus = "";

		try {
			CSClient client = null;
			client = user_ctx.getCSClient();
			
			String vpath = request.getParameter("vpath");
			String dcrName = request.getParameter("dcrName");
			String locale = request.getParameter("locale");
			String priLocale = request.getParameter("priLocale");
			String secLocale = request.getParameter("secLocale");
			logger.error("VPath[" + locale + vpath + "] DCR_Name[" + dcrName + "] Primary_Locale[" + priLocale + "] Secondary_Locale[" + secLocale + "]");
			
			CSFile categoryFile = client.getFile(new CSVPath(vpath));
			CSDir dataDir = (CSDir) categoryFile;
			logger.error("VPath: " + vpath + " # Data Directory: " + dataDir.getName());
			
			
			logger.error("DCR Name: " + dcrName);
			if(dcrName.contains(".xml")){
				String[] fileArr = dcrName.split("\\.");
				dcrName = fileArr[0];
			}else{
				 logger.error("DCR name does not contain xml ");
			}
			logger.error("DCR name after replace: " + dcrName);
			
			
			String priLocaleFolderPath = vpath + "/" + priLocale + "/";
			CSFile priLocaleFile = client.getFile(new CSVPath(priLocaleFolderPath));
			logger.error("Primary Locale Folder Path: " + priLocaleFolderPath + " # Primary DCR: " + vpath + "/" + priLocale + "/" + dcrName);
			
			if(priLocaleFile != null) {
				if(priLocaleFile.getKind() == CSDir.KIND) {
					logger.error(priLocale + " folder exists..");
				} else {
					if(priLocaleFile.getKind() == CSHole.KIND) {
						logger.error(priLocale + " folder is of type hole");
						CSDir priLocaleDir = dataDir.createChildDirectory(priLocale);
					} else {
						throw new Exception(priLocaleFile + " is not a Directory and a File");
					}
				}                         
			} else {
				logger.error(priLocale + " folder does not exist..Create new data folder");
				CSDir priLocaleDir = dataDir.createChildDirectory(priLocale);
			}
			
			priDCRName = priLocale + "/" + dcrName;
			CSFile priDCRFile = client.getFile(new CSVPath(priLocaleFolderPath + "/" + dcrName));
			if(priDCRFile != null){
				logger.error("LINE NUMBER [86]");
				if(priDCRFile.getKind() == CSSimpleFile.KIND) {
					logger.error(priDCRName + " DCR File Already exists..");
					CSSimpleFile priDCRSF = (CSSimpleFile) client.getFile(new CSVPath(priLocaleFolderPath + "/" + dcrName));
					
					priDCRExistFlag = true;
					priDCRModFlag = priDCRFile.isModified();
					priDCRIsMasterValue = priDCRSF.getExtendedAttribute("TeamSite/Metadata/isMaster").getValue();
					priDCRContentEntryStatus = priDCRSF.getExtendedAttribute("TeamSite/Metadata/contentEntryStatus").getValue();
					
					logger.error("Primary DCR File " + priDCRExistFlag + " # " + priDCRModFlag + " # " + priDCRIsMasterValue + " # " + priDCRContentEntryStatus);
				} else {
					priDCRExistFlag = false;
					logger.error("LINE NUMBER [95]");
				}
			}else{
				priDCRExistFlag = false;
				logger.error("LINE NUMBER [99]");
			}

			logger.error("Primary DCR name: " + priDCRName + " # Primary DCR exist flag: " + priDCRExistFlag + " # Primary DCR modification flag: " + priDCRModFlag);
			
			
			
			
			String secLocaleFolderPath = vpath + "/" + secLocale + "/";
			CSFile secLocaleFile = client.getFile(new CSVPath(secLocaleFolderPath));
			logger.error("Secondary locale folder path: " + secLocaleFolderPath + " # Secondary DCR: " + vpath + "/" + secLocale + "/" + dcrName);
			
			if(secLocaleFile != null) {
				if(secLocaleFile.getKind() == CSDir.KIND) {
					logger.error(secLocale + " folder exists..");
				} else {
					if(secLocaleFile.getKind() == CSHole.KIND) {
						logger.error(secLocale + " folder is of type hole");
						CSDir secLocaleDir = dataDir.createChildDirectory(secLocale);
					} else {
						throw new Exception(secLocaleFile + " is not a Directory and a File");
					}
				}                         
			} else {
				logger.error(secLocale + " folder does not exist..Create new data folder");
				CSDir secLocaleDir = dataDir.createChildDirectory(secLocale);
			}
			
			secDCRName = secLocale + "/" + dcrName;
			CSFile secDCRFile = client.getFile(new CSVPath(secLocaleFolderPath + "/" + dcrName));
			if(secDCRFile != null){
				logger.error("LINE NUMBER [131]");
				if(secDCRFile.getKind() == CSSimpleFile.KIND) {
					logger.error(secDCRFile + " DCR File Already exists..");
					CSSimpleFile secDCRSF = (CSSimpleFile) client.getFile(new CSVPath(secLocaleFolderPath + "/" + dcrName));
					
					secDCRExistFlag = true;
					secDCRModFlag = secDCRFile.isModified();
					secDCRIsMasterValue = secDCRSF.getExtendedAttribute("TeamSite/Metadata/isMaster").getValue();
					secDCRContentEntryStatus = secDCRSF.getExtendedAttribute("TeamSite/Metadata/contentEntryStatus").getValue();
					
					logger.error("Secondary DCR File " + secDCRExistFlag + " # " + secDCRModFlag + " # " + secDCRIsMasterValue + " # " + secDCRContentEntryStatus);
				} else {
					secDCRExistFlag = false;
					logger.error("LINE NUMBER [140]");
				}
			}else{
				secDCRExistFlag = false;
				logger.error("LINE NUMBER [144]");
			}

			logger.error("Secondary DCR name: " + secDCRName + " # Secondary DCR exist flag: " + secDCRExistFlag + " # Secondary DCR modification flag: " + secDCRModFlag);
		} catch (Exception e) {
			logger.log(Level.ERROR, e.getMessage(), e);
		}
	%>
	<script type="text/javascript">
		var priDCRName = "<%=priDCRName%>";
		var priDCRExistFlag = "<%=priDCRExistFlag%>";
		var priDCRModFlag = "<%=priDCRModFlag%>";
		var priDCRIsMasterValue	= "<%=priDCRIsMasterValue%>";
		var priDCRContentEntryStatus = "<%=priDCRContentEntryStatus%>";
		
		var secDCRName = "<%=secDCRName%>";
		var secDCRExistFlag = "<%=secDCRExistFlag%>";
		var secDCRModFlag = "<%=secDCRModFlag%>";
		var secDCRIsMasterValue = "<%=secDCRIsMasterValue%>";
		var secDCRContentEntryStatus = "<%=secDCRContentEntryStatus%>";		
	</script>
	
	<body onload="parent.getScriptFrame().callBack(priDCRName, priDCRExistFlag, priDCRModFlag, priDCRIsMasterValue, priDCRContentEntryStatus, secDCRName, secDCRExistFlag, secDCRModFlag, secDCRIsMasterValue, secDCRContentEntryStatus)"></body>
</html>
