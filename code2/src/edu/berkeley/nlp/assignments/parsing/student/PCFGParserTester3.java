package edu.berkeley.nlp.assignments.parsing.student;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import edu.berkeley.nlp.assignments.parsing.BaselineParser;
import edu.berkeley.nlp.assignments.parsing.Parser;
import edu.berkeley.nlp.assignments.parsing.ParserFactory;
import edu.berkeley.nlp.assignments.parsing.student.CoarseToFineParserFactory;
import edu.berkeley.nlp.assignments.parsing.student.GenerativeParserFactory;
import edu.berkeley.nlp.io.PennTreebankReader;
import edu.berkeley.nlp.ling.Tree;
import edu.berkeley.nlp.ling.Trees;
import edu.berkeley.nlp.parser.EnglishPennTreebankParseEvaluator;
import edu.berkeley.nlp.util.CommandLineUtils;
import edu.berkeley.nlp.util.Indexer;

/**
 * Harness for PCFG Parser project.
 * 
 * @author Dan Klein
 */
public class PCFGParserTester3 {
	static int h = 2;
	static int v = 2;

	public enum ParserType {
		BASELINE {
			public ParserFactory getParserFactory(int h, int v) {
				return getParserFactory();

			}

			@Override
			public ParserFactory getParserFactory() {
				return new BaselineParser.BaselineParserFactory();
			}
		},
		GENERATIVE {
			@Override
			public ParserFactory getParserFactory() {
				return new GenerativeParserFactory(h, v);
			}
		},
		COARSE_TO_FINE {
			@Override
			public ParserFactory getParserFactory() {
				return new CoarseToFineParserFactory();
			}
		};

		public abstract ParserFactory getParserFactory();
	}

	public static void main(String[] args) {
		// Parse command line flags and arguments
		Map<String, String> argMap = CommandLineUtils.simpleCommandLineParser(args);

		// Set up default parameters and settings
		String basePath = ".";
		boolean verbose = true;
		boolean sanity = false;
		String testMode = "validate";
		int maxTrainLength = 1000;
		int maxTestLength = 40;

		if (argMap.containsKey("-sanityCheck")) {
			sanity = true;
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
		System.out.println("Maximum length for training sentences: " + maxTrainLength);
		if (argMap.containsKey("-maxTestLength")) {
			maxTestLength = Integer.parseInt(argMap.get("-maxTestLength"));
		}
		System.out.println("Maximum length for test sentences: " + maxTestLength);
		if (argMap.containsKey("-verbose")) {
			verbose = true;
		}
		if (argMap.containsKey("-quiet")) {
			verbose = false;
		}

		if (argMap.containsKey("-h")) {
			// System.out.println(argMap.get("-h")+"|");

			if (argMap.get("-h").equals("INF"))
				h = Integer.MAX_VALUE;
			else
				h = Integer.parseInt(argMap.get("-h"));
			// System.out.println(h);
			// Scanner s = new Scanner(System.in);
			// s.nextLine();
		}

		if (argMap.containsKey("-v")) {
			v = Integer.parseInt(argMap.get("-v"));
		}

		if (argMap.containsKey("-noTagPA")) {
			GenerativeParserFactory.tagPA = false;
		}
		if (argMap.containsKey("-tagSplitting")) {
			GenerativeParserFactory.tagSplitting=true;
		}

		ParserType parserType = ParserType.BASELINE;

		if (argMap.containsKey("-parserType")) {
			parserType = ParserType.valueOf(argMap.get("-parserType"));
		}

		int trainTreesEnd = 2199;
		if (sanity) {
			maxTrainLength = 3;
			maxTestLength = 3;
			trainTreesEnd = 299;
		}
		System.out.print("Loading training trees (sections 2-21) ... ");
		List<Tree<String>> trainTrees = readTrees(basePath, 200, trainTreesEnd, maxTrainLength);
		System.out.println("done. (" + trainTrees.size() + " trees)");
		List<Tree<String>> testTrees = null;
		if (testMode.equalsIgnoreCase("validate")) {
			System.out.print("Loading validation trees (section 22) ... ");
			testTrees = readTrees(basePath, 2200, 2299, maxTestLength);
		} else {
			System.out.print("Loading test trees (section 23) ... ");
			testTrees = readTrees(basePath, 2300, 2399, maxTestLength);
		}
		System.out.println("done. (" + testTrees.size() + " trees)");

		ParserFactory factory = parserType.getParserFactory();
		Parser parser = factory.getParser(trainTrees);

		testParser(parser, testTrees, verbose);
	}

	private static void testParser(Parser parser, List<Tree<String>> testTrees, boolean verbose) {
		long nanos = System.nanoTime();
		EnglishPennTreebankParseEvaluator.LabeledConstituentEval<String> eval = new EnglishPennTreebankParseEvaluator.LabeledConstituentEval<String>(
				Collections.singleton("ROOT"),
				new HashSet<String>(Arrays.asList(new String[] { "''", "``", ".", ":", "," })));

		int i = 0;
		long startTime = System.currentTimeMillis();
		System.out.println("Testing starts ");

		for (Tree<String> testTree : testTrees) {

			i++;
			long stopTime = System.currentTimeMillis();
			System.out.println("Currently testParser progress " + i * 1.0 / testTrees.size() + " Elapsed time was "
					+ (stopTime - startTime) * 1.0 / 1000 + " miliseconds Raw estimate of total will be "
					+ (stopTime - startTime) * 1.0 / 1000 / i * testTrees.size());

			List<String> testSentence = testTree.getYield();
			Tree<String> guessedTree = parser.getBestParse(testSentence);
			if (verbose) {
				System.out.println("Guess:\n" + Trees.PennTreeRenderer.render(guessedTree));
				System.out.println("Gold:\n" + Trees.PennTreeRenderer.render(testTree));
			}
			eval.evaluate(guessedTree, testTree);
			// if (i%20==0)
			// eval.display(true);

		}
		eval.display(true);
		System.out.println("Decoding took " + (System.nanoTime() - nanos) / 1000000 + " millis");
	}

	private static List<Tree<String>> readTrees(String basePath, int low, int high, int maxLength) {
		Collection<Tree<String>> trees = PennTreebankReader.readTrees(basePath, low, high);
		// normalize trees
		Trees.TreeTransformer<String> treeTransformer = new Trees.StandardTreeNormalizer();
		List<Tree<String>> normalizedTreeList = new ArrayList<Tree<String>>();
		for (Tree<String> tree : trees) {
			Tree<String> normalizedTree = treeTransformer.transformTree(tree);
			if (normalizedTree.getYield().size() > maxLength)
				continue;
			// System.out.println(Trees.PennTreeRenderer.render(normalizedTree));
			normalizedTreeList.add(normalizedTree);
		}
		return normalizedTreeList;
	}
}
