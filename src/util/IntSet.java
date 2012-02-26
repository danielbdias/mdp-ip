/**
 * IntSet.java
 *
 *
 * Created: Mon Jul 8 13:07:49 2002
 *
 * @author Scott Sanner
 * @version 1.0
 * 
 * Note that because of _ref, this IntSet is really specific to
 * this implementation!
 */
package util;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;
import java.util.TreeSet;

// TODO: Verify remove()!!!

/**
 * An efficient class for maintaining integer sets.
 */
public class IntSet implements Comparable {

	public int[] l; // array

	public int count = 0; // num elements
	
	public IntSet() {
		l = new int[4];
		count = 0;
	}
	
	//
	// Note: This constructor does *not* perform a copy or a sort!
	public IntSet(int[] val) {
		l = val;
		count = val.length;
	}

	// This constructor does perform a copy and sort
	public IntSet(int[] val, int size) {
		l = new int[size];
		count = 0; // Start set with 0 elements and add
		for (int i = 0; i < size; i++)
			add(val[i]);
	}
	
	public void copy(IntSet s) {
		if (l.length < s.count) {
			l = new int[s.count];
		}
		count = s.count;
		System.arraycopy(s.l, 0, l, 0, count);
	}
		
	public int[] toArray() {
		int[] ret = new int[count];
		System.arraycopy(l, 0, ret, 0, count);
		return ret;
	}

	public int hashCode() {
		int val = 0;
		for (int i = 0; i < count; i++) {
			val = (val << 1) + l[i];
		}
		return val; // i.e. equal		
	}
	
	public boolean equals(Object o) {
		IntSet s = (IntSet)o;
		if (count != s.count) {
			return false; // if count < s.count => false
		} else {
			int val = 0;
			for (int i = 0; val == 0 && i < count; i++) {
				val = l[i] - s.l[i];
			}
			return (val == 0); // i.e. equal
		}		
	}
	
	public int compareTo(Object o) {
		IntSet s = (IntSet)o;
		if (count != s.count) {
			return s.count - count; // if s.count > count => positive (longer 1st)
		} else {
			int val = 0;
			for (int i = 0; val == 0 && i < count; i++) {
				val = l[i] - s.l[i];
			}
			return val;
		}
	}
	
	public void add(int oid) {

		int p = binarySearch(l, 0, count - 1, oid);

		//
		// Insert the value if we didn't find it.
		if (p < 0) {

			p = -(p + 1);
			if (count + 1 >= l.length) {

				//
				// Enlarge the array.
				int[] temp = new int[(count + 1) * 2];
				if (p == count) {
					System.arraycopy(l, 0, temp, 0, count);
				} else if (p == 0) {
					System.arraycopy(l, 0, temp, 1, count);
				} else {
					System.arraycopy(l, 0, temp, 0, p);
					System.arraycopy(l, p, temp, p + 1, count - p);
				}
				l = temp;
			} else {

				if (p != count) {

					//
					// Copy the array over by one to make room for the new ID.
					System.arraycopy(l, p, l, p + 1, count - p);
				}
			}
			l[p] = oid;
			count++;
		}
	}

	public void addAll(IntSet s) {
		addAll(s.l, s.count);
	}

	public void addAll(int[] a2) {
		addAll(a2, a2.length);
	}

	public void addAll(int[] a2, int c2) {

		//
		// Both lists are sorted so we can perform a merge sort
		// to do this in linear time
		int[] a1 = l;
		int c1 = count;
		int[] ret = new int[(c1 + c2) * 2];

		//
		// Perform the actual merge
		int i1 = 0;
		int i2 = 0;
		int l1, l2;
		int j = 0;
		while (i1 < c1 && i2 < c2) {
			l1 = a1[i1];
			l2 = a2[i2];
			if (l1 < l2) {
				ret[j++] = l1;
				i1++;
			} else if (l1 > l2) {
				ret[j++] = l2;
				i2++;
			} else {
				ret[j++] = l1;
				i1++;
				i2++;
			}
		}

		if (i1 < c1) {
			System.arraycopy(a1, i1, ret, j, c1 - i1);
			j += c1 - i1;
		}

		if (i2 < c2) {
			System.arraycopy(a2, i2, ret, j, c2 - i2);
			j += c2 - i2;
		}

		l = ret;
		count = j;
	}

	public boolean contains(int oid) {
		return (binarySearch(l, 0, count - 1, oid) >= 0);
	}

	public void remove(int oid) {

		int p = binarySearch(l, 0, count - 1, oid);

		//
		// Remove the value if we found it.
		if (p >= 0) {

			//
			// Copy the array over by one to remove the old ID.
			System.arraycopy(l, p + 1, l, p, count - p - 1);
			count--;
		}
	}

	public void removeAll(IntSet s) {
		removeAll(s.l, s.count);
	}

	public void removeAll(int[] a1) {
		removeAll(a1, a1.length);
	}

	public void removeAll(int[] a2, int c2) {
		//
		// This method should really use a mark/compact algorithm but
		// for now we're just removing objects element by element.
		// Probably not so bad if a2 is relatively small.
		int p;
		for (int i = 0; i < c2; i++) {
			p = binarySearch(l, 0, count - 1, a2[i]);

			//
			// Remove the value if we found it.
			if (p >= 0) {

				//
				// Copy the array over by one to remove the old ID
				System.arraycopy(l, p + 1, l, p, count - p - 1);
				count--;
			}
		}
	}

	public String toString() {
		StringBuffer sb = new StringBuffer("[ ");
		for (int i = 0; i < count; i++) {
			sb.append(l[i] + " ");
		}
		sb.append("]");
		return sb.toString();
	}

	public void clear() {
		count = 0;
	}

	public Object clone() {
		IntSet n = new IntSet();
		n.count = count;
		n.l = new int[l.length];
		System.arraycopy(l, 0, n.l, 0, count);
		return n;
	}

	public boolean isEmpty() {
		return (count == 0);
	}

	// Can do this efficiently by ensuring every element of
	// this is in sup
	//
	// Is sets are close in size then just iterating through
	// sup makes sense.
	//
	// Otherwise binary search would be better
	public boolean isSubsetOf(IntSet sup) {
		if (sup.count < this.count) return false;
		int sup_pos = 0;
		for (int i = 0; i < count; i++) {
			int el = this.l[i];
			
			// Find el in sup
			while (sup_pos < sup.count && sup.l[sup_pos] != el) {
				++sup_pos;
			}
			
			// Was el found?
			if (sup_pos >= sup.count || sup.l[sup_pos] != el) {
				return false;
			}
			
			// Inc sup_pos since next element must come after it
			++sup_pos;
		}
		
		// All elements must have been found
		return true;
	}
	
	/**
	 * Do a binary search on a region of an array. A curious omission from
	 * java.util.Arrays.
	 * 
	 * @param a
	 *            The array to be searched
	 * @param low
	 *            The low end of the region to be searched
	 * @param high
	 *            The high end of the region to be searched
	 * @param key
	 *            The value that we're searching for
	 * @return index of the search key, if it is contained in the list;
	 *         otherwise, <tt>(-(<i>insertion point</i>) - 1)</tt>. The
	 *         <i>insertion point</i> is defined as the point at which the key
	 *         would be inserted into the list: the index of the first element
	 *         greater than the key, or <tt>list.size()</tt>, if all elements
	 *         in the list are less than the specified key. Note that this
	 *         guarantees that the return value will be &gt;= 0 if and only if
	 *         the key is found.
	 */
	public static int binarySearch(int[] a, int low, int high, int key) {
		while (low <= high) {
			int mid = (low + high) / 2;
			int midVal = a[mid];
			if (midVal < key)
				low = mid + 1;
			else if (midVal > key)
				high = mid - 1;
			else
				return mid; // key found
		}
		return -(low + 1); // key not found.
	}
	
	public static void main(String[] args) {
		Test1();
		
		for (int sz = 10; sz <= 1000000; sz += sz) {
			Test2(sz);
		}
	}
	
	public static void Test1() {
		TreeSet ts = new TreeSet();
		
		IntSet ts1 = new IntSet(); 
		ts1.add(1);
		ts1.add(2);
		ts1.add(1);
		ts1.add(2);
		
		IntSet ts2 = new IntSet();
		ts2.add(2);
		ts2.add(3);
		ts2.add(2);
		ts2.add(3);
		
		IntSet ts3 = new IntSet();
		ts3.add(3);
		ts3.add(4);
		ts3.add(4);
		ts3.add(3);
		
		IntSet ts4 = new IntSet();
		ts4.add(3);
		ts4.add(1);
		ts4.add(2);
		
		IntSet ts5 = new IntSet();
		ts5.add(2);
		ts5.add(4);
		ts5.add(3);
		
		IntSet ts6 = new IntSet();
		ts6.add(5);
		ts6.add(6);

		IntSet ts7 = new IntSet();
		ts7.add(1);
		ts7.add(2);
		ts7.add(3);
		ts7.add(4);
		
		ts.add(ts1); 
		ts.add(ts2); 
		ts.add(ts3); 
		ts.add(ts4); 
		ts.add(ts5); 
		ts.add(ts6);
		ts.add(ts7);
		
		System.out.println(ts);
		
		for (Iterator i = ts.iterator(); i.hasNext(); ) {
			IntSet i1 = (IntSet)i.next();
			for (Iterator j = ts.iterator(); j.hasNext(); ) {
				IntSet i2 = (IntSet)j.next();
				TestSubset(i1, i2);
			}
		}
	}
	
	public static void TestSubset(IntSet i1, IntSet i2) {
		System.out.println(i1 + " isSubsetOf " + i2 + " = " + i1.isSubsetOf(i2));
	}
	
	public static void Test2(int max) {
		Random rg = new Random();
		
		TreeSet ts = new TreeSet();
		IntSet yes = null;
		IntSet no = new IntSet(); no.add(-1); no.add(-2);
		for (int i = 0; i < max; i++) {
			int sz = rg.nextInt(3) + 2;
			IntSet s = new IntSet();
			for (int j = 0; j < sz; j++) {
				s.add(rg.nextInt(100));
			}
			ts.add(s);
			if (i == (int)(max/2)) yes = s; 
		}
		
		long ctime = System.currentTimeMillis();
		for (int t = 0; t < 10000; t++) { ts.contains(yes); ts.contains(no); }
		System.out.print(max + ": true/" + ts.contains(yes) + 
				           " false/" + ts.contains(no));
		long etime = System.currentTimeMillis() - ctime;
		System.out.println(", time = " + etime + " ms");
	}
}
