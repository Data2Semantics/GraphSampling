package com.d2s.subgraph.queries;


import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.builder.HashCodeBuilder;
import com.d2s.subgraph.helpers.Helper;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QueryParseException;
import com.hp.hpl.jena.query.ResultSetRewindable;
import com.hp.hpl.jena.sparql.algebra.Algebra;
import com.hp.hpl.jena.sparql.algebra.OpVisitorBase;
import com.hp.hpl.jena.sparql.algebra.OpWalker;
import com.hp.hpl.jena.sparql.algebra.op.OpJoin;
import com.hp.hpl.jena.sparql.algebra.op.OpLeftJoin;
import com.hp.hpl.jena.sparql.core.TriplePath;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementPathBlock;
import com.hp.hpl.jena.sparql.syntax.ElementVisitorBase;
import com.hp.hpl.jena.sparql.syntax.ElementWalker;

public class QueryWrapper {
	private boolean onlyDbo;
	private Query query;
	private String answerType; 
	private int queryId;
	private ArrayList<HashMap<String, String>> answers = new ArrayList<HashMap<String, String>>();
	private ResultSetRewindable goldenStandardResults = null;
	private int triplePatternCountCcv = 0;
	private int triplePatternCountCvv = 0;
	private int triplePatternCountVcc = 0;
	private int numberOfJoins = 0;
	private int numberOfLeftJoins = 0;
	private int numberOfNonOptionalTriplePatterns = 0;
	public QueryWrapper() {
	}
	
	public QueryWrapper(String query) throws QueryParseException {
		setQuery(query);

	}
	public QueryWrapper(Query query) {
		this.query = query;
	}
	
	public void generateStats() {
		getTriplePatternsInfo();
		getJoinsStats();
	}
	
	
	public boolean isSelect() {
		return query.isSelectType();
	}
	
	public boolean isAsk() {
		return query.isAskType();
	}
	
	public Query getQuery() {
		return this.query;
	}
	
	public String getQueryString(String fromGraph) {
		return Helper.addFromClause(query, fromGraph).toString();
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
		return query.hasAggregators();
		
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
	
	
	public int getQueryId() {
		return queryId;
	}
	public void setQueryId(int queryId) {
		this.queryId = queryId;
	}
	
	public String toString() {
		return query.toString();
	}
	
	public boolean equals(Object o) {
		if (o.toString().equals(this.toString())) {
			return true;
		} else {
			return false;
		}
	}
	
	public int hashCode() {
        return new HashCodeBuilder(17, 31). // two randomly chosen prime numbers
            append(query.toString()).
            toHashCode();
    }
	

	
	public void setGoldenStandardResults(ResultSetRewindable resultSet) {
		this.goldenStandardResults  = resultSet;
	}
	
	public ResultSetRewindable getGoldenStandardResults() {
		return goldenStandardResults;
	}
	
	public void getJoinsStats() {
		OpWalker.walk(Algebra.compile(query), 
			new OpVisitorBase() {
				public void visit(OpJoin opJoin) {
					numberOfJoins++;
				}
				public void visit(OpLeftJoin opLeftJoin) {
					numberOfLeftJoins++;
				}
		});
	}
	
	private void getTriplePatternsInfo() {
		Element qPattern = query.getQueryPattern();

		// This will walk through all parts of the query
		ElementWalker.walk(qPattern,
		    // For each element...
		    new ElementVisitorBase() {
		        // ...when it's a block of triples...
		        public void visit(ElementPathBlock el) {
		        	
		            // ...go through all the triples...
		            Iterator<TriplePath> triples = el.patternElts();
		            while (triples.hasNext()) {
		            	numberOfNonOptionalTriplePatterns++;
		            	TriplePath triplePath = triples.next();
		            	if (	!triplePath.getSubject().isVariable() && 
		            			!triplePath.getPredicate().isVariable() &&
		            			triplePath.getObject().isVariable()) {
		            		triplePatternCountCcv++;
		            	} else if (	!triplePath.getSubject().isVariable() && 
			            			triplePath.getPredicate().isVariable() &&
			            			triplePath.getObject().isVariable()) {
		            		triplePatternCountCvv++;
		            	} else if (	triplePath.getSubject().isVariable() && 
			            			!triplePath.getPredicate().isVariable() &&
			            			!triplePath.getObject().isVariable()) {
		            		triplePatternCountVcc++;
		            	}
		            }
		        }
		    }
		);
	}
	
	/**
	 * given a sub/pred, obtain object value
	 * @return
	 */
	public int getTriplePatternCountCcv() {
		return triplePatternCountCcv;
	}
	/**
	 * given a subject, obtain pred/obj values
	 * @return
	 */
	public int getTriplePatternCountCvv() {
		return triplePatternCountCvv;
	}
	/**
	 * Given a predicate and object, obtain subject value
	 * @return
	 */
	public int getTriplePatternCountVcc() {
		return triplePatternCountVcc;
	}
	
	public int getNumberOfJoins() {
		return numberOfJoins;
	}
	public int getNumberOfLeftJoins() {
		return numberOfLeftJoins;
	}
	public int getNumberOfNonOptionalTriplePatterns() {
		return numberOfNonOptionalTriplePatterns ;
	}
	
	
	public static void main(String[] args)  {
		String query = "SELECT ?name ?city\n" + 
				"WHERE {\n" + 
				"    ?who <http://Person#fname> ?name .\n" + 
				"  ?b <http://blaat> ?bla. \n }";
		QueryWrapper evalQuery = new QueryWrapper(query);
		System.out.println(Algebra.compile(evalQuery.getQuery()).toString());
		System.out.println("ccv count: " + evalQuery.getTriplePatternCountCcv());
		System.out.println("cvv count: " + evalQuery.getTriplePatternCountCvv());
		System.out.println("vcc count: " + evalQuery.getTriplePatternCountVcc());
		System.out.println("number of joins: " + evalQuery.getNumberOfJoins());
		System.out.println("number of left joins: " + evalQuery.getNumberOfLeftJoins());
		System.out.println("number of non-optional triple patterns: " + evalQuery.getNumberOfNonOptionalTriplePatterns());
	}
}
