package mdp;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.TreeMap;

import add.Context;

import graph.*;

public class Principal {
	 public static long _lTime;       // For timing purposes
	 public static DecimalFormat df = Config.getConfig().getFormat();
     public final static String  NAME_FILE_AMPL = Config.getConfig().getAmplTempFile();
	/**
	 * @param args
	 * problem file name
	 * max number of iterations
	 * merge Error 
	 * type Context:  1:ADD 2:AditADD 3: Tables
	 * file name Report
	 * type Aproximation for ADD  0:normal 1:with lattice
	 * type MDP: Fact Flat
	 * file name for Value Star (without approximation) NOT= para exato ou para nao calcular o erro
	 * prune after each iteration  True: APRICODD style False:Objective style
	 * type solution: Total RTDP BRTDP MP RTDPEnum BRTDPEnum
	 * -------------------------- RTDP and derivations' parameters-------------------------
	 * max Depth
	 * time Out 
	 * max number of updates
	 * tau parameter for BRTDP
	 * numTrials: to do the learning instead of max number of updates
	 * 
	 * interval: to do the simulations every X intervals for RTDP BRTDP RTDPEnum BRTDPEnum
	 * numTrialsSimulation: number of trials in the simulation
	 * numberInitialStates: number of initial states to be sample in the simulation
	 * numberLearningRuns number of calls to RTDP 
	 */
	   public static void main(String[] args) {
           ////read parameters
		   String filename = args[0];
		   int maxIter   = Integer.parseInt(args[1]);
		   double mergeError=Double.parseDouble(args[2]);
		   int typeContext=Integer.parseInt(args[3]);
		   String fileNameReport = args[4];
		   int typeAproxPol=Integer.parseInt(args[5]);
		   String typeMDP=args[6];
		   String NAME_FILE_VALUE_STAR=args[7];
		   boolean pruneEachIt=Boolean.parseBoolean(args[8]);
		   String typeSolution=args[9];
		   int maxDepth= Integer.parseInt(args[10]);
		   long timeOut=Long.parseLong(args[11]);
		   long maxUpdates=Long.parseLong(args[12]);
		   double tau= Double.parseDouble(args[13]);
		   int numTrials=Integer.parseInt(args[14]);
		   int interval=Integer.parseInt(args[15]);
		   int numTrialsSimulation=Integer.parseInt(args[16]);
		   int numberInitialStates=Integer.parseInt(args[17]);
		   int numberLearningRuns=Integer.parseInt(args[18]);
		   ////
		   MDP myMDP=null;
		   
		   if(typeSolution.compareTo("RTDPEnum")==0 || typeSolution.compareTo("BRTDPEnum")==0){
			   typeContext=3;
		   }
		   
		   if(typeMDP.compareTo("Fact")==0){
			   myMDP=new MDP_Fac(filename,typeContext,typeAproxPol,typeSolution);
		   }
		   else{
			   myMDP=new MDP_Flat(filename,typeContext,typeAproxPol,typeSolution);
		   }

		   if(typeSolution.compareTo("Total")==0){

			   myMDP.pruneAfterEachIt=pruneEachIt;
			   ResetTimer();
			   int contNumNodes=myMDP.solve(maxIter,mergeError);
			   long timeSeg  = GetElapsedTime()/1000; //this time includes dumping V
			   
			   //Calculate the ||V*-valueiDD||_infty/////////////////////////////////////
			   Double Error=null;
			   Object valueStar=null;
			   if (NAME_FILE_VALUE_STAR.compareTo("NOT")!=0){
				   myMDP.context.workingWithParameterized=false;
				   valueStar=myMDP.context.readValueFunction(NAME_FILE_VALUE_STAR);
				   Object DiffDD=myMDP.context.apply(valueStar, myMDP.valueiDD, Context.SUB);
				   Double maxDiff=(Double) myMDP.context.apply(DiffDD, Context.MAXVALUE);
				   Double minDiff=(Double) myMDP.context.apply(DiffDD, Context.MINVALUE);
				   Error=Math.max(maxDiff.doubleValue(),-minDiff.doubleValue());
			   }
			   else{
				   Error=0d;
			   }
               ///////////////////////////////////////////////////////////////////////////
			   System.out.println("Name: "+filename+"  maxIter: " +maxIter+"  mergeError: "+mergeError+"  typeContext: "+typeContext+"  ContNumNodes: "+contNumNodes+" Time: "+timeSeg+" Reuse: "+myMDP.context.contReuse+" No reuse:  "+myMDP.context.contNoReuse+" Reduced to value: "+myMDP.context.numberReducedToValue+" NumCallSolver:  "+myMDP.context.numCallSolver+" Reuse Cache Internal Node instead of  Call Solver: "+myMDP.context.reuseCacheIntNode);
			   System.out.println("Error: "+Error);
			   printReport(filename,maxIter,mergeError, typeContext,contNumNodes,timeSeg,fileNameReport,myMDP.context.contReuse,myMDP.context.contNoReuse,myMDP.context.numberReducedToValue,myMDP.context.numCallSolver,myMDP.context.reuseCacheIntNode,Error,myMDP.pruneAfterEachIt,typeSolution);
		   }
		   //=================RTDP AND BRTDP================================================================
		   else if (typeSolution.compareTo("BRTDP")==0 ||typeSolution.compareTo("RTDP")==0 || typeSolution.compareTo("RTDPEnum")==0||typeSolution.compareTo("BRTDPEnum")==0){
			   myMDP.pruneAfterEachIt = pruneEachIt;
			   
			   ArrayList all_perf=new ArrayList();
			   
			   for (int i = 1; i <= numberLearningRuns; i++){
				   
				   System.out.println("LEARNING RUN: "+i);
				   ArrayList<ArrayList> perf=null;
				   
				   Random randomGenInitial=new Random(19580427+i*7);
				   Random randomGenNextState=new Random(19580800+i*7);
					
				   MDP myMDPSimulator=new MDP_Fac(filename,1,0,"RTDP");//typeContext=1; typeAproxPol= 0;
				   if (typeSolution.compareTo("BRTDP") == 0){
					    //IMPORTANT: for BRTDP you must use VLower to do the simulations
					   perf=myMDP.solveBRTDPFac(maxDepth,timeOut,maxUpdates,tau,typeMDP, typeSolution,numTrials,interval,numTrialsSimulation, numberInitialStates, randomGenInitial, randomGenNextState, myMDPSimulator);
				   }
				   else if (typeSolution.compareTo("RTDP")==0){
					   perf=myMDP.solveRTDPFac(maxDepth, timeOut, maxUpdates, typeMDP, typeSolution, numTrials, interval, numTrialsSimulation, numberInitialStates, randomGenInitial, randomGenNextState, myMDPSimulator);
					   myMDP.flushCachesRTDP(true); //true because we dont want to save Vupper
				   }
				   else if (typeSolution.compareTo("RTDPEnum")==0){
						perf=myMDP.solveRTDPEnum(maxDepth,timeOut,maxUpdates,typeMDP, typeSolution,numTrials,interval,numTrialsSimulation, numberInitialStates, randomGenInitial, randomGenNextState,myMDPSimulator);
				   }
				   else if (typeSolution.compareTo("BRTDPEnum")==0){
				      //IMPORTANT: for BRTDP you must use VLower to do the simulations
				        perf=myMDP.solveBRTDPEnum(maxDepth,timeOut,maxUpdates,tau,typeMDP, typeSolution,numTrials,interval,numTrialsSimulation, numberInitialStates, randomGenInitial, randomGenNextState,myMDPSimulator);
				   }
					
				   all_perf.add(perf);
				}
			   // average all vector
			   averageAllPerfAndPrintInAFile(all_perf,fileNameReport, filename, typeSolution,maxDepth,tau,myMDP);
		   }
		   //====================================================================================
		   else if(typeSolution.compareTo("RTDPIP")==0){
			   myMDP.pruneAfterEachIt = pruneEachIt;
			   			   
			   Random randomGenInitial = new Random(19580434);
			   Random randomGenNextState = new Random(19580807);
			   
			   ResetTimer();
			   
			   ArrayList<Object[]> result = myMDP.solveRTDPIPFac(maxDepth, timeOut, typeSolution, numTrialsSimulation, 
							   					interval, numberInitialStates, randomGenInitial, randomGenNextState);
			   
			   long timeSeg = GetElapsedTime() / 1000;
			   
			   double maxError = Double.NEGATIVE_INFINITY;
			   
			   if (NAME_FILE_VALUE_STAR.compareTo("NOT")!=0){
				   myMDP.context.workingWithParameterized = false;

				   Object valueStar = myMDP.context.readValueFunction(NAME_FILE_VALUE_STAR);
				   
				   for (Object[] item : result) {
					   TreeMap<Integer, Boolean> state = (TreeMap<Integer, Boolean>) item[0];
					   double approximatedValue = (Double) item[1];
					   
					   double optimumValue = myMDP.context.getValueForStateInContext((Integer)valueStar, state, null, null);
	
					   double error = Math.abs(optimumValue - approximatedValue);
					   maxError = Math.max(maxError, error);
				   }
			   }   
			   
			   int contNumNodes = myMDP.context.contNumberNodes(myMDP.VUpper);
			   
			   printReport(filename, maxIter, mergeError, typeContext, contNumNodes, timeSeg, fileNameReport, 
					   myMDP.context.contReuse, myMDP.context.contNoReuse, myMDP.context.numberReducedToValue, 
					   myMDP.context.numCallSolver, myMDP.context.reuseCacheIntNode,
					   maxError, myMDP.pruneAfterEachIt, typeSolution);
			   
			   myMDP.flushCachesRTDP(true); //true because we dont want to save Vupper
		   }
		   else if(typeSolution.compareTo("MP")==0){
			   ResetTimer();
			   myMDP.solveMP();
			   long time1  = GetElapsedTime();
			   long timeSeg=time1/1000;
			   //Calculate the ||V*-valueiDD||_infty/////////////////////////////////////
			   Double Error=null;
			   Object valueStar=null;
			   Object valueApprox=null;
			   if (NAME_FILE_VALUE_STAR.compareTo("NOT")!=0){
				   myMDP.context.workingWithParameterized=false;
				   valueStar=myMDP.context.readValueFunction(NAME_FILE_VALUE_STAR);
				   valueApprox=myMDP.context.readValueFunction(myMDP.NAME_FILE_VALUE);
				   Object DiffDD=myMDP.context.apply(valueStar, valueApprox, Context.SUB);
				   Double maxDiff=(Double) myMDP.context.apply(DiffDD, Context.MAXVALUE);
				   Double minDiff=(Double) myMDP.context.apply(DiffDD, Context.MINVALUE);
				   Error=Math.max(maxDiff.doubleValue(),-minDiff.doubleValue());
			   }
			   else{
				   Error=0d;
			   }
               ///////////////////////////////////////////////////////////////////////////
			   //I use conNumNodes to put the number of constraints in the MP problem
			   printReport(filename,0,0, typeContext,myMDP.contConstraintsMP,timeSeg,fileNameReport,0,0,0,0,0,Error,true,typeSolution);
		   }
	}
	   
	                                                                                                  
	   
	private static void averageAllPerfAndPrintInAFile(ArrayList all_perf, String fileNameReport, String filename, String typeSolution, int maxDepth, double tau, MDP myMDP) {
		   ArrayList<ArrayList> perf=null;
		   ArrayList<Double> sumMeanInterval=new ArrayList<Double>();
		   ArrayList <Long> sumTimeSecInterval=new ArrayList<Long>();
		   ArrayList <Integer> sumContNumNodesInterval=new ArrayList<Integer>();
		   ArrayList <Integer> trialInterval=new ArrayList<Integer>();
		   ArrayList <Integer> sumContUpdatesInterval=new ArrayList<Integer>();
		   ArrayList<ArrayList> listMeanIntervals=new ArrayList();
		   for(int i=0; i<all_perf.size(); i++){
			   perf=(ArrayList<ArrayList>) all_perf.get(i);
			   for(int j=0; j<perf.size(); j++){
				   ArrayList perfInterval=perf.get(j);
			       double meanInterval=(Double) perfInterval.get(0);
			       ArrayList<Double> listMeanInter;
			       if(listMeanIntervals.size()==j){
			    	   listMeanInter=new ArrayList<Double>();
			    	   listMeanIntervals.add(listMeanInter);
			       }
			       listMeanIntervals.get(j).add(meanInterval);
			       double standardErrorInterval=(Double) perfInterval.get(1);//not used 
			       double sigmaInterval=(Double) perfInterval.get(2);//not used
			       long timeSecInterval=(Long) perfInterval.get(3);
			       int contNumNodesInterval=(Integer)perfInterval.get(4);
			       int trial=(Integer)perfInterval.get(5);
			       int contUpdates=(Integer)perfInterval.get(6);
			       if(i==0){
			    	   sumMeanInterval.add(0d);
			    	   sumTimeSecInterval.add(0l);
			    	   sumContNumNodesInterval.add(0);
			    	   trialInterval.add(0);
			    	   sumContUpdatesInterval.add(0);
			       }
			       sumMeanInterval.set(j,sumMeanInterval.get(j)+meanInterval);
			       sumTimeSecInterval.set(j,sumTimeSecInterval.get(j)+timeSecInterval);
			       sumContNumNodesInterval.set(j,sumContNumNodesInterval.get(j)+contNumNodesInterval);
			       trialInterval.set(j, trial);
			       sumContUpdatesInterval.set(j,sumContUpdatesInterval.get(j)+contUpdates);
			   }
		   }
		   ArrayList<Double> mean=new ArrayList();
		   ArrayList<Double> sigma=new ArrayList();
		   ArrayList<Double> standardError=new ArrayList();
		   ArrayList<Long> timeSec=new ArrayList();
		   ArrayList<Integer> contNumNodes=new ArrayList();
		   ArrayList<Integer> contUpdates=new ArrayList();
		   for(int j=0; j<perf.size(); j++){
			   mean.add(sumMeanInterval.get(j)/all_perf.size() );
			   timeSec.add(sumTimeSecInterval.get(j)/all_perf.size() );
			   contNumNodes.add(sumContNumNodesInterval.get(j)/all_perf.size() );
			   contUpdates.add(sumContUpdatesInterval.get(j)/all_perf.size());
		   }
		   for(int j=0; j<perf.size(); j++){
			   double sigmaInt=myMDP.calculateStandarD(mean.get(j),listMeanIntervals.get(j));
			   sigma.add(sigmaInt);
			   standardError.add(sigmaInt/Math.sqrt(listMeanIntervals.get(j).size()));
		   }
		   for(int j=0; j<perf.size(); j++){
			   printReport(fileNameReport, filename, typeSolution, mean.get(j) , sigma.get(j), standardError.get(j), timeSec.get(j),  contNumNodes.get(j), trialInterval.get(j), maxDepth,tau,contUpdates.get(j));
		   }
	}
	private static void printReport(String fileNameReport, String filename, String typeSolution, Double mean, Double sigma,Double standardError,Long timeSec, Integer contNumNodes, Integer trialInterval, int maxDepth, double tau,int contUpdatesInterval) {
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(fileNameReport,true));
			out.write(filename+"  "+typeSolution+"  "+mean+"  "+sigma+"  "+standardError+"  "+timeSec+"  "+contNumNodes+"  "+trialInterval+"  "+maxDepth+"  "+tau + "  "+contUpdatesInterval);
			out.write(System.getProperty("line.separator"));
			out.close();
		} catch (IOException e) {
			System.out.println("Principal: Problem with the creation of the Report");
			System.exit(0);
		}
	}
	private static void printReportParcial(String filename, String typeSolution,int contNumNodes, long timeSeg, String fileNameReport, long maxUpdates, int contbreak,int maxDepth,double tau, int contUpperUpdates) {
		try {

			BufferedWriter out = new BufferedWriter(new FileWriter(fileNameReport,true));
			out.write(filename+"  "+typeSolution+"  "+contNumNodes+"  "+timeSeg+"  "+maxUpdates+"  "+contbreak+"  "+maxDepth+"  "+tau+"  "+contUpperUpdates);
			out.write(System.getProperty("line.separator"));
			out.close();
		} catch (IOException e) {
			System.out.println("Principal: Problem with the creation of the Report");
			System.exit(0);
		}
	}
	private static void printReport(String filename, int maxIter, double mergeError, int typeContext, int contNumNodes, long timeSeg, String fileNameReport,int contReuse,int contNoReuse,int numberReducedToValue,int numCallSolver,int reuseCacheIntNode,Double Error, boolean pruneAfterEachIt, String typeSolution) {
		String typeCon=new String();
		String typeAprox=new String();
		if (typeContext==1){
			typeCon="ADD";
		}
		else if (typeContext==2){
			typeCon="AditiveADD";
		}
		else{
			typeCon="Table";
		}
        if (pruneAfterEachIt==true){
        	typeAprox="APRI";
        }
        else{
        	typeAprox="REGR";
        }
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(fileNameReport,true));
			String tex=filename+"  "+maxIter+"  "+mergeError+"  "+ typeCon+"  "+typeAprox+"  "+contNumNodes+"  "+timeSeg+"   "+contReuse+"   "+contNoReuse+"  "+  numberReducedToValue +"  "+numCallSolver+"  "+reuseCacheIntNode;
			if (typeSolution.compareTo("MP")==0){
				tex=tex+" "+typeSolution;
			}
			if (typeSolution.compareTo("RTDPIP")==0){
				tex=tex+" "+typeSolution;
			}

			out.write(tex);
			if (Error != null && Error != Double.NEGATIVE_INFINITY){
				out.write(" "+df.format(Error));
			}			
			out.write(System.getProperty("line.separator"));
			out.close();
		} catch (IOException e) {
			System.out.println("Principal: Problem with the creation of the Report");
			System.exit(0);
		}
	}
	public static void ResetTimer() {
		_lTime = System.currentTimeMillis();
	}
	// Get the elapsed time since resetting the timer
	public static long GetElapsedTime() {
		return System.currentTimeMillis() - _lTime;
	}
}
