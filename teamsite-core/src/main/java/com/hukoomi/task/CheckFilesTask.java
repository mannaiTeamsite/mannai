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
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

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
import com.interwoven.cssdk.workflow.CSURLExternalTask;

public class CheckFilesTask implements CSURLExternalTask {

    /**
     * Logger object to check the flow of the code.
     */
    private final Logger logger = Logger.getLogger(CheckFilesTask.class);
    
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
                logger.info("WaitTask: execute()");
                Map<String, String> statusMap = null;

                statusMap = new HashMap<>();        

        
                statusMap.put(TRANSITION, SUCCESS_TRANSITION);
                statusMap.put(TRANSITION_COMMENT,
                        SUCCESS_TRANSITION_COMMENT); 
                CSAreaRelativePath[] taskFileList = task.getFiles();
                logger.info("TaskFileList Length : " + taskFileList.length);
                for (CSAreaRelativePath taskFilePath : taskFileList) {
                    CSFile file = task.getArea().getFile(taskFilePath);
                    String fileName = file.getName();
                    logger.info("File Name : " + fileName);

                                        
                }
      
                task.chooseTransition(statusMap.get(TRANSITION),
                statusMap.get(TRANSITION_COMMENT));
    }

    

}
