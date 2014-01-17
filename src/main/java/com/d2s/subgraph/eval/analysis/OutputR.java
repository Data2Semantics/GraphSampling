package com.d2s.subgraph.eval.analysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.data2semantics.query.QueryCollection;
import org.rosuda.JRI.Rengine;

import com.d2s.subgraph.eval.experiments.ExperimentSetup;
import com.d2s.subgraph.eval.results.SampleResults;
import com.d2s.subgraph.queries.Query;

public class OutputR extends OutputWrapper {
	private static String TEMP_DIR = ".tmp";
	private static String TMP_FILE_POSTFIX = "rScript.R";
	private static String SCRIPT_DRAW_PLOTS = "src/main/resources/rScripts/drawPlots.R";
	File tempDir;
	
	public OutputR(ExperimentSetup experimentSetup, ArrayList<SampleResults> allGraphResults, QueryCollection<Query> queryCollection, File resultsDir) {
		super(experimentSetup, allGraphResults, queryCollection, resultsDir);
		tempDir = new File(TEMP_DIR);
		if (!tempDir.exists()) {
			tempDir.mkdir();
		}
	}
	private void execute(String script) throws IOException, InterruptedException {
		execute(script, null);
	}
	
	private void execute(String script, File fileToAppendFrom) throws IOException, InterruptedException {
		if (fileToAppendFrom != null) {
			if (fileToAppendFrom.exists()) {
				script += FileUtils.readFileToString(fileToAppendFrom);
				File scriptFile = storeScriptInFile(script);
				executeScript(scriptFile);
			} else {
				System.out.println("ERROR! could not find r script to read from. tried: " + fileToAppendFrom.getAbsolutePath());
			}
		}
	}

	private File storeScriptInFile(String script) throws IOException {
		File file = new File(tempDir.getAbsolutePath() + "/" + RandomStringUtils.random(5, "abcdefghijklmnopqrstuvwxyz") + TMP_FILE_POSTFIX);
		FileUtils.writeStringToFile(file, script);
		return file;
	}

	private void executeScript(File scriptFile) throws IOException, InterruptedException {
		final String[] args = { "R", "-f", scriptFile.getAbsolutePath() };

		ProcessBuilder ps = new ProcessBuilder(args);
		ps.redirectErrorStream(true);
		Process pr = ps.start();
		BufferedReader in = new BufferedReader(new InputStreamReader(pr.getInputStream()));
		pr.waitFor();
		in.close();
	}

	public void plotRecallBoxPlots(File inputFile, File toFile) throws IOException, InterruptedException {
		String directory = inputFile.getParentFile().getAbsolutePath();
		String rScript = "setwd(\"" + directory + "\")\n";
		rScript += "input <- read.csv(\"" + inputFile.getName() + "\", header=TRUE, sep=\";\", quote=\"\\\"\")\n";
		rScript += "pdf(file=\"" + toFile.getAbsolutePath() + "\")\n";
		rScript += "par(omd=c(0,1,0.4,1))#increase bottom spacing for angled labels\n";
		rScript += "boxplot(recall~graph, data=input, main=\"Recall per graph\", xlab=\"Graphs\", ylab=\"Recall\", las=2);\n";
		rScript += "dev.off()\n";
		execute(rScript);
	}
	
	
	public void drawPlots() throws IOException, InterruptedException {
		System.out.println("drawing all R plots");
		String rScript = "setwd(\"" + resultsDir.getAbsolutePath() + "\")\n";
		File drawPlotsFile =  new File(SCRIPT_DRAW_PLOTS);
		execute(rScript, drawPlotsFile);
		FileUtils.copyFile(drawPlotsFile, new File(resultsDir.getAbsolutePath() +  "/" + drawPlotsFile.getName()));
	}
	
	

	public static Rengine getEngine() throws InstantiationException {
		Rengine re = new Rengine(new String[] { "--vanilla" }, false, null);
		if (!re.waitForR()) {
			throw new InstantiationException("Unable to start R Engine");
		}
		return re;
	}
	
}
