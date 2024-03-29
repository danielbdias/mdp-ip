package generator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import util.Pair;

public class RelaxedTriangleTireWorldGen {
	private static final String NUMBER_REGEX = "^[0-9]+$";
	private static final String DOUBLE_REGEX = "^[0-9]+\\.[0-9]+$";
	private static final String VARIABLE_MASK = "x%dy%d"; //e.g. x1y1
	
	private static final String NO_CHANGE_TRANSITION = "%1$s (%1$s ([1.00]) ([0.00]))";
	
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
		Integer numberOfColumns = (3 + (instanceNumber - 1));
		Integer numberOfLines = (numberOfColumns * 2) - 1;
		
		String variables = generateVariables(numberOfLines, numberOfColumns);
		
		HashMap<String, List<String>> actions = generateActions(numberOfLines, numberOfColumns);
		
		String reward = generateReward(numberOfLines, numberOfColumns);
		
		List<String> constraints = generateConstraints(numberOfLines);
		
		String initialState = generateInitialState(numberOfColumns);
		
		List<String> goalStates = generateGoalStates(numberOfLines, numberOfColumns);
		
		generateFormattedDomainFile(outputFile, variables, actions, reward, constraints, discount, tolerance, initialState, goalStates);
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

	public static int getNumberOfVariablesForInstance(Integer instanceNumber) {
		Integer numberOfColumns = (3 + (instanceNumber - 1));
		Integer numberOfLines = (numberOfColumns * 2) - 1;
		
		List<String> variables = getCellVariables(numberOfLines, numberOfColumns);
		
		return variables.size() + 2;
	}
	
	private static List<String> generateGoalStates(Integer numberOfLines, Integer numberOfColumns) {
		List<String> goals = new ArrayList<String>();
		
		String goalCell = getGoalCell(numberOfLines);
		
		goals.add(String.format("(hasspare flattired %s)", goalCell));
		goals.add(String.format("(hasspare %s)", goalCell));
		goals.add(String.format("(flattired %s)", goalCell));
		goals.add(String.format("(%s)", goalCell));
		
		return goals;
	}

	private static String generateInitialState(Integer numberOfLines) {
		String initialCell = String.format(VARIABLE_MASK, 1, 1);
		return String.format("(hasspare %s)", initialCell);
	}

	private static List<String> generateConstraints(Integer numberOfLines) {
		List<String> constraints = new ArrayList<String>();
		
		int constraint = 1;
		
		constraints.add(String.format("(p%1$d > = %2$f)", constraint, getExistenceProbability(1, numberOfLines)));
		constraints.add(String.format("(p%1$d < = %2$f)", constraint, getExistenceProbability(2, numberOfLines)));
		
		for (int line = 2; line <= numberOfLines-1; line++) {
			constraint = line;
			
			constraints.add(String.format("(p%1$d > = %2$f)", constraint, getExistenceProbability(line - 1, numberOfLines)));
			constraints.add(String.format("(p%1$d < = %2$f)", constraint, getExistenceProbability(line, numberOfLines)));
		}
		
		return constraints;
	}
	
	private static String generateReward(Integer numberOfLines, Integer numberOfColumns) {
		List<String> variables = getCellVariables(numberOfLines, numberOfColumns);
		String goalVariable = getGoalCell(numberOfLines);
		
		String reward = String.format("(%s (0) (-1) )", goalVariable);
		
		for (String variable : variables) {
			if (variable.equals(goalVariable)) continue;
			
			reward = String.format("(%s (-1) %s )", variable, reward);
		}			
		
		return reward;
	}
	
	private static String generateVariables(Integer numberOfLines, Integer numberOfColumns) {
		List<String> variables = new ArrayList<String>();
		
		variables.add("hasspare");
		variables.add("flattired");
		
		variables.addAll(getCellVariables(numberOfLines, numberOfColumns));
		
		String variablesList = variables.get(0);
		
		for (int i = 1; i < variables.size(); i++)
			variablesList += (" " + variables.get(i));
		
		return variablesList;
	}
	
	private static List<Pair> getCellPositions(Integer numberOfLines, Integer numberOfColumns) {
		List<Pair> positions = new ArrayList<Pair>();
		
		for (int x = 1; x <= numberOfColumns; x++)
			for (int y = x; y <= numberOfLines - x + 1; y+=2)
				positions.add(new Pair(x, y));
		
		return positions;
	}
	
	private static List<String> getCellVariables(Integer numberOfLines, Integer numberOfColumns) {
		List<String> variables = new ArrayList<String>();
				
		for (Pair position : getCellPositions(numberOfLines, numberOfColumns)) {
			int x = (Integer) position.get_o1();
			int y = (Integer) position.get_o2();
			
			variables.add(String.format(VARIABLE_MASK, x, y));
		}
		
		return variables;
	}
	
	private static String getGoalCell(Integer numberOfLines) {
		return String.format(VARIABLE_MASK, 1, numberOfLines);
	}
	
	private static double getExistenceProbability(int column, int numberOfColumns) {
		final double first_point = 0.1;
		final double last_point = 0.9;
		
		//linear formula to compute the probability of movement fail
		return first_point + (column - 1) * ((last_point - first_point) / (numberOfColumns - 1));
	}

	private static HashMap<String, List<String>> generateActions(Integer numberOfLines, Integer numberOfColumns) {
		HashMap<String, List<String>> actions = new HashMap<String, List<String>>();
		
		actions.put("changetire", getChangeTireActionADDs(numberOfLines, numberOfColumns));
		
		actions.put("loadtire", getLoadTireActionADDs(numberOfLines, numberOfColumns));
		
		actions.put("movenorth", getMoveNorthActionADDs(numberOfLines, numberOfColumns));
		
		actions.put("movenortheast", getMoveNortheastActionADDs(numberOfLines, numberOfColumns));
		
		actions.put("movenorthwest", getMoveNorthwestActionADDs(numberOfLines, numberOfColumns));
		
		return actions;
	}

	private static List<String> getMoveNortheastActionADDs(Integer numberOfLines, Integer numberOfColumns) {
		int x, y;
		
		List<String> adds = new ArrayList<String>();
		
		adds.add(String.format(NO_CHANGE_TRANSITION, "hasspare"));
		
		String flattiredMaskStart = "flattired (flattired ([1.00])";
		String flattiredMaskMiddle = "([0.00])";
		String flattiredMaskEnd = " )";
		
		for (x = 1; x <= numberOfColumns; x++)
		for (y = x; y <= numberOfLines - x - 1; y+=2) {
			String cell = String.format(VARIABLE_MASK, x, y);
			flattiredMaskStart += String.format(" (%1$s ([1.00*p%2$d])", cell, y);
			flattiredMaskEnd += " )";
		}
		
		adds.add(flattiredMaskStart + flattiredMaskMiddle + flattiredMaskEnd);
		
		List<Pair> cells = getCellPositions(numberOfLines, numberOfColumns);
		
		for (Pair cell : cells) {
			x = (Integer) cell.get_o1();
			y = (Integer) cell.get_o2();
			
			String currentCell = String.format(VARIABLE_MASK, x, y);
			
			if (x == 1 && y != numberOfLines) {
				String mask = "%1$s (%1$s (flattired ([1.00]) ([0.00])) ([0.00]))";
				adds.add(String.format(mask, currentCell));
			}
			else if (x == 1 && y == numberOfLines)
				adds.add(String.format(NO_CHANGE_TRANSITION, currentCell));
			else if (y != numberOfLines - x + 1){			
				String previousCell = String.format(VARIABLE_MASK, x - 1, y - 1);
				
				String mask = "%1$s (%1$s (flattired ([1.00]) ([0.00])) (%2$s (flattired ([0.00]) ([1.00])) ([0.00]) ) )";
				adds.add(String.format(mask, currentCell, previousCell));
			}
			else {			
				String previousCell = String.format(VARIABLE_MASK, x - 1, y - 1);
				
				String mask = "%1$s (%1$s ([1.00]) (%2$s (flattired ([0.00]) ([1.00])) ([0.00]) ) )";
				adds.add(String.format(mask, currentCell, previousCell));
			}
		}
		
		return adds;
	}
	
	private static List<String> getMoveNorthwestActionADDs(Integer numberOfLines, Integer numberOfColumns) {
		int x, y;
		
		List<String> adds = new ArrayList<String>();
		
		adds.add(String.format(NO_CHANGE_TRANSITION, "hasspare"));
		
		String flattiredMaskStart = "flattired (flattired ([1.00])";
		String flattiredMaskMiddle = "([0.00])";
		String flattiredMaskEnd = " )";
		
		for (x = 2; x <= numberOfColumns; x++)
		for (y = x; y <= numberOfLines - x + 1; y+=2) {
			String cell = String.format(VARIABLE_MASK, x, y);
			flattiredMaskStart += String.format(" (%1$s ([1.00*p%2$d])", cell, y);
			flattiredMaskEnd += " )";
		}
		
		adds.add(flattiredMaskStart + flattiredMaskMiddle + flattiredMaskEnd);
		
		List<Pair> cells = getCellPositions(numberOfLines, numberOfColumns);
		
		for (Pair cell : cells) {
			x = (Integer) cell.get_o1();
			y = (Integer) cell.get_o2();
			
			String currentCell = String.format(VARIABLE_MASK, x, y);
			
			if (x == 1 && y == 1) 
				adds.add(String.format(NO_CHANGE_TRANSITION, currentCell));
			else if (x == y) {
				String mask = "%1$s (%1$s (flattired ([1.00]) ([0.00])) ([0.00]))";
				adds.add(String.format(mask, currentCell));
			}
			else if (x == 1){			
				String previousCell = String.format(VARIABLE_MASK, x + 1, y - 1);
				
				String mask = "%1$s (%1$s ([1.00]) (%2$s (flattired ([0.00]) ([1.00])) ([0.00]) ) )";
				adds.add(String.format(mask, currentCell, previousCell));
			}
			else {			
				String previousCell = String.format(VARIABLE_MASK, x + 1, y - 1);
				
				String mask = "%1$s (%1$s (flattired ([1.00]) ([0.00])) (%2$s (flattired ([0.00]) ([1.00])) ([0.00]) ) )";
				adds.add(String.format(mask, currentCell, previousCell));
			}
		}
		
		return adds;		
	}

	private static List<String> getMoveNorthActionADDs(Integer numberOfLines, Integer numberOfColumns) {
		int x, y;
	
		List<String> adds = new ArrayList<String>();
		
		adds.add(String.format(NO_CHANGE_TRANSITION, "hasspare"));
		
		String flattiredMaskStart = "flattired (flattired ([1.00])";
		String flattiredMaskMiddle = "([0.00])";
		String flattiredMaskEnd = " )";
		
		for (x = 1; x <= numberOfColumns; x+=2)
		for (y = x; y <= numberOfLines - x - 1; y+=2) {
			String cell = String.format(VARIABLE_MASK, x, y);
			flattiredMaskStart += String.format(" (%1$s ([1.00*p%2$d])", cell, y);
			flattiredMaskEnd += " )";
		}
		
		adds.add(flattiredMaskStart + flattiredMaskMiddle + flattiredMaskEnd);
				
		List<Pair> cells = getCellPositions(numberOfLines, numberOfColumns);
		
		for (Pair cell : cells) {
			x = (Integer) cell.get_o1();
			y = (Integer) cell.get_o2();
			
			String currentCell = String.format(VARIABLE_MASK, x, y);
						
			if (x % 2 == 1 && x == y && x != numberOfColumns) {
				String mask = "%1$s (%1$s (flattired ([1.00]) ([0.00])) ([0.00]))";
				adds.add(String.format(mask, currentCell));
			}
			else if (x % 2 == 1 && y == numberOfLines - x + 1 && x != numberOfColumns) {
				String previousCell = String.format(VARIABLE_MASK, x, y - 2);
				
				String mask = "%1$s (%1$s ([1.00]) (%2$s (flattired ([0.00]) ([1.00])) ([0.00]) ) )";
				adds.add(String.format(mask, currentCell, previousCell));
			}
			else if (x % 2 == 1 && x != numberOfColumns) {			
				String previousCell = String.format(VARIABLE_MASK, x, y - 2);
				
				String mask = "%1$s (%1$s (flattired ([1.00]) ([0.00])) (%2$s (flattired ([0.00]) ([1.00])) ([0.00]) ) )";
				adds.add(String.format(mask, currentCell, previousCell));
			}
			else 
				adds.add(String.format(NO_CHANGE_TRANSITION, currentCell));
		}
		
		return adds;
	}

	private static List<String> getLoadTireActionADDs(Integer numberOfLines, Integer numberOfColumns) {
		List<String> adds = new ArrayList<String>();
			
		String addStart = "hasspare ";
		String addEnd = "";
		
		for (int x = 2; x < numberOfColumns; x+=2)
			for (int y = x; y <= numberOfLines - x + 1; y+=2) {
				String cell = String.format(VARIABLE_MASK, x, y);
				addStart += String.format("(%s ([1.00]) ", cell);
				addEnd += " )";
			}

		if (numberOfColumns > 3) {
			//Add the first and the last cell in the column
			for (int x = 3; x < numberOfColumns; x+=2) {
				String cell = String.format(VARIABLE_MASK, x, x);
				addStart += String.format("(%s ([1.00]) ", cell);
				addEnd += " )";
				
				cell = String.format(VARIABLE_MASK, x, numberOfLines - x + 1);
				addStart += String.format("(%s ([1.00]) ", cell);
				addEnd += " )";
			}
		}

		addStart += String.format("(%s ([1.00]) ", String.format(VARIABLE_MASK, numberOfColumns, numberOfColumns));
		addEnd += " )";
		
		adds.add(addStart + "(hasspare ([1.0]) ([0.0]) )" + addEnd);
		
		adds.add(String.format(NO_CHANGE_TRANSITION, "flattired"));
		
		for (String cell : getCellVariables(numberOfLines, numberOfColumns))
			adds.add(String.format(NO_CHANGE_TRANSITION, cell));
		
		return adds;
	}

	private static List<String> getChangeTireActionADDs(Integer numberOfLines, Integer numberOfColumns) {
		List<String> adds = new ArrayList<String>();
		
		adds.add("hasspare ([0.00])");
		adds.add("flattired (flattired (hasspare ([0.00]) ([1.00])) ([0.00]))");
		
		for (String cell : getCellVariables(numberOfLines, numberOfColumns))
			adds.add(String.format(NO_CHANGE_TRANSITION, cell));
		
		return adds;
	}
}
