package edu.berkeley.nlp.assignments.assign1.student;

import java.util.Arrays;
import java.util.Iterator;

import edu.berkeley.nlp.util.CollectionUtils;
import edu.berkeley.nlp.util.TIntOpenHashMap.Entry;
//import edu.berkeley.nlp.util.TIntOpenHashMap.EntryIterator;
//import edu.berkeley.nlp.util.TIntOpenHashMap.MapIterator;

public class longIntOpenHashMapBigram extends longIntOpenHashMap {

	private int[] bigramX_fertility;
	private int[] Xbigram_fertility;
	private boolean isPrintFertilityTable = true;

	public longIntOpenHashMapBigram() {
		this(10);
	}

	public longIntOpenHashMapBigram(int initialCapacity_) {
		this(initialCapacity_, 0.8);
	}

	public longIntOpenHashMapBigram(int initialCapacity_, double loadFactor) {
		super(initialCapacity_, loadFactor);
		int cap = getKeys().length;
		bigramX_fertility = new int[cap];
		Xbigram_fertility = new int[cap];
	}

	@Override
	public void rehash() {
		// System.out.println("inside the bigram hash!");

		long[] oldKeys = getKeys();
		int[] oldValues = getValues();

		int newLength = oldKeys.length * 3 / 2;

		long[] newKeys = new long[newLength];
		Arrays.fill(newKeys, EMPTY_KEY_PLACEHOLDER);
		int[] newValues = new int[newLength];
		int[] newbigramX_fertility = new int[newLength];
		int[] newXbigram_fertility = new int[newLength];
		setSize(0);
		for (int i = 0; i < oldKeys.length; ++i) {
			long curr = oldKeys[i];
			if (curr != EMPTY_KEY_PLACEHOLDER) {
				int val = oldValues[i];
				int bigramX_fertility_val = bigramX_fertility[i];
				int Xbigram_fertility_val = Xbigram_fertility[i];
				putHelp(curr, val, bigramX_fertility_val, Xbigram_fertility_val, newKeys, newValues,
						newbigramX_fertility, newXbigram_fertility);
			}
		}
		setKeys(newKeys);
		setValues(newValues);
		bigramX_fertility = newbigramX_fertility;
		Xbigram_fertility = newXbigram_fertility;
	}

	public int getbigramX_Value(int pos) {
		return bigramX_fertility[pos];
	}

	public int getXbigram_Value(int pos) {
		return Xbigram_fertility[pos];
	}

	/**
	 * @param k
	 * @param v
	 */
	public int putHelp(long k, int v, int bigramX_fertility_val, int Xbigram_fertility_val, long[] keyArray,
			int[] valueArray, int[] newbigramX_fertility, int[] Xbigram_fertility) {
		int pos = super.putHelp(k, v, keyArray, valueArray);
		newbigramX_fertility[pos] = bigramX_fertility_val;
		Xbigram_fertility[pos] = Xbigram_fertility_val;
		return pos;
	}

	public void updateBigramFertility(long bigram_w1w2X, int bigram_w2w3_Pos) {
		// TODO Auto-generated method stub
		int bigram_w1w2_Pos = this.fromKeyGetPos(bigram_w1w2X);
		bigramX_fertility[bigram_w1w2_Pos]++;
		Xbigram_fertility[bigram_w2w3_Pos]++;
	}

	@Override
	public void printStatus() {
		if (isPrintFertilityTable) {
			long[] this_key = this.getKeys();
			int[] this_value = this.getValues();
			System.out.println("this is "+this.getClass());
			for (int i = 0; i < this_key.length; i++)
				System.out.println(i + "\t" + this_key[i] + " " + this_value[i] + " " + bigramX_fertility[i] + " "
						+ Xbigram_fertility[i]);
		}
	}

	public int fromPosGetValue(int pos) {
		// TODO Auto-generated method stub
		return this.getValues()[pos];
	}

}
