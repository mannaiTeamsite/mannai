/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hukoomi.inline;

import java.io.File;
import java.util.List;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.SAXReader; 


public class FetchMasterDataPersona {

    public static void main(String[] args) {
	//	System.out.println("in main method");
	//	new FetchMasterDataPersona().fetchMasterData(args[0],args[1]);
		new FetchMasterDataPersona().fetchMasterData(args[0]);
	//	new FetchMasterDataPersona().fetchMasterData();
    }
    
    //public void fetchMasterData(String dcrName, String ListType) {
		 public void fetchMasterData(String dcrName) {
		//System.out.println("in fetchMasterData method");
		//System.out.println("dcrName : "+dcrName);
		//System.out.println("ListType : "+ListType);
        File master_data_file = new File("/iwmnt/default/main/Hukoomi/WORKAREA/default/templatedata/Taxonomy/Persona/data/en/"+dcrName);
        if (master_data_file.exists()) {
            try {
                SAXReader reader = new SAXReader();
                Document master_data = reader.read(master_data_file);
                System.out.println("<select>");
                    List<Node> nodes = master_data.selectNodes("/Root/Persona");
                    for(Node node:nodes){
                        System.out.println("<option label=\""+node.selectSingleNode("Label").getText()+"\" value=\""+node.selectSingleNode("Value").getText()+"\"></option>");
					}
                System.out.println("</select>");

            } catch (DocumentException ex) {
                System.out.println("Exception While Reading File");
                System.out.println(ex.getMessage());
                System.out.println(ex.getLocalizedMessage());
                System.out.println(ex.getCause());
            }
        }
    }
	
}
