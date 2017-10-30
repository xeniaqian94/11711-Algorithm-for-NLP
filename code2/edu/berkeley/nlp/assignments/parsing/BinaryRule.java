package edu.berkeley.nlp.assignments.parsing;

import edu.berkeley.nlp.util.Indexer;

public class BinaryRule {
  public final int parent;

  public final int leftChild;

  public final int rightChild;

  private double score;

  public int getParent() {
    return parent;
  }

  public int getLeftChild() {
    return leftChild;
  }

  public int getRightChild() {
    return rightChild;
  }

  public double getScore() {
    return score;
  }

  public void setScore(double score) {
    this.score = score;
  }

  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof BinaryRule)) return false;

    final BinaryRule binaryRule = (BinaryRule) o;

    return parent == binaryRule.parent && leftChild == binaryRule.leftChild && rightChild == binaryRule.rightChild;
  }

  public int hashCode() {
    int result;
    result = (parent + 1) * 13523472;
    result = (result + leftChild) * 842342521;
    result = (result + rightChild) * 2662123;
    return result;
  }

  public String toString() {
    return parent + " -> " + leftChild + " " + rightChild;
  }

  public String toString(Indexer<String> labelIndexer) {
    return labelIndexer.get(parent) + " -> " + labelIndexer.get(leftChild) + " " + labelIndexer.get(rightChild);
  }

  public BinaryRule(int parent, int leftChild, int rightChild) {
    this.parent = parent;
    this.leftChild = leftChild;
    this.rightChild = rightChild;
  }
}