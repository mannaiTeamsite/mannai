package com.hukoomi.task;

import com.interwoven.cssdk.access.CSAuthorizationException;
import com.interwoven.cssdk.access.CSExpiredSessionException;
import com.interwoven.cssdk.common.CSClient;
import com.interwoven.cssdk.common.CSException;
import com.interwoven.cssdk.common.CSObjectNotFoundException;
import com.interwoven.cssdk.common.CSRemoteException;
import com.interwoven.cssdk.filesys.CSArea;
import com.interwoven.cssdk.filesys.CSAreaRelativePath;
import com.interwoven.cssdk.filesys.CSFile;
import com.interwoven.cssdk.workflow.CSExternalTask;
import com.interwoven.cssdk.workflow.CSURLExternalTask;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

public class DetachLockedFiles implements CSURLExternalTask {

    /**
     * Logger object to check the flow of the code.
     */
    private final Logger logger = Logger.getLogger(DetachLockedFiles.class);
    /**
     * Success transition message
     */
    public static final String SUCCESS_TRANSITION = "Detach Locked Files Success";
    /**
     * Transition hashmap key
     */
    private static final String TRANSITION = "TRANSITION";
    /**
     * Transition comment hashmap key
     */
    private static final String TRANSITION_COMMENT = "TRANSITION_COMMENT";

    @Override
    public void execute(CSClient client, CSExternalTask task, Hashtable params) throws CSException {
        logger.info("Initiate Detaching Locked Files from workflow");
        HashMap<String, String> statusMap = new HashMap<>();
        try {
            CSAreaRelativePath[] files = task.getFiles();
            List<CSAreaRelativePath> filesToDetach = new ArrayList<>();
            CSArea area = task.getArea();
            String fileOwner;
            String jobOwner;

            for (CSAreaRelativePath file : files) {
                CSFile attachedFile = area.getFile(file);
                if (attachedFile.isLocked()) {
                    fileOwner = attachedFile.getLockOwner().getName();
                    jobOwner = task.getWorkflow().getOwner().getName();
                    if(!(fileOwner.equals(jobOwner)))
                        filesToDetach.add(file);
                }
            }
            if (!filesToDetach.isEmpty()) {
                CSAreaRelativePath[] detachFiles = new CSAreaRelativePath[filesToDetach.size()];
                task.detachFiles(filesToDetach.toArray(detachFiles));
                task.getWorkflow().setVariable("removedFiles", filesToDetach.toString());
            }
            statusMap.put(TRANSITION, SUCCESS_TRANSITION);
            statusMap.put(TRANSITION_COMMENT, "Successfully completed task: Detached Locked Files");
            logger.info("Detaching Locked Files from workflow completed");
            task.chooseTransition(statusMap.get(TRANSITION),statusMap.get(TRANSITION_COMMENT));
        } catch (CSException ex) {
            logger.error("Common Services Exception occurred while detaching the files from task", ex);
        } catch (Exception e){
            logger.error("Exception in DetachLockedFiles : ", e);
        }
    }
}
