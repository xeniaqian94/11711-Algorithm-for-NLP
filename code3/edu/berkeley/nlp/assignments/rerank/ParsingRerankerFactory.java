package edu.berkeley.nlp.assignments.rerank;

import edu.berkeley.nlp.ling.Tree;
import edu.berkeley.nlp.util.Pair;

/**
 * 
 * @author gdurrett
 *
 */
public interface ParsingRerankerFactory {

  public ParsingReranker trainParserReranker(Iterable<Pair<KbestList,Tree<String>>> kbestListsAndGoldTrees);
}
