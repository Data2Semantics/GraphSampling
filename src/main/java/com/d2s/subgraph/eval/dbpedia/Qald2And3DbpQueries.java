package com.d2s.subgraph.eval.dbpedia;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.d2s.subgraph.eval.EvalQuery;
import com.d2s.subgraph.eval.GetQueries;


public class Qald2And3DbpQueries implements GetQueries {
	ArrayList<EvalQuery> queries = new ArrayList<EvalQuery>();
	private static String QALD_2_QUERIES = "src/main/resources/qald2-dbpedia-train.xml";
	private static String QALD_3_QUERIES = "src/main/resources/qald3-dbpedia-train.xml";
	public Qald2And3DbpQueries() throws Exception {
		parseXml(new File(QALD_2_QUERIES));
	}
	
	
	public void parseXml(File xmlFile) throws SAXException, IOException, ParserConfigurationException {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(xmlFile);
	 
		//read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
		doc.getDocumentElement().normalize();
	 
	 
		NodeList nList = doc.getElementsByTagName("question");
		
		for (int i = 0; i < nList.getLength(); i++) {
			Node nNode = nList.item(i);
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
				storeQuery((Element) nNode);
				
				
			}
		}
		
		
	}
	
	private void storeQuery(Element element) {
		Node queryNode = element.getElementsByTagName("query").item(0);
		if (queryNode != null) {
			EvalQuery evalQuery = new EvalQuery(queryNode.getTextContent());
			evalQuery.setAnswers(getAnswersList(element));
			queries.add(evalQuery);
		}
	}
	
	private Element getNodeAsElement(Node node) {
		Element result = null;
		if (node != null && node.getNodeType() == Node.ELEMENT_NODE) {
			result = (Element) node;
		}
		return result;
	}
	
	private ArrayList<HashMap<String, String>> getAnswersList(Element element) {
		ArrayList<HashMap<String,String>> answerList = new ArrayList<HashMap<String,String>>();
		
		Element answersList = getNodeAsElement(element.getElementsByTagName("answers").item(0));
		if (answersList != null) {
			NodeList childNodes = answersList.getElementsByTagName("answer");
			for (int i = 0; i < childNodes.getLength(); i++) {//"answer"
				Element answer = getNodeAsElement(childNodes.item(i));
				answerList.add(getAnswers(answer));
			}
		}
		return answerList;
	}
	
	private HashMap<String,String> getAnswers(Element answerElement) {
		HashMap<String,String> answers = new HashMap<String,String>();
		NodeList answerSubElements = answerElement.getChildNodes();
		for (int i = 0; i < answerSubElements.getLength(); i++) {
			Element answer = getNodeAsElement(answerSubElements.item(i));
			if (answer != null) {
				String var = answer.getTagName();
				String answerString = answer.getTextContent().trim();
				answers.put(var, answerString);
			}
		}
		return answers;
	}

	public ArrayList<EvalQuery> getQueries() {
		return this.queries;
	}
	
	
	public static void main(String[] args)  {
		
		try {
			Qald2And3DbpQueries getQueries = new Qald2And3DbpQueries();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
