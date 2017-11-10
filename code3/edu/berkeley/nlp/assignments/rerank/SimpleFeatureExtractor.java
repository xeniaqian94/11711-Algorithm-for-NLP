package edu.berkeley.nlp.assignments.rerank;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import edu.berkeley.nlp.ling.AnchoredTree;
import edu.berkeley.nlp.ling.Constituent;
import edu.berkeley.nlp.ling.Tree;
import edu.berkeley.nlp.util.Indexer;

/**
 * Baseline feature extractor for k-best lists of parses. Note that this does
 * not implement Featurizer, though you can adapt it to do so.
 * 
 * @author gdurrett
 *
 */
public class SimpleFeatureExtractor {

  /**
   * 
   * @param kbestList
   * @param idx
   *          The index of the tree in the k-best list to extract features for
   * @param featureIndexer
   * @param addFeaturesToIndexer
   *          True if we should add new features to the indexer, false
   *          otherwise. When training, you want to make sure you include all
   *          possible features, but adding features at test time is pointless
   *          (since you won't have learned weights for those features anyway).
   * @return
   */
  public int[] extractFeatures(KbestList kbestList, int idx, Indexer<String> featureIndexer, boolean addFeaturesToIndexer) {
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
        // Fires a feature based on the identity of a nonterminal rule
        // production. This allows the model to learn features
        // roughly equivalent to those in an unbinarized coarse grammar.
        String rule = "Rule=" + subtree.getLabel() + " ->";
        for (AnchoredTree<String> child : subtree.getChildren()) {
          rule += " " + child.getLabel();
        }
        addFeature(rule, feats, featureIndexer, addFeaturesToIndexer);
      }
    }
    // Add your own features here!

    int[] featsArr = new int[feats.size()];
    for (int i = 0; i < feats.size(); i++) {
      featsArr[i] = feats.get(i).intValue();
    }
    return featsArr;
  }

  /**
   * Shortcut method for indexing a feature and adding it to the list of
   * features.
   * 
   * @param feat
   * @param feats
   * @param featureIndexer
   * @param addNew
   */
  private void addFeature(String feat, List<Integer> feats, Indexer<String> featureIndexer, boolean addNew) {
    if (addNew || featureIndexer.contains(feat)) {
      feats.add(featureIndexer.addAndGetIndex(feat));
    }
  }

}
