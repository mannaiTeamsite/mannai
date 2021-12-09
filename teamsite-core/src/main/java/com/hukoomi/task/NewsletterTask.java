package com.hukoomi.task;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import com.hukoomi.utils.TSPropertiesFileReader;
import com.interwoven.cssdk.access.CSAuthorizationException;
import com.interwoven.cssdk.common.CSClient;
import com.interwoven.cssdk.common.CSException;
import com.interwoven.cssdk.filesys.CSAreaRelativePath;
import com.interwoven.cssdk.filesys.CSFile;
import com.interwoven.cssdk.filesys.CSHole;
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
	 * XPath to the filename selection
	 */
	public static final String HTML_FILE_NAME = "/root/information/original-dcr-name";

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
	 * Title string
	 */
	public static final String TITLE = "title";
	/**
	 * column count string
	 */
	public static final String COLUMN_LAYOUT = "column-layout";
	
	/**
	 * column count string
	 */
	public static final String HTML = ".html";
	/**
	 * category-dcr string
	 */
	public static final String CATEGORY_DCR = "category-dcr";
	/**
	 * Lang string
	 */
	public static final String LANG = "_lang_";
	/**
	 * Category string
	 */
	public static final String CATEGORY = "category";
	/**
	 * delimiter string
	 */
	public static final String DELIMITER = "/";
	/**
	 * Success transition comment
	 */
	public static final String SUCCESS_TRANSITION_COMMENT = "Newsletter HTML Generated Successfully";
	/**
	 * DCR Type Meta data name
	 */
	public static final String META_DATA_NAME_DCR_TYPE = "TeamSite/Templating/DCR/Type";

	/**
	 * Charset for urlencoding
	 */
	private static final String CHARSET = StandardCharsets.UTF_8.name();

	private static final String[][] CHARACTERS = { { "\\+", "%20" }, { "%21", "!" }, { "%27", "'" }, { "%28", "(" },
			{ "%29", ")" }, { "%7E", "~" } };

	public static final String NEWSLETTER_DESCRIPTION_PATH = "/root/detail/newsletter-description";
	public static final String NEWSLETTER_ROOT_CATEGOTY_PATH = "/root/detail/newsletter-category";
	public static final String NEWSLETTER_CATEGOTY_PATH = "/root/detail/newsletter-category/newsletter-category-field";
	public static final String OTHER_NEWSLETTER_CATEGOTY_PATH = "/root/detail/newsletter-category/newsletter-other";
	public static final String TEMPLATE = "newsletter_template";
	public static final String HTML_PATH = "html_path";
	public static final String HTML_GENERATION_LOCATION = "generate_html_location";
	public static final String MAIL_MIME_TYPE = "text/html";
	Properties properties;

	String titleStr = "";
	String htmlName = "";
	String genrateHtmlLocation = "";
	String htmlTemplatePath = "";
	String baseUrl = "";
	String dcrName = "";
	String taxanomyName = ""; // Added to get Taxanomy Labels
    String taxanomybaseUrl = ""; // Added to get Taxanomy Labels
    public static final String NEWSLETTER_LIST_CONTENT_TYPE = "/master/master-data/Key-Label";// Added to get Taxanomy Labels
    
	@Override
	public void execute(CSClient client, CSExternalTask task, Hashtable params) throws CSException {
		logger.info("NewsletterTask: execute()");
		Map<String, String> statusMap = null;

		statusMap = new HashMap<>();

		CSAreaRelativePath[] taskFileList = task.getFiles();
		logger.debug("TaskFileList Length : " + taskFileList.length);

		for (CSAreaRelativePath taskFilePath : taskFileList) {
			CSFile file = task.getArea().getFile(taskFilePath);

			if (file.getKind() != CSHole.KIND) {
				String fileName = file.getName();
				logger.debug("File Name : " + fileName);

				CSSimpleFile taskSimpleFile = (CSSimpleFile) file;
				String dcrType = taskSimpleFile.getExtendedAttribute(META_DATA_NAME_DCR_TYPE).getValue();
				logger.info("DCR Type : " + dcrType);
				if ("Content/Newsletter".equals(dcrType)) {
					statusMap = processNewsletterDCR(client, task, taskSimpleFile, fileName);
				} else {
					statusMap.put(TRANSITION, SUCCESS_TRANSITION);
					statusMap.put(TRANSITION_COMMENT, SUCCESS_TRANSITION_COMMENT);
				}

			} else {
				statusMap.put(TRANSITION, SUCCESS_TRANSITION);
				statusMap.put(TRANSITION_COMMENT, SUCCESS_TRANSITION_COMMENT);

			}

		}

		task.chooseTransition(statusMap.get(TRANSITION), statusMap.get(TRANSITION_COMMENT));
	}

	public Map<String, String> processNewsletterDCR(CSClient client, CSExternalTask task, CSSimpleFile taskSimpleFile,
			String fileName)
			throws CSException {
		logger.info("NewsletterTask: processNewsletterDCR()");
		// get xsl
		HashMap<String, String> statusMap = new HashMap<>();
		loadProperties(client, task, "newsletter.properties");
		String strTemplate = properties.getProperty(TEMPLATE);
		logger.info("strTemplate : " + strTemplate);

		htmlTemplatePath = properties.getProperty(HTML_PATH);
		logger.info("htmlPath : " + htmlTemplatePath);

		genrateHtmlLocation = properties.getProperty(HTML_GENERATION_LOCATION);
		logger.info("genrateHtmlLocation : " + genrateHtmlLocation);

		baseUrl = properties.getProperty("labels_dcr_base_url");
		logger.info("baseUrl : " + baseUrl);

		dcrName = properties.getProperty("labels_dcr_name");
		logger.info("dcrName : " + dcrName);
		taxanomybaseUrl = properties.getProperty("taxanomy_dcr_base_url");
        logger.info("taxanomybaseUrl : " + taxanomybaseUrl);

        taxanomyName = properties.getProperty("labels_taxanomy_name");
        logger.info("taxanomyName : " + taxanomyName);

        String labelDCRPath = baseUrl + dcrName;

        String taxanomyDCRPath = taxanomybaseUrl + taxanomyName;
		// Social Media Links
		String facebook = properties.getProperty("facebook_link");
		String twitter = properties.getProperty("twitter_link");
		String instagram = properties.getProperty("instagram_link");
		String linkedin = properties.getProperty("linkedin_link");
		String youtube = properties.getProperty("youtube_link");

		// Footer Links
		String contactUsLink = properties.getProperty("contact_us_link");
		String privacyPolicyLink = properties.getProperty("privacy_policy_link");
		String newsletterUnsubscribeLink = properties.getProperty("newsletter_unsubscribe_link");

		// Base URl
		String baseUrlLink = properties.getProperty("base_url");

		// Newsletter link
		String newsletterpath = properties.getProperty("web_path");
		String shareitfb = properties.getProperty("shareit_fb");
		String shareittwitter = properties.getProperty("shareit_twitter");
		String shareitwhatsapp = properties.getProperty("shareit_whatsapp");
		String shareitlinkedin = properties.getProperty("shareit_linkedin");

		int maxChar = Integer.parseInt(properties.getProperty("max_char"));

		String encodedNewsletterUrl = encodeURIComponent(baseUrlLink + newsletterpath + fileName + HTML);

		String nlllinkFB = shareitfb + encodedNewsletterUrl;
		String nlltwitter = shareittwitter + encodedNewsletterUrl;
		String nllwhatsapp = shareitwhatsapp + encodedNewsletterUrl;
		String nlllinkedin = shareitlinkedin + encodedNewsletterUrl;

		logger.info("nlllinkedin : " + nlllinkedin);

		CSVPath templateVpath = new CSVPath(strTemplate);
		CSSimpleFile xslTemplateFile = (CSSimpleFile) (client.getFile(templateVpath));

		Document data = DocumentHelper.createDocument();

		logger.info("Data : " + data.asXML());

		Element rootElement = data.addElement("root");

		Element baseUrlElement = rootElement.addElement("baseURL");
		baseUrlElement.setText(baseUrlLink);

		Document labels = getLabelDCRFile(labelDCRPath);
		if(labels != null) {
			rootElement.add(labels.getRootElement());
		}
		

		// Add Social Media Links To Document
		Element socialMediaElement = rootElement.addElement("social-media");

		Element facebookLinkElement = socialMediaElement.addElement("facebook");
		facebookLinkElement.setText(facebook);

		Element twitterLinkElement = socialMediaElement.addElement("twitter");
		twitterLinkElement.setText(twitter);

		Element instagramLinkElement = socialMediaElement.addElement("instagram");
		instagramLinkElement.setText(instagram);

		Element linkedinLinkElement = socialMediaElement.addElement("linkedin");
		linkedinLinkElement.setText(linkedin);

		Element youtubeLinkElement = socialMediaElement.addElement("youtube");
		youtubeLinkElement.setText(youtube);

		Document document = getTaskDocument(taskSimpleFile);

		// HTML filename
		htmlName = document.selectSingleNode(HTML_FILE_NAME).getText();
		logger.info("HTML File Name : " + htmlName);

		titleStr = document.selectSingleNode(TITLE_PATH).getText();
		logger.info("title - " + titleStr);
		Element titleElement = rootElement.addElement(TITLE);
		titleElement.setText(titleStr);
		// desc
		String desc = document.selectSingleNode(NEWSLETTER_DESCRIPTION_PATH).getText();
		logger.info("desc - " + desc);
		Element descElement = rootElement.addElement("description");
		descElement.setText(desc);
		// lang
		String lang = document.selectSingleNode(LANG_PATH).getText();
		Element langElement = rootElement.addElement("lang");
		logger.info("lang - " + lang);
		langElement.setText(lang);

		// Add Footer Links To Document
		Element footerLinksElement = rootElement.addElement("footer-links");

		Element contactUsLinkElement = footerLinksElement.addElement("contactUs");
		contactUsLinkElement.setText(contactUsLink.replace(LANG, lang));

		Element privacyPolicyLinkElement = footerLinksElement.addElement("privacyPolicy");
		privacyPolicyLinkElement.setText(privacyPolicyLink.replace(LANG, lang));

		Element newsletterUnsubscribeLinkElement = footerLinksElement.addElement("newsletterUnsubscribe");
		newsletterUnsubscribeLinkElement.setText(newsletterUnsubscribeLink.replace(LANG, lang));

		// Add Share Links To document starts
		Element shareLinksElement = rootElement.addElement("share-links");

		Element shareFBLinkElement = shareLinksElement.addElement("facebook");
		shareFBLinkElement.setText(nlllinkFB.replace(LANG, lang));

		Element shareTTLinkElement = shareLinksElement.addElement("twitter");
		shareTTLinkElement.setText(nlltwitter.replace(LANG, lang));

		Element shareWPLinkElement = shareLinksElement.addElement("whatsapp");
		shareWPLinkElement.setText(nllwhatsapp.replace(LANG, lang));

		Element shareLILinkElement = shareLinksElement.addElement("linkedin");
		shareLILinkElement.setText(nlllinkedin.replace(LANG, lang));

		// Add Share Links To document ends

		// Add Categories
		List<Node> categoryList = document.selectNodes(NEWSLETTER_ROOT_CATEGOTY_PATH);

		for (int i = 0; i < categoryList.size(); i++) {
			Node selectnode = categoryList.get(i);
			if (selectnode.selectSingleNode("newsletter-category-field") != null) {
				Node node = selectnode.selectSingleNode("newsletter-category-field");
				Element categoryElement = rootElement.addElement(CATEGORY);
				String dctType = node.selectSingleNode(CATEGORY).getText();
				logger.info("dctType - " + dctType);
				// DCT Type
				Element dctTypeElement = categoryElement.addElement("dctType");
				dctTypeElement.setText(dctType);

				// Category Title
				String dctTitle = node.selectSingleNode("newsletter-category-title").getText();
				if (dctTitle != null) {
					Element dctTitleElement = categoryElement.addElement("categorytitle");
					dctTitleElement.setText(dctTitle);
				}
				logger.info("dctTitle - " + dctTitle);

				// Column Layout
				Element columnLayoutElement = categoryElement.addElement(COLUMN_LAYOUT);
				String dctColumnLayout = node.selectSingleNode(COLUMN_LAYOUT).getText();
				int columnLayout = 0;
				if (StringUtils.isNotBlank(dctColumnLayout)) {
					logger.info("Column Layout - " + dctColumnLayout);
					columnLayoutElement.setText(dctColumnLayout);
					columnLayout = Integer.parseInt(dctColumnLayout);
				}

				List<Node> category = node.selectNodes(CATEGORY_DCR);
				int dcrCount = category.size();
				logger.info("dcrCount Value : " + dcrCount);
				int dcrDivCol = 0;
				if(columnLayout>0)
				 dcrDivCol = dcrCount / columnLayout;
				logger.info("dcrDivCol : " + dcrDivCol);
				int dcrMod = 0;
				if(columnLayout>0)
				 dcrMod = dcrCount % columnLayout;
				logger.info("dcrMod : " + dcrMod);
				int rows = 0;
				
				if (dcrMod == 0) {
					rows = dcrDivCol;
				} else {
					rows = dcrDivCol + 1;
					
				}
				logger.info("rows : " + rows);

				Element dcrCountElement = categoryElement.addElement("dcrCount");
				dcrCountElement.setText(String.valueOf(dcrCount));
				int counter = 0;
				for (int rowIndex = 0; rowIndex < rows; rowIndex++) {
					logger.info("rowIndex : " + rowIndex);

					Element categoryDcrElement = categoryElement.addElement(CATEGORY_DCR);
					Element categroyDcrCountElement = categoryDcrElement.addElement("dcr-count");
					if (rowIndex + 1 == rows && dcrMod != 0) {
						categroyDcrCountElement.setText(String.valueOf(dcrMod));
					} else {
						categroyDcrCountElement.setText(dctColumnLayout);
					}

					for (int columnIndex = 0; columnIndex < columnLayout && dcrCount != 0; columnIndex++, dcrCount--) {
						logger.info("columnIndex : " + columnIndex);
						logger.info("dcrCount " + dcrCount);

						Node dcr = category.get(counter);
						Element dcrElement = categoryDcrElement.addElement("dcr");
						String dcrPath = dcr.getText();
						logger.info("dcrPath - " + dcrPath);

						Document dcrDocument = getDcrDocument(client, task, dcrPath);
						Element dcrTitleElement = dcrElement.addElement(TITLE);
						String dcrTitle = dcrDocument.selectSingleNode("/root/information/title").getText();
						logger.info("dcrTitle - " + dcrTitle);
						dcrTitleElement.setText(dcrTitle);

						Element dcrDescElement = dcrElement.addElement("desc");
						String dcrDescription = "";
						String seoDescription = "";
						String detailDescription = "";

						if (dcrDocument.selectSingleNode("/root/page-details/description") != null) {
							seoDescription = dcrDocument.selectSingleNode("/root/page-details/description").getText();
						}

						if (dcrDocument.selectSingleNode("/root/detail/description") != null) {
							detailDescription = dcrDocument.selectSingleNode("/root/detail/description").getText();
						}

						if (!seoDescription.equals("")  || !"".equals(seoDescription)) {
							dcrDescription = seoDescription;
						} else {
							dcrDescription = detailDescription.replaceAll("<[^>]*>", "");
						}
						int maxLength = (dcrDescription.length() < maxChar) ? dcrDescription.length() : maxChar;
						dcrDescription = dcrDescription.substring(0, maxLength);

						if (maxLength == maxChar) {
							dcrDescription = dcrDescription + "...";
						}

						dcrDescElement.setText(dcrDescription);

						Node image = dcrDocument.selectSingleNode("/root/information/image");
						if (image != null) {
							Element imgElement = dcrElement.addElement("image");
							String dcrImage = dcrDocument.selectSingleNode("/root/information/image").getText();
							imgElement.setText(dcrImage);
						}
						String detailName = getDetail(taxanomyDCRPath, dctType.toLowerCase());
	                    String originalDetailName = "/" + lang + "/" + detailName + "/"
	                            + dcrDocument
	                                    .selectSingleNode(HTML_FILE_NAME)
	                                    .getText();
	                    logger.info("originalDetailName - " + originalDetailName);
	                    
						Element readMoreLink = dcrElement.addElement("readMore");

						readMoreLink.setText(originalDetailName);
						counter++;
					}
				}
			} // New if condition
			else if (selectnode.selectSingleNode("newsletter-other") != null) {
				Node node = selectnode.selectSingleNode("newsletter-other");
				Element categoryElement = rootElement.addElement(CATEGORY);
				String dctType = node.selectSingleNode(CATEGORY).getText();

				// DCT Type
				Element dctTypeElement = categoryElement.addElement("dctType");
				dctTypeElement.setText(dctType);

				// Category Title
				String dctTitle = node.selectSingleNode("newsletter-category-title").getText();
				if (dctTitle != null) {
					Element dctTitleElement = categoryElement.addElement("categorytitle");
					dctTitleElement.setText(dctTitle);
				}
				logger.info("dctTitle - " + dctTitle);

				// Column Layout
				Element columnLayoutElement = categoryElement.addElement(COLUMN_LAYOUT);
				String dctColumnLayout = node.selectSingleNode(COLUMN_LAYOUT).getText();
				int columnLayout = 0;
				if (StringUtils.isNotBlank(dctColumnLayout)) {
					logger.info("Column Layout - " + dctColumnLayout);
					columnLayoutElement.setText(dctColumnLayout);
					columnLayout = Integer.parseInt(dctColumnLayout);
				}

				List<Node> category = node.selectNodes("newsletter-other-field");
				int dcrCount = category.size();
				logger.info("dcrCount : " + dcrCount);
				int dcrDivCol = 0;
				if(columnLayout > 0)
				 dcrDivCol = dcrCount / columnLayout;
				logger.info("dcrDivCol : " + dcrDivCol);
				int dcrMod = 0;
				if(columnLayout > 0)
				 dcrMod = dcrCount % columnLayout;
				logger.info("dcrMod : " + dcrMod);
				int rows = 0;
				if (dcrMod == 0) {
					rows = dcrDivCol;
				} else {
					rows = dcrDivCol + 1;
					
				}
				logger.info("rows : " + rows);

				Element dcrCountElement = categoryElement.addElement("dcrCount");
				dcrCountElement.setText(String.valueOf(dcrCount));
				int counter = 0;
				for (int rowIndex = 0; rowIndex < rows; rowIndex++) {
					logger.info("rowIndex : " + rowIndex);

					Element categoryDcrElement = categoryElement.addElement(CATEGORY_DCR);
					Element categroyDcrCountElement = categoryDcrElement.addElement("dcr-count");
					if (rowIndex + 1 == rows && dcrMod != 0) {
						categroyDcrCountElement.setText(String.valueOf(dcrMod));
					} else {
						categroyDcrCountElement.setText(dctColumnLayout);
					}

					for (int columnIndex = 0; columnIndex < columnLayout && dcrCount != 0; columnIndex++, dcrCount--) {
						logger.info("columnIndex : " + columnIndex);
						logger.info("dcrCount : " + dcrCount);

						Node dcr = category.get(counter);
						Element dcrElement = categoryDcrElement.addElement("dcr");
						String dcrPath = dcr.getText();
						logger.info("dcrPath - " + dcrPath);
						Element dcrTitleElement = dcrElement.addElement(TITLE);
						String dcrTitle = dcr.selectSingleNode("newsletter-dcr-title").getText();
						logger.info("other dcrTitle - " + dcrTitle);
						dcrTitleElement.setText(dcrTitle);

						Element dcrDescElement = dcrElement.addElement("desc");
						String dcrDescription = "";
						String detailDescription = "";

						if (dcr.selectSingleNode("newsletter-dcr-description") != null) {
							detailDescription = dcr.selectSingleNode("newsletter-dcr-description").getText();
						}
						dcrDescription = detailDescription.replaceAll("<[^>]*>", "");

						int maxLength = (dcrDescription.length() < maxChar) ? dcrDescription.length() : maxChar;
						dcrDescription = dcrDescription.substring(0, maxLength);

						if (maxLength == maxChar) {
							dcrDescription = dcrDescription + "...";
						}

						dcrDescElement.setText(dcrDescription);

						Node image = dcr.selectSingleNode("category-dcr-image");
						if (image != null) {
							Element imgElement = dcrElement.addElement("image");
							String dcrImage = dcr.selectSingleNode("category-dcr-image").getText();
							imgElement.setText(dcrImage);
						}

						String[] pathArr= dcr.selectSingleNode("category-dcr-link").getText().split(DELIMITER);  
						Element readMoreLink = dcrElement.addElement("readMore");
						 StringBuilder originalDcrName = new StringBuilder();
						 if(!(pathArr[0].equals("http:") || pathArr[0].equals("https:"))) {
	                        for(int count=3; count < pathArr.length ; count++){

	                            originalDcrName = originalDcrName.append(DELIMITER+pathArr[count]);                            

	                        } 
	                        originalDcrName = originalDcrName.append(DELIMITER + lang + originalDcrName);
	                        logger.info("originalDcrName : " + originalDcrName);
	                        readMoreLink.setText(originalDcrName.toString());
                    }else {
                        readMoreLink.setText(dcr.selectSingleNode("category-dcr-link").getText());
                    }
						counter++;
					}
				}

			}
		}

		if (data != null) {
			
			try {
				logger.info("data not null.");

				logger.debug("Data : " + data.asXML());
				transformToMailDataSource(data.asXML(), xslTemplateFile, lang);

				String htmlfilePath = htmlTemplatePath + lang + DELIMITER + htmlName + HTML;

				logger.debug("filePath1 : " + htmlfilePath);

				CSAreaRelativePath reportFile2 = new CSAreaRelativePath(htmlfilePath);
				CSAreaRelativePath[] files2 = new CSAreaRelativePath[1];
				files2[0] = reportFile2;
				task.attachFiles(files2);

				statusMap.put(TRANSITION, SUCCESS_TRANSITION);
				statusMap.put(TRANSITION_COMMENT, SUCCESS_TRANSITION_COMMENT);

			} catch (Exception ex) {
				logger.error("Exception in attach: ", ex);
			}
		}
		return statusMap;
	}

	private Document getLabelDCRFile(String dcrPath) {
		logger.info("dcrPath : " + dcrPath);
		File inputFile = new File(dcrPath);
		SAXReader reader = new SAXReader();
		Document document = null;
		try {
			document = reader.read(inputFile);
			logger.info("Root element :" + document.getRootElement().getName());
			logger.info("Labels XML Document :" + document.asXML());
		} catch (DocumentException e) {
			logger.error("Exception in getDCRFile: ", e);
		}
		return document;
	}
	// Added to get Taxanomy //    

    private String getDetail(String taxanomyDCRPath, String dctType) {
        String detail = "";
        String listingType = "";     
        Document contenttype = getLabelDCRFile(taxanomyDCRPath);
        if(contenttype != null) {
        List<Node> listcontenttype = contenttype.selectNodes(NEWSLETTER_LIST_CONTENT_TYPE);
        for (int j = 0; j < listcontenttype.size(); j++) {
            Node selectcontentnode = listcontenttype.get(j);            
                listingType = selectcontentnode.selectSingleNode("Listing").getText();
                if(listingType.equals(dctType)) {
                     detail = selectcontentnode.selectSingleNode("Detail").getText(); 
                }


            }
        }

        return detail;
    }
    // Added to get Taxanomy //
	private String changeDateFormat(CSClient client, CSExternalTask task, String dcrDate, String lang) {
		String convertedDate = "";

		try {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
			LocalDateTime date = LocalDateTime.parse(dcrDate, formatter);

			String monthName = getMonthName(client, task, date.getMonthValue(), lang);
			convertedDate = date.getDayOfMonth() + " " + monthName + ", " + date.getYear();

		} catch (Exception e) {
			logger.info("Exception in changeDateFormat: ", e);
		}
		logger.info("ConvertDate in changeDateFormat: " + convertedDate);
		return convertedDate;
	}

	private String getMonthName(CSClient client, CSExternalTask task, Integer month, String locale) {
		String monthName = "";
		String monthParam = "";
		loadProperties(client, task, "newsletter.properties");
		monthParam = month.toString() + "_" + locale;
		logger.info("Month Param " + monthParam);
		monthName = properties.getProperty(monthParam);
		return monthName;
	}

	private DataSource transformToMailDataSource(String xmlMailContent, CSSimpleFile xslTemplateFile, String lang)
			throws CSException, IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		
		try {
			logger.info("transformToMailDataSource");
			StringBuilder baseDir = new StringBuilder(genrateHtmlLocation).append(lang);
			logger.info("baseDir : " + baseDir);

			Path directory = Path.of(baseDir.toString());

			String baseFilePermission = properties.getProperty("filePermissions", "rwxr-xr-x");
			Set<PosixFilePermission> baseFilePermissions = PosixFilePermissions.fromString(baseFilePermission);

			String baseDirPermissions = properties.getProperty("dirPermissions", "rwxr-xr-x");

			logger.info("Permission for file : " + baseFilePermission);
			logger.info("Permission for directory : " + baseDirPermissions);

			Set<PosixFilePermission> baseDirPermission = PosixFilePermissions.fromString(baseDirPermissions);
			if (Files.notExists(directory)) {
				Files.createDirectory(directory, PosixFilePermissions.asFileAttribute(baseDirPermission));
			}
			File fout = new File(baseDir + DELIMITER + htmlName + HTML);
			if (fout.exists()) {
				fout.delete();
			}
			fout.createNewFile();
			
			try(FileOutputStream oFile = new FileOutputStream(fout, false);){
				
			
			logger.info("xmlMailContent- " + xmlMailContent);
			ByteArrayInputStream inputStream = new ByteArrayInputStream(xmlMailContent.getBytes(StandardCharsets.UTF_8));
			logger.info("before outputStream- " + outputStream.toString());
			XSLTransformer.transform(inputStream, xslTemplateFile, outputStream);
			logger.info("After outputStream- " + outputStream.toString());
			outputStream.writeTo(oFile);
			Files.setPosixFilePermissions(Path.of(directory + DELIMITER + htmlName + HTML), baseFilePermissions);
			}

		} catch (IOException ex) {
			logger.info("Exception in transformToMailDataSource: ", ex);
		} catch (Exception e) {
			logger.info("Exception in transformToMailDataSource: ", e);
		}
		return new javax.mail.util.ByteArrayDataSource(outputStream.toByteArray(), MAIL_MIME_TYPE);

	}

	/**
	 * Method to get the task file as a xml document.
	 *
	 * @param taskSimpleFile Task file of CSSimpleFile object
	 * @return Returns xml document of the task file.
	 */
	public Document getTaskDocument(CSSimpleFile taskSimpleFile) {
		logger.debug("NewsletterTask: getTaskDocument");
		Document document = null;
		try {
			byte[] taskSimpleFileByteArray = taskSimpleFile.read(0, -1);
			String taskSimpleFileString = new String(taskSimpleFileByteArray);
			logger.debug("taskSimpleFileString : " + taskSimpleFileString);
			document = DocumentHelper.parseText(taskSimpleFileString);
			logger.debug("document : " + document.asXML());
		} catch (Exception e) {
			logger.error("Exception in getTaskDocument: ", e);
		}
		return document;
	}

	public Document getDcrDocument(CSClient client, CSExternalTask task, String dcr) throws CSAuthorizationException {
		logger.debug(" getDcrDocument");
		Document document = null;
		try {
			String vPath = task.getArea().getRootDir().getVPath().toString();
			logger.debug("vPath : " + vPath);

			if (vPath != null && !vPath.equals("")) {
				String dcrPath = vPath + dcr;
				logger.debug("FilePath : " + dcrPath);
				CSSimpleFile dcrfile = (CSSimpleFile) client.getFile(new CSVPath(dcrPath));
				document = getTaskDocument(dcrfile);

			}
		} catch (Exception e) {
			logger.error("Exception in getDcrDocument: ", e);
		}
		return document;
	}

	private void loadProperties(final CSClient client, final CSExternalTask task, final String propertyFileName) {
		TSPropertiesFileReader propFileReader = new TSPropertiesFileReader(client, task, propertyFileName);
		properties = propFileReader.getPropertiesFile();
	}

	public String encodeURIComponent(String url) {
		String result = null;
		try {
			result = URLEncoder.encode(url, CHARSET);
		} catch (UnsupportedEncodingException e) {
			logger.info("Exception",e);
		}
		if(result != null) {
		for (String[] entry : CHARACTERS) {
			
			result = result.replaceAll(entry[0], entry[1]);
		}
		}
		logger.info(" Encoded Url : " + result);
		return result;
	}

}
