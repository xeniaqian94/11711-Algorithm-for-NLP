package edu.berkeley.nlp.assignments.rerank.student;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

import edu.berkeley.nlp.assignments.rerank.KbestList;
import edu.berkeley.nlp.assignments.rerank.LossAugmentedLinearModel;
import edu.berkeley.nlp.assignments.rerank.ParsingReranker;
import edu.berkeley.nlp.assignments.rerank.PrimalSubgradientSVMLearner;
import edu.berkeley.nlp.assignments.rerank.SimpleFeatureExtractor;
import edu.berkeley.nlp.assignments.rerank.LossAugmentedLinearModel.UpdateBundle;
import edu.berkeley.nlp.ling.Tree;
import edu.berkeley.nlp.math.LBFGSMinimizer;
import edu.berkeley.nlp.parser.EnglishPennTreebankParseEvaluator;
import edu.berkeley.nlp.util.Indexer;
import edu.berkeley.nlp.util.IntCounter;
import edu.berkeley.nlp.util.Pair;

//public class AwesomeParsingReranker implements ParsingReranker {

public class BasicParsingReranker extends UnifiedParsingReranker {
	

	class Datum {
		int goldIndex;
		ArrayList<IntCounter> features;
		ArrayList<Double> oneMinusF1;

		public Datum(int goldIndex) {
			this.goldIndex = goldIndex;
			this.features = new ArrayList<IntCounter>();
			this.oneMinusF1 = new ArrayList<Double>();
		}

		public void insertFeature(IntCounter feats) {
			this.features.add(feats);
		}

		public void insertFeature(int[] feats, double thisOneMinusF1) {
			IntCounter featsIC = new IntCounter();
			for (int i = 0; i < feats.length; i++) {
				featsIC.incrementCount(feats[i], 1);
			}
			this.features.add(featsIC);
			this.oneMinusF1.add(thisOneMinusF1);

		}

	}

	class SVMModel implements LossAugmentedLinearModel {

		public double score(IntCounter weights, IntCounter features) {
			double score = 0;
			for (int index : features.keySet()) {
				score = score + weights.get(index) * features.get(index);
			}
			return score;
		}

		@Override

		public UpdateBundle getLossAugmentedUpdateBundle(Object datumObject, IntCounter weights) {
			// TODO Auto-generated method stub
			Datum datum = (Datum) datumObject;
			int goldIndex = datum.goldIndex;

			double goldScore = score(weights, datum.features.get(goldIndex));

			double maxScore = goldScore;
			int maxIndex = goldIndex;
			for (int i = 1; i < datum.features.size(); i++) {
				if (i != goldIndex) {
					double thisScore = score(weights, datum.features.get(i));
					// if (thisScore > maxScore) {
					double thisOneMinusF1Loss = datum.oneMinusF1.get(i);

					if ((!useOneMinusF1 && thisScore + 1 > maxScore)
							| (useOneMinusF1 && thisScore + thisOneMinusF1Loss > maxScore)) {
						maxIndex = i;
						maxScore = thisScore;
					}
				}
			}
			int lossOfGuess = 0;
			if (maxIndex != goldIndex)
				lossOfGuess = 1;
			return new UpdateBundle(datum.features.get(goldIndex), datum.features.get(maxIndex), lossOfGuess);
		}

	}

	public BasicParsingReranker(Iterable<Pair<KbestList, Tree<String>>> kbestListsAndGoldTrees) {

		List<Datum> trainingData = new ArrayList<Datum>();
		for (Pair<KbestList, Tree<String>> pair : kbestListsAndGoldTrees) {

			KbestList kbest = pair.getFirst();
			int pseudoGoldIndex = pseudoGoldInKbestList(kbest, pair.getSecond());
			Datum fagf = new Datum(pseudoGoldIndex);

			for (int index = 0; index < kbest.getKbestTrees().size(); index++) {
				int[] feats = featureExtractor.extractFeatures(kbest, index, featureIndexer, true);
				double thisOneMinusF1 = 1.0d - eptpe.evaluateF1(kbest.getKbestTrees().get(index),
						kbest.getKbestTrees().get(pseudoGoldIndex));

				fagf.insertFeature(feats, thisOneMinusF1);
			}

			trainingData.add(fagf);
		}

		weights = initializeWeights(featureIndexer.size());

		IntCounter weightIC = new IntCounter();
		for (int i = 0; i < weights.length; i++) {
			weightIC.incrementCount(i, 0);
		}

		PrimalSubgradientSVMLearner pssl = new PrimalSubgradientSVMLearner(STEP_SIZE, C, featureIndexer.size());
		// printWeights(weightIC);
		weightIC = pssl.train(weightIC, new SVMModel(), trainingData, MAX_ITER);
		// System.out.println("After optimization ");
		// printWeights(weightIC);
		weights = weightIC.toArray(weightIC.size());

		// printWeights(weights);
		// countNonZeroWeights(weights);
		printLog();
	}

}
