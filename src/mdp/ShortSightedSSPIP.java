package mdp;

import java.math.*;
import java.util.*;

import add.Context;
import mdp.algorithms.ssipp.SSiPP_PlannerCaller;
import prob.mdp.HierarchicalParser;
import util.Pair;

public class ShortSightedSSPIP extends MDP_Fac {

	private static final double NEGATIVE_INFINITY = -1e4;
	
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

	///////////////////////////////////////////////////////////////////////////////////////////////////
	// SSiPP aux methods
	///////////////////////////////////////////////////////////////////////////////////////////////////

	private Pair getBestAction(HashMap<State, Double> vLower, State state, Random randomGenNextState) {		
		double max = Double.NEGATIVE_INFINITY;
		Action actionGreedy = null;
		
		Iterator actions = mName2Action.entrySet().iterator();
		
		int posAction = 0;
		int bestActionIndex = -1;
		
		while (actions.hasNext()){
			Map.Entry meaction=(Map.Entry) actions.next();
			Action action=(Action) meaction.getValue();

			double Qt = this.computeQEnum(vLower, state, action, action.tmID2ADD, 'u', posAction);
		
			max = Math.max(max,Qt);
		
			if (Math.abs(max - Qt) <= 1e-10d){
				actionGreedy = action;
				bestActionIndex = posAction;
			}
			
			posAction++;
		}
		
		return new Pair(actionGreedy, bestActionIndex);
	}

	private HashSet<State> shortSightedSSPIP(State s, int t) {
		
		HashSet<State> goalSet = new HashSet<State>();
		
		PriorityQueue<Pair> queue = new PriorityQueue<Pair>(10, new Comparator<Pair>() {
			 public int compare(Pair firstPair, Pair secondPair) {
				 Integer firstPriority = (Integer) firstPair.get_o1();
				 Integer secondPriority = (Integer) secondPair.get_o1();
				 
				 return firstPriority.compareTo(secondPriority);
	         }
		});
		
		queue.add(new Pair(new Integer(0), s));
		
		while (!queue.isEmpty()) {
			Pair pair = queue.poll();
			
			State sPrime = (State) pair.get_o2();
			int tCur = (Integer) pair.get_o1();  
			
			if (inGoalSet(sPrime.getValues()) || t == tCur)
			{
				goalSet.add(sPrime);
				continue;
			}
			else
			{
				for (Object actionName : mName2Action.keySet()) {
                    Action action = (Action) mName2Action.get(actionName);

                    SuccProbabilitiesM succ = computeSuccessorsProb(sPrime, action.tmID2ADD);
                    
                    for (State sHat : succ.getNextStatesPoly().keySet()) {
                    	if (sHat.equals(s) || sHat.equals(sPrime) || goalSet.contains(sHat))
                    		continue;
                    	else {
                    		Pair nextStatePair = findStateInQueue(sHat, queue);
                    		
                    		if (nextStatePair != null && ((Integer) nextStatePair.get_o1()) > tCur + 1) 
                    			nextStatePair.set_o1(new Integer(tCur + 1));
                    		else
                    			queue.add(new Pair(new Integer(tCur + 1), sHat));
                    	}
                    }
				}
			}
		}
		
		return goalSet;
	}
	
	private Pair findStateInQueue(State nextState, PriorityQueue<Pair> queue) {

		for (Iterator iterator = queue.iterator(); iterator.hasNext();) {
			Pair pair = (Pair) iterator.next();
			State s = (State) pair.get_o2();		
			if (s.equals(nextState)) return pair;
		}
		
		return null;
	}

	public HashMap<State,Double> planWithLRTDPEnum(State initialState, HashSet<State> goalStates, int maxDepth, long timeOut, 
			int stateSamplingType, Random randomGenInitial, Random randomGenNextState, HashMap<State,Double> vLower)
	{
		//change initial states to the ShortSighted Goals
		ArrayList<TreeMap> realInitialStates = listInitialStates;
		
		listInitialStates = new ArrayList<TreeMap>();
		listInitialStates.add(initialState.getValues());
		
		//change goals to the ShortSighted Goals
		ArrayList<TreeMap> realGoals = listGoalStates;
				
		listGoalStates = new ArrayList<TreeMap>();
		
		for (State state : goalStates)
			listGoalStates.add(state.getValues());		
		
		this.solveLRTDPIPEnum(maxDepth, timeOut, stateSamplingType, randomGenInitial, randomGenNextState, null, vLower);
		
		//Restore the original goals and initial states
		listInitialStates = realInitialStates;
		listGoalStates = realGoals;
		
		return (HashMap<State,Double>) VUpper;
	}

	public HashMap<State,Double> planWithLRTDPFact(State initialState, HashSet<State> goalStates, int maxDepth, long timeOut, 
			int stateSamplingType, Random randomGenInitial, Random randomGenNextState, HashMap<State,Double> vLower)
	{
		//change initial states to the ShortSighted Goals
		ArrayList<TreeMap> realInitialStates = listInitialStates;
		
		listInitialStates = new ArrayList<TreeMap>();
		listInitialStates.add(initialState.getValues());
		
		//change goals to the ShortSighted Goals
		ArrayList<TreeMap> realGoals = listGoalStates;
				
		listGoalStates = new ArrayList<TreeMap>();
		
		for (State state : goalStates)
			listGoalStates.add(state.getValues());		
		
		Object vLowerAsAdd = this.convertHashMapValueFunctionToAdd(vLower);
		
		this.solveLRTDPIPFac(maxDepth, timeOut, stateSamplingType, randomGenInitial, randomGenNextState, null, null, vLowerAsAdd);
		
		//Restore the original goals and initial states
		listInitialStates = realInitialStates;
		listGoalStates = realGoals;
		
		HashMap<State,Double> result = this.convertValueFunctionAddToHashMap(VUpper);
		
		return result;
	}
	
	protected boolean checkSolved(HashMap V, HashSet<State> solvedStates, State state) {
		return this.checkSolved(V, solvedStates, state, false);
	}
	
	protected boolean checkSolved(HashMap V, HashSet<State> solvedStates, State state, boolean verbose) {
		boolean rv = true;
		
		Stack<State> open = new Stack<State>();
		Stack<State> closed = new Stack<State>();
		
		if (!solvedStates.contains(state)) open.push(state);
		
		while (!open.empty()) {
			state = open.pop();
			closed.push(state);
			
			if (isGoal(state) || solvedStates.contains(state))
				continue;
			
			if (isDeadEnd(state)) {
				if (!V.containsKey(state))
					rv = false;
				else {
					double currentValue = (Double) V.get(state);
					
					if (currentValue != NEGATIVE_INFINITY)
						rv = false;	
				}
				
				V.put(state, NEGATIVE_INFINITY); //update to negative infinity
				solvedStates.add(state);
				
				if (verbose) {
					formattedPrintln("DEADEND: %s", state);
					formattedPrintln("SOLVED: %s", state);
					formattedPrintln("SOLVED VALUE: %s", V.get(state));
				}
				 
				continue;
			}
			
			double previousValue = Double.NaN;
			
			if (V.containsKey(state))
				previousValue = (Double) V.get(state);
			else
				previousValue = maxUpper;
			
			this.updateVUpper(state, V);
			
			double nextValue = (Double) V.get(state);
			
			if (Math.abs(nextValue - previousValue) > epsilon)
			{
				rv = false;
				continue;
			}
			
			int greedyActionIndex = this.getBestActionForState(V, state);
			
			SuccProbabilitiesM nextProbs = state.getActionSuccProbab()[greedyActionIndex];
			
			Set<State> nextStates = null;
			
			if (context.workingWithParameterized)
				nextStates = nextProbs.getNextStatesPoly().keySet();
			else
				nextStates = nextProbs.getNextStatesProbs().keySet();
						
			for (State nextState : nextStates) {
				if (!solvedStates.contains(nextState) && !open.contains(nextState) && !closed.contains(nextState))
					open.push(nextState);
			}
		}
		
		if (rv) {
			for (State nextState : closed) 
			{
				solvedStates.add(nextState);
				if (verbose) { 
					formattedPrintln("SOLVED: %s", nextState);
					formattedPrintln("SOLVED VALUE: %s", V.get(nextState));
				}
			}
		}
		else {
			while (!closed.empty()) {
				state = closed.pop();
				this.updateVUpper(state, V);
				contUpperUpdates++;
			}
		}
		
		return rv;
	}

	protected boolean LRTDP_IP_CheckSolved(Random randomGenNextState, State state, HashSet<State> solvedStates)
	{
		boolean rv = true;
		
		Stack<State> open = new Stack<State>();
		Stack<State> closed = new Stack<State>();
		
		if (!solvedStates.contains(state)) open.push(state);
		  
	    while (!open.empty()) {
	    	state = open.pop();
		    closed.push(state);
		    
		    // The residual was too big, so will update the nodes in the open
		    // list and not mark any node as solved
			TreeMap<Integer, Boolean> state_prime = remapWithPrimes(state.getValues());//Because Vupper has only prime variables
			double valueState = (Double) context.getValueForStateInContext((Integer) VUpper, state_prime, null, null);
		      
			Action greedyAction = updateVUpper(state.getValues()); // here it is computed maxUpperUpdated
			contUpperUpdates++;
			
			Double residual = Math.abs(valueState - maxUpperUpdated);
			if (residual == null || residual > epsilon){
				rv = false;
				continue;
			}

			if (listGoalStates.contains(state.getValues()))
				continue;
			
			// Here we need to loop over all the states that have non-zero probability
			// of happening when applying greedyAction. Notice that we can reuse the
			// probability distribution P(s'|cur_s,greedyAction) computed on the call
			// to updateVUpper (as opposed to call the non-linear solver again).
			List<State> successors = getSuccessorsFromAction(state, greedyAction); //getSuccessors(state, greedyAction);
			
			for (State next_s : successors) {				
				if (!solvedStates.contains(next_s) && !open.contains(next_s) && !closed.contains(next_s))
					open.push(next_s);
			}
	    }

		if (rv) {// Marking all nodes in the closed list as solved
			while (!closed.empty()) {
				state = closed.pop();
				formattedPrintln("SOLVED: " + getStateString(state.getValues()));			
		        solvedStates.add(state);
		    }
		}
		else {
			//update states with residuasl and ancestors??
			while (!closed.empty()) {
				state = closed.pop();
				updateVUpper(state.getValues());
		    }
		}
    
		return rv;
	}
	
	@Override
	protected double getRewardEnum(State state) {
//		if (inGoalSet(state.getValues()))
//			return maxUpper;

		return super.getRewardEnum(state);
	}

	private boolean isDeadEnd(State state) {
		for (Object actionName : this.mName2Action.keySet()) {
			Action action = (Action) this.mName2Action.get(actionName);
			
			List<State> successors = getSuccessorsFromAction(state, action);
			
			if (successors.size() != 1) return false;
			if (!successors.get(0).equals(state)) return false;
		}
		
		return true;
	}
	
	private boolean isGoal(State state) {
		return  listGoalStates.contains(state.getValues());
	}
	
	private long lrtdpEnumTrial(State state, HashSet<State> solvedStates, int maxDepth, Random randomGenNextState, long timeOut, long initialTime, String initialStateLogPath)
	{
		Stack<State> visited = new Stack<State>();
		
		long totalTrialTime = 0;
		long totalTrialTimeSec = 0;
		
		while (true) {
			if (solvedStates.contains(state)) { 
				//System.out.println("Found solved state: " + state);
				break; //ended because reached a solved state
			}
			
			visited.push(state);
			
			if (inGoalSet(state.getValues())) {
				//System.out.println("Found goal state: " + state);
				//((HashMap<State,Double>) this.VUpper).put(state, 0.0);
				break;
			}
			
			if (isDeadEnd(state)) {
				formattedPrintln("Reached a deadend at state: " + state);
				((HashMap<State,Double>) this.VUpper).put(state, NEGATIVE_INFINITY);
				break;
			}
			
			//this compute maxUpperUpdated and actionGreedy
			Action greedyAction = updateVUpper(state); // Here we fill probNature
			
			contUpperUpdates++;
			
			//System.out.println("action greedy: " + greedyAction.getName());
			
			context.workingWithParameterized = context.workingWithParameterizedBef;
			state = chooseNextStateRTDPEnum(state, greedyAction, randomGenNextState, posActionGreedy);
			
			//System.out.println("next state: " + state);
			flushCachesRTDP(false);       
			
			totalTrialTime = GetElapsedTime();
            totalTrialTimeSec = totalTrialTime / 1000;
            
            if (initialStateLogPath != null) {
	            //medição para o estado inicial
	            long elapsedTime = (System.currentTimeMillis() - initialTime);
	            
	            State initialState = new State(listInitialStates.get(0), mName2Action.size());
		    			    	
		    	Double value = ((HashMap<State,Double>) this.VUpper).get(initialState);
		    	        	    	
		    	this.logValueInFile(initialStateLogPath, value, elapsedTime);
            }
		}
		
		if (solvedStates.contains(state) || inGoalSet(state.getValues())) {
			while (!visited.empty()) {
				state = visited.pop();
				//if (!checkSolved((HashMap) VUpper, solvedStates, state, true))
				if (!checkSolved((HashMap) VUpper, solvedStates, state, false))
					break;
			}	
		}
		
//		System.out.println("***************************************************");
//		System.out.println(" TRIAL END ");
//		System.out.println("***************************************************");
		//this.printEnumValueFunction((HashMap<State,Double>) this.VUpper);
		
		totalTrialTime = GetElapsedTime();
        totalTrialTimeSec = totalTrialTime / 1000;		
		return totalTrialTimeSec;
	}	

	private long brtdpEnumTrial(State state, int maxDepth, double tau, Random randomGenNextState, long timeOut, long initialTime, String initialStateLogPath)
	{
		State initialState = new State(listInitialStates.get(0), mName2Action.size());
		
		Stack<State> visited = new Stack<State>();
		
		long totalTrialTime = 0;
		long totalTrialTimeSec = 0;
		
		while (true) {			
			if (state == null)
				break; //convergence
			
			visited.push(state);
			
			if (inGoalSet(state.getValues())) {
				//System.out.println("Found goal state: " + state);
				//((HashMap<State,Double>) this.VUpper).put(state, 0.0);
				break;
			}
			
			if (isDeadEnd(state)) {
				formattedPrintln("Reached a deadend at state: " + state);
				((HashMap<State,Double>) this.VUpper).put(state, NEGATIVE_INFINITY);
				((HashMap<State,Double>) this.VLower).put(state, NEGATIVE_INFINITY);
				break;
			}
			
			//this compute maxUpperUpdated and actionGreedy
			Action greedyAction = updateVUpper(state, (HashMap<State, Double>) VUpper); // Here we fill probNature
			
			updateVLower(state, (HashMap<State, Double>) VLower);
			
			contUpperUpdates++;
			
			//System.out.println("action greedy: " + greedyAction.getName());
			
			context.workingWithParameterized = context.workingWithParameterizedBef;
			state = chooseNextStateBRTDPEnum(state, greedyAction, randomGenNextState, posActionGreedy, tau, initialState);
			
			//System.out.println("next state: " + state);
			flushCachesRTDP(false);       
			
			totalTrialTime = GetElapsedTime();
            totalTrialTimeSec = totalTrialTime / 1000;
            
            if (initialStateLogPath != null) {
	            //medição para o estado inicial
	            long elapsedTime = (System.currentTimeMillis() - initialTime);
		    			    	
		    	Double value = ((HashMap<State,Double>) this.VUpper).get(initialState);
		    	        	    	
		    	this.logValueInFile(initialStateLogPath, value, elapsedTime);
            }
		}
		
		if (state == null || inGoalSet(state.getValues())) {
			while (!visited.empty()) {
				state = visited.pop();
				
				updateVUpper(state, (HashMap<State, Double>) VUpper);
				updateVLower(state, (HashMap<State, Double>) VLower);
			}	
		}
		
//		System.out.println("***************************************************");
//		System.out.println(" TRIAL END ");
//		System.out.println("***************************************************");
		//this.printEnumValueFunction((HashMap<State,Double>) this.VUpper);
		
		totalTrialTime = GetElapsedTime();
        totalTrialTimeSec = totalTrialTime / 1000;		
		return totalTrialTimeSec;
	}
	
	/**
	 * Labeled Real-time dynamic programming for Enumerative MDP-IPs
	 */
	public void solveLRTDPIPEnum(int maxDepth, long timeOut, int stateSamplingType, 
			Random randomGenInitial, Random randomGenNextState, String initialStateLogPath) {
		this.solveLRTDPIPEnum(maxDepth, timeOut, stateSamplingType, randomGenInitial, randomGenNextState, initialStateLogPath, new HashMap<State,Double>());
	}
	
	/**
	 * Labeled Real-time dynamic programming for Enumerative MDP-IPs
	 */
	public void solveLRTDPIPEnum(int maxDepth, long timeOut, int stateSamplingType, 
			Random randomGenInitial, Random randomGenNextState, String initialStateLogPath, HashMap<State,Double> vUpper) {		
		typeSampledRTDPMDPIP = stateSamplingType;
		
		HashSet<State> solvedStates = new HashSet<State>(); 
		
		ResetTimer();
		
		if (typeSampledRTDPMDPIP == 3)  //callSolver with constraints p_i>=epsilon 
			context.getProbSampleCallingSolver(NAME_FILE_CONTRAINTS_GREATERZERO);
		else if (typeSampledRTDPMDPIP == 5)
			context.probSample = context.sampleProbabilitiesSubjectTo(NAME_FILE_CONTRAINTS);
	
		//Initialize Vu with admissible value function //////////////////////////////////
		//create an ADD with  VUpper=Rmax/1-gamma /////////////////////////////////////////
		double Rmax = context.apply(this.rewardDD, Context.MAXVALUE);
		
		if (this.bdDiscount.doubleValue() == 1)
			maxUpper = Rmax * maxDepth;
		else
			maxUpper = Rmax / (1 - this.bdDiscount.doubleValue());
		
		for (TreeMap stateAsMap : this.listGoalStates) {
			State goalState = new State(stateAsMap);
			
			if (!vUpper.containsKey(goalState))
				vUpper.put(goalState, 0.0);
		}		
		
		VUpper = new HashMap<State,Double>(vUpper);
		
		contUpperUpdates = 0;

		context.workingWithParameterizedBef = context.workingWithParameterized;
				
		long initialTime = System.currentTimeMillis();
		
		State state = new State(sampleInitialStateFromList(randomGenInitial), mName2Action.size());
		
		while (!solvedStates.contains(state)){
			//do trial //////////////////////////////////
			this.lrtdpEnumTrial(state, solvedStates, maxDepth, randomGenNextState, timeOut, initialTime, initialStateLogPath);
		}
		
		if (printFinalADD) {
			HashMap<State,Double> vUpperAsHashMap = (HashMap<State,Double>) VUpper;
			
			this.printEnumValueFunction(vUpperAsHashMap);
		}
	}
	
	/**
	 * Bounded Real-time dynamic programming for Enumerative MDP-IPs
	 */
	public void solveBRTDPIPEnum(int maxDepth, long timeOut, double tau, int stateSamplingType, 
			Random randomGenInitial, Random randomGenNextState, String initialStateLogPath) {
		this.solveBRTDPIPEnum(maxDepth, timeOut, tau, stateSamplingType, randomGenInitial, randomGenNextState, initialStateLogPath, new HashMap<State,Double>(), new HashMap<State,Double>());
	}
	
	/**
	 * Bounded Real-time dynamic programming for Enumerative MDP-IPs
	 */
	public void solveBRTDPIPEnum(int maxDepth, long timeOut, double tau, int stateSamplingType, 
			Random randomGenInitial, Random randomGenNextState, String initialStateLogPath, HashMap<State,Double> vUpper, HashMap<State,Double> vLower) {		
		typeSampledRTDPMDPIP = stateSamplingType;
		
		HashSet<State> solvedStates = new HashSet<State>(); 
		
		ResetTimer();
		
		if (typeSampledRTDPMDPIP == 3)  //callSolver with constraints p_i>=epsilon 
			context.getProbSampleCallingSolver(NAME_FILE_CONTRAINTS_GREATERZERO);
		else if (typeSampledRTDPMDPIP == 5)
			context.probSample = context.sampleProbabilitiesSubjectTo(NAME_FILE_CONTRAINTS);
	
		double Rmax = context.apply(this.rewardDD, Context.MAXVALUE);
		
		if (this.bdDiscount.doubleValue() == 1)
			maxUpper = Rmax * maxDepth;
		else
			maxUpper = Rmax / (1 - this.bdDiscount.doubleValue());
		
		double Rmin = context.apply(this.rewardDD, Context.MINVALUE);
		
		if (this.bdDiscount.doubleValue() == 1)
			minLower = Rmin * maxDepth;
		else
			minLower = Rmin / (1 - this.bdDiscount.doubleValue());
		
		for (TreeMap stateAsMap : this.listGoalStates) {
			State goalState = new State(stateAsMap);
			
			if (!vUpper.containsKey(goalState))
				vUpper.put(goalState, 0.0);
			
			if (!vLower.containsKey(goalState))
				vLower.put(goalState, 0.0);
		}		
		
		VUpper = new HashMap<State,Double>(vUpper);
		
		VLower = new HashMap<State,Double>(vLower);
		
		contUpperUpdates = 0;

		context.workingWithParameterizedBef = context.workingWithParameterized;
				
		long initialTime = System.currentTimeMillis();
		
		State state = new State(sampleInitialStateFromList(randomGenInitial), mName2Action.size());
		
		double upperInitial = maxUpper;
		
		if (vUpper.containsKey(state))
			upperInitial = vUpper.get(state);
		
		double lowerInitial = minLower;
		
		if (vLower.containsKey(state))
			lowerInitial = vLower.get(state);
		
		while (upperInitial - lowerInitial > epsilon){
			//do trial //////////////////////////////////
			this.brtdpEnumTrial(state, maxDepth, tau, randomGenNextState, timeOut, initialTime, initialStateLogPath);
			
			vUpper = (HashMap<State,Double>) VUpper;
			vLower = (HashMap<State,Double>) VLower;
			
			if (vUpper.containsKey(state))
				upperInitial = vUpper.get(state);
			
			if (vLower.containsKey(state))
				lowerInitial = vLower.get(state);
		}
		
		if (printFinalADD) {
			HashMap<State,Double> vUpperAsHashMap = (HashMap<State,Double>) VUpper;
			
			this.printEnumValueFunction(vUpperAsHashMap);
		}
	}
	
	public boolean emulatePolicy(HashMap<State,Double> valueFunction, Random randomGenInitial, Random randomGenNextState, int stateSamplingType, int maxDepth) {
//		formattedPrintln("Policy execution started...");
		
		State initialState = new State(sampleInitialStateFromList(randomGenInitial), mName2Action.size());
		
//		formattedPrintln("Initial state [%s].", initialState);
		
		State state = initialState;
		
		for (int i = 0; i < maxDepth; i++) {
			if (isGoal(state)) {
//				formattedPrintln("Goal state [%s] found.", state);
				return true;
			}
			
			if (isDeadEnd(state)) {
//				formattedPrintln("Deadend state [%s] found.", state);
				return false;
			}
			
			Pair p = getBestAction(valueFunction, state, randomGenNextState);  
			
			Action actionGreedy = (Action) p.get_o1();
			int bestActionIndex = (Integer) p.get_o2();
			
//			formattedPrintln("Executing action [%s]...", actionGreedy.getName());
			
			state = chooseNextStateRTDPEnum(state, actionGreedy, randomGenNextState, bestActionIndex);
			
//			formattedPrintln("State [%s] reached...", state);
		}
		
		return false;
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////////
	// Heuristic
	///////////////////////////////////////////////////////////////////////////////////////////////////
	
	public HashMap<State,Double> computeHeuristic(Random randomGenInitial, Random randomGenNextState) {
		State initialState = new State(sampleInitialStateFromList(randomGenInitial), mName2Action.size());
		
		HashMap<State,Double> valueFunction = new HashMap<State,Double>();
		valueFunction.put(initialState, maxUpper);
		
		HashSet<State> solvedStates = new HashSet<State>();
		
		while (!solvedStates.contains(initialState)){
			this.executeHeristicComputationTrial(initialState, randomGenNextState, valueFunction, solvedStates);
		}
		
		return valueFunction;
	}
	
	private void executeHeristicComputationTrial(State state,
			Random randomGenNextState, HashMap<State, Double> heuristicFunction,
			HashSet<State> solvedStates) {
		
		Stack<State> visited = new Stack<State>();
		
		while (true) {
			if (solvedStates.contains(state)) { 
				break; //ended because reached a solved state
			}
			
			visited.push(state);
			
			if (inGoalSet(state.getValues())) {
				break;
			}
			
			if (isDeadEnd(state)) {
				formattedPrintln("Reached a deadend at state: " + state);
				heuristicFunction.put(state, NEGATIVE_INFINITY);
				break;
			}
			
			Action greedyAction = updateHeuristic(state, heuristicFunction);
			
			//System.out.println("action greedy: " + greedyAction.getName());
			
			context.workingWithParameterized = context.workingWithParameterizedBef;
			state = chooseNextStateRTDPEnum(state, greedyAction, randomGenNextState, posActionGreedy);
			
			//System.out.println("next state: " + state);
			flushCachesRTDP(false);       
		}
		
		if (solvedStates.contains(state) || inGoalSet(state.getValues())) {
			while (!visited.empty()) {
				state = visited.pop();
				if (!checkSolvedHeuristic(heuristicFunction, solvedStates, state))
					break;
			}	
		}
	}

	protected Action updateHeuristic(State state, HashMap heuristicFunction) {
		
		Pair result = this.computeVUpper(state, heuristicFunction);
		
		Action actionGreedy = (Action) result.get_o1();
		double maxTotal = (Double) result.get_o2();
		
		heuristicFunction.put(state, maxTotal);
		
		return actionGreedy;
	}
	
	protected Pair computeHeuristic(State state, HashMap heuristicFunction) {
		double max = Double.NEGATIVE_INFINITY;
		Action actionGreedy = null;
		
		Iterator actions = mName2Action.entrySet().iterator();
		
		int posAction = 0;
		posActionGreedy = -1;
		
		while (actions.hasNext()){
			Map.Entry meaction=(Map.Entry) actions.next();
			Action action=(Action) meaction.getValue();

			double h = this.computeMaxHeuristic(heuristicFunction, state, action, action.tmID2ADD, posAction);
		
			max = Math.max(max,h);
		
			if (Math.abs(max - h) <= 1e-10d){
				actionGreedy = action;
				posActionGreedy = posAction;
			}
			
			posAction++;
		}

		double rew = this.getRewardEnum(state);
		double maxTotal = rew + max;
		
		return new Pair(actionGreedy, maxTotal);
	}
	
	protected double computeMaxHeuristic(HashMap heuristicFunction, State state, Action action, TreeMap iD2ADD, int posAction) {
		SuccProbabilitiesM succ = state.getActionSuccProbab()[posAction];

		//if it has not been calculed before, compute it 
        if (succ == null) {
        	//succ = computeSuccesorsProbEnum(state, iD2ADD);
        	succ = computeSuccessorsProb(state, iD2ADD);
        	
        	if (succ.getNextStatesProbs().size() == 0 && succ.getNextStatesPoly().size() == 0){
        		System.out.println("Not Successors for state: " + state);
        		System.exit(1);
        	}
        	
        	state.getActionSuccProbab()[posAction] = succ;
        }
        
        double maxRew = Double.NEGATIVE_INFINITY;
        
        for (State nextState : succ.getNextStatesPoly().keySet()) {
        	double rew = maxUpper;
        	
			if (heuristicFunction.containsKey(nextState))
				rew = (Double) heuristicFunction.get(nextState);
			
			if (rew > maxRew)
				maxRew = rew;
		}
        
        return maxRew;
	}
	
	protected int getBestActionForStateHeuristic(HashMap heuristicFunction, State state) {
		
		double max = Double.NEGATIVE_INFINITY;
		
		Iterator actions = mName2Action.entrySet().iterator();
		
		int posAction = 0;
		int bestActionIndex = -1;
		
		while (actions.hasNext()){
			Map.Entry meaction=(Map.Entry) actions.next();
			Action action=(Action) meaction.getValue();

			double h = this.computeMaxHeuristic(heuristicFunction, state, action, action.tmID2ADD, posAction);
			
			max = Math.max(max,h);
		
			if (Math.abs(max - h) <= 1e-10d){
				posActionGreedy = posAction;
			}
			
			posAction++;
		}
		
		return bestActionIndex;
	}
	
	protected boolean checkSolvedHeuristic(HashMap heuristicFunction, HashSet<State> solvedStates, State state) {
		boolean rv = true;
		
		Stack<State> open = new Stack<State>();
		Stack<State> closed = new Stack<State>();
		
		if (!solvedStates.contains(state)) open.push(state);
		
		while (!open.empty()) {
			state = open.pop();
			closed.push(state);
			
			if (isGoal(state) || solvedStates.contains(state))
				continue;
			
			if (isDeadEnd(state)) {
				if (!heuristicFunction.containsKey(state))
					rv = false;
				else {
					double currentValue = (Double) heuristicFunction.get(state);
					
					if (currentValue != NEGATIVE_INFINITY)
						rv = false;	
				}
				
				heuristicFunction.put(state, NEGATIVE_INFINITY); //update to negative infinity
				solvedStates.add(state);
				 
				continue;
			}
			
			double previousValue = Double.NaN;
			
			if (heuristicFunction.containsKey(state))
				previousValue = (Double) heuristicFunction.get(state);
			else
				previousValue = maxUpper;
			
			this.updateHeuristic(state, heuristicFunction);
			
			double nextValue = (Double) heuristicFunction.get(state);
			
			if (Math.abs(nextValue - previousValue) > epsilon)
			{
				rv = false;
				continue;
			}
			
			int greedyActionIndex = this.getBestActionForStateHeuristic(heuristicFunction, state);
			
			SuccProbabilitiesM nextProbs = state.getActionSuccProbab()[greedyActionIndex];
			
			Set<State> nextStates = null;
			
			if (context.workingWithParameterized)
				nextStates = nextProbs.getNextStatesPoly().keySet();
			else
				nextStates = nextProbs.getNextStatesProbs().keySet();
						
			for (State nextState : nextStates) {
				if (!solvedStates.contains(nextState) && !open.contains(nextState) && !closed.contains(nextState))
					open.push(nextState);
			}
		}
		
		if (rv) {
			for (State nextState : closed) 
				solvedStates.add(nextState);
		}
		else {
			while (!closed.empty()) {
				state = closed.pop();
				this.updateHeuristic(state, heuristicFunction);
				contUpperUpdates++;
			}
		}
		
		return rv;
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////////
	// SSiPP
	///////////////////////////////////////////////////////////////////////////////////////////////////
	
	public void executeSSiPPuntilConvergence(int t, Random randomGenInitial, Random randomGenNextState, 
			int maxDepth, long timeOut, int stateSamplingType, SSiPP_PlannerCaller planner, String initialStateValuePath)
	{		
		//convert in milliseconds
		long timeOutInMilliseconds = timeOut * 1000;
		
		maxUpper = 0;
		
		//choose initial state
		State initialState = new State(sampleInitialStateFromList(randomGenInitial), mName2Action.size());
		
		HashMap<State,Double> valueFunction = new HashMap<State,Double>();
		valueFunction.put(initialState, maxUpper);
		
		for (TreeMap stateAsMap : this.listGoalStates) {
			State state = new State(stateAsMap);
			valueFunction.put(state, 0.0);
		}
		
		long initialTime = System.currentTimeMillis();
		
		//log do estado inicial
		if (initialStateValuePath != null)
			this.logValueInFile(initialStateValuePath, valueFunction.get(initialState), 0);
		
		while (true){
			long elapsedTime = (System.currentTimeMillis() - initialTime);
			
			if (elapsedTime >= timeOutInMilliseconds) {			
				formattedPrintln("Finished by timeout !");
				break; //timeout		
			}
			
			valueFunction = this.executeSSiPP(t, initialState, valueFunction, planner, randomGenInitial, randomGenNextState, maxDepth, timeOut, stateSamplingType);
			
			//medição para o estado inicial
			if (initialStateValuePath != null) {
	            elapsedTime = (System.currentTimeMillis() - initialTime);
		    	Double value = valueFunction.get(initialState);
		    	this.logValueInFile(initialStateValuePath, value, elapsedTime);
            }
		}
		
		formattedPrintln("Done !");
	}
	
	public HashMap<State,Double> executeSSiPP(int t, State initialState, HashMap<State,Double> valueFunction, 
			SSiPP_PlannerCaller planner, Random randomGenInitial, Random randomGenNextState, int maxDepth, long timeOut, int stateSamplingType)
	{
		formattedPrintln("Executing SSiPP with [%s] as initial state and [%s] as t...", initialState, t);
		
		State state = initialState;
		
		while (true)
		{
			if (inGoalSet(state.getValues())) {
				formattedPrintln("Goal state [%s] reached.", state);
				break; //goal reached
			}
			
			formattedPrintln("Planning using SS-SSP with [%s] as initial state...", state);
			
			HashSet<State> goalStates = shortSightedSSPIP(state, t);
			
			if (goalStates.size() == 0) {
				valueFunction.put(state, NEGATIVE_INFINITY);
				formattedPrintln("Deadend found, finishing the SSiPP.");
				break; //deadend, end loop
			}
			
			HashMap<State,Double> optimalValueFunction = planner.executePlanner(state, goalStates, maxDepth, timeOut, stateSamplingType, 
																		 randomGenInitial, randomGenNextState, valueFunction);
			
			for (State s : optimalValueFunction.keySet()) {
				if (!inGoalSet(state.getValues()))
					valueFunction.put(s, optimalValueFunction.get(s));
			}
			
			while (!goalStates.contains(state)) {
				context.workingWithParameterized = true;
				
				Pair p = getBestAction(valueFunction, state, randomGenNextState);  
				
				Action actionGreedy = (Action) p.get_o1();
				int bestActionIndex = (Integer) p.get_o2();
				
				formattedPrintln("Executing action [%s]...", actionGreedy.getName());
				
				state = chooseNextStateRTDPEnum(state, actionGreedy, randomGenNextState, bestActionIndex);
			}
		}
		
		formattedPrintln("SSiPP executed.");
		
		return valueFunction;
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////////
	// Labeled SSiPP
	//////////////l/////////////////////////////////////////////////////////////////////////////////////
	
	List<HashMap<State,Double>> valueFunctionPerTrial = new ArrayList<HashMap<State,Double>>();
	
	public void executeLabeledSSiPPuntilConvergence(int t, Random randomGenInitial, Random randomGenNextState, 
			int maxDepth, long timeOut, int stateSamplingType, SSiPP_PlannerCaller planner, String initialStateValuePath)
	{	
		maxUpper = 0;
		
		//choose initial state
		State initialState = new State(sampleInitialStateFromList(randomGenInitial), mName2Action.size());
		
		HashMap<State,Double> valueFunction = new HashMap<State,Double>();
		valueFunction.put(initialState, maxUpper);
		
		HashSet<State> solvedStates = new HashSet<State>();
		
		for (TreeMap stateAsMap : this.listGoalStates) {
			State goalState = new State(stateAsMap);
			valueFunction.put(goalState, 0.0);
			solvedStates.add(goalState);
		}
		
		//valueFunction = computeHeuristic(randomGenInitial, randomGenNextState);
		
		long initialTime = System.currentTimeMillis();
		
		//log do estado inicial
		if (initialStateValuePath != null)
			this.logValueInFile(initialStateValuePath, valueFunction.get(initialState), 0);
		
		while (true){
			long elapsedTime = (System.currentTimeMillis() - initialTime);
			
			if (solvedStates.contains(initialState)) {
				formattedPrintln("Finished by convergence !");
				break; //convergence of initial state
			}
			
			valueFunction = this.executeLabeledSSiPP(t, initialState, valueFunction, planner, solvedStates, randomGenInitial, randomGenNextState, maxDepth, timeOut, stateSamplingType);
			
			//medição para o estado inicial
			if (initialStateValuePath != null) {
	            elapsedTime = (System.currentTimeMillis() - initialTime);
		    	Double value = valueFunction.get(initialState);
		    	this.logValueInFile(initialStateValuePath, value, elapsedTime);
            }
		}
		
//		summarizeValueFunction(valueFunctionPerTrial);
		
		formattedPrintln("Done !");
	}
	
	public HashMap<State,Double> executeLabeledSSiPP(int t, State initialState, HashMap<State,Double> valueFunction, 
			SSiPP_PlannerCaller planner, HashSet<State> solvedStates, Random randomGenInitial, 
			Random randomGenNextState, int maxDepth, long timeOut, int stateSamplingType)
	{
//		formattedPrintln("Executing Labeled SSiPP with [%s] as initial state and [%s] as t...", initialState, t);
		
		Stack<State> visitedStates = new Stack<State>();
		
		State state = initialState;
		visitedStates.add(state);
		
		while (true)
		{
			if (solvedStates.contains(state)) {
				//formattedPrintln("Solved state [%s] reached.", state);
				break; //state solved
			}

			if (isDeadEnd(state)) {
				valueFunction.put(state, NEGATIVE_INFINITY);
				solvedStates.add(state);
				formattedPrintln("Deadend found, finishing the LSSiPP trial.");
				break; //deadend, end loop
			}
			
			formattedPrintln("Planning using SS-SSP with [%s] as initial state...", state);
			
			HashSet<State> goalStates = shortSightedSSPIP(state, t);
			
			goalStates.addAll(solvedStates);
			
			HashMap<State,Double> optimalValueFunction = planner.executePlanner(state, goalStates, maxDepth, timeOut, stateSamplingType, 
																		 randomGenInitial, randomGenNextState, valueFunction);
			
			for (State s : optimalValueFunction.keySet()) {
				//if (!goalStates.contains(s))
				valueFunction.put(s, optimalValueFunction.get(s));
			}
			
			valueFunctionPerTrial.add(new HashMap<State, Double>(valueFunction));
			
			while (!goalStates.contains(state)) {				
				Pair p = getBestAction(valueFunction, state, randomGenNextState);  
				
				Action actionGreedy = (Action) p.get_o1();
				int bestActionIndex = (Integer) p.get_o2();
				
				formattedPrintln("Executing action [%s]...", actionGreedy.getName());
				
				State reachedState = chooseNextStateRTDPEnum(state, actionGreedy, randomGenNextState, bestActionIndex);
				visitedStates.add(reachedState);
				
				formattedPrintln("State [%s] reached...", reachedState);
				
				state = reachedState;
			}
		}
		
		if (inGoalSet(state.getValues()) || solvedStates.contains(state)) {
			while (!visitedStates.empty()) {
				state = visitedStates.pop();
				if (!checkSolved(valueFunction, solvedStates, state, true))
					break;
			}
		}
		
//		formattedPrintln("Labeled SSiPP executed.");
		
		return valueFunction;
	}
	
	protected void summarizeValueFunction(List<HashMap<State,Double>> valueFunctionPerTrial) {
		if (valueFunctionPerTrial == null || valueFunctionPerTrial.size() == 0) return;
		
		Set<State> lastTrialStates = new TreeSet<State>(valueFunctionPerTrial.get(valueFunctionPerTrial.size() - 1).keySet());
		
		List<String> reportData = new ArrayList<String>();
		
		String line = "";
		
		//print value per trial
		
		for (State state : lastTrialStates) {
			line = String.format("%s;%s;", state.getIdentifier(), state.toString());
			
			for (int i = 0; i < valueFunctionPerTrial.size(); i++) {	
				HashMap<State,Double> valueFunction = valueFunctionPerTrial.get(i);
				
				if (valueFunction.containsKey(state))
					line += valueFunction.get(state) + ";";
				else
					line += maxUpper + ";";
			}
		
			reportData.add(line);
		}
		
		for (String string : reportData)
			formattedPrintln(string);
	}
}