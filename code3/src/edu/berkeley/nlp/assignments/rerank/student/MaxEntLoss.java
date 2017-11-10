package edu.berkeley.nlp.assignments.rerank.student;

import java.util.Arrays;
import java.util.List;

import edu.berkeley.nlp.assignments.rerank.student.AwesomeParsingReranker.FeaturesAndGoldFeatures;
import edu.berkeley.nlp.math.DifferentiableFunction;
import edu.berkeley.nlp.util.IntCounter;

public class MaxEntLoss implements DifferentiableFunction {

	List<FeaturesAndGoldFeatures> trainingData = null;
	static double LAMBDA = AwesomeParsingReranker.LAMBDA;

	int featureDimension = 0;

	public MaxEntLoss(List<FeaturesAndGoldFeatures> trainingData) {
		// TODO Auto-generated constructor stub
		this.trainingData = trainingData;
		featureDimension = AwesomeParsingReranker.featureIndexer.size();

	}

	@Override
	public int dimension() {
		// TODO Auto-generated method stub
		return featureDimension;
	}

	@Override
	public double valueAt(double[] weight) {

		// IntCounter implements the methods of a high-dimensional vector. In this
		// assignment, the most useful methods for you are perhaps:
		// IntCounter.normSquared(), which returns ∥w∥22,
		// IntCounter.dotProduct(other), which returns w⊤⋅w′.
		IntCounter ic = IntCounter.wrapArray(weight, weight.length);

		double value = 0;
		value += LAMBDA * ic.normSquared();

		for (FeaturesAndGoldFeatures fagf : trainingData) {

			// calculate nominator here
			for (int pos : fagf.goldFeatures)
				value -= weight[pos];

			double denominator = 0.0d;

			for (int[] thisFeature : fagf.features) {
				double effectiveWeights = 0.0d;
				for (int featurePos : thisFeature) {
					effectiveWeights += weight[featurePos];
				}
				denominator += Math.exp(effectiveWeights);
			}

			value += Math.log(denominator);

		}

		// TODO Auto-generated method stub

		// a sum of loss over all training instances
		System.out.println("value is " + value + " is it descending? ");

		return value;
	}

	@Override
	public double[] derivativeAt(double[] weight) {
		// TODO Auto-generated method stub

		double[] derivative = new double[featureDimension];
		for (FeaturesAndGoldFeatures fagf : trainingData) {
			for (int feature : fagf.goldFeatures)
				derivative[feature] -= 1;
			double[] expDotProduct = new double[fagf.features.size()];
			Arrays.fill(expDotProduct, 0);
			double denominator = 0;
			for (int index = 0; index < fagf.features.size(); index++) {
				int[] featureList = fagf.features.get(index);
				for (int feature : featureList)
					expDotProduct[index] += weight[feature];
				expDotProduct[index] = Math.exp(expDotProduct[index]);
				denominator += expDotProduct[index];

			}

			for (int index = 0; index < fagf.features.size(); index++) {
				for (int feature : fagf.features.get(index))
					derivative[feature] += expDotProduct[index] / denominator;
			}
		}
		//
		for (int index = 0; index < featureDimension; index++) {
			derivative[index] = derivative[index] / trainingData.size();
			derivative[index] += 2 * LAMBDA * weight[index];
		}

		AwesomeParsingReranker.printWeights(derivative);

		return derivative;
	}

}
