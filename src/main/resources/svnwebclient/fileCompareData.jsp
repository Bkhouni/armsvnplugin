<%@page contentType="text/html; charset=UTF-8"%>
<%--
  - Copyright (c) 2004, 2005 Polarion Software, All rights reserved.
  - Email: community@polarion.org
  -
  - This program and the accompanying materials are made available under the
  - terms of the Apache License, Version 2.0 (the "License"). You may not use
  - this file except in compliance with the License. Copy of the License is
  - located in the file LICENSE.txt in the project distribution. You may also
  - obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
  -
  -
  - POLARION SOFTWARE MAKES NO REPRESENTATIONS OR WARRANTIES
  - ABOUT THE SUITABILITY OF THE SOFTWARE, EITHER EXPRESSED OR IMPLIED,
  - INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY,
  - FITNESS FOR A PARTICULAR PURPOSE, OR NON-INFRINGEMENT. POLARION SOFTWARE
  - SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT
  - OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
  --%>
  
<%@ page import="org.polarion.svnwebclient.web.model.data.file.FileCompareResult,
                 org.polarion.svnwebclient.web.support.DifferenceLine,
                 java.util.Iterator"%>
                 
<jsp:useBean id="bean" scope="request" class="org.polarion.svnwebclient.web.controller.file.FileCompareDataBean"/>                 
<%
    if (bean.execute(request, response)) {
        FileCompareResult result = bean.getResult();
%>

<html>
    <jsp:include page="include/head.jsp">
        <jsp:param name="jsp.head.title" value="SVN Web Client - File Content"/>
    </jsp:include>
    <body>
         <table cellspacing="0" cellpadding="0" width="100%" height="100%">
<%
        if (result == null) {
%>
            <tr valign="middle">
                <td align="center" height="100%" style="font-family:Arial;font-size:11px;">
<%
            if (bean.isBinary()) {
%>                                
                    <b>File content is binary</b>
<%
            } else {
%>                
                    <b>There are no data to show</b>
<%
            }                    
%>            
                </td>
            </tr>                    
<%
        } else {
            for (Iterator i = result.getLines().iterator(); i.hasNext(); ) {
                FileCompareResult.Line line = (FileCompareResult.Line) i.next();
%>
            <tr valign="middle">
                <td class="compareImageTd">
                    <img src="<%=line.getImage()%>" class="compareImage"/>
                </td>            
                <td class="compareNumber">
                    <a class="number" name="<%=line.getNumber()%>" href="#<%=line.getNumber()%>">
                        <%=line.getNumber()%>
                    </a>
                </td>
                
<%
				if (DifferenceLine.ADDED == line.getChangeType()) {
%>					
				<td class="compareLineAdded">
<%					
				} else if (DifferenceLine.DELETED == line.getChangeType()) {
%>					
				<td class="compareLineDeleted">
<%				
				} else if (DifferenceLine.MODIFIED == line.getChangeType()) {
%>				
				<td class="compareLineModified">
<%					
				} else {
%>					
				<td class="compareLine">
<%					
				} 
%>                
				    <code class="prettyprint <%=bean.getLangExtension()%>">
                    <%=line.getLine()%>&nbsp;
 				    <code class="prettyprint">                   
                </td>                
            </tr>    
<%
            }                    
%>
            <tr>
                <td height="100%"/>
            </tr>            
<%                        
        }
%>        
        </table>
        <script type="text/javascript">
        	prettyPrint();
        </script>
  	   </body>
</html>
<%
    }
%>    