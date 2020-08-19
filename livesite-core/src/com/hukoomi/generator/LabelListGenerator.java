package com.hukoomi.generator;

import java.io.*;
import org.dom4j.Document;
import org.dom4j.Node;
import com.interwoven.livesite.dom4j.Dom4jUtils;
import com.interwoven.livesite.external.PropertyContext;
import java.util.List;
import org.apache.log4j.Logger;
import org.dom4j.Element;

public class LabelListGenerator {

    private static final Logger logger = Logger.getLogger(LabelListGenerator.class);

    @SuppressWarnings("unchecked")
    public Document getLabelOptions(PropertyContext context) {
        String xpath = context.getParameters().get("xpath") != null ? (String)context.getParameters().get("xpath"):"/labels/sectionList";
        return getOptions(context,xpath);
    }

    public Document getOptions(PropertyContext context, String xpath){
        Document optionsDoc = Dom4jUtils.newDocument();
        Element eleList = optionsDoc.addElement("Options");
        String value = "Value";
        String label = "Display";
        String record = "record";
        String dctPath = context.getParameters().get("dctPath") != null ? (String)context.getParameters().get("dctPath"):"/templatedata/Static/Labels/data/";

        String sitePath = context.getSite().getPath();
        sitePath = sitePath.replaceAll("/sites/.*","");
        String recordName = context.getParameters().get(record) != null ? (String)context.getParameters().get(record):"generic";
        logger.info("Category DCR : " + sitePath.concat(dctPath)+recordName);
        InputStream fis = context.getFileDAL().getStream(sitePath.concat(dctPath)+recordName);
        try {
            Document categoryDoc = Dom4jUtils.newDocument(fis);
            logger.info("XPath : " + xpath);
            List<Node> categoryNodes = categoryDoc.selectNodes(xpath);
            for (Node eleNode : categoryNodes) {
                String nodeKey = eleNode.selectSingleNode("Value").getText();
                String nodeEnLabel = eleNode.selectSingleNode("LabelEn").getText();
                String nodeArLabel = eleNode.selectSingleNode("LabelAr").getText();

                Element eleOption = eleList.addElement("Option");
                eleOption.addElement(value).addText(nodeEnLabel + "|" + nodeArLabel);
                eleOption.addElement(label).addText(nodeKey);
            }
        } catch (Exception ex) {
            logger.error(ex.getClass().getCanonicalName() + " : " + ex.getMessage(), ex);
            Element eleException = optionsDoc.addElement("Option");
            eleException.addElement(label).addText(ex.getMessage());
            eleException.addElement(value).addText(ex.getClass().getSimpleName());
        } finally {
            try {
                fis.close();
            } catch (IOException ex) {
                logger.error("IOException while closing DCR input stream", ex);
            }
        }

        return optionsDoc;
    }
}
