package com.hukoomi.task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.interwoven.cssdk.common.CSClient;
import com.interwoven.cssdk.common.CSException;
import com.interwoven.cssdk.filesys.CSFile;
import com.interwoven.cssdk.workflow.CSExternalTask;
import com.interwoven.cssdk.workflow.CSTask;
import com.interwoven.cssdk.workflow.CSURLExternalTask;
import com.interwoven.livesite.json.JsonUtils;
import com.interwoven.livesite.workflow.WorkflowUtils;

public class AttachFilesTask implements CSURLExternalTask {

	/**
	 * Logger object to check the flow of the code.
	 */
	private final Logger logger = Logger.getLogger(AttachFilesTask.class);

	/**
	 * Transition hashmap key
	 */
	private static final String TRANSITION = "TRANSITION";
	/**
	 * Transition comment hashmap key
	 */
	private static final String TRANSITION_COMMENT = "TRANSITION_COMMENT";
	/**
	 * Success transition message
	 */
	public static final String SUCCESS_TRANSITION = "check file review";
	/**
	 * Unkknown Target comment
	 */
	public static final String UNKNOW_TARGET = "UnknownTarget";
	/**
	 * Success transition comment
	 */
	public static final String SUCCESS_TRANSITION_COMMENT = "task transition check files completed";

	@Override
	public void execute(CSClient client, CSExternalTask task, Hashtable params) throws CSException {
	    this.logger.info("AttachFilesTask: execute()");
	    Map<String, String> statusMap = null;
	    statusMap = new HashMap<>();
	    statusMap.put(TRANSITION, SUCCESS_TRANSITION);
	    statusMap.put(TRANSITION_COMMENT, SUCCESS_TRANSITION_COMMENT);
	    try {
	      Map<String, CSFile> originalTaskFilesMap = WorkflowUtils.getTaskFileMap((CSTask)task, client);
	      List<CSFile> originalTaskFiles = new ArrayList<>();
	      originalTaskFiles.addAll(originalTaskFilesMap.values());
	      for (CSFile orgTaskFiles : originalTaskFiles)
	        this.logger.info("File Name orgTaskFiles: " + orgTaskFiles); 
	      Map<String, List<CSFile>> deploymentFileListMap = null;
	      Map<String, String> siteDeploymentTargetMap = WorkflowUtils.getSitesTargetMap(client, (CSTask)task);
	      Set<String> deploymentTargetSet = new HashSet<>();
	      deploymentTargetSet.addAll(siteDeploymentTargetMap.values());
	      this.logger.info("Deployment Targets present in the WORKAREA  : " + deploymentTargetSet);
	      if (deploymentTargetSet.size() == 2) {
	        deploymentFileListMap = new HashMap<>();
	        deploymentTargetSet.remove(UNKNOW_TARGET);
	        deploymentFileListMap.put(deploymentTargetSet.iterator().next(), originalTaskFiles);
	      } else {
	        deploymentFileListMap = getDeploymentFileListMap(originalTaskFiles, siteDeploymentTargetMap);
	      } 
	      this.logger.info("originalTaskFiles  : " + originalTaskFiles.size());
	      Map<String, List<String>> wfModelMap = new HashMap<>();
	      Map<String, List<List>> uiModelMap = new HashMap<>();
	      this.logger.info("Deployable Files obtained from the method  : " + deploymentFileListMap);
	      if (deploymentFileListMap.containsKey(UNKNOW_TARGET)) {
	        for (String deploymentTarget : deploymentTargetSet) {
	          if (UNKNOW_TARGET.equals(deploymentTarget)) {
	            List<CSFile> fileList = deploymentFileListMap.get(UNKNOW_TARGET);
	            putIntoUIModelMap(uiModelMap, fileList, UNKNOW_TARGET);
	            continue;
	          } 
	          putIntoUIModelMap(uiModelMap, Collections.emptyList(), deploymentTarget);
	        } 
	        deploymentFileListMap.remove(UNKNOW_TARGET);
	      } 
	      for (String deploymentTarget : deploymentFileListMap.keySet())
	        convertToRelativePath(deploymentFileListMap, deploymentTarget, wfModelMap); 
	     
	      String jsonWFModel = JsonUtils.toJson(wfModelMap);
	      this.logger.info("Shared JSON in the Workflow  : " + jsonWFModel);
	      WorkflowUtils.setLargeVariable("deployable.files", jsonWFModel, task
	          .getWorkflow());
	    } catch (CSException cse) {
	    			logger.info(cse);	    } 
	    task.chooseTransition(statusMap.get(TRANSITION), statusMap
	        .get(TRANSITION_COMMENT));
	  }
	  
	  private void putIntoUIModelMap(Map<String, List<List>> uiModelMap, List<CSFile> fileList, String key) throws CSException {
	    List<List> fileListMap = new ArrayList<>();
	    for (CSFile file : fileList) {
	      List<String> fileInfoList = new ArrayList<>();
	      fileInfoList.add(file.getName());
	      fileInfoList.add(file.getVPath().getAreaRelativePath().getParentPath().toString());
	      fileListMap.add(fileInfoList);
	    } 
	    uiModelMap.put(key, fileListMap);
	  }
	  
	  private void convertToRelativePath(Map<String, List<CSFile>> srcMap, String key, Map<String, List<String>> destMap) {
	    List<CSFile> fileList = srcMap.get(key);
	    List<String> fileNameStrArray = getRelativePathList(fileList);
	    destMap.put(key, fileNameStrArray);
	  }
	  
	  private List<String> getRelativePathList(List<CSFile> fileList) {
	    List<String> relativeNameList = new ArrayList<>(fileList.size());
	    for (CSFile file : fileList)
	      relativeNameList.add(file.getVPath().getAreaRelativePath().toString()); 
	    return relativeNameList;
	  }
	  
	  private Map<String, List<CSFile>> getDeploymentFileListMap(List<CSFile> areaFiles, Map<String, String> sitesTargetMap) throws CSException {
	    Map<String, List<CSFile>> deploymentFileListMap = new HashMap<>();
	    
	    List<CSFile> deploymentFileList = null;
	    for (Iterator<CSFile> localIterator1 = areaFiles.iterator(); localIterator1.hasNext(); ) {
	      CSFile deployableFile = localIterator1.next();
	      Set<String> siteNameSet = WorkflowUtils.getSitesReferred(deployableFile);
	      for (String siteName : siteNameSet) {
	        String deploymentTargetName = sitesTargetMap.get(siteName);
	        if (deploymentFileListMap.containsKey(deploymentTargetName)) {
	          deploymentFileList = deploymentFileListMap.get(deploymentTargetName);
	        } else {
	          deploymentFileList = new ArrayList<>();
	        } 
	        deploymentFileList.add(deployableFile);
	        deploymentFileListMap.put(deploymentTargetName, deploymentFileList);
	      } 
	    } 
	    return deploymentFileListMap;
	  }
	}
