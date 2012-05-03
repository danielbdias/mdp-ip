package evaluator;

import java.io.*;
import java.security.*;
import java.text.*;
import java.util.*;

import mdp.*;

public class PolicyEvaluator {
   
	// For printing
	public static DecimalFormat df = Config.getConfig().getFormat();

	/**
	 * This program is used to evaluate the policy performance
	 * @param args
	 * fileName:  problem name
	 * NAME_FILE_VALUE : file name where is the value ADD returned by the agent
	 * number Initial States
	 * number of samples (number of trajectories samples to average)
	 * tMax: number of steps in each trajectory 
	 * typeMDP coul be MDP MDPIP
	 * type solution: Total RTDP BRTDP RTDPEnum(to evaluate we create  MDP_Fac with type solution RTDP) se BRTDPEnum(to evaluate we create  MDP_Fac with type solution RTDP)  
	 * NAME_FILE_REPORT
	 */
	public static void main(String[] args) {
		
		try {
			String instanceDescriptionFilePath = getInstanceDescriptionFileFromArgs(args, 0);
			String valueFunctionFilePath = getValueFunctionFileFromArgs(args, 1);
			int numberInitialStates = getNumberOfInitialStatesFromArgs(args, 2);
			int numberSamples = getNumberOfSamplesFromArgs(args, 3);
			int horizonSize = getHorizonSizeFromArgs(args, 4);
			MDPType mdpType = getMDPTypeFromArgs(args, 5);
			String solutionType = getSolutionTypeFromArgs(args, 6);
			String reportFilePath = getReportFileFromArgs(args, 7);
			int simulationType = getSimulationTypeFromArgs(mdpType, args, 8); 
			
			int typeContext = 1; //ADD
			int typeAproxPol= 0;
			MDP myMDP;
			
			System.out.printf("Creating MDP for %s...", instanceDescriptionFilePath);
			System.out.println();
			
			if (solutionType.equals("RTDPEnum") || solutionType.equals("BRTDPEnum"))
				myMDP = new MDP_Fac(instanceDescriptionFilePath, typeContext, typeAproxPol, "RTDP", true);//create an MDP or an MDPIP automatically
			else
				myMDP = new MDP_Fac(instanceDescriptionFilePath, typeContext, typeAproxPol, solutionType, true);//create an MDP or an MDPIP automatically
			
			System.out.printf("Starting simulation...");
			System.out.println();
			
			ArrayList<Double> result = null;
			
			if (mdpType == MDPType.MDPIP)
				result = myMDP.simulateMDPIP(numberInitialStates, numberSamples, horizonSize, valueFunctionFilePath, simulationType);
			else
				result = myMDP.simulateMDPFromFile(numberInitialStates, numberSamples, horizonSize, valueFunctionFilePath, solutionType);

			System.out.printf("Simulation ended.");
			System.out.println();
			
			printReport(valueFunctionFilePath, result, reportFilePath);
		} catch (Exception e) {
			System.err.println("Exception in simulation...");
			System.err.println("Error:");
			e.printStackTrace(System.err);
		}
	}
	
	private static String getReportFileFromArgs(String[] args, int index) {
		String reportFile = getStringFromArgs(args, index);
		
		if (reportFile == null)
			throw new InvalidParameterException("The report file path wasn't informed.");
		
		return reportFile;
	}

	private static String getSolutionTypeFromArgs(String[] args, int index) {
		String solutionType = getStringFromArgs(args, index);
		
		if (solutionType == null)
			throw new InvalidParameterException("The solution type wasn't informed.");
		
		return solutionType;
	}

	private static MDPType getMDPTypeFromArgs(String[] args, int index) {
		String mdpTypeAsString = getStringFromArgs(args, index);
		
		if (mdpTypeAsString == null)
			throw new InvalidParameterException("The MDP type wasn't informed.");
		
		if (mdpTypeAsString.equalsIgnoreCase("MDP"))
			return MDPType.MDP;
		else if (mdpTypeAsString.equalsIgnoreCase("MDPIP"))
			return MDPType.MDPIP;
		else
			throw new InvalidParameterException("The MDP type must be MDP or MDPIP.");
	}

	private static int getHorizonSizeFromArgs(String[] args, int index) {
		String horizonSizeAsString = getStringFromArgs(args, index);
		
		if (horizonSizeAsString == null)
			throw new InvalidParameterException("The horizon size wasn't informed.");
		
		if (!horizonSizeAsString.matches("[0-9]+"))
			throw new InvalidParameterException("The horizon size isn't numeric.");
		
		return Integer.valueOf(horizonSizeAsString);
	}

	private static int getNumberOfSamplesFromArgs(String[] args, int index) {
		String numberOfSamplesAsString = getStringFromArgs(args, index);
		
		if (numberOfSamplesAsString == null)
			throw new InvalidParameterException("The number of samples wasn't informed.");
		
		if (!numberOfSamplesAsString.matches("[0-9]+"))
			throw new InvalidParameterException("The number of samples isn't numeric.");
		
		return Integer.valueOf(numberOfSamplesAsString);
	}

	private static int getNumberOfInitialStatesFromArgs(String[] args, int index) {
		String numberOfInitialStatesAsString = getStringFromArgs(args, index);
		
		if (numberOfInitialStatesAsString == null)
			throw new InvalidParameterException("The number of initial states wasn't informed.");
		
		if (!numberOfInitialStatesAsString.matches("[0-9]+"))
			throw new InvalidParameterException("The number of initial states isn't numeric.");
		
		return Integer.valueOf(numberOfInitialStatesAsString);
	}

	private static String getStringFromArgs(String[] args, int index) {
		if (args == null) return null;
		if (args.length < index + 1) return null;
		
		return args[index];
	}
	
	private static String getValueFunctionFileFromArgs(String[] args, int index) {
		String valueFunctionFilePath = getStringFromArgs(args, index);
		
		if (valueFunctionFilePath == null)
			throw new InvalidParameterException("The value function file wasn't informed.");
		
		File valueFunctionFile = new File(valueFunctionFilePath);
		
		if (!valueFunctionFile.exists())
			throw new InvalidParameterException(
				String.format("The value function file located in [%s] does not exist.", valueFunctionFilePath));
		
		return valueFunctionFilePath;
	}
	
	private static String getInstanceDescriptionFileFromArgs(String[] args, int index) {	
		String instanceDescriptionFilePath = getStringFromArgs(args, index);
		
		if (instanceDescriptionFilePath == null)
			throw new InvalidParameterException("The instance description file wasn't informed.");
		
		File instanceDescriptionFile = new File(instanceDescriptionFilePath);
		
		if (!instanceDescriptionFile.exists())
			throw new InvalidParameterException(
				String.format("The instance description file located in [%s] does not exist.", instanceDescriptionFilePath));
		
		return instanceDescriptionFilePath;
	}

	private static int getSimulationTypeFromArgs(MDPType mdpType, String[] args, int index) {	
		if (mdpType == MDPType.MDP) return -1;
		
		String simulationTypeAsString = getStringFromArgs(args, index);
		
		if (simulationTypeAsString == null)
			throw new InvalidParameterException("The simulation type wasn't informed.");
		
		if (simulationTypeAsString.equalsIgnoreCase("GlobalMyopicAdversarial"))
			return 1;
		else if (simulationTypeAsString.equalsIgnoreCase("LocalMyopicAdversarial"))
			return 2;
		else if (simulationTypeAsString.equalsIgnoreCase("NonStationary"))
			return 3;
		else if (simulationTypeAsString.equalsIgnoreCase("Stationary"))
			return 4;
		else
			throw new InvalidParameterException("The simulation type must be GlobalMyopicAdversarial, LocalMyopicAdversarial, NonStationary or Stationary.");
	}

	private static void printReport(String NAME_FILE_VALUE, ArrayList<Double> result, String NAME_FILE_REPORT) {
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(NAME_FILE_REPORT, true));
			
			out.write("mean: " + df.format(result.get(0)) + "  " + df.format(result.get(1)) + "  " + NAME_FILE_VALUE);
			out.write(System.getProperty("line.separator"));
			
			out.close();
		} catch (IOException e) {
			System.out.println("PolicyEvaluator: Problem with the creation of the Report");
			System.exit(0);
		}
	}

}
