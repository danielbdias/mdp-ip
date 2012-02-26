//////////////////////////////////////////////////////////////////////
//
// File:     MDP.java
// Author:   Scott Sanner, University of Toronto (ssanner@cs.toronto.edu)
// Date:     9/1/2003
// Requires: comshell package
//
// Description:
//
//   Generates network problems.
//
// TODO:
//
//   Should have a generator for both tree readers and ADD/AADD readers.
//
//////////////////////////////////////////////////////////////////////

// Package definition
package generator;

// Packages to import
import java.io.*;
import java.math.*;
import java.text.*;
import java.util.*;

import mdp.Config;
//import logic.add.*;

import add.Context;
import add.ContextADD;

/**
 * Generates network problems from a HashMap of ID's and connections
 * 
 * @version 1.0
 * @author Scott Sanner
 * @language Java (JDK 1.3)
 **/
public class TrafficGen {
	private String ZERO="0.0";
	private String ONE="1.0";
	public final static String  PATH_FILES = Config.getConfig().getProblemsDir();
	public static boolean PERTURB_REWARD = false;

	/** Constants **/
	public static  String PROB_TURN   = "0.7";
	public static  String PROB_NOT_ARRIVE = "0.75";
	
	/** For printing **/
	public static DecimalFormat _df = new DecimalFormat("#.###");

	/** Generator **/
	public  void GenTrafficFile(int size,String typeMDP) {
		if(typeMDP.compareTo("MDPIP")==0){
			ZERO="[0.0]";
			ONE="[1.0]";
			
		}

		if (size < 3) {
			System.out.println("TrafficGen: Size must be at least 3.");
			System.exit(1);
		}
		String filename =PATH_FILES+"traffic" + size + ".spudd";
		PrintWriter os = null;
		try {
			// Open the output file
			os = new PrintWriter(new FileOutputStream(filename));

			// Get all ids and print them to the file
			ArrayList<String> var_order = new ArrayList<String>();
			var_order.add("c1");
			var_order.add("c2");
			TreeSet<String> road_cell_ids = new TreeSet<String>();
			for (int i = size << 1; i >= 1 ; i--) {
				road_cell_ids.add("r" + i);
				var_order.add("r" + i);
			}
			var_order.add("t1");
			var_order.add("t2");
			TreeSet<String> ids = new TreeSet<String>(var_order);

			os.print("variables ( ");
			for (String var : var_order) {
				os.print(var + " ");
			}
			os.println(")");

			// Generate order and ADD
			HashMap<String,Integer> var2id = new HashMap<String,Integer>();
			HashMap<Integer,String> id2var = new HashMap<Integer,String>();
			ArrayList order = new ArrayList();
			int id = 1;
			for (String var : ids) {
				var2id.put(var, id);
				id2var.put(id, var);
				order.add(id);
				++id;
			}
			ContextADD context = new ContextADD();

			// Generate generic noreboot
			boolean[] actions = new boolean[] { false, true };
			
			// First generate
			for (boolean b : actions) {
				
				// Generate light change: c1, c2
				if (b) { // stay
					os.println("action stay");
					System.out.println("\nAction: stay");
					os.println("c1 (c1 ("+ONE+") ("+ZERO+"))");					
					os.println("c2 (c2 ("+ONE+") ("+ZERO+"))");
					
				} else { // change
					os.println("action change");
					// 00
					// 01
					// 11
					// 10
					System.out.println("\nAction: change");
					os.println("c1 (c2 ("+ONE+") ("+ZERO+"))");
					os.println("c2 (c1 ("+ZERO+") ("+ONE+"))");
				}
				
				// Generate lane turn update: t1, t2
				// Note: r1 = T means "unoccupied"
				String PROB_TURN1="";
				String PROB_TURN2="";
				if(typeMDP.compareTo("MDP")==0){
					PROB_TURN1=PROB_TURN;
					PROB_TURN2=PROB_TURN;
				}
				else{
					PROB_TURN1="[1*p1]";
					PROB_TURN2="[1*p2]";
				}
				os.println("t1 (r1 (" + PROB_TURN1 + ")");					
				os.println("       (t1 ("+ONE+") ("+ZERO+")))");					
				os.println("t2 (r2 (" + PROB_TURN2 + ")");					
				os.println("       (t2 ("+ONE+") ("+ZERO+")))");					
				
				// Generate intersection road cell: r1, r2
				// TODO: incorporate random braking
				os.println("r1 (c1 (r1 (r3 ("+ONE+") ("+ZERO+"))");					
				os.println("           (t1 (r2 ("+ONE+") (c2 (t2 ("+ONE+") ("+ZERO+")) ");
				os.println("                             ("+ONE+"))");					
				os.println("                   ("+ONE+"))");
				os.println("               ("+ONE+")))");
				os.println("       (r1 (r3 ("+ONE+") ("+ZERO+")) ("+ZERO+")))");
				
				os.println("r2 (c2 (r2 (r4 ("+ONE+") ("+ZERO+"))");					
				os.println("           (t2 (r1 ("+ONE+") (c1 (t1 ("+ONE+") ("+ZERO+")) ");
				os.println("                             ("+ONE+"))");					
				os.println("                   ("+ONE+"))");
				os.println("               ("+ONE+")))");
				os.println("       (r2 (r4 ("+ONE+") ("+ZERO+")) ("+ZERO+")))");
				
				// Generate intermediate road cell: r2, r3, ... , r(size-2), r(size-1)
				String interm_road_cell1 =
					"r3 (r3 (r5 ("+ONE+") ("+ZERO+"))\n" +
					"       (r1 ("+ONE+") ("+ZERO+")))";
				String interm_road_cell2 = interm_road_cell1.
					replace("r1 ", "r2 ").replace("r3 ", "r4 ").replace("r5 ", "r6 ");
				os.println(interm_road_cell1);
				os.println(interm_road_cell2);
				for (int i = 5; i <= (size << 1) - 2; i+=2) {
					os.println(interm_road_cell1.
							replace("r5 ", "r"+(i+2)+" ").replace("r3 ", "r"+i+" ").replace("r1 ", "r"+(i-2))+" ");
					os.println(interm_road_cell2.
							replace("r6 ", "r"+(i+3)+" ").replace("r4 ", "r"+(i+1)+" ").replace("r2 ", "r"+(i-1))+" ");
				}
				
				// Generate feeder road cell: r(size), r(size+1)
				String PROB_NOT_ARRIVE1="";
				String PROB_NOT_ARRIVE2="";
				if(typeMDP.compareTo("MDP")==0){
					PROB_NOT_ARRIVE1=PROB_NOT_ARRIVE;
					PROB_NOT_ARRIVE2=PROB_NOT_ARRIVE;
				}
				else{
					PROB_NOT_ARRIVE1="[1*p3]";
					PROB_NOT_ARRIVE2="[1*p4]";
				}
							
				int o = (size << 1) - 1;
				int e = size << 1;
				os.println("r"+o+" (r"+(o-2)+" (" + PROB_NOT_ARRIVE1 + ")");					
				os.println("       (r"+o+" (" + PROB_NOT_ARRIVE1 + ") ("+ZERO+")))");			
				os.println("r"+e+" (r"+(e-2)+" (" + PROB_NOT_ARRIVE2 + ")");					
				os.println("       (r"+e+" (" + PROB_NOT_ARRIVE2 + ") ("+ZERO+")))");			
				
				os.println("endaction");
			}
			
			// Generate reward
			// TODO: Penalize inter-green in reward
			os.print("reward ");
			int rew = GetCountingDD(context, road_cell_ids, 0d, var2id);

			context.dumpToTree(rew, id2var, os, _df, 0);
			//Generate constraint for every computer
			if (typeMDP.compareTo("MDPIP")==0){
				os.println("\nconstraints");
				os.println("   (");
				/*os.println("          ( p1 - p2 < = 0.2 )");
				os.println("          ( p2 - p1 < = 0.2 )");
				os.println("          ( p4 - p3 < = 0.2 )");
				os.println("          ( p3 - p4 < = 0.2 )");
				os.println("          ( 1 - p3 > = 0.1 )");
				os.println("          ( 1 - p4 > = 0.1 )");*/		
				
				os.println("          ( p1 - p2 < = 0.1 )");
				os.println("          ( p2 - p1 < = 0.1 )");
				os.println("          ( p4 - p3 < = 0.1 )");
				os.println("          ( p3 - p4 < = 0.1 )");
				os.println("          ( 1 - p3 > = 0.4 )");
				os.println("          ( 1 - p4 > = 0.4 )");
				os.println("          ( 1 - p3 < = 0.6 )");
				os.println("          ( 1 - p4 < = 0.6 )");
				os.println("   )");
			}
			// Generate discount and tolerance
			os.println("\n\ndiscount 0.900000");
			os.println("tolerance 0.010000");

			// Close file
			os.close();

		} catch (IOException ioe) {
			System.out.println(ioe);
			System.exit(1);
		}
	}
	

	// Returns a counting ADD from gid 1..max_gid
	public static int GetCountingDD(ContextADD context, Set<String> vars, double off,	HashMap<String,Integer> var2id) {
		// System.out.println("GETCD: " + vars + ", " + context._alOrder);
		int ret = context.getTerminalNode(off);
		for (String var : vars) {
			int var_id = var2id.get(var);
			Integer idHigh = context.getTerminalNode(1d);
			Integer idLow = context.getTerminalNode(0d);
			Integer idNode=context.getInternalNode(var_id, idHigh, idLow);			
			
			ret =  (Integer)  context.apply(ret, idNode, Context.SUM);
		}
		return ret;
	}

	/** Main **/
	public static void main(String[] args) {
		TrafficGen tg=new TrafficGen();
		if (args.length != 1) {
			System.out.println("java prob.mdp.TrafficGen");
			System.exit(1);
		} 
		String typeMDP = args[0];
		 tg.GenTrafficFile(3,typeMDP);
		 tg.GenTrafficFile(4,typeMDP);
		 tg.GenTrafficFile(5,typeMDP);
		 tg.GenTrafficFile(6,typeMDP);
		 tg.GenTrafficFile(7,typeMDP);
		 tg.GenTrafficFile(8,typeMDP);
		 tg.GenTrafficFile(9,typeMDP);
		 tg.GenTrafficFile(10,typeMDP);
	}

}
