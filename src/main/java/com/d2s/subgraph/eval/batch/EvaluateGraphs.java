package com.d2s.subgraph.eval.batch;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;
import org.xml.sax.SAXException;
import com.d2s.subgraph.eval.results.BatchResults;
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
	private ArrayList<String> graphs = new ArrayList<String>();;
	private HashMap<String,ArrayList<String>> sampleGraphs = new HashMap<String,ArrayList<String>>();
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
			results.writeAsCsv(resultsDir.getAbsolutePath());
		}
		if (sampleGraphs.size() > 0) {
			for (Map.Entry<String, ArrayList<String>> entry : sampleGraphs.entrySet()) {
				System.out.println("evaluating for " + sampleGraphs.size() + " samplegraphs (" + entry.getKey() + ")");
				GraphResultsSample sampleGraphResultsCombined = new GraphResultsSample();
				ArrayList<GraphResults> sampleGraphResults = new ArrayList<GraphResults>();
				for (String sampleGraph: entry.getValue()) {
					EvaluateGraph eval = new EvaluateGraph(queries, EvaluateGraph.OPS_VIRTUOSO, experimentSetup.getGoldenStandardGraph(), sampleGraph);
					eval.run();
					sampleGraphResults.add(eval.getResults());
				}
				sampleGraphResultsCombined.add(sampleGraphResults);
				sampleGraphResultsCombined.writeAsCsv(resultsDir.getAbsolutePath());
				batchResults.add(sampleGraphResultsCombined);
			}
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
		String line;
		while ((line = in.readLine()) != null) {
			line = line.trim();
			if (line.startsWith("http://" + experimentSetup.getGraphPrefix())) {
				if (line.contains("sample")) {
					if (line.contains("0.2")) {
						if (!sampleGraphs.containsKey("0.2")) {
							sampleGraphs.put("0.2", new ArrayList<String>());
						}
						sampleGraphs.get("0.2").add(line);
					} else if (line.contains("0.5")) {
						if (!sampleGraphs.containsKey("0.5")) {
							sampleGraphs.put("0.5", new ArrayList<String>());
						}
						sampleGraphs.get("0.5").add(line);
					} else {
						System.out.println("couldnt detect mode to run in for sample graph " + line);
						System.exit(1);
					}
					
				} else {
					graphs.add(line);
				}
			}
		}
		pr.waitFor();
		in.close();
		
		if (graphs.size() == 0 && sampleGraphs.size() == 0) {
			System.out.println("No graphs retrieved from OPS via SSH. Maybe virtuoso down?");
			System.exit(1);
		}
		for (ArrayList<String> sampleGraphsArray: sampleGraphs.values()) {
			Collections.sort(sampleGraphsArray);
		}
		Collections.sort(graphs);
		return graphs;
	}

	public static void main(String[] args)  {
		try {
			EvaluateGraphs evaluate = new EvaluateGraphs(new DbpExperimentSetup());
//			EvaluateGraphs evaluate = new EvaluateGraphs(new SwdfExperimentSetup());
			evaluate.run();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
