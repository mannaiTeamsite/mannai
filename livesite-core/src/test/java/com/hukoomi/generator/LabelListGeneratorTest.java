package com.hukoomi.generator;

import static org.junit.Assert.*;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import java.util.List;
import org.dom4j.Element;
import org.junit.Test;

public class LabelListGeneratorTest {


	public Document createXML() throws DocumentException {
		
		String stringXML = "<labels><sectionList><Value>student</Value><LabelEn>Student</LabelEn><LabelAr>طالب علم</LabelAr></sectionList></labels>";
		Document doc;
		doc = DocumentHelper.parseText(stringXML);

		return doc;
	}
	
	@Test
	public void NodeValidationTest() {

		try {
			Document categoryDoc = createXML();
			List<Node> categoryNodes = categoryDoc.selectNodes("/labels/sectionList");
            for (Node eleNode : categoryNodes) {
                String nodeKey = eleNode.selectSingleNode("Value").getText();
                 //good test
				assertEquals("Key should be student",nodeKey,"student");
            }

		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
