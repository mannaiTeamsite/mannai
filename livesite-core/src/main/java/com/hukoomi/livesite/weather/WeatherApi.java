package com.hukoomi.livesite.weather;

import com.hukoomi.utils.PropertiesFileReader;
import com.interwoven.livesite.dom4j.Dom4jUtils;
import com.interwoven.livesite.runtime.RequestContext;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Locale;
import java.util.Properties;


public class WeatherApi {

    /**
     * Logger object to check the flow of the code.
     */
    private static final Logger LOGGER = Logger.getLogger(WeatherApi.class);

    /**
     * Declaration of the Properties Object.
     */
    private Properties properties = null;

    private static String loadCurrentTemp(String inurl) {
        HttpURLConnection connection = null;
        LOGGER.info(inurl);
        try {
            URL url = new URL(inurl);
            LOGGER.info(url);
            connection =
                    (HttpURLConnection) url.openConnection();
            LOGGER.info("Open connection" + connection);

            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            String readLine;
            int responseCode = connection.getResponseCode();
            LOGGER.info("responseCode" + responseCode);
            if (responseCode == HttpURLConnection.HTTP_OK) {
                LOGGER.info("Inside current temp");
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                LOGGER.info("Inside current temp 1");
                StringBuilder response = new StringBuilder();

                while ((readLine = in.readLine()) != null) {
                    response.append(readLine);
                }
                LOGGER.info("Inside current temp 2");
                in.close();
                LOGGER.info("Inside current temp 3");
                return response.toString();

            }
        } catch (IOException e) {
            LOGGER.error("Exception while fetching Current Temperature API", e);

        } finally {
            if (connection != null) {
                connection.disconnect();
                LOGGER.info("Connection disconnected");
            }
        }


        return null;
    }

    public void getProperties(RequestContext context) {
        LOGGER.info("WeatherAPI : Loading Properties....");
        properties = loadProperties(context);
        LOGGER.info("WeatherAPI : Properties Loaded");
    }

    /**
     * This method will be used to load the configuration properties.
     *
     * @param context Request context object.
     */
    private Properties loadProperties(final RequestContext context) {
        LOGGER.info("loadProperties:Begin");
        PropertiesFileReader prop;
        prop = new PropertiesFileReader(context, "weather.properties");
        return prop.getPropertiesFile();

    }

    public Document getWeather(final RequestContext context) {
        getProperties(context);
        LOGGER.info("getWeather Started");
        String weatherUrl = properties.getProperty("weatherUrl", "https://qmet.com.qa/metservices/api/QMD/GetMeteoFactoryForecast");
        LOGGER.info("weatherUrl : " + weatherUrl);
        String currentTempURL = properties.getProperty("currentTempURL", "https://qweather.gov.qa/xml/EWSAll.xml");
        LOGGER.info("currentTempURL : " + currentTempURL);
        String currentCity = context.getParameterString("currentCity", "Doha");
        String temperatureScale = context.getParameterString("temperatureScale");
        String defaultCity = context.getParameterString("defaultCity", "DOHA AIRPORT");
        return callWeatherApi(weatherUrl, "WeatherResponse", currentCity, temperatureScale, currentTempURL, defaultCity);
    }

    /**
     * This method will be called from Component External for Weather Content
     * fetching.
     *
     * @param weatherUrl       The parameter passed from getWeather.
     * @param xmlRootName      XML Root Element of returned document.
     * @param currentCity      Current City.
     * @param temperatureScale Temperature Scale.
     * @param currentTempURL   Current Temperature URL.
     * @param defaultCity      Default City.
     * @return Document
     */
    public Document callWeatherApi(String weatherUrl, String xmlRootName, String currentCity,
                                   String temperatureScale, String currentTempURL, String defaultCity) {
        LOGGER.error("callWeatherApi Started");
        final String FORCAST_EN = "ForecastEn";
        final String WEATHER_RESPONSE = "WeatherResponse";
        final String SUNSET = "sunset";
        final String SUNRISE = "sunrise";
        final String VALUES = "Vals";
        final String CITIES = "Cities";
        final String CITY = "City";
        final String ELEMENT = "Elem";
        final String WEATHER = "weather";
        final String WEATHER_CODE = "weatherCode";
        final String WEATHER_AR = "weatherAr";
        final String dayEn = "dayEn";
        final String dayAr = "dayAr";

        String tempMin = null;
        String tempMax = null;
        Document doc = null;
        JSONObject getDataObject = new JSONObject();
        if (temperatureScale.equalsIgnoreCase("celcius")) {
            tempMin = "tmin";
            tempMax = "tmax";
        } else if (temperatureScale.equalsIgnoreCase("kelvin")) {
            tempMin = "tminK";
            tempMax = "tmaxK";
        } else if (temperatureScale.equalsIgnoreCase("fahrenheit")) {
            tempMin = "tminF";
            tempMax = "tmaxF";
        }
        try {
            getDataObject.put(xmlRootName, getRequest(weatherUrl));
            JSONObject forecastObject = new JSONObject(getDataObject.get(WEATHER_RESPONSE).toString());
            JSONObject weatherObject = new JSONObject(forecastObject.get(FORCAST_EN).toString());

            JSONArray jsonArrayWeather = weatherObject.getJSONArray(CITIES);
            JSONObject dataObject = new JSONObject();
            JSONObject returnObject = new JSONObject();

            JSONObject day1 = new JSONObject();
            JSONObject day2 = new JSONObject();
            JSONObject day3 = new JSONObject();
            JSONObject day4 = new JSONObject();
            day1.put(dayEn, "Today");
            day1.put(dayAr, "اليوم");
            String[] enDays = getDays("en");
            String[] arDays = getDays("ar");
            day2.put(dayEn, enDays[0]);
            day2.put(dayAr, arDays[0]);
            day3.put(dayEn, enDays[1]);
            day3.put(dayAr, arDays[1]);
            day4.put(dayEn, enDays[2]);
            day4.put(dayAr, arDays[2]);
            for (int i = 0; i < jsonArrayWeather.length(); i++) {

                JSONObject jsonobject = jsonArrayWeather.getJSONObject(i);
                String city = jsonobject.getString(CITY);
                String elem = jsonobject.getString(ELEMENT);
                LOGGER.info("city : " + city);
                if (city.equalsIgnoreCase(currentCity)) {
                    JSONArray weatherData = jsonobject.getJSONArray(VALUES);
                    if (elem.equalsIgnoreCase(SUNRISE)) {
                        dataObject.append(SUNRISE, weatherData.get(0));
                    }
                    if (elem.equalsIgnoreCase(SUNSET)) {
                        dataObject.append(SUNSET, weatherData.get(0));
                    }
                    if (elem.equalsIgnoreCase(tempMax)) {

                        day1.put("tmax", weatherData.get(0));
                        day2.put("tmax", weatherData.get(1));
                        day3.put("tmax", weatherData.get(2));
                        day4.put("tmax", weatherData.get(3));
                    }
                    if (elem.equalsIgnoreCase(tempMin)) {
                        day1.put("tmin", weatherData.get(0));
                        day2.put("tmin", weatherData.get(1));
                        day3.put("tmin", weatherData.get(2));
                        day4.put("tmin", weatherData.get(3));
                    }

                    if (elem.equalsIgnoreCase(WEATHER)) {
                        day1.put(elem, weatherData.get(0));
                        day2.put(elem, weatherData.get(1));
                        day3.put(elem, weatherData.get(2));
                        day4.put(elem, weatherData.get(3));
                    }
                    if (elem.equalsIgnoreCase(WEATHER_AR)) {
                        day1.put(elem, weatherData.get(0));
                        day2.put(elem, weatherData.get(1));
                        day3.put(elem, weatherData.get(2));
                        day4.put(elem, weatherData.get(3));
                    }
                    if (elem.equalsIgnoreCase(WEATHER_CODE)) {
                        day1.put(elem, weatherData.get(0));
                        day2.put(elem, weatherData.get(1));
                        day3.put(elem, weatherData.get(2));
                        day4.put(elem, weatherData.get(3));
                    }
                }
            }
            dataObject.append("Day1", day1);
            dataObject.append("Day2", day2);
            dataObject.append("Day3", day3);
            dataObject.append("Day4", day4);

            returnObject.put(xmlRootName, dataObject);
            String returnXML = XML.toString(returnObject);
            LOGGER.info("Weather xml" + returnXML);

            doc = Dom4jUtils.newDocument(returnXML);
            LOGGER.info("Before adding " + doc.asXML());
            String temperature = loadCurrentTemp(currentTempURL);
            LOGGER.info("temperature Loaded :" + temperature);
            String temp = "";
            if(temperature != null){
                Document document = Dom4jUtils.newDocument(temperature);
                LOGGER.info("current Data : " + document.asXML());
                temp = document.valueOf("/data/area[@name='" + currentCity + "']/parameter[@id='Temperature']/value");
                if (temp.isEmpty() || temp.equalsIgnoreCase("undefined")) {
                    temp = document.valueOf("/data/area[@name='" + defaultCity + "']/parameter[@id='Temperature']/value");

                }
            }

            LOGGER.info("current Temperature : " + temp);
            Element root = doc.getRootElement();
            Element currentTemp = root.addElement("CurrentTemp");
            currentTemp.setText(temp);
            LOGGER.error("document" + doc.asXML());
            LOGGER.error("callWeatherApi Ended");
            LOGGER.debug("After adding " + doc.asXML());
        } catch (JSONException ex) {
            LOGGER.error("Error while getting data for weather: ", ex);
        }
        return doc;
    }

    /**
     * This method will be called from Component External for Weather Content
     * fetching.
     *
     * @param url The parameter passed from callWeatherApi.
     * @return String return the weather response generated from weather Api.
     */
    public String getRequest(final String url) {
        LOGGER.error("getRequest Started");
        HttpURLConnection connection = null;
        try {
            URL urlForGetRequest = new URL(url);
            String readLine;
            connection = (HttpURLConnection) urlForGetRequest.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                while ((readLine = in.readLine()) != null) {
                    response.append(readLine);
                }
                in.close();

                return response.toString();
            } else {
                LOGGER.warn("GET NOT WORKED");
            }
        } catch (IOException e) {
            LOGGER.error("Exception while Fetching Data for Weather API: ", e);

        } finally {
            if (connection != null) {
                connection.disconnect();
                LOGGER.info("Connection disconnected");
            }
        }
        LOGGER.error("getRequest Ended");
        return null;
    }

    public String[] getDays(String language) {
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK) + 1;
        Locale locale;
        String[] days = new String[3];
        if (language.equals("ar")) {
            locale = new Locale(language, "QA");
        } else {
            locale = new Locale(language);
        }
        DateFormatSymbols dateFormat = new DateFormatSymbols(locale);
        for (int position = 0; position < 3; position++) {
            if (day == 8) {
                day = 1;
            }
            days[position] = dateFormat.getShortWeekdays()[day];
            day++;
        }
        return days;
    }

}
