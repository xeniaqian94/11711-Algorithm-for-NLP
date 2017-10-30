package edu.berkeley.nlp.assignments.parsing;

import edu.berkeley.nlp.util.Indexer;

public class UnaryRule
{
	public final int parent;

	public final int child;

	private double score;

	public int getParent() {
		return parent;
	}

	public int getChild() {
		return child;
	}

  public double getScore() {
    return score;
  }

  public void setScore(double score) {
    this.score = score;
  }

	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof UnaryRule)) return false;

		final UnaryRule unaryRule = (UnaryRule) o;

		return parent == unaryRule.parent && child == unaryRule.child;
	}

	public int hashCode() {
		int result;
		result = (parent + 1) * 13523472;
		result = (result + child) * 842342521;
		return result;
	}

	public String toString() {
		return parent + " -> " + child;
	}
	
  public String toString(Indexer<String> labelIndexer) {
    return labelIndexer.get(parent) + " -> " + labelIndexer.get(child);
  }

	/**
	 * Used for fake unary rules created by UnaryClosure
	 * 
	 * @param parent
	 * @param child
	 */
	public UnaryRule(int parent, int child) {
		this.parent = parent;
		this.child = child;
	}
}