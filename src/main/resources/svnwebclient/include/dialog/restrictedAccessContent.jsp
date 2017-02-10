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
  - POLARION SOFTWARE MAKES NO REFPRESENTATIONS OR WARRANTIES
  - ABOUT THE SUITABILITY OF THE SOFTWARE, EITHER EXPRESSED OR IMPLIED,
  - INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY,
  - FITNESS FOR A PARTICULAR PURPOSE, OR NON-INFRINGEMENT. POLARION SOFTWARE
  - SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT
  - OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
  --%>
  												
<%@ page import="org.polarion.svnwebclient.configuration.ConfigurationProvider"%>

<jsp:useBean id="bean" scope="request" type="org.polarion.svnwebclient.web.controller.RestrictedAccessBean"/>
<% 
	boolean isMultirepositoryMode = ConfigurationProvider.isMultiRepositoryMode();	    
%>	

<table class="dialogcontent" cellspacing="0" cellpadding="0" width="100%" height="100%">    
	<tr>
		<td>
			<table width="100%" cellpadding="0" cellspacing="0" class="dialogcontent">
<%
	if (isMultirepositoryMode) {
%>				<tr>
					<td cellpadding="0" cellspacing="0">
						You have no access to the url: <b><%=bean.getPageLocation()%></b>/<%=bean.getPageUrl()%> 												
					</td>

<%
	} else {
%>
					<td cellpadding="0" cellspacing="0">
						You have no access to the url: <b>Repository</b>/<%=bean.getPageUrl()%> 
					</td>
<%
	}
%>
				</tr>
<%
	if (ConfigurationProvider.isEmbedded()) {
%>				
				<tr>
					<td cellpadding="0" cellspacing="0" style="padding-top:5px;">
						If you want to access restricted folder, you can relogin.
					</td>
				</tr>		
<%
	} else {
%>
				<tr>
					<td cellpadding="0" cellspacing="0" style="padding-top:5px;">
						Are you sure you want to change credentials in order to access this page?
					</td>
				</tr>		
<%
	}
%>

			</table>
		</td>	
	</tr>	
</table>	
	
	

	