package com.hukoomi.task;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import org.apache.log4j.Logger;

import com.interwoven.cssdk.common.CSClient;
import com.interwoven.cssdk.common.CSException;
import com.interwoven.cssdk.filesys.CSArea;
import com.interwoven.cssdk.filesys.CSAreaRelativePath;
import com.interwoven.cssdk.filesys.CSFile;
import com.interwoven.cssdk.filesys.CSVPath;
import com.interwoven.cssdk.workflow.CSExternalTask;
import com.interwoven.cssdk.workflow.CSURLExternalTask;

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
	 * delimiter
	 */
	private static final String DELIMITER = "/";
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
		String strData = null;
		String str;
		CSAreaRelativePath[] files = task.getFiles();
		CSArea area = task.getArea();
		CSVPath path = area.getVPath();
		String areaPath = path.toString();

		List<String> linkArrayList = new ArrayList<>();

		for (CSAreaRelativePath file : files) {
			logger.info("Attachable: " + file.toString());
			fileName = new File(file.toString());
			CSFile attachedFile = area.getFile(file);

			if (fileName.toString().contains(".page") || fileName.toString().contains("templatedata/")) {
				logger.info("attacheFile Vpath: " + attachedFile.getVPath());
				String areaVPath = attachedFile.getVPath().toString();
				String fileLoc = areaVPath.substring(areaVPath.indexOf("/default/"));

				try (BufferedReader br = new BufferedReader(new FileReader(fileLoc));) {
					while ((str = br.readLine()) != null) {

						strData = validateDcr(str, strData, fileName, linkArrayList, brokenLinkList, areaPath, file);

					}
				} catch (FileNotFoundException e) {

					logger.info("File doesn't exist: " + e.getMessage());
					brokenLinkList.add(fileName.toString());
				} catch (IOException e) {
					logger.info(e);
				} finally {
					task.getWorkflow().setVariable("brokenLinks", brokenLinkList.toString());
					task.chooseTransition(statusMap.get(TRANSITION), statusMap.get(TRANSITION_COMMENT));
				}

				logger.info("Broken link to send in email: " + brokenLinkList);
			}
		}

	}

	public String validateDcr(String str, String strData, File fileName, List<String> linkArrayList,
			List<String> brokenLinkList, String areaPath, CSAreaRelativePath file) throws IOException {
		String internalFilePath;
		URL url;
		String tempStr = null;
		if (str.contains("templatedata/") && str.contains("</DCR>")) {
			strData = str.substring(str.indexOf("templatedata"), str.indexOf("</DCR>"));
			logger.info("DCR found in page file with path: " + strData);
		} else if (!(fileName.toString().contains(".page")) && (str.contains("http"))) {
			String[] linkArray = str.split("href");
			for (int i = 1; i < linkArray.length; i++) {
				tempStr = linkArray[i].substring(linkArray[i].indexOf("http"));
				linkArrayList.add(tempStr.substring(0, tempStr.indexOf("\"")));
			}
			logger.info("http link found in DCR: " + linkArrayList);
		}

		if (strData.contains("templatedata")) {
			internalFilePath = areaPath.substring(areaPath.indexOf("/default/")) + DELIMITER + strData;
			File internalFileName = new File(internalFilePath);
			if (!internalFileName.exists())
				brokenLinkList.add(strData);
			logger.info("DCR Path: " + internalFilePath);
			strData = "";
		} else if (!linkArrayList.isEmpty()) {
			for (String strDataTemp : linkArrayList) {
				url = new URL(strDataTemp);
				if (!doesURLExist(url))
					brokenLinkList.add(file.toString() + " : " + strDataTemp);
				logger.info("Link to validate: " + strDataTemp);
			}
			linkArrayList.clear();
		}
		return strData;
	}

	public boolean doesURLExist(URL url) throws IOException {
		HttpURLConnection.setFollowRedirects(false);
		HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

		httpURLConnection.setRequestMethod("HEAD");

		int responseCode = 0;
		try {
			responseCode = httpURLConnection.getResponseCode();
		} catch (SocketTimeoutException e) {

			logger.info("Socket exception: " + e.getMessage());
		} catch (Exception e) {

			logger.info("Exception occurred: " + e.getMessage());
		} finally {
			httpURLConnection.disconnect();
		}
		return (responseCode >= 200 && responseCode <= 399);
	}
}