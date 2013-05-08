package com.d2s.subgraph.queries;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import au.com.bytecode.opencsv.CSVWriter;
import com.d2s.subgraph.eval.EvaluateGraph;
import com.d2s.subgraph.eval.experiments.DbpoExperimentSetup;
import com.d2s.subgraph.helpers.Helper;
import com.d2s.subgraph.queries.filters.QueryFilter;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QueryParseException;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.query.ResultSetRewindable;


public class QaldDbpQueries implements GetQueries {
	public static String QALD_1_QUERIES = "src/main/resources/qald1-dbpedia-train.xml";
	public static String QALD_2_QUERIES = "src/main/resources/qald2-dbpedia-train.xml";
	public static String QALD_3_QUERIES = "src/main/resources/qald3-dbpedia-train.xml";
	private static String IGNORE_QUERY_STRING = "OUT OF SCOPE";
	ArrayList<QueryWrapper> queries = new ArrayList<QueryWrapper>();
	private int maxNumQueries = 0;
	private QueryFilter[] filters;
	private int filteredQueries = 0;
	private int validQueries = 0;
	private int invalidQueries = 0;
	private boolean removeStringProjVar;
	
	
	public QaldDbpQueries(String xmlFile, boolean removeStringProjVar, QueryFilter... filters) throws SAXException, IOException, ParserConfigurationException {
		this.filters = filters;
		this.removeStringProjVar = removeStringProjVar;
		parseXml(new File(xmlFile));
		eraseEmptyresultQueries();
	}

	public QaldDbpQueries(String xmlFile, boolean removeStringProjVar) throws SAXException, IOException, ParserConfigurationException {
		this(xmlFile, removeStringProjVar, new QueryFilter[]{});
	}
	
	private void eraseEmptyresultQueries() {
		ArrayList<QueryWrapper> validQueriesList = new ArrayList<QueryWrapper>();
		int emptyQueries = 0;
		int failedQueries = 0;
		for (QueryWrapper queryWrapper: queries) {
			try {
				Query query = QueryFactory.create(queryWrapper.getQueryString(DbpoExperimentSetup.GOLDEN_STANDARD_GRAPH));
				QueryExecution queryExecution = QueryExecutionFactory.sparqlService(EvaluateGraph.OPS_VIRTUOSO, query);
				ResultSetRewindable result = ResultSetFactory.copyResults(queryExecution.execSelect());
				if (Helper.getResultSize(result) > 0) {
					validQueriesList.add(queryWrapper);
				} else {
					//System.out.println(queryWrapper.getQueryString(DbpoExperimentSetup.GOLDEN_STANDARD_GRAPH));
					emptyQueries++;
				}
			} catch (Exception e) {
				// failed to execute. endpoint down, or incorrect query
				failedQueries++;
			}
		}
		System.out.println("ignored " + (emptyQueries + failedQueries) + " queries from xml. Empty queries: " + emptyQueries + ", failed queries: " + failedQueries);
		queries = validQueriesList;
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
			QueryWrapper evalQuery = new QueryWrapper();
			NamedNodeMap map = nNode.getAttributes();
			
			
			Node answerType = map.getNamedItem("answertype");
			if (answerType != null) evalQuery.setAnswerType(answerType.getTextContent().trim());
			
			Node id = map.getNamedItem("id");
			if (id != null) evalQuery.setQueryId(Integer.parseInt(id.getTextContent().trim()));
			
			Node onlyDbo = map.getNamedItem("onlydbo");
			if (onlyDbo != null) evalQuery.setOnlyDbo(onlyDbo.getTextContent().trim().equals("true"));
			storeQuery(evalQuery, (Element) nNode);
		}
	}
	
	private void storeQuery(QueryWrapper evalQuery, Element element) {
		Node queryNode = element.getElementsByTagName("query").item(0);
		if (queryNode != null && queryNode.getTextContent().trim().length() > 0 && !queryNode.getTextContent().trim().equals(IGNORE_QUERY_STRING)) {
			try {
				evalQuery.setQuery(queryNode.getTextContent());
				if (removeStringProjVar) {
					System.out.println("WATCH OUT!!!!!!!!!!!! Removing a project var!!!");
					evalQuery.removeProjectVar("string");
				}
			} catch (Exception e) {
				invalidQueries++;
				return;
			}
			evalQuery.setAnswers(getAnswersList(element));
			filterAndStoreQuery(evalQuery);
		}
	}
	
	private void filterAndStoreQuery(QueryWrapper query) {
		if (checkFilters(query)) {
			validQueries++;
			queries.add(query);
			query.generateStats();
		} else {
			filteredQueries++;
		}
			
	}

	/**
	 * 
	 * @param query
	 * @return True if this query passed through all the filters, false if one of the filters matches
	 */
	private boolean checkFilters(QueryWrapper query) {
		boolean passed = true;
		try {
			for (QueryFilter filter: filters) {
				if (filter.filter(query)) {
					passed = false;
					break;
				}
			}
		} catch (Exception e) {
			System.out.println(query.toString());
			e.printStackTrace();
			System.exit(1);
		}
		return passed;
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
	public void setMaxNQueries(int maxNum) {
		this.maxNumQueries  = maxNum;
	}

	public ArrayList<QueryWrapper> getQueries() {
		if (maxNumQueries > 0) {
			maxNumQueries = Math.min(maxNumQueries,  queries.size());
			return new ArrayList<QueryWrapper>(this.queries.subList(0, maxNumQueries));
		} else {
			return this.queries;
		}
	}
	
	public String toString() {
		return "valids: " + validQueries + " invalids: " + invalidQueries + " filtered: " + filteredQueries ;
	}
	
	public void saveCsvCopy(File csvFile) throws IOException {
		CSVWriter writer = new CSVWriter(new FileWriter(csvFile), ';');
		writer.writeNext(new String[] { "queryId", "query" });
		for (QueryWrapper query : queries) {
			writer.writeNext(new String[] { Integer.toString(query.getQueryId()), query.toString() });
		}
		writer.close();
	}
	
	
	public static void main(String[] args)  {
		
		try {
			
			QaldDbpQueries qaldQueries = new QaldDbpQueries(QALD_2_QUERIES, true);
			ArrayList<QueryWrapper> queries = qaldQueries.getQueries();
			for (QueryWrapper query: queries) {
				System.out.println(Integer.toString(query.getQueryId()));
			}
			
//			qaldQueries = new QaldDbpQueries(QALD_3_QUERIES);
//			queries = qaldQueries.getQueries();
//			try  
//			{
//			    FileWriter fstream = new FileWriter("qald3.txt", true); //true tells to append data.
//			    BufferedWriter out = new BufferedWriter(fstream);
//				for (QueryWrapper query: queries) {
//					    out.write("\n" + query.getQuery());
//					}
//				out.close();
//					
//				}
//			catch (Exception e)
//			{
//			    System.err.println("Error: " + e.getMessage());
//			}
			
//			System.out.println(qaldQueries.getQueries().size());
		} catch (Exception e) {
			System.out.println("bla");
			e.printStackTrace();
		}
	}
}
