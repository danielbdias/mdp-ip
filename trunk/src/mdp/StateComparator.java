package mdp;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Comparator;

public class StateComparator implements Comparator{
	   public int compare(Object o1, Object o2){
		   BigInteger idState1= ((State) o1).getIdentifier();
		   BigInteger idState2= ((State) o2).getIdentifier();
		 /*  ArrayList<Boolean> l1= state1.getValues();
		   ArrayList<Boolean> l2= state2.getValues();
		   for (int i=0;i<l1.size();i++){
		       if(l1.get(i).compareTo(l2.get(i))!=0){
		    	   return l1.get(i).compareTo(l2.get(i));
		       }
		   }*/
		   return idState1.compareTo(idState2);
	   }
	   public boolean equals(Object obj){
		   return this==obj;
	   }
		
	
}
