package com.d2s.subgraph.eval.qtriples;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.openrdf.query.algebra.Exists;
import org.xml.sax.SAXException;

import com.d2s.subgraph.eval.Config;
import com.d2s.subgraph.eval.experiments.ExperimentSetup;
import com.d2s.subgraph.eval.experiments.SwdfExperimentSetup;
import com.d2s.subgraph.queries.Query;

public class CalcRecall {
	
	private ExperimentSetup experimentSetup;
	private HashMap<String, Double> cutoffWeights = new HashMap<String, Double>();

	
	public CalcRecall(ExperimentSetup experimentSetup, double maxSampleSize) throws IOException {
		this.experimentSetup = experimentSetup;
		cutoffWeights = CalcCutoffWeight.get(experimentSetup, maxSampleSize);
		if (cutoffWeights.size() == 0) throw new IllegalStateException("Could not find any cutoff weights. unable to calc recall");
	}
	
	private void calcRecallForSamples() throws IOException {
		for (String sample: cutoffWeights.keySet()) {
			System.out.println("calculating recall for dataset " + experimentSetup.getId() + " and sample " + sample);
			calcForQueries(sample);
		}
	}
	
	private void calcForQueries(String sample) throws IOException {
		File queryTripleDir = new File(Config.PATH_QUERY_TRIPLES + experimentSetup.getId());
		Collection<File> queryDirs = FileUtils.listFiles(queryTripleDir, DirectoryFileFilter.DIRECTORY, null);
		if (queryDirs.size() == 0) throw new IllegalStateException("could not find queries to calc recall for. Searching in dir " + queryTripleDir.getPath());
		for (File queryDir: queryDirs) {
			calcForQuery(sample, queryDir);
		}
	}
	
	private void calcForQuery(String sample, File queryDir) throws IOException {
		File queryFile = new File(queryDir.getPath() + "/query.txt");
		if (!queryFile.exists()) throw new IOException("tried to locate " + queryFile.getPath() + ", but it isnt there. Unable to calc recall");
		
		Query query = Query.create(FileUtils.readFileToString(queryFile));
		
		
	}

	public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {
		CalcRecall calc = new CalcRecall(new SwdfExperimentSetup(true, true), 0.5);
		calc.calcRecallForSamples();
	}
}
