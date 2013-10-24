package mdp;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Random;
import java.util.TreeMap;

import prob.mdp.HierarchicalParser;

public class ShortSightedSSPIP extends MDP_Fac {

	public ShortSightedSSPIP(String filename, int typeContext, int typeAproxPol, String typeSolution) {
		super(filename, typeContext, typeAproxPol, typeSolution, false);
		
		this.bdDiscount = new BigDecimal(1.0);
	}
	
	public ShortSightedSSPIP(String filename, int typeContext, int typeAproxPol, String typeSolution, boolean simulation) {
		super(HierarchicalParser.parseFile(filename), typeContext, typeAproxPol, typeSolution, simulation);
		
		this.bdDiscount = new BigDecimal(1.0);
	}
	
	public ShortSightedSSPIP(ArrayList input, int typeContext, int typeAproxPol, String typeSolution, boolean simulationMode) {
		super(input, typeContext, typeAproxPol, typeSolution, simulationMode);
		
		this.bdDiscount = new BigDecimal(1.0);
	}
	
	public ArrayList<Double> runRTDPIP_AsOfflinePlanner(int maxDepth, long timeOut, int stateSamplingType, 
			Random randomGenInitial, Random randomGenNextState, String initialStateLogPath)
	{
		int simulations = 50;
		int simulationType = 4; //Stationary
		
		this.solveRTDPIPEnum(maxDepth, timeOut, stateSamplingType, randomGenInitial, randomGenNextState, initialStateLogPath);
		
		return this.simulateMDPIP_Enumerative(simulations, maxDepth, (HashMap) this.VUpper, simulationType);
	}
	
	public void runRTDPIP_AsOnlinePlanner()
	{
	
	}
	
	public ArrayList<Double> simulateMDPIP_Enumerative(int numberOfSimulations, int maxHorizons, HashMap valueFunction, int simulationType) {

		Integer valueRes = convertValueFunctionInAdd(valueFunction);
		
		context.workingWithParameterizedBef = context.workingWithParameterized;
		context.workingWithParameterized = false;

	    ArrayList<Double> listReward = new ArrayList<Double>();
	    
	    Random randomGenInitial = new Random(19580427);
		Random randomGenNextState = new Random(19580800);
		
	    // This policy should be based on the MDPIP regression
	    // where you do a min_{ p_1 ... P_n } Q(s,a,p_1,...,p_n)
	    TreeMap action2QDD = this.calculateQHash(valueRes, true); //here we call the solver

	    context.workingWithParameterized = true;
	    
    	double sumReward = 0;
    	
    	for (int simulation = 1; simulation <= numberOfSimulations; simulation++){
    		System.out.printf("Executing simulation %d...", simulation);
    		System.out.println();
    		
    		double rewardState = simulateSingleMDPIP(maxHorizons, valueRes, randomGenInitial, randomGenNextState, action2QDD, simulationType);

    		listReward.add(rewardState);
    		sumReward += rewardState;
    		
    		System.out.println("Simulation executed.");
    	}
    	
    	flushCachesSimulator(action2QDD, false, null, null, null);
    	
    	double mean = sumReward / numberOfSimulations;
        double sigma = calculateStandarD(mean, listReward);
        double standardError = sigma / Math.sqrt(numberOfSimulations);
        
        ArrayList<Double> res = new ArrayList<Double>();
        res.add(mean);
        res.add(standardError);

        return res;
	}
	
	private Integer convertValueFunctionInAdd(HashMap valueFunction) {
		Integer add = (Integer) context.getTerminalNode(0.0);
		
		for (Object key : valueFunction.keySet()) {
			State state = (State) key;
			
			Iterator iteratorState = state.getValues().keySet().iterator();			
			
			Double value = (Double) valueFunction.get(state);
			
			context.insertValueInDD(add, state.getValues(), value, iteratorState, this.hmPrimeRemap);
		}
		
		return add;
	}

	protected double simulateSingleSSPIP(int maxHorizons, int policeValueADD, Random randomGenInitial, Random randomGenNextState, TreeMap action2QDD, int simulationType) {
		
		if (this.stationarySimulatorProbabilities != null)
			this.stationarySimulatorProbabilities.clear();
		else
			this.stationarySimulatorProbabilities = new HashMap<HashMap, Hashtable>();
		
		if (simulationType == 4) { //Stationary
			context.sampleProbabilitiesSubjectTo(NAME_FILE_CONTRAINTS);
			probNature = new Hashtable<String, Double>(context.currentValuesProb);
		}
		
		HashMap state = getStateRepresentationAsHashMap(sampleInitialStateFromList(randomGenInitial));
		
		double rewardState = getReward(state);
		                
		for (int t = 1; t <= maxHorizons; t++) {
			Action aBest = findBestA(state, action2QDD); //this is the work of the agent
			
			if (aBest == null){
				System.out.println("Some problem finding best action");
				System.exit(0);
			}
			
			//this is the work of the simulator true for using tmID2ADDNewSample
			TreeMap<Integer, Integer> nextState = chooseNextStateForMDPIPSimulation(state, policeValueADD, aBest, randomGenNextState, simulationType);
			
			System.out.println("Horizon " + t + ": " + aBest.getName());
			System.out.println("Previous state:" + new TreeMap<Integer, Integer>(state));
			System.out.println("Next state:" + nextState);
			
			state = new HashMap(nextState);
			    			
			rewardState += Math.pow(this.bdDiscount.doubleValue(),t) * getReward(state);       			
		}
		
		return rewardState;
	}
}
