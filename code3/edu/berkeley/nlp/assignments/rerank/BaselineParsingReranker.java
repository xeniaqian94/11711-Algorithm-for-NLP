package edu.berkeley.nlp.assignments.rerank;

import java.util.List;

import edu.berkeley.nlp.ling.Tree;

/**
 * 1-best baseline reranker
 * 
 * @author gdurrett
 *
 */
public class BaselineParsingReranker implements ParsingReranker {

  public Tree<String> getBestParse(List<String> sentence, KbestList kbestList) {
    return kbestList.getKbestTrees().get(0);
  }
}
