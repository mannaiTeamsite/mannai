package com.hukoomi.livesite.external;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;

import com.google.api.services.analytics.Analytics;
import com.google.api.services.analytics.AnalyticsScopes;
import com.google.api.services.analytics.model.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;


/**
 * A simple example of how to access the Google Analytics API using a service
 * account.
 */
public class HelloAnalyticsReporting {


    private static final String APPLICATION_NAME = "GA-ServiceAccount";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String KEY_FILE_LOCATION = "/Users/jatin/Projects/Hukoomi Revamp/GoogleAnalytics/motc-oogp-4205147-849b1733bf06.json";
    public static void main(String[] args) {
        try {
            Analytics analytics = initializeAnalytics();
            System.out.println(analytics.getBaseUrl());
            System.out.println(analytics.getServicePath());

//            String profile = getFirstProfileId(analytics);
            String profile = "235015399";
            System.out.println("First Profile Id: "+ profile);
//            Accounts execute = analytics.management().accounts().list().execute();
//            System.out.printf(String.valueOf(execute));
//            AccountSummaries execute = analytics.management().accountSummaries().list().execute();
//            System.out.printf(String.valueOf(execute));

            Profiles execute1 = analytics.management().profiles().list("185914230","UA-185914230-1").execute();
            System.out.println(execute1.getUsername());
            System.out.println(execute1.getItems().size());
            printResults(getResults(analytics, profile));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Initializes an Analytics service object.
     *
     * @return An authorized Analytics service object.
     * @throws IOException
     * @throws GeneralSecurityException
     */
    private static Analytics initializeAnalytics() throws GeneralSecurityException, IOException {

        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        GoogleCredential credential = GoogleCredential
                .fromStream(new FileInputStream(KEY_FILE_LOCATION))
                .createScoped(AnalyticsScopes.all());
        System.out.println(credential.getServiceAccountId());
        System.out.println(credential.getServiceAccountScopes());
        // Construct the Analytics service object.
        Analytics build = new Analytics.Builder(httpTransport, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME).build();
        System.out.println(build.getApplicationName());

        return build;
    }


    private static String getFirstProfileId(Analytics analytics) throws IOException {
        // Get the first view (profile) ID for the authorized user.
        String profileId = null;

        // Query for the list of all accounts associated with the service account.
        Accounts accounts = analytics.management().accounts().list().execute();

        if (accounts.getItems().isEmpty()) {
            System.err.println("No accounts found");
        } else {
            String firstAccountId = accounts.getItems().get(0).getId();

            // Query for the list of properties associated with the first account.
            Webproperties properties = analytics.management().webproperties()
                    .list(firstAccountId).execute();

            if (properties.getItems().isEmpty()) {
                System.err.println("No Webproperties found");
            } else {
                String firstWebpropertyId = properties.getItems().get(0).getId();

                // Query for the list views (profiles) associated with the property.
                Profiles profiles = analytics.management().profiles()
                        .list(firstAccountId, firstWebpropertyId).execute();

                if (profiles.getItems().isEmpty()) {
                    System.err.println("No views (profiles) found");
                } else {
                    // Return the first (view) profile associated with the property.
                    profileId = profiles.getItems().get(0).getId();
                }
            }
        }
        return profileId;
    }

    private static GaData getResults(Analytics analytics, String profileId) throws IOException {
        // Query the Core Reporting API for the number of sessions
        // in the past seven days.
        return analytics.data().ga()
                .get("ga:" + profileId, "7daysAgo", "today", "ga:sessions")
                .execute();
    }

    private static void printResults(GaData results) {
        // Parse the response from the Core Reporting API for
        // the profile name and number of sessions.
        if (results != null && !results.getRows().isEmpty()) {
            System.out.println("View (Profile) Name: "
                    + results.getProfileInfo().getProfileName());
            System.out.println("Total Sessions: " + results.getRows().get(0).get(0));
        } else {
            System.out.println("No results found");
        }
    }
}
