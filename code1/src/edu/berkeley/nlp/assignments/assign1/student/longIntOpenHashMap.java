package edu.berkeley.nlp.assignments.assign1.student;

import java.util.Arrays;
import java.util.Iterator;

import edu.berkeley.nlp.util.CollectionUtils;
import edu.berkeley.nlp.util.TIntOpenHashMap.Entry;
//import edu.berkeley.nlp.util.TIntOpenHashMap.EntryIterator;
//import edu.berkeley.nlp.util.TIntOpenHashMap.MapIterator;

public class longIntOpenHashMap {

	private long[] keys;
	private int[] values;
	private int size = 0; // how many values have been put into the hashtable
	private final double MAX_LOAD_FACTOR;
	int EMPTY_KEY_PLACEHOLDER = -1;

	public long[] getKeys() {
		return keys;

	}

	public int[] getValues() {
		return values;
	}

	public void setValues(int[] newValues) {
		// TODO Auto-generated method stub

		values = newValues;

	}

	public void setKeys(long[] newKeys) {
		// TODO Auto-generated method stub
		keys = newKeys;
	}

	public void setSize(int size) {
		this.size = size;
	}

	// public boolean put(long k, int v) {
	// if (size / (double) keys.length > MAX_LOAD_FACTOR) {
	// rehash();
	// }
	// return putHelp(k, v, keys, values);
	//
	// }

	public longIntOpenHashMap() {
		this(10);
	}

	public longIntOpenHashMap(int initialCapacity_) {
		this(initialCapacity_, 0.8);
	}

	public longIntOpenHashMap(int initialCapacity_, double loadFactor) {
		int cap = Math.max(5, (int) (initialCapacity_ / loadFactor));
		MAX_LOAD_FACTOR = loadFactor;
		values = new int[cap];
		keys = new long[cap];
		Arrays.fill(keys, EMPTY_KEY_PLACEHOLDER);
	}

	public void rehash() {
		long[] newKeys = new long[keys.length * 3 / 2];
		int[] newValues = new int[values.length * 3 / 2];
		Arrays.fill(newKeys, EMPTY_KEY_PLACEHOLDER);
		size = 0;
		for (int i = 0; i < keys.length; ++i) {
			long curr = keys[i];
			if (curr != EMPTY_KEY_PLACEHOLDER) {
				int val = values[i];
				putHelp(curr, val, newKeys, newValues);
			}
		}
		setKeys(newKeys);
		setValues(newValues);
	}

	/**
	 * @param k
	 * @param v
	 */
	// public boolean putHelp(long k, int v, long[] keyArray, int[] valueArray) {
	// int pos = getInitialPos(k, keyArray); // given a key get an position?
	// long curr = keyArray[pos];
	// while (curr != 0 && curr!=k) {
	// pos++; //linear probing
	// if (pos == keyArray.length)
	// pos = 0;
	// curr = keyArray[pos];
	// }
	//
	// valueArray[pos] = v;
	// if (curr == 0) {
	// size++;
	// keyArray[pos] = k;
	// return true;
	// }
	// return false;
	// }

	public int putHelp(long k, int v, long[] keyArray, int[] valueArray) { // internally modify the hastable during												// rehash()
		int initialPos = getInitialPos(k, keyArray); // given a key get an position?
		int pos = getFinalPos(k, keyArray, initialPos);
		long curr = keyArray[pos];

		valueArray[pos] = v;
		if (curr == EMPTY_KEY_PLACEHOLDER) {
			size++;
			keyArray[pos] = k;

		}
		return pos;
	}

	public boolean insertOrAdd(long k) { // externally modify the hashtable, if k is not in the hashmap, insert it,
											// otherwise add it. Return value is whether the key exists or not
		if (size / (double) keys.length > MAX_LOAD_FACTOR) {
			rehash();
		}
		int initialPos = getInitialPos(k, keys); // given a key get an position?
		int pos = getFinalPos(k, keys, initialPos);
		long curr = keys[pos];

		values[pos] += 1;
		if (curr == EMPTY_KEY_PLACEHOLDER) {
			size++;
			keys[pos] = k;
			return true;
		}
		return false;

	}

	private int getFinalPos(long k, long[] thisKeyArray, int initialPos) {
		// TODO Auto-generated method stub
		long curr = thisKeyArray[initialPos];
		int finalPos = initialPos;
		while (curr != EMPTY_KEY_PLACEHOLDER && curr != k) {
			finalPos++; // linear probing
			if (finalPos == thisKeyArray.length)
				finalPos = 0;
			curr = thisKeyArray[finalPos];
		}
		return finalPos;
	}

	public int hash6432shift(long key) // source: https://gist.github.com/badboy/6267743
	{
		key = (~key) + (key << 18); // key = (key << 18) - key - 1;
		key = key ^ (key >>> 31);
		key = key * 21; // key = (key + (key << 2)) + (key << 4);
		key = key ^ (key >>> 11);
		key = key + (key << 6);
		key = key ^ (key >>> 22);
		return (int) key;
	}

	/**
	 * @param k
	 * @param keyArray
	 * @return
	 */
	public int getInitialPos(long k, long[] keyArray) {
		
		int hash = hash6432shift(k);
		int pos = hash % keyArray.length;
		if (pos < 0)
			pos += keyArray.length;
		// N.B. Doing it this old way causes Integer.MIN_VALUE to be
		// handled incorrect since -Integer.MIN_VALUE is still
		// Integer.MIN_VALUE
		// if (hash < 0) hash = -hash;
		// int pos = hash % keyArray.length;
		return pos;
	}

	public int fromKeyGetValue(long k) { // get value from key
		int pos = find(k);
		return values[pos];
	}

	public long getKeyFromIndex(int pos) {
		return keys[pos];
	}

	/**
	 * @param k
	 * @return
	 */
	public int find(long k) { // find the pos for key k
		int initialPos = getInitialPos(k, keys);
		return getFinalPos(k, keys, initialPos);
	}

	// public void increment(long k, int c) {
	// int pos = find(k);
	// long currKey = keys[pos];
	// if (currKey == 0) { // no key in this slot so far
	// put(k, c);
	// } else
	// values[pos]++;
	// }

	public int size() {
		return size;
	}

	public void printStatus() {
		// for (int i =0;i<keys.length;i++)
		// System.out.println(keys[i]+" "+values[i]);
		System.out.println(this.getClass() + " current hashmap size " + keys.length + " current ocuupied size " + size);
		System.out.println("current load factor " + (size * 1.0 / (double) keys.length));

	}

}
