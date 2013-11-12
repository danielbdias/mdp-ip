package mdp.algorithms;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

import mdp.ShortSightedSSPIP;
import mdp.State;
import mdp.algorithms.ssipp.SSiPP_LRTDPCaller;
import mdp.algorithms.ssipp.SSiPP_PlannerCaller;

public class LabeledSSiPP {

	public static void main(String[] args) {
		//Nome do arquivo com a descrição do domínio e do problema a ser resolvido
		String problemFilename 	= args[0];
		
		//Nome do arquivo de relatório de execução
		String outputFilename 	= args[1];
		
		//Profundidade máxima que um trial do RTDP-IP pode assumir
		int maxTrialDepth 		= Integer.parseInt(args[2]);
		
		//Tempo máximo de execução do algoritmo
		long timeOut 			= Long.parseLong(args[3]);
		
		//Indica o tipo de amostragem de estados que será utilizado
		//Pode variar de 1 a 4, sendo:
		//1: sorteio min, utilizando o resultado da minimização do Bellman Backup 
		//2: if p=0  => p=epsilon 
		//3: using  the result of a problem with constraints p>= epsilon 
		//4: sorteio random, sorteando aleatoriamente as os parâmetros de probabilidades do conjunto credal 
		int stateSamplingType   = Integer.parseInt(args[4]);
		
		//Profundidade escolhida para o ShortSighted
		int t 					= Integer.parseInt(args[5]);
		
		//Arquivo estado de valores do estado inicial
		String initialStateLogPath   = null;
		if (args.length > 6) initialStateLogPath = args[6];
		
		//Tipo do contexto do problema. Pode ter 3 valores possíveis:
		//1:ADD 2:AditADD 3: Tables
		int typeContext			= 1;
		
		//Tipo da aproximação utilizada. Pode ter 2 valores: 
		//0:normal 1:with lattice
		int typeAproxPol 		= 0; //Como não há aproximação neste algoritmo o valor sempre será 0 (zero).
		
		//Indica que o tipo de algoritmo usado para solucionar o MDP-IP é o RTDP-IP (para efeito de inicialização de variáveis)
		String typeSolution 	= "RTDPIP";
		
		Random randomGenInitial = new Random(19580434);
		Random randomGenNextState = new Random(19580807);
		
		ShortSightedSSPIP myMDP = new ShortSightedSSPIP(problemFilename, typeContext, typeAproxPol, typeSolution);
		
		SSiPP_PlannerCaller planner = new SSiPP_LRTDPCaller(myMDP);
		
		long startTime = System.currentTimeMillis();
		
		myMDP.executeLabeledSSiPPuntilConvergence(t, randomGenInitial, randomGenNextState, maxTrialDepth, timeOut, stateSamplingType, planner, initialStateLogPath);
				
		long timeSeg = (System.currentTimeMillis() - startTime) / 1000;
		 
		int numVariables = myMDP.hmPrime2IdRemap.keySet().size();
		
		//Corrige o tipo de algoritmo usado para solucionar o MDP-IP
		typeSolution = "LabeledSSiPP";
		
		printReport(problemFilename, typeContext, timeSeg, outputFilename, 
				   myMDP.context.numCallNonLinearSolver, myMDP.contUpperUpdates, 
				   typeSolution, numVariables);
	}
	
	private static void printReport(String filename, int typeContext, long timeSeg, 
			String fileNameReport, int numCallSolver, int numBackups, 
			String typeSolution, int numVariables) {

		String typeCon = "Table";
		String typeAprox = "REGR";
		
		try {
			File reportFile = new File(fileNameReport);
			boolean reportExists = reportFile.exists();
			
			BufferedWriter out = new BufferedWriter(new FileWriter(reportFile.getAbsolutePath(), true));
			
			if (!reportExists) {
				//imprime header do arquivo			
				out.write("Problema\t");
				out.write("Contexto\t");
				out.write("Aproximação\t");
				out.write("Tempo de execução\t");
				out.write("Chamadas ao Solver\t");
				out.write("Número de Backups\t");
				out.write("Algoritmo\t");
				out.write("Número de variáveis\t");
				out.write(System.getProperty("line.separator"));
			}
			
			out.write(filename + "\t");
			out.write(typeCon + "\t");
			out.write(typeAprox + "\t");
			out.write(timeSeg + "\t");
			out.write(numCallSolver + "\t");
			out.write(numBackups + "\t");
			out.write(typeSolution + "\t");
			out.write(numVariables + "\t");
			out.write(System.getProperty("line.separator"));
			
			out.close();
		} catch (IOException e) {
			System.out.println("Problem with the creation of the report");
			System.exit(0);
		}
	}
}
