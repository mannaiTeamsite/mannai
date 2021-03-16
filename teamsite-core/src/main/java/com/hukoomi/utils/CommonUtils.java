package com.hukoomi.utils;

public class CommonUtils {

    /*
     * Get pretty URL from the Page Link items.
     *
     * @param url URL to convert as a pretty print.
     * @param locale String representing the locale for which URL to be generated while pretty print.
     * @param dcrName String DCR Name if the URL is required for any Detail pages.
     *
     * @return String prettyURL Converted Pretty URL.
     */
    public String getPrettyURLForPage(String url, String locale, String dcrName) {
        String prettyURL = "";
        if (url.equals("")){
            return prettyURL;
        }
        prettyURL = url.replaceAll("/sites|/portal-|-details.page|home.page","/");
        prettyURL = prettyURL.replaceFirst("/en/|/ar/","/"+locale+"/");
        if(!dcrName.equals("")){
            prettyURL = prettyURL + dcrName;
        }
        prettyURL = prettyURL.endsWith("/") ? prettyURL.substring(0,prettyURL.length()-1) : prettyURL;
        prettyURL = prettyURL.endsWith(".page") ? prettyURL.substring(0,prettyURL.indexOf(".page")) : prettyURL;
        return prettyURL;
    }

}
