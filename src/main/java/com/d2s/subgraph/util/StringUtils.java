package com.d2s.subgraph.util;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class StringUtils {
	public enum RewriteMethods{RESOURCE_SIMPLE, RESOURCE_WITHOUT_LIT, RESOURCE_UNIQUE, RESOURCE_CONTEXT, PATH}
	public enum Algorithms{EIGENVECTOR, PAGERANK, BETWEENNESS, INDEGREE, OUTDEGREE}

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


	public static RewriteMethods getRewriteMethod(String graphName) {
		RewriteMethods rewriteMethod;
		if (graphName.contains("resourceUnique")) {
			rewriteMethod = RewriteMethods.RESOURCE_UNIQUE;
		} else if (graphName.contains("resourceWithoutLit")) {
			rewriteMethod = RewriteMethods.RESOURCE_WITHOUT_LIT;
		} else if (graphName.contains("resourceSimple")) {
			rewriteMethod = RewriteMethods.RESOURCE_SIMPLE;
		} else if (graphName.contains("resourceContext")) {
			rewriteMethod = RewriteMethods.RESOURCE_CONTEXT;
		} else if (graphName.contains("so-so")) {
			rewriteMethod = RewriteMethods.PATH;
		} else {
			throw new IllegalStateException("unable to detect rewrite method based on " + graphName);
		}
		return rewriteMethod;
	}
	
	public static Algorithms getAnalysisAlgorithm(String graphName) throws IllegalStateException {
		Algorithms alg;
		if (graphName.contains("eigenvector")) {
			alg = Algorithms.EIGENVECTOR;
		} else if (graphName.contains("pagerank")) {
			alg = Algorithms.PAGERANK;
		} else if (graphName.contains("betweenness")) {
			alg = Algorithms.BETWEENNESS;
		} else if (graphName.contains("indegree")) {
			alg = Algorithms.INDEGREE;
		} else if (graphName.contains("outdegree")) {
			alg = Algorithms.OUTDEGREE;
		} else {
			throw new IllegalStateException("could not detect algorithm based on " + graphName);
		}
		return alg;
	}

	public static String getRewriteMethodAsString(String graphName) {
		RewriteMethods rewriteMethod = StringUtils.getRewriteMethod(graphName);
		return getRewriteMethodAsString(rewriteMethod);
	}
	
	public static String getRewriteMethodAsString(RewriteMethods rewriteMethod) {
		String rewriteMethodString = "";
		if (rewriteMethod == RewriteMethods.RESOURCE_SIMPLE) {
			rewriteMethodString = "Simple";
		} else if (rewriteMethod == RewriteMethods.RESOURCE_WITHOUT_LIT) {
			rewriteMethodString = "WithoutLiterals";
		} else if (rewriteMethod == RewriteMethods.RESOURCE_UNIQUE) {
			rewriteMethodString = "UniqueLiterals";
		} else if (rewriteMethod == RewriteMethods.RESOURCE_UNIQUE) {
			rewriteMethodString = "ContextLiterals";
		} else if (rewriteMethod == RewriteMethods.PATH) {
			rewriteMethodString = "Path";
		}
		return rewriteMethodString;
	}
	
	public static String getAlgorithmAsString(String graphName) {
		Algorithms algorithm = StringUtils.getAnalysisAlgorithm(graphName);
		return getAlgorithmAsString(algorithm);
	}
	
	public static String getAlgorithmAsString(Algorithms alg) {
		String algorithmString = "";
		if (alg == Algorithms.BETWEENNESS) {
			algorithmString = "betweenness";
		} else if (alg == Algorithms.EIGENVECTOR) {
			algorithmString = "eigenvector";
		} else if (alg == Algorithms.INDEGREE) {
			algorithmString = "indegree";
		} else if (alg == Algorithms.OUTDEGREE) {
			algorithmString = "outdegree";
		} else if (alg == Algorithms.PAGERANK) {
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
