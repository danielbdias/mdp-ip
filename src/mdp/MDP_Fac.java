package mdp;

import graph.Graph;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import logic.lattice.Lattice;

import add.AditArc;
import add.ContextADD;
import add.ContextAditADD;
import add.Context;
import add.ContextTable;
import add.InternalNodeKeyADD;
import add.Table;
import add.TerminalNodeKeyPar;

import prob.mdp.HierarchicalParser;

public class MDP_Fac  extends MDP{
	

    
	/** Constructor - filename
	 **/
	public MDP_Fac(String filename,int typeContext,int typeAproxPol, String typeSolution) {
		this(filename, typeContext, typeAproxPol, typeSolution, false);
	}
	
	/** Constructor - filename
	 **/
	public MDP_Fac(String filename,int typeContext,int typeAproxPol, String typeSolution, boolean simulation) {
		this(HierarchicalParser.parseFile(filename), typeContext, typeAproxPol, typeSolution, simulation);
		
		String[] partsFileName = filename.split("/");
		
		String netName=partsFileName[partsFileName.length-1];
		
		NAME_FILE_VALUE = NAME_FILE_VALPART + netName.substring(0, netName.length() - 4);
		
		this.simulationMode = simulation;
	}
	
	/** Constructor - pre-parsed file
	 **/
	public MDP_Fac(ArrayList input,int typeContext,int typeAproxPol,String typeSolution, boolean simulationMode) {
		//System.out.println(input);
		//System.exit(1);

//		_prevDD = _maxDD = _rewardDD = _valueDD = null;
//		_nDDType    = dd_type;
		this.simulationMode = simulationMode;
		
		alVars     = new ArrayList();
		alOrder    = new ArrayList();
		tmVar2ID   = new TreeMap();
		tmID2Var   = new TreeMap();
//		_tmAct2Regr = new TreeMap();
		hmPrimeRemap  = new HashMap();
		this.hmPrime2IdRemap=new HashMap();
		mName2Action = new TreeMap();
		alSaveNodesValue   = new ArrayList();
		bdDiscount  = new BigDecimal(""+(-1));
		bdTolerance = new BigDecimal(""+(-1));
//		_nIter       = -1;
//		_sRegrAction = null;
//		_nMaxRegrSz  = -1;
		this.typeContext=typeContext;
		buildMDP_Fac(input,typeContext,typeSolution);
		this.context.typeAproxPol=typeAproxPol;
		if(this.context.typeAproxPol==1){//(1 means use lattice
			// Create and add primitive concepts to lattice
			this.context.lattice = new Lattice();	
			for (int i = 1; i <= this.context.listVarProb.size(); i++){
				Lattice.AddPrimitive(this.context.lattice , i);
			}
		}


	}

	public Object regress(Object VDD, Action action, double mergeError, TreeMap iD2ADD, boolean simulating, boolean firsTimeSimulating){
		ArrayList primeIdsProd = new ArrayList(); //list of primes ids that was multiplied

		VDD = context.remapIdWithPrime(VDD, this.hmPrimeRemap);

		Integer xiprime;
		Iterator x = this.hmPrimeRemap.entrySet().iterator();
		
		while (x.hasNext()) {

			Map.Entry xiprimeme = (Map.Entry) x.next();
			xiprime=(Integer) xiprimeme.getValue();
			
			Object cpt_a_xiprime=iD2ADD.get(xiprime);
			
			if (!primeIdsProd.contains(xiprime)){
				VDD = context.apply(VDD, cpt_a_xiprime, Context.PROD);
				primeIdsProd.add(xiprime);
			}

			//			 TODO: For asyncronic arcs Make a set of all CPTs to multiply (before loop)
			//       Then when summing out xi', remove any CPTs from set
			//       that involve xi' and multiply them in.
			ArrayList dependPrimeList=getIdVarPrimeDependOn(xiprime,action.varId2DependPrimeList);
            for(int i=0; i<dependPrimeList.size();i++){
            	Integer xiprimeSyn=(Integer)dependPrimeList.get(i);
            	Object cpt_a_xiprimeSyn=iD2ADD.get(xiprimeSyn);
            	if(!primeIdsProd.contains(xiprimeSyn)){//this cpt was not multiplied before
            		VDD = context.apply(VDD, cpt_a_xiprimeSyn, Context.PROD);
            		primeIdsProd.add(xiprimeSyn);
            	}
            }
            
			VDD = context.apply(xiprime, Context.SUMOUT, VDD);
		} 
	
		// Reduce memory if neededs
		if (!simulating) 
			flushCaches(VDD);

		if(context.workingWithParameterized){
			
			context.mergeError = mergeError;

			VDD = context.doMinCallOverNodes(VDD, NAME_FILE_CONTRAINTS, this.pruneAfterEachIt); // the parameter is ParADD and the result is an ADD
		}
		
		context.workingWithParameterized = false;
		
		return VDD;
	}
	
	private ArrayList getIdVarPrimeDependOn(Integer xiprime, HashMap varId2DependPrimeList) {
		ArrayList dependPrimeList=new ArrayList();
		Iterator it=varId2DependPrimeList.keySet().iterator();
		while (it.hasNext()){
			  Integer prime=(Integer)it.next();
              ArrayList list=(ArrayList)varId2DependPrimeList.get(prime);
              if (list.contains(xiprime)){
            	  dependPrimeList.add(prime);
              }
		}
		return dependPrimeList;
	}


     
  
	
 
     

    	    
}
