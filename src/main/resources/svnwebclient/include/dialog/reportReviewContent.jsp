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

<%@ page import="org.polarion.svnwebclient.web.servlet.SendEmailServlet,
                 org.polarion.svnwebclient.web.support.MailSettingsProvider"%>
  
<table width="100%" height="100%" border="0" cellspacing="0" cellpadding="0" class="dialogcontent">                    
<%
    if (request.getParameter("falseSend") != null) {
%>
        <tr>
            <td style="padding-bottom:10px;">
                <font color="red">
                    There are problems with error report sending. 
                    Please copy error report text and send it manually to the address <%=MailSettingsProvider.getInstance().getEmailTo()%>.
                    Please click Back button to exit from this view.
                   </font>
            </td>
<%        
    } else {
%>                
        <tr>
            <td style="padding-bottom:10px;">
                Please preview error report which will be sent
            </td>
        </tr>                
<%
    }
%>

    <tr>
        <td width="100%" height="100%"  style="background-color:white;">
            <iframe class="reportFrame" width="100%" height="100%" frameborder="0" src="include/dialog/reportReview.jsp">
               <a href="include/dialog/reportReview.jsp">Report Review</a>
            </iframe>
        </td>
    </tr>
     <tr>        
        <td width="100%" style="padding-top:20px;">
            <table cellspacing="0" cellpadding="0" width="100%" border="0">
                <tr>
                    <td align="left">
                        <input type="submit" value="Send" name="<%=SendEmailServlet.PRESSED_BUTTON%>" class="button" />            
                    </td>    
                    <td align="left" style="padding-left:10px;" width="100%">
                        <input type="submit" value="Back" name="<%=SendEmailServlet.PRESSED_BUTTON%>" class="button"/>
                    </td>    
                </tr>    
            </table>    
        </td>    
    </tr>              
</table> 