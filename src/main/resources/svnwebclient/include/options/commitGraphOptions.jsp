<jsp:useBean id="bean" scope="request" type="org.polarion.svnwebclient.web.controller.CommitGraphBean"/>

<table class="tabcontent"  cellpadding="0" cellspacing="0" border="0" height="100%"> 
    <tr>
		 <td class="value" nowrap="true" style="padding-right:20px;" align="left" valign="middle">
            <div id="progressbar"></div>
         </td> 
          <td class="value" style="padding-top:5px;">
              From:
          </td>
         
          <td class="value" style="padding-top:5px;padding-left:5px"">
               <input type="text" id="startrange" name="startrevision" maxlength="10" style="font-size:11px;width:50px;margin:0;text-align:right"'/>
          </td>          
          
          <td class="value" style="padding-top:5px;padding-left:10px;">
              To:
          </td>
          
          <td class="value" style="padding-top:5px;padding-left:5px">
             <input type="text" id="endrange" name="endrevision" maxlength="10" style="font-size:11px;width:50px;margin:0;text-align:right"'/>
          </td>          
          
          <td class="value" style="padding-left:10px;padding-top:5px;" style="padding-right:20px;">
              <input type="button" class="button" value="Show revisions" name="submitButton" title="Show the revisions in range only" onclick='return handleClickRange()'/>
          </td> 
         <td class="value" nowrap="true" style="padding-right:20px; padding-left:20px;" valign="middle">
           <input id="filter_all" type='checkbox' onclick='handleClickAll(this);' title="Show all the commits">All
         </td>        
         <td class="value" nowrap="true" style="padding-right:20px;" valign="middle">
           <input id="filter_tags" type='checkbox' onclick='handleClickTags(this);' title="Show tags">Tags
         </td>        
        <td class="value" nowrap="true" style="padding-right:20px;" valign="middle">
           <input id="filter_deleted" type='checkbox' onclick='handleClickDeleted(this);' title="Show deleted branches">Deleted
         </td>        
       <td class="value" nowrap="true" style="padding-right:20px;" valign="middle">
           <input id="filter_unused" type='checkbox' onclick='handleClickUnused(this);' title="Show unused branches">Unused
         </td>        
        <td class="value" nowrap="true" style="padding-right:20px;" valign="middle">
           <input id="filter_strict" type='checkbox' onclick='handleClickStrict(this);' title="Strict copy source">Strict
         </td>        
       <td class="value" nowrap="true" style="padding-right:20px;" valign="middle">
           <input id="filter_issues" type='checkbox' onclick='handleClickIssues(this);' title="Show JIRA issues">Issues
         </td>    
     </tr>
</table>
