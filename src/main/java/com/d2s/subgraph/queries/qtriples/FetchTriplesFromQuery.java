package com.d2s.subgraph.queries.qtriples;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import com.d2s.subgraph.eval.Config;
import com.d2s.subgraph.eval.experiments.ExperimentSetup;
import com.d2s.subgraph.eval.experiments.SwdfExperimentSetup;
import com.d2s.subgraph.queries.Query;
import com.d2s.subgraph.util.Utils;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

public class FetchTriplesFromQuery {
	
	private ExperimentSetup experimentSetup;
	private Query originalQuery;
	private Query rewrittenQuery;
	private File experimentDir;
	private File queryOutputDir;
	
	//swdf
	//	query-0
	//		solution-0

	public FetchTriplesFromQuery(ExperimentSetup experimentSetup, Query query, File experimentDir) throws IOException {
		this.originalQuery = query;
		this.experimentSetup = experimentSetup;
		this.experimentDir = Utils.mkdir(experimentDir);
		setupDirStructure();
	}
	
	
	private void process() throws IOException {
		//rewrite to * query
		rewrittenQuery = originalQuery.getQueryForTripleRetrieval();
		
		//make sure the query targets the proper graph
		rewrittenQuery = rewrittenQuery.getQueryWithFromClause(experimentSetup.getGoldenStandardGraph());
		
		//execute on endpoint
		QueryExecution queryExecution = QueryExecutionFactory.sparqlService(Config.EXPERIMENT_ENDPOINT, rewrittenQuery);
		ResultSet result = queryExecution.execSelect();
		
		//combine results and query patterns to retrieve triples (a set of triples per row (i.e. query solution);
		fetchAndStoresTriplesFromResultSet(result);
		//write these to file (no csv). use structure:
		//experimentDir -> query -> each query solution in a file, where each row is the set of triples for that query solution
	}
	
	private void setupDirStructure() throws IOException {
		queryOutputDir = Utils.mkdir(experimentDir.getPath() + "/query-" + experimentDir.listFiles().length);
		
		//write query to this path
		FileUtils.write(new File(queryOutputDir.getPath() + "/query.txt"), originalQuery.toString());
	}
	
	
	private File getQuerySolutionDir() {
		File file = new File(queryOutputDir.getPath() + "/qs" + queryOutputDir.listFiles().length);
		if (file.exists()) throw new IllegalStateException("Query solution dir " + file.getPath() + " already exists. Stopping getting triples from queries");
		file.mkdir();
		return file;
	}
	
	private String getNodeRepresentation(Node node) throws UnsupportedOperationException {
		String nodeString;
		if (node.toString().startsWith("?")) {
			throw new UnsupportedOperationException("Our node " + node.toString() + " is still a variable... We have a problem!");
		}
		nodeString = node.toString();
		if (node.isURI()) {
			nodeString = "<" + nodeString + ">";
		}
		return nodeString;
	}
	
	private String getStringRepresentation(Triple triple) throws UnsupportedOperationException{
		String tripleString;
		tripleString = getNodeRepresentation(triple.getSubject()) + "\t" + getNodeRepresentation(triple.getPredicate()) + "\t" + getNodeRepresentation(triple.getObject());
		return tripleString + "\n";
	}
	
	private void fetchAndStoresTriplesFromResultSet(ResultSet resultSet) throws IOException {
		try {
			while (resultSet.hasNext()) {
				QuerySolution solution = resultSet.next();
				File outputDir = getQuerySolutionDir();
				File requiredTriplesFile = new File(outputDir.getPath() + "/" + Config.FILE_QTRIPLES_REQUIRED);
				for (Triple triple: rewrittenQuery.fetchTriplesFromPatterns(solution)) {
					FileUtils.write(requiredTriplesFile, getStringRepresentation(triple), true);
				}
				
			}
		} catch (UnsupportedOperationException e) {
			System.out.println(originalQuery.toString());
			throw e;
		} catch (IllegalArgumentException e) {
			System.out.println(originalQuery.toString());
			throw e;
		}
		
	}

	
	public static void fetch(ExperimentSetup experimentSetup, Query query, File experimentDir) throws IOException {
		FetchTriplesFromQuery fetch = new FetchTriplesFromQuery(experimentSetup, query, experimentDir);
		fetch.process();
	}

	public static void main(String[] args) throws Exception {
		boolean useCachedQueries = true;
		ExperimentSetup experimentsetup = new SwdfExperimentSetup(useCachedQueries, true);
		
		Query query = Query.create("SELECT * WHERE {[] a ?name} LIMIT 1");
		FetchTriplesFromQuery.fetch(experimentsetup, query, new File("test"));
		// new EvaluateGraphs(new
		// DbpoExperimentSetup(DbpoExperimentSetup.QALD_REMOVE_OPTIONALS)),
		// new EvaluateGraphs(new
		// DbpoExperimentSetup(DbpoExperimentSetup.QALD_KEEP_OPTIONALS)),
		// new EvaluateGraphs(new
		// DbpoExperimentSetup(DbpoExperimentSetup.QUERY_LOGS)),
		// new EvaluateGraphs(new Sp2bExperimentSetup()),
		// new EvaluateGraphs(new LmdbExperimentSetup()),
		// new EvaluateGraphs(new LgdExperimentSetup()),
		// new EvaluateGraphs(new DbpExperimentSetup()),
	}



}
