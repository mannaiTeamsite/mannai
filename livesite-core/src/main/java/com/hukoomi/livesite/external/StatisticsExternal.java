package com.hukoomi.livesite.external;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.text.DateFormatSymbols;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

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

public class StatisticsExternal {
	private String value = "value";
	/** JSON Factory object to use for Google APIs. */
	private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
	/** Logger object to check the flow of the code. */
	private final Logger logger = Logger.getLogger(StatisticsExternal.class);
	
	@SuppressWarnings("deprecation")
	public Document getStatistics(final RequestContext context) {
		Document document = DocumentHelper.createDocument();
		Element root = document.addElement("root");
		FileDal fileDal = context.getFileDal();
		String fileRoot = fileDal.getRoot();
		InputStream keyfile = fileDal.getStream(fileRoot
				+ context.getParameterString("keyfile", "/iw/config/properties/motc-oogp-4205147-849b1733bf06.json"));
		GoogleCredential credentials = getCredentials(keyfile);
		if (credentials == null) {
			logger.debug("Could not Get Credentials.");
			return document;
		}
		Analytics analytics = initializeAnalytics(context, credentials);
		if (analytics == null) {
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
		StringBuilder realtimeObjectId = getDimensionId(realtimeObjects,"rt");
		String analyticsObjects = context.getParameterString("analytics-objects", "pageviews,sessions");
		StringBuilder analyticsObjectId = getDimensionId(analyticsObjects,"ga");
		String currentYearDimensions = context.getParameterString("current-year-dimension", "deviceCategory");
		StringBuilder currentYearDimensionId = getDimensionId(currentYearDimensions,"ga");
		String analyticsDimensions = context.getParameterString("analytics-dimension", "month,deviceCategory");
		StringBuilder analyticsDimensionId = getDimensionId(analyticsDimensions,"ga");
		document = getRealtimeData(getRealtimeObj(analytics, profile, realtimeObjectId.toString()), document);
		document = getAnalyticsData(getAnalyticsObj(analytics, profile, dateFrom, dateTo, analyticsObjectId.toString()),
				document, currentYear, locale);
		document = getAnalyticsData(getAnalyticsObjWithDimensions(analytics, profile, dateFrom, dateTo,
				analyticsObjectId.toString(), currentYearDimensionId.toString()), document, currentYear, locale);
		int defaultYear = Integer.parseInt(startYear);
		for (int year = defaultYear; year <= currYear; year++) {
			dateFrom = year + "-01-01";
			dateTo = year + "-12-31";
			document = getAnalyticsData(getAnalyticsObjWithDimensions(analytics, profile, dateFrom, dateTo,
					analyticsObjectId.toString(), analyticsDimensionId.toString()), document, String.valueOf(year),
					locale);
		}
		return document;
	}
	
	
	
	public StringBuilder getDimensionId(String object, String appendStr) {
		StringBuilder dimensionId = new StringBuilder();
		for (String dimension : object.split(",")) {
			if (!dimensionId.toString().equals("")) {
				dimensionId.append(",");
			}
			dimensionId.append(appendStr+":" + dimension);
		}
		return dimensionId;
	}

	/**
	 * Initializes an Analytics service object.
	 *
	 * @return An authorized Analytics service object.
	 * @throws IOException
	 * @throws GeneralSecurityException
	 */
	 @SuppressWarnings("deprecation")
	private Analytics initializeAnalytics(RequestContext context, GoogleCredential credential) {
		try {
			HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
			String application = context.getParameterString("application", "GA-ServiceAccount");
			return new Analytics.Builder(httpTransport, JSON_FACTORY, credential)
					.setApplicationName(application).build();
		} catch (GeneralSecurityException |IOException e) {
			logger.error("Error in initializeAnalytics",e);
		} 
		return null;
	}
	 @SuppressWarnings("deprecation")
	public GoogleCredential getCredentials(InputStream keyfile) {
		try {
			return GoogleCredential.fromStream(keyfile).createScoped(AnalyticsScopes.all());
		} catch (IOException e) {
			logger.error("Error in getCredentials" , e);
		}
		return null;
	}

	private GaData getAnalyticsObj(Analytics analytics, String profileId, String dateFrom, String dateTo,
			String fields) {
		if (StringUtils.isBlank(profileId)) {
			logger.info("Profile ID is not passed.");
			return null;
		}
		try {
			logger.info("Getting data for: " + fields);
			return analytics.data().ga().get("ga:" + profileId, dateFrom, dateTo, fields)
					.setSort(fields.split(",")[0]).execute();
		
		} catch (IOException ex) {
			logger.error("error in getAnalyticsObj " , ex);
		}
		return null;
	}

	private GaData getAnalyticsObjWithDimensions(Analytics analytics, String profileId, String dateFrom, String dateTo,
			String fields, String dimension) {
		if (StringUtils.isBlank(dimension)) {
			logger.info("Dimension is not passed.");
			return null;
		}
		try {
			logger.info("Getting data for: " + dimension);
			return analytics.data().ga().get("ga:" + profileId, dateFrom, dateTo, fields)
					.setDimensions(dimension).setSort(dimension.split(",")[0]).execute();
			
		} catch (IOException ex) {
			logger.error("error in getAnalyticsObjWithDimensions" , ex);
		}
		return null;
	}

	private Document getAnalyticsData(GaData results, Document document, String year, String locale) {
		Element root = document.getRootElement();
		Element gaData = root.addElement("ga-data");
		Element yearElement = gaData.addElement("year").addAttribute(value, year);
		if (results != null && null != results.getRows() && !results.getRows().isEmpty()
				&& null != results.getColumnHeaders() && !results.getColumnHeaders().isEmpty()) {
			logger.info("Getting Realtime Data from Object");
			List<GaData.ColumnHeaders> columnHeaders = results.getColumnHeaders();
			List<List<String>> rowSets = results.getRows();
			for (int traverseRow = 0; traverseRow < rowSets.size(); traverseRow++) {
				Element deviceElement = yearElement.addElement("deviceCategory");
				for (int traverseColumn = 0; traverseColumn < columnHeaders.size(); traverseColumn++) {
					String metricName = columnHeaders.get(traverseColumn).getName();
					if (metricName.equals("ga:month")) {
						yearElement.addElement("month")
								.addAttribute(value, rowSets.get(traverseRow).get(traverseColumn)).addAttribute(
										"name", formatMonth(rowSets.get(traverseRow).get(traverseColumn), locale));
						
					} else if (metricName.equals("ga:deviceCategory")) {
						deviceElement.addAttribute("name", formatNumbers(rowSets.get(traverseRow).get(traverseColumn)));
					} else {
						deviceElement.addElement(metricName.replace("ga:", ""))
								.addText(formatNumbers(rowSets.get(traverseRow).get(traverseColumn)));
					}
				}
			}
			return document;
		}
		return document;
	}

	private RealtimeData getRealtimeObj(Analytics analytics, String profileId, String objectId) {
		if (StringUtils.isBlank(profileId)) {
			logger.debug("Profile ID is not passed.");
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
		if (results != null && null != results.getRows() && !results.getRows().isEmpty()
				&& null != results.getColumnHeaders() && !results.getColumnHeaders().isEmpty()) {
			logger.info("Getting Realtime Data from Object");
			List<RealtimeData.ColumnHeaders> columnHeaders = results.getColumnHeaders();
			List<List<String>> rowSets = results.getRows();
			for (int traverseRow = 0; traverseRow < rowSets.size(); traverseRow++) {
				for (int traverseColumn = 0; traverseColumn < columnHeaders.size(); traverseColumn++) {
					Element rtData = root.addElement("rt-data");
					rtData.addElement("name").addText(columnHeaders.get(traverseColumn).getName());
					rtData.addElement(value).addText(formatNumbers(rowSets.get(traverseRow).get(traverseColumn)));
				}
			}
			return document;
		}
		return document;
	}

	private String formatNumbers(String number) {
		logger.debug("Number before format: " + number);
		if (StringUtils.isBlank(number) || !number.matches("[0-9]+")) {
			return number;
		}
		return NumberFormat.getNumberInstance().format(Double.parseDouble(number));
	}

	private String formatMonth(String month, String language) {
		logger.debug("Month: " + month);
		logger.debug("Language: " + language);
		if (StringUtils.isBlank(month) || !month.matches("[0-9]+")) {
			return month;
		}
		Locale locale;
		if (language.equals("ar")) {
			locale = new Locale(language, "QA");
		} else {
			locale = new Locale(language);
		}
		DateFormatSymbols dateFormat = new DateFormatSymbols(locale);
		return dateFormat.getMonths()[Integer.parseInt(month) - 1];
	}

}
