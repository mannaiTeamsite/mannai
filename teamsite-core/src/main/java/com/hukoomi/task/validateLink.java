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
import com.interwoven.cssdk.filesys.CSVPath;
import com.interwoven.cssdk.workflow.CSExternalTask;
import com.interwoven.cssdk.workflow.CSURLExternalTask;
import org.apache.log4j.Logger;

import java.awt.desktop.SystemEventListener;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class validateLink implements CSURLExternalTask {
    /**
     * Logger object to check the flow of the code.
     */
    private final Logger logger = Logger.getLogger(validateLink.class);

    @Override
    public void execute(CSClient client, CSExternalTask task, Hashtable params) throws CSException {
        logger.info("Initiate validation links present in Files from workflow");
        String hName = getHostName();
        logger.info("HostName: "+hName);
        String tsDomain = "https://devauth.hukoomi.gov.qa";
        //StringBuilder stringBuilder = new StringBuilder();
        List<String> brokenLinkList = new ArrayList<>();
        File fileName = null;
        try {
            CSAreaRelativePath[] files = task.getFiles();
            CSArea area = task.getArea();
            logger.info("Files: "+files.toString());
            for (CSAreaRelativePath file : files) {
                logger.info("Attachable: "+file.toString());
                fileName = new File(file.toString());
                CSFile attachedFile = area.getFile(file);
                CSVPath path = area.getVPath();
                CSAreaRelativePath relativePath = path.getAreaRelativePath();
                if (fileName.toString().contains(".page") || fileName.toString().contains("templatedata/")) {
                    String str;
                    String strData = "";
                    String strLink;
                    URL url;
                    boolean response = false;
                    logger.info("attacheFile Vpath: "+attachedFile.getVPath());
                    logger.info("Path Areaname: "+path.getAreaName());
                    logger.info("Path String: "+path.toString());
                    logger.info("relativePath: "+relativePath.toString());
                    logger.info("attacheFile String : "+attachedFile.toString());
                    logger.info("attacheFile Name : "+attachedFile.getName());
                    String areaVPath = attachedFile.getVPath().toString();
                    String fileLoc = areaVPath.substring(areaVPath.indexOf("default")-1);
                    String tempStr;
                    //BufferedReader br = new BufferedReader(new FileReader(attachedFile.getName()));
                    BufferedReader br = new BufferedReader(new FileReader(fileLoc));
                    while ((str = br.readLine()) != null) {
                        if (str.contains("/default/sites/") && str.contains(".page")) {
                            logger.info("In If Condition");
                            //strData = str.substring(str.indexOf(""),str.indexOf(""));
                        }else if (str.contains("templatedata/") && str.contains("/data/")) {
                            strData = str.substring(str.indexOf("templatedata"),str.indexOf("</DCR>")-1);
                            logger.info("In first else if: "+ strData);
                        }else if(!(fileName.toString().contains(".page")) && (str.contains("http"))){
                            tempStr = str.substring(str.indexOf("http"));
                            strData = tempStr.substring(0,tempStr.indexOf("\"")-1);
                            logger.info("In Second else if: "+ strData);
                        }else{
                            logger.info("Do Nothing: "+str);
                        }
                        logger.info("Before strData: "+str);
                        logger.info("strData: "+strData);

                        if(!strData.isEmpty()){
                            if(strData.contains("templatedata"))
                                strLink = tsDomain+"/iw-cc/command/iw.group.preview_file?vpath=//"+hName+"//default/main/Hukoomi/WORKAREA/default/"+strData;
                            else
                                strLink = strData;
                            logger.info("strLink: "+strLink);

                            url = new URL (strLink);
                            response  = doesURLExist(url);
                            logger.info("response: "+response);
                            if(!response){
                                brokenLinkList.add(strLink);
                                //stringBuilder.append(strLink);
                                //stringBuilder.append(System.getProperty("line.separator"));
                                //stringBuilder.append(System.lineSeparator());
                            }
                        }
                        strData = "";
                    }
                    br.close();
                    logger.info("brokenLinkList: "+brokenLinkList);
                }
            }
            //task.getWorkflow().setVariable("brokenLinks", stringBuilder.toString());
        } catch (FileNotFoundException e){
            e.printStackTrace();
            brokenLinkList.add(fileName.toString());
            //stringBuilder.add(System.lineSeparator());
        } catch (CSException | IOException e) {
            e.printStackTrace();
        } finally {
            task.getWorkflow().setVariable("brokenLinks", brokenLinkList.toString());
        }
        //task.getWorkflow().setVariable("brokenLinks", stringBuilder.toString());
    }

    public String getHostName(){
        String hostN = "";
        try
        {
            Process process = Runtime.getRuntime().exec("hostname");
            process.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line=reader.readLine())!=null)
            {
                hostN = line;
            }
            process.destroy();
        }
        catch(Exception e)
        {
            //System.out.println(e);
            e.printStackTrace();
        }
        return hostN;
    }

    public boolean doesURLExist(URL url) throws IOException
    {
        // We want to check the current URL

        HttpURLConnection.setFollowRedirects(false);
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

        // We don't need to get data
        httpURLConnection.setRequestMethod("HEAD");

        // Some websites don't like programmatic access so pretend to be a browser
        httpURLConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 6.0; en-US; rv:1.9.1.2) Gecko/20090729 Firefox/3.5.2 (.NET CLR 3.5.30729)");
        int responseCode = 0;
        try {
            responseCode = httpURLConnection.getResponseCode();
        } catch (Exception e){
            e.printStackTrace();
            logger.info("exception: "+e.getMessage());
        } finally {
            httpURLConnection.disconnect();
        }

        return (responseCode >=200 && responseCode <= 399);

        // We only accept response code 200
        //return responseCode == HttpURLConnection.HTTP_OK;
    }
}