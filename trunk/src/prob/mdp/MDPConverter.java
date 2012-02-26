//////////////////////////////////////////////////////////////////////
//
// File:     MDPConverter.java
// Author:   Scott Sanner, University of Toronto (ssanner@cs.toronto.edu)
// Date:     9/1/2003
// Requires: comshell package
//
// Description:
//
//   Parsing of hierarchical files (i.e. LISP-like).
//
//////////////////////////////////////////////////////////////////////

// Package definition
package prob.mdp;

// Packages to import
import java.io.*;
import java.math.*;
import java.util.*;

/**
 * Input helper class.
 *
 * @version   1.0
 * @author    Scott Sanner
 * @language  Java (JDK 1.3)
 **/
public class MDPConverter
{
    public static class TermNode {
	public TermNode(Object o) {
	    if (o instanceof String) {
		try {
		    _bdVal = new BigDecimal(Integer.parseInt((String)o));
		} catch (NumberFormatException e) {
		    System.out.println("Illegal term node value: " + o.getClass() + ":" + o);
		    System.exit(1);
		}
	    } else if (o instanceof BigDecimal) {
		_bdVal = (BigDecimal)o;
	    } else {
		System.out.println("Illegal term node class: " + o.getClass() + ":" + o);
		System.exit(1);
	    }
	}
	public BigDecimal _bdVal;
    }

    public static class InternNode {
	public InternNode(String var, Integer high, Integer low) {
	    _sVar  = var;
	    _nHigh = high;
	    _nLow  = low;
	}
	public String  _sVar;
	public Integer _nHigh;
	public Integer _nLow;
    }

    /** Converter from ADD file format to the ArrayList tree
     ** form that can be read by the decision diagrams in
     ** logic.ADD.
     **/
    public static ArrayList ADDFileToTree(String filename) {
	return ADDFileToTree(HierarchicalParser.ParseFile(filename));
    }

    public static ArrayList ADDFileToTree(ArrayList token_list) {
	Iterator i = token_list.iterator();
	Integer root = null;
	HashMap id_map = new HashMap();
	Object o = null;

	while (i.hasNext()) {
	    o = i.next();
	    if (o instanceof HierarchicalParser.Keyword) {
		if (((HierarchicalParser.Keyword)o).matches("rootids")) {
		    break;
		}
	    }
	}
	
	// Get the second root
	if (!((HierarchicalParser.Keyword)o).matches("rootids")) {
	    System.out.println("No 'rootids', illegal ADD format");
	    System.exit(1);
	}
	
	i.next();
	root = new Integer(i.next().toString());
	
	// Now get nodes
	o = i.next();
	if (!((HierarchicalParser.Keyword)o).matches("nodes")) {
	    System.out.println("No 'nodes', illegal ADD format");
	    System.exit(1);
	}
	
	while (i.hasNext()) {
	    o = i.next();
	    if (o instanceof HierarchicalParser.Keyword &&
		((HierarchicalParser.Keyword)o).matches("end")) {
		break;
	    }
	    Object id = new Integer(o.toString());
	    String type = (String)i.next();
	    if (type.equals("T")) {
		i.next();
		i.next();
		i.next();
		id_map.put(id, new TermNode(i.next()));
	    } else {
		Integer high = new Integer(i.next().toString());
		Integer low  = new Integer(i.next().toString());
		i.next(); // level
		id_map.put(id, new InternNode(type, high, low));
	    }
	}
	
	// Finally build the tree
	return BuildTree(id_map, root);
    }

    /** Recursively builds tree from a HashMap containing node
     ** information and the current root.
     **/
    public static ArrayList BuildTree(HashMap nodes, Integer n) {
	Object o = nodes.get(n);
	ArrayList a = new ArrayList();
	if (o instanceof TermNode) {
	    a.add( ((TermNode)o)._bdVal );
	} else if (o instanceof InternNode) {
	    InternNode i = (InternNode)o;
	    a.add( i._sVar );
	    a.add( BuildTree(nodes, i._nHigh) );
	    a.add( BuildTree(nodes, i._nLow) );
	} else {
	    System.out.println("Illegal mapping: " + o.getClass() + ": " + o);
	    System.exit(1);
	}

	return a;
    }
}
