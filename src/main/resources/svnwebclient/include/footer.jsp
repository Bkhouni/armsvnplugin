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
<%@ page import="java.util.Calendar"%>
  
<%
    if (!ConfigurationProvider.isEmbedded()) {
%>
    <table class="footer" cellspacing="0" cellpadding="0" width="100%" style="border-width: 0; border-top: 1px solid black;">
        <tr>
            <td align="left" nowrap="true" style="padding-left: 10; padding-right: 5;">
                <a class="footer" href="http://www.kintosoft.com" target="_blank">&copy; KintoSoft Ltd <%=Calendar.getInstance().get(Calendar.YEAR)%></a>
            </td>
            <td align="center" nowrap="true" width="100%" style="padding-left: 5; padding-right: 5;">
                <a class="footer" href="" target="_blank">Powered by Polarion SVN Web Client</a>
            </td>
            <td align="right" nowrap="true" style="padding-left: 5; padding-right: 10;">
                <a class="footer" href="http://answers.atlassian.com" target="_blank">Support and Additional Information</a>
            </td>           
        </tr>   
    </table>
<%
    }
%>
<script>
	if(parent)
		parent.postMessage("resize","*");
</script>