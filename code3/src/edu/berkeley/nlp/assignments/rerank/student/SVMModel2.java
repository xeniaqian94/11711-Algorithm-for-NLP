package edu.berkeley.nlp.assignments.rerank.student;

import java.util.ArrayList;
import java.util.Iterator;

import edu.berkeley.nlp.assignments.rerank.LossAugmentedLinearModel;
import edu.berkeley.nlp.util.IntCounter;

public class SVMModel2 implements LossAugmentedLinearModel {
	class Datum {
		int goldIdx;
		ArrayList<IntCounter> features;

		public Datum() {
			goldIdx = -1;
			this.features = new ArrayList<IntCounter>();
		}

		public Datum(int k) {
			goldIdx = k;
			this.features = new ArrayList<IntCounter>();
		}

		public void setGoldIdx(int k) {
			goldIdx = k;
		}

		public int getGoldIdx() {
			return goldIdx;
		}

		public void insertFeature(IntCounter featrow) {
			this.features.add(featrow);
		}

	}

	public double calcScore(IntCounter weights, IntCounter fv) {
		Iterable<Integer> featureIndices = fv.keySet();
		Iterator fi = featureIndices.iterator();
		double score = 0;
		while (fi.hasNext()) {
			int featureIndex = (int) fi.next();
			score = score + weights.get(featureIndex) * fv.get(featureIndex);
		}
		return score;
	}

	@Override
	public UpdateBundle getLossAugmentedUpdateBundle(Object datum, IntCounter weights) {
		// TODO Auto-generated method stub
		Datum data = (Datum) datum;
		int l = data.features.size();
		double goldscore = calcScore(weights, data.features.get(0));
		double maxscore = 0;
		int maxi = 0;
		for (int i = 1; i < l; i++) {
			double score = calcScore(weights, data.features.get(i));
			if (score > maxscore || maxscore == 0) {
				maxscore = score;
				maxi = i;
			}
		}
		UpdateBundle ub = new UpdateBundle(data.features.get(0), data.features.get(maxi),
				-1.0 * (goldscore - maxscore));
		return ub;
	}

}