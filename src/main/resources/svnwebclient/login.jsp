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
  
<%@ page import="com.kintosoft.svnwebclient.jira.SWCUtils,
	org.polarion.svnwebclient.configuration.ConfigurationProvider" %>

<%
    String originalUrl = (String) request.getAttribute("javax.servlet.forward.request_uri");
    if (originalUrl == null) {
        StringBuffer requestURL = request.getRequestURL();
        if (requestURL == null) {
            originalUrl = "";
        } else {
            originalUrl = requestURL.toString();
        }
    }
    String queryString = (String) request.getAttribute("javax.servlet.forward.query_string");
    if (queryString == null) {
        queryString = request.getQueryString();
    }
    if (queryString != null) {
        originalUrl += "?";
        originalUrl += queryString;
    }
%>    
<html>
    <jsp:include page="include/head.jsp">
        <jsp:param name="jsp.head.title" value="SVN Web Client - Login"/>
    </jsp:include>
    <body>
        <table cellspacing="0" cellpadding="0" width="100%" height="100%">
            <tr>
                <td>
                    <jsp:include page="include/header.jsp"/>
                </td>
            </tr>
            <tr>
                <td height="100%">
                    <form name="loginForm" method="POST" enctype="application/x-www-form-urlencoded" action="<%=originalUrl%>">
                        <table align="center" valign="middle" class="login" width="470px">
                            <tr>
                                <td class="logintitle" align="center">
                                    Login
                                </td>
                            </tr>
                            <tr>
                                <td height="20">
                                </td>
                            </tr>
                            <tr>
                                <td width="100%" align="center">
                                    <br/>
                                    <table cellpadding="0" cellspacing="2">  
<%
	if (request.getParameter("wrongCredentials") != null) {
%>                                    
                                    	<tr>
                                    		<td colspan="2">
                                    			<font color="red">Your credentials are not correct. Please check them and try again.</font>
                                    		</td>
                                    	</tr>                 
<%
	} else {
%>                                    	
            							<tr>
            								<td colspan="2">&nbsp;</td>
            							</tr>         
<%
	}
%>
                                    </table>
                                    <table cellpadding="0" cellspacing="2">              							               	                                                                                             

										<tr>
											<td class="pr0" align="right">
                                    			Repository:
                                    		</td>	
                                    		<td>
<% 
	if (SWCUtils.getRepositories().size()==0){
%>
	                                    		<font color="red">No Subversion repository is registered yet. Please contact your administrator.</font>
<% 
	} else { 
%>
												<select name="repoId">
<%
	String paramId = request.getParameter("repoId");
	String disabled="disabled=\"disabled\"";
	if(paramId==null){
		paramId="-1";
		disabled = "";
	}
	long longId=Long.parseLong(paramId);
	
	for(ConfigurationProvider repo : SWCUtils.getRepositories()){
%>
	                                            <option value="<%=repo.getRepoId()%>" <%=(longId==repo.getRepoId()?"selected":disabled)%>><%=repo.getDisplayName()%>  [<%=repo.getRepoId()%>]</option>
<%
	}
%>
                                                </select>
<%
	}
%>
                                     		</td>
										</tr>	

                                    	<tr>
                                        	<td class="pr0" align="right">
                                                Username:
                                            </td>
                                            <td>
                                                <input name="username" size="12" value="" maxlength="25" type="text" style="width: 25ex"/>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td class="pr0" align="right">
                                                Password:
                                            </td>
                                            <td>
                                                <input size="12" value="" name="password" maxlength="25" style="width: 25ex;" type="password"/>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td colspan="2" height="15"></td>
                                        </tr>
                                        <tr>
                                            <td colspan="2" align="center">
                                                <input class="button" name="submit" type="submit" value="Login"/>
                                            </td>
                                        </tr>
                                    </table>
                                </td>
                            </tr>
                            <tr>
                                <td height="30"></td>
                            </tr>
                        </table>
                        <input type="hidden" name="wrongCredentials" value="wrongCredentials"/>
                    </form>
                </td>
            </tr>
            <tr>
                <td>
                    <jsp:include page="include/footer.jsp"/>
                </td>
            </tr>
        </table>
    </body>
</html>