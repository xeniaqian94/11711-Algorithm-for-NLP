package edu.berkeley.nlp.assignments.assign1.student;

import java.util.List;

import edu.berkeley.nlp.langmodel.LanguageModelFactory;
import edu.berkeley.nlp.langmodel.NgramLanguageModel;

public class LmFactory implements LanguageModelFactory {

	/**
	 * Returns a new NgramLanguageModel; this should be an instance of a class that
	 * you implement. Please see edu.berkeley.nlp.langmodel.NgramLanguageModel for
	 * the interface specification.
	 * 
	 * @param trainingData
	 */
	public NgramLanguageModel newLanguageModel(Iterable<List<String>> trainingData) {
		return new KneserNeyLanguageModel(trainingData);

	}

	public NgramLanguageModel newLanguageModel(Iterable<List<String>> trainingData, int maxSent, double loadFactor,
			double discountFactor, boolean isLinearProbing) {
		System.out.println("reading limited sent");
		return new KneserNeyLanguageModel(trainingData, maxSent, loadFactor, discountFactor, isLinearProbing);
	}
}
