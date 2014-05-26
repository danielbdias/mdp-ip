package mdp.simulators;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

import util.Pair;
import mdp.ShortSightedSSPIP;
import mdp.State;
import mdp.algorithms.LRTDPIPEnumWithSSPIP;
import mdp.algorithms.LabeledSSiPP;

public class SSPIPSimulator {

	public static void main(String[] args) {
		//Simulator specific configs
		int planningTime = Integer.parseInt(args[0]);
		
		int simulationRounds = Integer.parseInt(args[1]);
		
		int maxActionsToExecute = Integer.parseInt(args[2]);

		String algorithm = args[3];
		
		String reportFile = args[4];
		
		String problemFilename 	= args[5];
		
		//Planner specific configs
		String[] newArgs = Arrays.copyOfRange(args, 6, args.length);
		
		//Tipo do contexto do problema. Pode ter 3 valores possíveis:
		//1:ADD 2:AditADD 3: Tables
		int typeContext			= 1;
		
		//Tipo da aproximação utilizada. Pode ter 2 valores: 
		//0:normal 1:with lattice
		int typeAproxPol 		= 0; //Como não há aproximação neste algoritmo o valor sempre será 0 (zero).
		
		//Indica que o tipo de algoritmo usado para solucionar o MDP-IP é o RTDP-IP (para efeito de inicialização de variáveis)
		String typeSolution 	= "RTDPIP";
		
		ShortSightedSSPIP myMDP = new ShortSightedSSPIP(problemFilename, typeContext, typeAproxPol, typeSolution);
		
		HashMap<State, Double> valueFunction = executePlanning(algorithm, planningTime, newArgs, myMDP);
		
		Pair result = simulatePolicy(valueFunction, simulationRounds, maxActionsToExecute, myMDP);
		
		int numVariables = myMDP.hmPrime2IdRemap.keySet().size();
		
		buildReport(algorithm, problemFilename, numVariables, result, reportFile);
	}

	private static void buildReport(String algorithm, String problemFilename, int numVariables, Pair result,
			String reportFilePath) {
		
		try {
			File reportFile = new File(reportFilePath);
			boolean reportExists = reportFile.exists();
			
			BufferedWriter out = new BufferedWriter(new FileWriter(reportFile.getAbsolutePath(), true));
			
			if (!reportExists) {
				//imprime header do arquivo			
				out.write("Problema\t");
				out.write("Num. Sucesso\t");
				out.write("Num. Falha\t");
				out.write("% Sucesso\t");
				out.write("Algoritmo\t");
				out.write("Número de variáveis\t");
				out.write(System.getProperty("line.separator"));
			}
			
			Integer success = (Integer) result.get_o1();
			Integer fail = (Integer) result.get_o2();
			
			double percentSuccess = success.doubleValue() / (success + fail);
			
			out.write(reportFilePath + "\t");
			out.write(success + "\t");
			out.write(fail + "\t");
			out.write(percentSuccess + "\t");
			out.write(algorithm + "\t");
			out.write(numVariables + "\t");
			out.write(System.getProperty("line.separator"));
			
			out.close();
		} catch (IOException e) {
			System.out.println("Problem with the creation of the report");
			System.exit(0);
		}
	}

	private static Pair simulatePolicy(HashMap<State, Double> valueFunction,
			int simulationRounds, int maxActionsToExecute, ShortSightedSSPIP myMDP) {
		System.out.println("Simulation started...");
		
		int stateSamplingType = 1;
		
		Random randomGenInitial = new Random(0);
		Random randomGenNextState = new Random(0);
		
		int successCounter = 0;
		int failCounter = 0;
		
		for (int i = 0; i < simulationRounds; i++) {
			boolean result = myMDP.emulatePolicy(valueFunction, randomGenInitial, randomGenNextState, stateSamplingType, maxActionsToExecute);
			
			if (result)
				successCounter++;
			else
				failCounter++;
		}
		
		System.out.println("Simulation finished.");
		
		return new Pair(successCounter, failCounter);
	}

	private static HashMap<State, Double> executePlanning(final String algorithm, int planningTime, final String[] newArgs, final ShortSightedSSPIP myMDP) {
		long maxTime = planningTime * 1000L;
		long initialTime = System.currentTimeMillis();
		
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					runPlanner(algorithm, newArgs, myMDP);
				} catch (Exception e) {
					System.err.println(e.getMessage());
					e.printStackTrace(System.err);
				}
			}
		});
		
		t.run();
		
		while (t.isAlive()) {
			long elapsedTime = System.currentTimeMillis() - initialTime;
			
			if (elapsedTime > maxTime)
				t.interrupt();
		}
		
		return (HashMap<State, Double>) myMDP.VUpper;
	}

	private static void runPlanner(String algorithm, String[] newArgs, ShortSightedSSPIP myMDP) {
		if (algorithm.equalsIgnoreCase("lssipp"))
			LabeledSSiPP.runSimulation(newArgs, myMDP);
		else if (algorithm.equalsIgnoreCase("lrtdp"))
			LRTDPIPEnumWithSSPIP.runSimulation(newArgs, myMDP);

		//TODO: add more algs
	}
}
