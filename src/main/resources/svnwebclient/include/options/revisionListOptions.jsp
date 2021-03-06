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

<%@ page import="org.polarion.svnwebclient.web.controller.RevisionListBean"%>


<script media="screen" src="script/checkForm.js" type="text/javascript">
</script>

<jsp:useBean id="bean" scope="request" type="org.polarion.svnwebclient.web.controller.RevisionListBean"/>    
       
    <table class="tabcontent" cellpadding="0" cellspacing="0" border="0" height="100%">
       
<%
    if (session.getAttribute(RevisionListBean.ADVANCED_NAVIGATION_ATTRIBUTE) == null) {
%>        
          
        <tr nowrap="true">
                <td class="value" style="padding-top:5px;">
<%
        if (bean.isPreviousDisabled()) {
%>            
                    <input type="button" class="button" value="Previous page" name="submitButton" disabled/>
<%
        } else {
%>
                    <input type="button" class="button" value="Previous page" name="submitButton" onclick='return navigate("<%=bean.getButtonUrl().getPrevious()%>")'/>  
<%
        }
%>                    
                </td>
                
                <td class="value" style="padding-left:10px;padding-top:5px;">
                    <input type="text" id="revcount" maxlength="10" style="font-size:11px;width:50px;margin:0;text-align:right" value="<%=bean.getRevisionList().getRevisionsCount()%>"/>
                </td>  
                
                <td class="value" style="padding-left:10px;padding-top:5px;">
<%
        if (bean.isNextDisabled()) {
%>            
                    <input type="button" class="button" value="Next page" name="submitButton" disabled/>
<%
        } else {
%>
                    <input type="button" class="button" value="Next page" name="submitButton" onclick='return navigate("<%=bean.getButtonUrl().getNext()%>")'/>  
<%
        }
%>            
                </td>           
<%
    } else {
%>        
        <tr>   
                <td class="value" style="padding-top:5px;">
                    From:
                </td>
                
                <td class="value" style="padding-top:5px;padding-left:5px"">
                     <input type="text" id="startrange" name="startrevision" maxlength="10" style="font-size:11px;width:50px;margin:0;text-align:right" value='<%=bean.getRangeStartRevision()%>'/>
                </td>          
                
                <td class="value" style="padding-top:5px;padding-left:10px;">
                    To:
                </td>
                
                <td class="value" style="padding-top:5px;padding-left:5px">
                   <input type="text" id="endrange" name="endrevision" maxlength="10" style="font-size:11px;width:50px;margin:0;text-align:right" value='<%=bean.getRangeEndRevision()%>'/>
                </td>          
                
                <td class="value" style="padding-left:10px;padding-top:5px;">
                    <input type="button" class="button" value="Show revisions" name="submitButton" onclick='return rangeNavigate("<%=bean.getButtonUrl().getUrl()%>")'/>
                </td> 
<%
    }   
%>           

            <td class="value" style="padding-left:10px;padding-top:5px;">
                 <select name="views" class="combo" onchange='window.location="<%=bean.getSelectUrl()%>"'>
<%
    if (session.getAttribute(RevisionListBean.ADVANCED_NAVIGATION_ATTRIBUTE) == null) {
%>                  
                     <option selected>Page Mode</option>
                     <option>Range Mode</option>                    
<%
    } else {
%>          
                     <option>Page Mode</option>
                     <option selected>Range Mode</option>  
<%
    }
%>           
                 </select>
            </td>

            <td class="value" style="padding-top:5px;padding-right:20px;padding-left:20px;">
                <input type="button" class="button" value="Show all" name="submitButton" onclick='window.location="<%=bean.getButtonUrl().getUrl()%>"'/>
            </td> 
        </tr>    
    </table>
