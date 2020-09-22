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
			String priLocale = request.getParameter("priLocale");
			String secLocale = request.getParameter("secLocale");
			logger.info("VPath[" + vpath + "] DCR_Name[" + dcrName + "] Primary_Locale[" + priLocale + "] Secondary_Locale[" + secLocale + "]");
			
			CSFile categoryFile = client.getFile(new CSVPath(vpath));
			CSDir dataDir = (CSDir) categoryFile;
			logger.info("Data Directory[" + dataDir.getName() + "]");
			
			
			logger.info("DCR Name: " + dcrName);
			if(dcrName.contains(".xml")){
				String[] fileArr = dcrName.split("\\.");
				dcrName = fileArr[0];
			}else{
				 logger.info("DCR name does not contain xml");
			}
			logger.info("DCR name after replace[" + dcrName + "]");
			
			
			String priLocaleFolderPath = vpath + "/" + priLocale + "/";
			CSFile priLocaleFile = client.getFile(new CSVPath(priLocaleFolderPath));
			logger.info("Primary Locale Folder Path: " + priLocaleFolderPath + " # Primary DCR: " + vpath + "/" + priLocale + "/" + dcrName);
			
			if(priLocaleFile != null) {
				if(priLocaleFile.getKind() == CSDir.KIND) {
					logger.info(priLocale + " folder exists..");
				} else {
					if(priLocaleFile.getKind() == CSHole.KIND) {
						logger.info(priLocale + " folder is of type hole");
						CSDir priLocaleDir = dataDir.createChildDirectory(priLocale);
					} else {
						throw new Exception(priLocaleFile + " is not a Directory and a File");
					}
				}                         
			} else {
				logger.info(priLocale + " folder does not exist..Create new data folder");
				CSDir priLocaleDir = dataDir.createChildDirectory(priLocale);
			}
			
			priDCRName = priLocale + "/" + dcrName;
			CSFile priDCRFile = client.getFile(new CSVPath(priLocaleFolderPath + "/" + dcrName));
			if(priDCRFile != null){
				logger.info("LINE NUMBER [92]");
				if(priDCRFile.getKind() == CSSimpleFile.KIND) {
					logger.info(priDCRName + " DCR File Already exists..");
					CSSimpleFile priDCRSF = (CSSimpleFile) client.getFile(new CSVPath(priLocaleFolderPath + "/" + dcrName));
					
					priDCRExistFlag = true;
					priDCRModFlag = priDCRFile.isModified();
					priDCRIsMasterValue = priDCRSF.getExtendedAttribute("TeamSite/Metadata/isMaster").getValue();
					priDCRContentEntryStatus = priDCRSF.getExtendedAttribute("TeamSite/Metadata/contentEntryStatus").getValue();
					
					logger.info("Primary DCR File " + priDCRExistFlag + " # " + priDCRModFlag + " # " + priDCRIsMasterValue + " # " + priDCRContentEntryStatus);
				} else {
					priDCRExistFlag = false;
					logger.info("LINE NUMBER [105]");
				}
			}else{
				priDCRExistFlag = false;
				logger.info("LINE NUMBER [109]");
			}

			logger.info("Primary DCR Name[" + priDCRName + "] Exist_Flag[" + priDCRExistFlag + "] Mod_Flag: " + priDCRModFlag);
			
			String secLocaleFolderPath = vpath + "/" + secLocale + "/";
			CSFile secLocaleFile = client.getFile(new CSVPath(secLocaleFolderPath));
			logger.info("Secondary locale folder path: " + secLocaleFolderPath + " # Secondary DCR: " + vpath + "/" + secLocale + "/" + dcrName);
			
			if(secLocaleFile != null) {
				if(secLocaleFile.getKind() == CSDir.KIND) {
					logger.info(secLocale + " folder exists..");
				} else {
					if(secLocaleFile.getKind() == CSHole.KIND) {
						logger.info(secLocale + " folder is of type hole");
						CSDir secLocaleDir = dataDir.createChildDirectory(secLocale);
					} else {
						throw new Exception(secLocaleFile + " is not a Directory and a File");
					}
				}                         
			} else {
				logger.info(secLocale + " folder does not exist..Create new data folder");
				CSDir secLocaleDir = dataDir.createChildDirectory(secLocale);
			}
			
			secDCRName = secLocale + "/" + dcrName;
			CSFile secDCRFile = client.getFile(new CSVPath(secLocaleFolderPath + "/" + dcrName));
			if(secDCRFile != null) {
				logger.info("LINE NUMBER [137]");
				if(secDCRFile.getKind() == CSSimpleFile.KIND) {
					logger.info(secDCRFile + ": DCR File Already exists..");
					CSSimpleFile secDCRSF = (CSSimpleFile) client.getFile(new CSVPath(secLocaleFolderPath + "/" + dcrName));
					
					secDCRExistFlag = true;
					secDCRModFlag = secDCRFile.isModified();
					secDCRIsMasterValue = secDCRSF.getExtendedAttribute("TeamSite/Metadata/isMaster").getValue();
					secDCRContentEntryStatus = secDCRSF.getExtendedAttribute("TeamSite/Metadata/contentEntryStatus").getValue();
					
					logger.info("Secondary DCR Exist_Flag[" + secDCRExistFlag + "] Mod_Flag[" + secDCRModFlag + "] Is_Master[" + secDCRIsMasterValue + "] ContentEntryStatus[" + secDCRContentEntryStatus + "]");
				} else {
					secDCRExistFlag = false;
					logger.info("LINE NUMBER [150]");
				}
			} else {
				secDCRExistFlag = false;
				logger.info("LINE NUMBER [154]");
			}

			logger.info("Secondary DCR Name[" + secDCRName + "] Exist_Flag[" + secDCRExistFlag + "] Mod_Flag: " + secDCRModFlag);
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
