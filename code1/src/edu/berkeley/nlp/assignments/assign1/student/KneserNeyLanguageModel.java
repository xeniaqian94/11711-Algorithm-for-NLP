package edu.berkeley.nlp.assignments.assign1.student;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import edu.berkeley.nlp.langmodel.EmpiricalUnigramLanguageModel;
import edu.berkeley.nlp.langmodel.EnglishWordIndexer;
import edu.berkeley.nlp.langmodel.LanguageModelFactory;
import edu.berkeley.nlp.langmodel.NgramLanguageModel;
import edu.berkeley.nlp.util.CollectionUtils;

public class KneserNeyLanguageModel implements NgramLanguageModel {

	static final String STOP = NgramLanguageModel.STOP;
	Scanner scanner = new Scanner(System.in);
	String line;
	boolean isPrint = false;
	double d = 0.7d;

	// intIndexer unigramIndexer=new intIndexer();

	// longIndexer bigramIndexer = new longIndexer();
	// longIndexer trigramIndexer = new longIndexer();
	longIntOpenHashMapBigram bigramIndexer = new longIntOpenHashMapBigram();
	longIntOpenHashMap trigramIndexer = new longIntOpenHashMap();

	long total = 0;

	long[] wordCounter = new long[10];

	int twentyBitMask = 0xFFFFF;

	public KneserNeyLanguageModel(Iterable<List<String>> sentenceCollection) {
		System.out.println("Building KneserNeyLanguageModel . . .");
		int sent = 0; // sentence counter
		for (List<String> sentence : sentenceCollection) {
			sent++;
			if (sent % 1000000 == 0)
				System.out.println("On sentence " + sent);
			List<String> stoppedSentence = new ArrayList<String>(sentence);
			stoppedSentence.add(0, NgramLanguageModel.START);
			stoppedSentence.add(STOP);
			// for (String word : stoppedSentence) {
			String word = null;
			String prev_word = null;
			int prev_index = -1;
			int prev2_index = -1;
			if (isPrint)
				bigramIndexer.printStatus();
			for (int i = 0; i < stoppedSentence.size(); i++) {
				word = stoppedSentence.get(i);
				// store unigram fertility

				int index = EnglishWordIndexer.getIndexer().addAndGetIndex(word);

				if (isPrint)
					System.out.println("unigram " + word + " " + index + " " + wordCounter.length);

				if (index >= wordCounter.length)
					wordCounter = CollectionUtils.copyOf(wordCounter, wordCounter.length * 2);
				wordCounter[index]++;

				if (i > 0) {
					long bigram_key = ((((long) prev_index) & twentyBitMask) << 20) | (((long) index) & twentyBitMask);
					if (isPrint)
						System.out.println("bigram inserted " + bigram_key + " " + index + " " + wordCounter.length);
					bigramIndexer.insertOrAdd(bigram_key);
					// bigramIndexer.printStatus();

					if (i > 1) {
						long trigram_key = ((((long) prev2_index) & twentyBitMask) << 40)
								| ((((long) prev_index) & twentyBitMask) << 20) | (((long) index) & twentyBitMask);
						if (isPrint)
							System.out.println("trigram " + trigram_key + " " + index + " " + wordCounter.length);
						trigramIndexer.insertOrAdd(trigram_key);
					}
				}
				if (i > 0)
					prev2_index = prev_index;
				prev_index = index;

			}
		}
		if (isPrint)
			System.out.println("Done building EmpiricalUnigramLanguageModel.");
		wordCounter = CollectionUtils.copyOf(wordCounter, EnglishWordIndexer.getIndexer().size());
		total = CollectionUtils.sum(wordCounter);

		// wordCounter.toString();
		System.out.println("index of shell " + EnglishWordIndexer.getIndexer().indexOf("shell"));
		System.out.println("unigram table size" + EnglishWordIndexer.getIndexer().size() + " word count length "
				+ wordCounter.length + " however total is " + total);
		bigramIndexer.printStatus();
		trigramIndexer.printStatus();

		// if (isPrint) {
		// System.out.println("print bigramIndexer table");
		// bigramIndexer.printStatus();
		// System.out.println("print trigramIndexer table");
		// trigramIndexer.printStatus();
		// }

		System.gc();

	}

	public int getOrder() {
		return 3;
	}

	public double getNgramLogProbability(int[] ngram, int from, int to) {
		if (to - from != 1) { // higher order ones
			if (to - from == 3) {
				long bigram_key = ((((long) ngram[from + 1]) & twentyBitMask) << 20)
						| (((long) ngram[from]) & twentyBitMask);
				long trigram_key = ((((long) ngram[from + 2]) & twentyBitMask) << 40)
						| ((((long) ngram[from + 1]) & twentyBitMask) << 20) | (((long) ngram[from]) & twentyBitMask);
				double alpha;
				double bigramProbability;
				// return
				// Math.log(Math.max(trigramIndexer.fromKeyGetValue(trigram_key)-d,0)/bigramIndexer.fromKeyGetValue(bigram_key)+alpha*bigramProbability);
			} else if (to - from == 2) {
			}

			System.out.println("WARNING: to - from > 1 for EmpiricalUnigramLanguageModel, but is okay for KN trigram");
			return 0.0d;
		}

		int word = ngram[from];
		return Math.log((word < 0 || word >= wordCounter.length) ? 1.0 : wordCounter[word] / (total + 1.0));
	}

	public long getCount(int[] ngram) {
		System.out.println("in here getting count for ngram " + ngram.toString());
		if (ngram.length > 1) {

			long key = 0;
			for (int ngram_index : ngram)
				key = (key << 20) | (ngram_index & twentyBitMask);
			if (ngram.length == 2)
				return bigramIndexer.fromKeyGetValue(key);
			else if (ngram.length == 3)
				return trigramIndexer.fromKeyGetValue(key);
		} else if (ngram.length == 1) {
			final int word = ngram[0];
			if (word < 0 || word >= wordCounter.length)
				return 0;
			return wordCounter[word];
		}
		return 0;
	}
}
