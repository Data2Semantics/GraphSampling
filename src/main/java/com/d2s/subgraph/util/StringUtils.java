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

public class StringUtils {
	public static int REWRITE_RESOURCE_SIMPLE = 0;
	public static int REWRITE_RESOURCE_WITHOUT_LIT = 1;
	public static int REWRITE_RESOURCE_UNIQUE = 2;
	public static int REWRITE_RESOURCE_CONTEXT = 3;
	public static int REWRITE_PATH = 4;

	public static int ALG_EIGENVECTOR = 0;
	public static int ALG_PAGERANK = 1;
	public static int ALG_BETWEENNESS = 2;
	public static int ALG_INDEGREE = 3;
	public static int ALG_OUTDEGREE = 4;
	

	public static String boolAsString(boolean bool) {
		if (bool) {
			return "1";
		} else {
			return "0";
		}
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
		String doubleString = StringUtils.getAsRoundedString(value, 3);
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


	public static int getRewriteMethod(String graphName) {
		int rewriteMethod = -1;
		if (graphName.contains("resourceUnique")) {
			rewriteMethod = REWRITE_RESOURCE_UNIQUE;
		} else if (graphName.contains("resourceWithoutLit")) {
			rewriteMethod = REWRITE_RESOURCE_WITHOUT_LIT;
		} else if (graphName.contains("resourceSimple")) {
			rewriteMethod = REWRITE_RESOURCE_SIMPLE;
		} else if (graphName.contains("resourceContext")) {
			rewriteMethod = REWRITE_RESOURCE_CONTEXT;
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
		int rewriteMethod = StringUtils.getRewriteMethod(graphName);
		return getRewriteMethodAsString(rewriteMethod);
	}
	
	public static String getRewriteMethodAsString(int rewriteMethod) {
		String rewriteMethodString = "";
		if (rewriteMethod == REWRITE_RESOURCE_SIMPLE) {
			rewriteMethodString = "Simple";
		} else if (rewriteMethod == REWRITE_RESOURCE_WITHOUT_LIT) {
			rewriteMethodString = "WithoutLiterals";
		} else if (rewriteMethod == REWRITE_RESOURCE_UNIQUE) {
			rewriteMethodString = "UniqueLiterals";
		} else if (rewriteMethod == REWRITE_RESOURCE_CONTEXT) {
			rewriteMethodString = "ContextLiterals";
		} else if (rewriteMethod == REWRITE_PATH) {
			rewriteMethodString = "Path";
		}
		return rewriteMethodString;
	}
	
	public static String getAlgorithmAsString(String graphName) {
		int algorithm = StringUtils.getAnalysisAlgorithm(graphName);
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
	public static String getFileSystemGraphName(String graphName) {
		graphName = graphName.replace("//", "_");
		graphName = graphName.replace(".", "_");
		graphName = graphName.replace(" ", "_");
		return graphName;
	}
	
}
