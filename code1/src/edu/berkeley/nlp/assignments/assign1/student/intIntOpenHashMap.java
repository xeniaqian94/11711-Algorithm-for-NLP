package edu.berkeley.nlp.assignments.assign1.student;

import java.util.Arrays;
import java.util.Iterator;

import edu.berkeley.nlp.util.CollectionUtils;
import edu.berkeley.nlp.util.TIntOpenHashMap.Entry;
//import edu.berkeley.nlp.util.TIntOpenHashMap.EntryIterator;
//import edu.berkeley.nlp.util.TIntOpenHashMap.MapIterator;

public class intIntOpenHashMap {

	private int[] keys;
	private int[] values;

	private int size = 0;  //how many values have been put into the hashtable

//	private final int EMPTY_KEY = null;

	private final double MAX_LOAD_FACTOR;

	public boolean put(int k, int v) {
		if (size / (double) keys.length > MAX_LOAD_FACTOR) {
			rehash();
		}
		return putHelp(k, v, keys, values);

	}

	public intIntOpenHashMap() {
			this(10);
		}

	public intIntOpenHashMap(int initialCapacity_) {
			this(initialCapacity_, 0.8);
		}

	public intIntOpenHashMap(int initialCapacity_, double loadFactor) {
			int cap = Math.max(5, (int) (initialCapacity_ / loadFactor));
			MAX_LOAD_FACTOR = loadFactor;
			values = new int[cap];
//			Arrays.fill(values, 0);
			keys = new int[cap];
		}

	private void rehash() {
		int[] newKeys = new int[keys.length * 2-1];
		int[] newValues = new int[values.length * 2-1];
//		Arrays.fill(newValues, -1);
		size = 0;
		for (int i = 0; i < keys.length; ++i) {
			int curr = keys[i];
			if (curr!=0) {
				int val = values[i];
				putHelp(curr, val, newKeys, newValues);
			}
		}
		keys = newKeys;
		values = newValues;
	}

	/**
	 * @param k
	 * @param v
	 */
	private boolean putHelp(int k, int v, int[] keyArray, int[] valueArray) {
		int pos = getInitialPos(k, keyArray); // given a key get an position?
		int curr = keyArray[pos];
		while (curr != 0 && curr!=k) { 
			pos++;   //linear probing
			if (pos == keyArray.length)
				pos = 0;
			curr = keyArray[pos];
		}

		valueArray[pos] = v;
		if (curr == 0) {
			size++;
			keyArray[pos] = k;
			return true;
		}
		return false;
	}
	
	public boolean insertOrAdd(int k) {
		if (size / (double) keys.length > MAX_LOAD_FACTOR) {
			rehash();
		}
		int pos = getInitialPos(k, keys); // given a key get an position?
		int curr = keys[pos];
		while (curr != 0 && curr!=k) { 
			pos++;   //linear probing
			if (pos == keys.length)
				pos = 0;
			curr = keys[pos];
		}

		values[pos] +=1;
		if (curr == 0) {
			size++;
			keys[pos] = k;
			return true;
		}
		return false;
				
	}
	
	public int hash6432shift(int key) //source: https://gist.github.com/badboy/6267743
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
	private int getInitialPos(int k, int[] keyArray) {
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

	public int get(int k) {  //get value from key
		int pos = find(k);
		return values[pos];
	}
	
	public int get_key_from_index(int pos) {
		return keys[pos];
	}

	/**
	 * @param k
	 * @return
	 */
	private int find(int k) {  // find the pos for key k
		int pos = getInitialPos(k, keys);
		int curr = keys[pos];
		while (curr !=0 && curr!=k) { //collision linear probing  
			pos++;
			if (pos == keys.length)
				pos = 0;
			curr = keys[pos];
		}
		return pos;  //find a position that either holds the key or is empty
	}

	public void increment(int k, int c) {
		int pos = find(k);
		int currKey = keys[pos];
		if (currKey == 0) {    // no key in this slot so far
			put(k, c);
		} else
			values[pos]++;
	}

	public int size() {
		return size;
	}
	
	public void printStatus() {
		for (int i =0;i<keys.length;i++)
			System.out.println(keys[i]+" "+values[i]);
		System.out.println("current load factor "+(size*1.0 / (double) keys.length));
	}

}
