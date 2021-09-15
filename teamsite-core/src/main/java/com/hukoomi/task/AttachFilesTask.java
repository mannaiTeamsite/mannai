package com.hukoomi.task;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

import javax.activation.DataSource;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import com.hukoomi.utils.TSPropertiesFileReader;
import com.interwoven.cssdk.access.CSAuthorizationException;
import com.interwoven.cssdk.access.CSExpiredSessionException;
import com.interwoven.cssdk.common.CSClient;
import com.interwoven.cssdk.common.CSException;
import com.interwoven.cssdk.common.CSRemoteException;
import com.interwoven.cssdk.filesys.CSAreaRelativePath;
import com.interwoven.cssdk.filesys.CSFile;
import com.interwoven.cssdk.filesys.CSSimpleFile;
import com.interwoven.cssdk.filesys.CSVPath;
import com.interwoven.cssdk.transform.XSLTransformer;
import com.interwoven.cssdk.workflow.CSExternalTask;
import com.interwoven.cssdk.workflow.CSTask;
import com.interwoven.cssdk.workflow.CSURLExternalTask;
import com.interwoven.livesite.json.JsonUtils;
import com.interwoven.livesite.workflow.WorkflowUtils;
import com.interwoven.livesite.workflow.task.AttachDeploymentTargetTask;
import com.interwoven.livesite.workflow.task.HeadlessUrlTaskContext;
import com.interwoven.livesite.workflow.task.TaskContext;
import com.interwoven.livesite.workflow.web.task.AjaxWebTaskContext;

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
     * Success transition comment
     */
    public static final String SUCCESS_TRANSITION_COMMENT = "task transition check files completed";
    

    @Override
    public void execute(CSClient client, CSExternalTask task,
            Hashtable params) throws CSException {
                logger.info("AttachFilesTask: execute()");
                Map<String, String> statusMap = null;

                statusMap = new HashMap<>();        

                
                statusMap.put(TRANSITION, SUCCESS_TRANSITION);
                statusMap.put(TRANSITION_COMMENT,
                        SUCCESS_TRANSITION_COMMENT);                             
                
                try
                {
                  
                  Map<String, CSFile> originalTaskFilesMap = WorkflowUtils.getTaskFileMap(task, client);
                  List<CSFile> originalTaskFiles = new ArrayList();
                  originalTaskFiles.addAll(originalTaskFilesMap.values());
                  
                  for (CSFile orgTaskFiles : originalTaskFiles)   
                  {  
                    
                      logger.info("File Name orgTaskFiles: " + orgTaskFiles);  
                  }  
                  
                  Map<String, List<CSFile>> deploymentFileListMap = null;
                  
                  Map<String, String> siteDeploymentTargetMap = WorkflowUtils.getSitesTargetMap(client, task);
                  Set<String> deploymentTargetSet = new HashSet();                 
                 
                  
                  deploymentTargetSet.addAll(siteDeploymentTargetMap.values());
                 
                      logger.info("Deployment Targets present in the WORKAREA  : " + deploymentTargetSet);
                 
                  if ((deploymentTargetSet.size() == 2))
                  {
                    deploymentFileListMap = new HashMap();
                    deploymentTargetSet.remove("UnknownTarget");
                    deploymentFileListMap.put(deploymentTargetSet.iterator().next(), originalTaskFiles);
                  }
                  else
                  {
                    deploymentFileListMap = getDeploymentFileListMap(originalTaskFiles, siteDeploymentTargetMap);
                  }
                  logger.info("originalTaskFiles  : " + originalTaskFiles.size());
                  Map<String, List<String>> wfModelMap = new HashMap();
                  
                  Map<String, List<List>> uiModelMap = new HashMap();                 
                      logger.info("Deployable Files obtained from the method  : " + deploymentFileListMap);
                
                  if (deploymentFileListMap.containsKey("UnknownTarget"))
                  {
                    for (String deploymentTarget : deploymentTargetSet) {
                      if ("UnknownTarget".equals(deploymentTarget))
                      {
                        List<CSFile> fileList = (List)deploymentFileListMap.get("UnknownTarget");
                        
                        putIntoUIModelMap(uiModelMap, fileList, "UnknownTarget");
                      }
                      else
                      {
                        putIntoUIModelMap(uiModelMap, Collections.emptyList(), deploymentTarget);
                      }
                    }
                    deploymentFileListMap.remove("UnknownTarget");
                  }
                  for (String deploymentTarget : deploymentFileListMap.keySet()) {
                    convertToRelativePath(deploymentFileListMap, deploymentTarget, wfModelMap);
                  }
                  
                    String jsonUIModel = JsonUtils.toJson(new TreeMap(uiModelMap)); 
                    String jsonWFModel = JsonUtils.toJson(wfModelMap); 
                        
                    logger.info("Shared JSON in the Workflow  : " + jsonWFModel); 
                    
                    WorkflowUtils.setLargeVariable("deployable.files",
                    jsonWFModel, task.getWorkflow());
                   
                }
                catch (CSException cse)
                {
                  throw new RuntimeException("Error while Getting Deployment Target for the files : " + cse.getMessage(), cse);
                }               
                            
      
                task.chooseTransition(statusMap.get(TRANSITION),
                statusMap.get(TRANSITION_COMMENT));
                
               
    }
    private void putIntoUIModelMap(Map<String, List<List>> uiModelMap, List<CSFile> fileList, String key)
            throws CSException
          {
            List<List> fileListMap = new ArrayList();
            for (CSFile file : fileList)
            {
              List<String> fileInfoList = new ArrayList();
              fileInfoList.add(file.getName());
              fileInfoList.add(file.getVPath().getAreaRelativePath().getParentPath().toString());
              fileListMap.add(fileInfoList);
            }
            uiModelMap.put(key, fileListMap);
          }

    private void convertToRelativePath(Map<String, List<CSFile>> srcMap, String key, Map<String, List<String>> destMap)
    {
      List<CSFile> fileList = (List)srcMap.get(key);
      List<String> fileNameStrArray = getRelativePathList(fileList);
      destMap.put(key, fileNameStrArray);
    }
    private List<String> getRelativePathList(List<CSFile> fileList)
    {
      List<String> relativeNameList = new ArrayList(fileList.size());
      for (CSFile file : fileList) {
        relativeNameList.add(file.getVPath().getAreaRelativePath().toString());
      }
      return relativeNameList;
    }
    private Map<String, List<CSFile>> getDeploymentFileListMap(List<CSFile> areaFiles, Map<String, String> sitesTargetMap)
            throws CSException
          {
            Map<String, List<CSFile>> deploymentFileListMap = new HashMap();
            Set<String> siteNameSet = new HashSet();
            List<CSFile> deploymentFileList = null;
            CSFile deployableFile;
            for (Iterator localIterator1 = areaFiles.iterator(); localIterator1.hasNext();)
            {
              deployableFile = (CSFile)localIterator1.next();
              
              siteNameSet = WorkflowUtils.getSitesReferred(deployableFile);
              for (String siteName : siteNameSet)
              {
                String deploymentTargetName = (String)sitesTargetMap.get(siteName);
                if (deploymentFileListMap.containsKey(deploymentTargetName)) {
                  deploymentFileList = (List)deploymentFileListMap.get(deploymentTargetName);
                } else {
                  deploymentFileList = new ArrayList();
                }
                deploymentFileList.add(deployableFile);
                deploymentFileListMap.put(deploymentTargetName, deploymentFileList);
              }
            }
            return deploymentFileListMap;
          }

}
