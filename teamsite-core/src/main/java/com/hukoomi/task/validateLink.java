package com.hukoomi.task;

import com.interwoven.cssdk.common.CSClient;
import com.interwoven.cssdk.common.CSException;
import com.interwoven.cssdk.filesys.CSArea;
import com.interwoven.cssdk.filesys.CSAreaRelativePath;
import com.interwoven.cssdk.filesys.CSFile;
import com.interwoven.cssdk.filesys.CSVPath;
import com.interwoven.cssdk.workflow.CSExternalTask;
import com.interwoven.cssdk.workflow.CSURLExternalTask;
import org.apache.log4j.Logger;


import java.io.*;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

public class validateLink implements CSURLExternalTask {
    /**
     * Logger object to check the flow of the code.
     */
    private final Logger logger = Logger.getLogger(validateLink.class);
    /**
     * Success transition message
     */
    public static final String SUCCESS_TRANSITION = "Validate Links Success";
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
        logger.info("Initiate validation links present in Files from workflow");
        HashMap<String, String> statusMap = new HashMap<>();
        statusMap.put(TRANSITION, SUCCESS_TRANSITION);
        statusMap.put(TRANSITION_COMMENT, "Successfully completed task: Validate Links");

        List<String> brokenLinkList = new ArrayList<>();
        File fileName = null;
        File internalFileName;
        try {
            CSAreaRelativePath[] files = task.getFiles();
            CSArea area = task.getArea();
            CSVPath path = area.getVPath();
            String areaPath = path.toString();
            String internalFilePath;
            String str;
            String strData = "";
            URL url;
            List<String> linkArrayList = new ArrayList<>();
            logger.info("Files: "+files.toString());
            for (CSAreaRelativePath file : files) {
                logger.info("Attachable: "+file.toString());
                fileName = new File(file.toString());
                CSFile attachedFile = area.getFile(file);

                if (fileName.toString().contains(".page") || fileName.toString().contains("templatedata/")) {
                    logger.info("attacheFile Vpath: "+attachedFile.getVPath());
                    String areaVPath = attachedFile.getVPath().toString();
                    String fileLoc = areaVPath.substring(areaVPath.indexOf("/default/"));
                    String tempStr;

                    BufferedReader br = new BufferedReader(new FileReader(fileLoc));
                    while ((str = br.readLine()) != null) {
                        if (str.contains("templatedata/") && str.contains("</DCR>")) {
                            strData = str.substring(str.indexOf("templatedata"),str.indexOf("</DCR>"));
                            logger.info("DCR found in page file with path: "+ strData);
                        }else if(!(fileName.toString().contains(".page")) && (str.contains("http"))){
                            String[] linkArray = str.split("href");
                            for(int i=1;i<linkArray.length;i++){
                                tempStr = linkArray[i].substring(linkArray[i].indexOf("http"));
                                linkArrayList.add(tempStr.substring(0,tempStr.indexOf("\"")));
                            }
                            logger.info("http link found in DCR: "+linkArrayList);
                        }

                        if(strData.contains("templatedata")){
                            internalFilePath = areaPath.substring(areaPath.indexOf("/default/"))+"/"+strData;
                            internalFileName = new File(internalFilePath);
                            if(!internalFileName.exists())
                                brokenLinkList.add(strData);
                            logger.info("DCR Path: "+internalFilePath);
                            strData = "";
                        }else if(!linkArrayList.isEmpty()){
                            for(String strDataTemp:linkArrayList) {
                                url = new URL(strDataTemp);
                                if (!doesURLExist(url))
                                    brokenLinkList.add(strDataTemp);
                                logger.info("Link to validate: " + strDataTemp);
                            }
                            linkArrayList.clear();
                        }
                    }
                    br.close();
                    logger.info("Broken link to send in email: "+brokenLinkList);
                }
            }

        } catch (FileNotFoundException e){
            //e.printStackTrace();
            logger.info("File doesn't exist: "+e.getMessage());
            brokenLinkList.add(fileName.toString());
        } catch (CSException | IOException e) {
            //e.printStackTrace();
            logger.info("Exception occurred: "+e.getMessage());
        } finally {
            task.getWorkflow().setVariable("brokenLinks", brokenLinkList.toString());
            task.chooseTransition(statusMap.get(TRANSITION),statusMap.get(TRANSITION_COMMENT));
        }

    }

    public boolean doesURLExist(URL url) throws IOException
    {
        HttpURLConnection.setFollowRedirects(false);
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

        httpURLConnection.setRequestMethod("HEAD");
        //httpURLConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 6.0; en-US; rv:1.9.1.2) Gecko/20090729 Firefox/3.5.2 (.NET CLR 3.5.30729)");

        int responseCode = 0;
        try {
            responseCode = httpURLConnection.getResponseCode();
        }catch (SocketTimeoutException e){
            //e.printStackTrace();
            logger.info("Socket exception: "+e.getMessage());
        } catch (Exception e){
            //e.printStackTrace();
            logger.info("Exception occurred: "+e.getMessage());
        } finally {
            httpURLConnection.disconnect();
        }
        return (responseCode >=200 && responseCode <= 399);
    }
}