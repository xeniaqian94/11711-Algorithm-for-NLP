package edu.berkeley.nlp.assignments.parsing.student;

import java.util.List;

import edu.berkeley.nlp.assignments.parsing.Parser;
import edu.berkeley.nlp.assignments.parsing.ParserFactory;
import edu.berkeley.nlp.ling.Tree;

public class CoarseToFineParserFactory implements ParserFactory {

	static int h = 2;
	static int v = 2;
	static int maxLength = 41;
	static boolean tagPA = true;
	public static boolean tagSplitting = true;
	static double threshold = -12.0;
//			Double.NEGATIVE_INFINITY;

	public Parser getParser(List<Tree<String>> trainTrees) {

		return new CoarseToFineParser(trainTrees);
	}
}
