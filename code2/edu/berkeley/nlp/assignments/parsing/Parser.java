package edu.berkeley.nlp.assignments.parsing;

import java.util.List;

import edu.berkeley.nlp.ling.Tree;

/**
 * Parsers are required to map sentences to trees. How a parser is constructed
 * and trained is not specified.
 */
public interface Parser {
	Tree<String> getBestParse(List<String> sentence);
}