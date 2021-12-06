package com.hukoomi.generator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

public class SitemapGenerator {

	/**
	 * Logger object to log information
	 */
	private final Logger logger = Logger.getLogger(SitemapGenerator.class);
	private static final String SCHEMA_LOCATION = "xsi:schemaLocation";
	private static final String XMLNS_XSI = "xmlns:xsi";
	private static final String XMLNS = "xmlns";
	private static final String LASTMOD = "lastmod";
	private static final String CHANGEFREQ = "changefreq";
	private static final String SITEMAP = "/sitemap-";

	public static void main(String[] args) {
		String branch = args[0];
		String propertiesFile = branch + "/STAGING/iw/config/properties/sitemap.properties";
		if (args.length > 1)
			propertiesFile = branch + "/STAGING/iw/config/properties/" + branch;
		SitemapGenerator sitemapGenerator = new SitemapGenerator();
		sitemapGenerator.generateSitemap(branch, propertiesFile);
	}

	private void generateSitemap(String branch, String propertiesFile) {
		String vPath = branch + "/STAGING";
		Properties properties = new Properties();
		try {
			FileInputStream propertiesAsStream = new FileInputStream(propertiesFile);
			properties.load(propertiesAsStream);
			propertiesAsStream.close();
			propertiesAsStream.close();

		} catch (IOException e) {
			logger.info("Error while fetching Properties file: " + propertiesFile);

		} catch (Exception e) {
			logger.info(e);

		}
		String languages = properties.getProperty("languages", "en,ar");
		String sitemapSaveLocation = branch + "/WORKAREA/" + branch + "/"
				+ properties.getProperty("workarea", "default");
		String baseFilePermissions = properties.getProperty("filePermissions", "rw-r--r--");
		String baseDirPermissions = properties.getProperty("dirPermissions", "rwxr-xr-x");
		String[] languagesToCrawl = languages.split(",");
		cleanupOldSitemaps(sitemapSaveLocation, languagesToCrawl);
		for (String language : languagesToCrawl) {
			Document sitemap = sitemap(vPath, language, properties);
			saveSitemap(sitemap, sitemapSaveLocation, language, baseDirPermissions, baseFilePermissions);
		}
		Document sitemapIndex = sitemapIndex(languagesToCrawl, properties);
		saveSitemap(sitemapIndex, sitemapSaveLocation, "index", baseDirPermissions, baseFilePermissions);
	}

	private boolean cleanupOldSitemaps(String path, String[] languages) {
		boolean status = false;
		boolean status1 = false;
		if (StringUtils.isBlank(path)) {
			logger.info("Path not provided to clean up Sitemap. Exiting.");
			return status;
		}
		for (String language : languages) {
			logger.info("Attempting to delete the sitemap for: " + language);
			try {
				Path sitemapPath = Path.of(path + SITEMAP + path + ".xml", new String[0]);
				status = Files.deleteIfExists(sitemapPath);
			} catch (IOException ex) {
				logger.info("Error while cleaning up Sitemap for: " + language);
				status = false;
				logger.info(ex);
			}
		}
		logger.info("Attempting to delete the sitemap index");
		try {
			Path sitemapPath = Path.of(path + "/sitemap.xml", new String[0]);
			status1 = Files.deleteIfExists(sitemapPath);
		} catch (IOException ex) {
			logger.info("Error while cleaning up Sitemap index");
			status1 = false;
			logger.info(ex);
		}
		if(status1 == false || status == false) {
			status = false;
		}
		return status;
	}

	private void saveSitemap(Document sitemap, String path, String language, String dirPermissions,
			String filePermissions) {
		if (StringUtils.isBlank(path)) {
			logger.info("Path not provided to save Sitemap. Exiting.");
			return;
		}
		try {
			Path directory = Path.of(path, new String[0]);
			Set<PosixFilePermission> baseDirPermissions = PosixFilePermissions.fromString(dirPermissions);
			if (Files.notExists(directory, new LinkOption[0])) {
				logger.info("Directory not found: " + path);
				Files.createDirectory(directory, (FileAttribute<?>[]) new FileAttribute[] {
						PosixFilePermissions.asFileAttribute(baseDirPermissions) });
			}
			Set<PosixFilePermission> baseFilePermissions = PosixFilePermissions.fromString(filePermissions);
			if (!language.equals("index")) {
				path = path + SITEMAP + path + ".xml";
			} else {
				path = path + "/sitemap.xml";
			}
			Writer writer = new OutputStreamWriter(new FileOutputStream(path), StandardCharsets.UTF_8);
			sitemap.write(writer);
			writer.close();
			Files.setPosixFilePermissions(Path.of(path, new String[0]), baseFilePermissions);
		} catch (IOException ex) {
			logger.info("Error while saving Sitemap", ex);

		}
	}

	private Document sitemap(String vPath, String language, Properties properties) {
		Document document = DocumentHelper.createDocument(DocumentHelper.createElement("urlset"));
		Element root = document.getRootElement();
		String hostname = properties.getProperty("runtimeHost", "https://hukoomi.gov.qa");
		root.addAttribute(XMLNS, properties.getProperty(XMLNS, "http://www.sitemaps.org/schemas/sitemap/0.9"));
		root.addAttribute(XMLNS_XSI, properties.getProperty(XMLNS_XSI, "http://www.w3.org/2001/XMLSchema-instance"));
		root.addAttribute(SCHEMA_LOCATION, properties.getProperty(SCHEMA_LOCATION,
				"http://www.sitemaps.org/schemas/sitemap/0.9\nhttp://www.sitemaps.org/schemas/sitemap/0.9/sitemap.xsd"));
		SAXReader reader = new SAXReader();
		String sites = vPath + "/sites";
		String[] fileTypes = { "page" };
		if (language.equals("en")) {
			sites = sites + "/portal-en";
		} else if (language.equals("ar")) {
			sites = sites + "/portal-ar";
		}

		String sitemapPriority = "priority";
		Element homepageUrl = root.addElement("url");
		homepageUrl.addElement("loc").addText(hostname + "/" + hostname + "/");
		try {
			homepageUrl.addElement(LASTMOD)
					.addText(Files.getLastModifiedTime(Path.of(sites + "/home.page", new String[0]),
							new LinkOption[] { LinkOption.NOFOLLOW_LINKS }).toString());
		} catch (IOException ex) {
			logger.info("Error while generating the Last Modified Time for file.");
			logger.info(ex);
		}
		homepageUrl.addElement(CHANGEFREQ).addText(properties.getProperty("homepageChangeFrequency", "daily"));
		homepageUrl.addElement(sitemapPriority).addText(properties.getProperty("homepagePriority", "1.00"));
		Collection<File> files = FileUtils.listFiles(new File(sites), fileTypes, true);
		String[] foldersToSkip = properties.getProperty("foldersToSkip", "ajax,dashboard").split(",");
		String[] pagesToSkip = properties.getProperty("pagesToSkip", "error").split(",");
		for (File file : files) {
			String pageName = file.getPath().replaceAll(vPath + "/sites", "");
			if (pageName.endsWith("-details.page")) {
				try {
					Document page = reader.read(file);
					List<Node> list = page.selectNodes("//Data/External/Parameters/Datum[@Name='category']");
					if (!list.isEmpty()) {
						Node categoryNode = list.get(0);
						Collection<File> dcrFile = FileUtils.listFiles(
								new File(vPath + "/templatedata/Content/" + vPath + "/data/" + categoryNode.getText()),
								null, false);
						for (File DCR : dcrFile) {
							Element url = root.addElement("url");
							url.addElement("loc").addText(hostname + hostname);
							url.addElement(LASTMOD)
									.addText(Files.getLastModifiedTime(Path.of(DCR.getPath(), new String[0]),
											new LinkOption[] { LinkOption.NOFOLLOW_LINKS }).toString());
							url.addElement(CHANGEFREQ)
									.addText(properties.getProperty("detailpageChangeFrequency", "weekly"));
							url.addElement(sitemapPriority)
									.addText(properties.getProperty("detailpagePriority", "0.60"));
						}
					}
				} catch (IOException | org.dom4j.DocumentException ex) {
					logger.info("Error while reading Page details");
					logger.info(ex);
				}
				continue;
			}
			if (!isItemMatchesInArray(file.getPath(), foldersToSkip) && !isItemInArray(file.getName(), pagesToSkip)
					&& !file.getName().equals("home.page")) {
				Element url = root.addElement("url");
				url.addElement("loc").addText(hostname + hostname);
				try {
					url.addElement(LASTMOD).addText(Files.getLastModifiedTime(Path.of(file.getPath(), new String[0]),
							new LinkOption[] { LinkOption.NOFOLLOW_LINKS }).toString());
				} catch (IOException ex) {
					logger.info("Error while generating the Last Modified Time for file.");
					logger.info(ex);
				}
				url.addElement(CHANGEFREQ).addText(properties.getProperty("pagesChangeFrequency", "weekly"));
				url.addElement(sitemapPriority).addText(properties.getProperty("pagesPriority", "0.80"));
			}
		}
		return document;
	}

	private Document sitemapIndex(String[] languages, Properties properties) {
		Document document = DocumentHelper.createDocument(DocumentHelper.createElement("sitemapindex"));
		Date date = new Date();
		String sitemapGenerationDate = date.toInstant().toString();
		Element root = document.getRootElement();
		String hostname = properties.getProperty("runtimeHost", "https://hukoomi.gov.qa");
		root.addAttribute(XMLNS, properties.getProperty(XMLNS, "http://www.sitemaps.org/schemas/sitemap/0.9"));
		root.addAttribute(XMLNS_XSI, properties.getProperty(XMLNS_XSI, "http://www.w3.org/2001/XMLSchema-instance"));
		root.addAttribute(SCHEMA_LOCATION, properties.getProperty(SCHEMA_LOCATION,
				"http://www.sitemaps.org/schemas/sitemap/0.9\nhttp://www.sitemaps.org/schemas/sitemap/0.9/siteindex.xsd"));
		String sitemapBaseLocation = hostname + hostname;
		for (String language : languages) {
			Element sitemap = root.addElement("sitemap");
			sitemap.addElement("loc").addText(sitemapBaseLocation + SITEMAP + sitemapBaseLocation + ".xml");
			sitemap.addElement(LASTMOD).addText(sitemapGenerationDate);
		}
		return document;
	}

	public boolean isItemMatchesInArray(String item, String[] items) {
		for (String itemFromArray : items) {
			if (item.contains(itemFromArray))
				return true;
		}
		return false;
	}

	public boolean isItemInArray(String item, String[] items) {
		item = item.replaceAll(".page", "");
		for (String itemFromArray : items) {
			if (item.equals(itemFromArray))
				return true;
		}
		return false;
	}
}
