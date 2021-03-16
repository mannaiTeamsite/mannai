package com.hukoomi.generator;

import com.hukoomi.utils.CommonUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.dom4j.*;
import org.dom4j.io.SAXReader;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.*;

public class SitemapGenerator {

    private static final String STAGING = "STAGING";
    private static final String PROPERTIES_FILE_PATH = "/iw/config/properties";
    private static final String PROPERTIES_FILE = "sitemap.properties";

    public static void main(String[] args) {
        String branch = args[0];
        String propertiesFile = branch + "/" + STAGING + PROPERTIES_FILE_PATH + "/" + PROPERTIES_FILE;
        if(args.length>1){
            propertiesFile = branch + "/" + STAGING + PROPERTIES_FILE_PATH + "/" + args[1];
        }
        String vPath = branch + "/" + STAGING;
        Properties properties = new Properties();
        try (FileInputStream propertiesAsStream = new FileInputStream(propertiesFile)) {
            properties.load(propertiesAsStream);
        } catch (IOException e) {
            System.out.println("Error while fetching Properties file: " + propertiesFile);
            e.printStackTrace(System.out);
        }
        SitemapGenerator sitemapGenerator = new SitemapGenerator();
        String languages = properties.getProperty("languages", "en,ar");
        String sitemapSaveLocation = branch + "/WORKAREA/" + properties.getProperty("workarea","default") + "/" + properties.getProperty("sitemapSaveLocation","/sitemaps");
        String baseFilePermissions = properties.getProperty("filePermissions","rw-r--r--");
        String baseDirPermissions = properties.getProperty("dirPermissions","rwxr-xr-x");
        final String[] languagesToCrawl = languages.split(",");
        sitemapGenerator.cleanupOldSitemaps(sitemapSaveLocation,languagesToCrawl);
        for (String language : languagesToCrawl) {
            Document sitemap = sitemapGenerator.sitemap(vPath, language, properties);
            sitemapGenerator.saveSitemap(sitemap, sitemapSaveLocation, language, baseDirPermissions, baseFilePermissions);
        }
        Document sitemapIndex = sitemapGenerator.sitemapIndex(languagesToCrawl, properties);
        sitemapGenerator.saveSitemap(sitemapIndex,sitemapSaveLocation,"index", baseDirPermissions, baseFilePermissions);
    }

    private boolean cleanupOldSitemaps(String path, String[] languages) {
        boolean status = false;
        if(StringUtils.isBlank(path)){
            System.out.println("Path not provided to clean up Sitemap. Exiting.");
            return status;
        }
        for(String language : languages){
            System.out.println("Attempting to delete the sitemap for: " + language);
            try{
                Path sitemapPath = Path.of(path + "/sitemap-" + language + ".xml");
                status = Files.deleteIfExists(sitemapPath);
            } catch (IOException ex) {
                System.out.println("Error while cleaning up Sitemap for: " + language);
                status = false;
                ex.printStackTrace(System.out);
            }
        }
        System.out.println("Attempting to delete the sitemap index");
        try{
            Path sitemapPath = Path.of(path + "/sitemap.xml");
            status = Files.deleteIfExists(sitemapPath);
        } catch (IOException ex) {
            System.out.println("Error while cleaning up Sitemap index");
            status = false;
            ex.printStackTrace(System.out);
        }
        return status;
    }

    private void saveSitemap(Document sitemap, String path, String language, String dirPermissions, String filePermissions) {
        if(StringUtils.isBlank(path)){
            System.out.println("Path not provided to save Sitemap. Exiting.");
            return;
        }
        try {
            Path directory = Path.of(path);
            Set<PosixFilePermission> baseDirPermissions = PosixFilePermissions.fromString(dirPermissions);
            if(Files.notExists(directory)) {
                System.out.println("Directory not found: "+path);
                Files.createDirectory(directory, PosixFilePermissions.asFileAttribute(baseDirPermissions));
            }
            Set<PosixFilePermission> baseFilePermissions = PosixFilePermissions.fromString(filePermissions);
            if(!language.equals("index")) {
                path = path + "/sitemap-" + language + ".xml";
            } else {
                path = path + "/sitemap.xml";
            }
            Writer writer = new OutputStreamWriter(new FileOutputStream(path), StandardCharsets.UTF_8);
            sitemap.write(writer);
            writer.close();
            Files.setPosixFilePermissions(Path.of(path),baseFilePermissions);
        } catch (IOException ex){
            System.out.println("Error while saving Sitemap");
            ex.printStackTrace(System.out);
        }
    }

    private Document sitemap(String vPath, String language,Properties properties) {
        Document document = DocumentHelper.createDocument(DocumentHelper.createElement("urlset"));
        CommonUtils utils = new CommonUtils();
        Element root = document.getRootElement();
        String hostname = properties.getProperty("runtimeHost","https://hukoomi.gov.qa");
        root.addAttribute("xmlns",properties.getProperty("xmlns","http://www.sitemaps.org/schemas/sitemap/0.9"));
        root.addAttribute("xmlns:xsi",properties.getProperty("xmlns:xsi","http://www.w3.org/2001/XMLSchema-instance"));
        root.addAttribute("xsi:schemaLocation",properties.getProperty("xsi:schemaLocation","http://www.sitemaps.org/schemas/sitemap/0.9\n" +
                "http://www.sitemaps.org/schemas/sitemap/0.9/sitemap.xsd"));
        SAXReader reader = new SAXReader();
        String sites = vPath + "/sites";
        String[] fileTypes = {"page"};
        if(language.equals("en")){
            sites = sites + "/portal-en";
        } else if(language.equals("ar")){
            sites = sites + "/portal-ar";
        }
        final String sitemap_url = "url";
        final String sitemap_location = "location";
        final String sitemap_lastmod = "lastmod";
        final String sitemap_changefreq = "changefreq";
        final String sitemap_priority = "priority";
        Element homepageUrl = root.addElement(sitemap_url);
        homepageUrl.addElement(sitemap_location).addText(hostname+"/"+language+"/");
        try {
            homepageUrl.addElement(sitemap_lastmod).addText(Files.getLastModifiedTime(Path.of(sites + "/home.page"), LinkOption.NOFOLLOW_LINKS).toString());
        } catch (IOException ex){
            System.out.println("Error while generating the Last Modified Time for file.");
            ex.printStackTrace(System.out);
        }
        homepageUrl.addElement(sitemap_changefreq).addText(properties.getProperty("homepageChangeFrequency","daily"));
        homepageUrl.addElement(sitemap_priority).addText(properties.getProperty("homepagePriority","1.00"));
        Collection<File> files = FileUtils.listFiles(new File(sites), fileTypes, true);
        String[] foldersToSkip = properties.getProperty("foldersToSkip", "ajax,dashboard").split(",");
        String[] pagesToSkip = properties.getProperty("pagesToSkip", "error").split(",");
        for ( File file : files ) {
            String pageName = file.getPath().replaceAll(vPath+"/sites", "");
            if(pageName.endsWith("-details.page")){
                try{
                Document page = reader.read(file);
                List list = page.selectNodes("//Data/External/Parameters/Datum[@Name='category']");
                if(list.size()>0){
                    Node categoryNode = (Node) list.get(0);
                    Collection<File> DCRs = FileUtils.listFiles(new File(vPath + "/templatedata/Content/" + categoryNode.getText() + "/data/" + language), null, false);
                    for ( File DCR : DCRs ) {
                        Element url = root.addElement(sitemap_url);
                        url.addElement(sitemap_location).addText(hostname + utils.getPrettyURLForPage(pageName, language, DCR.getName()));
                        url.addElement(sitemap_lastmod).addText(Files.getLastModifiedTime(Path.of(DCR.getPath()), LinkOption.NOFOLLOW_LINKS).toString());
                        url.addElement(sitemap_changefreq).addText(properties.getProperty("detailpageChangeFrequency","weekly"));
                        url.addElement(sitemap_priority).addText(properties.getProperty("detailpagePriority","0.60"));
                    }
                }} catch (IOException | DocumentException ex){
                    System.out.println("Error while reading Page details");
                    ex.printStackTrace(System.out);
                }
            } else if(!isItemMatchesInArray(file.getPath(),foldersToSkip) && !isItemInArray(file.getName(),pagesToSkip) && !file.getName().equals("home.page")) {
                Element url = root.addElement(sitemap_url);
                url.addElement(sitemap_location).addText(hostname + utils.getPrettyURLForPage(pageName, language,""));
                try {
                    url.addElement(sitemap_lastmod).addText(Files.getLastModifiedTime(Path.of(file.getPath()), LinkOption.NOFOLLOW_LINKS).toString());
                } catch (IOException ex){
                    System.out.println("Error while generating the Last Modified Time for file.");
                    ex.printStackTrace(System.out);
                }
                url.addElement(sitemap_changefreq).addText(properties.getProperty("pagesChangeFrequency","weekly"));
                url.addElement(sitemap_priority).addText(properties.getProperty("pagesPriority","0.80"));
            }
        }
        return document;
    }

    private Document sitemapIndex(String[] languages,Properties properties) {
        Document document = DocumentHelper.createDocument(DocumentHelper.createElement("sitemapindex"));
        Date date = new Date();
        String sitemapGenerationDate = date.toInstant().toString();
        Element root = document.getRootElement();
        String hostname = properties.getProperty("runtimeHost","https://hukoomi.gov.qa");
        root.addAttribute("xmlns",properties.getProperty("xmlns","http://www.sitemaps.org/schemas/sitemap/0.9"));
        root.addAttribute("xmlns:xsi",properties.getProperty("xmlns:xsi","http://www.w3.org/2001/XMLSchema-instance"));
        root.addAttribute("xsi:schemaLocation",properties.getProperty("xsi:schemaLocation","http://www.sitemaps.org/schemas/sitemap/0.9\n" +
                "http://www.sitemaps.org/schemas/sitemap/0.9/siteindex.xsd"));
        String sitemapBaseLocation = hostname + properties.getProperty("sitemapSaveLocation","/sitemaps");
        for(String language : languages){
            Element sitemap = root.addElement("sitemap");
            sitemap.addElement("loc").addText(sitemapBaseLocation + "/sitemap-" + language + ".xml");
            sitemap.addElement("lastmod").addText(sitemapGenerationDate);
        }
        return document;
    }

    public boolean isItemMatchesInArray(String item, String[] items) {
        for (String itemFromArray : items) {
            if(item.contains(itemFromArray)){
                return true;
            }
        }
        return false;
    }

    public boolean isItemInArray(String item, String[] items) {
        item = item.replaceAll(".page","");
        for (String itemFromArray : items) {
            if(item.equals(itemFromArray)) {
                return true;
            }
        }
        return false;
    }

}
