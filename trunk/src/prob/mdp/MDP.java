//////////////////////////////////////////////////////////////////////////
//
// File:     MDP.java
// Author:   Scott Sanner, University of Toronto (ssanner@cs.toronto.edu)
// Date:     9/1/2003
//
// Description:
//
//   An MDP inference package that uses both Tables, ADDs, AADDs as the
//   underlying computational mechanism.  (All via the logic.add.FBR
//   interface.)  See SPUDD (Hoey et al, UAI 1999) for more details on the
//   algorithm.
//
//////////////////////////////////////////////////////////////////////////

// Package definition
package prob.mdp;

// Packages to import
import java.io.*;
import java.math.*;
import java.text.*;
import java.util.*;

// DD & FBR interfaces
import logic.add.*;
import graph.*;

/**
 * Main MDP inference class
 *
 * @version   1.0
 * @author    Scott Sanner
 * @language  Java (JDK 1.3)
 **/
public class MDP
{

    /////////////////////////////////////////////////////////////////////////////
    //                      Class Constants and Data Members
    /////////////////////////////////////////////////////////////////////////////

    /* Local constants */
    public final static int VERBOSE_LEVEL = 0; // Determines how much output is displayed
    public final static boolean ALWAYS_FLUSH = false;         // Always flush DD caches?
    public final static double FLUSH_PERCENT_MINIMUM = 0.1d; // Won't flush until < this amt

    /* For printing */
    public static DecimalFormat _df = new DecimalFormat("#.###");

    /* Static variables */
    public static long _lTime;       // For timing purposes
    public static Runtime RUNTIME = Runtime.getRuntime();

    /* Local vars */
    public ArrayList  _alVars;        // List of variable names (including primes) index is ID
    public TreeMap    _tmID2Var;      // Maps names -> Integers (including primes a',b',etc...)
    public TreeMap    _tmVar2ID;      // Maps names -> Integers (including primes a',b',etc...)
    public HashMap    _hmPrimeRemap;  // Maps non-prime GIDs to their primed counterparts
    public ArrayList  _alOrder;       // The variable order used in decision diagrams
    public Map        _hmName2Action; // List of actions (see Action.java)
    public FBR        _context;
    public Object     _rewardDD;      // The reward for this MDP
    public Object     _valueDD;       // The resulting value function once this MDP has been solved
    public Object     _maxDD;
    public Object     _prevDD;
    public BigDecimal _bdDiscount;    // Discount (gamma) for MDP
    public BigDecimal _bdTolerance;   // Tolerance (gamma) for MDP
    public int        _nDDType;       // Type of DD to use
    public TreeMap    _tmAct2Regr;    // Cached DDs from last regression step
    public int        _nIter;      
    public int        _nMaxRegrSz;
    public String     _sRegrAction;
    public ArrayList  _alSaveNodes;   // Nodes to save during cache flushing

    /////////////////////////////////////////////////////////////////////////////
    //                               Constructors
    /////////////////////////////////////////////////////////////////////////////
    
    /** Constructor - filename
     **/
    public MDP(String filename, int dd_type) {
	this(HierarchicalParser.parseFile(filename), dd_type);
    }

    /** Constructor - pre-parsed file
     **/
    public MDP(ArrayList input, int dd_type) {
    	System.out.println(input);
    	//System.exit(1);
    	
	_prevDD = _maxDD = _rewardDD = _valueDD = null;
	_nDDType    = dd_type;
	_alVars     = new ArrayList();
	_alOrder    = new ArrayList();
	_tmVar2ID   = new TreeMap();
	_tmID2Var   = new TreeMap();
	_tmAct2Regr = new TreeMap();
	_hmPrimeRemap  = new HashMap();
	_hmName2Action = new TreeMap();
	_alSaveNodes   = new ArrayList();
	_bdDiscount  = new BigDecimal(""+(-1));
	_bdTolerance = new BigDecimal(""+(-1));
	_nIter       = -1;
	_sRegrAction = null;
	_nMaxRegrSz  = -1;

	buildMDP(input);
    }

    /////////////////////////////////////////////////////////////////////////////
    //                       Generic MDP Inference Methods
    /////////////////////////////////////////////////////////////////////////////

    /** MDP inference methods
     **/ 
    public int solve(double precision) {

	// Result goes in _valueDD
	int max_iter = 0;
	boolean b_iter = false;
	if (precision >= 1.0d) {
	    b_iter   = true;
	    max_iter = (int)precision;
	}

	//////////////////////////////////////////////////////////////
	// Set value function equal to reward
	//////////////////////////////////////////////////////////////
	_valueDD = _rewardDD;

	// Other initialization
	int iter = 0;
	double max_diff = Double.POSITIVE_INFINITY;
	double tolerance    = _bdTolerance.doubleValue();
	boolean error_decreasing = true;
	System.out.println("Using discount:  " + _bdDiscount);
	System.out.println("Using tolerance: " + tolerance + "\n");

	//////////////////////////////////////////////////////////////
	// Iterate until convergence (or max iterations)
	//////////////////////////////////////////////////////////////
	while (	(max_diff >= tolerance) /* convergence */ 
		&& (b_iter && (iter < max_iter)) /* iteration check */) {
	    
	    _nIter = iter;

	    // Cache maintenance
	    flushCaches();

	    // Error decreasing?
	    System.out.print(error_decreasing ? "  " : "* ");
	    System.out.println("Iteration #" + iter +  ", " + 
			       _context.countExactNodes(_valueDD) + " nodes / " +
			       _context.getCacheSize() + " cache / " + 
			       MemDisplay() + " bytes " +
			       "[" + _df.format(max_diff) + "], mr:[" +
			       _df.format(_context.getMaxValue(_valueDD)) + "]");
	    
	    // Flush cache now to prevent accumulation of src links
	    //Runtime.getRuntime().gc();

	    // Prime the value function diagram so it is in terms of next state vars!
	    _prevDD = _valueDD;
	    _valueDD = _context.remapGIDsInt(_valueDD, _hmPrimeRemap);

	    //////////////////////////////////////////////////////////////
	    // Iterate over each action
	    //////////////////////////////////////////////////////////////
	    _maxDD = null;
	    Iterator i = _hmName2Action.entrySet().iterator();
	    _tmAct2Regr.clear();
	    while (i.hasNext()) {

		Map.Entry me = (Map.Entry)i.next();
		Action a = (Action)me.getValue();
		_sRegrAction = (String)me.getKey();

		//////////////////////////////////////////////////////////////
		// Regress the current value function through each action
		//////////////////////////////////////////////////////////////
		Object regr = regress(_valueDD, a);
		
		// Cache maintenance
		clearSaveNodes(); 
		saveNode(regr);
		flushCaches();

		// Screen output
		if (VERBOSE_LEVEL >= 1) {
		    System.out.println("  - After regress '" + a._sName +  "', " + 
				       _context.countExactNodes(regr) + " nodes / " +
				       _context.getCacheSize() + " cache");
		}

		// In case comparing last regressions, uncomment the following
		//_tmAct2Regr.put(a._sName, regr);
		
		//////////////////////////////////////////////////////////////
		// Take the max over this action and the previous action
		//////////////////////////////////////////////////////////////
		_maxDD = ((_maxDD == null) ? regr : _context.applyInt(_maxDD, regr, DD.ARITH_MAX));

		// Cache maintance
		flushCaches();

		// Screen output
		if (VERBOSE_LEVEL >= 1) {
		    System.out.println("  - After max '" + a._sName +  "', " + 
				       _context.countExactNodes(_maxDD) + " nodes / " +
				       _context.getCacheSize() + " cache");
		}
	    }

	    //////////////////////////////////////////////////////////////
	    // Discount the max'ed value function backup and add in reward
	    //////////////////////////////////////////////////////////////
	    _valueDD = _context.applyInt(_rewardDD, _context.scalarMultiply(
                                            _maxDD, _bdDiscount.doubleValue()), DD.ARITH_SUM);

	    // Screen output
	    if (VERBOSE_LEVEL >= 1) {
		System.out.println("\n  - After sum, " + 
				   _context.countExactNodes(_valueDD) + " nodes / " +
				   _context.getCacheSize() + " cache");
	    }

	    // Prune?
	    _valueDD = _context.pruneNodes(_valueDD);

	    /////////////////////////////////////////////////////////////////////
	    // Compute max difference between current and previous value function
	    /////////////////////////////////////////////////////////////////////
	    Object diff = _context.applyInt(_valueDD, _prevDD, DD.ARITH_MINUS);
	    double max_diff_prev = max_diff;
	    double max_pos_diff = _context.getMaxValue(diff);
	    double max_neg_diff = _context.getMinValue(diff);
	    max_diff = Math.max(Math.abs(max_pos_diff), Math.abs(max_neg_diff));
	    error_decreasing = (max_diff < max_diff_prev);

	    // Increment counter
	    iter++;
	}
	
	// Flush caches and return number of iterations
	flushCaches();
	return iter;
    }

    /** Regress a DD through an action
     **/
    public Object regress(Object vfun, Action a) {
	
	// For every next-state var in Action, multiply by DD and sumOut var
	long max = -1;
	Iterator i = a._tmID2DD.entrySet().iterator();
	Object dd_ret = vfun;

	// Find what gids are currently in vfun (probs cannot introduce new primed gids)
	Set gids = _context.getGIDs(vfun);
	if (VERBOSE_LEVEL >= 1) {
	    System.out.println("Regressing action: " + a._sName + "\nGIDs: " + gids);
	}
	
	//////////////////////////////////////////////////////////////
	// For each next state variable in DBN for action 'a'
	//////////////////////////////////////////////////////////////
	while (i.hasNext()) {

	    Map.Entry me = (Map.Entry)i.next();
	    Integer head_id = (Integer)me.getKey();
	    
	    // No use in multiplying by a gid that does not exist (and will sum to 1)
	    if (!gids.contains(head_id)) {
		if (VERBOSE_LEVEL >= 1) {
		    System.out.println("Skipping " + head_id);
		}
	    	continue;
	    }

	    // Get the dd for this action
	    Object dd = me.getValue();

	    // Screen output
	    if (VERBOSE_LEVEL >= 2) {
		System.out.println("  - Summing out: " + head_id);
	    }

	    ///////////////////////////////////////////////////////////////////
	    // Multiply next state variable DBN into current value function
	    ///////////////////////////////////////////////////////////////////
	    dd_ret = _context.applyInt(dd_ret, dd, DD.ARITH_PROD);
	    int regr_sz = _context.getGIDs(dd_ret).size();
	    if (regr_sz > _nMaxRegrSz) {
		_nMaxRegrSz = regr_sz;
	    }

	    ///////////////////////////////////////////////////////////////////
	    // Sum out next state variable
	    ///////////////////////////////////////////////////////////////////
	    dd_ret = _context.opOut(dd_ret, head_id.intValue(), DD.ARITH_SUM); // CHANGED - 11-17-04

	    // Cache maintenance
	    clearSaveNodes(); 
	    saveNode(dd_ret);
	    flushCaches();
	}

	// Return regressed value function (which is now in terms of prev state vars)
	return dd_ret;
    }

    /////////////////////////////////////////////////////////////////////////////
    //                           DD Cache Maintenance
    /////////////////////////////////////////////////////////////////////////////

    /** Clear nodes on save list
     **/
    public void clearSaveNodes() {
	_alSaveNodes.clear();
    }

    /** Add node to save list
     **/
    public void saveNode(Object dd) {
	_alSaveNodes.add(dd);
    }

    /** Frees up memory... only do this if near limit?
     **/
    public void flushCaches() {
	if (!ALWAYS_FLUSH &&
	    ((double)RUNTIME.freeMemory() / 
	     (double)RUNTIME.totalMemory()) > FLUSH_PERCENT_MINIMUM) {
	    return; // Still enough free mem to exceed minimum requirements
	}

	_context.clearSpecialNodes();
	Iterator i = _hmName2Action.values().iterator();
	while (i.hasNext()) {
	    Action a = (Action)i.next();
	    Iterator j = a._hsTransDDs.iterator();
	    while (j.hasNext()) {
		_context.addSpecialNode(j.next());
	    }
	}
	_context.addSpecialNode(_rewardDD);
	_context.addSpecialNode(_valueDD);
	if (_maxDD != null)  _context.addSpecialNode(_maxDD);
	if (_prevDD != null) _context.addSpecialNode(_prevDD);

	Iterator j = _alSaveNodes.iterator();
	while (j.hasNext()) {
	    _context.addSpecialNode(j.next());
	}
	_context.flushCaches(false);
    }

    /////////////////////////////////////////////////////////////////////////////
    //                           MDP Construction Methods
    /////////////////////////////////////////////////////////////////////////////

    /** MDP construction methods
     **/ 
    public void buildMDP(ArrayList input) {

	if (input == null) {
	    System.out.println("Empty input file!");
	    System.exit(1);
	}

	Iterator i = input.iterator();
	Object o;

	// Set up variables
	o = i.next();
	if ( !(o instanceof String) || !((String)o).equalsIgnoreCase("variables")) {
	    System.out.println("Missing variable declarations: " + o);
	    System.exit(1);
	}
	o = i.next();
	int id_count = 1;
	_alVars = (ArrayList)((ArrayList)o).clone();
	Iterator vars = _alVars.iterator();
	while (vars.hasNext()) {
	    String vname = ((String)vars.next()) + "'";
	    _tmID2Var.put(new Integer(id_count), vname);
	    _tmVar2ID.put(vname, new Integer(id_count));
	    _alOrder.add(new Integer(id_count));
	    ++id_count;
	}
	int nvars = _alOrder.size();
	vars = _alVars.iterator();
	while (vars.hasNext()) {
	    String vname = ((String)vars.next());
	    _tmID2Var.put(new Integer(id_count), vname);
	    _tmVar2ID.put(vname, new Integer(id_count));
	    _alOrder.add(new Integer(id_count));
	    _hmPrimeRemap.put(new Integer(id_count), new Integer(id_count - nvars));
	    ++id_count;	    
	}
	_context = new FBR(_nDDType, _alOrder);
	//System.out.println("Remap: " + _hmPrimeRemap);
	//System.exit(1);
	
	// Set up actions
	while(true) {
	    o = i.next();
	    if ( !(o instanceof String) || !((String)o).equalsIgnoreCase("action")) {
		break;
	    }

	    // o == "action"
	    String aname = (String)i.next();
	    HashMap cpt_map = new HashMap();
	    
	    o = i.next();
	    while ( !((String)o).equalsIgnoreCase("endaction") ) {
		cpt_map.put((String)o + "'", (ArrayList)i.next());
		o = i.next();
	    }

	    _hmName2Action.put(aname, new Action(this, aname, cpt_map));
	}

	// Set up reward
	if ( !(o instanceof String) || !((String)o).equalsIgnoreCase("reward")) {
	    System.out.println("Missing reward declaration: " + o);
	    System.exit(1);
	}
	ArrayList reward = (ArrayList)i.next();
	//System.out.println(reward);
	_rewardDD = _context.buildDDFromUnorderedTree(reward, _tmVar2ID);
	if (DD.PRUNE_TYPE == DD.REPLACE_RANGE) {
	    System.out.println("MDP: PruneReward not implemented");
	    System.exit(1);
	    //System.out.println("Pruning reward...");
	    // TODO: _context.pruneNodes(_rewardDD);
	}

	// Read discount and tolerance
	o = i.next();
	if ( !(o instanceof String) || !((String)o).equalsIgnoreCase("discount")) {
	    System.out.println("Missing discount declaration: " + o);
	    System.exit(1);
	}
	_bdDiscount = ((BigDecimal)i.next());

	o = i.next();
	if ( !(o instanceof String) || !((String)o).equalsIgnoreCase("tolerance")) {
	    System.out.println("Missing tolerance declaration: " + o);
	    System.exit(1);
	}
	_bdTolerance = ((BigDecimal)i.next());	

	// Normalize the reward [0,1] !!!
	//double max = max(_rewardDD);
	//BigDecimal inv_rmax = new BigDecimal(""+((1.0d - _bdDiscount.doubleValue())/max));
	//System.out.println(inv_rmax);
	//System.exit(1);
	//_rewardDD = scalarMultiply(_rewardDD, inv_rmax);
    }

    /////////////////////////////////////////////////////////////////////////////
    //                              Testing Interface
    /////////////////////////////////////////////////////////////////////////////

    /** Basic testing interface.
     **/
    public static void main(String args[]) 
    {
	if (args.length < 6 || args.length > 7) {
	    System.out.println("\nMust enter MDP-filename, " + 
			       "prune-prec, type<none,low,high,min,max,avg,range>" + 
			       "\n           iter-Tab iter-ADD iter-AADD [spudd-file]!\n");
	    System.exit(1);
	}

	// Parse problem filename
	String filename = args[0];
	String spuddfile = null;

	// Parse prune precision and type
	int prune_type = -1;
	double prune_prec = -1d;
	try {
	    prune_prec = (new BigDecimal(args[1])).doubleValue();
	} catch (NumberFormatException nfe) {
	    System.out.println("\nIllegal precision specification\n");
	    System.exit(1);
	}
	if (args[2].equalsIgnoreCase("none")) {
	    prune_type = ADD.NO_REPLACE;
	} else if (args[2].equalsIgnoreCase("low")) {
	    prune_type = ADD.REPLACE_LOW;	    
	} else if (args[2].equalsIgnoreCase("high")) {
	    prune_type = ADD.REPLACE_HIGH;
	} else if (args[2].equalsIgnoreCase("min")) {
	    prune_type = ADD.REPLACE_MIN;	    
	} else if (args[2].equalsIgnoreCase("max")) {
	    prune_type = ADD.REPLACE_MAX;
	} else if (args[2].equalsIgnoreCase("avg")) {
	    prune_type = ADD.REPLACE_AVG;
	} else if (args[2].equalsIgnoreCase("range")) {
	    prune_type = ADD.REPLACE_RANGE;
	} else {
	    System.out.println("\nIllegal prune type");
	    System.exit(1);
	}
	
	// Set up FBR for all operations
	FBR.SetPruneInfo(prune_type, prune_prec);

	// Parse iterations
	int iter_tab   = -1;
	int iter_add   = -1;
	int iter_aadd  = -1;
	try {
	    iter_tab   = Integer.parseInt(args[3]);
	    iter_add   = Integer.parseInt(args[4]);
	    iter_aadd  = Integer.parseInt(args[5]);
	} catch (NumberFormatException nfe) {
	    System.out.println("\nIllegal iteration value\n");
	    System.exit(1);
	}
	if (args.length == 7) {
	    spuddfile = args[6];
	}

	// Show args
	System.out.println("\nRunning with args '" + filename + "' " + 
			   prune_type + ":" + prune_prec + 
			   ", <tab: " + iter_tab + ", add:" + iter_add + 
			   ", aadd:" + iter_aadd + 
			   ">, " + ((spuddfile == null) ? "no spudd comp" : spuddfile) + 
			   "\n");

	// Build a new TABLE-based MDP from file, display, solve
	MDP mdp1 = new MDP(filename, DD.TYPE_TABLE);
	ResetTimer();
	long iter1  = mdp1.solve(iter_tab);
	long time1  = GetElapsedTime();
	long nodes1 = mdp1._context.countExactNodes(mdp1._valueDD);
	long cache1 = mdp1._context.getCacheSize();
	double max_val1 = mdp1._context.getMaxValue(mdp1._valueDD);
	System.out.println();
	System.out.println(mdp1);

	// Build a new AADD MDP from file, display, solve
	MDP mdp2 = new MDP(filename, DD.TYPE_ADD);
	ResetTimer();
	long iter2  = mdp2.solve(iter_add);
	long time2  = GetElapsedTime();
	long nodes2 = mdp2._context.countExactNodes(mdp2._valueDD);
	long cache2 = mdp2._context.getCacheSize();
	double max_val2 = mdp2._context.getMaxValue(mdp2._valueDD);
	System.out.println();
	System.out.println(mdp2);
	Graph g = mdp2._context.getGraph(mdp2._valueDD);
	g.launchViewer(1250, 700);

	// Build a new AADD MDP from file, display, solve
	MDP mdp3 = new MDP(filename, DD.TYPE_AADD);
	ResetTimer();
	long iter3  = mdp3.solve(iter_aadd);
	long time3  = GetElapsedTime();
	long nodes3 = mdp3._context.countExactNodes(mdp3._valueDD);
	long cache3 = mdp3._context.getCacheSize();
	double max_val3 = mdp3._context.getMaxValue(mdp3._valueDD);
	System.out.println();
	System.out.println(mdp3);

	// Build the SPUDD ADD if appropriate
	Object SPUDD = null;
	if (spuddfile != null) {
	    SPUDD = mdp2._context.buildDDFromUnorderedTree(MDPConverter.ADDFileToTree(spuddfile), 
							   mdp2._tmVar2ID);
	}

	// Compare representations
	//CompareRep(mdp1, mdp2);
	//CompareRep(mdp3, mdp2);

	// Compare last regressions
	//mdp1.solve(1);
	//mdp2.solve(1);
	//CompareLastRegr(mdp1, mdp2);
	//System.exit(1);
	
	// Compare results
	//PrintEnum((ADD)mdp1._valueDD, (AADD)mdp2._valueDD, mdp1._tmID2Var, true);
	System.out.println("Final results:");
	System.out.println("--------------\n");
	System.out.println("   Table MDP: " + iter1 + " iterations, (" + mdp1._nMaxRegrSz + "), " + 
			   time1 + " ms, " + nodes1 + 
			   " nodes, " + cache1 + " cache, max val: " + 
			   DD._df.format(max_val1) /*+ " [-" + _df.format(range1) + "]"*/);
	System.out.println("   ADD MDP:   " + iter2 + " iterations, (" + mdp2._nMaxRegrSz + "), " + 
			   time2 + " ms, " + nodes2 + 
			   " nodes, " + cache2 + " cache, max_val: " + 
			   DD._df.format(max_val2) /*+ " [-" + _df.format(range2) + "]"*/);
	System.out.println("   AADD MDP:  " + iter3 + " iterations, (" + mdp3._nMaxRegrSz + "), " + 
			   time3 + " ms, " + nodes3 + 
			   " nodes, " + cache3 + " cache, max_val: " + 
			   DD._df.format(max_val3) /*+ " [-" + _df.format(range3) + "]"*/);

	// Compare value functions
	if (DD.PRUNE_TYPE != DD.REPLACE_RANGE) {
	    System.out.println("\n  Max diff Table/ADD  = " + 
			       FBR.CompareEnum(mdp1._context, mdp1._valueDD, mdp2._context, mdp2._valueDD));
	    System.out.println("  Max diff ADD/AADD   = " + 
			       FBR.CompareEnum(mdp2._context, mdp2._valueDD, mdp3._context, mdp3._valueDD));

	    // Compare to SPUDD result if provided
	    if (SPUDD != null) {
		System.out.print("  Max diff AADD/SPUDD = " + FBR.CompareEnum(mdp2._context, SPUDD, mdp3._context, mdp3._valueDD));
		System.out.println(", SPUDD max val: " + mdp2._context.getMaxValue(SPUDD) + ", " + 
				   mdp2._context.countExactNodes(SPUDD) + " nodes");
		if (mdp2._context.countExactNodes(SPUDD) < 20) {
		    System.out.println("\n\n------------SPUDD------------");
		    System.out.println(SPUDD);
		    System.out.println("-----------------------------");
		}
	    }
	}

	System.out.println();
	mdp1._context.pruneReport();
	mdp2._context.pruneReport();
	mdp3._context.pruneReport();
    }

    /////////////////////////////////////////////////////////////////////////////
    //                               Miscellaneous
    /////////////////////////////////////////////////////////////////////////////

    public String toString() {
	StringBuffer sb = new StringBuffer();
	sb.append("\nMDP Definition:\n===============\n");
	sb.append("Actions (" + _hmName2Action.size() + "):\n");
	//sb.append(_hmName2Action.toString() + "\n\n");
	Iterator actions = _hmName2Action.entrySet().iterator();
	while (actions.hasNext()) {
	   Map.Entry me = (Map.Entry)actions.next();
	   sb.append("   " + me.getKey() + "\n" /*+ ":\n" + me.getValue() + "\n\n"*/);
	   //sb.append("   " + me.getKey() + "\n" + ":\n" + me.getValue() + "\n\n");
	}
	sb.append("\nMDP Definition (cont):\n======================\n");
	sb.append("Vars:        " + _alVars + "\n");
	sb.append("Order:       " + _alOrder + "\n");
	sb.append("ID Map:      " + _tmVar2ID + "\n");
	sb.append("Inverse Map: " + _tmID2Var + "\n");
	sb.append("Discount:    " + _bdDiscount + "\n");
	sb.append("Tolerance:   " + _bdTolerance + "\n");
	sb.append("DD Type:     ");
	switch (_nDDType) {
	case DD.TYPE_TABLE: sb.append("TABLE\n");   break;
	case DD.TYPE_ADD:   sb.append("ADD\n");   break;
	case DD.TYPE_AADD:  sb.append("AADD\n");  break;
	default:       sb.append("Unknown\n"); break;
	}
	if (_context.countExactNodes(_rewardDD) < 25/*20*/) {
	    sb.append("Reward - \n" + _context.printNode(_rewardDD) + "\n");
	}
	if (_valueDD != null && _context.countExactNodes(_valueDD) < 25/*20*/) {
	    sb.append("Value fun: " + _context.printNode(_valueDD) + "\n");
	}
	return sb.toString();
    }

    /** Compare the last regression of the ADD/AADD representation
     **/
    public static void CompareLastRegr(MDP mdp1, MDP mdp2) {

	// Cycle through all actions and compare CPTs
	System.out.println("Comparing last regression");
	Iterator i1 = mdp1._tmAct2Regr.entrySet().iterator();
	Iterator i2 = mdp2._tmAct2Regr.entrySet().iterator();
	while (i1.hasNext()) {
	    Map.Entry me1 = (Map.Entry)i1.next();
	    Map.Entry me2 = (Map.Entry)i2.next();
	    System.out.println("- Comparing regr " + me1.getKey() + "(" + 
			       mdp1._context.countExactNodes(me1.getValue())+ ") / " + me2.getKey() + "(" +
			       mdp2._context.countExactNodes(me2.getValue()) + ") md = " +
			       FBR.CompareEnum(mdp1._context, me1.getValue(), 
					       mdp2._context, me2.getValue()));
	    //PrintEnum((ADD)me1.getValue(), (AADD)me2.getValue(), mdp1._tmID2Var, true);
	    //if (!((AADD)me2.getValue()).verifyOrder()) {
	    //	System.out.println("AADD order incorrect!");
	    //}
	    System.out.println(mdp1._context.printNode(me1.getValue()));
	    System.out.println(mdp2._context.printNode(me2.getValue()));
	}
    }

    /** Compare the ADD and AADD representations
     **/
    public static void CompareRep(MDP mdp1, MDP mdp2) {

	// Compare reward
	System.out.println("Reward md = " + 
			   FBR.CompareEnum(mdp1._context, mdp1._rewardDD, 
					   mdp2._context, mdp2._rewardDD));
	
	// Cycle through all actions and compare CPTs
	Iterator i1 = mdp1._hmName2Action.values().iterator();
	Iterator i2 = mdp2._hmName2Action.values().iterator();
	while (i1.hasNext()) {
	    Action a1 = (Action)i1.next();
	    Action a2 = (Action)i2.next();
	    System.out.println("Comparing " + a1._sName + "/" + a2._sName);
	    Iterator i3 = a1._tmID2DD.entrySet().iterator();
	    Iterator i4 = a2._tmID2DD.entrySet().iterator();
	    while (i3.hasNext()) {
		Map.Entry me1 = (Map.Entry)i3.next();
		Map.Entry me2 = (Map.Entry)i4.next();
		double diff = FBR.CompareEnum(mdp1._context, me1.getValue(), 
					      mdp2._context, me2.getValue());
		System.out.println("- Comparing " + me1.getKey() + "/" + me2.getKey() + 
				   " md = " + diff);
		/*if (!((ADD)me1.getValue()).verifyOrder() || 
		  !((AADD)me2.getValue()).verifyOrder()) {
		  System.out.println("Order!!!");
		  System.exit(1);
		  }
		  if (diff > 0) {
		  System.out.println((ADD)me1.getValue());
		  System.out.println((AADD)me2.getValue());
		  PrintEnum((ADD)me1.getValue(), (AADD)me2.getValue(), mdp1._tmID2Var, true);
		  }*/
	    }
	}
    }
    
    public static void ResetTimer() {
	_lTime = System.currentTimeMillis();
    }

    // Get the elapsed time since resetting the timer
    public static long GetElapsedTime() {
	return System.currentTimeMillis() - _lTime;
    }

    public static String MemDisplay() {
	long total = RUNTIME.totalMemory();
	long free  = RUNTIME.freeMemory();
	return total - free + ":" + total;
    }
}
