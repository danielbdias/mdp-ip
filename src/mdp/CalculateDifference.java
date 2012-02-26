package mdp;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Iterator;

import add.Context;
import add.ContextADD;

public class CalculateDifference {
//	 For printing
    public static DecimalFormat df = new DecimalFormat("#.######");
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		int beginProblem=Integer.parseInt(args[0]);
		int endProblem=Integer.parseInt(args[1]);
		String NAME_FILE_VALUE=args[2];
		String fileNameReport = args[3];
		//This is the form of the NAME_FILE_VALUE	/home/karina/ADDVer2/ADD/reportsMDPIP/valueuni_ring_IP
		//This is the part that I need to add to the name _6_0_0.net
		HashMap problemAprox2Error=new HashMap();
		for(int numberProb=beginProblem; numberProb<=endProblem; numberProb++){		    	
			String NAME_FILE_VALUE_STAR=NAME_FILE_VALUE+"_"+numberProb+"_0_0.net";
			ContextADD context=new ContextADD();
			context.workingWithParameterized=false;
			Double Error=null;
			Object valueStar=context.readValueFunction(NAME_FILE_VALUE_STAR);
			
			for(int prec=1;prec<=10;prec++){
				String NAME_FILE_VALUE_iDD;
				if(prec==10){
					NAME_FILE_VALUE_iDD=NAME_FILE_VALUE+"_"+numberProb+"_1_0.net";
				}
				else{
					NAME_FILE_VALUE_iDD=NAME_FILE_VALUE+"_"+numberProb+"_0_"+prec+".net";
				}
				Object valueiDD=context.readValueFunction(NAME_FILE_VALUE_iDD);
				Object DiffDD=context.apply(valueStar, valueiDD, Context.SUB);
				Double maxDiff=(Double) context.apply(DiffDD, Context.MAXVALUE);
				Double minDiff=(Double) context.apply(DiffDD, Context.MINVALUE);
				Error=Math.max(maxDiff.doubleValue(),-minDiff.doubleValue());
				System.out.println(NAME_FILE_VALUE_iDD+"Error: "+df.format(Error));
				try {

					BufferedWriter out = new BufferedWriter(new FileWriter(fileNameReport,true));
					out.write(NAME_FILE_VALUE_iDD+"  "+df.format(Error));
					out.write(System.getProperty("line.separator"));
					out.close();
				} catch (IOException e) {
					System.out.println("Principal: Problem with the creation of the Report");
					System.exit(0);
				}

				problemAprox2Error.put(NAME_FILE_VALUE_iDD, Error);
			}
		}
		//printReport(fileNameReport,problemAprox2Error);
	}
	
	/*private static void printReport(String fileNameReport,HashMap problemAprox2Error) {
		
			
		try {

			BufferedWriter out = new BufferedWriter(new FileWriter(fileNameReport,true));
			Iterator it=problemAprox2Error.keySet().iterator();
			while(it.hasNext()){
				String name=(String)it.next();
				out.write(name+"   "+problemAprox2Error.get(name));
				out.write(System.getProperty("line.separator"));
			}
			out.close();
		} catch (IOException e) {
			System.out.println("Principal: Problem with the creation of the Report");
			System.exit(0);
		}

	}*/
}
