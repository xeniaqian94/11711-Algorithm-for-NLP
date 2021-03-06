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

	int num_access = 0;
	int num_collision = 0;

	private boolean isLinearProbing;

	public void setLinearProbing(boolean value) {
		isLinearProbing = value;
	}

	public boolean getLinearProbing() {
		return isLinearProbing;
	}

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

	public longIntOpenHashMap() {
		this(10);
	}

	public longIntOpenHashMap(double loadFactor) {
		this(10, loadFactor);
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
		setLinearProbing(true);
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

	public int putHelp(long k, int v, long[] keyArray, int[] valueArray) { // internally modify the hastable during //
																			// rehash()
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

	public int insertOrAdd(long k) { // externally modify the hashtable, if k is not in the hashmap, insert it,
										// otherwise add it. Return value is whether the key exists or not
		if (size / (double) keys.length >= MAX_LOAD_FACTOR) {
			rehash();
			// System.out.println("rehashed after this:");
			// this.printStatus();
		}
//		System.out.println(size / (double) keys.length+" "+(double) keys.length+" "+MAX_LOAD_FACTOR);
		int initialPos = getInitialPos(k, keys); // given a key get an position?
		int pos = getFinalPos(k, keys, initialPos);
		long curr = keys[pos];

		values[pos] += 1;
		if (curr == EMPTY_KEY_PLACEHOLDER) {
			size++;
			keys[pos] = k;
		}
		return pos;

	}

	private int getFinalPos(long k, long[] thisKeyArray, int initialPos) {
		// TODO Auto-generated method stub
		long curr = thisKeyArray[initialPos];
		int finalPos = initialPos;
		int i = 1;
		// if (curr == EMPTY_KEY_PLACEHOLDER | curr == k) {
		// System.out.println("no collision here");
		// }
		while (curr != EMPTY_KEY_PLACEHOLDER && curr != k) {
			if (isLinearProbing) {
				finalPos++; // linear probing
			} else {
				// System.out.println("within quadratic probing");
				int shift = i * i;
//				System.out.println(finalPos+" "+i+" "+shift);
				finalPos += shift % thisKeyArray.length;
//				System.out.println(finalPos+" "+size+" "+thisKeyArray.length);
				i++;
			}
			num_collision++;
			if (finalPos >= thisKeyArray.length)
				finalPos = finalPos % thisKeyArray.length;
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
		num_access++;
		return pos;
	}

	public int fromKeyGetValue(long k) { // get value from key
		int pos = fromKeyGetPos(k);
		return values[pos];
	}

	public int fromKeyGetPos(long k) {
		return find(k);
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

	public int size() {
		return size;
	}

	public void printStatus() {
		System.out.println(this.getClass() + " current hashmap size " + keys.length + " current ocupied size " + size
				+ " current actual load factor " + (size * 1.0 / (double) keys.length));
		System.out.println(this.getClass() + " num_collision " + num_collision + " num_access " + num_access + " ratio "
				+ 1.0 * num_collision / num_access);

		// System.out.println("this is " + this.getClass());
		// for (int i = 0; i < keys.length; i++)
		// System.out.println(i + "\t" + keys[i] + " " + values[i]);

	}

}
