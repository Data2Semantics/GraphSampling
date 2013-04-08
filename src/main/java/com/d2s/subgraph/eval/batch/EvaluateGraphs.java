package com.d2s.subgraph.eval.batch;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;
import org.xml.sax.SAXException;
import com.d2s.subgraph.eval.dbpedia.QaldDbpQueries;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.sparql.resultset.ResultSetRewindable;


public class EvaluateGraphs {
	private static String GOLDEN_STANDARD_GRAPH = "http://dbpo.org";
	private File resultsDir;
	public EvaluateGraphs(String resultsDirPath) {
		this.resultsDir = new File(resultsDirPath);
		if (!(resultsDir.exists() && resultsDir.isDirectory())) {
			resultsDir.mkdir();
		}
	}
	
	public void run() throws RepositoryException, MalformedQueryException, QueryEvaluationException, SAXException, IOException, ParserConfigurationException {
//		ArrayList<String> graphs = getGraphsToEvaluate();
		ArrayList<String> graphs = new ArrayList<String>();
		graphs.add("http://dbp_s-o_unweighted_litAsNode_directed_indegree_0.5.nt");
		QaldDbpQueries queries = new QaldDbpQueries(QaldDbpQueries.QALD_2_QUERIES);
		for (String graph: graphs) {
			System.out.println("evaluating for graph " + graph);
			EvaluateGraph eval = new EvaluateGraph(queries, EvaluateGraph.OPS_VIRTUOSO, GOLDEN_STANDARD_GRAPH, graph);
			eval.run();
			Results results = eval.getResults();
			String filename = graph.substring(7);//remove http://
			results.writeAsCsv(resultsDir.getAbsolutePath() + "/" + filename + ".csv", true);
		}
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
			if (graph.startsWith("http://dbp_") && graph.endsWith("0.5.nt")) {
				graphs.add(solution.get("graph").toString());
			}
			
		}
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
