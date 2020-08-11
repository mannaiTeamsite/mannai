	function init(){
		var id_item = IWDatacapture.getItem('/events/Events Tab/id');		
		if(id_item.getValue() == ""){		  
		  id_item.setValue((new Date().getTime()).toString());	
		}
		// registerAutonaming("/events/Events Tab/dcr-name", "/events/Events Tab/original-dcr-name", "/events/Events Tab/lang", false);
		registerAutonaming("/events/Events Tab/dcr-name", "/events/Events Tab/original-dcr-name", null, false);
		return true;		
	} 
	
	IWEventRegistry.addItemHandler('/events/Events Tab/title', "onItemChange", ChangeInSelection);
	
	function ChangeInSelection(){
		var ptitle =  IWDatacapture.getItem("/events/Events Tab/title");		
			
		var dcrName_item = IWDatacapture.getItem("/events/Events Tab/dcr-name");	
		var title_value = ptitle.getValue().toString().toLowerCase();
		if(dcrName_item.getValue() == ""){				
			dcrName_item.setValue(title_value);
		}
	}
	
	var dCRObject = null;
	var dataFolderPath = "";

	function DCRObject(dcrnameitem, hiddenitem, languageitem, externalcall) {
		this.dcrnameitem        = dcrnameitem;
		this.hiddenitem         = hiddenitem;
		this.languageitem       = languageitem;
		this.externalcall       = externalcall;
		this.dcrname            = "";
	}
	
	function registerAutonaming(dcrnameitem, hiddenitem, languageitem, externalcall) 
	{
		console.log("Autonaming registeration start");
		//alert("Autonaming registeration start");
		var dctPath  = IWDatacapture.getDCTPath();
		dataFolderPath = dctPath.replace(/datacapture.cfg/,"data/");
		dCRObject = new DCRObject(dcrnameitem, hiddenitem, languageitem, externalcall);
		IWEventRegistry.addFormHandler("onSaveValid", autodcrname);
		//alert("Autonaming registeration end");
	}
	
	function autodcrname() 
	{
		//alert(typeof(dCRObject.externalcall));
		if(typeof(dCRObject.externalcall) == "function"){
			dCRObject.externalcall();
		}
		var value ="";
		var item = IWDatacapture.getItem(dCRObject.dcrnameitem);
		var itemType = item.getType();
		if(itemType == "select"){
			var selectedItem = item.getOptions()[item.getValue()];
			value = selectedItem.value;
		}else{
			value = item.getValue();
		}
			//alert("language item : " + dCRObject.languageitem + ", value : " + value);
		if(dCRObject.languageitem != null){
			var langaugeValue = "";
			var languageitem = IWDatacapture.getItem(dCRObject.languageitem);
			var languageitemType = languageitem.getType();
			if(languageitemType == "select"){
				langaugeValue = languageitem.getOptions()[languageitem.getValue()].value;
			}else{
				langaugeValue = languageitem.getValue();
			}
			var server = window.location.hostname;
			var params = new Object();
			params.folderpath = dataFolderPath + langaugeValue + "/";
			//IWDatacapture.callServer("https://" + server + "/iw-bin/hukoomi/makeDir.pl", params);
			dCRObject.dcrname = langaugeValue + "/" + toDCRName(value);
			//        setTimeout("wait()", 1000);
			setRecordName(dCRObject.dcrname);
		}else{
			dCRObject.dcrname = toDCRName(value);
			setRecordName(dCRObject.dcrname);
		}
	}

	function wait(){
		setRecordName();
		return true;
	}
	
	/*================================================================
		This function sets the dcr name using itemvalue
	 =================================================================*/

	function setRecordName(dcrname) {
			//alert("setRecordName : " + dcrname);
		if(IWDCRInfo.getDCRName() == "") {
			IWDCRInfo.setDCRName(dcrname, statusCallback);
		} else  {
			var oldname = IWDatacapture.getItem(dCRObject.hiddenitem).getValue();
					//alert("oldname : " + oldname);
			if (dcrname == oldname) {
				IWDatacapture.save();
			} else {
				IWDCRInfo.setDCRName(dcrname, statusCallback);
			}
		}
	}

	/*====================================================================================
		For Checking DCR Name. Whether it exists or not.
	 ===================================================================================== */

	function statusCallback()
	{
		var status = IWDCRInfo.getStatus();
		if (status == IWDCRInfo.PENDING) {
			/** re-check again in 2 seconds **/
			setTimeout("statusCallback()",2000);
		} else if (status == IWDCRInfo.AVAILABLE) {
			var namelabel = dCRObject.dcrnameitem.getLabel();
			var category  = IWDatacapture.getFormType();

			alert("\"" +  dCRObject.dcrname + "\" record exists in \"" + category + "\"."
				+ " Please provide a different value in field \"" + namelabel + "\"");

		} else if (status == IWDCRInfo.UNAVAILABLE) {
			var hiddenValue = IWDatacapture.getItem(dCRObject.hiddenitem).getValue();

			if (hiddenValue == "") {
				/** For New DCRs. **/
				IWDatacapture.getItem(dCRObject.hiddenitem).setValue(dCRObject.dcrname);
				IWDatacapture.save();
			} else {
				/** Old DCR . Saved with New name **/
				var doReplace = confirm("Are you sure you want to save changes in DCR?");

				if (doReplace) {
					var dctPath  = IWDatacapture.getDCTPath();
					var dataPath = dctPath.replace(/datacapture.cfg/,"data/");                
					IWDatacapture.getItem(dCRObject.hiddenitem).setValue(dCRObject.dcrname);
					IWDatacapture.save();
				}
			}
		} else {
			alert("Please contact TeamSite Developers. System status code '" + status  + "'.");
		}
	}

	function toDCRName(value) {
		var dcrname = value.replace(/\&/g, "and");
		dcrname = dcrname.replace(/[:',!%\"?=\/\\]/g, " ");
		dcrname = dcrname.replace(/\s+/g, "-");
		return dcrname;
	}

	function DcrToTile(value) {
		var values = new Array();
		values = value.split("-_");
		var result = values[0]+"-_"+values[1]+"-_"+toDCRName(values[2]);
		return result;
		
	}


	function handleOnSave(obj)
	{			
		 return true;
	}	
		
	function handleOnSaveDone(obj)
	{
		 return true;
	}
	