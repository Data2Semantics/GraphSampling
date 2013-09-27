package com.d2s.subgraph.util;

import java.io.IOException;
import java.util.Collection;

import au.com.bytecode.opencsv.CSVWriter;

public class Utils {
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
	
	public static void executeCommand(String[] args) throws IOException, InterruptedException {
		ProcessBuilder ps = new ProcessBuilder(args);
		Process pr = ps.start();
		pr.waitFor();
	}
	
	public static void writeRow(Collection<String> row, CSVWriter writer) {
		writer.writeNext(row.toArray(new String[row.size()]));
	}
	
}
