package util;

import java.util.*;

public class Factory {

	////////////////////////////////////////////////////////////////////////
	//                       Factory for Efficient Reuse
	//
	// a has entry for every FeatureArray created (in case all are returned)
	//
	// a [ 0 1 2 ... length-2 length-1 ]
	//                  ^
	//                index = first unused FeatureArray
	//
	// When FeatureArray requested:  if index >= length, double array, create new
	//                              FeatureArrays starting at index (< are null)
	//                          return a[index], null out a[index], increment index
	//                         
	// When FeatureArray f returned: a[--index] = f
	//                          so long as only requested objects are returned,
	//                          space is guaranteed
	//                         
	////////////////////////////////////////////////////////////////////////
		
	public static TreeMapFactory _tfactory = new TreeMapFactory();

	// TODO: Only make array larger to store overflow of returned items!
	public static class TreeMapFactory {
		
		public static final int INIT_SZ = 100; 
		public TreeMap[] _a;
		public int       _index;

		public TreeMapFactory() {
			_a = new TreeMap[INIT_SZ];
			for (int i = 0; i < INIT_SZ; i++) {
				_a[i] = new TreeMap();
			}
			_index = 0;
		}
		
		public TreeMap request() {
			
			if (_index >= _a.length) {
				int new_sz = _a.length << 1;
				TreeMap[] tmp = new TreeMap[new_sz]; // i.e. *2
				
				// Will be null below length, leave it that way
				for (int i = _a.length; i < new_sz; i++) {
					tmp[i] = new TreeMap();
				}
				_a = tmp;
			}
			
			TreeMap o = _a[_index];
			_a[_index++] = null;
			return o;
		}
		
		// Cannot return features b/c still part of this TreeMap
		// (i.e. could be reused)
		public void ret(TreeMap fa) {
			fa.clear();
			_a[--_index] = fa;
		}
	}
		
	////////////////////////////////////////////////////////////////////////
	//                       Factory for Efficient Reuse
	//
	// a has entry for every FeatureArray created (in case all are returned)
	//
	// a [ 0 1 2 ... length-2 length-1 ]
	//                  ^
	//                index = first unused FeatureArray
	//
	// When FeatureArray requested:  if index >= length, double array, create new
	//                              FeatureArrays starting at index (< are null)
	//                          return a[index], null out a[index], increment index
	//                         
	// When FeatureArray f returned: a[--index] = f
	//                          so long as only requested objects are returned,
	//                          space is guaranteed
	//                         
	////////////////////////////////////////////////////////////////////////
		
	public static IntSetFactory _ifactory = new IntSetFactory();

	// TODO: Only make array larger to store overflow of returned items!
	public static class IntSetFactory {
		
		public static final int INIT_SZ = 100; 
		public IntSet[] _a;
		public int       _index;

		public IntSetFactory() {
			_a = new IntSet[INIT_SZ];
			for (int i = 0; i < INIT_SZ; i++) {
				_a[i] = new IntSet();
			}
			_index = 0;
		}
		
		public IntSet request() {
			
			if (_index >= _a.length) {
				int new_sz = _a.length << 1;
				IntSet[] tmp = new IntSet[new_sz]; // i.e. *2
				
				// Will be null below length, leave it that way
				for (int i = _a.length; i < new_sz; i++) {
					tmp[i] = new IntSet();
				}
				_a = tmp;
			}
			
			IntSet o = _a[_index];
			_a[_index++] = null;
			return o;
		}
		
		// Cannot return features b/c still part of this IntSet
		// (i.e. could be reused)
		public void ret(IntSet fa) {
			fa.clear();
			_a[--_index] = fa;
		}
	}
}
