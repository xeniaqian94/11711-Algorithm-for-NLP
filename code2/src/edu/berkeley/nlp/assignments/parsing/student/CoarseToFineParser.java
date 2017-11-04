package edu.berkeley.nlp.assignments.parsing.student;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import edu.berkeley.nlp.assignments.parsing.SimpleLexicon;
import edu.berkeley.nlp.assignments.parsing.ParserFactory;
import edu.berkeley.nlp.assignments.parsing.Parser;
import edu.berkeley.nlp.assignments.parsing.UnaryClosure;
import edu.berkeley.nlp.assignments.parsing.UnaryRule;
import edu.berkeley.nlp.assignments.parsing.BinaryRule;
import edu.berkeley.nlp.assignments.parsing.Grammar;
import edu.berkeley.nlp.assignments.parsing.TreeAnnotations;
import edu.berkeley.nlp.ling.Tree;
import edu.berkeley.nlp.math.SloppyMath;
import edu.berkeley.nlp.util.Indexer;
import edu.berkeley.nlp.util.Counter;

class CoarseToFineParser implements Parser {
	SimpleLexicon fineLexicon, coarseLexicon;
	Grammar fineGrammar, coarseGrammar;

	List<String> currentSentence;
	int numCoarseStates, numFineStates;

	static double threshold = CoarseToFineParserFactory.threshold;
	// static double MIN_OCCURRENCES = 10d;
	static int maxLength = CoarseToFineParserFactory.maxLength;
	UnaryClosure fineUnaryClosure, coarseUnaryClosure;
	Indexer<String> fineLabelIndexer, coarseLabelIndexer;

	double[][][] scoreFineUnary, scoreFineBinary;
	int[][][] backFineUnary, backFineBinarySplit, backFineBinaryLeft, backFineBinaryRight;

	double[][][] coarseBinaryAlpha;
	double[][][] coarseUnaryAlpha;
	double[][][] coarseBinaryBeta;
	double[][][] coarseUnaryBeta;

	int numWords;
	int[] fineToCoarseMap;
	boolean isPrint = false;

	CoarseToFineParser(List<Tree<String>> trainTrees) {
		ArrayList<Tree<String>> coarseTrees = new ArrayList<Tree<String>>();
		TreeMarkovAnnotations.setHV(0, 1, true);

		for (Tree<String> tree : trainTrees) {

			Tree<String> newTree = TreeMarkovAnnotations.annotateBinarization(tree);
			coarseTrees.add(newTree);
		}

		coarseLexicon = new SimpleLexicon(coarseTrees);
		coarseGrammar = Grammar.generativeGrammarFromTrees(coarseTrees);
		coarseLabelIndexer = coarseGrammar.getLabelIndexer();
		coarseUnaryClosure = new UnaryClosure(coarseLabelIndexer, coarseGrammar.getUnaryRules());
		numCoarseStates = coarseLabelIndexer.size();

		ArrayList<Tree<String>> fineTrees = new ArrayList<Tree<String>>();
		TreeMarkovAnnotations.setHV(2, 2, true);

		for (Tree<String> tree : trainTrees) {
			Tree<String> newTree = TreeMarkovAnnotations.annotateBinarization(tree);
			fineTrees.add(newTree);
		}
		// assert fineTrees.size() > 0 : "No training trees";
		//
		// collapseTrees(fineTrees);

		fineLexicon = new SimpleLexicon(fineTrees);
		fineGrammar = Grammar.generativeGrammarFromTrees(fineTrees);
		fineLabelIndexer = fineGrammar.getLabelIndexer();
		fineUnaryClosure = new UnaryClosure(fineLabelIndexer, fineGrammar.getUnaryRules());
		numFineStates = fineLabelIndexer.size();

		generateFineToCoarseMap();

		scoreFineUnary = new double[maxLength][maxLength + 1][numFineStates];
		scoreFineBinary = new double[maxLength][maxLength + 1][numFineStates];
		// score = new double[maxLength][maxLength + 1][numStates];

		backFineUnary = new int[maxLength][maxLength + 1][numFineStates];
		backFineBinarySplit = new int[maxLength][maxLength + 1][numFineStates];
		backFineBinaryLeft = new int[maxLength][maxLength + 1][numFineStates];
		backFineBinaryRight = new int[maxLength][maxLength + 1][numFineStates];

		coarseBinaryBeta = new double[maxLength][maxLength + 1][numCoarseStates];
		coarseUnaryBeta = new double[maxLength][maxLength + 1][numCoarseStates];
		coarseBinaryAlpha = new double[maxLength][maxLength + 1][numCoarseStates];
		coarseUnaryAlpha = new double[maxLength][maxLength + 1][numCoarseStates];

	}

	private void generateFineToCoarseMap() {
		fineToCoarseMap = new int[fineLabelIndexer.size()];
		for (int x = 0; x < fineLabelIndexer.size(); x++) {
			String fineLabel = fineLabelIndexer.get(x);
			int index = fineLabel.indexOf('>') + 1;
			if (index == 0) {
				index = fineLabel.indexOf('^');
			} else {
				assert fineLabel.indexOf('^') == -1;
			}
			String coarseLabel = index == -1 ? fineLabel : fineLabel.substring(0, index);
			int coarseIndex = coarseLabelIndexer.indexOf(coarseLabel);
			System.out.println(coarseIndex + ":" + coarseLabel + " " + x + ":" + fineLabel);
			fineToCoarseMap[x] = coarseIndex;
		}
	}

	public Tree<String> getBestParse(List<String> sentence) {

		Scanner s = new Scanner(System.in);
		// System.out.println("sentence = " + sentence);
		currentSentence = sentence;
		numWords = sentence.size();
		// area = (numWords * numWords + numWords) / 2 - 1;

		for (int i = 0; i <= numWords; i++) {
			for (int j = i; j <= numWords; j++) {
				for (int k = 0; k < numCoarseStates; k++) {
					coarseUnaryAlpha[i][j][k] = Double.NEGATIVE_INFINITY;
					coarseBinaryAlpha[i][j][k] = Double.NEGATIVE_INFINITY;
					coarseUnaryBeta[i][j][k] = Double.NEGATIVE_INFINITY;
					coarseBinaryBeta[i][j][k] = Double.NEGATIVE_INFINITY;
				}
			}
		}

		for (int i = 0; i < numWords; i++) {
			for (int j = 0; j < numCoarseStates; j++) { // labelIndexer has all non-term states
				double emissionScore = CYKParser.getScoreTag(sentence.get(i), coarseLabelIndexer.get(j), coarseLexicon);
				// System.out.println(i + " " + sentence.get(i) + " " + labelIndexer.get(j) + "
				// " + emissionScore);

				if (!(emissionScore == Double.NaN) && !(emissionScore == Double.NEGATIVE_INFINITY))
					coarseBinaryAlpha[i][i + 1][j] = emissionScore + coarseBinaryAlpha[i][i + 1][j];
				// else
				// coarseBinaryAlpha[i][i + 1][j] = Double.NEGATIVE_INFINITY;
			}
		}

		for (int i = 0; i < numWords; i++) { // looping over rules of sate[i]->state[j]
			// System.out.println("\n\nposition i " + i);
			for (int b = 0; b < numCoarseStates; b++) {
				// System.out.println(a + " " + labelIndexer.get(a));
				// System.out.println(ur.toString());
				if (coarseBinaryAlpha[i][i + 1][b] != Double.NEGATIVE_INFINITY) {
					for (UnaryRule ur : coarseUnaryClosure.getClosedUnaryRulesByChild(b)) {
						int a = ur.getParent();
						double prob = ur.getScore() + coarseBinaryAlpha[i][i + 1][b];
						// System.out.println(i + " " + a + " " + b + " " + ur.getScore() + " " +
						// scoreBinary[i][i + 1][b]
						// + " " + prob);
						// if (coarseUnaryAlpha[i][i + 1][a] < prob) {
						// coarseUnaryAlpha[i][i + 1][a] = prob;
						// }
						coarseUnaryAlpha[i][i + 1][a] = SloppyMath.logAdd(prob, coarseUnaryAlpha[i][i + 1][a]);
					}
				}
			}
		}

		// inside pass goes here to change to sum instead of max

		for (int diff = 2; diff <= numWords; diff++) { // alternating layers

			for (int i = 0; i <= (numWords - diff); i++) {
				for (int split = i + 1; split < i + diff; split++) {
					for (int b = 0; b < numCoarseStates; b++) {
						if (coarseUnaryAlpha[i][split][b] != Double.NEGATIVE_INFINITY) {

							for (BinaryRule br : coarseGrammar.getBinaryRulesByLeftChild(b)) {

								int rc = br.getRightChild();
								int lc = br.getLeftChild();
								int p = br.getParent();

								if (coarseUnaryAlpha[split][i + diff][rc] != Double.NEGATIVE_INFINITY) {
									double prob = br.getScore() + coarseUnaryAlpha[i][split][lc]
											+ coarseUnaryAlpha[split][i + diff][rc];
									// System.out.println(
									// br.toString() + " " + br.getScore() + " " + scoreUnary[split][i + diff][rc]
									// + " " + scoreUnary[i][split][b] + " " + prob);

									coarseBinaryAlpha[i][i + diff][p] = SloppyMath
											.logAdd(coarseBinaryAlpha[i][i + diff][p], prob);
									// if (prob > coarseBinaryAlpha[i][i + diff][p]) {
									// if coarseBinaryAlpha[i][i + diff][p]== coarseBinaryAlpha[i][i + diff][p] =
									// prob;

									// backBinarySplit[i][i + diff][p] = split;
									// backBinaryLeft[i][i + diff][p] = lc;
									// backBinaryRight[i][i + diff][p] = rc;
									// }
								}
							}
						}
					}
				}
			}

			for (int i = 0; i <= (numWords - diff); i++) {
				for (int b = 0; b < numCoarseStates; b++) {
					if (coarseBinaryAlpha[i][i + diff][b] != Double.NEGATIVE_INFINITY) {
						for (UnaryRule ur : coarseUnaryClosure.getClosedUnaryRulesByChild(b)) { // a->b
							// System.out.println(ur.toString());
							int a = ur.getParent();

							double prob = ur.getScore() + coarseBinaryAlpha[i][i + diff][b];
							coarseUnaryAlpha[i][i + diff][a] = SloppyMath.logAdd(coarseUnaryAlpha[i][i + diff][a],
									prob);
							// if (coarseUnaryAlpha[i][i + diff][a] < prob) {
							// coarseUnaryAlpha[i][i + diff][a] = prob;
							// // backUnary[i][i + diff][a] = b;
							// }
						}
					}
				}

			}

		}

		printScores(sentence);
		System.out.println("Finished inside pass");
		// s.nextLine();

		coarseUnaryBeta[0][numWords][0] = 0;

		for (UnaryRule rule : coarseUnaryClosure.getClosedUnaryRulesByParent(0)) {
			coarseBinaryBeta[0][numWords][rule.getChild()] = SloppyMath
					.logAdd(coarseBinaryBeta[0][numWords][rule.getChild()], rule.getScore());
		}

		System.out.println(numFineStates + " " + numCoarseStates + " " + numWords);
		// Scanner s=new Scanner(System.in);
		// //s.nextLine();

		for (int i = 0; i < numWords; i++)
			for (int j = numWords; j > 0; j--) {
				for (int split = i + 1; split < j; split++)
					for (int x = 0; x < numCoarseStates; x++) {
						// System.out.println(i + " " + j + " " + x);
						if (coarseBinaryBeta[i][j][x] != Double.NEGATIVE_INFINITY) {
							for (BinaryRule br : coarseGrammar.getBinaryRulesByParent(x)) {

								int z = br.getRightChild();
								int y = br.getLeftChild();

								// int z = br.getRightChild();
								// int y = br.getLeftChild();
								// int score=br.getScore();
								if (coarseUnaryAlpha[i][split][y] != Double.NEGATIVE_INFINITY
										&& coarseUnaryAlpha[split][j][z] != Double.NEGATIVE_INFINITY) {
									double probAddedToY = br.getScore() + coarseBinaryBeta[i][j][x]
											+ coarseUnaryAlpha[split][j][z];
									coarseUnaryBeta[i][split][y] = SloppyMath.logAdd(coarseUnaryBeta[i][split][y],
											probAddedToY);

									double probAddedToZ = br.getScore() + coarseBinaryBeta[i][j][x]
											+ coarseUnaryAlpha[i][split][y];
									coarseUnaryBeta[split][j][z] = SloppyMath.logAdd(coarseUnaryBeta[split][j][z],
											probAddedToZ);

								}
							}
						}
					}
				for (int x = 0; x < numCoarseStates; x++) {
					if (coarseUnaryBeta[i][j][x] != Double.NEGATIVE_INFINITY)
						for (UnaryRule rule : coarseUnaryClosure.getClosedUnaryRulesByParent(x)) {
							coarseBinaryBeta[i][j][rule.getChild()] = SloppyMath.logAdd(
									coarseBinaryBeta[i][j][rule.getChild()],
									rule.getScore() + coarseUnaryBeta[i][j][x]);

						}
				}

			}

		printScores(sentence);
		System.out.println("Finished outside pass");
		// s.nextLine();

	    double denominator = -coarseUnaryAlpha[0][numWords][0];
		// printScores(sentence);
		System.out.println("Finished outside pass " + denominator);
		s.nextLine();

		for (int i = 0; i <= numWords; i++) {
			for (int j = i; j <= numWords; j++) {
				for (int k = 0; k < numFineStates; k++) {
					scoreFineBinary[i][j][k] = Double.NEGATIVE_INFINITY;
					scoreFineUnary[i][j][k] = Double.NEGATIVE_INFINITY;
				}
			}
		}

		// System.out.println(fineLexicon.getAllTags());
		// s.nextLine();
		for (int i = 0; i < numWords; i++) {
			for (int j = 0; j < numFineStates; j++) { // labelIndexer has all non-term states
				int x = fineToCoarseMap[j];

				// Scanner s = new Scanner(System.in);
				// //s.nextLine();
				// System.out.println(j + " " + fineLabelIndexer.get(j));
				// System.out.println(" " + coarseLabelIndexer.get(x));
				//
				// if (x == -1)
				// continue;

				// double coarseScore = denominator + coarseBinaryBeta[i][i + 1][x] +
				// coarseBinaryAlpha[i][i + 1][x];
				// System.out.println("Finished outside pass " + (coarseScore +
				// coarseBinaryAlpha[i][i + 1][x]));
				// //s.nextLine();

				// if (coarseScore < threshold)
				// continue;
				// coarseScore += coarseBinaryAlpha[i][i + 1][x];
				// System.out.println(denominator + " " + coarseBinaryBeta[i][i + 1][x] + " "
				// + coarseBinaryAlpha[i][i + 1][x] + " total adds up to " + coarseScore + " "
				// + (coarseScore < threshold) + " = " + threshold);
				// if (coarseScore < threshold)
				// continue;

				double emissionScore = CYKParser.getScoreTag(sentence.get(i), fineLabelIndexer.get(j), fineLexicon);

				// System.out.println(i + " " + sentence.get(i) + " " + labelIndexer.get(j) + "
				// " + emissionScore);

				if (!(emissionScore == Double.NaN) && !(emissionScore == Double.NEGATIVE_INFINITY)) {
					scoreFineBinary[i][i + 1][j] = emissionScore;
					// System.out.println("updating!");
					// //s.nextLine();

				}

			}
		}

		System.out.println("Finished bottomest fine pass");
		printFineScore();
		// s.nextLine();

		for (int i = 0; i < numWords; i++) { // looping over rules of sate[i]->state[j]

			// System.out.println("\n\nposition i " + i);
			for (int b = 0; b < numFineStates; b++) {
				// System.out.println(a + " " + labelIndexer.get(a));
				// System.out.println(ur.toString());
				if (scoreFineBinary[i][i + 1][b] != Double.NEGATIVE_INFINITY) {
					for (UnaryRule ur : fineUnaryClosure.getClosedUnaryRulesByChild(b)) {
						int a = ur.getParent();
						int aa = fineToCoarseMap[a];
						if (aa == -1) {
							scoreFineUnary[i][i + 1][a] = Double.NEGATIVE_INFINITY;
							continue;
						}
						double coarseScore = denominator + coarseUnaryBeta[i][i + 1][aa];
						if (coarseScore < threshold) {

							scoreFineUnary[i][i + 1][a] = Double.NEGATIVE_INFINITY;
							continue;
						}
						coarseScore += coarseUnaryAlpha[i][i + 1][aa];
						if (coarseScore < threshold) {

							scoreFineUnary[i][i + 1][a] = Double.NEGATIVE_INFINITY;
							continue;
						}
						System.out.println("a valid coarseScore is " + coarseScore + " " + denominator + " "
								+ coarseUnaryBeta[i][i + 1][aa] + " " + coarseUnaryAlpha[i][i + 1][aa]);

						double prob = ur.getScore() + scoreFineBinary[i][i + 1][b];
						// System.out.println(i + " " + a + " " + b + " " + ur.getScore() + " " +
						// scoreBinary[i][i + 1][b]
						// + " " + prob);
						if (scoreFineUnary[i][i + 1][a] < prob) {

							// System.out.println("updating");

							scoreFineUnary[i][i + 1][a] = prob;
							backFineUnary[i][i + 1][a] = b;
						}
					}
				}
			}
		}

		for (int diff = 2; diff <= numWords; diff++) { // alternating layers

			for (int i = 0; i <= (numWords - diff); i++) {
				for (int split = i + 1; split < i + diff; split++) {
					for (int b = 0; b < numFineStates; b++) {
						if (scoreFineUnary[i][split][b] != Double.NEGATIVE_INFINITY) {

							for (BinaryRule br : fineGrammar.getBinaryRulesByLeftChild(b)) {

								int rc = br.getRightChild();
								int lc = br.getLeftChild();
								int p = br.getParent();

								int pp = fineToCoarseMap[p];
								if (pp == -1) {
									scoreFineBinary[i][i + diff][p] = Double.NEGATIVE_INFINITY;
									continue;
								}
								double coarseScore = denominator + coarseBinaryBeta[i][i + diff][pp];

								if (coarseScore < threshold) {
									scoreFineBinary[i][i + diff][p] = Double.NEGATIVE_INFINITY;
									continue;
								}

								coarseScore += coarseBinaryAlpha[i][i + diff][pp];
								if (coarseScore < threshold) {
									scoreFineBinary[i][i + diff][p] = Double.NEGATIVE_INFINITY;
									continue;
								}
								if (scoreFineUnary[split][i + diff][rc] != Double.NEGATIVE_INFINITY) {
									double prob = br.getScore() + scoreFineUnary[i][split][lc]
											+ scoreFineUnary[split][i + diff][rc];
									// System.out.println(
									// br.toString() + " " + br.getScore() + " " + scoreUnary[split][i +
									// diff][rc]
									// + " " + scoreUnary[i][split][b] + " " + prob);
									if (prob > scoreFineBinary[i][i + diff][p]) {
										// System.out.println("updating binary");
										scoreFineBinary[i][i + diff][p] = prob;
										backFineBinarySplit[i][i + diff][p] = split;
										backFineBinaryLeft[i][i + diff][p] = lc;
										backFineBinaryRight[i][i + diff][p] = rc;
									}

								}
							}
						}
					}
				}
			}

			// printFineScore();
			// //s.nextLine();

			for (int i = 0; i <= (numWords - diff); i++) {
				for (int b = 0; b < numFineStates; b++) {
					if (scoreFineBinary[i][i + diff][b] != Double.NEGATIVE_INFINITY) {
						for (UnaryRule ur : fineUnaryClosure.getClosedUnaryRulesByChild(b)) { // a->b
							// System.out.println(ur.toString());
							int a = ur.getParent();
							int aa = fineToCoarseMap[a];
							if (aa == -1) {
								scoreFineUnary[i][i + diff][a] = Double.NEGATIVE_INFINITY;
								continue;

							}

							double coarseScore = denominator + coarseUnaryBeta[i][i + diff][aa];
							if (coarseScore < threshold) {
								scoreFineUnary[i][i + diff][a] = Double.NEGATIVE_INFINITY;
								continue;

							}
							coarseScore += coarseUnaryAlpha[i][i + diff][aa];
							if (coarseScore < threshold) {
								scoreFineUnary[i][i + diff][a] = Double.NEGATIVE_INFINITY;
								continue;

							}

							double prob = ur.getScore() + scoreFineBinary[i][i + diff][b];
							if (scoreFineUnary[i][i + diff][a] < prob) {
								// System.out.println("updating");
								scoreFineUnary[i][i + diff][a] = prob;
								backFineUnary[i][i + diff][a] = b;
							}
						}
					}
				}

			}

		}

		if (scoreFineUnary[0][numWords][0] == Double.NEGATIVE_INFINITY)
			return new Tree<String>("ROOT", Collections.singletonList(new Tree<String>("JUNK")));
		else {

			try {
				return TreeAnnotations.unAnnotateTree(backtrackUnary(0, numWords, 0, sentence));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		return null;

	}

	public Tree<String> backtrackUnary(int i, int j, int a, List<String> sentence) throws Exception {
		// TODO Auto-generated method stub

		// System.out.println("Currently backtracking unary i j a " + i + " " + j + " "
		// + a + " " + labelIndexer.get(a)
		// + " back track to a->b where b is " + backUnary[i][j][a] + " " +
		// labelIndexer.get(backUnary[i][j][a]));

		int b = backFineUnary[i][j][a];
		Tree<String> treeAsB = backtrackBinary(i, j, b, sentence);
		List<Tree<String>> treesUnderA = new ArrayList<Tree<String>>();

		List<Integer> pathBetweenAB = fineUnaryClosure
				.getPath(new UnaryRule(a, fineLabelIndexer.indexOf(treeAsB.getLabel())));
		// System.out.println("path between AB here " + pathBetweenAB + " where A is " +
		// labelIndexer.get(a) + " B is "
		// + labelIndexer.get(b));
		// System.out.print("full path here");
		// for (int node : pathBetweenAB)
		// System.out.print(" " + labelIndexer.get(node));
		// System.out.println();
		if (pathBetweenAB.size() == 1) {
			treesUnderA.addAll(treeAsB.getChildren());
		} else if (pathBetweenAB.size() == 2) {
			treesUnderA.add(treeAsB);
		} else if (pathBetweenAB.size() > 2) {

			Tree<String> treeAsPrevIntermediate = treeAsB;

			for (int ind = pathBetweenAB.size() - 2; ind >= 1; ind--) {
				int intermediate = pathBetweenAB.get(ind);
				List<Tree<String>> treesUnderIntermediate = new ArrayList<Tree<String>>();
				treesUnderIntermediate.add(treeAsPrevIntermediate);
				treeAsPrevIntermediate = new Tree<String>(fineLabelIndexer.get(intermediate), treesUnderIntermediate);
			}
			treesUnderA.add(treeAsPrevIntermediate);
		}

		Tree<String> finalTree = new Tree<String>(fineLabelIndexer.get(a), treesUnderA);
		// System.out.println(Trees.PennTreeRenderer.render(finalTree) + "\n");
		return finalTree;
	}

	private Tree<String> backtrackBinary(int i, int j, int a, List<String> sentence) throws Exception {
		// TODO Auto-generated method stub
		// System.out.println("Currently backtracking binary i j a " + i + " " + j + " "
		// + a + " " + labelIndexer.get(a));

		if (j == (i + 1)) {
			List<Tree<String>> treesUnderA = new ArrayList<Tree<String>>();
			treesUnderA.add(new Tree<String>(sentence.get(i)));
			// System.out.println("leaf node here " + i + " " + j + " " + sentence.get(i));
			return new Tree<String>(fineLabelIndexer.get(a), treesUnderA);
		}

		int split = backFineBinarySplit[i][j][a];
		int lc = backFineBinaryLeft[i][j][a];
		int rc = backFineBinaryRight[i][j][a];
		List<Tree<String>> treesUnderA = new ArrayList<Tree<String>>();
		Tree<String> treeAsLC = backtrackUnary(i, split, lc, sentence);
		Tree<String> treeAsRC = backtrackUnary(split, j, rc, sentence);
		treesUnderA.add(treeAsLC);
		treesUnderA.add(treeAsRC);

		Tree<String> finalTree = new Tree<String>(fineLabelIndexer.get(a), treesUnderA);
		// System.out.println(Trees.PennTreeRenderer.render(finalTree) + "\n");
		return finalTree;
	}

	private void printScores(List<String> sentence) {

		if (isPrint) {
			// TODO Auto-generated method stub
			int numWords = sentence.size();
			for (int i = 0; i <= numWords; i++) {
				for (int j = i; j <= numWords; j++) { // upper triangle
					System.out.println("i = " + i + " j = " + j);
					for (int a = 0; a < numCoarseStates; a++) {
						System.out.print(a + ": (coarseBinaryAlpha)" + coarseBinaryAlpha[i][j][a] + "\t");
						System.out.print(a + ": (coarseUnaryAlpha)" + coarseUnaryAlpha[i][j][a] + "\t");
						System.out.print(a + ": (coarseBinaryBeta)" + coarseBinaryBeta[i][j][a] + "\t");
						System.out.print(a + ": (coarseUnaryBeta)" + coarseUnaryBeta[i][j][a] + "\t");
						// System.out.print(a + ": (unary) " + scoreUnary[i][j][a] + "\t");
						// System.out.print(a + ": (unary) " + backUnary[i][j][a] + "\t");
						System.out.println();

					}
					System.out.println();
				}
				System.out.println();
			}
		}
	}

	private void printFineScore() {
		// TODO Auto-generated method stub
		if (isPrint) {
			int numWords = this.currentSentence.size();
			for (int i = 0; i <= numWords; i++) {
				for (int j = i; j <= numWords; j++) { // upper triangle
					System.out.println("i = " + i + " j = " + j);
					for (int a = 0; a < numFineStates; a++) {
						// System.out.print(a + ": (coarseBinaryAlpha)" + coarseBinaryAlpha[i][j][a]
						// +
						// "\t");
						// System.out.print(a + ": (coarseUnaryAlpha)" + coarseUnaryAlpha[i][j][a] +
						// "\t");
						// System.out.print(a + ": (coarseBinaryBeta)" + coarseBinaryBeta[i][j][a] +
						// "\t");
						// System.out.print(a + ": (coarseUnaryBeta)" + coarseUnaryBeta[i][j][a] +
						// "\t");
						System.out.print(a + ": (unary) " + scoreFineUnary[i][j][a] + "\t");
						System.out.print(a + ": (binary) " + scoreFineBinary[i][j][a] + "\t");
						// System.out.print(a + ": (unary) " + backUnary[i][j][a] + "\t");
						System.out.println();

					}
					System.out.println();
				}
				System.out.println();
			}
		}
	}
}
