//////////////////////////////////////////////////////////////////////
//
// File:     HierarchicalParser.java
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
import java.math.*;
import java.util.*;

/**
 * Input helper class.
 *
 * @version   1.0
 * @author    Scott Sanner
 * @language  Java (JDK 1.3)
 **/
public class HierarchicalParser
{
    /** Keyword identifier
     **/
    public static class Keyword { 
    	public String _s;
    	
    	public Keyword(String s) {
    		_s = s;
    	}
    	
    	public String toString() {
    		return "K:[" +_s + "]";
    	}
    	
    	public boolean matches(String s) {
    		return _s.equalsIgnoreCase(s);
    	}
    }

    /** Static file parsing methods
     **/
    public static ArrayList<Object> parseFile(String filename) {
		try {
		    TokenStream ts = new TokenStream();
		    ts.open(filename);
		    
		    return parseFileInt(ts, 0);
		} catch (TokenStreamException tse) {
		    System.err.println("Error: " + tse);
		    return null;
		}
    }

    /** 
     * Handles paren nesting and converting Integer.Integer -> Double.
     * Assumes an Integer must follow a period.
     **/
    public static ArrayList<Object> parseFileInt(TokenStream ts, int level) {

    	ArrayList<Object> a = new ArrayList<Object>();

    	try {
    		Token t;
    		
    		while ((t = ts.nextToken()) != null) {
    			if (t._sToken == null) {
		    
    				switch (t._nSymbolID) {
    					case Token.LPAREN: 
    						a.add(parseFileInt(ts, level + 1)); 
    						break;
    					case Token.RPAREN: 
    						return a;
    					case Token.PERIOD: {
    						Token t_next = ts.nextToken();
    						if (Character.isLetter(t_next._sToken.charAt(0))) {
    							// Keyword - so can load Spudd output files as well
    							a.add(new Keyword(t_next._sToken));
    						} else {
    							// Decimal number
    							int max_index = a.size() - 1;
    							Object o = a.get(max_index);
    							String bds = null;
    							if (o instanceof String) {
    								try {
    									bds = ((String)o) + "." + t_next._sToken;
    									a.set(max_index, new BigDecimal(bds));
    								} catch (NumberFormatException nfe) {
    									System.err.println("Parse error after period: " + t);
    									System.err.println("Could not translate: " + bds);
    									System.exit(1);
    								}
    							} else {
    								System.err.println("Number must preceed '.' " + 
    										"followed by number: " + t);
    								System.exit(1);
    							}
    						}
    					} break;
    				}
    			} else if (t._bInteger) {
    				a.add(t._sToken); // Could make into a double
    			} else {
    				a.add(t._sToken);
    			}
    		}
    	} catch (TokenStreamException tse) {
    		System.out.println("Error: " + tse);
    		return null;
    	}

    	if (level != 0) {
    		System.err.println("'" + ts._sFilename + 
			       "' contains unbalanced parentheses!");
    		System.exit(1);
    	} 
	
    	return a;
    }
}
