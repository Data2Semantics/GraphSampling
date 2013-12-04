package com.d2s.subgraph.eval.generation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import org.data2semantics.query.QueryCollection;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;

import com.d2s.subgraph.eval.Config;
import com.d2s.subgraph.eval.experiments.ExperimentSetup;
import com.d2s.subgraph.eval.experiments.Sp2bExperimentSetup;
import com.d2s.subgraph.eval.results.GraphResults;
import com.d2s.subgraph.eval.results.QueryResultsRegular;
import com.d2s.subgraph.io.QResultsSaver;
import com.d2s.subgraph.queries.Query;
import com.d2s.subgraph.util.QueryUtils;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.query.ResultSetRewindable;
import com.hp.hpl.jena.sparql.engine.binding.Binding;

public class FetchGraphResults {
	private String goldenStandardGraph;
	private String subGraph;
	private ExperimentSetup experimentSetup;
	public int validCount = 0;
	public int invalidCount = 0;
	GraphResults graphResults;

	public FetchGraphResults(ExperimentSetup experimentSetup, String subGraph) {
		this.goldenStandardGraph = experimentSetup.getGoldenStandardGraph();
		this.experimentSetup = experimentSetup;
		this.subGraph = subGraph;
		graphResults.setGraphName(subGraph);
	}

	public ExperimentSetup getExperimentSetup() {
		return this.experimentSetup;
	}


	public void runAndStore() throws RepositoryException, MalformedQueryException, QueryEvaluationException, IOException, IllegalStateException {
		for (Query evalQuery : experimentSetup.getQueryCollection().getQueries()) {
			runForQuery(evalQuery);
		}
		System.out.println();
		System.out.println("Invalids (i.e. no results on golden standard, or sparql error on execution): " + Integer.toString(invalidCount)
				+ ", valids: " + Integer.toString(validCount));
		if (validCount == 0 && invalidCount > 0) {
			throw new IllegalStateException("Something wrong. only invalids. OPS down? No internet connection?");
		}
		QResultsSaver.write(this);
	}

	

	public void runForQuery(Query query) throws QueryEvaluationException, IOException {
		if (query.isSelectType()) {
			ResultSetRewindable goldenStandardResults;
			ResultSetRewindable subgraphResults;
			QueryResultsRegular result = new QueryResultsRegular();
			// System.out.println(evalQuery.getQueryString(subGraph));
			try {
				if (query.getGoldenStandardResults() == null) {
					//store golden standard results, so we don't have to execute this query for every subgraph
					Date start = new Date();
					goldenStandardResults = executeSelect(Config.EXPERIMENT_ENDPOINT, query.getQueryWithFromClause(goldenStandardGraph));
					Date end = new Date();
					query.setGoldenStandardDuration(new Date(start.getTime()-end.getTime()));
					query.setGoldenStandardResults(goldenStandardResults);
				} else {
					goldenStandardResults = query.getGoldenStandardResults();
				}
				Date start = new Date();
				subgraphResults = executeSelect(Config.EXPERIMENT_ENDPOINT, query.getQueryWithFromClause(subGraph));
				Date end = new Date();
				result.setSubgraphDuration(new Date(start.getTime()-end.getTime()));
			} catch (Exception e) {
				// e.printStackTrace();
				invalidCount++;
				return;
			}
			int goldenSize = QueryUtils.getResultSize(goldenStandardResults);
			if (goldenSize == 0) {
				// System.out.println("no results retrieved for query " + evalQuery.getQueryString(goldenStandardGraph));
				invalidCount++;
				return; // havent loaded complete dbpedia yet. might be missing things, so just skip this query
			} else {
				// System.out.println("size: " + goldenSize);
				// System.out.println("sub size: " + Helper.getResultSize(subgraphResults));
				validCount++;
				// double precision = getPrecision(goldenStandardResults, subgraphResults);
				// double recall = getRecallOnBindings(goldenStandardResults, subgraphResults);
				double recall = getRecallOnProjectionVars(goldenStandardResults, subgraphResults);
				// double recall = getRecallOnBindings(goldenStandardResults, subgraphResults);
				if (recall > 1.0) {
					System.out.println("recall higher than 1.0???? yeah, right");
					System.out.println(query.getQueryWithFromClause(subGraph).toString());
					System.exit(1);
				}
				
//				result.setQuery(evalQuery);
				result.setPrecision(0.0);// NOTICE: set to 0.0 for now. saves us time to calculate (we don't use precision anyway)
				result.setRecall(recall);
				result.setGoldenStandardSize(goldenSize);
				query.setResults(result);
				graphResults.add(query);
				
				// System.out.println(result.toString());
				System.out.print(recall + ":");
			}
		} else if (query.isAskType()) {
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
		// ResultSetFormatter.out(goldenStandard);
		// ResultSetFormatter.out(subgraph);
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
	private double getRecallOnProjectionVars(ResultSetRewindable goldenStandard, ResultSetRewindable subgraph)
			throws QueryEvaluationException {
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
				for (String projectionVar : projectionVars) {
					if (!subgraphSolution.contains(projectionVar) && !goldenStandardSolution.contains(projectionVar)) {
						// both are null
						nullMatches++;
					} else if (subgraphSolution.contains(projectionVar) && goldenStandardSolution.contains(projectionVar)
							&& subgraphSolution.get(projectionVar).equals(goldenStandardSolution.get(projectionVar))) {
						// both have a result var, and are the same
						regularMatches = true;
						truePositivesForBinding++;
					}
				}
				if (regularMatches)
					truePositivesForBinding += nullMatches;
				if (truePositivesForBinding > maxTruePositives) {
					maxTruePositives = truePositivesForBinding;
				}
			}

			truePositives += maxTruePositives;
		}
		double recall = 0;
		double totalSize = (double)(QueryUtils.getResultSize(goldenStandard) * projectionVars.size());
		graphResults.addRecallGoldenStandardSize((int)totalSize);
		graphResults.addRecallTruePositives((int)truePositives);
		//divide by projection vars size, so we don't give too strong of an influence to queries with lots of projection vars
		if (totalSize > 0)
			recall = truePositives / totalSize;
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

	private static ResultSetRewindable executeSelect(String endpoint, Query query) throws RepositoryException,
			MalformedQueryException, QueryEvaluationException {
		QueryExecution queryExecution = QueryExecutionFactory.sparqlService(endpoint, query);
		return ResultSetFactory.copyResults(queryExecution.execSelect());
	}

	public GraphResults getResults() {
		return graphResults;
	}

	public static void main(String[] args) {

		String goldenStandardGraph = "http://sp2b";
		String subgraph = "http://df-s-o-litAsLit-unweighted-directed-pagerank-min-max-50-50.nt";
		try {
			// EvaluateGraph evaluate = new EvaluateGraph(new QaldDbpQueries(QaldDbpQueries.QALD_2_QUERIES, new OnlyDboQueries()),
			// EvaluateGraph.OPS_VIRTUOSO, goldenStandardGraph, subgraph);
			// EvaluateGraph evaluate = new EvaluateGraph(new SwdfQueries(new DescribeFilter(), new SimpleBgpFilter()),
			// EvaluateGraph.OPS_VIRTUOSO, goldenStandardGraph, subgraph);
			FetchGraphResults evaluate = new FetchGraphResults(new Sp2bExperimentSetup(), subgraph);
			Query query = Query.create("", new QueryCollection<Query>());
			evaluate.runForQuery(query);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
