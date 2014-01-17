package com.d2s.subgraph.eval.analysis;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.data2semantics.query.QueryCollection;

import com.d2s.subgraph.eval.Config;
import com.d2s.subgraph.eval.experiments.ExperimentSetup;
import com.d2s.subgraph.eval.results.QueryResults;
import com.d2s.subgraph.eval.results.SampleResults;
import com.d2s.subgraph.queries.Query;
import com.d2s.subgraph.util.StringUtils;

public class OutputHtml extends OutputWrapper{
	public OutputHtml(ExperimentSetup experimentSetup, ArrayList<SampleResults> allGraphResults, QueryCollection<Query> queryCollection, File resultsDir) {
		super(experimentSetup, allGraphResults, queryCollection, resultsDir);
	}
	/**
	 * html table with graph evalutations as columns, and queries per row
	 * @throws IOException
	 */
	public void asHtmlTable() throws IOException {
//		System.out.println("writing html files for "  + onlyGraphsContaining);
		String encodedEndpoint = URLEncoder.encode(Config.EXPERIMENT_ENDPOINT, "UTF-8"); 
		String html = "<html><head>\n" +
				"<link rel='stylesheet' href='http://www.few.vu.nl/~lrietveld/static/style.css' type='text/css' />\n" +
				"<script src='http://ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.min.js'></script>\n" +
				"<script src='http://www.few.vu.nl/~lrietveld/static/tablesorter/jquery.tablesorter.min.js'></script>\n" +
				"<script src='http://www.few.vu.nl/~lrietveld/static/script.js'></script>\n" +
				"<script src='http://www.few.vu.nl/~lrietveld/static/jquery.stickytableheaders.js'></script>\n" +
				"</head>" +
				"\n<body>" +
				"\n<table id='myTable' class='tablesorter'>\n";
		HashMap<Integer, ArrayList<String>> table = new HashMap<Integer, ArrayList<String>>();
		
		
		//fill first two col of table (queryId and avg for this query)
		for (Query query: allGraphResults.get(0).getQueryCollection().getQueries()) {
			int queryId = query.getQueryId();
			
			ArrayList<String> row = new ArrayList<String>();
			double totalRecall = 0.0;// totalrecall!
			int numGraphs = 0;
			for (SampleResults results: allGraphResults) {
				if (results.getQueryCollection().getQuery(query.toString()) != null) {
//					if (onlyGraphsContaining.length() == 0 || results.getGraphName().contains(onlyGraphsContaining)) {
//					if (StringUtils.partialStringMatch(results.getGraphName(), onlyGraphsContaining)) {
						QueryResults queryResults = results.getQueryCollection().getQuery(query.toString()).getResults();
						totalRecall += queryResults.getRecall();
						numGraphs++;
//					}
				}
			}
			
			double avgRecall = totalRecall / (double)numGraphs;
			int goldenStandardSize = 0;
			String url = "#";
//			if (batchResults.get(0).contains(queryId)) {
//				Query queryObj = batchResults.get(0).get(queryId).getQuery();
				goldenStandardSize = query.getResults().getGoldenStandardSize();
				String encodedQuery = URLEncoder.encode(query.getQueryWithFromClause(experimentSetup.getGoldenStandardGraph()).toString(), "UTF-8");
				url = "http://yasgui.laurensrietveld.nl?endpoint=" + encodedEndpoint + "&query=" + encodedQuery + "&tabTitle=" + query.getQueryId();
//			}
			row.add("<td>" + queryId + "</td>");
			if (experimentSetup.privateQueries()) {
				row.add("<td>" + StringUtils.getDoubleAsFormattedString(avgRecall) + " (n:" + goldenStandardSize + ")</td>");
			} else {
				row.add("<td><a href='" + url + "' target='_blank'>" + StringUtils.getDoubleAsFormattedString(avgRecall) + " (n:" + goldenStandardSize + ")</a></td>");
			}
			
			row.add("<td>" + query.getNumberOfNonOptionalTriplePatterns() + "</td>");//non optional triple patterns
			row.add("<td>" + query.triplePatternCountCcv + "</td>");
			row.add("<td>" + query.triplePatternCountCvv + "</td>");
			row.add("<td>" + query.triplePatternCountVcc + "</td>");
			table.put(queryId, row);
		}
		
		
		html += "<thead>\n<tr>";
		html += "<th>queryId</th><th>avg</th><th>#tp's<br>(non opt)<br><th>#ccv<br></th><th>#cvv<br></th><th>#vcc<br></th>";
		
		for (SampleResults graphResults: allGraphResults) {
//			if (StringUtils.partialStringMatch(graphResults.getGraphName(), onlyGraphsContaining)) {
				html += "\n<th>" + graphResults.getGraphName().substring("http://".length()).replace('_', '-') + "<br>(avg: " + StringUtils.getDoubleAsFormattedString(graphResults.getAverageRecall()) + ")</th>";
//				for (Query query: queryCollection.getQueries()) {
				for (Query query: graphResults.getQueryCollection().getQueries()) {
					ArrayList<String> row = table.get(query.getQueryId());
					if (query.getResults() != null) {
						QueryResults queryResults = query.getResults();
						String queryString = query.getQueryWithFromClause(graphResults.getGraphName()).toString();
						String encodedQuery = URLEncoder.encode(queryString, "UTF-8");
						String url = "http://yasgui.laurensrietveld.nl?endpoint=" + encodedEndpoint + "&query=" + encodedQuery + "&tabTitle=" + query.getQueryId();
						String title = StringEscapeUtils.escapeHtml(queryString);
						String cell = "<td title='" + title + "'><a href='" + url + "' target='_blank'>" + StringUtils.getDoubleAsFormattedString(queryResults.getRecall()) + "</a></td>";
						if (experimentSetup.privateQueries()) {
							row.add("<td>" + StringUtils.getDoubleAsFormattedString(queryResults.getRecall()) + "</td>");
						} else {
							row.add(cell);
						}
					} else {
						row.add("<td>N/A</td>");
					}
				}
//			}
		}
		
		html += "</tr></thead><tbody>\n";
		for (ArrayList<String> row: table.values()) {
			html += "\n<tr>";
			for (String cell: row) {
				html += cell;
			}
			html += "</tr>";
		}
		html += "\n</tbody> </table></body></html>";
		FileUtils.writeStringToFile(new File(Config.PATH_EVALUATION_OUTPUT + experimentSetup.getId() + "/" + Config.FILE_HTML_SUMMARY), html);
	}
}
