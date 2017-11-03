package edu.berkeley.nlp.assignments.parsing.student;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import edu.berkeley.nlp.ling.Tree;
import edu.berkeley.nlp.ling.Trees;
import edu.berkeley.nlp.util.Filter;

public class TreeMarkovAnnotations {

	/**
	 * This performs lossless binarization. You'll need to define your own function
	 * to do more intelligent markovization.
	 * 
	 * @param unAnnotatedTree
	 * @return
	 */
	static int h = GenerativeParserFactory.h;
	static int v = GenerativeParserFactory.v;
	static boolean tagPA = GenerativeParserFactory.tagPA;
	static boolean tagSplitting = GenerativeParserFactory.tagSplitting;

	static HashSet<String> prepositions = new HashSet<String>(
			Arrays.asList(new String("atop amongst toward beyond after aboard over toward among astride "
					+ "along underneath alongside throughout under behind amid near outside upon en "
					+ "across during below above between beneath post besides into nearest around beside "
					+ "inside down within ago out up through fiscal via save v. vs. versus towards de on past")
							.split(" ")));
	static HashSet<String> subordinatingConjuctions = new HashSet<String>(Arrays
			.asList(new String("than that though after although as because before in order that once since so that "
					+ "unless until when whenever where whereas wherever whether while").split(" ")));
	static HashSet<String> except = new HashSet<String>(
			Arrays.asList(new String("but pending lest except minus without albeit despite unlike plus").split(" ")));
	static HashSet<String> complementizer = new HashSet<String>(
			Arrays.asList(new String("if whether because unless since").split(" ")));

	static String splitINTag(String word) {
		word = word.toLowerCase();
		if (prepositions.contains(word))
			return "INPREP";
		else if (subordinatingConjuctions.contains(word))
			return "INSUBCON";
		else if (except.contains(word))
			return "INEXCEPT";
		else if (complementizer.contains(word))
			return "INCOMP";
		return "";
	}

	public static Tree<String> annotateBinarization(Tree<String> unAnnotatedTree) {

		// List<String> parentLabel = new ArrayList<String>(v - 1);

		// Tree<String> va = verticalAnnotate(unAnnotatedTree);

		String[] parents = new String[v - 1];
		for (int i = 0; i < v - 1; i++)
			parents[i] = "";

		return binarizeTree(unAnnotatedTree, parents);
	}

	private static Tree<String> binarizeTree(Tree<String> tree, String[] parents) {
		String label = tree.getLabel();
		String labelWithParents = label;
		for (int counter = 0; counter < parents.length; counter++) {
			if (parents[counter].length() > 0)
				labelWithParents += "^" + parents[counter];
		}
		String[] parentsChild = new String[v - 1];
		if (v > 2)
			System.arraycopy(parents, 0, parentsChild, 1, v - 2);
		if (v > 1)
			parentsChild[0] = label;
		if (tree.isLeaf())
			return new Tree<String>(label);
		if (tree.getChildren().size() == 1) {
			String actualLabel = labelWithParents;

			if (!tagPA && tree.isPreTerminal())
				actualLabel = label;

			if (GenerativeParserFactory.tagSplitting) {

				if (!tree.isPreTerminal() && !label.equals("ROOT")) { // UNARY-INTERNAL
					actualLabel = actualLabel + "-U";
				}
				Tree<String> child = tree.getChildren().get(0);
				if (child.getLabel().equals("DT") || child.getLabel().equals("RB")) { // Two examples of UNARY-EXTERNAL
					parentsChild[0] = parentsChild[0] + "^U";
				}

				if (child.isLeaf() && label.equals("IN")) {
					String inTag = splitINTag(child.getLabel());
					if (inTag.length() > 0)

						actualLabel = actualLabel + "^" + inTag;
				}
			}

			return new Tree<String>(actualLabel,
					Collections.singletonList(binarizeTree(tree.getChildren().get(0), parentsChild)));
		}

		// otherwise, it's a binary-or-more local tree, so decompose it into a sequence
		// of binary and unary trees.
		String intermediateLabel = "@" + label + "->";
		Tree<String> intermediateTree = binarizeTreeHelper(tree, 0, intermediateLabel, parentsChild);
		return new Tree<String>(labelWithParents, intermediateTree.getChildren());
	}

	private static Tree<String> binarizeTreeHelper(Tree<String> tree, int numChildrenGenerated,
			String intermediateLabel, String[] parentsChild) {
		Tree<String> leftTree = tree.getChildren().get(numChildrenGenerated);
		List<Tree<String>> children = new ArrayList<Tree<String>>();

		// TODO!!!!!!!!!

		children.add(binarizeTree(leftTree, parentsChild));
		String hisLabel = intermediateLabel;
		if (numChildrenGenerated < tree.getChildren().size() - 1) {

			Tree<String> rightTree = binarizeTreeHelper(tree, numChildrenGenerated + 1, intermediateLabel,
					parentsChild);
			children.add(rightTree);
		}
		for (int i = Math.max(0, numChildrenGenerated - h); i < numChildrenGenerated; i++) {

			hisLabel += "_" + tree.getChildren().get(i).getLabel();
		}
		return new Tree<String>(hisLabel, children);
	}

	public static Tree<String> unAnnotateTree(Tree<String> annotatedTree) {
		// Remove intermediate nodes (labels beginning with "@"
		// Remove all material on node labels which follow their base symbol (cuts
		// anything after <,>,^,=,_ or ->)
		// Examples: a node with label @NP->DT_JJ will be spliced out, and a node with
		// label NP^S will be reduced to NP
		Tree<String> debinarizedTree = Trees.spliceNodes(annotatedTree, new Filter<String>() {
			public boolean accept(String s) {
				return s.startsWith("@");
			}
		});
		Tree<String> unAnnotatedTree = (new Trees.LabelNormalizer()).transformTree(debinarizedTree);
		return unAnnotatedTree;
	}

	public static void setHV(int hh, int vv, boolean tagPAA) {
		// TODO Auto-generated method stub
		h = hh;
		v = vv;
		tagPA = tagPAA;

	}
}
