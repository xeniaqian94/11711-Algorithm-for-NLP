package edu.berkeley.nlp.assignments.rerank.student;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import edu.berkeley.nlp.assignments.rerank.KbestList;
import edu.berkeley.nlp.assignments.rerank.ParsingReranker;
import edu.berkeley.nlp.assignments.rerank.SimpleFeatureExtractor;
import edu.berkeley.nlp.ling.Tree;
import edu.berkeley.nlp.math.LBFGSMinimizer;
import edu.berkeley.nlp.parser.EnglishPennTreebankParseEvaluator;
import edu.berkeley.nlp.util.Indexer;
import edu.berkeley.nlp.util.Pair;

public class AwesomeParsingReranker implements ParsingReranker {
	
	class FeaturesAndGoldFeatures {
		int[] goldFeatures;
		List<int[]> features = new ArrayList<int[]>();

	}

	SimpleFeatureExtractor featureExtractor = new SimpleFeatureExtractor();

	public AwesomeParsingReranker(Iterable<Pair<KbestList, Tree<String>>> kbestListsAndGoldTrees) {

		List<FeaturesAndGoldFeatures> trainingData = new ArrayList<FeaturesAndGoldFeatures>();
		for (Pair<KbestList, Tree<String>> pair : kbestListsAndGoldTrees) {
			FeaturesAndGoldFeatures fagf = new FeaturesAndGoldFeatures();
			KbestList kbest = pair.getFirst();
			int pseudoGoldIndex = pseudoGoldInKbestList(kbest, pair.getSecond());

			for (int index = 0; index < kbest.getKbestTrees().size(); index++) {
				int[] feats = featureExtractor.extractFeatures(kbest, index, featureIndexer, true);
				fagf.features.add(feats);
				if (index == pseudoGoldIndex)
					fagf.goldFeatures = feats;
			}
			trainingData.add(fagf);

		}

		weights = new double[featureIndexer.size()];
		Arrays.fill(weights, 0);

		// this is where training on the fly goes

		LBFGSMinimizer lbfgsMinimizer = new LBFGSMinimizer();
		MaxEntLoss maxEntLoss = new MaxEntLoss(trainingData);
		System.out.println("Before optimization ");
		printWeights(weights);
		weights = lbfgsMinimizer.minimize(maxEntLoss, weights, TOLERENCE);
		System.out.println("After optimization ");
		printWeights(weights);
	}

	private int pseudoGoldInKbestList(KbestList kbest, Tree<String> gold) {
		// TODO Auto-generated method stub
		// Tree<String> bestTree = null;
		double bestF1 = Double.MIN_VALUE;
		int bestTreeIndex = 0;
		EnglishPennTreebankParseEvaluator.LabeledConstituentEval<String> eptpe = new EnglishPennTreebankParseEvaluator.LabeledConstituentEval<String>(
				new HashSet<String>(Arrays.asList("ROOT")), new HashSet<String>());

		for (int index = 0; index < kbest.getKbestTrees().size(); index++) {
			// Tree<String> tree : kbest.getKbestTrees()) {
			Tree<String> tree = kbest.getKbestTrees().get(index);
			if (gold.hashCode() == tree.hashCode())
				return index;
			else {
				double thisF1 = eptpe.evaluateF1(tree, gold);
				if (bestF1 < thisF1) {
					bestF1 = thisF1;
					bestTreeIndex = index;
				}

			}

		}
		return bestTreeIndex;
	}

	static void printWeights(double[] weights) {
		System.out.println("Here we print out weights. Only none-zero is shown: ");
		// System.out.println(weights);
		int count=0;
		for (int i = 0; i < weights.length; i++) {
			if (weights[i] != 0d)
				System.out.print(featureIndexer.get(i) + ": " + weights[i] + "\t");
			count++;
			if (count>200)
				break;
		}
		System.out.println();
	}

	@Override
	public Tree<String> getBestParse(List<String> sentence, KbestList kbestList) {
		// TODO Auto-generated method stub

		Tree<String> bestTree = null;
		int bestIndex = 0;
		double bestScore = -1.0 * Double.MAX_VALUE;
		for (int index = 0; index < kbestList.getKbestTrees().size(); index++) {
			// Tree<String> tree:kbestList.getKbestTrees()) {
			int[] feats = featureExtractor.extractFeatures(kbestList, index, featureIndexer, false);
			// System.out.println("Testing " + index + " feats: " + feats);
			double score = 0;
			for (int feat : feats)
				score += weights[feat];
			if (score > bestScore) {
				bestScore = score;
				bestTree = kbestList.getKbestTrees().get(index);
				bestIndex = index;
			}
		}
		// System.out.println("This testing selected bestIndex as " + bestIndex);

		return bestTree;
	}

}
