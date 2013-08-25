package mdp.algorithms;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import mdp.MDP_Flat;

public class VI {

	public static void main(String[] args) {
		//Nome do arquivo com a descrição do domínio e do problema a ser resolvido
		String problemFilename 	= args[0];
		
		//Nome do arquivo de relatório de execução
		String outputFilename 	= args[1];

		//Número máximo de iterações que o algoritmo irá assumir
		int maxNumberIterations = Integer.parseInt(args[2]);
		
		String initialStateLogPath   = null;
		if (args.length > 3) initialStateLogPath = args[3];
		
		//Tipo do contexto do problema. Pode ter 3 valores possíveis:
		//1:ADD 2:AditADD 3: Tables
		int typeContext			= 3; //Tables, significando que sempre resolveremos problemas enumerativos
		
		//Tipo da aproximação utilizada. Pode ter 2 valores: 
		//0:normal 1:with lattice
		int typeAproxPol 		= 0; //Como não há aproximação neste algoritmo o valor sempre será 0 (zero).
		
		//Indica que o tipo de algoritmo usado para solucionar o MDP-IP é o RTDP-IP.
		String typeSolution 	= "Total";
		
		MDP_Flat myMDP = new MDP_Flat(problemFilename, typeContext, typeAproxPol, typeSolution);
		
		long startTime = System.currentTimeMillis();
		
		myMDP.solveVI(maxNumberIterations, initialStateLogPath);
		
		long timeSeg = (System.currentTimeMillis() - startTime) / 1000;
		 
		int numVariables = myMDP.hmPrime2IdRemap.keySet().size();
		
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
