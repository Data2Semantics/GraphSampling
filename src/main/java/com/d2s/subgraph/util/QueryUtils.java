package com.d2s.subgraph.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;

import com.d2s.subgraph.queries.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.core.TriplePath;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.resultset.ResultSetRewindable;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementPathBlock;
import com.hp.hpl.jena.sparql.syntax.ElementVisitorBase;
import com.hp.hpl.jena.sparql.syntax.ElementWalker;
import com.hp.hpl.jena.sparql.syntax.Template;

public class QueryUtils {
	public static int REWRITE_NODE1 = 0;
	public static int REWRITE_NODE2 = 1;
	public static int REWRITE_NODE3 = 2;
	public static int REWRITE_NODE4 = 3;
	public static int REWRITE_PATH = 4;

	public static int ALG_EIGENVECTOR = 0;
	public static int ALG_PAGERANK = 1;
	public static int ALG_BETWEENNESS = 2;
	public static int ALG_INDEGREE = 3;
	public static int ALG_OUTDEGREE = 4;
	



	public static int getResultSize(ResultSetRewindable resultSet) {
		resultSet.reset();
		int result = 0;
		while (resultSet.hasNext()) {
			resultSet.next();
			result++;
		}
		return result;
	}


	public static Query getAsConstructQuery(Query origQuery) throws IOException {
		Query constructQuery = Query.create(origQuery.toString());
		constructQuery.setQueryConstructType();

		final BasicPattern constructBp = new BasicPattern();

		Element queryPattern = origQuery.getQueryPattern();
		ElementWalker.walk(queryPattern, new ElementVisitorBase() {
			public void visit(ElementPathBlock el) {
				Iterator<TriplePath> triples = el.patternElts();
				while (triples.hasNext()) {
					constructBp.add(triples.next().asTriple());
				}
			}
		});
		constructQuery.setConstructTemplate(new Template(constructBp));
		return constructQuery;
	}

	public static Model executeConstruct(String endpoint, Query query) throws RepositoryException,
			MalformedQueryException, QueryEvaluationException {
		QueryExecution queryExecution = QueryExecutionFactory.sparqlService(endpoint, query);
		return queryExecution.execConstruct();
	}
	

	public static Query removeProjectVarFromQuery(Query origQuery, String varToRemove) throws IOException {
		if (!origQuery.isSelectType()) {
			return null;
		}
		List<String> resultVars = origQuery.getResultVars();
		if (resultVars.contains(varToRemove)) {
			List<String> newResultVars = new ArrayList<String>();
			
			for (String var: resultVars) {
				
				if (var.equals(varToRemove)) {
					//yes, we have it. don't add it to new project vars list!
				} else {
					newResultVars.add(var);
				}
			}
			
			//we cannot remove result vars from query... create new query object..
			//copy all original properties:
			Query newQuery = new Query(origQuery.getPrologue());
			
			//prologue doesnt add prefix? (strange....) Add them manually
			newQuery.setPrefixMapping(origQuery.getPrefixMapping());
			
			newQuery.setQueryPattern(origQuery.getQueryPattern());
			newQuery.setDistinct(origQuery.isDistinct());
//			newQuery.setBaseURI(query.getBaseURI());
			newQuery.setLimit(origQuery.getLimit());
			newQuery.setOffset(origQuery.getOffset());
			newQuery.setQuerySelectType();
			Map<Var, Expr> groupByList = origQuery.getGroupBy().getExprs();
			for (Map.Entry<Var, Expr> entry : groupByList.entrySet()) {
			    newQuery.addGroupBy(entry.getKey(), entry.getValue());
			}
			List<Expr> havingExpressions = origQuery.getHavingExprs();
			for (Expr havingExpr : havingExpressions) {
				newQuery.addHavingCondition(havingExpr);
			}
			//set new result vars (where we removed one)
			newQuery.addProjectVars(newResultVars);
			return newQuery;
		} else {
			return origQuery;
		}
	}
	
}
