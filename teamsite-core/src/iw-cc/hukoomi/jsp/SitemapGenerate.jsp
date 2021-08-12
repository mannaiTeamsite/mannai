<%@page import="com.hukoomi.generator.SitemapGenerator"%>
<%@page import="org.apache.log4j.Level"%>
<%@page import="org.apache.log4j.Logger"%>

<%@ page pageEncoding="UTF-8" %>
<%! Logger logger = Logger.getLogger(getClass()); %>

<!DOCTYPE html>
<html>
<meta charset="UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Admin | Blog Comments</title>
    <link rel="stylesheet" href="https://stgauth.hukoomi.gov.qa/iw/cci/meta/no-injection/iw-mount/default/main/Hukoomi/WORKAREA/default/assets/css/bootstrap.css">
    <link rel="stylesheet"  href="https://stgauth.hukoomi.gov.qa/iw/cci/meta/no-injection/iw-mount/default/main/Hukoomi/WORKAREA/default/assets/css/datatables.min.css"/>
 

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
    </style>
<script language="JavaScript" src="https://stgauth.hukoomi.gov.qa/iw/cci/meta/no-injection/iw-mount/default/main/Hukoomi/WORKAREA/default/assets/js/jquery.js"></script>
<script src="https://stgauth.hukoomi.gov.qa/iw/cci/meta/no-injection/iw-mount/default/main/Hukoomi/WORKAREA/default/assets/js/popper.min.js"></script>
    <script src="https://stgauth.hukoomi.gov.qa/iw/cci/meta/no-injection/iw-mount/default/main/Hukoomi/WORKAREA/default/assets/js/bootstrap.min.js"></script>
	<script type="text/javascript" src="https://stgauth.hukoomi.gov.qa/iw/cci/meta/no-injection/iw-mount/default/main/Hukoomi/WORKAREA/default/assets/js/datatables.min.js"></script>

</head>
<body>


 <div id="blog_comments">
        <nav class="navbar navbar-expand-lg navbar-light bg-light">
          
            <div class="collapse navbar-collapse" id="navbarSupportedContent">
              <div class="navbar-nav mr-auto col-sm-3">
		   <%
			SitemapGenerator.main(new String[] {"/default/main/Hukoomi"});
		   %>
                   <h1>Sitemap Generation is in Progress</h1>
              </div>
              
            </div>
          </nav>
</div>
</body>
</html>
