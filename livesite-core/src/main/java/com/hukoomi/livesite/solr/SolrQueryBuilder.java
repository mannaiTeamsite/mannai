package com.hukoomi.livesite.solr;

import com.interwoven.livesite.runtime.RequestContext;
import com.hukoomi.utils.PropertiesFileReader;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class SolrQueryBuilder {
    /** Default query to fetch all solr content. */
    public static final String DEFAULT_QUERY = "*:*";
    /** Default query to fetch all solr content. */
    public static final String UTF = "UTF-8";
    /** Logger object to check the flow of the code.*/
    private final Logger logger =
            Logger.getLogger(SolrQueryBuilder.class);
    /** Map HashMap that
     *  will define the param-value set
     *  in the solr query url.
     */
    private final Map<String, String> parameterMap =
            new HashMap<String, String>();
    /** baseUrl String that
     *  will contain (host + core) value
     *  in the solr query url.
     */
    private String baseUrl;
    /** baseQuery String that
     * will hold search string
     * of solr query url. */
    private String baseQuery;
    /** rows int that
     * will define the number of results
     * fetched by the solr query url. */
    private int rows = 0;
    /** start int that
     * will define the start index
     * of results fetched by the
     * solr query url. */
    private int start = 0;
    /** mltFl int String
     * will define the mlt_fl
     * attribute to the
     * solr query url. */
    private String mltFl;
    /** Declare fields String that
     * will define the field filter
     * of results fetched by the
     * solr query url. */
    private String fields;
    /** Declare enableMLT boolean that
     * will define whether to set mlt_fl
     * of results fetched by the
     * solr query url. */
    private boolean enableMLT;
    /** sort String that
     * will define sort field
     * of results fetched by the
     * solr query url. */
    private String sort;
    /** fieldQuery String that
     * will define field & its values
     * to be considered for the
     * solr query url. */
    private String fieldQuery;
    /** requestHandler String that
     * will define solr request handler
     * in the solr query url. */
    private String requestHandler;
    /** groupingField String that
     * will define the group field to group
     * the results fetching from solr query url. */
    private String groupingField;
    /** facetField String that
     * will define the facet field to group
     * the results fetching from solr query url. */
    private String facetField;

    /** This method will be called from Component
     * External Java for solr query building.
     * @param context The parameter context object passed from Component.
     */
    public SolrQueryBuilder(final RequestContext context) {
        init(context);
    }

    /** This method will be called for solr query building.
     * @param context The parameter context object passed from Component.
     */
    private void init(final RequestContext context) {
        logger.info("Initialising Solr Query Builder");
        PropertiesFileReader propertyFileReader = new PropertiesFileReader(
                context, "solrconfig.properties");
        Properties properties = propertyFileReader.getPropertiesFile();
        final String solrHost = context.getParameterString("solrHost",
                properties.getProperty("solrHost"));
        logger.debug("Solr Host: " + solrHost);
        final String solrCore = context.getParameterString("solrCore");
        logger.debug("Solr Core: " + solrCore);
        this.baseUrl = solrHost + "/" + solrCore;
        logger.debug("Solr Base URL: " + baseUrl);
        this.requestHandler = context.getParameterString("requestHandler",
                properties.getProperty("requestHandler"));
        logger.debug("Solr Request Handler: " + requestHandler);
        try {
            this.baseQuery = URLDecoder.decode(context
                    .getParameterString("baseQuery", DEFAULT_QUERY), UTF);
            logger.debug("Solr Base Query: " + baseQuery);
        } catch (UnsupportedEncodingException e) {
            logger.error("Unable to decode baseQuery="
                    + context.getParameterString("baseQuery",
                    DEFAULT_QUERY), e);
        }

        try {
            this.fieldQuery = URLDecoder.decode(context
                    .getParameterString("fieldQuery", ""), UTF);
            logger.debug("Solr Field Query: " + fieldQuery);
        } catch (UnsupportedEncodingException e) {
            logger.error("Unable to decode fieldQuery="
                    + context.getParameterString("fieldQuery"), e);
        }

        try {
            this.groupingField = URLDecoder.decode(context
                    .getParameterString("groupingField", ""), UTF);
            logger.debug("Solr Grouping Field: " + fieldQuery);
        } catch (UnsupportedEncodingException e) {
            logger.error("Unable to decode groupingField="
                    + context.getParameterString("groupingField"), e);
        }

        try {
            this.facetField = URLDecoder.decode(context
                    .getParameterString("facet", ""), UTF);
            logger.debug("Solr Facet Fields: " + fieldQuery);
        } catch (UnsupportedEncodingException e) {
            logger.error("Unable to decode facet Field="
                    + context.getParameterString("facet"), e);
        }

        String strRows = context.getParameterString("rows", "9");
        try {
            if (StringUtils.isNotBlank(strRows)) {
                this.rows = Integer.parseInt(strRows);
            }
        } catch (NumberFormatException e) {
            logger.error("Invalid value for parameter::rows: " + strRows, e);
        }

        if (this.rows <= 0) {
            logger.warn("SOLR row parameter value provided less "
                    + "than or equal to zero, defaulting to configuration");
            this.rows = Integer.parseInt(properties.getProperty("rows"));
        }
        logger.debug("Solr Result Rows: " + this.rows);

        String strStart = context.getParameterString("start", "0");
        try {
            if (StringUtils.isNotBlank(strStart)) {
                this.start = Integer.parseInt(strStart);
            }
        } catch (NumberFormatException e) {
            logger.error("Invalid value for parameter::start: " + strStart, e);
        }

        if (this.start < 0) {
            logger.warn("SOLR start parameter value provided"
                    + " less than zero, defaulting to zero");
            this.start = 0;
        }
        logger.debug("Solr Result Rows: " + this.start);

        this.sort = context.getParameterString("sort",
                properties.getProperty("sort"));
        logger.info("Solr Result Sort: " + this.sort);
        this.mltFl = context.getParameterString("mlt_fl",
                properties.getProperty("mlt_fl"));
        logger.info("Solr Result mltFl: " + this.mltFl);
    }

    /**
     * Add base query  to URL.
     * @param baseQueryValue set base query for solr query url.
     *
     * @return this set base query to solr url.
     */
    public SolrQueryBuilder addQuery(final String baseQueryValue) {
        this.baseQuery = baseQueryValue;
        return this;
    }

    /**
     * Add fields to solr query url.
     * @param fieldsValue fields params to solr query
     *
     * @return this set fields to solr url.
     */
    public SolrQueryBuilder addFields(final String fieldsValue) {
        this.fields = fieldsValue;
        return this;
    }

    /**
     * Set MLT field in solr query.
     *
     * @return this set mlt_ft solr attribute.
     */
    public SolrQueryBuilder enableMLT() {
        this.enableMLT = true;
        return this;
    }

    /**
     * Add Parameter for the Query.
     * @param paramName set field query param to solr query
     * @param paramValue set field query value to be matched
     *                  in solr query.
     *
     * @return this set solr query params.
     */
    public SolrQueryBuilder addQueryParam(
            final String paramName, final String paramValue) {
        this.parameterMap.put(paramName, paramValue);
        return this;
    }

    /**
     * Specify requestHandler to be used for SolrQuery through external java
     * code. This method should be used only when logic requires multiple SOLR
     * queries using different requestHandlers. "requestHandler" can passed
     * from Commponent as RequestContext parameter. If not provided the
     * configuration defaults to requestHandler property provided in SOLR
     * configurations.
     * @param requestHandlerValue set requst handler to solr query.
     *
     * @return this set requestHandler to solr query url.
     */
    public SolrQueryBuilder useRequestHandler(
            final String requestHandlerValue) {
        this.requestHandler = requestHandlerValue;
        return this;
    }

    /**
     * Provide field query through Component external code this
     * method would override FieldQuery parameter provided in
     * request context and also default property configuration if provided.
     * @param fieldQueryValue to set solr fieldQuery values.
     *
     * @return this set fieldQuery solr attribute
     */
    public SolrQueryBuilder addFieldQuery(final String fieldQueryValue) {
        this.fieldQuery = fieldQueryValue;
        return this;
    }

    /**
     * Generate solr query from various context params and solr config
     * properties file to return a solr query url.
     *
     * @return sb append all solr query params to a string builder
     * return it as a final url string.
     */
    public String build() {
        StringBuilder sb = new StringBuilder(this.baseUrl);

        sb.append("/" + this.requestHandler);

        sb.append("?q=" + (StringUtils.isNotBlank(this.baseQuery)
                ? this.baseQuery : DEFAULT_QUERY));

        if (StringUtils.isNotBlank(fields)) {
            sb.append("&fl=" + this.fields);
        }

        if (enableMLT) {
            sb.append("&mlt.fl=" + this.mltFl);
        }

        if (StringUtils.isNotBlank(this.fieldQuery)) {
            sb.append("&fq=" + this.fieldQuery);
        }

        if (StringUtils.isNotBlank(this.groupingField)) {
            sb.append("&group=true").append("&group.field="
                    + this.groupingField).append("&group.limit="
                    + Integer.toString(this.rows));
        }

        if (StringUtils.isNotBlank(this.facetField)) {
            sb.append("&facet=true");
            String[] values = this.facetField.split(",");
            for (int i = 0; i < values.length; i++) {
                sb.append("&facet.field=" + values[i]);
            }
        }

        for (Map.Entry<String, String> entry : parameterMap.entrySet()) {
                sb.append("&" + entry.getKey() + "=" + entry.getValue());
        }

        sb.append("&rows=" + Integer.toString(this.rows));

        if (this.start > 0) {
            sb.append("&start=" + Integer.toString(this.start));
        }

        if (StringUtils.isNotBlank(this.sort)) {
            sb.append("&sort=" + this.sort);
        }
        logger.debug("Generated Solr Query: " + sb.toString());
        return sb.toString();
    }

    /**
     * Set rows of solr results.
     *
     * @param row number of solr result docs to be fetched.
     *
     * @return this set rows solr attribute.
     */
    public SolrQueryBuilder setRows(final int row) {
        this.rows = row;
        return this;
    }
}
