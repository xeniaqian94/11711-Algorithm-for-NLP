package edu.berkeley.nlp.assignments.rerank;

import java.io.Serializable;
import java.util.List;

import edu.berkeley.nlp.ling.Tree;

/**
 * Representation of a k-best list as a list of trees and associated scores (log
 * probabilities from the parser that was used to extract them).
 * 
 * @author gdurrett
 *
 */
public class KbestList implements Serializable {

  private static final long serialVersionUID = 1L;
  private final List<Tree<String>> kbestTrees;
  private final double[] scores;

  public KbestList(List<Tree<String>> kbestTrees, double[] scores) {
    this.kbestTrees = kbestTrees;
    this.scores = scores;
  }

  public List<Tree<String>> getKbestTrees() {
    return kbestTrees;
  }

  public double[] getScores() {
    return scores;
  }
}
