package com.d2s.subgraph.eval.results;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringEscapeUtils;
import au.com.bytecode.opencsv.CSVWriter;
import com.d2s.subgraph.eval.QueryWrapper;
import com.d2s.subgraph.eval.batch.EvaluateGraph;
import com.d2s.subgraph.eval.batch.ExperimentSetup;
import com.d2s.subgraph.eval.results.GraphResults;
import com.d2s.subgraph.eval.results.QueryResults;
import com.d2s.subgraph.helpers.Helper;
import com.d2s.subgraph.queries.GetQueries;

public class BatchResults {
	private File resultsDir;
	private ArrayList<GraphResults> batchResults = new ArrayList<GraphResults>();
	private GetQueries queries;
	private ExperimentSetup experimentSetup;
	
	public BatchResults(ExperimentSetup experimentSetup, GetQueries queries) {
		this.experimentSetup = experimentSetup;
		this.queries = queries;
		this.resultsDir = new File(experimentSetup.getResultsDir());
		if (!(resultsDir.exists() && resultsDir.isDirectory())) {
			resultsDir.mkdir();
		}
	}
	
	public void add(GraphResults graphResults) {
		this.batchResults.add(graphResults);
	}
	
	public void writeSummaryCsv() throws IOException {
		File csvFile = new File(resultsDir.getAbsolutePath() + "/summary.csv");
		CSVWriter writer = new CSVWriter(new FileWriter(csvFile), ';');
		writer.writeNext(new String[]{"graph", "avgRecall"});
		for (GraphResults graphResults: batchResults) {
			writer.writeNext(new String[]{graphResults.getGraphName(), Double.toString(graphResults.getAverageRecall())});
		}
		writer.close();
	}
	
	
	
	public void outputAsHtmlTable() throws IOException {
		String encodedEndpoint = URLEncoder.encode(EvaluateGraph.OPS_VIRTUOSO, "UTF-8"); 
		String html = "<html><head>\n" +
				"<link rel='stylesheet' href='../static/style.css' type='text/css' />\n" +
				"<script src='http://ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.min.js'></script>\n" +
				"<script src='../static/tablesorter/jquery.tablesorter.min.js'></script>\n" +
				"<script src='../static/script.js'></script>\n" +
				"<script src='../static/jquery.stickytableheaders.js'></script>\n" +
				"</head>" +
				"\n<body>" +
				"\n<table id='myTable' class='tablesorter'>\n";
		ArrayList<ArrayList<String>> table = new ArrayList<ArrayList<String>>();
		
		
		//fill first col of table (avg for this query)
		for (QueryWrapper query: queries.getQueries()) {
			ArrayList<String> row = new ArrayList<String>();
			double totalRecall = 0.0;// totalrecall!
			for (GraphResults results: batchResults) {
				int queryId = query.getQueryId();
				QueryResults queryResults = results.get(queryId);
				totalRecall += queryResults.getRecall();
			}
			
			double avgRecall = totalRecall / (double)batchResults.size();
			System.out.println("total recall: " + totalRecall + " size: " + batchResults.size() + " avg: " + avgRecall);
			row.add("<td>" + Helper.getDoubleAsFormattedString(avgRecall) + "</td>");
			table.add(row);
		}
		
		
		html += "<thead>\n<tr>";
		html += "<th>avg</th>";
		
		for (int colIndex = 0; colIndex < batchResults.size(); colIndex++) {
			GraphResults graphResults = batchResults.get(colIndex);
			html += "\n<th>" + graphResults.getGraphName().substring("http://".length()).replace('_', '-') + "<br>(avg: " + Helper.getDoubleAsFormattedString(graphResults.getAverageRecall()) + ")</th>";
			ArrayList<QueryResults> queryResultsArrayList = graphResults.getAsArrayList();
			
			for (int rowIndex = 0; rowIndex < queryResultsArrayList.size(); rowIndex++) {
				QueryResults queryResults = queryResultsArrayList.get(rowIndex);
				ArrayList<String> row;
				if (rowIndex < table.size()) {
					row = table.get(rowIndex);
				} else {
					row = new ArrayList<String>();
					table.add(row);
				}
				String encodedQuery = URLEncoder.encode(queryResults.getQuery().getQueryString(graphResults.getGraphName()), "UTF-8");
				String url = "http://yasgui.laurensrietveld.nl?endpoint=" + encodedEndpoint + "&query=" + encodedQuery + "&tabTitle=" + queryResults.getQuery().getQueryId();
				String title = StringEscapeUtils.escapeHtml(queryResults.getQuery().getQueryString(graphResults.getGraphName()));
				String cell = "<td title='" + title + "'><a href='" + url + "' target='_blank'>" + Helper.getDoubleAsFormattedString(queryResults.getRecall()) + "</a></td>";
				row.add(cell);
			}
		}
		
		html += "</tr></thead><tbody>\n";
		for (ArrayList<String> row: table) {
			html += "\n<tr>";
			for (String cell: row) {
				html += cell;
			}
			html += "</tr>";
		}
		html += "\n</tbody> </table></body></html>";
		FileUtils.writeStringToFile(new File(experimentSetup.getResultsDir() + "/results.html"), html);
	}
	
	
	

	public static void main(String[] args)  {
		
	}

}
