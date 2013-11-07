package mdp;

import java.math.*;
import java.util.*;

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
	// Short Sighted SSP-IP
	///////////////////////////////////////////////////////////////////////////////////////////////////
	
	public void executeSSiPPwithLRTDPuntilConvergence(int t, Random randomGenInitial, Random randomGenNextState, 
			int maxDepth, long timeOut, int stateSamplingType, String initialStateValuePath)
	{
		//convert in milliseconds
		timeOut = timeOut * 1000;
		
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
			long elapsedTime = System.currentTimeMillis() - initialTime; 
			
			if (elapsedTime >= timeOut) 
				break; //timeout
			
			//if (solvedStates.contains(initialState)) break; //convergence of initial state
			
			valueFunction = this.executeSSiPP(t, initialState, valueFunction, solvedStates, randomGenInitial, randomGenNextState, maxDepth, timeOut, stateSamplingType);
			
			//medição para o estado inicial
			if (initialStateValuePath != null) {
	            elapsedTime = System.currentTimeMillis() - initialTime;
		    	Double value = valueFunction.get(initialState);
		    	this.logValueInFile(initialStateValuePath, value, elapsedTime);
            }
		}
		
		System.out.println("Done !");
	}
	
	public HashMap<State,Double> executeSSiPP(int t, State initialState, HashMap<State,Double> valueFunction, HashSet<State> solvedStates, 
			Random randomGenInitial, Random randomGenNextState, int maxDepth, long timeOut, int stateSamplingType)
	{
		Stack<State> visitedStates = new Stack<State>();
		
		State state = initialState;
		visitedStates.add(state);
		
		while (true)
		{
			if (inGoalSet(state.getValues())) break; //goal reached
			//if (solvedStates.contains(state)) break; //state solved
			
			System.out.println(String.format("Planning using [%s] as initial state...", state));
			
			HashSet<State> goalStates = shortSightedSSPIP(state, t);
			
			if (goalStates.size() == 0) break; //deadend, end loop
			
			System.out.println(goalStates);
			
			goalStates.addAll(solvedStates);
			
			HashMap<State,Double> optimalValueFunction = this.planWithLRTDPEnum(state, goalStates, maxDepth, timeOut, stateSamplingType, 
																		 randomGenInitial, randomGenNextState, valueFunction);
			
			for (State s : optimalValueFunction.keySet()) 
				valueFunction.put(s, optimalValueFunction.get(s));
			
			while (!goalStates.contains(state)) {
				state = executeAction(valueFunction, state, randomGenNextState);
				visitedStates.add(state);
			}
		}
		
		if (inGoalSet(state.getValues())) {
//			while (!visitedStates.empty()) {
//				state = visitedStates.pop();
//				if (!checkSolved(valueFunction, solvedStates, state))
//					break;
//			}
		}
		
		System.out.println("SSiPP executed.");
		
		return valueFunction;
	}

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
                    
                    for (State nextState : succ.getNextStatesPoly().keySet()) {
                    	if (nextState.equals(sPrime) || goalSet.contains(nextState))
                    		continue;
                    	else {
                    		Pair nextStatePair = findStateInQueue(nextState, queue);
                    		
                    		if (nextStatePair != null && ((Integer) nextStatePair.get_o1()) > tCur + 1) 
                    			nextStatePair.set_o1(new Integer(tCur + 1));
                    		else
                    			queue.add(new Pair(new Integer(tCur + 1), nextState));
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

	private HashMap<State,Double> planWithLRTDPEnum(State initialState, HashSet<State> goalStates, int maxDepth, long timeOut, 
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
				//System.out.println("SOLVED: " + nextState);
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
		if (inGoalSet(state.getValues()))
			return maxUpper;

		return super.getRewardEnum(state);
	}
}