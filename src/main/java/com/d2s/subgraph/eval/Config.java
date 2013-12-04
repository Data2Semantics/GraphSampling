package com.d2s.subgraph.eval;

public class Config {
	public static String EXPERIMENT_ENDPOINT = "http://ops.few.vu.nl:8890/sparql";
	
	/**
	 * paths
	 */
	public static String PATH_QUERY_RESULTS = "queryResults";
	
	/**
	 * file names
	 */
	public static String FILE_CSV_SUMMARY = "summary.csv";
	public static String FILE_CSV_QUERY_SUMMARY = "querySummary.csv";
	public static String FILE_HTML_SUMMARY = "results.html";
	public static String FILE_CSV_FULL_LIST = "list.csv";
	public static String FILE_CSV_FLAT_FULL_LIST = "flatlist.csv";
	public static String FILE_CSV_REWR_VS_ALGS = "rewrVsAlgs.csv";
	public static String FILE_CSV_AVG_RECALL_PER_QUERY = "avgRecallPerQuery.csv";
	public static String FILE_CSV_BEST_RECALL_PER_ALG = "bestRecallPerAlgorithm.csv";
	
	
	/**
	 * Sample info
	 */
	public static String[] SAMPLE_SIZES = new String[]{"0.5"};
	
	/**
	 * Time queries? (for benchmarking purposes)
	 */
	public static boolean TIME_QUERIES = true;
}
