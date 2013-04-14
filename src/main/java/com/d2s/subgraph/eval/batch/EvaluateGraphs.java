package com.d2s.subgraph.eval.batch;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import javax.xml.parsers.ParserConfigurationException;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;
import org.xml.sax.SAXException;

import au.com.bytecode.opencsv.CSVWriter;

import com.d2s.subgraph.helpers.Helper;
import com.d2s.subgraph.queries.GetQueries;
import com.d2s.subgraph.queries.QaldDbpQueries;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.reasoner.rulesys.builtins.Max;
import com.hp.hpl.jena.sparql.resultset.ResultSetRewindable;


public class EvaluateGraphs {
	private static String GOLDEN_STANDARD_GRAPH = "http://dbpo";
	private File resultsDir;
	private ArrayList<Results> allResults = new ArrayList<Results>();
	public EvaluateGraphs(String resultsDirPath) {
		this.resultsDir = new File(resultsDirPath);
		if (!(resultsDir.exists() && resultsDir.isDirectory())) {
			resultsDir.mkdir();
		}
	}
	
	public void run() throws RepositoryException, MalformedQueryException, QueryEvaluationException, SAXException, IOException, ParserConfigurationException {
		ArrayList<String> graphs = getGraphsToEvaluate();
//		ArrayList<String> graphs = new ArrayList<String>();
//		graphs.add("http://dbp_s-o_unweighted_noLit_directed_outdegree_0.5.nt");
		
		File csvFile = new File(resultsDir.getAbsolutePath() + "/summary.csv");
		CSVWriter writer = new CSVWriter(new FileWriter(csvFile), ';');
		writer.writeNext(new String[]{"graph", "avgRecall"});
		QaldDbpQueries queries = new QaldDbpQueries(QaldDbpQueries.QALD_2_QUERIES);
		for (String graph: graphs) {
			System.out.println("evaluating for graph " + graph);
			EvaluateGraph eval = new EvaluateGraph(queries, EvaluateGraph.OPS_VIRTUOSO, GOLDEN_STANDARD_GRAPH, graph);
			eval.run();
			Results results = eval.getResults();
			allResults.add(results);
			String filename = graph.substring(7);//remove http://
			results.writeAsCsv(resultsDir.getAbsolutePath() + "/" + filename + ".csv", true);
			writer.writeNext(new String[]{graph, Double.toString(results.getAverageRecall())});
		}
		writer.close();
		writeAllResults();
	}
	
	private void writeAllResults() throws IOException {
		File csvFile = new File(resultsDir.getAbsolutePath() + "/allRecallResults.csv");
		CSVWriter writer = new CSVWriter(new FileWriter(csvFile), ';');

		ArrayList<Integer> queryIds = getQueryIds();
		ArrayList<String> header = Helper.getIntAsString(queryIds);
		header.add(0, "subgraph");//prepend col header
		writer.writeNext(header.toArray(new String[header.size()]));//header
		
		for (Results results: allResults) {
			ArrayList<String> row = new ArrayList<String>();
			row.add(results.getGraphName());
			for (int queryId: queryIds) {
				if (results.queryIdExists(queryId)) {
					Result result = results.get(queryId);
					System.out.println(result.getRecall());
					row.add(Double.toString(result.getRecall()));
				} else {
					row.add("");
				}
			}
			writer.writeNext(row.toArray(new String[row.size()]));//header
//			HashMap<Integer, Result> hashmapResults = results.getAsHashMap();
//			if (hashmapResults.containsKey(i)) {
//				foundResultForQueryId = true;
//				recalls.add(Double.toString(hashmapResults.get(i).getRecall()));
//			} else {
//				recalls.add("");
//			}
		}
		
		writer.close();
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private ArrayList<Integer> getQueryIds() {
		ArrayList<Integer> allQueryIds = new ArrayList<Integer>();
		
		for (Results results: allResults) {
			ArrayList<Integer> queryIds = results.getQueryIds();
			allQueryIds.addAll(queryIds);
		}
		
		//get unique
		HashSet hs = new HashSet();
		hs.addAll(allQueryIds);
		allQueryIds.clear();
		allQueryIds.addAll(hs);
		
		Collections.sort(allQueryIds);
		System.out.println(allQueryIds);
		return allQueryIds;
	}
	
	private ArrayList<String> getGraphsToEvaluate() {
		ArrayList<String> graphs = new ArrayList<String>();
		System.out.println("retrieving graphs");
		String queryString = "SELECT DISTINCT ?graph\n" + 
				"WHERE {\n" + 
				"  GRAPH ?graph {\n" + 
				"    ?s ?p ?o\n" + 
				"  }\n" + 
				"}";
		Query query = QueryFactory.create(queryString);
		QueryExecution queryExecution = QueryExecutionFactory.sparqlService(EvaluateGraph.OPS_VIRTUOSO, query);
		ResultSetRewindable queryResults =  ResultSetFactory.copyResults(queryExecution.execSelect());
		while (queryResults.hasNext()) {
			QuerySolution solution = queryResults.next();
			String graph = solution.get("graph").toString();
			if (graph.startsWith("http://dbp_") && (false
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

	public static void main(String[] args)  {
		try {
			EvaluateGraphs evaluate = new EvaluateGraphs("results");
			evaluate.run();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
