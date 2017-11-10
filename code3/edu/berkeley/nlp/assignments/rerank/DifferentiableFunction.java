package edu.berkeley.nlp.assignments.rerank;

import edu.berkeley.nlp.util.IntCounter;
import edu.berkeley.nlp.util.Pair;

public interface DifferentiableFunction {
  public abstract Pair<Double,IntCounter> calculate(IntCounter x);
}
