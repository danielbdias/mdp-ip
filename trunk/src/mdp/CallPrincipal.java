package mdp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class CallPrincipal {

	/**
	 * @param args
	 */
	
	public static void main(String args[])
	   {
	      Process theProcess = null;
	      BufferedReader inStream = null;
	      
	      for(int numComp=3; numComp<=7; numComp++){
	    	 /* for (int prec=0;prec<=50;prec=prec+10){
	    		  float precision=(float)numComp*prec/100;
	    		  String prog="java mdp.Principal /home/karina/ADDVer2/ADD/problemsMDPIP/uni_ring_IP_"+numComp+".net 50 "+precision+" 1  /home/karina/ADDVer2/ADD/reportsMDPIP/ADDResults.tex 0";
	    		  System.out.println("calling "+prog);
	    		  try
	    		  {

	    			  theProcess = Runtime.getRuntime().exec(prog);
	    			  theProcess.waitFor();
	    			  System.out.println("Finish ADD");
	    		  }catch (InterruptedException ie) {
	    			  System.out.println(ie);
	    			  ie.printStackTrace();
	    		  }catch(IOException e)
	    		  {
	    			  System.err.println("Error on exec() method");
	    			  e.printStackTrace();  
	    		  }
	    		  // read from the called program's standard output stream
	    		  try
	    		  {
	    			  inStream = new BufferedReader(new InputStreamReader( theProcess.getInputStream() ));  
	    			  System.out.println(inStream.readLine());
	    		  }
	    		  catch(IOException e)
	    		  {
	    			  System.err.println("Error on inStream.readLine()");
	    			  e.printStackTrace();  
	    		  }

	    	  }*/
	    	  Process theProcess1 = null;
	    	  String progTable="java mdp.Principal /home/karina/ADDVer2/ADD/problemsMDPIP/uni_ring_IP_"+numComp+".net 50 0 3  /home/karina/ADDVer2/ADD/reportsMDPIP/TableResults.tex 0 Flat";
    		  System.out.println("calling "+progTable);
    		  try
    		  {

    			  theProcess1= Runtime.getRuntime().exec(progTable);
    			  theProcess1.waitFor();
    			  System.out.println("Finish Table");
    		  }catch (InterruptedException ie) {
    			  System.out.println(ie);
    			  ie.printStackTrace();
    		  }catch(IOException e)
    		  {
    			  System.err.println("Error on exec() method");
    			  e.printStackTrace();  
    		  }
    		  // read from the called program's standard output stream
    		  try
    		  {
    			  inStream = new BufferedReader(new InputStreamReader( theProcess1.getInputStream() ));  
    			  System.out.println(inStream.readLine());
    		  }
    		  catch(IOException e)
    		  {
    			  System.err.println("Error on inStream.readLine()");
    			  e.printStackTrace();  
    		  }
	    	  
        	  Process theProcess2 = null;
	    	  String progTable2="java mdp.Principal /home/karina/ADDVer2/ADD/problemsMDPIP/bi_ring_IP_"+numComp+".net 50 0 3  /home/karina/ADDVer2/ADD/reportsMDPIP/TableResults.tex 0 Flat";
    		  System.out.println("calling "+progTable2);
    		  try
    		  {

    			  theProcess2= Runtime.getRuntime().exec(progTable2);
    			  theProcess2.waitFor();
    			  System.out.println("Finish Table");
    		  }catch (InterruptedException ie) {
    			  System.out.println(ie);
    			  ie.printStackTrace();
    		  }catch(IOException e)
    		  {
    			  System.err.println("Error on exec() method");
    			  e.printStackTrace();  
    		  }
    		  // read from the called program's standard output stream
    		  try
    		  {
    			  inStream = new BufferedReader(new InputStreamReader( theProcess2.getInputStream() ));  
    			  System.out.println(inStream.readLine());
    		  }
    		  catch(IOException e)
    		  {
    			  System.err.println("Error on inStream.readLine()");
    			  e.printStackTrace();  
    		  }
         	  Process theProcess3 = null;
	    	  String progTable3="java mdp.Principal /home/karina/ADDVer2/ADD/problemsMDPIP/indep_ring_IP_"+numComp+".net 50 0 3  /home/karina/ADDVer2/ADD/reportsMDPIP/TableResults.tex 0 Flat";
    		  System.out.println("calling "+progTable3);
    		  try
    		  {

    			  theProcess3= Runtime.getRuntime().exec(progTable3);
    			  theProcess3.waitFor();
    			  System.out.println("Finish Table");
    		  }catch (InterruptedException ie) {
    			  System.out.println(ie);
    			  ie.printStackTrace();
    		  }catch(IOException e)
    		  {
    			  System.err.println("Error on exec() method");
    			  e.printStackTrace();  
    		  }
    		  // read from the called program's standard output stream
    		  try
    		  {
    			  inStream = new BufferedReader(new InputStreamReader( theProcess3.getInputStream() ));  
    			  System.out.println(inStream.readLine());
    		  }
    		  catch(IOException e)
    		  {
    			  System.err.println("Error on inStream.readLine()");
    			  e.printStackTrace();  
    		  }	  
	    	  
	      }
	   }
}
