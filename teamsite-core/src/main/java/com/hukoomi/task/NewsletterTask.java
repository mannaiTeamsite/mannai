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
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.activation.DataSource;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;

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

public class NewsletterTask implements CSURLExternalTask {

    /**
     * Logger object to check the flow of the code.
     */
    private final Logger logger = Logger.getLogger(NewsletterTask.class);
    /**
     * XPath to the newsletter id
     */
    public static final String ID_PATH = "/root/information/id";
    /**
     * XPath to the language selection
     */
    public static final String LANG_PATH = "/root/information/language/value";
    /**
     * XPath to the Title path
     */
    public static final String TITLE_PATH = "/root/detail/newsletter-title";
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
    public static final String SUCCESS_TRANSITION = "Newsletter HTML Generation Success";
    /**
     * Success transition comment
     */
    public static final String SUCCESS_TRANSITION_COMMENT = "Newsletter HTML Generated Successfully";
    /**
     * DCR Type Meta data name
     */
    public static final String META_DATA_NAME_DCR_TYPE = "TeamSite/Templating/DCR/Type";

    public static final String NEWSLETTER_DESCRIPTION_PATH = "/root/detail/newsletter-description";
    public static final String NEWSLETTER_CATEGOTY_PATH = "/root/detail/newsletter-category/newsletter-category-field";
    public static final String TEMPLATE = "newsletter_template";
    public static final String HTML_PATH = "html_path";
    public static final String HTML_GENERATION_LOCATION = "generate_html_location";
    public static final String MAIL_ENCODING = "UTF-8";
    public static final String MAIL_MIME_TYPE = "text/html";
    Properties properties;
    String lang = "";
    String title = "";

    String genrateHtmlLocation = "";
    String htmlTemplatePath = "";

    @Override
    public void execute(CSClient client, CSExternalTask task,
            Hashtable params) throws CSException {
        logger.info("NewsletterTask: execute()");
        Map<String, String> statusMap = null;

        statusMap = new HashMap<>();

        CSAreaRelativePath[] taskFileList = task.getFiles();
        logger.debug("TaskFileList Length : " + taskFileList.length);

        for (CSAreaRelativePath taskFilePath : taskFileList) {
            CSFile file = task.getArea().getFile(taskFilePath);
            String fileName = file.getName();
            logger.debug("File Name : " + fileName);

            CSSimpleFile taskSimpleFile = (CSSimpleFile) file;
            String dcrType = taskSimpleFile
                    .getExtendedAttribute(META_DATA_NAME_DCR_TYPE)
                    .getValue();
            logger.info("DCR Type : " + dcrType);
            if ("Content/Newsletter".equals(dcrType)) {
                statusMap = processNewsletterDCR(client, task,
                        taskSimpleFile);
            }
        }

        task.chooseTransition(statusMap.get(TRANSITION),
                statusMap.get(TRANSITION_COMMENT));
    }

    public Map<String, String> processNewsletterDCR(CSClient client,
            CSExternalTask task, CSSimpleFile taskSimpleFile)
            throws CSAuthorizationException, CSExpiredSessionException,
            CSRemoteException, CSException {
        logger.info("NewsletterTask: processNewsletterDCR()");
        // get xsl
        HashMap<String, String> statusMap = new HashMap<>();
        loadProperties(client, task, "newsletter.properties");
        String strTemplate = properties.getProperty(TEMPLATE);
        logger.info("strTemplate : " + strTemplate);

        htmlTemplatePath = properties.getProperty(HTML_PATH);
        logger.info("htmlPath : " + htmlTemplatePath);

        genrateHtmlLocation = properties
                .getProperty(HTML_GENERATION_LOCATION);
        logger.info("htmlPath : " + genrateHtmlLocation);

        CSVPath templateVpath = new CSVPath(strTemplate);
        CSSimpleFile xslTemplateFile = (CSSimpleFile) (client
                .getFile(templateVpath));

        Document data = DocumentHelper.createDocument();

        logger.info("Data : " + data.asXML());

        Element rootElement = data.addElement("root");

        // title

        Document document = getTaskDocument(taskSimpleFile);
        title = document.selectSingleNode(TITLE_PATH).getText();
        logger.info("title - " + title);
        Element titleElement = rootElement.addElement("title");
        titleElement.setText(title);
        // desc
        String desc = document
                .selectSingleNode(NEWSLETTER_DESCRIPTION_PATH).getText();
        logger.info("desc - " + desc);
        Element descElement = rootElement.addElement("description");
        descElement.setText(desc);
        // lang
        lang = document.selectSingleNode(LANG_PATH).getText();
        Element langElement = rootElement.addElement("lang");
        logger.info("lang - " + lang);
        langElement.setText(lang);

        List<Node> categoryList = document
                .selectNodes(NEWSLETTER_CATEGOTY_PATH);

        for (Node node : categoryList) {
            Element categoryElement = rootElement.addElement("category");
            String dctType = node.selectSingleNode("category").getText();
            // DCT Type
            Element dctTypeElement = categoryElement.addElement("dctType");
            dctTypeElement.setText(dctType);
            // Category Title
            String dctTitle = node
                    .selectSingleNode("newsletter-category-title")
                    .getText();
            Element dctTitleElement = categoryElement
                    .addElement("categorytitle");
            dctTitleElement.setText(dctTitle);
            logger.info("dctTitle - " + dctTitle);

            Element categoryDcrElement = categoryElement
                    .addElement("category-dcr");
            List<Node> category = node.selectNodes("category-dcr");
            int dcrCount = category.size();
            Element dcrCountElement = categoryDcrElement
                    .addElement("dcrCount");
            dcrCountElement.setText(String.valueOf(dcrCount));

            for (Node dcr : category) {

                Element dcrElement = categoryDcrElement.addElement("dcr");
                String dcrPath = dcr.getText();
                logger.info("dcrPath - " + dcrPath);

                Document dcrDocument = getDcrDocument(client, task,
                        dcrPath);
                Element dcrTitleElement = dcrElement.addElement("title");
                String dcrTitle = dcrDocument
                        .selectSingleNode("/root/information/title")
                        .getText();
                logger.info("dcrTitle - " + dcrTitle);
                dcrTitleElement.setText(dcrTitle);

                Element dcrDescElement = dcrElement.addElement("desc");
                String dcrDescription = dcrDocument
                        .selectSingleNode("/root/detail/description")
                        .getText();
                dcrDescElement.setText(dcrDescription);

                if ("Events".equals(dctType)) {
                    Element dcrOrgElement = dcrElement
                            .addElement("organizer");
                    String dcrorganaizer = "";
                    if (lang.equals("ar")) {
                        Node organizer = dcrDocument.selectSingleNode(
                                "/root/settings/organizers/label-ar");
                        if (organizer != null) {
                            dcrorganaizer = organizer.getText();
                        }
                    }
                    Node organizer = dcrDocument.selectSingleNode(
                            "/root/settings/organizers/label-en");
                    if (organizer != null) {
                        dcrorganaizer = organizer.getText();
                    }

                    dcrOrgElement.setText(dcrorganaizer);

                    Element dateElement = dcrElement.addElement("date");

                    String dcrDate = dcrDocument
                            .selectSingleNode(
                                    "/root/event-date/start-date")
                            .getText();

                    dateElement.setText(dcrDate);

                    Element imgElement = dcrElement.addElement("image");
                    String dcrImage = dcrDocument
                            .selectSingleNode("/root/information/image")
                            .getText();
                    imgElement.setText(dcrImage);

                    Element locationNameElement = dcrElement
                            .addElement("locationName");
                    Node location = dcrDocument.selectSingleNode(
                            "/root/location/location-name");
                    String locationName = "";
                    if (location != null) {
                        locationName = location.getText();
                    }
                    locationNameElement.setText(locationName);

                    Element readMoreLink = dcrElement
                            .addElement("readMore");
                    String originalDcrName = "/" + lang + "/event/"
                            + dcrDocument.selectSingleNode(
                                    "/root/information/original-dcr-name")
                                    .getText();
                    readMoreLink.setText(originalDcrName);

                } else if ("News".equals(dctType)) {
                    Element sourceElement = dcrElement
                            .addElement("source");
                    String source = "";
                    if (lang.equals("ar")) {
                        Node sourceEle = dcrDocument.selectSingleNode(
                                "/root/settings/channels/label-ar");
                        if (sourceEle != null) {
                            source = sourceEle.getText();
                        }

                    }
                    Node sourceEle = dcrDocument.selectSingleNode(
                            "/root/settings/channels/label-en");
                    if (sourceEle != null) {
                        source = sourceEle.getText();
                    }

                    sourceElement.setText(source);

                    Element imgElement = dcrElement.addElement("image");
                    String dcrImage = dcrDocument
                            .selectSingleNode("/root/information/image")
                            .getText();
                    imgElement.setText(dcrImage);

                    Element dateElement = dcrElement.addElement("date");
                    String dcrDate = dcrDocument
                            .selectSingleNode("/root/information/date")
                            .getText();
                    dateElement.setText(dcrDate);

                    Element readMoreLink = dcrElement
                            .addElement("readMore");
                    String originalDcrName = "/" + lang + "/news/"
                            + dcrDocument.selectSingleNode(
                                    "/root/information/original-dcr-name")
                                    .getText();

                    readMoreLink.setText(originalDcrName);

                } else if ("Blog".equals(dctType)) {

                    Element dateElement = dcrElement.addElement("date");
                    String dcrDate = dcrDocument
                            .selectSingleNode("/root/information/date")
                            .getText();
                    dateElement.setText(dcrDate);

                    Element readMoreLink = dcrElement
                            .addElement("readMore");
                    String originalDcrName = "/" + lang + "/blog/"
                            + dcrDocument.selectSingleNode(
                                    "/root/information/original-dcr-name")
                                    .getText();

                    readMoreLink.setText(originalDcrName);
                } else if ("Articles".equals(dctType)) {

                    Element dateElement = dcrElement.addElement("date");
                    String dcrDate = dcrDocument
                            .selectSingleNode("/root/information/date")
                            .getText();
                    dateElement.setText(dcrDate);

                    Element topicElement = dcrElement.addElement("topics");
                    String topics = "";
                    if (lang.equals("ar")) {
                        Node topicsEle = dcrDocument.selectSingleNode(
                                "/root/settings/topics/label-ar");
                        if (topicsEle != null) {
                            topics = topicsEle.getText();
                        }
                    }
                    Node topicsEle = dcrDocument.selectSingleNode(
                            "/root/settings/topics/label-en");
                    if (topicsEle != null) {
                        topics = topicsEle.getText();
                    }



                    topicElement.setText(topics);

                    Element readMoreLink = dcrElement
                            .addElement("readMore");
                    String originalDcrName = "/" + lang + "/article/"
                            + dcrDocument.selectSingleNode(
                                    "/root/information/original-dcr-name")
                                    .getText();

                    readMoreLink.setText(originalDcrName);
                } else if ("Services".equals(dctType)) {
                    Element serviceModeElement = dcrElement
                            .addElement("serviceMode");
                    String serviceMode = "";
                    Element serviceProviderElement = dcrElement
                            .addElement("serviceProvider");
                    String serviceProvider = "";
                    if (lang.equals("ar")) {
                        Node serviceModeEle = dcrDocument.selectSingleNode(
                                "/root/settings/service-mode/label-ar");
                        if (serviceModeEle != null) {
                            serviceMode = serviceModeEle.getText();
                        }

                        Node serviceProviderEle = dcrDocument
                                .selectSingleNode(
                                        "/root/settings/service-entities/label-ar");
                        if (serviceProviderEle != null) {
                            serviceProvider = serviceProviderEle.getText();
                        }
                    }

                    Node serviceModeEle = dcrDocument.selectSingleNode(
                            "/root/settings/service-mode/label-en");
                    if (serviceModeEle != null) {
                        serviceMode = serviceModeEle.getText();
                    }

                    Node serviceProviderEle = dcrDocument.selectSingleNode(
                            "/root/settings/service-entities/label-en");
                    if (serviceProviderEle != null) {
                        serviceProvider = serviceProviderEle.getText();
                    }

                    serviceModeElement.setText(serviceMode);
                    serviceProviderElement.setText(serviceProvider);

                    Element readMoreLink = dcrElement
                            .addElement("readMore");
                    String originalDcrName = "/" + lang + "/service/"
                            + dcrDocument.selectSingleNode(
                                    "/root/information/original-dcr-name")
                                    .getText();

                    readMoreLink.setText(originalDcrName);
                }
            }
        }

        if (data != null) {
            DataSource mailDataSource = null;
            try {
                logger.info("data not null.");

                logger.debug("Data : " + data.asXML());
                transformToMailDataSource(data.asXML(), xslTemplateFile);

                String htmlfilePath = htmlTemplatePath + lang + "/" + title
                        + ".html";

                logger.debug("filePath1 : " + htmlfilePath);

                CSAreaRelativePath reportFile2 = new CSAreaRelativePath(
                        htmlfilePath);
                CSAreaRelativePath[] files2 = new CSAreaRelativePath[1];
                files2[0] = reportFile2;
                task.attachFiles(files2);

                statusMap.put(TRANSITION, SUCCESS_TRANSITION);
                statusMap.put(TRANSITION_COMMENT,
                        SUCCESS_TRANSITION_COMMENT);

            } catch (Exception ex) {
                logger.error("Exception in attach: ", ex);
            }
        }
        return statusMap;
    }

    private DataSource transformToMailDataSource(String xmlMailContent,
            CSSimpleFile xslTemplateFile)
            throws CSException, UnsupportedEncodingException,
            FileNotFoundException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            logger.info("transformToMailDataSource");
            StringBuffer baseDir = new StringBuffer(genrateHtmlLocation)
                    .append(lang);
            logger.info("baseDir : " + baseDir);

            Path directory = Path.of(baseDir.toString());

            String baseFilePermission = properties
                    .getProperty("filePermissions", "rwxr-xr-x");
            Set<PosixFilePermission> baseFilePermissions = PosixFilePermissions
                    .fromString(baseFilePermission);

            String baseDirPermissions = properties
                    .getProperty("dirPermissions", "rwxr-xr-x");

            logger.info("Permission for file : " + baseFilePermission);
            logger.info(
                    "Permission for directory : " + baseDirPermissions);

            Set<PosixFilePermission> baseDirPermission = PosixFilePermissions
                    .fromString(baseDirPermissions);
            if (Files.notExists(directory)) {
                Files.createDirectory(directory, PosixFilePermissions
                        .asFileAttribute(baseDirPermission));
            }

            File fout = new File(baseDir + "/" + title + ".html");
            fout.createNewFile();

            FileOutputStream oFile = new FileOutputStream(fout, false);
            logger.info("xmlMailContent- " + xmlMailContent);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(
                    xmlMailContent.getBytes(MAIL_ENCODING));
            logger.info("before outputStream- " + outputStream.toString());
            XSLTransformer.transform(inputStream, xslTemplateFile,
                    outputStream);
            logger.info("After outputStream- " + outputStream.toString());
            outputStream.writeTo(oFile);
            Files.setPosixFilePermissions(
                    Path.of(directory + "/" + title + ".html"),
                    baseFilePermissions);

        } catch (IOException ex) {
            logger.info("Exception in transformToMailDataSource: ", ex);
        }
        catch (Exception e) {
            logger.info("Exception in transformToMailDataSource: ", e);
        }
        return new javax.mail.util.ByteArrayDataSource(
                outputStream.toByteArray(), MAIL_MIME_TYPE);

    }

    /**
     * Method to get the task file as a xml document.
     *
     * @param taskSimpleFile
     *                       Task file of CSSimpleFile object
     * @return Returns xml document of the task file.
     */
    public Document getTaskDocument(CSSimpleFile taskSimpleFile) {
        logger.debug("NewsletterTask: getTaskDocument");
        Document document = null;
        try {
            byte[] taskSimpleFileByteArray = taskSimpleFile.read(0, -1);
            String taskSimpleFileString = new String(
                    taskSimpleFileByteArray);
            logger.debug("taskSimpleFileString : " + taskSimpleFileString);
            document = DocumentHelper.parseText(taskSimpleFileString);
            logger.debug("document : " + document.asXML());
        } catch (Exception e) {
            logger.error("Exception in getTaskDocument: ", e);
        }
        return document;
    }

    public Document getDcrDocument(CSClient client, CSExternalTask task,
            String dcr) throws CSAuthorizationException {
        logger.debug(" getDcrDocument");
        Document document = null;
        try {
            String vPath = task.getArea().getRootDir().getVPath()
                    .toString();
            logger.debug("vPath : " + vPath);

            if (vPath != null && !vPath.equals("")) {
                String dcrPath = vPath + dcr;
                logger.debug("FilePath : " + dcrPath);
                CSSimpleFile dcrfile = (CSSimpleFile) client
                        .getFile(new CSVPath(dcrPath));
                document = getTaskDocument(dcrfile);

            }
        } catch (Exception e) {
            logger.error("Exception in getDcrDocument: ", e);
        }
        return document;
    }

    private void loadProperties(final CSClient client,
            final CSExternalTask task, final String propertyFileName) {
        TSPropertiesFileReader propFileReader = new TSPropertiesFileReader(
                client, task, propertyFileName);
        properties = propFileReader.getPropertiesFile();
    }

}
