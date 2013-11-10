package mdp.algorithms.ssipp;

import java.util.*;

import mdp.State;

/**
 * Represents a planner to be called by the SSiPP algorithm.
 * @author Daniel
 */
public interface SSiPP_PlannerCaller {
	/**
	 * Execute a call to a planner using a ShortSighted SSP-IP.
	 * @param initialState Initial state of a ShortSighted SSP-IP.
	 * @param goalStates Artificial goal states of a ShortSighted SSP-IP.
	 * @param maxDepth Max depth to be explored using the planner
	 * @param timeOut Max time allowed to plan.
	 * @param stateSamplingType Type of state sampling in a ShortSighted SSP-IP.
	 * @param randomGenInitial Pseudo-random generetor used to sample a initial state (legacy code, to be removed).
	 * @param randomGenNextState Pseudo-random generetor used to sample a state.
	 * @param vLower V-Lower used to initialise the planner.
	 * @return Optimal V-Lower of the ShortSighted SSP-IP.
	 */
	HashMap<State,Double> executePlanner(State initialState, HashSet<State> goalStates, int maxDepth, long timeOut, 
			int stateSamplingType, Random randomGenInitial, Random randomGenNextState, HashMap<State,Double> vLower);
}
