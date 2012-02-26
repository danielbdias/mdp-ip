package mdp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import add.ContextADD;
import add.Polynomial;



public class Action {
    private String actionName;
    private MDP mdp;               // MDP of which this action is a part
    public HashSet  hsTransADDs;  //HashSet of the CPTs represented each one as ADD
    public TreeMap   tmID2ADD; // Maps ID (xiprime) to a CPT represented as a ADD
    public TreeMap tmID2ADDNewSample;// Maps ID to a CPT represented as a ADD result from Sample an MDP from an MDPIP
    public HashMap varId2DependPrimeList;//create map for the sincronic arcs
    //for MP///////////////////////
    public ArrayList ciList;//the list with all DDs representing c_i_a, where i is the number of basis functions, this is only used with MP
    public Hashtable<String, HashSet> var2ListVar=new Hashtable<String, HashSet>();
    
    //For Enumerative RTDP///////////////////////////////////////
    //HashMap<State,TreeMap<State,Double>> state2SuccProb=new HashMap<State,TreeMap<State,Double>>();  
    
    /**
     * Build action  from  CPTs description to ADDs
     * @param mdp MDP of which this action is a part
     * @param actionName
     * @param cpt_desc hashMap with the CPT for each variable.
     */
	public Action(MDP mdp,String actionName, HashMap cpt_desc) {
		// TODO Auto-generated constructor stub
		this.actionName=actionName;
		this.mdp=mdp;
		this.hsTransADDs=new HashSet();
		this.tmID2ADD=new TreeMap();
		varId2DependPrimeList=new HashMap();
		buildAction(mdp,cpt_desc);
		
	}
	
	public String getName() {
		return actionName;
	}
	
	/** Build action from description to DDs
     **/
    public void buildAction(MDP mdp,HashMap cpt_desc) {

	// Head will be for current next-state
    	
	Iterator entries = cpt_desc.entrySet().iterator();  //get all values (CPT description) stored in HashMap
	while (entries.hasNext()) {

	    // Get head variable 
	    Map.Entry me = (Map.Entry)entries.next();  //me is a CPT description for one variable  
	    Integer varID = (Integer)mdp.tmVar2ID.get(((String)me.getKey()));  //obtain the id related to the variable that has the CPT description
	    
	    // Now build up high and low-side prob decision diagram
	    Object cpt_true = mdp.context.buildDDFromUnorderedTree((ArrayList)me.getValue(),this.mdp.tmVar2ID,varID,varId2DependPrimeList);
	    //mdp.context.view(cpt_true);
	    Object cpt_false;
	    if(!mdp.context.workingWithParameterized){
	    	cpt_false = mdp.context.apply(mdp.context.getTerminalNode(1),cpt_true, ContextADD.SUB);
	    }
	    else{
	    	Polynomial polynomial=new Polynomial(1.0,new Hashtable(),mdp.context);
			cpt_false = mdp.context.apply(mdp.context.getTerminalNode(polynomial),cpt_true, ContextADD.SUB);
	    }
	    //mdp.context.view(cpt_false);
	    Object final_dd=mdp.context.computeFinalADDForVarIDFromHighLow(varID, cpt_true, cpt_false);
	    //mdp.context.view(final_dd);
	    // Set this final DD
	    this.hsTransADDs.add(final_dd);
		this.tmID2ADD.put(varID, final_dd);
	    
	}
    }
    public void setciList(ArrayList ciList){
    	this.ciList=ciList;
    }
	

}
