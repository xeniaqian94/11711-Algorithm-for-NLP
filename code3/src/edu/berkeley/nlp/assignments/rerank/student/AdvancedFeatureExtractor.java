package edu.berkeley.nlp.assignments.rerank.student;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import edu.berkeley.nlp.assignments.rerank.KbestList;
import edu.berkeley.nlp.assignments.rerank.SimpleFeatureExtractor;
import edu.berkeley.nlp.assignments.rerank.SurfaceHeadFinder;
import edu.berkeley.nlp.ling.AnchoredTree;
import edu.berkeley.nlp.ling.Constituent;
import edu.berkeley.nlp.ling.Tree;
import edu.berkeley.nlp.util.Indexer;

public class AdvancedFeatureExtractor extends SimpleFeatureExtractor {

	public List<Integer> extractFeauturesShen(KbestList kbestList, int idx, Indexer<String> featureIndexer,
			boolean addFeaturesToIndexer) {
		Tree<String> tree = kbestList.getKbestTrees().get(idx);
		// Converts the tree
		// (see below)
		AnchoredTree<String> anchoredTree = AnchoredTree.fromTree(tree);
		// If you just want to iterate over labeled spans, use the constituent list
		Collection<Constituent<String>> constituents = tree.toConstituentList();
		// You can fire features on parts of speech or words
		List<String> poss = tree.getPreTerminalYield();
		List<String> words = tree.getYield();
		// Allows you to find heads of spans of preterminals. Use this to fire
		// dependency-based features
		// like those discussed in Charniak and Johnson
		SurfaceHeadFinder shf = new SurfaceHeadFinder();

		// FEATURE COMPUTATION
		List<Integer> feats = new ArrayList<Integer>();
		// Fires a feature based on the position in the k-best list. This should
		// allow the model to learn that
		// high-up trees
		addFeature("Posn=" + idx, feats, featureIndexer, addFeaturesToIndexer);

		for (AnchoredTree<String> subtree : anchoredTree.toSubTreeList()) {
			if (!subtree.isPreTerminal() && !subtree.isLeaf()) {
				String parent = "Parent=" + subtree.getLabel();
				// addFeature(parent, feats, featureIndexer, addFeaturesToIndexer);

				int counter = 0;
				String rule = "Rule=" + subtree.getLabel() + " ->";
				String rule_ngram = "nngram=" + subtree.getLabel() + " ->";
				for (AnchoredTree<String> child : subtree.getChildren()) {
					rule += " " + child.getLabel();
					if (!child.isLeaf())
						counter += 1;
					rule_ngram += " " + child.getLabel();
					if ((counter == 1 && subtree.getChildren().size() != 1)
							|| (counter == 2 && subtree.getChildren().size() != 2)
							|| (counter == 3 && subtree.getChildren().size() != 3)) {
						addFeature(rule_ngram, feats, featureIndexer, addFeaturesToIndexer);
					}
				}
				addFeature(rule, feats, featureIndexer, addFeaturesToIndexer);
				String l = getBucket(subtree.getSpanLength()) + ", " + rule;
				addFeature(l, feats, featureIndexer, addFeaturesToIndexer);
				// l=getBucket(subtree.getSpanLength())+", "+parent;
				// addFeature(l,feats,featureIndexer,addFeaturesToIndexer);

				int si = subtree.getStartIdx();
				int ei = subtree.getEndIdx();

				String spanShape = "";
				for (int j = si; j < ei; j++) {
					String word = words.get(j);
					if (word.charAt(0) >= 'A' && word.charAt(0) <= 'Z')
						spanShape = spanShape + "X";
					else if (word.charAt(0) >= 'a' && word.charAt(0) <= 'z')
						spanShape = spanShape + "x";
					else if (Character.isDigit(word.charAt(0)))
						spanShape = spanShape + "1";
					else if (word.length() == 1)
						spanShape = spanShape + word;
				}
				addFeature(rule + "=" + spanShape, feats, featureIndexer, addFeaturesToIndexer);

				String firstword = words.get(si);
				addFeature(firstword + "_1_" + rule, feats, featureIndexer, addFeaturesToIndexer);
				// addFeature(firstword + "_1_" + parent, feats, featureIndexer,
				// addFeaturesToIndexer);

				String lastword = words.get(ei - 1);
				addFeature(lastword + "_e_" + rule, feats, featureIndexer, addFeaturesToIndexer);
				// addFeature(lastword + "_e_" + parent, feats, featureIndexer,
				// addFeaturesToIndexer);

				if (si - 1 >= 0) {
					String previousword = words.get(si - 1);
					// addFeature(previousword + ">" + parent, feats, featureIndexer,
					// addFeaturesToIndexer);
					addFeature(previousword + ">" + rule, feats, featureIndexer, addFeaturesToIndexer);
				}
				if (ei < words.size()) {
					String nextword = words.get(ei);
					// addFeature(nextword + "<" + parent, feats, featureIndexer,
					// addFeaturesToIndexer);
					addFeature(nextword + "<" + rule, feats, featureIndexer, addFeaturesToIndexer);

				}
				if (counter == 2) {
					AnchoredTree leftchild = subtree.getChildren().get(0);
					int splitindex = leftchild.getEndIdx();
					if (splitindex - 1 >= 0) {
						String wrdbefsplit = words.get(splitindex - 1);
						addFeature(wrdbefsplit + "<->" + parent, feats, featureIndexer, addFeaturesToIndexer);
						// addFeature(wrdbefsplit+"<->"+rule,feats,featureIndexer,addFeaturesToIndexer);
					}
					if (splitindex < words.size()) {
						String wrdaftersplit = words.get(splitindex);
						addFeature(wrdaftersplit + "<-" + parent, feats, featureIndexer, addFeaturesToIndexer);
						// addFeature(wrdaftersplit+"<->"+rule,feats,featureIndexer,addFeaturesToIndexer);
					}
				}
				int rightBranchL = 0;
				int nc = subtree.getChildren().size();
				AnchoredTree rightmostnode = subtree;
				while (nc >= 1) {
					rightmostnode = (AnchoredTree) rightmostnode.getChildren().get(nc - 1);
					rightBranchL += 1;
					nc = rightmostnode.getChildren().size();
				}
				l = getBucket(rightBranchL) + ":" + parent;
				addFeature(l, feats, featureIndexer, addFeaturesToIndexer);

			}
		}
		return feats;
	}

	// best of all
	public List<Integer> extractFeauturesShenNew(KbestList kbestList, int idx, Indexer<String> featureIndexer,
			boolean addFeaturesToIndexer) {
		Tree<String> tree = kbestList.getKbestTrees().get(idx);
		// Converts the tree
		// (see below)
		AnchoredTree<String> anchoredTree = AnchoredTree.fromTree(tree);
		// If you just want to iterate over labeled spans, use the constituent list
		Collection<Constituent<String>> constituents = tree.toConstituentList();
		// You can fire features on parts of speech or words
		List<String> poss = tree.getPreTerminalYield();
		List<String> words = tree.getYield();
		// Allows you to find heads of spans of preterminals. Use this to fire
		// dependency-based features
		// like those discussed in Charniak and Johnson
		SurfaceHeadFinder shf = new SurfaceHeadFinder();

		// FEATURE COMPUTATION
		List<Integer> feats = new ArrayList<Integer>();
		// Fires a feature based on the position in the k-best list. This should
		// allow the model to learn that
		// high-up trees
		addFeature("Posn=" + idx, feats, featureIndexer, addFeaturesToIndexer);

		for (AnchoredTree<String> subtree : anchoredTree.toSubTreeList()) {
			if (!subtree.isPreTerminal() && !subtree.isLeaf()) {
				String parent = "Parent=" + subtree.getLabel();
				// addFeature(parent, feats, featureIndexer, addFeaturesToIndexer);

				int counter = 0;
				String rule = "Rule=" + subtree.getLabel() + " ->";
				String rule_ngram = "nngram=" + subtree.getLabel() + " ->";
				for (AnchoredTree<String> child : subtree.getChildren()) {
					rule += " " + child.getLabel();
					if (!child.isLeaf())
						counter += 1;
					rule_ngram += " " + child.getLabel();
					if ((counter == 1 && subtree.getChildren().size() != 1)
							|| (counter == 2 && subtree.getChildren().size() != 2)
							|| (counter == 3 && subtree.getChildren().size() != 3)) {

						// addFeature(rule_ngram, feats, featureIndexer, addFeaturesToIndexer);

					}
				}
				addFeature(rule, feats, featureIndexer, addFeaturesToIndexer);
				String l = getBucket(subtree.getSpanLength()) + ", " + rule;

				addFeature(l, feats, featureIndexer, addFeaturesToIndexer);

				// l=getBucket(subtree.getSpanLength())+", "+parent;
				// addFeature(l,feats,featureIndexer,addFeaturesToIndexer);

				int si = subtree.getStartIdx();
				int ei = subtree.getEndIdx();

				String spanShape = "";
				for (int j = si; j < ei; j++) {
					String word = words.get(j);
					if (word.charAt(0) >= 'A' && word.charAt(0) <= 'Z')
						spanShape = spanShape + "X";
					else if (word.charAt(0) >= 'a' && word.charAt(0) <= 'z')
						spanShape = spanShape + "x";
					else if (Character.isDigit(word.charAt(0)))
						spanShape = spanShape + "1";
					else if (word.length() == 1)
						spanShape = spanShape + word;
				}

				addFeature(rule + "=" + spanShape, feats, featureIndexer, addFeaturesToIndexer);

				String firstword = words.get(si);
				addFeature(firstword + "_1_" + rule, feats, featureIndexer, addFeaturesToIndexer);

				String lastword = words.get(ei - 1);
				addFeature(lastword + "_e_" + rule, feats, featureIndexer, addFeaturesToIndexer);

				if (si - 1 >= 0) {
					String previousword = words.get(si - 1);
					addFeature(previousword + ">" + rule, feats, featureIndexer, addFeaturesToIndexer);
				}
				if (ei < words.size()) {
					String nextword = words.get(ei);
					addFeature(nextword + "<" + rule, feats, featureIndexer, addFeaturesToIndexer);

				}
				if (counter == 2) {
					AnchoredTree leftchild = subtree.getChildren().get(0);
					int splitindex = leftchild.getEndIdx();
					if (splitindex - 1 >= 0) {
						String wrdbefsplit = words.get(splitindex - 1);
						// addFeature(wrdbefsplit + "<->" + parent, feats, featureIndexer,
						// addFeaturesToIndexer);

					}
					if (splitindex < words.size()) {
						String wrdaftersplit = words.get(splitindex);
						// addFeature(wrdaftersplit+"<-"+parent,feats,featureIndexer,addFeaturesToIndexer);

					}
				}
				int rightBranchL = 0;
				int nc = subtree.getChildren().size();
				AnchoredTree rightmostnode = subtree;
				while (nc >= 1) {
					rightmostnode = (AnchoredTree) rightmostnode.getChildren().get(nc - 1);
					rightBranchL += 1;
					nc = rightmostnode.getChildren().size();
				}
				l = getBucket(rightBranchL) + ":" + parent;
				// addFeature(l,feats,featureIndexer,addFeaturesToIndexer);

			}
		}
		return feats;
	}

	public List<Integer> extractFeauturesShenNew2(KbestList kbestList, int idx, Indexer<String> featureIndexer,
			boolean addFeaturesToIndexer) {
		Tree<String> tree = kbestList.getKbestTrees().get(idx);
		// Converts the tree
		// (see below)
		AnchoredTree<String> anchoredTree = AnchoredTree.fromTree(tree);
		// If you just want to iterate over labeled spans, use the constituent list
		Collection<Constituent<String>> constituents = tree.toConstituentList();
		// You can fire features on parts of speech or words
		List<String> poss = tree.getPreTerminalYield();
		List<String> words = tree.getYield();
		// Allows you to find heads of spans of preterminals. Use this to fire
		// dependency-based features
		// like those discussed in Charniak and Johnson
		SurfaceHeadFinder shf = new SurfaceHeadFinder();

		// FEATURE COMPUTATION
		List<Integer> feats = new ArrayList<Integer>();
		// Fires a feature based on the position in the k-best list. This should
		// allow the model to learn that
		// high-up trees
		addFeature("Posn=" + idx, feats, featureIndexer, addFeaturesToIndexer);

		for (AnchoredTree<String> subtree : anchoredTree.toSubTreeList()) {
			if (!subtree.isPreTerminal() && !subtree.isLeaf()) {
				String parent = "Parent=" + subtree.getLabel();
				// addFeature(parent, feats, featureIndexer, addFeaturesToIndexer);

				int counter = 0;
				String rule = "Rule=" + subtree.getLabel() + " ->";
				String rule_ngram = "nngram=" + subtree.getLabel() + " ->";
				for (AnchoredTree<String> child : subtree.getChildren()) {
					rule += " " + child.getLabel();
					if (!child.isLeaf())
						counter += 1;
					rule_ngram += " " + child.getLabel();
					if ((counter == 1 && subtree.getChildren().size() != 1)
							|| (counter == 2 && subtree.getChildren().size() != 2)
							|| (counter == 3 && subtree.getChildren().size() != 3)) {

						// addFeature(rule_ngram, feats, featureIndexer, addFeaturesToIndexer);

					}
				}
				addFeature(rule, feats, featureIndexer, addFeaturesToIndexer);
				String l = getBucket(subtree.getSpanLength()) + ", " + rule;

				addFeature(l, feats, featureIndexer, addFeaturesToIndexer);

				// l=getBucket(subtree.getSpanLength())+", "+parent;
				// addFeature(l,feats,featureIndexer,addFeaturesToIndexer);

				int si = subtree.getStartIdx();
				int ei = subtree.getEndIdx();

				String spanShape = "";
				for (int j = si; j < ei; j++) {
					String word = words.get(j);
					if (word.charAt(0) >= 'A' && word.charAt(0) <= 'Z')
						spanShape = spanShape + "X";
					else if (word.charAt(0) >= 'a' && word.charAt(0) <= 'z')
						spanShape = spanShape + "x";
					else if (Character.isDigit(word.charAt(0)))
						spanShape = spanShape + "1";
					else if (word.length() == 1)
						spanShape = spanShape + word;
				}

				addFeature(rule + "=" + spanShape, feats, featureIndexer, addFeaturesToIndexer);

				String firstword = words.get(si);
				addFeature(firstword + "_1_" + rule, feats, featureIndexer, addFeaturesToIndexer);

				String firstPOS = poss.get(si);
				addFeature(firstPOS + "_sPOS_" + rule, feats, featureIndexer, addFeaturesToIndexer);

				String lastword = words.get(ei - 1);
				addFeature(lastword + "_e_" + rule, feats, featureIndexer, addFeaturesToIndexer);

				String lastPOS = poss.get(ei - 1);
				addFeature(lastPOS + "_ePOS_" + rule, feats, featureIndexer, addFeaturesToIndexer);

				if (si - 1 >= 0) {
					String previousword = words.get(si - 1);
					addFeature(previousword + ">" + rule, feats, featureIndexer, addFeaturesToIndexer);
				}
				if (ei < words.size()) {
					String nextword = words.get(ei);
					addFeature(nextword + "<" + rule, feats, featureIndexer, addFeaturesToIndexer);

				}
				if (counter == 2) {
					AnchoredTree leftchild = subtree.getChildren().get(0);
					int splitindex = leftchild.getEndIdx();
					if (splitindex - 1 >= 0) {
						String wrdbefsplit = words.get(splitindex - 1);
						// addFeature(wrdbefsplit + "<->" + parent, feats, featureIndexer,
						// addFeaturesToIndexer);

					}
					if (splitindex < words.size()) {
						String wrdaftersplit = words.get(splitindex);
						// addFeature(wrdaftersplit+"<-"+parent,feats,featureIndexer,addFeaturesToIndexer);

					}
				}
				int rightBranchL = 0;
				int nc = subtree.getChildren().size();
				AnchoredTree rightmostnode = subtree;
				while (nc >= 1) {
					rightmostnode = (AnchoredTree) rightmostnode.getChildren().get(nc - 1);
					rightBranchL += 1;
					nc = rightmostnode.getChildren().size();
				}
				l = getBucket(rightBranchL) + ":" + parent;
				// addFeature(l,feats,featureIndexer,addFeaturesToIndexer);

			}
		}
		return feats;
	}

	public List<Integer> extractFeauturesUs(KbestList kbestList, int idx, Indexer<String> featureIndexer,
			boolean addFeaturesToIndexer) {

		List<Integer> feats = new ArrayList<Integer>();

		Tree<String> tree = kbestList.getKbestTrees().get(idx);
		// Converts the tree
		// (see below)
		AnchoredTree<String> anchoredTree = AnchoredTree.fromTree(tree);
		// If you just want to iterate over labeled spans, use the constituent list
		Collection<Constituent<String>> constituents = tree.toConstituentList();
		// You can fire features on parts of speech or words
		List<String> poss = tree.getPreTerminalYield();
		List<String> words = tree.getYield();
		// Allows you to find heads of spans of preterminals. Use this to fire
		// dependency-based features
		// like those discussed in Charniak and Johnson
		SurfaceHeadFinder shf = new SurfaceHeadFinder();

		// FEATURE COMPUTATION

		// Fires a feature based on the position in the k-best list. This should
		// allow the model to learn that
		// high-up trees
		addFeature("Posn=" + idx, feats, featureIndexer, addFeaturesToIndexer);

		for (AnchoredTree<String> subtree : anchoredTree.toSubTreeList()) {
			if (!subtree.isPreTerminal() && !subtree.isLeaf()) {
				String parent = "Parent=" + subtree.getLabel();
				// addFeature(parent, feats, featureIndexer, addFeaturesToIndexer);

				int counter = 0;
				String rule = "Rule=" + subtree.getLabel() + " ->";
				String rule_ngram = "nngram=" + subtree.getLabel() + " ->";
				for (AnchoredTree<String> child : subtree.getChildren()) {
					rule += " " + child.getLabel();
					if (!child.isLeaf())
						counter += 1;
					rule_ngram += " " + child.getLabel();
					if ((counter == 1 && subtree.getChildren().size() != 1)
							|| (counter == 2 && subtree.getChildren().size() != 2)
							|| (counter == 3 && subtree.getChildren().size() != 3)) {
						addFeature(rule_ngram, feats, featureIndexer, addFeaturesToIndexer);
					}
				}
				List<AnchoredTree<String>> children = subtree.getChildren();

				if (children.size() >= 2) {
					for (int i = 0; i < children.size() - 1; i++) {
						addFeature("Bigram=" + subtree.getLabel() + "->" + children.get(i).getLabel() + "_"
								+ children.get(i + 1).getLabel(), feats, featureIndexer, addFeaturesToIndexer);
					}
				}

				addFeature(rule, feats, featureIndexer, addFeaturesToIndexer);
				String l = getBucket(subtree.getSpanLength()) + ", " + rule;
				addFeature(l, feats, featureIndexer, addFeaturesToIndexer);

				int si = subtree.getStartIdx();
				int ei = subtree.getEndIdx();

				// spanShape of Captitalization or punctuation

				String spanShape = "";
				for (int j = si; j < ei; j++) {
					String word = words.get(j);
					if (word.charAt(0) >= 'A' && word.charAt(0) <= 'Z')
						spanShape = spanShape + "X";
					// else if (word.charAt(0) >= 'a' && word.charAt(0) <= 'z')
					// spanShape = spanShape + "x";
					// else if (Character.isDigit(word.charAt(0)))
					// spanShape = spanShape + "1";
					else if (word.length() == 1)
						spanShape = spanShape + "p";
					else
						spanShape = spanShape + "N";

				}
				addFeature(rule + "=" + spanShape, feats, featureIndexer, addFeaturesToIndexer);

				String firstword = words.get(si);
				addFeature(firstword + "_sWord_" + rule, feats, featureIndexer, addFeaturesToIndexer);

				String firstwordPOS = poss.get(si);
				addFeature(firstwordPOS + "_sPOS_" + rule, feats, featureIndexer, addFeaturesToIndexer);

				String lastword = words.get(ei - 1);
				addFeature(lastword + "_eWord_" + rule, feats, featureIndexer, addFeaturesToIndexer);

				String lastwordPOS = poss.get(ei - 1);
				addFeature(lastwordPOS + "_ePOS_" + rule, feats, featureIndexer, addFeaturesToIndexer);

				if (si - 1 >= 0) {
					String previousword = words.get(si - 1);
					// addFeature(previousword + ">" + parent, feats, featureIndexer,
					// addFeaturesToIndexer);
					addFeature(previousword + ">" + rule, feats, featureIndexer, addFeaturesToIndexer);
				}
				if (ei < words.size()) {
					String nextword = words.get(ei);
					// addFeature(nextword + "<" + parent, feats, featureIndexer,
					// addFeaturesToIndexer);
					addFeature(nextword + "<" + rule, feats, featureIndexer, addFeaturesToIndexer);

				}
				if (counter == 2) {
					AnchoredTree leftchild = subtree.getChildren().get(0);
					int splitindex = leftchild.getEndIdx();
					if (splitindex - 1 >= 0) {
						String wrdbefsplit = words.get(splitindex - 1);
						addFeature(wrdbefsplit + "<->" + parent, feats, featureIndexer, addFeaturesToIndexer);
						// addFeature(wrdbefsplit+"<->"+rule,feats,featureIndexer,addFeaturesToIndexer);
					}
					if (splitindex < words.size()) {
						String wrdaftersplit = words.get(splitindex);
						addFeature(wrdaftersplit + "<-" + parent, feats, featureIndexer, addFeaturesToIndexer);
						// addFeature(wrdaftersplit+"<->"+rule,feats,featureIndexer,addFeaturesToIndexer);
					}
				}
				int rightBranchL = 0;
				int nc = subtree.getChildren().size();
				AnchoredTree rightmostnode = subtree;
				while (nc >= 1) {
					rightmostnode = (AnchoredTree) rightmostnode.getChildren().get(nc - 1);
					rightBranchL += 1;
					nc = rightmostnode.getChildren().size();
				}
				l = getBucket(rightBranchL) + ":" + parent;
				addFeature(l, feats, featureIndexer, addFeaturesToIndexer);

			}
		}
		return feats;
	}

	@Override
	public int[] extractFeatures(KbestList kbestList, int idx, Indexer<String> featureIndexer,
			boolean addFeaturesToIndexer) {
		//best of ALL
//		List<Integer> feats = extractFeauturesShenNew(kbestList, idx, featureIndexer, addFeaturesToIndexer);
		
		
		
		//new attempt 
		List<Integer> feats = extractFeauturesShenNew2(kbestList, idx, featureIndexer, addFeaturesToIndexer);
		
		
		// Add your own features here!

		int[] featsArr = new int[feats.size()];
		for (int i = 0; i < feats.size(); i++) {
			featsArr[i] = feats.get(i).intValue();
		}
		return featsArr;
	}

	private void addFeature(String feat, List<Integer> feats, Indexer<String> featureIndexer, boolean addNew) {
		if (addNew || featureIndexer.contains(feat)) {
			feats.add(featureIndexer.addAndGetIndex(feat));
		}
	}

	private String getBucket(int l) {
		if (l <= 5)
			return Integer.toString(l);
		if (l <= 10)
			return "10";
		if (l <= 20)
			return "20";
		return ">=21";
	}

}
