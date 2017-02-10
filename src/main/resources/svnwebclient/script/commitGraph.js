/*Copyright (c) "Kinto Soft Ltd"

Subversion ALM is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.*/

//////////////////////////////////////////////////////////
//////////////////////// FILTERS //////////////////////////
//////////////////////////////////////////////////////////


$.cookies.json=true;

var FILTER_COOKIE_NAME="svnplus_graph_filters";
var FILTER_SHOW_ALL="showAll";
var FILTER_SHOW_DELETED="showDeleted";
var FILTER_SHOW_TAGS="showTags";
var FILTER_SHOW_ISSUES="showIssues";
var FILTER_STRICT="strict";
var FILTER_UNUSED="showUnused";

function Filters(){
	this.showUnused=true;
	this.showDeleted=true;
	this.showTags=true;
	this.showAll=true;
	this.strict=false;
	this.showIssues=true;
}

Filters.prototype.getCookieOptions=function(){
	return {path:'/', expires: 365};
};

Filters.prototype.load=function(){
	var jFilters = $.cookies.get(FILTER_COOKIE_NAME);
	if(jFilters){
		this.showAll=jFilters.showAll;
		this.showDeleted=jFilters.showDeleted;
		this.showTags=jFilters.showTags;
		this.strict=jFilters.strict;
		this.showIssues=jFilters.showIssues;
		this.showUnused=jFilters.showUnused;
	}
	$("#filter_all").prop("checked", this.showAll);
	$("#filter_deleted").prop("checked", this.showDeleted);
	$("#filter_tags").prop("checked", this.showTags);	
	$("#filter_strict").prop("checked", this.strict);	
	$("#filter_issues").prop("checked", this.showIssues);	
	$("#filter_unused").prop("checked", this.showUnused);	
};

Filters.prototype.save=function(){
	$.cookies.set(FILTER_COOKIE_NAME, this, this.getCookieOptions());
};

function handleClickAll(cb) {
	  SP_DURATION=1000;
	  filters.showAll=cb.checked;
	  filters.save();
	  updateGraph();
	}

function handleClickTags(cb) {
	  SP_DURATION=1000;
	  filters.showTags=cb.checked;
	  filters.save();
	  updateGraph();
	}

function handleClickDeleted(cb) {
	  SP_DURATION=1000;
	  filters.showDeleted=cb.checked;
	  filters.save();
	  updateGraph();
	}

function handleClickUnused(cb) {
	  SP_DURATION=1000;
	  filters.showUnused=cb.checked;
	  filters.save();
	  updateGraph();
	}

function handleClickStrict(cb) {
	  SP_DURATION=1000;
	  filters.strict=cb.checked;
	  filters.save();
	  updateGraph();
	}

function handleClickIssues(cb){
	 SP_DURATION=1000;
	 filters.showIssues=cb.checked;
	 filters.save();
	updateGraph();
}
function handleClickRange(){
  SP_DURATION=1000;
	updateGraph();
}

filters = new Filters();

//////////////////////////////////////////////////////////
///////////////////////// GRAPH //////////////////////////
//////////////////////////////////////////////////////////


var loadSegmentCommits=false, segmentsLoading=0, queue_segments=[], pending_segments=0;
var SP_DURATION=1000;
var w=150, h=20, wgap=30, hgap=2;
var tw=150, th=20, thgap=3;
var svgWidth=0, svgHeight=0;
var graphMargin=40, vis, zoomRect, isDirectory, callback, repoId;

var aItems, aRevisions, aActions;

function errh(msg, exc) {
	//removeLoading();
	//alert(msg+": "+ dwr.util.toDescriptiveString(exc, 0));
	console.log(msg+": "+ dwr.util.toDescriptiveString(exc, 0));
}

dwr.engine.setErrorHandler(errh);


var tooltip = d3.select("body")
.append("div")
.style("position", "absolute")
.style("z-index", "10")
.style("background", "#FFFF66")
.style("border", "2px solid")
.style("border-radius", "4px")
.style("box-shadow", "5px 5px 1px #888888")
.style("padding","5px")
.style("visibility", "hidden");

function toArray(obj){
	var a=[];
	$.each(obj, function(key, value){
		a.push(value);
	});
	return a;
}


var line = d3.svg.line()
                         .x(function(d) { return d.x; })
                         .y(function(d) { return d.y; })
                        .interpolate(/*"linear"*/ "bundle");

var diagonal = d3.svg.diagonal().projection(function(d) {
	return [ d.x, d.y ];
});

function minRevisionFromItem(item){
	var min = Number.MAX_VALUE;
	
	for (x in item.segments){
		var s=item.segments[x];
		if(s.start < min){
			min=s.start;
		}
	}
	
	return min;
}

function resetGraphData(){
	aItems = toArray(graph.items);
	aItems.sort(function(a,b){
		return minRevisionFromItem(a)-minRevisionFromItem(b);
	});
	aRevisions = toArray(graph.revisions);
	aRevisions.sort(function(a,b){
		return a.number-b.number;
	});
	
	aActions = toArray(graph.actions);
	aCopies = graph.copies;
			
	var parent,x,y,item,a,s;
	$.each(aItems, function(){
		item = this;
		$.each(item.segments, function(_,s){
			parent=null;
			$.each(s.actions, function(_,a){
				a.meta_isFromCopy=false;
				a.meta_isToCopy=false;
				a.parent=null;
				a.children=[];
				if(parent!=null){
					a.parent=parent;
					parent.children.push(a);					
				}
				parent=a;
			})
		})
	});
	
	var c, aFrom, aTo;
	$.each(aCopies, function(){
		c=this;
		aFrom = getActionFrom(c);
		aFrom.meta_isFromCopy=true;
		
		aTo = getActionTo(c);
		aTo.meta_isToCopy=true;
		
		aTo.parent=aFrom;
		aFrom.children.push(aTo);
	});
}

function populateUIRange(){
	var minRev=Number.MAX_VALUE, maxRev=0, rev;
	$.each(graph.revisions, function(_,rev){
		if(minRev>rev.number){
			minRev=rev.number;
		}
		
		if(maxRev<rev.number){
			maxRev=rev.number;
		}
	});
	
	$("#startrange").prop("value",minRev);
	$("#endrange").prop("value",maxRev);
}

function drawGraph(_repoId, path, revision, _isDirectory, _callback){
	if(path==null || path=="" || path=="/"){
		removeLoading();
		//alert("The repository root has not a commit graph.");
		return;
	}
	
	repoId=_repoId;
	isDirectory=_isDirectory;
	callback=_callback;
	
	progress(false);

	CommitGraphService.getGraph(repoId, path, revision, isDirectory, loadSegmentCommits, function(g){
		graph=g;
		vis = d3.select("#graph")
		.append("svg:svg")
		.attr("width", "100%")
		.attr("height", 600)
		.attr("id","topsvg")
		.append("svg:g")
		.call(d3.behavior.zoom().scaleExtent([.01, 2])
				.on("zoom", function(){
					vis.attr("transform", "translate(" + d3.event.translate + ")scale(" + d3.event.scale + ")");
				}))
		.append("svg:g")
		.attr("transform", "translate("+(graphMargin+w)/2+","+(graphMargin+h)/2+")");
	
		zoomRect = vis.append("svg:rect")
		.style("fill", "none")
		.style("pointer-events","all")
		.style("stroke","#000000")
		.style("stroke-width",1)
		.style("stroke-dasharray", "10, 5")
		.attr("transform", "translate(-"+(graphMargin+w)/2+",-"+(graphMargin+h)/2+")")
		.attr("rx",5)
		.attr("ry",5);
		
		$.each(graph.actions, function(_,act){
			act.x0=0;
			act.y0=0;
		});

		updateGraph();
	
		var parent;
		if(!loadSegmentCommits){
			$.each(aItems, function(_, item){
				$.each(item.segments, function(_,s){
					parent=null;
						queue_segments.push(s);
				});
			});
		}

		pending_segments=queue_segments.length;
		progress(0);
		processQueueSegments();		
	});		
	
}

function progress(value){
	$( "#progressbar" ).progressbar({
	      value: value
	    });
}

function processQueueSegments(){
	if(queue_segments.length==0){
		removeLoading();
		populateUIRange();
		if(!loadSegmentCommits){
			updateGraph();
		}
		return;
	}
	
	if(queue_segments.length%10==0){
		updateGraph();
	}
	
	var segment = queue_segments.shift();
	
	var id = window.setInterval(function(){
		window.clearInterval(id);
		processQueueSegmentsResponse(segment);
	},1)
}

function processQueueSegmentsResponse(segment){
	var item=segment.item;
	segmentsLoading++;
	CommitGraphService.getSegmentCommits(repoId, item.parent, item.child, segment.start, segment.end, isDirectory, function(response){
		segmentsLoading--;
		SP_DURATION=0;
		progress(100*(1-queue_segments.length/pending_segments));
		var item = graph.items[response.path+response.name];
		var segment = item.segments[response.startRev+":"+response.endRev];
		var startAction = segment.actions[0];
		if(response.actions.length>1 && response.actions[1].id=="M"){
			segment.meta_unused=false;
		}else{
			segment.meta_unused=true;
		}
		response.actions.forEach(function(fooAct){
			fooAct.x0=startAction.x0;
			fooAct.y0=startAction.y0;
			var itemPath = item.parent+item.child;
			var key=itemPath+"@"+fooAct.revision.number;
			var existingAction = graph.actions[key];
			if(existingAction){
				if(existingAction.id=="G"){
					existingAction.id=fooAct.id;
				}
			}else{
				graph.actions[key]=fooAct;
				fooAct.segment=segment;
				segment.actions.push(fooAct);
				var existingRevision = graph.revisions[fooAct.revision.number];
				if(existingRevision){
					fooAct.revision=existingRevision;
					existingRevision.actions.push(fooAct);
				}else{
					graph.revisions[fooAct.revision.number]=fooAct.revision;
					fooAct.revision.actions.push(fooAct);
				}
			}
		});
		segment.actions.sort(function(a,b){
			return a.revision.number-b.revision.number;
		});
		processQueueSegments();
	});
}

function key(d){
	return path(d)+"@"+d.revision.number;
}

function isDeletable(act){
	return act.id=="M" && !act.meta_isFromCopy && !act.meta_isToCopy;
}


function removeItemFromArray(array, item){
	array.slice(array.indexOf(item),1);
}

function keepInTree(act){
	/*if(filters.showIssues && act.revision.tickets.length>0){
		return true;
	}*/
	if(act.parent){
		$.each(act.children, function(){
			child=this;
			child.parent=act.parent;
			act.parent.children.push(child);
			if(!act.parent.meta_isFromCopy){
				act.parent.meta_isFromCopy=act.meta_isFromCopy;
			}
			if(!act.parent.meta_isToCopy){
				act.parent.meta_isToCopy=act.meta_isToCopy;
			}
		});
	}
	act.children=[];
	act.parent=null;
	return false;
}

function applyFilterStrict(){
	if(!filters.strict){
		return;
	}
	
	aActions = $.grep(aActions, function(act){		
		if(act.id=="G"){
			return keepInTree(act);
		}else{
			return true;
		}
	});	
}

function applyFilterShowAll(){
	if(filters.showAll){
		return;
	}
	
	aActions = $.grep(aActions, function(act){		
		if(isDeletable(act)){
			return keepInTree(act);
		}else{
			return true;
		}
	});	
}


function compactActions(){
	if(filters.showAll){
		return;
	}
	aActions=$.grep(aActions, function(act){
		if(act.id=="M" || act.id=="G"){
			var keep=false;
			$.each(act.children, function(_,child){
				if(aActions.indexOf(child)>-1){
					if(path(act)!=path(child)){
						keep=true;
					}	
				}
			});
			if(!keep){
				keepInTree(act);
			}
			return keep;
		}else{
			return true;
		}
	});
}


function compactRevisions(){
	var i=0;
	var prev;
	aRevisions = $.grep(aRevisions, function(rev){
		var deleteable = true;
		$.each(rev.actions, function(){
			act=this;
			if(aActions.indexOf(act)>-1){
				deleteable=false;

			}
		});
		if(!deleteable){
			// rev.y=(h+hgap)*i++;		
			if(prev){
				rev.y=prev.y+(h+hgap);
				
				if(filters.showIssues){
					var t=prev.tickets.length;
					if(t){
						rev.y+=t*(th+thgap);
					}
				}
			}else{
				rev.y=0;
			}
			prev=rev;
		}
		return !deleteable;
	});
}

function compactItems(){
	var i=0;
	aItems = $.grep(aItems, function(item){
		var deleteable = true;
		$.each(item.segments, function(){
			var seg=this;
			$.each(seg.actions, function(){
				var act=this;
				if(aActions.indexOf(act)>-1){
					deleteable=false;

				}
			});
		});
		if(!deleteable){
			item.x=(w+wgap)*i++;
		}
		return !deleteable;
	});
}

function hasUndeletedTags(act){
	if(!filters.showTags){
		return false;
	}
	var res=false;
	$.each(act.children, function(_, child){
		if(!child.segment.meta_deleted){
			if(child.segment.item.meta_tag){
				res=(path(act)!=path(child));
			}
		}
	});
	return res;
}

function applyFilterDeleted(){
	if(filters.showDeleted){
		return;
	}
	aActions = $.grep(aActions, function(act){
		if(act.segment.meta_deleted){
			if(hasUndeletedTags(act)){
				return true;
			}else{
				return keepInTree(act);				
			}
			//return keepInTree(act);				
		}else{
			return true;
		}
	});	
}

function applyFilterUnused(){
	if(filters.showUnused){
		return;
	}
	aActions = $.grep(aActions, function(act){
		if(act.segment.meta_unused){
			return keepInTree(act);
		}else{
			return true;
		}
	});	
}
function applyFilterTags(){
	if(filters.showTags){
		return;
	}
	aActions = $.grep(aActions, function(act){
		if(act.segment.item.meta_tag){
			return keepInTree(act);
		}else{
			return true;
		}
	});
}

function applyFilterRange(){
	var minRev=$("#startrange").prop("value");
	minRev=(minRev==""?0:minRev);
	
	var maxRev=$("#endrange").prop("value");
	maxRev=(maxRev==""?Number.MAX_VALUE:maxRev);
	
	aActions=$.grep(aActions, function(act){
		var rev=act.revision.number;
		if(rev<minRev || rev>maxRev){
			return keepInTree(act);
		}else{
			return true;
		}
	});
}

function updateCoordinates(d){
	//d.x = (w+wgap) * aItems.indexOf(d.segment.item);
	d.x = d.segment.item.x;
	
	//d.y = (h+hgap) * aRevisions.indexOf(d.revision);
	d.y = d.revision.y;

	right = d.x+w;
	bottom = d.y+h;
	
	if(filters.showIssues){
		var t = d.revision.tickets.length;
		bottom+=t*(th+thgap);
	}
	
	if(svgWidth<(right)){
		svgWidth=right;
	}
	if(svgHeight<(bottom)){
		svgHeight=bottom;
	}
}

function position(d){
	return "translate("+d.x+","+d.y+")";
}

function positionOld(d){
	return "translate("+d.x0+","+d.y0+")";
}

function getSegmentStart(act){
	var actStart=act.segment.actions[0];
	return actStart;
}

function actionHeight(d) {
	if(!d.revision){
	}
	var t = d.revision.tickets.length;
	if(filters.showIssues){
		return h + t * (th + thgap);		
	}else{
		return h;
	}
}

function updateGraph(){
	
	resetGraphData();
	
	applyFilterStrict();
	
	applyFilterShowAll();
	
	applyFilterTags();
	
	applyFilterDeleted();
	
	applyFilterUnused();
	
	applyFilterRange();
	
	compactActions();
	
	compactRevisions();
	
	compactItems();
	
	$.each(aActions, function(){
		var act=this;
		updateCoordinates(act);
	});
	
	var nodes = vis.selectAll(".node").data(aActions, key);
	
	var g =	nodes.enter()
		.append("svg:g")
		.attr("class", "node")
		.attr("opacity", 1e-6)
		.attr("transform", function(d){return positionOld(getSegmentStart(d))});

		//.transition().duration(SP_DURATION)
		//.attr("transform",position)
		
		
	var rects = g.append("svg:rect")
			.attr("class","action")
			.attr("width",w)
			.attr("height",actionHeight)
			.attr("fill",function(d){return color(d)})
			.attr("x",-w/2)
			.attr("y",-h/2)
			.attr("rx",4)
			.attr("ry",4)
			.attr("stroke","#000000");
		
	rects.append("svg:title").attr("class", "actionTitle");
	
	var withLink = g.filter(function(d){return d.id=="A" || d.id=="M" || d.id=="G" || d.id=="R"});
 
	var withNoLink = g.filter(function(d){return d.id=="D"});
	
	withLink = withLink.append("svg:a")
		.attr("xlink:href", function(d){
			var item = path(d);
			if(isDirectory) 
				return "revisionList.jsp?url="+item+"&pegrev="+d.revision.number+"&crev="+d.revision.number+"&startrevision="+d.revision.number+"&crev="+d.revision.number;
			else{
					var x = "fileCompare.jsp?url="+item+"&pegrev="+d.revision.number+"&endrev="+d.revision.number+"&crev="+d.revision.number;
					if(d.id=="M"){
						x=x+"&startrev="+(d.parent.revision.number);
					}
					return x;
				}
			});
	
	addLabel(withLink);
	addLabel(withNoLink);
		
	nodes
		.transition().duration(SP_DURATION)
		.style("opacity", 1)
		.attr("transform", position)
			.select("rect")
			.attr("fill",function(d){return color(d)});
	
	nodes.selectAll(".actionTitle").text(addTooltip);
	
	var updateRects = nodes.selectAll(".action").transition().duration(SP_DURATION).attr("height", actionHeight)
		.attr("stroke", function(d){return (!filters.showDeleted && d.id!="D" && d.segment.meta_deleted)?"#FFCCCC":"#000000";});
	
	
	var issues = 
		nodes.filter(function(d){return d.revision.tickets.length>0;})
		.selectAll(".issue").data(function(d){return filters.showIssues?d.revision.tickets:[];});
			
	
	var issueEnter = issues.enter().append("svg:g").attr("class", "issue")
		.attr("transform",function(d,i){return "translate(-"+tw/2+","+(5+thgap+i*(th+thgap))+")"})
		.style("opacity", 1e-6);
		
	var icons =  issueEnter.append("svg:image")
		 .attr("width",16)
		 .attr("height", 16)
		 .attr("y",2)
		 .attr("xlink:href", function(d){
				 return d.jiraBaseUrl+d.iconUrl;
			});
		/*.on("mouseover", function(d){tooltip.html("hola"); return tooltip.style("visibility", "visible");})
		.on("mousemove", function(){return tooltip.style("top", (d3.event.pageY-10)+"px").style("left",(d3.event.pageX+10)+"px");})
		.on("mouseout", function(){return tooltip.style("visibility", "hidden");})*/

	icons.append("svg:title").text(tooltipIssue);
	
	issueEnter
		.append("svg:text")
		.attr("class","simpletext")
		.style("font-size","11")
		.style("cursor","pointer")
		.on("click", function(d){navigateTo(d.jiraBaseUrl+"/browse/"+d.keyNum);})
		//.attr("transform",function(d,i){return "translate("+(0)+","+(15)+")"})
		.text(function(d){return d.keyNum;})
		.attr("x",function(d){ d.nx=16+2+(tw-(16+2)-this.getComputedTextLength())/2; return d.nx;})
		.attr("y",14)
		.filter(function(d){return d.resolved;})
			 .style("text-decoration","line-through");

	
	icons.attr("x", function(d){return d.nx-18;});
	
	issues.transition().duration(SP_DURATION).style("opacity",1);

	issues.exit()
		.transition().duration(SP_DURATION)
		.style("opacity", 1e-6)
		.remove();

	nodes.exit()
		.transition().duration(SP_DURATION)
		.style("opacity", 1e-6)
		.attr("transform", function(d){return position(getSegmentStart(d))})
		.remove();
	
	var aEdges = $.grep(aActions, function(act){
		return act.parent && aActions.indexOf(act.parent)>-1;
		});

	var edges = vis.selectAll("path").data(aEdges, key);
	
	edges.enter()
	    .append("svg:path")
		.attr("fill","none")
		.attr("stroke","#000000")
		.attr("d", function(d){
			var actStart = getSegmentStart(d);
			return line(copy2linkpointsOld(actStart, actStart));
		})
		.transition().duration(SP_DURATION)
		.attr("opacity",1e-6)
		.attr("d", function(d){
			return line(copy2linkpoints(d.parent, d));
		});
	
	edges
	.transition()
	.duration(SP_DURATION)
	.attr("opacity","0.5")
	.attr("d", function(d){
		return line(copy2linkpoints(d.parent, d));
	});
	
	
	edges.exit()
	.transition()
	.duration(SP_DURATION)
	.attr("opacity",1e-6)
	.attr("d", function(d){
		var actStart = getSegmentStart(d);
		return line(copy2linkpoints(actStart, actStart));
	})
	.remove();
	
	
	zoomRect.transition().duration(SP_DURATION).attr("width", svgWidth+graphMargin)
	.attr("height", svgHeight+graphMargin);

	$.each(aActions, function(){
		var act=this;
		act.x0=act.x;
		act.y0=act.y;
	});

	callback();
	
}

function tooltipIssue(d){
	var versionsAffected="", versionsFixed="";
	$.each(d.versionsAffected, function(){
		if(versionsAffected!=""){
			versionsAffected+=" | ";
		}
		versionsAffected+=this;
	});
	$.each(d.versionsFixed, function(){
		if(versionsFixed!=""){
			versionsFixed+=" | ";
		}
		versionsFixed+=this;
	});
	var text = "Issue: "+d.keyNum+
				"\nSummary: "+d.summary+
				"\nType: "+d.typeName+
				"\nPriority: "+d.priorityName+
				"\nStatus: "+d.statusName+
				"\nAssignee: "+d.assigneeDisplayName+
				"\nReporter: "+d.reporter+
				"\nCreated: "+formatDate(d.created)+
				"\nUpdated: "+formatDate(d.updated)+
				"\nAffect Versions: "+versionsAffected+
				"\nFix Versions: "+versionsFixed+
				"\nDescription:\n"+(d.description?d.description:"");
	return text;
}

function formatDate(d){
	var dd = d.getDate(); 
	var mm = d.getMonth()+1; //January is 0! 
	var yyyy = d.getFullYear(); 
	if(dd<10){dd='0'+dd} 
	if(mm<10){mm='0'+mm} 
	return yyyy+"-"+mm+"-"+dd;
	return d;
}


function removeLoading(){
	d3.select("#progressbar").remove();
}

function getActionTo(copy){
    var toItemPath=copy.toItem.parent+copy.toItem.child;
    return graph.actions[toItemPath+"@"+copy.toRevision.number];
}

function getActionFrom(copy){
   var fromItemPath=copy.fromItem.parent+copy.fromItem.child;
   return graph.actions[fromItemPath+"@"+copy.fromRevision.number];
}

function path(a){
	return a.segment.item.parent+a.segment.item.child;
}

function addTooltip(d){
		var label =  "Action: " + actionLabel(d)+
		"\nRevision: "+d.revision.number+
		"\nPath: "+path(d)+
		"\nAuthor: "+d.revision.author+
		"\nDate: "+d.revision.date;
		if(d.parent && path(d.parent)!=path(d)){
			label += "\nCopied from path: "+path(d.parent)+
			"\nCopied from revision: "+d.parent.revision.number;
		}
		label+= "\nCommnent: "+(d.revision.comment==null?"":d.revision.comment.replace(/\n/g,'\n').replace(/\t/g,'    '));
		
		return label;
}
function addLabel(node){
	node
		.append("svg:text")
		.text(function(d){return d.revision.number;})
		.attr("class","simpletext")
		.style("font-weight","bold")
		.attr("y", 10/2)
		.attr("x",function(){return -w/2+(w-this.getComputedTextLength())/2;}
		);
}

function copy2diagonal(a1, a2){
	return diagonal({source:a1 , target: a2})
}
function copy2linkpoints(a1, a2){
	return copy2linkpointsAux(a1.x, a1.y, a2.x, a2.y);
}

function copy2linkpointsOld(a1, a2){
	return copy2linkpointsAux(a1.x0, a1.y0, a2.x0, a2.y0);
}

function copy2linkpointsAux(x1, y1, x2, y2){
	var points=[];
	
	
	if(x1==x2){
		points.push({x:x1+w/2 , y:y1});
		points.push({x:x1+w/2+wgap/2, y:y1});
		points.push({x:x1+w/2+wgap/2, y:y2});
		points.push({x:x2+w/2, y:y2});
	}else if(x1>x2){
		points.push({x:x1-w/2, y:y1});
		points.push({x:x1-wgap/2-w/2, y:y1});
		points.push({x:x1-wgap/2-w/2, y:y2});
		points.push({x:x2+w-w/2, y:y2});
	}else{
		points.push({x:x1+w-w/2 , y:y1});
		points.push({x:x1+w+wgap/2-w/2, y:y1});
		points.push({x:x1+w+wgap/2-w/2, y:y2});
		points.push({x:x2-w/2, y:y2});
	}
	return points;
}

function color(action){
	switch(action.id){
	case "A":
		if(action.segment.item.meta_tag){
			return "#FFFFCC";
		}else{
			return "#E6FFE5";
		}
	case "M":
		return "#EEEEEE";
	case "D":
		return "#FFCCCC";
	case "G":
		return "#FFFFFF";
	case "R":
		return "#66FFFF";

	}
	
	return 0;
}

function actionLabel(action){
	var label="Unknown";
	switch(action.id){
	case "A":
		label = "Added";
		break;
	case "M":
		label = "Modified";
		break;
	case "D":
		label = "Deleted";
		break;
	case "G":
		label = "Not modified";
		break;
	case "R":
		label = "Replaced";
		break;
	}
	
	if(action.id!="D" && action.segment.meta_deleted){
		label+= " (deleted)";
	}
	
	return label;
}
