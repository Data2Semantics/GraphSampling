package com.d2s.partialreplication.queries.evaluate;

import java.util.ArrayList;

import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.http.HTTPRepository;

import com.d2s.partialreplication.helpers.Helper;
import com.d2s.partialreplication.queries.EvalQuery;
import com.d2s.partialreplication.queries.GetQueries;
import com.d2s.partialreplication.queries.dbpedia.GetDbPediaQueries;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;


public class Evaluate {
	private static String LOCAL_SESAME_REPO = "http://localhost:8080/openrdf-sesame";
	private ArrayList<EvalQuery> queries = new ArrayList<EvalQuery>();
	private String goldenStandardEndpoint;
	private String subgraphEndpoint;
	ArrayList<Result> results = new ArrayList<Result>();
	public Evaluate(GetQueries getQueries, String goldenStandardEndpoint, String subgraphEndpoint) {
		queries = getQueries.getQueries();
		this.goldenStandardEndpoint = goldenStandardEndpoint;
		this.subgraphEndpoint = subgraphEndpoint;
	}
	
	public void run() throws RepositoryException, MalformedQueryException, QueryEvaluationException {
		for (EvalQuery evalQuery: queries) {
			if (evalQuery.isSelect()) {
				
				TupleQueryResult goldenStandardResults = runSelectUsingSesame(LOCAL_SESAME_REPO, goldenStandardEndpoint, evalQuery.getQuery());
				if (Helper.getResultSize(goldenStandardResults) == 0) {
					System.out.println(evalQuery.getQuery());
					return;
				}
//				TupleQueryResult subgraphResults = runSelectUsingSesame(LOCAL_SESAME_REPO, subgraphEndpoint, evalQuery.getQuery());
//				double precision = getPrecision(goldenStandardResults, subgraphResults);
//				double recall = getRecall(goldenStandardResults, subgraphResults);
//				Result result = new Result();
//				result.setQuery(evalQuery.getQuery());
//				result.setPrecision(precision);
//				result.setRecall(recall);
//				results.add(result);
			} else if (evalQuery.isAsk()) {
				//todo
			}
		}
	}
	//Precision is the number of relevant documents a search retrieves divided by the total number of documents retrieved
	public double getPrecision(TupleQueryResult goldenStandard, TupleQueryResult subgraph) throws QueryEvaluationException {
		int falsePositives = 0;
		int truePositives = 0;
		while (subgraph.hasNext()) {
			BindingSet binding = goldenStandard.next();
			if (bindingFoundInQueryResult(binding, goldenStandard)) {
				truePositives++;
			} else {
				falsePositives++;
			}
		}
		return truePositives / (truePositives + falsePositives);
	}
	//while recall is the number of relevant documents retrieved divided by the total number of existing relevant documents that should have been retrieved.
	public double getRecall(TupleQueryResult goldenStandard, TupleQueryResult subgraph) throws QueryEvaluationException {
		int falseNegatives = 0;
		int truePositives = 0;
		while (goldenStandard.hasNext()) {
			BindingSet binding = goldenStandard.next();
			if (bindingFoundInQueryResult(binding, subgraph)) {
				truePositives++;
			} else {
				falseNegatives++;
			}
		}
		return truePositives / (truePositives + falseNegatives);
	}
	
	private boolean bindingFoundInQueryResult(BindingSet binding, TupleQueryResult queryResult) throws QueryEvaluationException {
		boolean found = false;
		while (queryResult.hasNext()) {
			if (queryResult.next().equals(binding)) {
				found = true;
				break;
			}
		}
		return found;
	}
	
	public static ResultSet runSelecUsingJena(String endpoint, String queryString) throws RepositoryException, MalformedQueryException, QueryEvaluationException {
		Query query = QueryFactory.create(queryString);
		QueryExecution queryExecution = QueryExecutionFactory.sparqlService(endpoint, query);
		return queryExecution.execSelect();
	}
	
	public static TupleQueryResult runSelectUsingSesame(String endpoint, String repo, String queryString) throws RepositoryException, QueryEvaluationException, MalformedQueryException {
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
	
	
	public static void main(String[] args)  {
		String goldenStandard = "dbpedia";
		String subgraph = "subgraph";
		try {
			Evaluate evaluate = new Evaluate(new GetDbPediaQueries(), goldenStandard, subgraph);
			evaluate.run();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
