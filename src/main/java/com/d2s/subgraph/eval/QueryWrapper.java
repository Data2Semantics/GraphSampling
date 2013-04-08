package com.d2s.subgraph.eval;


import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.d2s.subgraph.helpers.Helper;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QueryParseException;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.Expr;

public class QueryWrapper {
	private boolean aggregation;
	private boolean onlyDbo;
	private Query query;
	private String answerType; 
	private ArrayList<HashMap<String, String>> answers = new ArrayList<HashMap<String, String>>();
	public QueryWrapper(String query) throws QueryParseException {
		setQuery(query);
	}
	public QueryWrapper() {
		
	}
	
	public boolean isSelect() {
		return query.isSelectType();
	}
	
	public boolean isAsk() {
		return query.isAskType();
	}
	
	public String getQuery() {
		return this.query.toString();
	}
	
	public String getQuery(String fromGraph) {
		
		Query queryWithFromClause = query.cloneQuery();
		queryWithFromClause.addGraphURI(fromGraph);
		return queryWithFromClause.toString();
	}
	
	public String getAsConstructQuery() {
		return Helper.getAsConstructQuery(query).toString();
	}
	
	public void setQuery(String query) throws QueryParseException {
		this.query = QueryFactory.create(query);
	}
	
	public ArrayList<HashMap<String, String>> getAnswers() {
		return answers;
	}

	public void setAnswers(ArrayList<HashMap<String, String>> answers) {
		this.answers = answers;
	}
	
	public boolean isAggregation() {
		return aggregation;
	}
	public void setAggregation(boolean aggregation) {
		this.aggregation = aggregation;
	}
	public String getAnswerType() {
		return answerType;
	}
	public void setAnswerType(String answerType) {
		this.answerType = answerType;
	}
	public boolean isOnlyDbo() {
		return onlyDbo;
	}
	public void setOnlyDbo(boolean onlyDbo) {
		this.onlyDbo = onlyDbo;
	}
	
	public void removeProjectVar(String varToRemove) throws IOException {
		if (!query.isSelectType()) {
			return;
		}
		List<String> resultVars = query.getResultVars();
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
			Query newQuery = new Query(query.getPrologue());
			
			//prologue doesnt add prefix? (strange....) Add them manually
			newQuery.setPrefixMapping(query.getPrefixMapping());
			
			newQuery.setQueryPattern(query.getQueryPattern());
			newQuery.setDistinct(query.isDistinct());
//			newQuery.setBaseURI(query.getBaseURI());
			newQuery.setLimit(query.getLimit());
			newQuery.setOffset(query.getOffset());
			newQuery.setQuerySelectType();
			Map<Var, Expr> groupByList = query.getGroupBy().getExprs();
			for (Map.Entry<Var, Expr> entry : groupByList.entrySet()) {
			    newQuery.addGroupBy(entry.getKey(), entry.getValue());
			}
			List<Expr> havingExpressions = query.getHavingExprs();
			for (Expr havingExpr : havingExpressions) {
				newQuery.addHavingCondition(havingExpr);
			}
			//set new result vars (where we removed one)
			newQuery.addProjectVars(newResultVars);
			query = newQuery;
		}
	}
	
	
	public static void main(String[] args)  {
		String query = "PREFIX : <http://bla>\n" +
				"SELECT ?bla ?bla2\n" + 
				"			WHERE\n" + 
				"			{\n" + 
				"			?bla ?v ?bla2 ." +
				"			}";
		QueryWrapper evalQuery = new QueryWrapper(query);
		System.out.println(evalQuery.getQuery());
		try {
			evalQuery.removeProjectVar("bla2");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(evalQuery.getAsConstructQuery());
		
	}
	
}
