/*
 *================================================================
 *      Document   	: autodcrname.js
 *      Purpose		: Generating Dynamic DCR Name.
 *      DevelopedBy	: Hayagreeva Consulting Pvt Ltd.
 *      Author  	: Karan Amarnani
 *      Date            : November 28th, 2011
 *================================================================
 */



var dCRObject = null;
var dataFolderPath = "";

function DCRObject(dcrnameitem, hiddenitem, languageitem, externalcall) {
    this.dcrnameitem        = dcrnameitem;
    this.hiddenitem         = hiddenitem;
    this.languageitem       = languageitem;
    this.externalcall       = externalcall;
    this.dcrname            = '';
}

/**
 *
 * externalcall - Pass 'true' if some DCT specific task has to be perform and
 * write that logic inside external method inside DCT specific javascript file.
 */

function registerAutonaming(dcrnameitem, hiddenitem, languageitem, externalcall) 
{
    //alert("Autonaming registeration start");
    var dctPath  = IWDatacapture.getDCTPath();
    dataFolderPath = dctPath.replace(/datacapture.cfg/,"data/");
    dCRObject = new DCRObject(dcrnameitem, hiddenitem, languageitem, externalcall);
    //IWEventRegistry.addFormHandler("onSaveValid", autodcrname);
    alert("Autonaming registeration end");
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

