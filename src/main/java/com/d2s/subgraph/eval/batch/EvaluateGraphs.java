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
import com.d2s.subgraph.eval.results.GraphResults;
import com.d2s.subgraph.eval.results.GraphResultsSample;
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
	private ArrayList<String> sampleGraphs;
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
		getGraphsToEvaluateViaSsh();
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
			results.writeAsCsv(resultsDir.getAbsolutePath() + "/" + filename + ".csv");
		}
		if (sampleGraphs.size() > 0) {
			System.out.println("evaluating for " + sampleGraphs.size() + " samplegraphs");
			GraphResultsSample sampleGraphResultsCombined = new GraphResultsSample();
			ArrayList<GraphResults> individualSampleResults = new ArrayList<GraphResults>();
			for (String sampleGraph: sampleGraphs) {
				EvaluateGraph eval = new EvaluateGraph(queries, EvaluateGraph.OPS_VIRTUOSO, experimentSetup.getGoldenStandardGraph(), sampleGraph);
				eval.run();
				individualSampleResults.add(eval.getResults());
			}
			sampleGraphResultsCombined.add(individualSampleResults);
		}
		batchResults.writeOutput();
	}
	
	
	
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
		graphs = new ArrayList<String>();
		String line;
		while ((line = in.readLine()) != null) {
			line = line.trim();
			if (line.startsWith("http://" + experimentSetup.getGraphPrefix())) {
				if (line.contains("sample")) {
					sampleGraphs.add(line);
				} else {
					graphs.add(line);
				}
			}
		}
		pr.waitFor();
		in.close();
		
		if (graphs.size() == 0) {
			System.out.println("No graphs retrieved from OPS via SSH. Maybe virtuoso down?");
			System.exit(1);
		}
		Collections.sort(sampleGraphs);
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
