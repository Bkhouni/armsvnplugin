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

<jsp:useBean id="bean" scope="request" type="org.polarion.svnwebclient.web.controller.file.FileUpdateBean"/>
<form name="fileUpdate" method="POST" enctype="multipart/form-data" action="<%=bean.getOkUrl()%>" style="padding:0;margin:0;" onSubmit="return validateForm();">
    <input type="hidden" name="originalname" id="originalname" value="<%=bean.getFilename()%>"/>
            
    <table class="dialogcontent" cellspacing="0" cellpadding="0" width="100%" height="100%">
        <tr>
            <td>
                <b>File path:</b>
            </td>
        </tr>
        <tr>
            <td>
                <input type="file" id="filepath" name="filepath" style="width:100%;margin:0;font-size:11px;"/>
            </td>
        </tr>    
        <tr>
            <td style="padding-top:10px;">
                <b>Comment:</b>
            </td>
        </tr>
        <tr>
            <td>
                <textarea id="comment" name="comment" rows="5" style="width:100%;margin:0;font-size:11px;">File updated remotely</textarea>
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

<script language="JavaScript">
	function validateForm() {
	    var isValid = false;
		var entered_filename = document.getElementById('filepath').value;
		var originalFileName = document.getElementById('originalname').value;
		
		if (entered_filename.length > 0) {
		
			 //under FF3 it returns only file name but not the full path
		     var slashIdx = entered_filename.lastIndexOf('\\');
		     if(slashIdx == -1) {
		         slashIdx = entered_filename.lastIndexOf('/');
		     }
		     
		     if(slashIdx != -1) {
		         entered_filename = entered_filename.substring(slashIdx+1, entered_filename.length);                        
		     }
		     
		     if (entered_filename != originalFileName) {
		     	alert('File name must have the same as stored on the server. ' + 
		     		'Server file name: ' + originalFileName + ', uploading file name: ' + entered_filename);
		     } else {
		     	isValid = true;
		     }                                        
		 } else {                
		     alert('Please, fill file path');
		 }
		 
		 return isValid;		
	}

</script>