package edu.berkeley.nlp.assignments.rerank.student;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import edu.berkeley.nlp.assignments.rerank.KbestList;
import edu.berkeley.nlp.assignments.rerank.ParsingReranker;
import edu.berkeley.nlp.assignments.rerank.SimpleFeatureExtractor;
import edu.berkeley.nlp.ling.Tree;
import edu.berkeley.nlp.parser.EnglishPennTreebankParseEvaluator;
import edu.berkeley.nlp.util.Indexer;

public class UnifiedParsingReranker implements ParsingReranker {

	static double TOLERENCE = 1e-6;
	static double LAMBDA = 0.001d;
	EnglishPennTreebankParseEvaluator.LabeledConstituentEval<String> eptpe = new EnglishPennTreebankParseEvaluator.LabeledConstituentEval<String>(
			new HashSet<String>(Arrays.asList("ROOT")), new HashSet<String>());
	
	static double STEP_SIZE = 0.1;
	static double C = 0.01;
	static boolean useOneMinusF1 = true;
	
	//best of all 
//	static double TOLERENCE = 1e-6;
//	static double LAMBDA = 0.001d;
	
	
	static int MAX_ITER = 30;
	static double INITIALIZATION_SCALE = 1e-4;
	double[] weights = null;
	static Indexer<String> featureIndexer = new Indexer<String>();
	// SimpleFeatureExtractor featureExtractor = new SimpleFeatureExtractor();
	AdvancedFeatureExtractor featureExtractor = new AdvancedFeatureExtractor();
	Random rand = new Random();

	@Override
	public Tree<String> getBestParse(List<String> sentence, KbestList kbestList) {
		// TODO Auto-generated method stub
		// countNonZeroWeights(weights);
		// printWeights(weights);
		Tree<String> bestTree = null;
		int bestIndex = 0;
		double bestScore = -1.0 * Double.MAX_VALUE;
		for (int index = 0; index < kbestList.getKbestTrees().size(); index++) {
			// Tree<String> tree:kbestList.getKbestTrees()) {
			int[] feats = featureExtractor.extractFeatures(kbestList, index, featureIndexer, false);
			// System.out.println("Testing " + index + " feats: " + feats);

			// TODO
			double score = 0;
			for (int feat : feats)
				score += weights[feat];
			if (score > bestScore) {
				bestScore = score;
				bestTree = kbestList.getKbestTrees().get(index);
				bestIndex = index;
				// System.out.println("bestScore gets beated ");
			}
		}
		// System.out.println("This testing selected bestIndex as " + bestIndex);

		return bestTree;
	}

	void printLog() {
		System.out.println("print # of features " + featureIndexer.size());

	}

	double[] initializeWeights(int size) {
		double[] weights = new double[size];

		// Arrays.fill(weights, 0);

		for (int index = 0; index < size; index++)
			weights[index] = rand.nextDouble() * INITIALIZATION_SCALE;

		return weights;
	}

	static void printWeights(double[] weights) {
		System.out.println(
				"Here we print out weights. Only none-zero is shown, total dimension " + featureIndexer.size());
		// System.out.println(weights);
		int count = 0;
		for (int i = 0; i < weights.length; i++) {
			if (weights[i] != 0d)
				System.out.print(featureIndexer.get(i) + ": " + weights[i] + "\t");
			count++;
			if (count > 100)
				break;
		}
		System.out.println();
	}

	static void countNonZeroWeights(double[] weights) {
		int count = 0;
		for (int i = 0; i < weights.length; i++) {
			if (weights[i] != 0d)
				count++;
		}
		System.out.println("None zero weights are this many! " + count);

	}

	protected int pseudoGoldInKbestList(KbestList kbest, Tree<String> gold) {
		// TODO Auto-generated method stub
		// Tree<String> bestTree = null;
		double bestF1 = Double.MIN_VALUE;
		int bestTreeIndex = 0;

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

}
