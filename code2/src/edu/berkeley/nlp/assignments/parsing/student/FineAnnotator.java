package edu.berkeley.nlp.assignments.parsing.student;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import edu.berkeley.nlp.ling.Tree;

class FineAnnotator {
	static HashSet<String> INtags = new HashSet<String>();

	static String rawPrepositions = "atop amongst toward beyond after aboard over toward among astride "
			+ "along underneath alongside throughout under behind amid near outside upon en "
			+ "across during below above between beneath post besides into nearest around beside "
			+ "inside down within ago out up through fiscal via save v. vs. versus towards de on past";
	static String rawSubordinatingConjuctions = "than that though "
			+ "after although as because before in order that once since so that "
			+ "unless until when whenever where whereas wherever whether while";
	static String rawExcept = "but pending lest except minus without albeit despite unlike plus";

	static HashSet<String> prepositions = new HashSet<String>(Arrays.asList(rawPrepositions.split(" ")));
	static HashSet<String> subordinatingConjuctions = new HashSet<String>(
			Arrays.asList(rawSubordinatingConjuctions.split(" ")));
	static HashSet<String> except = new HashSet<String>(Arrays.asList(rawExcept.split(" ")));

	static String categorizeIN(String word) {
		word = word.toLowerCase();
		if (prepositions.contains(word))
			return "INPREP";
		if (subordinatingConjuctions.contains(word))
			return "INSUBCON";
		if (except.contains(word))
			return "INEXCEPT";
		INtags.add(word);
		return word;
	}

	static Tree<String> annotateTree(Tree<String> unannotated) {
		return binarizeTree(unannotated, "", "");
	}

	static Tree<String> binarizeTree(Tree<String> tree, String parent, String parent2) {
		String label = tree.getLabel();
		String parentLabel = parent.isEmpty() ? "" : "^" + parent;
		parentLabel = parentLabel + (parent2.isEmpty() ? "" : "^" + parent2);
		if (tree.isLeaf())
			return new Tree<String>(label);
		if (tree.getChildren().size() == 1) {
//			if (!tree.isPreTerminal() && !label.equals("ROOT")) {
//				parentLabel = parentLabel + "-U";
//			}
//			Tree<String> child = tree.getChildren().get(0);
//			if (child.getLabel().equals("RB") || child.getLabel().equals("DT")) {
//				parent = parent + "^U";
//			}
//			if (child.isLeaf() && tree.getLabel().equals("IN")) {
//				label = label + "^" + categorizeIN(child.getLabel());
//			}
			return new Tree<String>(label + parentLabel,
					Collections.singletonList(binarizeTree(tree.getChildren().get(0), label, parent)));
		}
//		assert !tree.getLabel().equals("IN");
		// otherwise, it's a binary-or-more local tree, so decompose it into a sequence
		// of binary and unary trees.
		String labelHeader = "@" + label + "->";
		Tree<String> intermediateTree = binarizeTreeHelper(tree, 0, labelHeader, "", "", label, parent);
		return new Tree<String>(label + parentLabel, intermediateTree.getChildren());
	}

	static Tree<String> binarizeTreeHelper(Tree<String> tree, int numChildrenGenerated, String labelHeader, String prev,
			String prev2, String parent, String parent2) {
		Tree<String> leftTree = tree.getChildren().get(numChildrenGenerated);
		List<Tree<String>> children = new ArrayList<Tree<String>>();
		children.add(binarizeTree(leftTree, parent, parent2));
		if (numChildrenGenerated < tree.getChildren().size() - 1) {
			Tree<String> rightTree = binarizeTreeHelper(tree, numChildrenGenerated + 1, labelHeader,
					leftTree.getLabel(), prev, parent, parent2);
			children.add(rightTree);
		}
		return new Tree<String>(labelHeader + (prev2.isEmpty() ? "" : "_" + prev2) + (prev.isEmpty() ? "" : "_" + prev),
				children);
	}
}
