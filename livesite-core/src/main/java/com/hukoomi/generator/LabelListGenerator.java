/**
 * Generator Java to fetch and List DCR content into component Datums
 */
package com.hukoomi.generator;

import java.io.IOException;
import java.io.InputStream;
import org.dom4j.Document;
import org.dom4j.Node;
import com.interwoven.livesite.dom4j.Dom4jUtils;
import com.interwoven.livesite.external.PropertyContext;
import java.util.List;
import org.apache.log4j.Logger;
import org.dom4j.Element;

public class LabelListGenerator {

    /** Logger object to check the flow of the code. */
    private static final Logger LOGGER =
            Logger.getLogger(LabelListGenerator.class);
    /** This method will be called from Component
     * Generator Datum for Content Listing.
     * @param context The parameter context object passed from Component.
     *
     * @return optionsDoc return the option doc generated from
     * the mapped DCR.
     */
    public Document getLabelOptions(final PropertyContext context) {
        final String xpath = context.getParameters().get("xpath") != null
                ? (String) context.getParameters().get("xpath")
                : "/labels/sectionList";
        return getOptions(context, xpath);
    }
    /** This method will take context params and
     * xpath value to iterate through the
     * mapped DCR and write the Option Doc.
     * @param context The parameter context object passed from Component.
     * @param xpath The parameter from Component to iterate through
     * nodes of the mapped DCR.
     *
     * @return optionsDoc return the option doc generated from
     * the mapped DCR.
     */
    public Document getOptions(final PropertyContext context,
                                final String xpath) {
        Document optionsDoc = Dom4jUtils.newDocument();
        Element eleList = optionsDoc.addElement("Options");
        String value = "Value";
        String label = "Display";
        String record = "record";
        String dctPath = context.getParameters().get("dctPath") != null
                ? (String) context.getParameters().get("dctPath")
                : "/templatedata/Static/Labels/data/";
        LOGGER.info("dctPath Param : " + dctPath);
        String sitePath = context.getSite().getPath();
        sitePath = sitePath.replaceAll("/sites/.*", "");
        String recordName = context.getParameters().get(record) != null
                ? (String) context.getParameters().get(record)
                : "generic";
        LOGGER.info("record Param : " + record);
        LOGGER.info("Category DCR : " + sitePath + dctPath
                        + recordName);
        InputStream fis = context.getFileDAL().getStream(sitePath + dctPath
                        + recordName);
        try {
            Document categoryDoc = Dom4jUtils.newDocument(fis);
            LOGGER.info("XPath Param : " + xpath);
            List<Node> categoryNodes = categoryDoc.selectNodes(xpath);
            for (Node eleNode : categoryNodes) {
                String nodeKey =
                        eleNode.selectSingleNode("Value").getText();
                String nodeEnLabel =
                        eleNode.selectSingleNode("LabelEn").getText();
                String nodeArLabel =
                        eleNode.selectSingleNode("LabelAr") != null
                                ? "|" + eleNode.selectSingleNode("LabelEn")
                                .getText() : "";
                LOGGER.info("Option Tag : " + "<Option><Value>" + nodeEnLabel
                        + nodeArLabel + "</Value><Display>" + nodeKey
                        + "</Display></Option>");
                Element eleOption = eleList.addElement("Option");
                eleOption.addElement(value).addText(nodeEnLabel
                        + nodeArLabel);
                eleOption.addElement(label).addText(nodeKey);
            }
        } catch (Exception ex) {
            LOGGER.error(ex.getClass().getCanonicalName()
                    + " : " + ex.getMessage(), ex);
            Element eleException = optionsDoc.addElement("Option");
            eleException.addElement(label).addText(ex.getMessage());
            eleException.addElement(value)
                        .addText(ex.getClass().getSimpleName());
        } finally {
            try {
                fis.close();
            } catch (IOException ex) {
                LOGGER.error("IOException while closing DCR input stream", ex);
            }
        }

        return optionsDoc;
    }
}
