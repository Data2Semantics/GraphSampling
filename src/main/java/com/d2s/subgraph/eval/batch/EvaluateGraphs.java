package com.d2s.subgraph.eval.batch;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import javax.xml.parsers.ParserConfigurationException;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;
import org.xml.sax.SAXException;
import au.com.bytecode.opencsv.CSVWriter;
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
	private ArrayList<Results> allResults = new ArrayList<Results>();
	public EvaluateGraphs(ExperimentSetup experimentSetup) {
		this.experimentSetup = experimentSetup;
		this.resultsDir = new File(experimentSetup.getResultsDir());
		if (!(resultsDir.exists() && resultsDir.isDirectory())) {
			resultsDir.mkdir();
		}
	}
	
	public void run() throws RepositoryException, MalformedQueryException, QueryEvaluationException, SAXException, IOException, ParserConfigurationException {
		ArrayList<String> graphs = getGraphsToEvaluate();
		
		File csvFile = new File(resultsDir.getAbsolutePath() + "/summary.csv");
		CSVWriter writer = new CSVWriter(new FileWriter(csvFile), ';');
		writer.writeNext(new String[]{"graph", "avgRecall"});
		GetQueries queries = experimentSetup.getQueries();
		for (String graph: graphs) {
			System.out.println("evaluating for graph " + graph);
			EvaluateGraph eval = new EvaluateGraph(queries, EvaluateGraph.OPS_VIRTUOSO, experimentSetup.getGoldenStandardGraph(), graph);
			eval.run();
			Results results = eval.getResults();
			allResults.add(results);
			String filename = graph.substring(7);//remove http://
			results.writeAsCsv(resultsDir.getAbsolutePath() + "/" + filename + ".csv", true);
			writer.writeNext(new String[]{graph, Double.toString(results.getAverageRecall())});
		}
		writer.close();
	}
	
	
	
	private ArrayList<String> getGraphsToEvaluate() {
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

	public static void main(String[] args)  {
		try {
			EvaluateGraphs evaluate = new EvaluateGraphs(new DbpExperimentSetup());
			evaluate.run();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
