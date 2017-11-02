package edu.berkeley.nlp.assignments.parsing.student;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.berkeley.nlp.assignments.parsing.Grammar;
import edu.berkeley.nlp.assignments.parsing.Parser;
import edu.berkeley.nlp.assignments.parsing.ParserFactory;
import edu.berkeley.nlp.assignments.parsing.SimpleLexicon;
import edu.berkeley.nlp.assignments.parsing.TreeAnnotations;
import edu.berkeley.nlp.assignments.parsing.UnaryClosure;
import edu.berkeley.nlp.ling.Tree;
import edu.berkeley.nlp.ling.Trees;
import edu.berkeley.nlp.util.CounterMap;

public class GenerativeParserFactory implements ParserFactory {
	int h = Integer.MAX_VALUE;
	int v = 1;

	public GenerativeParserFactory(int h, int v) {
		this.h = h;
		this.v = v;
	}

	public GenerativeParserFactory() {
	}

	public Parser getParser(List<Tree<String>> trainTrees) {
		return new GenerativeParser(trainTrees, h, v, 41);
	}

//	public Parser getParser(List<Tree<String>> trainTrees, boolean sanity) {
//		return new GenerativeParser(trainTrees, h, v, 41, sanity);
//	}
}
