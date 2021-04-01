<?xml version="1.0" encoding="UTF-8" ?>

<!DOCTYPE html-entities SYSTEM "http://www.interwoven.com/livesite/xsl/xsl-html.dtd">

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

  <xsl:include href="http://www.interwoven.com/livesite/xsl/HTMLTemplates.xsl"/>
  <xsl:include href="http://www.interwoven.com/livesite/xsl/StringTemplates.xsl"/>

  <xsl:output method="html" encoding="UTF-8"/>

  <!-- globals -->
  <xsl:variable name="crlf">
<xsl:text><![CDATA[
]]></xsl:text>
  </xsl:variable>

  <!-- holds the TeamSite hostname -->
  <xsl:variable name="teamSiteHostName" select="/EmailTask/Environment/Property[@Name='teamSiteHostName']"/>

  <xsl:variable name="activeTask" select="/EmailTask/Job/Tasks/Task[@Active='true']"/>

  <!-- area vpath of the active task -->
  <xsl:variable name="areaVpath" select="$activeTask/AreaVpath"/>

  <!-- holds the ID of the first active task ( should be the email task ) -->
  <xsl:variable name="activeTaskId" select="$activeTask/@Id"/>

	<!-- holds the reviewer task name. -->
	<xsl:variable name="reviewTaskName" select="'Review Files'"/>
	<xsl:variable name="reviewTask" select="/EmailTask/Job/Tasks/Task[Name=$reviewTaskName]"/>
	<xsl:variable name="reviewTaskId" select="/EmailTask/Job/Tasks/Task[Name=$reviewTaskName]/@Id"/>

	<xsl:variable name="reviewerName">
		<xsl:choose>
			<xsl:when test="$reviewTask/Owner/Name != ''">
				<xsl:value-of select="$reviewTask/Owner/Name"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$reviewTask/Owner/@Id"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:variable>
  
  <!-- holds the revise task name. -->
  <xsl:variable name="reviseTaskName" select="'Revise Files'"/>
  <xsl:variable name="reviseTask" select="/EmailTask/Job/Tasks/Task[Name=$reviseTaskName]"/>
  <xsl:variable name="reviseTaskId" select="$reviseTask/@Id"/>

  <xsl:variable name="authorName">
    <xsl:choose>
      <xsl:when test="/EmailTask/Job/Owner/Name != ''">
        <xsl:value-of select="/EmailTask/Job/Owner/Name"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="/EmailTask/Job/Owner/@Id"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

  <xsl:variable name="safeAuthorName">
    <xsl:value-of select="translate($authorName,' ','+')"/>
  </xsl:variable>

  <!-- if a submit has occurred as part of the job, this variable is true -->
  <xsl:variable name="submitOccurred">
    <xsl:choose>
      <xsl:when test="(//Tasks[@Type='submittask']) and (//Tasks[@Type='submittask']/ActivationTime != '')">
        <xsl:text>true</xsl:text>
      </xsl:when>
      <xsl:otherwise>
        <xsl:text>false</xsl:text>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

  <!-- /globals -->

  <!-- the main execution template -->
  <xsl:template match="/">
    <html>
      <xsl:apply-templates mode="head" select="/"/>
      <body>

        <!-- main content -->
        <!-- <b> -->
          <!-- <xsl:choose> -->
            <!-- <xsl:when test="//Job/Owner/Name != ''"> -->
              <!-- <xsl:value-of select="//Job/Owner/Name"/> -->
            <!-- </xsl:when> -->
            <!-- <xsl:otherwise> -->
              <!-- <xsl:value-of select="//Job/Owner/@Id"/> -->
            <!-- </xsl:otherwise> -->
          <!-- </xsl:choose> -->
        <!-- </b> -->
		<b>
			<xsl:value-of select="$reviewerName"/>
        </b>
        <xsl:text> has assigned you the following files for your approval</xsl:text>

        <!-- <xsl:variable name="latestReviseComment"> -->
          <!-- <xsl:for-each select="//Job/Comments/Comment[@TaskId=$reviseTaskId]"> -->
            <!-- <xsl:sort select="CreationDate/@Time" order="descending" data-type="number"/> -->
            <!-- <xsl:if test="( position() = 1 ) and ( CreationDate/@Time &gt; $reviseTask/ActivationDate/@Time )"> -->
              <!-- <xsl:value-of select="Message" disable-output-escaping="yes"/> -->
            <!-- </xsl:if> -->
          <!-- </xsl:for-each> -->
        <!-- </xsl:variable> -->

        <!-- <xsl:variable name="commentsForReviewer" select="$activeTask/Variables/Variable[Name='Comments']/Value"/> -->

        <!-- <xsl:choose> -->
          <!-- <xsl:when test="$latestReviseComment = '' and $commentsForReviewer = ''">.</xsl:when> -->
          <!-- <xsl:otherwise> -->
            <!-- <xsl:text>, with the following comments.</xsl:text> -->
            <!-- <div class="indent description"> -->
              <!-- <xsl:variable name="commentText"> -->
                <!-- <xsl:choose> -->
                  <!-- <xsl:when test="$latestReviseComment != ''"><xsl:value-of select="$latestReviseComment"/></xsl:when> -->
                  <!-- <xsl:otherwise><xsl:value-of select="$commentsForReviewer"/></xsl:otherwise> -->
                <!-- </xsl:choose> -->
              <!-- </xsl:variable> -->
              <!-- <xsl:call-template name="replace-substring"> -->
                <!-- <xsl:with-param name="original" select="$commentText"/> -->
                <!-- <xsl:with-param name="substring" select="$crlf"/> -->
                <!-- <xsl:with-param name="replacement" select="'&lt;br/&gt;'"/> -->
              <!-- </xsl:call-template> -->
            <!-- </div> -->
          <!-- </xsl:otherwise> -->
        <!-- </xsl:choose> -->
		
		<!-- get the reviewer's comment from the active task, making sure that it's the correct if we're got multiple iterations -->
        <xsl:variable name="reviewerComment">
          <xsl:for-each select="//Job/Comments/Comment[@TaskId=$reviewTaskId]">
            <xsl:sort select="CreationDate/@Time" order="descending" data-type="number"/>
            <xsl:if test="( position() = 1 ) and ( CreationDate/@Time &gt; $reviewTask/ActivationDate/@Time )">
              <xsl:value-of select="Message" disable-output-escaping="yes"/>
            </xsl:if>
          </xsl:for-each>
        </xsl:variable>

        <xsl:choose>
          <xsl:when test="$reviewerComment = ''">.</xsl:when>
          <xsl:otherwise>
            <xsl:text>, with the following comments.</xsl:text>
            <div class="indent description">
              <xsl:call-template name="replace-substring">
                <xsl:with-param name="original" select="$reviewerComment"/>
                <xsl:with-param name="substring" select="$crlf"/>
                <xsl:with-param name="replacement" select="'&lt;br/&gt;'"/>
              </xsl:call-template>
            </div>
          </xsl:otherwise>
        </xsl:choose>
		
		<xsl:choose>
            <xsl:when test="//Job/Variables/Variable[Name='ErrorMessages']/Value != ''">
				<div class="indent description" style="color: #880000">
					<b><xsl:value-of select="//Job/Variables/Variable[Name='ErrorMessages']/Value"/></b>
				</div>
            </xsl:when>
		</xsl:choose>
		
		<xsl:choose>
            <xsl:when test="//Job/Variables/Variable[Name='removedFiles']/Value != ''">
				<div class="indent description" style="color: #880000">
					<b>
						<xsl:text>Following files are locked in workarea:</xsl:text>
						<xsl:value-of select="//Job/Variables/Variable[Name='removedFiles']/Value"/>
					</b>
				</div>
            </xsl:when>
		</xsl:choose>
		
		<xsl:choose>
            <xsl:when test="//Job/Variables/Variable[Name='brokenLinks']/Value != ''">
				<div class="indent description" style="color: #880000">
					<b>
						<xsl:text>Following Links are broken in mentinoed files(FILENAME : BROKENLINK):</xsl:text>
						<xsl:value-of select="//Job/Variables/Variable[Name='brokenLinks']/Value"/>
					</b>
				</div>
            </xsl:when>
		</xsl:choose>

        <div class="indent" style="margin-top:10px;">
          If you would like to use VisualAnnotate to review and edit a file, click the file's "Review and Annotate" link.
          If you need to install the VisualAnnotate toolbar, click
          <a href="http://{$teamSiteHostName}/iw-cc/vannotate/ifatclient/install.jsp">install toolbar</a>.
        </div>
        <div class="indent" style="margin-top:10px;">
          You can review a file by clicking the file name. Then if you do not want to use VisualAnnotate or if your browser does not support the toolbar,
          you can approve or request revisions for the files in TeamSite by clicking the "Approve" or "Revisions Needed" links, respectively.
          <div class="header">Files to Review: (Review not avaiable for all files)</div>

          <!-- filter the task files to hide files that shouldn't be displayed -->
          <xsl:variable name="fileSet" select="$activeTask/Files/File[substring(Vpath,string-length(Vpath)-string-length('.dd') + 1,3) != '.dd']"/>

          <table cellpadding="0" cellspacing="0" border="0" class="file-table">
            <xsl:apply-templates select="$fileSet">
              <xsl:sort select="Vpath"/>
            </xsl:apply-templates>
          </table>
        </div>
        <!-- /main content -->

        <!-- buttons -->
        <div class="buttons">
          <table cellspacing="0" cellpadding="0" border="0" style="display:inline;">
            <tr>
              <td width="1">
                <img src="http://{$teamSiteHostName}/iw-cc/base/images/dialog_btn_left.gif"/>
              </td>
              <td style="white-space:nowrap;" valign="middle" align="middle" background="http://{$teamSiteHostName}/iw-cc/base/images/dialog_btn_mid.gif">
                <a href="http://{$teamSiteHostName}/iw-cc/livesite/Workflow/TransitionComments.jsp?taskid={//Task[Name=$reviewTaskName]/@Id}&amp;transition=Approve+Changes&amp;type=approve" class="button-link">Approve</a>
              </td>
              <td width="1">
                <img src="http://{$teamSiteHostName}/iw-cc/base/images/dialog_btn_right.gif"/>
              </td>
            </tr>
          </table>
          &nbsp;&nbsp;&nbsp;
          <table cellspacing="0" cellpadding="0" border="0" style="display:inline;">
            <tr>
              <td width="1">
                <img src="http://{$teamSiteHostName}/iw-cc/base/images/dialog_btn_left.gif"/>
              </td>
              <td style="white-space:nowrap;" valign="middle" align="middle" background="http://{$teamSiteHostName}/iw-cc/base/images/dialog_btn_mid.gif">
                <a href="http://{$teamSiteHostName}/iw-cc/livesite/Workflow/TransitionComments.jsp?taskid={//Task[Name=$reviewTaskName]/@Id}&amp;transition=Revisions+Needed&amp;type=reject&amp;author={$safeAuthorName}" class="button-link">Revisions Needed</a>
              </td>
              <td width="1">
                <img src="http://{$teamSiteHostName}/iw-cc/base/images/dialog_btn_right.gif"/>
              </td>
            </tr>
          </table>&nbsp;&nbsp;&nbsp;<table cellspacing="0" cellpadding="0" border="0" style="display:inline;">
            <tr>
              <td width="1">
                <img src="http://{$teamSiteHostName}/iw-cc/base/images/dialog_btn_left.gif"/>
              </td>
              <td style="white-space:nowrap;" valign="middle" align="middle" background="http://{$teamSiteHostName}/iw-cc/base/images/dialog_btn_mid.gif">
                <a href="http://{$teamSiteHostName}/iw-cc/viewtaskdetails?taskid={//Task[Name=$reviewTaskName]/@Id}" class="button-link">Task Details</a>
              </td>
              <td width="1">
                <img src="http://{$teamSiteHostName}/iw-cc/base/images/dialog_btn_right.gif"/>
              </td>
            </tr>
          </table>&nbsp;&nbsp;&nbsp;<table cellspacing="0" cellpadding="0" border="0" style="display:inline;">
            <tr>
              <td width="1">
                <img src="http://{$teamSiteHostName}/iw-cc/base/images/dialog_btn_left.gif"/>
              </td>
              <td style="white-space:nowrap;" valign="middle" align="middle" background="http://{$teamSiteHostName}/iw-cc/base/images/dialog_btn_mid.gif">
                <a href="http://{$teamSiteHostName}/iw-cc" class="button-link">Login to SitePublisher</a>
              </td>
              <td width="1">
                <img src="http://{$teamSiteHostName}/iw-cc/base/images/dialog_btn_right.gif"/>
              </td>
            </tr>
          </table>
        </div>
        <!-- /buttons -->

        <!-- workflow history -->
        <b>Workflow History</b>
        <div class="indent">
          <table cellpadding="0" cellspacing="0" border="0">
            <tr>
              <td class="workflow-history-heading">Job:</td>
              <td class="workflow-history">
                <a href="http://{$teamSiteHostName}/iw-cc/viewjobdetails?jobid={//Job/@Id}">
                  <xsl:value-of select="//Job/@Id"/>
                </a>
              </td>
            </tr>
            <tr>
              <td class="workflow-history-heading">Description:</td>
              <td class="workflow-history">
                <xsl:choose>
                  <xsl:when test="//Job/Description != ''">
                    <xsl:value-of select="//Job/Description" disable-output-escaping="yes"/>
                  </xsl:when>
                  <xsl:otherwise>None specified.</xsl:otherwise>
                </xsl:choose>
              </td>
            </tr>
            <tr>
              <td class="workflow-history-heading">Priority:</td>
              <td class="workflow-history">
                <xsl:variable name="priority" select="//Job/Variables/Variable[Name='priority']/Value"/>
                <xsl:choose>
                  <xsl:when test="$priority = '0'">
                    <xsl:text>0 - Very High</xsl:text>
                  </xsl:when>
                  <xsl:when test="$priority = '1'">
                    <xsl:text>1 - High</xsl:text>
                  </xsl:when>
                  <xsl:when test="$priority = '2'">
                    <xsl:text>2 - Medium</xsl:text>
                  </xsl:when>
                  <xsl:when test="$priority = '3'">
                    <xsl:text>3 - Low</xsl:text>
                  </xsl:when>
                  <xsl:when test="$priority = '4'">
                    <xsl:text>4 - Very Low</xsl:text>
                  </xsl:when>
                </xsl:choose>
              </td>
            </tr>
            <tr>
              <td class="workflow-history-heading">Status:</td>
              <td class="workflow-history">In process - Review Content</td>
            </tr>
          </table>
          <div class="header">Comments: ( most recent first )</div>
          <div class="small-indent">
            <table cellpadding="0" cellspacing="0" border="0">
              <xsl:apply-templates select="$activeTask/Comments/Comment">
                <xsl:sort select="CreationDate/@Time" order="descending" data-type="number"/>
              </xsl:apply-templates>
            </table>
          </div>
        </div>


      </body>
      <xsl:comment>
        <xsl:text>activeTaskId:</xsl:text>
        <xsl:value-of select="$activeTaskId"/>
        <xsl:text>, submitOccurred:</xsl:text>
        <xsl:value-of select="$submitOccurred"/>
      </xsl:comment>
    </html>
  </xsl:template>

  <!-- files -->
  <xsl:template match="File">
    <xsl:choose>
      <!-- when branch.taxonomies, currently this file is not editable -->
      <xsl:when test="substring(Vpath,string-length(Vpath)-string-length('branch.taxonomies') + 1,string-length('branch.taxonomies')) = 'branch.taxonomies'">
        <tr>
          <td style="width:15px;">
            <img src="http://{$teamSiteHostName}/iw-cc/base/images/icn_pageright.gif" style="width:15px;height:19px;"/>
          </td>
          <td style="width:410px;">
            <xsl:value-of select="substring(Vpath,string-length($areaVpath)+2)"/>
          </td>
          <td colspan="2">
            &nbsp;
          </td>
        </tr>
      </xsl:when>
      <!-- when default.sitemap -->
      <xsl:when test="substring(Vpath,string-length(Vpath)-string-length('default.sitemap') + 1,string-length('default.sitemap')) = 'default.sitemap'">
        <tr>
          <td style="width:15px;">
            <img src="http://{$teamSiteHostName}/iw-cc/base/images/icn_pageright.gif" style="width:15px;height:19px;"/>
          </td>
          <td style="width:410px;">
            <xsl:value-of select="substring(Vpath,string-length($areaVpath)+2)"/>
          </td>
          <td colspan="2">
            &nbsp;
          </td>
        </tr>
      </xsl:when>
      <!-- when not a file -->
      <xsl:when test="@Type != 'file'">
        <tr>
          <td style="width:15px;">
            <img src="http://{$teamSiteHostName}/iw-cc/base/images/icn_pageright.gif" style="width:15px;height:19px;"/>
          </td>
          <td style="width:410px;">
            <xsl:value-of select="substring(Vpath,string-length($areaVpath)+2)"/>
          </td>
          <td colspan="2">
            &nbsp;
          </td>
        </tr>
      </xsl:when>
      <!-- otherwise assume content -->
      <xsl:otherwise>
        <tr>
          <td style="width:15px;">
            <img src="http://{$teamSiteHostName}/iw-cc/base/images/icn_pageright.gif" style="width:15px;height:19px;"/>
          </td>
          <td style="width:410px;">
            <a href="http://{$teamSiteHostName}/iw-cc/previewfile?vpath={Vpath}">
              <xsl:value-of select="substring(Vpath,string-length($areaVpath)+2)"/>
            </a>
          </td>
          <td style="width:15px;">
            <img src="http://{$teamSiteHostName}/iw-cc/base/images/icn_pageright.gif" style="width:15px;height:19px;"/>
          </td>
          <td>
            <a href="http://{$teamSiteHostName}/iw-cc/review?vpath={Vpath}">Review and Annotate</a>
          </td>
        </tr>
      </xsl:otherwise>
    </xsl:choose>

  </xsl:template>

  <xsl:template match="Comment">
    <xsl:if test="position() &gt; 1">
      <tr class="comment">
        <td colspan="3" class="comment-separator">
          <hr/>
        </td>
      </tr>
    </xsl:if>
    <tr>
      <td rowspan="4" style="width:15px;vertical-align:top;padding-top:4px;">
        <img src="http://{$teamSiteHostName}/iw-cc/base/images/icn_pageright.gif" style="width:15px;height:19px;"/>
      </td>
      <td colspan="2" class="workflow-history">
        <xsl:value-of select="Message"/>
      </td>
    </tr>
    <xsl:variable name="taskId" select="@TaskId"/>
    <xsl:variable name="commentTask" select="//Task[@Id=$taskId]"/>
    <tr>
      <td class="workflow-history-heading">User:</td>
      <td class="workflow-history">
        <xsl:variable name="userId" select="User/@Id"/>
        <xsl:variable name="user" select="//Owner[@Id=$userId] | //Creator[@Id=$userId]"/>
        <xsl:choose>
          <xsl:when test="$user != ''">
            <xsl:choose>
              <xsl:when test="$user[1]/Email != ''">
                <a href="mailto:{$user[1]/Email}">
                  <xsl:value-of select="$user[1]/Name"/>
                </a>
              </xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="$user[1]/Name"/>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="$userId"/>
          </xsl:otherwise>
        </xsl:choose>
      </td>
    </tr>
    <tr>
      <td class="workflow-history-heading">Date:</td>
      <td class="workflow-history">
        <xsl:value-of select="CreationDate"/>
      </td>
    </tr>
    <tr>
      <td class="workflow-history-heading">Task:</td>
      <td class="workflow-history">
        <a href="http://{$teamSiteHostName}/iw-cc/viewtaskdetails?taskid={$commentTask/@Id}">
          <xsl:value-of select="concat($commentTask/Name,' (', $commentTask/@Id, ') ')"/>
        </a>
      </td>
    </tr>
  </xsl:template>

  <!--
  * the HTML <head>
  -->
  <xsl:template mode="head" match="/">
    <head>
      <xsl:apply-templates mode="css" select="/"/>
    </head>
  </xsl:template>

  <!--
  * page styles
  -->
  <xsl:template mode="css" match="/">
    <style type="text/css">
      body,
      td
      {
      font: 10pt Arial;
      }

      div, tr, td
      {
      margin-top: 10px;
      }

      .small-indent
      {
      padding-left: 5px;
      }


      .indent
      {
      padding-left: 15px;
      }

      a,
      a:visited
      {
      color: #003399;
      }

      .description
      {
      border: 1px solid #cccccc;
      background-color: #f5f5f5;
      padding: 15px;
      }

      .header
      {
      color: #336699;
      font-weight: bold;
      }

      table.file-table
      {
      margin-top:10px;
      }

      div.buttons
      {
      border-top: 1px solid #cccccc;
      padding-top: 10px;
      text-align: right;
      margin-right:15px;
      margin-top: 20px
      }

      a.button-link,
      a:visited.button-link
      {
      text-decoration:none;
      color: #003366;
      font: 11px Verdana, Arial;
      padding-right:3px;
      }

      a:hover.button-link
      {
      text-decoration:underline;
      }

      td.workflow-history
      {
      padding-top: 5px;
      }

      td.workflow-history-heading
      {
      width: 100px;
      padding-top: 5px;
      font-weight: bold;
      color: #666666;
      }

      .comment-separator
      {
      height: 20px;
      verical-align: bottom;
      }

      .comment-separator hr
      {
      height: 1px;
      border-top: 1px solid #cccccc;
      }

    </style>
  </xsl:template>

</xsl:stylesheet>