package evaluator;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;



import mdp.Config;
import mdp.MDP;
import mdp.MDP_Fac;


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
		// TODO Auto-generated method stub
		
		String fileName=args[0];
		String NAME_FILE_VALUE = args[1];
		int numberInitialStates=Integer.parseInt(args[2]);
		int numberSamples   = Integer.parseInt(args[3]);
		int tMax= Integer.parseInt(args[4]);
		String typeMDP=args[5];
		String typeSolution=args[6];
		String NAME_FILE_REPORT =args[7];
		
		int typeContext=1; //ADD
		int typeAproxPol= 0;
		MDP myMDP;
		
		System.out.printf("Creating MDP for %s...", fileName);
		System.out.println();
		
		if(typeSolution.compareTo("RTDPEnum")==0 || typeSolution.compareTo("BRTDPEnum")==0)
			myMDP=new MDP_Fac(fileName,typeContext,typeAproxPol,"RTDP");//create an MDP or an MDPIP automatically
		else
			myMDP=new MDP_Fac(fileName,typeContext,typeAproxPol,typeSolution);//create an MDP or an MDPIP automatically
		
		System.out.printf("Starting simulation...");
		System.out.println();
		
		ArrayList result = null;
		
		if(typeMDP.compareTo("MDPIP")==0)
			result=myMDP.simulateMDPIP(numberInitialStates,numberSamples,tMax,NAME_FILE_VALUE);
		else
			result=myMDP.simulateMDPFromFile(numberInitialStates,numberSamples,tMax,NAME_FILE_VALUE,typeSolution);
	
		System.out.printf("Simulation ended.");
		System.out.println();
		
		printReport(NAME_FILE_VALUE,result,NAME_FILE_REPORT);
	}
	
	private static void printReport(String NAME_FILE_VALUE , ArrayList result,String NAME_FILE_REPORT) {
		;	
		try {

			BufferedWriter out = new BufferedWriter(new FileWriter(NAME_FILE_REPORT,true));
			out.write("mean: "+df.format(result.get(0))+ "  "+df.format(result.get(1))+"  "+NAME_FILE_VALUE);
			out.write(System.getProperty("line.separator"));
			out.close();
		} catch (IOException e) {
			System.out.println("PolicyEvaluator: Problem with the creation of the Report");
			System.exit(0);
		}

	}

}
