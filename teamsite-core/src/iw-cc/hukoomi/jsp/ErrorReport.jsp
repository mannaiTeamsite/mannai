<%@page import="com.hukoomi.utils.Postgre"%>
<%@page import="java.sql.DriverManager"%>
<%@page import="com.hukoomi.utils.CommonUtils"%>
<%@page import="org.apache.log4j.Level"%>
<%@page import="org.apache.log4j.Logger"%>
<%@page import="java.sql.Connection"%>
<%@page import="java.sql.PreparedStatement"%>
<%@page import="java.sql.Statement"%>
<%@page import="java.sql.ResultSet"%>
<%@page import="java.sql.SQLException"%>
<%@page import="java.util.Properties"%>
<%@page import="java.io.BufferedReader"%>
<%@page import="java.io.FileInputStream"%>
<%@page import="java.io.IOException"%>
<%@page import="java.io.InputStream"%>
<%@page import="java.io.InputStreamReader"%>
<%@page import="java.io.OutputStream"%>
<%@page import="java.net.*"%>
<%@page import="java.io.*"%>

<%@page import="com.interwoven.cssdk.common.CSClient" %>
<%@page import="com.interwoven.cssdk.filesys.CSFile" %>
<%@page import="com.interwoven.cssdk.filesys.CSSimpleFile" %>
<%@page import="com.interwoven.cssdk.filesys.CSVPath" %>
<%@page import="com.interwoven.cssdk.filesys.CSExtendedAttribute" %>
<%@ page pageEncoding="UTF-8" %>
<%! Logger logger = Logger.getLogger(getClass()); %>

<%@ taglib uri="/WEB-INF/tlds/context.tld" prefix="context"%>
<context:use var="user_ctx" createIfNeeded="true" contextClass="com.interwoven.ui.teamsite.auth.CSClientContext"/>

<!DOCTYPE html>
<html>
<meta charset="UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Admin | Blog Comments</title>
    <!--<link rel="stylesheet" href="https://devauth.hukoomi.gov.qa/iw/cci/meta/no-injection/iw-mount/default/main/Hukoomi/WORKAREA/default/assets/css/bootstrap.css">-->
	<link rel="stylesheet" href="/iw-mount/default/main/Hukoomi/WORKAREA/default/assets/css/bootstrap.css">
    <link rel="stylesheet"  href="/iw-mount/default/main/Hukoomi/WORKAREA/default/assets/css/datatables.min.css"/>
 

    <style>
      .table{
        border: 1px solid #cccccc;
        margin: 20px 0;
      }
        #blog_comments{
            max-width: 1280px;
            margin: 0 auto;
        }
        .toast{
          min-width: 300px;
        }
        #approve_toast{
          background-color: #d4ffd4 !important;
        }
        #reject_toast{
          background-color: #ffd4d4 !important;
        }
		table{
        width: 100% !important;
		
    }
    table.dataTable td:last-child{
        white-space: nowrap;
    }
	th:first-child {
    min-width: 100px;
    white-space: nowrap;
	
}
.comments{width:350px !important}
    </style>
<script language="JavaScript" src="/iw-mount/default/main/Hukoomi/WORKAREA/default/assets/js/jquery.js"></script>
<script src="/iw-mount/default/main/Hukoomi/WORKAREA/default/assets/js/popper.min.js"></script>
    <script src="/iw-mount/default/main/Hukoomi/WORKAREA/default/assets/js/bootstrap.min.js"></script>
	<script type="text/javascript" src="/iw-mount/default/main/Hukoomi/WORKAREA/default/assets/js/datatables.min.js"></script>
	<script type="text/javascript">
	
	var dataArray = [];
var errorId ="";
var dbConfigPath = "/iwmnt/default/main/Hukoomi/WORKAREA/default/iw/config/properties/dbconfig.properties";
$( document ).ready(function() {
    console.log( "ready!" );
	getErrorResponse();
	$(document).on('change', '.select', function(){
    var $item = this.closest("tr") ;
    var $status = $(this).val() ;	
	requestData = {
		errorId:$item.firstElementChild.dataset.id,
		status: $status,
		"path" : dbConfigPath
	}
	 $.ajax({
        type: "POST",
        url: '/iw-cc/ErrorReport',
        enctype: 'multipart/form-data',
        data: JSON.stringify(requestData),
        processData: false,
        contentType: false,
        success: function (result) {
            console.log( "Status Updated" );
        },
        error: function (xhr, status, errorThrown) {
            console.log( "Sorry, there was a problem!" );
        }
    });

		});

});
function getErrorResponse(){
	$.ajax({
        type: "GET",
        url: '/iw-cc/ErrorReport',
        
        data: {
            
			"path" : dbConfigPath
		},
        
        success: function (result) {
            var obj = result;
			//data = JSON.parse(obj);
			dataArray = obj.comments;
			renderData();
        },
        error: function (xhr, status, errorThrown) {
            console.log( "Sorry, there was a problem!" );
        }
    });
}

function renderData(){
	var str = 'Open, Closed, Reopend';
var str_array = str.split(',');
var str1 = "";
for(var i = 0; i < str_array.length; i++) {
   
   str_array[i] = str_array[i].replace(/^\s*/, "").replace(/\s*$/, "");
   str1 += '<option value="'+str_array[i]+'">'+str_array[i]+'</option>';  
}
var str2 = '<select class="select">' + str1 + '</select>';
	console.log(str2);

	$('#table_blog_comments').DataTable( {
    data: dataArray,
    columns: [
        { data: 'errorId' },
        { data: 'broken_link' },
        { data: 'content_page' },
        { data: 'last_reported' },
        { data: 'language' },
        { data: 'status_code' },
        { data: 'count' },
        { data: 'status' },
        
		{"data":null,
            "render": function(data, type, full, meta){

                    return str2;
                
            }
		 }
        
    ],
	createdRow : function (row, data, indice){
		$(row).find("td:eq(0)").attr('data-id',data.errorId);
		$(row).find("td:eq(0)").html(indice + 1);
		$(row).find("td:eq(1)").html("<a href='"+data.broken_link+"'target = '_blank'>"+data.broken_link+"</a>");
    $(row).find("td:eq(2)").html("<a href='"+data.content_page+"'target = '_blank'>"+data.content_page+"</a>");
	},
	searching: false,
    ordering:  false
} );
	
}
	</script>


</head>

<body>

<%

//client = user_ctx.getCSClient();
//logger.error(("User Triggering the Utility: "+ user_ctx.getCSClient().getCurrentUser().getName()+"<br/>");
CSClient client = null;
				client = user_ctx.getCSClient();

%>
<!--<input type= "text" value = <%= user_ctx.getCSClient().getCurrentUser().getName() %> >-->


 <div id="blog_comments">
      


<table class="table" id= "table_blog_comments">
            <thead class="thead-dark">
              <tr>
                <th scope="col">ID</th>
                <th scope="col">Broken Link</th>
                <th scope="col">Content Page</th>
                <th scope="col">Reported On</th>
                <th scope="col">Language</th>
                <th scope="col">Status Code</th>
                <th scope="col">Count</th>
                <th scope="col">Current Status</th>
                <th scope="col">Change Status</th>
              </tr>
            </thead>

         


</table>
</div>
</body>
</html>