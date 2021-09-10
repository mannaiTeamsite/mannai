package com.hukoomi.task;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
//import com.interwoven.cssdk.filesys.CSAreaRelativePath;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
//import org.w3c.dom.*;
//import org.xml.sax.SAXException;

//import javax.xml.parsers.DocumentBuilder;
//import javax.xml.parsers.DocumentBuilderFactory;
//import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class TaxonomyValidation {
    /**
     * Logger object to check the flow of the code.
     */
    private final static Logger logger = Logger.getLogger(TaxonomyValidation.class);

   /* public static void main(String[] args) {
        DOMConfigurator.configure("/usr/opentext/ApplicationContainer/standalone/deployments/iw-cc.war/WEB-INF/classes/log4j.xml");
        String str, strContent = "";
        String dcrType = "", taxonomyKey = "", labelEn ="", labelAr = "", taxonomyFirstVal = "", taxonomySecondVal = "" , taxonomyTagValue = "", taxonomyKeySub = "", labelEnSub = "", labelArSub = "";
        File files = null;
        //String[] fileList = null;
        List<File> fileList = new ArrayList<>();
        HashMap<String, String> taxonomyMap = new HashMap<>();
        BufferedReader brContent = null;
        try {
            BufferedReader br = new BufferedReader(new FileReader("/usr/opentext/TeamSite/tmp/taxonomyFilesPublished.txt"));
            //BufferedReader br = new BufferedReader(new FileReader("C:\\Users\\happe\\Documents\\hukoomiTaxonomy\\taxonomyFilesPublished.txt"));

            FileReader reader=new FileReader("/default/main/Hukoomi/WORKAREA/default/iw/config/properties/taxonomyValues.properties");
            //FileReader reader=new FileReader("C:\\Users\\happe\\Documents\\hukoomiTaxonomy\\taxonomyValues.properties");
            Properties p=new Properties();
            p.load(reader);

            while ((str = br.readLine()) != null) {
                dcrType = str.split("/")[2];
                //dcrType = "Persona";
                if (p.containsKey(dcrType)) {
                    taxonomyFirstVal = p.getProperty(dcrType).split(",")[0];
                    taxonomySecondVal = p.getProperty(dcrType).split(",")[1];
                    System.out.println("Taxonomy Values are: " + taxonomyFirstVal + "---" + taxonomySecondVal + "---" + taxonomyTagValue);

                    File file1 = new File("/default/main/Hukoomi/WORKAREA/default/" + str);
                    //File file1 = new File(str);
                    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                    DocumentBuilder db = dbf.newDocumentBuilder();
                    Document document = db.parse(file1);
                    document.getDocumentElement().normalize();
                    if (dcrType.equals("Persona"))
                        taxonomyTagValue = "Category";
                    else
                        taxonomyTagValue = "Key-Label";
                    NodeList nodeList = document.getElementsByTagName(taxonomyTagValue);

                    for (int itr = 0; itr < nodeList.getLength(); itr++) {
                        Node node = nodeList.item(itr);
                        System.out.println("\nNode Name :" + node.getNodeName());
                        if (node.getNodeType() == Node.ELEMENT_NODE) {
                            Element eElement = (Element) node;

                            taxonomyKey = eElement.getElementsByTagName("Value").item(0).getTextContent();
                            logger.info("Taxonomy Key : " + taxonomyKey);

                            labelEn = eElement.getElementsByTagName("LabelEn").item(0).getTextContent();
                            logger.info("Taxonomy English Label : " + labelEn);

                            labelAr = eElement.getElementsByTagName("LabelAr").item(0).getTextContent();
                            logger.info("Taxonomy Arabic Label : " + labelAr);

                            System.out.println("Taxonomy Keys: " + taxonomyKey + "---" + labelEn + "---" + labelAr);

                            if (taxonomyTagValue.equals("Category")) {
                                NodeList nodeListSub = eElement.getElementsByTagName("SubCategory");
                                for (int itrSub = 0; itrSub < nodeListSub.getLength(); itrSub++) {
                                    Node nodeSub = nodeListSub.item(itrSub);
                                    System.out.println("\nNode Name :" + nodeSub.getNodeName());
                                    if (nodeSub.getNodeType() == Node.ELEMENT_NODE) {
                                        Element eElementSub = (Element) nodeSub;

                                        taxonomyKeySub = eElementSub.getElementsByTagName("Value").item(0).getTextContent();
                                        //logger.info("Taxonomy Key : " + taxonomyKey);

                                        labelEnSub = eElementSub.getElementsByTagName("LabelEn").item(0).getTextContent();
                                        //logger.info("Taxonomy English Label : " + labelEn);

                                        labelArSub = eElementSub.getElementsByTagName("LabelAr").item(0).getTextContent();
                                        //logger.info("Taxonomy Arabic Label : " + labelAr);

                                        System.out.println("Taxonomy Sub Keys: " + taxonomyKeySub + "---" + labelEnSub + "---" + labelArSub);
                                        taxonomyMap.put(taxonomyKey + "-" + taxonomyKeySub, labelEnSub + "|" + labelArSub);
                                    }
                                }
                            } else {
                                taxonomyMap.put(taxonomyKey, labelEn + "|" + labelAr);
                            }

                        }
                    }

                    //fileList = listf("C:\\Users\\happe\\Documents\\hukoomiTaxonomy\\Content");
                    fileList = listf("/default/main/Hukoomi/WORKAREA/default/templatedata/Content");

                    //File dirCheck = null;
                    String fileStr = "";
                    for (File fileListStr : fileList) {
                        fileStr = fileListStr.getAbsolutePath();
                        System.out.println("Content DCR File: " + fileStr);
                        if (fileListStr.isFile() && !fileStr.equalsIgnoreCase("datacapture.cfg")) {
                            brContent = new BufferedReader(new FileReader(fileStr));
                            while ((strContent = brContent.readLine()) != null) {
                                if (strContent.contains("<" + taxonomyFirstVal + ">" + "<" + taxonomySecondVal + ">")) {
                                    Pattern pattern = Pattern.compile("<" + taxonomyFirstVal + "><" + taxonomySecondVal + ">(.*)</" + taxonomySecondVal + "><value>(.*)</value><label-en>(.*)</label-en><label-ar>(.*)</label-ar></" + taxonomyFirstVal + ">", Pattern.CASE_INSENSITIVE);
                                    Matcher matcher = pattern.matcher(strContent);
                                    String[] valueStrArray = null;
                                    if (matcher.find()) {
                                        String keyStr = matcher.group(1);
                                        String valueStr = matcher.group(2);
                                        String enStr = matcher.group(3);
                                        String arStr = matcher.group(4);
                                        System.out.println("Matcher Values: " + keyStr + "---" + valueStr + "---" + enStr + "---" + arStr);
                                        Pattern pattern1 = null;
                                        Matcher matcher1 = null;
                                        for (String key : taxonomyMap.keySet()) {
                                            String keyEn = taxonomyMap.get(key).split("\\|")[0];
                                            String keyAr = taxonomyMap.get(key).split("\\|")[1];
                                            String keyValueSet = "value:" + key + "|lab-en:" + keyEn + "|lab-ar:" + keyAr;
                                            System.out.println("Key Value: " + keyValueSet + "---" + valueStr + "---" + key);
                                            if (valueStr.contains(key)) {
                                                String enValue = "", arValue = "";
                                                if (valueStr.contains(",")) {
                                                    valueStrArray = valueStr.split(",");
                                                    if (valueStrArray.length > 0) {
                                                        for (String strArrayElement : valueStrArray) {
                                                            System.out.println("strArrayElementValue: " + strArrayElement + "---" + key);
                                                            if (strArrayElement.trim().equals(key)) {
                                                                System.out.println("keyStrValue :" + keyStr + "---" + keyValueSet);
                                                                if (!keyStr.contains(keyValueSet)) {
                                                                    //replace code
                                                                    String[] keyStrArray;
                                                                    if (keyStr.contains(",")) {
                                                                        keyStrArray = keyStr.split(",");
                                                                        for (String keyStrArrayElement : keyStrArray) {
                                                                            System.out.println("keyStrArrayElementValue: " + keyStrArrayElement);
                                                                            if (keyStrArrayElement.contains(key) && !(keyStrArrayElement.trim().equals(keyStr))) {
                                                                                pattern1 = Pattern.compile("value:" + key + "\\|lab-en:(.*)\\|lab-ar:(.*)", Pattern.CASE_INSENSITIVE);
                                                                                matcher1 = pattern1.matcher(keyStrArrayElement);
                                                                                if (matcher1.find()) {
                                                                                    enValue = matcher1.group(1);
                                                                                    arValue = matcher1.group(2);
                                                                                }
                                                                                System.out.println("enValue: " + enValue + " arValue: " + arValue);
                                                                                String oldString = "<" + taxonomyFirstVal + "><" + taxonomySecondVal + ">" + keyStr + "</" + taxonomySecondVal + "><value>" + valueStr + "</value><label-en>" + enStr + "</label-en><label-ar>" + arStr + "</label-ar></" + taxonomyFirstVal + ">";
                                                                                keyStr = keyStr.replace(enValue, keyEn).replace(arValue, keyAr);
                                                                                enStr = enStr.replace(enValue, keyEn);
                                                                                arStr = arStr.replace(arValue, keyAr);
                                                                                String finalString = "<" + taxonomyFirstVal + "><" + taxonomySecondVal + ">" + keyStr + "</" + taxonomySecondVal + "><value>" + valueStr + "</value><label-en>" + enStr + "</label-en><label-ar>" + arStr + "</label-ar></" + taxonomyFirstVal + ">";
                                                                                replaceString(fileStr, oldString, finalString);
                                                                                deployFileWriter(fileStr);
                                                                                break;
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                                break;
                                                            }
                                                        }
                                                    }
                                                } else if (valueStr.equals(key)) {
                                                    if (!keyStr.equals(keyValueSet)) {
                                                        //replace code
                                                        System.out.println("Key String Values: " + key + "---" + keyStr + "---" + keyValueSet);
                                                        pattern1 = Pattern.compile("value:" + key + "\\|lab-en:(.*)\\|lab-ar:(.*)", Pattern.CASE_INSENSITIVE);
                                                        matcher1 = pattern1.matcher(keyStr);
                                                        if (matcher1.find()) {
                                                            enValue = matcher1.group(1);
                                                            arValue = matcher1.group(2);
                                                        }
                                                        System.out.println("enValue: " + enValue + " arValue: " + arValue);
                                                        System.out.println("labelEn: " + labelEn + " labelAr: " + labelAr);
                                                        String oldString = "<" + taxonomyFirstVal + "><" + taxonomySecondVal + ">" + keyStr + "</" + taxonomySecondVal + "><value>" + valueStr + "</value><label-en>" + enStr + "</label-en><label-ar>" + arStr + "</label-ar></" + taxonomyFirstVal + ">";
                                                        String finalString = "<" + taxonomyFirstVal + "><" + taxonomySecondVal + ">" + keyStr.replace(enValue, keyEn).replace(arValue, keyAr) + "</" + taxonomySecondVal + "><value>" + valueStr + "</value><label-en>" + enStr.replace(enValue, keyEn) + "</label-en><label-ar>" + arStr.replace(arValue, keyAr) + "</label-ar></" + taxonomyFirstVal + ">";
                                                        replaceString(fileStr, oldString, finalString);
                                                        deployFileWriter(fileStr);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            br.close();
            reader.close();
            brContent.close();
            //PrintWriter writer = new PrintWriter("C:\\Users\\happe\\Documents\\hukoomiTaxonomy\\taxonomyFilesPublished.txt");
            PrintWriter writer = new PrintWriter("/usr/opentext/TeamSite/tmp/taxonomyFilesPublished.txt");
            writer.print("");
            writer.close();
        } catch (FileNotFoundException e) {
            System.out.println("FileNotFoundException: "+e);
        } catch (IOException e) {
            System.out.println("IOException: "+e);
        } catch (ParserConfigurationException e) {
            System.out.println("ParserConfigurationException: "+e);
        } catch (SAXException e) {
            System.out.println("SAXException: "+e);
        } catch (Exception e){
            System.out.println("Exception: "+e);
        }
    }*/
    private static void replaceString(String fileName, String oldStr, String rplStr){
        File DcrFile = new File(fileName);
        String s;
        String totalStr = "";
        System.out.println("replaceString funtion string: "+fileName+"---"+oldStr+"---"+rplStr);
        try
        {
            FileReader fr = new FileReader(DcrFile);
            BufferedReader br = new BufferedReader(fr);
            while ((s = br.readLine()) != null) {
                s = s.replace(oldStr, rplStr);
                if (totalStr == "")
                    totalStr = s;
                else
                    totalStr = totalStr+"\n"+s;
            }
            FileWriter fw = new FileWriter(fileName);
            fw.write(totalStr);
            fw.close();
            br.close();
        } catch (FileNotFoundException ex) {
            System.out.println("FileNotFoundException: "+ex);
        } catch(IOException ex) {
            System.out.println("IOException"+ex);
        } catch (Exception ex) {
            System.out.println("Exception"+ex);
        }
    }

    private static void deployFileWriter(String fileName){

        String currentLine;
        Boolean scanFlag = true;

        try{
            FileWriter writer = new FileWriter("/usr/opentext/TeamSite/tmp/taxonomyFilesDeploy.txt", true);
            //FileWriter writer = new FileWriter("C:\\Users\\happe\\Documents\\hukoomiTaxonomy\\taxonomyFilesDeploy.txt", true);
            BufferedWriter bufferedWriter = new BufferedWriter(writer);

            //File file = new File("C:\\Users\\happe\\Documents\\hukoomiTaxonomy\\taxonomyFilesDeploy.txt");
            File file = new File("/usr/opentext/TeamSite/tmp/taxonomyFilesDeploy.txt");
            Scanner scanner = new Scanner(file);

            while (scanner.hasNext()) {
                currentLine = scanner.nextLine();
                if (currentLine.trim().equals(fileName)) {
                    scanFlag = false;
                    break;
                }
            }
            if (scanFlag) {
                bufferedWriter.write(fileName);
                bufferedWriter.newLine();
            }
            bufferedWriter.close();
            writer.close();
        }catch (IOException e) {
            System.out.println("IOException"+e);
        }catch (Exception e){
            System.out.println("Exception"+e);
        }
    }

    private static List<File> listf(String directoryName) {
        File directory = new File(directoryName);
        List<File> resultList = new ArrayList<File>();

        File[] fList = directory.listFiles();
        resultList.addAll(Arrays.asList(fList));
        for (File file : fList) {
            if (file.isFile()) {
                System.out.println("fileName with Path: "+file.getAbsolutePath());
            } else if (file.isDirectory()) {
                resultList.addAll(listf(file.getAbsolutePath()));
            }
        }
        return resultList;
    }
}