package mdp;

import graph.Graph;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;

import add.Context;
import add.ContextADD;
import add.ContextAditADD;
import add.ContextTable;
import add.InternalNodeKeyADD;
import add.Polynomial;
import add.Table;
import add.TerminalNodeKeyADD;
import add.TerminalNodeKeyPar;

public abstract class MDP {
	public boolean pruneAfterEachIt;//=true;
	boolean forceNumberIt=true;
	boolean printFinalADD=false;
	boolean dumpValue=true;
	boolean printTrafficFormat=false;
	boolean sampleInitialFromI=true;
	boolean printPolicy=false;
	int typeSampledRTDPMDPIP = 3; //typeSampledRTDPMDPIP   1: allProbabilities 2: if p=0  => p=epsilon 3: using  the result of a problem with constraints p>= epsilon
	double epsilon=0.000001;
	
	/* Local constants */
//  public final static int VERBOSE_LEVEL = 0; // Determines how much output is displayed
//  public final static boolean ALWAYS_FLUSH = false;         // Always flush DD caches?
 public final static double FLUSH_PERCENT_MINIMUM = 0.1d; // used to flush
 public final static String  NAME_FILE_CONTRAINTS = Config.getConfig().getAmplConstraintFile();
 public final static String  NAME_FILE_CONTRAINTS_GREATERZERO = Config.getConfig().getAmplConstraintFileGreaterZero();
 public final static String  NAME_FILE_VALPART = Config.getConfig().getReportsDir() + "value";
 public final static String  NAME_FILE_MPPART = Config.getConfig().getReportsDir() + "AMPL";
 public String NAME_FILE_VALUE;

  /* Static variables */
//  public static long _lTime;       // For timing purposes
  public static Runtime RUNTIME = Runtime.getRuntime();
  public static int NUMBEROFSEEDS =30; //for simulate MDP 
  /* Local vars */
  public int numVars;                 // number of variables   
  public ArrayList  alVars;        // List of variable names (including primes) index is ID
  public TreeMap    tmID2Var;      // Maps Integer -> names (including primes a',b',etc...)
  public TreeMap    tmVar2ID;      // Maps names -> Integers (including primes a',b',etc...)
  public HashMap    hmPrimeRemap;  // Maps non-prime IDs to their primed counterparts
  public HashMap    hmPrime2IdRemap;  // Maps prime IDs to their non-prime counterparts
  public ArrayList  alOrder;       // The variable order used in decision diagrams  (including primes a',b',etc...)
  public TreeMap        mName2Action; // List of actions //(see Action.java)
  public Context  context;
  public int typeContext;
  public Object     rewardDD;      // The reward for this MDP
  public Object     valueiPlus1DD;       // The resulting value function once this MDP has been solved
  public Object     valueiDD;       // The resulting value function once this MDP has been solved
//   public Object     _maxDD;
//   public Object     _prevDD;
  public BigDecimal bdDiscount;    // Discount (gamma) for MDP
  public BigDecimal bdTolerance;   // Tolerance (gamma) for MDP
//   public int        _nDDType;       // Type of DD to use
//   public TreeMap    _tmAct2Regr;    // Cached DDs from last regression step
//   public int        _nIter;      
//   public int        _nMaxRegrSz;
//   public String     _sRegrAction;
  public ArrayList  alSaveNodesValue;   // Nodes to save related to valuei during cache flushing

  //for paramereterized ADD
  private int numConstraints=0;
  public Hashtable<String, Double> probNature=new Hashtable<String, Double>();     //  idProb--> valProb
  
 //for MDP_Flat
  public HashMap action2TransTable=new HashMap();
  //for RTDP BRTDP////////////////////////
  public ArrayList<TreeMap> listInitialStates;
  public ArrayList<TreeMap> listGoalStates;
  public Object   VUpper;
  public Object   VLower;
  public Object   VGap;
  public int contbreak; 
  public int contUpperUpdates;
  
  private double maxUpper,minLower;
  private double maxUpperUpdated,maxLowerUpdated;
  public static long timeTrials;  
  double gapInitial;
  
  //for RTDPEnum and BRTDPEnum ////////////// 
  public double firstGap;
  public double B;
  public int posActionGreedy;
  protected HashMap<Integer,State> states=new HashMap<Integer, State>();
  public ArrayList<State> listInitialStatesEnum;
  public ArrayList<State> listGoalStatesEnum;
  
  //for MP///////////////////////
  public ArrayList listBasisFunctions;
  public ArrayList listFactoredReward;
  public HashSet nameNewVariables;
  public int contConstraintsMP;
  public final static String  NAME_FILE_CONTRAINTS_MP="/home/karina/ADDVer2/ADD/reportsMDPIP/constraintMP.ampl";
  public int numOriginalConstraints;
  public Object valueWHDD;
   
  ///////////////////////////////
  
  public void buildMDP_Fac(ArrayList input,int typeContext,String typeSolution) {

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
	  //prime variables first
	  o = i.next();
	  int id_count = 1;
	  alVars = (ArrayList)((ArrayList)o).clone();
	  Iterator vars = alVars.iterator();
	  while (vars.hasNext()) {
		  String vname = ((String)vars.next()) + "'";
		  tmID2Var.put(new Integer(id_count), vname);
		  tmVar2ID.put(vname, new Integer(id_count));
		  alOrder.add(new Integer(id_count));
		  ++id_count;
	  }
	  //non-prime variables second
	  numVars = alOrder.size();
	  vars = alVars.iterator();
	  while (vars.hasNext()) {
		  String vname = ((String)vars.next());
		  tmID2Var.put(new Integer(id_count), vname);
		  tmVar2ID.put(vname, new Integer(id_count));
		  alOrder.add(new Integer(id_count));
		  hmPrimeRemap.put(new Integer(id_count), new Integer(id_count - numVars));
		  hmPrime2IdRemap.put(new Integer(id_count - numVars),new Integer(id_count));
		  ++id_count;	    
	  }
	  if (this.typeContext==1)
		  context = new ContextADD();
	  else if (this.typeContext == 2){
		  context = new ContextAditADD();
	  }
	  else{
		  context=new ContextTable();
	  }

	  // Set up actions
	  while(true) {
		  o = i.next();
		  if ( !(o instanceof String) || !((String)o).equalsIgnoreCase("action")) {
			  break;
		  }

		  // o == "action"
		  String actionName = (String)i.next();  //aname= action name
		  HashMap cpt_map = new HashMap();

		  o = i.next();
		  while ( !((String)o).equalsIgnoreCase("endaction") ) {
			  cpt_map.put((String)o + "'", (ArrayList)i.next());
			  o = i.next();
		  }

		  mName2Action.put(actionName, new Action(this,actionName, cpt_map)); 
	  }

	  // Set up reward
	  context.workingWithParameterizedBef=context.workingWithParameterized;
	  context.workingWithParameterized=false; //in order to create and ADD for Reward 
	  if (!(o instanceof String) || !((String)o).equalsIgnoreCase("reward")) {
		  System.out.println("Missing reward declaration: " + o);
		  System.exit(1);
	  }
	  ArrayList reward = (ArrayList)i.next();

	  rewardDD = context.buildDDFromUnorderedTree(reward, tmVar2ID,null,null);
	  //Read constraints only for Parameterized 
	  context.workingWithParameterized=context.workingWithParameterizedBef;
	  if(context.workingWithParameterized){
		  o = i.next();
		  if ( !(o instanceof String) || !((String)o).equalsIgnoreCase("constraints")) {
			  System.out.println("Missing constraints declaration: " + o);
			  System.exit(1);
		  }
		  ArrayList constraints = (ArrayList)i.next();
		  createFileConstraints(constraints, NAME_FILE_CONTRAINTS);
		  numOriginalConstraints=constraints.size();
		  createFileConstraintsGreaterZero(constraints, NAME_FILE_CONTRAINTS_GREATERZERO);
		  		  
	  }
	  // Read discount and tolerance
	  o = i.next();
	  if ( !(o instanceof String) || !((String)o).equalsIgnoreCase("discount")) {
		  System.out.println("Missing discount declaration: " + o);
		  System.exit(1);
	  }
	  bdDiscount = ((BigDecimal)i.next());

	  o = i.next();
	  if (!(o instanceof String) || !((String)o).equalsIgnoreCase("tolerance")) {
		  System.out.println("Missing tolerance declaration: " + o);
		  System.exit(1);
	  }
	  bdTolerance = ((BigDecimal)i.next());

	  //For RTDP and BRTDP Read initial and goal states/////////////////////////////
	  if(typeSolution.equals("RTDP") || typeSolution.equals("RTDPIP") || typeSolution.equals("BRTDP") ){
		  listInitialStates = new ArrayList();
		  listGoalStates = new ArrayList();
		  o = i.next();
		  if ( !(o instanceof String) || !((String)o).equalsIgnoreCase("initial")) {
			  System.out.println("Missing initial state declaration: " + o);
			  System.exit(1);
		  }
		  o = i.next();
		  while (  !(o instanceof String)  ) {
			 
			  TreeMap<Integer,Boolean> initialState=createState(o);
			  listInitialStates.add(initialState);
			  o = i.next();
		  }
	  
		  o = i.next();
		  if ( !(o instanceof String) || !((String)o).equalsIgnoreCase("goal")) {
			  System.out.println("Missing goal state declaration: " + o);
		  }
		  o = i.next();
		  while ( !(o instanceof String) ) {
			  
			  TreeMap<Integer,Boolean> goalState=createState(o);
			  listGoalStates.add(goalState);
			  o = i.next();
		  }

	  }
	  
	  if(typeSolution.compareTo("RTDPEnum")==0 || typeSolution.compareTo("BRTDPEnum")==0){
		  listInitialStatesEnum=new ArrayList<State>();
		  listGoalStatesEnum=new ArrayList<State>();
		  o = i.next();
		  if ( !(o instanceof String) || !((String)o).equalsIgnoreCase("initial")) {
			  System.out.println("Missing initial state declaration: " + o);
			  System.exit(1);
		  }
		  o = i.next();
		  while (  !(o instanceof String)  ) { 
			  State initialState=createStateEnum(o);
			  listInitialStatesEnum.add(initialState);
			  o = i.next();
		  }
	  
		  o = i.next();
		  if ( !(o instanceof String) || !((String)o).equalsIgnoreCase("goal")) {
			  System.out.println("Missing goal state declaration: " + o);
		  }
		  o = i.next();
		  while ( !(o instanceof String) ) {
			  
			  State goalState=createStateEnum(o);
			  listGoalStatesEnum.add(goalState);
			  o = i.next();
		  }

	  }
	  
	  
	  
	  ///////Read basis function and factored reward for MP////////////////////////////////////////
	  
	  if(typeSolution.compareTo("MP")==0 ){

		  //TODO: 
		  listBasisFunctions=new ArrayList();
		  listFactoredReward=new ArrayList();
		  o = i.next();
		  while ( !(o instanceof String) || !((String)o).equalsIgnoreCase("basisFunctions")) {
			  o = i.next();
		  }
		  // o == "basisFunctions"
		  //context.workingWithParameterizedBef=context.workingWithParameterized;
		  //context.workingWithParameterized=false; //in order to create and ADD for Reward
		  o = i.next();
		  while ( !(o instanceof String) ||!((String)o).equalsIgnoreCase("endbasis") ) {
			  ArrayList basis = (ArrayList)o;
			  Object basisDD = context.buildDDFromUnorderedTree(basis, tmVar2ID,null,null);
			  listBasisFunctions.add(basisDD);
			  o = i.next();
		  }
		  context.workingWithParameterizedBef=context.workingWithParameterized;
		  context.workingWithParameterized=false; //in order to create and ADD for Reward 
		  o = i.next();
		  while ( !(o instanceof String) || !((String)o).equalsIgnoreCase("factoredReward")) {
			  o = i.next();
		  }
		  o = i.next();
		  while ( !(o instanceof String) ||!((String)o).equalsIgnoreCase("endfactoredReward") ) {
			  ArrayList rew = (ArrayList)o;
			  Object rewDD = context.buildDDFromUnorderedTree(rew, tmVar2ID,null,null);
			  this.listFactoredReward.add(rewDD);
			  o = i.next();
		  }
		  context.workingWithParameterized=context.workingWithParameterizedBef;
	
	  }
 }
  
  
/**
 * create a State represented with non prime variables
 * @param o: an ArrayList with the variables
 * @return
 */  
private TreeMap<Integer, Boolean> createState(Object o) {
	
	    TreeMap<Integer,Boolean> state=new TreeMap();
	    //set false all variables in state  
	    for (int i=this.numVars+1; i<= (this.numVars)*2;i++){
	    	state.put(Integer.valueOf(i),false);
	    }
	    //change to true the variables in the SPUDD file
        ArrayList alVarsPos = (ArrayList)((ArrayList)o).clone();
    	Iterator vars = alVarsPos.iterator();
    	while (vars.hasNext()) {
    	    String vname = ((String)vars.next());// + "'"; Verificar si necesitamos ' o no   NO NECESITAMOS
    	    Integer id=(Integer)tmVar2ID.get(vname);
    	    state.put(id,true);
  	    }
    	return state; 
}

// For RTDPEnum and BRTDPEnum
private State createStateEnum(Object o) {
	
	TreeMap<Integer,Boolean> values=new TreeMap<Integer,Boolean>();
    //set false all variables in state  
    for (int i=this.numVars+1; i<= (this.numVars)*2;i++){
    	values.put(Integer.valueOf(i),false);
    }
    //change to true the variables in the SPUDD file
    int identifier=0; // take into account the position (prime) variable  
    ArrayList alVarsPos = (ArrayList)((ArrayList)o).clone();
	Iterator vars = alVarsPos.iterator();
	while (vars.hasNext()) {
	    String vname = ((String)vars.next());// + "'"; Verificar si necesitamos ' o no   NO NECESITAMOS
	    Integer id=(Integer)tmVar2ID.get(vname);
	    values.put(id,true);
	    identifier=(int) (identifier+Math.pow(2,2*numVars-id)) ; 
	    }
	
	State state=new State(values,identifier);
	return state; 
}

	//For constraints in MDPIP  
    private void createFileConstraints(ArrayList constraints, String nameFileConstraint) {
		
    	try {
            BufferedWriter out = new BufferedWriter(new FileWriter(nameFileConstraint));
            writeConstraints(constraints,out);
            out.close();
        } catch (IOException e) {
        	System.out.println("Problem with the Constraint file");
        	System.exit(0);
        }

	}
    
    

    private void writeConstraints(ArrayList constraints, BufferedWriter out) throws IOException {
        for(int i=0;i< constraints.size();i++){
        	String line=createLineFileConst((ArrayList)constraints.get(i));
        	out.write(line);
        	out.append(System.getProperty("line.separator"));
        }		
	}


	private void createFileConstraintsGreaterZero(ArrayList constraints, String nameFileConstraint) {
		
    	try {
    		numConstraints=0;
            BufferedWriter out = new BufferedWriter(new FileWriter(nameFileConstraint));
            writeConstraints(constraints,out);
            int i=numConstraints;
            //write constrains p_i >= epsilon 
            Iterator it=context.listVarProb.iterator();
            while(it.hasNext()){
             i++;
           	 out.append("subject to r"+i+": p" + it.next() + ">="+Context._df.format(epsilon)+";");
           	 out.append(System.getProperty("line.separator"));
            }
            
            out.close();
        } catch (IOException e) {
        	System.out.println("Problem with the Constraint file");
        	System.exit(0);
        }

	}

    
    
	private String createLineFileConst(ArrayList list) {
		numConstraints++;
		String line=new String("subject to r"+numConstraints+":  ");
		for(int i=0;i< list.size();i++){
			line=line+list.get(i);
		}
		return line+";";
	}
	
	public int solve(int maxNumberIterations, double mergeError) {
		if(this.pruneAfterEachIt){
			NAME_FILE_VALUE=NAME_FILE_VALUE+"_"+Double.toString(mergeError).replace(".", "_")+"APRI"+".net";//NAME_FILE_VALUE is inicializated in MDP_Fac(...)
		}
		else{
			NAME_FILE_VALUE=NAME_FILE_VALUE+"_"+Double.toString(mergeError).replace(".", "_")+"REGR"+".net";
		}
		int numIterations=0;   
        Object QiPlus1DD, DiffDD;
    	valueiDD = rewardDD;
        //context.view(valueiDD);
    	double Rmax=context.apply(valueiDD, Context.MAXVALUE);
    	double Vmax=Rmax;
    	context.workingWithParameterizedBef=context.workingWithParameterized;
    	context.createBoundsProb(NAME_FILE_CONTRAINTS);
    	while(numIterations<maxNumberIterations){
       		valueiPlus1DD=context.getTerminalNode(Double.NEGATIVE_INFINITY);
    		//iterate over each action
    		Iterator actions=mName2Action.entrySet().iterator();
    		while (actions.hasNext()){
    			Map.Entry meaction=(Map.Entry) actions.next();
    			Action action=(Action) meaction.getValue();
    			System.out.println("  - Regress action " + action.getName());
    			//context.view(valueiDD);
      			context.workingWithParameterized=context.workingWithParameterizedBef;
    			QiPlus1DD=this.regress(valueiDD, action,mergeError*Vmax,action.tmID2ADD,false,false);
    			
    			//context.view(QiPlus1DD);
    			//context.view(valueiPlus1DD);
     			valueiPlus1DD=context.apply(valueiPlus1DD, QiPlus1DD, Context.MAX);
     			//context.view(valueiPlus1DD);
     			
        	    flushCaches(null);			
    		}
    		//traje estas 2 lineas aqui de regress    	
    		//context.view(valueiPlus1DD);
    		valueiPlus1DD=context.apply(valueiPlus1DD, context.getTerminalNode(this.bdDiscount.doubleValue()), Context.PROD);
    		valueiPlus1DD=context.apply(valueiPlus1DD, this.rewardDD, Context.SUM);
    				
    		//context.view(valueiPlus1DD);
    		//context.view(valueiDD);
    		DiffDD=context.apply(valueiPlus1DD, valueiDD, Context.SUB);
    		Double maxDiff=(Double) context.apply(DiffDD, Context.MAXVALUE);
    		Double minDiff=(Double) context.apply(DiffDD, Context.MINVALUE);
    		Double BellErro=Math.max(maxDiff.doubleValue(),-minDiff.doubleValue());
    		//if(BellErro.compareTo((1-bdDiscount.doubleValue())*this.bdTolerance.doubleValue())/(2*bdDiscount.doubleValue())<0){
    		//for testing we comment this line 
    		if (BellErro.compareTo(this.bdTolerance.doubleValue())<0 && !forceNumberIt){
    			 int contNumNodes=this.context.contNumberNodes(valueiPlus1DD);
    			 System.out.println("Terminate after "+numIterations+" iterations");
    			 //context.view(valueiPlus1DD);
    			 return contNumNodes;
    		}
    		
    		valueiDD=valueiPlus1DD;
    		
    		if (mergeError!=0 && !this.context.workingWithParameterized&&pruneAfterEachIt){
    			
    			//context.view(valueiDD);
    			System.out.println("Prune ADD");
      			valueiDD=context.pruneNodesValue(valueiDD, mergeError*Vmax);
      			
     			//context.view(valueiDD);
    		}
    		System.out.println("Iteration:  "+numIterations +" NumCallSolver:  "+context.numCallSolver+" Reuse Cache Internal Node instead of  Call Solver: "+context.reuseCacheIntNode +" reuse: "+context.contReuse+" no reuse: "+context.contNoReuse+" reduced to value:  "+context.numberReducedToValue+ " reuse using lattice "+context.contReuseUsingLattice);
    		
    		//context.view(valueiDD);
    		numIterations=numIterations+1;
    	//  Display lattice with primitive concepts
		//	Graph g1 = context.lattice.getGraph(false/* suppress primitive concepts */); 
		//	g1.launchViewer();
    		Vmax=Rmax+this.bdDiscount.doubleValue()*Vmax;
    		
    	}
    	if(printFinalADD){
    	  context.view(valueiDD);
    	}
    	if(printPolicy){
    		System.out.println("not implemented yet");

    	}
    	
    	if(dumpValue && this.typeContext==1){ 
    	  context.dump(valueiDD,NAME_FILE_VALUE);
    	}
        int contNumNodes=this.context.contNumberNodes(valueiDD);
        //System.out.println("Number of Nodes:  "+contNumNodes);
    	flushCaches(null);		
        return contNumNodes;    	
    }
    
	
	public void flushCaches(Object VDD) {
		if (((double)RUNTIME.freeMemory() / 
		     (double)RUNTIME.totalMemory()) > FLUSH_PERCENT_MINIMUM) {
		    return; // Still enough free mem to exceed minimum requirements
		}
		System.out.println("Before flush,freeMemory: "+RUNTIME.freeMemory());

		context.clearSpecialNodes();
		Iterator i = mName2Action.values().iterator();
		while (i.hasNext()) {
		    Action a = (Action)i.next();
		    Iterator j = a.hsTransADDs.iterator(); //list of ADDs  in action
		    while (j.hasNext()) {
			 context.addSpecialNode(j.next());
		    }
		}
		if(VDD!=null){
			System.out.println("flush before call solver");
			context.addSpecialNode(VDD);
		}
		if (this instanceof MDP_Flat){
			//copy the  tables in action2TransTable
			Iterator it= action2TransTable.keySet().iterator();
			while (it.hasNext()){
				
				
				context.addSpecialNode(action2TransTable.get(it.next()));
			}

		}
		context.addSpecialNode(rewardDD);
		if (valueiDD!=null){
			context.addSpecialNode(valueiDD);
		}
		if (valueiPlus1DD!=null){
			context.addSpecialNode(valueiPlus1DD); 
		}
		context.flushCaches();
		System.out.println("After flush,freeMemory: "+RUNTIME.freeMemory());
   }
	public abstract Object regress(Object VDD, Action action,double mergeError,TreeMap iD2ADD,boolean simulating,boolean firsTimeSimulating);
	
	//////////////////////////////////////////////////////////////FOR SIMULATION MDPIP ///////////////////////////////////////////////////////////////////////////////////////////////////////////
	public ArrayList simulateMDPIP(int numberInitialStates,int numberSamples,int tMax, String NAME_FILE_VALUE) {

		context.workingWithParameterizedBef=context.workingWithParameterized;
		context.workingWithParameterized=false;
		Object valueRes=context.readValueFunction(NAME_FILE_VALUE);
		//context.view(valueRes);
	    HashMap initialState2AvgValue=new HashMap();
	    double totalSum=0;
	    ArrayList listReward=new ArrayList();
	    
	    Random randomGenInitial=new Random(19580427);
		Random randomGenNextState=new Random(19580800);
	    // This policy should be based on the MDPIP regression
	    // where you do a min_{ p_1 ... P_n } Q(s,a,p_1,...,p_n)
	    TreeMap action2QDD=calculateQHash(valueRes,true); //here we call the solver
	    
	    
        for(int nI=1; nI<=numberInitialStates; nI++){
 
        	HashMap initialState=sampleInitialState(randomGenInitial);
            HashMap state=null;
        	double sumReward=0;
        	for ( int numS=1; numS<=numberSamples; numS++){
            	state=new HashMap(initialState);
            	System.out.println("Initial state:"+state);
        		double rewardState=getReward(state);
        		context.workingWithParameterized=context.workingWithParameterizedBef;
        		if(context.workingWithParameterized){
        		     Hashtable sampleProbabilities=context.sampleProbabilitiesSubjectTo(NAME_FILE_CONTRAINTS);
        		     convertParADD2ADDCPTs(sampleProbabilities);//fills tmID2ADDNewSample
        		}
        		//because after this line we sampled an MDP given an MDPIP
        		context.workingWithParameterized=false;
        		                
        		for(int t=1;t<=tMax;t++){
        			Action aBest=findBestA(state,action2QDD);//this is the work of the agent
        			//System.out.println("action:"+aBest.getName());
        			if (aBest==null){
        				System.out.println("Some problem finding best action");
        				System.exit(0);
        			}
        			HashMap nextState=chooseNextState(state,aBest,true,randomGenNextState);//this is the work of the simulator true for using tmID2ADDNewSample
        			//System.out.println("Next State:"+nextState);
        			state=remapWithOutPrimes(nextState);
        			if (printTrafficFormat){
        				System.out.println("state: "+getTrafficString(state));
        			}
        			//TODO: preguntar a Scott cual de los dos es el correcto, yo creo que la primera
        			rewardState=rewardState+Math.pow(this.bdDiscount.doubleValue(),t)* getReward(state);
        			//rewardState=this.bdDiscount.doubleValue()*rewardState+getReward(state)
        			
        		}
        		listReward.add(rewardState);
        		//System.out.println("reward State:"+rewardState);
        		sumReward=sumReward+rewardState;
               
        	}
        	double avgValue=sumReward/numberSamples;
        	System.out.println("Average for state: "+initialState+" is "+avgValue);
        	initialState2AvgValue.put(new HashMap(initialState),avgValue);
        	totalSum=totalSum+avgValue;
        	flushCachesSimulator(action2QDD,false,null,null,null);
        }
        // double mean=calculateMean(listReward);
        double mean=totalSum/numberInitialStates;
        double sigma=calculateStandarD(mean,listReward);
        double standardError=sigma/Math.sqrt(listReward.size());
        ArrayList res=new ArrayList();
        res.add(mean);
        res.add(standardError);
        //System.out.println("Values: "+listReward);
        //System.out.println("Mean: :"+mean);
        //System.out.println("Standard Error: :"+standardError);
         return res;
	}
	
	/**
	 * A string representation of the traffic state 4-way intersection
	 * @param state
	 * @return
	 */
    public String getTrafficString(HashMap<String,Boolean> state) {

        //  r6 r4 r2  TG  ||  RN  r1 r3 r5
        StringBuilder sb = new StringBuilder();
        int max_size = (this.alVars.size() - 8)/4;
        for (int i = max_size; i >= 1; i--) {
                boolean val = state.get("r" + (i << 1));
                sb.append(val ? "." : "#");
        }

        sb.append(" " +  (state.get("t2") ? "T" : "N") +
                                 (state.get("c2") ? "G" : "R") + " |");

        sb.append("| " + (state.get("t1") ? "T" : "N") +
                         (state.get("c1") ? "G" : "R") + " ");

        for (int i = 1; i <= max_size; i++) {
                boolean val = state.get("r" + ((i << 1) - 1));
                sb.append(val ? "." : "#");
        }

        //  r6 r4 r2  TG  ||  RN  r1 r3 r5
        StringBuilder sb2 = new StringBuilder();
        for (int i = max_size; i >= 1; i--) {
                boolean val = state.get("q" + (i << 1));
                sb2.append(val ? "." : "#");
        }

        sb2.append(" " + (state.get("t4") ? "T" : "N") +
                                 (state.get("c4") ? "G" : "R") + " |");

        sb2.append("| " + (state.get("t3") ? "T" : "N") +
                          (state.get("c3") ? "G" : "R") + " ");

        for (int i = 1; i <= max_size; i++) {
                boolean val = state.get("q" + ((i << 1) - 1));
                sb2.append(val ? "." : "#");
        }

        return sb.toString() + "\n" + sb2.toString() + "\n";
}
	/**
	 * A string representation of the traffic state 2-way intersection
	 * @param state
	 * @return
	 */	
public String getTrafficStringOneLane(HashMap<String,Boolean> state) {
	  
	    state=remapVariablesWithNames(state);  
	     
		//  r6 r4 r2  TG  ||  RN  r1 r3 r5
		StringBuilder sb = new StringBuilder();
		int max_size = (this.alVars.size() - 4)/2;
		for (int i = max_size; i >= 1; i--) {
			boolean val = state.get("r" + (i << 1));
			sb.append(val ? "." : "#");
		}
		
		sb.append(" " +  (state.get("t2") ? "T" : "N") + 
				         (state.get("c2") ? "G" : "R") + " |");
		
		sb.append("| " + (state.get("t1") ? "T" : "N") + 
		                 (state.get("c1") ? "G" : "R") + " ");
				
		for (int i = 1; i <= max_size; i++) {
			boolean val = state.get("r" + ((i << 1) - 1));
			sb.append(val ? "." : "#");
		}		
		
		return sb.toString();
	}
	private HashMap<String, Boolean> remapVariablesWithNames(HashMap state) {
	    HashMap <String, Boolean> State=new HashMap();
		Iterator it=state.keySet().iterator();
		while (it.hasNext()){
			Integer idVar=(Integer)it.next();
			String var=(String)this.tmID2Var.get(idVar);
			int value=((Integer) state.get(idVar)).intValue();
			if (value==1){
				State.put(var, true);				
			}
			else{
				State.put(var,false);
			}
				
		}
		return State;
}
	public void flushCachesSimulator(TreeMap action2QDD, boolean copyVUpperAndLower, Object valueRes, Object otherValueRes, Object valueGap) {
		if (((double)RUNTIME.freeMemory() / 
		     (double)RUNTIME.totalMemory()) > FLUSH_PERCENT_MINIMUM) {
		    return; // Still enough free mem to exceed minimum requirements
		}
		//System.out.println("Before flush,freeMemory: "+RUNTIME.freeMemory());
        System.out.println("flushing cache");
		context.clearSpecialNodes();
		Iterator i = mName2Action.values().iterator();
		while (i.hasNext()) {
		    Action a = (Action)i.next();
		    Iterator j = a.hsTransADDs.iterator(); //list of ADDs  in action
		    while (j.hasNext()) {
			 context.addSpecialNode(j.next());
		    }
		}
		
		context.addSpecialNode(rewardDD);
		
		if (copyVUpperAndLower ){
			context.addSpecialNode(valueRes);
			if (otherValueRes!=null){
				context.addSpecialNode(otherValueRes);
			}
			if (valueGap!=null){
				context.addSpecialNode(valueGap);
			}
		}
			
		
		Iterator it=action2QDD.keySet().iterator();
		while(it.hasNext()){
			Object QADD=action2QDD.get((Action)it.next());
			context.addSpecialNode(QADD);
		}
		context.flushCaches();
		//System.out.println("After flush,freeMemory: "+RUNTIME.freeMemory());
   }
	
/*	private double calculateMean(ArrayList listReward) {
		double sum=0;
		int n=listReward.size();
		for (int i=0;i<n;i++){
			sum=sum+(Double)listReward.get(i);
		}
		return sum/n; 
	}*/
	
	public double calculateStandarD(double mean, ArrayList listReward) {
		double sum=0;
		int n=listReward.size();
		for (int i=0;i<n;i++){
			sum=sum+Math.pow((Double)listReward.get(i)-mean,2d);
		}
		return Math.sqrt(sum/n); 
	}
	private HashMap remapWithOutPrimes(HashMap nextState) {
		HashMap state=new HashMap();
		Iterator it=nextState.keySet().iterator();
		while (it.hasNext()){
			Integer var=(Integer)it.next();
			state.put(this.hmPrime2IdRemap.get(var),nextState.get(var));
		}
		return state;
	}
	private TreeMap remapWithPrimes(TreeMap nextState) {
		TreeMap state=new TreeMap();
		Iterator it=nextState.keySet().iterator();
		while (it.hasNext()){
			Integer var=(Integer)it.next();
			
			state.put(this.hmPrimeRemap.get(var),nextState.get(var));
		}
		return state;
	}
	
	/**
	 * it fills tmID2ADDNewSample for all actions using the sampleProbabilities
	 * @param sampleProbabilities
	 */
	
	private void convertParADD2ADDCPTs(Hashtable sampleProbabilities) {
		// go through all actions and cpts in each action and replace the sampleProbabilities in the leaves
		System.out.println("  evaluating actions with probabilities ");
		Iterator actions=mName2Action.entrySet().iterator();
		while (actions.hasNext()){
			Map.Entry meaction=(Map.Entry) actions.next();
			Action action=(Action) meaction.getValue();
			//System.out.println("  - evaluate action with probabilities " + action.getName());
			convertCPTsAction(action,sampleProbabilities); 			
		}
	}
	
	
	
	private void convertCPTsAction(Action action,Hashtable sampleProbabilities) {
		action.tmID2ADDNewSample=new TreeMap();
		Integer xiprime;
		Iterator x=this.hmPrimeRemap.entrySet().iterator();
		while (x.hasNext()){
			Map.Entry xiprimeme = (Map.Entry)x.next();
			xiprime=(Integer) xiprimeme.getValue();
			Object cpt_a_xiprime=action.tmID2ADD.get(xiprime);
			//context.workingWithParameterized=true;
			//context.view(cpt_a_xiprime);
			context.reduceConvert=new Hashtable();
			Integer newcpt_a_xiprime=context.convertCPT((Integer)cpt_a_xiprime,sampleProbabilities);
			action.tmID2ADDNewSample.put(xiprime,newcpt_a_xiprime);
			//context.workingWithParameterized=false;
			//context.view(newcpt_a_xiprime);
		}
		
	}
	  
	HashMap chooseNextState(HashMap state,Action action,Boolean workWithMDPIP,Random randomGenerator){
		//we create a topological order in the case that we have asyncronous arcs in the DBN
		//because this must be sampled before the other ones
		HashMap nextState=new HashMap();
		Graph g=createGraphFor(action.varId2DependPrimeList);
		List xiprimeOrder=g.topologicalSort(false);
		Integer xiprime;
		//add the xiprimeOrder in a list
		List list=new ArrayList();
		for (int i=0;i<xiprimeOrder.size();i++){
			list.add(Integer.parseInt((String)xiprimeOrder.get(i)));
		}
		//add the xiprimes that not have dependences in the list
		Iterator it=hmPrimeRemap.keySet().iterator();
		while(it.hasNext()){
			xiprime=(Integer)hmPrimeRemap.get(it.next());
			if(!list.contains(xiprime)){
				list.add(xiprime);
			}
		}
		
		//now sample based on the ordered list
  		for (int j=0;j<list.size();j++){
  			xiprime= (Integer)list.get(j);
  			Object cpt_a_xiprime;
  			if(workWithMDPIP){
  				cpt_a_xiprime=action.tmID2ADDNewSample.get(xiprime);
  			}
  			else{
  				cpt_a_xiprime=action.tmID2ADD.get(xiprime);
  			}
  			if(cpt_a_xiprime==null){
  				System.out.println("not find cpt in choose next state");
  				System.exit(1);
  			}
  			
  			//context.view(cpt_a_xiprime);
  			double probTrue,probFalse;
  			if(context.isTerminalNode(cpt_a_xiprime)){
  				probTrue=((TerminalNodeKeyADD)context.getInverseNodesCache().get(cpt_a_xiprime)).getValue();
  				//probFalse=((TerminalNodeKeyADD)context.getInverseNodesCache().get(cpt_a_xiprime)).getValue();
   			}
  			else{
   				probTrue=context.getValueForStateInADD((Integer)cpt_a_xiprime,state,nextState,xiprime,1);
  				//probFalse=context.getValueForStateInADD((Integer)cpt_a_xiprime,state,nextState,xiprime,0);
  			}
  			double ran=randomGenerator.nextDouble();
   			//double ran=Math.random();
			if (ran<=probTrue){
				nextState.put(xiprime,1); 
			}
			else{
				nextState.put(xiprime,0); 
			}
  		}
  		return nextState;	
  	}
	private Graph createGraphFor(HashMap varId2DependPrimeList) {
		 Graph g = new Graph();
         g.setBottomToTop(false);
         g.setMultiEdges(false); // Note: still does not allow cyclic edges
         Iterator it= varId2DependPrimeList.keySet().iterator();
         while (it.hasNext()){
        	 Integer varId=(Integer)it.next();
        	 ArrayList dep=(ArrayList)varId2DependPrimeList.get(varId);
        	 for(int i=0;i<dep.size();i++){
        		 g.addUniLink(dep.get(i).toString(),varId.toString());	 
        	 }
         }
         //g.launchViewer();
         return g;
        
	}
	private Action findBestA(HashMap state, TreeMap action2QDD) {
		double bestQ=Double.NEGATIVE_INFINITY;
	    Action bestAction=null;
	    Action qAction=null;
		Iterator it=action2QDD.keySet().iterator();
		while(it.hasNext()){
			qAction=(Action)it.next();
			//System.out.println(" "+qAction.getName());
			Object QADD=action2QDD.get(qAction);
			//context.view(QADD);
			double valueQ=context.getValueForStateInADD((Integer)QADD,state,null,null,null);
			if(valueQ> bestQ){
				bestQ=valueQ;
				bestAction=qAction;
			}
		}
		return bestAction;
	}
	private Action findBestANew(TreeMap<Integer, Boolean>  state, TreeMap action2QDD) {
		double bestQ=Double.NEGATIVE_INFINITY;
	    Action bestAction=null;
	    Action qAction=null;
		Iterator it=action2QDD.keySet().iterator();
		while(it.hasNext()){
			qAction=(Action)it.next();
			//System.out.println(" "+qAction.getName());
			Object QADD=action2QDD.get(qAction);
			//context.view(QADD);
			double valueQ=context.getValueForStateInContext((Integer)QADD, state, null, null);
			if(valueQ> bestQ){
				bestQ=valueQ;
				bestAction=qAction;
			}
		}
		return bestAction;
	}
	private double getReward(HashMap state) {
		//context.view(rewardDD);
		return context.getValueForStateInADD((Integer)rewardDD,state,null,null,null);
	}

	private HashMap sampleInitialState( Random randomGenerator) {
		HashMap state=new HashMap();
		Iterator it=hmPrimeRemap.keySet().iterator();
		while (it.hasNext()){
			double ran=randomGenerator.nextDouble();
			if (ran<=0.5){
				state.put(it.next(),1);
			}
			else{
				state.put(it.next(),0);
			}
		}
       return state;		
	}
	private HashMap sampleInitialStateFromListIntVal(Random randomGenerator) {
		TreeMap<Integer, Boolean> ini=this.sampleInitialStateFromList(randomGenerator);
		HashMap initial=new HashMap();
		Iterator it=ini.keySet().iterator();
		while (it.hasNext()){
			Integer var=(Integer)it.next();
			if(ini.get(var)){
				initial.put(var, 1);
			}
			else{
				initial.put(var, 0);
			}
		}
		return initial;
	}
		
	private TreeMap calculateQHash(Object valueRes,Boolean workWithMDPIP) {
		TreeMap action2QDD=new TreeMap(new ActionComparator());
		//iterate over each action
		//System.out.println("  - Regress actions ");
		Iterator actions=mName2Action.entrySet().iterator();
		while (actions.hasNext()){
			Map.Entry meaction=(Map.Entry) actions.next();
			Action action=(Action) meaction.getValue();
			//System.out.println("  - Regress action " + action.getName());
  			context.workingWithParameterized=workWithMDPIP;
  			action2QDD.put(action,(this.regress(valueRes, action,0,action.tmID2ADD,true,true)));
		}
		return action2QDD;
	}
	public static boolean HasOnlyDigits(String s) {
		 for (int i = 0; i < s.length(); i++) {
			 if (!Character.isDigit(s.charAt(i))) {
				 return false;
			 }
		 }
		 return true;
	 }
	
	/////////////////////////////////////////Factored BRTDP///////////////////////////////////////////////////////
	public  ArrayList<ArrayList> solveBRTDPFac(int maxDepth, long timeOut,long maxUpdates,double tau,String typeMDP,String typeSolution, int numTrials, int interval,int numTrialsSimulation, int numberInitialStates,Random randomGenInitial, Random randomGenNextState,MDP myMDPSimulator) {
	    System.out.println("In solveBRTDP Fac");
		NAME_FILE_VALUE=NAME_FILE_VALUE+"_"+typeSolution+".net";//NAME_FILE_VALUE is inicializated in MDP_Fac(...)
		Stack<TreeMap<Integer,Boolean>> visited=new Stack<TreeMap<Integer,Boolean>>();
		long timeSum=0;
		ResetTimer();
		Action actionGreedy=null;
		//Initialize Vu and Vl with admissible value function
		//create an ADD with 0 for V_l and V_u=Rmax/1-gamma
		
		double Rmax=context.apply(this.rewardDD, Context.MAXVALUE);
		double Rmin=context.apply(this.rewardDD, Context.MINVALUE);
		if(this.bdDiscount.doubleValue()==1){
			minLower=Rmin*maxDepth;
			maxUpper=Rmax*maxDepth;
		}
		else{
			minLower=Rmin/(1-this.bdDiscount.doubleValue());
			maxUpper=Rmax/(1-this.bdDiscount.doubleValue());
		}
		
		VLower=context.getTerminalNode(minLower);
		VUpper=context.getTerminalNode(maxUpper);
		ArrayList<ArrayList> perf=new ArrayList<ArrayList>();

		VGap=context.apply(VUpper, VLower, Context.SUB);
        contUpperUpdates=0;
        contbreak=0;
		//do trials until convergence or timeOut
        //while (contUpperUpdates<maxUpdates){//timeSeg<timeOut){ not using timeOut only for test
        for(int trial=1; trial <=numTrials;trial++){
			int depth=0;
			visited.clear();// clear visited states stack
			//draw initial state
			TreeMap<Integer,Boolean> stateInitial=sampleInitialStateFromList(randomGenInitial);

			boolean firstTime=true;
			TreeMap<Integer,Boolean> state=new TreeMap<Integer,Boolean>(stateInitial);
			//do trial
			while(!inGoalSet(state) && (state !=null)&& depth<maxDepth){//&& contUpperUpdates<maxUpdates){//not update more than maxUpdates in the last iteration
				depth++;
				visited.push(state);
				//this compute maxUpperUpdated and actionGreedy
				actionGreedy=updateVUpper(state);
				contUpperUpdates++;
				//System.out.println("action greedy: "+actionGreedy.getName());
				//context.view(VUpper);
				//this compute maxLowerUpdated
				updateVLower(state);
				//context.view(VLower);
				double gap=maxUpperUpdated-maxLowerUpdated; 
				updateValueBranch(state,'g',gap);
				//context.view(VGap);

				state=chooseNextStateBRTDP(state,actionGreedy,randomGenNextState,tau,stateInitial,firstTime);
				firstTime=false;
				if(state==null){
					contbreak++;
				}
				flushCachesBRTDP(false); 
			}
			//do optimization
			while(!visited.empty()){
				state=visited.pop();
				updateVUpper(state);
				contUpperUpdates++;
				updateVLower(state);
			}
			//IMPORTANT: for BRTDP you use the VLower to do the simulations
			//New part for simulate the policy //////////////////////////////////
			if(trial%interval==0){
				//context.view(VGap);
				//context.view(VLower);
				//context.view(VUpper);
				long time1  = GetElapsedTime();
				timeSum=timeSum+time1;
	            long timeSec=timeSum/1000;
				int nNodes=this.context.contNumberNodes(VLower);
				System.out.println("performing simulation with current value, with number of updates:"+contUpperUpdates);
				//Copy ADD in other MDP only to do simulation, the result must be the same
				//Object VLower2=myMDPSimulator.createADDFromADD((ContextADD)context,(Integer)VLower);
				//ArrayList resultSimulation=myMDPSimulator.simulateMDPFromADD(numberInitialStates,numTrialsSimulation,maxDepth,VLower2,typeSolution, randomGenInitial,randomGenNextState);
				ArrayList resultSimulation=simulateMDPFromADD(numberInitialStates,numTrialsSimulation,maxDepth,VLower,typeSolution, randomGenInitial,randomGenNextState,VUpper,VGap); //we do the simulations with the first ADD, the other we copy only to flush 
				resultSimulation.add(timeSec);
				resultSimulation.add(nNodes);
				resultSimulation.add(trial);
				resultSimulation.add(contUpperUpdates);
				perf.add(resultSimulation);
			}
			////////////////////////////////////////////////////////////////////
			ResetTimer();
		}
	   	if(printFinalADD){
	    	  context.view(VUpper);
	    }
	    return perf;
	}	
	/* Copy  V (an ADD), that is in context2, to the MDP 
	 * 
	 */	
	private Integer createADDFromADD(ContextADD context2, Integer F) {
		   	if(context2.isTerminalNode(F)){
		   		
		   		return (Integer) context.getTerminalNode(((TerminalNodeKeyADD)context2.getInverseNodesCache().get(F)).getValue());
	    	}
	    	Integer Fr= (Integer) ((ContextADD)context).getReduceCache().get(F);
		   	if(Fr==null){
	    		InternalNodeKeyADD intNodeKey=(InternalNodeKeyADD)context2.getInverseNodesCache().get(F);
	    		Integer Fh=createADDFromADD(context2,intNodeKey.getHigh());
	    		Integer Fl=createADDFromADD(context2,intNodeKey.getLower());
	    		Integer Fvar= intNodeKey.getVar();
	    		Fr=(Integer)context.GetNode(Fvar,Fh,Fl);
	    		((ContextADD)context).getReduceCache().put(F, Fr);
	    		
	    	}
	    	return Fr;
	 }


	public void flushCachesBRTDP(boolean workWithEnum) {
		if (((double)RUNTIME.freeMemory() / 
		     (double)RUNTIME.totalMemory()) > FLUSH_PERCENT_MINIMUM) {
		    return; // Still enough free mem to exceed minimum requirements
		}
		System.out.println("Before flush,freeMemory: "+RUNTIME.freeMemory());

		context.clearSpecialNodes();
		Iterator i = mName2Action.values().iterator();
		while (i.hasNext()) {
		    Action a = (Action)i.next();
		    Iterator j = a.hsTransADDs.iterator(); //list of ADDs  in action
		    while (j.hasNext()) {
			 context.addSpecialNode(j.next());
		    }
		}
		context.addSpecialNode(rewardDD);
		if(!workWithEnum){
			context.addSpecialNode(VUpper); 
			context.addSpecialNode(VLower);
			context.addSpecialNode(VGap);
		}
		context.flushCaches();
		System.out.println("After flush,freeMemory: "+RUNTIME.freeMemory());
   }
	
	
	private TreeMap<Integer, Boolean> chooseNextStateBRTDP(TreeMap<Integer, Boolean> state, Action actionGreedy,Random randomGenerator,double tau,TreeMap<Integer, Boolean> stateInitial,boolean firstTime) {
		
		// set weighted probabilities for each node from botton to top
		context.reduceCacheWeighted=new HashSet(); //in order to not repeat the calculation
		context.setProbWeightVGap(VGap,state,actionGreedy.tmID2ADD);
		//context.view(VGap);
		
		//check end trial/////////////////////
		if(!context.isTerminalNode(VGap)){
			//compute B
			InternalNodeKeyADD intNodeKey=(InternalNodeKeyADD)context.getInverseNodesCache().get(VGap);
			double B=intNodeKey.getprobWeightH()+intNodeKey.getprobWeightL();
			//System.out.println("B:"+B);
			
			
			// could precompute and store gInitial for each trial
			if (firstTime){
				stateInitial=this.remapWithPrimes(stateInitial);
				gapInitial=context.getValueForStateInContext((Integer)VGap, stateInitial, null, null);
			}
			if(B< gapInitial/tau){
				return null;
			}
		}
		//sampling each state variable from top to bottom using the weighted probabilities
		return samplingVGap(VGap,randomGenerator);
	}




	private TreeMap<Integer, Boolean> samplingVGap(Object beginF,Random randomGenerator) {
		TreeMap<Integer, Boolean> nextState=new TreeMap<Integer, Boolean>();
		Object F=beginF;
		for (int i=1; i<= this.numVars;i++){
			double ran=randomGenerator.nextDouble();

			Integer varPrime=Integer.valueOf(i);
			Integer var=Integer.valueOf(varPrime+this.numVars);
			if(!context.isTerminalNode(F)){
				InternalNodeKeyADD intNodeKey=(InternalNodeKeyADD)context.getInverseNodesCache().get(F);
				Integer Fvar= intNodeKey.getVar();
				if(Fvar.compareTo(varPrime)==0){ //var is in the ADD
					double wH=intNodeKey.getprobWeightH();
					double wL=intNodeKey.getprobWeightL();
					if (ran<=(wL/(wH + wL))){
						nextState.put(var,false);
						F=intNodeKey.getHigh();
					}
					else{
						nextState.put(var,true);
						F=intNodeKey.getLower();

					}
				}
				else{ //var is not in the ADD sample equality
					if (ran<=0.5){
						nextState.put(var,false);  		
					}
					else{
						nextState.put(var,true);  	
					}
				}
			}
			else{//is terminal node and there are more state variables to sample
				if (ran<=0.5){
					nextState.put(var,false);  		
				}
				else{
					nextState.put(var,true);  	
				}
			}
		}
		return nextState;
	}
	private void updateVLower(TreeMap<Integer, Boolean> state) {
		double max=Double.NEGATIVE_INFINITY;
		Iterator actions=mName2Action.entrySet().iterator();
		while (actions.hasNext()){
			Map.Entry meaction=(Map.Entry) actions.next();
			Action action=(Action) meaction.getValue();
			//System.out.println("  Computing Q Lower action " + action.getName());
			double Qt;
			Qt=this.computeQ(VLower,state,action,action.tmID2ADD);
			max=Math.max(max,Qt);
		}
		double rew=context.getValueForStateInContext((Integer)this.rewardDD, state, null, null);
		double maxTotal=rew+this.bdDiscount.doubleValue()*max;
	    //update the ADD
		updateValueBranch(state,'l',maxTotal);
		maxLowerUpdated=maxTotal;
	}
	private void updateVLower(State state) {
		double max=Double.NEGATIVE_INFINITY;
		Iterator actions=mName2Action.entrySet().iterator();
		int posAction=0;
		while (actions.hasNext()){
			Map.Entry meaction=(Map.Entry) actions.next();
			Action action=(Action) meaction.getValue();
			//System.out.println("  Computing Q Lower action " + action.getName());
			double Qt;
			Qt=this.computeQEnum((HashMap)VLower, state,action,action.tmID2ADD,'l',posAction);
			max=Math.max(max,Qt);
			posAction++;
		}
		
		double rew=context.getRewardForStateInContextEnum((Integer)this.rewardDD, state,this.numVars);
		double maxTotal=rew+this.bdDiscount.doubleValue()*max;
        //update hash
		((HashMap)VLower).put(state,maxTotal);
		maxLowerUpdated=maxTotal;
	}
	
	
	
	
	
	
	/**
	 * Update VUpper and return the action greedy
	 */
	private Action updateVUpper(TreeMap<Integer, Boolean> state) {
		double max=Double.NEGATIVE_INFINITY;
		Action actionGreedy=null;
		Iterator actions=mName2Action.entrySet().iterator();
		while (actions.hasNext()){
			Map.Entry meaction=(Map.Entry) actions.next();
			Action action=(Action) meaction.getValue();
			//context.view(VUpper); 
			//System.out.println("  Computing Q Upper action " + action.getName());
			double Qt;
			context.workingWithParameterized=context.workingWithParameterizedBef; 
		    Qt=this.computeQ(VUpper, state,action,action.tmID2ADD);
		    System.out.println("probNature:"+context.currentValuesProb+"action:"+action.getName()+"Quality:"+Qt);
			max=Math.max(max,Qt);
			if(max==Qt){
				actionGreedy=action;
				probNature=new Hashtable(context.currentValuesProb); //new line Karina
			}
		
		}

		double rew=context.getValueForStateInContext((Integer)this.rewardDD, state, null, null);
		double maxTotal=rew+this.bdDiscount.doubleValue()*max;
        //update the ADD VUpper
		updateValueBranch(state,'u',maxTotal);
		//context.view(VUpper);
		maxUpperUpdated=maxTotal; //Error, maxUpper must not be updated, we need to use other variable
		return actionGreedy;

	}
	
	
	
	
	private void updateValueBranch(TreeMap<Integer, Boolean> state, char c, double value) {
		Iterator iteratorState=state.keySet().iterator();			
		if (c=='u'){
			VUpper=context.insertValueInDD(VUpper, state,  value, iteratorState,this.hmPrimeRemap);		
		}
		else if(c=='l'){
			VLower=context.insertValueInDD(VLower, state,  value, iteratorState,this.hmPrimeRemap);		
		}
		else if(c=='g'){
			VGap=context.insertValueInDD(VGap, state,  value, iteratorState,this.hmPrimeRemap);
		}
    }

	private double computeQ(Object V,TreeMap<Integer, Boolean> state, Action action, TreeMap iD2ADD) {
		//it is not necessary to do remapIdWithPrime because V has all prime variables 
		
		Object VPrime=V; 
		Integer xiprime;
		Iterator x=this.hmPrimeRemap.entrySet().iterator();
		//context.view(VPrime);
		
		while (x.hasNext()){

			Map.Entry xiprimeme = (Map.Entry)x.next();
			xiprime=(Integer) xiprimeme.getValue();
			Object cpt_a_xiprime=iD2ADD.get(xiprime);
			//context.view(cpt_a_xiprime);
			Object Fh,Fl;
			
			if(!context.workingWithParameterized){ //new part Karina
				Double probTrue,probFalse;
				probTrue=(Double)context.getValuePolyForStateInContext((Integer)cpt_a_xiprime,state,xiprime,true);////new method Karina
				probFalse=1-probTrue;
				Fh=context.getTerminalNode(probTrue);
				Fl=context.getTerminalNode(probFalse);

			}
		    else{
		    	Polynomial probTrue,probFalse;
				probTrue=(Polynomial)context.getValuePolyForStateInContext((Integer)cpt_a_xiprime,state,xiprime,true);////new method Karina
				Polynomial polynomial1= new Polynomial(1.0,new Hashtable(),context);
				probFalse= polynomial1.subPolynomial((Polynomial)probTrue);
				Fh=context.getTerminalNode(probTrue);
				Fl=context.getTerminalNode(probFalse);

		    }
			
			
			//crear ADD con xiprime con probTrue probFalse
			Object newCPT=context.getInternalNode(xiprime, Fh, Fl);
            //context.view(newCPT);
			VPrime = context.apply(VPrime, newCPT, Context.PROD);
			//context.view(VPrime);
			VPrime=context.apply(xiprime, Context.SUMOUT, VPrime);
			//context.view(VPrime);
		}
		
		if(context.workingWithParameterized){//new  Karina
			//TODO: here we inicialize mergeError of the context
			//context.mergeError = mergeError;
			//context.view(VPrime);
			//Important: Here VPrime only have one node
			VPrime=context.doMinCallOverNodes(VPrime,NAME_FILE_CONTRAINTS,this.pruneAfterEachIt); // the parameter is a ParADD and the result is an ADD
			//after this we have currentValuesProb
			//context.workingWithParameterized=false;//para ver el VDD
						
			//context.view(VPrime);
		}
		context.workingWithParameterized=false;//new line Karina
//		at the end of the while the VUpperPrime must be a number only
		return ((TerminalNodeKeyADD)context.getInverseNodesCache().get(VPrime)).getValue();

	}
	private boolean inGoalSet(TreeMap<Integer, Boolean> state) {
		return listGoalStates.contains(state); 
	}
	//private Double getBellErroVGap() {

		//Double maxDiff=(Double) context.apply(VGap, Context.MAXVALUE);
		//Double minDiff=(Double) context.apply(VGap, Context.MINVALUE);
		//return  Math.max(maxDiff.doubleValue(),-minDiff.doubleValue());

	//}

	public static void ResetTimer() {
		timeTrials = System.currentTimeMillis();
	}

	// Get the elapsed time since resetting the timer
	public static long GetElapsedTime() {
		return System.currentTimeMillis() - timeTrials;
	}

	
/////////////////////////////////////////RTDP///////////////////////////////////////////////////////
	public  ArrayList<ArrayList> solveRTDPFac(int maxDepth, long timeOut, long maxUpdates,String typeMDP,String typeSolution, int numTrials, int interval,int numTrialsSimulation, int numberInitialStates,Random randomGenInitial, Random randomGenNextState,MDP myMDPSimulator) {
		NAME_FILE_VALUE=NAME_FILE_VALUE+"_"+typeSolution+".net";//NAME_FILE_VALUE is inicializated in MDP_Fac(...)
		
		Stack<TreeMap<Integer,Boolean>> visited=new Stack<TreeMap<Integer,Boolean>>();
		
		long timeSum=0;
		ResetTimer();
		
		Action actionGreedy=null;

		if (typeSampledRTDPMDPIP == 3)  //callSolver with constraints p_i>=epsilon 
			context.getProbSampleCallingSolver(NAME_FILE_CONTRAINTS_GREATERZERO);

		//Initialize Vu with admissible value function //////////////////////////////////
		//create an ADD with  VUpper=Rmax/1-gamma /////////////////////////////////////////
		double Rmax = context.apply(this.rewardDD, Context.MAXVALUE);
		
		if (this.bdDiscount.doubleValue() == 1)
			maxUpper=Rmax*maxDepth;
		else
			maxUpper=Rmax/(1-this.bdDiscount.doubleValue());
		
		VUpper = context.getTerminalNode(maxUpper);
		
		ArrayList<ArrayList> perf=new ArrayList<ArrayList>();
		contUpperUpdates=0;

		context.workingWithParameterizedBef = context.workingWithParameterized;
		
		for (int trial = 1; trial <= numTrials; trial++){	
			int depth = 0;
			visited.clear();// clear visited states stack

			TreeMap<Integer,Boolean> state=sampleInitialStateFromList(randomGenInitial);

			//do trial //////////////////////////////////
			while (!inGoalSet(state) && (state !=null) && depth < maxDepth){
				depth++;
				visited.push(state);
				
				//this compute maxUpperUpdated and actionGreedy
				actionGreedy = updateVUpper(state); // Here we fill probNature
				
				contUpperUpdates++;
				
				System.out.println("action greedy: " + actionGreedy.getName());
				
				context.workingWithParameterized = context.workingWithParameterizedBef;
				state = chooseNextStateRTDP(state,actionGreedy,randomGenNextState);
				
				System.out.println("next state: "+state);
				flushCachesRTDP(false);
			}
			
			//do optimization
			while (!visited.empty()) {
				state = visited.pop();
				updateVUpper(state);
				contUpperUpdates++;
			}
			
			//New part for simulate the policy //////////////////////////////////
			context.workingWithParameterized = context.workingWithParameterizedBef;
			
			if(trial%interval==0 && !context.workingWithParameterized){
				long time1  = GetElapsedTime();
				timeSum=timeSum+time1;
	            long timeSec=timeSum/1000;
				int nNodes=this.context.contNumberNodes(VUpper);
				
				System.out.println("performing simulation with current value, with number of updates:"+contUpperUpdates);
				//Copy ADD in other MDP only to do simulations, the result must be the same
				//Object VUpper2=myMDPSimulator.createADDFromADD((ContextADD)context,(Integer)VUpper);
				//ArrayList resultSimulation=myMDPSimulator.simulateMDPFromADD(numberInitialStates,numTrialsSimulation,maxDepth,VUpper2,typeSolution, randomGenInitial,randomGenNextState);
				
				ArrayList resultSimulation=simulateMDPFromADD(numberInitialStates,numTrialsSimulation,maxDepth,VUpper,typeSolution, randomGenInitial,randomGenNextState,null,null);
				
				resultSimulation.add(timeSec);
				resultSimulation.add(nNodes);
				resultSimulation.add(trial);
				resultSimulation.add(contUpperUpdates);
				perf.add(resultSimulation);
				
			}
			////////////////////////////////////////////////////////////////////
			ResetTimer();
		}

		return perf;
	}
	
	public  ArrayList<Object[]> solveRTDPIPFac(int maxDepth, long timeOut,  
			String typeSolution, int numTrials, int interval, int numberInitialStates, 
			Random randomGenInitial, Random randomGenNextState) {
		NAME_FILE_VALUE=NAME_FILE_VALUE+"_"+typeSolution+".net";
		
		Stack<TreeMap<Integer,Boolean>> visited = new Stack<TreeMap<Integer,Boolean>>();
		
		long timeSum=0;
		ResetTimer();
		
		if (typeSampledRTDPMDPIP == 3)  //callSolver with constraints p_i>=epsilon 
			context.getProbSampleCallingSolver(NAME_FILE_CONTRAINTS_GREATERZERO);

		//Initialize Vu with admissible value function //////////////////////////////////
		//create an ADD with  VUpper=Rmax/1-gamma /////////////////////////////////////////
		double Rmax = context.apply(this.rewardDD, Context.MAXVALUE);
		
		if (this.bdDiscount.doubleValue() == 1)
			maxUpper = Rmax * maxDepth;
		else
			maxUpper = Rmax / (1 - this.bdDiscount.doubleValue());
		
		VUpper = context.getTerminalNode(maxUpper);
		
		ArrayList<ArrayList> perf=new ArrayList<ArrayList>();
		contUpperUpdates=0;

		context.workingWithParameterizedBef = context.workingWithParameterized;
		
		for (int trial = 1; trial <= numTrials; trial++){	
			int depth = 0;
			visited.clear();// clear visited states stack
			
			TreeMap<Integer,Boolean> state = sampleInitialStateFromList(randomGenInitial); 

			//do trial //////////////////////////////////
			while (!inGoalSet(state) && (state !=null) && depth < maxDepth){
				depth++;
				visited.push(state);
				
				//this compute maxUpperUpdated and actionGreedy
				Action greedyAction = updateVUpper(state); // Here we fill probNature
				
				contUpperUpdates++;
				
				System.out.println("action greedy: " + greedyAction.getName());
				
				context.workingWithParameterized = context.workingWithParameterizedBef;
				state = chooseNextStateRTDP(state, greedyAction, randomGenNextState);
				
				System.out.println("next state: " + state);
				flushCachesRTDP(false);
			}
			
			//do optimization
			while (!visited.empty()) {
				state = visited.pop();
				updateVUpper(state);
				contUpperUpdates++;
			}
			
			////////////////////////////////////////////////////////////////////
			ResetTimer();
		}
		
		ArrayList<Object[]> result = new ArrayList<Object[]>();
		
		context.workingWithParameterized = false;
		
		for (TreeMap<Integer, Boolean> state : listInitialStates) {		
			double value = (Double) context.getValueForStateInContext((Integer) this.VUpper, state, 1, true);			
			result.add(new Object[] { state, value });
		}
		
		return result;
	}
	
	public void dumpADDtoFile(Object V){
		System.out.println("Dumping Value");
		context.dump(context.remapIdWithOutPrime(V, hmPrime2IdRemap),NAME_FILE_VALUE);		
	}


	public void flushCachesRTDP(boolean workWithEnum) {
		if (((double)RUNTIME.freeMemory() / 
				(double)RUNTIME.totalMemory()) > FLUSH_PERCENT_MINIMUM) {
			return; // Still enough free mem to exceed minimum requirements
		}
		System.out.println("Before flush,freeMemory: "+RUNTIME.freeMemory());

		context.clearSpecialNodes();
		Iterator i = mName2Action.values().iterator();
		while (i.hasNext()) {
			Action a = (Action)i.next();
			Iterator j = a.hsTransADDs.iterator(); //list of ADDs  in action
			while (j.hasNext()) {
				context.addSpecialNode(j.next());
			}
		}
		context.addSpecialNode(rewardDD);
		if (!workWithEnum){
			context.addSpecialNode(VUpper); 
		}
		context.flushCaches();
		System.out.println("After flush,freeMemory: "+RUNTIME.freeMemory());
	}


	private TreeMap<Integer, Boolean> chooseNextStateRTDP(TreeMap<Integer, Boolean> state, Action actionGreedy,Random randomGenerator) {
		return sampling(state,actionGreedy.tmID2ADD, randomGenerator);
	}

	private TreeMap<Integer, Boolean> sampling(TreeMap<Integer, Boolean> state, TreeMap iD2ADD, Random randomGenerator) {
		TreeMap<Integer, Boolean> nextState=new TreeMap<Integer, Boolean>();
		for (int i=1; i<= this.numVars;i++){
			double ran=randomGenerator.nextDouble();
			Integer varPrime=Integer.valueOf(i);
			Integer var=Integer.valueOf(varPrime+this.numVars);
			Object cpt_a_xiprime=iD2ADD.get(varPrime);
			double probFalse;
			if(cpt_a_xiprime==null){
				System.out.println("Prime var not found");
				System.exit(1);
			}
			
			if(!context.workingWithParameterized){ //new line Karina
				probFalse=(Double)context.getValuePolyForStateInContext((Integer)cpt_a_xiprime,state,varPrime,false);
				//double probFalse=1-probTrue;
				if (ran<=probFalse){
					nextState.put(var,false);  		
				}
				else{
					nextState.put(var,true);  	
				}
			}
			else{//new part for IP
					Polynomial probFalsePol=(Polynomial)context.getValuePolyForStateInContext((Integer)cpt_a_xiprime,state,varPrime,false);
					System.out.println(""+probFalsePol.toString(context, "p"));
					probFalse=samplingOneVariableIP(probFalsePol);
					if (ran<=probFalse){
						nextState.put(var,false);  		
					}
					else{
						nextState.put(var,true);  	
					}
			}
		}
		return nextState;
	}
	
	private double samplingOneVariableIP(Polynomial probFalsePol) {
		// typeSampledRTDPMDPIP   1: allProbabilities 2: if p=0  => p=epsilon 3: using  the result of a problem with constraints p>= epsilon 
		double probFalse;
		
		if(typeSampledRTDPMDPIP==1 || typeSampledRTDPMDPIP==2 ){    
            if(probNature.size()!=0){ //i.e. the solver was  called when we have computed Q
            	probFalse=probFalsePol.evalWithListValues(probNature, context);
            	System.out.println("probNature:"+probNature);
            }
            else{ //probNature was not created, then call the solver for each variable s.t.constraints
            	//PADD with only one node
            	Object probFalseNode =(Object)context.doMinCallOverNodes(context.getTerminalNode(probFalsePol),NAME_FILE_CONTRAINTS,this.pruneAfterEachIt);
            	probFalse=((TerminalNodeKeyADD)context.getInverseNodesCache().get(probFalseNode)).getValue();
            }
            if (typeSampledRTDPMDPIP==2 && probFalse==0){ //OPTION 2 
            	probFalse=epsilon;
            }
            
		}
        else{// OPTION 3
//        	TODO:  
        	probFalse=probFalsePol.evalWithListValues(context.probSample, context);
        }
		return probFalse;
	}

	
	//////////////////////////////////////////////////////////////FOR SIMULATING MDPs ///////////////////////////////////////////////////////////////////////////////////////////////////////////
	public ArrayList simulateMDPFromFile(int numberInitialStates,int numberSamples,int tMax, String NAME_FILE_VALUE,String typeSolution) {
		ArrayList res=new ArrayList();
		Object valueRes=context.readValueFunction(NAME_FILE_VALUE);
		//context.view(valueRes);
		TreeMap action2QDD=calculateQHash(valueRes,false); //here we dont call the solver because we are working with an MDP
		ArrayList performancelist=new ArrayList();
		double totalPerformance=0;
		for(int nSeed=1; nSeed <= NUMBEROFSEEDS; nSeed++){
			Random randomGenInitial=new Random(System.currentTimeMillis() + nSeed);
			Random randomGenNextState=new Random(System.currentTimeMillis() + nSeed);
		    double performance=getPerformance(randomGenInitial, randomGenNextState,numberInitialStates,numberSamples,tMax, action2QDD);	
			performancelist.add(performance);
			totalPerformance=totalPerformance+performance;
			//System.out.println("Values: "+listReward);
		}
		double mean=totalPerformance/performancelist.size();
		double sigma=calculateStandarD(mean,performancelist);
		double standardError=sigma/Math.sqrt(performancelist.size());
		res.add(mean);
		res.add(standardError);
		res.add(sigma);
		System.out.println("mean:"+mean);
		System.out.println("SE:"+standardError);
		System.out.println("SD:"+sigma);
		return res;
	}
	

	public ArrayList simulateMDPFromADD(int numberInitialStates,int numberSamples,int tMax, Object valueRes, String typeSolution, Random randomGenInitial, Random randomGenNextState,Object otherValueRes, Object valueGap) {
		TreeMap action2QDD=calculateQHash(valueRes,false); //here we dont call the solver because we are working with an MDP
		//TODO:
		return getPerformanceOneLearnRun(randomGenInitial, randomGenNextState,numberInitialStates,numberSamples,tMax, action2QDD, valueRes,otherValueRes,valueGap);
	}
	
		
	private ArrayList getPerformanceOneLearnRun(Random randomGenInitial, Random randomGenNextState, int numberInitialStates, int numberSamples,int tMax, TreeMap action2QDD, Object valueRes, Object otherValueRes, Object valueGap) {
		ArrayList listReward=new ArrayList();
		double sumListReward=0;
		for(int nI=1; nI<=numberInitialStates; nI++){
			TreeMap<Integer, Boolean> initialState=null;
			if(!sampleInitialFromI){
				System.out.println("NOT IMPLEMENTED");
			}
			else{
				initialState=this.sampleInitialStateFromList(randomGenInitial);	
			}
			TreeMap<Integer, Boolean> state=null;
			double sumReward=0;
			for ( int numS=1; numS<=numberSamples; numS++){
				state=new TreeMap<Integer, Boolean>(initialState);
				//System.out.println("Initial state:"+state);
				double RewardInitialState=getRewardTrialNew(state,tMax,action2QDD,randomGenNextState);
				//System.out.println("reward State:"+RewardInitialState);
				listReward.add(RewardInitialState);
				sumListReward=sumListReward+RewardInitialState;
			}
			this.flushCachesSimulator(action2QDD,true, valueRes,otherValueRes, valueGap);
		}
		double mean=sumListReward/listReward.size();
		double sigma=calculateStandarD(mean,listReward);
		double standardError=sigma/Math.sqrt(listReward.size());
		ArrayList res=new ArrayList();
		res.add(mean);
		res.add(standardError);
		res.add(sigma);
		System.out.println("mean:"+mean);
		System.out.println("SE:"+standardError);
		System.out.println("SD:"+sigma);
		return res;
	}
	
	private double getPerformance(Random randomGenInitial, Random randomGenNextState, int numberInitialStates, int numberSamples,int tMax, TreeMap action2QDD) {
	    ArrayList listReward=new ArrayList();
		double sumListReward=0;
		for(int nI=1; nI<=numberInitialStates; nI++){
			HashMap initialState;
			if(!sampleInitialFromI){
				initialState=sampleInitialState(randomGenInitial);
			}
			else{
				initialState=this.sampleInitialStateFromListIntVal(randomGenInitial);	
			}
			HashMap state=null;
			double sumReward=0;
			for ( int numS=1; numS<=numberSamples; numS++){
				state=new HashMap(initialState);
				//System.out.println("Initial state:"+state);
				double RewardInitialState=getRewardTrial(state,tMax,action2QDD,randomGenNextState);
				//System.out.println("reward State:"+RewardInitialState);
				listReward.add(RewardInitialState);
				sumListReward=sumListReward+RewardInitialState;
			}
			
		}
		double performance=sumListReward/listReward.size();
		//System.out.println("Performance: :"+performance);		
		return performance;
	}
	private double getRewardTrial(HashMap state, int tMax, TreeMap action2QDD, Random randomGenNextState) {
		double RewardInitialState=getReward(state);
		double cur_discount = 1d;
		for(int t=1;t<=tMax;t++){
			Action aBest=findBestA(state,action2QDD);//this is the work of the agent
			//System.out.println("action:"+aBest.getName());
			if (aBest==null){
				System.out.println("Some problem finding best action");
				System.exit(0);
			}
			HashMap nextState=chooseNextState(state,aBest,false,randomGenNextState);//this is the work of the simulator
			state=remapWithOutPrimes(nextState);
			if (printTrafficFormat){
				System.out.println("state: "+getTrafficString(state));
			}
						
			cur_discount *= this.bdDiscount.doubleValue();
			RewardInitialState=RewardInitialState + cur_discount*getReward(state);
		}
		//flushCachesSimulator(action2QDD);
		return RewardInitialState;
		
	}
	private double getRewardTrialNew(TreeMap<Integer, Boolean> state, int tMax, TreeMap action2QDD, Random randomGenNextState) {
		double RewardInitialState=context.getValueForStateInContext((Integer)this.rewardDD, state, null, null);
		double cur_discount = 1d;
		for(int t=1;t<=tMax;t++){
			Action aBest=findBestANew(state,action2QDD);
			//System.out.println("action:"+aBest.getName());
			if (aBest==null){
				System.out.println("Some problem finding best action");
				System.exit(0);
			}
			state=sampling(state,aBest.tmID2ADD, randomGenNextState);
		
			cur_discount *= this.bdDiscount.doubleValue();
			RewardInitialState=RewardInitialState + cur_discount*context.getValueForStateInContext((Integer)this.rewardDD, state, null, null);
			/*if(inGoalSet(state)){
				return RewardInitialState;
			}*/
		}
		return RewardInitialState;
	}
	////////////////////////////FOR MULTILINEAR PROGRAMMING MP /////////////////////////////////////////////////
	public void solveMP() {
		//NAME_FILE_MP_AMPL=NAME_FILE_MP_AMPL+".mod";//NAME_FILE_MP_AMPL is inicializated in MDP_Fac(...
		
		//compute the backprojection g_i^a and c_i^a
   		//iterate over each action
		Iterator actions=mName2Action.entrySet().iterator();
		while (actions.hasNext()){
			Map.Entry meaction=(Map.Entry) actions.next();
			Action action=(Action) meaction.getValue();
			System.out.println("  - Compute c for action: " + action.getName());
			//context.workingWithParameterized=context.workingWithParameterizedBef;
			ArrayList ciList=new ArrayList();
			//add the constant basis function in the beginning of the ci list
			Polynomial ONEPOLY=new Polynomial(1d*this.bdDiscount.doubleValue()-1d,new Hashtable(),null);
			ciList.add(context.getTerminalNode(ONEPOLY));
			//for each basis function calculate g_i_a
			for(int i=0;i<listBasisFunctions.size();i++){
			
				//Object base=listBasisFunctions.get(i);
				//context.view(base);
				Object giaDD=computeGiaction(i,action,action.tmID2ADD);
				//context.view(giaDD);
				context.workingWithParameterized=true;
				Object ygiaDD=context.apply(context.getTerminalNode(this.bdDiscount.doubleValue()),giaDD,  Context.PROD);
				Object ciaDD=context.apply(ygiaDD,listBasisFunctions.get(i), Context.SUB);
				
				ciList.add(ciaDD);
    		}
            action.setciList(ciList);  			
			flushCachesMP();			
		}
		//compute the coefficients of the objective function
        Polynomial polObj=computeObjective();
        
        	
		//create the ampl file with the smaller set of constraints
        contConstraintsMP=numOriginalConstraints;
        this.nameNewVariables=new HashSet();
        //create constraints for the reward  and c0 that not depend on the action
        Hashtable<String, HashSet>var2ListVar=new Hashtable<String,HashSet>();
        createEqualConsRewardC0(var2ListVar);
        addNewVarRewardAllActions(var2ListVar);
        //create the equivalent set of constraints
		Iterator actionsI=mName2Action.entrySet().iterator();
		int contAction=0;
		while (actionsI.hasNext()){
			Map.Entry meaction=(Map.Entry) actionsI.next();
			Action action=(Action) meaction.getValue();
			System.out.println("  - Writing equivalent constraints that depend on w for action: " + action.getName());
			writeEquivalentConstraints(action,contAction);
			contAction++;
		}
		//write all in a file
		context.createFileAMPLMP(polObj.toString(context,""),NAME_FILE_CONTRAINTS_MP,NAME_FILE_CONTRAINTS,listBasisFunctions,nameNewVariables);
		//call solver
		 Double obj=context.callNonLinearSolver();// fill   currentValuesProb and currentValuesW
		 if (obj==null){
			 System.out.println("doMinCallOverNodes: Problems with the solver it return null");
			 System.exit(0);
		 }
		//create ADD
		 
		 //context.workingWithParameterized=false;
		 
		 Double valW0=(Double)context.currentValuesW.get("0");
		 Polynomial polW0=new Polynomial(valW0,new Hashtable(),context);
		 valueWHDD=context.getTerminalNode(polW0);//get the value of w0
		 
		for(int i=1;i<=listBasisFunctions.size();i++){
			    Double valW=(Double)context.currentValuesW.get(Integer.toString(i));
			    //context.view(listBasisFunctions.get(i-1));
				Object wHDD=context.apply(context.getTerminalNode(valW),listBasisFunctions.get(i-1) ,Context.PROD);
				
				valueWHDD=context.apply(valueWHDD,wHDD ,Context.SUM);
		}
		//print sumWHDD
		if(printFinalADD){
	    	  context.view(valueWHDD);
	   	}
		
		//dump solution
		dumpValue=true;
		if(dumpValue && this.typeContext==1){ 
			  System.out.println(NAME_FILE_VALUE);
	    	  context.dump(valueWHDD,NAME_FILE_VALUE);
	    }
	}
	
	private void addNewVarRewardAllActions(Hashtable<String, HashSet> var2ListVar) {
		Iterator actionsI=mName2Action.entrySet().iterator();
		while (actionsI.hasNext()){
			Map.Entry meaction=(Map.Entry) actionsI.next();
			Action action=(Action) meaction.getValue();
			action.var2ListVar=new Hashtable(var2ListVar);
		}
	}
	private void createEqualConsRewardC0(Hashtable<String, HashSet>var2ListVar) {
	   	try {
            BufferedWriter out = new BufferedWriter(new FileWriter(NAME_FILE_CONTRAINTS_MP));
            for(int i=0;i<listFactoredReward.size();i++){
            	contConstraintsMP++;
            	HashSet listVarPath=new HashSet();
            	createLinesFileConstRewC0MP((Integer)listFactoredReward.get(i),out,"", i+1,listVarPath,var2ListVar,"R");
            	
            }
            //For  C_0 (related to the constant basis function)
        	Iterator actionsI=mName2Action.entrySet().iterator();
    		Map.Entry meaction=(Map.Entry) actionsI.next();
    		Action action=(Action) meaction.getValue();
    		HashSet listVarPath=new HashSet();           
            createLinesFileConstRewC0MP((Integer)action.ciList.get(0),out,"", 0,listVarPath,var2ListVar,"c");
            out.close();
        } catch (IOException e) {
        	System.out.println("Problem with the Constraint file");
        	System.exit(0);
        }
        
		
	}
	
		
    public void createLinesFileConstRewC0MP(Integer F, BufferedWriter out, String path,int i,HashSet<Integer> listVarPath,Hashtable<String, HashSet>var2ListVar,String type){
    	  
    	if(context.isTerminalNode(F)){
    		String line;
    		if(context.getInverseNodesCache().get(F) instanceof TerminalNodeKeyADD){
    			double val=((TerminalNodeKeyADD)context.getInverseNodesCache().get(F)).getValue();
    			path="u"+type+i+path;
    			line="subject to r"+contConstraintsMP+":  "+path+"="+Context._df.format(val)+";";
    		}
    		else{
        		Polynomial pol=((TerminalNodeKeyPar)context.getInverseNodesCache().get(F)).getPolynomial();
        		path="u"+type+i+path;
	         	line="subject to r"+contConstraintsMP+":  "+path+"= ("+pol.toString(context,"p")+") * w"+i+";";
    		}
         	contConstraintsMP++;
        	try {
				out.write(line);
				out.append(System.getProperty("line.separator"));
			} catch (IOException e) {
		       	System.out.println("Problem with the Constraint file");
	        	System.exit(0);
				e.printStackTrace();
			}
			var2ListVar.put(path,listVarPath);
        	listVarPath=new HashSet();
        	this.nameNewVariables.add(path);
            return;  		
 
    	}
   		InternalNodeKeyADD intNodeKey=(InternalNodeKeyADD)context.getInverseNodesCache().get(F);
   		Integer Fvar= intNodeKey.getVar();
   		listVarPath.add(Fvar);
   		createLinesFileConstRewC0MP(intNodeKey.getHigh(),out,path+"_1x"+Fvar,i,listVarPath,var2ListVar,type);
   		createLinesFileConstRewC0MP(intNodeKey.getLower(),out,path+"_0x"+Fvar,i,listVarPath,var2ListVar,type);
   		
    	
    }
	
	private void writeEquivalentConstraints(Action action,int contAction) {
		
		//TODO: working in it 
		
        createEqualConsCi(action,contAction);
        createInequalityCons(action,contAction);
        
	}
	private void createInequalityCons(Action action, int contAction) {
	  //TODO: working on it	
	  //iterate over each variable x_i (we are eliminating x_i)
		ArrayList<String> listVarNewDep=new ArrayList();
		for (int i=1+this.numVars; i<= 2*this.numVars;i++){
			HashSet listVarZ=new HashSet();
			listVarNewDep=getListVarDepXiAndErase(i,listVarZ,action);
			//for each configuration of the eliminated variable x_i
			//for each configuration of variables in Z create a new constraint
			for(int j=0;j<=1;j++){
			    for(int k=0;k<Math.pow(2.0, listVarZ.size());k++){
			       TreeMap conf=getConfiguration(k,listVarZ);
			       String newVarUe=computeNewVarUe(conf,i,contAction); //must be before include the eliminated variable
			       conf.put(i,j);//add the configuration for the eliminated variable x_i
			       String rightSide=computeRightSideConst(listVarNewDep,conf,contAction);
			   	   createInequalConst(newVarUe,rightSide);
				   //agregate the new variable in the list of variable new of the action and eliminate the other ones
			   	   action.var2ListVar.put(newVarUe,listVarZ);
			   	   this.nameNewVariables.add(newVarUe);
			    }
			}
		}	
		//include the constraints  for the functions of empty scope
		String rightSideEmpty=computeRightSideEmpty(action.var2ListVar.keySet());
		createInequalConst("0",rightSideEmpty);

	}


	private String computeRightSideEmpty(Set<String> listVarNewDep) {
      String right="";
      Iterator it=listVarNewDep.iterator();
      while(it.hasNext()){
		  right=right+it.next()+"+";
      }
	  
	  return right.substring(0, right.length()-1);
	}
	private void createInequalConst(String newVarUe, String rightSide) {
		try {
            BufferedWriter out = new BufferedWriter(new FileWriter(NAME_FILE_CONTRAINTS_MP,true));
            String line="subject to r"+contConstraintsMP+":  "+newVarUe+" >= "+rightSide+";";
			out.write(line);
			out.append(System.getProperty("line.separator"));
			out.close();
			contConstraintsMP++;
        } catch (IOException e) {
        	System.out.println("Problem with the Constraint file");
        	System.exit(0);
        }
	}
	private String computeRightSideConst(ArrayList<String> listVarNewDep, TreeMap conf,int contAction) {
		String rightSide="";
		Iterator it=listVarNewDep.iterator();
		while (it.hasNext()){
			String varNew=(String)it.next();
			boolean found=true;
			Iterator it2=conf.keySet().iterator();
			while(it2.hasNext() && found){
				Integer var=(Integer)it2.next();
				String valVar=conf.get(var)+"x"+var;
				if(varNew.contains("x"+var+"_")||varNew.endsWith("x"+var+"a"+contAction)||varNew.endsWith("x"+var)){
					if(!(varNew.contains(valVar+"_")||varNew.endsWith(valVar+"a"+contAction)|| varNew.endsWith(valVar) )){
								found=false;
					}
				}
			}
			if (found){
				rightSide=rightSide+varNew+"+";
			}
			
		}
		//System.out.print(rightSide.length());
		if(rightSide.length()==0){
			return rightSide;
		}
		return rightSide.substring(0, rightSide.length()-1);
	}
	private String computeNewVarUe(TreeMap conf,int i,int contAction) {
		String newVarUe="ue"+i;
		Iterator it=conf.keySet().iterator();
		while (it.hasNext()){
			Integer var=(Integer)it.next();
			newVarUe=newVarUe+"_"+conf.get(var)+"x"+var;
		}
		return newVarUe+"a"+contAction;
	}
	private TreeMap getConfiguration(int pos, HashSet listVarZ) {
		TreeMap conf= new TreeMap();
		Iterator it=listVarZ.iterator();
		while(it.hasNext()){
		   conf.put(it.next(),pos%2);
		   pos=pos/2;         
		}
		return conf;
	}
	public int[] getAssigment(int pos, int numberVariables) {
		int  assigment[]= new int[numberVariables];
		for(int j=numberVariables-1;j>=0;j--){
		   assigment[j]= pos%2;
		   pos=pos/2;         
		}
		return assigment;
	}
	
	
	private ArrayList<String> getListVarDepXiAndErase(int i, HashSet listVarZ,Action action) {
		//TODO: working on it, revisar con un ejemplo si esto esta funcionando bien porque estoy eliminando elementos enquanto recorro var2ListVar
		ArrayList<String> listVarNewDep=new ArrayList<String>();
		Iterator it=action.var2ListVar.keySet().iterator();
		while(it.hasNext()){
			String varNewU=(String)it.next();
			HashSet<Integer> list=action.var2ListVar.get(varNewU);
			if(list.contains(i)){
				listVarNewDep.add(varNewU);
				//list.remove(i);
				listVarZ.addAll(list);
		
			}
		}
		//I am doing the remove after that because problems of concurrent modification
		for (int j=0;j<listVarNewDep.size();j++){
			action.var2ListVar.remove(listVarNewDep.get(j));
		}
		listVarZ.remove(i);
		return listVarNewDep;
	}
	private void createEqualConsCi(Action action, int contAction) {
	   	try {
            BufferedWriter out = new BufferedWriter(new FileWriter(NAME_FILE_CONTRAINTS_MP,true));
			out.write("#Constraints related to "+ action.getName()+ " action");
			out.append(System.getProperty("line.separator"));
            for(int i=1;i<action.ciList.size();i++){
            	contConstraintsMP++;
            	HashSet listVarPath=new HashSet();
            	createLinesFileConstCiMP((Integer)action.ciList.get(i),out,"", i,contAction,listVarPath,action);
              }
            out.close();
        } catch (IOException e) {
        	System.out.println("Problem with the Constraint file");
        	System.exit(0);
        }
		
	}
	
	   public void createLinesFileConstCiMP(Integer F, BufferedWriter out, String path,int i,int contAction,HashSet listVarPath,Action action){
	    	  
	    	if(context.isTerminalNode(F)){
	    		Polynomial pol=((TerminalNodeKeyPar)context.getInverseNodesCache().get(F)).getPolynomial();
	    		path="uc"+i+path+"a"+contAction;
	         	String line="subject to r"+contConstraintsMP+":  "+path+"= ("+pol.toString(context,"p")+") * w"+i+";";
	         	contConstraintsMP++;
	        	try {
					out.write(line);
					out.append(System.getProperty("line.separator"));
				} catch (IOException e) {
			       	System.out.println("Problem with the Constraint file");
		        	System.exit(0);
					e.printStackTrace();
				}
	        	
	        	this.nameNewVariables.add(path);
	        	action.var2ListVar.put(path,listVarPath);
	        	listVarPath=new HashSet();
	            return;  		
	 
	    	}
	   		InternalNodeKeyADD intNodeKey=(InternalNodeKeyADD)context.getInverseNodesCache().get(F);
	   		Integer Fvar= intNodeKey.getVar();
	   		listVarPath.add(Fvar);
	   		createLinesFileConstCiMP(intNodeKey.getHigh(),out,path+"_1x"+Fvar,i,contAction,listVarPath,action);
	   		createLinesFileConstCiMP(intNodeKey.getLower(),out,path+"_0x"+Fvar,i,contAction,listVarPath,action);
	   		
	    	
	    }
	

	private Polynomial computeObjective() {
		String varWeight="w";
		//and for the constant basis function
		Polynomial CONSTANTPOLY=new Polynomial(Math.pow(2, this.numVars),new Hashtable(),null);
		
		Object sumWHDD=context.getTerminalNode(new Double(0.0));
		for(int i=1;i<=listBasisFunctions.size();i++){
			Object wHDD=context.apply(context.getTerminalNode(varWeight+i,1.0),listBasisFunctions.get(i-1) ,Context.PROD);
			sumWHDD=context.apply(sumWHDD,wHDD ,Context.SUM);
			context.listVarWeight.add(varWeight+i);
		}
		for(int i=1; i<= this.numVars;i++){
			Integer varPrime=Integer.valueOf(i);
			Integer var=Integer.valueOf(varPrime+this.numVars);
			sumWHDD=context.apply(var, Context.SUMOUT, sumWHDD);
		}
		Object obj=context.apply(context.getTerminalNode(varWeight+0,Math.pow(2, this.numVars)),sumWHDD ,Context.SUM);
		
		Polynomial polObj=((TerminalNodeKeyPar)context.getInverseNodesCache().get(obj)).getPolynomial();
		return polObj;
		
	}
	public void flushCachesMP() {
		//save CPTs, reward basis function and ciList for each action
		if (((double)RUNTIME.freeMemory() / 
		     (double)RUNTIME.totalMemory()) > FLUSH_PERCENT_MINIMUM) {
		    return; // Still enough free mem to exceed minimum requirements
		}
		System.out.println("Before flush,freeMemory: "+RUNTIME.freeMemory());

		context.clearSpecialNodes();
		Iterator i = mName2Action.values().iterator();
		while (i.hasNext()) {
		    Action a = (Action)i.next();
		    Iterator j = a.hsTransADDs.iterator(); //list of ADDs  in action
		    while (j.hasNext()) {
			 context.addSpecialNode(j.next());
		    }
		    for(int k=0;k<a.ciList.size();k++){
				context.addSpecialNode(a.ciList.get(k));
			}
		}
		context.addSpecialNode(rewardDD);
		for(int k=0;k<listBasisFunctions.size();k++){
			context.addSpecialNode(listBasisFunctions.get(k));
		}
		context.flushCaches();
		System.out.println("After flush,freeMemory: "+RUNTIME.freeMemory());
   }

	private Object computeGiaction(int i, Action action,TreeMap iD2ADD) {
		//convert basis function with primes
		 
		Object ciDD=listBasisFunctions.get(i);
		ciDD=context.remapIdWithPrime(ciDD,this.hmPrimeRemap);
		//context.view(ciDD);
		Integer xiprime;
		Iterator x=this.hmPrimeRemap.entrySet().iterator();
		while (x.hasNext()){

			Map.Entry xiprimeme = (Map.Entry)x.next();
			xiprime=(Integer) xiprimeme.getValue();
			Object cpt_a_xiprime=iD2ADD.get(xiprime);
			//context.view(cpt_a_xiprime);
			ciDD = context.apply( ciDD,cpt_a_xiprime, Context.PROD);
			ciDD=context.apply(xiprime, Context.SUMOUT, ciDD);
			//context.view(ciDD);
		}
		return ciDD;
	}	
	
/////////////////////////////////////////Enumerative RTDP ///////////////////////////////////////////////////////
	public  ArrayList<ArrayList> solveRTDPEnum(int maxDepth, long timeOut, long maxUpdates,String typeMDP,String typeSolution, int numTrials, int interval,int numTrialsSimulation, int numberInitialStates,Random randomGenInitial, Random randomGenNextState,MDP myMDPSimulator) {	
	    System.out.println("In solveRTDPEnum ");
		NAME_FILE_VALUE=NAME_FILE_VALUE+"_"+typeSolution+".net";//NAME_FILE_VALUE is inicializated in MDP_Fac(...)
		Stack<State> visited=new Stack<State>();
		long timeSum=0;
		ResetTimer();
		Action actionGreedy=null;
		//Initialize Vu with admissible value function
		double Rmax=context.apply(this.rewardDD, Context.MAXVALUE);
		if(this.bdDiscount.doubleValue()==1){
			maxUpper=Rmax*maxDepth;
		}
		else{
			maxUpper=Rmax/(1-this.bdDiscount.doubleValue());
		}
		VUpper=new HashMap<State,Double>();
		ArrayList<ArrayList> perf=new ArrayList<ArrayList>();
		contUpperUpdates=0;
		//do trials until convergence or timeOut (in practice we dont check convergence )
		//while (contUpperUpdates<maxUpdates){//timeSeg<timeOut){ not using timeOut only for test
		for(int trial=1; trial <=numTrials;trial++){	
			int depth=0;
			visited.clear();// clear visited states stack
			states.clear(); // error found by Scott & student
			//draw initial state
			State state=sampleInitialStateFromListEnum(randomGenInitial);
			//System.out.println("initial state: "+state);
			//do trial
			while(!inGoalSetS(state) && (state !=null)&& depth<maxDepth){ //&& contUpperUpdates<maxUpdates){ //not update more than maxUpdates in the last iteration
				depth++;
				visited.push(state);
				//this compute maxUpperUpdated and actionGreedy
				actionGreedy=updateVUpper(state);
				//context.view(VUpper);
				contUpperUpdates++;
				//System.out.println("action greedy: "+actionGreedy.getName());
				state=chooseNextStateRTDPEnum(state,actionGreedy,randomGenNextState,posActionGreedy);
				//System.out.println("next state: "+state);
				flushCachesRTDP(true);
			}
			//do optimization
			while(!visited.empty()){
				state=visited.pop();
				updateVUpper(state);
				contUpperUpdates++;
			}
			//New part for simulate the policy //////////////////////////////////
			if(trial%interval==0){
				long time1  = GetElapsedTime();
				timeSum=timeSum+time1; 
				long timeSec=timeSum/1000;
				int nNodes=((HashMap)VUpper).size();
				System.out.println("performing simulation with current value, with number of updates:"+contUpperUpdates);
				Object VADD=createADDFromVHashMapEnum(myMDPSimulator.context,(HashMap)VUpper,true); //it is important because we need an ADD context and not TABLE context 
				ArrayList resultSimulation=myMDPSimulator.simulateMDPFromADD(numberInitialStates,numTrialsSimulation,maxDepth,VADD,typeSolution, randomGenInitial,randomGenNextState,null,null);
				resultSimulation.add(timeSec);
				resultSimulation.add(nNodes);
				resultSimulation.add(trial);
				resultSimulation.add(contUpperUpdates);
				perf.add(resultSimulation);
			}
			////////////////////////////////////////////////////////////////////
            ResetTimer(); 
		}
		if(printFinalADD){
			System.out.println(VUpper.toString());
		}
		return perf;
	}	


public void dumpVHashtoADDtoFile(HashMap V,boolean upper) {
	Context contextADD=new ContextADD();
	System.out.println("Dumping V ");
	Object VADD=createADDFromVHashMapEnum(contextADD,V,upper);
	contextADD.dump(contextADD.remapIdWithOutPrime(VADD, hmPrime2IdRemap),NAME_FILE_VALUE);	
}


private Object createADDFromVHashMapEnum(Context contextADD,HashMap V,boolean upper) {
	Object VADD;
	if (upper){
	   VADD=contextADD.getTerminalNode(maxUpper);
	}
	else{
		   VADD=contextADD.getTerminalNode(minLower);
	}
	Iterator it=((HashMap)V).keySet().iterator(); //here V is a HashMap
	while(it.hasNext()){
		State state=(State)it.next();
		Double valueV=(Double) ((HashMap)V).get(state);
		TreeMap <Integer,Boolean> stateTM=state.getValues();
		Iterator iteratorState=stateTM.keySet().iterator();			
		VADD=contextADD.insertValueInDD(VADD, stateTM,  valueV, iteratorState,this.hmPrimeRemap);	
	}
	//contextADD.view(VADD);
	return VADD;
}

/*private Object createADDFromTable(Context contextADD,Object V,boolean upper) {
	Object VADD;
	if (upper){
		   VADD=contextADD.getTerminalNode(maxUpper);
		}
		else{
			   VADD=contextADD.getTerminalNode(minLower);
		}

	Table table=(Table)((ContextTable)contextADD).getInverseNodesCache().get(valueiDD);
	for(int i=0;i<table.getValues().size();i++){
		
		Double valueV=(Double)table.getValues().get(i);
		
		TreeMap <Integer,Boolean> stateTM=getConfigurationTable(i,table.getVars());
		Iterator iteratorState=stateTM.keySet().iterator();
		VADD=contextADD.insertValueInDD(VADD, stateTM,  valueV, iteratorState,this.hmPrimeRemap);	
	}
	//contextADD.view(VADD);
	return VADD;
}*/


private TreeMap <Integer,Boolean>getConfigurationTable(int pos, TreeSet listVar) {
	TreeMap <Integer,Boolean>conf= new TreeMap<Integer,Boolean>();
	Iterator it=listVar.iterator();
	Boolean val;
	while(it.hasNext()){
	   if(pos%2==0){
		   val=false;
	   }
	   else{
		   val=true;
	   }
	   conf.put((Integer)it.next(),val);
	   pos=pos/2;         
	}
	return conf;
}
/* updateVUpper and put the max in maxUpperUpdated 
 * return the actionGreedy
 */
private Action updateVUpper(State state) {
	double max=Double.NEGATIVE_INFINITY;
	Action actionGreedy=null;
	Iterator actions=mName2Action.entrySet().iterator();
	int posAction=0;
	posActionGreedy=-1;
	while (actions.hasNext()){
		Map.Entry meaction=(Map.Entry) actions.next();
		Action action=(Action) meaction.getValue();
		//context.view(VUpper); 
		//System.out.println("  Computing Q Upper action " + action.getName());
		double Qt;
		Qt=this.computeQEnum((HashMap)VUpper, state,action,action.tmID2ADD,'u',posAction);
		//System.out.println(" Qt:" + Qt);
		
		max=Math.max(max,Qt);
		if(Math.abs(max - Qt) <= 1e-10d){//max==Qt){
			actionGreedy=action;
			posActionGreedy=posAction;
		}
		posAction++;
	}
	//context.view(rewardDD);
	double rew=context.getRewardForStateInContextEnum((Integer)this.rewardDD, state,this.numVars);
	double maxTotal=rew+this.bdDiscount.doubleValue()*max;
    //update hash VUpper
	((HashMap)VUpper).put(state,maxTotal);

	maxUpperUpdated=maxTotal;
	return actionGreedy;
}


    private State sampleInitialStateFromListEnum(Random randomGenerator) {
    	int ranIndex=randomGenerator.nextInt(listInitialStatesEnum.size());
		State state=listInitialStatesEnum.get(ranIndex);
        //if state is in the list of states then does not put it again in states         		
		if(states.containsKey(state.getIdentifier())){
			state=states.get(state.getIdentifier());
			return state;
		}
		state.initActionSucc(mName2Action.size());
		states.put(state.getIdentifier(),state);
    	return state;
    }
                
	private TreeMap<Integer, Boolean> sampleInitialStateFromList(Random randomGenerator) {
		int ranIndex=randomGenerator.nextInt(listInitialStates.size());
		return listInitialStates.get(ranIndex);
	}
	
	private boolean inGoalSetS(State state) {
		int idState=state.getIdentifier();
		for(int i=0; i<listGoalStatesEnum.size();i++){
			State stateinList=listGoalStatesEnum.get(i);
			int idGoal=stateinList.getIdentifier();
			if (idState==idGoal){
				return true;
			}
		}
		return false;
	}

	private State chooseNextStateRTDPEnum(State state, Action actionGreedy, Random randomGenerator,int posActionGreedy) {
		SuccProbabilities succState=state.getActionSuccProbab()[posActionGreedy];
		State nextState=null;
		Double value,sum=0d;
		double ran=randomGenerator.nextDouble();
		for(int i=0;i<succState.getNextStates().size();i++){
              nextState=succState.getNextStates().get(i);
              value=succState.getProbs().get(i);
              sum=sum+value;
              if(ran<=sum){
            	  return nextState;
              }
		}
		return nextState;
        
    }
	private double computeQEnum(HashMap V,State state, Action action,TreeMap iD2ADD, char c, int posAction) {
		SuccProbabilities succ=state.getActionSuccProbab()[posAction];
		//if it has not been calculed before, compute it 
        if(succ==null){
        	succ=computeSuccesorsProbEnum(state,iD2ADD);
        	if (succ.getNextStates().size()==0){
        		System.out.println("Not Sucessors for state: "+state);
        		System.exit(1);
        	}
        	state.getActionSuccProbab()[posAction]=succ;
        }
        	
        double res=mulSumSuccessors(succ,V,c);
 		return res;
		//at the end we have a number		
	}
	
	
	
	
	private SuccProbabilities computeSuccesorsProbEnum(State state,TreeMap iD2ADD) {
	       
		Object multCPTs=context.getTerminalNode(1d);
		Integer xiprime;
		Iterator x=this.hmPrimeRemap.entrySet().iterator();
		while (x.hasNext()){
			Map.Entry xiprimeme = (Map.Entry)x.next();
			xiprime=(Integer) xiprimeme.getValue();
			Object cpt_a_xiprime=iD2ADD.get(xiprime);
			//context.view(cpt_a_xiprime);
			double probTrue,probFalse;
			probTrue=context.getProbCPTForStateInContextEnum((Integer)cpt_a_xiprime,state,xiprime,true,this.numVars);
			probFalse=1-probTrue;

			Object Fh=context.getTerminalNode(probTrue);
			Object Fl=context.getTerminalNode(probFalse);
			Object newCPT=context.getInternalNode(xiprime, Fh, Fl);
			//multiply all the new 
			multCPTs = context.apply(multCPTs, newCPT, Context.PROD);
		}
		//context.view(multCPTs);
		return getSuccessors(multCPTs);
		
	}
	/**
	 * calculate Sum_s' P(s'!s,a)V^u(s')
	 * @param succ transition probabilities between s and s'
	 * @param V  V upper or V lower
	 * @param c  type upper=' u' lower='l'
	 * @return
	 */
	private double mulSumSuccessors(SuccProbabilities succ, HashMap V,char c) {
		double sum=0;
		
		State state;
		Double prob;
		for(int i=0;i<succ.getNextStates().size();i++){
			state=succ.getNextStates().get(i);
			prob=succ.getProbs().get(i);
			Double valueV=(Double)V.get(state);
			if (valueV==null){
				if (c=='u'){
					valueV=maxUpper;
				}
				else if (c=='l'){
					valueV=minLower;
				}
				else{
					System.out.println("must be u:upper l:lower");
					System.exit(1);
				}
			}
			sum=sum+prob*valueV;
		}
        return sum;
	}
	/**
	 * it gets all the states with not zero probability 
	 * @param multCPTs
	 * @return
	 */
	private SuccProbabilities getSuccessors(Object multCPTs) {
		double sumProb=0;
		SuccProbabilities succ=new SuccProbabilities(); 
		Table table=(Table)context.getInverseNodesCache().get(multCPTs);
		for (int i=0;i< table.getValues().size();i++){//for all states (with prime)
			Double val=(Double)table.getValues().get(i);
			//if(val.compareTo(0d)!=0){
			if(Math.abs(val.doubleValue() - 0d) > 1e-10d){
				State newState=createStateFrom(i,table.getVars().size()); //create only if state does not exist yet 
				succ.getNextStates().add(newState);
				succ.getProbs().add(val);
				sumProb=sumProb+val;
			}
		}
		if (Math.abs(sumProb - 1d) > 1e-10d){
			System.out.println("Precision error sumProb must be 1 and is: " +sumProb);
		}
		return succ;
	}
	
	
		
	
	
	public State  createStateFrom(int pos, int numberVariables) {
		
		State newState;
		if(states.containsKey(new Integer(pos))){
			newState=states.get(new Integer(pos));
			return newState;
		}
		TreeMap <Integer,Boolean> list=new TreeMap<Integer,Boolean>();
		int  assigment[]=getAssigment(pos,numberVariables);
		for(int i=0;i<assigment.length;i++){
			    if  (assigment[i]==1){
			    	list.put(i+1,true);
		        }
				else{
					list.put(i+1,false);
				}
			    
		}
		newState=new State(list,this.mName2Action.size(),pos);
		states.put(pos, newState);
		return newState;
	
	}
	
	
	
	/////////////////////////////////////////Enumerative BRTDP///////////////////////////////////////////////////////
	public  ArrayList<ArrayList> solveBRTDPEnum(int maxDepth, long timeOut,long maxUpdates,double tau,String typeMDP,String typeSolution, int numTrials, int interval,int numTrialsSimulation, int numberInitialStates,Random randomGenInitial, Random randomGenNextState,MDP myMDPSimulator) {
	    System.out.println("In solveBRTDPEnum");
		NAME_FILE_VALUE=NAME_FILE_VALUE+"_"+typeSolution+".net";//NAME_FILE_VALUE is inicializated in MDP_Fac(...)
		Stack<State> visited=new Stack<State>();
		long timeSum=0;
		ResetTimer();
		Action actionGreedy=null;
		//Initialize Vu and Vl with admissible value function
		//create an ADD with 0 for V_l and V_u=Rmax/1-gamma
		double Rmax=context.apply(this.rewardDD, Context.MAXVALUE);
		double Rmin=context.apply(this.rewardDD, Context.MINVALUE);
		if(this.bdDiscount.doubleValue()==1){
			minLower=Rmin*maxDepth;
			maxUpper=Rmax*maxDepth;
		}
		else{
			minLower=Rmin/(1-this.bdDiscount.doubleValue());
			maxUpper=Rmax/(1-this.bdDiscount.doubleValue());
		}
		VUpper=new HashMap<State,Double>();
		VLower=new HashMap<State,Double>();
		ArrayList<ArrayList> perf=new ArrayList<ArrayList>();
		firstGap=maxUpper-minLower;
		VGap=new HashMap<State,Double>();
        contUpperUpdates=0;
        contbreak=0;
		//do trials until convergence or timeOut
        //while (contUpperUpdates<maxUpdates){//timeSeg<timeOut){ not using timeOut only for test
        for(int trial=1; trial <=numTrials;trial++){
			int depth=0;
			visited.clear();// clear visited states stack
			states.clear();// error found by Scott & student 
			//draw initial state
			State stateInitial=sampleInitialStateFromListEnum(randomGenInitial);
			//System.out.println("state initial: "+stateInitial);
			State state=stateInitial;   
			//do trial
			while(!inGoalSetS(state) && (state !=null)&& depth<maxDepth){// && contUpperUpdates<maxUpdates){ //not update more than maxUpdates in the last iteration
				depth++;
				visited.push(state);
				//this compute maxUpperUpdated and actionGreedy
				actionGreedy=updateVUpper(state);
				contUpperUpdates++;
				//System.out.println("action greedy: "+actionGreedy.getName());
				System.out.println("VUpper: "+VUpper);
				//this compute maxLowerUpdated
				updateVLower(state);
				System.out.println("VLower: "+VLower);
				double gap=maxUpperUpdated-maxLowerUpdated; 
				//Update HashMap
				Double gapBefore=(Double)((HashMap) VGap).get(state);
				if (gapBefore !=null && gapBefore.compareTo(gap)<0){
					System.out.println("some error with gap"+" "+gapBefore+"<"+gap);
					System.exit(1);
				}
				((HashMap) VGap).put(state,gap);
				//System.out.println("VGap: "+VGap);
				state=chooseNextStateBRTDPEnum(state,actionGreedy,randomGenNextState,tau,stateInitial,posActionGreedy);
				//System.out.println("next state: "+state);
				if(state==null){
					contbreak++;
				}
				flushCachesBRTDP(true);
			}
			//do optimization
			while(!visited.empty()){
				state=visited.pop();
				updateVUpper(state);
				contUpperUpdates++;
				updateVLower(state);
			}
			//New part for simulate the policy //////////////////////////////////
			if(trial%interval==0){
				long time1  = GetElapsedTime();
				timeSum=timeSum+time1; 
				long timeSec=timeSum/1000;
				int nNodes=((HashMap)VLower).size();
				System.out.println("performing simulation with current value, with number of updates:"+contUpperUpdates);
				Object VADD=createADDFromVHashMapEnum(myMDPSimulator.context,(HashMap)VLower,false); //it is important because we need an ADD context and not TABLE context (to work with VLower put VLower and false
				ArrayList resultSimulation=myMDPSimulator.simulateMDPFromADD(numberInitialStates,numTrialsSimulation,maxDepth,VADD,typeSolution, randomGenInitial,randomGenNextState,null,null);
				resultSimulation.add(timeSec);
				resultSimulation.add(nNodes);
				resultSimulation.add(trial);
				resultSimulation.add(contUpperUpdates);
				perf.add(resultSimulation);
			}
			////////////////////////////////////////////////////////////////////
            ResetTimer(); 
        }
	   	if(printFinalADD){
	    	  System.out.println(VUpper.toString());
	    }
	    return perf; 
	}	
	private State chooseNextStateBRTDPEnum(State state, Action actionGreedy, Random randomGenerator,double tau,State stateInitial,int posActionGreedy) {
		SuccProbabilities succState=state.getActionSuccProbab()[posActionGreedy];
		//calculate b
		SuccProbabilities  b=multByVGap(succState);
		State nextState=null;
		Double valueb,sum=0d;
		//verify end of trial
		Double gapInitial=(Double)((HashMap)VGap).get(stateInitial);
        if (gapInitial==null){
        	gapInitial=firstGap;
        	System.out.println("It must never happen, because it is after update");
        	System.exit(1);
        }
		if(B< gapInitial/tau){
			return null;
		}
		//sample from distribution b(y)/B
		double ran=randomGenerator.nextDouble();		
		for(int i=0;i<b.getNextStates().size();i++){
              nextState=b.getNextStates().get(i);
              valueb=b.getProbs().get(i);
              sum=sum+valueb/B;
              if(ran<=sum){
            	  return nextState;
              }
		}
		System.out.println("It must never happen, because it must return before");
		return nextState;
    }
	private SuccProbabilities multByVGap(SuccProbabilities  succState) {
		SuccProbabilities  b=new SuccProbabilities();
		State nextState;
		Double prob;
		B=0d;
		for(int i=0;i<succState.getNextStates().size();i++){
			nextState=succState.getNextStates().get(i);
            prob=succState.getProbs().get(i);
            Double gap=(Double)((HashMap)VGap).get(nextState);
            if (gap==null){
            	gap=firstGap;
            }
            double probGap=prob*gap;
            b.getNextStates().add(nextState);
            b.getProbs().add(probGap);
            B=B+probGap;
		}
		return b;
	}
	
	
}