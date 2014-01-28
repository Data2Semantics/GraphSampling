package com.d2s.subgraph.eval.experiments;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;


public class ExperimentSetupHelper {
	public static ExperimentSetup get(String setupId) throws IOException, ParserConfigurationException, SAXException {
		ExperimentSetup experimentSetup = null;
		
		if (setupId.equals("lgd")) {
			experimentSetup = new LgdExperimentSetup(true);
		} else if (setupId.equals("dbpedia")) {
			experimentSetup = new DbpediaExperimentSetup(true);
		} else if (setupId.equals("bio2rdf")) {
			experimentSetup = new Bio2RdfExperimentSetup(true);
		} else if (setupId.equals("obm")) {
			experimentSetup = new ObmExperimentSetup(true);
		} else if (setupId.equals("swdf")) {
			experimentSetup = new SwdfExperimentSetup(true);
		} else if (setupId.equals("metalex")) {
			experimentSetup = new MetalexExperimentSetup(true);
		}
		
		System.out.println("fetching experiment setup " + experimentSetup.getId());
		return experimentSetup;
	}
}
