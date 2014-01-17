package com.d2s.subgraph.queries.qtriples;

import java.util.HashSet;
import java.util.Set;

import com.d2s.subgraph.eval.Config;
import com.d2s.subgraph.queries.Query;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;


public class QTriples {
	private Set<Triple> requiredTriples = new HashSet<Triple>();
	private Set<Triple> possibleTriples = new HashSet<Triple>();
	
	public QTriples() {
		
	}
	
	
	public void setPossibleTriple(Triple triple) {
		this.possibleTriples.add(triple);
	}
	public void setRequiredTriple(Triple triple) {
		this.requiredTriples.add(triple);
	}
	public Set<Triple> getPossibleTriples() {
		return this.possibleTriples;
	}
	public Set<Triple> getRequiredTriples() {
		return this.requiredTriples;
	}
	
	public void checkPossibleTriples(String namedGraph, Set<Triple> knownTriples, Set<Triple> knownNotToExist) {
		//first reduce by the triples -already- in our known list
		possibleTriples.removeAll(requiredTriples);
		
		//now reduce by triples we already checked, and do not exist
		possibleTriples.removeAll(knownNotToExist);
		
		
		for (Triple triple: possibleTriples) {
			if (knownTriples.contains(triple)) {
				//no need to check! We already know this one exists
				requiredTriples.add(triple);
			} else if (tripleExists(namedGraph, triple)) {
				requiredTriples.add(triple);
			} else {
				knownNotToExist.add(triple);
			}
		}
	}
	
	private boolean tripleExists(String namedGraph, Triple triple) {
		Query query = new Query();
		query.setQueryAskType();
		ElementTriplesBlock block = new ElementTriplesBlock();
		block.addTriple(triple);
		query.setQueryPattern(block);
		query.addGraphURI(namedGraph);
		QueryExecution queryExecution = QueryExecutionFactory.sparqlService(Config.EXPERIMENT_ENDPOINT, query);
		boolean result = queryExecution.execAsk();
		
//		System.out.println(query.toString());
//		if (result) {
//			System.out.println("triple " + triple.toString() + " exists");
//		} else {
//			System.out.println("triple " + triple.toString() + " !exists");
//		}
		return result;
	}
}
