package com.d2s.subgraph.eval;

import java.io.IOException;
import java.util.ArrayList;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;
import com.d2s.subgraph.eval.results.GraphResults;
import com.d2s.subgraph.eval.results.GraphResultsRegular;
import com.d2s.subgraph.eval.results.QueryResultsRegular;
import com.d2s.subgraph.helpers.Helper;
import com.d2s.subgraph.queries.GetQueries;
import com.d2s.subgraph.queries.QueryWrapper;
import com.d2s.subgraph.queries.Sp2bQueries;
import com.d2s.subgraph.queries.SwdfQueries;
import com.d2s.subgraph.queries.filters.DescribeFilter;
import com.d2s.subgraph.queries.filters.SimpleBgpFilter;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.query.ResultSetRewindable;
import com.hp.hpl.jena.sparql.engine.binding.Binding;

public class EvaluateGraph {
	// private static String LOCAL_SESAME_REPO = "http://localhost:8080/openrdf-sesame";
	public static String OPS_VIRTUOSO = "http://ops.few.vu.nl:8890/sparql";
	private ArrayList<QueryWrapper> queries = new ArrayList<QueryWrapper>();
	private String goldenStandardGraph;
	private String subGraph;
	public int validCount = 0;
	public int invalidCount = 0;
	private String endpoint;
	GraphResults results = new GraphResultsRegular();

	public EvaluateGraph(GetQueries getQueries, String endpoint, String goldenStandardGraph, String subGraph) {
		queries = getQueries.getQueries();
		this.endpoint = endpoint;
		this.goldenStandardGraph = goldenStandardGraph;
		this.subGraph = subGraph;
		results.setGraphName(subGraph);
	}
	public EvaluateGraph(String endpoint, String goldenStandardGraph, String subGraph) {
		this.endpoint = endpoint;
		this.goldenStandardGraph = goldenStandardGraph;
		this.subGraph = subGraph;
		results.setGraphName(subGraph);
	}

	public void run() throws RepositoryException, MalformedQueryException, QueryEvaluationException, IOException {
		for (QueryWrapper evalQuery : queries) {
			runForQuery(evalQuery);
		}
		System.out.println();
		System.out.println("Invalids (i.e. no results on golden standard, or sparql error on execution): " + Integer.toString(invalidCount)
				+ ", valids: " + Integer.toString(validCount));
	}
	
	public void runForQuery(QueryWrapper evalQuery) throws QueryEvaluationException {
		if (evalQuery.isSelect()) {
			ResultSetRewindable goldenStandardResults;
			ResultSetRewindable subgraphResults;
//			System.out.println(evalQuery.getQueryString(subGraph));
			try {
				goldenStandardResults = executeSelect(endpoint, evalQuery.getQueryString(goldenStandardGraph));
				subgraphResults = executeSelect(endpoint, evalQuery.getQueryString(subGraph));
			} catch (Exception e) {
//				e.printStackTrace();
				invalidCount++;
				return;
			}
			int goldenSize = Helper.getResultSize(goldenStandardResults);
			if (goldenSize == 0) {
//				System.out.println("no results retrieved for query " + evalQuery.getQueryString(goldenStandardGraph));
				invalidCount++;
				return; // havent loaded complete dbpedia yet. might be missing things, so just skip this query
			} else {
//				System.out.println("size: " + goldenSize);
//				System.out.println("sub size: " + Helper.getResultSize(subgraphResults));
				validCount++;
				//double precision = getPrecision(goldenStandardResults, subgraphResults);
//				double recall = getRecallOnBindings(goldenStandardResults, subgraphResults);
				double recall = getRecallOnProjectionVars(goldenStandardResults, subgraphResults);
//				double recall = getRecallOnBindings(goldenStandardResults, subgraphResults);
//				if (recall > 1.0) {
//					System.out.println(evalQuery.getQueryString(subGraph));
//					System.exit(1);
//				}
				QueryResultsRegular result = new QueryResultsRegular();
				result.setQuery(evalQuery);
				result.setPrecision(0.0);//NOTICE: set to 0.0 for now. saves us time to calculate (we don't use precision anyway)
				result.setRecall(recall);
				result.setGoldenStandardSize(goldenSize);
				results.add(result);
				// System.out.println(result.toString());
				System.out.print(recall + ":");
			}
		} else if (evalQuery.isAsk()) {
			// todo
		}
	}

	// Precision is the number of relevant documents a search retrieves divided by the total number of documents retrieved
	@SuppressWarnings("unused")
	private double getPrecision(ResultSetRewindable goldenStandard, ResultSetRewindable subgraph) throws QueryEvaluationException {
		goldenStandard.reset();
		subgraph.reset();
		double falsePositives = 0;
		double truePositives = 0;
		while (subgraph.hasNext()) {
			Binding binding = subgraph.nextBinding();
			if (bindingFoundInQueryResult(binding, goldenStandard)) {
				truePositives++;
			} else {
				falsePositives++;
			}
		}
		double precision = 0;
		if ((truePositives + falsePositives) != 0)
			precision = truePositives / (truePositives + falsePositives);
		return precision;
	}

	// while recall is the number of relevant documents retrieved divided by the total number of existing relevant documents that should
	// have been retrieved.
	@SuppressWarnings("unused")
	private double getRecallOnBindings(ResultSetRewindable goldenStandard, ResultSetRewindable subgraph) throws QueryEvaluationException {
		goldenStandard.reset();
		subgraph.reset();
//		ResultSetFormatter.out(goldenStandard);
//		ResultSetFormatter.out(subgraph);
		double falseNegatives = 0;
		double truePositives = 0;
		while (goldenStandard.hasNext()) {
			Binding binding = goldenStandard.nextBinding();
			if (bindingFoundInQueryResult(binding, subgraph)) {
				truePositives++;
			} else {
				falseNegatives++;
			}
		}
		// System.out.println("truePositives: " + truePositives + " falseNegatives: " + falseNegatives);
		double recall = 0;
		if ((truePositives + falseNegatives) != 0)
			recall = truePositives / (truePositives + falseNegatives);
		return recall;
	}


	// while recall is the number of relevant documents retrieved divided by the total number of existing relevant documents that should
	// have been retrieved.
	private double getRecallOnProjectionVars(ResultSetRewindable goldenStandard, ResultSetRewindable subgraph) throws QueryEvaluationException {
		goldenStandard.reset();
		subgraph.reset();
		double truePositives = 0;
		ArrayList<String> projectionVars = new ArrayList<String>(goldenStandard.getResultVars());
		
		while (subgraph.hasNext()) {
			double maxTruePositives = 0;
			QuerySolution subgraphSolution = subgraph.next();
			goldenStandard.reset();
			while (goldenStandard.hasNext()) {
				double truePositivesForBinding = 0.0;
				QuerySolution goldenStandardSolution = goldenStandard.next();
				double nullMatches = 0.0;
				boolean regularMatches = false;
				for (String projectionVar: projectionVars) {
					if (!subgraphSolution.contains(projectionVar) && !goldenStandardSolution.contains(projectionVar)) {
						//both are null
						nullMatches++;
					} else if (subgraphSolution.contains(projectionVar) && goldenStandardSolution.contains(projectionVar) &&
							subgraphSolution.get(projectionVar).equals(goldenStandardSolution.get(projectionVar))) {
						//both have a result var, and are the same
						regularMatches = true;
						truePositivesForBinding++;
					}
				}
				if (regularMatches) truePositivesForBinding+=nullMatches;
				if (truePositivesForBinding > maxTruePositives) {
					maxTruePositives = truePositivesForBinding;
				}
			}
			
			truePositives+=maxTruePositives;
		}
		double recall = 0;
		if ((Helper.getResultSize(goldenStandard) * projectionVars.size()) > 0)
			recall = truePositives / (Helper.getResultSize(goldenStandard) * projectionVars.size());
		return recall;
	}

	private boolean bindingFoundInQueryResult(Binding binding, ResultSetRewindable queryResult) throws QueryEvaluationException {
		boolean found = false;
		queryResult.reset();
		while (queryResult.hasNext()) {
			if (queryResult.nextBinding().equals(binding)) {
				found = true;
				break;
			}
		}
		return found;
	}

	private static ResultSetRewindable executeSelect(String endpoint, String queryString) throws RepositoryException,
			MalformedQueryException, QueryEvaluationException {
		Query query = QueryFactory.create(queryString);
		QueryExecution queryExecution = QueryExecutionFactory.sparqlService(endpoint, query);
		return ResultSetFactory.copyResults(queryExecution.execSelect());
	}

	public GraphResults getResults() {
		return results;
	}

	public static void main(String[] args) {

		String goldenStandardGraph = "http://sp2b";
		String subgraph = "http://sp2b_s-o-litAsLit_unweighted_directed_betweenness-5_max-50-43.nt";
		try {
//			 EvaluateGraph evaluate = new EvaluateGraph(new QaldDbpQueries(QaldDbpQueries.QALD_2_QUERIES, new OnlyDboQueries()), EvaluateGraph.OPS_VIRTUOSO, goldenStandardGraph, subgraph);
//			 EvaluateGraph evaluate = new EvaluateGraph(new SwdfQueries(new DescribeFilter(), new SimpleBgpFilter()), EvaluateGraph.OPS_VIRTUOSO, goldenStandardGraph, subgraph);
			EvaluateGraph evaluate = new EvaluateGraph( EvaluateGraph.OPS_VIRTUOSO, goldenStandardGraph, subgraph);
			 QueryWrapper query = new QueryWrapper("PREFIX  person: <http://localhost/persons/>\n" + 
			 		"\n" + 
			 		"SELECT  ?subject ?predicate\n" + 
			 		"WHERE\n" + 
			 		"  { ?subject ?predicate person:Paul_Erdoes }");
			 evaluate.runForQuery(query);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
