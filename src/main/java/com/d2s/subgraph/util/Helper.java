package com.d2s.subgraph.util;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;

import au.com.bytecode.opencsv.CSVWriter;

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

public class Helper {
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

	public static String boolAsString(boolean bool) {
		if (bool) {
			return "1";
		} else {
			return "0";
		}
	}

	public static Query getAsConstructQuery(Query origQuery) {
		Query constructQuery = (Query) origQuery.cloneQuery();
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

	public static String getAsRoundedString(double value, int precision) {
		// formatting numbers upto 3 decimal places in Java
		String decimalFormat = "#,###,##0.";
		for (int i = 0; i < precision; i++) {
			decimalFormat += "0";
		}
		DecimalFormat df = new DecimalFormat(decimalFormat);
		return df.format(value);
	}


	public static String getDoubleAsFormattedString(double value) {
		String doubleString = Helper.getAsRoundedString(value, 3);
		if (value == 0.0) {
			doubleString = "<span style='color:red;font-weight:bold;'>" + doubleString + "</span>";
		} else if (value == 1.0) {
			doubleString = "<span style='color:green;font-weight:bold;'>" + doubleString + "</span>";
		} else if (value < 0.5) {
			doubleString = "<span style='color:red;'>" + doubleString + "</span>";
		} else {
			doubleString = "<span style='color:green;'>" + doubleString + "</span>";
		}

		return doubleString;

	}

	public static boolean partialStringMatch(String haystack, ArrayList<String> needles) {
		boolean foundMatch = false;
		for (String needle : needles) {
			if (haystack.contains(needle)) {
				foundMatch = true;
				break;
			}
		}
		return foundMatch;
	}

	public static Model executeConstruct(String endpoint, Query query) throws RepositoryException,
			MalformedQueryException, QueryEvaluationException {
		QueryExecution queryExecution = QueryExecutionFactory.sparqlService(endpoint, query);
		return queryExecution.execConstruct();
	}
	
	public static Query addFromClauseToQuery(Query query, String fromGraph) {
		Query queryWithFromClause = (Query) query.cloneQuery();
		queryWithFromClause.addGraphURI(fromGraph);
		return queryWithFromClause;
	}
	
	public static void executeCommand(String[] args) throws IOException, InterruptedException {
		ProcessBuilder ps = new ProcessBuilder(args);
		Process pr = ps.start();
		pr.waitFor();
	}

	public static int getRewriteMethod(String graphName) {
		int rewriteMethod = -1;
		if (graphName.contains("litAsLit")) {
			rewriteMethod = REWRITE_NODE3;
		} else if (graphName.contains("noLit")) {
			rewriteMethod = REWRITE_NODE2;
		} else if (graphName.contains("litAsNode")) {
			rewriteMethod = REWRITE_NODE1;
		} else if (graphName.contains("litWithPred")) {
			rewriteMethod = REWRITE_NODE4;
		} else if (graphName.contains("so-so")) {
			rewriteMethod = REWRITE_PATH;
		}
		return rewriteMethod;
	}
	
	public static int getAnalysisAlgorithm(String graphName) {
		int algorithm = -1;
		if (graphName.contains("eigenvector")) {
			algorithm = ALG_EIGENVECTOR;
		} else if (graphName.contains("pagerank")) {
			algorithm = ALG_PAGERANK;
		} else if (graphName.contains("betweenness")) {
			algorithm = ALG_BETWEENNESS;
		} else if (graphName.contains("indegree")) {
			algorithm = ALG_INDEGREE;
		} else if (graphName.contains("outdegree")) {
			algorithm = ALG_OUTDEGREE;
		}
		return algorithm;
	}

	public static String getRewriteMethodAsString(String graphName) {
		int rewriteMethod = Helper.getRewriteMethod(graphName);
		return getRewriteMethodAsString(rewriteMethod);
	}
	
	public static String getRewriteMethodAsString(int rewriteMethod) {
		String rewriteMethodString = "";
		if (rewriteMethod == REWRITE_NODE1) {
			rewriteMethodString = "Simple";
		} else if (rewriteMethod == REWRITE_NODE2) {
			rewriteMethodString = "WithoutLiterals";
		} else if (rewriteMethod == REWRITE_NODE3) {
			rewriteMethodString = "UniqueLiterals";
		} else if (rewriteMethod == REWRITE_NODE4) {
			rewriteMethodString = "ContextLiterals";
		} else if (rewriteMethod == REWRITE_PATH) {
			rewriteMethodString = "Path";
		}
		return rewriteMethodString;
	}
	
	public static String getAlgorithmAsString(String graphName) {
		int algorithm = Helper.getAnalysisAlgorithm(graphName);
		return getAlgorithmAsString(algorithm);
	}
	
	public static String getAlgorithmAsString(int algorithmInt) {
		String algorithmString = "";
		if (algorithmInt == ALG_BETWEENNESS) {
			algorithmString = "betweenness";
		} else if (algorithmInt == ALG_EIGENVECTOR) {
			algorithmString = "eigenvector";
		} else if (algorithmInt == ALG_INDEGREE) {
			algorithmString = "indegree";
		} else if (algorithmInt == ALG_OUTDEGREE) {
			algorithmString = "outdegree";
		} else if (algorithmInt == ALG_PAGERANK) {
			algorithmString = "pagerank";
		}
		return algorithmString;
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
	public static String getFileSystemGraphName(String graphName) {
		graphName = graphName.replace("//", "_");
		graphName = graphName.replace(".", "_");
		graphName = graphName.replace(" ", "_");
		return graphName;
	}
	
	public static void writeRow(Collection<String> row, CSVWriter writer) {
		writer.writeNext(row.toArray(new String[row.size()]));
	}
	
	public static void main(String[] args) {
		System.out.println(getFileSystemGraphName("http://graphss.sdf.df"));
	}
}
