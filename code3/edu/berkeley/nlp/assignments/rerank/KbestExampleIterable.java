package edu.berkeley.nlp.assignments.rerank;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import edu.berkeley.nlp.io.IOUtils;
import edu.berkeley.nlp.ling.Tree;
import edu.berkeley.nlp.ling.Trees;
import edu.berkeley.nlp.ling.Trees.PennTreeReader;
import edu.berkeley.nlp.ling.Trees.PennTreeRenderer;
import edu.berkeley.nlp.util.Pair;

public class KbestExampleIterable implements Iterable<Pair<KbestList,Tree<String>>> {

  private String file;
  private List<Tree<String>> goldTrees;
  private int maxLength;
  private int maxListsToRead;
  private int kbestLen;

  public KbestExampleIterable(String file, List<Tree<String>> goldTrees, int maxLength, int maxListsToRead, int kbestLen) {
    this.file = file;
    this.goldTrees = goldTrees;
    this.maxLength = maxLength;
    this.maxListsToRead = maxListsToRead;
    this.kbestLen = kbestLen;
  }

  @Override
  public Iterator<Pair<KbestList,Tree<String>>> iterator() {
    return new KbestExampleIterator(file, goldTrees, maxLength, maxListsToRead, kbestLen);
  }

  /**
   * Produces k-best lists associated with gold parses, i.e. training examples
   * for your reranker.
   * 
   * @author gdurrett
   *
   */
  public static class KbestExampleIterator implements Iterator<Pair<KbestList,Tree<String>>> {

    private BufferedReader reader;
    private List<Tree<String>> goldTrees;
    private int maxLength;
    private int maxListsToRead;
    private int kbestLen;
    private int currTreeIdx;
    private KbestList nextList;

    public KbestExampleIterator(String file, List<Tree<String>> goldTrees, int maxLength, int maxListsToRead, int kbestLen) {
      this.reader = IOUtils.openInHard(file);
      this.goldTrees = goldTrees;
      this.maxLength = maxLength;
      this.maxListsToRead = maxListsToRead;
      this.kbestLen = kbestLen;
      this.currTreeIdx = 0;
      this.nextList = null;
      queueUpNextList();
    }

    private void queueUpNextList() {
      List<Tree<String>> treesThisKbestList = new ArrayList<Tree<String>>();
      List<Double> scoresThisKbestList = new ArrayList<Double>();
      Set<Integer> currTreeHashes = new HashSet<Integer>();
      try {
        while (reader.ready()) {
          String line = reader.readLine();
          if (line.isEmpty()) {
            // If we've read a nontrivial thing and it's within the length
            // constraints
            if (treesThisKbestList.size() > 0 && treesThisKbestList.get(0).getYield().size() <= maxLength) {
              nextList = new KbestList(treesThisKbestList, convert(scoresThisKbestList));
              break;
            }
            treesThisKbestList = new ArrayList<Tree<String>>();
            scoresThisKbestList = new ArrayList<Double>();
            currTreeHashes = new HashSet<Integer>();
          } else {
            String[] lineSplit = line.split("\\t");
            double score = Double.parseDouble(lineSplit[0]);
            PennTreeReader treeReader = new Trees.PennTreeReader(new StringReader(lineSplit[1]));
            Tree<String> currTree = treeReader.next();
            int treeHc = currTree.hashCode();
            if (treesThisKbestList.size() < kbestLen && !currTreeHashes.contains(treeHc)) {
              treesThisKbestList.add(currTree);
              scoresThisKbestList.add(score);
              currTreeHashes.add(treeHc);
            }
          }
        }
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public boolean hasNext() {
      // if ((nextList == null && currTreeIdx != goldTrees.size()) || (nextList
      // != null && currTreeIdx == goldTrees.size())) {
      // System.out.println("ERROR reading in; ran out of k-best lists = " +
      // (nextList == null) + " when on gold tree " + currTreeIdx + "/" +
      // goldTrees.size());
      // }
      return nextList != null && (maxListsToRead < 0 || currTreeIdx < maxListsToRead) && currTreeIdx < goldTrees.size();
    }

    @Override
    public Pair<KbestList,Tree<String>> next() {
      KbestList currNextList = nextList;
      Tree<String> tree = goldTrees.get(currTreeIdx);
      if (!currNextList.getKbestTrees().get(0).getYield().equals(tree.getYield())) {
        throw new RuntimeException("K-best file and gold trees file don't match! " + PennTreeRenderer.render(currNextList.getKbestTrees().get(0)) + "\n" + PennTreeRenderer.render(tree));
      }
      queueUpNextList();
      currTreeIdx++;
      return new Pair<KbestList,Tree<String>>(currNextList, tree);
    }
  }

  public static List<KbestList> readKbestLists(String file, int maxLength, int maxListsToRead, int kbestLen) {
    List<KbestList> kbestLists = new ArrayList<KbestList>();
    BufferedReader reader = IOUtils.openInHard(file);
    List<Tree<String>> treesThisKbestList = new ArrayList<Tree<String>>();
    List<Double> scoresThisKbestList = new ArrayList<Double>();
    Set<Integer> currTreeHashes = new HashSet<Integer>();
    try {
      while (reader.ready() && (maxListsToRead == -1 || kbestLists.size() < maxListsToRead)) {
        String line = reader.readLine();
        if (line.isEmpty()) {
          if (treesThisKbestList.get(0).getYield().size() <= maxLength) {
            kbestLists.add(new KbestList(treesThisKbestList, convert(scoresThisKbestList)));
          }
          treesThisKbestList = new ArrayList<Tree<String>>();
          scoresThisKbestList = new ArrayList<Double>();
          currTreeHashes = new HashSet<Integer>();
          if (kbestLists.size() % 100 == 0) {
            System.out.println(kbestLists.size() + " sentences loaded");
          }
        } else {
          String[] lineSplit = line.split("\\t");
          double score = Double.parseDouble(lineSplit[0]);
          PennTreeReader treeReader = new Trees.PennTreeReader(new StringReader(lineSplit[1]));
          Tree<String> currTree = treeReader.next();
          int treeHc = currTree.hashCode();
          if (treesThisKbestList.size() < kbestLen && !currTreeHashes.contains(treeHc)) {
            treesThisKbestList.add(currTree);
            scoresThisKbestList.add(score);
            currTreeHashes.add(treeHc);
          }
        }
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    if (!treesThisKbestList.isEmpty() && treesThisKbestList.get(0).getYield().size() <= maxLength) {
      kbestLists.add(new KbestList(treesThisKbestList, convert(scoresThisKbestList)));
    }
    return kbestLists;
  }

  private static double[] convert(List<Double> lst) {
    double[] arr = new double[lst.size()];
    for (int i = 0; i < arr.length; i++) {
      arr[i] = lst.get(i).doubleValue();
    }
    return arr;
  }

}
