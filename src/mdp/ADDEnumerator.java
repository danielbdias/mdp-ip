package mdp;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class ADDEnumerator {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length < 6) {
			System.out.println("Invalid arguments.");
			return;
		}
		
		String problemFile = args[0];
		String typeSolution = args[1];
		int typeContext = Integer.valueOf(args[2]);
		int typeAproxPol = Integer.valueOf(args[3]);
		String addFileName = args[4];
		String outputFile = args[5];

		MDP_Fac myMDP = new MDP_Fac(problemFile, typeContext, typeAproxPol, typeSolution);
		
		myMDP.context.workingWithParameterized = false;
		int addId = (Integer) myMDP.context.readValueFunction(addFileName);
		
		List<Double> enumerateValues = enumerateValuesInADD(myMDP, addId);
		
		try {
			java.io.FileWriter writer = new FileWriter(outputFile);
			
			for (int i = 0; i < enumerateValues.size(); i++)
				writer.write(i + " " + enumerateValues.get(i) + "\n");
			
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("ADD enumeration finished");
	}

	private static List<Double> enumerateValuesInADD(MDP_Fac myMDP, int addId) {
		List<Double> values = new ArrayList<Double>();
		
		List<Integer> variables = new ArrayList<Integer>(myMDP.hmPrimeRemap.keySet());
		
		HashMap state = new HashMap();
		
		enumerateValuesInADDRecursive(values, myMDP, addId, state, variables, 0);
		
		return values;
	}

	private static void enumerateValuesInADDRecursive(List<Double> values, MDP_Fac myMDP, int addId,
			HashMap state, List<Integer> variables, int index) {
		if (index >= variables.size()) {
			Double value = myMDP.context.getValueForStateInADD(addId, state, null, null, null);
			values.add(value);
		}
		else {
			state.put(variables.get(index), true);
			enumerateValuesInADDRecursive(values, myMDP, addId, state, variables, index + 1);
			
			state.put(variables.get(index), false);
			enumerateValuesInADDRecursive(values, myMDP, addId, state, variables, index + 1);
		}
	}
}
