package mdp;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeMap;

import add.BinaryOperKeyADD;
import add.BinaryOperation;

public class State {
	private int identifier;
	private TreeMap<Integer,Boolean> values;
	private SuccProbabilitiesM actionSuccProbab[];
	
	/*public State(int numActions){
		this.values=new ArrayList();
		actionSuccProbab=new  SuccProbabilities[numActions]; //parece que hay que inicializarlo con null
	}*/
	
	public State(TreeMap<Integer,Boolean> values,int identifier){
		this.identifier=identifier;
		this.values=values;
	}
	
	public State(TreeMap<Integer, Boolean> values,int numActions,int identifier){
		this.identifier=identifier;
		this.values=values;
		actionSuccProbab=new SuccProbabilitiesM[numActions];
		for (int i=0;i<numActions;i++){
			actionSuccProbab[i]=null;
		}
	}
	
	public State (TreeMap<Integer, Boolean> values) { // Para LRTDP-IP
	    int identifier=0; // take into account the position (prime) variable  
		Iterator it = values.keySet().iterator();
		while (it.hasNext()) {
		    Integer id=(Integer)it.next();
		    Boolean value=values.get(id);
		    if (value){
		    	identifier= (int) (identifier+Math.pow(2,id)) ;
		    }
		}
		this.identifier=identifier;
		this.values=values;
/*		actionSuccProbab=new  SuccProbabilities[numActions];
		for (int i=0;i<numActions;i++){
			actionSuccProbab[i]=null;
		}*/
	}
	
	
	public void initActionSucc(int numActions){
		actionSuccProbab=new  SuccProbabilitiesM[numActions];
		for (int i=0;i<numActions;i++){
			actionSuccProbab[i]=null;
		}
	}
	
	
	public TreeMap<Integer,Boolean> getValues(){
		return values;
	}
	public SuccProbabilitiesM [] getActionSuccProbab(){
		return actionSuccProbab;
	}
	
	
	public int getIdentifier(){
		return identifier;
	}

	public String  toString(){
		String part= "state: "+identifier;
		/*part=part+ "  values: "+values.toString()+" Succ action (total actions "+actionSuccProbab.length+") : [";
		for(int i=0;i<actionSuccProbab.length;i++){
			if(actionSuccProbab[i]!=null)
			    part=part+actionSuccProbab[i].toString()+";";
			else{
				part=part+"null;";
			}
		}*/
		return part;
		//+"]";
	}
	
	
	
	public int hashCode() {
		return new Integer(identifier).hashCode();  // TODO: define hashCode()
	}
	
    public boolean equals(Object other) {
        // Not strictly necessary, but often a good optimization
        if (this == other)
          return true;
        if (!(other instanceof State))
          return false;
        State otherA = (State) other;
        return ((new Integer(this.identifier)).equals(new Integer(otherA.identifier)));
      }

}
