package com.hukoomi.livesite.external;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.analytics.Analytics;
import com.google.api.services.analytics.AnalyticsScopes;
import com.google.api.services.analytics.model.GaData;
import com.google.api.services.analytics.model.Profile;
import com.google.api.services.analytics.model.RealtimeData;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class StatisticsExternal {

    public static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    public void generateStatistics(){
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        String dateFrom=Integer.toString(currentYear) + "-01-01";
        String dateTo="today";
        Analytics analytics = initializeAnalytics();
        if(analytics == null){
            System.out.println("Could not initialize Analytics object.");
            return;
        }
        System.out.println("Analytics account initialized....");
        String totalSessionsCount = getAnalyticsData(getAnalyticsObj(analytics,"235015399",dateFrom,dateTo,"ga:pageviews,ga:sessions"),true);
        System.out.println(totalSessionsCount);
        String activeUsers = getRealtimeData(getRealtimeObj(analytics,"235015399"));
        System.out.println("Current Active Users " + "   " + formatNumbers(activeUsers));
        String otherData = getAnalyticsData(getAnalyticsObj(analytics,"235015399",dateFrom,dateTo,"ga:pageviews,ga:sessions"),true);
        System.out.println(otherData);
        String analyticsData = getAnalyticsData(getAnalyticsObjWithDimensions(analytics,"235015399",dateFrom,dateTo,"ga:pageviews,ga:sessions","ga:month"),false);
    }

    public static void main(String[] args) {
        StatisticsExternal statisticsExternal = new StatisticsExternal();
        statisticsExternal.generateStatistics();
    }

    protected Analytics initializeAnalytics() {
        try {
            HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            GoogleCredential credential = new GoogleCredential.Builder()
                    .setTransport(httpTransport)
                    .setJsonFactory(JSON_FACTORY)
                    .set
                    .setServiceAccountId("796485088924-0p8duvoi5rieneul88j36hc3forhgtdq@developer.gserviceaccount.com")
                    .setServiceAccountPrivateKeyFromP12File(new File("/Users/jatin/Projects/Hukoomi Revamp/GoogleAnalytics/" + "client_secrets.p12"))
                    .setServiceAccountScopes(AnalyticsScopes.all())
                    .build();

            // Construct the Analytics service object.
            return new Analytics.Builder(httpTransport, JSON_FACTORY, credential)
                    .setApplicationName("GA-ServiceAccount").build();
        } catch(IOException | GeneralSecurityException ex) {

        }
        return null;
    }

    private GaData getAnalyticsObjWithDimensions(Analytics analytics, String profileId, String dateFrom, String dateTo, String fields, String dimension) {
        if(StringUtils.isBlank(dimension)){
            System.out.println("Dimension is not passed.");
            return null;
        }
        try {
            System.out.println("Getting data for: "+ dimension);
            System.out.println(analytics.getBaseUrl());
            System.out.println(analytics.data().ga().get("ga:" + profileId, dateFrom, dateTo, fields).execute());
            GaData execute = analytics.data().ga().get("ga:" + profileId, dateFrom, dateTo, fields).setDimensions(dimension).execute();
            System.out.println(execute);
            return execute;
        } catch (IOException ex) {

        }
        return null;
    }

    private GaData getAnalyticsObj(Analytics analytics, String profileId, String dateFrom, String dateTo, String dimension) {
        if(StringUtils.isBlank(dimension)){
            System.out.println("Dimension is not passed.");
            return null;
        }
        try {
            System.out.println("Getting data for: "+ dimension);
            System.out.println(analytics.management().accounts().list().execute());
            System.out.println(analytics.management().profiles().list("~all","~all").execute());
            GaData execute = analytics.data().ga().get("ga:" + profileId, dateFrom, dateTo, dimension).execute();
            System.out.println(execute);
            return execute;
        } catch (IOException ex) {

        }
        return null;
    }

    private String getAnalyticsData(GaData results, Boolean isSingleDimension) {
        String data = "";
        System.out.println(results);
        if (results != null && null != results.getRows() && !results.getRows().isEmpty()) {
            System.out.println("Getting Data from GAData Object");
            List<GaData.ColumnHeaders> columnHeaders = results.getColumnHeaders();
            List<String> gaRows = results.getRows().get(0);
            for ( int i = 0; i < gaRows.size(); i++ ) {
                data = data + "       " + columnHeaders.get(i).getName() + ">>>>" + formatNumbers(gaRows.get(i));
            }
        }
        return data;
    }

    private RealtimeData getRealtimeObj(Analytics analytics, String profileId) {
        if(StringUtils.isBlank(profileId)){
            System.out.println("Dimension is not passed.");
            return null;
        }
        try {
            return analytics.data().realtime().get("ga:" + profileId, "rt:activeUsers").execute();
        } catch (IOException ex) {

        }
        return null;
    }

    private String getRealtimeData(RealtimeData results) {
        String data = "";
        System.out.println(results);
        if (results != null && null != results.getRows() && !results.getRows().isEmpty()) {
            System.out.println("Getting Realtime Data from Object");
            return results.getRows().get(0).get(0);
        }
        return data;
    }

    private String formatNumbers(String number){
        System.out.println("Number before format: "+ number);
        if(StringUtils.isBlank(number)){
            return null;
        }
        return DecimalFormat.getNumberInstance().format(Double.parseDouble(number));
    }

}
