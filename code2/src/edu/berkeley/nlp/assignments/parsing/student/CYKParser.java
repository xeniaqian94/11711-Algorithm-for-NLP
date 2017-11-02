package edu.berkeley.nlp.assignments.parsing.student;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import edu.berkeley.nlp.assignments.parsing.BinaryRule;
import edu.berkeley.nlp.assignments.parsing.Grammar;
import edu.berkeley.nlp.assignments.parsing.SimpleLexicon;
import edu.berkeley.nlp.assignments.parsing.UnaryClosure;
import edu.berkeley.nlp.assignments.parsing.UnaryRule;
import edu.berkeley.nlp.ling.Tree;
import edu.berkeley.nlp.util.Indexer;

public class CYKParser {

	int numStates;
	int maxLength;

	double[][][] scoreUnary, scoreBinary, score;
	int[][][] backUnary, backBinarySplit, backBinaryLeft, backBinaryRight;

	UnaryClosure unaryClosure;
	Indexer<String> labelIndexer;

	public CYKParser(int maxLength, int numStates, Grammar grammar) {
		this.maxLength = maxLength;
		this.numStates = numStates;

		scoreUnary = new double[maxLength][maxLength + 1][numStates];
		scoreBinary = new double[maxLength][maxLength + 1][numStates];
		score = new double[maxLength][maxLength + 1][numStates];

		backUnary = new int[maxLength][maxLength + 1][numStates];
		backBinarySplit = new int[maxLength][maxLength + 1][numStates];
		backBinaryLeft = new int[maxLength][maxLength + 1][numStates];
		backBinaryRight = new int[maxLength][maxLength + 1][numStates];

		unaryClosure = new UnaryClosure(grammar.getLabelIndexer(), grammar.getUnaryRules());
		labelIndexer = grammar.getLabelIndexer();

	}

	public double getScoreTag(String word, String tag, SimpleLexicon lexicon) {
		if (lexicon.getAllTags().contains(tag)) {
			return lexicon.scoreTagging(word, tag);
		}
		return Double.NEGATIVE_INFINITY;
	}

	public Tree<String> parse(Grammar grammar, SimpleLexicon lexicon, List<String> sentence) {
		// TODO Auto-generated method stub

		int numWords = sentence.size();

		// System.out.println("statecount, indexer size = "+stateCount + " " +
		// labelIndexer.size());
		for (int i = 0; i <= numWords; i++) {
			for (int j = i; j <= numWords; j++) {
				for (int k = 0; k < numStates; k++) {
					scoreBinary[i][j][k] = Double.NEGATIVE_INFINITY;
					scoreUnary[i][j][k] = Double.NEGATIVE_INFINITY;
				}
			}
		}

		for (int i = 0; i < numWords; i++) {
			for (int j = 0; j < numStates; j++) { // labelIndexer has all non-term states
				double emissionScore = getScoreTag(sentence.get(i), labelIndexer.get(j), lexicon);
//				System.out.println(i + " " + sentence.get(i) + " " + labelIndexer.get(j) + " " + emissionScore);

				if (!(emissionScore == Double.NaN || emissionScore == Double.NEGATIVE_INFINITY))
					scoreBinary[i][i + 1][j] = emissionScore;
				else
					scoreBinary[i][i + 1][j] = Double.NEGATIVE_INFINITY;
			}
		}
//		printScores(sentence);

		for (int i = 0; i < numWords; i++) { // looping over rules of sate[i]->state[j]
//			System.out.println("\n\nposition i " + i);
			for (int a = 0; a < numStates; a++) {
//				System.out.println(a + " " + labelIndexer.get(a));
				for (UnaryRule ur : unaryClosure.getClosedUnaryRulesByParent(a)) {
					int b = ur.getChild();
//					System.out.println(ur.toString());
					if (scoreBinary[i][i + 1][b] != Double.NEGATIVE_INFINITY) {
						double prob = ur.getScore() + scoreBinary[i][i + 1][b];
//						System.out.println(i + " " + a + " " + b + " " + ur.getScore() + " " + scoreBinary[i][i + 1][b]
//								+ " " + prob);
						if (scoreUnary[i][i + 1][a] < prob) {

							scoreUnary[i][i + 1][a] = prob;
							backUnary[i][i + 1][a] = b;
						}
					}
				}
			}
		}
//		printScores(sentence);

		for (int diff = 2; diff <= numWords; diff++) { // alternating layers

			for (int i = 0; i <= (numWords - diff); i++) {
				for (int split = i + 1; split < i + diff; split++) {
					for (int b = 0; b < numStates; b++) {

						for (BinaryRule br : grammar.getBinaryRulesByLeftChild(b)) {
							if (scoreUnary[i][split][b] != Double.NEGATIVE_INFINITY) {

								int rc = br.getRightChild();
								int lc = br.getLeftChild();
								int p = br.getParent();

								if (scoreUnary[split][i + diff][rc] != Double.NEGATIVE_INFINITY) {
									double prob = br.getScore() + scoreUnary[i][split][lc]
											+ scoreUnary[split][i + diff][rc];
//									System.out.println(
//											br.toString() + " " + br.getScore() + " " + scoreUnary[split][i + diff][rc]
//													+ " " + scoreUnary[i][split][b] + " " + prob);
									if (prob > scoreBinary[i][i + diff][p]) {
										scoreBinary[i][i + diff][p] = prob;
										backBinarySplit[i][i + diff][p] = split;
										backBinaryLeft[i][i + diff][p] = lc;
										backBinaryRight[i][i + diff][p] = rc;
									}
								}
							}
						}
					}
				}
			}

//			printScores(sentence);

			for (int i = 0; i <= (numWords - diff); i++) {
				for (int a = 0; a < numStates; a++) {
					for (UnaryRule ur : unaryClosure.getClosedUnaryRulesByParent(a)) { // a->b
//						System.out.println(ur.toString());
						int b = ur.getChild();
						if (scoreBinary[i][i + diff][b] != Double.NEGATIVE_INFINITY) {

							double prob = ur.getScore() + scoreBinary[i][i + diff][b];
							if (scoreUnary[i][i + diff][a] < prob) {
								scoreUnary[i][i + diff][a] = prob;
								backUnary[i][i + diff][a] = b;
							}
						}
					}
				}

			}

		}

//		printScores(sentence);

		if (scoreUnary[0][numWords][0] == Double.NEGATIVE_INFINITY)
			return new Tree<String>("ROOT", Collections.singletonList(new Tree<String>("JUNK")));
		else {

			return backtrackUnary(0, numWords, 0, sentence);

		}
		// return null;

	}

	private void printScores(List<String> sentence) {
		// TODO Auto-generated method stub
		int numWords = sentence.size();
		for (int i = 0; i <= numWords; i++) {
			for (int j = i; j <= numWords; j++) { // upper triangle
				System.out.println("i = " + i + " j = " + j);
				for (int a = 0; a < numStates; a++) {
					System.out.print(a + ": (binary)" + scoreBinary[i][j][a] + "\t");
					System.out.print(a + ": (binary split)" + backBinarySplit[i][j][a] + "\t");
					System.out.print(a + ": (binary lc)" + backBinaryLeft[i][j][a] + "\t");
					System.out.print(a + ": (binary rc)" + backBinaryRight[i][j][a] + "\t");
					System.out.print(a + ": (unary) " + scoreUnary[i][j][a] + "\t");
					System.out.print(a + ": (unary) " + backUnary[i][j][a] + "\t");
					System.out.println();

				}
				System.out.println();
			}
			System.out.println();
		}

	}

	private Tree<String> backtrackUnary(int i, int j, int a, List<String> sentence) {
		// TODO Auto-generated method stub

//		System.out.println("Currently backtracking i j a " + i + " " + j + " " + a + " " + labelIndexer.get(a));

		int b = backUnary[i][j][a];
		Tree<String> treeAsB = backtrackBinary(i, j, b, sentence);
		List<Tree<String>> treesUnderA = new ArrayList<Tree<String>>();

		List<Integer> pathBetweenAB = unaryClosure.getPath(new UnaryRule(a, labelIndexer.indexOf(treeAsB.getLabel())));

		if (pathBetweenAB.size() == 1) {
			treesUnderA.addAll(treeAsB.getChildren());
		} else if (pathBetweenAB.size() == 2) {
			treesUnderA.add(treeAsB);

		} else if (pathBetweenAB.size() > 2) {
			List<Tree<String>> treesUnderIntermediate = new ArrayList<Tree<String>>();
			Tree<String> treeAsPrevIntermediate = treeAsB;

			for (int ind = pathBetweenAB.size() - 2; ind >= 1; ind--) {
				int intermediate = pathBetweenAB.get(ind);
				treesUnderIntermediate.add(treeAsPrevIntermediate);
				treeAsPrevIntermediate = new Tree<String>(labelIndexer.get(intermediate), treesUnderIntermediate);
			}
			treesUnderA.add(treeAsPrevIntermediate);
		}
		return new Tree<String>(labelIndexer.get(a), treesUnderA);
	}

	private Tree<String> backtrackBinary(int i, int j, int a, List<String> sentence) {
		// TODO Auto-generated method stub
//		System.out.println("Currently backtracking i j a " + i + " " + j + " " + a + " " + labelIndexer.get(a));

		if (j == (i + 1)) {
			List<Tree<String>> treesUnderA = new ArrayList<Tree<String>>();
			treesUnderA.add(new Tree<String>(sentence.get(i)));
			return new Tree<String>(labelIndexer.get(a), treesUnderA);
		}

		int split = backBinarySplit[i][j][a];
		int lc = backBinaryLeft[i][j][a];
		int rc = backBinaryRight[i][j][a];
		List<Tree<String>> treesUnderA = new ArrayList<Tree<String>>();
		Tree<String> treeAsLC = backtrackUnary(i, split, lc, sentence);
		Tree<String> treeAsRC = backtrackUnary(split, j, rc, sentence);
		treesUnderA.add(treeAsLC);
		treesUnderA.add(treeAsRC);

		return new Tree<String>(labelIndexer.get(a), treesUnderA);
	}

}
