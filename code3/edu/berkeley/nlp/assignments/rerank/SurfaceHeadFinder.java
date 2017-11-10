package edu.berkeley.nlp.assignments.rerank;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Head finder that takes a list of preterminals and returns a prediction of
 * their head. Heads are generally syntactic, though the head of a noun phrase
 * is not the determiner (instead the semantic head is used).
 * 
 * @author gdurrett
 *
 */
public class SurfaceHeadFinder {

  private Map<String,Boolean> searchDirections;
  private Map<String,Set<String>> validHeads;

  private Set<String> npHeads = new HashSet<String>(Arrays.asList(new String[] { "NN", "NNP", "NNPS", "NNS", "NX", "POS", "JJR", "$", "PRN" }));
  private Set<String> npBlockers = new HashSet<String>(Arrays.asList(new String[] { ",", "WDT", "TO", "IN", "-LRB-", ":", "CC", "(" }));

  public SurfaceHeadFinder() {
    this.searchDirections = new HashMap<String,Boolean>();
    searchDirections.put("ADJP", true);
    searchDirections.put("ADVP", false);
    searchDirections.put("QP", true);
    searchDirections.put("PP", true);
    searchDirections.put("S", true);
    searchDirections.put("VP", true);

    this.validHeads = new HashMap<String,Set<String>>();
    validHeads.put("ADJP", new HashSet<String>(Arrays.asList(new String[] { "NNS", "NN", "$", "JJ", "VBN", "VBG", "JJR", "JJS" })));
    validHeads.put("ADVP", new HashSet<String>(Arrays.asList(new String[] { "RB", "RBR", "RBS", "FW" })));
    validHeads.put("QP", new HashSet<String>(Arrays.asList(new String[] { "$", "IN", "CD" })));
    validHeads.put("PP", new HashSet<String>(Arrays.asList(new String[] { "IN", "TO", "VBG", "VBN", "RP", "FW" })));
    validHeads.put("S", new HashSet<String>(Arrays.asList(new String[] { "TO", "VBD", "VBN", "MD", "VBZ", "VB", "VBG", "VBP" })));
    validHeads.put("VP", new HashSet<String>(Arrays.asList(new String[] { "TO", "VBD", "VBN", "MD", "VBZ", "VB", "VBG", "VBP" })));

  }

  /**
   * @param label
   *          The nonterminal label
   * @param preterminals
   *          The list of preterminals for which we want to extract the head
   * @return The integer index of the head
   */
  public int findHead(String label, List<String> preterminals) {
    if (label.equals("NP")) {
      return searchFindLastBefore(preterminals, true, npHeads, npBlockers);
    } else if (label.equals("PRN")) {
      return (preterminals.size() > 1 ? 1 : 0);
    } else if (validHeads.containsKey(label)) {
      return searchFindFirst(preterminals, true, validHeads.get(label));
    } else {
      return 0;
    }
  }

  private int searchFindFirst(List<String> preterminals, boolean leftToRight, Set<String> validHeads) {
    int start = (leftToRight ? 0 : preterminals.size() - 1);
    int end = (leftToRight ? preterminals.size() : -1);
    int headIdx = -1;
    for (int i = start; i != end && headIdx == -1; i += (leftToRight ? 1 : -1)) {
      if (validHeads.contains(preterminals.get(i))) {
        headIdx = i;
      }
    }
    if (headIdx < 0 || headIdx >= preterminals.size()) {
      headIdx = start;
    }
    return headIdx;
  }

  private int searchFindLastBefore(List<String> preterminals, boolean leftToRight, Set<String> validHeads, Set<String> blockers) {
    int start = (leftToRight ? 0 : preterminals.size() - 1);
    int end = (leftToRight ? preterminals.size() : -1);
    int headIdx = -1;
    boolean blocked = false;
    int i = start;
    for (; i != end && !blocked; i += (leftToRight ? 1 : -1)) {
      if (validHeads.contains(preterminals.get(i))) {
        headIdx = i;
      } else if (blockers.contains(preterminals.get(i))) {
        blocked = true;
      }
    }
    if (headIdx == -1) {
      headIdx = (leftToRight ? Math.max(0, i - 1) : Math.min(i + 1, preterminals.size()));
    }
    if (headIdx < 0 || headIdx >= preterminals.size()) {
      headIdx = start;
    }
    return headIdx;
  }
}
