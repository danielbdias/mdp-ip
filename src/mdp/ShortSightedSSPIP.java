package mdp;

import java.math.*;
import java.util.*;

import mdp.algorithms.ssipp.SSiPP_PlannerCaller;
import prob.mdp.HierarchicalParser;
import util.Pair;

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

	///////////////////////////////////////////////////////////////////////////////////////////////////
	// SSiPP aux methods
	///////////////////////////////////////////////////////////////////////////////////////////////////

	private State executeAction(HashMap<State, Double> vLower, State state, Random randomGenNextState) {
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
		
		return chooseNextStateRTDPEnum(state, actionGreedy, randomGenNextState, bestActionIndex);
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
			
			double previousValue = Double.NaN;
			
			if (V.containsKey(state))
				previousValue = (Double) V.get(state);
			else
				previousValue = maxUpper;
			
			this.updateVUpper(state);
			V = (HashMap) VUpper;
			
			double nextValue = (Double) V.get(state);
			
			if (Math.abs(nextValue - previousValue) > epsilon)
			{
				rv = false;
				continue;
			}
			
			if (listGoalStates.contains(state.getValues()))
				continue;
			
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
				if (verbose) System.out.println("SOLVED: " + nextState);
			}
		}
		else {
			while (!closed.empty()) {
				state = closed.pop();
				this.updateVUpper(state);
				contUpperUpdates++;
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
		
		long initialTime = System.currentTimeMillis();
		
		//log do estado inicial
		if (initialStateValuePath != null)
			this.logValueInFile(initialStateValuePath, valueFunction.get(initialState), 0);
		
		while (true){
			long elapsedTime = (System.currentTimeMillis() - initialTime);
			
			if (elapsedTime >= timeOutInMilliseconds) {
				System.out.println("Finished by timeout !");
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
		
		System.out.println("Done !");
	}
	
	public HashMap<State,Double> executeSSiPP(int t, State initialState, HashMap<State,Double> valueFunction, 
			SSiPP_PlannerCaller planner, Random randomGenInitial, Random randomGenNextState, int maxDepth, long timeOut, int stateSamplingType)
	{
		System.out.println(String.format("Executing SSiPP with [%s] as initial state and [%s] as t...", initialState, t));
		
		State state = initialState;
		
		int depth = 0;
		
		while (true)
		{
			if (inGoalSet(state.getValues())) {
				System.out.println(String.format("Goal state [%s] reached.", state));
				break; //goal reached
			}
			
			if (depth > maxDepth) {
				System.out.println(String.format("Max depth of [%s] reached.", maxDepth));
				break; //max depth reached
			}
			
			System.out.println(String.format("Planning using SS-SSP with [%s] as initial state...", state));
			
			HashSet<State> goalStates = shortSightedSSPIP(state, t);
			
			if (goalStates.size() == 0) {
				System.out.println("Deadend found, finishing the SSiPP.");
				break; //deadend, end loop
			}
			
			HashMap<State,Double> optimalValueFunction = planner.executePlanner(state, goalStates, maxDepth, timeOut, stateSamplingType, 
																		 randomGenInitial, randomGenNextState, valueFunction);
			
			for (State s : optimalValueFunction.keySet()) {
				if (!inGoalSet(state.getValues()))
					valueFunction.put(s, optimalValueFunction.get(s));
			}
				
			
			while (!goalStates.contains(state)) {
				state = executeAction(valueFunction, state, randomGenNextState);
				depth++;
			}
		}
		
		System.out.println("SSiPP executed.");
		
		return valueFunction;
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////////
	// Labeled SSiPP
	///////////////////////////////////////////////////////////////////////////////////////////////////
	
	public void executeLabeledSSiPPuntilConvergence(int t, Random randomGenInitial, Random randomGenNextState, 
			int maxDepth, long timeOut, int stateSamplingType, SSiPP_PlannerCaller planner, String initialStateValuePath)
	{
		//convert in milliseconds
		long timeOutInMilliseconds = timeOut * 1000;
		
		maxUpper = 0;
		
		//choose initial state
		State initialState = new State(sampleInitialStateFromList(randomGenInitial), mName2Action.size());
		
		HashMap<State,Double> valueFunction = new HashMap<State,Double>();
		valueFunction.put(initialState, maxUpper);
		
		long initialTime = System.currentTimeMillis();
		
		//log do estado inicial
		if (initialStateValuePath != null)
			this.logValueInFile(initialStateValuePath, valueFunction.get(initialState), 0);
		
		HashSet<State> solvedStates = new HashSet<State>();
		
		while (true){
			long elapsedTime = (System.currentTimeMillis() - initialTime);
			
			if (elapsedTime >= timeOutInMilliseconds) {
				System.out.println("Finished by timeout !");
				break; //timeout		
			}
			
			if (solvedStates.contains(initialState)) {
				System.out.println("Finished by convergence !");
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
		
		System.out.println("Done !");
	}
	
	public HashMap<State,Double> executeLabeledSSiPP(int t, State initialState, HashMap<State,Double> valueFunction, 
			SSiPP_PlannerCaller planner, HashSet<State> solvedStates, Random randomGenInitial, 
			Random randomGenNextState, int maxDepth, long timeOut, int stateSamplingType)
	{
		System.out.println(String.format("Executing Labeled SSiPP with [%s] as initial state and [%s] as t...", initialState, t));
		
		Stack<State> visitedStates = new Stack<State>();
		
		State state = initialState;
		visitedStates.add(state);
		
		int depth = 0;
		
		while (true)
		{
			if (inGoalSet(state.getValues())) {
				System.out.println(String.format("Goal state [%s] reached.", state));
				break; //goal reached
			}
			
			if (solvedStates.contains(state)) {
				System.out.println(String.format("Solved state [%s] reached.", state));
				break; //state solved
			}
			
			if (depth > maxDepth) {
				System.out.println(String.format("Max depth of [%s] reached.", maxDepth));
				break; //max depth reached
			}
			
			System.out.println(String.format("Planning using SS-SSP with [%s] as initial state...", state));
			
			HashSet<State> goalStates = shortSightedSSPIP(state, t);
			
			if (goalStates.size() == 0) {
				System.out.println("Deadend found, finishing the SSiPP.");
				visitedStates.clear(); //do not update visited states in deadend cases
				break; //deadend, end loop
			}
			
			goalStates.addAll(solvedStates);
			
			HashMap<State,Double> optimalValueFunction = planner.executePlanner(state, goalStates, maxDepth, timeOut, stateSamplingType, 
																		 randomGenInitial, randomGenNextState, valueFunction);
			
			for (State s : optimalValueFunction.keySet()) 
				valueFunction.put(s, optimalValueFunction.get(s));
			
			while (!goalStates.contains(state)) {
				state = executeAction(valueFunction, state, randomGenNextState);
				visitedStates.add(state);
				depth++;
			}
		}
		
		if (inGoalSet(state.getValues())) {
			while (!visitedStates.empty()) {
				state = visitedStates.pop();
				if (!checkSolved(valueFunction, solvedStates, state, true))
					break;
			}
		}
		
		System.out.println("Labeled SSiPP executed.");
		
		return valueFunction;
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////////
	// Labeled SSiPP
	///////////////////////////////////////////////////////////////////////////////////////////////////
}