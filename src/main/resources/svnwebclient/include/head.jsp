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

<%@page import="org.polarion.svnwebclient.web.resource.Links"%>
<head>
<meta equiv="Content-Type" content="text/html; charset=UTF-8">

<!--  <script src="script/analytics.js"></script> -->

<% 
	String path = request.getServletPath();
	if(path!=null){
		if(path.endsWith("/"+Links.COMMIT_GRAPH)){
	
%>
<meta http-equiv="X-UA-Compatible" content="IE=9" />

<link href="style/redmond/jquery-ui-1.10.3.custom.css" type="text/css"
	rel="stylesheet" media="screen" />

<script src="dwr/engine.js"></script>
<script src="dwr/util.js"></script>
<script src="dwr/interface/CommitGraphService.js"></script>
<script src="script/d3.v3.min.js" charset="utf-8"></script>
<script src="script/jquery-2.0.3.min.js" charset="utf-8"></script>
<script src="script/json2.js" charset="utf-8"></script>
<script src="script/jquery.cookies.2.2.0.min.js" charset="utf-8"></script>
<script src="script/jquery-ui-1.10.3.custom.min.js" charset="utf-8"></script>
<script type="text/javascript" src="script/commitGraph.js"
	media="screen"></script>


<%
		} else if(path.endsWith("/"+Links.STATS_ITEM) || path.endsWith("/"+Links.STATS_USER)){
%>
<meta http-equiv="X-UA-Compatible" content="IE=9" />

<link href="style/stats.css" type="text/css" rel="stylesheet"
	media="screen" />

<script src="dwr/engine.js"></script>
<script src="dwr/util.js"></script>
<script src="dwr/interface/StatsService.js"></script>
<script src="script/d3.v3.min.js" charset="utf-8"></script>
<script type="text/javascript" src="script/statsItem.js" media="screen"></script>

<%			 
		} else if(path.endsWith("/"+Links.FILE_DATA) || path.endsWith("/"+Links.FILE_COMPARE_DATA) || path.endsWith("/"+Links.FILE_ANNOTATION)){
%>
<link href="style/prettify.css" type="text/css" rel="stylesheet"
	media="screen" />

<script type="text/javascript" src="script/syntax/prettify.js"
	media="screen"></script>
<script type="text/javascript" src="script/syntax/lang-apollo.js"
	media="screen"></script>
<script type="text/javascript" src="script/syntax/lang-basic.js"
	media="screen"></script>
<script type="text/javascript" src="script/syntax/lang-clj.js"
	media="screen"></script>
<script type="text/javascript" src="script/syntax/lang-css.js"
	media="screen"></script>
<script type="text/javascript" src="script/syntax/lang-dart.js"
	media="screen"></script>
<script type="text/javascript" src="script/syntax/lang-erlang.js"
	media="screen"></script>
<script type="text/javascript" src="script/syntax/lang-go.js"
	media="screen"></script>
<script type="text/javascript" src="script/syntax/lang-hs.js"
	media="screen"></script>
<script type="text/javascript" src="script/syntax/lang-lisp.js"
	media="screen"></script>
<script type="text/javascript" src="script/syntax/lang-llvm.js"
	media="screen"></script>
<script type="text/javascript" src="script/syntax/lang-lua.js"
	media="screen"></script>
<script type="text/javascript" src="script/syntax/lang-matlab.js"
	media="screen"></script>
<script type="text/javascript" src="script/syntax/lang-ml.js"
	media="screen"></script>
<script type="text/javascript" src="script/syntax/lang-mumps.js"
	media="screen"></script>
<script type="text/javascript" src="script/syntax/lang-n.js"
	media="screen"></script>
<script type="text/javascript" src="script/syntax/lang-pascal.js"
	media="screen"></script>
<script type="text/javascript" src="script/syntax/lang-proto.js"
	media="screen"></script>
<script type="text/javascript" src="script/syntax/lang-r.js"
	media="screen"></script>
<script type="text/javascript" src="script/syntax/lang-rd.js"
	media="screen"></script>
<script type="text/javascript" src="script/syntax/lang-scala.js"
	media="screen"></script>
<script type="text/javascript" src="script/syntax/lang-sql.js"
	media="screen"></script>
<script type="text/javascript" src="script/syntax/lang-tcl.js"
	media="screen"></script>
<script type="text/javascript" src="script/syntax/lang-tex.js"
	media="screen"></script>
<script type="text/javascript" src="script/syntax/lang-vb.js"
	media="screen"></script>
<script type="text/javascript" src="script/syntax/lang-vhdl.js"
	media="screen"></script>
<script type="text/javascript" src="script/syntax/lang-wiki.js"
	media="screen"></script>
<script type="text/javascript" src="script/syntax/lang-xq.js"
	media="screen"></script>
<script type="text/javascript" src="script/syntax/lang-yaml.js"
	media="screen"></script>

<%
		}
	}
%>
<title><%=request.getParameter("jsp.head.title")%></title>

<script type="text/javascript" src="script/common.js" media="screen">
    </script>

<script type="text/javascript" src="script/selection.js" media="screen">
    </script>


<script media="screen" src="script/scroll.js" type="text/javascript">
    </script>

<script type="text/javascript" src="script/tooltip.js" media="screen">
    </script>

<script type="text/javascript" src="script/picker.js" media="screen">
    </script>

<script type="text/javascript">
		function browserPageLoaded(page, path, parameters){
			try {
    			if (parent && parent != self && parent.browserPageLoaded) {
				    parent.browserPageLoaded(page, path, parameters);
			    }
			} catch(e) {
			}
		}
    </script>
<link href="style/default.css" type="text/css" rel="stylesheet"
	media="screen" />
<link href="style/table.css" type="text/css" rel="stylesheet"
	media="screen" />
</head>