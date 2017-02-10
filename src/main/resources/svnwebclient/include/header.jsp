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

<%@ page import="org.polarion.svnwebclient.configuration.ConfigurationProvider"%>
<%
    if (!ConfigurationProvider.isEmbedded()) {
%>
<table class="actionbar_head" width="100%" cellpadding="0" cellspacing="0">
    <tr>
        <td style="padding-left:8px;padding-right:7px;">
            <img src="images/logo_small.gif" alt="by KintoSoft Ltd" title="SVN Web Client for JIRA" style="padding:0 0 0 0; margin:0 0 0 0;vertical-align:middle;" border="0"></img>
         </td>
        <td nowrap="nowrap" class="title">
            Powered by Polarion SVN Web Client
        </td>
        <td width="100%">
        </td>
<%
	if (ConfigurationProvider.isLogout()) {
%>        

<%
		if (request.getSession().getAttribute(org.polarion.svnwebclient.authorization.ICredentialsManager.IS_LOGGED_IN) == null) {
%>
		<td style="padding-left:8px;padding-right:7px;" class="title">
             <a href="">
                Logout
            </a>
        </td>
<%
		} else {
%>
		<td style="padding-left:8px;padding-right:7px;" class="title">
            <a href="logout.jsp">
                Logout
            </a>
        </td>
<%
		}
%>
        
<%
	}
%>        
    </tr>
</table>
<%
    }
%>    