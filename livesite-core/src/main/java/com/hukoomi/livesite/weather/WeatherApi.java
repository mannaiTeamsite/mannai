package src.main.java.com.hukoomi.livesite.weather;

import java.io.IOException;
import com.interwoven.livesite.runtime.RequestContext;
import com.interwoven.livesite.dom4j.Dom4jUtils;
import org.apache.log4j.Logger;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.json.JSONObject;
import org.json.XML;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class WeatherApi {

    /** Logger object to check the flow of the code.*/
    private static final Logger LOGGER =
            Logger.getLogger(WeatherApi.class);

    /** This method will be called from Component
     * External for Weather Content fetching.
     * @param context The parameter context object passed from Component.
     * @return doc return the weather response
     * generated from API.
     */
    public Document getWeather(
            final RequestContext context) throws IOException {
        String weatherApiUrl =
                "https://api.openweathermap.org/data/2.5/onecall";
        String decode = "UTF-8";
        String lat = context.getParameterString(
                "lat", "");
        try {
            String latEn = URLDecoder.decode(
                    lat, decode);
            weatherApiUrl = weatherApiUrl.concat("?lat=" + latEn);
            LOGGER.debug("weatherApiUrl lat : " + weatherApiUrl);
            LOGGER.info("weatherApiUrl lat : " + weatherApiUrl);

        } catch (UnsupportedEncodingException e) {
            LOGGER.warn("Unable to decode lat="
                    + lat, e);
        }

        String lon = context.getParameterString(
                "lon", "");
        try {
            String lonEn = URLDecoder.decode(
                    lon, decode);
            weatherApiUrl = weatherApiUrl.concat("&lon=" + lonEn);
            LOGGER.debug("weatherApiUrl lon : " + weatherApiUrl);
            LOGGER.info("weatherApiUrl lon : " + weatherApiUrl);

        } catch (UnsupportedEncodingException e) {
            LOGGER.warn("Unable to decode lon="
                    + lon, e);
        }

        String key = context.getParameterString(
                "key", "");
        try {
            String keyEn = URLDecoder.decode(
                    key, decode);
            weatherApiUrl = weatherApiUrl.concat("&appid=" + keyEn);
            LOGGER.debug("weatherApiUrl key : " + weatherApiUrl);
            LOGGER.info("weatherApiUrl key : " + weatherApiUrl);

        } catch (UnsupportedEncodingException e) {
            LOGGER.warn("Unable to decode key="
                    + key, e);
        }

        String unit = context.getParameterString(
                "unit", "");
        try {
            String unitEn = URLDecoder.decode(
                    unit, decode);
            weatherApiUrl = weatherApiUrl.concat("&units=" + unitEn);
            LOGGER.debug("weatherApiUrl unit : " + weatherApiUrl);
            LOGGER.info("weatherApiUrl unit : " + weatherApiUrl);

        } catch (UnsupportedEncodingException e) {
            LOGGER.warn("Unable to decode unit="
                    + unit, e);
        }

        weatherApiUrl = weatherApiUrl.concat("&exclude=hourly,minutely");

        return WeatherApi.callWeatherApi(weatherApiUrl, "WeatherResponse");
    }

    /** This method will be called from Component
     * External for Weather Content fetching.
     * @param url The parameter passed from getWeather.
     * @param xmlRootName XML Root Element of returned document.
     * @return String return the document
     */
    public static Document callWeatherApi(
            final String url,
            final String xmlRootName) throws IOException {
        Document document = DocumentHelper.createDocument();
        try {
            JSONObject returnObject = new JSONObject();
            returnObject.put(xmlRootName, WeatherApi.getRequest(url));
            String returnXML = XML.toString(returnObject);
            document = Dom4jUtils.newDocument(returnXML);
        } catch (Exception e) {
            LOGGER.error("Weather Exception", e);
        }
        return document;
    }
    /** This method will be called from Component
     * External for Weather Content fetching.
     * @param url The parameter passed from callWeatherApi.
     * @return String return the weather response
     * generated from weather Api.
     */
    public static String getRequest(final String url) throws IOException {
        URL urlForGetRequest = new URL(url);
        String readLine = null;
        HttpURLConnection connection = (HttpURLConnection)
                urlForGetRequest.openConnection();
        connection.setRequestMethod("GET");
        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            while ((readLine = in .readLine()) != null) {
                response.append(readLine);
            } in .close();
            return response.toString();
        } else {
            LOGGER.warn("GET NOT WORKED");
        }
        return null;
    }
}
