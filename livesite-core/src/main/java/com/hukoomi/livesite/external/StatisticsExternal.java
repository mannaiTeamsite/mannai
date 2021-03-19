package com.hukoomi.livesite.external;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.analytics.Analytics;
import com.google.api.services.analytics.AnalyticsScopes;
import com.google.api.services.analytics.model.GaData;
import com.google.api.services.analytics.model.RealtimeData;
import com.interwoven.livesite.file.FileDal;
import com.interwoven.livesite.runtime.RequestContext;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class StatisticsExternal {

    /** JSON Factory object to use for Google APIs. */
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    /** Logger object to check the flow of the code. */
    private final Logger logger = Logger.getLogger(StatisticsExternal.class);

    public Document getStatistics(final RequestContext context){
        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("root");
        FileDal fileDal = context.getFileDal();
        String fileRoot = fileDal.getRoot();
        InputStream keyfile = fileDal.getStream(fileRoot + context.getParameterString("keyfile", "/iw/config/properties//motc-oogp-4205147-849b1733bf06.json"));
        GoogleCredential credentials = getCredentials(keyfile);
        if(credentials == null){
            logger.debug("Could not Get Credentials.");
            return document;
        }
        Analytics analytics = initializeAnalytics(context,credentials);
        if(analytics == null){
            logger.debug("Could not initialize Analytics object.");
            return document;
        }
        try {
            credentials.refreshToken();
            root.addElement("accessToken").addText(credentials.getAccessToken());
        } catch (IOException ex) {
            logger.error("Error while refreshing Google API token.", ex);
        }
        Calendar calendar = Calendar.getInstance();
        int currYear = calendar.get(Calendar.YEAR);
        String currentYear = Integer.toString(currYear);
        root.addElement("currentYear").addText(currentYear);
        String dateFrom = currentYear + "-01-01";
        String dateTo = "today";
        String profile = context.getParameterString("profile", "235015399");
        String realtimeObjects = context.getParameterString("realtime-objects", "activeUsers");
        String locale = context.getParameterString("locale", "en");
        String startYear = context.getParameterString("start-year", "2020");
        StringBuilder realtimeObjectId = new StringBuilder();
        for(String realtimeObject : realtimeObjects.split(",")){
            if(!realtimeObjectId.toString().equals("")){
                realtimeObjectId.append(",");
            }
            realtimeObjectId.append("rt:"+realtimeObject);
        }
        String analyticsObjects = context.getParameterString("analytics-objects", "pageviews,sessions");
        StringBuilder analyticsObjectId = new StringBuilder();
        for(String analyticsObject : analyticsObjects.split(",")){
            if(!analyticsObjectId.toString().equals("")){
                analyticsObjectId.append(",");
            }
            analyticsObjectId.append("ga:"+analyticsObject);
        }
        String currentYearDimensions = context.getParameterString("current-year-dimension", "deviceCategory");
        StringBuilder currentYearDimensionId = new StringBuilder();
        for(String  currentYearDimension : currentYearDimensions.split(",")){
            if(!currentYearDimensionId.toString().equals("")){
                currentYearDimensionId.append(",");
            }
            currentYearDimensionId.append("ga:"+currentYearDimension);
        }
        String analyticsDimensions = context.getParameterString("analytics-dimension", "month,deviceCategory");
        StringBuilder analyticsDimensionId = new StringBuilder();
        for(String  analyticsDimension : analyticsDimensions.split(",")){
            if(!analyticsDimensionId.toString().equals("")){
                analyticsDimensionId.append(",");
            }
            analyticsDimensionId.append("ga:"+analyticsDimension);
        }
        document = getRealtimeData(getRealtimeObj(analytics,profile, realtimeObjectId.toString()), document);
        document = getAnalyticsData(getAnalyticsObjWithDimensions(analytics,profile,dateFrom,dateTo,analyticsObjectId.toString(),currentYearDimensionId.toString()), document, currentYear, currentYear, locale);
        int defaultYear = Integer.parseInt(startYear);
        for(int year=defaultYear;year<currYear;year++){
            document = getAnalyticsData(getAnalyticsObjWithDimensions(analytics,profile,dateFrom,dateTo,analyticsObjectId.toString(),analyticsDimensionId.toString()), document, year+"-01-01", year+"-12-31", locale);
        }
        return document;
    }

    /**
     * Initializes an Analytics service object.
     *
     * @return An authorized Analytics service object.
     * @throws IOException
     * @throws GeneralSecurityException
     */
    private Analytics initializeAnalytics(RequestContext context, GoogleCredential credential) {
        try{
            HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            String application = context.getParameterString("application", "GA-ServiceAccount");
            Analytics build = new Analytics.Builder(httpTransport, JSON_FACTORY, credential)
                    .setApplicationName(application).build();
            return build;
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public GoogleCredential getCredentials(InputStream keyfile){
        try {
            return GoogleCredential
                    .fromStream(keyfile)
                    .createScoped(AnalyticsScopes.all());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private GaData getAnalyticsObjWithDimensions(Analytics analytics, String profileId, String dateFrom, String dateTo, String fields, String dimension) {
        if(StringUtils.isBlank(dimension)){
            logger.info("Dimension is not passed.");
            return null;
        }
        try {
            logger.info("Getting data for: "+ dimension);
            GaData execute = analytics.data().ga().get("ga:" + profileId, dateFrom, dateTo, fields).setDimensions(dimension).setSort(dimension.split(",")[0]).execute();
            return execute;
        } catch (IOException ex) {

        }
        return null;
    }

    private Document getAnalyticsData(GaData results, Document document,String yearFrom, String yearTo, String locale) {
        Element root = document.getRootElement();
        Element gaData = root.addElement("ga-data");
        gaData.addElement("year-from").addText(yearFrom);
        gaData.addElement("year-to").addText(yearTo);
        if (results != null && null != results.getRows() && !results.getRows().isEmpty() && null != results.getColumnHeaders() && !results.getColumnHeaders().isEmpty()) {
            logger.info("Getting Realtime Data from Object");
            List<GaData.ColumnHeaders> columnHeaders = results.getColumnHeaders();
            List<List<String>> rowSets = results.getRows();
            for(int traverseRow=0; traverseRow < rowSets.size(); traverseRow++){
                for(int traverseColumn=0; traverseColumn < columnHeaders.size(); traverseColumn++){
                    String metricName = columnHeaders.get(traverseColumn).getName();
                    gaData.addElement("name").addText(metricName);
                    if(metricName.equals("ga:month")){
                        gaData.addElement("value").addAttribute("month", rowSets.get(traverseRow).get(traverseColumn)).addText(formatMonth(rowSets.get(traverseRow).get(traverseColumn),locale));
                    } else {
                        gaData.addElement("value").addText(formatNumbers(rowSets.get(traverseRow).get(traverseColumn)));
                    }
                }
            }
            return document;
        }
        return document;
    }

    private RealtimeData getRealtimeObj(Analytics analytics, String profileId, String objectId) {
        if(StringUtils.isBlank(profileId)){
            logger.debug("Dimension is not passed.");
            return null;
        }
        try {
            return analytics.data().realtime().get("ga:" + profileId, objectId).execute();
        } catch (IOException ex) {
            logger.error("Error while fetching realtime data from API.", ex);
        }
        return null;
    }

    private Document getRealtimeData(RealtimeData results, Document document) {
        Element root = document.getRootElement();
        if (results != null && null != results.getRows() && !results.getRows().isEmpty() && null != results.getColumnHeaders() && !results.getColumnHeaders().isEmpty()) {
            logger.info("Getting Realtime Data from Object");
            List<RealtimeData.ColumnHeaders> columnHeaders = results.getColumnHeaders();
            List<List<String>> rowSets = results.getRows();
            for(int traverseRow=0; traverseRow < rowSets.size(); traverseRow++){
                for(int traverseColumn=0; traverseColumn < columnHeaders.size(); traverseColumn++){
                    Element rtData = root.addElement("rt-data");
                    rtData.addElement("name").addText(columnHeaders.get(traverseColumn).getName());
                    rtData.addElement("value").addText(formatNumbers(rowSets.get(traverseRow).get(traverseColumn)));
                }
            }
            return document;
        }
        return document;
    }

    private String formatNumbers(String number){
        logger.debug("Number before format: " + number);
        if(StringUtils.isBlank(number) || !number.matches("[0-9]+")){
            return number;
        }
        return DecimalFormat.getNumberInstance().format(Double.parseDouble(number));
    }

    private String formatMonth(String month, String language){
        logger.debug("Month: " + month);
        logger.debug("Language: " + language);
        if(StringUtils.isBlank(month) || !month.matches("[0-9]+")){
            return month;
        }
        Locale locale;
        if(language.equals("ar")){
            locale = new Locale(language,"QA");
        } else {
            locale = new Locale(language);
        }
        DateFormatSymbols dateFormat = new DateFormatSymbols(locale);
        return dateFormat.getMonths()[Integer.parseInt(month)-1];
    }

}
