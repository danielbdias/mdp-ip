package mdp;

import java.util.Comparator;

public class ActionComparator implements Comparator{
   public int compare(Object o1, Object o2){
	   Action action1= (Action) o1;
	   Action action2= (Action) o2;
	   return action1.getName().compareTo(action2.getName());
   }
   public boolean equals(Object obj){
	   return this==obj;
   }
	
}
