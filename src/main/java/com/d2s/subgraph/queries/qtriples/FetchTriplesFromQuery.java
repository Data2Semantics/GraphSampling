package com.d2s.subgraph.queries.qtriples;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.syntax.TripleCollectorMark;

public class FetchTriplesFromQuery {
	
	private ExperimentSetup experimentSetup;
	private Query originalQuery;
	private Query rewrittenQuery;
	private File experimentDir;
	private File queryOutputDir;
	private int querySolutionsCount = 0;
	private Map<File, Integer> fileCounts = new HashMap<File, Integer>();
	private List<Var> projectionVariables;
	private Map<Map<String, RDFNode>, File> storedQuerySolutions = new HashMap<Map<String, RDFNode>, File>();
	//swdf
	//	query-0
	//		solution-0
	//			collapsed-0

	public FetchTriplesFromQuery(ExperimentSetup experimentSetup, Query query, File experimentDir) throws IOException {
		this.originalQuery = query;
		this.experimentSetup = experimentSetup;
		this.experimentDir = Utils.mkdir(experimentDir);
		setupDirStructure();
	}
	
	
	private void process() throws IOException {
		projectionVariables = originalQuery.getProjectVars();
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
	
	
	private File getRootQuerySolutionDir() {
		//only use listfiles() once! This becomes very expensive when there are a lot of files in this dir.. Just use our own iterator
		if (querySolutionsCount == 0) querySolutionsCount = queryOutputDir.listFiles().length;
		
		File file = new File(queryOutputDir.getPath() + "/qs" + querySolutionsCount);
		
		
		if (file.exists()) throw new IllegalStateException("Query solution dir " + file.getPath() + " already exists. Stopping getting triples from queries");
		file.mkdir();
		querySolutionsCount++;
		
		
		
		return file;
	}
	
	/**
	 * Each query solution might have several 'collapsed' query solution. There may be several query solutions to our distinct * query, which are each able to answer
	 * a single query solution from the original query (e.g. when that query uses distinct icw projection vars). In that case we only need 1 of the query solutions from our distinct * query!
	 * Therefore, we need to differentiate between some query solutions. We do that by storing them in separate 'collapsed' dirs
	 * @return
	 */
	private File getCollapsedQuerySolutionDir(File rootQsDir) {
		//we use our own filecount object, to avoid using the listFiles() java function (which can get quite expensive)
		Integer fileCount = fileCounts.get(rootQsDir);
		if (fileCount == null) {
			fileCount = 0;
			fileCounts.put(rootQsDir, fileCount);//havent used this dir yet to store stuff in, so it has zero files
			
		}
		
		File file = new File(rootQsDir.getPath() + "/collapsed" + fileCount);
		if (file.exists()) throw new IllegalStateException("Query solution dir " + file.getPath() + " already exists. Stopping getting triples from queries");
		file.mkdir();
		
		fileCounts.put(rootQsDir, fileCounts.get(rootQsDir) + 1);
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
				
				Map<String, RDFNode> projectionVarBindings = getProjectVarBindings(solution);
				File outputDir = null;
				if (originalQuery.isDistinct() && !originalQuery.isQueryResultStar()) {
//					System.out.println(projectionVarBindings.toString());
					if (storedQuerySolutions.keySet().contains(projectionVarBindings)) {
						outputDir = storedQuerySolutions.get(projectionVarBindings);
						outputDir = getCollapsedQuerySolutionDir(outputDir);
					}
				}
				if (outputDir == null) {
					outputDir = getRootQuerySolutionDir();
					storedQuerySolutions.put(projectionVarBindings, outputDir);
					outputDir = getCollapsedQuerySolutionDir(outputDir);
				}
				
				
				ExtractTriplePatternsVisitor visitor = new ExtractTriplePatternsVisitor(experimentSetup.getGoldenStandardGraph(), knownToExist, knownNotToExist);
				
				Query queryToFetchPatternsFrom = rewrittenQuery.clone(); //clone, otherwise we replace vars with values, and our next iterations fucks up
				queryToFetchPatternsFrom.fetchTriplesFromPatterns(solution, visitor);
//				System.out.println(visitor.getRequiredTriples());
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

	
	private Map<String, RDFNode> getProjectVarBindings(QuerySolution solution) {
		HashMap<String, RDFNode> projVarBindings = new HashMap<String, RDFNode>();
		
		for (Var projVar: projectionVariables) {
			projVarBindings.put(projVar.toString(), solution.get(projVar.toString()));
		}
		
		return projVarBindings;
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
		Query query = Query.create(""
				+ "BASE    <http://192.168.27.144/eswc_planner/run/index.php>\n" + 
				"PREFIX  dct:  <http://purl.org/dc/terms/>\n" + 
				"PREFIX  swrc: <http://swrc.ontoware.org/ontology#>\n" + 
				"PREFIX  swc:  <http://data.semanticweb.org/ns/swc/ontology#>\n" + 
				"\n" + 
				"SELECT DISTINCT  ?paper ?title ?abstract\n" + 
				"FROM <http://swdf>\n" + 
				"WHERE\n" + 
				"  { ?conf swc:hasAcronym \"WWW2009\" .\n" + 
				"    ?conf swc:hasRelatedDocument ?proceedings .\n" + 
				"    ?proceedings swc:hasPart ?paper .\n" + 
				"    ?paper dct:title ?title .\n" + 
				"    ?paper swrc:abstract ?abstract\n" + 
				"  }\n" + 
				"ORDER BY ?title\n" + 
				"OFFSET  0\n" + 
				"LIMIT   10" + 
				"");
//		System.out.println(query.toString());
		
		File outputDir = new File("test");
		if (outputDir.exists()) FileUtils.deleteDirectory(outputDir);
		FetchTriplesFromQuery.fetch(experimentsetup, query, outputDir);
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
