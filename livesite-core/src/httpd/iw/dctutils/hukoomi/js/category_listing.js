	/**
	 *  Initializes the FormAPI objects.
	 */
	function init()
	{	
		categoryList(IWDatacapture.getItem("/Root/Service Personalization/LifeStage"));
		subCategoryList(IWDatacapture.getItem("/Root/Service Personalization/LifeStage"));			
		return true;
	}
	/**
	 * OnSave handler.
	 */
	function handleOnSave(obj)
	{
		 return true;
	}
	/**
	 * OnSaveDone handler.
	 */
	function handleOnSaveDone(obj)
	{
		 return true;
	}
	// form handlers
	IWEventRegistry.addFormHandler("onFormInit", init);
	IWEventRegistry.addFormHandler("onSave", handleOnSave);
	IWEventRegistry.addFormHandler("onSaveDone", handleOnSaveDone);
	
	function categoryList(item){
		alert(item.getValue());
	}
	function subCategoryList(item){
		alert(item.getValue());
	}
	
	function subCategoryList(item){
		if(!(item.getValue()==null)){
		//alert(item.getOptions()[item.getValue()].value);
		linkReset();
		
			//alert(item.getValue());
		var server = window.location.hostname;
		var params = new Object();
		params.category=item.getOptions()[item.getValue()].value;
		alert(category);
		params.listNode=rootElementName+"/list";
		params.nodeXpath="/master/master-data/category [@categoryValue='"+item.getOptions()[item.getValue()].value+"']/subCategory";
		IWDatacapture.callServer("http://" + server + "/iw-bin/Mahindra-NewsRoom/subdirectory-listing.cgi", params);
		}				
	}
	
	function linkReset(){
								
		var cmb = IWDatacapture.getItem(rootElementName+"/industry");
		cmb.setValue(null);
		cmb.setOptions(null);
		var cmb_options = cmb.getOptions(); 
		if(!(cmb_options==null)){
			for (var zz = 1; zz<=cmb_options.length; zz++) {
				//alert(zz+" - "+cmb.getOptions(zz));
				cmb.removeOption(zz);
				cmb.removeOption(cmb_options.length-zz);
			}
		}
		
		var sub = new Array();
		 sub = IWDatacapture.getItem(rootElementName+"/list").getValue().split(",");
		 //alert(sub);
		 itemList = IWDatacapture.getItem(rootElementName+"/industry");
		 for (index = 0; index < sub.length; index++) { 
		 //alert(sub[index]);
			 newOption = new Option(sub[index], sub[index], false, sub[index]==val?true:false);
			 itemList.addOption(newOption);
		} 
		
	}