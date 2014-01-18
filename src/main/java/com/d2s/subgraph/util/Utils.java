package com.d2s.subgraph.util;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import au.com.bytecode.opencsv.CSVWriter;

public class Utils {
//	public static int REWRITE_RESOURCE_SIMPLE = 0;
//	public static int REWRITE_RESOURCE_WITHOUT_LIT = 1;
//	public static int REWRITE_RESOURCE_UNIQUE = 2;
//	public static int REWRITE_RESOURCE_CONTEXT = 3;
//	public static int REWRITE_PATH = 4;


	public static void executeCommand(String[] args) throws IOException, InterruptedException {
		ProcessBuilder ps = new ProcessBuilder(args);
		Process pr = ps.start();
		pr.waitFor();
	}

	public static void writeRow(Collection<String> row, CSVWriter writer) {
		writer.writeNext(row.toArray(new String[row.size()]));
	}
	public static File mkdir(File file) {
		if (!file.exists()) {
			file.mkdir();
		}
		return file;
	}
	public static File mkdir(String filePath) {
		return mkdir(new File(filePath));
	}

}
