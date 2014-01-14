package com.d2s.subgraph.querytriples;

import java.util.HashSet;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Set;

import com.hp.hpl.jena.graph.Node;
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

public class ExtractQueryVariablesVisitor implements ElementVisitor {
	Set<String> variables = new HashSet<String>();
	private int unionDepth = 0;
	private int optionalDepth = 0;
	
	public Set<String> getVariables() {
		return this.variables;
	}

	public void visit(ElementTriplesBlock el) {
		Iterator<Triple> it = el.getPattern().iterator();
		while (it.hasNext()) {
			Triple triple = it.next();
			addVarFromNode(triple.getSubject());
			addVarFromNode(triple.getPredicate());
			addVarFromNode(triple.getObject());
		}
	}

	public void visit(ElementPathBlock el) {
		ListIterator<TriplePath> it = el.getPattern().iterator();
		while (it.hasNext()) {
			final TriplePath triple = it.next();
			addVarFromNode(triple.getSubject());
			addVarFromNode(triple.getPredicate());
			addVarFromNode(triple.getObject());
		}
	}
	
	public void addVarFromNode(Node node) {
		if (node.isVariable()) variables.add(node.getName());
	}
	
	
	public boolean tripleContainsVar(Triple triple) {
		return (triple.getSubject().isVariable() || triple.getPredicate().isVariable() || triple.getObject().isVariable());
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
			e.visit(this);
		}
		unionDepth--;
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

}
