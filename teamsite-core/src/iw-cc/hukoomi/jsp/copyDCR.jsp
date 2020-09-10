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
String dcrname = "";
Boolean dcrExistFlag = false;

			try {
				CSClient client = null;
				client = user_ctx.getCSClient();
				String vpath = request.getParameter("vpath");
				logger.error("vpath "+vpath);
				CSFile categoryFile=client.getFile(new CSVPath(vpath));
				CSDir catDir = (CSDir) categoryFile;
				logger.error("dataDir "+catDir.getName());
				String locale = request.getParameter("locale");
				String dataFolderPath = vpath + "/" + locale + "/";
				logger.error("dataFolderPath "+dataFolderPath);
				CSFile dataFile=client.getFile(new CSVPath(dataFolderPath));
				logger.error(vpath + " : " + locale + " : " + request.getParameter("dcrname"));
				if(dataFile!=null){
					if(dataFile.getKind()==CSDir.KIND){
						logger.error("all folders exists..");
					}else{
						if(dataFile.getKind()==CSHole.KIND){
							logger.error("data folder is of type hole");
							CSDir localeDir=catDir.createChildDirectory(locale);
						}else{
							throw new Exception(dataFile+"is not a directory and a File");
						}
					}                         
				}else{
					logger.error("data folder does not exist..Create new data folder");
					CSDir localeDir=catDir.createChildDirectory(locale);

				}
				logger.error("vpath "+vpath);
				String filename = request.getParameter("dcrname");
				logger.error("filename "+filename);
				if(filename.contains(".xml")){
					String[] fileArr = filename.split("\\.");
					filename = fileArr[0];
				}else{
					 logger.error("filename does not contain xml ");
				}
				logger.error("filename after replace :"+filename);
				dcrname=locale+"/"+filename;
				CSFile dcrfile=client.getFile(new CSVPath(dataFolderPath+"/"+filename));
				if(dcrfile!=null){
					logger.error("LINE NUMBER [77]");
					if(dcrfile.getKind()==CSSimpleFile.KIND){
						logger.error("File Already exists..");
						dcrExistFlag = true;
					}else{
						dcrExistFlag = false;
						logger.error("LINE NUMBER [83]");
					}
				}else{
					dcrExistFlag = false;
					logger.error("LINE NUMBER [87]");
				}

				logger.error("dcrname[" + dcrname + "] dcrExistFlag  " + dcrExistFlag);
			}catch (Exception e) {
				logger.log(Level.ERROR, e.getMessage(), e);
				logger.log(Level.ERROR, e.getMessage(), e);
			}
		%>
	<script type="text/javascript">
		// var api = parent.getScriptFrame();
		var newdcrname = "<%=dcrname%>";
		var flag = "<%=dcrExistFlag%>";
		// alert("newdcrname[" + newdcrname + "] flag[" + flag + "]");
		// api.IWDCRInfo.setDCRName(newdcrname);
		// alert("Before saving");
		// api.IWDatacapture.save();
	</script>
	
	  <body onload="parent.getScriptFrame().CallBack(flag,newdcrname)">
		</body>

</html>
