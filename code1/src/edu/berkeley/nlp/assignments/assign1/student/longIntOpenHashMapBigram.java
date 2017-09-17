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
//		System.out.println("inside the bigram hash!");

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

}
