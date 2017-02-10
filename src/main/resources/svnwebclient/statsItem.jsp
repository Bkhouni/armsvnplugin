<%@page contentType="text/html; charset=UTF-8"%>
    
<jsp:useBean id="bean" scope="request" class="org.polarion.svnwebclient.web.controller.StatsItemBean"/>
<%
    if (bean.execute(request, response)) {
        String msg = bean.getMessage();
%>
<html>
    <jsp:include page="include/head.jsp">
        <jsp:param name="jsp.head.title" value="SVN Web Client - Item Statistics"/>
    </jsp:include>
    <body>   	
        <table cellspacing="0" cellpadding="0" width="100%" height="99%">
            <tr>
                <td>
                    <jsp:include page="include/top.jsp"/>
                </td>
            </tr>
            <tr>
                <td style="padding:0px;spacing:0px; padding-left:5px; padding-right:5px; padding-top:2px;">
                    <jsp:include page="include/infoPanel.jsp">
                        <jsp:param name="jsp.infoPanel.info" value="statsItemInfo.jsp"/>
                     </jsp:include>
                </td>
            </tr>
              <tr>
            	<td height="25px">
            	</td>
            </tr>      
             <tr>
                <td class="title" width="100%" valign="bottom" style="padding:0px;spacing:0px; padding-left:5px; padding-right:7px;" align="center">
 		     			<p>Commits Calendar</p>
                </td>
            </tr>
            <tr>
                <td class="label" width="100%" valign="bottom" style="padding:0px;spacing:0px; padding-left:5px; padding-right:7px;" align="center">
 		     			<p>The full history of the item (including its children whether it is a directory) from it was created until it has been deleted.</p>
                </td>
            </tr>
            <tr>
                <td width="100%" style="padding:0px;spacing:0px; padding-left:5px; padding-right:7px;" align="center" valign="top">
 		     			<jsp:include page="include/progress.jsp">
 		     				<jsp:param value="loadingCalendar" name="jsp.div.id"/>
 		     			</jsp:include>		     			
 		     			<div id="calendar"></div>
                </td>
            </tr>
            <tr>
            	<td align="center" width="100%" class="caption">
            		<div id="threshold"/>
            	</td>
            </tr>
            <tr>
            	<td height="25px">
            	</td>
            </tr>      
            <tr>
                <td width="100%" style="padding:0px;spacing:0px; padding-left:5px; padding-right:7px;" align="center" valign="top">
  		     		<div id="bars"></div>
                </td>
            </tr>             
            <tr>
            	<td align="center" width="100%" class="caption">
            		<div id="groupby"/>
            	</td>
            </tr>          
             <tr>
            	<td height="25px">
            	</td>
            </tr>      
             <tr>
                <td class="title" width="100%" valign="bottom" style="padding:0px;spacing:0px; padding-left:5px; padding-right:7px;" align="center">
 		     			<p>User commits</p>
                </td>
            </tr>
            <tr>
                <td class="label" width="100%" valign="bottom" style="padding:0px;spacing:0px; padding-left:5px; padding-right:7px;" align="center">
 		     			<p>The user contribution to the history of the item displayed above.</p>
                </td>
            </tr>
            <tr>
                <td width="100%" style="padding:0px;spacing:0px; padding-left:5px; padding-right:7px;" align="center" valign="top">
 		     			<jsp:include page="include/progress.jsp">
 		     				<jsp:param value="loadingUsers" name="jsp.div.id"/>
 		     			</jsp:include>
 		     			<div id="users"></div>
                </td>
            </tr>             
            <tr>
            	<td height="100%">
            	</td>
            </tr>
            <tr>
                <td>
                    <jsp:include page="include/footer.jsp"/>
                </td>
            </tr>
        </table>
        <script>
 	    	showStatsItem('<%=bean.getRepoId()%>','<%=bean.getCurrentPageInfo().getPath()%>','<%=bean.getRevision()%>', <%= bean.isDirectory() %>, function(){
	    		d3.select("#loadingCalendar").remove();
	    		d3.select("#loadingUsers").remove();
	    		if(parent)
	    			parent.postMessage("resize","*");
	    	});
     	</script>
     </body>
</html>
<%
    }
%>