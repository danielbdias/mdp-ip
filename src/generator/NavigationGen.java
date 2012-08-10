package generator;

import java.io.*;
import java.util.*;

public class NavigationGen {
	private static final String NUMBER_REGEX = "^[0-9]+$";
	private static final String DOUBLE_REGEX = "^[0-9]+\\.[0-9]+$";
	private static final String VARIABLE_MASK = "x%dy%d"; //e.g. x1y1
	
	private static final String UNCERTAIN_MOVEMENT_TRANSITION = "%1$s (%1$s (%2$s ([0.0]) ([0.0])) (%2$s ([%3$f*p%5$d]) ([%4$f*p%6$d])))"; //e.g. x1y1 (x1y1 (0.0) (x1y2 (p1*0.9) (p2*0.1)))
	private static final String MOVEMENT_TRANSITION = "%1$s (%1$s (%2$s ([0.0]) ([0.0])) (%2$s ([%3$f]) ([%4$f])))"; //e.g. x1y1 (x1y1 (0.0) (x1y2 (0.9) (0.1)))
	private static final String STATIC_TRANSITION = "%1$s ([1.00])"; //e.g. x1y1 ([1.00])
	
	private static final String[] CONSTRAINT_MASKS = {
		"(p%1$d > = 0.85 + p%2$d)",
		"(p%1$d < = 0.95)",
		"(p%2$d < = 0.1)",
	};
	
	public static void main(String[] args) {
		if (args == null || args.length != 5) {
			System.out.println("Invalid parameters.");
			help();
			return;
		}
		
		String outputFilePath = args[0];
		String numberOfLinesAsString = args[1];
		String numberOfColumnsAsString = args[2];
		String discountAsString = args[3];
		String toleranceAsString = args[4];
		
		if (!numberOfColumnsAsString.matches(NUMBER_REGEX) || !numberOfLinesAsString.matches(NUMBER_REGEX)) {
			System.out.println("The number of rows and columns must be numbers !");
			return;
		}
		
		if (!discountAsString.matches(DOUBLE_REGEX) || !toleranceAsString.matches(DOUBLE_REGEX)) {
			System.out.println("The discount and tolerance must be decimal numbers !");
			return;
		}
		
		Integer numberOfColumns = Integer.parseInt(numberOfColumnsAsString);
		Integer numberOfLines = Integer.parseInt(numberOfLinesAsString);
		
		Double discount = Double.parseDouble(discountAsString);
		Double tolerance = Double.parseDouble(toleranceAsString);
		
		if (numberOfColumns < 2) {
			System.out.println("The number of columns must be greater or equal to 2.");
			return;
		}
		
		if (numberOfLines < 3) {
			System.out.println("The number of lines must be greater or equal to 3.");
			return;
		}
		
		File outputFile = new File(outputFilePath);
		
		if (outputFile.exists())
			System.out.println("The output file exists. The file will be overrided.");
		
		generateDomainFile(outputFile, numberOfLines, numberOfColumns, discount, tolerance);
	}
	
	static void help() {
		System.out.println("To use this generator use the parameters:");
		System.out.println("output-file number-of-rows number-of-columns discount-factor tolerance");
	}
	
	private static void generateDomainFile(File outputFile, Integer numberOfLines, Integer numberOfColumns, Double discount, Double tolerance) {
		String variables = generateVariables(numberOfLines, numberOfColumns);
		
		HashMap<String, List<String>> actions = generateActions(numberOfLines, numberOfColumns);
		
		String reward = generateReward(numberOfLines, numberOfColumns);
		
		List<String> constraints = generateConstraints(numberOfColumns);
		
		String initialState = generateInitialState(numberOfColumns);
		
		String goalState = generateGoalState(numberOfLines, numberOfColumns);
		
		generateFormattedDomainFile(outputFile, variables, actions, reward, constraints, discount, tolerance, initialState, goalState);
	}

	private static void generateFormattedDomainFile(File outputFile,
		String variables, HashMap<String, List<String>> actions, String reward, List<String> constraints,
		Double discount, Double tolerance, String initialState, String goalState) {
		
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
			writer.write(goalState);
			writer.newLine();
			writer.write("endgoal");
			
			writer.close();
		} catch (IOException e) {
			System.err.println("Error when creating domain file...");
			e.printStackTrace();
		}
	}

	private static String generateGoalState(Integer numberOfLines, Integer numberOfColumns) {
		String variable = String.format(VARIABLE_MASK, numberOfLines, numberOfColumns);
		return String.format("(%s)", variable);
	}

	private static String generateInitialState(Integer numberOfColumns) {
		String variable = String.format(VARIABLE_MASK, 1, numberOfColumns);
		return String.format("(%s)", variable);
	}

	private static List<String> generateConstraints(Integer numberOfColumns) {
		
		List<String> constraints = new ArrayList<String>();
		
		for (int column = 1; column <= numberOfColumns; column++) {
			int firstConstraint = 2*column - 1;
			int secondConstraint = 2*column;
		
			for (String mask : CONSTRAINT_MASKS)
				constraints.add(String.format(mask, firstConstraint, secondConstraint));
		}
		
		return constraints;
	}
	
	private static String generateReward(Integer numberOfLines, Integer numberOfColumns) {
		List<String> variables = new ArrayList<String>();
		
		for (int line = 1; line <= numberOfLines; line++)
			for (int column = 1; column <= numberOfColumns; column++)
				variables.add(String.format(VARIABLE_MASK, line, column));
		
		String reward = String.format("(%s (0) (-1) )", variables.get(variables.size() - 1));
		
		for (int i = variables.size() - 2; i >= 0; i--)
			reward = String.format("(%s (-1) %s )", variables.get(i), reward);
		
		return reward;
	}

	private static HashMap<String, List<String>> generateActions(Integer numberOfLines, Integer numberOfColumns) {
		HashMap<String, List<String>> actions = new HashMap<String, List<String>>();
		
		List<String> moveNorthADDs = getMoveNorthADDs(numberOfLines, numberOfColumns);
		actions.put("movenorth", moveNorthADDs);
		
		List<String> moveSouthADDs = getMoveSouthADDs(numberOfLines, numberOfColumns);
		actions.put("movesouth", moveSouthADDs);
		
		List<String> moveEastADDs = getMoveEastADDs(numberOfLines, numberOfColumns);
		actions.put("moveeast", moveEastADDs);
		
		List<String> moveWestADDs = getMoveWestADDs(numberOfLines, numberOfColumns);
		actions.put("movewest", moveWestADDs);
		
		return actions;
	}

	private static List<String> getMoveNorthADDs(Integer numberOfLines, Integer numberOfColumns) {
		List<String> adds = new ArrayList<String>();
		
		//Don't consider the first line, because in that line this action does not have an effect
		for (int line = 2; line <= numberOfLines; line++) 
			for (int column = 1; column <= numberOfColumns; column++) {
				//Ignore goal state
				if (line == numberOfLines && column == numberOfColumns) continue;
				
				String upperVariable = String.format(VARIABLE_MASK, line - 1, column);
				String bottomVariable = String.format(VARIABLE_MASK, line, column);
				
				String transition = null;
				
				if (line == 2) //One line after first line 
					//Deterministic transition to next position
					transition = getMovementTransitionString(bottomVariable, upperVariable);
				else //Another lines
					transition = getUncertainMovementTransitionString(bottomVariable, upperVariable, column, numberOfColumns);
				
				adds.add(transition);
			}
		
		//Now consider only the first line
		for (int column = 1; column <= numberOfColumns; column++) {
			String variable = String.format(VARIABLE_MASK, 1, column);
			adds.add(String.format(STATIC_TRANSITION, variable));
		}
		
		//Goal state
		String goalVariable = String.format(VARIABLE_MASK, numberOfLines, numberOfColumns);
		adds.add(String.format(STATIC_TRANSITION, goalVariable));
		
		return adds;
	}
	
	private static List<String> getMoveSouthADDs(Integer numberOfLines, Integer numberOfColumns) {
		List<String> adds = new ArrayList<String>();
		
		//Don't consider the last line, because in that line this action does not have an effect
		for (int line = 1; line <= numberOfLines - 1; line++) 
			for (int column = 1; column <= numberOfColumns; column++) {
				String upperVariable = String.format(VARIABLE_MASK, line, column);
				String bottomVariable = String.format(VARIABLE_MASK, line + 1, column);
				
				String transition = null;
				
				if (line == numberOfLines - 1) //One line before end line 
					//Deterministic transition to next position
					transition = getMovementTransitionString(upperVariable, bottomVariable);
				else //Another lines
					transition = getUncertainMovementTransitionString(upperVariable, bottomVariable, column, numberOfColumns);
				
				adds.add(transition);
			}
		
		//Now consider only the last line (also consider the goal state)
		for (int column = 1; column <= numberOfColumns; column++) {
			String variable = String.format(VARIABLE_MASK, numberOfLines, column);
			adds.add(String.format(STATIC_TRANSITION, variable));
		}
		
		return adds;
	}
	
	private static List<String> getMoveEastADDs(Integer numberOfLines, Integer numberOfColumns) {
		List<String> adds = new ArrayList<String>();
		
		//Don't consider the last column, because in that column this action does not have an effect
		for (int line = 1; line <= numberOfLines; line++) 
			for (int column = 1; column <= numberOfColumns - 1; column++) {
				String leftVariable = String.format(VARIABLE_MASK, line, column);
				String rightVariable = String.format(VARIABLE_MASK, line, column + 1);
				
				String transition = null;
				
				if ((line == 1 || line == numberOfLines) //First and last line
						&& !(line == numberOfLines && column == numberOfColumns)) //Not the goal state  
					//Deterministic transition to next position
					transition = getMovementTransitionString(leftVariable, rightVariable);
				else //Another lines
					transition = getUncertainMovementTransitionString(leftVariable, rightVariable, column + 1, numberOfColumns);
				
				adds.add(transition);
			}
		
		//Now consider only the last column (also includes the goal state)
		for (int line = 1; line <= numberOfLines; line++) {
			String variable = String.format(VARIABLE_MASK, line, numberOfColumns);
			adds.add(String.format(STATIC_TRANSITION, variable));
		}
		
		return adds;
	}
	
	private static List<String> getMoveWestADDs(Integer numberOfLines, Integer numberOfColumns) {
		List<String> adds = new ArrayList<String>();
		
		//Don't consider the first column, because in that column this action does not have an effect
		for (int line = 1; line <= numberOfLines; line++) 
			for (int column = 2; column <= numberOfColumns; column++) {
				//Ignore goal state
				if (line == numberOfLines && column == numberOfColumns) continue;
				
				String leftVariable = String.format(VARIABLE_MASK, line, column - 1);
				String rightVariable = String.format(VARIABLE_MASK, line, column);
				
				String transition = null;
				
				if (line == 1 || line == numberOfLines) //First and last line  
					//Deterministic transition to next position
					transition = getMovementTransitionString(rightVariable, leftVariable);
				else //Another lines
					transition = getUncertainMovementTransitionString(rightVariable, leftVariable, column - 1, numberOfColumns);
				
				adds.add(transition);
			}
		
		//Now consider only the first column
		for (int line = 1; line <= numberOfLines; line++) {
			String variable = String.format(VARIABLE_MASK, line, 1);
			adds.add(String.format(STATIC_TRANSITION, variable));
		}
		
		//Goal state
		String goalVariable = String.format(VARIABLE_MASK, numberOfLines, numberOfColumns);
		adds.add(String.format(STATIC_TRANSITION, goalVariable));
		
		return adds;
	}

	private static String generateVariables(Integer numberOfLines, Integer numberOfColumns) {
		List<String> variables = new ArrayList<String>();
		
		for (int line = 1; line <= numberOfLines; line++)
			for (int column = 1; column <= numberOfColumns; column++)
				variables.add(String.format(VARIABLE_MASK, line, column));
		
		String variablesList = variables.get(0);
		
		for (int i = 1; i < variables.size(); i++)
			variablesList += (" " + variables.get(i));
		
		return variablesList;
	}

	private static String getMovementTransitionString(String firstVariable, String secondVariable) {
		return String.format(MOVEMENT_TRANSITION, firstVariable, secondVariable, 1.0, 0.0);
	}
	
	private static String getUncertainMovementTransitionString(String firstVariable, String secondVariable, int column, int numberOfColumns) {
		int firstConstraint = 2*column - 1;
		int secondConstraint = 2*column;
		
		//linear formula to compute the probability of movement fail
		double falseProbability = 0.1 + (column - 1) * ((0.9 - 0.1) / (numberOfColumns - 1));
		double trueProbability = 1.0 - falseProbability;
		
		return String.format(UNCERTAIN_MOVEMENT_TRANSITION, firstVariable, secondVariable, trueProbability, falseProbability, firstConstraint, secondConstraint);
	} 
}