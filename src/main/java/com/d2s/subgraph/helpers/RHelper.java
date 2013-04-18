package com.d2s.subgraph.helpers;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.rosuda.JRI.Rengine;

public class RHelper {
	private static String TEMP_DIR = ".tmp";
	private static String FILE_POSTFIX = "rScript.R";
	File tempDir;

	public RHelper() {
		tempDir = new File(TEMP_DIR);
		if (!tempDir.exists()) {
			tempDir.mkdir();
		}
	}

	private void execute(String script) throws IOException, InterruptedException {
		File scriptFile = storeScriptInFile(script);
		executeScript(scriptFile);
	}

	private File storeScriptInFile(String script) throws IOException {
		File file = new File(tempDir.getAbsolutePath() + "/" + RandomStringUtils.random(5, "abcdefghijklmnopqrstuvwxyz") + FILE_POSTFIX);
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

	public static Rengine getEngine() throws InstantiationException {
		Rengine re = new Rengine(new String[] { "--vanilla" }, false, null);
		if (!re.waitForR()) {
			throw new InstantiationException("Unable to start R Engine");
		}
		return re;
	}

	public static void main(String[] args) throws IOException, InterruptedException {
		RHelper rHelper = new RHelper();
		rHelper.plotRecallBoxPlots(new File("swdfResults/flatlist.csv"), new File("swdfResults/boxplots.pdf"));

	}
}
