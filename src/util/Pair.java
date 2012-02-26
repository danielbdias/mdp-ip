//////////////////////////////////////////////////////////////////////
//
// File:     Pair.java
// Author:   Scott Sanner, University of Toronto (ssanner@cs.toronto.edu)
// Date:     9/1/2003
// Requires: comshell package
//
// Description:
//
//   Pair data structure.
//
//////////////////////////////////////////////////////////////////////

// Package definition
package util;

// Packages to import
import java.io.*;
import java.math.*;
import java.util.*;

import graph.*;

/**
 * @version   1.0
 * @author    Scott Sanner
 * @language  Java (JDK 1.3)
 **/
public class Pair implements Comparable {

    Object _o1;
    Object _o2;

    public Pair(Object o1, Object o2) {
	_o1 = o1;
	_o2 = o2;
    }

    public int hashCode() {
	return _o1.hashCode() - _o2.hashCode();
    }

    public boolean equals(Object o) {
	if (o instanceof Pair) {
	    Pair p = (Pair)o;
	    return (_o1.equals(p._o1) && _o2.equals(p._o2));
	} else {
	    return false;
	}
    }
    
    // Perform an ordered comparison, o1's then o2's if o1's are equal
    public int compareTo(Object o) {

	Pair p = (Pair)o;
	Comparable c1   = (Comparable)_o1;
	Comparable p_c1 = (Comparable)p._o1;
	int comp_o1 = c1.compareTo(p_c1);
	if (comp_o1 != 0) {
	    return comp_o1;
	} 
	
	Comparable c2   = (Comparable)_o2;
	Comparable p_c2 = (Comparable)p._o2;
	return c2.compareTo(p_c2);
    }
}
