package com.d2s.subgraph.eval.batch;

import java.io.IOException;
import java.util.ArrayList;

import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;
import com.d2s.subgraph.eval.QueryWrapper;
import com.d2s.subgraph.eval.results.GraphResults;
import com.d2s.subgraph.eval.results.GraphResultsRegular;
import com.d2s.subgraph.eval.results.QueryResultsRegular;
import com.d2s.subgraph.helpers.Helper;
import com.d2s.subgraph.queries.GetQueries;
import com.d2s.subgraph.queries.QaldDbpQueries;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
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

	public void run() throws RepositoryException, MalformedQueryException, QueryEvaluationException, IOException {
		for (QueryWrapper evalQuery : queries) {
			if (evalQuery.isSelect()) {
				ResultSetRewindable goldenStandardResults;
				ResultSetRewindable subgraphResults;
				try {
					goldenStandardResults = runSelectUsingJena(endpoint, evalQuery.getQueryString(goldenStandardGraph));
					subgraphResults = runSelectUsingJena(endpoint, evalQuery.getQueryString(subGraph));
				} catch (Exception e) {
					// e.printStackTrace();
					invalidCount++;
					continue;
				}

				if (Helper.getResultSize(goldenStandardResults) == 0) {
					// System.out.println("no results retrieved for query " + evalQuery.getQueryString(goldenStandardGraph));
					invalidCount++;
					continue; // havent loaded complete dbpedia yet. might be missing things, so just skip this query
				} else {
					validCount++;
					double precision = getPrecision(goldenStandardResults, subgraphResults);
					double recall = getRecall(goldenStandardResults, subgraphResults);
					QueryResultsRegular result = new QueryResultsRegular();
					result.setQuery(evalQuery);
					result.setPrecision(precision);
					result.setRecall(recall);
					results.add(result);
					// System.out.println(result.toString());
					System.out.print(recall + ":");
				}
			} else if (evalQuery.isAsk()) {
				// todo
			}
		}
		System.out.println();
		System.out.println("Invalids (i.e. no results on golden standard, or sparql error on execution): " + Integer.toString(invalidCount)
				+ ", valids: " + Integer.toString(validCount));
	}

	// Precision is the number of relevant documents a search retrieves divided by the total number of documents retrieved
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
	private double getRecall(ResultSetRewindable goldenStandard, ResultSetRewindable subgraph) throws QueryEvaluationException {
		goldenStandard.reset();
		subgraph.reset();
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

	private double getCorrelation(ResultSetRewindable goldenStandard, ResultSetRewindable subgraph) {
		double correlation = 0;

		return correlation;
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

	private static ResultSetRewindable runSelectUsingJena(String endpoint, String queryString) throws RepositoryException,
			MalformedQueryException, QueryEvaluationException {
		Query query = QueryFactory.create(queryString);
		QueryExecution queryExecution = QueryExecutionFactory.sparqlService(endpoint, query);
		return ResultSetFactory.copyResults(queryExecution.execSelect());
	}

	public GraphResults getResults() {
		return results;
	}

	public static void main(String[] args) {

		String goldenStandardGraph = "http://dbpo";
		String subgraph = "http://dbp_s-o_unweighted_noLit_directed_outdegree_0.5.nt";
		try {
			 EvaluateGraph evaluate = new EvaluateGraph(new QaldDbpQueries(QaldDbpQueries.QALD_2_QUERIES), EvaluateGraph.OPS_VIRTUOSO,
			 goldenStandardGraph, subgraph);
			 evaluate.run();
			 System.out.println(evaluate.getResults().getQueryIds());
			 System.out.println(evaluate.getResults().get(2));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
