package com.d2s.subgraph.eval;

public class Config {
	public static String EXPERIMENT_ENDPOINT = "http://ops.few.vu.nl:8890/sparql";
	
	/**
	 * paths
	 */
	public static String PATH_QUERY_RESULTS = "output/queryResults/";
	public static String PATH_QUERY_TRIPLES = "output/queryTriples/";
	public static String PATH_QUERY_CACHE = "cache/queries/";
	public static String PATH_QUERY_LOGS = "input/queryLogs/";
	public static String PATH_QUERY_CSV_COPY = "cache/queryCsvCopies/";
	public static String PATH_QUERY_CONSTRUCT_TRIPLES = "output/constructTriples/";
	public static String PATH_FAILED_QUERIES = "output/failedQueries/";
	public static String PATH_WEIGHTED_QUERY_TRIPLES = "input/qTripleWeights/";
	public static String PATH_WEIGHT_DISTRIBUTION = "input/weightDistribution/";
	public static String PATH_EVALUATION_OUTPUT = "output/evaluation/";
	public static String PATH_QUERY_FEATURES = "output/queryFeatures/";
	
	
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
	public static String FILE_CSV_QUERY_FEATURES = "queryFeatures.csv";
	public static String FILE_QUERY_MAPPING = "queryMapping.txt";
	public static String FILE_QTRIPLES_REQUIRED = "required";
	public static String FILE_QTRIPLES_OPTIONAL = "optional";
	public static String FILE_QTRIPLES_UNION = "unions";
	
	/**
	 * Sample info
	 */
	public static String[] SAMPLE_SIZES = new String[]{"0.5"};
	
	
}
