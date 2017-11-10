package edu.berkeley.nlp.assignments.rerank.student;

import java.util.List;

import edu.berkeley.nlp.assignments.rerank.KbestList;
import edu.berkeley.nlp.assignments.rerank.ParsingReranker;
import edu.berkeley.nlp.assignments.rerank.ParsingRerankerFactory;
import edu.berkeley.nlp.ling.Tree;
import edu.berkeley.nlp.util.Pair;

public class BasicParsingRerankerFactory implements ParsingRerankerFactory {

	public ParsingReranker trainParserReranker(Iterable<Pair<KbestList, Tree<String>>> kbestListsAndGoldTrees) {
		return new Baseline();

	}

	class Baseline implements ParsingReranker {
		public Baseline() {
		}

		public Tree<String> getBestParse(List<String> sentence, KbestList kbestList) {
			return kbestList.getKbestTrees().get(0); // returns the first tree
		}
	}
}
