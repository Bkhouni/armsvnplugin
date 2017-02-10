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

<%@ page import="org.polarion.svnwebclient.web.model.data.DeletedElementsList" %>
<jsp:useBean id="bean" scope="request" type="org.polarion.svnwebclient.web.controller.DeleteBean"/>
<form name="directoryAdd" method="POST" action="<%=bean.getOkUrl()%>" style="padding:0;margin:0;">
    <table class="dialogcontent" cellspacing="0" cellpadding="0" width="100%" height="100%">
        <tr>
            <td>
                <b>Are you sure you want to delete selected elements?</b>
            </td>
        </tr>
        <tr>
            <td>
                <table id="table_list_of_files" name="table_list_of_files" class="list" width="100%" cellpadding="0" cellspacing="0" rules="all">
                    <tr>
                        <th nowrap="true" width="30%">
                            Name
                        </th>
                        <th nowrap="true" width="5%">
                            Revision
                        </th>
                        <th nowrap="true" width="5%">
                            Date
                        </th>
                        <th nowrap="true" width="10%">
                            Author
                        </th>
                        <th nowrap="true" width="45%">
                            Comment
                        </th>
                    </tr>
<%
		int counter = 0;
        for (int i = 0; i < bean.getDeletedElements().getDeletedElements().size(); i++) {
            DeletedElementsList.Element element = (DeletedElementsList.Element) bean.getDeletedElements().getDeletedElements().get(i);
%>            
                    <tr>
                        <td>
                            <table width="100%" cellpadding="0" cellspacing="0" border="0">
                                <tr>
                                    <td class="internal" style="padding-right:5px;">
                                        <img src="<%=element.getType()%>" />
                                    </td>
                                    <td class="internal" width="100%" nowrap="true">
                                        <%=element.getName()%>
                                    </td>                                        
                                </tr>
                            </table>
                        </td>
                        <td align="right">
                            <%=element.getDecoratedRevision()%>
                        </td>
                        <td nowrap="true">
                            <span title="<%=element.getAge()%> ago"><%=element.getDate()%></span>
                        </td>
                        <td nowrap="true">
                            <%=element.getAuthor()%>
                        </td>
<%                                
			if (element.isMultiLineComment()) {
				counter ++;
				String cell = "cell_" + counter;
				String tool = "tooltip_" + counter;
%>	
						<td id="<%=cell%>" onmouseover="xstooltip_show('<%=tool%>', '<%=cell%>');" onmouseout="xstooltip_hide('<%=tool%>');">									
									<div id="<%=tool%>" class="xstooltip">
										<%=element.getTooltip()%>	
									</div>						
							<img src="images/multiline_text.gif" style="position:absolute;margin-top:2px" align="middle" width="8" height="9">
		     				&nbsp;&nbsp;&nbsp;&nbsp;<%=element.getFirstLine()%>				
		     			</td>	
<%
			} else {								 
%>				    
			         	<td>
                            <%=element.getComment()%>
                        </td>
<%
			}
%> 
                    </tr>
<%
        }                    
%>        
                </table>
            </td>
        </tr>    
        
        <tr>
            <td style="padding-top:10px;">
                <b>Comment:</b>
            </td>
        </tr>
        <tr>
            <td>
                <textarea id="comment" name="comment" rows="5" style="width:100%;margin:0;font-size:11px;">Delete elements remotely</textarea>
            </td>
        </tr>           
        <tr>
            <td width="100%" style="padding-top:20px;padding-bottom:0px;">
                <table cellspacing="0" cellpadding="0" width="100%">
                    <tr>
                        <td align="left">
                            <input type="submit" class="button" value="OK"/>                
                        </td>
                        <td align="left" style="padding-left:10px;">
                            <input type="button" class="button" value="Cancel" onclick="window.location='<%=bean.getCancelUrl()%>'"/>
                        </td>      
                        <td width="100%"/>
                    </tr>
                </table>
            </td>    
        </tr>    
    </table>
</form>    
<script language="javascript">
    firstsecond('table_list_of_files');
</script>
