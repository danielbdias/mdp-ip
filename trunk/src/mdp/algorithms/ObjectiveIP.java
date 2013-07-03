package mdp.algorithms;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import mdp.MDP_Fac;

public class ObjectiveIP {
	public static void main(String[] args) {
		//Nome do arquivo com a descrição do domínio e do problema a ser resolvido
		String problemFilename 	= args[0];
		
		//Nome do arquivo de relatório de execução
		String outputFilename 	= args[1];
		
		//Número máximo de iterações que o algoritmo irá assumir
		int maxNumberIterations = Integer.parseInt(args[2]);
		
		//Erro de merge do ObjectiveIP
		double mergeError = Double.parseDouble(args[3]);
		
		String finalVUpperPath  = null;
		if (args.length > 4) finalVUpperPath = args[4];
		
		String initialStateLogPath   = null;
		if (args.length > 5) initialStateLogPath = args[5];
		
		String initVUpperPath   = null;
		if (args.length > 6) initVUpperPath = args[6];
		
		//Tipo do contexto do problema. Pode ter 3 valores possíveis:
		//1:ADD 2:AditADD 3: Tables
		int typeContext			= 1; //ADDs, significando que sempre resolveremos problemas fatorados
		
		//Tipo da aproximação utilizada. Pode ter 2 valores: 
		//0:normal 1:with lattice
		int typeAproxPol 		= 0; //Como não há aproximação neste algoritmo o valor sempre será 0 (zero).
		
		//Indica que o tipo de algoritmo usado para solucionar o MDP-IP é o RTDP-IP.
		String typeSolution 	= "Total";
		   
		MDP_Fac myMDP = new MDP_Fac(problemFilename, typeContext, typeAproxPol, typeSolution);
		
		long startTime = System.currentTimeMillis();
		
		myMDP.solveObjectiveIP(maxNumberIterations, mergeError, finalVUpperPath, initialStateLogPath, initVUpperPath);
		
		int contNumNodes = myMDP.context.contNumberNodes(myMDP.valueiDD);
		   
		long timeSeg = (System.currentTimeMillis() - startTime) / 1000;
				
		printReport(problemFilename, typeContext, contNumNodes, timeSeg, outputFilename, 
				   myMDP.context.contReuse, myMDP.context.contNoReuse, myMDP.context.numberReducedToValue, 
				   myMDP.context.numCallNonLinearSolver, myMDP.context.reuseCacheIntNode,
				   myMDP.pruneAfterEachIt, typeSolution);
	}
	
	private static void printReport(String filename, int typeContext, int contNumNodes, long timeSeg, 
			String fileNameReport, int contReuse, int contNoReuse, int numberReducedToValue, 
			int numCallSolver, int reuseCacheIntNode, boolean pruneAfterEachIt, String typeSolution) {

		String typeCon = "ADD";
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
				out.write("contNumNodes\t");
				out.write("Tempo de execução\t");
				out.write("contReuse\t");
				out.write("contNoReuse\t");
				out.write("numberReducedToValue\t");
				out.write("Chamadas ao Solver\t");
				out.write("reuseCacheIntNode\t");
				out.write("Algoritmo\t");
				out.write(System.getProperty("line.separator"));
			}
			
			out.write(filename + "\t");
			out.write(typeCon + "\t");
			out.write(typeAprox + "\t");
			out.write(contNumNodes + "\t");
			out.write(timeSeg + "\t");
			out.write(contReuse + "\t");
			out.write(contNoReuse + "\t");
			out.write(numberReducedToValue + "\t");
			out.write(numCallSolver + "\t");
			out.write(reuseCacheIntNode + "\t");
			out.write(typeSolution + "\t");
			out.write(System.getProperty("line.separator"));
			
			out.close();
		} catch (IOException e) {
			System.out.println("Problem with the creation of the report");
			System.exit(0);
		}
	}
}