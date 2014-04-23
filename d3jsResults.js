var margin = {
	top : 20,
	right : 20,
	bottom : 30,
	left : 50
}, width = 900 - margin.left - margin.right, height = 700 - margin.top
		- margin.bottom, plotWidth = width - 300, plotHeight = height - 200;

var color = d3.scale.category20();

var x = d3.scale.linear().range([ 0, plotWidth ]);

var y = d3.scale.linear().range([ plotHeight, 0 ]);




var xAxis = d3.svg.axis().scale(x).orient("bottom");

var yAxis = d3.svg.axis().scale(y).orient("left");

var lineFunction = d3.svg.line().x(function(d) {
	return x(d.SampleSize);
}).y(function(d) {
	return y(d.recall);
});


var voronoi = d3.geom.voronoi()
.x(function(d) { return x(d.SampleSize); })
.y(function(d) { return y(d.recall); })
.clipExtent([[-margin.left, -margin.top], [plotWidth + margin.right, plotHeight + margin.bottom]]);


var svgContainer = d3.select("#d3jsResults").attr("width",
		width + margin.left + margin.right).attr("height",
		height + margin.top + margin.bottom).append("g").attr("transform",
		"translate(" + margin.left + "," + margin.top + ")");
var sampleMethods = null;
var selectedSampleMethods = null;
var legends = null;
var drawLegend = function() {
	/**
	 * add legend
	 */
	if (!legends) {
		legends = svgContainer.append('g').attr('class', 'legend').attr('id', 'legendBox').attr("transform", "translate(35 ,0)");
	}
	
	legends.selectAll('g').remove();
	var legend = legends.selectAll('g').data(sampleMethods.filter(function(dataObj){
		return $(".sampleMethod." + dataObj.name).css("display") == "inline";
	})).enter()
			.append('g').each(function(methodObj) {
				d3.select(this).attr("class",
						"sampleMethodLegend " + methodObj.name);
			});

	legend.append('rect').attr('x', plotWidth - 20).attr('y',
			function(d, i) {
				return i * 20;
			}).attr('width', 10).attr('height', 10)
				.style('fill', function(d) {
				return color(d.name);
			});

	legend.append('text').attr('x', plotWidth - 8).attr('y',
			function(d, i) {
				return (i * 20) + 9;
			}).text(function(d) {
		return d.name.replace(/_/g, " - ");
	});
	
	
	
	var ctx = document.getElementById("legendBox"),
	SVGRect = ctx.getBBox();

	var rect = document.getElementById("whitebackground");
	if (!rect) {
		rect = document.createElementNS("http://www.w3.org/2000/svg", "rect");
		rect.setAttribute("id", "whitebackground");
		ctx.insertBefore(rect, ctx.firstChild);
	}
    rect.setAttribute("x", SVGRect.x);
    rect.setAttribute("y", SVGRect.y);
    rect.setAttribute("width", SVGRect.width);
    rect.setAttribute("height", SVGRect.height);
    rect.setAttribute("fill", "white");
    
};

function updateSampleMethodVisibility() {
	for (var i = 0; i < sampleMethods.length; i++) {
//		if (i < 10) {
//			sampleMethods[i].visibility = Math.random() < 0.5 ;
//		}
	}
}
function drawResults() {
	updateSampleMethodVisibility();
	
	selectedSampleMethods = sampleMethods.filter(function(sampleMethod) {
		return sampleMethod.visibility;
	});
	var sampleMethod = svgContainer.selectAll(".sampleMethod").data(selectedSampleMethods, function(d){return d.name;});
//	console.log(sampleMethod.exit());
	
	
	
	
	sampleMethod.enter().append("g").each(
			function(methodObj) {
				d3.select(this).attr("class",
						"sampleMethod " + methodObj.name);
			}).append("path")
		.attr("class", "line")
		.attr("d",
			function(d) {
				d.line = this;
				return lineFunction(d.values);
			})
		.style("stroke", function(d) {
			return color(d.name);
		});

	sampleMethod.exit().transition().style("opacity", 0).remove();
//	sampleMethod.enter().append("path")
//		.attr("class", "line")
//		.attr("d",
//			function(d) {
//				d.line = this;
//				return lineFunction(d.values);
//			})
//		.style("stroke", function(d) {
//			return color(d.name);
//		}
//	);
	
	var focus = svgContainer.append("g")
      .attr("transform", "translate(-100,-100)")
      .attr("class", "focus");

	  focus.append("circle")
	      .attr("r", 3.5);

	  focus.append("text")
	      .attr("y", -10);
	  
	  var voronoiGroup = svgContainer.append("g")
      .attr("class", "voronoi");
	  
	  var voronoiPaths = voronoiGroup.selectAll("path")
      .data(function() {
    	  var nest = d3.nest().key(function(d) {
	        	  return x(d.SampleSize) + "," + y(d.recall); })
	          .rollup(function(v) { 
	        	  return v[0]; })
	          .entries(d3.merge(selectedSampleMethods.map(function(d) {
	        	  return d.values; 
	        	  })))
	          .map(function(d) { 
	        	  return d.values; });
    	  	return voronoi(nest);
    	  });
	  voronoiPaths.enter().append("path")
      .attr("d", function(d) { 
    	  if (d) return "M" + d.join("L") + "Z"; }
      )
      .datum(function(d) { 
    	  if (d) return d.point; }
      )
      .on("mouseover", mouseover)
      .on("mouseout", mouseout);
	  voronoiPaths.exit().remove();
  
  function mouseover(d) {
  focus.attr("transform", "translate(" + x(d.SampleSize) + "," + y(d.recall) + ")");
  focus.selectAll("text").remove();//remove previous ones
  focus.append("text").attr("transform","translate(0, -10)").text(d.name.replace(/_/g, " - "));
  var amt = parseFloat(d.recall);
  focus.append("text").attr("transform","translate(0, 20)").text(  amt.toFixed(2) + "/" + d.SampleSize);
}

function mouseout(d) {
  d3.select(".sampleMethod." + d.name).classed("city--hover", false);
  focus.attr("transform", "translate(-100,-100)");
}
}

d3.tsv("allResults.tsv", function(error, data) {
	color.domain(d3.keys(data[0]).filter(function(key) {
		return key !== "SampleSize";
	}));

	sampleMethods = color.domain().map(function(name) {
		return {
			name : name,
			visibility: false,
			line: null,
			values : data.map(function(d) {
				return {
					name: name,
					SampleSize : d.SampleSize,
					recall : +d[name]
				};
			})
		};
	});
	x.domain(d3.extent(data, function(d) {
		return d.SampleSize;
	}));
	y.domain([ d3.min(sampleMethods, function(c) {
		return d3.min(c.values, function(v) {
			return v.recall;
		});
	}), d3.max(sampleMethods, function(c) {
		return d3.max(c.values, function(v) {
			return v.recall;
		});
	}) ]);

	// draw x axis
	svgContainer.append("g").attr("class", "x axis").attr("transform",
			"translate(0," + plotHeight + ")").call(xAxis);

	// add x label
	svgContainer.append("text").attr(
			"transform",
			"translate(" + (plotWidth) + " ,"
					+ (plotHeight + margin.bottom - 40) + ")").style(
			"text-anchor", "end").style("font-size", "15px").text("Sample Size");

	// draw y axis
	svgContainer.append("g").attr("class", "y axis").call(yAxis);

	// add y label
	svgContainer.append("text").attr("transform", "rotate(-90)").attr(
			"y", 6)// offset to right
	.attr("dy", ".71em").style("text-anchor", "end").style("font-size", "15px").text("Recall");
	
	
	if (window.location.search.match( /showGrid/gi)) {
		//draw grid
		svgContainer.append("g").attr("class", "grid").attr("transform",
				"translate(0," + plotHeight + ")").call(
						xAxis.tickSize(-plotHeight, 0, 0).tickFormat(""))
	
		svgContainer.append("g").attr("class", "grid").call(
				yAxis.tickSize(-plotWidth, 0, 0).tickFormat(""))
	}
	drawSelectionTable();
});


var drawSelectionTable = function() {
	/**
	 * First, get information of datasets and sampling methods we've just drawn.
	 */
	var getInfoFromClass = function(className){
		var classInfoArray = className.split("_");
		datasets[classInfoArray[0]] = true;
		if (classInfoArray.length == 2) {
			baselines[classInfoArray[1]] = true;
		} else {
			samplingMethods[classInfoArray[1] + " - " + classInfoArray[2]] = true;
		}
	};
	var datasets = {};
	var baselines = {};
	var samplingMethods = {};
	for (var i = 0; i < sampleMethods.length; i++) {
		getInfoFromClass(sampleMethods[i].name);
	}
	datasets = Object.keys(datasets).sort(), baselines = Object.keys(baselines).sort(), samplingMethods = Object.keys(samplingMethods).sort();
	var allSamplingMethods = $.merge(baselines, samplingMethods);
	/**
	 * Now, draw the actual table
	 */
	
	var table = $("table");
	/**
	 * draw header
	 */
	var header = $("<tr></tr>");
	header.append("<th colspan=2>Sampling Methods</th>");
	for (var i = 0; i < datasets.length; i++) {
		header.append("<th class='rotate'><span class='intact'>" + datasets[i] + "</span></th>");
	}
	table.append($("<thead></thead>").append(header));
	
	
	/**
	 * draw body
	 */
	var body = $("<tbody></tbody>");
	
	//create 'select whole col' checkboxes
	var row = $("<tr class='selectWholeCol'></tr>").append("<td>&nbsp;</td><td>&nbsp;</td>");
	for (var colId = 0; colId < datasets.length; colId++) {
		row.append("<td><input type='checkbox'></td>");
	}
	body.append(row);
	for (var rowId = 0; rowId < allSamplingMethods.length; rowId++) {
		var row = $("<tr ></tr>");
		row.append("<td>" + allSamplingMethods[rowId] + "</td>");
		row.append("<td class='selectWholeRow'><input type='checkbox'></td>");
		for (var colId = 0; colId < datasets.length; colId++) {
			row.append("<td><input type='checkbox'></td>");
		}
		body.append(row);
	}
	
	table.append(body);
	table.delegate( "input", "change", onCheckboxChanged);
//	$("div").append(table);
	
	//init table with default vals
	var setEnabled = function(dataset, samplingMethod) {
		
		var colHeader = table.find("th:has(span:contains('" + dataset + "'))");
		var colNum = table.find("thead tr").children().index(colHeader) + 2;
		var rowHeader = table.find("tr td:contains('" + samplingMethod + "')");
		rowHeader.parent().find("td:nth-child(" + (colNum) + ") input").each(function(){
			$(this).prop("checked", true);
			onCheckboxChanged($(this), true);
		});
	};
	
	
	var getEnabledPlots = function() {
		  var params = location.search.substr(location.search.indexOf("?")+1);
		  var plots = [];
		  params = params.split("&");
		    // split param and value into individual pieces
		    for (var i=0; i<params.length; i++)
		       {
		         temp = params[i].split("=");
		         if (temp[0] == "enabled") {
		        	var value = temp[1];
		        	var sampleMethod = decodeURIComponent(value.substring(value.indexOf('-')+1));
		        	var dataset = value.substring(0, value.indexOf('-'));
		        	if (sampleMethod.length > 0 && dataset.length > 0) plots.push({dataset: dataset, sampleMethod:sampleMethod});
		         }
//		         if ( [temp[0]] == sname ) { sval = temp[1]; }
		       }
		  return plots;
	}
	var enabledPlots = getEnabledPlots();
	console.log(enabledPlots);
	if (enabledPlots.length == 0) {
		enabledPlots = [
			{dataset:"SemanticWebDogFood",sampleMethod: "Path - Pagerank"},
			{dataset:"DBpedia",sampleMethod: "Path - Pagerank"},
			{dataset:"OpenBioMed",sampleMethod: "WithoutLiterals - Outdegree"},
			{dataset:"BIO2RDF",sampleMethod:  "UniqueLiterals - Outdegree"},
			{dataset:"LinkedGeoData",sampleMethod:  "UniqueLiterals - Outdegree"},
			{dataset:"Metalex",sampleMethod: "ResourceFrequency"}	
		];
	}
	for (var i = 0; i < enabledPlots.length; i++) {
		setEnabled(enabledPlots[i].dataset, enabledPlots[i].sampleMethod);
	}
	drawResults();
	drawLegend();
};

var onCheckboxChanged = function(checkBox, skipDrawingResults) {
	var getDataset = function() {
		var tdElement = checkBox.parent();
		var col = tdElement.parent().children().index(tdElement);
		var title = checkBox.closest("table").find("th").eq(col - 1).text();
		return title;
	};
	var getSamplingMethod = function() {
		return checkBox.closest("tr").find("td:first").text();
	};
	if (checkBox.currentTarget) {
		checkBox = $(checkBox.currentTarget);
	} else {
		checkBox = $(checkBox);
	}
	var doCheck = checkBox.is(':checked');
	var dataset = getDataset();
	var samplingMethod = getSamplingMethod();
	
	if (checkBox.closest("tr").attr("class") == "selectWholeCol") {
		var tdElement = checkBox.parent();
		var col = tdElement.parent().children().index(tdElement);
		checkBox.closest("table").find('tr td:nth-child(' + (col+1) + ') input').not(checkBox).each(function(){
			$(this).prop("checked",doCheck);
			onCheckboxChanged($(this), true);
			
		});
		drawResults();
		drawLegend();
		
	} else if (checkBox.parent().attr("class") == "selectWholeRow") {
		checkBox.parent().parent().find("input").not(checkBox).each(function(){
			$(this).prop("checked",doCheck);
			onCheckboxChanged($(this), true);
		});
		drawResults();
		drawLegend();
	} else {
		var className = dataset + "_" + samplingMethod.replace(" - ", "_");
		for (var i = 0; i < sampleMethods.length; i++) {
			if (sampleMethods[i].name == className) {
				sampleMethods[i].visibility = doCheck;
			}
		} 
		if (!skipDrawingResults) {
			drawResults();
			drawLegend();
		}
		
		
		if (!doCheck) {
			//we've just unchecked this thing. make sure the 'selectAll' of this col and row is now unchecked as well
			var tdElement = checkBox.parent();
			var col = tdElement.parent().children().index(tdElement);
			//uncheck row
			tdElement.parent().find("td.selectWholeRow input").prop("checked",false);
			//uncheck col
			checkBox.closest("table").find('tr.selectWholeCol td:nth-child(' + (col+1) + ') input').prop("checked",doCheck);
		}
	}
};

$( document ).ready(function() {
	if (window.location.search.match( /hidetable/gi)) {
		$("table").hide();
		$("body").css("overflow", "hidden");
		$("svg").css("margin-top", 0);
	}
});
