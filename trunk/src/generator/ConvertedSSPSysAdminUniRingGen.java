package generator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import mdp.Config;

public class ConvertedSSPSysAdminUniRingGen {
	private static final String NUMBER_REGEX = "^[0-9]+$";
	private static final String DOUBLE_REGEX = "^[0-9]+\\.[0-9]+$";
	private static final String VARIABLE_MASK = "c%d"; //e.g. c1
	private static final String GOAL_VARIABLE = "goal";
	
	public static void main(String[] args) {
		if (args == null || args.length != 4) {
			System.out.println("Invalid parameters.");
			help();
			return;
		}
		
		String outputFilePath = args[0];
		String instanceAsString = args[1];
		String discountAsString = args[2];
		String toleranceAsString = args[3];
		
		if (!instanceAsString.matches(NUMBER_REGEX)) {
			System.out.println("The instance number must be a positive integer !");
			return;
		}
		
		if (!discountAsString.matches(DOUBLE_REGEX) || !toleranceAsString.matches(DOUBLE_REGEX)) {
			System.out.println("The discount and tolerance must be decimal numbers !");
			return;
		}
		
		Integer instanceNumber = Integer.parseInt(instanceAsString);
		
		Double discount = Double.parseDouble(discountAsString);
		Double tolerance = Double.parseDouble(toleranceAsString);
		
		File outputFile = new File(outputFilePath);
		
		if (outputFile.exists())
			System.out.println("The output file exists. The file will be overrided.");
		
		generateDomainFile(outputFile, instanceNumber, discount, tolerance);
	}
	
	static void help() {
		System.out.println("To use this generator use the parameters:");
		System.out.println("output-file instance-number discount-factor tolerance");
	}
	
	private static void generateDomainFile(File outputFile, Integer instanceNumber, Double discount, Double tolerance) {
		Integer computers = instanceNumber;
				
		String variables = generateVariables(computers);
		
		HashMap<String, List<String>> actions = generateActions(computers, discount);
		
		String reward = generateReward(computers);
		
		List<String> constraints = generateConstraints(computers);
		
		String initialState = generateInitialState(computers);
		
		List<String> goalStates = generateGoalStates(computers);
		
		generateFormattedDomainFile(outputFile, variables, actions, reward, constraints, discount, tolerance, initialState, goalStates);
	}

	private static List<String> generateGoalStates(Integer computers) {
		List<String> goals = new ArrayList<String>();
		
		for (List<String> combination : getComputerCombinations(computers)) {		
			String combinationAsString = "";
			
			for (int i = 0; i < combination.size(); i++)
				combinationAsString += (" " + combination.get(i));
			
			goals.add(String.format("(%s%s)", GOAL_VARIABLE, combinationAsString));
		}
		
		return goals;
	}
	
	private static List<List<String>> getComputerCombinations(Integer numberOfComputers) {
		List<String> computers = new ArrayList<String>();
		
		for (int i = 1; i <= numberOfComputers; i++) 
			computers.add(String.format(VARIABLE_MASK, i));
		
		List<List<String>> result = new ArrayList<List<String>>();
		
		getComputerCombinationsRecursive(result, computers, new HashMap<String, Boolean>(), 0);
		
		return result;
	}
	
	private static void getComputerCombinationsRecursive(List<List<String>> result, List<String> computers, HashMap<String, Boolean> composition, int computerIndex) {	
		if (computerIndex == computers.size())
		{
			List<String> computersToAdd = new ArrayList<String>();
			
			for (String key : composition.keySet()) {
				if (composition.get(key))
					computersToAdd.add(key);
			}
			
			result.add(computersToAdd);
		}
		else {
		
			String cell = computers.get(computerIndex);
			
			composition.put(cell, true);
			getComputerCombinationsRecursive(result, computers, composition, computerIndex + 1);
			
			composition.put(cell, false);
			getComputerCombinationsRecursive(result, computers, composition, computerIndex + 1);
		}
	}
	
	private static void generateFormattedDomainFile(File outputFile,
		String variables, HashMap<String, List<String>> actions, String reward, List<String> constraints,
		Double discount, Double tolerance, String initialState, List<String> goalStates) {
		
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
			
			writer.write("variables ");	
			writer.write(String.format("(%s)", variables));
			writer.newLine();
			
			for (String action : actions.keySet()) {
				writer.write("action " + action);
				writer.newLine();
				
				List<String> adds = actions.get(action);
				
				for (String add : adds) {
					writer.write("\t" + add);
					writer.newLine();
				}
				
				writer.write("endaction");
				writer.newLine();
			}
			
			writer.write("reward");
			writer.newLine();
			
			writer.write("\t" + reward);
			writer.newLine();
			
			writer.write("constraints");
			writer.newLine();
			writer.write("\t(");
			writer.newLine();
			
			for (String constraint : constraints) {
				writer.write("\t\t" + constraint);
				writer.newLine();	
			}
			
			writer.write("\t)");
			writer.newLine();
			writer.newLine();
			
			writer.write("discount 1.0");
			writer.newLine();
			
			writer.write("tolerance " + tolerance);
			writer.newLine();
			
			writer.newLine();
			writer.write("initial");
			writer.newLine();
			writer.write(initialState);
			writer.newLine();
			writer.write("endinitial");
			
			writer.newLine();
			writer.write("goal");
			writer.newLine();
			
			for (String goalState : goalStates) {
				writer.write(goalState);
				writer.newLine();	
			}
			
			writer.write("endgoal");
			
			writer.close();
		} catch (IOException e) {
			System.err.println("Error when creating domain file...");
			e.printStackTrace();
		}
	}
	
	private static String generateInitialState(Integer computers) {
		
		String initialState = "(";
		
		for (int i = 1; i <= computers; i+=2)
			initialState += String.format(VARIABLE_MASK, i) + " ";
		
		initialState = initialState.substring(0, initialState.length() - 1);
		initialState += ")";
		
		return initialState;
	}

	private static List<String> generateConstraints(Integer computers) {
		List<String> constraints = new ArrayList<String>();
		
		for (int i = 1; i <= computers; i++) {
			constraints.add(String.format("(p%1$d > = 0.85 + p%2$d)", 2*i-1, 2*i));
			constraints.add(String.format("(p%1$d < = 0.95)", 2*i-1));
			constraints.add(String.format("(p%1$d < = 0.10)", 2*i));
		}
		
		return constraints;
	}
	
	private static String generateReward(Integer computers) {		
		return String.format("(%s (0) %s)", GOAL_VARIABLE, generateRecursiveReward(computers, 1, 0));
	}
	
	private static String generateRecursiveReward(Integer computers, Integer currentComputer, Integer currentReward) {
		if (currentComputer == computers) {
			return String.format("(c%d (%d) (%d))", currentComputer, currentReward, currentReward - 1);
		}
		else {
			String trueBranch = generateRecursiveReward(computers, currentComputer + 1, currentReward);
			String falseBranch = generateRecursiveReward(computers, currentComputer + 1, currentReward - 1);
			
			return String.format("(c%d %s %s)", currentComputer, trueBranch, falseBranch);
		}
	}
	
	private static String generateVariables(Integer computers) {
		
		String variablesList = GOAL_VARIABLE + " ";
		
		for (int i = 1; i <= computers; i++) 
			variablesList += String.format(VARIABLE_MASK, i) + " ";
		
		return variablesList.substring(0, variablesList.length() - 1);
	}
	
	private static HashMap<String, List<String>> generateActions(Integer computers, Double discount) {
		HashMap<String, List<String>> actions = new HashMap<String, List<String>>();
		
		actions.put("noreboot", getActionADDs(computers, discount, 0));
		
		for (int i = 1; i <= computers; i++) 
			actions.put("reboot" + i, getActionADDs(computers, discount, i));
		
		return actions;
	}

	private static List<String> getActionADDs(Integer computers, Double discount, Integer index) {	
		List<String> adds = new ArrayList<String>();
		
		DecimalFormat numberFormat = Config.getConfig().getFormat();
		
		String add = String.format("%1$s (%1$s ([1.0]) ([%2$s]))", GOAL_VARIABLE, numberFormat.format(1.0 - discount));
		adds.add(add);
		
		for (int i = 1; i <= computers; i++) {
			add = String.format(VARIABLE_MASK, i) + " ";
			
			if (i == index) {
				add += String.format("(%1$s (c%2$d ([1.0]) ([0.0])) ([1.0]))", GOAL_VARIABLE, i);
			}
			else {				
				Integer lastComputer = i - 1;
				if (lastComputer <= 0) lastComputer = computers;
				
				int firstParam = 2*i - 1;
				int secondParam = 2*i;
				
				String trueTransition = String.format("(c%1$d ([1*p%2$d]) ([0.5*p%2$d]))", lastComputer, firstParam);
				String falseTransition = String.format("(c%1$d ([1*p%2$d]) ([0.5*p%2$d]))", lastComputer, secondParam);
				
				add += String.format("(%1$s (c%2$d ([1.0]) ([0.0])) (c%2$d %3$s %4$s ) )", GOAL_VARIABLE, i, trueTransition, falseTransition);
			}
			
			adds.add(add);
		}
		
		return adds;
	}
}
