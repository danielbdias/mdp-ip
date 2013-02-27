package generator;

import java.util.Iterator;

import add.Context;

import mdp.Action;
import mdp.MDP_Fac;

public class DomainAnalyser {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length < 4) {
			System.out.println("Invalid arguments.");
			return;
		}
		
		String problemFile = args[0];
		String typeSolution = args[1];
		int typeContext = Integer.valueOf(args[2]);
		int typeAproxPol = Integer.valueOf(args[3]);
		
		MDP_Fac myMDP = new MDP_Fac(problemFile, typeContext, typeAproxPol, typeSolution);
		
		for (Object actionName : myMDP.mName2Action.keySet()) {
			Object add = (Integer) myMDP.context.getTerminalNode(1.0);
			
			System.out.println("Action: " + actionName);
			
			Action action = (Action) myMDP.mName2Action.get(actionName);
		
			for (Object variable : action.tmID2ADD.keySet()) {
				Object variableAdd = action.tmID2ADD.get(variable);
				
				add = myMDP.context.apply(add, variableAdd, Context.PROD);
			}
			
			myMDP.context.view(add);
		}
	}
}
