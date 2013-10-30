package mdp;

import java.math.*;
import java.util.*;

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

	///////////////////////////////////////////////////////////////////////////////////////////////////
	// Short Sighted SSP-IP
	///////////////////////////////////////////////////////////////////////////////////////////////////
	
	public HashMap<State,Double> executeSSiPP(int t, Random randomGenInitial, Random randomGenNextState, 
			int maxDepth, long timeOut, int stateSamplingType)
	{
		maxUpper = 0;
		
		HashMap<State,Double> vLower = new HashMap<State,Double>();
		
		State state = new State(sampleInitialStateFromList(randomGenInitial), mName2Action.size());
		
		while (!inGoalSet(state.getValues()))
		{
			HashSet<State> goalStates = shortSightedSSPIP(state, t);
			
			HashMap<State,Double> optimalVLower = this.planWithLRTDPEnum(state, goalStates, maxDepth, timeOut, stateSamplingType, 
																		 randomGenInitial, randomGenNextState, vLower);
			
			for (State s : optimalVLower.keySet()) 
				vLower.put(s, optimalVLower.get(s));
			
			while (!goalStates.contains(state))
				state = executeAction(vLower, state, randomGenNextState);
		}
		
		return vLower;
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
		
		HashSet<State> previousEpochStates = new HashSet<State>();
		previousEpochStates.add(s);
		
		HashSet<State> currentEpochStates = null;
		
		for (int epoch = 1; epoch <= t; epoch++) {
			currentEpochStates = new HashSet<State>();
			
			for (Object actionName : mName2Action.keySet()) {
				Action action = (Action) mName2Action.get(actionName);
				
				for (State state : previousEpochStates) {
					SuccProbabilitiesM succ = computeSuccessorsProb(state, action.tmID2ADD);
					
					for (State nextState : succ.getNextStatesPoly().keySet())
						currentEpochStates.add(nextState);
				}
			}
			
			previousEpochStates = currentEpochStates;
		}
		
		if (currentEpochStates.contains(s))
			currentEpochStates.remove(s);
		
		return currentEpochStates;
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
				System.out.println("SOLVED: " + nextState);
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

	///////////////////////////////////////////////////////////////////////////////////////////////////
	// LRTDP-IP with simulation
	///////////////////////////////////////////////////////////////////////////////////////////////////
	
	public HashMap<State,Double> executeLRTDPIWithSimulation(int t, Random randomGenInitial, Random randomGenNextState, 
			int maxDepth, long timeOut, int stateSamplingType)
	{
		maxUpper = 0;
		
		HashMap<State,Double> vLower = new HashMap<State,Double>();
		
		HashSet<State> goalStates = new HashSet<State>();
		
		for (TreeMap state : listGoalStates)
			goalStates.add(new State(state));
		
		State state = new State(sampleInitialStateFromList(randomGenInitial), mName2Action.size());
		
		while (!inGoalSet(state.getValues()))
		{		
			HashMap<State,Double> optimalVLower = this.planWithLRTDPEnum(state, goalStates, maxDepth, timeOut, stateSamplingType, 
																		 randomGenInitial, randomGenNextState, vLower);
			
			for (State s : optimalVLower.keySet()) 
				vLower.put(s, optimalVLower.get(s));
			
			state = executeAction(vLower, state, randomGenNextState);
		}
		
		return vLower;
	}
}