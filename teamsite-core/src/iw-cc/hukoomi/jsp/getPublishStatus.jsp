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
	String dcrPublishMetaData = "";
	int dcrPublishStatus = 0;
	String vpath = "";
	String dcrName = "";
	String locale = "";
		

		try {
			CSClient client = null;
			client = user_ctx.getCSClient();
			
			vpath = request.getParameter("vpath");
			dcrName = request.getParameter("dcrName");
			locale = request.getParameter("locale");
			
			logger.error("vpath : "+vpath);
			logger.error("dcrName : "+dcrName);
			logger.error("locale : "+locale);
			
			//logger.info("VPath[" + locale + vpath + "] DCR_Name[" + dcrName);
			
			CSFile categoryFile = client.getFile(new CSVPath(vpath));
			CSDir dataDir = (CSDir) categoryFile;
			logger.info("VPath: " + vpath + " # Data Directory: " + dataDir.getName());
			
			
			logger.info("DCR Name: " + dcrName);
			if(dcrName.contains(".xml")){
				String[] fileArr = dcrName.split("\\.");
				dcrName = fileArr[0];
			}else{
				 logger.info("DCR name does not contain xml ");
			}
			logger.info("DCR name after replace: " + dcrName);
			
			
			String priLocaleFolderPath = vpath + "/" + locale + "/";
			CSFile priLocaleFile = client.getFile(new CSVPath(priLocaleFolderPath));
			logger.error("Primary Locale Folder Path: " + priLocaleFolderPath + " # Primary DCR: " + vpath + "/" + locale + "/" + dcrName);
			
			if(priLocaleFile != null) {
				if(priLocaleFile.getKind() == CSDir.KIND) {
					logger.error(locale + " folder exists..");
				} else {
					if(priLocaleFile.getKind() == CSHole.KIND) {
						logger.error(locale + " folder is of type hole");
						CSDir priLocaleDir = dataDir.createChildDirectory(locale);
					} else {
						throw new Exception(priLocaleFile + " is not a Directory and a File");
					}
				}                         
			} else {
				logger.error(locale + " folder does not exist..Create new data folder");
				CSDir priLocaleDir = dataDir.createChildDirectory(locale);
			}
			
			priDCRName = locale + "/" + dcrName;
			CSFile priDCRFile = client.getFile(new CSVPath(priLocaleFolderPath + "/" + dcrName));
			
			if(priDCRFile != null){
				logger.error("LINE NUMBER [86]");
					if(priDCRFile.getKind() == CSSimpleFile.KIND) {
						logger.info(dcrName + " DCR File Already exists..");
						CSSimpleFile priDCRSF = (CSSimpleFile) client.getFile(new CSVPath(priLocaleFolderPath + "/" + dcrName));
						
						dcrPublishMetaData = priDCRSF.getExtendedAttribute("TeamSite/LiveSite/PublishDate").getValue();
	
						logger.info("DCR Published Date : " + dcrPublishMetaData);
						
							if(!dcrPublishMetaData.equalsIgnoreCase("") || dcrPublishMetaData != null){
							    dcrPublishStatus = 1;
							    logger.info("DCR is Published");
							}else{
							    dcrPublishStatus = 0;
							    logger.info("DCR is not Published");
							}
					} else {
						
						logger.error("DCR File is not exist");
					}
			}else{
				logger.error("LINE NUMBER [99]");
			}

			logger.info("DCR name: " + dcrName + " # Primary DCR Publish Stauts flag: " + dcrPublishStatus);
			
			} catch (Exception e) {
			logger.log(Level.ERROR, e.getMessage(), e);
		}
	%>
	<script type="text/javascript">
		var dcrName = "<%=dcrName%>";
		var dcrPublishStatus = "<%=dcrPublishStatus%>";
	</script>
	
	<body onload="parent.getScriptFrame().getPublishStatusCallBack(dcrName, dcrPublishStatus)"></body>
</html>
