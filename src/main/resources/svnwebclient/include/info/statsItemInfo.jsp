
<jsp:useBean id="bean" scope="request" type="org.polarion.svnwebclient.web.controller.StatsItemBean"/>  
<table class="tabcontent" cellpadding="0" cellspacing="0" width="100%" border="0">
    <tr class="value" nowrap="true">
        <td class="value" style="padding-left:20px;" nowrap="true">
            <b>pegRevision:</b>&nbsp;
            <a href="<%=bean.getRevisionUrl()%>">
                <%=bean.getDecoratedRevision()%>
            </a>    
<%
    if (bean.isHeadRevision()) {
%>
            &nbsp;[HEAD]
<%
    }                    
%>            
        </td>    
        <td width="100%"/>
    </tr>
   
</table>