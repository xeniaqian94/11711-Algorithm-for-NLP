package edu.berkeley.nlp.assignments.parsing.student;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.berkeley.nlp.assignments.parsing.BinaryRule;
import edu.berkeley.nlp.assignments.parsing.Grammar;
import edu.berkeley.nlp.assignments.parsing.Parser;
import edu.berkeley.nlp.assignments.parsing.SimpleLexicon;
import edu.berkeley.nlp.assignments.parsing.TreeAnnotations;
import edu.berkeley.nlp.assignments.parsing.UnaryRule;
import edu.berkeley.nlp.ling.Tree;
import edu.berkeley.nlp.ling.Trees;
import java.util.Scanner;
import edu.berkeley.nlp.util.CounterMap;

public class GenerativeParser implements Parser {
	int h = GenerativeParserFactory.h;
	int v = GenerativeParserFactory.v;
	boolean tagPA = GenerativeParserFactory.tagPA;
	int maxLength = GenerativeParserFactory.maxLength;
	boolean sanity = false;

	CounterMap<List<String>, Tree<String>> knownParses;

	CounterMap<Integer, String> spanToCategories;

	SimpleLexicon lexicon;

	Grammar grammar;

	CYKParser cykParser;

	public GenerativeParser(List<Tree<String>> trainTrees, int maxLength) {

		// this.sanity = sanity;

		System.out.print("Annotating / binarizing training trees ... ");
		List<Tree<String>> annotatedTrainTrees = annotateTrees(trainTrees);
		System.out.println("done.");
		System.out.print("Building grammar ... ");
		grammar = Grammar.generativeGrammarFromTrees(annotatedTrainTrees);
		System.out.println("done Number of symbols " + grammar.getLabelIndexer().size() + " states");
		System.out.println("done Number of binary rules " + grammar.getBinaryRules().size() + " rules");
		System.out.println("done Number of unary rules " + grammar.getUnaryRules().size() + " rules");

		// printIndexer();

		System.out.println(h + " " + v);

		// Scanner s = new Scanner(System.in);
		// s.nextLine();

		int numStates = grammar.getLabelIndexer().size();

		cykParser = new CYKParser(maxLength, numStates, grammar);

		grammar.getUnaryRules();

		// UnaryClosure uc = new UnaryClosure(grammar);
		// System.out.println(uc);

		System.out.print("Discarding grammar and setting up a generative parser ... ");
		// For FeaturizedLexiconDiscriminativeParserFactory, you should construct an
		// instance of your own
		// of LexiconFeaturizer here.
		lexicon = new SimpleLexicon(annotatedTrainTrees);
	}

	// public GenerativeParser(List<Tree<String>> trainTrees, int h, int v, int
	// maxLength) {
	// this(trainTrees, h, v, maxLength, false);
	//
	// // knownParses = new CounterMap<List<String>, Tree<String>>();
	// // spanToCategories = new CounterMap<Integer, String>();
	// // for (Tree<String> trainTree : annotatedTrainTrees) {
	// // List<String> tags = trainTree.getPreTerminalYield();
	// // knownParses.incrementCount(tags, trainTree, 1.0);
	// // tallySpans(trainTree, 0);
	// // }
	// // System.out.println("done.");
	// }

	@Override
	public Tree<String> getBestParse(List<String> sentence) {
		try {
			System.out.println("\nCurrently parsing sentence " + sentence + " with h, v value as " + h + " " + v);
			List<String> tags = getBaselineTagging(sentence);
			Tree<String> annotatedBestParse = cykParser.parse(grammar, lexicon, sentence, true);
			System.out.println("Parse okay here wait to be unannotated");
			System.out.println(Trees.PennTreeRenderer.render(annotatedBestParse));

			// if (knownParses.keySet().contains(tags)) {
			// annotatedBestParse = getBestKnownParse(tags);
			// } else {
			// annotatedBestParse = buildRightBranchParse(sentence, tags);
			// }

			// System.out.println(annotatedBestParse.toString());

			// if (v == 1 && h == Integer.MAX_VALUE) {
			// System.out.println("unannotatedBestParse");
			// System.out.println(TreeAnnotations.unAnnotateTree(annotatedBestParse));
			return TreeAnnotations.unAnnotateTree(annotatedBestParse);

			// }
			// return TreeMarkovAnnotations.unAnnotateTree(annotatedBestParse);
		} catch (Exception e) {
			System.out.println(e.getMessage().substring(0, 1000));
		}
		return new Tree<String>("ROOT", Collections.singletonList(new Tree<String>("JUNK")));
	}

	private Tree<String> buildRightBranchParse(List<String> words, List<String> tags) {
		int currentPosition = words.size() - 1;
		Tree<String> rightBranchTree = buildTagTree(words, tags, currentPosition);
		while (currentPosition > 0) {
			currentPosition--;
			rightBranchTree = merge(buildTagTree(words, tags, currentPosition), rightBranchTree);
		}
		rightBranchTree = addRoot(rightBranchTree);
		return rightBranchTree;
	}

	private Tree<String> merge(Tree<String> leftTree, Tree<String> rightTree) {
		int span = leftTree.getYield().size() + rightTree.getYield().size();
		String mostFrequentLabel = spanToCategories.getCounter(span).argMax();
		if (mostFrequentLabel == null)
			mostFrequentLabel = "NP";
		List<Tree<String>> children = new ArrayList<Tree<String>>();
		children.add(leftTree);
		children.add(rightTree);
		return new Tree<String>(mostFrequentLabel, children);
	}

	private Tree<String> addRoot(Tree<String> tree) {
		return new Tree<String>("ROOT", Collections.singletonList(tree));
	}

	private Tree<String> buildTagTree(List<String> words, List<String> tags, int currentPosition) {
		Tree<String> leafTree = new Tree<String>(words.get(currentPosition));
		Tree<String> tagTree = new Tree<String>(tags.get(currentPosition), Collections.singletonList(leafTree));
		return tagTree;
	}

	private Tree<String> getBestKnownParse(List<String> tags) {
		return knownParses.getCounter(tags).argMax();
	}

	private List<String> getBaselineTagging(List<String> sentence) {
		List<String> tags = new ArrayList<String>();
		for (String word : sentence) {
			String tag = getBestTag(word);
			tags.add(tag);
		}
		return tags;
	}

	private String getBestTag(String word) {
		double bestScore = Double.NEGATIVE_INFINITY;
		String bestTag = null;
		for (String tag : lexicon.getAllTags()) {
			double score = lexicon.scoreTagging(word, tag);
			if (bestTag == null || score > bestScore) {
				bestScore = score;
				bestTag = tag;
			}
		}
		return bestTag;
	}

	private List<Tree<String>> annotateTrees(List<Tree<String>> trees) {
		List<Tree<String>> annotatedTrees = new ArrayList<Tree<String>>();
		// if (!(v == 1 && h == Integer.MAX_VALUE)) {
		TreeMarkovAnnotations.setHV(h, v, tagPA);
		// }
		int i = 0;
		for (Tree<String> tree : trees) {
			i++;

			if (i < 5)
				System.out.println("Unannotated tree \n" + new Trees.PennTreeRenderer().render(tree));
			Tree<String> annotatedTree;
			// if (v == 1 && h == Integer.MAX_VALUE) {
			// // System.out.println("Generic annotations");
			// annotatedTree = TreeAnnotations.annotateTreeLosslessBinarization(tree);
			// } else {
			annotatedTree = TreeMarkovAnnotations.annotateBinarization(tree);
			// annotatedTree = FineAnnotator.annotateTree(tree);

			// }
			annotatedTrees.add(annotatedTree);
			if (i < 5) {
				System.out.println("1 INF Annotated tree \n"
						+ new Trees.PennTreeRenderer().render(TreeAnnotations.annotateTreeLosslessBinarization(tree)));

				// System.out.println(" Annotated tree \n" + new
				// Trees.PennTreeRenderer().render(annotatedTree));
				System.out.println("Our own annotated tree by our annotator\n"
						+ new Trees.PennTreeRenderer().render(TreeMarkovAnnotations.annotateBinarization(tree)));

			}
			if (i == 5) {
				// String[] a = new String[0];
				// System.out.println("a length" + a.length);
				// Scanner s = new Scanner(System.in);
				// s.nextLine();
			}
		}
		return annotatedTrees;
	}

	private int tallySpans(Tree<String> tree, int start) {
		if (tree.isLeaf() || tree.isPreTerminal())
			return 1;

		int end = start;
		for (Tree<String> child : tree.getChildren()) {
			int childSpan = tallySpans(child, end);
			end += childSpan;
		}
		String category = tree.getLabel();
		if (!category.equals("ROOT"))
			spanToCategories.incrementCount(end - start, category, 1.0);
		return end - start;
	}

	private void printIndexer() {
		System.out.println();
		System.out.println("Number of labels here \n" + grammar.getLabelIndexer().size());

		int i = 0;
		for (String s : grammar.getLabelIndexer()) {
			System.out.println(i + ":" + s);
			i++;
			if (i > 20)
				break;
		}
		for (BinaryRule br : grammar.getBinaryRules()) {
			System.out.println(br.toString() + " " + br.getScore());
		}
		for (UnaryRule ur : grammar.getUnaryRules()) {
			System.out.println(ur.toString() + " " + ur.getScore());
		}

	}

}
