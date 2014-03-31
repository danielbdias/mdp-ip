package generator;

import java.io.*;
import java.util.*;

public class NavigationGen {
	private static final String NUMBER_REGEX = "^[0-9]+$";
	private static final String DOUBLE_REGEX = "^[0-9]+\\.[0-9]+$";
	private static final String VARIABLE_MASK = "x%dy%d"; //e.g. x1y1

	private static final String UNCERTAIN_MOVEMENT_TRANSITION = "%1$s (%1$s ([%4$f]) (%2$s ([0.0]) (%3$s ([%5$f]) ([0.0]))))"; //e.g. x1y3 (x1y3 ([1.0]) (x2y3 ([0.0]) (x1y2 ([1.0]) ([0.0]))))
	private static final String UNCERTAIN_AND_IMPRECISE_MOVEMENT_TRANSITION = "%1$s (%1$s ([%4$f]) (%2$s ([0.0]) (%3$s ([1.00*p%6$d]) ([0.0]))))"; //e.g. x1y3 (x1y3 ([1.0]) (x2y3 ([0.0]) (x1y2 ([0.9*p1]) ([0.0]))))
	private static final String MOVEMENT_TRANSITION = "%1$s (%1$s ([%3$f]) (%2$s ([%4$f]) ([0.0]) ))"; //e.g. x2y3 (x2y3 ([1.0]) (x2y2 ([1.0]) ([0.0]) )) 

	private static final String ABSORVENT_TRANSITION = "%1$s (%1$s ([%2$f]) ([0.00]))"; //e.g. x2y3 (x2y3 ([1.00]) ([0.00]))
	private static final String UNCERTAIN_ABSORVENT_TRANSITION = "%1$s (%1$s ([1.00*p%2$d]) ([0.00]))"; //e.g. x2y3 (x2y3 ([1.00]) ([0.00]))
	private static final String NO_TRANSITION = "%1$s ([0.00])"; //e.g. x1y3 ([0.00])
		
	public static void main(String[] args) {
		if (args == null || args.length != 7) {
			System.out.println("Invalid parameters.");
			help();
			return;
		}
		
		String outputFilePath = args[0];
		String numberOfLinesAsString = args[1];
		String numberOfColumnsAsString = args[2];
		String discountAsString = args[3];
		String toleranceAsString = args[4];
		String minProbOfDisappearAsString = "0.1";
		if (args.length > 5) minProbOfDisappearAsString = args[5];
		String maxProbOfDisappearAsString = "0.9";
		if (args.length > 6) maxProbOfDisappearAsString = args[6];
		
		if (!numberOfColumnsAsString.matches(NUMBER_REGEX) || !numberOfLinesAsString.matches(NUMBER_REGEX)) {
			System.out.println("The number of rows and columns must be numbers !");
			return;
		}
		
		if (!discountAsString.matches(DOUBLE_REGEX) || !toleranceAsString.matches(DOUBLE_REGEX)) {
			System.out.println("The discount and tolerance must be decimal numbers !");
			return;
		}
		
		if (!minProbOfDisappearAsString.matches(DOUBLE_REGEX) || !maxProbOfDisappearAsString.matches(DOUBLE_REGEX)) {
			System.out.println("The robot disappear probabilities must be decimal numbers !");
			return;
		}
		
		Integer numberOfColumns = Integer.parseInt(numberOfColumnsAsString);
		Integer numberOfLines = Integer.parseInt(numberOfLinesAsString);
		
		Double discount = Double.parseDouble(discountAsString);
		Double tolerance = Double.parseDouble(toleranceAsString);
		
		Double minProbOfDisappear = Double.parseDouble(minProbOfDisappearAsString);
		Double maxProbOfDisappear = Double.parseDouble(maxProbOfDisappearAsString);
		
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
		
		generateDomainFile(outputFile, numberOfLines, numberOfColumns, discount, tolerance, minProbOfDisappear, maxProbOfDisappear);
	}
	
	static void help() {
		System.out.println("To use this generator use the parameters:");
		System.out.println("output-file number-of-rows number-of-columns discount-factor tolerance");
	}
	
	private static void generateDomainFile(File outputFile, Integer numberOfLines, Integer numberOfColumns, Double discount, Double tolerance, Double minProbOfDisappear, Double maxProbOfDisappear) {
		String variables = generateVariables(numberOfLines, numberOfColumns);
		
		HashMap<String, List<String>> actions = generateActions(numberOfLines, numberOfColumns, minProbOfDisappear, maxProbOfDisappear);
		
		String reward = generateReward(numberOfLines, numberOfColumns);
		
		List<String> constraints = generateConstraints(numberOfColumns, minProbOfDisappear, maxProbOfDisappear);
		
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
		String variable = String.format(VARIABLE_MASK, numberOfColumns, numberOfLines);
		return String.format("(%s)", variable);
	}

	private static String generateInitialState(Integer numberOfLines) {
		String variable = String.format(VARIABLE_MASK, numberOfLines, 1);
		return String.format("(%s)", variable);
	}

	private static List<String> generateConstraints(Integer numberOfColumns, Double minProbOfDisappear, Double maxProbOfDisappear) {
		
		List<String> constraints = new ArrayList<String>();
		
		int constraint = 1;
		
		constraints.add(String.format("(p%1$d > = %2$f)", constraint, getExistenceProbability(1, numberOfColumns, minProbOfDisappear, maxProbOfDisappear)));
		
		for (int column = 2; column <= numberOfColumns; column++) {
			constraint = column;
			
			constraints.add(String.format("(p%1$d < = %2$f)", constraint, Math.min(getExistenceProbability(column - 1, numberOfColumns, minProbOfDisappear, maxProbOfDisappear) + 0.1, 1.0)));
			constraints.add(String.format("(p%1$d > = %2$f)", constraint, getExistenceProbability(column, numberOfColumns, minProbOfDisappear, maxProbOfDisappear)));
		}
		
		return constraints;
	}
	
	private static String generateReward(Integer numberOfLines, Integer numberOfColumns) {
		List<String> variables = new ArrayList<String>();
		
		for (int x = 1; x <= numberOfColumns; x++)
			for (int y = 1; y <= numberOfLines; y++)
				variables.add(String.format(VARIABLE_MASK, x, y));
		
		String reward = String.format("(%s (0) (-1) )", variables.get(variables.size() - 1));
		
		for (int i = variables.size() - 2; i >= 0; i--)
			reward = String.format("(%s (-1) %s )", variables.get(i), reward);
		
		return reward;
	}

	private static HashMap<String, List<String>> generateActions(Integer numberOfLines, Integer numberOfColumns, Double minProbOfDisappear, Double maxProbOfDisappear) {
		HashMap<String, List<String>> actions = new HashMap<String, List<String>>();
		
		List<String> moveNorthADDs = getMoveNorthADDs(numberOfLines, numberOfColumns, minProbOfDisappear, maxProbOfDisappear);
		actions.put("movenorth", moveNorthADDs);
		
		List<String> moveSouthADDs = getMoveSouthADDs(numberOfLines, numberOfColumns, minProbOfDisappear, maxProbOfDisappear);
		actions.put("movesouth", moveSouthADDs);
		
		List<String> moveEastADDs = getMoveEastADDs(numberOfLines, numberOfColumns, minProbOfDisappear, maxProbOfDisappear);
		actions.put("moveeast", moveEastADDs);
		
		List<String> moveWestADDs = getMoveWestADDs(numberOfLines, numberOfColumns, minProbOfDisappear, maxProbOfDisappear);
		actions.put("movewest", moveWestADDs);
		
		List<String> noopADDs = getNoopADDs(numberOfLines, numberOfColumns, minProbOfDisappear, maxProbOfDisappear);
		actions.put("noop", noopADDs);
		
		return actions;
	}

	private static List<String> getNoopADDs(Integer numberOfLines, Integer numberOfColumns, Double minProbOfDisappear, Double maxProbOfDisappear) {
		List<String> adds = new ArrayList<String>();
		
		for (int x = 1; x <= numberOfColumns; x++) 
			for (int y = 1; y <= numberOfLines; y++) {
				
				String transition = null;
				
				String currentVariable = String.format(VARIABLE_MASK, x, y);
				String goalVariable = String.format(VARIABLE_MASK, numberOfColumns, numberOfLines);
				
				if (y == 1 || (y == numberOfLines && !currentVariable.equals(goalVariable))) //first or last line
					transition = getAbsorventTransitionString(currentVariable);
				else if (y != 1 && y != numberOfLines) //middle lines
					transition = getAbsorventTransitionString(currentVariable, x, numberOfColumns, true);
				else if (currentVariable.equals(goalVariable)) //goal state
					transition = getAbsorventTransitionString(currentVariable);
				
				adds.add(transition);
			}
		
		return adds;
	}

	private static List<String> getMoveNorthADDs(Integer numberOfLines, Integer numberOfColumns, Double minProbOfDisappear, Double maxProbOfDisappear) {
		List<String> adds = new ArrayList<String>();
		
		for (int x = 1; x <= numberOfColumns; x++) 
			for (int y = 1; y <= numberOfLines; y++) {
				
				String transition = null;
				
				String currentVariable = String.format(VARIABLE_MASK, x, y);
				String previousVariable = String.format(VARIABLE_MASK, x, y - 1);
				String goalVariable = String.format(VARIABLE_MASK, numberOfColumns, numberOfLines);
				
				if (y == 1) //first line
					transition = getNoTransitionString(currentVariable);
				else if (y == numberOfLines && !currentVariable.equals(goalVariable)) //last line
					transition = getUncertainMovementTransitionString(currentVariable, previousVariable, goalVariable, 1.0, x, numberOfColumns, false, minProbOfDisappear, maxProbOfDisappear);
				else if (y != 1 && y != numberOfLines) //middle lines
					transition = getUncertainMovementTransitionString(currentVariable, previousVariable, goalVariable, 0.0, x, numberOfColumns, true, minProbOfDisappear, maxProbOfDisappear);
				else if (currentVariable.equals(goalVariable)) //goal state
					transition = getMovementTransitionString(currentVariable, previousVariable);
				
				adds.add(transition);
			}
		
		return adds;
	}

	private static List<String> getMoveSouthADDs(Integer numberOfLines, Integer numberOfColumns, Double minProbOfDisappear, Double maxProbOfDisappear) {
		List<String> adds = new ArrayList<String>();
		
		for (int x = 1; x <= numberOfColumns; x++) 
			for (int y = 1; y <= numberOfLines; y++) {
				
				String transition = null;
				
				String currentVariable = String.format(VARIABLE_MASK, x, y);
				String previousVariable = String.format(VARIABLE_MASK, x, y + 1);
				String goalVariable = String.format(VARIABLE_MASK, numberOfColumns, numberOfLines);
				
				if (y == numberOfLines && !currentVariable.equals(goalVariable)) // last line
					transition = getNoTransitionString(currentVariable);
				else if (y == 1) //first line
					transition = getUncertainMovementTransitionString(currentVariable, previousVariable, goalVariable, 1.0, x, numberOfColumns, false, minProbOfDisappear, maxProbOfDisappear);
				else if (y == numberOfLines - 1 && x == numberOfColumns) //cell above the goal
					transition = getNoTransitionString(currentVariable);
				else if (y != 1 && y != numberOfLines) //middle lines
					transition = getUncertainMovementTransitionString(currentVariable, previousVariable, goalVariable, 0.0, x, numberOfColumns, true, minProbOfDisappear, maxProbOfDisappear);
				else if (currentVariable.equals(goalVariable)) //goal state
					transition = getAbsorventTransitionString(currentVariable);
				
				adds.add(transition);
			}
		
		return adds;
	}
	
	private static List<String> getMoveEastADDs(Integer numberOfLines, Integer numberOfColumns, Double minProbOfDisappear, Double maxProbOfDisappear) {
		List<String> adds = new ArrayList<String>();
		
		for (int x = 1; x <= numberOfColumns; x++) 
			for (int y = 1; y <= numberOfLines; y++) {
				
				String transition = null;
				
				String currentVariable = String.format(VARIABLE_MASK, x, y);
				String previousVariable = String.format(VARIABLE_MASK, x - 1, y);
				String goalVariable = String.format(VARIABLE_MASK, numberOfColumns, numberOfLines);
				
				if (x == 1) // first column
					transition = getNoTransitionString(currentVariable);
				else if (x == numberOfColumns && !currentVariable.equals(goalVariable)) //last column
					if (y == 1 || y == numberOfLines)
						transition = getUncertainMovementTransitionString(currentVariable, previousVariable, goalVariable, 1.0, x, numberOfColumns, false, minProbOfDisappear, maxProbOfDisappear);
					else
						transition = getUncertainMovementTransitionString(currentVariable, previousVariable, goalVariable, 1.0, x, numberOfColumns, true, minProbOfDisappear, maxProbOfDisappear);
				else if (x > 1 && x < numberOfColumns) //middle coluns
					if (y == 1 || y == numberOfLines)
						transition = getUncertainMovementTransitionString(currentVariable, previousVariable, goalVariable, 0.0, x, numberOfColumns, false, minProbOfDisappear, maxProbOfDisappear);
					else
						transition = getUncertainMovementTransitionString(currentVariable, previousVariable, goalVariable, 0.0, x, numberOfColumns, true, minProbOfDisappear, maxProbOfDisappear);
				else if (currentVariable.equals(goalVariable)) //goal state
					transition = getMovementTransitionString(currentVariable, previousVariable);
				
				adds.add(transition);
			}
		
		return adds;
	}
	
	private static List<String> getMoveWestADDs(Integer numberOfLines, Integer numberOfColumns, Double minProbOfDisappear, Double maxProbOfDisappear) {
		List<String> adds = new ArrayList<String>();
		
		for (int x = 1; x <= numberOfColumns; x++) 
			for (int y = 1; y <= numberOfLines; y++) {
				
				String transition = null;
				
				String currentVariable = String.format(VARIABLE_MASK, x, y);
				String previousVariable = String.format(VARIABLE_MASK, x + 1, y);
				String goalVariable = String.format(VARIABLE_MASK, numberOfColumns, numberOfLines);
				
				if (x == numberOfColumns - 1 && y == numberOfLines) //cell before goal
					transition = getMovementTransitionString(currentVariable, previousVariable);
				else if (x == 1) // first column
					if (y == 1 || y == numberOfLines)
						transition = getUncertainMovementTransitionString(currentVariable, previousVariable, goalVariable, 1.0, x, numberOfColumns, false, minProbOfDisappear, maxProbOfDisappear);
					else
						transition = getUncertainMovementTransitionString(currentVariable, previousVariable, goalVariable, 1.0, x, numberOfColumns, true, minProbOfDisappear, maxProbOfDisappear);
				else if (x == numberOfColumns && !currentVariable.equals(goalVariable)) //last column
					transition = getNoTransitionString(currentVariable);
				else if (x > 1 && x < numberOfColumns) //middle columns
					if (y == 1 || y == numberOfLines)
						transition = getUncertainMovementTransitionString(currentVariable, previousVariable, goalVariable, 0.0, x, numberOfColumns, false, minProbOfDisappear, maxProbOfDisappear);
					else
						transition = getUncertainMovementTransitionString(currentVariable, previousVariable, goalVariable, 0.0, x, numberOfColumns, true, minProbOfDisappear, maxProbOfDisappear);
				else if (currentVariable.equals(goalVariable)) //goal state
					transition = getAbsorventTransitionString(currentVariable);
				
				adds.add(transition);
			}
		
		return adds;
	}

	private static String generateVariables(Integer numberOfLines, Integer numberOfColumns) {
		List<String> variables = new ArrayList<String>();
		
		for (int x = 1; x <= numberOfColumns; x++)
			for (int y = 1; y <= numberOfLines; y++)
				variables.add(String.format(VARIABLE_MASK, x, y));
		
		String variablesList = variables.get(0);
		
		for (int i = 1; i < variables.size(); i++)
			variablesList += (" " + variables.get(i));
		
		return variablesList;
	}

	private static String getNoTransitionString(String currentVariable) {
		return String.format(NO_TRANSITION, currentVariable);
	}
	
	private static String getMovementTransitionString(String firstVariable, String secondVariable) {
		return String.format(MOVEMENT_TRANSITION, firstVariable, secondVariable, 1.0, 1.0);
	}
	
	private static String getAbsorventTransitionString(String currentVariable) {
		return getAbsorventTransitionString(currentVariable, -1, -1, false);
	}
	
	private static String getAbsorventTransitionString(String currentVariable, int column, int numberOfColumns, boolean computeExistenceProbability) {
		
		if (computeExistenceProbability) { //linear formula to compute the probability of movement fail		
			return String.format(UNCERTAIN_ABSORVENT_TRANSITION, currentVariable, column);
			//return String.format(ABSORVENT_TRANSITION, currentVariable, 1.0);
		}
		else {
			return String.format(ABSORVENT_TRANSITION, currentVariable, 1.0);
		}
	}
	
	private static String getUncertainMovementTransitionString(String currentVariable, String previousVariable, String goalVariable, double stayInPlaceProbability, int column, int numberOfColumns, boolean computeExistenceProbability, Double minProbOfDisappear, Double maxProbOfDisappear) {		
						
		if (computeExistenceProbability) { //linear formula to compute the probability of movement fail
			double existenceProbability = getExistenceProbability(column, numberOfColumns, minProbOfDisappear, maxProbOfDisappear);
			
			return String.format(UNCERTAIN_AND_IMPRECISE_MOVEMENT_TRANSITION, 
			//return String.format(UNCERTAIN_MOVEMENT_TRANSITION,
				currentVariable, goalVariable, previousVariable, stayInPlaceProbability, existenceProbability, column);
		}
		else {
			return String.format(UNCERTAIN_MOVEMENT_TRANSITION, 
					currentVariable, goalVariable, previousVariable, stayInPlaceProbability, 1.0);
		}
	} 

	private static double getExistenceProbability(int column, int numberOfColumns, Double minProbOfDisappear, Double maxProbOfDisappear) {
		double first_point = maxProbOfDisappear;
		double last_point = minProbOfDisappear;
		
		//linear formula to compute the probability of movement fail
		return first_point + (column - 1) * ((last_point - first_point) / (numberOfColumns - 1));
	}
}