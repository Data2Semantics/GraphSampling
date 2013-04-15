package com.d2s.subgraph.eval.batch;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import javax.xml.parsers.ParserConfigurationException;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;
import org.xml.sax.SAXException;
import com.d2s.subgraph.eval.results.BatchResults;
import com.d2s.subgraph.eval.results.GraphResults;
import com.d2s.subgraph.queries.GetQueries;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.sparql.resultset.ResultSetRewindable;


public class EvaluateGraphs {
	private File resultsDir;
	private ExperimentSetup experimentSetup;
	private BatchResults batchResults;
	private GetQueries queries;
	private ArrayList<String> graphs;
	public EvaluateGraphs(ExperimentSetup experimentSetup) {
		queries = experimentSetup.getQueries();
		batchResults = new BatchResults(experimentSetup, queries);
		this.experimentSetup = experimentSetup;
		this.resultsDir = new File(experimentSetup.getResultsDir());
		if (!(resultsDir.exists() && resultsDir.isDirectory())) {
			resultsDir.mkdir();
		}
	}
	
	public void run() throws RepositoryException, MalformedQueryException, QueryEvaluationException, SAXException, IOException, ParserConfigurationException, InterruptedException {
//		ArrayList<String> graphs = getGraphsToEvaluateViaSparql();
		graphs = getGraphsToEvaluateViaSsh();
//		graphs = new ArrayList<String>();
//		graphs.add("http://df_s-o-litAsNode_unweighted_directed_indegree_0.2.nt");
//		graphs.add("http://df_s-o-litAsNode_unweighted_directed_outdegree_0.2.nt");
		System.out.println("Running evaluation for graphs " + graphs.toString());
		for (String graph: graphs) {
			System.out.println("evaluating for graph " + graph);
			EvaluateGraph eval = new EvaluateGraph(queries, EvaluateGraph.OPS_VIRTUOSO, experimentSetup.getGoldenStandardGraph(), graph);
			eval.run();
			GraphResults results = eval.getResults();
			batchResults.add(results);
			String filename = graph.substring(7);//remove http://
			results.writeAsCsv(resultsDir.getAbsolutePath() + "/" + filename + ".csv", true);
		}
		batchResults.writeSummaryCsv();
		batchResults.outputAsHtmlTable();
	}
	
	
	
//	private void outputAsHtmlTable() throws IOException {
//		String encodedEndpoint = URLEncoder.encode(EvaluateGraph.OPS_VIRTUOSO, "UTF-8"); 
//		String html = "<html><head>\n" +
//				"<link rel='stylesheet' href='../static/style.css' type='text/css' />\n" +
//				"<script src='http://ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.min.js'></script>\n" +
//				"<script src='../static/tablesorter/jquery.tablesorter.min.js'></script>\n" +
//				"<script src='../static/script.js'></script>\n" +
//				"<script src='../static/jquery.stickytableheaders.js'></script>\n" +
//				"</head>" +
//				"\n<body>" +
//				"\n<table id='myTable' class='tablesorter'>\n";
//		ArrayList<ArrayList<String>> table = new ArrayList<ArrayList<String>>();
//		
//		
//		//fill first col of table (avg for this query)
//		for (QueryWrapper query: queries.getQueries()) {
//			ArrayList<String> row = new ArrayList<String>();
//			double totalRecall = 0.0;// totalrecall!
//			for (GraphResults results: allResults) {
//				int queryId = query.getQueryId();
//				QueryResults queryResults = results.get(queryId);
//				totalRecall += queryResults.getRecall();
//			}
//			
//			double avgRecall = totalRecall / (double)allResults.size();
//			System.out.println("total recall: " + totalRecall + " size: " + allResults.size() + " avg: " + avgRecall);
//			row.add("<td>" + Helper.getDoubleAsFormattedString(avgRecall) + "</td>");
//			table.add(row);
//		}
//		
//		
//		html += "<thead>\n<tr>";
//		html += "<th>avg</th>";
//		
//		for (int colIndex = 0; colIndex < allResults.size(); colIndex++) {
//			GraphResults graphResults = allResults.get(colIndex);
//			html += "\n<th>" + graphResults.getGraphName().substring("http://".length()).replace('_', '-') + "<br>(avg: " + Helper.getDoubleAsFormattedString(graphResults.getAverageRecall()) + ")</th>";
//			ArrayList<QueryResults> queryResultsArrayList = graphResults.getAsArrayList();
//			
//			for (int rowIndex = 0; rowIndex < queryResultsArrayList.size(); rowIndex++) {
//				QueryResults queryResults = queryResultsArrayList.get(rowIndex);
//				ArrayList<String> row;
//				if (rowIndex < table.size()) {
//					row = table.get(rowIndex);
//				} else {
//					row = new ArrayList<String>();
//					table.add(row);
//				}
//				String encodedQuery = URLEncoder.encode(queryResults.getQuery().getQueryString(graphResults.getGraphName()), "UTF-8");
//				String url = "http://yasgui.laurensrietveld.nl?endpoint=" + encodedEndpoint + "&query=" + encodedQuery + "&tabTitle=" + queryResults.getQuery().getQueryId();
//				String title = StringEscapeUtils.escapeHtml(queryResults.getQuery().getQueryString(graphResults.getGraphName()));
//				String cell = "<td title='" + title + "'><a href='" + url + "' target='_blank'>" + Helper.getDoubleAsFormattedString(queryResults.getRecall()) + "</a></td>";
//				row.add(cell);
//			}
//		}
//		
//		html += "</tr></thead><tbody>\n";
//		for (ArrayList<String> row: table) {
//			html += "\n<tr>";
//			for (String cell: row) {
//				html += cell;
//			}
//			html += "</tr>";
//		}
//		html += "\n</tbody> </table></body></html>";
//		FileUtils.writeStringToFile(new File(experimentSetup.getResultsDir() + "/results.html"), html);
//	}
	
	
	
	
	
	@SuppressWarnings("unused")
	private ArrayList<String> getGraphsToEvaluateViaSparql() {
		ArrayList<String> graphs = new ArrayList<String>();
		System.out.println("retrieving graphs");
		String queryString = "SELECT DISTINCT ?graph\n" + 
				"WHERE {\n" + 
				"  GRAPH ?graph {\n" + 
				"    ?s ?p ?o\n" + 
				"FILTER (regex(str(?graph),'http://" + experimentSetup.getGraphPrefix()  + ".*','i'))" +
				"  }\n" + 
				"}";
		System.out.println(queryString);
		Query query = QueryFactory.create(queryString);
		QueryExecution queryExecution = QueryExecutionFactory.sparqlService(EvaluateGraph.OPS_VIRTUOSO, query);
		ResultSetRewindable queryResults =  ResultSetFactory.copyResults(queryExecution.execSelect());
		while (queryResults.hasNext()) {
			QuerySolution solution = queryResults.next();
			String graph = solution.get("graph").toString();
			if (graph.startsWith("http://" + experimentSetup.getGraphPrefix()) && (false
					|| graph.endsWith("0.2.nt") 
					|| graph.endsWith("0.5.nt")
					)) {
				graphs.add(solution.get("graph").toString());
			}
		}
		Collections.sort(graphs); //pfff, so adding an 'order by' to the query contains duplicates (even with the 'DISTINCT' keyword being used). just order manually 
		System.out.println(graphs.size() + " graphs retrieved");
		return graphs;
	}
	
	private ArrayList<String> getGraphsToEvaluateViaSsh() throws IOException, InterruptedException {
		final String[] args = { "ssh", "ops.few.vu.nl", "subgraphSelection/bin/virtuoso/listGraphs.sh" };

		ProcessBuilder ps = new ProcessBuilder(args);
		ps.redirectErrorStream(true);
		Process pr = ps.start();
		BufferedReader in = new BufferedReader(new
		InputStreamReader(pr.getInputStream()));
		ArrayList<String> graphs = new ArrayList<String>();
		String line;
		while ((line = in.readLine()) != null) {
			line = line.trim();
			if (line.startsWith("http://" + experimentSetup.getGraphPrefix())) {
				graphs.add(line);
			}
		}
		pr.waitFor();
		in.close();
		
		if (graphs.size() == 0) {
			System.out.println("No graphs retrieved from OPS via SSH. Maybe virtuoso down?");
			System.exit(1);
		}
			
		Collections.sort(graphs);
		return graphs;
	}

	public static void main(String[] args)  {
		try {
//			EvaluateGraphs evaluate = new EvaluateGraphs(new DbpExperimentSetup());
			EvaluateGraphs evaluate = new EvaluateGraphs(new SwdfExperimentSetup());
			evaluate.run();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
