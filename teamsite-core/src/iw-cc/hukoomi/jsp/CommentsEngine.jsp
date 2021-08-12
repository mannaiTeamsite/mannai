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

<script>
var dataArray = [];
var blogId ="";
var dbConfigPath = "/iwmnt/default/main/Hukoomi/WORKAREA/default/iw/config/properties/dbconfig.properties";
$( document ).ready(function() {
    console.log( "ready!" );
	getBlogs();
	$(document).on('click', '.approve', function(){
	var $item = this.closest("tr") ;
	requestData = {
		commentId:$item.firstElementChild.dataset.id,
		status: "Approved",
		"path" : "/iwmnt/default/main/Hukoomi/WORKAREA/default/iw/config/properties/dbconfig.properties",
		"blogpath" : "/iwmnt/default/main/Hukoomi/WORKAREA/default/iw/config/properties/blogcomment.properties"
	}
			    $.ajax({
        type: "POST",
        url: '/iw-cc/reviewcomment',
        enctype: 'multipart/form-data',
        data: JSON.stringify(requestData),
        processData: false,
        contentType: false,
        success: function (result) {
            var obj = result;
			$("#table_blog_comments").dataTable().fnDestroy();
            getComments(blogId);
        },
        error: function (xhr, status, errorThrown) {
            console.log( "Sorry, there was a problem!" );
        }
    });

		});
		
		$(document).on('click', '.reject', function(){
			var $item = this.closest("tr") ;
	requestData = {
		commentId:$item.firstElementChild.dataset.id,
		status: "Rejected",
		"path" : "/iwmnt/default/main/Hukoomi/WORKAREA/default/iw/config/properties/dbconfig.properties",
		"blogpath" : "/iwmnt/default/main/Hukoomi/WORKAREA/default/iw/config/properties/blogcomment.properties"
	}
			    $.ajax({
        type: "POST",
        url: '/iw-cc/reviewcomment',
        enctype: 'multipart/form-data',
        data: JSON.stringify(requestData),
        processData: false,
        contentType: false,
        success: function (result) {
            var obj = result;
			$("#table_blog_comments").dataTable().fnDestroy();
            getComments(blogId);
        },
        error: function (xhr, status, errorThrown) {
            console.log( "Sorry, there was a problem!" );
        }
    });
		});
    $("#selectBlog").change(function() {
	console.log( "Handler for .change() called." );
	blogId = $(this).val();
	$("#table_blog_comments").dataTable().fnDestroy();
	getComments(blogId);
});


});
function getBlogs(){
	$.ajax({
        type: "GET",
        url: '/iw-cc/reviewcomment',
        
        data: {
			"action" : "getBlogs",
			"path" : dbConfigPath
		},
        
        success: function (result) {
            var obj = result;
			//data = JSON.parse(obj);
			dataArray = obj.comments;
			loadBlogData(dataArray);
			
			blogId =  $("#selectBlog").val();
			getComments(blogId);
        },
        error: function (xhr, status, errorThrown) {
            console.log( "Sorry, there was a problem!" );
        }
    });
}
function loadBlogData(dataArray){
	$('#selectBlog').append('<option id ="0" value = "0"  >ALL</option>');
	length = dataArray.length;
	for(i=0;i<length;i++)
	$('#selectBlog').append('<option id ="' + dataArray[i].blogId+'" value = "'+dataArray[i].blogId+'"  >'+dataArray[i].Title+'</option>');
}
function renderData(){
	$('#table_blog_comments').DataTable( {
    data: dataArray,
    columns: [
        { data: 'CommentId' },
        { data: 'UserName' },
        { data: 'IP' },
        { data: 'Comment' },
        { data: 'BlogURL' },
		{"data":null,
            "render": function(data, type, full, meta){
                    return '<button type="button" class="approve btn btn-success btn-sm" ">Approve</button> <button type="button" class="reject btn btn-danger btn-sm" >Reject</button>'
                
            }
		 }
        
    ],
	createdRow : function (row, data, indice){
		$(row).find("td:eq(0)").attr('data-id',data.CommentId);
		$(row).find("td:eq(0)").html(indice + 1);
		$(row).find("td:eq(4)").html("<a href='"+data.BlogURL+"'target = '_blank'>"+data.BlogURL+"</a>");
	},
	searching: false,
    ordering:  false
} );
	
}
function getComments(blogId){
	$.ajax({
        type: "GET",
        url: '/iw-cc/reviewcomment',
        
        data: {
			"action":"getComments",
			"blogId": blogId,
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
        <nav class="navbar navbar-expand-lg navbar-light bg-light">
          
            <div class="collapse navbar-collapse" id="navbarSupportedContent">
              <div class="navbar-nav mr-auto col-sm-3">
                <div class="form-group">
                    <label for="selectBlog">Select Blog</label>
                    <select class="form-control" id="selectBlog">
                    </select>
                  </div>
                  
              </div>
              
            </div>
          </nav>


<table class="table" id= "table_blog_comments">
            <thead class="thead-dark">
              <tr>
                <th scope="col">Comment No</th>
                <th scope="col">Username</th>
                <th scope="col">IP Details</th>
                <th scope="col" style="with:200px" class="comments">Comments</th>
                <th scope="col">Blog Url</th>
                <th scope="col">Action</th>
              </tr>
            </thead>

         


</table>
</div>
</body>
</html>