<%@page contentType="text/html; charset=UTF-8"%>
    
<jsp:useBean id="bean" scope="request" class="org.polarion.svnwebclient.web.controller.CommitGraphBean"/>
<%
    if (bean.execute(request, response)) {
        String msg = bean.getMessage();
%>
<html>
    <jsp:include page="include/head.jsp">
        <jsp:param name="jsp.head.title" value="SVN Web Client - Commit Graph"/>
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
                        <jsp:param name="jsp.infoPanel.info" value="commitGraphInfo.jsp"/>
                        <jsp:param name="jsp.infoPanel.options" value="commitGraphOptions.jsp"/>
                     </jsp:include>
                </td>
            </tr>
            <tr>
                <td width="100%" height="100%" valign="top" style="padding:0px;spacing:0px; padding-left:5px; padding-right:7px;">
		             	<div id="graph" style="overflow-x: auto;">
                 </td>
            </tr>
            <tr>
                <td>
                    <jsp:include page="include/footer.jsp"/>
                </td>
            </tr>
        </table>
    <script>
		filters.load();
    	drawGraph('<%=bean.getRepoId()%>','<%=bean.getCurrentPageInfo().getPath()%>','<%=bean.getRevision()%>', <%= bean.isDirectory() %>, function(){
    		if(parent)
    			parent.postMessage("resize","*");
    	});
    </script>
    </body>
</html>
<%
    }
%>