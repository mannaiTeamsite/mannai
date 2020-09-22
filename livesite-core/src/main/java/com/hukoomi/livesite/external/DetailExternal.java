/**
 * External Java to fetch the dcr content passed as param.
 */
package com.hukoomi.livesite.external;

import com.interwoven.livesite.runtime.RequestContext;
//import com.interwoven.livesite.runtime.model.page.RuntimePage;
import com.hukoomi.utils.CommonUtils;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;

public class DetailExternal {
/** Logger object to check the flow of the code. */
private final Logger logger = Logger.getLogger(DetailExternal.class);
/** This method will be called from Component
 * External for DCR Content fetching.
 * @param context The parameter context object passed from Component.
 *
 * @return detailDocument return the document generated from
 * the mapped DCR.
 */
@SuppressWarnings("deprecation")
public Document getContentDetail(final RequestContext context) {
    Document detailDocument = DocumentHelper.createDocument();
    CommonUtils commonUtils = new CommonUtils();
    String paramLocale = context.getParameterString("locale", "en");
    logger.info("paramLocale : " + paramLocale);
    try {
        detailDocument = commonUtils.getDCRContent(context);
        if (detailDocument != null) {
            /*processHtmlMetadata(context, detailDocument);*/
            Node titleNode = detailDocument
                    .selectSingleNode("/root/information/title");
            if (titleNode != null) {
                String title = titleNode.getStringValue();
                context.getPageScopeData().put("record.title",
                        title);
                logger.info("Meta Title : " + title);
            }
        }
    } catch (Exception e) {
        logger.error("Error fetching detail content for Blog in record "
                + context.getParameterString("record"), e);
    }
    return detailDocument;
}
}
