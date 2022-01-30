/*
 * External Java to fetch the dcr content passed as param.
 */
package com.hukoomi.livesite.external;

import org.apache.log4j.Logger;
import org.dom4j.Document;

import com.hukoomi.utils.CommonUtils;
import com.interwoven.livesite.runtime.RequestContext;

public class DetailExternal {
	/** Logger object to check the flow of the code. */
	private final Logger logger = Logger.getLogger(DetailExternal.class);

	/**
	 * This method will be called from Component External for DCR Content fetching.
	 * Throws DCRNotFound exception if the DCR is not found.
	 * 
	 * @param context The parameter context object passed from Component.
	 *
	 * @return detailDocument return the document generated from the mapped DCR.
	 */
	@SuppressWarnings("deprecation")
	public Document getContentDetail(final RequestContext context) {
		CommonUtils commonUtils = new CommonUtils();
		String ignoreDCRNotFoundError = context.getParameterString("ignoreDCRNotFoundError", "false");
		Document detailDocument = commonUtils.getDCRContent(context);
		if (detailDocument == null && ignoreDCRNotFoundError.equals("false")) {
			commonUtils.throwDCRNotFoundError(context, "No Content Record found");
		}
		if (!context.getParameterString("detail-page", "true").equals("true")) {
			logger.info("The Component is not present on a detail page. Skipping the Dynamic metadata values.");
			return detailDocument;
		}
		commonUtils.generateSEOMetaTagsForDynamicContent(detailDocument, context);
		return detailDocument;
	}
}
