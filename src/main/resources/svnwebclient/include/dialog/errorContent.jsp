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
                              
<jsp:useBean id="errorBean" scope="request" type="org.polarion.svnwebclient.web.controller.ErrorBean"/>
                 
<%      
    String description = errorBean.getDescription();
%>             
    
<table  cellspacing="0" cellpadding="0" width="600px" height="100%" border="0" class="dialogcontent">  
        <tr>
            <td style="padding-top:10px;">
                <b>Cause:</b>
            </td>
        </tr>

        <tr>
           <td>
                <textarea readonly rows="10" name="description" style="width:100%;margin:0;font-size:11px;"><%=description%></textarea>
            </td>
        </tr> 
                
        </tr> 
</table>              
