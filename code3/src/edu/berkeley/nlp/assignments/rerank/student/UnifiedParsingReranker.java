package edu.berkeley.nlp.assignments.rerank.student;

import java.util.List;

import edu.berkeley.nlp.assignments.rerank.KbestList;
import edu.berkeley.nlp.assignments.rerank.ParsingReranker;
import edu.berkeley.nlp.ling.Tree;
import edu.berkeley.nlp.util.Indexer;


public class UnifiedParsingReranker implements ParsingReranker{
	
	static double TOLERENCE = 1e-4;
	static double LAMBDA = 0.01d;
	double[] weights = null;
	static Indexer<String> featureIndexer = new Indexer<String>();
	

	@Override
	public Tree<String> getBestParse(List<String> sentence, KbestList kbestList) {
		// TODO Auto-generated method stub
		return null;
	}
	

}
