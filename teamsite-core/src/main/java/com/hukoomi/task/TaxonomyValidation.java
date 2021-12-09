package com.hukoomi.task;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import org.apache.log4j.Logger;

public class TaxonomyValidation {
	/**
	 * Logger object to check the flow of the code.
	 */
	private static final Logger logger = Logger.getLogger(TaxonomyValidation.class);

	private static void replaceString(String fileName, String oldStr, String rplStr) {
		File dcrFile = new File(fileName);
		String s = "";
		String totalStr = "";
		logger.info("replaceString funtion string: " + fileName + "---" + oldStr + "---" + rplStr);
		try (FileReader fr = new FileReader(dcrFile);
			BufferedReader br = new BufferedReader(fr);
				FileWriter fw = new FileWriter(fileName);){
			
			while ((s = br.readLine()) != null) {
				s = s.replace(oldStr, rplStr);
				if (totalStr != null)
					totalStr = s;
				else
					totalStr = totalStr + "\n" + s;
			}
			
			fw.write(totalStr);
			
		} catch (FileNotFoundException ex) {
			logger.info("FileNotFoundException: " + ex);
		} catch (IOException ex) {
			logger.info("IOException" + ex);
		} catch (Exception ex) {
			logger.info("Exception" + ex);
		}
	}

	private static void deployFileWriter(String fileName) {

		String currentLine;
		Boolean scanFlag = true;

		File file = new File("/usr/opentext/TeamSite/tmp/taxonomyFilesDeploy.txt");

		try (Scanner scanner = new Scanner(file);
				FileWriter writer = new FileWriter("/usr/opentext/TeamSite/tmp/taxonomyFilesDeploy.txt", true);) {

			BufferedWriter bufferedWriter = new BufferedWriter(writer);

			while (scanner.hasNext()) {
				currentLine = scanner.nextLine();
				if (currentLine.trim().equals(fileName)) {
					scanFlag = false;
					break;
				}
			}
			if (Boolean.TRUE.equals(scanFlag)) {
				bufferedWriter.write(fileName);
				bufferedWriter.newLine();
			}
			bufferedWriter.close();
			
		} catch (IOException e) {
			logger.info("IOException" + e);
		} catch (Exception e) {
			logger.info("Exception" + e);
		}
	}

	private static List<File> listf(String directoryName) {
		File directory = new File(directoryName);
		List<File> resultList = new ArrayList<>();

		File[] fList = directory.listFiles();
		resultList.addAll(Arrays.asList(fList));
		for (File file : fList) {
			if (file.isFile()) {
				logger.info("fileName with Path: " + file.getAbsolutePath());
			} else if (file.isDirectory()) {
				resultList.addAll(listf(file.getAbsolutePath()));
			}
		}
		return resultList;
	}
}