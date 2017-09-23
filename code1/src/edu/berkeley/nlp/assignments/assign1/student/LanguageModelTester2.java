package edu.berkeley.nlp.assignments.assign1.student;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import edu.berkeley.nlp.assignments.assign1.student.LmFactory;
import edu.berkeley.nlp.io.SentenceCollection;
import edu.berkeley.nlp.langmodel.EmpiricalUnigramLanguageModel.EmpiricalUnigramLanguageModelFactory;
import edu.berkeley.nlp.langmodel.EnglishWordIndexer;
import edu.berkeley.nlp.langmodel.LanguageModelFactory;
import edu.berkeley.nlp.langmodel.NgramLanguageModel;
import edu.berkeley.nlp.langmodel.StubLanguageModel.StubLanguageModelFactory;
import edu.berkeley.nlp.math.SloppyMath;
import edu.berkeley.nlp.mt.BleuScore;
import edu.berkeley.nlp.mt.Weights;
import edu.berkeley.nlp.mt.decoder.Decoder;
import edu.berkeley.nlp.mt.decoder.Logger;
import edu.berkeley.nlp.mt.decoder.internal.BeamDecoder;
import edu.berkeley.nlp.mt.phrasetable.PhraseTable;
import edu.berkeley.nlp.util.CollectionUtils;
import edu.berkeley.nlp.util.CommandLineUtils;
import edu.berkeley.nlp.util.MemoryUsageUtils;
import edu.berkeley.nlp.util.Pair;
import edu.berkeley.nlp.util.StrUtils;
import edu.berkeley.nlp.util.StringIndexer;

/**
 * This is the main harness for assignment 1. To run this harness, use
 * <p/>
 * java edu.berkeley.nlp.assignments.assign1.LanguageModelTester -path
 * ASSIGNMENT_DATA_PATH -lmType LM_TYPE_DESCRIPTOR_STRING
 * <p/>
 * First verify that the data can be read on your system. Second, find the point
 * in the main method (near the bottom) where an EmpiricalUnigramLanguageModel
 * is constructed. You will be writing new implementations of the LanguageModel
 * interface and constructing them there.
 * 
 * @author Adam Pauls
 */
public class LanguageModelTester2 {
	static String basePath = ".";
	static boolean isPrint = false;

	enum LmType {
		STUB {
			@Override
			public LanguageModelFactory getFactory() {
				return new StubLanguageModelFactory();
			}
		},
		UNIGRAM {
			@Override
			public LanguageModelFactory getFactory() {
				return new EmpiricalUnigramLanguageModelFactory();
			}
		},
		TRIGRAM {
			@Override
			public LanguageModelFactory getFactory() {
				return new LmFactory();
			}
		};

		public abstract LanguageModelFactory getFactory();
	}

	public static void main(String[] args) {
		// Parse command line flags and arguments
		Map<String, String> argMap = CommandLineUtils.simpleCommandLineParser(args);

		// Set up default parameters and settings

		LmType lmType = LmType.UNIGRAM;
		// You can use this to make decoding runs run in less time, but remember that we
		// will
		// evaluate you on all test sentences.
		int maxNumTest = Integer.MAX_VALUE;
		boolean sanityCheck = false;
		boolean printTranslations = true;

		// Update defaults using command line specifications

		// The path to the assignment data
		if (argMap.containsKey("-path")) {
			basePath = argMap.get("-path");
		}
		System.out.println("Using base path: " + basePath);

		// A string descriptor of the model to use
		if (argMap.containsKey("-lmType")) {
			lmType = LmType.valueOf(argMap.get("-lmType"));
		}
		System.out.println("Using lmType: " + lmType);

		if (argMap.containsKey("-maxNumTest")) {
			maxNumTest = Integer.parseInt(argMap.get("-maxNumTest"));
		}
		System.out.println("Decoding " + (maxNumTest == Integer.MAX_VALUE ? "all" : ("" + maxNumTest)) + " sentences.");

		if (argMap.containsKey("-noprint")) {
			printTranslations = false;
		}

		if (argMap.containsKey("-sanityCheck")) {
			sanityCheck = true;
		}
		if (sanityCheck)
			System.out.println("Only doing sanity check.");

		String prefix = sanityCheck ? "sanity_" : "";

		// Read in all the assignment data
		File trainingSentencesFile = new File(basePath, prefix + "training.en.gz");
		File phraseTableFile = new File(basePath, prefix + "phrasetable.txt.gz");

		File testFrench = new File(basePath, prefix + "test.fr");
		File testEnglish = new File(basePath, prefix + "test.en");
		File weightsFile = new File(basePath, "weights.txt");
		Iterable<List<String>> trainingSentenceCollection = SentenceCollection.Reader
				.readSentenceCollection(trainingSentencesFile.getPath());
		LanguageModelFactory languageModelFactory = lmType.getFactory();
		NgramLanguageModel languageModel;
		System.out.println(argMap);

		if (argMap.containsKey("-calculatePerplexity")) {
			int sent = Integer.valueOf(argMap.get("-calculatePerplexity"));

			languageModel = ((LmFactory) languageModelFactory).newLanguageModel(trainingSentenceCollection, sent,
					Double.MAX_VALUE, Double.MAX_VALUE, true);
			final String englishData = (testEnglish).getPath();
			Iterable<List<String>> englishSentences = SentenceCollection.Reader.readSentenceCollection(englishData);
			perplexity(languageModel, englishSentences);
			sent = (int) (((KneserNeyLanguageModel) languageModel).getTotalSent() * 0.9);
		} else {
			// Build the language model‘

			long startTime = System.nanoTime();
			double loadFactor = Double.MAX_VALUE;
			double discountFactor = Double.MAX_VALUE;
			int sent = Integer.MAX_VALUE;
			boolean isLinearProbing = true;
			if (argMap.containsKey("-loadFactor")) {
				loadFactor = Double.valueOf(argMap.get("-loadFactor"));
				System.out.println("load factor " + loadFactor);
			}
			if (argMap.containsKey("-discountFactor")) {
				discountFactor = Double.valueOf(argMap.get("-discountFactor"));
				System.out.println("discount factor " + discountFactor);
			}
			if (argMap.containsKey("-quadraticProbing")) {
				isLinearProbing = false;
			}
			languageModel = ((LmFactory) languageModelFactory).newLanguageModel(trainingSentenceCollection, sent,
					loadFactor, discountFactor, isLinearProbing);

			long endTime = System.nanoTime();
			System.out.println(
					"Building language model took " + BleuScore.formatDouble((endTime - startTime) / 1e9) + "s");
		}

		if (lmType == LmType.TRIGRAM) {
			spotCheckLanguageModel(languageModel, sanityCheck);
		}

		MemoryUsageUtils.printMemoryUsage();

		evaluateLanguageModel(phraseTableFile, testFrench, testEnglish, weightsFile, languageModel, maxNumTest,
				printTranslations);
	}

	private static int perplexity(NgramLanguageModel languageModel, Iterable<List<String>> englishSentences) {
		// TODO Auto-generated method stub
		int wordTotal = 0;
		int sent = 0;
		double sumLogP = 0.0d;
		for (List<String> sentence : englishSentences) {
			sent++;
			if (sent % 1000 == 0)
				System.out.println("Calculating perplexity on sentence " + sent);
			List<String> stoppedSentence = new ArrayList<String>(sentence);
			stoppedSentence.add(0, NgramLanguageModel.START);
			stoppedSentence.add(NgramLanguageModel.STOP);

			for (int i = 2; i < stoppedSentence.size(); i++) {

				String[] context = new String[3];
				context[0] = stoppedSentence.get(i - 2);
				context[1] = stoppedSentence.get(i - 1);
				context[2] = stoppedSentence.get(i);
				int[] ngram = index(context);

				double this_prob = languageModel.getNgramLogProbability(ngram, 0, ngram.length);
				if (this_prob > -100) {
					sumLogP += this_prob;
					wordTotal++;
				}
				// else {
				// System.out.println(
				// this_prob + " comes from context " + context[0] + " " + context[1] + " " +
				// context[2]);
				// }
			}
		}
		double perplexity = Math.exp(-1.0 * sumLogP / wordTotal);
		System.out.println("perplexity for training size " + ((KneserNeyLanguageModel) languageModel).getTotalSent()
				+ " " + perplexity);
		return sent;
	}

	private static void spotCheckLanguageModel(NgramLanguageModel languageModel, boolean sanityCheck) {
		System.out.println("Performing spot checks...");
		// if (!basePath.contains("toy")) {
		{
			if (!sanityCheck) {
				spotCheckCount(languageModel, new String[] { "the" }, 19880264L);
				spotCheckCount(languageModel, new String[] { "in", "terms", "of" }, 31257L); // is this a trigram?
				spotCheckCount(languageModel, new String[] { "romanian", "independent", "society" }, 30L);
				spotCheckCount(languageModel, new String[] { "XXXtotally", "XXXunseen", "XXXtrigram" }, 0L);
			}
			// spotCheckContextNormalizes(languageModel, new String[] { "in", "terms" });
			spotCheckContextNormalizes(languageModel, new String[] { "romanian", "independent" });
			// spotCheckContextNormalizes(languageModel, new String[] { "prosecution",
			// "office" });
			// spotCheckContextNormalizes(languageModel, new String[] { "commerce",
			// "control" });
			// spotCheckContextNormalizes(languageModel, new String[] { "authenticated",
			// "by" });
			// spotCheckContextNormalizes(languageModel, new String[] { "final",
			// "destination" });
			// spotCheckContextNormalizes(languageModel, new String[] { "the" });
		}
		// else {
		// if (!sanityCheck) {
		// spotCheckCount(languageModel, new String[] { "the" }, 4L);
		// spotCheckCount(languageModel, new String[] { "or", "shell" }, 2L); // is this
		// a trigram?
		// spotCheckCount(languageModel, new String[] { "shell", "or", "shell" }, 1L);
		// spotCheckCount(languageModel, new String[] { "XXXtotally", "XXXunseen",
		// "XXXtrigram" }, 0L);
		// }
		// spotCheckContextNormalizes(languageModel, new String[] { "the" });
		// spotCheckContextNormalizes(languageModel, new String[] { "or", "shell" });
		// spotCheckContextNormalizes(languageModel, new String[] { "problem", "is" });
		// }

		System.out.println("Spot checks completed");
	}

	private static void spotCheckCount(NgramLanguageModel languageModel, String[] arr, long expectedCount) {
		long count = languageModel.getCount(index(arr));
		if (count != expectedCount) {
			System.out.println("ERROR: Count does not match expected count " + count + " != " + expectedCount + " for "
					+ Arrays.toString(arr));
		} else {
			System.out.println(
					"Count matches expected count " + count + " = " + expectedCount + " for " + Arrays.toString(arr));
		}
	}

	private static void spotCheckContextNormalizes(NgramLanguageModel languageModel, String[] rawContext) {
		StringIndexer indexer = EnglishWordIndexer.getIndexer();
		int[] context = index(rawContext);
		int[] ngram = new int[context.length + 1];
		for (int i = 0; i < context.length; i++) {
			ngram[i] = context[i];
		}
		double totalLogProb = Double.NEGATIVE_INFINITY;
		for (int wordIdx = 0; wordIdx < indexer.size(); wordIdx++) {
			// Don't include the START symbol since it is only observed in contexts
			if (wordIdx != indexer.indexOf(NgramLanguageModel.START)) {
				ngram[ngram.length - 1] = wordIdx;
				double this_prob = languageModel.getNgramLogProbability(ngram, 0, ngram.length);
				if (this_prob > -20 && isPrint) {
					String[] words = Arrays.stream(ngram).mapToObj(i -> indexer.get(i)).toArray(String[]::new);
					System.out.println(String.join(" ", Arrays.toString(ngram)) + " aka. " + String.join(" ", words)
							+ " " + this_prob);

				}
				totalLogProb = SloppyMath.logAdd(totalLogProb, this_prob);

			}
		}
		if (Math.abs(totalLogProb) > 0.001) {
			System.out.println("WARNING: Distribution for context " + Arrays.toString(rawContext)
					+ " does not normalize correctly, sums to " + Math.exp(totalLogProb));
		} else {
			System.out.println("Distribution for context " + Arrays.toString(rawContext)
					+ " normalizes correctly, sums to " + Math.exp(totalLogProb));
		}
	}

	private static int[] index(String[] arr) {
		int[] indexedArr = new int[arr.length];
		for (int i = 0; i < indexedArr.length; i++) {
			indexedArr[i] = EnglishWordIndexer.getIndexer().addAndGetIndex(arr[i]);
		}
		return indexedArr;
	}

	/**
	 * @param useTestSet
	 * @param phraseTableFile
	 * @param devFrench
	 * @param devEnglish
	 * @param testFrench
	 * @param testEnglish
	 * @param weightsFile
	 * @param languageModel
	 */
	private static void evaluateLanguageModel(File phraseTableFile, File testFrench, File testEnglish, File weightsFile,
			NgramLanguageModel languageModel, int maxNumTest, boolean printTranslations) {
		PhraseTable phraseTable = new PhraseTable(5, 30); // int maxPhraseSize, int maxNumTranslations
		phraseTable.readFromFile(phraseTableFile.getPath(), Weights.readWeightsFile(weightsFile));

		Decoder decoder = new BeamDecoder(languageModel, phraseTable, EnglishWordIndexer.getIndexer());
		MemoryUsageUtils.printMemoryUsage();
		final String frenchData = (testFrench).getPath();
		Iterable<List<String>> frenchSentences = SentenceCollection.Reader.readSentenceCollection(frenchData);
		final String englishData = (testEnglish).getPath();
		Iterable<List<String>> englishSentences = SentenceCollection.Reader.readSentenceCollection(englishData);
		List<BleuScore> scores = new ArrayList<BleuScore>();
		doDecoding(decoder, frenchSentences, englishSentences, scores, maxNumTest, printTranslations);
		String bleuString = new BleuScore(scores).toString();
		System.out.println("BLEU score on " + ("test") + " data was " + bleuString);

	}

	/**
	 * @param decoder
	 * @param frenchSentences
	 * @param englishSentences
	 * @param scores
	 */
	private static void doDecoding(Decoder decoder, Iterable<List<String>> frenchSentences,
			Iterable<List<String>> englishSentences, List<BleuScore> scores, int maxNumTest,
			boolean printTranslations) {
		long startTime = System.nanoTime();
		int sent = 0;
		System.out.println(
				"Decoding " + (maxNumTest == Integer.MAX_VALUE ? "all" : ("" + maxNumTest)) + " test sentences");
		for (Pair<List<String>, List<String>> input : CollectionUtils
				.zip(Pair.makePair(frenchSentences, englishSentences))) {
			if (sent >= maxNumTest)
				break;
			sent++;

			if (sent % 100 == 0)
				Logger.logs("On sentence " + sent);
			List<String> hypothesis = Decoder.StaticMethods.extractEnglish(decoder.decode(input.getFirst()));
			List<String> reference = input.getSecond();
			if (printTranslations) {
				System.out.println("Input:\t\t" + StrUtils.join(input.getFirst()));
				System.out.println("Hypothesis\t" + StrUtils.join(hypothesis));
				System.out.println("Reference:\t" + StrUtils.join(reference));
			}
			BleuScore bleuScore = new BleuScore(hypothesis, reference);
			scores.add(bleuScore);
		}
		long endTime = System.nanoTime();

		System.out.println("Decoding took " + BleuScore.formatDouble((endTime - startTime) / 1e9) + "s");
	}

}