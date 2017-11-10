package edu.berkeley.nlp.assignments.rerank;

import edu.berkeley.nlp.util.IntCounter;

// an interface for hooking up a model to an SVM trainer (see comment below)
public interface LossAugmentedLinearModel<T> {

  public class UpdateBundle {
    public UpdateBundle(IntCounter goldFeatures, IntCounter lossAugGuessFeatures, double lossOfGuess) {
      this.goldFeatures = goldFeatures;
      this.lossAugGuessFeatures = lossAugGuessFeatures;
      this.lossOfGuess = lossOfGuess;
    }

    public final IntCounter goldFeatures;
    public final IntCounter lossAugGuessFeatures;
    public final double lossOfGuess;
  }

  // returns everything an SVM trainer needs to do its thing for a given
  // training datum
  // datum: current training datum... including gold label... you get to define
  // the type T
  // goldFeatures: feature vector of correct label for current training datum
  // lossAugGuessFeatures: feature vector of loss-augmented guess using weights
  // provided for current training datum
  // lossOfGuess: loss of loss-augmented guess compared to gold label for
  // current training datum
  public UpdateBundle getLossAugmentedUpdateBundle(T datum, IntCounter weights);

}
