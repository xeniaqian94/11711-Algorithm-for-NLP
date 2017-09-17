package edu.berkeley.nlp.assignments.assign1.student;

import java.io.Serializable;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

import edu.berkeley.nlp.util.TIntOpenHashMap;

public class longIndexer implements Serializable
{
	private static final long serialVersionUID = -8769544079136550516L;

//	List<String> objects;

	longIntOpenHashMap indexes;

	/**
	 * Return the object with the given index
	 * 
	 * @param index
	 */
//	@Override
//	public String get(int index) {
////		return objects.get(index);
////		return get_value_from_index
//		return "";
//	}
//
//	/**
//	 * Returns the number of objects indexed.
//	 */
//	@Override
	public int size() {
		return indexes.size();
//		return objects.size();
	}

	/**
	 * Returns the index of the given object, or -1 if the object is not present
	 * in the indexer.
	 * 
	 * @param o
	 * @return
	 */
//	@Override
//	public int indexOf(Object o) {
//		if (!(o instanceof String)) return -1;
//		int index = indexes.get((long) o);
//
//		return index;
//	}

	/**
	 * Add an element to the indexer if not already present. In either case,
	 * returns the index of the given object.
	 * 
	 * @param e
	 * @return
	 */
//	public int addAndGetIndex(long e) {
//		int index = indexes.get(e);
//		
//		if (index >= 0) { return index; }
//		//  Else, add
//		int newIndex = size();
////		objects.add(e);
//		indexes.put(e, newIndex);
//		return newIndex;
//	}
//	
//	
	
	public int fromKeyGetValue(long k) {
		int value = indexes.fromKeyGetValue(k);
		return value; // if value==0 then this means that the key not exist

	}
	
	public boolean insertOrAdd(long e) {
		boolean inserted=indexes.insertOrAdd(e);
		return inserted;
	}
	
	

	/**
	 * Add an element to the indexer. If the element is already in the indexer,
	 * the indexer is unchanged (and false is returned).
	 * 
	 * @param e
	 * @return
	 */
//	@Override
	
//	public boolean add(long e) {
//		return addAndGetIndex(e) == size() - 1;
//	}

	public longIndexer() {
//		objects = new ArrayList<String>();
		indexes = new longIntOpenHashMap();
	}

	public void printStatus() {
		// TODO Auto-generated method stub
		indexes.printStatus();
		
		
		
	}

}
