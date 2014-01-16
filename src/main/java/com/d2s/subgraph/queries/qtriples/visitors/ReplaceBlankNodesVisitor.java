package com.d2s.subgraph.queries.qtriples.visitors;

import java.util.ListIterator;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_Variable;
import com.hp.hpl.jena.graph.Triple;
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

public class ReplaceBlankNodesVisitor implements ElementVisitor {
	private enum TripleLoc {SUB, PRED, OBJ};
	public void visit(ElementTriplesBlock el) {
		// TODO Auto-generated method stub

	}

	public void visit(ElementPathBlock el) {
		ListIterator<TriplePath> it = el.getPattern().iterator();
		
		while (it.hasNext()) {
			TriplePath origTriple = it.next();
			origTriple = replaceBNodeIfNeeded(TripleLoc.SUB, origTriple, it);
			origTriple = replaceBNodeIfNeeded(TripleLoc.PRED, origTriple, it);
			origTriple = replaceBNodeIfNeeded(TripleLoc.OBJ, origTriple, it);
		}
	}
	private TriplePath replaceBNodeIfNeeded(TripleLoc location, TriplePath origTriple, ListIterator<TriplePath> it) {
		TriplePath newPath;
		if (origTriple.isTriple() || location == TripleLoc.PRED) {
			newPath = new TriplePath(new Triple(
					(location == TripleLoc.SUB? getNodeAsBnode(origTriple.getSubject()): origTriple.getSubject()), 
					(location == TripleLoc.PRED? getNodeAsBnode(origTriple.getPredicate()): origTriple.getPredicate()), 
					(location == TripleLoc.OBJ? getNodeAsBnode(origTriple.getObject()): origTriple.getObject()) 
					));
		} else {
			//contains a path
			newPath = new TriplePath(
					(location == TripleLoc.SUB? getNodeAsBnode(origTriple.getSubject()): origTriple.getSubject()), 
					origTriple.getPath(), 
					(location == TripleLoc.OBJ? getNodeAsBnode(origTriple.getObject()): origTriple.getObject()) 
					);
		}
		it.set(newPath);
		return newPath;
	}
	
	
	private Node getNodeAsBnode(Node node) {
		//argh, 'isBlankNode' does not work!!!! it is rewritten to query with two questionmarks.......
		if (node.isVariable() && node.toString().substring(0, 2).equals("??")) {
			return new Node_Variable(node.toString().substring(2));
		}
		return node;
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
		for (Element e : el.getElements()) {
			e.visit(this);
		}
	}

	public void visit(ElementOptional el) {
		el.getOptionalElement().visit(this);
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
}
