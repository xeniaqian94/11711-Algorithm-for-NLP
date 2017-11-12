package edu.berkeley.nlp.assignments.rerank;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.berkeley.nlp.assignments.rerank.LossAugmentedLinearModel.UpdateBundle;
import edu.berkeley.nlp.util.IntCounter;
import edu.berkeley.nlp.util.Pair;

// max-margin training procedure
// uses adagrad subgradient descent to optimize primal SVM objective
// stepSize: size for each stochastic gradient step (try 1e0, 1e-1, 1e-2, etc...)
// regConstant: multiplier on the L2 norm of weights
// numFeatures: needs to know total number of features
// batchSize: number of training instances to group together for each stochastic gradient step
public class PrimalSubgradientSVMLearner<D> {

  int numFeatures;
  double regConstant;
  double stepSize;
  int batchSize;

  public PrimalSubgradientSVMLearner(double stepSize, double regConstant, int numFeatures) {
    this(stepSize, regConstant, numFeatures, 1);
  }

  public PrimalSubgradientSVMLearner(double stepSize, double regConstant, int numFeatures, int batchSize) {
    this.stepSize = stepSize;
    this.regConstant = regConstant;
    this.numFeatures = numFeatures;
    this.batchSize = batchSize;
  }

  // trains an SVM and returns leared weight vector
  // requires a data set and model
  public IntCounter train(IntCounter initWeights, final LossAugmentedLinearModel<D> model, List<D> data, int iters) {
    List<DifferentiableFunction> objs = new ArrayList<DifferentiableFunction>();

    int numBatches = (int) Math.ceil(data.size() / (double) batchSize);
    for (int b = 0; b < numBatches; ++b) {
      final List<D> batch = data.subList(b * batchSize, Math.min(data.size(), (b + 1) * batchSize));
      objs.add(new DifferentiableFunction() {
    	  
    	  
        public Pair<Double,IntCounter> calculate(IntCounter weights) {
          List<UpdateBundle> ubBatch = new ArrayList<UpdateBundle>();
          for (D datum : batch) {
            ubBatch.add(model.getLossAugmentedUpdateBundle(datum, weights));
          }
          double valBatch = 0.0;
          IntCounter deltaBatch = new IntCounter();
          for (UpdateBundle ub : ubBatch) {
            IntCounter delta = new IntCounter();
            delta.incrementAll(ub.goldFeatures, -1.0);
            delta.incrementAll(ub.lossAugGuessFeatures, 1.0);
            double dotProd = 0.0f;
            for (Map.Entry<Integer,Double> entry : delta.entries()) {
              final int key = entry.getKey();
              final float val = entry.getValue().floatValue();
              dotProd += val * weights.getCount(key);
            }
            double val = ub.lossOfGuess + dotProd;
            if (val > 0.0) {
              valBatch += val;
              deltaBatch.incrementAll(delta);
            }
          }
          return Pair.makePair(valBatch, deltaBatch);
        }
        
        
      });
    }

    OnlineMinimizer minimizer = new AdagradMinimizer(stepSize, regConstant, iters);
    return minimizer.minimize(objs, initWeights.toArray(numFeatures), true, null);
  }

}
