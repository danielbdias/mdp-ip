package mdp;


import java.util.HashMap;

public class SuccProbabilitiesM { 
	private HashMap<State, Double>  nextStatesProbs;
		
	public SuccProbabilitiesM(){
		nextStatesProbs=new HashMap<State, Double>();
	}
	public  HashMap<State, Double> getNextStatesProbs(){
		return this.nextStatesProbs;
	}
	
	public String toString(){
		return "numNextStates:"+nextStatesProbs.size()+" "+nextStatesProbs.toString();
	}
}
