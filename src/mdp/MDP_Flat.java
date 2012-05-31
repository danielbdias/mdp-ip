package mdp;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import add.Context;
import add.Polynomial;
import add.Table;

import logic.lattice.Lattice;
import prob.mdp.HierarchicalParser;

public class MDP_Flat extends MDP {
	
	
	//public Object [] rewardVec=null;
	
	/** Constructor - filename
	 **/
	public MDP_Flat(String filename,int typeContext,int typeAproxPol,String typeSolution) {
		this(HierarchicalParser.parseFile(filename),typeContext,typeAproxPol,typeSolution);
	}
	/** Constructor - pre-parsed file
	 **/
	public MDP_Flat(ArrayList input,int typeContext,int typeAproxPol,String typeSolution) {
		//System.out.println(input);
		//System.exit(1);

//		_prevDD = _maxDD = _rewardDD = _valueDD = null;
//		_nDDType    = dd_type;
		states =new HashMap();
		alVars     = new ArrayList();
		alOrder    = new ArrayList();
		tmVar2ID   = new TreeMap();
		tmID2Var   = new TreeMap();
//		_tmAct2Regr = new TreeMap();
		hmPrimeRemap  = new HashMap();
		mName2Action = new TreeMap();
		alSaveNodesValue   = new ArrayList();
		bdDiscount  = new BigDecimal(""+(-1));
		bdTolerance = new BigDecimal(""+(-1));
//		_nIter       = -1;
//		_sRegrAction = null;
//		_nMaxRegrSz  = -1;
		this.typeContext=3;                                                                                  //always the type is Table for Flat_MDPs
		buildMDP_Fac(input,typeContext,typeSolution);
		this.context.typeAproxPol=typeAproxPol;
		if(this.context.typeAproxPol==1){
			// Create and add primitive concepts to lattice
			this.context.lattice = new Lattice();	
			for (int i = 1; i <= this.context.listVarProb.size(); i++){
				Lattice.AddPrimitive(this.context.lattice , i);
			}
		}
       buildMDP_Flat();

	}
	private void buildMDP_Flat() {
		//createTransition
		Object transTable=context.getTerminalNode(1);
		Iterator actions=mName2Action.entrySet().iterator();
		while (actions.hasNext()){
			Map.Entry meaction=(Map.Entry) actions.next();
			Action action=(Action) meaction.getValue();
			System.out.println("creating transition matrix for action: "+action.getName());
			transTable=multiplyCPTs(action);
			//Object[][] TransMatrix = convertTableToMatrix(transTable);		//can be numbers or polynomials
			this.action2TransTable.put(action, transTable);
		}
	}
	

	private Object multiplyCPTs(Action action) {
		Object transTable=context.getTerminalNode(1);
		Integer xiprime;
		Iterator x=this.hmPrimeRemap.entrySet().iterator();
		while (x.hasNext()){
			Map.Entry xiprimeme = (Map.Entry)x.next();
			xiprime=(Integer) xiprimeme.getValue();
			Object cpt_a_xiprime=action.tmID2ADD.get(xiprime);
			transTable = context.apply(transTable, cpt_a_xiprime, Context.PROD);
			//context.view(transTable);
		} 
		
		return transTable;
	}
	
	public Object regress(Object VDD, Action action,double mergeError,TreeMap iD2ADD, OptimizationType optimization, boolean simulating, boolean firsTimeSimulating){

		VDD=context.remapIdWithPrime(VDD,this.hmPrimeRemap); 
		//context.view(VDD);
		//first multiply VDD by the Table representation of the probability
		Object cpt_a_xiprime_prod=action2TransTable.get(action);
		//context.view(cpt_a_xiprime_prod);
		VDD = context.apply(VDD, cpt_a_xiprime_prod, Context.PROD);
		
		
		//only sum_out over all xiprime variables
		Integer xiprime;
		Iterator x=this.hmPrimeRemap.entrySet().iterator();
		while (x.hasNext()){
			Map.Entry xiprimeme = (Map.Entry)x.next();
			xiprime=(Integer) xiprimeme.getValue();
			//Object cpt_a_xiprime=action.tmID2ADD.get(xiprime);
			//VDD = context.apply(VDD, cpt_a_xiprime, Context.PROD);
			VDD=context.apply(xiprime, Context.SUMOUT, VDD);
		} 
		//context.view(VDD);
        
		// Reduce memory if neededs
		/*if(typeContext==3){
			flushCaches(VDD);
		}*/
		if(context.workingWithParameterized){
			if(mergeError!=0){
				//TODO: we moved pruneNodesValue -we are using other prune method
				//VDD=context.pruneNodesValue(VDD, mergeError/2d);//because we prune after the maximization over actions
				//System.out.println("Prune ParADDBeforeCallSolver");
				//context.view(VDD);
			}
			context.mergeError = mergeError;
			
			if (optimization == OptimizationType.Maximization)
				VDD = context.doMaxCallOverNodes(VDD,NAME_FILE_CONTRAINTS,this.pruneAfterEachIt);
			else
				VDD = context.doMinCallOverNodes(VDD,NAME_FILE_CONTRAINTS,this.pruneAfterEachIt);
		}
		context.workingWithParameterized=false;
		return VDD;
	}
    
	
}
