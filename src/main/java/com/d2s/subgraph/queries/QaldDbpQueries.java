package com.d2s.subgraph.queries;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.data2semantics.query.QueryCollection;
import org.data2semantics.query.filters.QueryFilter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.d2s.subgraph.eval.Config;
import com.d2s.subgraph.eval.experiments.DbpoExperimentSetup;
import com.d2s.subgraph.util.QueryUtils;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryParseException;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.query.ResultSetRewindable;


public class QaldDbpQueries extends QueriesFetcher {
	public static String QALD_1_QUERIES = "src/main/resources/qald1-dbpedia-train.xml";
	public static String QALD_2_QUERIES = "src/main/resources/qald2-dbpedia-train.xml";
	public static String QALD_3_QUERIES = "src/main/resources/qald3-dbpedia-train.xml";
	private static String IGNORE_QUERY_STRING = "OUT OF SCOPE";
	private boolean removeStringProjVar;
	private boolean onlyDbo;
	
	public QaldDbpQueries(String xmlFile, boolean removeStringProjVar, boolean onlyDbo, QueryFilter... filters) throws SAXException, IOException, ParserConfigurationException {
		super();
		this.filters = new ArrayList<QueryFilter>(Arrays.asList(filters));
		this.removeStringProjVar = removeStringProjVar;
		this.onlyDbo = onlyDbo;
		parseXml(new File(xmlFile));
		eraseEmptyresultQueries();
	}

	public QaldDbpQueries(String xmlFile, boolean removeStringProjVar) throws SAXException, IOException, ParserConfigurationException {
		this(xmlFile, removeStringProjVar, false);
	}
	public QaldDbpQueries(String xmlFile, boolean removeStringProjVar, boolean onlyDbo) throws SAXException, IOException, ParserConfigurationException {
		this(xmlFile, removeStringProjVar, onlyDbo, new QueryFilter[]{});
	}
	
	private void eraseEmptyresultQueries() throws IOException {
		int emptyQueries = 0;
		int failedQueries = 0;
		QueryCollection<Query> validQueryCollection = new QueryCollection<Query>();
		for (Query query: queryCollection.getQueries()) {
			try {
				Query goldenStandardQuery = (Query) query.getQueryStringWithFromClause(DbpoExperimentSetup.GOLDEN_STANDARD_GRAPH);
				QueryExecution queryExecution = QueryExecutionFactory.sparqlService(Config.EXPERIMENT_ENDPOINT, goldenStandardQuery);
				ResultSetRewindable result = ResultSetFactory.copyResults(queryExecution.execSelect());
				if (QueryUtils.getResultSize(result) > 0) {
					validQueryCollection.addQuery(query);
				} else {
					emptyQueries++;
				}
			} catch (Exception e) {
				// failed to execute. endpoint down, or incorrect query
				failedQueries++;
			}
		}
		System.out.println("ignored " + (emptyQueries + failedQueries) + " queries from xml. Empty queries: " + emptyQueries + ", failed queries: " + failedQueries);
		queryCollection = validQueryCollection;
	}
	private void parseXml(File xmlFile) throws SAXException, IOException, ParserConfigurationException {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(xmlFile);
	 
		//read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
		doc.getDocumentElement().normalize();
	 
	 
		NodeList nList = doc.getElementsByTagName("question");
		int failedExtractQueries = 0;
		for (int i = 0; i < nList.getLength(); i++) {
			try {
				doMainLoop(nList.item(i));
			} catch (QueryParseException e) {
				//ok, so we failed to parse a query. lets just ignore for now
				failedExtractQueries++;
			}
		}
		if (failedExtractQueries > 0) {
			System.out.println("Failed to extract " + failedExtractQueries + " queries from xml. Jena could not parse them");
		}
		
	}
	
	private void doMainLoop(Node nNode) {
		if (nNode.getNodeType() == Node.ELEMENT_NODE) {
			NamedNodeMap map = nNode.getAttributes();
			boolean onlyDboVal = false;
			Node onlyDbo = map.getNamedItem("onlydbo");
			if (onlyDbo != null) onlyDboVal = onlyDbo.getTextContent().trim().equals("true");
			
			storeQuery(onlyDboVal, (Element) nNode);
		}
	}
	
	private void storeQuery(boolean onlyDbo, Element element) {
		Node queryNode = element.getElementsByTagName("query").item(0);
		if (queryNode != null && queryNode.getTextContent().trim().length() > 0 && !queryNode.getTextContent().trim().equals(IGNORE_QUERY_STRING)) {
			Query evalQuery;
			try {
				evalQuery = Query.create(queryNode.getTextContent(), queryCollection);
				if (removeStringProjVar) {
					System.out.println("WATCH OUT!!!!!!!!!!!! Removing a project var!!!");
					evalQuery = QueryUtils.removeProjectVarFromQuery(evalQuery, "string");
				}
				evalQuery.setAnswers(getAnswersList(element));
				filterAndStoreQuery(evalQuery);
			} catch (Exception e) {
				invalidQueries++;
				return;
			}
		}
	}
	
	protected void filterAndStoreQuery(Query query) {
		//apply custom filter as well, where we filter out the onlydbo queries
		if (checkFilters(query) || (onlyDbo && !query.isOnlyDbo())) {
			queryCollection.addQuery(query);
		} else {
			filteredQueries++;
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
	
	public static void main(String[] args)  {
		
		try {
			
			QaldDbpQueries qaldQueries = new QaldDbpQueries(QALD_2_QUERIES, true);
			System.out.println("number of stored queries: " + qaldQueries.getQueryCollection().getDistinctQueryCount());
//			for (Query query: qaldQueries.getQueryCollection().getQueries()) {
//				System.out.println(Integer.toString(query.getQueryId()));
//			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
