package edu.berkeley.nlp.assignments.parsing;

import java.util.List;

import edu.berkeley.nlp.ling.Tree;

public interface ParserFactory {
	Parser getParser(List<Tree<String>> trainTrees);
}
