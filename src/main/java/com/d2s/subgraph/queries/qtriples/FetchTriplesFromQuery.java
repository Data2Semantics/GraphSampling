package com.d2s.subgraph.queries.qtriples;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import com.d2s.subgraph.eval.Config;
import com.d2s.subgraph.eval.experiments.ExperimentSetup;
import com.d2s.subgraph.eval.experiments.SwdfExperimentSetup;
import com.d2s.subgraph.queries.Query;
import com.d2s.subgraph.queries.qtriples.visitors.ExtractTriplePatternsVisitor;
import com.d2s.subgraph.util.Utils;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;
import com.hp.hpl.jena.sparql.syntax.TripleCollectorMark;

public class FetchTriplesFromQuery {
	
	private ExperimentSetup experimentSetup;
	private Query originalQuery;
	private Query rewrittenQuery;
	private File experimentDir;
	private File queryOutputDir;
	private int querySolutionsCount = 0;
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
//		System.out.println("rewrite");
		//rewrite to * query
		rewrittenQuery = originalQuery.getQueryForTripleRetrieval();
		
		//make sure the query targets the proper graph
		rewrittenQuery = rewrittenQuery.getQueryWithFromClause(experimentSetup.getGoldenStandardGraph());
		
		//execute on endpoint
		QueryExecution queryExecution = QueryExecutionFactory.sparqlService(Config.EXPERIMENT_ENDPOINT, rewrittenQuery);
//		System.out.println("exec");
		ResultSet result = queryExecution.execSelect();
//		System.out.println("store");
		fetchAndStoresTriplesFromResultSet(result);
		//combine results and query patterns to retrieve triples (a set of triples per row (i.e. query solution);
		//experimentDir -> query -> query solution -> triple files
	}
	
	private void setupDirStructure() throws IOException {
		queryOutputDir = Utils.mkdir(experimentDir.getPath() + "/query-" + experimentDir.listFiles().length);
		
		//write query to this path
		FileUtils.write(new File(queryOutputDir.getPath() + "/query.txt"), originalQuery.toString());
	}
	
	
	private File getQuerySolutionDir() {
		//only use listfiles() once! This becomes very expensive when there are a lot of files in this dir.. Just use our own iterator
		if (querySolutionsCount == 0) querySolutionsCount = queryOutputDir.listFiles().length;
		File file = new File(queryOutputDir.getPath() + "/qs" + querySolutionsCount);
		if (file.exists()) throw new IllegalStateException("Query solution dir " + file.getPath() + " already exists. Stopping getting triples from queries");
		file.mkdir();
		querySolutionsCount++;
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
//		System.out.println("fetching and storing");
		try {
			//to avoid constantly doing ask calls, we should save the triple blocks we -know- are there, here
			Set<TripleCollectorMark> knownToExist = new HashSet<TripleCollectorMark>(); 
			Set<TripleCollectorMark> knownNotToExist = new HashSet<TripleCollectorMark>(); 
			boolean hasResults = false;
			while (resultSet.hasNext()) {
				hasResults = true;
				QuerySolution solution = resultSet.next();
				File outputDir = getQuerySolutionDir();
				ExtractTriplePatternsVisitor visitor = new ExtractTriplePatternsVisitor(experimentSetup.getGoldenStandardGraph(), knownToExist, knownNotToExist);
				rewrittenQuery.fetchTriplesFromPatterns(solution, visitor);
				writeRequiredTriples(new File(outputDir.getPath() + "/" + Config.FILE_QTRIPLES_REQUIRED), visitor.getRequiredTriples());
				writeOptionalTriples(new File(outputDir.getPath() + "/" + Config.FILE_QTRIPLES_OPTIONAL), visitor.getOptionalTriples());
				writeUnionTriples(new File(outputDir.getPath() + "/" + Config.FILE_QTRIPLES_UNION), visitor.getUnionTriples());
//				QTriples fetchedTriples = rewrittenQuery.fetchTriplesFromPatterns(solution);
//				fetchedTriples.checkPossibleTriples(experimentSetup.getGoldenStandardGraph(), knownTriples, knownNotToExistTriples);
//				knownTriples.addAll(fetchedTriples.getRequiredTriples());
				
				
			}
			if (!hasResults) System.out.println("STRANGE! query " + queryOutputDir.getPath() + " does not have any results!");
		} catch (UnsupportedOperationException e) {
			System.out.println(originalQuery.toString());
			throw e;
		} catch (IllegalArgumentException e) {
			System.out.println(originalQuery.toString());
			throw e;
		}
		
	}

	
	private void writeUnionTriples(File file, LinkedHashMap<String, Set<Triple>> unionTriples) throws UnsupportedOperationException, IOException {
		for (Entry<String, Set<Triple>> triples: unionTriples.entrySet()) {
			for (Triple triple: triples.getValue()) {
				FileUtils.write(new File(file.getPath() + "/" + triples.getKey()), getStringRepresentation(triple), true);
			}
		}
	}


	private void writeOptionalTriples(File file, LinkedHashMap<Integer, Set<Triple>> optionalTriples) throws UnsupportedOperationException, IOException {
		for (Entry<Integer, Set<Triple>> triples: optionalTriples.entrySet()) {
			for (Triple triple: triples.getValue()) {
				FileUtils.write(new File(file.getPath() + "/" + triples.getKey()), getStringRepresentation(triple), true);
			}
		}

	}


	private void writeRequiredTriples(File file, Set<Triple> set) throws UnsupportedOperationException, IOException {
		for (Triple triple: set) {
			FileUtils.write(file, getStringRepresentation(triple), true);
		}
		
	}


	public static void fetch(ExperimentSetup experimentSetup, Query query, File experimentDir) throws IOException {
		FetchTriplesFromQuery fetch = new FetchTriplesFromQuery(experimentSetup, query, experimentDir);
		fetch.process();
	}

	public static void main(String[] args) throws Exception {
		boolean useCachedQueries = true;
		ExperimentSetup experimentsetup = new SwdfExperimentSetup(useCachedQueries, true);
		
		Query query = Query.create("SELECT  ?result\n" + 
				"FROM <http://swdf>\n" + 
				"WHERE\n" + 
				"  { ?result <http://www.w3.org/2000/01/rdf-schema#label> \"Christophe Guéret\" }");
//		System.out.println(query.toString());
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
