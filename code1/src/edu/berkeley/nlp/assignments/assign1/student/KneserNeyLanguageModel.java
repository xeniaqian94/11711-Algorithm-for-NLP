package edu.berkeley.nlp.assignments.assign1.student;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import edu.berkeley.nlp.langmodel.EmpiricalUnigramLanguageModel;
import edu.berkeley.nlp.langmodel.EnglishWordIndexer;
import edu.berkeley.nlp.langmodel.LanguageModelFactory;
import edu.berkeley.nlp.langmodel.NgramLanguageModel;
import edu.berkeley.nlp.mt.BleuScore;
import edu.berkeley.nlp.util.CollectionUtils;
import edu.berkeley.nlp.util.StringIndexer;

public class KneserNeyLanguageModel implements NgramLanguageModel {

	static final String STOP = NgramLanguageModel.STOP;
	Scanner scanner = new Scanner(System.in);
	String line;
	boolean isPrint = false;
	double d = 0.8d;
	int totalSent = 0;
	int maxSent = Integer.MAX_VALUE;
	// intIndexer unigramIndexer=new intIndexer();

	// longIndexer bigramIndexer = new longIndexer();
	// longIndexer trigramIndexer = new longIndexer();
	longIntOpenHashMapBigram bigramIndexer;
	longIntOpenHashMap trigramIndexer;
	LRUCache lruCache = null;
	int capacity = 100;

	long total = 0;

	long[] wordCounter = new long[10];
	int[] Xunigram = new int[10];
	int[] unigramX = new int[10];
	int[] XunigramX = new int[10];

	int twentyBitMask = 0xFFFFF;

	public int getTotalSent() {
		return totalSent;
	}

	public KneserNeyLanguageModel(Iterable<List<String>> sentenceCollection, int maxSent, double loadFactor,
			double discountFactor) {
		this(sentenceCollection, maxSent, loadFactor, discountFactor, true);

	}

	public KneserNeyLanguageModel(Iterable<List<String>> sentenceCollection) {
		this(sentenceCollection, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, true);

	}

	public KneserNeyLanguageModel(Iterable<List<String>> sentenceCollection, int maxSent, double loadFactor,
			double discountFactor, boolean isLinearProbing) {
		if (discountFactor <= 1) {
			d = discountFactor;
		}
		if (loadFactor <= 1) {
			bigramIndexer = new longIntOpenHashMapBigram(loadFactor);
			trigramIndexer = new longIntOpenHashMap(loadFactor);
		} else {
			bigramIndexer = new longIntOpenHashMapBigram();
			trigramIndexer = new longIntOpenHashMap();
		}

		if (isLinearProbing) {
			bigramIndexer.setLinearProbing(false);
			trigramIndexer.setLinearProbing(false);
		}

		this.maxSent = maxSent;
		System.out.println("Building KneserNeyLanguageModel . . . isPrint " + isPrint + " isLinearProbing "
				+ bigramIndexer.getLinearProbing() + " " + trigramIndexer.getLinearProbing());
		long startTime = System.nanoTime();

		int sent = 0; // sentence counter
		for (List<String> sentence : sentenceCollection) {
			sent++;
			if (sent % 1000000 == 0)
				System.out.println("On sentence " + sent);
			if (sent > maxSent) // if we are reading the complete sentenceCollection, then maxSent can be set as
								// Integer.MAX_INT
				break;
			List<String> stoppedSentence = new ArrayList<String>(sentence);
			stoppedSentence.add(0, NgramLanguageModel.START);
			stoppedSentence.add(STOP);
			// for (String word : stoppedSentence) {
			String w3_word = null;
			String w2_word = null;
			int w2_index = -1;
			int w1_index = -1;
			if (isPrint)
				bigramIndexer.printStatus();
			for (int i = 0; i < stoppedSentence.size(); i++) {
				w3_word = stoppedSentence.get(i);
				// store unigram fertility

				// this get the index of this word, to only query an index please use indexOf
				int w3_index = EnglishWordIndexer.getIndexer().addAndGetIndex(w3_word);

				if (isPrint)
					System.out.println("unigram " + w3_word + " " + w3_index + " " + wordCounter.length);

				if (w3_index >= wordCounter.length) {
					wordCounter = CollectionUtils.copyOf(wordCounter, wordCounter.length * 2);
					Xunigram = CollectionUtils.copyOf(Xunigram, Xunigram.length * 2);
					unigramX = CollectionUtils.copyOf(unigramX, unigramX.length * 2);
					XunigramX = CollectionUtils.copyOf(XunigramX, XunigramX.length * 2);
				}
				wordCounter[w3_index]++;

				if (i > 0) {
					// we have saved w2_index
					long bigram_key = ((((long) w2_index) & twentyBitMask) << 20) | (((long) w3_index) & twentyBitMask);
					int thisBigramKeyPos = bigramIndexer.insertOrAdd(bigram_key);
					if (isPrint) {
						System.out.println("current trigram index: w1_index " + w1_index + " w2_index " + w2_index
								+ " w3_index " + w3_index);
						System.out
								.println("bigram inserted key: " + bigram_key + " bigram_position " + thisBigramKeyPos);
					}
					if (bigramIndexer.getValues()[thisBigramKeyPos] == 1) { // it's a new bigram
						Xunigram[w3_index]++;
						unigramX[w2_index]++;
						if (isPrint) {
							printNonZeroUnigramFertility(Xunigram, "Xunigram");
							printNonZeroUnigramFertility(unigramX, "unigramX");
							scanner.nextLine();
						}
					}

					if (i > 1) {
						// we have saved w1_index
						long trigram_key = ((((long) w1_index) & twentyBitMask) << 40)
								| ((((long) w2_index) & twentyBitMask) << 20) | (((long) w3_index) & twentyBitMask);
						if (isPrint)
							System.out.println("trigram " + trigram_key + " " + w3_index);
						int thisTrigramKeyPos = trigramIndexer.insertOrAdd(trigram_key);
						if (trigramIndexer.getValues()[thisTrigramKeyPos] == 1) {
							XunigramX[w2_index]++;
							long bigram_w1w2X = ((((long) w1_index) & twentyBitMask) << 20)
									| (((long) w2_index) & twentyBitMask);
							long bigram_Xw2w3 = ((((long) w2_index) & twentyBitMask) << 20)
									| (((long) w3_index) & twentyBitMask);
							bigramIndexer.updateBigramFertility(bigram_w1w2X, thisBigramKeyPos);
							if (isPrint) {
								System.out.println("printing bigram table status");
								bigramIndexer.printStatus();
								scanner.nextLine();
							}
						}
					}
				}
				if (i > 0)
					w1_index = w2_index;
				w2_index = w3_index;

			}
			totalSent = sent;
		}
		wordCounter = CollectionUtils.copyOf(wordCounter, EnglishWordIndexer.getIndexer().size()); // shrink size to
																									// what's needed

		Xunigram = CollectionUtils.copyOf(Xunigram, EnglishWordIndexer.getIndexer().size());
		unigramX = CollectionUtils.copyOf(unigramX, EnglishWordIndexer.getIndexer().size());
		XunigramX = CollectionUtils.copyOf(XunigramX, EnglishWordIndexer.getIndexer().size());

		total = CollectionUtils.sum(wordCounter);

		System.out.println("Building took " + BleuScore.formatDouble((System.nanoTime() - startTime) / 1e9) + "s");

		// wordCounter.toString();
		System.out.println("index of shell " + EnglishWordIndexer.getIndexer().indexOf("shell"));
		System.out.println("unigram table size" + EnglishWordIndexer.getIndexer().size() + " word count length "
				+ wordCounter.length + " however total is " + total);
		bigramIndexer.printStatus();
		trigramIndexer.printStatus();
		if (isPrint) {
			System.out.println("bookkeeping after training finished");
			System.out.println(EnglishWordIndexer.getIndexer().size());
			System.out.println("unigram wordCounter table " + String.join(" ", Arrays.toString(wordCounter)));
			System.out.println("unigram Xunigram table " + String.join(" ", Arrays.toString(Xunigram)));
			System.out.println("unigram unigramX table " + String.join(" ", Arrays.toString(unigramX)));
			System.out.println("unigram XunigramX table " + String.join(" ", Arrays.toString(XunigramX)));
		}

		System.gc();

	}

	private void printNonZeroUnigramFertility(int[] array, String title) {
		// TODO Auto-generated method stub
		String s = title + " ";
		for (int i = 0; i < array.length; i++)
			if (array[i] > 0)
				s += String.valueOf(i) + ":" + String.valueOf(array[i]) + " ";
		System.out.println(s);
	}

	public int getOrder() {
		return 3;
	}

	public double getNgramLogProbability(int[] ngram, int from, int to) {

		if (lruCache == null)
			lruCache = new LRUCache(capacity);
		else {
			if ((to - from) == 3) {
				int w3_index = ngram[to - 1];
				int w2_index = ngram[to - 2];
				int w1_index = ngram[from];

				long trigram_key = (((long) (w1_index) & twentyBitMask) << 40)
						| (((long) (w2_index)) & twentyBitMask) << 20 | (w3_index) & twentyBitMask;
				double value = lruCache.get(trigram_key);
				if (value > 0)
					return Math.log(value);
			}

		}

		int w3_index = ngram[to - 1];
		// System.out.println("sanity check if w3 is unseen " + wordCounter.length + " "
		// + w3_index + " "
		// + (w3_index >= wordCounter.length));
		if (w3_index < 0 | (w3_index >= wordCounter.length)) { // if w3 is unseen in the context{
			return Math.log(1e-100);
		}
		double prob_w3 = Xunigram[w3_index] * 1.0 / bigramIndexer.size();
		// System.out.println("prob_w3 " + prob_w3);
		if (to - from != 1) { // higher order ones
			int w2_index = ngram[to - 2];
			// System.out.println("w2_index is " + w2_index);
			if (w2_index >= wordCounter.length | w2_index >= XunigramX.length) {
				// System.out.println("w2 does not exist/invalid, backoff to P(w3) " +
				// Math.log(prob_w3));
				return Math.log(prob_w3); // backoff to unigram prob
			}
			double denominator = XunigramX[w2_index];
			if (denominator <= 0)
				return Math.log(prob_w3);
			long bigram_key = (((long) ((w2_index) & twentyBitMask)) << 20) | (w3_index & twentyBitMask);
			double alpha_w2 = d * unigramX[w2_index] / denominator;

			// System.out.println("alpha_w2 " + alpha_w2);

			double prob_w3_given_w2 = Math
					.max(bigramIndexer.getXbigram_Value(bigramIndexer.fromKeyGetPos(bigram_key)) - d, 0) * 1.0
					/ XunigramX[w2_index] + alpha_w2 * prob_w3;
			// System.out.println("prob_w3_given_w2 " + prob_w3_given_w2);
			if (to - from == 3) {
				int w1_index = ngram[from];

				if (w1_index >= wordCounter.length) {
					// System.out.println("w1 not exist backoff to bigram ");
					return Math.log(prob_w3_given_w2);
				}
				long trigram_key = (((long) (w1_index) & twentyBitMask) << 40)
						| (((long) (w2_index)) & twentyBitMask) << 20 | (w3_index) & twentyBitMask;
				long bigram_key_w1w2 = (((long) w1_index) & twentyBitMask) << 20 | (w2_index & twentyBitMask);
				int bigram_key_w1w2_Pos = bigramIndexer.fromKeyGetPos(bigram_key_w1w2);
				int count_w1w2 = bigramIndexer.fromPosGetValue(bigram_key_w1w2_Pos);
				if (count_w1w2 <= 0)
					return Math.log(prob_w3_given_w2); // backoff to bigram prob
				double alpha_w1w2 = d * bigramIndexer.getbigramX_Value(bigram_key_w1w2_Pos) / count_w1w2;
				// System.out.println("alpha_w1w2 " + alpha_w1w2);
				// System.out.println(w1_index + " " + w2_index + " " + w3_index + " " +
				// trigram_key + " "
				// + trigramIndexer.fromKeyGetValue(trigram_key) + " "
				// + Math.max(trigramIndexer.fromKeyGetValue(trigram_key) - d, 0));
				double prob_w3_given_w1w2 = Math.max(trigramIndexer.fromKeyGetValue(trigram_key) - d, 0) * 1.0
						/ count_w1w2 + alpha_w1w2 * prob_w3_given_w2;
				lruCache.set(trigram_key, prob_w3_given_w2);
				return Math.log(prob_w3_given_w1w2);
			} else if (to - from == 2) {
				return Math.log(prob_w3_given_w2);
			}

		}
		return Math.log(wordCounter[w3_index] / total);
	}

	public long getCount(int[] ngram) {
		// System.out.println("in here getting count for ngram " + ngram.toString());
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
