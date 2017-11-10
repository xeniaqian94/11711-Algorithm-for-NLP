package edu.berkeley.nlp.assignments.rerank;

import java.util.List;
import java.util.Map;
import java.util.Random;

import edu.berkeley.nlp.util.Counter;
import edu.berkeley.nlp.util.IntCounter;
import edu.berkeley.nlp.util.Pair;

// adagrad subgradient descent minimizer
// eta: step size (try 1e0, 1e-1, 1e-2, etc...)
// regConstant: multiplier on the L2 norm of weights
// epochs: number of passes through all functions
public class AdagradMinimizer implements OnlineMinimizer {

  double eta;
  double delta;
  double regConstant;
  int epochs;
  double r;

  public AdagradMinimizer(double eta, double regConstant, int epochs) {
    this.eta = eta;
    this.delta = 1e-2;
    this.regConstant = regConstant;
    this.epochs = epochs;
    this.r = (double) (eta * regConstant);
  }

  private static final void flushShrinkageUpdates(final int index, final int[] current, final int[] lastUpdate, final double[] x, final double[] sqrGradSum, final double r) {
    final int dt = current[0] - lastUpdate[index];
    if (dt > 0) {
      final double s = (double) Math.sqrt(sqrGradSum[index]);
      final double factor = s / (r + s);
      x[index] = x[index] * ((double) Math.pow(factor, dt));
      lastUpdate[index] = current[0];
    }
  }

  public IntCounter minimize(List<DifferentiableFunction> functions, double[] initial, boolean verbose) {
    return minimize(functions, initial, verbose, null);
  }

  // functions: objective function being minimized is the sum of the functions in this list
  // initial: starting point for minimization
  // verbose: whether or not to print stuff out
  // iterCallbackFunction: callback for printing intermediate info out, can be left null
  public IntCounter minimize(List<DifferentiableFunction> functions, double[] initial, boolean verbose, Callback iterCallbackFunction) {
    Random rand = new Random(0);

    int[] current = new int[] { 0 };
    double[] x = WeightsUtils.copy(initial);
    int[] lastUpdate = new int[x.length];
    double[] sqrGradSum = new double[x.length];
    WeightsUtils.addi(sqrGradSum, (double) delta);

    LazyAdaGradResult lazyResult = new LazyAdaGradResult(current, lastUpdate, x, sqrGradSum);

    for (int epoch = 0; epoch < epochs; ++epoch) {
      double epochValSum = 0.0f;
      for (int funcIndex : WeightsUtils.shuffle(WeightsUtils.enumerate(0, functions.size()), rand)) {
        DifferentiableFunction func = functions.get(funcIndex);
        Pair<Double,IntCounter> valAndGrad = func.calculate(lazyResult);
        epochValSum += valAndGrad.getFirst();
        IntCounter grad = valAndGrad.getSecond();

        for (Map.Entry<Integer,Double> entry : grad.entries()) {
          final int index = entry.getKey();
          final double gradVal = entry.getValue();
          sqrGradSum[index] += gradVal * gradVal;
        }

        current[0] += 1;

        for (Map.Entry<Integer,Double> entry : grad.entries()) {
          final int index = entry.getKey();
          flushShrinkageUpdates(index, current, lastUpdate, x, sqrGradSum, r);
          final double gradVal = entry.getValue().doubleValue();
          double s = (double) Math.sqrt(sqrGradSum[index]);
          x[index] += -eta * gradVal / (r + s);
        }
      }

      if (verbose || iterCallbackFunction != null) {
        double[] result = new double[initial.length];
        for (int i = 0; i < result.length; ++i) {
          result[i] = (double) lazyResult.getCount(i);
        }
        double funcVal = epochValSum + regConstant * WeightsUtils.innerProd(result, result);
        if (verbose)
          System.out.println(String.format("[AdaGradMinimizer.minimize] Epoch %d ended with value %.6f", epoch, funcVal));
        if (iterCallbackFunction != null)
          iterCallbackFunction.callback(x, epoch, funcVal);
      }
    }

    double[] result = new double[initial.length];
    for (int i = 0; i < result.length; ++i) {
      result[i] = lazyResult.getCount(i);
    }
    return IntCounter.wrapArray(result, result.length);
  }

  // Lazily computed IntCounter representing intermediate weights inside adagrad.
  // Immutable, many IntCounter methods are not implemented.
  private class LazyAdaGradResult extends IntCounter {

    int[] current = null;
    int[] lastUpdate = null;
    double[] x = null;
    double[] sqrGradSum = null;

    public LazyAdaGradResult(int[] current, int[] lastUpdate, double[] x, double[] sqrGradSum) {
      this.current = current;
      this.lastUpdate = lastUpdate;
      this.x = x;
      this.sqrGradSum = sqrGradSum;
    }

    public double get(int k) {
      return getCount(k);
    }

    public double getCount(int k) {
      return getCount((Integer) k);
    }

    public final double getCount(final Integer index) {
      flushShrinkageUpdates(index, current, lastUpdate, x, sqrGradSum, r);
      return (double) x[index];
    }

    public final int size() {
      return x.length;
    }

    public double[] toArray(int length) {
      throw new Error("Method not implemented.");
    }

    public void setLoadFactor(double loadFactor) {
      throw new Error("Method not implemented.");
    }

    public void toSorted() {
      throw new Error("Method not implemented.");
    }

    public boolean put(int k, double v) {
      throw new Error("Method not implemented.");
    }

    public double dotProduct(Counter<Integer> other) {
      throw new Error("Method not implemented.");
    }

    public double normSquared() {
      throw new Error("Method not implemented.");
    }

    public double dotProduct(double[] weights) {
      throw new Error("Method not implemented.");
    }

    public void incrementCount(int k, double d) {
      throw new Error("Method not implemented.");
    }

    public <T extends Integer> void incrementAll(Counter<T> c, double d) {
      throw new Error("Method not implemented.");
    }

    public void incrementAll(int[] arr, double d) {
      throw new Error("Method not implemented.");
    }

    public void incrementAll(int[] arr) {
      throw new Error("Method not implemented.");
    }

    public <T extends Integer> void incrementAll(Counter<T> c) {
      throw new Error("Method not implemented.");
    }

    public void ensureCapacity(int capacity) {
      throw new Error("Method not implemented.");
    }

    public Counter<Integer> toCounter() {
      throw new Error("Method not implemented.");
    }

    public void setCount(int k, double d) {
      throw new Error("Method not implemented.");
    }

    public Iterable<Entry> primitiveEntries() {
      throw new Error("Method not implemented.");
    }

    public void clear() {
      throw new Error("Method not implemented.");
    }

    public double dotProduct(IntCounter c) {
      throw new Error("Method not implemented.");
    }

    public Iterable<Map.Entry<Integer,Double>> entries() {
      throw new Error("Method not implemented.");
    }

    public double incrementCount(Integer key, double d) {
      throw new Error("Method not implemented.");
    }

    public void incrementAll(IntCounter c, double d) {
      throw new Error("Method not implemented.");
    }

    public void incrementAll(IntCounter newWeights) {
      throw new Error("Method not implemented.");
    }

    public void scale(double d) {
      throw new Error("Method not implemented.");
    }

    public void setCount(Integer k, double d) {
      throw new Error("Method not implemented.");
    }

    public double totalCount() {
      throw new Error("Method not implemented.");
    }

    public Iterable<Integer> keySet() {
      throw new Error("Method not implemented.");
    }

  }

}
