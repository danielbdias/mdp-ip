package mdp;

import java.io.*;
import java.text.DecimalFormat;
import java.util.*;

public class AggResults {

   public static final String PREFIX_IN = "/home/karina/ADDVer2/ADD/reportsMDPIP/";
   public static final String PREFIX_OUT = "/home/karina/ADDVer2/ADD/reportsMDPIP/results/";
   public static final String SUFFIX = ".txt";
   public static int[] SIZES = new int[] { 3,4,5,6, 7,8};
   public static int[] PRECS = new int[] { 0, 10, 20, 30, 40, 50 };
   public static String[] ALGS = new String[] { "bi_ring","indep_ring", "uni_ring" };
   public static DecimalFormat _df = new DecimalFormat("#.##");

  //Format input
  //uni_ring_6.txt
  //  PREC         TYPE  
  //  O            ADD
  //  0            Table
  //  10           ADD
  //  10           Table  
  // and so on     
   public static void main(String[] args) {

               BufferedReader in = null;
               PrintStream ps_out = null;
               try {

                       // First Generate a table for each file X size with
                       //                      Table     Tm[2]   Table  Sz[3]       ADD Tm[2]      ADD Sz[3]
                       // prec=0       ...
                       // prec=10      ...

               for (int alg = 0; alg < ALGS.length; alg++) {

               for (int sz = 0; sz < SIZES.length; sz++) {

                       ps_out = new PrintStream(new FileOutputStream(
                                       PREFIX_OUT + "varprec_" + ALGS[alg] + "_" + SIZES[sz] + SUFFIX));

                       String input_file = PREFIX_IN + ALGS[alg] +"_" + SIZES[sz] +  SUFFIX;
                       System.out.println("Processing: " + input_file);
                       in = new BufferedReader(new FileReader(input_file));
                       for (int pr = 0; pr < PRECS.length; pr++) {
                               ps_out.print(PRECS[pr] + "\t");
                               //in.readLine(); // Burn first line (for unused Table comparison)
                               String line1 = in.readLine();
                               String[] add = line1.split("  ");
                               String line2 = in.readLine();
                               String[] table = line2.split("  ");
                               if (line2.compareTo("")!=0){
                            	   ps_out.println(add[5] + "\t" + add[9] + "\t" + table[5] + "\t" + table[9]);
                               }
                               else{
                            	   ps_out.println(add[5] + "\t" + add[9] + "\t" + "-" + "\t" + "-");
                               }
                       }
                       in.close();
                       ps_out.close();
               }
               }


                       // Next Generate a table for each file X prec with
                       //                      ADD     Tm[2]   ADD Sz[3]       AADD Tm[2]      AADD Sz[3]
                       // size=6       ...
                       // size=7       ...

               for (int alg = 0; alg < ALGS.length; alg++) {

                       for (int pr = 0; pr < PRECS.length; pr++) {

                       ps_out = new PrintStream(new FileOutputStream(
                                       PREFIX_OUT + "varsize_" + ALGS[alg] + "_" + PRECS[pr] + SUFFIX));

                       for (int sz = 0; sz < SIZES.length; sz++) {

                               String input_file = PREFIX_IN + ALGS[alg] +
                                       "_" + SIZES[sz] + SUFFIX;

                               System.out.println("Processing: " + input_file);

                               ps_out.print(SIZES[sz] + "\t");
                               in = new BufferedReader(new FileReader(input_file));
                               //find the line 
                               for(int i=1;i<=pr;i++){
                            	   in.readLine(); // Burn first line (for unused Table comparison)
                            	   in.readLine();
                               }
                              
                               String line1 = in.readLine();
                               String[] add = line1.split("  ");
                               String line2 = in.readLine();
                               String[] table = line2.split("  ");
                               if (line2.compareTo("")!=0){
                            	   ps_out.println(add[5] + "\t" + add[9] + "\t" + table[5] + "\t" + table[9]);
                               }
                               else{
                            	   ps_out.println(add[5] + "\t" + add[9] + "\t" + "-" + "\t" + "-");
                               }
                               in.close();

                       }

                       ps_out.close();
               }
               }


               }
               catch (Exception ignore) {
                   System.out.println("ERROR: " + ignore);
                   ignore.printStackTrace();
                   System.exit(1);
               }
       }

}