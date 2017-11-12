package edu.berkeley.nlp.assignments.rerank.student;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import edu.berkeley.nlp.assignments.rerank.BaselineParsingReranker;
import edu.berkeley.nlp.assignments.rerank.KbestExampleIterable;
import edu.berkeley.nlp.assignments.rerank.KbestList;
import edu.berkeley.nlp.assignments.rerank.ParsingReranker;
import edu.berkeley.nlp.assignments.rerank.ParsingRerankerFactory;
import edu.berkeley.nlp.assignments.rerank.student.AwesomeParsingRerankerFactory;
import edu.berkeley.nlp.assignments.rerank.student.BasicParsingRerankerFactory;
import edu.berkeley.nlp.ling.Tree;
import edu.berkeley.nlp.ling.Trees;
import edu.berkeley.nlp.ling.Trees.PennTreeReader;
import edu.berkeley.nlp.parser.EnglishPennTreebankParseEvaluator;
import edu.berkeley.nlp.util.CommandLineUtils;

public class ParsingRerankerTester2 {

	public enum RerankerType {
		BASIC {
			@Override
			public ParsingRerankerFactory getRerankerFactory() {
				return new BasicParsingRerankerFactory();
			}
		},
		AWESOME {
			@Override
			public ParsingRerankerFactory getRerankerFactory() {
				return new AwesomeParsingRerankerFactory();
			}
		};

		public abstract ParsingRerankerFactory getRerankerFactory();
	}

	public static void main(String[] args) {
		// Parse command line flags and arguments
		Map<String, String> argMap = CommandLineUtils.simpleCommandLineParser(args);

		// Set up default parameters and settings
		String basePath = ".";
		boolean sanity = false;
		String testMode = "validate";
		int maxTrainLength = 40;
		int maxTestLength = 40;
		int maxTrainTrees = -1;
		int maxTestTrees = -1;
		int kbestLen = 20;

		if (argMap.containsKey("-sanityCheck")) {
			sanity = true;
			maxTrainTrees = 100;
			maxTestTrees = 10;
		}

		// Update defaults using command line specifications
		if (argMap.containsKey("-path")) {
			basePath = argMap.get("-path");
			System.out.println("Using base path: " + basePath);
		}
		if (argMap.containsKey("-test")) {
			testMode = "test";
			System.out.println("Testing on final test data.");
		} else {
			System.out.println("Testing on validation data.");
		}
		if (argMap.containsKey("-maxTrainLength")) {
			maxTrainLength = Integer.parseInt(argMap.get("-maxTrainLength"));
		}
		if (argMap.containsKey("-maxTrainTrees")) {
			maxTrainTrees = Integer.parseInt(argMap.get("-maxTrainTrees"));
		}
		System.out.println(
				"Maximum length for training sentences: " + maxTrainLength + ", " + maxTrainTrees + " max trees");
		if (argMap.containsKey("-maxTestLength")) {
			maxTestLength = Integer.parseInt(argMap.get("-maxTestLength"));
		}
		if (argMap.containsKey("-maxTestTrees")) {
			maxTestTrees = Integer.parseInt(argMap.get("-maxTestTrees"));
		}

		if (argMap.containsKey("-tolerence")) {
			UnifiedParsingReranker.TOLERENCE = Double.parseDouble(argMap.get("-tolerence"));
		}
		System.out.println("Maximum length for test sentences: " + maxTestLength + ", " + maxTestTrees + " max trees");

		RerankerType rerankerType = RerankerType.BASIC;

		if (argMap.containsKey("-rerankerType")) {
			rerankerType = RerankerType.valueOf(argMap.get("-rerankerType"));
		}

		String trainTreesPath = basePath + "/train.mrg";
		String devTreesPath = basePath + "/dev.mrg";
		String testTreesPath = basePath + "/test.mrg";
		// String trainKbestPath = basePath + "/train.kbest.gz";
		// String devKbestPath = basePath + "/dev.kbest.gz";
		// String testKbestPath = basePath + "/test.kbest.gz";
		String trainKbestPath = basePath + "/train.20best.gz";
		String devKbestPath = basePath + "/dev.20best.gz";
		String testKbestPath = basePath + "/test.20best.gz";

		System.out.print("Loading training trees (sections 2-21) ... ");
		List<Tree<String>> trainTrees = readTrees(trainTreesPath, maxTrainLength, maxTrainTrees);
		// List<KbestList> trainKbest = KbestExampleIterator.readKbestLists(basePath
		// + "/train.kbest.gz", maxTrainLength, maxTrainTrees, kbestLen);
		System.out.println("done. (" + trainTrees.size() + " trees)");
		List<Tree<String>> testTrees = null;
		List<KbestList> testKbest = null;
		if (testMode.equalsIgnoreCase("validate")) {
			System.out.print("Loading validation trees (section 22) ... ");
			testTrees = readTrees(devTreesPath, maxTestLength, maxTestTrees);
			// testKbest = KbestExampleIterator.readKbestLists(basePath +
			// "/dev.kbest.gz", maxTestLength, maxTestTrees, kbestLen);
			testKbest = KbestExampleIterable.readKbestLists(devKbestPath, maxTestLength, maxTestTrees, kbestLen);
		} else {
			System.out.print("Loading test trees (section 23) ... ");
			testTrees = readTrees(testTreesPath, maxTestLength, maxTestTrees);
			// testKbest = KbestExampleIterator.readKbestLists(basePath +
			// "/test.kbest.gz", maxTestLength, maxTestTrees, kbestLen);
			testKbest = KbestExampleIterable.readKbestLists(testKbestPath, maxTestLength, maxTestTrees, kbestLen);
		}
		System.out.println("done. (" + testTrees.size() + " trees)");

		long nanos = System.nanoTime();
		// ParsingReranker reranker =
		// rerankerType.getRerankerFactory().trainParserReranker(new
		// DumbKbestExampleIterator(trainKbest, trainTrees));
		ParsingReranker reranker = rerankerType.getRerankerFactory().trainParserReranker(
				new KbestExampleIterable(trainKbestPath, trainTrees, maxTrainLength, maxTrainTrees, kbestLen));
		System.out.println("Training took " + (System.nanoTime() - nanos) / 1000000 + " millis");
		System.out.println("1-best baseline");
		testReranker(new BaselineParsingReranker(), testTrees, testKbest, false);

		System.out.println("==========================================");
		System.out.println("Your system");
		testReranker(reranker, testTrees, testKbest, false);
	}

	private static void testReranker(ParsingReranker reranker, List<Tree<String>> testTrees, List<KbestList> testKbest,
			boolean verbose) {
		long nanos = System.nanoTime();
		EnglishPennTreebankParseEvaluator.LabeledConstituentEval<String> eval = new EnglishPennTreebankParseEvaluator.LabeledConstituentEval<String>(
				Collections.singleton("ROOT"),
				new HashSet<String>(Arrays.asList(new String[] { "''", "``", ".", ":", "," })));
		PrintWriter nullWriter = new PrintWriter(new OutputStream() {
			public void write(int i) throws IOException {
				// do nothing
			}
		});
		PrintWriter systemOut = new PrintWriter(System.out, true);
		for (int i = 0; i < testTrees.size(); i++) {
			Tree<String> testTree = testTrees.get(i);
			List<String> testSentence = testTree.getYield();
			Tree<String> guessedTree = reranker.getBestParse(testSentence, testKbest.get(i));
			eval.evaluate(guessedTree, testTree, (verbose ? systemOut : nullWriter));
		}
		eval.display(true);
		System.out.println("Decoding took " + (System.nanoTime() - nanos) / 1000000 + " millis");
	}

	public static List<Tree<String>> readTrees(String file, int maxLength, int maxTreesToRead) {
		List<Tree<String>> trees = new ArrayList<Tree<String>>();
		Trees.TreeTransformer<String> treeTransformer = new Trees.StandardTreeNormalizer();
		try {
			PennTreeReader nextTreeIterator = new Trees.PennTreeReader(new BufferedReader(new FileReader(file)));
			while (nextTreeIterator.hasNext() && (maxTreesToRead == -1 || trees.size() < maxTreesToRead)) {
				Tree<String> currTree = treeTransformer.transformTree(nextTreeIterator.next());
				if (currTree.getYield().size() <= maxLength) {
					trees.add(currTree);
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return trees;
	}
}
