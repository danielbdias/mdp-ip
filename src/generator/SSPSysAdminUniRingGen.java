package generator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SSPSysAdminUniRingGen {
	private static final String NUMBER_REGEX = "^[0-9]+$";
	private static final String DOUBLE_REGEX = "^[0-9]+\\.[0-9]+$";
	private static final String VARIABLE_MASK = "c%d"; //e.g. c1
	
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
		
		HashMap<String, List<String>> actions = generateActions(computers);
		
		String reward = generateReward(computers);
		
		List<String> constraints = generateConstraints(computers);
		
		String initialState = generateInitialState(computers);
		
		List<String> goalStates = generateGoalStates(computers);
		
		generateFormattedDomainFile(outputFile, variables, actions, reward, constraints, discount, tolerance, initialState, goalStates);
	}

	private static List<String> generateGoalStates(Integer computers) {
		List<String> states = new ArrayList<String>();
		
		states.add("(" + generateVariables(computers) + ")");
		
		return states;
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
			
			writer.write("discount " + discount);
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
		String beginRew = "";
		String endRew = "";
		
		for (int i = 1; i <= computers; i++) {
			beginRew += "(" + String.format(VARIABLE_MASK, i) + " ";
			endRew += " (-1) )";
		}
		
		return beginRew + "(0)" + endRew;
	}
	
	private static String generateVariables(Integer computers) {
		
		String variablesList = "";
		
		for (int i = 1; i <= computers; i++) 
			variablesList += String.format(VARIABLE_MASK, i) + " ";
		
		return variablesList.substring(0, variablesList.length() - 1);
	}
	
	private static HashMap<String, List<String>> generateActions(Integer computers) {
		HashMap<String, List<String>> actions = new HashMap<String, List<String>>();
		
		actions.put("noreboot", getActionADDs(computers, 0));
		
		for (int i = 1; i <= computers; i++) 
			actions.put("reboot" + i, getActionADDs(computers, i));
		
		return actions;
	}

	private static List<String> getActionADDs(Integer computers, Integer index) {	
		List<String> adds = new ArrayList<String>();
		
		for (int i = 1; i <= computers; i++) {
			String add = String.format(VARIABLE_MASK, i) + " ";
			
			if (i == index) {
				add += "([1.00])";
			}
			else {
				Integer lastComputer = i - 1;
				if (lastComputer <= 0) lastComputer = computers;
				
				int firstParam = 2*i - 1;
				int secondParam = 2*i;
				
				String begin = "(" + String.format(VARIABLE_MASK, i);
				String end = "";
				
				for (int j = 1; j <= computers; j++) {
					if (j == lastComputer || j == i) continue;
					
					begin += " (" + String.format(VARIABLE_MASK, j);
					end += String.format(" ([0.5*p%1$d]) )", firstParam);
				}
				
				add += begin + String.format(" (c%1$d ([1.0]) ([0.5*p%2$d]) )", lastComputer, firstParam) + end;
				add += String.format(" (c%1$d ([1*p%2$d]) ([0.5*p%2$d]) ) )", lastComputer, secondParam);
			}
			
			adds.add(add);
		}
		
		return adds;
	}
}
