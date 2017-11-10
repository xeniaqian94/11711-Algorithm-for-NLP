package edu.berkeley.nlp.assignments.rerank;

import java.util.List;

import edu.berkeley.nlp.util.IntCounter;

public interface OnlineMinimizer {

  public static interface Callback {
    public void callback(double[] guess, int iter, double val);

    public static class NullCallback implements Callback {
      public void callback(double[] guess, int iter, double val) {
      }
    }
  }

  public abstract IntCounter minimize(List<DifferentiableFunction> functions, double[] initial, boolean verbose, Callback iterCallbackFunction);
}