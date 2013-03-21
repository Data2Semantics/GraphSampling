package com.d2s.subgraph.eval.evaluate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.http.HTTPRepository;
import com.d2s.subgraph.eval.EvalQuery;
import com.d2s.subgraph.eval.GetQueries;
import com.d2s.subgraph.eval.dbpedia.QaldDbpQueries;
import com.d2s.subgraph.helpers.Helper;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.query.ResultSetRewindable;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.binding.Binding;


public class Evaluate {
//	private static String LOCAL_SESAME_REPO = "http://localhost:8080/openrdf-sesame";
	private static String OPS_VIRTUOSO = "http://ops.few.vu.nl:8890/sparql";
	private ArrayList<EvalQuery> queries = new ArrayList<EvalQuery>();
	private String goldenStandardGraph;
	private String subGraph;
	private String endpoint;
	ArrayList<Result> results = new ArrayList<Result>();
	public Evaluate(GetQueries getQueries, String endpoint, String goldenStandardGraph, String subGraph) {
		queries = getQueries.getQueries();
		this.endpoint = endpoint;
		this.goldenStandardGraph = goldenStandardGraph;
		this.subGraph = subGraph;
	}
	
	public void run() throws RepositoryException, MalformedQueryException, QueryEvaluationException {
		for (EvalQuery evalQuery: queries) {
			if (evalQuery.isSelect()) {
				ResultSetRewindable goldenStandardResults;
				ResultSetRewindable subgraphResults;
				try {
					goldenStandardResults = runSelectUsingJena(endpoint, evalQuery.getQuery(goldenStandardGraph));
					subgraphResults = runSelectUsingJena(endpoint, evalQuery.getQuery(subGraph));
				} catch (Exception e) {
					System.out.println(e.getMessage());
					continue;
				}
				
				if (Helper.getResultSize(goldenStandardResults) == 0) {
					System.out.println("no results");
//				} else if (Helper.getResultSize(goldenStandardResults) != evalQuery.getAnswers().size()) {
//					System.out.println(evalQuery.getQuery());
//					System.out.println("Not matching number of results, " + Helper.getResultSize(goldenStandardResults) + " vs " + evalQuery.getAnswers().size());
//					return;
				} else {
					double precision = getPrecision(goldenStandardResults, subgraphResults);
					double recall = getRecall(goldenStandardResults, subgraphResults);
					if (precision == 1.0) {
						ResultSetFormatter.out(goldenStandardResults);
						ResultSetFormatter.out(subgraphResults);
						System.exit(1);
					}
					System.out.println("precision: " + precision + ", recall: " + recall );
//					Result result = new Result();
//					result.setQuery(evalQuery.getQuery());
//					result.setPrecision(precision);
//					result.setRecall(recall);
//					results.add(result);
				}
			} else if (evalQuery.isAsk()) {
				//todo
			}
		}
	}
	@SuppressWarnings("unused")
	private double getPrecision(EvalQuery evalQuery, ResultSetRewindable subGraph) throws QueryEvaluationException {
		int falsePositives = 0;
		int truePositives = 0;
		while (subGraph.hasNext()) {
			Binding binding = subGraph.nextBinding();
			if (bindingFoundInAnswerSet(binding, evalQuery)) {
				truePositives++;
			} else {
				falsePositives++;
			}
		}
		double precision = 0;
		if ((truePositives + falsePositives) != 0) precision =  truePositives / (truePositives + falsePositives);
		return precision;
	}


	//Precision is the number of relevant documents a search retrieves divided by the total number of documents retrieved
	private double getPrecision(ResultSetRewindable goldenStandard, ResultSetRewindable subgraph) throws QueryEvaluationException {
		goldenStandard.reset();
		subgraph.reset();
		int falsePositives = 0;
		int truePositives = 0;
		while (subgraph.hasNext()) {
			Binding binding = subgraph.nextBinding();
			if (bindingFoundInQueryResult(binding, goldenStandard)) {
				truePositives++;
			} else {
				falsePositives++;
			}
		}
		double precision = 0;
		if ((truePositives + falsePositives) != 0) precision =  truePositives / (truePositives + falsePositives);
		return precision;
	}
	
	
	//while recall is the number of relevant documents retrieved divided by the total number of existing relevant documents that should have been retrieved.
	private double getRecall(ResultSetRewindable goldenStandard, ResultSetRewindable subgraph) throws QueryEvaluationException {
		goldenStandard.reset();
		subgraph.reset();
		int falseNegatives = 0;
		int truePositives = 0;
		while (goldenStandard.hasNext()) {
			Binding binding = goldenStandard.nextBinding();
			if (bindingFoundInQueryResult(binding, subgraph)) {
				truePositives++;
			} else {
				falseNegatives++;
			}
		}
		double recall = 0;
		if ((truePositives + falseNegatives) != 0) recall = truePositives / (truePositives + falseNegatives);
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
	
	private boolean bindingFoundInAnswerSet(Binding binding, EvalQuery evalQuery) {
		boolean found = false;
		ArrayList<HashMap<String,String>> goldenAnswers = evalQuery.getAnswers();
		for(HashMap<String,String> answerSet: goldenAnswers) {
			Set<Var> vars = getVarIteratorAsStringSet(binding.vars());
			boolean allMatch = true;
			for(Var var:vars) {
				if (answerSet.containsKey(var)) {
					if (answerSet.get(var).equals(binding.get(var).toString())) {
						//this one is fine. check rest of answers
					}
				} else {
					allMatch = false;
					break;
				}
			}
			if (allMatch) {
				found = true;
				break;
			}
			
			
		}
		return found;
	}
	
	
	private static ResultSetRewindable runSelectUsingJena(String endpoint, String queryString) throws RepositoryException, MalformedQueryException, QueryEvaluationException {
		Query query = QueryFactory.create(queryString);
		QueryExecution queryExecution = QueryExecutionFactory.sparqlService(endpoint, query);
		return ResultSetFactory.copyResults(queryExecution.execSelect());
	}
	
	@SuppressWarnings("unused")
	private static TupleQueryResult runSelectUsingSesame(String endpoint, String repo, String queryString) throws RepositoryException, QueryEvaluationException, MalformedQueryException {
		HTTPRepository dbpediaEndpoint = new HTTPRepository(endpoint, repo);
		dbpediaEndpoint.initialize();
		RepositoryConnection conn =  dbpediaEndpoint.getConnection();
		try {
		  TupleQuery query = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
		  TupleQueryResult result = query.evaluate();
		  return result;
		 
		}
		finally {
		  conn.close();
		}
	}
	
	private Set<Var> getVarIteratorAsStringSet(Iterator<Var> iterator){
	    Set<Var> result = new HashSet<Var>();
	    while (iterator.hasNext()) {
	        result.add(iterator.next());
	    }
	    return result;
	}
	
	public static void main(String[] args)  {
		String goldenStandardGraph = "http://dbpedia.org";
		String subgraph = "htpp://dbpediasubgraph.org";
		try {
			Evaluate evaluate = new Evaluate(new QaldDbpQueries(QaldDbpQueries.QALD_2_QUERIES), Evaluate.OPS_VIRTUOSO, goldenStandardGraph, subgraph);
			evaluate.run();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
