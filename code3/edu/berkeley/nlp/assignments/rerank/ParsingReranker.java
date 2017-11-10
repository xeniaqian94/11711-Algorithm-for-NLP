package edu.berkeley.nlp.assignments.rerank;

import java.util.List;

import edu.berkeley.nlp.ling.Tree;

/**
 * 
 * @author gdurrett
 * 
 */
public interface ParsingReranker {

  /**
   * @param sentence
   *          The input sentence. This equals the terminal yield of each tree in
   *          the k-best list; it's just provided in this form so that you have
   *          more convenient accessors.
   * @param kbestList
   *          The list of input trees and their associated scores.
   * @return The tree from the k-best list selected by the reranker.
   */
  public Tree<String> getBestParse(List<String> sentence, KbestList kbestList);
}
