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

function errh(msg, exc) {
		//alert(msg+": "+ dwr.util.toDescriptiveString(exc, 0));
	restoreDateFormat();
}

dwr.engine.setErrorHandler(errh);

var jiraDateProtoype;
var gFilterId;
var gMode="svn";


function saveDateFormat(){
	if(Date.prototype.__msh_oldSetFullYear){
		jiraDateProtoype=Date.prototype.setFullYear;
		Date.prototype.setFullYear = Date.prototype.__msh_oldSetFullYear;
	}
}

function restoreDateFormat(){
	if(jiraDateProtoype){
		Date.prototype.setFullYear=jiraDateProtoype;
		jiraDateProtoype=null;		
	}
}

function showStatsItem(repoId, path, revision, isDirectory, callback){
	StatsService.getHistorySizeByUser(repoId, path, revision, isDirectory, function(sizes){
		showStats(sizes, callback, true);
	});
}

function showStatsUser(repoId, username, callback){
	StatsService.getHistorySizeForUser(repoId,username, function(sizes){
		showStats(sizes, callback, false);
	});
}


function showStatsFilter(filterId, callback){
	gMode="jira";
	StatsService.getHistorySizeForJIRAFilter(filterId, function(sizes){
		gFilterId=filterId;
		saveDateFormat();
		showStats(sizes, callback, true);
		restoreDateFormat();
	});
}

function showStats(sizes, callback, showUsersPie){
		var tMin=Number.MAX_VALUE, tMax=Number.MIN_VALUE, maxSize=Number.MIN_VALUE;
		var dMin,dMax;
		var rows = [];//d3.csv.parse(sizes);
		
		for (var author in sizes){
			for(var d in sizes[author]){
				rows.push({user:author, date:d, size: sizes[author][d]})				
			}
		}
		var daysSizeData = d3.nest()
			.key(function(d) {return d.date; })
			.rollup(function(d){var size = d3.sum(d, function(d){return d.size;}); 
			
				var dt = format.parse(d[0].date);
				var t = dt.getTime();
				if(t<tMin){
					tMin = t;
					dMin = dt;
				}
				if(t>tMax){
					tMax=t;
					dMax=dt;
				}
				maxSize=Math.max(size, maxSize); 
				return {date:dt, changeSize:size};})
			.map(rows);
	
		if(dMin){
			calendar(daysSizeData, dMin.getFullYear(), dMax.getFullYear(), maxSize);
			
			drawChangeSizeChart(d3.values(daysSizeData), dMin, dMax, maxSize);
			
			if(showUsersPie){
					var total=0;
					var usersSizeData = d3.nest()
					.key(function(d) {return d.user; })
					.rollup(function(d){var size = d3.sum(d, function(d){return d.size;}); 
						total+=size;
						return {user:d[0].user, size:size};})
					.map(rows);
					
					usersSizeData = d3.values(usersSizeData).sort(function(b,a){
						if(a.size>b.size){
							return 1;
						}else if(a.size < b.size){
							return -1;
						}else{
							return 0;
						}
					});
				
					drawPie("foo", usersSizeData, "#users", "colorScale20", 10, 200, 100, 0);
			}
		}
		
		callback();
}



var day = d3.time.format("%w"),
week = d3.time.format("%U"),
percent = d3.format(".1%"),
format = d3.time.format("%Y-%m-%d");

var cellSize = 17;

function getUserLink(user){
	if(gMode=="jira"){
		return "SWCTopMenuAction!filterCommitsByAuthor.jspa?author="+user+"&filterId="+gFilterId;
	}
	return "statsUser.jsp?username="+user;
}

function getDateLink(d){
	if(gMode=="jira"){
		return "SWCTopMenuAction!filterCommitsByDate.jspa?day="+format(d)+"&filterId="+gFilterId;
	}
	return "";
}

		
function calendar(data, yMin, yMax, maxSize){
	var width = 960,
	height = 136; // cell size

	var color = d3.scale.quantize()
	.domain([0, 1])
	.range(d3.range(10).map(function(d) { return "q" + d + "-10"; }));
	
	
	var svg = d3.select("#calendar").selectAll("svg")
	.data(d3.range(yMin, yMax+1))
	.enter().append("svg")
	.attr("width", width)
	.attr("height", height)
	.attr("class", "swc_calendar")
	.append("g")
	.attr("transform", "translate(" + ((width - cellSize * 53) / 2) + "," + (height - cellSize * 7 - 1) + ")");

	svg.append("text")
	.attr("transform", "translate(-6," + cellSize * 3.5 + ")rotate(-90)")
	.style("text-anchor", "middle")
	.text(function(d) { return d; });
	
	var rect = svg.selectAll(".day")
	.data(function(d) { return d3.time.days(new Date(d, 0, 1), new Date(d + 1, 0, 1)); })
	.enter()
	.append("svg:a")
	.attr("xlink:href", function(d) { return getDateLink(d);})
	.append("rect")
	.attr("class", "swc_day")
	.attr("width", cellSize)
	.attr("height", cellSize)
	.attr("x", function(d) { return week(d) * cellSize; })
	.attr("y", function(d) { return day(d) * cellSize; })
	.datum(format);
	
	rect.append("title")
	.text(function(d) { return d; });

	svg.selectAll(".month")
	.data(function(d) { return d3.time.months(new Date(d, 0, 1), new Date(d + 1, 0, 1)); })
	.enter().append("path")
	.attr("class", "swc_month")
	.attr("d", monthPath);
	
	var threshold = 100;
	var thresholdPanel = d3.select("#threshold");
	thresholdPanel.append("label").html("Color threshold: ");
    var colorThresholdLabel = thresholdPanel.append("label");
	thresholdPanel.append("button").text("-").on("click", function () {
        threshold -= 10;
        if (threshold < 0) threshold = 0;
        updateCalendarView(rect, data, maxSize, color, colorThresholdLabel, threshold);
    });
	thresholdPanel.append("button").text("+").on("click", function () {
        threshold += 10;
        if (threshold > 1000) threshold = 1000;
        updateCalendarView(rect, data, maxSize, color, colorThresholdLabel, threshold);
    });
	thresholdPanel.append("button").text("reset").on("click", function () {
        threshold = 100;
        updateCalendarView(rect, data, maxSize, color, colorThresholdLabel, threshold);
    });
	
	updateCalendarView(rect, data, maxSize, color, colorThresholdLabel, threshold);
}

function updateCalendarView(rect, data, maxSize, color, colorThresholdLabel, threshold) {
	rect.filter(function(d) {return d in data; })
	  .attr("class", function(d) { var norm=(1-(maxSize-(data[d].changeSize*threshold/100)) / maxSize); return "swc_day " + color(norm); })
	  //.style("cursor", "pointer")
	  .select("title")
	  .text(function(d) { return "Date: "+d+"\nChange size: "+data[d].changeSize; });
	

    colorThresholdLabel.html(threshold + "%");
}

function monthPath(t0) {
	var t1 = new Date(t0.getFullYear(), t0.getMonth() + 1, 0),
	  d0 = +day(t0), w0 = +week(t0),
	  d1 = +day(t1), w1 = +week(t1);
	return "M" + (w0 + 1) * cellSize + "," + d0 * cellSize
	  + "H" + w0 * cellSize + "V" + 7 * cellSize
	  + "H" + w1 * cellSize + "V" + (d1 + 1) * cellSize
	  + "H" + (w1 + 1) * cellSize + "V" + 0
	  + "H" + (w0 + 1) * cellSize + "Z";
}


// ///////////////////////////////////////////////////////////////7
// delta-flora


function drawChangeSizeChart(sizesData, dMin, dMax, maxSize) {
    var margin = {top: 20, right: 20, bottom: 100, left: 40},
        margin2 = {top: 430, right: 20, bottom: 15, left: 40},
        width = 960 - margin.left - margin.right,
        height = 500 - margin.top - margin.bottom,
        height2 = 500 - margin2.top - margin2.bottom;

    var dateFormat = format;
    var valueFormat = function(n) {
         var s = d3.format(",")(n);
        return s.length < 6 ? s : d3.format("s")(n);
    };

    var x = d3.time.scale().rangeRound([0, width]);
    var x2 = d3.time.scale().rangeRound([0, width]);
    var y = d3.scale.linear().range([height, 0]);
    var y2 = d3.scale.linear().range([height2, 0]);
    var xAxis = d3.svg.axis().scale(x).orient("bottom");
    var xAxis2 = d3.svg.axis().scale(x2).orient("bottom");
    var yAxis = d3.svg.axis().scale(y).orient("left");//.tickFormat(valueFormat);
    var xAxisLabel = "Date";
    var yAxisLabel = "Change size";

    var brush = d3.svg.brush().x(x2).on("brush", onBrush);

    var rootElement = d3.select("#bars");
    var svg = rootElement.append("svg")
            .attr("width", width + margin.left + margin.right)
            .attr("height", height + margin.top + margin.bottom);
    svg.append("defs").append("clipPath").attr("id", "chartClip")
            .append("rect").attr("width", width).attr("height", height);

   
    var daysData = sizesData;

    var timeInterval = d3.time.day;
    var data = aggregate(daysData, timeInterval);

    var fromDate = dMin;
    var toDate = dMax;
    x.domain([fromDate, timeInterval.offset(toDate, 1)]);
    y.domain([0, maxSize]);
    x2.domain(x.domain());
    y2.domain(y.domain());
    var barWidth = barWidthFor(timeInterval, fromDate, toDate);
    var zoomedView = svg.append("g")
            .attr("transform", "translate(" + margin.left + "," + margin.top + ")");
    zoomedView.append("g")
            .attr("class", "x axis")
            .attr("transform", "translate(0," + height + ")")
            .call(xAxis);

    zoomedView.append("g")
            .attr("class", "y axis")
            .call(yAxis)
            .append("text")
            .attr("transform", "rotate(-90)")
            .attr("y", 6)
            .attr("dy", ".71em")
            .style("text-anchor", "end")
            .text(yAxisLabel);
    createBarsOn(zoomedView);

    var brushFooter = svg.append("g")
            .attr("transform", "translate(" + margin2.left + "," + margin2.top + ")");
    createBarsAndBrushOn(brushFooter);


    var groupByPanel = d3.select("#groupby");
    groupByPanel.append("label").html("Group by: ");
    var groupByDropDown = groupByPanel.append("select");
    groupByDropDown.append("option").attr("value", "day").html("days");
    groupByDropDown.append("option").attr("value", "monday").html("weeks");

    groupByDropDown.on("change", function() {
        timeInterval = d3.time[this.value];
        updateChart();
    });

    function updateChart() {
		saveDateFormat();
        data = aggregate(daysData, timeInterval);
        x.domain([fromDate, timeInterval.offset(toDate, 1)]);
        y.domain([0, d3.max(data, function (d) {
            return d.changeSize;
        })]);
        x2.domain(x.domain());
        y2.domain(y.domain());

        barWidth = barWidthFor(timeInterval, fromDate, toDate);

        createBarsOn(zoomedView);
        createBarsAndBrushOn(brushFooter);

        zoomedView.select(".y.axis").call(yAxis);

        onBrush();
       restoreDateFormat();
    }
    function aggregate(daysData, timeInterval) {
        if (timeInterval == d3.time.day) return daysData;
        return d3.values(d3.nest()
                        .key(function (d) { return timeInterval.floor(d.date); })
                        .rollup(function (days) {
                            var totalChangeSize = d3.sum(days, function (d) { return d.changeSize; });
                            var date = (days.length == 0 ? null : timeInterval.floor(days[0].date));
                            return {date: date, changeSize: totalChangeSize };
                        })
                        .map(daysData)).filter(function (d) {
                    return d.changeSize > 0;
                });
    }
    function createBarsAndBrushOn(element) {
        element.selectAll("g").remove();
        element.append("g")
                .selectAll(".bar2")
                .data(data)
                .enter().append("rect")
                .attr("class", "bar")
                .attr("x", function (d) { return x2(d.date); })
                .attr("width", barWidth)
                .attr("y", function (d) { return y2(d.changeSize); })
                .attr("height", function (d) { return height2 - y2(d.changeSize); })
                .append("title");
        element.append("g")
                .attr("class", "x brush")
                .call(brush)
                .selectAll("rect")
                .attr("y", -6)
                .attr("height", height2 + 7);
    }
    function createBarsOn(element) {
        element.selectAll(".bar").remove();
        element.selectAll(".bar")
                .data(data)
                .enter().append("rect")
                .attr("class", "bar")
                .attr("clip-path", "url(#chartClip)")
                .attr("x", function (d) { return x(d.date); })
                .attr("width", barWidth)
                .attr("y", function (d) { return y(d.changeSize); })
                .attr("height", function (d) { return height - y(d.changeSize); })
                .append("title")
                .text(function (d) {
                    var dateAsString;
                    if (timeInterval == d3.time.day) {
                        dateAsString = dateFormat(d.date);
                    } else {
                        dateAsString = dateFormat(d.date) + " to " + dateFormat(timeInterval.offset(d.date, 1));
                    }
                    return xAxisLabel + ": " + dateAsString + "\n" +
                            yAxisLabel + ": " + valueFormat(d.changeSize);
                });
    }
    function onBrush() {
        var domain = brush.empty() ? x2.domain() : brush.extent();
        x.domain(domain);
        barWidth = barWidthFor(timeInterval, domain[0], domain[1]);
        zoomedView.selectAll(".bar")
                .data(data)
                .attr("x", function (d) { return x(d.date); })
                .attr("width", barWidth);
        zoomedView.select(".x.axis").call(xAxis);
    }
    function barWidthFor(timeInterval, fromDate, toDate) {
        var barWidth = width / nonZero(timeInterval.range(fromDate, toDate).length);
        barWidth = Math.floor(barWidth);
        return barWidth > 0 ? barWidth : 1;
    }
    function nonZero(length) {
        return (length > 0 ? length : 0.0000001);
    }
}

/// pie

function drawPie(pieName, dataSet, selectString, colors, margin, outerRadius, innerRadius, sortArcs) {

// pieName => A unique drawing identifier that has no spaces, no "." and no "#" characters.
// dataSet => Input Data for the chart, itself.
// selectString => String that allows you to pass in
//           a D3 select string.
// colors => String to set color scale.  Values can be...
//           => "colorScale10"
//           => "colorScale20"
//           => "colorScale20b"
//           => "colorScale20c"
// margin => Integer margin offset value.
// outerRadius => Integer outer radius value.
// innerRadius => Integer inner radius value.
// sortArcs => Controls sorting of Arcs by value.
//              0 = No Sort.  Maintain original order.
//              1 = Sort by arc value size.

// Color Scale Handling...
  var colorScale = d3.scale.category20c();
  switch (colors)
  {
    case "colorScale10":
      colorScale = d3.scale.category10();
      break;
    case "colorScale20":
      colorScale = d3.scale.category20();
      break;
    case "colorScale20b":
      colorScale = d3.scale.category20b();
      break;
    case "colorScale20c":
      colorScale = d3.scale.category20c();
      break;
    default:
      colorScale = d3.scale.category20c();
  }
    var canvasWidth = 960;
    var pieWidthTotal = outerRadius * 2;
    var pieCenterX = outerRadius + margin/2;
	var pieCenterY = outerRadius + margin/2;
	  var legendBulletOffset = 30;
	  var legendVerticalOffset = outerRadius - margin;
	  var legendTextOffset = 20;
	  var textVerticalSpace = 20;
	
	var canvasHeight = 0;
	  var pieDrivenHeight = outerRadius*2 + margin*2;
	  var legendTextDrivenHeight = (dataSet.length * textVerticalSpace) + margin*2;
	// Autoadjust Canvas Height
	if (pieDrivenHeight >= legendTextDrivenHeight)
	{
	canvasHeight = pieDrivenHeight;
	}
	else
	{
	canvasHeight = legendTextDrivenHeight;
	}

  var x = d3.scale.linear().domain([0, d3.max(dataSet, function(d) { return d.size; })]).rangeRound([0, pieWidthTotal]);
  var y = d3.scale.linear().domain([0, dataSet.length]).range([0, (dataSet.length * 20)]);


  var synchronizedMouseOver = function() {
    var arc = d3.select(this);
    var indexValue = arc.attr("index_value");

    var arcSelector = "." + "pie-" + pieName + "-arc-" + indexValue;
    var selectedArc = d3.selectAll(arcSelector);
    selectedArc.style("fill", "Maroon");

    var bulletSelector = "." + "pie-" + pieName + "-legendBullet-" + indexValue;
    var selectedLegendBullet = d3.selectAll(bulletSelector);
    selectedLegendBullet.style("fill", "Maroon");

    var textSelector = "." + "pie-" + pieName + "-legendText-" + indexValue;
    var selectedLegendText = d3.selectAll(textSelector);
    selectedLegendText.style("fill", "Maroon");
  };

  var synchronizedMouseOut = function() {
    var arc = d3.select(this);
    var indexValue = arc.attr("index_value");

    var arcSelector = "." + "pie-" + pieName + "-arc-" + indexValue;
    var selectedArc = d3.selectAll(arcSelector);
    var colorValue = selectedArc.attr("color_value");
    selectedArc.style("fill", colorValue);

    var bulletSelector = "." + "pie-" + pieName + "-legendBullet-" + indexValue;
    var selectedLegendBullet = d3.selectAll(bulletSelector);
    var colorValue = selectedLegendBullet.attr("color_value");
    selectedLegendBullet.style("fill", colorValue);

    var textSelector = "." + "pie-" + pieName + "-legendText-" + indexValue;
    var selectedLegendText = d3.selectAll(textSelector);
    selectedLegendText.style("fill", "Blue");
  };

  var tweenPie = function (b) {
    b.innerRadius = 0;
    var i = d3.interpolate({startAngle: 0, endAngle: 0}, b);
    return function(t) {
      return arc(i(t));
    };
  };

  // Create a drawing canvas...
  var canvas = d3.select(selectString)
    .append("svg:svg") //create the SVG element inside the <body>
      .data([dataSet]) //associate our data with the document
      .attr("width", canvasWidth) //set the width of the canvas
      .attr("height", canvasHeight) //set the height of the canvas
      .append("svg:g") //make a group to hold our pie chart
        .attr("transform", "translate(" + pieCenterX + "," + pieCenterY + ")"); // Set center of pie

// Define an arc generator. This will create <path> elements for using arc data.
  var arc = d3.svg.arc()
      .innerRadius(innerRadius) // Causes center of pie to be hollow
      .outerRadius(outerRadius);

// Define a pie layout: the pie angle encodes the value of dataSet.
// Since our data is in the form of a post-parsed CSV string, the
// values are Strings which we coerce to Numbers.
  var pie = d3.layout.pie()
  .value(function(d) { return d.size; })
  .sort(function(a, b) {if (sortArcs==1) { return b.size - a.size; } else { return null; } });

  // Select all <g> elements with class slice (there aren't any yet)
  var arcs = canvas.selectAll("g.slice")
    // Associate the generated pie data (an array of arcs, each having startAngle,
    // endAngle and value properties) 
    .data(pie)
    // This will create <g> elements for every "extra" data element that should be associated
    // with a selection. The result is creating a <g> for every object in the data array
    // Create a group to hold each slice (we will have a <path> and a <text>      // element associated with each slice)
.enter().append("svg:a")
      .attr("xlink:href", function(d) { return d.data.link; })
      .append("svg:g")
        .attr("class", "slice")    //allow us to style things in the slices (like text)
        // Set the color for each slice to be chosen from the color function defined above
        // This creates the actual SVG path using the associated data (pie) with the arc drawing function
        .style("stroke", "White" )
        .attr("d", arc);

  arcs.append("svg:path")
    // Set the color for each slice to be chosen from the color function defined above
    // This creates the actual SVG path using the associated data (pie) with the arc drawing function
    .attr("fill", function(d, i) { return colorScale(i); } )
    .attr("color_value", function(d, i) { return colorScale(i); }) // Bar fill color...
    .attr("index_value", function(d, i) { return "index-" + i; })
    .attr("class", function(d, i) { return "pie-" + pieName + "-arc-index-" + i; })
    .style("stroke", "#cccccc" )
    .attr("d", arc)
    .on('mouseover', synchronizedMouseOver)
    .on("mouseout", synchronizedMouseOut)
    .transition()
      .ease("bounce")
      .duration(500)
      .delay(function(d, i) { return i * 50; })
      .attrTween("d", tweenPie);

  // Add a magnitude value to the larger arcs, translated to the arc centroid and rotated.
  arcs.filter(function(d) { return d.endAngle - d.startAngle > .2; }).append("svg:text")
    .attr("dy", ".35em")
    .attr("text-anchor", "middle")
    //.attr("transform", function(d) { return "translate(" + arc.centroid(d) + ")rotate(" + angle(d) + ")"; })
    .attr("transform", function(d) { //set the label's origin to the center of the arc
      //we have to make sure to set these before calling arc.centroid
      d.outerRadius = outerRadius; // Set Outer Coordinate
      d.innerRadius = innerRadius; // Set Inner Coordinate
      return "translate(" + arc.centroid(d) + ")rotate(" + angle(d) + ")";
    })
    .style("fill", "White")
    .style("font", "normal 12px Arial")
    .text(function(d) { return d.data.size; });

  // Computes the angle of an arc, converting from radians to degrees.
  function angle(d) {
    var a = (d.startAngle + d.endAngle) * 90 / Math.PI - 90;
    return a > 90 ? a - 180 : a;
  }

  // Plot the bullet circles...
  canvas.selectAll("circle")
    .data(dataSet).enter().append("svg:circle") // Append circle elements
    .attr("cx", pieWidthTotal + legendBulletOffset)
    .attr("cy", function(d, i) { return i*textVerticalSpace - legendVerticalOffset; } )
    .attr("stroke-width", ".5")
    .style("fill", function(d, i) { return colorScale(i); }) // Bullet fill color
    .attr("r", 5)
    .attr("color_value", function(d, i) { return colorScale(i); }) // Bar fill color...
    .attr("index_value", function(d, i) { return "index-" + i; })
    .attr("class", function(d, i) { return "pie-" + pieName + "-legendBullet-index-" + i; })
    .on('mouseover', synchronizedMouseOver)
    .on("mouseout", synchronizedMouseOut);

  // Create hyper linked text at right that acts as label key...
  canvas.selectAll("a.legend_link")
    .data(dataSet) // Instruct to bind dataSet to text elements
    .enter().append("svg:a") // Append legend elements
      .attr("xlink:href", function(d) { return getUserLink(d.user);})
      .append("text")
        .attr("text-anchor", "center")
        .attr("x", pieWidthTotal + legendBulletOffset + legendTextOffset)
        //.attr("y", function(d, i) { return legendOffset + i*20 - 10; })
    //.attr("cy", function(d, i) {    return i*textVerticalSpace - legendVerticalOffset; } )
        .attr("y", function(d, i) { return i*textVerticalSpace - legendVerticalOffset; } )
        .attr("dx", 0)
        .attr("dy", "5px") // Controls padding to place text in alignment with bullets
        .text(function(d) { return d.user + "  ["+d.size+"]";})
        .attr("color_value", function(d, i) { return colorScale(i); }) // Bar fill color...
        .attr("index_value", function(d, i) { return "index-" + i; })
        .attr("class", function(d, i) { return "pie-" + pieName + "-legendText-index-" + i; })
        .style("fill", "Blue")
        .style("font", "normal 1.5em Arial")
        .on('mouseover', synchronizedMouseOver)
        .on("mouseout", synchronizedMouseOut);

}

