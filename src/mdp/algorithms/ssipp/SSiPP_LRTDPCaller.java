package mdp.algorithms.ssipp;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

import mdp.ShortSightedSSPIP;
import mdp.State;

public class SSiPP_LRTDPCaller implements SSiPP_PlannerCaller {

	public SSiPP_LRTDPCaller(ShortSightedSSPIP ssp) {
		this.ssp = ssp;
	}
	
	private ShortSightedSSPIP ssp = null;
	
	@Override
	public HashMap<State, Double> executePlanner(State initialState, HashSet<State> goalStates, int maxDepth, long timeOut,
			int stateSamplingType, Random randomGenInitial, Random randomGenNextState, HashMap<State, Double> vLower) {
		
		return ssp.planWithLRTDPEnum(initialState, goalStates, maxDepth, timeOut, stateSamplingType, randomGenInitial, randomGenNextState, vLower);
	}
}
