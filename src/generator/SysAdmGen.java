package generator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import mdp.Config;

import add.Context;
import add.ContextADD;



public class SysAdmGen {
	/** For printing * */
	public static DecimalFormat _df = new DecimalFormat("#.####");
	public final static String  NAME_FILE_CONTRAINTS="/home/karina/Desktop/constraints.tex";
	
	/** Static configurations * */
	public static HashMap INDEX = new HashMap();
	
	public static HashMap STAR_6 = new HashMap();
	public static HashMap STAR_7 = new HashMap();
	public static HashMap STAR_8 = new HashMap();
	public static HashMap STAR_9 = new HashMap();
	public static HashMap STAR_10 = new HashMap();
	public static HashMap STAR_11 = new HashMap();
	public static HashMap STAR_12 = new HashMap();
	public static HashMap STAR_13 = new HashMap();
	public static HashMap STAR_14 = new HashMap();
	public static HashMap STAR_15 = new HashMap();
	public static HashMap STAR_16 = new HashMap();
	public static final int MAX_RING = 2;
	public static final int MAX_RING_UNI = 25;
	public static HashMap[] UNI_RING = new HashMap[MAX_RING_UNI + 1];
	public static HashMap[] BI_RING = new HashMap[MAX_RING + 1];
	public static HashMap[] INDEP_RING = new HashMap[MAX_RING + 1];
	
	public static HashMap INDEP_RING3_6 = new HashMap();
	public static HashMap INDEP_RING4_8 = new HashMap();
	public static HashMap INDEP_RING5_10 = new HashMap();
	public static HashMap INDEP_RING_EPS_6 = new HashMap();
	public static HashMap INDEP_RING_EPS_8 = new HashMap();
	public static HashMap INDEP_RING_EPS_10 = new HashMap();
	public final static String  PATH_FILES = Config.getConfig().getProblemsDir();
	static {
		int i;
		for (i = 20; i <= MAX_RING_UNI; i++) {
			UNI_RING[i] = new HashMap();
			GenUniRing(UNI_RING[i], i);
			INDEX.put(PATH_FILES+"uni_ring_IP_" + i, UNI_RING[i]);
		}
		
		for (i = 1; i <= MAX_RING; i++) {
			BI_RING[i] = new HashMap();
			GenBiRing(BI_RING[i], i);
			INDEX.put(PATH_FILES+"bi_ring_IP_" + i, BI_RING[i]);
		}
		for (i = 1; i <= MAX_RING; i++) {
			INDEP_RING[i] = new HashMap();
			GenIndepRing(INDEP_RING[i], i);
			INDEX.put(PATH_FILES+"indep_ring_IP_" + i, INDEP_RING[i]);
		}
	}

	public static void GenUniRing(HashMap hmVarConn, int n) {
		for (int i = 1; i < n; i++) {
			AddConn(hmVarConn, i, i + 1);
		}
		AddConn(hmVarConn, n, 1);
	}

	public static void GenBiRing(HashMap hmVarConn, int n) {
		for (int i = 1; i < n; i++) {
			AddConn(hmVarConn, i, i + 1);
		}
		AddConn(hmVarConn, n, 1);
		for (int j = n; j > 1; j--) {
			AddConn(hmVarConn, j, j - 1);
		}
		AddConn(hmVarConn, 1, n);
	}

	public static void GenIndepRing(HashMap hmVarConn, int n) {
		for (int i = 1; i < n; i += 2) {
			AddConn(hmVarConn, i, i + 1);
			AddConn(hmVarConn, i + 1, i);
		}
		if ((n & 1) == 1) {
			// Odd
			AddConn(hmVarConn, n, n);
		}
	}

	/** Network building methods * */
	public static void AddConn(HashMap hmVarConn, int from, int to) {
		Integer FROM = new Integer(from);
		Integer TO = new Integer(to);
		TreeSet ts = (TreeSet) hmVarConn.get(FROM);
		if (ts == null) {
			ts = new TreeSet();
			hmVarConn.put(FROM, ts);
		}
		ts.add(TO);
	}
	
	
	
	
	
	/** Generator * */
	public static void GenNetworkFile(String filename, HashMap config,String typeMDP,String equalProb,String typeRew, String pairBasis) {
        int idProb=0;
		PrintWriter os = null;
		File file = new File(NAME_FILE_CONTRAINTS);
		boolean success = file.delete();
		
		try {
			os = new PrintWriter(new FileOutputStream(filename));
			//// Example variables (c1 c2 c3 c4 )/////////////////////////////////////////// 
			// Get all ids and print them to the file
			TreeSet ids_tmp = new TreeSet(config.keySet());
			TreeSet idsVar = new TreeSet();
						
			// Filter out zero and negative IDs (but don't think will encounter in this code)
			Iterator it = ids_tmp.iterator();
			while (it.hasNext()) {
				int c_id = ((Integer) it.next()).intValue();
				if (c_id > 0) {
					idsVar.add(new Integer(c_id));
				}
			}
			if (idsVar.size()==0){
				System.out.println("problem with config");
				System.exit(0);
			}
			// Add in all values() of config HashMap
			Integer idVar1 = null, idVar2 = null;
			Iterator it2 = ((Set) idsVar.clone()).iterator();
			while (it2.hasNext()) {
				idsVar.addAll((Set) config.get(it2.next()));
			}
			os.print("variables (");
			it2 = idsVar.iterator();
			while (it2.hasNext()) {
				os.print("c" + it2.next() + " ");
			}
			os.println(")");
            //////////////// Generating CPT for each action      ////////////////////////////
			
			// Generate order and ADD
		//	ArrayList order = new ArrayList(idsVar);
			ContextADD context = new ContextADD();
			// Generate generic noreboot
			os.println("action noreboot");
			System.out.println("\nAction: noreboot");
			Iterator it3 = idsVar.iterator();
			 int timeLoop=1;
			while (it3.hasNext()) {
//				TODO: new line for have the same probabilities
				if (equalProb.compareTo("Y")==0){
					idProb=0;
				}
				idVar2 = (Integer) it3.next();
				idProb=writeInFileCPT(idVar2,config,context,os,typeMDP,idProb,true,equalProb,timeLoop);
				timeLoop++;
			}
			os.println("endaction");
             
			// Now, generate all actions
			it2 = idsVar.iterator();
			
			while (it2.hasNext()) {
				idProb=0;
				idVar1 = (Integer) it2.next();
				os.println("action reboot" + idVar1);
				System.out.println("\nAction: reboot" + idVar1);
				it3 = idsVar.iterator();
				while (it3.hasNext()) {
					idVar2 = (Integer) it3.next();
					//TODO: new line for have the same probabilities
					if (equalProb.compareTo("Y")==0){
							idProb=0;
					}
					if (idVar1.equals(idVar2)) {
						// Is being rebooted?
						//TODO: this line was before for pass 2 idProb when the machine is rebooted
						if (equalProb.compareTo("Y")!=0){
							idProb=idProb+2;
						}
						if (typeMDP.compareTo("MDP")==0){
							os.println("\tc" + idVar2 + " (1.00)");
						}
						else{
							os.println("\tc" + idVar2 + " ([1.00])");
						}
					} else {
						// Not being rebooted
						idProb=writeInFileCPT(idVar2,config,context,os,typeMDP,idProb,false,equalProb,timeLoop);
	                   
					}
				}
				os.println("endaction");
			}

			// Now generate reward
			os.print("reward ");
			Integer rew=null;
			if (typeRew.compareTo("S")==0){
				//only reward for the first variable and the variable in the middlein the idsVar
				TreeSet idFirstLast = new TreeSet();
				idFirstLast.add(idsVar.first());
				Object [] list =idsVar.toArray();
				if(idsVar.size()>1){
					idFirstLast.add(list[idsVar.size()/2]);
				}
				rew=GetCountingDD(context, idFirstLast, 0d);
			}
			else if (typeRew.compareTo("A")==0){
			    rew = GetCountingDD(context, idsVar, 0d);
			}
			else if (typeRew.compareTo("C")==0){
			    rew = GetConjDD(context, idsVar);
			}
									
			context.dumpToTree(rew, "c", os, _df, 0);

			//Generate constraint for every computer
			if (typeMDP.compareTo("MDPIP")==0){
				os.println("\nconstraints");
				os.println("   (");
				printConstraintFile(os); 
				os.println("   )");
			}
			// Generate discount and tolerance
			os.println("\n\ndiscount 0.900000");
			os.println("tolerance 0.010000");
			// Generate basis functions
			//TODO: hacer una funcion para funciones base simples
			//hacer una funcion para funciones base pares que llame a la otra funcion 4 vezs
			//y haga context.dumpToTree
			os.println("basisFunctions");
			if(pairBasis.compareTo("N")==0){
				createSimpleBasis(os,idsVar);
			}
			else{
				createPairBasis(os,config,context);
			}
			os.println("\n endbasis");
			//Generate local reward
			os.println("factoredReward");
			it2 = idsVar.iterator();
			while (it2.hasNext()) {
				os.println("(c" + it2.next() + " (1)  (0))");
			}
			os.println("endfactoredReward");
			// Close file
			os.close();

		} catch (IOException ioe) {
			System.out.println(ioe);
			System.exit(1);
		}
	}
	
private static void createPairBasis(PrintWriter os, HashMap config,ContextADD context) {
	// create pairs from idsVar, only for adjacent variables
	//config is variableFrom -> TreeSet Connection 
	Iterator it = config.keySet().iterator();
	while (it.hasNext()) {
		Integer fromVar = (Integer) it.next();
		if (fromVar.intValue() < 0) {
			continue;
		}
		TreeSet t = (TreeSet) config.get(fromVar);
		if (t != null) {
		    Iterator it2=t.iterator();
		    while(it2.hasNext()){
		    	Integer toVar=(Integer)it2.next();
		    	GetBasisFunPairAll(context, fromVar,toVar,os); 
		    }

		}
	}
		
}


private static void createSimpleBasis(PrintWriter os, TreeSet idsVar) {
		// TODO Auto-generated method stub
		Iterator it2 = idsVar.iterator();
		while (it2.hasNext()) {
			//os.println("begin");
			os.println("(c" + it2.next() + " (1)  (0))");
			//os.println("end");
		}
}


private static void printConstraintFile(PrintWriter os) {
	try {
        BufferedReader input= new BufferedReader(new FileReader(NAME_FILE_CONTRAINTS));
        String line = null;
        while (( line = input.readLine()) != null){
            os.println("       "+line);
        }
        input.close();
    } catch (IOException e) {
    	System.out.println("Problem with reading Constraints file");
    	System.exit(0);
    }
		
	}

private static int writeInFileCPT(Integer idVar, HashMap config, ContextADD context,PrintWriter os, String typeMDP,int idProb, boolean norebootAction,String equalProb,int timeLoop) {
	// Not being rebooted 
	TreeSet tsVars = GetIncomingConn(idVar, config);
	// Here need to generate positive side of CPT where dependent upon current computer's
	// status and scaled by conn computer's status.
	Integer idADD = GetCountingDD(context, tsVars, 1d);
	Integer idNorm = context.getTerminalNode(1d/((double) tsVars.size() + 1.0d));
	Integer connADD = (Integer)context.apply(idADD, idNorm, Context.PROD);
	
	Integer conn_t=null,conn_f=null;
	if(typeMDP.compareTo("MDP")==0){
		Integer idTrue = context.getTerminalNode(0.95d);
		Integer idFalse=context.getTerminalNode(0.36d);
		conn_t=(Integer) context.apply(connADD, idTrue, Context.PROD);
		conn_f=(Integer) context.apply(connADD, idFalse, Context.PROD);
	}
	else if(typeMDP.compareTo("MDPIP")==0){
		context.workingWithParameterized=true;
		idProb++;
		Integer idTrue = context.getTerminalNode(Integer.toString(idProb));// Instead of 0.95, multiply by p_id_1
		idProb++;
		Integer idFalse=context.getTerminalNode(Integer.toString(idProb));// Instead of 0.0475, multiply by p_id_2
		conn_t=(Integer) context.apply(connADD, idTrue, Context.PROD);
		conn_f=(Integer) context.apply(connADD, idFalse, Context.PROD);
		//create a file with the constraints
		//Then for constraint: p_id_1 > c + p_id_2 for c \in [0,1]
		//only for the noreboot because the constraints are equal for the other actions
		if(norebootAction && ((equalProb.compareTo("Y")==0 && timeLoop==1 )|| (equalProb.compareTo("Y")!=0))){
			appendConstraintFile(idProb);
		}
	}
	else{
		System.out.println("this typeMDP not exist");
		System.exit(0);
	}
	
	os.print("\tc" + idVar + " (c" + idVar + " ");
	//TODO: Write a dumpToTree for ParamADDs so that you can read it back in as a SPUDD MDPIP
	context.dumpToTree(conn_t, "c", os, _df, 4);
	context.dumpToTree(conn_f, "c", os, _df, 4);
	os.println(") ");		
	context.workingWithParameterized=false;	
	return idProb;
	}




	private static void appendConstraintFile(int idProb) {
	try {
    		
            BufferedWriter out = new BufferedWriter(new FileWriter(NAME_FILE_CONTRAINTS,true));
            out.write("(p" + Integer.toString(idProb-1)+ " > = 0.85 + "+"p"+Integer.toString(idProb)+")");
         	out.write(System.getProperty("line.separator"));
         	out.write("(p" + Integer.toString(idProb-1)+ " < = 0.95 )");
         	out.write(System.getProperty("line.separator"));
         	out.write("(p" + Integer.toString(idProb)+ " < = 0.10 )");
         	out.write(System.getProperty("line.separator"));
            out.close();
        } catch (IOException e) {
        	System.out.println("Problem with the creation Constraint file");
        	System.exit(0);
        }
}

	//	 Return  additive reward
	public static int GetCountingDD(ContextADD context, Set vars, double fval) {
		// System.out.println("GETCD: " + vars + ", " + context._alOrder);
		Integer ret = context.getTerminalNode(fval);
		Iterator it = vars.iterator();
		while (it.hasNext()) {
			int var = ((Integer) it.next()).intValue();
			Integer idHigh = context.getTerminalNode(1d);
			Integer idLow = context.getTerminalNode(0d);
			Integer idNode=context.getInternalNode(var, idHigh, idLow);
			ret = (Integer) context.apply(ret, idNode, Context.SUM);
		}
		return ret;
	}
	//	 Returns a conjunction reward
	public static int GetConjDD(ContextADD context, Set vars) {
		// System.out.println("GETCD: " + vars + ", " + context._alOrder);
		Integer ret = context.getTerminalNode(1d);
		Iterator it = vars.iterator();
		while (it.hasNext()) {
			int var = ((Integer) it.next()).intValue();
			Integer idHigh = context.getTerminalNode(1d);
			Integer idLow = context.getTerminalNode(0d);
			Integer idNode=context.getInternalNode(var, idHigh, idLow);
			ret = (Integer) context.apply(ret, idNode, Context.PROD);
		}
		return ret;
	}
	
	
    public static void 	GetBasisFunPairAll(ContextADD context, int var1,int var2,PrintWriter os) {
    	
    	for (int i=0;i<4;i++){
    		int valueL1=0,valueL2=0;
    		int valueH1=i%2;
    		if (valueH1==0){
    			valueL1=1;
    		}
    		int valueH2=(i/2)%2;
    		if (valueH2==0){
    			valueL2=1;
    		}
    		Integer rew=GetBasisFunPair(context, var1,var2, valueH1, valueL1 ,valueH2, valueL2);
    		//os.println("begin");
    		context.dumpToTree(rew, "c", os, _df, 0);
    		//os.println("end");
    	}
    }
	//	 get basis function pair from the variables
	public static int GetBasisFunPair(ContextADD context, int var1,int var2, double valueH1, double valueL1 ,double valueH2, double valueL2) {
			Integer idHigh1 = context.getTerminalNode(valueH1);
			Integer idLow1 = context.getTerminalNode(valueL1);
			Integer idNode1=context.getInternalNode(var1, idHigh1, idLow1);
			Integer idHigh2 = context.getTerminalNode(valueH2);
			Integer idLow2 = context.getTerminalNode(valueL2);
			Integer idNode2=context.getInternalNode(var2, idHigh2, idLow2);
			Integer ret = (Integer) context.apply(idNode1, idNode2, Context.PROD);
			return ret;
	}

	
	//get the set of incoming connections to idVar
	public static TreeSet GetIncomingConn(Integer idVar, HashMap config) {
		TreeSet ret = new TreeSet();
		Iterator it = config.keySet().iterator();
		while (it.hasNext()) {
			Integer itVar = (Integer) it.next();
			if (itVar.intValue() < 0) {
				continue;
			}
			TreeSet t = (TreeSet) config.get(itVar);
			if (t != null && t.contains(idVar)) {
				ret.add(itVar);
			}
		}
		return ret;
	}
	/**
	 * 
	 * @param args
	 * typeMDP coul be MDP MDPIP
	 * equalProb could be Y:yes N:not
	 * typeRew could be  S: simple reward A: additive reward  C: conjunctive reward
	 * pairBasis Y: pair basis N:simple basis 
	 * For the experiments we use the following parameters: MDPIP N C
	 */
	public static void main(String[] args) {
		
		String typeMDP = args[0];
		String equalProb= args[1];
 
		String typeRew=args[2];
		String pairBasis=args[3];
		Iterator i = INDEX.entrySet().iterator();
		while (i.hasNext()) {
			Map.Entry me = (Map.Entry) i.next();
			String name = ((String) me.getKey()) + ".net";
			HashMap config = (HashMap) me.getValue();
			if (config.size()==0){
				System.out.println("problem with config");
			}
			System.out.println("Generating '" + name + "' from configuration.");
			if(name.compareTo("/home/karina/ADDVer2/ADD/problemsMDPIP/star_IP_6.net")==0)
			{
				System.out.println("prob");
			}
			GenNetworkFile(name, config,typeMDP,equalProb,typeRew,pairBasis);
		}
	}
	
	///STAR INDEP_RING INDEP_RING_EPS ARE CREATED BY HAND/////////////////////////////////
	
	static {
		
		
		INDEX.put(PATH_FILES+"star_IP_6", STAR_6);
		INDEX.put(PATH_FILES+"star_IP_7", STAR_7);
		INDEX.put(PATH_FILES+"star_IP_8", STAR_8);
		INDEX.put(PATH_FILES+"star_IP_9", STAR_9);
		INDEX.put(PATH_FILES+"star_IP_10", STAR_10);
		INDEX.put(PATH_FILES+"star_IP_11", STAR_11);
		INDEX.put(PATH_FILES+"star_IP_12", STAR_12);
		INDEX.put(PATH_FILES+"star_IP_13", STAR_13);
		INDEX.put(PATH_FILES+"star_IP_14", STAR_14);
		INDEX.put(PATH_FILES+"star_IP_15", STAR_15);
		INDEX.put(PATH_FILES+"star_IP_16", STAR_16);

		INDEX.put(PATH_FILES+"indep_ring3_IP_6", INDEP_RING3_6);
		INDEX.put(PATH_FILES+"indep_ring4_IP_8", INDEP_RING4_8);
		INDEX.put(PATH_FILES+"indep_ring5_IP_10", INDEP_RING5_10);

		INDEX.put(PATH_FILES+"indep_ring_eps_IP_6", INDEP_RING_EPS_6);
		INDEX.put(PATH_FILES+"indep_ring_eps_IP_8", INDEP_RING_EPS_8);
		INDEX.put(PATH_FILES+"indep_ring_eps_IP_10", INDEP_RING_EPS_10);
	}
	
	/** STAR 6 * */
	static {
		AddConn(STAR_6, 4, 1);
		AddConn(STAR_6, 5, 2);
		AddConn(STAR_6, 5, 3);
		AddConn(STAR_6, 6, 4);
		AddConn(STAR_6, 6, 5);
	}

	/** STAR 7 * */
	static {
		AddConn(STAR_7, 5, 1);
		AddConn(STAR_7, 5, 2);
		AddConn(STAR_7, 6, 3);
		AddConn(STAR_7, 6, 4);
		AddConn(STAR_7, 7, 5);
		AddConn(STAR_7, 7, 6);
	}

	/** STAR 8 * */
	static {
		AddConn(STAR_8, 5, 1);
		AddConn(STAR_8, 5, 2);
		AddConn(STAR_8, 6, 3);
		AddConn(STAR_8, 7, 4);
		AddConn(STAR_8, 8, 5);
		AddConn(STAR_8, 8, 6);
		AddConn(STAR_8, 8, 7);
	}

	/** STAR 9 * */
	static {
		AddConn(STAR_9, 6, 1);
		AddConn(STAR_9, 6, 2);
		AddConn(STAR_9, 7, 3);
		AddConn(STAR_9, 7, 4);
		AddConn(STAR_9, 8, 5);
		AddConn(STAR_9, 9, 6);
		AddConn(STAR_9, 9, 7);
		AddConn(STAR_9, 9, 8);
	}

	/** STAR 10 * */
	static {
		AddConn(STAR_10, 7, 1);
		AddConn(STAR_10, 7, 2);
		AddConn(STAR_10, 8, 3);
		AddConn(STAR_10, 8, 4);
		AddConn(STAR_10, 9, 5);
		AddConn(STAR_10, 9, 6);
		AddConn(STAR_10, 10, 7);
		AddConn(STAR_10, 10, 8);
		AddConn(STAR_10, 10, 9);
	}

	/** STAR 11 * */
	static {
		AddConn(STAR_11, 8, 1);
		AddConn(STAR_11, 8, 2);
		AddConn(STAR_11, 9, 3);
		AddConn(STAR_11, 9, 4);
		AddConn(STAR_11, 10, 5);
		AddConn(STAR_11, 10, 6);
		AddConn(STAR_11, 10, 7);
		AddConn(STAR_11, 11, 8);
		AddConn(STAR_11, 11, 9);
		AddConn(STAR_11, 11, 10);
	}

	/** STAR 12 * */
	static {
		AddConn(STAR_12, 9, 1);
		AddConn(STAR_12, 9, 2);
		AddConn(STAR_12, 10, 3);
		AddConn(STAR_12, 10, 4);
		AddConn(STAR_12, 10, 5);
		AddConn(STAR_12, 11, 6);
		AddConn(STAR_12, 11, 7);
		AddConn(STAR_12, 11, 8);
		AddConn(STAR_12, 12, 9);
		AddConn(STAR_12, 12, 10);
		AddConn(STAR_12, 12, 11);
	}

	/** STAR 13 * */
	static {
		AddConn(STAR_13, 10, 1);
		AddConn(STAR_13, 10, 2);
		AddConn(STAR_13, 10, 3);
		AddConn(STAR_13, 11, 4);
		AddConn(STAR_13, 11, 5);
		AddConn(STAR_13, 11, 6);
		AddConn(STAR_13, 12, 7);
		AddConn(STAR_13, 12, 8);
		AddConn(STAR_13, 12, 9);
		AddConn(STAR_13, 13, 10);
		AddConn(STAR_13, 13, 11);
		AddConn(STAR_13, 13, 12);
	}

	/** STAR 14 * */
	static {
		AddConn(STAR_14, 10, 1);
		AddConn(STAR_14, 10, 2);
		AddConn(STAR_14, 10, 3);
		AddConn(STAR_14, 11, 4);
		AddConn(STAR_14, 11, 5);
		AddConn(STAR_14, 11, 6);
		AddConn(STAR_14, 12, 7);
		AddConn(STAR_14, 12, 8);
		AddConn(STAR_14, 13, 9);
		AddConn(STAR_14, 14, 10);
		AddConn(STAR_14, 14, 11);
		AddConn(STAR_14, 14, 12);
		AddConn(STAR_14, 14, 13);
	}

	/** STAR 15 * */
	static {
		AddConn(STAR_15, 11, 1);
		AddConn(STAR_15, 11, 2);
		AddConn(STAR_15, 11, 3);
		AddConn(STAR_15, 12, 4);
		AddConn(STAR_15, 12, 5);
		AddConn(STAR_15, 12, 6);
		AddConn(STAR_15, 13, 7);
		AddConn(STAR_15, 13, 8);
		AddConn(STAR_15, 14, 9);
		AddConn(STAR_15, 14, 10);
		AddConn(STAR_15, 15, 11);
		AddConn(STAR_15, 15, 12);
		AddConn(STAR_15, 15, 13);
		AddConn(STAR_15, 15, 14);
	}

	/** STAR 16 * */
	static {
		AddConn(STAR_16, 12, 1);
		AddConn(STAR_16, 12, 2);
		AddConn(STAR_16, 12, 3);
		AddConn(STAR_16, 13, 4);
		AddConn(STAR_16, 13, 5);
		AddConn(STAR_16, 13, 6);
		AddConn(STAR_16, 14, 7);
		AddConn(STAR_16, 14, 8);
		AddConn(STAR_16, 14, 9);
		AddConn(STAR_16, 15, 10);
		AddConn(STAR_16, 15, 11);
		AddConn(STAR_16, 16, 12);
		AddConn(STAR_16, 16, 13);
		AddConn(STAR_16, 16, 14);
		AddConn(STAR_16, 16, 15);
	}

	/** INDEPENDENT RING 2x3 (6 total) * */
	static {
		AddConn(INDEP_RING3_6, 1, 2);
		AddConn(INDEP_RING3_6, 2, 3);
		AddConn(INDEP_RING3_6, 3, 1);

		AddConn(INDEP_RING3_6, 4, 5);
		AddConn(INDEP_RING3_6, 5, 6);
		AddConn(INDEP_RING3_6, 6, 4);
	}

	/** INDEPENDENT RING 2x4 (8 total) * */
	static {
		AddConn(INDEP_RING4_8, 1, 2);
		AddConn(INDEP_RING4_8, 2, 3);
		AddConn(INDEP_RING4_8, 3, 4);
		AddConn(INDEP_RING4_8, 4, 1);

		AddConn(INDEP_RING4_8, 5, 6);
		AddConn(INDEP_RING4_8, 6, 7);
		AddConn(INDEP_RING4_8, 7, 8);
		AddConn(INDEP_RING4_8, 8, 5);
	}

	/** INDEPENDENT RING 2x5 (10 total) * */
	static {
		AddConn(INDEP_RING5_10, 1, 2);
		AddConn(INDEP_RING5_10, 2, 3);
		AddConn(INDEP_RING5_10, 3, 4);
		AddConn(INDEP_RING5_10, 4, 5);
		AddConn(INDEP_RING5_10, 5, 1);

		AddConn(INDEP_RING5_10, 6, 7);
		AddConn(INDEP_RING5_10, 7, 8);
		AddConn(INDEP_RING5_10, 8, 9);
		AddConn(INDEP_RING5_10, 9, 10);
		AddConn(INDEP_RING5_10, 10, 6);
	}

	/** INDEPENDENT RING 6 EPS * */
	static {
		AddConn(INDEP_RING_EPS_6, 1, 2);
		AddConn(INDEP_RING_EPS_6, 2, 1);
		AddConn(INDEP_RING_EPS_6, -3, -2); // eps conn

		AddConn(INDEP_RING_EPS_6, 3, 4);
		AddConn(INDEP_RING_EPS_6, 4, 3);
		AddConn(INDEP_RING_EPS_6, -5, -4); // eps conn

		AddConn(INDEP_RING_EPS_6, 5, 6);
		AddConn(INDEP_RING_EPS_6, 6, 5);
		AddConn(INDEP_RING_EPS_6, -1, -6); // eps conn
	}

	/** INDEPENDENT RING 8 EPS * */
	static {
		AddConn(INDEP_RING_EPS_8, 1, 2);
		AddConn(INDEP_RING_EPS_8, 2, 1);
		AddConn(INDEP_RING_EPS_8, -3, -2); // eps conn

		AddConn(INDEP_RING_EPS_8, 3, 4);
		AddConn(INDEP_RING_EPS_8, 4, 3);
		AddConn(INDEP_RING_EPS_8, -5, -4); // eps conn

		AddConn(INDEP_RING_EPS_8, 5, 6);
		AddConn(INDEP_RING_EPS_8, 6, 5);
		AddConn(INDEP_RING_EPS_8, -7, -6); // eps conn

		AddConn(INDEP_RING_EPS_8, 7, 8);
		AddConn(INDEP_RING_EPS_8, 8, 7);
		AddConn(INDEP_RING_EPS_8, -1, -8); // eps conn
	}

	/** INDEPENDENT RING 10 EPS * */
	static {
		AddConn(INDEP_RING_EPS_10, 1, 2);
		AddConn(INDEP_RING_EPS_10, 2, 1);
		AddConn(INDEP_RING_EPS_10, -3, -2); // eps conn

		AddConn(INDEP_RING_EPS_10, 3, 4);
		AddConn(INDEP_RING_EPS_10, 4, 3);
		AddConn(INDEP_RING_EPS_10, -5, -4); // eps conn

		AddConn(INDEP_RING_EPS_10, 5, 6);
		AddConn(INDEP_RING_EPS_10, 6, 5);
		AddConn(INDEP_RING_EPS_10, -7, -6); // eps conn

		AddConn(INDEP_RING_EPS_10, 7, 8);
		AddConn(INDEP_RING_EPS_10, 8, 7);
		AddConn(INDEP_RING_EPS_10, -9, -8); // eps conn

		AddConn(INDEP_RING_EPS_10, 9, 10);
		AddConn(INDEP_RING_EPS_10, 10, 9);
		AddConn(INDEP_RING_EPS_10, -1, -10); // eps conn
	}
}
