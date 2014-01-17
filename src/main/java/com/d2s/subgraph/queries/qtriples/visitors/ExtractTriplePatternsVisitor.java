package com.d2s.subgraph.queries.qtriples.visitors;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;

import com.d2s.subgraph.eval.Config;
import com.d2s.subgraph.queries.Query;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.sparql.core.TriplePath;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementAssign;
import com.hp.hpl.jena.sparql.syntax.ElementBind;
import com.hp.hpl.jena.sparql.syntax.ElementData;
import com.hp.hpl.jena.sparql.syntax.ElementDataset;
import com.hp.hpl.jena.sparql.syntax.ElementExists;
import com.hp.hpl.jena.sparql.syntax.ElementFetch;
import com.hp.hpl.jena.sparql.syntax.ElementFilter;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementMinus;
import com.hp.hpl.jena.sparql.syntax.ElementNamedGraph;
import com.hp.hpl.jena.sparql.syntax.ElementNotExists;
import com.hp.hpl.jena.sparql.syntax.ElementOptional;
import com.hp.hpl.jena.sparql.syntax.ElementPathBlock;
import com.hp.hpl.jena.sparql.syntax.ElementService;
import com.hp.hpl.jena.sparql.syntax.ElementSubQuery;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;
import com.hp.hpl.jena.sparql.syntax.ElementUnion;
import com.hp.hpl.jena.sparql.syntax.ElementVisitor;
import com.hp.hpl.jena.sparql.syntax.TripleCollectorMark;

public class ExtractTriplePatternsVisitor implements ElementVisitor {
	private Set<Triple> requiredTriples = new HashSet<Triple>();
	private LinkedHashMap<Integer, Set<Triple>> optionalTriples = new LinkedHashMap<Integer, Set<Triple>>();
	private LinkedHashMap<String, Set<Triple>> unionTriples = new LinkedHashMap<String, Set<Triple>>();
	
	
	private Set<TripleCollectorMark> knownToExist = new HashSet<TripleCollectorMark>(); 
	private Set<TripleCollectorMark> knownNotToExist = new HashSet<TripleCollectorMark>(); 
	private String namedGraph;
	
	
	private LinkedHashMap<Integer, Integer> unionIterator = new LinkedHashMap<Integer, Integer>();
	
	private int optionalDepth;
	private int unionDepth;
	
	public ExtractTriplePatternsVisitor(String namedGraph) {
		this(namedGraph, null, null);
	}
	
	public ExtractTriplePatternsVisitor(String namedGraph, Set<TripleCollectorMark> knownToExist, Set<TripleCollectorMark> knownNotToExist) {
		this.namedGraph = namedGraph;
		if (knownToExist != null) this.knownToExist = knownToExist;
		if (knownNotToExist != null) this.knownNotToExist = knownNotToExist;
	}
	

	public void visit(ElementTriplesBlock el) {

		if (unionDepth > 0 || optionalDepth > 0) {
			if (blockContainsVar(el) || !checkBlockExists(el)) {
				//no need to fetch triples. we still have variables, i.e. this block is not answered!
				//Or we have our triples, but our ask query return false..
				return;
			}
		} else {
			knownToExist.add(el);
		}
		Iterator<Triple> it = el.getPattern().iterator();
		while (it.hasNext()) {
			fetchFromTriple(it.next());
		}
		
		
	}

	public void visit(ElementPathBlock el) {

		if (unionDepth > 0 || optionalDepth > 0) {
			if (pathBlockContainsVar(el) || !checkPathBlockExists(el)) {
				//no need to fetch triples. we still have variables, i.e. this block is not answered!
				//Or we have our triples, but our ask query return false..
				return;
			}
		} else {
			knownToExist.add(el);
		}
		Iterator<TriplePath> it = el.getPattern().iterator();
		while (it.hasNext()) {
			fetchFromTriple(it.next().asTriple());
		}
		
		
//		ListIterator<TriplePath> it = el.getPattern().iterator();
		
//		while (it.hasNext()) {
//			final TriplePath origTriple = it.next();
//			fetchFromTriple(origTriple.asTriple());
//			
//		}
	}
	
	public void fetchFromTriple(Triple triple) {
		if (triple != null) {
			if (tripleContainsVar(triple)) {
				if (optionalDepth > 0) {
					//this means we don't have an answer for this optional. We can safely ignore this
					//System.out.println("warn: variable found in optional triple pattern. " + triple.toString());
				} else if (unionDepth > 0) {
					//this means we don't have an answer for side of the union. We can safely ignore this
					//System.out.println("warn: variable found in union triple pattern. " + triple.toString());
				} else {
					throw new IllegalArgumentException("we still have a variable in our triple. this should not be the case");
				}
			} else {
				if (optionalDepth > 0) {
					addOptionalTriple(triple);
				} else if (unionDepth > 0) {
					addUnionTriple(triple);
				} else {
					requiredTriples.add(triple);
				}
			}
		} else {
			throw new IllegalArgumentException("we tried to fetch values from triple pattern. However, triple pattern is null!");
		}
	}
	
	private void addOptionalTriple(Triple triple) {
		if (optionalTriples.get(optionalDepth) == null) {
			optionalTriples.put(optionalDepth, new HashSet<Triple>());
		}
		optionalTriples.get(optionalDepth).add(triple);
	}
	private void addUnionTriple(Triple triple) {
		String locationIdentifier = "" + unionDepth + "_" + unionIterator.get(unionDepth);
		if (unionTriples.get(locationIdentifier) == null) {
			unionTriples.put(locationIdentifier, new HashSet<Triple>());
		}
		unionTriples.get(locationIdentifier).add(triple);
	}
	public boolean tripleContainsVar(Triple triple) {
		return (triple.getSubject().isVariable() || triple.getPredicate().isVariable() || triple.getObject().isVariable());
	}
	public boolean tripleContainsVar(TriplePath triple) {
		return (triple.getSubject().isVariable() || triple.getPredicate().isVariable() || triple.getObject().isVariable());
	}
	
	public boolean blockContainsVar(ElementTriplesBlock block) {
		boolean containsVar = false;
		for (Triple triple: block.getPattern().getList()) {
			if (tripleContainsVar(triple)) {
				containsVar = true;
				break;
			}
		}
		
		return containsVar;
	}
	public boolean pathBlockContainsVar(ElementPathBlock el) {
		boolean containsVar = false;
		for (TriplePath triple: el.getPattern().getList()) {
			if (tripleContainsVar(triple)) {
				containsVar = true;
				break;
			}
		}
		
		return containsVar;
	}
	
	public Set<TripleCollectorMark> getKnownNotToExist() {
		return this.knownNotToExist;
	}
	public Set<TripleCollectorMark> getKnownToExist() {
		return this.knownToExist;
	}
	public void visit(ElementFilter el) {
		// TODO Auto-generated method stub

	}

	public void visit(ElementAssign el) {
		// TODO Auto-generated method stub

	}

	public void visit(ElementBind el) {
		// TODO Auto-generated method stub

	}

	public void visit(ElementUnion el) {
		unionDepth++;
		for (Element e : el.getElements()) {
			increaseUnionIterationForDepth(unionDepth);
			e.visit(this);
			
		}
		resetUnionIterationForDepth(unionDepth);
		unionDepth--;
	}
	
	private void increaseUnionIterationForDepth(int depth) {
		if (unionIterator.get(unionDepth) == null) {
			unionIterator.put(unionDepth, 0);
		} else {
			unionIterator.put(unionDepth, unionIterator.get(unionDepth) + 1);
		}
	}
	
	private void resetUnionIterationForDepth(int depth) {
		if (unionIterator.get(unionDepth) == null) {
			//do nothing
		} else {
			unionIterator.remove(unionDepth);
//			unionIterator.put(unionDepth, unionIterator.get(unionDepth) - 1);
		}
	}
	
	
	public void visit(ElementOptional el) {
		optionalDepth++;
		el.getOptionalElement().visit(this);
		optionalDepth--;
	}

	public void visit(ElementGroup el) {
		for (Element e : el.getElements()) {
			e.visit(this);
		}
	}

	public void visit(ElementDataset el) {
		// TODO Auto-generated method stub

	}

	public void visit(ElementNamedGraph el) {
		el.getElement().visit(this);
	}

	public void visit(ElementExists el) {
		// TODO Auto-generated method stub

	}

	public void visit(ElementNotExists el) {
		// TODO Auto-generated method stub

	}

	public void visit(ElementMinus el) {
		// TODO Auto-generated method stub

	}

	public void visit(ElementService el) {
		// TODO Auto-generated method stub

	}

	public void visit(ElementFetch el) {
		// TODO Auto-generated method stub

	}

	public void visit(ElementSubQuery el) {
		// TODO Auto-generated method stub

	}

	public void visit(ElementData el) {
		// TODO Auto-generated method stub

	}
	
	
//	public void checkPossibleTriples(String namedGraph) {
//		//first reduce by the triples -already- in our known list
//		possibleTriples.removeAll(requiredTriples);
//		
//		//now reduce by triples we already checked, and do not exist
//		possibleTriples.removeAll(knownNotToExist);
//		
//		
//		for (Triple triple: possibleTriples) {
//			if (knownToExist.contains(triple)) {
//				//no need to check! We already know this one exists
//				requiredTriples.add(triple);
//			} else if (checkTripleExists(triple)) {
//				requiredTriples.add(triple);
//			} else {
//				knownNotToExist.add(triple);
//			}
//		}
//	}
	
	private boolean checkPathBlockExists(ElementPathBlock el) {
		if (knownToExist.contains(el)) {
			return true;
		} else if (knownNotToExist.contains(el)) {
			return false;
		}
		Query query = new Query();
		query.setQueryAskType();
//		ElementTriplesBlock block = new ElementTriplesBlock();
//		block.addTriple(triple);
		query.setQueryPattern(el);
		query.addGraphURI(namedGraph);
		QueryExecution queryExecution = QueryExecutionFactory.sparqlService(Config.EXPERIMENT_ENDPOINT, query);
		boolean exists = queryExecution.execAsk();
		
		if (exists) {
			knownToExist.add(el);
		} else {
			knownNotToExist.add(el);
		}
		return exists;
	}
	private boolean checkBlockExists(ElementTriplesBlock el) {
		if (knownToExist.contains(el)) {
			return true;
		} else if (knownNotToExist.contains(el)) {
			return false;
		}
		Query query = new Query();
		query.setQueryAskType();
//		ElementTriplesBlock block = new ElementTriplesBlock();
//		block.addTriple(triple);
		query.setQueryPattern(el);
		query.addGraphURI(namedGraph);
		QueryExecution queryExecution = QueryExecutionFactory.sparqlService(Config.EXPERIMENT_ENDPOINT, query);
		boolean exists = queryExecution.execAsk();
		
		if (exists) {
			knownToExist.add(el);
		} else {
			knownNotToExist.add(el);
		}
		return exists;
	}
//	private boolean checkPathExists(ElementTriplesBlock block) {
//		if (knownToExist.contains(block)) {
//			return true;
//		} else if (knownNotToExist.contains(block)) {
//			return false;
//		}
//		Query query = new Query();
//		query.setQueryAskType();
////		ElementTriplesBlock block = new ElementTriplesBlock();
////		block.addTriple(triple);
//		query.setQueryPattern(block);
//		query.addGraphURI(namedGraph);
//		QueryExecution queryExecution = QueryExecutionFactory.sparqlService(Config.EXPERIMENT_ENDPOINT, query);
//		boolean exists = queryExecution.execAsk();
//		
//		if (exists) {
//			knownToExist.add(block);
//		} else {
//			knownNotToExist.add(block);
//		}
//		return exists;
//	}
	
	
//	private boolean checkTripleExists(Triple triple) {
//		Query query = new Query();
//		query.setQueryAskType();
//		ElementTriplesBlock block = new ElementTriplesBlock();
//		block.addTriple(triple);
//		query.setQueryPattern(block);
//		query.addGraphURI(namedGraph);
//		QueryExecution queryExecution = QueryExecutionFactory.sparqlService(Config.EXPERIMENT_ENDPOINT, query);
//		boolean result = queryExecution.execAsk();
//		
//		return result;
//	}
	public Set<Triple> getRequiredTriples() {
		return this.requiredTriples;
	}
	public LinkedHashMap<Integer, Set<Triple>> getOptionalTriples() {
		return this.optionalTriples;
	}
	public LinkedHashMap<String, Set<Triple>> getUnionTriples() {
		return this.unionTriples;
	}
}
